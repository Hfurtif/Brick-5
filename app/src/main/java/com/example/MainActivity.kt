package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AddExpenseDialog
import com.example.ui.screens.AssetsLiabilitiesScreen
import com.example.ui.screens.BiometricLockScreen
import com.example.ui.screens.BrickAnimatedSplashScreen
import com.example.ui.screens.BudgetAnalyticsScreen
import com.example.ui.screens.BusinessFlowScreen
import com.example.ui.screens.CoachAiScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EveningRecapDialog
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileSecurityScreen
import com.example.ui.screens.PurchaseSimulatorDialog
import com.example.ui.screens.UltraFastExpenseSheet
import com.example.ui.theme.BrickBackground
import com.example.ui.theme.BrickSurface
import com.example.ui.theme.BrickSurfaceBorder
import com.example.ui.theme.BrickSurfaceElevated
import com.example.ui.theme.BrickTheme
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonVioletGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.BrickViewModel

class MainActivity : FragmentActivity() {

    private val viewModel: BrickViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIncomingIntent(intent)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        setContent {
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
            BrickTheme(themeMode = userProfile.themeMode) {
                var isSplashActive by remember { mutableStateOf(true) }

                Box(modifier = Modifier.fillMaxSize()) {
                    MainAppContent(viewModel = viewModel)

                    androidx.compose.animation.AnimatedVisibility(
                        visible = isSplashActive,
                        enter = androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.fadeOut(
                            animationSpec = androidx.compose.animation.core.tween(400)
                        )
                    ) {
                        BrickAnimatedSplashScreen(
                            onAnimationComplete = {
                                isSplashActive = false
                            }
                        )
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                }
            } catch (e: Exception) {
                // Graceful fallback
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action == "com.example.brick.ACTION_QUICK_ADD" || intent.getBooleanExtra("quick_add", false)) {
            viewModel.openQuickAddSheet()
        } else if (intent.action == "com.example.brick.ACTION_EVENING_RECAP" || intent.getBooleanExtra("open_evening_recap", false)) {
            viewModel.openEveningRecap()
        }
    }
}

@Composable
fun MainAppContent(viewModel: BrickViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val assetsLiabilities by viewModel.assetsLiabilities.collectAsStateWithLifecycle()
    val financialSnapshot by viewModel.financialSnapshot.collectAsStateWithLifecycle()
    val badges by viewModel.badges.collectAsStateWithLifecycle()

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val showAddExpense by viewModel.showAddExpenseDialog.collectAsStateWithLifecycle()
    val addExpenseInitialMode by viewModel.addExpenseInitialMode.collectAsStateWithLifecycle()
    val showQuickAdd by viewModel.showQuickAddSheet.collectAsStateWithLifecycle()
    val showEveningRecap by viewModel.showEveningRecap.collectAsStateWithLifecycle()
    val todayTransactions by viewModel.todayTransactions.collectAsStateWithLifecycle()
    val todayTotalSpent by viewModel.todayTotalSpent.collectAsStateWithLifecycle()
    val yesterdayTotalSpent by viewModel.yesterdayTotalSpent.collectAsStateWithLifecycle()

    val showSimulator by viewModel.showPurchaseSimulator.collectAsStateWithLifecycle()
    val simulationResult by viewModel.simulationResult.collectAsStateWithLifecycle()
    val coachMessages by viewModel.coachMessages.collectAsStateWithLifecycle()
    val isCoachTyping by viewModel.isCoachTyping.collectAsStateWithLifecycle()

    // Handle back button on sub-screens to return to dashboard
    BackHandler(enabled = currentScreen != 0) {
        viewModel.selectScreen(0)
    }

    var isAppUnlocked by remember {
        mutableStateOf(false)
    }

    if (userProfile.isOnboarded && userProfile.biometricEnabled && !isAppUnlocked) {
        BiometricLockScreen(
            userPinCode = userProfile.pinCode,
            onUnlockSuccess = {
                isAppUnlocked = true
            }
        )
        return
    }

    if (!userProfile.isOnboarded) {
        OnboardingScreen(
            initialUser = userProfile,
            onComplete = { updated ->
                viewModel.updateProfile(updated)
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = BrickBackground,
            bottomBar = {
                BrickBottomNavigationBar(
                    selectedScreen = currentScreen,
                    onSelectScreen = { viewModel.selectScreen(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    0 -> DashboardScreen(
                        user = userProfile,
                        snapshot = financialSnapshot,
                        transactions = transactions,
                        todayTotal = todayTotalSpent,
                        isOnline = viewModel.isOnline(),
                        onToggleHideAmounts = { viewModel.toggleHideAmounts() },
                        onOpenQuickAdd = { viewModel.openQuickAddSheet() },
                        onOpenAddExpense = { viewModel.openAddExpenseDialog() },
                        onOpenEveningRecap = { viewModel.openEveningRecap() },
                        onOpenSimulator = { viewModel.openPurchaseSimulator() },
                        onOpenBusinessHub = { viewModel.selectScreen(1) },
                        onOpenAddFlow = { mode -> viewModel.openAddExpenseDialog(mode) },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) }
                    )

                    1 -> BusinessFlowScreen(
                        user = userProfile,
                        snapshot = financialSnapshot,
                        transactions = transactions,
                        onOpenAddTransactionWithMode = { mode -> viewModel.openAddExpenseDialog(mode) },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) }
                    )

                    2 -> BudgetAnalyticsScreen(
                        user = userProfile,
                        snapshot = financialSnapshot,
                        transactions = transactions
                    )

                    3 -> AssetsLiabilitiesScreen(
                        user = userProfile,
                        assetsLiabilities = assetsLiabilities,
                        goals = goals,
                        onAddAssetLiability = { name, type, category, value, cashflow, note ->
                            viewModel.addAssetLiability(name, type, category, value, cashflow, note)
                        },
                        onDeleteAssetLiability = { viewModel.deleteAssetLiability(it) },
                        onAddGoal = { title, target, current, category ->
                            viewModel.addGoal(title, target, current, category)
                        },
                        onUpdateGoalAmount = { goal, added ->
                            viewModel.updateGoalAmount(goal, added)
                        },
                        onDeleteGoal = { viewModel.deleteGoal(it) }
                    )

                    4 -> CoachAiScreen(
                        messages = coachMessages,
                        isTyping = isCoachTyping,
                        isOnline = viewModel.isOnline(),
                        onSendMessage = { viewModel.askCoach(it) },
                        currency = userProfile.currency
                    )

                    5 -> ProfileSecurityScreen(
                        user = userProfile,
                        snapshot = financialSnapshot,
                        transactions = transactions,
                        badges = badges,
                        onUpdateProfile = { viewModel.updateProfile(it) },
                        onResetData = { viewModel.resetData() }
                    )
                }
            }

            // Add Expense Dialog
            if (showAddExpense) {
                AddExpenseDialog(
                    currency = userProfile.currency,
                    initialFlowType = addExpenseInitialMode,
                    onDismiss = { viewModel.closeAddExpenseDialog() },
                    onConfirm = { title, amount, category, type, priority, note ->
                        viewModel.addTransaction(
                            title = title,
                            amount = amount,
                            category = category,
                            type = type,
                            priority = priority,
                            note = note
                        )
                    }
                )
            }

            // Purchase Simulator Dialog
            if (showSimulator) {
                PurchaseSimulatorDialog(
                    currency = userProfile.currency,
                    simulationResult = simulationResult,
                    onSimulate = { amount, title ->
                        viewModel.runSimulation(amount, title)
                    },
                    onDismiss = { viewModel.closePurchaseSimulator() }
                )
            }

            // Ultra-Fast 5-Second Expense BottomSheet
            if (showQuickAdd) {
                UltraFastExpenseSheet(
                    currency = userProfile.currency,
                    todayTotal = todayTotalSpent,
                    onDismiss = { viewModel.closeQuickAddSheet() },
                    onSaveExpense = { title, amount, category, type, priority ->
                        viewModel.addTransaction(
                            title = title,
                            amount = amount,
                            category = category,
                            type = type,
                            priority = priority
                        )
                    }
                )
            }

            // Evening Recap Dialog
            if (showEveningRecap) {
                EveningRecapDialog(
                    todayTransactions = todayTransactions,
                    todayTotal = todayTotalSpent,
                    yesterdayTotal = yesterdayTotalSpent,
                    dailyBudget = financialSnapshot.dailyAllowanceRemaining,
                    currency = userProfile.currency,
                    onDismiss = { viewModel.closeEveningRecap() }
                )
            }
        }
    }
}

@Composable
fun BrickBottomNavigationBar(
    selectedScreen: Int,
    onSelectScreen: (Int) -> Unit
) {
    val items = listOf(
        NavigationItem("Accueil", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
        NavigationItem("Affaires", Icons.Filled.Business, Icons.Outlined.Business, "nav_business"),
        NavigationItem("Budgets", Icons.Filled.PieChart, Icons.Outlined.PieChart, "nav_budgets"),
        NavigationItem("Actifs", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance, "nav_assets"),
        NavigationItem("Coach", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_coach"),
        NavigationItem("Profil", Icons.Filled.Security, Icons.Outlined.Security, "nav_profile")
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .border(
                1.dp,
                Brush.verticalGradient(listOf(BrickSurfaceBorder, Color.Transparent)),
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .navigationBarsPadding(),
        containerColor = BrickSurface,
        tonalElevation = 8.dp
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedScreen == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectScreen(index) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ElectricCyan,
                    selectedTextColor = ElectricCyan,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = ElectricCyan.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}

data class NavigationItem(
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
