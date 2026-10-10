package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.FinancialSnapshot
import com.example.data.model.TransactionEntity
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrickSurfaceBorder
import com.example.ui.theme.BrickSurfaceElevated
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.CyberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeedColor
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonVioletGlow
import com.example.ui.theme.SaveColor
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WantColor
import java.util.Calendar
import kotlin.math.max

@Composable
fun HealthScoreGauge(
    score: Int,
    grade: String,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(score) {
        animProgress.animateTo(
            targetValue = score / 100f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    val scoreColor = when {
        score >= 80 -> EmeraldSuccess
        score >= 50 -> CyberGold
        else -> CoralDanger
    }

    val trackColor = BrickSurfaceBorder
    val colCyan = ElectricCyan
    val colVioletGlow = NeonVioletGlow

    Box(
        modifier = modifier.size(130.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val strokeWidth = 10.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter, diameter)

            // Background circle track
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Animated score arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(colCyan, colVioletGlow, scoreColor)
                ),
                startAngle = 135f,
                sweepAngle = 270f * animProgress.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(score * animProgress.value).toInt()}",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite
            )
            Text(
                text = "/100",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun DonutBreakdownChart(
    needsAmount: Double,
    wantsAmount: Double,
    savingsAmount: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    val total = max(1.0, needsAmount + wantsAmount + savingsAmount)
    val needsRatio = (needsAmount / total).toFloat()
    val wantsRatio = (wantsAmount / total).toFloat()
    val savingsRatio = (savingsAmount / total).toFloat()

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(needsAmount, wantsAmount, savingsAmount) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val colNeed = NeedColor
        val colWant = WantColor
        val colSave = SaveColor

        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(100.dp)) {
                val strokeWidth = 18.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                val arcSize = Size(diameter, diameter)

                var currentAngle = -90f

                // Needs Arc
                val sweepNeeds = 360f * needsRatio * animProgress.value
                if (sweepNeeds > 0f) {
                    drawArc(
                        color = colNeed,
                        startAngle = currentAngle,
                        sweepAngle = sweepNeeds,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )
                    currentAngle += sweepNeeds
                }

                // Wants Arc
                val sweepWants = 360f * wantsRatio * animProgress.value
                if (sweepWants > 0f) {
                    drawArc(
                        color = colWant,
                        startAngle = currentAngle,
                        sweepAngle = sweepWants,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )
                    currentAngle += sweepWants
                }

                // Savings Arc
                val sweepSavings = 360f * savingsRatio * animProgress.value
                if (sweepSavings > 0f) {
                    drawArc(
                        color = colSave,
                        startAngle = currentAngle,
                        sweepAngle = sweepSavings,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(total).toInt()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = currency,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        // Legend
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LegendRow(
                color = NeedColor,
                title = "Besoins (50%)",
                amount = needsAmount,
                percent = (needsRatio * 100).toInt(),
                currency = currency
            )
            LegendRow(
                color = WantColor,
                title = "Envies (30%)",
                amount = wantsAmount,
                percent = (wantsRatio * 100).toInt(),
                currency = currency
            )
            LegendRow(
                color = SaveColor,
                title = "Épargne / Actifs (20%)",
                amount = savingsAmount,
                percent = (savingsRatio * 100).toInt(),
                currency = currency
            )
        }
    }
}

@Composable
private fun LegendRow(
    color: Color,
    title: String,
    amount: Double,
    percent: Int,
    currency: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column {
            Text(text = title, fontSize = 11.sp, color = TextWhite, fontWeight = FontWeight.Medium)
            Text(
                text = "${amount.toInt()} $currency ($percent%)",
                fontSize = 11.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SpendingTrendCurve(
    transactions: List<TransactionEntity>,
    monthlyBudget: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    val cal = Calendar.getInstance()
    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = cal.get(Calendar.DAY_OF_MONTH)

    // Compute cumulative sum per day
    val dailySums = DoubleArray(maxDays + 1)
    val currentMonth = cal.get(Calendar.MONTH)
    val currentYear = cal.get(Calendar.YEAR)

    for (t in transactions) {
        val tCal = Calendar.getInstance().apply { timeInMillis = t.timestamp }
        if (tCal.get(Calendar.MONTH) == currentMonth && tCal.get(Calendar.YEAR) == currentYear) {
            val d = tCal.get(Calendar.DAY_OF_MONTH)
            if (d in 1..maxDays) {
                dailySums[d] += t.amount
            }
        }
    }

    val cumulative = DoubleArray(currentDay + 1)
    var running = 0.0
    for (d in 1..currentDay) {
        running += dailySums[d]
        cumulative[d] = running
    }

    val maxVal = max(monthlyBudget * 1.1, running * 1.1)

    val colBudgetLine = CoralDanger.copy(alpha = 0.6f)
    val colGridBorder = BrickSurfaceBorder
    val colCyan = ElectricCyan
    val colVioletGlow = NeonVioletGlow

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(vertical = 8.dp)
        ) {
            val w = size.width
            val h = size.height

            // Budget target reference horizontal dashed line
            val budgetY = (h - ((monthlyBudget / maxVal) * h)).toFloat().coerceIn(10f, h - 10f)
            drawLine(
                color = colBudgetLine,
                start = Offset(0f, budgetY),
                end = Offset(w, budgetY),
                strokeWidth = 2.dp.toPx()
            )

            // Draw grid guide lines
            drawLine(
                color = colGridBorder,
                start = Offset(0f, h),
                end = Offset(w, h),
                strokeWidth = 1.dp.toPx()
            )

            if (currentDay > 1) {
                val path = Path()
                val stepX = w / maxDays

                for (d in 1..currentDay) {
                    val x = (d - 1) * stepX
                    val y = (h - ((cumulative[d] / maxVal) * h)).toFloat().coerceIn(5f, h - 5f)
                    if (d == 1) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(listOf(colCyan, colVioletGlow)),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Fill area under curve
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo((currentDay - 1) * stepX, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(colCyan.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "1er du mois", fontSize = 10.sp, color = TextMuted)
            Text(
                text = "Plafond budget : ${monthlyBudget.toInt()} $currency",
                fontSize = 10.sp,
                color = CoralDanger,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = "Jour $maxDays", fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
fun WeeklyHeatmap(
    transactions: List<TransactionEntity>,
    currency: String = "FCFA",
    modifier: Modifier = Modifier
) {
    // 7 days (Lundi to Dimanche)
    val dayNames = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
    val dayTotals = DoubleArray(7)

    for (t in transactions) {
        val c = Calendar.getInstance().apply { timeInMillis = t.timestamp }
        val dayOfWeek = c.get(Calendar.DAY_OF_WEEK) // Sunday = 1, Monday = 2
        val index = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
        if (index in 0..6) {
            dayTotals[index] += t.amount
        }
    }

    val maxTotal = max(1.0, dayTotals.maxOrNull() ?: 1.0)
    val peakDayIndex = dayTotals.indices.maxByOrNull { dayTotals[it] } ?: 4

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dayNames.forEachIndexed { i, name ->
                val ratio = (dayTotals[i] / maxTotal).toFloat()
                val cellColor = when {
                    ratio > 0.7f -> CoralDanger
                    ratio > 0.4f -> AmberWarning
                    ratio > 0.15f -> ElectricCyan
                    else -> BrickSurfaceElevated
                }

                val amountBadgeText = when {
                    dayTotals[i] == 0.0 -> "-"
                    dayTotals[i] >= 10000 -> "${(dayTotals[i] / 1000).toInt()}k"
                    dayTotals[i] >= 1000 -> "${String.format(java.util.Locale.FRENCH, "%.1f", dayTotals[i] / 1000)}k"
                    else -> "${dayTotals[i].toInt()}"
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(cellColor.copy(alpha = max(0.2f, ratio)))
                            .border(
                                1.dp,
                                if (i == peakDayIndex && dayTotals[i] > 0) CyberGold else BrickSurfaceBorder,
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = amountBadgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (ratio > 0.4f) TextWhite else TextMuted
                        )
                    }
                    Text(
                        text = name,
                        fontSize = 10.sp,
                        color = if (i == peakDayIndex) CyberGold else TextMuted,
                        fontWeight = if (i == peakDayIndex) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        val peakAmountFormatted = String.format(java.util.Locale.FRENCH, "%,d", dayTotals[peakDayIndex].toLong()).replace('\u00A0', ' ')
        Text(
            text = "Pic de dépenses détecté le ${dayNames[peakDayIndex]} ($peakAmountFormatted $currency).",
            fontSize = 11.sp,
            color = CyberGold,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ExplanationCalloutCard(
    text: String,
    modifier: Modifier = Modifier
) {
    NeonGlassCard(
        modifier = modifier,
        borderColor = ElectricCyan.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan.copy(alpha = 0.15f))
                    .border(1.dp, ElectricCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Ce que ça veut dire",
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CE QUE ÇA VEUT DIRE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ElectricCyan,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = text,
                    fontSize = 13.sp,
                    color = TextWhite,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
