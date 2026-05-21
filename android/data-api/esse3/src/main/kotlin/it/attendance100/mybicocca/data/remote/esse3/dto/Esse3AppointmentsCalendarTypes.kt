package it.attendance100.mybicocca.data.remote.esse3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3Appointment(
    /** Identificativo turno */
    @SerialName("turnoId")
    val shiftId: Int = 0,

    /** Identificativo utente */
    @SerialName("stuId")
    val studentId: Int? = null,

    /** Nota */
    @SerialName("nota")
    val note: String? = null,

    /** Identificativo nota */
    @SerialName("notaId")
    val noteId: Long? = null
)

@Serializable
data class Esse3ShiftId(
    /** Identificativo turno */
    @SerialName("turnoId")
    val shiftId: Long? = null
)

@Serializable
data class Esse3ExamSessionShiftCalendar(
    /** Identificativo del calendario */
    @SerialName("calendarioAppId")
    val calendarCallId: Long? = null,

    /** Codice tipologia del calendario */
    @SerialName("tipoCalAppCod")
    val calendarCallTypeCode: String? = null,

    /** Codice del calendario */
    @SerialName("calendarioAppCod")
    val calendarCallCode: String? = null,

    /** Descrizione del calendario */
    @SerialName("calendarioAppDes")
    val calendarCallDescription: String? = null,

    /** Data effettiva di inizio del turno */
    @SerialName("dataIniTurno")
    val shiftStartDate: String? = null,

    /** Data effettiva di fine del turno */
    @SerialName("dataFineTurno")
    val shiftEndDate: String? = null,

    /** Anno accademico di applicazione del calendario */
    @SerialName("calendarioAppAaDes")
    val calendarCallAcademicYearDescription: String? = null,

    /** Identificativo del turno */
    @SerialName("calTurnoId")
    val calendarShiftId: Long? = null,

    /** Data inizio del turno vista dagli studenti */
    @SerialName("dataIniTurnoVis")
    val shiftViewStartDate: String? = null,

    /** Data fine del turno vista dagli studenti */
    @SerialName("dataFineTurnoVis")
    val shiftViewEndDate: String? = null,

    /** Data di apertura delle prenotazioni */
    @SerialName("dataIniPren")
    val bookingStartDate: String? = null,

    /** Data di chiusura delle prenotazioni */
    @SerialName("dataFinePren")
    val bookingEndDate: String? = null,

    /** Sede */
    @SerialName("sedeDes")
    val siteDescription: String? = null,

    /** Struttura amministrativa */
    @SerialName("strutAmmDes")
    val administrativeStructureDescription: String? = null,

    /** Abilita l'inserimento delle note */
    @SerialName("abilNotaprenFlg")
    val bookingNoteAuthorizationFlag: Long? = null,

    /** Tipologia della nota */
    @SerialName("tipoNotaId")
    val noteTypeId: Long? = null,

    /** Giorno della settimana */
    @SerialName("giorno")
    val day: String? = null
)

@Serializable
data class Esse3Booking(
    /** Codice tipologia del calendario */
    @SerialName("tipoCalAppCod")
    val calendarCallTypeCode: String? = null,

    /** Descrizione tipologia del calendario */
    @SerialName("tipoCalAppDes")
    val calendarCallTypeDescription: String? = null,

    /** Modalità di cancellazione dell'iscrizione al calendario */
    @SerialName("modCancIscrCalId")
    val enrollmentCancellationCalendarModeId: Long? = null,

    /** Identificativo iscrizione al calendario */
    @SerialName("calAppIscrittiId")
    val calendarCallEnrolledId: Long? = null,

    /** Data inizio del turno vista dagli studenti */
    @SerialName("dataIniTurnoVis")
    val shiftViewStartDate: String? = null,

    /** Data fine del turno vista dagli studenti */
    @SerialName("dataFineTurnoVis")
    val shiftViewEndDate: String? = null,

    /** giorno */
    @SerialName("giorno")
    val day: String? = null,

    /** Numero di giorni ammesso per la modifica della prenotazione */
    @SerialName("nGgModifica")
    val modificationDaysNumber: Long? = null,

    /** Data di prenotazione */
    @SerialName("dataPrenotazione")
    val bookingDate: String? = null,

    /** Sede */
    @SerialName("sede")
    val site: String? = null,

    /** Struttura amministrativa */
    @SerialName("strutAmmDes")
    val administrativeStructureDescription: String? = null
)

@Serializable
data class Esse3ExamSessionCalendarTypesList(
    /** Codice tipologia del calendario */
    @SerialName("tipoCalAppCod")
    val calendarCallTypeCode: String? = null,

    /** Descrizione tipologia del calendario */
    @SerialName("tipoCalAppDes")
    val calendarCallTypeDescription: String? = null,

    /** Modalità di cancellazione dell'iscrizione al calendario */
    @SerialName("modCancIscrCalId")
    val enrollmentCancellationCalendarModeId: Long? = null
)
