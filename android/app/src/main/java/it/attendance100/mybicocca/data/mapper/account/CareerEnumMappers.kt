package it.attendance100.mybicocca.data.mapper.account

import android.util.Log
import it.attendance100.mybicocca.domain.model.career.CareerStatus

private const val TAG = "CareerMappers"

/**
 * Maps the Esse3 career status code (`staStuCod`) onto the domain status. Esse3 emits either
 * single-letter or mnemonic codes; the interruption family covers interrupted ("I"/"INT"),
 * transferred ("T"/"TRA"), and withdrawn ("R"/"RIN") careers. Unrecognized codes are logged and
 * degrade to OTHER.
 */
internal fun mapCareerStatus(code: String?): CareerStatus = when (code?.uppercase()) {
    "A", "ATT", "ATTIVA" -> CareerStatus.ACTIVE
    "S", "SOS", "SOSPESA" -> CareerStatus.SUSPENDED
    "L", "LAU", "LAUREATO", "LAUREATA" -> CareerStatus.GRADUATED
    "I", "INT", "T", "TRA", "R", "RIN" -> CareerStatus.INTERRUPTED
    null, "" -> CareerStatus.OTHER
    else -> {
        Log.w(TAG, "Unknown career status code: '$code' — falling back to OTHER")
        CareerStatus.OTHER
    }
}
