package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3Appointment(
    @SerialName("turnoId")
    val shiftId: Int,

    @SerialName("stuId")
    val studentId: Int? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("notaId")
    val noteId: Long? = null
)

@Serializable
data class Esse3ShiftId(
    @SerialName("turnoId")
    val shiftId: Long? = null
)

@Serializable
data class Esse3ExamSessionShiftCalendar(
    @SerialName("calendarioAppId")
    val calendarCallId: Long? = null,

    @SerialName("tipoCalAppCod")
    val calendarCallTypeCode: String? = null,

    @SerialName("calendarioAppCod")
    val calendarCallCode: String? = null,

    @SerialName("calendarioAppDes")
    val calendarCallDescription: String? = null,

    @SerialName("dataIniTurno")
    val shiftStartDate: String? = null,

    @SerialName("dataFineTurno")
    val shiftEndDate: String? = null,

    @SerialName("calendarioAppAaDes")
    val calendarCallAcademicYearDescription: String? = null,

    @SerialName("calTurnoId")
    val calendarShiftId: Long? = null,

    @SerialName("dataIniTurnoVis")
    val shiftViewStartDate: String? = null,

    @SerialName("dataFineTurnoVis")
    val shiftViewEndDate: String? = null,

    @SerialName("dataIniPren")
    val bookingStartDate: String? = null,

    @SerialName("dataFinePren")
    val bookingEndDate: String? = null,

    @SerialName("sedeDes")
    val siteDescription: String? = null,

    @SerialName("strutAmmDes")
    val administrativeStructureDescription: String? = null,

    @SerialName("abilNotaprenFlg")
    val bookingNoteAuthorizationFlag: Long? = null,

    @SerialName("tipoNotaId")
    val noteTypeId: Long? = null,

    @SerialName("giorno")
    val day: String? = null
)

@Serializable
data class Esse3Booking(
    @SerialName("tipoCalAppCod")
    val calendarCallTypeCode: String? = null,

    @SerialName("tipoCalAppDes")
    val calendarCallTypeDescription: String? = null,

    @SerialName("modCancIscrCalId")
    val enrollmentCancellationCalendarModeId: Long? = null,

    @SerialName("calAppIscrittiId")
    val calendarCallEnrolledId: Long? = null,

    @SerialName("dataIniTurnoVis")
    val shiftViewStartDate: String? = null,

    @SerialName("dataFineTurnoVis")
    val shiftViewEndDate: String? = null,

    @SerialName("giorno")
    val day: String? = null,

    @SerialName("nGgModifica")
    val modificationDaysNumber: Long? = null,

    @SerialName("dataPrenotazione")
    val bookingDate: String? = null,

    @SerialName("sede")
    val site: String? = null,

    @SerialName("strutAmmDes")
    val administrativeStructureDescription: String? = null
)

@Serializable
data class Esse3ExamSessionCalendarTypesList(
    @SerialName("tipoCalAppCod")
    val calendarCallTypeCode: String? = null,

    @SerialName("tipoCalAppDes")
    val calendarCallTypeDescription: String? = null,

    @SerialName("modCancIscrCalId")
    val enrollmentCancellationCalendarModeId: Long? = null
)
