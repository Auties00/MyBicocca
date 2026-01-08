package it.attendance100.mybicocca.data.dto.easystaff

import java.time.LocalDate
import java.time.LocalTime

/**
 * Type of bookable event in the system.
 *
 * These correspond to the "tipo prenotazione" values in the Agenda Web system.
 */
enum class EasyStaffEventType(val id: Int, val italianName: String) {
    LESSON(1, "Lezione"),
    EXAM(2, "Esame"),
    SEMINAR(3, "Seminari"),
    MASTER(4, "Master"),
    CONGRESS(6, "Congressi"),
    UNION_MEETING(7, "Riunione sindacale"),
    OTHER(8, "Altro"),
    INSTITUTIONAL_ACTIVITY(12, "Attività istituzionali"),
    CONFERENCE(13, "Convegni"),
    BLENDED_COURSE(14, "Corsi Blended"),
    TRAINING_COURSE(15, "Corsi di Formazione"),
    ELECTION(18, "Elezioni"),
    EXERCISE(19, "Esercitazioni"),
    ENTRANCE_TEST(24, "Test d'ingresso"),
    INTERNSHIP(26, "Tirocini"),
    STUDENT_ASSOCIATION_MEETING(28, "Incontri ass. studentesche"),
    GRADUATION(29, "Lauree"),
    PUBLIC_COMPETITION(31, "Concorsi pubblici"),
    MAINTENANCE(32, "Manutenzione/Lavori"),
    STATE_EXAM(33, "Esami di Stato"),
    OPEN_DAY(34, "Open day"),
    TEACHING_LAB(35, "Laboratori didattici"),
    TUTOR_PRESENCE(36, "Presenza Tutor (LIB)"),
    RESERVATION(37, "Prenotazione"),
    SELF_SERVICE_OPENING(38, "Apertura self-service (LIB)"),
    NON_CURRICULAR_ACTIVITY(39, "Altre attività didattiche non curriculari"),
    TUTORING(40, "Tutorato"),
    PHD(41, "Dottorati"),
    MASTER_LESSON(42, "Lezioni master"),
    MASTER_EXAM(43, "Esami master"),
    ADVANCED_COURSE(44, "Corsi di Perfezionamento"),
    PODIUM_RESERVATION(45, "Prenotazione PODIO (registrazione lezione)"),
    EVENT(46, "Eventi"),
    STUDENT_EVENT(47, "ZZZ Evento STUDENTI"),
    MENTORING(48, "Tutoraggio"),
    MEETING(49, "Riunione");

    companion object {
        /**
         * Gets an event type by its ID.
         *
         * @param id The event type ID
         * @return The matching [EasyStaffEventType], or [OTHER] if not found
         */
        fun fromId(id: Int): EasyStaffEventType {
            return entries.find { it.id == id } ?: OTHER
        }

        /**
         * Gets an event type by its Italian name.
         *
         * @param name The Italian name
         * @return The matching [EasyStaffEventType], or [OTHER] if not found
         */
        fun fromItalianName(name: String): EasyStaffEventType {
            val normalized = name.trim().lowercase()
            return entries.find { it.italianName.lowercase() == normalized } ?: OTHER
        }
    }
}

/**
 * Status of a booking/event.
 */
enum class EasyStaffBookingStatus(val value: String, val italianName: String) {
    CONFIRMED("0", "Confermato"),
    CANCELLED("1", "Annullato");

    companion object {
        /**
         * Gets a booking status by its value.
         *
         * @param value The status value ("0" or "1")
         * @return The matching [EasyStaffBookingStatus], or [CONFIRMED] as default
         */
        fun fromValue(value: String): EasyStaffBookingStatus {
            return entries.find { it.value == value } ?: CONFIRMED
        }

        /**
         * Gets a booking status from Italian text.
         *
         * @param text The Italian status text
         * @return The matching [EasyStaffBookingStatus], or [CONFIRMED] as default
         */
        fun fromItalian(text: String): EasyStaffBookingStatus {
            return when (text.trim().lowercase()) {
                "annullato" -> CANCELLED
                else -> CONFIRMED
            }
        }
    }
}

/**
 * Represents a scheduled event/booking in the system.
 *
 * @property title The event title
 * @property date The date of the event
 * @property startTime The start time
 * @property endTime The end time
 * @property room The room name
 * @property building The building name
 * @property eventType The type of event
 * @property organizers List of organizers/teachers
 * @property status The booking status
 */
data class EasyStaffScheduledEvent(
    val title: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val room: String?,
    val building: String?,
    val eventType: EasyStaffEventType,
    val organizers: List<String>,
    val status: EasyStaffBookingStatus
)

/**
 * Parameters for searching events.
 *
 * @property buildings Buildings to search (empty for all)
 * @property rooms Rooms to search (empty for all)
 * @property startDate The start date for the search range
 * @property endDate The end date for the search range
 * @property eventTypes Event types to filter by (empty for all)
 * @property status Booking status filter (null for all)
 * @property keyword Optional keyword search
 * @property daysOfWeek Days of week to filter by (empty for all)
 * @property startTime Optional start time filter
 * @property endTime Optional end time filter
 */
data class EasyStaffEventSearchQuery(
    val buildings: List<String> = emptyList(),
    val rooms: List<String> = emptyList(),
    val startDate: LocalDate,
    val endDate: LocalDate,
    val eventTypes: List<EasyStaffEventType> = emptyList(),
    val status: EasyStaffBookingStatus? = null,
    val keyword: String? = null,
    val daysOfWeek: List<EasyStaffDayOfWeek> = emptyList(),
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null
)

/**
 * Results from an event search.
 *
 * @property events The list of matching events
 * @property searchSummary A summary of the search parameters
 */
data class EasyStaffEventSearchResults(
    val events: List<EasyStaffScheduledEvent>,
    val searchSummary: String
)

/**
 * Options available for event searches, loaded from the server.
 *
 * @property buildings Available buildings
 * @property rooms Available rooms (may be filtered by building)
 * @property eventTypes Available event types
 */
data class EasyStaffEventSearchOptions(
    val buildings: List<EasyStaffBuilding>,
    val rooms: List<EasyStaffRoom>,
    val eventTypes: List<EasyStaffEventType>
)
