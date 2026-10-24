package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.BudgetBucketBreakdown
import com.example.ui.viewmodel.BudgetCategoryType

enum class Donut503020Mode {
    TARGET_ALLOCATION,
    ACTUAL_SPENT
}

data class DonutSliceModel(
    val category: BudgetCategoryType,
    val label: String,
    val amount: Double,
    val percentage: Double,
    val color: Color,
    val isExceeded: Boolean = false
)

@Composable
fun Donut503020Chart(
    needs: BudgetBucketBreakdown,
    wants: BudgetBucketBreakdown,
    savings: BudgetBucketBreakdown,
    currency: String,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    strokeWidth: Dp = 22.dp,
    mode: Donut503020Mode = Donut503020Mode.ACTUAL_SPENT
) {
    var selectedSlice by remember { mutableStateOf<BudgetCategoryType?>(null) }
    var animationPlayed by remember { mutableStateOf(false) }

    val animProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "donut_anim"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    val needsColor = NeedColor
    val wantsColor = WantColor
    val savingsColor = SaveColor
    val coralDangerColor = CoralDanger

    val totalSpent = needs.spentAmount + wants.spentAmount + savings.spentAmount
    val totalTarget = needs.targetAmount + wants.targetAmount + savings.targetAmount

    val slices = remember(needs, wants, savings, mode, needsColor, wantsColor, savingsColor, coralDangerColor) {
        if (mode == Donut503020Mode.TARGET_ALLOCATION) {
            listOf(
                DonutSliceModel(BudgetCategoryType.NEEDS, "Besoins (50%)", needs.targetAmount, 50.0, needsColor),
                DonutSliceModel(BudgetCategoryType.WANTS, "Envies (30%)", wants.targetAmount, 30.0, wantsColor),
                DonutSliceModel(BudgetCategoryType.SAVINGS, "Épargne (20%)", savings.targetAmount, 20.0, savingsColor)
            )
        } else {
            val safeTotal = if (totalSpent > 0) totalSpent else 1.0
            listOf(
                DonutSliceModel(
                    BudgetCategoryType.NEEDS,
                    "Besoins",
                    needs.spentAmount,
                    if (totalSpent > 0) (needs.spentAmount / safeTotal) * 100.0 else 0.0,
                    if (needs.isExceeded) coralDangerColor else needsColor,
                    needs.isExceeded
                ),
                DonutSliceModel(
                    BudgetCategoryType.WANTS,
                    "Envies",
                    wants.spentAmount,
                    if (totalSpent > 0) (wants.spentAmount / safeTotal) * 100.0 else 0.0,
                    if (wants.isExceeded) coralDangerColor else wantsColor,
                    wants.isExceeded
                ),
                DonutSliceModel(
                    BudgetCategoryType.SAVINGS,
                    "Épargne / Dettes",
                    savings.spentAmount,
                    if (totalSpent > 0) (savings.spentAmount / safeTotal) * 100.0 else 0.0,
                    if (savings.isExceeded) coralDangerColor else savingsColor,
                    savings.isExceeded
                )
            )
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasSize = this.size.minDimension
                val strokePx = strokeWidth.toPx()
                val radius = (canvasSize - strokePx) / 2f
                val centerOffset = Offset(this.size.width / 2f, this.size.height / 2f)

                // Draw background empty ring
                drawCircle(
                    color = Color(0x1A94A3B8),
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = strokePx)
                )

                if (totalSpent > 0 || mode == Donut503020Mode.TARGET_ALLOCATION) {
                    var startAngle = -90f
                    val sweepAngles = slices.map { slice ->
                        (slice.percentage.toFloat() / 100f) * 360f * animProgress
                    }

                    slices.forEachIndexed { index, slice ->
                        val sweep = sweepAngles[index]
                        val isSelected = selectedSlice == slice.category
                        val currentStroke = if (isSelected) strokePx * 1.25f else strokePx

                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = currentStroke, cap = StrokeCap.Round)
                        )
                        startAngle += sweep
                    }
                }
            }

            // Center details
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val active = selectedSlice?.let { cat -> slices.firstOrNull { it.category == cat } }
                if (active != null) {
                    Text(
                        text = active.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = BrickTextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${active.percentage.toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        color = active.color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = String.format("%,.0f %s", active.amount, currency),
                        style = MaterialTheme.typography.labelMedium,
                        color = BrickTextPrimary,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = if (mode == Donut503020Mode.TARGET_ALLOCATION) "Règle" else "Total Dépensé",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrickTextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = if (mode == Donut503020Mode.TARGET_ALLOCATION) "50/30/20" else String.format("%,.0f", totalSpent),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (totalSpent > totalTarget && totalTarget > 0) CoralDanger else BrickTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (mode == Donut503020Mode.TARGET_ALLOCATION) 18.sp else 16.sp
                    )
                    if (mode != Donut503020Mode.TARGET_ALLOCATION) {
                        Text(
                            text = currency,
                            style = MaterialTheme.typography.labelSmall,
                            color = BrickTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Legend chips with click feedback
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            slices.forEach { slice ->
                val isSelected = selectedSlice == slice.category
                Surface(
                    modifier = Modifier.clickable {
                        selectedSlice = if (isSelected) null else slice.category
                    },
                    color = if (isSelected) slice.color.copy(alpha = 0.2f) else BrickSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) slice.color else BrickSurfaceBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(slice.color, RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = slice.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = BrickTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${slice.percentage.toInt()}% • ${String.format("%,.0f", slice.amount)} $currency",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (slice.isExceeded) CoralDanger else BrickTextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
