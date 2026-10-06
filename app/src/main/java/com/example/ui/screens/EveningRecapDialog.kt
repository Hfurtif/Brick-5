package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.notification.BrickNotificationHelper
import com.example.notification.DailyEveningRecapWorker
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrickBackground
import com.example.ui.theme.BrickSurface
import com.example.ui.theme.BrickSurfaceBorder
import com.example.ui.theme.BrickSurfaceElevated
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.CyberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeedColor
import com.example.ui.theme.NeonVioletGlow
import com.example.ui.theme.SaveColor
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WantColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

@Composable
fun EveningRecapDialog(
    todayTransactions: List<TransactionEntity>,
    todayTotal: Double,
    yesterdayTotal: Double,
    dailyBudget: Double,
    currency: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isOverDailyBudget = todayTotal > dailyBudget
    val diffYesterday = todayTotal - yesterdayTotal
    val diffPct = if (yesterdayTotal > 0) ((diffYesterday / yesterdayTotal) * 100).toInt() else 0

    val shortAdvice = when {
        todayTotal == 0.0 -> "Journée zéro dépense ! Tu as laissé ton portefeuille intact aujourd'hui, bravo."
        isOverDailyBudget -> "Aujourd'hui a dépassé ton quota journalier de ${(todayTotal - dailyBudget).toInt()} $currency. Compense dès demain en limitant strictement les envies."
        todayTotal <= dailyBudget * 0.75 -> "Excellente retenue ! Tu as dépensé moins que ton budget autorisé. Cette économie renforce tes actifs."
        else -> "Journée équilibrée et conforme à ton rythme de vie. Continue ainsi !"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrickSurfaceElevated,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🌙 Bilan du Soir",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "Récapitulatif de ta journée",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isOverDailyBudget) CoralDanger.copy(alpha = 0.2f) else EmeraldSuccess.copy(alpha = 0.2f))
                        .border(1.dp, if (isOverDailyBudget) CoralDanger else EmeraldSuccess, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isOverDailyBudget) "Hors quota" else "Sous contrôle",
                        color = if (isOverDailyBudget) CoralDanger else EmeraldSuccess,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Big total banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(BrickSurface)
                            .border(1.dp, BrickSurfaceBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "TOTAL DÉPENSÉ AUJOURD'HUI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${String.format("%,.2f", todayTotal).replace(',', '.')} $currency",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isOverDailyBudget) CoralDanger else ElectricCyan
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Progress bar vs daily budget
                            val progress = (todayTotal / max(1.0, dailyBudget)).toFloat().coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (isOverDailyBudget) CoralDanger else ElectricCyan,
                                trackColor = BrickSurfaceBorder
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Budget autorisé : ${dailyBudget.toInt()} $currency/j",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = if (isOverDailyBudget) "Dépassement : +${(todayTotal - dailyBudget).toInt()} $currency" else "Reste du jour : ${(dailyBudget - todayTotal).toInt()} $currency",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverDailyBudget) CoralDanger else EmeraldSuccess
                                )
                            }
                        }
                    }
                }

                // Comparison with yesterday
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrickSurface)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = if (diffYesterday > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (diffYesterday > 0) CoralDanger else EmeraldSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(text = "Comparé à hier :", fontSize = 12.sp, color = TextWhite)
                        }

                        Text(
                            text = if (yesterdayTotal == 0.0) "${todayTotal.toInt()} $currency aujourd'hui"
                            else "${if (diffYesterday >= 0) "+" else ""}${diffYesterday.toInt()} $currency (${if (diffYesterday >= 0) "+" else ""}$diffPct%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (diffYesterday > 0) CoralDanger else EmeraldSuccess
                        )
                    }
                }

                // Short AI advice
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(NeonVioletGlow.copy(alpha = 0.10f))
                            .border(1.dp, NeonVioletGlow.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonVioletGlow, modifier = Modifier.size(18.dp))
                            Column {
                                Text(
                                    text = "CONSEIL DU COACH BRICK",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonVioletGlow,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = shortAdvice,
                                    fontSize = 12.sp,
                                    color = TextWhite,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // Today's expenses list
                item {
                    Text(
                        text = "Détail de la journée (${todayTransactions.size}) :",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                if (todayTransactions.isEmpty()) {
                    item {
                        Text(
                            text = "Aucune dépense enregistrée aujourd'hui.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(todayTransactions) { tx ->
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val timeStr = timeFormat.format(Date(tx.timestamp))
                        val typeColor = when (tx.type) {
                            "BESOIN" -> NeedColor
                            "ENVIE" -> WantColor
                            "EPARGNE" -> SaveColor
                            else -> TextMuted
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrickSurface)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = tx.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                                Text(text = "$timeStr • ${tx.category} • ${tx.type}", fontSize = 10.sp, color = typeColor)
                            }
                            Text(
                                text = "-${String.format("%.2f", tx.amount)} $currency",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        DailyEveningRecapWorker.runImmediateTest(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrickSurfaceElevated),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CyberGold)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("send_test_notification_btn")
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = CyberGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tester WorkManager", color = CyberGold, fontSize = 11.sp)
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Fermer", color = BrickBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    )
}
