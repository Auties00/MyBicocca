package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3AttendanceProcedureParameters(
    /** flag che indica se assegnare la frequenza anche per studenti cessati */
    @SerialName("forzaStudentiX")
    val forceStudentsX: Boolean = false,

    /** flag che indica se assegnare la data di frequenza */
    @SerialName("forzaDataFreq")
    val forceAttendanceDate: Boolean = false,

    /** flag che indica se assegnare la frequenza anche alle AD già frequentate */
    @SerialName("forzaStatoF")
    val forceStateFFlag: Boolean = false,

    /** flag che indica se assegnare la frequenza anche se non previsto dalla frequenza automatica */
    @SerialName("forzaNonPreviste")
    val forceUnplanned: Boolean = false,

    /** partizione dell'anno accademico (semestre) */
    @SerialName("partCod")
    val partialCode: String? = null,

    /** dominio di partizione da filtrare (domPartCod e fatPartCod devono essere entrambi valorizzati) */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** fattore di partizione da filtrare (domPartCod e fatPartCod devono essere entrambi valorizzati) */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** codice dell'attività didattica su cui applicare la frequenza, in alternativa al parametro adId */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** id dell'attività didattica su cui applicare la frequenza, in alternativa al parametro adCod */
    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("filtriComuni")
    val commonFilters: Esse3FilterParameters? = null
)

@Serializable
data class Esse3SubstitutionProcedureParameters(
    @SerialName("filtriComuni")
    val commonFilters: Esse3FilterParameters? = null,

    /** id dell'attività didattica da sostituire, da utilizzare in alternativa ad adCodDaSostituire */
    @SerialName("adIdDaSostituire")
    val activityIdToReplace: Long? = null,

    /** codice attività didattica da sostituire, da utilizzare in alternativa ad adIdDaSostituire */
    @SerialName("adCodDaSostituire")
    val activityCodeToReplace: String? = null,

    /** flag che indica se sostituire l'attività didattica anche se superata */
    @SerialName("forzaStatoS")
    val forceStateSFlag: Boolean = false,

    @SerialName("chiaveADdaInserire")
    val teachingActivityKeyToInsert: Esse3ContextualizedActivityKey? = null,

    /** id dell'unità didattica da sostituire, valido solo se valorizzata la chiave dell'AD */
    @SerialName("udId")
    val teachingUnitId: Long? = null
)

@Serializable
data class Esse3CareerUpdateRow(
    /** id della testata della lista da processare */
    @SerialName("aggcarrId")
    val careerUpdateId: Long? = null,

    /** id del dettaglio della lista da processare */
    @SerialName("aggcarrdettId")
    val careerUpdateDetailId: Long? = null,

    /** id della carriera dello studente */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** id del tratto di carriera dello studente */
    @SerialName("matId")
    val matId: Long? = null,

    /** id della riga di libretto dello studente */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** id della prova di una riga di libretto dello studente */
    @SerialName("adregId")
    val activityRegulationId: Long? = null,

    /** matricola dello studente */
    @SerialName("matricola")
    val matricola: String? = null,

    /** nome dello studente */
    @SerialName("nome")
    val name: String? = null,

    /** cognome dello studente */
    @SerialName("cognome")
    val surname: String? = null,

    /** coorte dello studente */
    @SerialName("coorte")
    val cohort: Int? = null,

    /** id del corso di studio di iscrizone dello studente */
    @SerialName("cdsStuId")
    val courseOfStudyStudentId: Long? = null,

    /** codice del corso di studio di iscrizone dello studente */
    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    /** descrizione del corso di studio di iscrizone dello studente */
    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    /** id dell'anno di ordinamento di studio di iscrizone dello studente */
    @SerialName("aaOrdStuId")
    val academicYearOrderStudentId: Int? = null,

    /** id del percorso di studio di iscrizone dello studente */
    @SerialName("pdsStuId")
    val studyPlanStudentId: Long? = null,

    /** codice del percorso di studio di iscrizone dello studente */
    @SerialName("pdsStuCod")
    val studyPlanStudentCode: String? = null,

    /** descrizione del percorso di studio di iscrizone dello studente */
    @SerialName("pdsStuDes")
    val studyPlanStudentDescription: String? = null,

    /** coorte dello studente */
    @SerialName("adId")
    val activityId: Int? = null,

    /** codice attività didattica del libretto */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione attività didattica del libretto */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** anno di frequenza */
    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    /** data di acquisizione della frequenza */
    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    /** data di superamento dell'attività */
    @SerialName("dataSup")
    val supDate: String? = null,

    @SerialName("esito")
    val outcome: Esse3Result? = null,

    /** codice profilo studente */
    @SerialName("profCod")
    val professionCode: String? = null,

    /** flag che indica se l'attività risulta da elaborare oppure no */
    @SerialName("elaboraFlg")
    val processFlag: Int? = null,

    /** descrizione dell'errore nel caso l'elaborazione della riga risulta fallita */
    @SerialName("errMsg")
    val errorMessage: String? = null
)

@Serializable
data class Esse3SegmentProcedureParameters(
    /** flag che indica se ricalcolare i segmenti anche per le attività didattiche in stato Frequentato o superato */
    @SerialName("forzaStatoFS")
    val forceStateFSFlag: Boolean = false,

    /** flag che indica ricalcolare i segmenti anche per studenti cessati */
    @SerialName("forzaStudentiX")
    val forceStudentsX: Boolean = false,

    /** flag che indica se calcolare i segmenti anche per attività didattiche libere */
    @SerialName("forzaADLibereFlg")
    val forceFreeTeachingActivitiesFlag: Boolean = false,

    /** anno di offerta di erogazione dell'attività didatta del libretto */
    @SerialName("aaOffId")
    val academicYearOfferId: Long? = null,

    /** id dell'attività didattica, in alternativa al parametro adCod */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice dell'attività didattica, in alternativa al parametro adId */
    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("filtriComuni")
    val commonFilters: Esse3FilterParameters? = null
)

@Serializable
data class Esse3CareerUpdateHeaderParameter(
    /** id della testata della lista da processare */
    @SerialName("aggcarrId")
    val careerUpdateId: Long? = null,

    /** codice del parametro */
    @SerialName("paramCod")
    val parameterCode: String? = null,

    /** descrizione del parametro */
    @SerialName("paramDes")
    val parameterDescription: String? = null,

    /** valore alfanumerico del parametro (se previsto) */
    @SerialName("valAlfa")
    val alphanumericValue: String? = null,

    /** valore numerico del parametro (se previsto) */
    @SerialName("valNum")
    val numericValue: Float? = null
)

@Serializable
data class Esse3ProcedureUpdateActivityOfferParameters(
    @SerialName("filtriComuni")
    val commonFilters: Esse3FilterParameters? = null,

    /** flag che indica se calcolare i segmenti anche per attività didattiche libere */
    @SerialName("forzaADLibereFlg")
    val forceFreeTeachingActivitiesFlag: Boolean = false,

    /** id attività didattica da aggiornare, da utilizzare in alternativa ad adCodDaAggiornare */
    @SerialName("adIdDaAggiornare")
    val activityIdToUpdate: Long = 0L,

    /** codice attività didattica da aggiornare, da utilizzare in alternativa ad adIdDaAggiornare */
    @SerialName("adCodDaAggiornare")
    val activityCodeToUpdate: String? = null
)

@Serializable
data class Esse3CareerUpdateHeader(
    /** id della testata della lista da processare */
    @SerialName("aggcarrId")
    val careerUpdateId: Long? = null,

    /** procedura associata */
    @SerialName("procCod")
    val procedureCode: String? = null,

    /** stato della preview 0 - preview, 1 - elaborazione già effettuata */
    @SerialName("stato")
    val state: Int? = null,

    /** id della facoltà/dipartimento su cui è stato lanciato il motore */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice della facoltà/dipartimento su cui è stato lanciato il motore */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione della facoltà/dipartimento su cui è stato lanciato il motore */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** id del corso di studio su cui è stato lanciato il motore */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio su cui è stato lanciato il motore */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di studio su cui è stato lanciato il motore */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** anno di ordinamento su cui è stato lanciato il motore */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** id del percorso di studio su cui è stato lanciato il motore */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** codice del percorso di studio su cui è stato lanciato il motore */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** descrizione del percorso di studio su cui è stato lanciato il motore */
    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    /** anno di coorte su cui è stato lanciato il motore */
    @SerialName("aaRegId")
    val academicYearRegulationId: Long? = null,

    @SerialName("parametri")
    val parameters: List<Esse3CareerUpdateHeaderParameter> = emptyList()
)

@Serializable
data class Esse3CareerUpdateLog(
    /** id della testata della lista da processare */
    @SerialName("aggcarrId")
    val careerUpdateId: Long? = null,

    /** id della testata del log */
    @SerialName("logTId")
    val logTypeId: Long? = null,

    /** log */
    @SerialName("logDes")
    val logDescription: String? = null
)

@Serializable
data class Esse3FilterParameters(
    /** id della facoltà/dipartimento di iscrizione dello studente (facId e cdsId sono mutuamente esclusivi) */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice della facoltà/dipartimento di iscrizione dello studente da utilizzare in alternativa all'id (facCod e cdsCod sono mutuamente esclusivi) */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** id del corso di studio di iscrizione dello studente (facId e cdsId sono mutuamente esclusivi) */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio di iscrizione dello studente da utilizzare in alternativa all'id (facCod e cdsCod sono mutuamente esclusivi) */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** anno di ordinamento di iscrizione dello studente (valido assieme a cdsId o cdsCod) */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** id del percorso di studio di iscrizione dello studente, vanno valorizzati anche cdsId e aaOrdId */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** codice del percorso di studio di iscrizione dello studente da utilizzare in alternativa all'id, vanno valorizzati anche cdsCod e aaOrdId */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** anno di coorte dello studente */
    @SerialName("aaRegId")
    val academicYearRegulationId: Long? = null
)

@Serializable
data class Esse3RemoveAttendanceProcedureParameters(
    /** flag che indica se rimuovere la frequenza anche per studenti cessati */
    @SerialName("forzaStudentiX")
    val forceStudentsX: Boolean = false,

    /** id dell'attività didattica su cui applicare la frequenza, in alternativa al parametro adCod */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice dell'attività didattica su cui applicare la frequenza, in alternativa al parametro adId */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** fattore di partizione da filtrare (domPartCod e fatPartCod devono essere entrambi valorizzati) */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** dominio di partizione da filtrare (domPartCod e fatPartCod devono essere entrambi valorizzati) */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** partizione dell'anno accademico (semestre) */
    @SerialName("partCod")
    val partialCode: String? = null,

    @SerialName("filtriComuni")
    val commonFilters: Esse3FilterParameters? = null
)
