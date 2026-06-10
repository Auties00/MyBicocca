package it.attendance100.mybicocca.domain.usecase.search

import it.attendance100.mybicocca.core.search.MatchInput
import it.attendance100.mybicocca.core.search.SearchMatcher
import it.attendance100.mybicocca.domain.model.search.SearchDestination
import it.attendance100.mybicocca.domain.model.search.SearchResult
import javax.inject.Inject

/**
 * Scores every navigable page against the query. Titles mirror the app-bar titles; aliases
 * are the words students actually type ("voti" -> Esiti, "pagopa" -> Tasse) — bilingual
 * where students mix English ("dark mode").
 */
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
        /**
         * Catalog notes: libretto/carriera statistics live in the Profile page, so those
         * search terms resolve to [SearchDestination.Profile]; the Settings-prefixed
         * destinations open the settings modal already on the matching page.
         */
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
                SearchDestination.Profile, "Profilo", null,
                listOf(
                    "account", "media", "statistiche", "matricola",
                    "carriera", "libretto", "esami sostenuti", "crediti", "cfu",
                ),
            ),
            Entry(
                SearchDestination.Taxes, "Tasse", "Servizi",
                listOf("pagamenti", "pagopa", "bollettini", "rette", "fatture"),
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
                SearchDestination.Questionnaires, "Questionari", "Servizi",
                listOf("questionario", "valutazione didattica"),
            ),
            Entry(
                SearchDestination.Appointments, "Appuntamenti", "Servizi",
                listOf("appuntamento", "sportello", "prenotazione", "ritiro badge", "pergamena"),
            ),
            Entry(
                SearchDestination.Enrollments, "Iscrizioni", "Servizi",
                listOf("iscrizione", "rinnovo", "rinnovo iscrizione", "immatricolazione"),
            ),
            Entry(
                SearchDestination.Titles, "Titoli", "Servizi",
                listOf("titoli di studio", "maturità", "diploma"),
            ),
            Entry(
                SearchDestination.Certificates, "Certificati", "Servizi",
                listOf("certificato", "autodichiarazioni", "autocertificazione"),
            ),
            Entry(
                SearchDestination.Library, "Biblioteca", "Servizi",
                listOf("biblioteche", "sala studio", "posto biblioteca", "posto studio", "affluences"),
            ),
            Entry(
                SearchDestination.Refunds, "Rimborsi", "Servizi",
                listOf("rimborso", "restituzione"),
            ),
            Entry(
                SearchDestination.Isee, "ISEE", "Servizi",
                listOf("isee", "indicatore situazione economica", "fascia", "reddito"),
            ),
            Entry(
                SearchDestination.Settings, "Impostazioni", null,
                listOf("preferenze", "opzioni", "configurazione", "settings"),
            ),
            Entry(
                SearchDestination.SettingsAppearance, "Aspetto", "Impostazioni",
                listOf("tema", "colori", "tema scuro", "tema chiaro", "dark mode", "colore dinamico"),
            ),
            Entry(
                SearchDestination.SettingsSecurity, "Sicurezza", "Impostazioni",
                listOf("biometria", "impronta", "blocco", "sblocco", "fingerprint"),
            ),
            Entry(
                SearchDestination.SettingsLanguage, "Lingua", "Impostazioni",
                listOf("language", "italiano", "english", "traduzione"),
            ),
            Entry(
                SearchDestination.SettingsFileAssociations, "Associazioni file", "Impostazioni",
                listOf("apri con", "app predefinita", "file pdf", "lettore pdf"),
            ),
            Entry(
                SearchDestination.SettingsLicenses, "Licenze", "Impostazioni",
                listOf("open source", "librerie", "licenza"),
            ),
            Entry(
                SearchDestination.SettingsAppInfo, "Info app", "Impostazioni",
                listOf("versione", "informazioni", "about", "github"),
            ),
        )
    }
}
