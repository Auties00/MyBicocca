package it.attendance100.mybicocca.data.api.easystaff

import io.ktor.client.*
import it.attendance100.mybicocca.data.dto.easystaff.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * API for core query operations.
 *
 * Provides access to:
 * - Academic years
 * - Teaching areas
 * - Study programs
 * - Teachers
 */
class EasyStaffCoreApi(
    client: HttpClient,
    json: Json
) : EasyStaffAbstractApi(client, json) {
    /**
     * Gets the list of available academic years.
     *
     * @return List of available [EasyStaffAcademicYear], sorted by start year descending (newest first)
     */
    suspend fun getAcademicYears(): List<EasyStaffAcademicYear> {
        val response = executeGetText(
            COMBO_ENDPOINT,
            mapOf(
                "sw" to listOf("ec_"),
                "aa" to listOf("1")
            )
        )

        val jsonString = extractJsonFromJsVariable(response, "anni_accademici_ec")
            ?: throw IllegalStateException("Failed to parse academic years response")

        return json.parseToJsonElement(jsonString)
            .jsonObject
            .asSequence()
            .map { (_, value) -> json.decodeFromJsonElement<EasyStaffAcademicYear>(value) }
            .sortedByDescending { it.startYear }
            .toList()
    }

    /**
     * Gets the list of all teaching areas.
     *
     * @param language The language for labels
     * @return List of available [EasyStaffTeachingArea]
     */
    suspend fun getTeachingAreas(
        language: EasyStaffLanguage = EasyStaffLanguage.ITALIAN
    ): List<EasyStaffTeachingArea> {
        val response = executeGetText(
            COMBO_ENDPOINT,
            mapOf(
                "sw" to listOf("rooms_"),
                "_lang" to listOf(language.code)
            )
        )

        val jsonString = extractJsonFromJsVariable(response, "elenco_scuole")
            ?: throw IllegalStateException("Failed to parse teaching areas response")

        return json.decodeFromString(jsonString)
    }

    /**
     * Gets the list of all teachers for a given academic year.
     *
     * @param academicYear The academic year
     * @return List of teachers
     */
    suspend fun getTeachers(
        academicYear: EasyStaffAcademicYear
    ): List<EasyStaffTeacher> {
        val response = executeGetText(
            COMBO_ENDPOINT, mapOf(
                "sw" to listOf("ec_"),
                "aa" to listOf(academicYear.value),
                "page" to listOf("docenti")
            )
        )

        // Extract JSON from JavaScript variable assignment
        val jsonString = extractJsonFromJsVariable(response, "elenco_docenti")
            ?: throw IllegalStateException("Failed to parse teachers response")

        return json.parseToJsonElement(jsonString)
            .jsonArray
            .map { value -> json.decodeFromJsonElement<EasyStaffTeacher>(value) }
    }

    /**
     * Gets the study programs available for a teaching area in a given academic year.
     *
     * This loads detailed information including years of study and subjects.
     *
     * @param academicYear The academic year
     * @param teachingAreaCode The teaching area code (optional, returns all if null)
     * @return The available study programs with details
     */
    suspend fun getStudyPrograms(
        academicYear: EasyStaffAcademicYear,
        teachingAreaCode: String? = null
    ): List<EasyStaffStudyProgramDetails> {
        val response = executeGetText(
            COMBO_ENDPOINT,
            mapOf(
                "sw" to listOf("ec_"),
                "aa" to listOf(academicYear.value),
                "page" to listOf("corsi")
            )
        )

        val jsonString = extractJsonFromJsVariable(response, "elenco_corsi")
            ?: throw IllegalStateException("Failed to parse study programs response")

        val allPrograms: List<EasyStaffStudyProgramDetails> = json.decodeFromString(jsonString)

        return if (teachingAreaCode != null) {
            allPrograms.filter { it.teachingAreaCode == teachingAreaCode }
        } else {
            allPrograms
        }
    }
}
