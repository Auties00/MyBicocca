package it.attendance100.mybicocca.data.remote.esse3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3TeacherDiaryWithDetails(
    /** id del diario docente */
    @SerialName("diarioId")
    val diaryId: Long = 0L,

    /** id del diario docente */
    @SerialName("aaId")
    val academicYearId: Int = 0,

    /** id del del docente a cui appartiene il registro */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** cognome del docente a cui apprtiene il registro */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome del docente a cui apprtiene il registro */
    @SerialName("nome")
    val name: String? = null,

    /** cognome del docente a cui apprtiene il registro */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** codice dello stato del diario */
    @SerialName("statoDiarioCod")
    val diaryStateCode: String? = null,

    /** descrizione dello stato del diario */
    @SerialName("statoDiarioDes")
    val diaryStateDescription: String? = null,

    /** tipo gestione diario */
    @SerialName("tipoGestDiarioDocCod")
    val documentDiaryManagementTypeCode: String? = null,

    /** flag per la firma digitale del diario */
    @SerialName("firmaDigitaleFlg")
    val digitalSignatureFlag: Int? = null,

    /** annotazioni del docente sul proprio diario */
    @SerialName("osservazioni")
    val observations: String? = null,

    /** totale ore rendicontate del dettaglio */
    @SerialName("oreDett")
    val detailedHours: Float? = null,

    /** totale ore rendicontate del dettaglio annuale */
    @SerialName("oreDettAnnuali")
    val annualDetailedHours: Float? = null,

    @SerialName("attivita")
    val activity: List<Esse3TeacherDiaryDetail> = emptyList(),

    @SerialName("attivitaAnnuali")
    val annualActivities: List<Esse3TeacherDiaryDetailAcademicYear> = emptyList(),

    @SerialName("note")
    val notes: List<Esse3TeacherDiaryNotes> = emptyList()
)

@Serializable
data class Esse3TeacherDiaryDetail(
    /** id del diario docente */
    @SerialName("dettDiarioId")
    val diaryDetailId: Long = 0L,

    /** codice tipo attività */
    @SerialName("tipoAttCod")
    val activityTypeCode: String? = null,

    /** descrizione tipo attività */
    @SerialName("tipoAttDes")
    val activityTypeDescription: String? = null,

    /** data dell'attività rendicontata nel formato DD/MM/YYYY */
    @SerialName("data")
    val date: String? = null,

    /** ore rendicontate */
    @SerialName("ore")
    val hours: Int? = null,

    /** minuti rendicontati */
    @SerialName("minuti")
    val minutes: Int? = null
)

@Serializable
data class Esse3TeacherRegisterDetailGroup(
    /** descrizione del gruppo collegato con il dettaglio del registro */
    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3TeacherRegister(
    /** id del registro docente */
    @SerialName("regId")
    val registrationId: Long = 0L,

    /** id del registro docente */
    @SerialName("aaOffId")
    val academicYearOfferId: Int = 0,

    /** id del del docente a cui appartiene il registro */
    @SerialName("docenteId")
    val lecturerId: Long = 0L,

    /** cognome del docente a cui apprtiene il registro */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome del docente a cui apprtiene il registro */
    @SerialName("nome")
    val name: String? = null,

    /** cognome del docente a cui apprtiene il registro */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** matricola del docente a cui apprtiene il registro */
    @SerialName("matricola")
    val matricola: String? = null,

    /** codice del fattore di partizione collegato alla condivisione logista del registro */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** descrizione del fattore di partizione collegato alla condivisione logista del registro */
    @SerialName("fatPartDes")
    val invoicePartialDescription: String? = null,

    /** codice del dominio di partizione collegato alla condivisione logista del registro */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** descrizione del dominio di partizione collegato alla condivisione logista del registro */
    @SerialName("domPartDes")
    val domicilePartialDescription: String? = null,

    /** codice del semestre collegato alla condivisione logista del registro */
    @SerialName("partCod")
    val partialCode: String? = null,

    /** id del della condivisione logistica del registro */
    @SerialName("adLogId")
    val activityLogId: Long? = null,

    /** codice dello stato del registro */
    @SerialName("statoRegCod")
    val regulationStateCode: String? = null,

    /** descrizione dello stato del registro */
    @SerialName("statoRegDes")
    val regulationStateDescription: String? = null,

    /** tipo gestione registro */
    @SerialName("tipoGestRegCod")
    val regulationManagementTypeCode: String? = null,

    /** flag per la firma digitale del registro */
    @SerialName("firmaDigitaleFlg")
    val digitalSignatureFlag: Int? = null,

    /** data di stampa del registro (DD/MM/YYYY) */
    @SerialName("regadDataStampa")
    val teachingActivityRegistrationPrintDate: String? = null,

    /** flag che indica se la didattica risulta conclusa */
    @SerialName("regadFinitaDidFlg")
    val teachingActivityDidacticFinishedFlag: Int? = null,

    /** numero di studenti presenti alla prima lezione */
    @SerialName("numStuL1")
    val l1StudentNumber: Int? = null,

    /** numero di studenti presenti alla quarta lezione */
    @SerialName("numStuL4")
    val l4StudentNumber: Int? = null,

    /** numero medio di studenti frequentanti */
    @SerialName("numStuMedio")
    val averageStudentNumber: Int? = null,

    /** numero di ore effettivamente riconosciute al docente rispetto a quelle rendicontate */
    @SerialName("oreRiconosciute")
    val recognizedHours: Int? = null,

    /** numero di ore in eccedenza */
    @SerialName("eccedenza")
    val excess: Int? = null,

    /** flag che indica se il registro è stato pagato al docente */
    @SerialName("liquidatoFlg")
    val settledFlag: Int? = null,

    /** Annotazioni del docente proprietario sul suo registro */
    @SerialName("osservazioni")
    val observations: String? = null,

    /** data di ultima variazione di stato nel formato DD/MM/YYYY */
    @SerialName("dataUltimoTransStato")
    val lastStateTransitionDate: String? = null,

    @SerialName("identificativiCoperture")
    val coverageIdentifiers: List<Esse3TeacherRegisterCoverage> = emptyList(),

    /** totale di ore di didattica frontale rendicontate */
    @SerialName("totOreDid")
    val totalDidacticHours: Float? = null,

    /** totale di ore di altre attività rendicontate */
    @SerialName("totOreAltro")
    val totalOtherHours: Float? = null
)

@Serializable
data class Esse3TeacherDiaryDetailAcademicYear(
    /** id di rendicontazione del dettaglio attività annuale */
    @SerialName("dettAaDiarioId")
    val academicYearDiaryDetailId: Long = 0L,

    /** codice tipo attività */
    @SerialName("tipoAttCod")
    val activityTypeCode: String? = null,

    /** descrizione tipo attività */
    @SerialName("tipoAttDes")
    val activityTypeDescription: String? = null,

    /** ore rendicontate */
    @SerialName("ore")
    val hours: Int? = null,

    /** minuti rendicontati */
    @SerialName("minuti")
    val minutes: Int? = null,

    /** ore previste */
    @SerialName("orePrev")
    val predictedHours: Int? = null,

    /** minuti previsti */
    @SerialName("minutiPrev")
    val predictedMinutes: Int? = null
)

@Serializable
data class Esse3TeacherRegisterDetail(
    /** id del dettaglio del registro docente */
    @SerialName("dettRegId")
    val ruleDetailId: Long = 0L,

    /** codice tipo attività */
    @SerialName("tipoAttCod")
    val activityTypeCode: String? = null,

    /** codice tipo credito */
    @SerialName("tipoCreCod")
    val creditTypeCode: String? = null,

    /** descrizione tipo attività */
    @SerialName("tipoAttDes")
    val activityTypeDescription: String? = null,

    /** data dell'attività rendicontata nel formato DD/MM/YYYY */
    @SerialName("data")
    val date: String? = null,

    /** ora di inizio dell'attivita nel formato hh:mm */
    @SerialName("oraInizio")
    val startTime: String? = null,

    /** ora di fine dell'attivita nel formato hh:mm */
    @SerialName("oraFine")
    val endTime: String? = null,

    /** Durata in ore accademiche dell'attività inserita dal docente (non necessariamente coincide con la differenza tra ORA_FINE e ORA_INIZIO). */
    @SerialName("oreAccademiche")
    val academicHours: Float? = null,

    /** titolo dell'attività rendicontata */
    @SerialName("titolo")
    val title: String? = null,

    /** descrizione dell'attività rendicontata */
    @SerialName("des")
    val description: String? = null,

    /** nota del docente che ha compilato il registro */
    @SerialName("nota")
    val note: String? = null,

    /** lista dei nomi dei supplenti che hanno compilato l'attività */
    @SerialName("supplenti")
    val substitutes: String? = null,

    /** codice dell'unità didattica al quale si riferisce l'attività rendicontata */
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    /** descrizione dell'unità didattica al quale si riferisce l'attività rendicontata */
    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("gruppi")
    val groups: List<Esse3TeacherRegisterDetailGroup> = emptyList()
)

@Serializable
data class Esse3TeacherRegisterCoverage(
    /** id della copertura importata da U-GOV/GDA */
    @SerialName("coperId")
    val coverageId: Long? = null
)

@Serializable
data class Esse3TeacherRegisterLog(
    /** id del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** id del dell'attività didattica */
    @SerialName("adId")
    val activityId: Long? = null,

    /** id del del modulo dell'attività didattica */
    @SerialName("udId")
    val teachingUnitId: Long? = null,

    /** codice del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** codice dell'attività didattica */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell'attività didattica */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** codice del modulo dell'attività didattica */
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    /** descrizione del modulo dell'attività didattica */
    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    /** flag che indica se il l'elemento della condivisione logista è quello che eroga effettivamente la didattica */
    @SerialName("fisicaFlg")
    val physicalFlag: Int? = null,

    /** flag che indica se il modulo idicato è master per la condivisione logistica */
    @SerialName("masterFlg")
    val masterFlag: Int? = null
)

@Serializable
data class Esse3TeacherDiary(
    /** id del diario docente */
    @SerialName("diarioId")
    val diaryId: Long = 0L,

    /** id del diario docente */
    @SerialName("aaId")
    val academicYearId: Int = 0,

    /** id del del docente a cui appartiene il registro */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** cognome del docente a cui apprtiene il registro */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome del docente a cui apprtiene il registro */
    @SerialName("nome")
    val name: String? = null,

    /** cognome del docente a cui apprtiene il registro */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** codice dello stato del diario */
    @SerialName("statoDiarioCod")
    val diaryStateCode: String? = null,

    /** descrizione dello stato del diario */
    @SerialName("statoDiarioDes")
    val diaryStateDescription: String? = null,

    /** tipo gestione diario */
    @SerialName("tipoGestDiarioDocCod")
    val documentDiaryManagementTypeCode: String? = null,

    /** flag per la firma digitale del diario */
    @SerialName("firmaDigitaleFlg")
    val digitalSignatureFlag: Int? = null,

    /** annotazioni del docente sul proprio diario */
    @SerialName("osservazioni")
    val observations: String? = null,

    /** totale ore rendicontate del dettaglio */
    @SerialName("oreDett")
    val detailedHours: Float? = null,

    /** totale ore rendicontate del dettaglio annuale */
    @SerialName("oreDettAnnuali")
    val annualDetailedHours: Float? = null
)

@Serializable
data class Esse3TeacherRegisterWithDetails(
    /** id del registro docente */
    @SerialName("regId")
    val registrationId: Long = 0L,

    /** id del registro docente */
    @SerialName("aaOffId")
    val academicYearOfferId: Int = 0,

    /** id del del docente a cui appartiene il registro */
    @SerialName("docenteId")
    val lecturerId: Long = 0L,

    /** cognome del docente a cui apprtiene il registro */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome del docente a cui apprtiene il registro */
    @SerialName("nome")
    val name: String? = null,

    /** cognome del docente a cui apprtiene il registro */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** matricola del docente a cui apprtiene il registro */
    @SerialName("matricola")
    val matricola: String? = null,

    /** codice del fattore di partizione collegato alla condivisione logista del registro */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** descrizione del fattore di partizione collegato alla condivisione logista del registro */
    @SerialName("fatPartDes")
    val invoicePartialDescription: String? = null,

    /** codice del dominio di partizione collegato alla condivisione logista del registro */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** descrizione del dominio di partizione collegato alla condivisione logista del registro */
    @SerialName("domPartDes")
    val domicilePartialDescription: String? = null,

    /** codice del semestre collegato alla condivisione logista del registro */
    @SerialName("partCod")
    val partialCode: String? = null,

    /** id del della condivisione logistica del registro */
    @SerialName("adLogId")
    val activityLogId: Long? = null,

    /** codice dello stato del registro */
    @SerialName("statoRegCod")
    val regulationStateCode: String? = null,

    /** descrizione dello stato del registro */
    @SerialName("statoRegDes")
    val regulationStateDescription: String? = null,

    /** tipo gestione registro */
    @SerialName("tipoGestRegCod")
    val regulationManagementTypeCode: String? = null,

    /** flag per la firma digitale del registro */
    @SerialName("firmaDigitaleFlg")
    val digitalSignatureFlag: Int? = null,

    /** data di stampa del registro (DD/MM/YYYY) */
    @SerialName("regadDataStampa")
    val teachingActivityRegistrationPrintDate: String? = null,

    /** flag che indica se la didattica risulta conclusa */
    @SerialName("regadFinitaDidFlg")
    val teachingActivityDidacticFinishedFlag: Int? = null,

    /** numero di studenti presenti alla prima lezione */
    @SerialName("numStuL1")
    val l1StudentNumber: Int? = null,

    /** numero di studenti presenti alla quarta lezione */
    @SerialName("numStuL4")
    val l4StudentNumber: Int? = null,

    /** numero medio di studenti frequentanti */
    @SerialName("numStuMedio")
    val averageStudentNumber: Int? = null,

    /** numero di ore effettivamente riconosciute al docente rispetto a quelle rendicontate */
    @SerialName("oreRiconosciute")
    val recognizedHours: Int? = null,

    /** numero di ore in eccedenza */
    @SerialName("eccedenza")
    val excess: Int? = null,

    /** flag che indica se il registro è stato pagato al docente */
    @SerialName("liquidatoFlg")
    val settledFlag: Int? = null,

    /** Annotazioni del docente proprietario sul suo registro */
    @SerialName("osservazioni")
    val observations: String? = null,

    /** data di ultima variazione di stato nel formato DD/MM/YYYY */
    @SerialName("dataUltimoTransStato")
    val lastStateTransitionDate: String? = null,

    @SerialName("identificativiCoperture")
    val coverageIdentifiers: List<Esse3TeacherRegisterCoverage> = emptyList(),

    /** totale di ore di didattica frontale rendicontate */
    @SerialName("totOreDid")
    val totalDidacticHours: Float? = null,

    /** totale di ore di altre attività rendicontate */
    @SerialName("totOreAltro")
    val totalOtherHours: Float? = null,

    @SerialName("logistica")
    val logistics: List<Esse3TeacherRegisterLog> = emptyList(),

    @SerialName("attivita")
    val activity: List<Esse3TeacherRegisterDetail> = emptyList()
)

@Serializable
data class Esse3TeacherDiaryNotes(
    /** id della nota del diario */
    @SerialName("notaDiarioId")
    val diaryNoteId: Long = 0L,

    /** nota del docente sul diario */
    @SerialName("nota")
    val note: String? = null
)
