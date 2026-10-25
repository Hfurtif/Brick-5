package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.BudgetBucketBreakdown
import com.example.ui.viewmodel.BudgetCategoryType
import kotlin.math.cos
import kotlin.math.sin

/**
 * Recharts-inspired Sector / Pie / Donut Chart for Jetpack Compose.
 * Delivers web Recharts quality with native 60fps performance:
 * - Animated sector sweeps
 * - Interactive activeShape slice expansion on tap
 * - Floating custom Tooltip card (Recharts Tooltip)
 * - Interactive Legend with quick filters
 * - Seamless toggle between Target 50/30/20 allocation and Actual monthly expenses
 */
enum class RechartsChartDisplayMode {
    TARGET_ALLOCATION,
    ACTUAL_SPENT
}

data class RechartsSectorData(
    val category: BudgetCategoryType,
    val name: String,
    val value: Double,
    val percentage: Double,
    val color: Color,
    val secondaryColor: Color,
    val isExceeded: Boolean = false,
    val startAngle: Float = 0f,
    val sweepAngle: Float = 0f
)

@Composable
fun Recharts503020PieChart(
    needs: BudgetBucketBreakdown,
    wants: BudgetBucketBreakdown,
    savings: BudgetBucketBreakdown,
    currency: String,
    modifier: Modifier = Modifier,
    size: Dp = 230.dp,
    donutHoleRatio: Float = 0.58f,
    initialMode: RechartsChartDisplayMode = RechartsChartDisplayMode.ACTUAL_SPENT
) {
    var displayMode by remember { mutableStateOf(initialMode) }
    var selectedCategory by remember { mutableStateOf<BudgetCategoryType?>(null) }
    var tooltipSector by remember { mutableStateOf<RechartsSectorData?>(null) }

    val needsColor = NeedColor
    val wantsColor = WantColor
    val savingsColor = SaveColor
    val coralDangerColor = CoralDanger
    val surfaceElevatedColor = BrickSurfaceElevated

    val totalSpent = needs.spentAmount + wants.spentAmount + savings.spentAmount
    val totalTarget = needs.targetAmount + wants.targetAmount + savings.targetAmount

    // Recharts Animated sweep progression
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(displayMode) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    // Build Recharts sectors
    val sectors = remember(needs, wants, savings, displayMode, needsColor, wantsColor, savingsColor, coralDangerColor) {
        if (displayMode == RechartsChartDisplayMode.TARGET_ALLOCATION) {
            val total = if (totalTarget > 0) totalTarget else 1.0
            listOf(
                RechartsSectorData(
                    category = BudgetCategoryType.NEEDS,
                    name = "Besoins Vitaux (50%)",
                    value = needs.targetAmount,
                    percentage = 50.0,
                    color = needsColor,
                    secondaryColor = Color(0xFF0284C7)
                ),
                RechartsSectorData(
                    category = BudgetCategoryType.WANTS,
                    name = "Envies & Confort (30%)",
                    value = wants.targetAmount,
                    percentage = 30.0,
                    color = wantsColor,
                    secondaryColor = Color(0xFFA78BFA)
                ),
                RechartsSectorData(
                    category = BudgetCategoryType.SAVINGS,
                    name = "Épargne & Dettes (20%)",
                    value = savings.targetAmount,
                    percentage = 20.0,
                    color = savingsColor,
                    secondaryColor = Color(0xFFD97706)
                )
            )
        } else {
            val safeTotal = if (totalSpent > 0) totalSpent else 1.0
            listOf(
                RechartsSectorData(
                    category = BudgetCategoryType.NEEDS,
                    name = "Besoins (50%)",
                    value = needs.spentAmount,
                    percentage = if (totalSpent > 0) (needs.spentAmount / safeTotal) * 100.0 else 0.0,
                    color = if (needs.isExceeded) coralDangerColor else needsColor,
                    secondaryColor = if (needs.isExceeded) Color(0xFFB91C1C) else Color(0xFF0284C7),
                    isExceeded = needs.isExceeded
                ),
                RechartsSectorData(
                    category = BudgetCategoryType.WANTS,
                    name = "Envies (30%)",
                    value = wants.spentAmount,
                    percentage = if (totalSpent > 0) (wants.spentAmount / safeTotal) * 100.0 else 0.0,
                    color = if (wants.isExceeded) coralDangerColor else wantsColor,
                    secondaryColor = if (wants.isExceeded) Color(0xFFB91C1C) else Color(0xFFA78BFA),
                    isExceeded = wants.isExceeded
                ),
                RechartsSectorData(
                    category = BudgetCategoryType.SAVINGS,
                    name = "Épargne & Dettes (20%)",
                    value = savings.spentAmount,
                    percentage = if (totalSpent > 0) (savings.spentAmount / safeTotal) * 100.0 else 0.0,
                    color = if (savings.isExceeded) coralDangerColor else savingsColor,
                    secondaryColor = if (savings.isExceeded) Color(0xFFB91C1C) else Color(0xFFD97706),
                    isExceeded = savings.isExceeded
                )
            )
        }
    }

    // Assign start & sweep angles
    val angleSectors = remember(sectors, animationProgress.value) {
        var currentAngle = -90f
        sectors.map { sector ->
            val sweep = (sector.percentage.toFloat() / 100f) * 360f * animationProgress.value
            val item = sector.copy(startAngle = currentAngle, sweepAngle = sweep)
            currentAngle += sweep
            item
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BrickSurfaceElevated)
            .border(BorderStroke(1.dp, BrickSurfaceBorder), RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Switcher Header (Tab toggle like Recharts storybook)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(BrickSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clickable {
                        displayMode = RechartsChartDisplayMode.ACTUAL_SPENT
                        selectedCategory = null
                        tooltipSector = null
                    },
                color = if (displayMode == RechartsChartDisplayMode.ACTUAL_SPENT) BrickPrimary else Color.Transparent,
                shape = RoundedCornerShape(9.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "📊 Dépenses Réelles",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (displayMode == RechartsChartDisplayMode.ACTUAL_SPENT) Color(0xFF0A0E1A) else BrickTextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clickable {
                        displayMode = RechartsChartDisplayMode.TARGET_ALLOCATION
                        selectedCategory = null
                        tooltipSector = null
                    },
                color = if (displayMode == RechartsChartDisplayMode.TARGET_ALLOCATION) BrickPrimary else Color.Transparent,
                shape = RoundedCornerShape(9.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "🎯 Cible 50/30/20",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (displayMode == RechartsChartDisplayMode.TARGET_ALLOCATION) Color(0xFF0A0E1A) else BrickTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Pie / Donut Canvas with Recharts ActiveShape effect
        Box(
            modifier = Modifier
                .size(size)
                .pointerInput(angleSectors) {
                    detectTapGestures { tapOffset ->
                        val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                        val dx = tapOffset.x - center.x
                        val dy = tapOffset.y - center.y
                        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                        val outerRadius = size.toPx() / 2f
                        val innerRadius = outerRadius * donutHoleRatio

                        if (distance in innerRadius..outerRadius * 1.15f) {
                            var angle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < -90f) angle += 360f

                            val clickedSector = angleSectors.firstOrNull { sector ->
                                val start = sector.startAngle
                                val end = sector.startAngle + sector.sweepAngle
                                if (end > 270f && angle < 0f) {
                                    angle + 360f in start..end
                                } else {
                                    angle in start..end
                                }
                            }

                            if (clickedSector != null) {
                                selectedCategory = if (selectedCategory == clickedSector.category) null else clickedSector.category
                                tooltipSector = if (selectedCategory != null) clickedSector else null
                            }
                        } else {
                            selectedCategory = null
                            tooltipSector = null
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasSize = this.size.minDimension
                val strokeWidth = canvasSize * (1f - donutHoleRatio) / 2f
                val radius = (canvasSize - strokeWidth) / 2f
                val centerOffset = Offset(this.size.width / 2f, this.size.height / 2f)

                // Background track
                drawCircle(
                    color = Color(0x1494A3B8),
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = strokeWidth)
                )

                // Sectors
                angleSectors.forEach { sector ->
                    val isSelected = selectedCategory == sector.category
                    val effectiveStroke = if (isSelected) strokeWidth * 1.25f else strokeWidth

                    // Radial offset for selected slice (Recharts activeShape pop-out)
                    val midAngleRad = Math.toRadians((sector.startAngle + sector.sweepAngle / 2f).toDouble())
                    val offsetDistance = if (isSelected) 8.dp.toPx() else 0f
                    val sectorCenter = Offset(
                        x = centerOffset.x + (offsetDistance * cos(midAngleRad)).toFloat(),
                        y = centerOffset.y + (offsetDistance * sin(midAngleRad)).toFloat()
                    )

                    drawArc(
                        brush = Brush.radialGradient(
                            colors = listOf(sector.color, sector.secondaryColor),
                            center = sectorCenter,
                            radius = radius * 1.2f
                        ),
                        startAngle = sector.startAngle,
                        sweepAngle = sector.sweepAngle,
                        useCenter = false,
                        topLeft = Offset(sectorCenter.x - radius, sectorCenter.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = effectiveStroke, cap = StrokeCap.Butt)
                    )

                    // Sector divider gap (Recharts padAngle)
                    if (sector.sweepAngle > 4f) {
                        drawArc(
                            color = surfaceElevatedColor,
                            startAngle = sector.startAngle - 0.75f,
                            sweepAngle = 1.5f,
                            useCenter = false,
                            topLeft = Offset(sectorCenter.x - radius, sectorCenter.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = effectiveStroke + 2.dp.toPx())
                        )
                    }
                }
            }

            // Center Content (Recharts Custom Center Label / Tooltip Summary)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                val activeSector = tooltipSector ?: angleSectors.firstOrNull { it.category == selectedCategory }
                if (activeSector != null) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(activeSector.color)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = activeSector.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = BrickTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = "${activeSector.percentage.toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        color = activeSector.color,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "${String.format("%,.0f", activeSector.value)} $currency",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrickTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                } else {
                    Text(
                        text = if (displayMode == RechartsChartDisplayMode.TARGET_ALLOCATION) "CIBLE IDÉALE" else "TOTAL DU MOIS",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrickTextMuted,
                        letterSpacing = 1.sp,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (displayMode == RechartsChartDisplayMode.TARGET_ALLOCATION) "50/30/20" else String.format("%,.0f", totalSpent),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (totalSpent > totalTarget && totalTarget > 0 && displayMode == RechartsChartDisplayMode.ACTUAL_SPENT) CoralDanger else BrickTextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = if (displayMode == RechartsChartDisplayMode.TARGET_ALLOCATION) 20.sp else 18.sp
                    )
                    Text(
                        text = if (displayMode == RechartsChartDisplayMode.TARGET_ALLOCATION) "Équilibre Recommandé" else currency,
                        style = MaterialTheme.typography.labelSmall,
                        color = BrickTextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Recharts Interactive Legend (horizontal scrollable to avoid vertical letter crush!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            angleSectors.forEach { sector ->
                val isSelected = selectedCategory == sector.category
                Surface(
                    modifier = Modifier
                        .clickable {
                            selectedCategory = if (isSelected) null else sector.category
                            tooltipSector = if (selectedCategory != null) sector else null
                        },
                    color = if (isSelected) sector.color.copy(alpha = 0.2f) else BrickSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) sector.color else BrickSurfaceBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(sector.color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = sector.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = BrickTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${sector.percentage.toInt()}% • ${String.format("%,.0f", sector.value)} $currency",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (sector.isExceeded) CoralDanger else BrickTextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
