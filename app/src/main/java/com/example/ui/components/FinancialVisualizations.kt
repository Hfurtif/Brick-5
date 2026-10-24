package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun HealthScoreGauge(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 130.dp
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(1000),
        label = "gauge_anim"
    )

    val primaryColor = BrickPrimary
    val gaugeColor = when {
        score >= 75 -> EmeraldSuccess
        score >= 50 -> CyberGold
        else -> CoralDanger
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val canvasSize = this.size.minDimension
            val radius = (canvasSize - strokeWidth) / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Background arc (240 degrees)
            drawArc(
                color = Color(0x2294A3B8),
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc
            val sweep = (animatedScore / 100f).coerceIn(0f, 1f) * 240f
            drawArc(
                brush = Brush.horizontalGradient(listOf(primaryColor, gaugeColor)),
                startAngle = 150f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${animatedScore.toInt()}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = gaugeColor,
                fontSize = 28.sp
            )
            Text(
                text = "/ 100",
                style = MaterialTheme.typography.labelSmall,
                color = BrickTextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun FinancialProgressBar(
    ratio: Double,
    modifier: Modifier = Modifier,
    color: Color = BrickPrimary,
    height: Dp = 8.dp
) {
    val safeRatio = ratio.coerceIn(0.0, 1.0).toFloat()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(Color(0x2694A3B8), RoundedCornerShape(height / 2))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(safeRatio)
                .background(color, RoundedCornerShape(height / 2))
        )
    }
}

@Composable
fun FormattedAmountText(
    amount: Double,
    currency: String,
    hideAmounts: Boolean,
    modifier: Modifier = Modifier,
    color: Color = BrickTextPrimary,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium,
    prefix: String = ""
) {
    val text = if (hideAmounts) {
        "•••••• $currency"
    } else {
        "$prefix${String.format("%,.0f", amount)} $currency"
    }

    Text(
        text = text,
        style = style,
        color = color,
        modifier = modifier
    )
}
