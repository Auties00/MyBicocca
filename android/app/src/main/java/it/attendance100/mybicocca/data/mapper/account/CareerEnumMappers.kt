package it.attendance100.mybicocca.data.mapper.account

import android.util.Log
import it.attendance100.mybicocca.domain.model.career.CareerStatus

private const val TAG = "CareerMappers"

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
