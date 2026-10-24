package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.CoachMessage
import com.example.ui.theme.*

@Composable
fun CoachAiScreen(
    messages: List<CoachMessage>,
    isTyping: Boolean,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val quickQuestions = listOf(
        "Comment optimiser ma caisse ?",
        "Explique-moi la règle 50/30/20",
        "Comment bien gérer ma tontine ?",
        "Quel est mon reste à vivre ?"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrickBackground)
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🤖", fontSize = 26.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Coach Financier BRICK",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrickTextPrimary
                    )
                    Text(
                        text = "Conseils personnalisés & stratégie offline-first",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrickTextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                reverseLayout = false
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isCoach = msg.sender == "COACH"
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isCoach) Alignment.CenterStart else Alignment.CenterEnd
                    ) {
                        Surface(
                            modifier = Modifier.widthIn(max = 320.dp),
                            color = if (isCoach) BrickSurfaceElevated else BrickPrimary,
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isCoach) 4.dp else 16.dp,
                                bottomEnd = if (isCoach) 16.dp else 4.dp
                            ),
                            border = if (isCoach) BorderStroke(1.dp, BrickSurfaceBorder) else null
                        ) {
                            Text(
                                text = msg.text,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isCoach) BrickTextPrimary else Color(0xFF0A0E1A),
                                lineHeight = 19.sp,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                if (isTyping) {
                    item {
                        Surface(
                            color = BrickSurfaceElevated,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Le Coach analyse vos finances...",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = BrickPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Question Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickQuestions.take(2).forEach { q ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSendMessage(q) },
                        color = BrickSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BrickSurfaceBorder)
                    ) {
                        Text(
                            text = q,
                            modifier = Modifier.padding(6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = BrickPrimary,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("coach_input"),
                    placeholder = { Text("Posez votre question financière...", color = BrickTextMuted, fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText.trim())
                                inputText = ""
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrickPrimary,
                        unfocusedBorderColor = BrickSurfaceBorder,
                        focusedTextColor = BrickTextPrimary,
                        unfocusedTextColor = BrickTextPrimary
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText.trim())
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(BrickPrimary, RoundedCornerShape(24.dp))
                        .testTag("send_coach_msg_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer", tint = Color(0xFF0A0E1A))
                }
            }
        }
    }
}
