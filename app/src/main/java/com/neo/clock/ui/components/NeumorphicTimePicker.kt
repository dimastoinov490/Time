package com.neo.clock.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neo.clock.ui.modifiers.neuClickable
import com.neo.clock.ui.modifiers.neuSunk
import com.neo.clock.ui.theme.NeuBlue2
import com.neo.clock.ui.theme.NeuMuted
import com.neo.clock.ui.theme.Poppins

@Composable
fun NeumorphicTimePicker(
    hour: Int,
    minute: Int,
    is12Hour: Boolean,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var isEditingHour by remember { mutableStateOf(false) }
    var isEditingMinute by remember { mutableStateOf(false) }

    val displayHour = if (is12Hour) {
        val h = hour % 12
        if (h == 0) 12 else h
    } else {
        hour
    }
    val isPm = hour >= 12

    var hourTextValue by remember(hour, isEditingHour) {
        mutableStateOf(TextFieldValue(String.format("%02d", displayHour), TextRange(0, 2)))
    }
    var minTextValue by remember(minute, isEditingMinute) {
        mutableStateOf(TextFieldValue(String.format("%02d", minute), TextRange(0, 2)))
    }

    val hourFocusRequester = remember { FocusRequester() }
    val minFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isEditingHour) {
        if (isEditingHour) {
            hourFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(isEditingMinute) {
        if (isEditingMinute) {
            minFocusRequester.requestFocus()
        }
    }

    fun commitHour(text: String) {
        val parsed = text.toIntOrNull()
        if (parsed != null) {
            if (is12Hour) {
                val clamped = parsed.coerceIn(1, 12)
                val final24Hour = if (isPm) {
                    if (clamped == 12) 12 else clamped + 12
                } else {
                    if (clamped == 12) 0 else clamped
                }
                onHourChange(final24Hour)
            } else {
                onHourChange(parsed.coerceIn(0, 23))
            }
        }
        isEditingHour = false
        focusManager.clearFocus()
    }

    fun commitMinute(text: String) {
        val parsed = text.toIntOrNull()
        if (parsed != null) {
            onMinuteChange(parsed.coerceIn(0, 59))
        }
        isEditingMinute = false
        focusManager.clearFocus()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hours column
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .neuClickable(cornerRadius = 14.dp, onClick = {
                            onHourChange((hour + 1) % 24)
                        })
                        .testTag("time_hour_up"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Hour +", tint = NeuMuted)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .width(86.dp)
                        .height(72.dp)
                        .neuSunk(cornerRadius = 18.dp)
                        .clickable {
                            isEditingHour = true
                            isEditingMinute = false
                            hourTextValue = TextFieldValue(String.format("%02d", displayHour), TextRange(0, 2))
                        }
                        .testTag("time_hour_box"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isEditingHour) {
                        BasicTextField(
                            value = hourTextValue,
                            onValueChange = { newVal ->
                                val digitsOnly = newVal.text.filter { it.isDigit() }.take(2)
                                hourTextValue = newVal.copy(text = digitsOnly, selection = TextRange(digitsOnly.length))
                                if (digitsOnly.length == 2) {
                                    commitHour(digitsOnly)
                                }
                            },
                            textStyle = TextStyle(
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp,
                                letterSpacing = 0.06.sp,
                                color = NeuBlue2,
                                textAlign = TextAlign.Center
                            ),
                            cursorBrush = SolidColor(NeuBlue2),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { commitHour(hourTextValue.text) }
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .width(74.dp)
                                .focusRequester(hourFocusRequester)
                                .onFocusChanged {
                                    if (!it.isFocused && isEditingHour) {
                                        commitHour(hourTextValue.text)
                                    }
                                }
                                .testTag("time_hour_input")
                        )
                    } else {
                        Text(
                            text = String.format("%02d", displayHour),
                            style = TextStyle(
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 34.sp,
                                letterSpacing = 0.06.sp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFE8E8EA), Color(0xFF8D8E92))
                                )
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .neuClickable(cornerRadius = 14.dp, onClick = {
                            onHourChange((hour - 1 + 24) % 24)
                        })
                        .testTag("time_hour_down"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Hour -", tint = NeuMuted)
                }
            }

            // Colon
            Text(
                text = ":",
                style = TextStyle(
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp,
                    color = NeuBlue2
                ),
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // Minutes column
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .neuClickable(cornerRadius = 14.dp, onClick = {
                            onMinuteChange((minute + 1) % 60)
                        })
                        .testTag("time_min_up"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Min +", tint = NeuMuted)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .width(86.dp)
                        .height(72.dp)
                        .neuSunk(cornerRadius = 18.dp)
                        .clickable {
                            isEditingMinute = true
                            isEditingHour = false
                            minTextValue = TextFieldValue(String.format("%02d", minute), TextRange(0, 2))
                        }
                        .testTag("time_minute_box"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isEditingMinute) {
                        BasicTextField(
                            value = minTextValue,
                            onValueChange = { newVal ->
                                val digitsOnly = newVal.text.filter { it.isDigit() }.take(2)
                                minTextValue = newVal.copy(text = digitsOnly, selection = TextRange(digitsOnly.length))
                                if (digitsOnly.length == 2) {
                                    commitMinute(digitsOnly)
                                }
                            },
                            textStyle = TextStyle(
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp,
                                letterSpacing = 0.06.sp,
                                color = NeuBlue2,
                                textAlign = TextAlign.Center
                            ),
                            cursorBrush = SolidColor(NeuBlue2),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { commitMinute(minTextValue.text) }
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .width(74.dp)
                                .focusRequester(minFocusRequester)
                                .onFocusChanged {
                                    if (!it.isFocused && isEditingMinute) {
                                        commitMinute(minTextValue.text)
                                    }
                                }
                                .testTag("time_min_input")
                        )
                    } else {
                        Text(
                            text = String.format("%02d", minute),
                            style = TextStyle(
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 34.sp,
                                letterSpacing = 0.06.sp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFE8E8EA), Color(0xFF8D8E92))
                                )
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .neuClickable(cornerRadius = 14.dp, onClick = {
                            onMinuteChange((minute - 1 + 60) % 60)
                        })
                        .testTag("time_min_down"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Min -", tint = NeuMuted)
                }
            }

            // AM/PM if 12-hour format
            if (is12Hour) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .width(52.dp)
                        .neuClickable(cornerRadius = 14.dp, onClick = {
                            onHourChange((hour + 12) % 24)
                        })
                        .testTag("toggle_am_pm"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPm) "PM" else "AM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NeuBlue2
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Нажмите на цифры для ручного ввода с клавиатуры",
            style = TextStyle(
                fontFamily = Poppins,
                fontSize = 11.sp,
                color = NeuMuted
            )
        )
    }
}
