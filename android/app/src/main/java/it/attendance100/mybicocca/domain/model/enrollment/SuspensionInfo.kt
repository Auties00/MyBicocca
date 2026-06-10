package it.attendance100.mybicocca.domain.model.enrollment

/**
 * Suspension details of an annual enrollment, present only when the year's enrollment
 * was a suspended (fictitious) one (Esse3 `sospFlg == 1`).
 *
 * @property reasonCode Esse3 motivation code behind the suspension.
 */
data class SuspensionInfo(
    val reasonCode: String?,
)
