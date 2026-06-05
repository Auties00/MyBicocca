package it.attendance100.mybicocca.domain.model.enrollment

// Present only when the year's enrollment was a suspended (fictitious) one (sospFlg == 1).
data class SuspensionInfo(
    val reasonCode: String?,
)
