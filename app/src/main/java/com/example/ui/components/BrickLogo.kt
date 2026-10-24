package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BrickPrimary
import com.example.ui.theme.BrickSecondary
import com.example.ui.theme.CyberGold

@Composable
fun BrickLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showGlow: Boolean = true
) {
    val primaryColor = BrickPrimary
    val goldColor = CyberGold
    val secondaryColor = BrickSecondary

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Outer Shield / Brick Foundation
            val brickHeight = h * 0.26f
            val brickWidth = w * 0.88f
            val corner = w * 0.12f

            // Bottom base brick (Savings / Fortress)
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(goldColor, Color(0xFFD97706))),
                topLeft = Offset((w - brickWidth) / 2f, h * 0.64f),
                size = Size(brickWidth, brickHeight),
                cornerRadius = CornerRadius(corner, corner)
            )

            // Middle brick left (Needs 50%)
            val halfBrick = (brickWidth - w * 0.08f) / 2f
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(primaryColor, Color(0xFF0284C7))),
                topLeft = Offset((w - brickWidth) / 2f, h * 0.34f),
                size = Size(halfBrick, brickHeight),
                cornerRadius = CornerRadius(corner * 0.8f, corner * 0.8f)
            )

            // Middle brick right (Wants 30%)
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(secondaryColor, Color(0xFFA78BFA))),
                topLeft = Offset((w - brickWidth) / 2f + halfBrick + w * 0.08f, h * 0.34f),
                size = Size(halfBrick, brickHeight),
                cornerRadius = CornerRadius(corner * 0.8f, corner * 0.8f)
            )

            // Top crown brick (Independence & Growth)
            val topWidth = brickWidth * 0.6f
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(primaryColor, goldColor)),
                topLeft = Offset((w - topWidth) / 2f, h * 0.06f),
                size = Size(topWidth, brickHeight * 0.9f),
                cornerRadius = CornerRadius(corner * 0.7f, corner * 0.7f)
            )

            if (showGlow) {
                drawRoundRect(
                    color = primaryColor.copy(alpha = 0.3f),
                    topLeft = Offset(w * 0.04f, h * 0.04f),
                    size = Size(w * 0.92f, h * 0.92f),
                    cornerRadius = CornerRadius(corner * 1.5f, corner * 1.5f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
