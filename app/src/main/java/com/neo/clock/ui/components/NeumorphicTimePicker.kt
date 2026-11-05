package com.neo.clock.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.neo.clock.ui.modifiers.neuRaised
import com.neo.clock.ui.modifiers.neuSunk
import com.neo.clock.ui.theme.NeuBg
import com.neo.clock.ui.theme.NeuBlue2
import com.neo.clock.ui.theme.NeuMuted
import com.neo.clock.ui.theme.NeuText
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
    var showManualInputDialog by remember { mutableStateOf(false) }

    val displayHour = if (is12Hour) {
        val h = hour % 12
        if (h == 0) 12 else h
    } else {
        hour
    }
    val isPm = hour >= 12

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
                        .size(44.dp)
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
                        .width(88.dp)
                        .height(74.dp)
                        .neuSunk(cornerRadius = 18.dp)
                        .clickable {
                            showManualInputDialog = true
                        }
                        .testTag("time_hour_box"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d", displayHour),
                        style = TextStyle(
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 36.sp,
                            letterSpacing = 0.06.sp,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFE8E8EA), Color(0xFF8D8E92))
                            )
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
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
                    fontSize = 34.sp,
                    color = NeuBlue2
                ),
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .clickable { showManualInputDialog = true }
            )

            // Minutes column
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
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
                        .width(88.dp)
                        .height(74.dp)
                        .neuSunk(cornerRadius = 18.dp)
                        .clickable {
                            showManualInputDialog = true
                        }
                        .testTag("time_minute_box"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d", minute),
                        style = TextStyle(
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 36.sp,
                            letterSpacing = 0.06.sp,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFE8E8EA), Color(0xFF8D8E92))
                            )
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .neuClickable(cornerRadius = 14.dp, onClick = {
                            onMinuteChange((minute - 1 + 60) % 60)
                        })
                        .testTag("time_min_down"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Min -", tint = NeuMuted)
                }
            }

            // AM/PM toggle button if in 12-hour format
            if (is12Hour) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .height(52.dp)
                        .width(54.dp)
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

        Spacer(modifier = Modifier.height(10.dp))

        // Prominent button to open manual time keyboard input
        Box(
            modifier = Modifier
                .neuClickable(cornerRadius = 12.dp, onClick = { showManualInputDialog = true })
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("open_manual_time_keyboard"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Ввести время",
                    tint = NeuBlue2,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ввести время вручную",
                    style = TextStyle(
                        fontFamily = Poppins,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = NeuBlue2
                    )
                )
            }
        }
    }

    // Modal dialog with direct numeric keyboard input
    if (showManualInputDialog) {
        ManualTimeInputDialog(
            initialHour = if (is12Hour) (if (hour % 12 == 0) 12 else hour % 12) else hour,
            initialMinute = minute,
            is12Hour = is12Hour,
            isPm = isPm,
            onDismiss = { showManualInputDialog = false },
            onConfirm = { newHour, newMinute ->
                if (is12Hour) {
                    val clamped = newHour.coerceIn(1, 12)
                    val final24Hour = if (isPm) {
                        if (clamped == 12) 12 else clamped + 12
                    } else {
                        if (clamped == 12) 0 else clamped
                    }
                    onHourChange(final24Hour)
                } else {
                    onHourChange(newHour.coerceIn(0, 23))
                }
                onMinuteChange(newMinute.coerceIn(0, 59))
                showManualInputDialog = false
            }
        )
    }
}

@Composable
fun ManualTimeInputDialog(
    initialHour: Int,
    initialMinute: Int,
    is12Hour: Boolean,
    isPm: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var hourText by remember { mutableStateOf(TextFieldValue(String.format("%02d", initialHour), TextRange(0, 2))) }
    var minText by remember { mutableStateOf(TextFieldValue(String.format("%02d", initialMinute), TextRange(0, 2))) }

    val hourFocusRequester = remember { FocusRequester() }
    val minFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        hourFocusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ввод времени с клавиатуры",
                style = TextStyle(
                    fontFamily = Poppins,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeuText
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (is12Hour) "Часы (1–12) : Минуты (0–59)" else "Часы (0–23) : Минуты (0–59)",
                    style = TextStyle(fontFamily = Poppins, fontSize = 12.sp, color = NeuMuted),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour text field
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = { newVal ->
                            val digits = newVal.text.filter { it.isDigit() }.take(2)
                            hourText = newVal.copy(text = digits, selection = TextRange(digits.length))
                            if (digits.length == 2) {
                                minFocusRequester.requestFocus()
                            }
                        },
                        textStyle = TextStyle(
                            fontFamily = Poppins,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeuBlue2,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { minFocusRequester.requestFocus() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeuBlue2,
                            unfocusedBorderColor = Color(0xFF3E4044),
                            focusedContainerColor = Color(0xFF222325),
                            unfocusedContainerColor = Color(0xFF222325),
                            cursorColor = NeuBlue2
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .width(80.dp)
                            .height(72.dp)
                            .focusRequester(hourFocusRequester)
                            .testTag("manual_hour_input")
                    )

                    Text(
                        text = ":",
                        style = TextStyle(
                            fontFamily = Poppins,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeuBlue2
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    // Minute text field
                    OutlinedTextField(
                        value = minText,
                        onValueChange = { newVal ->
                            val digits = newVal.text.filter { it.isDigit() }.take(2)
                            minText = newVal.copy(text = digits, selection = TextRange(digits.length))
                        },
                        textStyle = TextStyle(
                            fontFamily = Poppins,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeuBlue2,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val h = hourText.text.toIntOrNull() ?: initialHour
                                val m = minText.text.toIntOrNull() ?: initialMinute
                                onConfirm(h, m)
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeuBlue2,
                            unfocusedBorderColor = Color(0xFF3E4044),
                            focusedContainerColor = Color(0xFF222325),
                            unfocusedContainerColor = Color(0xFF222325),
                            cursorColor = NeuBlue2
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .width(80.dp)
                            .height(72.dp)
                            .focusRequester(minFocusRequester)
                            .testTag("manual_minute_input")
                    )
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeuBlue2)
                    .clickable {
                        val h = hourText.text.toIntOrNull() ?: initialHour
                        val m = minText.text.toIntOrNull() ?: initialMinute
                        onConfirm(h, m)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("confirm_manual_time_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Установить",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_manual_time_button")
            ) {
                Text("Отмена", color = NeuMuted)
            }
        },
        containerColor = NeuBg
    )
}
