package com.example.pomodoroproject

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "pomodoro_prefs"
        const val KEY_WORK = "work_minutes"
        const val KEY_BREAK = "break_minutes"
    }

    private lateinit var tvTimer: TextView
    private lateinit var btnStartPause: Button
    private lateinit var btnReset: Button
    private lateinit var timerCard: CardView
    private lateinit var btnSettings: Button
    private lateinit var btnStats: Button
    private var isPaused = false

    private var countDownTimer: CountDownTimer? = null
    private var isRunning = false
    private var isWorkSession = true

    private var workTimeMillis = 25 * 60 * 1000L
    private var breakTimeMillis = 5 * 60 * 1000L
    private var timeLeftMillis = workTimeMillis


    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        tvTimer = findViewById(R.id.tvTimer)
        btnStartPause = findViewById(R.id.btnStartPause)
        btnReset = findViewById(R.id.btnReset)
        timerCard = findViewById(R.id.timerCard)
        btnSettings = findViewById(R.id.btnSettings)
        btnStats = findViewById(R.id.btnStats)

        updateTimerText()
        updateTimerColor()

        btnStartPause.setOnClickListener {
            if (isRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        btnReset.setOnClickListener {
            resetTimer()
        }
        btnReset.setOnLongClickListener {
            switchSession()
            true
        }
        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        btnStats.setOnClickListener {
            val intent = Intent(this, StatsActivity::class.java)
            startActivity(intent)
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }



    override fun onResume() {
        super.onResume()
        loadSettings()

        if (!isRunning) {
            timeLeftMillis = if (isWorkSession) workTimeMillis else breakTimeMillis
            updateTimerText()
            updateTimerColor()
        }
    }


    private fun loadSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        workTimeMillis = prefs.getInt(KEY_WORK, 25) * 60 * 1000L
        breakTimeMillis = prefs.getInt(KEY_BREAK, 5) * 60 * 1000L

    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                isRunning = false
                btnStartPause.text = "Start"
                timeLeftMillis = 0
                updateTimerText()

                if (isWorkSession) {
                    incrementCompletedSessions()
                }
            }

            private fun incrementCompletedSessions() {
                val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                val completed = prefs.getInt("completed_sessions", 0)
                prefs.edit()
                    .putInt("completed_sessions", completed + 1)
                    .apply()
            }
        }.start()

        isRunning = true
        btnStartPause.text = "Pause"
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isRunning = false
        btnStartPause.text = "Start"
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        isRunning = false



        timeLeftMillis = if (isWorkSession) {
            workTimeMillis
        } else {
            breakTimeMillis
        }

        btnStartPause.text = "Start"
        updateTimerText()
    }

    private fun switchSession() {
        countDownTimer?.cancel()
        isRunning = false



        isWorkSession = !isWorkSession
        timeLeftMillis = if (isWorkSession) workTimeMillis else breakTimeMillis

        updateTimerText()
        updateTimerColor()
        btnStartPause.text = "Start"
    }


    private fun updateTimerColor() {
        val colorRes = if (isWorkSession) {
            R.color.work_red
        } else {
            R.color.break_green
        }
        timerCard.setCardBackgroundColor(
            ContextCompat.getColor(this, colorRes)
        )
    }

    private fun updateTimerText() {
        val minutes = (timeLeftMillis / 1000) / 60
        val seconds = (timeLeftMillis / 1000) % 60
        tvTimer.text = String.format("%02d:%02d", minutes, seconds)
    }
}





