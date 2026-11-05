package com.neo.clock.ui.modifiers

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neo.clock.ui.theme.NeuDarkShadow
import com.neo.clock.ui.theme.NeuLightShadow
import com.neo.clock.ui.theme.NeuSunkLight
import com.neo.clock.ui.theme.NeuSurfaceRaisedBottom
import com.neo.clock.ui.theme.NeuSurfaceRaisedTop
import com.neo.clock.ui.theme.NeuSurfaceSunk

fun Modifier.neuRaised(
    cornerRadius: Dp = 20.dp,
    surfaceGradient: Brush = Brush.linearGradient(
        colors = listOf(NeuSurfaceRaisedTop, NeuSurfaceRaisedBottom),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    ),
    darkShadowColor: Color = NeuDarkShadow,
    lightShadowColor: Color = NeuLightShadow,
    shadowOffset: Dp = 4.dp,
    blurRadius: Dp = 8.dp
): Modifier = this.drawBehind {
    val crPx = cornerRadius.toPx()
    val shadowOffsetPx = shadowOffset.toPx()
    val blurPx = blurRadius.toPx()

    drawIntoCanvas { canvas ->
        val nativeCanvas = canvas.nativeCanvas

        // 1. Dark bottom-right shadow
        val darkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkShadowColor.copy(alpha = 0.9f).toArgb()
            if (blurPx > 0f) {
                maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
            }
        }
        val darkRect = RectF(
            shadowOffsetPx,
            shadowOffsetPx,
            size.width + shadowOffsetPx,
            size.height + shadowOffsetPx
        )
        nativeCanvas.drawRoundRect(darkRect, crPx, crPx, darkPaint)

        // 2. Subtle light top-left shadow - soft alpha so it doesn't cause white glow
        val lightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lightShadowColor.copy(alpha = 0.35f).toArgb()
            if (blurPx > 0f) {
                maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
            }
        }
        val lightRect = RectF(
            -shadowOffsetPx,
            -shadowOffsetPx,
            size.width - shadowOffsetPx,
            size.height - shadowOffsetPx
        )
        nativeCanvas.drawRoundRect(lightRect, crPx, crPx, lightPaint)
    }

    // 3. Raised surface gradient
    drawRoundRect(
        brush = surfaceGradient,
        size = size,
        cornerRadius = CornerRadius(crPx, crPx)
    )
}

fun Modifier.neuSunk(
    cornerRadius: Dp = 20.dp,
    surfaceColor: Color = NeuSurfaceSunk,
    darkShadowColor: Color = NeuDarkShadow,
    lightShadowColor: Color = NeuSunkLight,
    shadowDepth: Dp = 5.dp
): Modifier = this.drawBehind {
    val crPx = cornerRadius.toPx()
    val depthPx = shadowDepth.toPx()

    // Base sunk surface
    drawRoundRect(
        color = surfaceColor,
        size = size,
        cornerRadius = CornerRadius(crPx, crPx)
    )

    // Inner top-left dark shadow
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(darkShadowColor.copy(alpha = 0.9f), Color.Transparent),
            start = Offset(0f, 0f),
            end = Offset(depthPx * 2, depthPx * 2)
        ),
        size = size,
        cornerRadius = CornerRadius(crPx, crPx)
    )

    // Inner bottom-right light highlight
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(Color.Transparent, lightShadowColor.copy(alpha = 0.35f)),
            start = Offset(size.width - depthPx * 2, size.height - depthPx * 2),
            end = Offset(size.width, size.height)
        ),
        size = size,
        cornerRadius = CornerRadius(crPx, crPx)
    )
}

fun Modifier.neuClickable(
    cornerRadius: Dp = 20.dp,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedElevation by animateFloatAsState(
        targetValue = if (isPressed) 0f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "neuElevation"
    )

    val mod = if (animatedElevation > 0.5f) {
        Modifier.neuRaised(
            cornerRadius = cornerRadius,
            shadowOffset = (6 * animatedElevation).dp,
            blurRadius = (12 * animatedElevation).dp
        )
    } else {
        Modifier.neuSunk(cornerRadius = cornerRadius)
    }

    this
        .then(mod)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}
