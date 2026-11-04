package com.neo.clock

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.neo.clock.service.AlarmService
import com.neo.clock.service.TimerService

class NeoClockApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // 1. Alarm channel
            val alarmChannel = NotificationChannel(
                AlarmService.CHANNEL_ID_ALARM,
                "Будильник",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Сигналы активного будильника"
                setSound(null, null) // Sound is played directly by AudioTrack/SoundEngine
                enableVibration(false)
            }

            // 2. Timer channel
            val timerChannel = NotificationChannel(
                TimerService.CHANNEL_ID_TIMER,
                "Таймер",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Отсчет времени таймера"
            }

            manager.createNotificationChannel(alarmChannel)
            manager.createNotificationChannel(timerChannel)
        }
    }
}
