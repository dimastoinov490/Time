package com.neo.clock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.neo.clock.service.AlarmService

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_ALARM_TRIGGER = "com.neo.clock.ACTION_ALARM_TRIGGER"
        const val EXTRA_ALARM_ID = "ALARM_ID"
        const val EXTRA_ALARM_LABEL = "ALARM_LABEL"
        const val EXTRA_ALARM_SOUND = "ALARM_SOUND"
        const val EXTRA_ALARM_VIBRATE = "ALARM_VIBRATE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "NeoClock::AlarmReceiverWakeLock"
        )
        wakeLock.acquire(10000L) // 10s safety timeout

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_START_ALARM
            putExtra(EXTRA_ALARM_ID, intent.getLongExtra(EXTRA_ALARM_ID, -1L))
            putExtra(EXTRA_ALARM_LABEL, intent.getStringExtra(EXTRA_ALARM_LABEL) ?: "Будильник")
            putExtra(EXTRA_ALARM_SOUND, intent.getStringExtra(EXTRA_ALARM_SOUND) ?: "birds")
            putExtra(EXTRA_ALARM_VIBRATE, intent.getBooleanExtra(EXTRA_ALARM_VIBRATE, true))
        }

        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
