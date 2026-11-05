package com.neo.clock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.neo.clock.MainActivity
import com.neo.clock.R
import com.neo.clock.audio.SoundEngine

class TimerService : Service() {

    companion object {
        const val ACTION_START_TIMER = "com.neo.clock.ACTION_START_TIMER"
        const val ACTION_PAUSE_TIMER = "com.neo.clock.ACTION_PAUSE_TIMER"
        const val ACTION_STOP_TIMER = "com.neo.clock.ACTION_STOP_TIMER"

        const val EXTRA_TOTAL_SECONDS = "EXTRA_TOTAL_SECONDS"
        const val EXTRA_REMAINING_SECONDS = "EXTRA_REMAINING_SECONDS"
        const val EXTRA_TIMER_SOUND = "EXTRA_TIMER_SOUND"

        const val CHANNEL_ID_TIMER = "timer_channel_ongoing"
        const val NOTIFICATION_ID = 2002
    }

    private var totalSeconds: Long = 300L
    private var remainingSeconds: Long = 300L
    private var timerSound: String = "crystal"
    private var isRunning: Boolean = false

    private val handler = Handler(Looper.getMainLooper())
    private val tickRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                if (remainingSeconds > 0) {
                    remainingSeconds--
                    updateNotification()
                    handler.postDelayed(this, 1000L)
                } else {
                    onTimerFinished()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                totalSeconds = intent.getLongExtra(EXTRA_TOTAL_SECONDS, 300L)
                remainingSeconds = intent.getLongExtra(EXTRA_REMAINING_SECONDS, totalSeconds)
                timerSound = intent.getStringExtra(EXTRA_TIMER_SOUND) ?: "crystal"
                isRunning = true

                startForeground(NOTIFICATION_ID, buildTimerNotification())
                handler.removeCallbacks(tickRunnable)
                handler.post(tickRunnable)
            }
            ACTION_PAUSE_TIMER -> {
                isRunning = false
                handler.removeCallbacks(tickRunnable)
                updateNotification()
            }
            ACTION_STOP_TIMER -> {
                isRunning = false
                handler.removeCallbacks(tickRunnable)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun onTimerFinished() {
        isRunning = false
        handler.removeCallbacks(tickRunnable)

        // Play alarm sound and vibrate
        SoundEngine.play(this, timerSound, loop = false, rampVolume = false)
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000L, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(1000L)
        }

        val completedNotification = NotificationCompat.Builder(this, CHANNEL_ID_TIMER)
            .setSmallIcon(R.drawable.ic_notification_timer)
            .setLargeIcon(android.graphics.BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setContentTitle("Таймер завершён")
            .setContentText("Установленное время истекло")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID + 1, completedNotification)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildTimerNotification())
    }

    private fun buildTimerNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)

        val stopIntent = Intent(this, TimerService::class.java).apply { action = ACTION_STOP_TIMER }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIconBmp = try {
            android.graphics.BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        } catch (_: Exception) {
            null
        }

        return NotificationCompat.Builder(this, CHANNEL_ID_TIMER)
            .setSmallIcon(R.drawable.ic_notification_timer)
            .apply {
                if (largeIconBmp != null) {
                    setLargeIcon(largeIconBmp)
                }
            }
            .setContentTitle(if (isRunning) "Таймер: $timeFormatted" else "Таймер на паузе ($timeFormatted)")
            .setContentText("Нажмите, чтобы открыть приложение")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isRunning)
            .setContentIntent(openPendingIntent)
            .addAction(0, "Стоп", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_TIMER,
                "Таймер",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Уведомления обратного отсчета таймера"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        isRunning = false
        handler.removeCallbacks(tickRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
