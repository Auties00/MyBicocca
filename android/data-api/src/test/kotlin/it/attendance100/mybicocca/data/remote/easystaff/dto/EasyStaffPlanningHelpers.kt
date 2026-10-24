package it.attendance100.mybicocca.data.remote.easystaff.dto

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/**
 * The university's Portale Planning portals, as the values of the generated API's
 * `portalId` and `portalCode` parameters.
 */
enum class EasyStaffPlanningPortal(val id: Int, val code: String) {
    /** The student information desks ("Sportelli Informativi"), which support anonymous booking. */
    INFORMATION_DESKS(2, "unimib-segreteria"),

    /**
     * The legacy study rooms portal ("Prenotazione Aule Studio"). Its login is mandatory and
     * blocked, so it lists nothing to anonymous users; study rooms moved to Affluences.
     */
    STUDY_ROOMS(1, "unimib-aulestudio"),
}

/** An option of a booking form field: the value to send, and the label to show. */
data class EasyStaffPlanningFieldOption(val value: String, val label: String)

/**
 * The options of the field, from [EasyStaffPlanningFormField.values]: the labels of a select
 * by value, a list of values, or a single value (the privacy policy URL of the GDPR checkbox).
 * Values without a label are their own label. Null when the field has none.
 */
val EasyStaffPlanningFormField.options: List<EasyStaffPlanningFieldOption>?
    get() = when (val values = values) {
        is JsonObject -> values.entries.mapNotNull { (value, label) ->
            (label as? JsonPrimitive)?.contentOrNull?.let { EasyStaffPlanningFieldOption(value, it) }
        }
        is JsonArray -> values.mapNotNull { item ->
            (item as? JsonPrimitive)?.contentOrNull?.let { EasyStaffPlanningFieldOption(it, it) }
        }
        is JsonPrimitive -> values.contentOrNull?.takeUnless { it.isBlank() }?.let { listOf(EasyStaffPlanningFieldOption(it, it)) }
        null -> null
    }

/**
 * The entry identifier of the reservation, which confirms it and downloads its PDF. The
 * backend sends either the bare identifier or the entry object.
 */
val EasyStaffPlanningReservationResult.entryId: Int?
    get() = when (val entry = entry) {
        is JsonPrimitive -> entry.intOrNull
        is JsonObject -> (entry["id"] as? JsonPrimitive)?.intOrNull
        else -> null
    }

/** The slots of the requested day, which the backend nests under its date. */
val EasyStaffPlanningDaySchedule.slots: Map<EasyStaffPlanningTimeRange, EasyStaffPlanningSlotAvailability>
    get() = days.values.firstOrNull().orEmpty()
