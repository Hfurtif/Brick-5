package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrickBackground
import com.example.ui.theme.BrickSurface
import com.example.ui.theme.CyberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonVioletGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BrickAnimatedSplashScreen(
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation states
    val iconScale = remember { Animatable(0f) }
    val iconAlpha = remember { Animatable(0f) }
    val brick1Progress = remember { Animatable(0f) }
    val brick2Progress = remember { Animatable(0f) }
    val brick3Progress = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textScale = remember { Animatable(0.85f) }
    val glowPulse = remember { Animatable(0.2f) }

    // Infinite ambient rotation for background orbital ring
    val infiniteTransition = rememberInfiniteTransition(label = "halo")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit"
    )

    LaunchedEffect(Unit) {
        // Step 1: Pop in icon frame with spring
        launch {
            iconAlpha.animateTo(1f, tween(300))
        }
        launch {
            iconScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        delay(150)
        // Step 2: Brick 1 (base foundation) animates into place
        launch {
            brick1Progress.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
            )
        }

        delay(120)
        // Step 3: Brick 2 (middle) drops in
        launch {
            brick2Progress.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
            )
        }

        delay(120)
        // Step 4: Brick 3 (summit gold) clicks into place
        launch {
            brick3Progress.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium)
            )
        }

        delay(100)
        // Step 5: Glow burst and brand title fade-in
        launch {
            glowPulse.animateTo(0.85f, tween(400, easing = FastOutSlowInEasing))
        }
        launch {
            textAlpha.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        }
        launch {
            textScale.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        }

        // Hold for pleasant visual confirmation
        delay(850)

        // Callback to fade out into main app
        onAnimationComplete()
    }

    val colBackground = BrickBackground
    val colSurface = BrickSurface
    val colCyan = ElectricCyan
    val colViolet = NeonViolet
    val colVioletGlow = NeonVioletGlow
    val colGold = CyberGold
    val colWhite = TextWhite
    val colMuted = TextMuted

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        colViolet.copy(alpha = 0.18f),
                        colSurface.copy(alpha = 0.95f),
                        colBackground
                    ),
                    center = Offset(0.5f, 0.45f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Icon container with rotating orbital halo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(150.dp)
                    .scale(iconScale.value)
                    .alpha(iconAlpha.value)
            ) {
                // Orbital gradient ring
                Canvas(
                    modifier = Modifier
                        .size(144.dp)
                        .rotate(orbitAngle)
                ) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(
                                colCyan.copy(alpha = 0.8f),
                                colVioletGlow.copy(alpha = 0.6f),
                                colGold.copy(alpha = 0.4f),
                                Color.Transparent,
                                colCyan.copy(alpha = 0.8f)
                            )
                        ),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Ambient Radial Glow
                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    colCyan.copy(alpha = glowPulse.value * 0.4f),
                                    colViolet.copy(alpha = glowPulse.value * 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Central Icon Hexa-Card Container
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    colSurface.copy(alpha = 0.95f),
                                    colBackground
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                listOf(colCyan, colViolet, colGold)
                            ),
                            shape = RoundedCornerShape(26.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Animated 3-Layer Bricks Canvas
                    Canvas(modifier = Modifier.size(56.dp)) {
                        val w = size.width
                        val h = size.height
                        val brickHeight = h * 0.22f
                        val cornerPx = 4.dp.toPx()

                        // Brick 1 (Foundation Base)
                        val b1W = w * 0.90f
                        val b1X = (w - b1W) / 2f
                        val b1TargetY = h - brickHeight - (h * 0.08f)
                        val b1StartY = h + 20f
                        val b1Y = b1StartY + (b1TargetY - b1StartY) * brick1Progress.value
                        if (brick1Progress.value > 0.01f) {
                            drawRoundRect(
                                brush = Brush.horizontalGradient(listOf(colCyan, colViolet)),
                                topLeft = Offset(b1X, b1Y),
                                size = Size(b1W, brickHeight),
                                cornerRadius = CornerRadius(cornerPx, cornerPx)
                            )
                        }

                        // Brick 2 (Middle Layer)
                        val b2W = w * 0.66f
                        val b2X = (w - b2W) / 2f
                        val b2TargetY = b1TargetY - brickHeight - (h * 0.08f)
                        val b2StartY = -20f
                        val b2Y = b2StartY + (b2TargetY - b2StartY) * brick2Progress.value
                        if (brick2Progress.value > 0.01f) {
                            drawRoundRect(
                                brush = Brush.horizontalGradient(listOf(colVioletGlow, colGold)),
                                topLeft = Offset(b2X, b2Y),
                                size = Size(b2W, brickHeight),
                                cornerRadius = CornerRadius(cornerPx, cornerPx)
                            )
                        }

                        // Brick 3 (Summit Gold Brick)
                        val b3W = w * 0.42f
                        val b3X = (w - b3W) / 2f
                        val b3TargetY = b2TargetY - brickHeight - (h * 0.08f)
                        val b3StartY = -40f
                        val b3Y = b3StartY + (b3TargetY - b3StartY) * brick3Progress.value
                        if (brick3Progress.value > 0.01f) {
                            drawRoundRect(
                                brush = Brush.horizontalGradient(listOf(colGold, colCyan)),
                                topLeft = Offset(b3X, b3Y),
                                size = Size(b3W, brickHeight),
                                cornerRadius = CornerRadius(cornerPx, cornerPx)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Brand Typography with staggered scale and alpha
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .scale(textScale.value)
                    .alpha(textAlpha.value)
            ) {
                Text(
                    text = "BRICK",
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    letterSpacing = 6.sp,
                    color = colWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "FONDATIONS FINANCIÈRES",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    color = colCyan
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Discipline • Actifs vs Passifs • Reste à Vivre",
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = colMuted,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
