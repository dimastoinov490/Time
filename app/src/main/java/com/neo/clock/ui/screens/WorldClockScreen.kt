package com.neo.clock.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.input.pointer.pointerInput
import com.neo.clock.ui.theme.NeuRed
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neo.clock.audio.SoundEngine
import com.neo.clock.ui.MainUiState
import com.neo.clock.ui.MainViewModel
import com.neo.clock.ui.modifiers.neuClickable
import com.neo.clock.ui.modifiers.neuRaised
import com.neo.clock.ui.modifiers.neuSunk
import com.neo.clock.ui.theme.NeuBg
import com.neo.clock.ui.theme.NeuBlue
import com.neo.clock.ui.theme.NeuBlue2
import com.neo.clock.ui.theme.NeuDarkShadow
import com.neo.clock.ui.theme.NeuLightShadow
import com.neo.clock.ui.theme.NeuMuted
import com.neo.clock.ui.theme.NeuText
import com.neo.clock.ui.theme.Poppins
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldClockScreen(
    state: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var showAddCitySheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var deletingCityId by remember { mutableStateOf<String?>(null) }

    // 18s rotating blue sweep glow
    val infiniteTransition = rememberInfiniteTransition(label = "dialSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAngle"
    )

    val timeFormatPattern = if (state.is12Hour) "hh mm a" else "HH mm"
    val localTime = state.currentTime
    val hoursStr = if (state.is12Hour) {
        val h = localTime.hour % 12
        String.format("%02d", if (h == 0) 12 else h)
    } else {
        String.format("%02d", localTime.hour)
    }
    val minutesStr = String.format("%02d", localTime.minute)
    val amPmStr = if (state.is12Hour) (if (localTime.hour < 12) " AM" else " PM") else ""

    val ruDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy г.", Locale.forLanguageTag("ru"))
    val dateStr = localTime.format(ruDateFormatter)

    val pagerState = rememberPagerState(pageCount = { state.cities.size })

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Мировое время",
                style = TextStyle(
                    fontFamily = Poppins,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.04.sp,
                    color = NeuText
                )
            )

            // Settings gear button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .neuClickable(cornerRadius = 16.dp, onClick = { showSettingsSheet = true })
                    .testTag("settings_gear_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Настройки",
                    tint = NeuText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Big Neumorphic Dial (width: 280px, height: 280px)
        Box(
            modifier = Modifier.size(280.dp),
            contentAlignment = Alignment.Center
        ) {
            // Rotating Blue Glow Conic/Sweep gradient arc
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(sweepAngle)
            ) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF1B3F9E),
                            NeuBlue,
                            Color(0xFF1B3F9E),
                            Color.Transparent
                        ),
                        center = center
                    ),
                    radius = radius - 3.dp.toPx(),
                    center = center,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Inner dark radial surface with inset shadow
            Box(
                modifier = Modifier
                    .size(264.dp)
                    .neuSunk(cornerRadius = 132.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Big tabular digits with gradient & blinking colon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = hoursStr,
                            style = TextStyle(
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 48.sp,
                                letterSpacing = 0.06.sp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFE8E8EA), Color(0xFF8D8E92))
                                )
                            )
                        )

                        Text(
                            text = " : ",
                            style = TextStyle(
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 44.sp,
                                color = if (state.isBlinkOn) NeuBlue2 else NeuBlue2.copy(alpha = 0.25f)
                            )
                        )

                        Text(
                            text = "$minutesStr$amPmStr",
                            style = TextStyle(
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 48.sp,
                                letterSpacing = 0.06.sp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFE8E8EA), Color(0xFF8D8E92))
                                )
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = dateStr,
                        style = TextStyle(
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            color = NeuMuted
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Cities Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Города",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = NeuMuted
            )

            Box(
                modifier = Modifier
                    .height(34.dp)
                    .neuClickable(cornerRadius = 10.dp, onClick = { showAddCitySheet = true })
                    .testTag("add_city_button")
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = NeuBlue2, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Добавить", color = NeuBlue2, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Carousel of Cities
        if (state.cities.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) { page ->
                val city = state.cities[page]
                val cityZone = try { ZoneId.of(city.timezoneId) } catch (e: Exception) { ZoneId.systemDefault() }
                val cityTime = state.currentTime.withZoneSameInstant(cityZone)
                val cityTimeFormatter = DateTimeFormatter.ofPattern(
                    if (state.is12Hour) "hh : mm a" else "HH : mm",
                    Locale.ENGLISH
                )
                val ruCityDateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.forLanguageTag("ru"))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp)
                        .neuRaised(cornerRadius = 20.dp)
                        .pointerInput(city.timezoneId) {
                            detectTapGestures(
                                onLongPress = {
                                    deletingCityId = if (deletingCityId == city.timezoneId) null else city.timezoneId
                                },
                                onTap = {
                                    if (deletingCityId != null) deletingCityId = null
                                }
                            )
                        }
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = city.name,
                                style = TextStyle(
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 20.sp,
                                    color = Color(0xFF8FA6D8)
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = cityTime.format(ruCityDateFormatter),
                                style = TextStyle(
                                    fontFamily = Poppins,
                                    fontSize = 13.sp,
                                    color = NeuMuted
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = cityTime.format(cityTimeFormatter).uppercase(),
                                style = TextStyle(
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 24.sp,
                                    letterSpacing = 0.06.sp,
                                    color = NeuText
                                )
                            )
                            AnimatedVisibility(visible = deletingCityId == city.timezoneId && state.cities.size > 1) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            deletingCityId = null
                                            viewModel.removeCity(city.timezoneId)
                                        },
                                        modifier = Modifier.size(32.dp).testTag("remove_city_${city.timezoneId}")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .neuRaised(cornerRadius = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Удалить",
                                                tint = NeuRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dot indicators below carousel
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(state.cities.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 12.dp else 10.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) NeuBlue else Color(0xFF1C1D1E))
                    )
                }
            }
        }
    }

    // Add City BottomSheet
    if (showAddCitySheet) {
        AddCityModal(
            existingIds = state.cities.map { it.timezoneId }.toSet(),
            onDismiss = { showAddCitySheet = false },
            onSelect = { name, tz ->
                viewModel.addCity(name, tz)
                showAddCitySheet = false
            }
        )
    }

    // Settings BottomSheet
    if (showSettingsSheet) {
        SettingsModal(
            is12Hour = state.is12Hour,
            selectedSound = state.timerSound,
            isRampVolume = state.isRampVolumeEnabled,
            customSounds = state.customSounds,
            onToggle12Hour = { viewModel.toggle12HourFormat() },
            onToggleRampVolume = { viewModel.setRampVolumeEnabled(it) },
            onSoundSelect = { viewModel.setTimerSound(it) },
            onAddCustomSound = { uri, name -> viewModel.addCustomSound(uri, name) },
            onRenameCustomSound = { uri, name -> viewModel.renameCustomSound(uri, name) },
            onDeleteCustomSound = { uri -> viewModel.deleteCustomSound(uri) },
            onDismiss = { showSettingsSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCityModal(
    existingIds: Set<String>,
    onDismiss: () -> Unit,
    onSelect: (String, String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val allTzs = remember {
        ZoneId.getAvailableZoneIds().sorted().map { id ->
            val cityClean = id.substringAfterLast("/").replace("_", " ")
            Pair(cityClean, id)
        }
    }

    val filtered = allTzs.filter {
        (it.first.contains(query, ignoreCase = true) || it.second.contains(query, ignoreCase = true)) &&
                !existingIds.contains(it.second)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NeuBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Добавить часовой пояс",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = NeuText
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Поиск города или зоны...", color = NeuMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeuMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .neuSunk(cornerRadius = 14.dp)
                    .testTag("city_search_field"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                items(filtered.take(30)) { (city, tz) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .neuClickable(cornerRadius = 14.dp, onClick = { onSelect(city, tz) })
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(text = city, style = MaterialTheme.typography.titleMedium, color = NeuText, fontWeight = FontWeight.Medium)
                            Text(text = tz, style = MaterialTheme.typography.bodySmall, color = NeuMuted)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsModal(
    is12Hour: Boolean,
    selectedSound: String,
    isRampVolume: Boolean,
    customSounds: List<com.neo.clock.audio.CustomSound>,
    onToggle12Hour: () -> Unit,
    onToggleRampVolume: (Boolean) -> Unit,
    onSoundSelect: (String) -> Unit,
    onAddCustomSound: (String, String) -> Unit,
    onRenameCustomSound: (String, String) -> Unit,
    onDeleteCustomSound: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showSoundPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NeuBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(26.dp)
        ) {
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.titleLarge,
                color = NeuText,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 12/24 hour switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuRaised(cornerRadius = 16.dp)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Формат времени", style = MaterialTheme.typography.titleMedium, color = NeuText)
                    Text(text = if (is12Hour) "12-часовой (AM/PM)" else "24-часовой", color = NeuMuted, fontSize = 12.sp)
                }

                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .neuClickable(cornerRadius = 12.dp, onClick = onToggle12Hour)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (is12Hour) "12 ч" else "24 ч", color = NeuBlue2, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Volume ramp toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuRaised(cornerRadius = 16.dp)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Нарастание громкости", style = MaterialTheme.typography.titleMedium, color = NeuText)
                    Text(text = "Плавное нарастание звука за 10 секунд", color = NeuMuted, fontSize = 12.sp)
                }

                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .neuClickable(cornerRadius = 12.dp, onClick = { onToggleRampVolume(!isRampVolume) })
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isRampVolume) "Вкл" else "Выкл",
                        color = if (isRampVolume) NeuBlue2 else NeuMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Default timer sound selector row
            val currentSoundTitle = SoundEngine.resolveTitle(context, selectedSound)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuRaised(cornerRadius = 16.dp)
                    .clickable { showSoundPicker = true }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Мелодия таймера по умолчанию", color = NeuMuted, fontSize = 11.sp)
                        Text(
                            text = currentSoundTitle,
                            color = NeuText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(text = "Выбрать >", color = NeuBlue2, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }

    if (showSoundPicker) {
        com.neo.clock.ui.sound.SoundPickerSheet(
            selectedSoundId = selectedSound,
            customSounds = customSounds,
            onSelectSound = {
                onSoundSelect(it)
                showSoundPicker = false
            },
            onAddCustomSound = onAddCustomSound,
            onRenameCustomSound = onRenameCustomSound,
            onDeleteCustomSound = onDeleteCustomSound,
            onDismiss = { showSoundPicker = false }
        )
    }
}
