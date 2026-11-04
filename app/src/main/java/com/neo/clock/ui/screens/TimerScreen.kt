package com.neo.clock.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.neo.clock.ui.MainUiState
import com.neo.clock.ui.MainViewModel
import com.neo.clock.ui.modifiers.neuClickable
import com.neo.clock.ui.modifiers.neuRaised
import com.neo.clock.ui.modifiers.neuSunk
import com.neo.clock.ui.theme.NeuBlue
import com.neo.clock.ui.theme.NeuBlue2
import com.neo.clock.ui.theme.NeuText
import com.neo.clock.ui.theme.Poppins

@Composable
fun TimerScreen(
    state: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val totalSec = state.timerTotalSeconds
    val remSec = state.timerRemainingSeconds
    val progress = if (totalSec > 0) (remSec.toFloat() / totalSec.toFloat()).coerceIn(0f, 1f) else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 250),
        label = "timerProgress"
    )

    val minutes = remSec / 60
    val seconds = remSec % 60
    val formattedTime = String.format("%02d : %02d", minutes, seconds)

    val presets = listOf(1, 5, 10, 15)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
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
                text = "Таймер",
                style = TextStyle(
                    fontFamily = Poppins,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.04.sp,
                    color = NeuText
                )
            )
        }

        // Animated Circular Progress Ring (250dp x 250dp)
        Box(
            modifier = Modifier.size(250.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = (size.minDimension / 2f) - 16.dp.toPx()
                val center = Offset(size.width / 2f, size.height / 2f)

                // Background track circle
                drawCircle(
                    color = Color(0xFF1D1E20),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 14.dp.toPx())
                )

                // Active glowing blue progress arc
                val sweepAngle = animatedProgress * 360f
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(NeuBlue, NeuBlue2, NeuBlue),
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Big Centered Time in gradient tabular text
            Text(
                text = formattedTime,
                style = TextStyle(
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 44.sp,
                    letterSpacing = 0.06.sp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE8E8EA), Color(0xFF8D8E92))
                    )
                )
            )
        }

        // Preset Chips Row (.chips)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            presets.forEach { m ->
                val isSelected = totalSec == m * 60L
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .then(if (isSelected) Modifier.neuSunk(cornerRadius = 14.dp) else Modifier.neuRaised(cornerRadius = 14.dp))
                        .neuClickable(cornerRadius = 14.dp, onClick = { viewModel.setTimerPreset(m) })
                        .testTag("preset_${m}m")
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$m мин",
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) NeuBlue2 else NeuText
                    )
                }
            }
        }

        // Bottom Controls Row: Сброс and Старт/Пауза (.row)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // Сброс (.pill.raise)
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(52.dp)
                    .neuClickable(cornerRadius = 18.dp, onClick = { viewModel.resetTimer() })
                    .testTag("timer_reset_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Сброс",
                    color = NeuText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(22.dp))

            // Старт / Пауза (.pill.go)
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF4A7DFF), Color(0xFF2253D6))
                        )
                    )
                    .neuClickable(cornerRadius = 18.dp, onClick = { viewModel.toggleTimer() })
                    .testTag("timer_start_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.isTimerRunning) "Пауза" else "Старт",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
