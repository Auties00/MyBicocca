package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappTeacher(
    @SerialName("title")
    val title: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("surname")
    val surname: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("email_alternate")
    val emailAlternate: String? = null,

    @SerialName("teacherCode")
    val teacherCode: String? = null,

    @SerialName("gender")
    val gender: String? = null,

    @SerialName("teacherKey")
    val teacherKey: String? = null,

    @SerialName("receivesOn")
    val receivesOn: String? = null,

    @SerialName("roles")
    val roles: List<BicoccappTeacherRole> = emptyList(),

    @SerialName("phones")
    val phones: List<BicoccappTeacherPhone> = emptyList(),

    @SerialName("rooms")
    val rooms: List<BicoccappTeacherRoom> = emptyList(),

    @SerialName("offices")
    val offices: List<BicoccappTeacherOffice> = emptyList()
)

