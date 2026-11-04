package com.neo.clock.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.neo.clock.MainActivity
import com.neo.clock.data.local.AlarmEntity
import com.neo.clock.receiver.AlarmReceiver
import java.time.ZonedDateTime

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    fun schedule(context: Context, alarm: AlarmEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerMillis = calculateNextTrigger(alarm.hour, alarm.minute, alarm.daysBitmask)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_TRIGGER
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(AlarmReceiver.EXTRA_ALARM_SOUND, alarm.sound)
            putExtra(AlarmReceiver.EXTRA_ALARM_VIBRATE, alarm.vibrate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = PendingIntent.getActivity(
            context,
            alarm.id.toInt(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerMillis, showIntent),
                pendingIntent
            )
            Log.d(TAG, "Scheduled alarm ${alarm.id} for $triggerMillis")
        } catch (e: SecurityException) {
            Log.e(TAG, "Cannot schedule exact alarm without permission", e)
        }
    }

    fun cancel(context: Context, alarmId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm $alarmId")
        }
    }

    fun calculateNextTrigger(hour: Int, minute: Int, daysBitmask: Int): Long {
        val now = ZonedDateTime.now()
        if (daysBitmask == 0) {
            // One-off alarm
            var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
            if (!target.isAfter(now)) {
                target = target.plusDays(1)
            }
            return target.toInstant().toEpochMilli()
        } else {
            // Recurring alarm
            for (offset in 0..7) {
                val candidate = now.plusDays(offset.toLong()).withHour(hour).withMinute(minute).withSecond(0).withNano(0)
                val dayOfWeekVal = candidate.dayOfWeek.value // 1 = Mon, 7 = Sun
                val bit = dayOfWeekVal - 1
                val isMatch = (daysBitmask and (1 shl bit)) != 0
                if (isMatch && candidate.isAfter(now)) {
                    return candidate.toInstant().toEpochMilli()
                }
            }
            return now.plusDays(1).withHour(hour).withMinute(minute).withSecond(0).withNano(0).toInstant().toEpochMilli()
        }
    }
}
