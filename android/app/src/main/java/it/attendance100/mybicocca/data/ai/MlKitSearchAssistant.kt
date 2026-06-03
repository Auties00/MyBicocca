package it.attendance100.mybicocca.data.ai

import android.os.Build
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import it.attendance100.mybicocca.domain.model.search.SearchAssistantStatus
import it.attendance100.mybicocca.domain.model.search.SearchDestination
import it.attendance100.mybicocca.domain.model.search.SearchSuggestion
import it.attendance100.mybicocca.domain.repository.SearchAssistant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Gemini Nano via the ML Kit GenAI Prompt API. Self-gating: on devices without AICore /
// Nano (or below API 26) every status check resolves to Unavailable and interpret() returns
// null, so no separate no-op binding is needed.
@Singleton
class MlKitSearchAssistant @Inject constructor() : SearchAssistant {

    private val model by lazy { Generation.getClient() }

    // Re-emitting re-runs the status probe — kicked after a download completes.
    private val statusProbe = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    override fun observeStatus(): Flow<SearchAssistantStatus> = statusProbe.map { fetchStatus() }

    override suspend fun ensureReady(): Boolean {
        return when (fetchStatus()) {
            SearchAssistantStatus.Available -> true
            SearchAssistantStatus.Downloadable, SearchAssistantStatus.Downloading -> {
                runCatching { model.download().collect {} }
                val ready = fetchStatus() == SearchAssistantStatus.Available
                statusProbe.tryEmit(Unit)
                ready
            }

            SearchAssistantStatus.Unavailable -> false
        }
    }

    override suspend fun interpret(query: String): SearchSuggestion? {
        if (fetchStatus() != SearchAssistantStatus.Available) return null
        return runCatching {
            val response = model.generateContent(buildPrompt(query))
            response.candidates.firstOrNull()?.text?.let { parse(it) }
        }.getOrNull()
    }

    private suspend fun fetchStatus(): SearchAssistantStatus {
        if (Build.VERSION.SDK_INT < MIN_SDK) return SearchAssistantStatus.Unavailable
        return runCatching {
            when (model.checkStatus()) {
                FeatureStatus.AVAILABLE -> SearchAssistantStatus.Available
                FeatureStatus.DOWNLOADABLE -> SearchAssistantStatus.Downloadable
                FeatureStatus.DOWNLOADING -> SearchAssistantStatus.Downloading
                else -> SearchAssistantStatus.Unavailable
            }
        }.getOrDefault(SearchAssistantStatus.Unavailable)
    }

    // Constrained classification rather than free generation: the model picks one in-app
    // page by enum name and produces a one-line Italian interpretation. Anything that
    // doesn't parse simply yields no card.
    private fun buildPrompt(query: String): String = """
        Sei l'assistente di MyBicocca, l'app per gli studenti dell'Università di Milano-Bicocca.
        L'utente ha scritto questa ricerca: "$query"

        Le pagine dell'app sono: ${SearchDestination.entries.joinToString(", ") { it.name }}.

        Rispondi ESATTAMENTE in questo formato, senza altro testo:
        PAGINA: <il nome esatto di una pagina, oppure NESSUNA>
        RISPOSTA: <una sola frase in italiano che spiega cosa può fare l'utente>
    """.trimIndent()

    private fun parse(raw: String): SearchSuggestion? {
        val lines = raw.lines().map { it.trim() }
        val page = lines.firstOrNull { it.startsWith("PAGINA:", ignoreCase = true) }
            ?.substringAfter(':')?.trim()
        val answer = lines.firstOrNull { it.startsWith("RISPOSTA:", ignoreCase = true) }
            ?.substringAfter(':')?.trim()
        if (answer.isNullOrBlank()) return null
        val target = SearchDestination.entries.firstOrNull { it.name.equals(page, ignoreCase = true) }
        return SearchSuggestion(intent = answer, target = target, explanation = null)
    }

    private companion object {
        // ML Kit GenAI requires API 26; the app's minSdk is 25.
        const val MIN_SDK = 26
    }
}
