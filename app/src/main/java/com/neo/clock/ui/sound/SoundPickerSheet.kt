package com.neo.clock.ui.sound

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neo.clock.audio.BuiltInMelodies
import com.neo.clock.audio.CustomSound
import com.neo.clock.audio.DeviceSoundHelper
import com.neo.clock.audio.SoundEngine
import com.neo.clock.audio.SoundItem
import com.neo.clock.audio.SoundSource
import com.neo.clock.ui.modifiers.neuClickable
import com.neo.clock.ui.modifiers.neuRaised
import com.neo.clock.ui.modifiers.neuSunk
import com.neo.clock.ui.theme.NeuBg
import com.neo.clock.ui.theme.NeuBlue
import com.neo.clock.ui.theme.NeuBlue2
import com.neo.clock.ui.theme.NeuMuted
import com.neo.clock.ui.theme.NeuText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundPickerSheet(
    selectedSoundId: String,
    customSounds: List<CustomSound>,
    onSelectSound: (String) -> Unit,
    onAddCustomSound: (String, String) -> Unit,
    onRenameCustomSound: (String, String) -> Unit,
    onDeleteCustomSound: (String) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Встроенные, 1: Устройство, 2: Мои
    var searchQuery by remember { mutableStateOf("") }
    var previewingId by remember { mutableStateOf<String?>(null) }

    // Device sounds loaded asynchronously
    var deviceSounds by remember { mutableStateOf<List<SoundItem>>(emptyList()) }
    var isLoadingDeviceSounds by remember { mutableStateOf(false) }

    // Custom sound rename dialog state
    var soundToRename by remember { mutableStateOf<CustomSound?>(null) }
    var renameInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            isLoadingDeviceSounds = true
            val sounds = DeviceSoundHelper.getDeviceSounds(context)
            withContext(Dispatchers.Main) {
                deviceSounds = sounds
                isLoadingDeviceSounds = false
            }
        }
    }

    // Audio file picker launcher (OpenMultipleDocuments)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        for (uri in uris) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            val name = DeviceSoundHelper.queryDisplayName(context, uri)
            onAddCustomSound(uri.toString(), name)
        }
    }

    fun handlePreview(soundId: String) {
        if (previewingId == soundId) {
            SoundEngine.stop()
            previewingId = null
        } else {
            SoundEngine.play(context, soundId, loop = false, rampVolume = false)
            previewingId = soundId
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            SoundEngine.stop()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = NeuBg,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Выбор мелодии",
                    color = NeuText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .neuClickable(cornerRadius = 12.dp, onClick = {
                            SoundEngine.stop()
                            onDismiss()
                        })
                        .testTag("sound_picker_close"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = NeuMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuSunk(cornerRadius = 14.dp)
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск",
                        tint = NeuMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Поиск мелодии...", color = NeuMuted, fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = NeuText,
                            unfocusedTextColor = NeuText,
                            cursorColor = NeuBlue2
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sound_search_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Segmented Tabs: Встроенные | Устройство | Мои
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuSunk(cornerRadius = 16.dp)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val tabs = listOf("Встроенные", "Устройство", "Мои (${customSounds.size})")
                tabs.forEachIndexed { index, tabTitle ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .then(if (isSelected) Modifier.neuRaised(cornerRadius = 12.dp) else Modifier)
                            .clickable { selectedTab = index }
                            .testTag("sound_tab_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabTitle,
                            color = if (isSelected) NeuBlue2 else NeuMuted,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // ВСТРОЕННЫЕ (52 мелодии с группами)
                    val filtered = BuiltInMelodies.LIST.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                                it.group.contains(searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("builtin_sounds_list"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val grouped = filtered.groupBy { it.group }
                        grouped.forEach { (groupName, sounds) ->
                            item {
                                Text(
                                    text = groupName.uppercase(),
                                    color = NeuMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
                                )
                            }
                            items(sounds, key = { it.id }) { sound ->
                                val isSelected = (sound.id == selectedSoundId) ||
                                        (sound.id.removePrefix("builtin:") == selectedSoundId.removePrefix("builtin:"))
                                val isPreviewing = previewingId == sound.id

                                SoundItemRow(
                                    title = sound.title,
                                    subtitle = sound.group,
                                    isSelected = isSelected,
                                    isPreviewing = isPreviewing,
                                    onSelect = {
                                        onSelectSound(sound.id)
                                    },
                                    onPreview = { handlePreview(sound.id) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // УСТРОЙСТВО (Системные звуки телефона)
                    if (isLoadingDeviceSounds) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = NeuBlue2)
                        }
                    } else {
                        val filtered = deviceSounds.filter {
                            it.title.contains(searchQuery, ignoreCase = true)
                        }

                        if (filtered.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Системные мелодии не найдены",
                                    color = NeuMuted,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("device_sounds_list"),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filtered, key = { it.id }) { sound ->
                                    val isSelected = selectedSoundId == sound.id
                                    val isPreviewing = previewingId == sound.id

                                    SoundItemRow(
                                        title = sound.title,
                                        subtitle = sound.group,
                                        isSelected = isSelected,
                                        isPreviewing = isPreviewing,
                                        onSelect = { onSelectSound(sound.id) },
                                        onPreview = { handlePreview(sound.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // МОИ (Пользовательские файлы из памяти)
                    val filtered = customSounds.filter {
                        it.title.contains(searchQuery, ignoreCase = true)
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Button to add files
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .neuClickable(cornerRadius = 14.dp, onClick = {
                                    filePickerLauncher.launch(arrayOf("audio/*"))
                                })
                                .testTag("add_custom_sound_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Добавить",
                                    tint = NeuBlue2,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Добавить из памяти",
                                    color = NeuBlue2,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (filtered.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Нет добавленных файлов.\nНажмите «Добавить из памяти», чтобы выбрать аудиофайлы MP3, M4A, OGG, WAV, FLAC, AAC.",
                                    color = NeuMuted,
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .testTag("custom_sounds_list"),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filtered, key = { it.uriString }) { custom ->
                                    val soundId = "custom:${custom.uriString}"
                                    val isSelected = (selectedSoundId == soundId) || (selectedSoundId == custom.uriString)
                                    val isPreviewing = previewingId == soundId

                                    CustomSoundItemRow(
                                        title = custom.title,
                                        isSelected = isSelected,
                                        isPreviewing = isPreviewing,
                                        onSelect = { onSelectSound(soundId) },
                                        onPreview = { handlePreview(soundId) },
                                        onRename = {
                                            soundToRename = custom
                                            renameInput = custom.title
                                        },
                                        onDelete = {
                                            if (previewingId == soundId) {
                                                SoundEngine.stop()
                                                previewingId = null
                                            }
                                            onDeleteCustomSound(custom.uriString)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename Custom Sound Dialog
    soundToRename?.let { target ->
        AlertDialog(
            onDismissRequest = { soundToRename = null },
            title = { Text("Переименовать мелодию", color = NeuText) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NeuText,
                        unfocusedTextColor = NeuText,
                        cursorColor = NeuBlue2,
                        focusedBorderColor = NeuBlue2
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = renameInput.trim()
                        if (trimmed.isNotEmpty()) {
                            onRenameCustomSound(target.uriString, trimmed)
                        }
                        soundToRename = null
                    }
                ) {
                    Text("Сохранить", color = NeuBlue2)
                }
            },
            dismissButton = {
                TextButton(onClick = { soundToRename = null }) {
                    Text("Отмена", color = NeuMuted)
                }
            },
            containerColor = NeuBg
        )
    }
}

@Composable
fun SoundItemRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isPreviewing: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isSelected) Modifier.neuSunk(cornerRadius = 14.dp) else Modifier.neuRaised(cornerRadius = 14.dp))
            .clickable { onSelect() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Radio button
                RadioButton(
                    selected = isSelected,
                    onClick = { onSelect() },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = NeuBlue2,
                        unselectedColor = NeuMuted
                    ),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        color = if (isSelected) NeuBlue2 else NeuText,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                    Text(
                        text = subtitle,
                        color = NeuMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Play / Stop Preview button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .neuClickable(cornerRadius = 10.dp, onClick = onPreview),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPreviewing) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = "Прослушать",
                    tint = if (isPreviewing) NeuBlue2 else NeuText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CustomSoundItemRow(
    title: String,
    isSelected: Boolean,
    isPreviewing: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isSelected) Modifier.neuSunk(cornerRadius = 14.dp) else Modifier.neuRaised(cornerRadius = 14.dp))
            .clickable { onSelect() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onSelect() },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = NeuBlue2,
                        unselectedColor = NeuMuted
                    ),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        color = if (isSelected) NeuBlue2 else NeuText,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                    Text(
                        text = "Файл из памяти",
                        color = NeuMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Play / Stop preview
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .neuClickable(cornerRadius = 10.dp, onClick = onPreview),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPreviewing) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "Прослушать",
                        tint = if (isPreviewing) NeuBlue2 else NeuText,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Rename button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .neuClickable(cornerRadius = 10.dp, onClick = onRename),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Переименовать",
                        tint = NeuMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Delete button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .neuClickable(cornerRadius = 10.dp, onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
