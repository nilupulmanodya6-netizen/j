package com.helakuru.promax

import android.content.Context
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

/**
 * \uD83C\uDF10 Auto Translate Manager
 * Sinhala -> English using ML Kit on-device translation
 * 
 * Flow: User types "ඔයා කොහොමද" -> ML Kit -> "How are you?"
 * Model downloads once (~40MB) then works offline
 */
class TranslatorManager(private val context: Context) {

    private var siToEnTranslator: Translator? = null
    private var isModelReady: Boolean = false

    init {
        setupTranslator()
    }

    private fun setupTranslator() {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.SINHALA)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()

        siToEnTranslator = Translation.getClient(options)
    }

    fun checkAndDownloadModel() {
        val translator = siToEnTranslator ?: return
        
        // Check if model already downloaded
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                isModelReady = true
                println("✅ ML Kit SI->EN model ready")
            }
            .addOnFailureListener { e ->
                println("❌ Model download failed: ${e.message}")
                isModelReady = false
            }
    }

    fun downloadModel(onComplete: (Boolean) -> Unit) {
        val translator = siToEnTranslator ?: run {
            onComplete(false)
            return
        }

        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                isModelReady = true
                onComplete(true)
            }
            .addOnFailureListener {
                isModelReady = false
                onComplete(false)
            }
    }

    fun isModelDownloaded(callback: (Boolean) -> Unit) {
        // ML Kit doesn't expose direct check, we try translate small text
        callback(isModelReady)
    }

    /**
     * Main function: Sinhala -> English
     * Example: "ඔයා කොහොමද" -> "How are you"
     */
    fun translateSiToEn(siText: String, callback: (Result<String>) -> Unit) {
        if (siText.isBlank()) {
            callback(Result.success(""))
            return
        }

        val translator = siToEnTranslator ?: run {
            callback(Result.failure(Exception("Translator not initialized")))
            return
        }

        // Auto download if needed then translate
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                isModelReady = true
                translator.translate(siText)
                    .addOnSuccessListener { translated ->
                        callback(Result.success(translated))
                    }
                    .addOnFailureListener { e ->
                        callback(Result.failure(e))
                    }
            }
            .addOnFailureListener { e ->
                callback(Result.failure(e))
            }
    }

    /**
     * Optional: English -> Sinhala (for future)
     */
    fun translateEnToSi(enText: String, callback: (Result<String>) -> Unit) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.SINHALA)
            .build()
        val enSiTranslator = Translation.getClient(options)

        enSiTranslator.downloadModelIfNeeded()
            .addOnSuccessListener {
                enSiTranslator.translate(enText)
                    .addOnSuccessListener { callback(Result.success(it)) }
                    .addOnFailureListener { callback(Result.failure(it)) }
            }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun close() {
        siToEnTranslator?.close()
    }
}
