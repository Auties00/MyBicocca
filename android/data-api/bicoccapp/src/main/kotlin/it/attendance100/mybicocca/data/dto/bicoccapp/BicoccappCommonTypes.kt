package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Gender of a person.
 */
@Serializable
enum class BicoccappGender {
    @SerialName("M")
    MALE,
    @SerialName("F")
    FEMALE
}