package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BrickLogo
import com.example.ui.theme.BrickBackground
import com.example.ui.theme.BrickPrimary
import com.example.ui.theme.BrickTextMuted
import kotlinx.coroutines.delay

@Composable
fun BrickAnimatedSplashScreen(
    onAnimationFinished: () -> Unit
) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
        delay(700)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrickBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            BrickLogo(size = 90.dp, showGlow = true)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "BRICK",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = BrickPrimary,
                letterSpacing = 4.sp,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Bâtissez votre forteresse financière",
                style = MaterialTheme.typography.bodyMedium,
                color = BrickTextMuted,
                fontSize = 13.sp
            )
        }
    }
}
