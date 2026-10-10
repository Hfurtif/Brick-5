package com.example.ai

import com.example.data.model.AssetLiability
import com.example.data.model.FinancialGoal
import com.example.data.model.TransactionEntity
import com.example.data.model.UserProfile
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class FinancialSnapshot(
    val monthlyIncome: Double,
    val totalExpenses: Double,
    val needsSpent: Double,
    val wantsSpent: Double,
    val savingsSpent: Double,
    val needsTarget: Double,
    val wantsTarget: Double,
    val savingsTarget: Double,
    val needsPercentageActual: Int,
    val wantsPercentageActual: Int,
    val savingsPercentageActual: Int,
    val remainingBudget: Double,
    val dailyAllowanceRemaining: Double,
    val projectedMonthEndExpenses: Double,
    val isOverBudget: Boolean,
    val overBudgetAmount: Double,
    val healthScore: Int,
    val scoreGrade: String,
    val scoreSummary: String,
    val detectedLeaks: List<FinancialLeak>,
    val plainFrenchSummary: String,
    val totalBusinessInflow: Double = 0.0,
    val totalDonations: Double = 0.0,
    val totalBusinessInvestments: Double = 0.0,
    val netBusinessProfit: Double = 0.0
)

data class FinancialLeak(
    val title: String,
    val amount: Double,
    val impactText: String,
    val adviceText: String
)

data class PurchaseSimulationResult(
    val amount: Double,
    val canAfford: Boolean,
    val recommendation: String,
    val hoursOfWork: Double,
    val impactOnDailyAllowance: Double,
    val classification: String, // "BESOIN" ou "ENVIE"
    val alternativeRichDadAdvice: String
)

data class BrickBadge(
    val id: String,
    val icon: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean
)

object LocalFinancialEngine {

    fun calculateSnapshot(
        user: UserProfile,
        transactions: List<TransactionEntity>,
        assets: List<AssetLiability> = emptyList(),
        goals: List<FinancialGoal> = emptyList()
    ): FinancialSnapshot {
        val income = user.monthlySalary

        // Calculate days in current month
        val cal = Calendar.getInstance()
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val remainingDays = max(1, maxDays - currentDay)

        // Filter this month transactions
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val monthTransactions = transactions.filter { t ->
            val tCal = Calendar.getInstance().apply { timeInMillis = t.timestamp }
            tCal.get(Calendar.MONTH) == currentMonth && tCal.get(Calendar.YEAR) == currentYear
        }

        var needs = 0.0
        var wants = 0.0
        var savings = 0.0
        var businessInflow = 0.0
        var donations = 0.0
        var businessInvestments = 0.0

        for (t in monthTransactions) {
            when (t.type.uppercase()) {
                "ENTREE", "REVENU", "AFFAIRES_IN" -> businessInflow += t.amount
                "DON", "DIME", "AUMONE" -> donations += t.amount
                "INVESTISSEMENT_AFFAIRES", "STOCK", "MATERIEL_AFFAIRES" -> businessInvestments += t.amount
                "BESOIN" -> needs += t.amount
                "ENVIE" -> wants += t.amount
                "EPARGNE", "ACTIF", "INVESTISSEMENT" -> savings += t.amount
                else -> {
                    // Fallback to category heuristics
                    if (t.category in listOf("Vente", "Commerce", "Prestation", "Chiffre d'Affaires", "Revenu", "Salaire", "Affaires")) {
                        businessInflow += t.amount
                    } else if (t.category in listOf("Don", "Dîme", "Charité", "Solidarité", "Aumône")) {
                        donations += t.amount
                    } else if (t.category in listOf("Stock", "Marchandise", "Matériel Pro", "Investissement Business")) {
                        businessInvestments += t.amount
                    } else if (t.category in listOf("Logement", "Alimentation", "Transports", "Santé")) {
                        needs += t.amount
                    } else if (t.category in listOf("Investissement", "Épargne")) {
                        savings += t.amount
                    } else {
                        wants += t.amount
                    }
                }
            }
        }

        val totalIncome = income + businessInflow
        val totalExpenses = needs + wants + donations
        val totalAllocatedCapital = savings + businessInvestments
        val remainingBudget = totalIncome - totalExpenses - totalAllocatedCapital
        val dailyAllowance = max(0.0, remainingBudget / remainingDays)
        val netBusinessProfit = businessInflow - businessInvestments

        // 50/30/20 Targets (based on total incoming funds)
        val needsTarget = totalIncome * (user.needsBudgetPercentage / 100.0)
        val wantsTarget = totalIncome * (user.wantsBudgetPercentage / 100.0)
        val savingsTarget = totalIncome * (user.savingsBudgetPercentage / 100.0)

        val totalTracked = max(1.0, totalExpenses + totalAllocatedCapital)
        val needsPct = ((needs / totalTracked) * 100).roundToInt()
        val wantsPct = ((wants / totalTracked) * 100).roundToInt()
        val savingsPct = (((savings + businessInvestments) / totalTracked) * 100).roundToInt()

        // Projection
        val dailyBurn = if (currentDay > 0) (totalExpenses / currentDay) else 0.0
        val projectedMonthEnd = totalExpenses + (dailyBurn * remainingDays)

        val isOver = totalExpenses + savings > income
        val overAmount = if (isOver) (totalExpenses + savings) - income else 0.0

        // Financial Health Score Calculation (0 to 100)
        var score = 100

        // 1. Dépassement global (max -30)
        if (isOver) {
            val ratio = (totalExpenses / income)
            score -= min(35, ((ratio - 1.0) * 80).toInt() + 10)
        }

        // 2. Respect des Envies (30% max)
        if (wants > wantsTarget) {
            val excessWants = ((wants - wantsTarget) / wantsTarget) * 20
            score -= min(25, excessWants.toInt())
        }

        // 3. Se payer en premier (Épargne/Investissement)
        if (savings < (savingsTarget * 0.5)) {
            score -= 15
        } else if (savings >= savingsTarget) {
            score += 5 // Bonus discipline
        }

        // 4. Passifs pénalisants
        val liabilitiesTotal = assets.filter { it.type == "PASSIF" }.sumOf { it.value }
        if (liabilitiesTotal > income * 2) {
            score -= 10
        }

        score = score.coerceIn(5, 100)

        val grade = when {
            score >= 85 -> "Excellent (Fondation Solide)"
            score >= 70 -> "Bon (En progression)"
            score >= 50 -> "Moyen (Vigilance Requise)"
            else -> "Critique (Fuites majeures)"
        }

        val summary = when {
            score >= 85 -> "Tu gères comme un investisseur discipliné ! Tes besoins sont maîtrisés et tu te payes en premier."
            score >= 70 -> "Ta base est saine, mais attention aux petites envies superflues qui grignotent ton épargne."
            score >= 50 -> "Tu dépenses trop en envies non planifiées. Ajuste ton budget 50/30/20 pour sécuriser ta fin de mois."
            else -> "Alerte rouge : tu vis au-dessus de tes moyens et tes passifs s'accumulent. Réagis dès aujourd'hui."
        }

        // Détection des fuites
        val leaks = mutableListOf<FinancialLeak>()
        val superflous = monthTransactions.filter { it.priority == "SUPERFLU" }
        val sumSuperflous = superflous.sumOf { it.amount }
        if (sumSuperflous > 50.0) {
            leaks.add(
                FinancialLeak(
                    title = "Achats superflus ou impulsifs",
                    amount = sumSuperflous,
                    impactText = "Représente ${(sumSuperflous / max(1.0, income) * 100).roundToInt()}% de ton salaire mensuel.",
                    adviceText = "Règle des 72h : attends 3 jours avant tout achat de confort pour désamorcer l'impulsion."
                )
            )
        }

        val subscriptions = monthTransactions.filter { it.category == "Abonnements" }
        val sumSubs = subscriptions.sumOf { it.amount }
        if (sumSubs > 60.0) {
            leaks.add(
                FinancialLeak(
                    title = "Accumulation d'abonnements",
                    amount = sumSubs,
                    impactText = "${subscriptions.size} abonnements actifs détectés (${sumSubs.roundToInt()} ${user.currency}/mois).",
                    adviceText = "Résilie les services non utilisés depuis plus de 30 jours (loi du Père Riche : coupe les passifs invisibles)."
                )
            )
        }

        if (wants > wantsTarget) {
            leaks.add(
                FinancialLeak(
                    title = "Dépassement du quota Envies (30%)",
                    amount = wants - wantsTarget,
                    impactText = "Tu as dépensé ${(wants).roundToInt()} ${user.currency} en envies pour un plafond de ${wantsTarget.roundToInt()} ${user.currency}.",
                    adviceText = "Bascule en mode 'Bunker' sur les sorties et le shopping jusqu'à ta prochaine paie."
                )
            )
        }

        // Plain French Translation ("Ce que ça veut dire")
        val plainFrench = buildString {
            if (isOver) {
                append("⚠️ Tu as déjà dépensé ${overAmount.roundToInt()} ${user.currency} de plus que tes revenus ce mois-ci. ")
                append("Pour combler ce déficit, gèle immédiatement toutes les dépenses d'envies (shopping, restos, extras).")
            } else {
                append("✅ Il te reste ${remainingBudget.roundToInt()} ${user.currency} de reste à vivre pour les $remainingDays jours restants, ")
                append("soit environ ${dailyAllowance.roundToInt()} ${user.currency}/jour. ")
                if (wantsPct > 35) {
                    append("Attention : tes plaisirs occupent $wantsPct% de tes sorties (cible recommandée: 30%). ")
                }
                if (savingsPct >= 20) {
                    append("Félicitations pour le respect de la règle 'Se payer en premier' ($savingsPct% épargnés).")
                } else {
                    append("Pense à transférer au moins ${savingsTarget.roundToInt()} ${user.currency} vers tes actifs dès réception de ta paie.")
                }
            }
        }

        return FinancialSnapshot(
            monthlyIncome = totalIncome,
            totalExpenses = totalExpenses,
            needsSpent = needs,
            wantsSpent = wants,
            savingsSpent = savings,
            needsTarget = needsTarget,
            wantsTarget = wantsTarget,
            savingsTarget = savingsTarget,
            needsPercentageActual = needsPct,
            wantsPercentageActual = wantsPct,
            savingsPercentageActual = savingsPct,
            remainingBudget = remainingBudget,
            dailyAllowanceRemaining = dailyAllowance,
            projectedMonthEndExpenses = projectedMonthEnd,
            isOverBudget = isOver,
            overBudgetAmount = overAmount,
            healthScore = score,
            scoreGrade = grade,
            scoreSummary = summary,
            detectedLeaks = leaks,
            plainFrenchSummary = plainFrench,
            totalBusinessInflow = businessInflow,
            totalDonations = donations,
            totalBusinessInvestments = businessInvestments,
            netBusinessProfit = netBusinessProfit
        )
    }

    fun simulatePurchase(
        amount: Double,
        title: String,
        user: UserProfile,
        snapshot: FinancialSnapshot
    ): PurchaseSimulationResult {
        // Taux horaire net estimé (salaire mensuel / 151.67h)
        val hourlyRate = max(5.0, user.monthlySalary / 151.67)
        val hoursNeeded = (amount / hourlyRate)

        val remainingAfter = snapshot.remainingBudget - amount
        val canAfford = remainingAfter >= 0

        val cal = Calendar.getInstance()
        val remainingDays = max(1, cal.getActualMaximum(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_MONTH))
        val newDailyAllowance = max(0.0, remainingAfter / remainingDays)

        val classification = if (amount > 100 || title.lowercase().containsAny("sneaker", "resto", "jeu", "montre", "voyage", "fringue", "gadget")) {
            "ENVIE (Non Vitale)"
        } else {
            "ÉVALUATION EN COURS"
        }

        val recommendation = if (canAfford) {
            if (newDailyAllowance < 15.0) {
                "⚠️ Achat possible sur le papier, mais il va réduire ton reste à vivre à seulement ${newDailyAllowance.roundToInt()} ${user.currency}/jour. Réfléchis bien !"
            } else {
                "✅ Achat absorbable par ton budget ce mois-ci, à condition qu'il s'agisse d'un plaisir réfléchi et non d'une impulsion."
            }
        } else {
            "⛔ DÉCONSEILLÉ : Cet achat te fera basculer dans le rouge de ${(-remainingAfter).roundToInt()} ${user.currency}. Tu travailles pour enrichir les autres au lieu de bâtir ta liberté."
        }

        val richDadAdvice = if (!canAfford) {
            "Principe Père Riche : N'achète jamais un passif avec ton salaire de base. Fais en sorte que ce soit tes actifs qui financent tes plaisirs !"
        } else {
            "Si tu achètes cet objet, engage-toi à investir la même somme (${amount.roundToInt()} ${user.currency}) dans tes actifs d'ici 30 jours."
        }

        return PurchaseSimulationResult(
            amount = amount,
            canAfford = canAfford,
            recommendation = recommendation,
            hoursOfWork = hoursNeeded,
            impactOnDailyAllowance = newDailyAllowance,
            classification = classification,
            alternativeRichDadAdvice = richDadAdvice
        )
    }

    fun getBadges(snapshot: FinancialSnapshot, streak: Int): List<BrickBadge> {
        return listOf(
            BrickBadge(
                id = "b1",
                icon = "🧱",
                title = "Première Brique",
                description = "Avoir commencé à poser les fondations de ses finances",
                isUnlocked = true
            ),
            BrickBadge(
                id = "b2",
                icon = "🔥",
                title = "Discipline 5 Jours",
                description = "Saisie quotidienne de ses finances pendant 5 jours",
                isUnlocked = streak >= 5
            ),
            BrickBadge(
                id = "b3",
                icon = "🛡️",
                title = "Maître du 50/30/20",
                description = "Maintenir les dépenses de besoins sous 50% et envies sous 30%",
                isUnlocked = snapshot.wantsPercentageActual <= 32 && snapshot.needsPercentageActual <= 55
            ),
            BrickBadge(
                id = "b4",
                icon = "👑",
                title = "Se Payer en Premier",
                description = "Consacrer au moins 20% de ses revenus à l'épargne et aux actifs",
                isUnlocked = snapshot.savingsSpent >= (snapshot.monthlyIncome * 0.18)
            ),
            BrickBadge(
                id = "b5",
                icon = "🏆",
                title = "Score d'Or (85+)",
                description = "Atteindre un score de santé financière supérieur à 85",
                isUnlocked = snapshot.healthScore >= 85
            )
        )
    }

    private fun String.containsAny(vararg words: String): Boolean {
        val lower = this.lowercase()
        return words.any { lower.contains(it) }
    }
}
