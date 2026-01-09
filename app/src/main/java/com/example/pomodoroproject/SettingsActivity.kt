package com.example.pomodoroproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var etWorkTime: EditText
    private lateinit var etBreakTime: EditText
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        etWorkTime = findViewById(R.id.etWorkTime)
        etBreakTime = findViewById(R.id.etBreakTime)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)

        loadSavedSettings()

        btnSave.setOnClickListener {
            saveSettings()
        }
        btnBack.setOnClickListener {
            finish()
        }



        }

    private fun loadSavedSettings() {
        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)

        val workTime = prefs.getInt(MainActivity.KEY_WORK, 25)
        val breakTime = prefs.getInt(MainActivity.KEY_BREAK, 5)

        etWorkTime.setText(workTime.toString())
        etBreakTime.setText(breakTime.toString())
    }

    private fun saveSettings() {
        val workMinutes = etWorkTime.text.toString().toIntOrNull() ?: 25
        val breakMinutes = etBreakTime.text.toString().toIntOrNull() ?: 5

        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putInt(MainActivity.KEY_WORK, workMinutes)
            .putInt(MainActivity.KEY_BREAK, breakMinutes)
            .apply()

        finish() // vraća na MainActivity
    }

}
