package it.attendance100.mybicocca.data.remote.esse3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3PutTeacherNotes(
    /** Note biografiche del docente. */
    @SerialName("noteBiografiche")
    val biographicalNotes: String? = null,

    /** Note sulle pubblicazioni del docente. */
    @SerialName("notePubblicazioni")
    val publicationsNotes: String? = null,

    /** Note sul curriculum accademico del docente. */
    @SerialName("noteCurriculum")
    val curriculumNotes: String? = null,

    /** Note libere relative al docente. */
    @SerialName("noteDocente")
    val lecturerNotes: String? = null
)

@Serializable
data class Esse3TeacherRole(
    /** Codice ruolo docente. */
    @SerialName("ruoloDocCod")
    val lecturerRoleCode: String? = null,

    /** Descrizione codice ruolo docente. */
    @SerialName("ruoloDocDes")
    val lecturerRoleDescription: String? = null,

    /** Codice tipologia ruolo docente. */
    @SerialName("tipoRuoloDocCod")
    val lecturerRoleTypeCode: String? = null,

    /** Descrizione codice tipologia ruolo docente. */
    @SerialName("tipoRuoliDocDes")
    val lecturerRolesDescription: String? = null,

    /** Codice CSA. */
    @SerialName("csaCod")
    val csaCode: String? = null,

    /** Ruolo docente. */
    @SerialName("ruoloDoc")
    val lecturerRole: String? = null
)

@Serializable
data class Esse3PostTeacherSchedule(
    /** giorno della settimana */
    @SerialName("giorno")
    val day: Int = 0,

    /** ora inizio dell'appuntemento. */
    @SerialName("oraInizio")
    val startTime: String = "",

    /** ora fine dell'appuntemento. */
    @SerialName("oraFine")
    val endTime: String = "",

    /** luogo dell'appuntamento */
    @SerialName("desLuogo")
    val placeDescription: String? = null,

    /** nota collegata all'appuntamento */
    @SerialName("nota")
    val note: String? = null
)

@Serializable
data class Esse3TeacherParameters(
    /** email */
    @SerialName("email")
    val email: String? = null
)
