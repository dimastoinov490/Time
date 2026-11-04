package com.neo.clock.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val hour: Int,
    val minute: Int,
    val label: String,
    val daysBitmask: Int, // Bit 0: Mon, Bit 1: Tue, ..., Bit 6: Sun. 0 = Once
    val sound: String,
    val vibrate: Boolean = true,
    val enabled: Boolean = true
) {
    fun isDayEnabled(dayOfWeek: Int): Boolean { // 1 = Monday .. 7 = Sunday
        val bit = dayOfWeek - 1
        return (daysBitmask and (1 shl bit)) != 0
    }

    fun isRepeating(): Boolean = daysBitmask > 0
}
