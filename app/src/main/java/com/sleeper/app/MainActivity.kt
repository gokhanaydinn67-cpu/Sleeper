package com.sleeper.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView
    private lateinit var toggle: Button
    private lateinit var secondsInput: EditText

    private val prefs by lazy { getSharedPreferences("sleeper", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        status = findViewById(R.id.status)
        toggle = findViewById(R.id.toggle)
        secondsInput = findViewById(R.id.seconds)

        secondsInput.setText(prefs.getInt("seconds", 30).toString())

        secondsInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val v = s.toString().toIntOrNull()
                if (v != null && v > 0) {
                    prefs.edit().putInt("seconds", v).apply()
                    LockService.instance?.reschedule()
                }
            }
        })

        toggle.setOnClickListener {
            val now = !prefs.getBoolean("enabled", false)
            prefs.edit().putBoolean("enabled", now).apply()
            LockService.instance?.reschedule()
            refresh()
        }

        findViewById<Button>(R.id.accessBtn).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val on = prefs.getBoolean("enabled", false)
        status.text = if (on) "Açık" else "Kapalı"
        toggle.text = if (on) "Kapat" else "Aç"
    }
}
