package com.neo.clock.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "neoclock_settings")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_12_HOUR = booleanPreferencesKey("is_12_hour")
        val KEY_TIMER_SOUND = stringPreferencesKey("timer_sound")
        val KEY_CITIES = stringPreferencesKey("selected_cities_json")
        val KEY_TIMER_END_TIME = longPreferencesKey("timer_end_time")
        val KEY_TIMER_TOTAL_SEC = longPreferencesKey("timer_total_seconds")
        val KEY_TIMER_RUNNING = booleanPreferencesKey("timer_is_running")
    }

    val is12HourFormat: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_12_HOUR] ?: false
    }

    val timerSound: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_TIMER_SOUND] ?: "radar"
    }

    val selectedCities: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CITIES] ?: "Кишинёв:Europe/Chisinau,Бандунг:Asia/Jakarta,Лондон:Europe/London,Нью-Йорк:America/New_York,Токио:Asia/Tokyo,Дубай:Asia/Dubai"
    }

    val timerEndTime: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_TIMER_END_TIME] ?: 0L
    }

    val timerTotalSec: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_TIMER_TOTAL_SEC] ?: 300L
    }

    val timerRunning: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_TIMER_RUNNING] ?: false
    }

    suspend fun set12HourFormat(is12Hour: Boolean) {
        context.dataStore.edit { it[KEY_12_HOUR] = is12Hour }
    }

    suspend fun setTimerSound(sound: String) {
        context.dataStore.edit { it[KEY_TIMER_SOUND] = sound }
    }

    suspend fun setSelectedCities(citiesJoined: String) {
        context.dataStore.edit { it[KEY_CITIES] = citiesJoined }
    }

    suspend fun saveTimerState(endTime: Long, totalSec: Long, isRunning: Boolean) {
        context.dataStore.edit {
            it[KEY_TIMER_END_TIME] = endTime
            it[KEY_TIMER_TOTAL_SEC] = totalSec
            it[KEY_TIMER_RUNNING] = isRunning
        }
    }
}
