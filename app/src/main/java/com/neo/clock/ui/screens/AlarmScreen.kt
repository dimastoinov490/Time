package com.neo.clock.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import com.neo.clock.ui.theme.NeuRed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neo.clock.audio.SoundEngine
import com.neo.clock.data.local.AlarmEntity
import com.neo.clock.ui.MainUiState
import com.neo.clock.ui.MainViewModel
import com.neo.clock.ui.components.NeumorphicTimePicker
import com.neo.clock.ui.modifiers.neuClickable
import com.neo.clock.ui.modifiers.neuRaised
import com.neo.clock.ui.modifiers.neuSunk
import com.neo.clock.ui.theme.NeuBg
import com.neo.clock.ui.theme.NeuBlue
import com.neo.clock.ui.theme.NeuBlue2
import com.neo.clock.ui.theme.NeuBlueDark
import com.neo.clock.ui.theme.NeuDarkShadow
import com.neo.clock.ui.theme.NeuLightShadow
import com.neo.clock.ui.theme.NeuMuted
import com.neo.clock.ui.theme.NeuText
import com.neo.clock.ui.theme.Poppins

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    state: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var showNewAlarmSheet by remember { mutableStateOf(false) }
    var editingSoundAlarm by remember { mutableStateOf<AlarmEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Будильник",
                style = TextStyle(
                    fontFamily = Poppins,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.04.sp,
                    color = NeuText
                )
            )
            Text(
                text = "${state.alarms.count { it.enabled }} акт.",
                style = TextStyle(
                    fontFamily = Poppins,
                    fontSize = 13.sp,
                    color = NeuMuted
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Alarms List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (state.alarms.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Нет будильников",
                            style = TextStyle(fontFamily = Poppins, fontSize = 14.sp, color = NeuMuted)
                        )
                    }
                }
            } else {
                items(state.alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        is12Hour = state.is12Hour,
                        onToggle = { viewModel.toggleAlarm(alarm) },
                        onDelete = { viewModel.deleteAlarm(alarm) },
                        onSoundClick = { editingSoundAlarm = alarm }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }

        // "+ Новый будильник" button (.pill.raise)
        Box(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .neuClickable(cornerRadius = 22.dp, onClick = { showNewAlarmSheet = true })
                .padding(horizontal = 32.dp, vertical = 15.dp)
                .testTag("new_alarm_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = NeuBlue2,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Новый будильник",
                    style = TextStyle(
                        fontFamily = Poppins,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeuText
                    )
                )
            }
        }
    }

    if (showNewAlarmSheet) {
        NewAlarmModal(
            is12Hour = state.is12Hour,
            customSounds = state.customSounds,
            onAddCustomSound = { uri, name -> viewModel.addCustomSound(uri, name) },
            onRenameCustomSound = { uri, name -> viewModel.renameCustomSound(uri, name) },
            onDeleteCustomSound = { uri -> viewModel.deleteCustomSound(uri) },
            onDismiss = {
                viewModel.stopSoundPreview()
                showNewAlarmSheet = false
            },
            onSave = { hour, minute, label, daysBitmask, sound, vibrate ->
                viewModel.stopSoundPreview()
                viewModel.createAlarm(hour, minute, label, daysBitmask, sound, vibrate)
                showNewAlarmSheet = false
            }
        )
    }

    editingSoundAlarm?.let { alarm ->
        com.neo.clock.ui.sound.SoundPickerSheet(
            selectedSoundId = alarm.sound,
            customSounds = state.customSounds,
            onSelectSound = { newSound ->
                viewModel.updateAlarm(alarm.copy(sound = newSound))
                editingSoundAlarm = null
            },
            onAddCustomSound = { uri, name -> viewModel.addCustomSound(uri, name) },
            onRenameCustomSound = { uri, name -> viewModel.renameCustomSound(uri, name) },
            onDeleteCustomSound = { uri -> viewModel.deleteCustomSound(uri) },
            onDismiss = { editingSoundAlarm = null }
        )
    }
}

@Composable
fun AlarmCard(
    alarm: AlarmEntity,
    is12Hour: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onSoundClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showDelete by remember { mutableStateOf(false) }

    val displayHour = if (is12Hour) {
        val h = alarm.hour % 12
        String.format("%02d", if (h == 0) 12 else h)
    } else {
        String.format("%02d", alarm.hour)
    }
    val minutesStr = String.format("%02d", alarm.minute)
    val amPmStr = if (is12Hour) (if (alarm.hour < 12) " AM" else " PM") else ""

    val soundName = SoundEngine.resolveTitle(context, alarm.sound)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuRaised(cornerRadius = 20.dp)
            .pointerInput(alarm.id) {
                detectTapGestures(
                    onLongPress = {
                        showDelete = !showDelete
                    },
                    onTap = {
                        if (showDelete) showDelete = false
                    }
                )
            }
            .padding(16.dp, 18.dp)
            .testTag("alarm_card_${alarm.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = alarm.label.ifBlank { "Будильник" },
                        style = TextStyle(
                            fontFamily = Poppins,
                            fontSize = 13.sp,
                            color = NeuMuted
                        )
                    )
                    Text(text = " · ", color = NeuMuted, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSoundClick() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("alarm_card_sound_${alarm.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Мелодия",
                            tint = NeuBlue2,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = soundName,
                            style = TextStyle(
                                fontFamily = Poppins,
                                fontSize = 13.sp,
                                color = NeuBlue2,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Time digits
                Text(
                    text = "$displayHour : $minutesStr$amPmStr",
                    style = TextStyle(
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 30.sp,
                        letterSpacing = 0.06.sp,
                        color = if (alarm.enabled) NeuText else NeuMuted
                    )
                )

                if (showDelete) {
                    Text(
                        text = "Нажмите крестик справа для удаления",
                        style = TextStyle(
                            fontFamily = Poppins,
                            fontSize = 11.sp,
                            color = NeuRed
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Delete button appears ONLY on long press
                AnimatedVisibility(visible = showDelete) {
                    IconButton(
                        onClick = {
                            showDelete = false
                            onDelete()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .padding(end = 6.dp)
                            .testTag("delete_alarm_${alarm.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .neuRaised(cornerRadius = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Удалить",
                                tint = NeuRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Neumorphic Spring Switch (.tg.sunk)
                NeumorphicSwitch(
                    checked = alarm.enabled,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                )
            }
        }
    }
}

@Composable
fun NeumorphicSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // 56dp track width, 30dp height, knob 22dp, left offset 4dp when off, 30dp when on
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 30.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = 0.55f, // spring bounce
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "switchThumb"
    )

    Box(
        modifier = modifier
            .width(56.dp)
            .height(30.dp)
            .neuSunk(
                cornerRadius = 15.dp,
                surfaceColor = if (checked) NeuBlueDark else Color(0xFF232426)
            )
            .clickable { onCheckedChange(!checked) }
    ) {
        // Knob
        Box(
            modifier = Modifier
                .offset(x = thumbOffset, y = 4.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    if (checked) {
                        Brush.linearGradient(listOf(Color(0xFF7EA3FF), NeuBlue))
                    } else {
                        Brush.linearGradient(listOf(Color(0xFF4A4B4E), Color(0xFF2C2D2F)))
                    }
                )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAlarmModal(
    is12Hour: Boolean,
    customSounds: List<com.neo.clock.audio.CustomSound>,
    onAddCustomSound: (String, String) -> Unit,
    onRenameCustomSound: (String, String) -> Unit,
    onDeleteCustomSound: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (hour: Int, minute: Int, label: String, daysBitmask: Int, sound: String, vibrate: Boolean) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var hour by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(0) }
    var label by remember { mutableStateOf("") }
    var daysBitmask by remember { mutableIntStateOf(0) } // 0 = once
    var selectedSound by remember { mutableStateOf("builtin:dawn") }
    var vibrate by remember { mutableStateOf(true) }

    var showSoundPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val daysLabels = listOf(
        Pair("Пн", 1),
        Pair("Вт", 2),
        Pair("Ср", 3),
        Pair("Чт", 4),
        Pair("Пт", 5),
        Pair("Сб", 6),
        Pair("Вс", 7)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NeuBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Новый будильник",
                style = TextStyle(
                    fontFamily = Poppins,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeuText
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Time Picker
            NeumorphicTimePicker(
                hour = hour,
                minute = minute,
                is12Hour = is12Hour,
                onHourChange = { hour = it },
                onMinuteChange = { minute = it }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Name Field (.f.sunk)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuSunk(cornerRadius = 14.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { if (it.length <= 24) label = it },
                    placeholder = { Text("Название (например: Подъём)", color = NeuMuted, fontSize = 14.sp) },
                    singleLine = true,
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = NeuText,
                        unfocusedTextColor = NeuText
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("alarm_name_input")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Days of week repeat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysLabels.forEach { (name, dayNum) ->
                    val bit = dayNum - 1
                    val isDayActive = (daysBitmask and (1 shl bit)) != 0

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .then(if (isDayActive) Modifier.neuSunk(cornerRadius = 12.dp) else Modifier.neuRaised(cornerRadius = 12.dp))
                            .neuClickable(cornerRadius = 12.dp, onClick = {
                                daysBitmask = if (isDayActive) {
                                    daysBitmask and (1 shl bit).inv()
                                } else {
                                    daysBitmask or (1 shl bit)
                                }
                            })
                            .testTag("day_chip_$dayNum"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name,
                            fontWeight = if (isDayActive) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (isDayActive) NeuBlue2 else NeuText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dedicated Melody Row with Play/Stop Preview Button
            val currentSoundTitle = SoundEngine.resolveTitle(context, selectedSound)
            var isSoundPlaying by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuRaised(cornerRadius = 14.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("alarm_sound_row")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                SoundEngine.stop()
                                isSoundPlaying = false
                                showSoundPicker = true
                            }
                    ) {
                        Text(text = "Мелодия", color = NeuMuted, fontSize = 11.sp)
                        Text(
                            text = currentSoundTitle,
                            color = NeuText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Play / Stop preview button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .neuClickable(cornerRadius = 10.dp, onClick = {
                                    if (isSoundPlaying) {
                                        SoundEngine.stop()
                                        isSoundPlaying = false
                                    } else {
                                        SoundEngine.play(context, selectedSound, loop = false, rampVolume = false)
                                        isSoundPlaying = true
                                    }
                                })
                                .testTag("preview_alarm_sound_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSoundPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Прослушать мелодию",
                                tint = if (isSoundPlaying) NeuBlue2 else NeuText,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Выбрать >",
                            color = NeuBlue2,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                SoundEngine.stop()
                                isSoundPlaying = false
                                showSoundPicker = true
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: Отмена and Сохранить
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .neuClickable(cornerRadius = 18.dp, onClick = onDismiss)
                        .testTag("cancel_alarm_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Отмена", color = NeuText, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF4A7DFF), Color(0xFF2253D6))
                            )
                        )
                        .neuClickable(cornerRadius = 18.dp, onClick = {
                            onSave(hour, minute, label, daysBitmask, selectedSound, vibrate)
                        })
                        .testTag("save_alarm_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Сохранить", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }
    }

    if (showSoundPicker) {
        com.neo.clock.ui.sound.SoundPickerSheet(
            selectedSoundId = selectedSound,
            customSounds = customSounds,
            onSelectSound = {
                selectedSound = it
                showSoundPicker = false
            },
            onAddCustomSound = onAddCustomSound,
            onRenameCustomSound = onRenameCustomSound,
            onDeleteCustomSound = onDeleteCustomSound,
            onDismiss = { showSoundPicker = false }
        )
    }
}
