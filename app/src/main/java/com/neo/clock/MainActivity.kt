package com.neo.clock

import android.Manifest
import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neo.clock.ui.MainViewModel
import com.neo.clock.ui.components.PermissionBanner
import com.neo.clock.ui.screens.AlarmScreen
import com.neo.clock.ui.screens.TimerScreen
import com.neo.clock.ui.screens.WorldClockScreen
import com.neo.clock.ui.theme.NeoClockTheme
import com.neo.clock.ui.theme.NeuBg
import com.neo.clock.ui.theme.NeuBlue
import com.neo.clock.ui.theme.NeuBlue2
import com.neo.clock.ui.theme.NeuDarkShadow
import com.neo.clock.ui.theme.NeuLightShadow
import com.neo.clock.ui.theme.NeuMuted
import com.neo.clock.ui.theme.NeuSurfaceRaisedTop
import com.neo.clock.util.PermissionHelper
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NeoClockTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    var hasNotificationPermission by remember { mutableStateOf(true) }
    var hasExactAlarmPermission by remember { mutableStateOf(true) }

    val context = androidx.compose.ui.platform.LocalContext.current

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    LaunchedEffect(Unit) {
        hasNotificationPermission = PermissionHelper.hasNotificationPermission(context)
        hasExactAlarmPermission = PermissionHelper.hasExactAlarmPermission(context)

        // Automatically prompt for notification permission on Android 13+ if not granted
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = NeuBg,
        bottomBar = {
            // Smoothly animated Neumorphic Bottom Navigation Bar matching alarm.html
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 12.dp)
                    .testTag("neu_bottom_nav"),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val navItems = listOf(
                    Triple(0, Icons.Default.Public, "Мировое время"),
                    Triple(1, Icons.Default.Alarm, "Будильник"),
                    Triple(2, Icons.Default.HourglassBottom, "Таймер")
                )

                navItems.forEach { (pageIndex, icon, label) ->
                    // Calculate dynamic progress based on pager offset for real-time gesture tracking
                    val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue
                    val targetProgress = (1f - pageOffset).coerceIn(0f, 1f)

                    val animProgress by animateFloatAsState(
                        targetValue = targetProgress,
                        animationSpec = spring(
                            dampingRatio = 0.78f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "nav_progress_$pageIndex"
                    )

                    NeuNavMorphButton(
                        icon = icon,
                        label = label,
                        progress = animProgress,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(
                                    pageIndex,
                                    animationSpec = spring(
                                        dampingRatio = 0.85f,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("nav_button_$pageIndex")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Permission warning banner if permission is missing
            PermissionBanner(
                hasNotificationPermission = hasNotificationPermission,
                hasExactAlarmPermission = hasExactAlarmPermission,
                onRequestNotification = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onRequestExactAlarm = {
                    PermissionHelper.openExactAlarmSettings(context)
                }
            )

            // Smooth HorizontalPager with scale and alpha interpolation
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                val scale = 1f - (0.06f * pageOffset.coerceIn(0f, 1f))
                val alpha = 1f - (0.35f * pageOffset.coerceIn(0f, 1f))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                ) {
                    when (page) {
                        0 -> WorldClockScreen(state = state, viewModel = viewModel)
                        1 -> AlarmScreen(state = state, viewModel = viewModel)
                        2 -> TimerScreen(state = state, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

/**
 * Ultra-smooth morphing Neumorphic Navigation Button that smoothly blends
 * between raised (unselected) and sunk + blue glow (selected).
 */
@Composable
fun NeuNavMorphButton(
    icon: ImageVector,
    label: String,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .graphicsLayer {
                    val scale = 1f + (0.06f * progress)
                    scaleX = scale
                    scaleY = scale
                }
                .drawBehind {
                    val crPx = 20.dp.toPx()
                    val offsetPx = 5.dp.toPx() * (1f - progress)
                    val blurPx = 10.dp.toPx()

                    drawIntoCanvas { canvas ->
                        val nativeCanvas = canvas.nativeCanvas

                        // 1. Blue outer glow when active (alarm.html: 0 0 18px -2px rgba(47,107,255,.5))
                        if (progress > 0.05f) {
                            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = Color(0xFF2F6BFF).copy(alpha = 0.45f * progress).toArgb()
                                maskFilter = BlurMaskFilter(14.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                            }
                            val glowRect = RectF(0f, 0f, size.width, size.height)
                            nativeCanvas.drawRoundRect(glowRect, crPx, crPx, glowPaint)
                        }

                        // 2. Dark shadow for raised state (fades out as progress -> 1)
                        if (1f - progress > 0.05f) {
                            val darkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = NeuDarkShadow.copy(alpha = 1f - progress).toArgb()
                                maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
                            }
                            val darkRect = RectF(
                                offsetPx,
                                offsetPx,
                                size.width + offsetPx,
                                size.height + offsetPx
                            )
                            nativeCanvas.drawRoundRect(darkRect, crPx, crPx, darkPaint)

                            // 3. Light shadow for raised state
                            val lightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = NeuLightShadow.copy(alpha = (1f - progress) * 0.7f).toArgb()
                                maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
                            }
                            val lightRect = RectF(
                                -offsetPx,
                                -offsetPx,
                                size.width - offsetPx,
                                size.height - offsetPx
                            )
                            nativeCanvas.drawRoundRect(lightRect, crPx, crPx, lightPaint)
                        }
                    }

                    // 4. Interpolate surface color: #252628 -> #202A45 (active blue sunk tint)
                    val baseSurface = lerp(NeuSurfaceRaisedTop, Color(0xFF202A45), progress)
                    drawRoundRect(
                        color = baseSurface,
                        size = size,
                        cornerRadius = CornerRadius(crPx, crPx)
                    )

                    // 5. Inner sunk shadow when active (fades in as progress -> 1)
                    if (progress > 0.05f) {
                        val depthPx = 5.dp.toPx()
                        // Inner dark top-left shadow
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF0F1422).copy(alpha = 0.9f * progress), Color.Transparent),
                                start = Offset(0f, 0f),
                                end = Offset(depthPx * 2.2f, depthPx * 2.2f)
                            ),
                            size = size,
                            cornerRadius = CornerRadius(crPx, crPx)
                        )
                        // Inner light bottom-right highlight
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.Transparent, Color(0xFF3B568A).copy(alpha = 0.5f * progress)),
                                start = Offset(size.width - depthPx * 2, size.height - depthPx * 2),
                                end = Offset(size.width, size.height)
                            ),
                            size = size,
                            cornerRadius = CornerRadius(crPx, crPx)
                        )
                    }
                }
                .clip(RoundedCornerShape(20.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Icon color interpolates from NeuMuted (#85868A) to NeuBlue2 (#5B8DFF)
            val iconTint = lerp(NeuMuted, NeuBlue2, progress)
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        val iconScale = 1f + (0.08f * progress)
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Glowing active dot / indicator pill
        Box(
            modifier = Modifier
                .height(3.dp)
                .width((18 * progress).dp)
                .graphicsLayer {
                    alpha = progress
                }
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(NeuBlue, NeuBlue2, NeuBlue)
                    )
                )
        )
    }
}
