package com.neo.clock.ui.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
    val displayHour = if (is12Hour) {
        val h = hour % 12
        if (h == 0) 12 else h
    } else {
        hour
    }
    val isPm = hour >= 12

    Row(
        modifier = modifier.fillMaxWidth(),
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
                    .width(82.dp)
                    .height(68.dp)
                    .neuSunk(cornerRadius = 18.dp),
                contentAlignment = Alignment.Center
            ) {
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
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        // Minutes column
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .neuClickable(cornerRadius = 14.dp, onClick = {
                        onMinuteChange((minute + 5) % 60)
                    })
                    .testTag("time_min_up"),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Min +", tint = NeuMuted)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .width(82.dp)
                    .height(68.dp)
                    .neuSunk(cornerRadius = 18.dp),
                contentAlignment = Alignment.Center
            ) {
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

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .neuClickable(cornerRadius = 14.dp, onClick = {
                        onMinuteChange((minute - 5 + 60) % 60)
                    })
                    .testTag("time_min_down"),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Min -", tint = NeuMuted)
            }
        }

        // AM/PM if 12-hour format
        if (is12Hour) {
            Spacer(modifier = Modifier.width(14.dp))
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
}
