package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrickSurface
import com.example.ui.theme.BrickSurfaceBorder
import com.example.ui.theme.BrickSurfaceElevated
import com.example.ui.theme.CyberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonVioletGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun BrickLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showText: Boolean = true
) {
    // 3 stacked bricks animation
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val colCyan = ElectricCyan
    val colViolet = NeonViolet
    val colVioletGlow = NeonVioletGlow
    val colGold = CyberGold

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            val brickH = h * 0.22f
            val corner = 4.dp.toPx()

            // Brick 1 (Bottom wide base foundation)
            val b1W = w * 0.90f
            val b1X = (w - b1W) / 2f
            val b1Y = h - brickH - (h * 0.05f)
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(colCyan, colViolet)),
                topLeft = Offset(b1X, b1Y * animProgress.value),
                size = Size(b1W, brickH),
                cornerRadius = CornerRadius(corner, corner)
            )

            // Brick 2 (Middle brick)
            val b2W = w * 0.65f
            val b2X = (w - b2W) / 2f
            val b2Y = b1Y - brickH - (h * 0.06f)
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(colVioletGlow, colGold)),
                topLeft = Offset(b2X, b2Y * animProgress.value),
                size = Size(b2W, brickH),
                cornerRadius = CornerRadius(corner, corner)
            )

            // Brick 3 (Top summit brick)
            val b3W = w * 0.40f
            val b3X = (w - b3W) / 2f
            val b3Y = b2Y - brickH - (h * 0.06f)
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(colGold, colCyan)),
                topLeft = Offset(b3X, b3Y * animProgress.value),
                size = Size(b3W, brickH),
                cornerRadius = CornerRadius(corner, corner)
            )
        }

        if (showText) {
            Column {
                Text(
                    text = "BRICK",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp,
                    color = TextWhite
                )
                Text(
                    text = "FONDATIONS FINANCIÈRES",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 8.sp,
                    letterSpacing = 1.sp,
                    color = ElectricCyan
                )
            }
        }
    }
}

@Composable
fun NeonGlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = GlassBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val cardModifier = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    BrickSurfaceElevated.copy(alpha = 0.90f),
                    BrickSurface.copy(alpha = 0.85f)
                )
            )
        )
        .border(
            width = 1.2.dp,
            brush = Brush.linearGradient(
                colors = listOf(borderColor, BrickSurfaceBorder, Color.Transparent)
            ),
            shape = shape
        )
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        .padding(18.dp)

    Box(modifier = cardModifier) {
        content()
    }
}

@Composable
fun OnlineStatusBadge(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val statusColor = if (isOnline) EmeraldSuccess else CyberGold
    val statusText = if (isOnline) "En ligne (IA+)" else "Mode Hors-ligne"

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BrickSurfaceElevated.copy(alpha = 0.7f))
            .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Text(
            text = statusText,
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PrivacyAmountText(
    amount: Double,
    currency: String,
    hideAmounts: Boolean,
    modifier: Modifier = Modifier,
    fontSize: Int = 28,
    color: Color = TextWhite,
    fontWeight: FontWeight = FontWeight.Bold
) {
    val display = if (hideAmounts) "•••• $currency" else "${String.format("%,.2f", amount).replace(',', '.')} $currency"
    Text(
        text = display,
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier
    )
}

@Composable
fun StreakPill(streakDays: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(CyberGold.copy(alpha = 0.2f), NeonViolet.copy(alpha = 0.2f))))
            .border(1.dp, CyberGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = "🔥", fontSize = 13.sp)
        Text(
            text = "$streakDays j",
            color = CyberGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
