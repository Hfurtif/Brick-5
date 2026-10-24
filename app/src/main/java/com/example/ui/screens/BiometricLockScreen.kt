package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.security.BiometricAuthManager
import com.example.ui.components.BrickLogo
import com.example.ui.theme.*

@Composable
fun BiometricLockScreen(
    pinCode: String,
    biometricEnabled: Boolean,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authManager = remember { BiometricAuthManager(context) }

    LaunchedEffect(biometricEnabled) {
        if (biometricEnabled && authManager.isBiometricAvailable() && context is FragmentActivity) {
            authManager.promptBiometric(
                activity = context,
                onSuccess = onUnlocked,
                onError = { err -> errorMessage = err }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrickBackground)
            .padding(24.dp)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrickLogo(size = 72.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "BRICK VERROUILLÉ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BrickTextPrimary,
                letterSpacing = 2.sp
            )
            Text(
                text = "Entrez votre code secret pour déverrouiller vos finances",
                style = MaterialTheme.typography.bodySmall,
                color = BrickTextMuted
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = enteredPin,
                onValueChange = {
                    if (it.length <= 6) {
                        enteredPin = it
                        errorMessage = null
                        if (it == pinCode || pinCode.isEmpty()) {
                            onUnlocked()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pin_input"),
                placeholder = { Text("Code PIN", color = BrickTextMuted) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "PIN", tint = BrickPrimary) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrickPrimary,
                    unfocusedBorderColor = BrickSurfaceBorder,
                    focusedTextColor = BrickTextPrimary,
                    unfocusedTextColor = BrickTextPrimary
                )
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = CoralDanger,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (biometricEnabled && authManager.isBiometricAvailable() && context is FragmentActivity) {
                Button(
                    onClick = {
                        authManager.promptBiometric(
                            activity = context,
                            onSuccess = onUnlocked,
                            onError = { err -> errorMessage = err }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("biometric_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrickSurfaceElevated)
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = "Empreinte", tint = BrickPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Utiliser l'empreinte digitale", color = BrickTextPrimary)
                }
            }
        }
    }
}
