package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.BrickBadge
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@Composable
fun ProfileSecurityScreen(
    user: UserProfile?,
    badges: List<BrickBadge>,
    onUpdateProfile: (UserProfile) -> Unit,
    onResetData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrickBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .fillMaxHeight(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⚙️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Profil & Sécurité",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrickTextPrimary
                        )
                        Text(
                            text = "Paramètres de votre forteresse financière",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrickTextMuted
                        )
                    }
                }
            }

            // User Info Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = BrickSurfaceElevated,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BrickSurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Titulaire de la forteresse", style = MaterialTheme.typography.labelSmall, color = BrickTextMuted)
                                Text(user?.name ?: "Alexandre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrickTextPrimary)
                            }
                            Surface(
                                color = BrickPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = user?.currency ?: "FCFA",
                                    color = BrickPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = BrickSurfaceBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bilan du soir quotidien", style = MaterialTheme.typography.bodySmall, color = BrickTextMuted)
                            Text("${user?.dailyReminderHour ?: 20}h00", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrickTextPrimary)
                        }
                    }
                }
            }

            // Security Controls Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = BrickSurfaceElevated,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BrickSurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🛡️ Sécurité & Confidentialité", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrickTextPrimary)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Hide amounts toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Masquer les montants en public", style = MaterialTheme.typography.bodyMedium, color = BrickTextPrimary, fontWeight = FontWeight.SemiBold)
                                Text("Affiche des étoiles (••••••) pour éviter les regards indiscrets", style = MaterialTheme.typography.bodySmall, color = BrickTextMuted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = user?.hideAmounts ?: false,
                                onCheckedChange = { checked ->
                                    if (user != null) onUpdateProfile(user.copy(hideAmounts = checked))
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = BrickPrimary, checkedTrackColor = BrickPrimary.copy(alpha = 0.5f))
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = BrickSurfaceBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Biometrics toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Verrouillage par empreinte / biométrie", style = MaterialTheme.typography.bodyMedium, color = BrickTextPrimary, fontWeight = FontWeight.SemiBold)
                                Text("Exige l'empreinte digitale pour ouvrir l'application", style = MaterialTheme.typography.bodySmall, color = BrickTextMuted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = user?.biometricEnabled ?: false,
                                onCheckedChange = { checked ->
                                    if (user != null) onUpdateProfile(user.copy(biometricEnabled = checked))
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = BrickPrimary, checkedTrackColor = BrickPrimary.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }

            // Badges & Discipline
            item {
                Text(
                    text = "Badges & Discipline (${badges.count { it.isUnlocked }}/${badges.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyberGold
                )
            }

            items(badges, key = { it.id }) { badge ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (badge.isUnlocked) BrickSurfaceElevated else BrickSurface.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (badge.isUnlocked) CyberGold.copy(alpha = 0.4f) else BrickSurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = badge.icon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = badge.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (badge.isUnlocked) BrickTextPrimary else BrickTextMuted
                            )
                            Text(
                                text = badge.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = BrickTextMuted,
                                fontSize = 11.sp
                            )
                        }
                        if (badge.isUnlocked) {
                            Text(text = "Débloqué ✅", color = EmeraldSuccess, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Reset Data Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("reset_data_btn"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CoralDanger),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralDanger)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = CoralDanger)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Réinitialiser toutes les données", fontWeight = FontWeight.Bold)
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Confirmer la réinitialisation", fontWeight = FontWeight.Bold, color = BrickTextPrimary) },
            text = {
                Text(
                    "Êtes-vous certain de vouloir effacer l'historique des transactions, les actifs et repartir de zéro ? Cette action est irréversible.",
                    color = BrickTextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetData()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralDanger)
                ) {
                    Text("Tout effacer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Annuler", color = BrickTextPrimary)
                }
            },
            containerColor = BrickSurface
        )
    }
}
