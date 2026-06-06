package it.attendance100.mybicocca.domain.usecase.search

import it.attendance100.mybicocca.core.search.MatchInput
import it.attendance100.mybicocca.core.search.SearchMatcher
import it.attendance100.mybicocca.domain.model.search.SearchDestination
import it.attendance100.mybicocca.domain.model.search.SearchResult
import javax.inject.Inject

// Scores every navigable page against the query. Titles mirror the app-bar titles; aliases
// are the words students actually type ("voti" -> Esiti, "pagopa" -> Tasse).
class SearchDestinationsUseCase @Inject constructor() {

    operator fun invoke(query: String): List<SearchResult.Destination> =
        Entries.mapNotNull { entry ->
            val score = SearchMatcher.score(query, MatchInput(entry.title, entry.aliases))
                ?: return@mapNotNull null
            SearchResult.Destination(entry.destination, entry.title, entry.subtitle, score)
        }

    private data class Entry(
        val destination: SearchDestination,
        val title: String,
        val subtitle: String?,
        val aliases: List<String>,
    )

    private companion object {
        val Entries = listOf(
            Entry(
                SearchDestination.TabCalendar, "Calendario", null,
                listOf("orario", "lezioni", "agenda", "settimana"),
            ),
            Entry(
                SearchDestination.TabElearning, "E-learning", null,
                listOf("elearning", "moodle", "corsi", "materiale"),
            ),
            Entry(
                SearchDestination.TabMap, "Mappe", null,
                listOf("mappa", "aule", "edifici", "campus", "aula"),
            ),
            Entry(
                SearchDestination.TabRegistry, "Servizi", null,
                listOf("segreterie", "segreteria", "sportello"),
            ),
            Entry(
                // Libretto/carriera and titoli/certificati moved into the Profile page, so their
                // search terms live here.
                SearchDestination.Profile, "Profilo", null,
                listOf(
                    "account", "media", "statistiche", "matricola",
                    "carriera", "libretto", "esami sostenuti", "crediti", "cfu",
                    "titoli", "certificati", "autodichiarazioni", "autocertificazione",
                ),
            ),
            Entry(
                SearchDestination.Taxes, "Tasse", "Servizi",
                listOf("pagamenti", "pagopa", "isee", "bollettini", "rette", "fatture"),
            ),
            Entry(
                SearchDestination.ExamResults, "Esiti", "Servizi",
                listOf("voti", "risultati", "valutazioni", "esiti esami"),
            ),
            Entry(
                SearchDestination.BookedExams, "Esami", "Servizi",
                listOf("appelli", "prenotazioni esami", "prenota esame", "iscrizione esame"),
            ),
            Entry(
                SearchDestination.StudyPlan, "Piano di Studi", "Servizi",
                listOf("piano", "piano carriera", "piano di studio"),
            ),
            Entry(
                SearchDestination.Attendance, "Presenze", "Servizi",
                listOf("frequenza", "frequenze", "presenza"),
            ),
            Entry(
                SearchDestination.Internships, "Stage", "Servizi",
                listOf("tirocini", "tirocinio", "internship"),
            ),
            Entry(
                SearchDestination.Questionnaires, "Questionari", "Servizi",
                listOf("questionario", "valutazione didattica"),
            ),
            Entry(
                SearchDestination.DegreeAward, "Conseguimento Titolo", "Servizi",
                listOf("laurea", "tesi", "titolo", "conseguimento"),
            ),
            Entry(
                SearchDestination.Reservations, "Prenotazioni", "Servizi",
                listOf("prenotazione", "appuntamenti"),
            ),
            Entry(
                SearchDestination.Messaging, "Messaggi", "E-learning",
                listOf("chat", "conversazioni", "messaggio"),
            ),
            Entry(
                // Appearance, language, app lock and app info all live inside the Settings
                // page now, so their search terms resolve here.
                SearchDestination.Settings, "Impostazioni", null,
                listOf(
                    "preferenze", "opzioni", "configurazione",
                    "tema", "colori", "tema scuro", "dark mode", "aspetto",
                    "lingua", "biometria", "impronta", "blocco", "sicurezza",
                    "versione", "informazioni", "about", "licenze", "github",
                ),
            ),
        )
    }
}
