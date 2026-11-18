package it.attendance100.mybicocca.data.remote.easystaff.api

import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFieldPosition
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFormField
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime

/**
 * Offline coverage for the booking form positions and the reservation request built from them.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EasyStaffPlanningReservationRequestTest {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }
    private val api = EasyStaffApi()
    private val informationDesks get() = api.planning.informationDesks

    @AfterAll
    fun close() = api.close()

    @Test
    fun positionsDecodeFromStringsNumbersAndBackoffice() {
        val positions = listOf("\"1\"", "2", "\"100\"", "\"backoffice\"", "\"7\"")
            .map { json.decodeFromString<EasyStaffPlanningFormField>(field("f", it)).position }

        assertEquals(
            listOf(
                EasyStaffPlanningFieldPosition.User,
                EasyStaffPlanningFieldPosition.Service,
                EasyStaffPlanningFieldPosition.Gdpr,
                EasyStaffPlanningFieldPosition.Backoffice,
                EasyStaffPlanningFieldPosition.Other("7"),
            ),
            positions
        )
    }

    @Test
    fun requestSplitsValuesByPositionLikeTheWebFrontEnd() {
        val form = listOf(
            field("email", "\"1\"", primary = true),
            field("cognome_nome", "\"1\""),
            field("matricola", "\"2\""),
            field("note_aggiuntive", "\"2\""),
            field("staff_note", "\"backoffice\""),
            field("gdpr", "\"100\""),
            field("legacy", "\"7\""),
        ).map { json.decodeFromString<EasyStaffPlanningFormField>(it) }

        val request = informationDesks.buildReservationRequest(
            serviceId = 37,
            areaId = 81,
            slotStart = LocalDateTime.of(2026, 10, 21, 11, 0),
            durationSeconds = 900,
            formFields = form,
            values = mapOf(
                "email" to "a@b.it",
                "cognome_nome" to "ROSSI MARIO",
                "matricola" to "123456",
                "note_aggiuntive" to " ",
                "staff_note" to "x",
                "gdpr" to "true",
                "legacy" to "y",
            )
        )

        assertEquals("a@b.it", request.primaryValue)
        assertEquals(mapOf("email" to "a@b.it", "cognome_nome" to "ROSSI MARIO"), request.userFields)
        assertEquals(mapOf("matricola" to "123456"), request.serviceFields)
        assertEquals(mapOf("staff_note" to "x"), request.backofficeFields)
        assertEquals(900L, request.endTimeEpochSeconds - request.startTimeEpochSeconds)
    }

    private fun field(code: String, position: String, primary: Boolean = false): String =
        """{"id":1,"label":"$code","codice":"$code","tipo":"text","cliente":2,""" +
            """"primaria":${if (primary) 1 else 0},"posizione":$position}"""
}
