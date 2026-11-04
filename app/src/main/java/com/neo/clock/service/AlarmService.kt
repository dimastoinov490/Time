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
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.neo.clock.R
import com.neo.clock.audio.SoundEngine
import com.neo.clock.data.local.AppDatabase
import com.neo.clock.receiver.AlarmReceiver
import com.neo.clock.ui.ring.AlarmRingActivity
import com.neo.clock.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmService : Service() {

    companion object {
        const val ACTION_START_ALARM = "com.neo.clock.ACTION_START_ALARM"
        const val ACTION_DISMISS_ALARM = "com.neo.clock.ACTION_DISMISS_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.neo.clock.ACTION_SNOOZE_ALARM"
        const val CHANNEL_ID_ALARM = "alarm_channel_high"
        const val NOTIFICATION_ID = 1001
        private const val AUTO_DISMISS_DELAY_MS = 10 * 60 * 1000L // 10 minutes
    }

    private var currentAlarmId: Long = -1L
    private var currentAlarmLabel: String = "Будильник"
    private var currentAlarmSound: String = "birds"
    private var currentAlarmVibrate: Boolean = true

    private var vibrator: Vibrator? = null
    private val handler = Handler(Looper.getMainLooper())
    private val autoDismissRunnable = Runnable { dismissAlarm() }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DISMISS_ALARM -> dismissAlarm()
            ACTION_SNOOZE_ALARM -> snoozeAlarm()
            ACTION_START_ALARM, null -> {
                currentAlarmId = intent?.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, -1L) ?: -1L
                currentAlarmLabel = intent?.getStringExtra(AlarmReceiver.EXTRA_ALARM_LABEL) ?: "Будильник"
                currentAlarmSound = intent?.getStringExtra(AlarmReceiver.EXTRA_ALARM_SOUND) ?: "birds"
                currentAlarmVibrate = intent?.getBooleanExtra(AlarmReceiver.EXTRA_ALARM_VIBRATE, true) ?: true

                startForeground(NOTIFICATION_ID, buildAlarmNotification())
                startAudioAndVibration()

                handler.removeCallbacks(autoDismissRunnable)
                handler.postDelayed(autoDismissRunnable, AUTO_DISMISS_DELAY_MS)
            }
        }
        return START_NOT_STICKY
    }

    private fun startAudioAndVibration() {
        SoundEngine.play(this, currentAlarmSound, loop = true, rampVolume = true)

        if (currentAlarmVibrate) {
            val pattern = longArrayOf(0, 500, 250, 500, 250)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        }
    }

    private fun dismissAlarm() {
        stopAudioAndVibration()
        handler.removeCallbacks(autoDismissRunnable)

        // If one-off alarm, set enabled = false. If recurring, recalculate next schedule.
        if (currentAlarmId != -1L) {
            val dao = AppDatabase.getInstance(applicationContext).alarmDao()
            CoroutineScope(Dispatchers.IO).launch {
                val alarm = dao.getAlarmById(currentAlarmId)
                if (alarm != null) {
                    if (alarm.isRepeating()) {
                        AlarmScheduler.schedule(applicationContext, alarm)
                    } else {
                        dao.updateAlarm(alarm.copy(enabled = false))
                    }
                }
            }
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun snoozeAlarm() {
        stopAudioAndVibration()
        handler.removeCallbacks(autoDismissRunnable)

        // Reschedule alarm in 5 minutes
        val now = java.time.ZonedDateTime.now().plusMinutes(5)
        val tempAlarm = com.neo.clock.data.local.AlarmEntity(
            id = if (currentAlarmId != -1L) currentAlarmId else 99999L,
            hour = now.hour,
            minute = now.minute,
            label = "$currentAlarmLabel (Отложено)",
            daysBitmask = 0,
            sound = currentAlarmSound,
            vibrate = currentAlarmVibrate,
            enabled = true
        )
        AlarmScheduler.schedule(applicationContext, tempAlarm)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopAudioAndVibration() {
        SoundEngine.stop()
        vibrator?.cancel()
    }

    private fun buildAlarmNotification(): Notification {
        val fullScreenIntent = Intent(this, AlarmRingActivity::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, currentAlarmId)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, currentAlarmLabel)
            putExtra(AlarmReceiver.EXTRA_ALARM_SOUND, currentAlarmSound)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_DISMISS_ALARM
        }
        val dismissPendingIntent = PendingIntent.getService(
            this,
            1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_SNOOZE_ALARM
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID_ALARM)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Будильник: $currentAlarmLabel")
            .setContentText("Нажмите, чтобы отключить или отложить")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(0, "Выключить", dismissPendingIntent)
            .addAction(0, "Ещё 5 мин", snoozePendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_ALARM,
                "Будильник",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления активного будильника"
                setSound(null, null) // Sound is played by AudioTrack/MediaPlayer
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopAudioAndVibration()
        handler.removeCallbacks(autoDismissRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
