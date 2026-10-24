package com.example.ai

import com.example.data.model.UserProfile
import kotlinx.coroutines.delay

class AiCoachService {

    suspend fun askCoach(
        question: String,
        user: UserProfile?,
        snapshot: FinancialSnapshot
    ): String {
        delay(600) // gentle natural typing delay

        val lower = question.lowercase()
        val currency = user?.currency ?: "FCFA"

        return when {
            lower.contains("bonjour") || lower.contains("salut") || lower.contains("hello") -> {
                "Salutations ${user?.name ?: "l'ami"} ! Je suis votre Coach Financier BRICK. Comment puis-je renforcer votre forteresse financière aujourd'hui ?"
            }
            lower.contains("reste à vivre") || lower.contains("reste a vivre") || lower.contains("caisse") -> {
                "Votre reste à vivre du jour s'élève à ${String.format("%,.0f", snapshot.resteAVivreDuJour)} $currency. Si vous ne dépensez pas tout aujourd'hui, le surplus consolidera automatiquement votre épargne de sécurité !"
            }
            lower.contains("50/30/20") || lower.contains("budget") || lower.contains("repartition") -> {
                "La règle d'or BRICK 50/30/20 :\n• 50% pour les Besoins vitaux (loyer, nourriture, transport, électricité)\n• 30% pour vos Envies (loisirs, sorties, petits plaisirs)\n• 20% pour l'Épargne & le Remboursement (tontine, projets, investissements).\nRespectez cette fondation et vos finances seront inébranlables !"
            }
            lower.contains("tontine") -> {
                "La tontine est un puissant accélérateur communautaire en Afrique. Conseil du Coach : placez votre part de levée de tontine immédiatement dans un actif productif (marchandise, terrain, fonds de commerce) plutôt que dans des dépenses d'apparence."
            }
            lower.contains("dépense") || lower.contains("achat") || lower.contains("acheter") -> {
                "Avant tout achat important, posez-vous les 3 questions BRICK :\n1. Est-ce un besoin ou une envie ?\n2. Combien de jours de travail me coûte cet objet ?\n3. Si j'attends 48 heures, en aurai-je encore vraiment besoin ?"
            }
            lower.contains("score") || lower.contains("sante") || lower.contains("santé") -> {
                "Votre Score de Santé Financière est actuellement de ${snapshot.financialHealthScore}/100. Votre valeur nette est de ${String.format("%,.0f", snapshot.netWorth)} $currency. Continuez à enregistrer chaque opération pour consolider votre empire."
            }
            else -> {
                "Excellente interrogation. Avec une discipline quotidienne et un suivi rigoureux de chaque brique financière (dépenses, caisse, tontine), vous bâtissez une indépendance solide. Votre reste à vivre est de ${String.format("%,.0f", snapshot.resteAVivreDuJour)} $currency. Donnez toujours la priorité aux actifs qui génèrent du cashflow !"
            }
        }
    }
}
