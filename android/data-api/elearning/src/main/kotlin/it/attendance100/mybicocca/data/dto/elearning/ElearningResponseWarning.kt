package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a Moodle warning returned by API calls.
 */
@Serializable
data class ElearningResponseWarning(
    @SerialName("item")
    val item: String? = null,
    @SerialName("itemid")
    val itemId: Int? = null,
    @SerialName("warningcode")
    val warningCode: String? = null,
    @SerialName("message")
    val message: String? = null
)