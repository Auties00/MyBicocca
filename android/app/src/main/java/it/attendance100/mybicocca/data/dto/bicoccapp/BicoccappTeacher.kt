package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappTeacher(
    @SerializedName("title")
    val title: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("surname")
    val surname: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("email_alternate")
    val emailAlternate: String? = null,

    @SerializedName("teacherCode")
    val teacherCode: String? = null,

    @SerializedName("gender")
    val gender: String? = null,

    @SerializedName("teacherKey")
    val teacherKey: String? = null,

    @SerializedName("receivesOn")
    val receivesOn: String? = null,

    @SerializedName("roles")
    val roles: List<BicoccappTeacherRole> = emptyList(),

    @SerializedName("phones")
    val phones: List<BicoccappTeacherPhone> = emptyList(),

    @SerializedName("rooms")
    val rooms: List<BicoccappTeacherRoom> = emptyList(),

    @SerializedName("offices")
    val offices: List<BicoccappTeacherOffice> = emptyList()
)

