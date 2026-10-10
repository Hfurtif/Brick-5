package com.example.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.BuildConfig
import com.example.data.model.TransactionEntity
import com.example.data.model.UserProfile
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(val text: String? = null)

@JsonClass(generateAdapter = true)
data class GeminiContent(val parts: List<GeminiPart>, val role: String? = null)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(val content: GeminiContent?)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

data class CoachMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "USER" or "COACH"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "LOCAL" // "GEMINI" or "LOCAL"
)

class AiCoachService(private val context: Context) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun askCoach(
        userMessage: String,
        user: UserProfile,
        snapshot: FinancialSnapshot,
        transactions: List<TransactionEntity>
    ): CoachMessage = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val hasValidKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"
        val canUseGemini = isOnline() && hasValidKey

        if (canUseGemini) {
            try {
                val systemPrompt = """
Tu es le Coach Financier de l'application BRICK.
Ton rôle : aider les personnes qui ont du mal à gérer leur argent et dépensent sans contrôle.
Tu t'inspires fortement des principes de 'Père riche, Père pauvre' (Robert Kiyosaki) et de la méthode YNAB / 50-30-20.
Principes clés :
- Distinguer impitoyablement Besoins vs Envies.
- 'Se payer en premier' : mettre au moins 20% en épargne/actifs dès que le salaire tombe.
- Privilégier les Actifs (qui mettent de l'argent dans la poche) et fuir les Passifs (qui en retirent chaque mois).
- Ton : Franc, percutant, bienveillant, motivant, direct, sans langue de bois ni culpabilité toxique. Réponses concises en français (max 3-4 paragraphes).

Voici le profil financier réel de l'utilisateur :
- Prénom : ${user.name}
- Salaire mensuel : ${user.monthlySalary} ${user.currency}
- Total dépensé ce mois : ${snapshot.totalExpenses} ${user.currency}
- Répartition réelle : Besoins ${snapshot.needsPercentageActual}% (${snapshot.needsSpent} ${user.currency}), Envies ${snapshot.wantsPercentageActual}% (${snapshot.wantsSpent} ${user.currency}), Épargne ${snapshot.savingsPercentageActual}% (${snapshot.savingsSpent} ${user.currency})
- Reste à vivre actuel : ${snapshot.remainingBudget} ${user.currency} (soit ~${snapshot.dailyAllowanceRemaining.toInt()} ${user.currency}/jour)
- Score de santé financière : ${snapshot.healthScore}/100 (${snapshot.scoreGrade})
- Dépassement de budget : ${if (snapshot.isOverBudget) "OUI (+${snapshot.overBudgetAmount.toInt()} ${user.currency})" else "NON (dans le vert)"}
- Fuites détectées : ${snapshot.detectedLeaks.joinToString { it.title }}
                """.trimIndent()

                val requestPayload = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = userMessage)), role = "user")
                    ),
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
                )

                val adapter = moshi.adapter(GeminiRequest::class.java)
                val jsonBody = adapter.toJson(requestPayload)

                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val respBody = response.body?.string()
                    val respAdapter = moshi.adapter(GeminiResponse::class.java)
                    val parsed = respBody?.let { respAdapter.fromJson(it) }
                    val replyText = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!replyText.isNullOrBlank()) {
                        return@withContext CoachMessage(
                            sender = "COACH",
                            text = replyText.trim(),
                            source = "GEMINI"
                        )
                    }
                }
            } catch (e: Exception) {
                // Silently fallback to local engine
            }
        }

        // Fallback local engine (Level 1 & 2 Local AI)
        val localReply = generateLocalResponse(userMessage, user, snapshot, transactions)
        CoachMessage(
            sender = "COACH",
            text = localReply,
            source = "LOCAL"
        )
    }

    private fun generateLocalResponse(
        query: String,
        user: UserProfile,
        snapshot: FinancialSnapshot,
        transactions: List<TransactionEntity>
    ): String {
        val q = query.lowercase()

        return when {
            q.contains("fuite") || q.contains("perte") || q.contains("gaspillage") -> {
                if (snapshot.detectedLeaks.isNotEmpty()) {
                    val leakDetails = snapshot.detectedLeaks.joinToString("\n• ") {
                        "${it.title} : ${it.amount.toInt()} ${user.currency}. ${it.adviceText}"
                    }
                    "🔍 Voici les fuites que j'ai repérées dans tes comptes :\n\n• $leakDetails\n\n💡 Conseil Père Riche : 'Ce n'est pas combien tu gagnes qui compte, mais combien tu gardes.' Coupe ces robinets dès cette semaine !"
                } else {
                    "✅ Bonne nouvelle : aucune fuite majeure détectée ce mois-ci ! Continue à surveiller tes abonnements et à différer tes achats d'impulsion de 72h."
                }
            }

            q.contains("achat") || q.contains("puis-je") || q.contains("acheter") || q.contains("m'offrir") || q.contains("resto") -> {
                val amountRegex = "\\b(\\d+)\\b".toRegex()
                val match = amountRegex.find(query)
                val amount = match?.value?.toDoubleOrNull() ?: 50.0
                val sim = LocalFinancialEngine.simulatePurchase(amount, query, user, snapshot)
                "${sim.recommendation}\n\n⏱️ Temps de travail requis : ${String.format("%.1f", sim.hoursOfWork)} heures de ton travail pour financer cet achat.\n📉 Reste à vivre après achat : ~${sim.impactOnDailyAllowance.toInt()} ${user.currency}/jour.\n\n🧱 ${sim.alternativeRichDadAdvice}"
            }

            q.contains("50/30/20") || q.contains("budget") || q.contains("repartition") -> {
                "📊 Ton équilibre actuel 50/30/20 :\n" +
                        "• Besoins réels : ${snapshot.needsPercentageActual}% (Cible : 50%)\n" +
                        "• Envies réelles : ${snapshot.wantsPercentageActual}% (Cible : 30%)\n" +
                        "• Épargne & Actifs : ${snapshot.savingsPercentageActual}% (Cible : 20%)\n\n" +
                        if (snapshot.wantsPercentageActual > 35) {
                            "⚠️ Tu dépenses trop en envies (${snapshot.wantsPercentageActual}%). Resserre la bride sur le superflu pour financer ta future indépendance financière."
                        } else {
                            "👏 Ta répartition est sur la bonne voie. Assure-toi de virer ton épargne dès le 1er du mois."
                        }
            }

            q.contains("payer en premier") || q.contains("epargne") || q.contains("investir") -> {
                "👑 Principe 'Se payer en premier' :\n" +
                        "La plupart des gens paient leur loyer, leurs factures, leurs restos, et épargnent ce qu'il reste (souvent 0 ${user.currency}).\n" +
                        "L'investisseur fait l'inverse : dès que ton salaire de ${user.monthlySalary.toInt()} ${user.currency} arrive, vire immédiatement ${snapshot.savingsTarget.toInt()} ${user.currency} (20%) sur tes actifs (compte d'épargne, investissement, stock). Ensuite seulement, tu vis avec le reste."
            }

            else -> {
                val sampleAmount = if (user.currency == "FCFA") "50 000 FCFA" else "80 ${user.currency}"
                "🧱 Analyse de ta situation actuelle :\n" +
                        "• Reste à vivre : ${snapshot.remainingBudget.toInt()} ${user.currency} (${snapshot.dailyAllowanceRemaining.toInt()} ${user.currency}/jour).\n" +
                        "• Score BRICK : ${snapshot.healthScore}/100 (${snapshot.scoreGrade}).\n\n" +
                        "${snapshot.plainFrenchSummary}\n\n" +
                        "Pose-moi une question précise : 'Analyse mes fuites', 'Puis-je m'offrir un achat à $sampleAmount ?' ou 'Comment appliquer Se payer en premier ?'."
            }
        }
    }
}
