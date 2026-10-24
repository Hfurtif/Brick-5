package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.*
import com.example.data.db.BrickDatabase
import com.example.data.model.AssetLiability
import com.example.data.model.FinancialGoal
import com.example.data.model.TransactionEntity
import com.example.data.model.UserProfile
import com.example.data.repository.BrickRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class BrickViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BrickDatabase.getInstance(application)
    private val repository = BrickRepository(
        userDao = db.userDao(),
        transactionDao = db.transactionDao(),
        goalDao = db.goalDao(),
        assetDao = db.assetDao(),
        categoryDao = db.categoryDao(),
        budgetSettingsDao = db.budgetSettingsDao()
    )
    private val aiCoachService = AiCoachService()

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<FinancialGoal>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assetsLiabilities: StateFlow<List<AssetLiability>> = repository.allAssetsLiabilities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetSettings = repository.budgetSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentScreen = MutableStateFlow(0)
    val currentScreen: StateFlow<Int> = _currentScreen.asStateFlow()

    private val _isPinLocked = MutableStateFlow(false)
    val isPinLocked: StateFlow<Boolean> = _isPinLocked.asStateFlow()

    private val _showAddExpenseDialog = MutableStateFlow(false)
    val showAddExpenseDialog: StateFlow<Boolean> = _showAddExpenseDialog.asStateFlow()

    private val _addExpenseInitialMode = MutableStateFlow("DEPENSE")
    val addExpenseInitialMode: StateFlow<String> = _addExpenseInitialMode.asStateFlow()

    private val _showQuickAddSheet = MutableStateFlow(false)
    val showQuickAddSheet: StateFlow<Boolean> = _showQuickAddSheet.asStateFlow()

    private val _showEveningRecap = MutableStateFlow(false)
    val showEveningRecap: StateFlow<Boolean> = _showEveningRecap.asStateFlow()

    private val _showPurchaseSimulator = MutableStateFlow(false)
    val showPurchaseSimulator: StateFlow<Boolean> = _showPurchaseSimulator.asStateFlow()

    private val _simulationResult = MutableStateFlow<PurchaseSimulationResult?>(null)
    val simulationResult: StateFlow<PurchaseSimulationResult?> = _simulationResult.asStateFlow()

    private val _coachMessages = MutableStateFlow<List<CoachMessage>>(
        listOf(
            CoachMessage(
                id = "1",
                sender = "COACH",
                text = "Bienvenue dans BRICK ! Je suis votre conseiller financier personnel. Posez-moi vos questions sur votre budget, vos dettes, vos tontines ou vos achats à venir."
            )
        )
    )
    val coachMessages: StateFlow<List<CoachMessage>> = _coachMessages.asStateFlow()

    private val _isCoachTyping = MutableStateFlow(false)
    val isCoachTyping: StateFlow<Boolean> = _isCoachTyping.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
        }
    }

    val todayTransactions: StateFlow<List<TransactionEntity>> = transactions.map { list ->
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = cal.timeInMillis
        list.filter { it.timestamp >= todayStart }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTotalSpent: StateFlow<Double> = todayTransactions.map { list ->
        list.filter { it.type == "DEPENSE" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val yesterdayTotalSpent: StateFlow<Double> = transactions.map { list ->
        val calStart = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calEnd = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        list.filter { it.type == "DEPENSE" && it.timestamp in calStart.timeInMillis..calEnd.timeInMillis }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val financialSnapshot: StateFlow<FinancialSnapshot> = combine(
        userProfile,
        transactions,
        assetsLiabilities
    ) { user, txs, assets ->
        LocalFinancialEngine.calculateFinancialSnapshot(user, txs, assets)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FinancialSnapshot(0.0, 0.0, 50, 0.0, emptyList())
    )

    val badges: StateFlow<List<BrickBadge>> = combine(
        userProfile,
        transactions,
        goals
    ) { user, txs, g ->
        LocalFinancialEngine.evaluateBadges(user, txs, g)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectScreen(index: Int) {
        _currentScreen.value = index
    }

    fun openAddExpenseDialog(mode: String = "DEPENSE") {
        _addExpenseInitialMode.value = mode
        _showAddExpenseDialog.value = true
    }

    fun closeAddExpenseDialog() {
        _showAddExpenseDialog.value = false
    }

    fun openQuickAddSheet() {
        _showQuickAddSheet.value = true
    }

    fun closeQuickAddSheet() {
        _showQuickAddSheet.value = false
    }

    fun openEveningRecap() {
        _showEveningRecap.value = true
    }

    fun closeEveningRecap() {
        _showEveningRecap.value = false
    }

    fun openPurchaseSimulator() {
        _simulationResult.value = null
        _showPurchaseSimulator.value = true
    }

    fun closePurchaseSimulator() {
        _showPurchaseSimulator.value = false
    }

    fun unlockPin() {
        _isPinLocked.value = false
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    fun toggleHideAmounts() {
        val current = userProfile.value ?: return
        viewModelScope.launch {
            repository.saveUserProfile(current.copy(hideAmounts = !current.hideAmounts))
        }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        category: String,
        type: String,
        priority: String = "NEEDS",
        note: String = "",
        isRecurring: Boolean = false
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(
                    title = title,
                    amount = amount,
                    category = category,
                    type = type,
                    priority = priority,
                    note = note,
                    isRecurring = isRecurring
                )
            )
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addGoal(title: String, targetAmount: Double, currentAmount: Double = 0.0, category: String = "EPARGNE") {
        viewModelScope.launch {
            repository.addGoal(
                FinancialGoal(
                    title = title,
                    targetAmount = targetAmount,
                    currentAmount = currentAmount,
                    category = category
                )
            )
        }
    }

    fun updateGoalAmount(goal: FinancialGoal, addedAmount: Double) {
        viewModelScope.launch {
            repository.updateGoal(goal.copy(currentAmount = goal.currentAmount + addedAmount))
        }
    }

    fun deleteGoal(goal: FinancialGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun addAssetLiability(
        name: String,
        type: String,
        category: String,
        value: Double,
        monthlyCashflow: Double = 0.0,
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.addAssetLiability(
                AssetLiability(
                    name = name,
                    type = type,
                    category = category,
                    value = value,
                    monthlyCashflow = monthlyCashflow,
                    note = note
                )
            )
        }
    }

    fun deleteAssetLiability(item: AssetLiability) {
        viewModelScope.launch {
            repository.deleteAssetLiability(item)
        }
    }

    fun askCoach(userMessage: String) {
        if (userMessage.isBlank()) return
        val current = _coachMessages.value.toMutableList()
        current.add(CoachMessage(id = System.currentTimeMillis().toString(), sender = "USER", text = userMessage))
        _coachMessages.value = current
        _isCoachTyping.value = true

        viewModelScope.launch {
            val reply = aiCoachService.askCoach(userMessage, userProfile.value, financialSnapshot.value)
            val updated = _coachMessages.value.toMutableList()
            updated.add(CoachMessage(id = (System.currentTimeMillis() + 1).toString(), sender = "COACH", text = reply))
            _coachMessages.value = updated
            _isCoachTyping.value = false
        }
    }

    fun simulatePurchase(amount: Double, title: String) {
        val result = LocalFinancialEngine.simulatePurchase(userProfile.value, financialSnapshot.value, amount, title)
        _simulationResult.value = result
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }
}
