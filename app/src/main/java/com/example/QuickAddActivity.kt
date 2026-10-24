package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.db.BrickDatabase
import com.example.data.model.TransactionEntity
import com.example.data.repository.BrickRepository
import com.example.ui.screens.UltraFastExpenseSheet
import com.example.ui.theme.BrickBackground
import com.example.ui.theme.BrickTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuickAddActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = BrickDatabase.getInstance(applicationContext)
        val repository = BrickRepository(db.userDao(), db.transactionDao(), db.goalDao(), db.assetDao())

        setContent {
            val userProfile by repository.userProfile.collectAsState(initial = null)
            val currency = userProfile?.currency ?: "FCFA"

            BrickTheme(themeMode = userProfile?.themeMode ?: "DARK") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color(0x990A0E1A)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    UltraFastExpenseSheet(
                        currency = currency,
                        onDismiss = { finish() },
                        onSubmit = { title, amount, category, type, priority ->
                            CoroutineScope(Dispatchers.IO).launch {
                                repository.addTransaction(
                                    TransactionEntity(
                                        title = title,
                                        amount = amount,
                                        category = category,
                                        type = type,
                                        priority = priority
                                    )
                                )
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }
}
