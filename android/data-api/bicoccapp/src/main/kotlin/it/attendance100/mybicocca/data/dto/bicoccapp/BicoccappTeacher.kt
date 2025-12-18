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
    val roles: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappTeacherRole> = emptyList(),

    @SerializedName("phones")
    val phones: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappTeacherPhone> = emptyList(),

    @SerializedName("rooms")
    val rooms: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappTeacherRoom> = emptyList(),

    @SerializedName("offices")
    val offices: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappTeacherOffice> = emptyList()
)

