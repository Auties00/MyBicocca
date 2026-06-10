package it.attendance100.mybicocca.domain.usecase.search

import it.attendance100.mybicocca.core.search.MatchInput
import it.attendance100.mybicocca.core.search.SearchMatcher
import it.attendance100.mybicocca.domain.model.search.SearchAction
import it.attendance100.mybicocca.domain.model.search.SearchResult
import javax.inject.Inject

/**
 * Scores the catalog of things a user can DO against the query — the command-palette half
 * of the unified search. Titles are imperative ("Prenota un esame") and aliases lean on
 * the verbs students actually type; that's what makes search feel like a command palette
 * rather than a page list.
 */
class SearchActionsUseCase @Inject constructor() {

    operator fun invoke(query: String): List<SearchResult.Action> =
        Entries.mapNotNull { entry ->
            val score = SearchMatcher.score(query, MatchInput(entry.title, entry.aliases))
                ?: return@mapNotNull null
            SearchResult.Action(entry.action, entry.title, entry.subtitle, score)
        }

    private data class Entry(
        val action: SearchAction,
        val title: String,
        val subtitle: String?,
        val aliases: List<String>,
    )

    private companion object {
        val Entries = listOf(
            Entry(
                SearchAction.BookExam, "Prenota un esame", "Servizi · Esami",
                listOf(
                    "prenota appello", "iscriviti a un esame", "iscrizione appello",
                    "prenotazione esame", "appello esame",
                ),
            ),
            Entry(
                SearchAction.MarkPresence, "Rileva presenza", "Servizi · Presenze",
                listOf(
                    "segna presenza", "scansiona qr", "codice lezione", "timbra",
                    "registra presenza", "qr presenza",
                ),
            ),
            Entry(
                SearchAction.ReserveLibrarySeat, "Prenota un posto in biblioteca", "Servizi · Biblioteca",
                listOf(
                    "prenota posto", "prenota biblioteca", "posto in sala studio",
                    "prenotazione biblioteca",
                ),
            ),
            Entry(
                SearchAction.BookAppointment, "Prenota un appuntamento", "Servizi · Appuntamenti",
                listOf("prenota sportello", "appuntamento segreteria", "prenotazione appuntamento"),
            ),
            Entry(
                SearchAction.AddCourse, "Aggiungi un corso", "E-learning",
                listOf(
                    "aggiungi corso", "iscriviti a un corso", "nuovo corso",
                    "iscrizione corso moodle", "cerca corso",
                ),
            ),
            Entry(
                SearchAction.PayTaxes, "Paga le tasse", "Servizi · Tasse",
                listOf("paga", "pagamento tasse", "paga rata", "paga bollettino", "pagopa"),
            ),
            Entry(
                SearchAction.CompileQuestionnaire, "Compila un questionario", "Servizi · Questionari",
                listOf("valuta la didattica", "compila valutazione", "questionario corso"),
            ),
            Entry(
                SearchAction.EditStudyPlan, "Compila il piano di studi", "Servizi · Piano di Studi",
                listOf("modifica piano", "compila piano", "piano carriera", "scegli esami"),
            ),
            Entry(
                SearchAction.HypotheticalAverage, "Calcola la media ipotetica", "Profilo",
                listOf(
                    "simula voto", "calcola media", "media ponderata", "proiezione media",
                    "calcolatrice voti", "voto di laurea",
                ),
            ),
            Entry(
                SearchAction.ChangeTheme, "Cambia tema", "Impostazioni · Aspetto",
                listOf("tema scuro", "tema chiaro", "dark mode", "modalità scura", "colori app"),
            ),
            Entry(
                SearchAction.ChangeLanguage, "Cambia lingua", "Impostazioni · Lingua",
                listOf("lingua app", "change language", "english"),
            ),
            Entry(
                SearchAction.AddAccount, "Aggiungi un account", "Account",
                listOf("secondo account", "altro account", "cambia account", "nuovo accesso"),
            ),
        )
    }
}
