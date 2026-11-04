package com.neo.clock.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.neo.clock.audio.CustomSound
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "neoclock_settings")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_12_HOUR = booleanPreferencesKey("is_12_hour")
        val KEY_TIMER_SOUND = stringPreferencesKey("timer_sound")
        val KEY_CITIES = stringPreferencesKey("selected_cities_json")
        val KEY_TIMER_END_TIME = longPreferencesKey("timer_end_time")
        val KEY_TIMER_TOTAL_SEC = longPreferencesKey("timer_total_seconds")
        val KEY_TIMER_RUNNING = booleanPreferencesKey("timer_is_running")
        val KEY_CUSTOM_SOUNDS = stringPreferencesKey("custom_sounds_json")
        val KEY_RAMP_VOLUME = booleanPreferencesKey("ramp_volume_enabled")
    }

    val is12HourFormat: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_12_HOUR] ?: false
    }

    val timerSound: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_TIMER_SOUND] ?: "builtin:crystal"
    }

    val isRampVolumeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_RAMP_VOLUME] ?: true
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

    val customSounds: Flow<List<CustomSound>> = context.dataStore.data.map { prefs ->
        val rawJson = prefs[KEY_CUSTOM_SOUNDS] ?: "[]"
        parseCustomSounds(rawJson)
    }

    suspend fun set12HourFormat(is12Hour: Boolean) {
        context.dataStore.edit { it[KEY_12_HOUR] = is12Hour }
    }

    suspend fun setTimerSound(sound: String) {
        context.dataStore.edit { it[KEY_TIMER_SOUND] = sound }
    }

    suspend fun setRampVolumeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_RAMP_VOLUME] = enabled }
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

    suspend fun addCustomSound(uriString: String, title: String) {
        context.dataStore.edit { prefs ->
            val list = parseCustomSounds(prefs[KEY_CUSTOM_SOUNDS] ?: "[]").toMutableList()
            // If already exists with this URI, update title; otherwise add
            val idx = list.indexOfFirst { it.uriString == uriString }
            if (idx >= 0) {
                list[idx] = list[idx].copy(title = title)
            } else {
                list.add(0, CustomSound(uriString = uriString, title = title))
            }
            prefs[KEY_CUSTOM_SOUNDS] = serializeCustomSounds(list)
        }
    }

    suspend fun renameCustomSound(uriString: String, newTitle: String) {
        context.dataStore.edit { prefs ->
            val list = parseCustomSounds(prefs[KEY_CUSTOM_SOUNDS] ?: "[]").map {
                if (it.uriString == uriString) it.copy(title = newTitle) else it
            }
            prefs[KEY_CUSTOM_SOUNDS] = serializeCustomSounds(list)
        }
    }

    suspend fun deleteCustomSound(uriString: String) {
        context.dataStore.edit { prefs ->
            val list = parseCustomSounds(prefs[KEY_CUSTOM_SOUNDS] ?: "[]").filter {
                it.uriString != uriString
            }
            prefs[KEY_CUSTOM_SOUNDS] = serializeCustomSounds(list)
        }
    }

    private fun parseCustomSounds(json: String): List<CustomSound> {
        val result = mutableListOf<CustomSound>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    CustomSound(
                        uriString = obj.optString("uri", ""),
                        title = obj.optString("title", "Моя мелодия"),
                        addedAt = obj.optLong("addedAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return result
    }

    private fun serializeCustomSounds(sounds: List<CustomSound>): String {
        val array = JSONArray()
        for (s in sounds) {
            val obj = JSONObject()
            obj.put("uri", s.uriString)
            obj.put("title", s.title)
            obj.put("addedAt", s.addedAt)
            array.put(obj)
        }
        return array.toString()
    }
}
