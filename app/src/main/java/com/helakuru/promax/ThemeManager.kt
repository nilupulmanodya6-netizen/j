package com.helakuru.promax

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import androidx.core.content.ContextCompat

/**
 * \uD83C\uDFA8 Theme Manager - 4 Premium Themes
 * Dark, Light, Amoled, Helakuru Blue
 */
class ThemeManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("helakuru_themes", Context.MODE_PRIVATE)

    enum class Theme {
        DARK,
        LIGHT,
        AMOLED,
        HELAKURU_BLUE
    }

    var currentTheme: Theme
        get() {
            val name = prefs.getString("current_theme", Theme.DARK.name) ?: Theme.DARK.name
            return try { Theme.valueOf(name) } catch (e: Exception) { Theme.DARK }
        }
        set(value) {
            prefs.edit().putString("current_theme", value.name).apply()
        }

    data class ThemeColors(
        val background: Int,
        val keyBackground: Int,
        val keyText: Int,
        val specialKeyBackground: Int,
        val suggestionBackground: Int,
        val accent: Int,
        val translationBar: Int
    )

    fun getColors(theme: Theme): ThemeColors {
        return when (theme) {
            Theme.DARK -> ThemeColors(
                background = 0xFF121212.toInt(),
                keyBackground = 0xFF2C2C2E.toInt(),
                keyText = 0xFFFFFFFF.toInt(),
                specialKeyBackground = 0xFF3A3A3C.toInt(),
                suggestionBackground = 0xFF1C1C1E.toInt(),
                accent = 0xFF0A84FF.toInt(),
                translationBar = 0xFF1E3A5F.toInt()
            )
            Theme.LIGHT -> ThemeColors(
                background = 0xFFF2F2F7.toInt(),
                keyBackground = 0xFFFFFFFF.toInt(),
                keyText = 0xFF000000.toInt(),
                specialKeyBackground = 0xFFE5E5EA.toInt(),
                suggestionBackground = 0xFFFFFFFF.toInt(),
                accent = 0xFF007AFF.toInt(),
                translationBar = 0xFFE3F2FD.toInt()
            )
            Theme.AMOLED -> ThemeColors(
                background = 0xFF000000.toInt(),
                keyBackground = 0xFF121212.toInt(),
                keyText = 0xFFFFFFFF.toInt(),
                specialKeyBackground = 0xFF1E1E1E.toInt(),
                suggestionBackground = 0xFF000000.toInt(),
                accent = 0xFF00FF88.toInt(),
                translationBar = 0xFF0A1F0A.toInt()
            )
            Theme.HELAKURU_BLUE -> ThemeColors(
                background = 0xFF0A192F.toInt(),
                keyBackground = 0xFF112240.toInt(),
                keyText = 0xFFE6F1FF.toInt(),
                specialKeyBackground = 0xFF1D3A5F.toInt(),
                suggestionBackground = 0xFF0A192F.toInt(),
                accent = 0xFF64FFDA.toInt(),
                translationBar = 0xFF102A43.toInt()
            )
        }
    }

    fun applyTheme(rootView: View, theme: Theme) {
        val colors = getColors(theme)
        rootView.setBackgroundColor(colors.background)
        
        // Recursively apply to keyboard container etc
        // In real app, we'd iterate through keys and set background
        // Simplified for demo
    }

    fun cycleTheme(): Theme {
        currentTheme = when (currentTheme) {
            Theme.DARK -> Theme.LIGHT
            Theme.LIGHT -> Theme.AMOLED
            Theme.AMOLED -> Theme.HELAKURU_BLUE
            Theme.HELAKURU_BLUE -> Theme.DARK
        }
        return currentTheme
    }

    fun getThemeDisplayName(theme: Theme): String {
        return when (theme) {
            Theme.DARK -> "\uD83C\uDF19 Dark Pro"
            Theme.LIGHT -> "☀️ Light"
            Theme.AMOLED -> "⚫ Amoled Pure"
            Theme.HELAKURU_BLUE -> "\uD83D\uDC99 Helakuru Blue"
        }
    }
}
