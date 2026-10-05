package com.helakuru.promax

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.KeyEvent
import android.speech.SpeechRecognizer
import android.content.ClipboardManager
import android.content.ClipData
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

/**
 * හෙලකුරු Pro Max - Main Keyboard Service
 * ලෝකයේ හොඳම සිංහල කීබෝඩ් එක
 * 
 * Features:
 * - Helakuru + Wijesekara + Singlish transliteration
 * - Auto Translate SI -> EN (ML Kit)
 * - Clipboard history, Emoji, Voice, Themes
 */
class SinhalaKeyboardService : InputMethodService() {

    // === Core Managers ===
    private lateinit var translatorManager: TranslatorManager
    private lateinit var themeManager: ThemeManager
    private lateinit var clipboardManager: ClipboardManager

    // === UI References ===
    private var keyboardContainer: LinearLayout? = null
    private var suggestionStrip: RecyclerView? = null
    private var translationPreview: LinearLayout? = null
    private var translationText: TextView? = null
    private var sendAsEnglishBtn: Button? = null
    private var translateChip: Chip? = null
    private var clipboardChip: Chip? = null
    private var emojiChip: Chip? = null

    // === State ===
    private var currentMode: KeyboardMode = KeyboardMode.SINHALA_HELAKURU
    private var isTranslateEnabled: Boolean = false
    private var isShiftOn: Boolean = false
    private var isSymbolsMode: Boolean = false
    private var composingBuffer: StringBuilder = StringBuilder()
    private var lastWord: String = ""
    
    private val clipboardHistory = mutableListOf<String>()
    private val suggestionAdapter = SuggestionAdapter()
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // Prediction Trie - next word prediction
    private val predictionTrie = PredictionTrie()

    enum class KeyboardMode {
        SINHALA_HELAKURU,
        SINHALA_WIJESEKARA,
        ENGLISH_QWERTY,
        SYMBOLS,
        EMOJI
    }

    // === Lifecycle ===
    override fun onCreate() {
        super.onCreate()
        translatorManager = TranslatorManager(this)
        themeManager = ThemeManager(this)
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        
        // Preload ML Kit model check
        translatorManager.checkAndDownloadModel()

        // Load prediction dictionary
        loadSinhalaDictionary()
        
        // Clipboard listener
        clipboardManager.addPrimaryClipChangedListener {
            val clip = clipboardManager.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString()
                text?.let { addToClipboardHistory(it) }
            }
        }
    }

    override fun onCreateInputView(): View {
        // Inflate keyboard_view.xml
        val view = layoutInflater.inflate(R.layout.keyboard_view, null)
        
        keyboardContainer = view.findViewById(R.id.keyboard_container)
        suggestionStrip = view.findViewById(R.id.suggestion_strip)
        translationPreview = view.findViewById(R.id.translation_preview_bar)
        translationText = view.findViewById(R.id.tv_translation_preview)
        sendAsEnglishBtn = view.findViewById(R.id.btn_send_as_english)
        translateChip = view.findViewById(R.id.chip_translate)
        clipboardChip = view.findViewById(R.id.chip_clipboard)
        emojiChip = view.findViewById(R.id.chip_emoji)

        setupSuggestionStrip()
        setupTopBar()
        renderKeyboard()

        // Apply theme
        themeManager.applyTheme(view, themeManager.currentTheme)

        return view
    }

    override fun onStartInput(info: EditorInfo?, restarting: Boolean) {
        super.onStartInput(info, restarting)
        composingBuffer.clear()
        updateSuggestions("")
    }

    // === Top Bar Setup ===
    private fun setupTopBar() {
        translateChip?.setOnClickListener {
            isTranslateEnabled = !isTranslateEnabled
            it.isSelected = isTranslateEnabled
            translateChip?.text = if (isTranslateEnabled) "\uD83C\uDF10 EN ON" else "\uD83C\uDF10 EN OFF"
            
            if (isTranslateEnabled) {
                Toast.makeText(this, "Auto Translate ON - සිංහල -> English", Toast.LENGTH_SHORT).show()
                triggerTranslation()
            } else {
                translationPreview?.visibility = View.GONE
            }
        }

        clipboardChip?.setOnClickListener {
            showClipboardHistory()
        }

        emojiChip?.setOnClickListener {
            currentMode = if (currentMode == KeyboardMode.EMOJI) KeyboardMode.SINHALA_HELAKURU else KeyboardMode.EMOJI
            renderKeyboard()
        }

        sendAsEnglishBtn?.setOnClickListener {
            // SEND AS ENGLISH - critical feature
            val translated = translationText?.text?.toString()?.replace("EN: ", "") ?: ""
            if (translated.isNotEmpty()) {
                // Delete current Sinhala
                val ic = currentInputConnection
                // Rough delete of composing
                ic?.deleteSurroundingText(composingBuffer.length + lastWord.length, 0)
                ic?.commitText(translated, 1)
                composingBuffer.clear()
                translationPreview?.visibility = View.GONE
            }
        }
    }

    // === Keyboard Rendering ===
    private fun renderKeyboard() {
        keyboardContainer?.removeAllViews()
        val layout = when (currentMode) {
            KeyboardMode.SINHALA_HELAKURU -> KeyboardLayout.HELAKURU_LAYOUT
            KeyboardMode.SINHALA_WIJESEKARA -> KeyboardLayout.WIJESEKARA_LAYOUT
            KeyboardMode.ENGLISH_QWERTY -> KeyboardLayout.ENGLISH_QWERTY
            KeyboardMode.SYMBOLS -> KeyboardLayout.SYMBOLS_LAYOUT
            KeyboardMode.EMOJI -> KeyboardLayout.EMOJI_LAYOUT
        }

        for (row in layout) {
            val rowView = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0, 1f
                )
                weightSum = row.size.toFloat()
            }

            for (key in row) {
                val keyView = createKeyView(key)
                rowView.addView(keyView)
            }
            keyboardContainer?.addView(rowView)
        }

        // Bottom toolbar - space, enter, etc
        addBottomToolbar()
    }

    private fun createKeyView(key: KeyboardLayout.Key): View {
        val button = Button(this).apply {
            text = key.label
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                setMargins(3, 3, 3, 3)
            }
            isAllCaps = false
            textSize = if (key.isSpecial) 12f else 16f
            // Theme will override background
        }

        button.setOnClickListener {
            handleKeyPress(key)
            // Haptic feedback
            it.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
        }

        // Long press for symbols / alternate chars
        button.setOnLongClickListener {
            showLongPressOptions(key)
            true
        }

        // Swipe to delete detection would be on container
        return button
    }

    private fun addBottomToolbar() {
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f
            )
        }

        // Symbols toggle
        val symBtn = Button(this).apply {
            text = "123"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            setOnClickListener {
                isSymbolsMode = !isSymbolsMode
                currentMode = if (isSymbolsMode) KeyboardMode.SYMBOLS else KeyboardMode.SINHALA_HELAKURU
                renderKeyboard()
            }
        }

        // Space - double tap for period
        var lastSpaceTap = 0L
        val spaceBtn = Button(this).apply {
            text = "Space"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3f)
            setOnClickListener {
                val now = System.currentTimeMillis()
                if (now - lastSpaceTap < 300) {
                    // Double tap -> period
                    handleKeyPress(KeyboardLayout.Key(".", -1, true))
                    handleKeyPress(KeyboardLayout.Key(" ", 32, true))
                } else {
                    handleKeyPress(KeyboardLayout.Key(" ", 32, true))
                }
                lastSpaceTap = now
            }
        }

        // Delete with swipe
        val delBtn = Button(this).apply {
            text = "⌫"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            setOnClickListener { handleDelete() }
            setOnLongClickListener { 
                // Clear all
                currentInputConnection?.deleteSurroundingText(100, 0)
                composingBuffer.clear()
                true 
            }
        }

        // Enter
        val enterBtn = Button(this).apply {
            text = "⏎"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            setOnClickListener {
                currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
        }

        // Voice typing button
        val voiceBtn = Button(this).apply {
            text = "\uD83C\uDFA4"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            setOnClickListener { startVoiceInput() }
        }

        toolbar.addView(symBtn)
        toolbar.addView(spaceBtn)
        toolbar.addView(delBtn)
        toolbar.addView(enterBtn)
        toolbar.addView(voiceBtn)

        keyboardContainer?.addView(toolbar)
    }

    // === Key Handling - CORE LOGIC ===
    private fun handleKeyPress(key: KeyboardLayout.Key) {
        val ic = currentInputConnection ?: return

        when (key.code) {
            -1 -> { // Shift
                isShiftOn = !isShiftOn
                renderKeyboard()
            }
            -2 -> { // Language switch
                currentMode = when (currentMode) {
                    KeyboardMode.SINHALA_HELAKURU -> KeyboardMode.ENGLISH_QWERTY
                    KeyboardMode.ENGLISH_QWERTY -> KeyboardMode.SINHALA_HELAKURU
                    else -> KeyboardMode.SINHALA_HELAKURU
                }
                renderKeyboard()
            }
            -5 -> { // Delete handled separately
                handleDelete()
                return
            }
            else -> {
                val charToCommit = if (isShiftOn && key.label.length == 1) key.label.uppercase() else key.label
                
                // Singlish transliteration check
                if (currentMode == KeyboardMode.SINHALA_HELAKURU && charToCommit.matches(Regex("[a-zA-Z]+"))) {
                    composingBuffer.append(charToCommit)
                    val transliterated = transliterateSinglish(composingBuffer.toString())
                    // Show as composing
                    ic.setComposingText(transliterated, 1)
                    updateSuggestions(composingBuffer.toString())
                    
                    // If ends with space or punctuation, commit
                    if (charToCommit == " ") {
                        ic.commitText(transliterated + " ", 1)
                        lastWord = transliterated
                        composingBuffer.clear()
                        if (isTranslateEnabled) triggerTranslation()
                    }
                } else {
                    // Direct commit - Helakuru chars
                    if (composingBuffer.isNotEmpty()) {
                        val finalSi = transliterateSinglish(composingBuffer.toString())
                        ic.commitText(finalSi, 1)
                        lastWord = finalSi
                        composingBuffer.clear()
                    }
                    
                    // Handle Sinhala conjuncts - auto add ් if needed
                    val processed = processSinhalaConjunct(charToCommit)
                    ic.commitText(processed, 1)
                    
                    if (charToCommit == " ") {
                        lastWord = ""
                        if (isTranslateEnabled) triggerTranslation()
                    }
                    
                    updateSuggestions(processed)
                }
            }
        }

        if (isShiftOn && key.code != -1) {
            isShiftOn = false
            renderKeyboard()
        }
    }

    private fun handleDelete() {
        val ic = currentInputConnection ?: return
        if (composingBuffer.isNotEmpty()) {
            composingBuffer.deleteCharAt(composingBuffer.length - 1)
            val transliterated = if (composingBuffer.isNotEmpty()) 
                transliterateSinglish(composingBuffer.toString()) else ""
            ic.setComposingText(transliterated, 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    // === Singlish -> Sinhala Transliteration ===
    // හෙලකුරු වගේම logic
    private fun transliterateSinglish(input: String): String {
        var text = input.lowercase()
        
        // Dictionary for common words - Oya -> ඔයා etc
        val commonDict = mapOf(
            "oya" to "ඔයා",
            "kohomada" to "කොහොමද",
            "mokakda" to "මොකක්ද",
            "kiyala" to "කියලා",
            "mata" to "මට",
            "mama" to "මම",
            "oyata" to "ඔයාට",
            "api" to "අපි",
            "ethakota" to "එතකොට",
            "hari" to "හරි",
            "na" to "නෑ",
            "ehema" to "එහෙම",
            "kawada" to "කවදා",
            "aayubowan" to "ආයුබෝවන්",
            "stuti" to "ස්තූතියි",
            "wade" to "වැඩේ"
        )
        
        commonDict[input.lowercase()]?.let { return it }

        // Letter-by-letter transliteration
        val singlishMap = mapOf(
            "a" to "අ", "aa" to "ආ", "ae" to "ඇ", "aee" to "ඈ",
            "i" to "ඉ", "ii" to "ඊ", "u" to "උ", "uu" to "ඌ",
            "e" to "එ", "ee" to "ඒ", "o" to "ඔ", "oo" to "ඕ",
            "ka" to "ක", "ga" to "ග", "cha" to "ච", "ja" to "ජ",
            "ta" to "ට", "da" to "ඩ", "na" to "න", "pa" to "ප",
            "ba" to "බ", "ma" to "ම", "ya" to "ය", "ra" to "ර",
            "la" to "ල", "va" to "ව", "sa" to "ස", "ha" to "හ",
            "sha" to "ශ", "kha" to "ඛ", "gha" to "ඝ", "tha" to "ථ",
            "dha" to "ධ", "pha" to "ඵ", "bha" to "භ"
        )

        // Simple longest-match replacement
        var result = text
        val sortedKeys = singlishMap.keys.sortedByDescending { it.length }
        for (k in sortedKeys) {
            result = result.replace(k, singlishMap[k]!!)
        }
        
        return result.ifEmpty { text }
    }

    private fun processSinhalaConjunct(char: String): String {
        // Auto handle ් (virama) for conjuncts like ක්‍ර
        // This is simplified - real Helakuru has complex logic
        return char
    }

    // === Auto Translate Feature ===
    private fun triggerTranslation() {
        val ic = currentInputConnection ?: return
        val extracted = ic.getTextBeforeCursor(100, 0)?.toString() ?: ""
        if (extracted.trim().length < 2) return

        // Get last sentence
        val sentences = extracted.split(Regex("[.!?\\n]"))
        val lastSentence = sentences.lastOrNull()?.trim() ?: return
        if (lastSentence.isEmpty()) return

        translationPreview?.visibility = View.VISIBLE
        translationText?.text = "Translating..."

        translatorManager.translateSiToEn(lastSentence) { result ->
            mainHandler.post {
                if (result.isSuccess) {
                    translationText?.text = "EN: ${result.getOrNull()}"
                } else {
                    translationText?.text = "EN: (model downloading...)"
                    // Try download
                    translatorManager.downloadModel { }
                }
            }
        }
    }

    // === Predictions ===
    private fun loadSinhalaDictionary() {
        val commonWords = listOf(
            "ඔයා", "මම", "අපි", "ඔයාට", "මට", "එතකොට", "කොහොමද", "මොකක්ද",
            "හරි", "නෑ", "ඔව්", "ආයුබෝවන්", "ස්තූතියි", "කියලා", "වගේ", "එහෙම"
        )
        commonWords.forEach { predictionTrie.insert(it) }
    }

    private fun updateSuggestions(current: String) {
        val suggestions = if (current.isEmpty()) {
            listOf("ඔයා", "මම", "කොහොමද", "හරි")
        } else {
            predictionTrie.search(current).take(5)
        }
        suggestionAdapter.update(suggestions) { word ->
            // On suggestion click
            val ic = currentInputConnection
            ic?.commitText(word + " ", 1)
            composingBuffer.clear()
            if (isTranslateEnabled) triggerTranslation()
        }
    }

    private fun setupSuggestionStrip() {
        suggestionStrip?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        suggestionStrip?.adapter = suggestionAdapter
    }

    // === Clipboard ===
    private fun addToClipboardHistory(text: String) {
        if (text.length > 500) return
        clipboardHistory.remove(text)
        clipboardHistory.add(0, text)
        if (clipboardHistory.size > 5) clipboardHistory.removeAt(5)
    }

    private fun showClipboardHistory() {
        // Show popup with last 5 clips
        Toast.makeText(this, "Clipboard: ${clipboardHistory.take(3)}", Toast.LENGTH_LONG).show()
    }

    // === Voice ===
    private fun startVoiceInput() {
        // Launch SpeechRecognizer intent
        Toast.makeText(this, "\uD83C\uDFA4 Voice typing - Coming soon: Sinhala voice", Toast.LENGTH_SHORT).show()
    }

    // === Long press symbols ===
    private fun showLongPressOptions(key: KeyboardLayout.Key) {
        val options = KeyboardLayout.getLongPressOptions(key.label)
        if (options.isNotEmpty()) {
            // Show popup - simplified
            Toast.makeText(this, options.joinToString(" "), Toast.LENGTH_SHORT).show()
        }
    }

    // === Helper Classes ===
    class PredictionTrie {
        private val root = TrieNode()
        
        fun insert(word: String) {
            var node = root
            for (c in word) {
                node = node.children.getOrPut(c) { TrieNode() }
            }
            node.isEnd = true
            node.word = word
        }

        fun search(prefix: String): List<String> {
            var node = root
            for (c in prefix) {
                node = node.children[c] ?: return emptyList()
            }
            return collectWords(node).take(10)
        }

        private fun collectWords(node: TrieNode): List<String> {
            val result = mutableListOf<String>()
            if (node.isEnd) node.word?.let { result.add(it) }
            for (child in node.children.values) {
                result.addAll(collectWords(child))
            }
            return result
        }

        data class TrieNode(
            val children: MutableMap<Char, TrieNode> = mutableMapOf(),
            var isEnd: Boolean = false,
            var word: String? = null
        )
    }

    class SuggestionAdapter : RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {
        private var suggestions = listOf<String>()
        private var onClick: ((String) -> Unit)? = null

        fun update(new: List<String>, click: (String) -> Unit) {
            suggestions = new
            onClick = click
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val tv = TextView(parent.context).apply {
                setPadding(32, 16, 32, 16)
                textSize = 14f
            }
            return ViewHolder(tv)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(suggestions[position])
        }

        override fun getItemCount() = suggestions.size

        inner class ViewHolder(private val tv: TextView) : RecyclerView.ViewHolder(tv) {
            fun bind(word: String) {
                tv.text = word
                tv.setOnClickListener { onClick?.invoke(word) }
            }
        }
    }
}
