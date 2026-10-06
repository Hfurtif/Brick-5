package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.security.BiometricAuthManager
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrickBackground
import com.example.ui.theme.BrickSurface
import com.example.ui.theme.BrickSurfaceBorder
import com.example.ui.theme.BrickSurfaceElevated
import com.example.ui.theme.CyberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay

@Composable
fun BiometricLockScreen(
    userPinCode: String,
    onUnlockSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPinFallback by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    fun launchBiometricPrompt() {
        if (activity != null) {
            errorMessage = null
            BiometricAuthManager.promptBiometric(
                activity = activity,
                title = "BRICK • Protection Financière",
                subtitle = "Place ton doigt sur le lecteur d'empreinte digitale",
                onSuccess = {
                    errorMessage = null
                    onUnlockSuccess()
                },
                onError = { err ->
                    errorMessage = err
                }
            )
        } else {
            errorMessage = "Vérification biométrique indisponible"
        }
    }

    // Auto-launch biometric prompt when lock screen appears
    LaunchedEffect(Unit) {
        delay(350)
        launchBiometricPrompt()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrickBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Animated Lock & Fingerprint Icon
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                ElectricCyan.copy(alpha = 0.25f),
                                BrickSurfaceElevated
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(ElectricCyan, NeonViolet)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Empreinte",
                    tint = ElectricCyan,
                    modifier = Modifier.size(56.dp)
                )
            }

            // Title & Subtitle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BRICK VÉRIFICATION",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Application verrouillée pour ta sécurité financière.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Utilise ton empreinte ou le code/schéma de ton smartphone.",
                    fontSize = 12.sp,
                    color = ElectricCyan,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }

            // Error display
            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BrickSurface)
                        .border(1.dp, AmberWarning.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 12.sp,
                        color = AmberWarning,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Main Unlock Button
            Button(
                onClick = { launchBiometricPrompt() },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("unlock_biometric_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = BrickBackground,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DÉVERROUILLER PAR EMPREINTE",
                    color = BrickBackground,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }

            // Fallback PIN toggle or keypad
            if (showPinFallback) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(BrickSurface)
                        .border(1.dp, BrickSurfaceBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Saisis ton code PIN BRICK",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            if (it.length <= 6) {
                                enteredPin = it
                                pinError = false
                            }
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (userPinCode.isNotBlank() && enteredPin == userPinCode) {
                                    onUnlockSuccess()
                                } else if (userPinCode.isBlank() && enteredPin.isNotBlank()) {
                                    // Default pin if none configured
                                    onUnlockSuccess()
                                } else {
                                    pinError = true
                                }
                            }
                        ),
                        placeholder = { Text("Code PIN", color = TextMuted) },
                        singleLine = true,
                        isError = pinError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = BrickSurfaceBorder,
                            focusedContainerColor = BrickSurfaceElevated,
                            unfocusedContainerColor = BrickSurfaceElevated
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("pin_code_input")
                    )

                    if (pinError) {
                        Text(
                            text = "Code PIN incorrect",
                            color = AmberWarning,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (userPinCode.isNotBlank() && enteredPin == userPinCode) {
                                onUnlockSuccess()
                            } else if (userPinCode.isBlank() && enteredPin.isNotBlank()) {
                                onUnlockSuccess()
                            } else {
                                pinError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("VALIDER LE CODE PIN", color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                TextButton(
                    onClick = { showPinFallback = true },
                    modifier = Modifier.testTag("toggle_pin_fallback_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Password,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Utiliser un code PIN de secours",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
