package it.attendance100.mybicocca.data.mapper.account

import it.attendance100.mybicocca.core.observability.UnknownValues
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.elearning.course.CourseLevel

/**
 * Maps the Esse3 career status code (`staStuCod`) onto the domain status.
 *
 * Esse3 documents "A = Attivo, S = Sospeso, I = Ipotesi", so "I" is a provisional career and not
 * an interrupted one. The mnemonic codes and the single letters for graduated, transferred
 * ("T"/"TRA") and withdrawn ("R"/"RIN") careers are unconfirmed guesses, kept until real data
 * says otherwise.
 *
 * Unrecognized and missing codes are reported and degrade to OTHER. The status reason
 * (`motStastuCod`) is reported with them: an ended career may carry one status code for every way
 * of ending, with only the reason telling a graduation from a withdrawal.
 */
internal fun mapCareerStatus(code: String?, reasonCode: String? = null): CareerStatus =
    when (code?.uppercase()) {
        "A", "ATT", "ATTIVA" -> CareerStatus.ACTIVE
        "S", "SOS", "SOSPESA" -> CareerStatus.SUSPENDED
        "I" -> CareerStatus.PROVISIONAL
        "L", "LAU", "LAUREATO", "LAUREATA" -> CareerStatus.GRADUATED
        "INT", "T", "TRA", "R", "RIN" -> CareerStatus.INTERRUPTED
        else -> {
            UnknownValues.report("career_status", code, reportMissing = true)
            UnknownValues.report("career_status_reason", reasonCode)
            CareerStatus.OTHER
        }
    }

/**
 * Maps the Esse3 course-type code (`tipoCorsoCod`) onto the degree level. "L2" (bachelor's) and
 * "LM" (master's) are confirmed against real careers; "LM5" and "LM6", the single-cycle master's,
 * follow the same Esse3 convention but have not been seen yet. Every other code is reported and
 * yields no level, so the tile simply shows no badge.
 */
internal fun mapCareerLevel(code: String?): CourseLevel? = when (code?.trim()?.uppercase()) {
    "L2" -> CourseLevel.Bachelor
    "LM", "LM5", "LM6" -> CourseLevel.Master
    else -> {
        UnknownValues.report("career_course_type", code)
        null
    }
}
