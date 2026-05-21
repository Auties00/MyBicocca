package it.attendance100.mybicocca.data.remote.esse3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3ContextualizedActivity(
    @SerialName("chiaveAdContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey,

    /** descrizione dell''attività didattica in inglese */
    @SerialName("adDesEng")
    val activityEnglishDescription: String? = null,

    /** descrizione del corso di erogazione dell'attività didattica in inglese */
    @SerialName("cdsDesEng")
    val courseOfStudyDescriptionEnglish: String? = null,

    /** descrizione dell''ordinamento di erogazione dell'attività didattica in inglese */
    @SerialName("aaOrdDesEng")
    val academicYearOrderEnglishDescription: String? = null,

    /** descrizione del percorso di erogazione dell'attività didattica */
    @SerialName("pdsDesEng")
    val studyPlanDescriptionEnglish: String? = null,

    /** lingua di insegnamento dell'attivita didattica usata per ECTS */
    @SerialName("linguaInsDes")
    val teachingLanguageDescription: String? = null,

    /** lingua di insegnamento dell'attivita didattica in inglese */
    @SerialName("linguaInsDesEng")
    val teachingLanguageDescriptionEnglish: String? = null,

    /** attività non erogabile. Se = 1 indica che per questa attività NON vanno tenute in considerazione le partizioni (classi) della p09_ad_log nei processi della carriera ovvero nel libretto studente non viene mai assegna la partizione (classe) per questa attivita */
    @SerialName("nonErogabileOdFlg")
    val nonDeliverableOdFlag: Int? = null,

    /** codice del Tipo di esame */
    @SerialName("tipoEsaCod")
    val graduationTypeCode: String? = null,

    /** descrizione del Tipo di esame */
    @SerialName("tipoEsaDes")
    val graduationTypeDescription: String? = null,

    /** descrizione del Tipo di esame in inglese */
    @SerialName("tipoEsaDesEng")
    val graduationTypeDescriptionEnglish: String? = null,

    /** codice del Tipo di valutazione */
    @SerialName("tipoValCod")
    val evaluationTypeCode: String? = null,

    /** descrizione del Tipo di valutazione */
    @SerialName("tipoValDes")
    val evaluationTypeDescription: String? = null,

    /** descrizione del Tipo di valutazione in inglese */
    @SerialName("tipoValDesEng")
    val evaluationTypeDescriptionEnglish: String? = null,

    /** codice del Tipo di insegnamento. Valido solo per i CDS ante riforma */
    @SerialName("tipoInsCod")
    val insertionTypeCode: String? = null,

    /** descrizione del Tipo di insegnamento. Valido solo per i CDS ante riforma */
    @SerialName("tipoInsDes")
    val insertionTypeDescription: String? = null,

    /** laddove il tipo valutazione è giudizio, ovvero TIPO_VAL_COD = G, indica il gruppo di giudizio utilizzato */
    @SerialName("gruppoGiudCod")
    val judgmentGroupCode: String? = null,

    /** descrizione del gruppo di giudizio */
    @SerialName("gruppoGiudDes")
    val judgmentGroupDescription: String? = null,

    /** indica se un attività può essere ripetuta più di una volta all''interno della carriera dello studente (es. i corsi di lettere). Contiene il numero massimo di possibili ripetizioni. */
    @SerialName("reiterabile")
    val repeatable: Int? = null,

    /** URL del sito web della struttura */
    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    /** URL del corso MOODLE collegato all'attivitÃ  didattica */
    @SerialName("urlCorsoMoodle")
    val moodleCourseUrl: String? = null,

    @SerialName("adCapogruppo")
    val activityGroupLeader: Esse3ActivityGroupLeader? = null,

    /** indica se si tratta di una AD capogruppo. */
    @SerialName("capoGruppoFlg")
    val groupLeaderFlag: Int? = null,

    /** indica se la AD corrente è visibile su web */
    @SerialName("adWebViewFlg")
    val activityWebViewFlag: Int? = null
)

@Serializable
data class Esse3TeachersPerTeachingUnit(
    @SerialName("chiaveUdContestualizzata")
    val contextualizedTeachingUnitKey: Esse3ContextualizedTeachingUnitKey,

    /** codice del tipo di copertura del docente */
    @SerialName("tipoCoperturaCod")
    val coverageTypeCode: String? = null,

    /** descrizione del tipo di copertura del docente */
    @SerialName("tipoCoperturaDes")
    val coverageTypeDescription: String? = null,

    /** Flag che indica se il docente non deve essere riportato per la SUA */
    @SerialName("noTraspFlg")
    val noTransportFlag: Int? = null,

    /** codice del fattore di partizione */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** descrizione del fattore di partizione */
    @SerialName("fatPartDes")
    val invoicePartialDescription: String? = null,

    /** codice del dominio di partizione */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** desrizione del dominio di partizione */
    @SerialName("domPartDes")
    val domicilePartialDescription: String? = null,

    /** chiave del docente che fa lezione */
    @SerialName("docenteId")
    val lecturerId: Long = 0L,

    /** matricola del docente che fa lezione */
    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    /** nome del docente che fa lezione */
    @SerialName("docenteNome")
    val lecturerName: String? = null,

    /** cognome del docente che fa lezione */
    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    /** Flag che indica se il docente fa lezione per la UD corrente */
    @SerialName("lezioneFlg")
    val lessonFlag: Int? = null,

    /** Flag che indica se il docente � titolare per la AD corrente */
    @SerialName("titolareFlg")
    val holderFlag: Int? = null,

    /** Flag che indica se il docente � responsabile didattico per la UD corrente */
    @SerialName("respDidFlg")
    val didacticResponsibleFlag: Int? = null
)

@Serializable
data class Esse3Teachers(
    /** Data inizio ruolo docente attuale. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataIniRuolo")
    val roleStartDate: String? = null,

    /** appellativo del docente */
    @SerialName("docenteAppellativo")
    val lecturerTitle: String? = null,

    /** Profilo docente */
    @SerialName("profilo")
    val profile: String? = null,

    /** Descrizione del ruolo del docente */
    @SerialName("ruoloDocDes")
    val lecturerRoleDescription: String? = null,

    /** Descrizione del dipartimento */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** Codice del dipartimento */
    @SerialName("dipCod")
    val departmentCode: String? = null,

    /** Identificativo del dipartimento */
    @SerialName("dipId")
    val departmentId: Long? = null,

    /** Descrizione del settore */
    @SerialName("settDes")
    val sectorDescription: String? = null,

    /** Data di inserimento del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Data di modifica del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Data di modifica del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataModDoc")
    val documentModificationDate: String? = null,

    /** ID_AB del docente */
    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    /** note docente */
    @SerialName("noteDocente")
    val lecturerNotes: String? = null,

    /** note curriculum */
    @SerialName("noteCurriculum")
    val curriculumNotes: String? = null,

    /** note biografiche */
    @SerialName("noteBiografiche")
    val biographicalNotes: String? = null,

    /** note pubblicazioni */
    @SerialName("notePubblicazioni")
    val publicationsNotes: String? = null,

    /** descrizione provincia di nascita */
    @SerialName("p01ProvDes")
    val p01ProvinceDescription: String? = null,

    /** sigla provincia di nascita */
    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    /** città straniera di nascita */
    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    /** descrizione del comune di nascita */
    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    /** codice  istat miur del comune di nascita */
    @SerialName("p01ComuCodIstatMiur")
    val p01MunicipalityMiurIstatCode: String? = null,

    /** codice  del comune di nascita */
    @SerialName("p01ComuComuneCod")
    val p01MunicipalityCommonCode: String? = null,

    /** codice  istat del comune di nascita */
    @SerialName("p01ComuCodIstat")
    val p01MunicipalityIstatCode: String? = null,

    /** ID del comune di nascita */
    @SerialName("p01ComuComuneId")
    val p01MunicipalityCommonId: Long? = null,

    /** codice  della nazione di nascita */
    @SerialName("p01NaziCod")
    val p01NationCode: String? = null,

    /** codice  iso della nazione di nascita */
    @SerialName("p01NaziNazioneCod")
    val p01NationNationCode: String? = null,

    /** descrizione della nazione di nascita */
    @SerialName("p01NaziDes")
    val p01NationDescription: String? = null,

    /** codice fiscale della nazione di nascita */
    @SerialName("p01NaziCodFisc")
    val p01NationFiscalCode: String? = null,

    /** Data di nascita del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** sesso del docente */
    @SerialName("sesso")
    val gender: String? = null,

    /** Data fine del attività del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataFinAtt")
    val activityEndDate: String? = null,

    /** Data inizio del attività del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataIniAtt")
    val activityStartDate: String? = null,

    /** hyperlink del docente */
    @SerialName("hyperlink")
    val hyperlink: String? = null,

    /** cellulare del docente */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** codice fiscale del docente */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** ruolo del docente */
    @SerialName("ruoloDocCod")
    val lecturerRoleCode: String? = null,

    /** descrizione della struttura di appartenenza del docente */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** codice della struttura di appartenenza del docente */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** ID della struttura di appartenenza del docente */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** email di riferimento per i docenti L.A. */
    @SerialName("emailDocenteLa")
    val lecturerLaEmail: String? = null,

    /** Indirizzo e-mail assegnato dall'ateneo. */
    @SerialName("emailAte")
    val universityEmail: String? = null,

    /** email del docente */
    @SerialName("eMail")
    val email: String? = null,

    /** numero badge */
    @SerialName("badge")
    val badge: String? = null,

    /** settore del docente */
    @SerialName("settCod")
    val sectorCode: String? = null,

    /** userId attivo collegato al docente */
    @SerialName("userId")
    val userId: String? = null,

    /** nome del docente */
    @SerialName("docenteNome")
    val lecturerName: String? = null,

    /** cognome del docente */
    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    /** matricola del docente */
    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    /** chiave del docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null
)

@Serializable
data class Esse3ActivityParentGroup(
    /** chiave dell'attività didattica raggruppata */
    @SerialName("adragoffId")
    val activityRaggruppamentoOfferId: Long? = null,

    /** chiave del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di erogazione dell'attività didattica */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** descrizione del corso di erogazione dell'attività didattica in inglese */
    @SerialName("cdsDesEng")
    val courseOfStudyDescriptionEnglish: String? = null,

    /** chiave dell'attività didattica */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice dell''attività didattica */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell''attività didattica */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** descrizione dell''attività didattica in inglese */
    @SerialName("adDesEng")
    val activityEnglishDescription: String? = null,

    /** codice del tipo di raggruppamento */
    @SerialName("tipoRagCod")
    val groupingTypeCode: String? = null,

    /** descrizione del tipo di raggruppamento */
    @SerialName("tipoRagDes")
    val groupingTypeDescription: String? = null,

    /** descrizione del tipo di raggruppamento in inglese */
    @SerialName("tipoRagDesEng")
    val groupingTypeDescriptionEnglish: String? = null,

    /** anno di coorte */
    @SerialName("annoCoorte")
    val cohortYear: Int? = null,

    @SerialName("adFiglie")
    val childActivities: List<Esse3ActivityChildGroups> = emptyList()
)

@Serializable
data class Esse3OfferTeachingUnitDeletable(
    /** ID Anno Accademico di offerta */
    @SerialName("aaOffId")
    val academicYearOfferId: Long = 0L,

    /** codice del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsCod")
    val courseOfStudyCode: String = "",

    /** anno di ordinamento del corso di studio di erogazione dell'attività didattica */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int = 0,

    /** codice del percorso di erogazione dell'attività didattica */
    @SerialName("pdsCod")
    val studyPlanCode: String = "",

    /** codice dell'attività didattica generica */
    @SerialName("adCod")
    val activityCode: String = "",

    /** codice dell'unità didattica offerta */
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    /** Intero che indica l'attività didattica offerta è cancellabile */
    @SerialName("cancellabile")
    val deletable: Int? = null
)

@Serializable
data class Esse3OfferWithDetails(
    /** anno di offerta di erogazione dell'attività didattica */
    @SerialName("aaOffId")
    val academicYearOfferId: Int = 0,

    /** chiave del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsOffId")
    val courseOfStudyOfferId: Long = 0L,

    /** codice del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di erogazione dell'attività didattica */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** stato dell'offerta didattica */
    @SerialName("statoAttCod")
    val activityStateCode: Esse3QuestionStateCode? = null,

    /** codice del tipo di corso di studio di erogazione dell'attività didattica */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** codice del tipo di corso di studio di erogazione dell'attività didattica */
    @SerialName("tipiCorsoCod")
    val courseTypesCode: String? = null,

    /** descrizione del tipo di corso di studio */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** descrizione del tipo di corso di studio in inglese */
    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    /** Data di ultima modifica dell'intera offerta didattica. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataModOd")
    val odModificationDate: String? = null,

    /** codice del dipartimento di afferenza amministrativa del corso di studio */
    @SerialName("dipCod")
    val departmentCode: String? = null,

    /** descrizione del dipartimento di afferenza amministrativa del corso di studio */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** Flag che indica se esiste l'offerta */
    @SerialName("offertaExistsFlg")
    val offerExistsFlag: Int? = null,

    /** Flag che indica se esiste la logistica */
    @SerialName("logisticaExistsFlg")
    val logisticsExistsFlag: Int? = null,

    @SerialName("ADContestConDettagli")
    val teachingActivityContestWithDetails: List<Esse3ActivityContestWithDetails> = emptyList()
)

@Serializable
data class Esse3TeachingUnitContestWithDetails(
    @SerialName("chiaveUdContestualizzata")
    val contextualizedTeachingUnitKey: Esse3ContextualizedTeachingUnitKey,

    /** descrizione del'unità (modulo) dell'attività didattica in inglese */
    @SerialName("udDesEng")
    val teachingUnitDescriptionEnglish: String? = null,

    /** tipo unità didattica, ad esempio modulo, corso, seminario. Obbligatorio se, nelle regole di scelta, la AD prevede la selezione di moduli di un particolare tipo */
    @SerialName("tipoUdCod")
    val teachingUnitTypeCode: String? = null,

    @SerialName("SEGContestualizzato")
    val contextualizedSegment: List<Esse3ContextualizedSegment> = emptyList(),

    @SerialName("DocentiPerUD")
    val teachersPerTeachingUnit: List<Esse3TeachersPerTeachingUnit> = emptyList()
)

@Serializable
data class Esse3Offer(
    /** anno di offerta di erogazione dell'attività didattica */
    @SerialName("aaOffId")
    val academicYearOfferId: Int = 0,

    /** chiave del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsOffId")
    val courseOfStudyOfferId: Long = 0L,

    /** codice del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di erogazione dell'attività didattica */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** stato dell'offerta didattica */
    @SerialName("statoAttCod")
    val activityStateCode: Esse3QuestionStateCode? = null,

    /** codice del tipo di corso di studio di erogazione dell'attività didattica */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** codice del tipo di corso di studio di erogazione dell'attività didattica */
    @SerialName("tipiCorsoCod")
    val courseTypesCode: String? = null,

    /** descrizione del tipo di corso di studio */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** descrizione del tipo di corso di studio in inglese */
    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    /** Data di ultima modifica dell'intera offerta didattica. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataModOd")
    val odModificationDate: String? = null,

    /** codice del dipartimento di afferenza amministrativa del corso di studio */
    @SerialName("dipCod")
    val departmentCode: String? = null,

    /** descrizione del dipartimento di afferenza amministrativa del corso di studio */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** Flag che indica se esiste l'offerta */
    @SerialName("offertaExistsFlg")
    val offerExistsFlag: Int? = null,

    /** Flag che indica se esiste la logistica */
    @SerialName("logisticaExistsFlg")
    val logisticsExistsFlag: Int? = null
)

@Serializable
data class Esse3ContextualizedSegmentKey(
    @SerialName("chiaveUdContestualizzata")
    val contextualizedTeachingUnitKey: Esse3ContextualizedTeachingUnitKey,

    /** chiave del segmento dell'attività didattica */
    @SerialName("segId")
    val segmentId: Long = 0L
)

@Serializable
data class Esse3TeachersWithDetails(
    @SerialName("DocentiOrario")
    val teachersSchedule: List<Esse3TeachersTimetable> = emptyList(),

    /** Data inizio ruolo docente attuale. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataIniRuolo")
    val roleStartDate: String? = null,

    /** appellativo del docente */
    @SerialName("docenteAppellativo")
    val lecturerTitle: String? = null,

    /** Profilo docente */
    @SerialName("profilo")
    val profile: String? = null,

    /** Descrizione del ruolo del docente */
    @SerialName("ruoloDocDes")
    val lecturerRoleDescription: String? = null,

    /** Descrizione del dipartimento */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** Codice del dipartimento */
    @SerialName("dipCod")
    val departmentCode: String? = null,

    /** Identificativo del dipartimento */
    @SerialName("dipId")
    val departmentId: Long? = null,

    /** Descrizione del settore */
    @SerialName("settDes")
    val sectorDescription: String? = null,

    /** Data di inserimento del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Data di modifica del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Data di modifica del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataModDoc")
    val documentModificationDate: String? = null,

    /** ID_AB del docente */
    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    /** note docente */
    @SerialName("noteDocente")
    val lecturerNotes: String? = null,

    /** note curriculum */
    @SerialName("noteCurriculum")
    val curriculumNotes: String? = null,

    /** note biografiche */
    @SerialName("noteBiografiche")
    val biographicalNotes: String? = null,

    /** note pubblicazioni */
    @SerialName("notePubblicazioni")
    val publicationsNotes: String? = null,

    /** descrizione provincia di nascita */
    @SerialName("p01ProvDes")
    val p01ProvinceDescription: String? = null,

    /** sigla provincia di nascita */
    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    /** città straniera di nascita */
    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    /** descrizione del comune di nascita */
    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    /** codice  istat miur del comune di nascita */
    @SerialName("p01ComuCodIstatMiur")
    val p01MunicipalityMiurIstatCode: String? = null,

    /** codice  del comune di nascita */
    @SerialName("p01ComuComuneCod")
    val p01MunicipalityCommonCode: String? = null,

    /** codice  istat del comune di nascita */
    @SerialName("p01ComuCodIstat")
    val p01MunicipalityIstatCode: String? = null,

    /** ID del comune di nascita */
    @SerialName("p01ComuComuneId")
    val p01MunicipalityCommonId: Long? = null,

    /** codice  della nazione di nascita */
    @SerialName("p01NaziCod")
    val p01NationCode: String? = null,

    /** codice  iso della nazione di nascita */
    @SerialName("p01NaziNazioneCod")
    val p01NationNationCode: String? = null,

    /** descrizione della nazione di nascita */
    @SerialName("p01NaziDes")
    val p01NationDescription: String? = null,

    /** codice fiscale della nazione di nascita */
    @SerialName("p01NaziCodFisc")
    val p01NationFiscalCode: String? = null,

    /** Data di nascita del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** sesso del docente */
    @SerialName("sesso")
    val gender: String? = null,

    /** Data fine del attività del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataFinAtt")
    val activityEndDate: String? = null,

    /** Data inizio del attività del docente. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataIniAtt")
    val activityStartDate: String? = null,

    /** hyperlink del docente */
    @SerialName("hyperlink")
    val hyperlink: String? = null,

    /** cellulare del docente */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** codice fiscale del docente */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** ruolo del docente */
    @SerialName("ruoloDocCod")
    val lecturerRoleCode: String? = null,

    /** descrizione della struttura di appartenenza del docente */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** codice della struttura di appartenenza del docente */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** ID della struttura di appartenenza del docente */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** email di riferimento per i docenti L.A. */
    @SerialName("emailDocenteLa")
    val lecturerLaEmail: String? = null,

    /** Indirizzo e-mail assegnato dall'ateneo. */
    @SerialName("emailAte")
    val universityEmail: String? = null,

    /** email del docente */
    @SerialName("eMail")
    val email: String? = null,

    /** numero badge */
    @SerialName("badge")
    val badge: String? = null,

    /** settore del docente */
    @SerialName("settCod")
    val sectorCode: String? = null,

    /** userId attivo collegato al docente */
    @SerialName("userId")
    val userId: String? = null,

    /** nome del docente */
    @SerialName("docenteNome")
    val lecturerName: String? = null,

    /** cognome del docente */
    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    /** matricola del docente */
    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    /** chiave del docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null
)

@Serializable
data class Esse3DeletedOffer(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** ID Anno Accademico di offerta */
    @SerialName("aaId")
    val academicYearId: Long = 0L,

    /** Data di ultima modifica dell'offerta e dettagli. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataModOd")
    val odModificationDate: String? = null
)

@Serializable
data class Esse3ActivityDeletable(
    /** codice che indica se la attività didattica è cancellabile */
    @SerialName("retCode")
    val returnCode: Int? = null,

    /** eventuale messaggio con informazioni aggiuntive */
    @SerialName("msg")
    val message: String? = null
)

@Serializable
data class Esse3OfferActivityDeletable(
    /** ID Anno Accademico di offerta */
    @SerialName("aaOffId")
    val academicYearOfferId: Long = 0L,

    /** codice del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsCod")
    val courseOfStudyCode: String = "",

    /** anno di ordinamento del corso di studio di erogazione dell'attività didattica */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int = 0,

    /** codice del percorso di erogazione dell'attività didattica */
    @SerialName("pdsCod")
    val studyPlanCode: String = "",

    /** codice dell'attività didattica generica */
    @SerialName("adCod")
    val activityCode: String = "",

    /** Intero che indica l'attività didattica offerta è cancellabile */
    @SerialName("cancellabile")
    val deletable: Int? = null
)

@Serializable
data class Esse3ActivityChildGroups(
    /** chiave dell'attività didattica raggruppata */
    @SerialName("adragoffId")
    val activityRaggruppamentoOfferId: Long? = null,

    /** chiave della attività didattica raggruppata figlia */
    @SerialName("adfiglioProgId")
    val childActivityProgressId: Long? = null,

    /** chiave del corso di studio dell'attività didattica figlia */
    @SerialName("cdsFiglioId")
    val childCourseOfStudyId: Long? = null,

    /** codice del corso di studio dell'attività didattica figlia */
    @SerialName("cdsFiglioCod")
    val childCourseOfStudyCode: String? = null,

    /** descrizione del corso dell'attività didattica figlia */
    @SerialName("cdsFiglioDes")
    val childCourseOfStudyDescription: String? = null,

    /** descrizione del corso in inglese dell'attività didattica figlia */
    @SerialName("cdsFiglioDesEng")
    val childCourseOfStudyDescriptionEnglish: String? = null,

    /** chiave dell'attività didattica figlia */
    @SerialName("adFiglioId")
    val childActivityId: Long? = null,

    /** codice dell'attività didattica figlia */
    @SerialName("adFiglioCod")
    val childActivityCode: String? = null,

    /** descrizione dell'attività didattica figlia */
    @SerialName("adFiglioDes")
    val childActivityDescription: String? = null,

    /** descrizione in inglese dell'attività didattica figlia */
    @SerialName("adFiglioDesEng")
    val childActivityEnglishDescription: String? = null
)

@Serializable
data class Esse3ActivitiesCountPlans(
    /** codice del corso dell'attività didattica */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** anno di ordinamento del corso di studio di erogazione dell'attività didattica */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    /** codice del percorso di erogazione dell'attività didattica */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** anno di offerta dell'attività */
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    /** codice dell'attività didattica */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** conteggio piani collegati all'attività */
    @SerialName("conteggioPiani")
    val planCount: Int? = null
)

@Serializable
data class Esse3ContextualizedSegment(
    @SerialName("chiaveSegContestualizzato")
    val contextualizedSecretKey: Esse3ContextualizedSegmentKey,

    /** codice identificativo del settore scientifico disciplinare di riferimento */
    @SerialName("settCod")
    val sectorCode: String? = null,

    /** descrizione del settore scientifico disciplinare di riferimento */
    @SerialName("settDes")
    val sectorDescription: String? = null,

    /** codice identificativo della disciplina (all''interno del settore scientifico disciplinare) del segmento corrente */
    @SerialName("discCod")
    val disciplineCode: String? = null,

    /** codice identificativo del tipo di credito del segmento corrente */
    @SerialName("tipoCreCod")
    val creditTypeCode: String? = null,

    /** descrizione identificativo del tipo di credito del segmento corrente */
    @SerialName("tipoCreDes")
    val creditTypeDescription: String? = null,

    /** descrizione identificativo del tipo di credito in inglese */
    @SerialName("tipoCreDesEng")
    val creditTypeDescriptionEnglish: String? = null,

    /** ore di attività frontale del segmento */
    @SerialName("durUniVal")
    val universityValidityDuration: Float? = null,

    /** ore di studio individuale del segmento. */
    @SerialName("durStuInd")
    val individualStudyDuration: Float? = null,

    /** Note libere. */
    @SerialName("nota")
    val note: String? = null,

    /** Tipo Attività Formativa */
    @SerialName("tipoAfCod")
    val teachingActivityTypeCode: Esse3TafMode? = null,

    /** descrizione del Tipo Attività Formativa */
    @SerialName("tipoAfDes")
    val teachingActivityTypeDescription: String? = null,

    /** descrizione del Tipo Attività Formativa in inglese */
    @SerialName("tipoAfDesEng")
    val teachingActivityTypeDescriptionEnglish: String? = null,

    /** indica se è richiesto l''obbligo di frequenza */
    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int? = null,

    /** valore minimo di ore di frequenza richieste, e viene riempito se la frequenza è obbligatoria (FREQ_OBBLIG_FLG settato a SI) */
    @SerialName("oreMinFreq")
    val minimumAttendanceHours: Float? = null,

    /** numero di crediti associato al segmento */
    @SerialName("peso")
    val weight: Float? = null,

    /** ID dell'ambito disciplinare relativo al segmento */
    @SerialName("ambId")
    val environmentId: Long? = null,

    /** descrizione dell'ambito disciplinare */
    @SerialName("ambDes")
    val environmentDescription: String? = null,

    /** descrizione dell'ambito disciplinare in inglese */
    @SerialName("ambDesEng")
    val environmentDescriptionEnglish: String? = null,

    /** Tipo attività formativa da attribuire all'attività in fase di attuazione dei piani carriera alla prima reiterazione */
    @SerialName("tipoAfReitCod")
    val repeatTeachingActivityTypeCode: Esse3TafMode? = null,

    /** descrizione del Tipo Attività Formativa alla prima reiterazione */
    @SerialName("tipoAfReitDes")
    val repeatTeachingActivityTypeDescription: String? = null,

    /** descrizione del Tipo Attività Formativa in inglese alla prima reiterazione */
    @SerialName("tipoAfReitDesEng")
    val repeatTeachingActivityTypeDescriptionEnglish: String? = null,

    /** Anno coorte (regole) inizio validità del segmento; come default utilizzare AA_ORD_ID */
    @SerialName("aaRegIni")
    val academicYearInitialRegulation: Int? = null,

    /** Anno coorte (regole) fine validità del segmento; come default utilizzare 9999 */
    @SerialName("aaRegFin")
    val academicYearFinalRegulation: Int? = null,

    /** Tipo attività formativa (TAF) valorizzato solo in caso di cds con interclasse (DM 270) */
    @SerialName("interclaTipoAfCod")
    val interclassTeachingActivityTypeCode: Esse3TafMode? = null,

    /** descrizione del Tipo Attività Formativa in caso di cds con interclasse */
    @SerialName("interclaTipoAfDes")
    val interclassTeachingActivityTypeDescription: String? = null,

    /** descrizione del Tipo Attività Formativa in inglese alla prima reiterazione */
    @SerialName("interclaTipoAfDesEng")
    val interclassTeachingActivityTypeDescriptionEnglish: String? = null,

    /** ID dell'ambito disciplinare valorizzato solo in caso di cds con interclasse (DM 270) */
    @SerialName("interclaAmbId")
    val interclassScopeId: Long? = null,

    /** descrizione dell'ambito disciplinare valorizzato solo in caso di cds con interclasse (DM 270) */
    @SerialName("interclaAmbDes")
    val interclassScopeDescription: String? = null,

    /** descrizione dell'ambito disciplinare in inglese valorizzato solo in caso di cds con interclasse (DM 270) */
    @SerialName("interclaAmbDesEng")
    val interclassScopeDescriptionEnglish: String? = null,

    /** indica se un'attività didattica è libera */
    @SerialName("liberaOdFlg")
    val freeOdFlag: Int? = null
)

@Serializable
data class Esse3FullPartitions(
    @SerialName("DominioDiPartizione")
    val partitionDomain: List<Esse3PartitionDomain> = emptyList(),

    /** codice del fattore di partizione */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** descrizione del fattore di partizione */
    @SerialName("fatPartDes")
    val invoicePartialDescription: String? = null,

    /** descrizione del fattore di partizione in inglese */
    @SerialName("fatPartDesEng")
    val invoicePartialDescriptionEnglish: String? = null,

    /** tipo del fattore di partizione */
    @SerialName("tipoFatt")
    val invoiceType: String? = null
)

@Serializable
data class Esse3TeachingLanguages(
    /** chiave della lingua */
    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    /** codice della lingua */
    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    /** descrizione della lingua */
    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    /** descrizione della lingua in inglese */
    @SerialName("linguaDidDesEng")
    val teachingLanguageDescriptionEnglish: String? = null
)

@Serializable
data class Esse3ActivityContestWithDetails(
    @SerialName("chiaveAdContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey,

    /** descrizione dell''attività didattica in inglese */
    @SerialName("adDesEng")
    val activityEnglishDescription: String? = null,

    /** descrizione del corso di erogazione dell'attività didattica in inglese */
    @SerialName("cdsDesEng")
    val courseOfStudyDescriptionEnglish: String? = null,

    /** descrizione dell''ordinamento di erogazione dell'attività didattica in inglese */
    @SerialName("aaOrdDesEng")
    val academicYearOrderEnglishDescription: String? = null,

    /** descrizione del percorso di erogazione dell'attività didattica */
    @SerialName("pdsDesEng")
    val studyPlanDescriptionEnglish: String? = null,

    /** lingua di insegnamento dell'attivita didattica usata per ECTS */
    @SerialName("linguaInsDes")
    val teachingLanguageDescription: String? = null,

    /** lingua di insegnamento dell'attivita didattica in inglese */
    @SerialName("linguaInsDesEng")
    val teachingLanguageDescriptionEnglish: String? = null,

    /** attività non erogabile. Se = 1 indica che per questa attività NON vanno tenute in considerazione le partizioni (classi) della p09_ad_log nei processi della carriera ovvero nel libretto studente non viene mai assegna la partizione (classe) per questa attivita */
    @SerialName("nonErogabileOdFlg")
    val nonDeliverableOdFlag: Int? = null,

    /** codice del Tipo di esame */
    @SerialName("tipoEsaCod")
    val graduationTypeCode: String? = null,

    /** descrizione del Tipo di esame */
    @SerialName("tipoEsaDes")
    val graduationTypeDescription: String? = null,

    /** descrizione del Tipo di esame in inglese */
    @SerialName("tipoEsaDesEng")
    val graduationTypeDescriptionEnglish: String? = null,

    /** codice del Tipo di valutazione */
    @SerialName("tipoValCod")
    val evaluationTypeCode: String? = null,

    /** descrizione del Tipo di valutazione */
    @SerialName("tipoValDes")
    val evaluationTypeDescription: String? = null,

    /** descrizione del Tipo di valutazione in inglese */
    @SerialName("tipoValDesEng")
    val evaluationTypeDescriptionEnglish: String? = null,

    /** codice del Tipo di insegnamento. Valido solo per i CDS ante riforma */
    @SerialName("tipoInsCod")
    val insertionTypeCode: String? = null,

    /** descrizione del Tipo di insegnamento. Valido solo per i CDS ante riforma */
    @SerialName("tipoInsDes")
    val insertionTypeDescription: String? = null,

    /** laddove il tipo valutazione è giudizio, ovvero TIPO_VAL_COD = G, indica il gruppo di giudizio utilizzato */
    @SerialName("gruppoGiudCod")
    val judgmentGroupCode: String? = null,

    /** descrizione del gruppo di giudizio */
    @SerialName("gruppoGiudDes")
    val judgmentGroupDescription: String? = null,

    /** indica se un attività può essere ripetuta più di una volta all''interno della carriera dello studente (es. i corsi di lettere). Contiene il numero massimo di possibili ripetizioni. */
    @SerialName("reiterabile")
    val repeatable: Int? = null,

    /** URL del sito web della struttura */
    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    /** URL del corso MOODLE collegato all'attivitÃ  didattica */
    @SerialName("urlCorsoMoodle")
    val moodleCourseUrl: String? = null,

    @SerialName("adCapogruppo")
    val activityGroupLeader: Esse3ActivityGroupLeader? = null,

    /** indica se si tratta di una AD capogruppo. */
    @SerialName("capoGruppoFlg")
    val groupLeaderFlag: Int? = null,

    /** indica se la AD corrente è visibile su web */
    @SerialName("adWebViewFlg")
    val activityWebViewFlag: Int? = null,

    @SerialName("UDContestConDettagli")
    val teachingUnitContestWithDetails: List<Esse3TeachingUnitContestWithDetails> = emptyList(),

    @SerialName("LinguaDidattiche")
    val teachingLanguages: List<Esse3TeachingLanguages> = emptyList()
)

@Serializable
data class Esse3PartitionDomain(
    /** codice del fattore di partizione */
    @SerialName("fatPartCod")
    val invoicePartialCode: String = "",

    /** codice del dominio di partizione */
    @SerialName("domPartCod")
    val domicilePartialCode: String = "",

    /** desrizione del dominio di partizione */
    @SerialName("domPartDes")
    val domicilePartialDescription: String? = null,

    /** desrizione del dominio di partizione in inglese */
    @SerialName("domPartDesEng")
    val domicilePartialDescriptionEnglish: String? = null
)

@Serializable
data class Esse3GenericActivity(
    /** chiave dell'attività didattica generica */
    @SerialName("adId")
    val activityId: Long = 0L,

    /** codice dell'attività didattica generica */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell'attività didattica generica */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** Flag che indica se esiste l'offerta per l'attività didattica generica */
    @SerialName("offertaExistsFlg")
    val offerExistsFlag: Int? = null
)

@Serializable
data class Esse3UpdateContextualizedActivity(
    /** URL del corso MOODLE collegato all'attivitÃ  didattica */
    @SerialName("urlCorsoMoodle")
    val moodleCourseUrl: String? = null
)

@Serializable
data class Esse3ActivitiesCountPlansFilters(
    /** codice del corso dell'attività didattica */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** anno di ordinamento del corso di studio di erogazione dell'attività didattica */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    /** codice del percorso di erogazione dell'attività didattica */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** anno di offerta dell'attività */
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    /** codice dell'attività didattica */
    @SerialName("adCod")
    val activityCode: String? = null
)

@Serializable
data class Esse3ContextualizedTeachingUnit(
    @SerialName("chiaveUdContestualizzata")
    val contextualizedTeachingUnitKey: Esse3ContextualizedTeachingUnitKey,

    /** descrizione del'unità (modulo) dell'attività didattica in inglese */
    @SerialName("udDesEng")
    val teachingUnitDescriptionEnglish: String? = null,

    /** tipo unità didattica, ad esempio modulo, corso, seminario. Obbligatorio se, nelle regole di scelta, la AD prevede la selezione di moduli di un particolare tipo */
    @SerialName("tipoUdCod")
    val teachingUnitTypeCode: String? = null
)

@Serializable
data class Esse3ActivityGroupLeader(
    /** chiave del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di erogazione dell'attività didattica */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** descrizione del corso di erogazione dell'attività didattica in inglese */
    @SerialName("cdsDesEng")
    val courseOfStudyDescriptionEnglish: String? = null,

    /** chiave dell'attività didattica */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice dell''attività didattica */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell''attività didattica */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** descrizione dell''attività didattica in inglese */
    @SerialName("adDesEng")
    val activityEnglishDescription: String? = null,

    /** codice del tipo di raggruppamento */
    @SerialName("tipoRagCod")
    val groupingTypeCode: String? = null,

    /** descrizione del tipo di raggruppamento */
    @SerialName("tipoRagDes")
    val groupingTypeDescription: String? = null,

    /** descrizione del tipo di raggruppamento in inglese */
    @SerialName("tipoRagDesEng")
    val groupingTypeDescriptionEnglish: String? = null,

    /** anno di coorte */
    @SerialName("annoCoorte")
    val cohortYear: Int? = null
)

@Serializable
data class Esse3PartitionFactor(
    /** tipo del fattore di partizione */
    @SerialName("tipoFatt")
    val invoiceType: String? = null,

    /** descrizione del fattore di partizione in inglese */
    @SerialName("fatPartDesEng")
    val invoicePartialDescriptionEnglish: String? = null,

    /** descrizione del fattore di partizione */
    @SerialName("fatPartDes")
    val invoicePartialDescription: String? = null,

    /** codice del fattore di partizione */
    @SerialName("fatPartCod")
    val invoicePartialCode: String = ""
)
