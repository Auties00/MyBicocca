package it.attendance100.mybicocca.data.remote.easystaff.api

import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFieldPosition
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFormField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Offline coverage for the positions of booking form fields, which arrive as numbers, numeric strings or `backoffice`. */
class EasyStaffPlanningFieldPositionTest {
    @Test
    fun positionsDecodeFromStringsNumbersAndBackoffice() {
        val positions = listOf("\"1\"", "2", "\"100\"", "\"backoffice\"", "\"7\"")
            .map { EasyStaffPlanningRuntime.json.decodeFromString<EasyStaffPlanningFormField>(field(it)).position }

        assertEquals(
            listOf(
                EasyStaffPlanningFieldPosition.User,
                EasyStaffPlanningFieldPosition.Service,
                EasyStaffPlanningFieldPosition.Gdpr,
                EasyStaffPlanningFieldPosition.Backoffice,
                EasyStaffPlanningFieldPosition.Unknown("7"),
            ),
            positions
        )
    }

    private fun field(position: String): String =
        """{"id":1,"label":"f","codice":"f","tipo":"text","cliente":2,"primaria":0,"posizione":$position}"""
}
