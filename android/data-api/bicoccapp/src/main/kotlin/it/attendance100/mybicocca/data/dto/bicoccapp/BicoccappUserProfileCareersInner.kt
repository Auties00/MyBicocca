package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappUserProfileCareersInner(
    @SerialName("matricId")
    val enrollmentId: Int? = null,

    @SerialName("selected")
    val selected: Boolean? = null,

    @SerialName("oldCode")
    val oldCode: Boolean? = null,

    @SerialName("matricCode")
    val enrollmentCode: String? = null,

    @SerialName("studentId")
    val studentId: Int? = null
)

