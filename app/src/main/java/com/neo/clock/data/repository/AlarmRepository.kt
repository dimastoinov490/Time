package com.neo.clock.data.repository

import android.content.Context
import com.neo.clock.data.local.AlarmDao
import com.neo.clock.data.local.AlarmEntity
import com.neo.clock.data.local.AppDatabase
import com.neo.clock.util.AlarmScheduler
import kotlinx.coroutines.flow.Flow

class AlarmRepository(private val context: Context) {
    private val dao: AlarmDao = AppDatabase.getInstance(context).alarmDao()

    val alarms: Flow<List<AlarmEntity>> = dao.getAllAlarms()

    suspend fun addAlarm(
        hour: Int,
        minute: Int,
        label: String,
        daysBitmask: Int,
        sound: String,
        vibrate: Boolean
    ): Long {
        val entity = AlarmEntity(
            hour = hour,
            minute = minute,
            label = label.ifBlank { "Будильник" },
            daysBitmask = daysBitmask,
            sound = sound,
            vibrate = vibrate,
            enabled = true
        )
        val id = dao.insertAlarm(entity)
        val created = entity.copy(id = id)
        AlarmScheduler.schedule(context, created)
        return id
    }

    suspend fun toggleAlarm(alarm: AlarmEntity) {
        val updated = alarm.copy(enabled = !alarm.enabled)
        dao.updateAlarm(updated)
        if (updated.enabled) {
            AlarmScheduler.schedule(context, updated)
        } else {
            AlarmScheduler.cancel(context, updated.id)
        }
    }

    suspend fun updateAlarm(alarm: AlarmEntity) {
        dao.updateAlarm(alarm)
        if (alarm.enabled) {
            AlarmScheduler.schedule(context, alarm)
        } else {
            AlarmScheduler.cancel(context, alarm.id)
        }
    }

    suspend fun deleteAlarm(alarm: AlarmEntity) {
        AlarmScheduler.cancel(context, alarm.id)
        dao.deleteAlarm(alarm)
    }

    suspend fun rescheduleAllActive() {
        val active = dao.getActiveAlarmsSync()
        active.forEach { AlarmScheduler.schedule(context, it) }
    }
}
