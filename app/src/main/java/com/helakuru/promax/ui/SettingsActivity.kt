package com.helakuru.promax.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 40, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Helakuru Pro Max\nහෙලකුරු Sinhala Keyboard"
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }

        val info = TextView(this).apply {
            text = "1. Enable the keyboard below.\n2. Select Helakuru Pro Max as your keyboard.\n3. Open WhatsApp, Messenger or another app and start typing."
            textSize = 16f
            setPadding(0, 0, 0, 24)
        }

        val enable = Button(this).apply {
            text = "Enable / Select Keyboard"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }

        val appSettings = Button(this).apply {
            text = "Open Keyboard Settings"
            setOnClickListener {
                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                startActivity(intent)
            }
        }

        root.addView(title, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        root.addView(info, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        root.addView(enable, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 16 })
        root.addView(appSettings, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        setContentView(root)
    }
}
