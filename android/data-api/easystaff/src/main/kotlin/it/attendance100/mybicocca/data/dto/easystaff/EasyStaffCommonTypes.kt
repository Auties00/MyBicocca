package it.attendance100.mybicocca.data.dto.easystaff

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.DayOfWeek

/**
 * Supported languages for the Agenda Web platform.
 */
enum class EasyStaffLanguage(val code: String) {
    ITALIAN("it"),
    ENGLISH("en"),
    SPANISH("es"),
    GERMAN("de"),
    FRENCH("fr");

    companion object {
        /**
         * Gets a language by its code.
         *
         * @param code The language code (e.g., "it", "en")
         * @return The matching [EasyStaffLanguage], or [ITALIAN] as default
         */
        fun fromCode(code: String): EasyStaffLanguage {
            return entries.find { it.code == code.lowercase() } ?: ITALIAN
        }
    }
}

/**
 * Represents an academic year in the format YYYY/YYYY+1 (e.g., 2024/2025).
 *
 * @property startYear The starting year of the academic year
 */
data class EasyStaffAcademicYear(
    val startYear: Int
) {
    /**
     * The ending year of the academic year.
     */
    val endYear: Int get() = startYear + 1

    /**
     * The display label (e.g., "2024/2025").
     */
    val label: String get() = "$startYear/$endYear"

    /**
     * The value used in API requests (e.g., "2024").
     */
    val value: String get() = startYear.toString()

    override fun toString(): String = label

    companion object {
        /**
         * Parses an academic year from a label string.
         *
         * @param label The label (e.g., "2024/2025" or "2024")
         * @return The parsed [EasyStaffAcademicYear], or null if parsing fails
         */
        fun parse(label: String): EasyStaffAcademicYear? {
            val year = label.split("/").firstOrNull()?.trim()?.toIntOrNull()
            return year?.let { EasyStaffAcademicYear(it) }
        }
    }
}

/**
 * Represents a teaching area (faculty/school) in the university.
 *
 * @property code The internal code used in API requests
 * @property name The display name
 */
data class EasyStaffTeachingArea(
    val code: String,
    val name: String
) {
    override fun toString(): String = name
}

/**
 * Represents a study program (corso di laurea).
 *
 * @property code The course code (e.g., "E3101Q")
 * @property name The course name (e.g., "INFORMATICA")
 * @property degreeType The type of degree (e.g., "Laurea", "Laurea Magistrale")
 * @property teachingAreaCode The code of the teaching area this course belongs to
 */
data class EasyStaffStudyProgram(
    val code: String,
    val name: String,
    val degreeType: EasyStaffDegreeType,
    val teachingAreaCode: String
) {
    override fun toString(): String = "$code - $name ($degreeType)"
}

/**
 * Type of academic degree.
 */
enum class EasyStaffDegreeType(val italianName: String) {
    BACHELOR("Laurea"),
    MASTER("Laurea Magistrale"),
    SINGLE_CYCLE("Laurea Magistrale a Ciclo Unico"),
    PHD("Dottorato"),
    OTHER("Altro");

    companion object {
        /**
         * Parses a degree type from Italian text.
         *
         * @param text The Italian degree type text
         * @return The matching [EasyStaffDegreeType]
         */
        fun fromItalian(text: String): EasyStaffDegreeType {
            val normalized = text.trim().lowercase()
            return when {
                normalized.contains("magistrale a ciclo") -> SINGLE_CYCLE
                normalized.contains("magistrale") -> MASTER
                normalized.contains("laurea") -> BACHELOR
                normalized.contains("dottorato") -> PHD
                else -> OTHER
            }
        }
    }
}

/**
 * Represents a year of study within a program (e.g., 1st year, 2nd year).
 *
 * @property value The API value (e.g., "GGG|1")
 * @property year The numeric year (1, 2, 3, etc.)
 * @property label The display label (e.g., "1 - PERCORSO COMUNE")
 * @property trackName The curriculum/track name if any
 */
data class EasyStaffYearOfStudy(
    val value: String,
    val year: Int,
    val label: String,
    val trackName: String
) {
    override fun toString(): String = label
}

/**
 * Represents a teaching period (semester) within an academic year.
 *
 * @property id The period ID
 * @property code The period code (e.g., "S1", "S2")
 * @property label The display label (e.g., "Primo semestre (S1)")
 */
data class EasyStaffTeachingPeriod(
    val id: String,
    val code: String,
    val label: String
) {
    override fun toString(): String = label
}

/**
 * Represents a building/location in the university campus.
 *
 * @property code The building code (e.g., "U01", "U06")
 * @property name The building name (e.g., "ATLAS ex U1", "AGORA' ex U6")
 */
@Serializable
data class EasyStaffBuilding(
    @SerialName("valore")
    val code: String,
    @SerialName("label")
    val name: String
) {
    override fun toString(): String = name

    companion object {
        /**
         * Special value representing all buildings.
         */
        val ALL = EasyStaffBuilding("all", "Tutte le sedi")

        /**
         * Special value representing events without a room.
         */
        val NO_ROOM = EasyStaffBuilding("senza_aula", "Senza aula")
    }
}

/**
 * Represents a room within a building.
 *
 * @property code The room code (e.g., "U6-22")
 * @property name The room name (e.g., "U6-22 con Podio")
 * @property buildingCode The code of the building containing this room
 * @property buildingName The name of the building
 */
data class EasyStaffRoom(
    val code: String,
    val name: String,
    val buildingCode: String,
    val buildingName: String
) {
    override fun toString(): String = "$name [$buildingName]"

    companion object {
        /**
         * Special value representing all rooms.
         */
        val ALL = EasyStaffRoom("all", "Tutte le aule", "", "")
    }
}

/**
 * Detailed information about a room.
 *
 * @property code The room code
 * @property name The room name
 * @property building The building containing this room
 * @property capacity The seating capacity
 * @property floor The floor number
 * @property isAccessible Whether the room is accessible for disabled persons
 * @property equipment List of available equipment
 * @property mapsUrl URL to Google Maps location
 */
data class EasyStaffRoomDetails(
    val code: String,
    val name: String,
    val building: EasyStaffBuilding,
    val capacity: Int?,
    val floor: String?,
    val isAccessible: Boolean,
    val equipment: List<String>,
    val mapsUrl: String?
)

/**
 * Display mode for schedule views.
 */
enum class EasyStaffScheduleDisplayMode(val value: String, val label: String) {
    /**
     * Weekly agenda view with time slots.
     */
    WEEKLY_AGENDA("cal", "Agenda settimanale"),

    /**
     * Basic schedule table.
     */
    BASIC_SCHEDULE("std", "Orario base"),

    /**
     * Compact view.
     */
    COMPACT("compact", "Orario compatto"),

    /**
     * Event list view.
     */
    EVENT_LIST("list", "Lista eventi");

    companion object {
        /**
         * Gets a display mode by its value.
         *
         * @param value The mode value
         * @return The matching [EasyStaffScheduleDisplayMode], or [WEEKLY_AGENDA] as default
         */
        fun fromValue(value: String): EasyStaffScheduleDisplayMode {
            return entries.find { it.value == value } ?: WEEKLY_AGENDA
        }
    }
}

/**
 * Day of the week.
 */
enum class EasyStaffDayOfWeek(val value: String, val italianName: String) {
    MONDAY("1", "Lunedì"),
    TUESDAY("2", "Martedì"),
    WEDNESDAY("3", "Mercoledì"),
    THURSDAY("4", "Giovedì"),
    FRIDAY("5", "Venerdì"),
    SATURDAY("6", "Sabato"),
    SUNDAY("7", "Domenica");

    companion object {
        /**
         * Gets a day of week by its value.
         *
         * @param value The day value (1-7)
         * @return The matching [EasyStaffDayOfWeek], or null if not found
         */
        fun fromValue(value: String): EasyStaffDayOfWeek? {
            return entries.find { it.value == value }
        }

        /**
         * Gets a day of week from Java's [DayOfWeek].
         */
        fun from(javaDay: DayOfWeek): EasyStaffDayOfWeek {
            return entries[javaDay.ordinal]
        }
    }
}
