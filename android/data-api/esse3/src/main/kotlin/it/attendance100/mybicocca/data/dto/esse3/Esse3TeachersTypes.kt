package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3PutTeacherNotes(
    @SerialName("noteBiografiche")
    val biographicalNotes: String? = null,

    @SerialName("notePubblicazioni")
    val publicationsNotes: String? = null,

    @SerialName("noteCurriculum")
    val curriculumNotes: String? = null,

    @SerialName("noteDocente")
    val lecturerNotes: String? = null
)

@Serializable
data class Esse3TeacherRole(
    @SerialName("ruoloDocCod")
    val lecturerRoleCode: String? = null,

    @SerialName("ruoloDocDes")
    val lecturerRoleDescription: String? = null,

    @SerialName("tipoRuoloDocCod")
    val lecturerRoleTypeCode: String? = null,

    @SerialName("tipoRuoliDocDes")
    val lecturerRolesDescription: String? = null,

    @SerialName("csaCod")
    val csaCode: String? = null,

    @SerialName("ruoloDoc")
    val lecturerRole: String? = null
)

@Serializable
data class Esse3PostTeacherSchedule(
    @SerialName("giorno")
    val day: Int,

    @SerialName("oraInizio")
    val startTime: String,

    @SerialName("oraFine")
    val endTime: String,

    @SerialName("desLuogo")
    val placeDescription: String? = null,

    @SerialName("nota")
    val note: String? = null
)

@Serializable
data class Esse3TeacherParameters(
    @SerialName("email")
    val email: String? = null
)
