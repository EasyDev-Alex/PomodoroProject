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
    private lateinit var etLongBreakTime: EditText
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        etWorkTime = findViewById(R.id.etWorkTime)
        etBreakTime = findViewById(R.id.etBreakTime)
        etLongBreakTime = findViewById(R.id.etLongBreakTime)
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
        val longBreakTime = prefs.getInt(MainActivity.KEY_LONG_BREAK, 15)

        etWorkTime.setText(workTime.toString())
        etBreakTime.setText(breakTime.toString())
        etLongBreakTime.setText(longBreakTime.toString())
    }

    private fun saveSettings() {
        val workMinutes = etWorkTime.text.toString().toIntOrNull() ?: 25
        val breakMinutes = etBreakTime.text.toString().toIntOrNull() ?: 5
        val longBreakMinutes = etLongBreakTime.text.toString().toIntOrNull() ?: 15

        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putInt(MainActivity.KEY_WORK, workMinutes)
            .putInt(MainActivity.KEY_BREAK, breakMinutes)
            .putInt(MainActivity.KEY_LONG_BREAK, longBreakMinutes)
            .apply()

        finish() // vraća na MainActivity
    }

}
