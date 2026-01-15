package it.attendance100.mybicocca.data.dto.easystaff

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Supported languages for the Agenda Web platform.
 */
@Serializable
enum class EasyStaffLanguage(val code: String) {
    @SerialName("it")
    ITALIAN("it"),

    @SerialName("en")
    ENGLISH("en"),

    @SerialName("es")
    SPANISH("es"),

    @SerialName("de")
    GERMAN("de"),

    @SerialName("fr")
    FRENCH("fr")
}

/**
 * Day of the week.
 */
@Serializable
enum class EasyStaffDayOfWeek(val value: String) {
    @SerialName("1")
    MONDAY("1"),

    @SerialName("2")
    TUESDAY("2"),

    @SerialName("3")
    WEDNESDAY("3"),

    @SerialName("4")
    THURSDAY("4"),

    @SerialName("5")
    FRIDAY("5"),

    @SerialName("6")
    SATURDAY("6"),

    @SerialName("7")
    SUNDAY("7")
}
