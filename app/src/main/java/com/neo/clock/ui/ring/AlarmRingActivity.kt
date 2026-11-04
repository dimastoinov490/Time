package com.neo.clock.ui.ring

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neo.clock.receiver.AlarmReceiver
import com.neo.clock.service.AlarmService
import com.neo.clock.ui.modifiers.neuClickable
import com.neo.clock.ui.modifiers.neuRaised
import com.neo.clock.ui.theme.NeuBg
import com.neo.clock.ui.theme.NeuBlue
import com.neo.clock.ui.theme.NeuBlue2
import com.neo.clock.ui.theme.NeoClockTheme
import com.neo.clock.ui.theme.NeuMuted
import com.neo.clock.ui.theme.NeuText
import com.neo.clock.ui.theme.Poppins
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class AlarmRingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake screen and show when locked
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val alarmLabel = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_LABEL) ?: "Будильник"

        setContent {
            NeoClockTheme {
                AlarmRingContent(
                    label = alarmLabel,
                    onDismiss = {
                        val serviceIntent = Intent(this, AlarmService::class.java).apply {
                            action = AlarmService.ACTION_DISMISS_ALARM
                        }
                        startService(serviceIntent)
                        finish()
                    },
                    onSnooze = {
                        val serviceIntent = Intent(this, AlarmService::class.java).apply {
                            action = AlarmService.ACTION_SNOOZE_ALARM
                        }
                        startService(serviceIntent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun AlarmRingContent(
    label: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    var currentTimeStr by remember { mutableStateOf(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            delay(1000L)
        }
    }

    // Bell shaking animation
    val infiniteTransition = rememberInfiniteTransition(label = "bellShake")
    val shakeRotation by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeRot"
    )

    // Glow pulse animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Swipe offset for dismissing
    var swipeOffsetY by remember { mutableFloatStateOf(0f) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(NeuBg)
            .draggable(
                state = rememberDraggableState { delta ->
                    swipeOffsetY = (swipeOffsetY + delta).coerceIn(-300f, 0f)
                },
                orientation = Orientation.Vertical,
                onDragStopped = {
                    if (swipeOffsetY < -120f) {
                        onDismiss()
                    } else {
                        swipeOffsetY = 0f
                    }
                }
            ),
        color = NeuBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, swipeOffsetY.roundToInt()) }
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Shaking Bell with pulsing halo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                // Pulsing glow circle
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(NeuBlue.copy(alpha = 0.45f), Color.Transparent)
                            )
                        )
                )

                // Bell Icon inside raised circle
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .neuRaised(cornerRadius = 45.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Ringing",
                        tint = NeuBlue2,
                        modifier = Modifier
                            .size(46.dp)
                            .rotate(shakeRotation)
                    )
                }
            }

            // Big Gradient Digital Time
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentTimeStr,
                    style = TextStyle(
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 62.sp,
                        letterSpacing = 0.06.sp,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFE8E8EA), Color(0xFF8D8E92))
                        )
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge,
                    color = NeuMuted,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "↑ Смахните вверх, чтобы отключить",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeuMuted.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            // Bottom action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Snooze button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .neuClickable(cornerRadius = 18.dp, onClick = onSnooze)
                        .testTag("snooze_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ещё 5 мин",
                        color = NeuText,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }

                // Dismiss button (.pill.go gradient)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF4A7DFF), Color(0xFF2253D6))
                            )
                        )
                        .neuClickable(cornerRadius = 18.dp, onClick = onDismiss)
                        .testTag("dismiss_alarm_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Выключить",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
