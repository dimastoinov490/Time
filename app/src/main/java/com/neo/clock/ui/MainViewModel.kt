package com.neo.clock.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neo.clock.audio.SoundEngine
import com.neo.clock.data.local.AlarmEntity
import com.neo.clock.data.prefs.UserPreferences
import com.neo.clock.data.repository.AlarmRepository
import com.neo.clock.service.TimerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

data class WorldCityItem(
    val name: String,
    val timezoneId: String
)

data class MainUiState(
    val currentTime: ZonedDateTime = ZonedDateTime.now(),
    val is12Hour: Boolean = false,
    val isBlinkOn: Boolean = true,
    val cities: List<WorldCityItem> = emptyList(),
    val alarms: List<AlarmEntity> = emptyList(),

    // Timer
    val timerTotalSeconds: Long = 300L,
    val timerRemainingSeconds: Long = 300L,
    val isTimerRunning: Boolean = false,
    val timerSound: String = "builtin:crystal",

    // Custom Sounds & Audio Settings
    val customSounds: List<com.neo.clock.audio.CustomSound> = emptyList(),
    val isRampVolumeEnabled: Boolean = true,

    // Preview
    val previewingSoundId: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AlarmRepository(application)
    private val prefs = UserPreferences(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var timeTickerJob: Job? = null
    private var localTimerJob: Job? = null

    init {
        startTimeTicker()
        observePreferences()
        observeAlarms()
    }

    private fun startTimeTicker() {
        timeTickerJob = viewModelScope.launch {
            var blinkToggle = true
            while (true) {
                delay(500L)
                blinkToggle = !blinkToggle
                _uiState.update {
                    it.copy(
                        currentTime = ZonedDateTime.now(),
                        isBlinkOn = blinkToggle
                    )
                }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            prefs.is12HourFormat.collect { is12 ->
                _uiState.update { it.copy(is12Hour = is12) }
            }
        }
        viewModelScope.launch {
            prefs.timerSound.collect { sound ->
                _uiState.update { it.copy(timerSound = sound) }
            }
        }
        viewModelScope.launch {
            prefs.selectedCities.collect { joined ->
                val list = joined.split(",").mapNotNull { entry ->
                    val parts = entry.split(":")
                    if (parts.size == 2) WorldCityItem(parts[0], parts[1]) else null
                }
                _uiState.update { it.copy(cities = list) }
            }
        }
        viewModelScope.launch {
            prefs.customSounds.collect { customList ->
                _uiState.update { it.copy(customSounds = customList) }
            }
        }
        viewModelScope.launch {
            prefs.isRampVolumeEnabled.collect { ramp ->
                _uiState.update { it.copy(isRampVolumeEnabled = ramp) }
            }
        }
    }

    private fun observeAlarms() {
        viewModelScope.launch {
            repository.alarms.collect { list ->
                // If list is completely empty, populate with the 3 default alarms from alarm.html
                if (list.isEmpty()) {
                    repository.addAlarm(5, 0, "Подъём", 0, "builtin:dawn", true)
                    repository.addAlarm(12, 0, "Учёба", 0, "builtin:crystal", false)
                    repository.addAlarm(12, 5, "Спорт", 0, "builtin:pulse", false)
                } else {
                    _uiState.update { it.copy(alarms = list) }
                }
            }
        }
    }

    fun toggle12HourFormat() {
        viewModelScope.launch {
            val current = _uiState.value.is12Hour
            prefs.set12HourFormat(!current)
        }
    }

    fun setTimerSound(sound: String) {
        viewModelScope.launch {
            prefs.setTimerSound(sound)
        }
    }

    fun addCity(name: String, timezoneId: String) {
        viewModelScope.launch {
            val current = _uiState.value.cities.toMutableList()
            if (current.none { it.timezoneId == timezoneId }) {
                current.add(WorldCityItem(name, timezoneId))
                val joined = current.joinToString(",") { "${it.name}:${it.timezoneId}" }
                prefs.setSelectedCities(joined)
            }
        }
    }

    fun removeCity(timezoneId: String) {
        viewModelScope.launch {
            val current = _uiState.value.cities.filter { it.timezoneId != timezoneId }
            val joined = current.joinToString(",") { "${it.name}:${it.timezoneId}" }
            prefs.setSelectedCities(joined)
        }
    }

    // --- Alarms ---
    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val turningOn = !alarm.enabled
            repository.toggleAlarm(alarm)
            if (turningOn) {
                showTimeUntilAlarmToast(alarm.hour, alarm.minute, alarm.daysBitmask)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
        }
    }

    fun createAlarm(
        hour: Int,
        minute: Int,
        label: String,
        daysBitmask: Int,
        sound: String,
        vibrate: Boolean
    ) {
        viewModelScope.launch {
            repository.addAlarm(hour, minute, label, daysBitmask, sound, vibrate)
            showTimeUntilAlarmToast(hour, minute, daysBitmask)
        }
    }

    private fun showTimeUntilAlarmToast(hour: Int, minute: Int, daysBitmask: Int) {
        try {
            val now = java.time.ZonedDateTime.now()
            var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
            if (!target.isAfter(now)) {
                target = target.plusDays(1)
            }
            if (daysBitmask != 0) {
                for (i in 0..7) {
                    val dayBit = target.dayOfWeek.value - 1
                    if ((daysBitmask and (1 shl dayBit)) != 0 && target.isAfter(now)) {
                        break
                    }
                    target = target.plusDays(1)
                }
            }
            val duration = java.time.Duration.between(now, target)
            val totalMinutes = duration.toMinutes().coerceAtLeast(1)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60

            val msg = when {
                hours > 0 && minutes > 0 -> "Будильник сработает через $hours ч $minutes мин"
                hours > 0 -> "Будильник сработает через $hours ч"
                else -> "Будильник сработает через $minutes мин"
            }

            android.widget.Toast.makeText(getApplication(), msg, android.widget.Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {}
    }

    fun updateAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.updateAlarm(alarm)
        }
    }

    // --- Sounds ---
    fun previewSound(soundId: String) {
        if (_uiState.value.previewingSoundId == soundId) {
            SoundEngine.stop()
            _uiState.update { it.copy(previewingSoundId = null) }
        } else {
            SoundEngine.play(getApplication(), soundId, loop = false, rampVolume = false)
            _uiState.update { it.copy(previewingSoundId = soundId) }
        }
    }

    fun stopSoundPreview() {
        SoundEngine.stop()
        _uiState.update { it.copy(previewingSoundId = null) }
    }

    fun addCustomSound(uriString: String, title: String) {
        viewModelScope.launch {
            prefs.addCustomSound(uriString, title)
        }
    }

    fun renameCustomSound(uriString: String, newTitle: String) {
        viewModelScope.launch {
            prefs.renameCustomSound(uriString, newTitle)
        }
    }

    fun deleteCustomSound(uriString: String) {
        viewModelScope.launch {
            prefs.deleteCustomSound(uriString)
        }
    }

    fun setRampVolumeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setRampVolumeEnabled(enabled)
        }
    }

    // --- Timer ---
    fun setTimerPreset(minutes: Int) {
        val totalSec = minutes * 60L
        localTimerJob?.cancel()
        _uiState.update {
            it.copy(
                timerTotalSeconds = totalSec,
                timerRemainingSeconds = totalSec,
                isTimerRunning = false
            )
        }
        val stopIntent = Intent(getApplication(), TimerService::class.java).apply {
            action = TimerService.ACTION_STOP_TIMER
        }
        getApplication<Application>().startService(stopIntent)
    }

    fun setCustomTimer(minutes: Int, seconds: Int) {
        val totalSec = minutes * 60L + seconds
        if (totalSec <= 0) return
        localTimerJob?.cancel()
        _uiState.update {
            it.copy(
                timerTotalSeconds = totalSec,
                timerRemainingSeconds = totalSec,
                isTimerRunning = false
            )
        }
    }

    fun toggleTimer() {
        val isRunning = _uiState.value.isTimerRunning
        val context = getApplication<Application>()

        if (isRunning) {
            // Pause
            localTimerJob?.cancel()
            _uiState.update { it.copy(isTimerRunning = false) }
            val intent = Intent(context, TimerService::class.java).apply {
                action = TimerService.ACTION_PAUSE_TIMER
            }
            context.startService(intent)
        } else {
            // Start
            var rem = _uiState.value.timerRemainingSeconds
            if (rem <= 0) {
                rem = _uiState.value.timerTotalSeconds
                _uiState.update { it.copy(timerRemainingSeconds = rem) }
            }
            _uiState.update { it.copy(isTimerRunning = true) }

            val intent = Intent(context, TimerService::class.java).apply {
                action = TimerService.ACTION_START_TIMER
                putExtra(TimerService.EXTRA_TOTAL_SECONDS, _uiState.value.timerTotalSeconds)
                putExtra(TimerService.EXTRA_REMAINING_SECONDS, rem)
                putExtra(TimerService.EXTRA_TIMER_SOUND, _uiState.value.timerSound)
            }
            context.startService(intent)

            localTimerJob = viewModelScope.launch {
                while (_uiState.value.isTimerRunning && _uiState.value.timerRemainingSeconds > 0) {
                    delay(1000L)
                    _uiState.update { state ->
                        val next = state.timerRemainingSeconds - 1
                        if (next <= 0) {
                            state.copy(
                                timerRemainingSeconds = state.timerTotalSeconds,
                                isTimerRunning = false
                            )
                        } else {
                            state.copy(timerRemainingSeconds = next)
                        }
                    }
                }
            }
        }
    }

    fun resetTimer() {
        localTimerJob?.cancel()
        val total = _uiState.value.timerTotalSeconds
        _uiState.update {
            it.copy(
                timerRemainingSeconds = total,
                isTimerRunning = false
            )
        }
        val context = getApplication<Application>()
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_STOP_TIMER
        }
        context.startService(intent)
    }

    override fun onCleared() {
        SoundEngine.stop()
        super.onCleared()
    }
}
