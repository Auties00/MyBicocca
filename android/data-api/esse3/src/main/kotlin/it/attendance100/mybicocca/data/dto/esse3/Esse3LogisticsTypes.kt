package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3EasystaffStudyPlanOrder(
    /** codice del percorso di erogazione dell'attività didattica */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** descrizione del percorso di erogazione dell'attività didattica */
    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    /** indica lo stato del pds */
    @SerialName("statoCod")
    val stateCode: String? = null,

    /** indica che il percorso è comune */
    @SerialName("comuneFlg")
    val municipalityFlag: Int? = null
)

@Serializable
data class Esse3EasystaffStudyPlanOrderWithDetails(
    /** codice del percorso di erogazione dell'attività didattica */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** descrizione del percorso di erogazione dell'attività didattica */
    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    /** indica lo stato del pds */
    @SerialName("statoCod")
    val stateCode: String? = null,

    /** indica che il percorso è comune */
    @SerialName("comuneFlg")
    val municipalityFlag: Int? = null,

    @SerialName("sedi")
    val sites: List<Esse3EasystaffLocations> = emptyList()
)

@Serializable
data class Esse3ReducedContextualizedActivityKey(
    /** chiave del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** anno di ordinamento del corso di studio di erogazione dell'attività didattica */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    /** chiave del percorso di studio di erogazione dell'attività didattica */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** anno di erogazione della partizione */
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    /** chiave dell'attività didattica */
    @SerialName("adId")
    val activityId: Long? = null
)

@Serializable
data class Esse3SyllabusActivity(
    /** id univoco che consente di individuare una condivisione logistica */
    @SerialName("adLogId")
    val activityLogId: Long = 0L,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey,

    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey,

    /** Flag che indica se le descrizioni delle attivit� didattiche sono pubblicabili */
    @SerialName("desAdPubblFlg")
    val teachingActivityPublicationFlag: Int = 0,

    /** Flag che indica se la AD corrente è la AD fisica della condivisione */
    @SerialName("fisicaFlg")
    val physicalFlag: Int? = null,

    /** Flag che indica se la AD corrente è la AD fisica della condivisione. Il flag non subisce delle elaborazioni e coincide con il valore preso dal db. */
    @SerialName("realFisicaFlg")
    val realPhysicalFlag: Int? = null,

    /** codice del tipo di catalogo del corso di studio */
    @SerialName("tipoCatalogoCod")
    val catalogTypeCode: String? = null,

    /** contenuti del corso */
    @SerialName("contenuti")
    val contents: String? = null,

    /** contenuti del corso in inglese */
    @SerialName("contenutiEng")
    val contentsEnglish: String? = null,

    /** obiettivi formativi */
    @SerialName("obiettiviFormativi")
    val trainingObjectives: String? = null,

    /** obiettivi formativi in inglese */
    @SerialName("obiettiviFormativiEng")
    val trainingObjectivesEnglish: String? = null,

    /** prerequisiti */
    @SerialName("prerequisiti")
    val prerequisites: String? = null,

    /** prerequisiti in inglese */
    @SerialName("prerequisitiEng")
    val prerequisitesEnglish: String? = null,

    /** metodi didattici */
    @SerialName("metodiDidattici")
    val teachingMethods: String? = null,

    /** metodi didattici in inglese */
    @SerialName("metodiDidatticiEng")
    val teachingMethodsEnglish: String? = null,

    /** modalita di verifica apprendimento */
    @SerialName("modalitaVerificaApprendimento")
    val learningVerificationMethod: String? = null,

    /** modalita di verifica apprendimento in inglese */
    @SerialName("modalitaVerificaApprendimentoEng")
    val learningVerificationMethodEnglish: String? = null,

    /** altre informazioni */
    @SerialName("altreInfo")
    val otherInfo: String? = null,

    /** altre informazioni in inglese */
    @SerialName("altreInfoEng")
    val otherInfoEnglish: String? = null,

    /** testi di riferimento */
    @SerialName("testiRiferimento")
    val referenceTexts: String? = null,

    /** testi di riferimento in inglese */
    @SerialName("testiRiferimentoEng")
    val referenceTextsEnglish: String? = null,

    @SerialName("adLogOpz")
    val optionalActivityLog: List<Esse3OptionalActivityLog> = emptyList(),

    /** campo opzionale 1 */
    @SerialName("syllabusOpt1")
    val syllabusOption1: String? = null,

    /** campo opzionale 1 in inglese */
    @SerialName("syllabusOpt1Eng")
    val syllabusOption1English: String? = null,

    /** campo opzionale 2 */
    @SerialName("syllabusOpt2")
    val syllabusOption2: String? = null,

    /** campo opzionale 2 in inglese */
    @SerialName("syllabusOpt2Eng")
    val syllabusOption2English: String? = null,

    /** campo opzionale 3 */
    @SerialName("syllabusOpt3")
    val syllabusOption3: String? = null,

    /** campo opzionale 3 in inglese */
    @SerialName("syllabusOpt3Eng")
    val syllabusOption3English: String? = null,

    /** obiettivi per lo sviluppo sostenibile */
    @SerialName("obiettiviSvilSostenibileDes")
    val sustainableDevelopmentObjectivesDescription: String? = null,

    /** obiettivi per lo sviluppo sostenibile in inglese */
    @SerialName("obiettiviSvilSostenibileDesEng")
    val sustainableDevelopmentObjectivesDescriptionEnglish: String? = null,

    /** lista degli obiettivi per lo sviluppo sostenibile associati */
    @SerialName("obiettiviSvilSostenibileList")
    val sustainableDevelopmentObjectivesList: String? = null,

    @SerialName("obiettiviSvilSostenibile")
    val sustainableDevelopmentObjectives: List<Esse3SustainableDevelopmentGoals> = emptyList(),

    /** contenuti del corso in spagnolo */
    @SerialName("contenutiSpa")
    val contentsSpanish: String? = null,

    /** obiettivi formativi in spagnolo */
    @SerialName("obiettiviFormativiSpa")
    val trainingObjectivesSpanish: String? = null,

    /** prerequisiti in spagnolo */
    @SerialName("prerequisitiSpa")
    val prerequisitesSpanish: String? = null,

    /** metodi didattici in spagnolo */
    @SerialName("metodiDidatticiSpa")
    val teachingMethodsSpanish: String? = null,

    /** modalita di verifica apprendimento in spagnolo */
    @SerialName("modalitaVerificaApprendimentoSpa")
    val learningVerificationMethodSpanish: String? = null,

    /** altre informazioni in spagnolo */
    @SerialName("altreInfoSpa")
    val otherInfoSpanish: String? = null,

    /** testi di riferimento in spagnolo */
    @SerialName("testiRiferimentoSpa")
    val referenceTextsSpanish: String? = null,

    /** campo opzionale 1 in spagnolo */
    @SerialName("syllabusOpt1Spa")
    val syllabusOption1Spanish: String? = null,

    /** campo opzionale 2 in spagnolo */
    @SerialName("syllabusOpt2Spa")
    val syllabusOption2Spanish: String? = null,

    /** campo opzionale 3 in spagnolo */
    @SerialName("syllabusOpt3Spa")
    val syllabusOption3Spanish: String? = null,

    /** obiettivi per lo sviluppo sostenibile in spagnolo */
    @SerialName("obiettiviSvilSostenibileDesSpa")
    val sustainableDevelopmentObjectivesDescriptionSpanish: String? = null,

    /** contenuti del corso in francese */
    @SerialName("contenutiFra")
    val contentsFrench: String? = null,

    /** obiettivi formativi in francese */
    @SerialName("obiettiviFormativiFra")
    val trainingObjectivesFrench: String? = null,

    /** prerequisiti in francese */
    @SerialName("prerequisitiFra")
    val prerequisitesFrench: String? = null,

    /** metodi didattici in francese */
    @SerialName("metodiDidatticiFra")
    val teachingMethodsFrench: String? = null,

    /** modalita di verifica apprendimento in francese */
    @SerialName("modalitaVerificaApprendimentoFra")
    val learningVerificationMethodFrench: String? = null,

    /** altre informazioni in francese */
    @SerialName("altreInfoFra")
    val otherInfoFrench: String? = null,

    /** testi di riferimento in francese */
    @SerialName("testiRiferimentoFra")
    val referenceTextsFrench: String? = null,

    /** campo opzionale 1 in francese */
    @SerialName("syllabusOpt1Fra")
    val syllabusOption1French: String? = null,

    /** campo opzionale 2 in francese */
    @SerialName("syllabusOpt2Fra")
    val syllabusOption2French: String? = null,

    /** campo opzionale 3 in francese */
    @SerialName("syllabusOpt3Fra")
    val syllabusOption3French: String? = null,

    /** obiettivi per lo sviluppo sostenibile in francese */
    @SerialName("obiettiviSvilSostenibileDesFra")
    val sustainableDevelopmentObjectivesDescriptionFrench: String? = null,

    /** contenuti del corso in tedesco */
    @SerialName("contenutiDeu")
    val contentsGerman: String? = null,

    /** obiettivi formativi in tedesco */
    @SerialName("obiettiviFormativiDeu")
    val trainingObjectivesGerman: String? = null,

    /** prerequisiti in tedesco */
    @SerialName("prerequisitiDeu")
    val prerequisitesGerman: String? = null,

    /** metodi didattici in tedesco */
    @SerialName("metodiDidatticiDeu")
    val teachingMethodsGerman: String? = null,

    /** modalita di verifica apprendimento in tedesco */
    @SerialName("modalitaVerificaApprendimentoDeu")
    val learningVerificationMethodGerman: String? = null,

    /** altre informazioni in tedesco */
    @SerialName("altreInfoDeu")
    val otherInfoGerman: String? = null,

    /** testi di riferimento in tedesco */
    @SerialName("testiRiferimentoDeu")
    val referenceTextsGerman: String? = null,

    /** campo opzionale 1 in tedesco */
    @SerialName("syllabusOpt1Deu")
    val syllabusOption1German: String? = null,

    /** campo opzionale 2 in tedesco */
    @SerialName("syllabusOpt2Deu")
    val syllabusOption2German: String? = null,

    /** campo opzionale 3 in tedesco */
    @SerialName("syllabusOpt3Deu")
    val syllabusOption3German: String? = null,

    /** obiettivi per lo sviluppo sostenibile in tedesco */
    @SerialName("obiettiviSvilSostenibileDesDeu")
    val sustainableDevelopmentObjectivesDescriptionGerman: String? = null
)

@Serializable
data class Esse3ActivityLog(
    /** Data di ultima modifica dell'intera logistica. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataModLog")
    val logModificationDate: String? = null,

    /** descrizione della sede in inglese */
    @SerialName("sedeDesEng")
    val siteDescriptionEnglish: String? = null,

    /** descrizione della sede */
    @SerialName("sedeDes")
    val siteDescription: String? = null,

    /** ID chiave della sede */
    @SerialName("sedeId")
    val siteId: Long? = null,

    /** descrizione del tipo di didattica del corso di studio in inglese */
    @SerialName("tipoDidDesEng")
    val didacticTypeDescriptionEnglish: String? = null,

    /** descrizione del tipo di didattica del corso di studio */
    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    /** codice del tipo di didattica del corso di studio */
    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    /** descrizione della lingua di erogazione della didattica */
    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    /** codice ISO6392 della lingua di erogazione della didattica */
    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    /** id della lingua di erogazione della didattica */
    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    /** descrizione della partizione effettiva dell'anno accademico in inglese */
    @SerialName("partEffDesEng")
    val effectivePartialDescriptionEnglish: String? = null,

    /** descrizione della partizione effettiva dell'anno accademico */
    @SerialName("partEffDes")
    val effectivePartialDescription: String? = null,

    /** codice della partizione effettiva dell'anno accademico */
    @SerialName("partEffCod")
    val effectivePartialCode: String? = null,

    /** desrizione del dominio di partizione effettivo */
    @SerialName("domPartEffDes")
    val domicileEffectivePartialDescription: String? = null,

    /** codice del dominio di partizione effettivo */
    @SerialName("domPartEffCod")
    val domicileEffectivePartialCode: String? = null,

    /** descrizione del fattore di partizione effettivo */
    @SerialName("fatPartEffDes")
    val invoiceEffectivePartialDescription: String? = null,

    /** codice del fattore di partizione effettivo */
    @SerialName("fatPartEffCod")
    val invoiceEffectivePartialCode: String? = null,

    /** Data fine compilazione questionari per la valutazione della didattica. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataFinValDid")
    val didacticEvaluationEndDate: String? = null,

    /** Data di inizio compilazione questionari per la valutazione della didattica. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataIniValDid")
    val didacticEvaluationStartDate: String? = null,

    /** Data fine del periodo didattico. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataFine")
    val endDate: String? = null,

    /** Data inizio del periodo didattico. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataInizio")
    val startDate: String? = null,

    @SerialName("chiaveADFisica")
    val physicalTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey
)

@Serializable
data class Esse3Building(
    /** id dell'edificio di ESSE3, chiave primaria di esse3 */
    @SerialName("edificioId")
    val buildingId: Long? = null,

    /** Codice dell'edificio di ESSE3, chiave alternativa di esse3 */
    @SerialName("edificioCod")
    val buildingCode: String? = null,

    /** Codice dell'edirficio del sistema di logistica esterno, chiave univoca nel sistema esterno */
    @SerialName("extEdificioCod")
    val externalBuildingCode: String? = null,

    /** Descrizione dell'edificio */
    @SerialName("edificioDes")
    val buildingDescription: String? = null,

    /** id della nazione di appartendenza dell'edificio */
    @SerialName("nazioneId")
    val nationId: Long? = null,

    /** codice nazione di appartenza dell'edificio */
    @SerialName("nazioneCod")
    val nationCode: String? = null,

    /** descrizione nazione di appartenza dell'edificio */
    @SerialName("nazioneDes")
    val nationDescription: String? = null,

    /** nel caso di risorse della nazione di default, rappresenta il comuneId di appartenenza dell'edificio */
    @SerialName("comuneId")
    val municipalityId: Long? = null,

    /** nel caso di risorse della nazione di default, rappresenta il codice comune di appartenza dell'edificio */
    @SerialName("comuneCod")
    val municipalityCode: String? = null,

    /** nel caso di risorse della nazione di default, rappresenta la descrizione del comune di appartenza dell'edificio */
    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    /** indirizzo aula */
    @SerialName("via")
    val street: String? = null,

    /** città straniera */
    @SerialName("citstra")
    val foreignCity: String? = null,

    /** nota libera */
    @SerialName("nota")
    val note: String? = null,

    /** url pagina web */
    @SerialName("urlWeb")
    val webUrl: String? = null
)

@Serializable
data class Esse3EasystaffActivity(
    /** codice del corso di erogazione dell'attività didattica */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di erogazione dell'attività didattica */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** codice dell'ordinamento di erogazione dell'attività didattica */
    @SerialName("cdsordCod")
    val courseOfStudyOrderCode: String? = null,

    /** descrizione dell'ordinamento di erogazione dell'attività didattica */
    @SerialName("cdsordDes")
    val courseOfStudyOrderDescription: String? = null,

    /** codice del percorso di erogazione dell'attività didattica */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** descrizione del percorso di erogazione dell'attività didattica */
    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    /** codice dell'attività didattica */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell'attività didattica */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** anno di corso in cui è prevista l'attività */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** codice del dipartimento di erogazione dell'attività didattica */
    @SerialName("dipCod")
    val departmentCode: String? = null,

    /** codice CSA del dipartimento di erogazione dell'attività didattica */
    @SerialName("dipCsaCod")
    val departmentCsaCode: String? = null,

    /** codice ISTAT del dipartimento di erogazione dell'attività didattica */
    @SerialName("dipIstatCod")
    val departmentIstatCode: String? = null,

    /** descrizione del dipartimento di erogazione dell'attività didattica */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** codice del tipo corso del corso di studio di erogazione dell'attività didattica */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** descrizione del tipo corso del corso di studio di erogazione dell'attività didattica */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** codice del tipo di specializzazione (tipoCorsoCod = S1) per le scuole di specializzazione */
    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    /** codice dominio di partizione per la scomposizione delle partizioni logistiche */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** descrizione dominio di partizione per la scomposizione delle partizioni logistiche */
    @SerialName("domPartDes")
    val domicilePartialDescription: String? = null,

    /** flag che indica se l'attività didattica è l'attività che eroga la lezione (fisicaFlg=1) oppure quella che la mutua (fisicaFlg=0) */
    @SerialName("fisicaFlg")
    val physicalFlag: Int? = null,

    /** id del docente */
    @SerialName("docenteTitolareId")
    val holderLecturerId: Long? = null,

    /** matricola del docente */
    @SerialName("docenteTitolareMatricola")
    val holderLecturerMatricola: String? = null,

    /** codice fiscale del docente */
    @SerialName("docenteTitolareCodFis")
    val holderLecturerFiscalCode: String? = null,

    /** id di ugov del docente */
    @SerialName("docenteTitolareIdAb")
    val holderLecturerAbbreviatedId: Long? = null
)

@Serializable
data class Esse3OptionalTeacherWorkload(
    /** id univoco che consente di individuare una condivisione logistica */
    @SerialName("adLogId")
    val activityLogId: Long = 0L,

    /** id univoco che consente di individuare una condivisione logistica a livello di moduli */
    @SerialName("udLogId")
    val teachingUnitLogId: Long = 0L,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3ReducedPartitionKey,

    /** codice identificativo del tipo di credito del segmento corrente */
    @SerialName("tipoCreCod")
    val creditTypeCode: String = "",

    /** chiave del docente che fa lezione */
    @SerialName("docenteId")
    val lecturerId: Long = 0L,

    /** codice fiscale del docente */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** email del docente */
    @SerialName("eMail")
    val email: String? = null,

    /** codice settore scientifico disciplinare */
    @SerialName("settCod")
    val sectorCode: String? = null,

    /** indica se il docente è titolare della partizione studenti dell´AD a livello di PDS */
    @SerialName("titolareFlg")
    val holderFlag: Int? = null,

    /** indica se il docente è responsabile didattico della partizione studenti dell´UD */
    @SerialName("respDidFlg")
    val didacticResponsibleFlag: Int? = null,

    /** id utente */
    @SerialName("userId")
    val userId: String? = null,

    /** data modifica informazioni sul docente */
    @SerialName("dataModDoc")
    val documentModificationDate: String? = null,

    /** data modifica informazioni relative alla ripartizione del carico didattico tra i docenti per tipo di attività */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** id univoco che consente di individuare una copertura */
    @SerialName("ugovCoperId")
    val uGovCoverageId: Long? = null,

    /** codice identificativo del tipo di copertura del docente */
    @SerialName("tipoCoperturaCod")
    val coverageTypeCode: String? = null,

    /** descrizione del tipo di copertura del docente */
    @SerialName("tipoCoperturaDes")
    val coverageTypeDescription: String? = null
)

@Serializable
data class Esse3SustainableDevelopmentGoals(
    /** codice obiettivo sviluppo sostenibile */
    @SerialName("obiettiviSvilSosCod")
    val sustainableDevelopmentObjectiveCode: String? = null,

    /** descrizione obiettivo sviluppo sostenibile */
    @SerialName("obiettiviSvilSosDes")
    val sustainableDevelopmentObjectiveDescription: String? = null,

    /** descrizione estesa obiettivo sviluppo sostenibile */
    @SerialName("obiettiviSvilSosDesEstesa")
    val extendedSustainableDevelopmentObjectiveDescription: String? = null,

    /** descrizione obiettivo sviluppo sostenibile in inglese */
    @SerialName("obiettiviSvilSosDesEng")
    val sustainableDevelopmentObjectiveDescriptionEnglish: String? = null,

    /** descrizione estesa obiettivo sviluppo sostenibile in inglese */
    @SerialName("obiettiviSvilSosDesEngEstesa")
    val extendedSustainableDevelopmentObjectiveDescriptionEnglish: String? = null
)

@Serializable
data class Esse3SyllabusActivityPatchResult(
    /** Numero di righe aggiornate in P09_AD_LOG_PDS */
    @SerialName("righeAdLogPdsAggiornate")
    val updatedStudyPlanTeachingActivityLogRows: Int? = null,

    @SerialName("righeAdLogPdsDesLinAggiornate")
    val updatedStudyPlanTeachingActivityLogDescriptionLines: List<kotlinx.serialization.json.JsonObject> = emptyList()
)

@Serializable
data class Esse3Classroom(
    /** id dell'aula di ESSE3, chiave primaria di esse3 insieme a edificioId */
    @SerialName("aulaId")
    val classroomId: Long? = null,

    /** id dell'edificio di ESSE3, chiave primaria di esse3 insieme a aulaId */
    @SerialName("edificioId")
    val buildingId: Long? = null,

    /** Codice dell'aula di ESSE3, chiave alternativa insieme al codice edificio */
    @SerialName("aulaCod")
    val classroomCode: String = "",

    /** Codice dell'aula di ESSE3, chiave alternativa insieme al codice aula */
    @SerialName("edificioCod")
    val buildingCode: String = "",

    /** Codice dell'aula del sistema di logistica esterno, chiave univoca nel sistema esterno */
    @SerialName("extAulaCod")
    val externalClassroomCode: String = "",

    /** Descrizione dell'aula */
    @SerialName("aulaDes")
    val classroomDescription: String? = null,

    /** capienza dell'aula */
    @SerialName("capienza")
    val capacity: Int? = null,

    /** indica il tipo di aula in relazione al CBT (Computer Base Test), assume i seguenti valori (N=> Non abilitata, C => abilitata solo al CBT, T => entrambi) */
    @SerialName("abilCbt")
    val computerBasedTestAuthorization: Esse3ComputerBasedTestAuthorization? = null,

    /** flag che indica se l'aula risulta abilitata */
    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    @SerialName("dipartimenti")
    val departments: List<Esse3Department> = emptyList()
)

@Serializable
data class Esse3LogisticsWithDetails(
    @SerialName("UdLogConDettagli")
    val teachingUnitLogWithDetails: List<Esse3TeachingUnitLogWithDetails> = emptyList(),

    @SerialName("SyllabusAD")
    val syllabusTeachingActivity: List<Esse3SyllabusActivity> = emptyList(),

    /** Data di ultima modifica dell'intera logistica. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataModLog")
    val logModificationDate: String? = null,

    /** descrizione della sede in inglese */
    @SerialName("sedeDesEng")
    val siteDescriptionEnglish: String? = null,

    /** descrizione della sede */
    @SerialName("sedeDes")
    val siteDescription: String? = null,

    /** ID chiave della sede */
    @SerialName("sedeId")
    val siteId: Long? = null,

    /** descrizione del tipo di didattica del corso di studio in inglese */
    @SerialName("tipoDidDesEng")
    val didacticTypeDescriptionEnglish: String? = null,

    /** descrizione del tipo di didattica del corso di studio */
    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    /** codice del tipo di didattica del corso di studio */
    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    /** descrizione della lingua di erogazione della didattica */
    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    /** codice ISO6392 della lingua di erogazione della didattica */
    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    /** id della lingua di erogazione della didattica */
    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    /** descrizione della partizione effettiva dell'anno accademico in inglese */
    @SerialName("partEffDesEng")
    val effectivePartialDescriptionEnglish: String? = null,

    /** descrizione della partizione effettiva dell'anno accademico */
    @SerialName("partEffDes")
    val effectivePartialDescription: String? = null,

    /** codice della partizione effettiva dell'anno accademico */
    @SerialName("partEffCod")
    val effectivePartialCode: String? = null,

    /** desrizione del dominio di partizione effettivo */
    @SerialName("domPartEffDes")
    val domicileEffectivePartialDescription: String? = null,

    /** codice del dominio di partizione effettivo */
    @SerialName("domPartEffCod")
    val domicileEffectivePartialCode: String? = null,

    /** descrizione del fattore di partizione effettivo */
    @SerialName("fatPartEffDes")
    val invoiceEffectivePartialDescription: String? = null,

    /** codice del fattore di partizione effettivo */
    @SerialName("fatPartEffCod")
    val invoiceEffectivePartialCode: String? = null,

    /** Data fine compilazione questionari per la valutazione della didattica. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataFinValDid")
    val didacticEvaluationEndDate: String? = null,

    /** Data di inizio compilazione questionari per la valutazione della didattica. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataIniValDid")
    val didacticEvaluationStartDate: String? = null,

    /** Data fine del periodo didattico. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataFine")
    val endDate: String? = null,

    /** Data inizio del periodo didattico. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataInizio")
    val startDate: String? = null,

    @SerialName("chiaveADFisica")
    val physicalTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey? = null
)

@Serializable
data class Esse3SyllabusActivityPatchField(
    /** Nome del campo da aggiornare */
    @SerialName("nomeCampo")
    val fieldName: Esse3FieldName? = null,

    /** Valore da aggiornare nel campo indicato in `nomeCampo` */
    @SerialName("valore")
    val value: String? = null,

    /** Codice iso6392 relativa alla lingua associata al del campo da modificare */
    @SerialName("iso6392")
    val iso6392: String = ""
)

@Serializable
data class Esse3EasystaffLocations(
    /** descrizione della sede */
    @SerialName("sedeDes")
    val siteDescription: String? = null,

    /** indica che è la sede principale */
    @SerialName("sedePrincipaleFlg")
    val mainSiteFlag: Int? = null
)

@Serializable
data class Esse3ReducedPartitionKey(
    /** anno di erogazione della partizione */
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    /** codice del fattore di partizione */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** codice del dominio di partizione */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** codice della partizione dell'anno accademico */
    @SerialName("partCod")
    val partialCode: String? = null,

    /** id del raggruppamento di logistica */
    @SerialName("adLogId")
    val activityLogId: Long? = null
)

@Serializable
data class Esse3EasystaffCourseOrderWithDetails(
    /** codice del corso di erogazione dell'attività didattica */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di erogazione dell'attività didattica */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** codice dell'ordinamento di erogazione dell'attività didattica */
    @SerialName("cdsordCod")
    val courseOfStudyOrderCode: String? = null,

    /** descrizione dell'ordinamento di erogazione dell'attività didattica */
    @SerialName("cdsordDes")
    val courseOfStudyOrderDescription: String? = null,

    /** codice del dipartimento di erogazione dell'attività didattica */
    @SerialName("dipCod")
    val departmentCode: String? = null,

    /** codice CSA del dipartimento di erogazione dell'attività didattica */
    @SerialName("dipCsaCod")
    val departmentCsaCode: String? = null,

    /** codice ISTAT del dipartimento di erogazione dell'attività didattica */
    @SerialName("dipIstatCod")
    val departmentIstatCode: String? = null,

    /** descrizione del dipartimento di erogazione dell'attività didattica */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** durata legale dell'ordinamento del corso di studio */
    @SerialName("durataAnni")
    val durationYears: Int? = null,

    /** anno di scelta del percorso di studio */
    @SerialName("acScelta")
    val choiceActivity: Int? = null,

    /** codice del tipo di corso di studio */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("percorsi")
    val paths: List<Esse3EasystaffStudyPlanOrderWithDetails> = emptyList()
)

@Serializable
data class Esse3SystemLogImportResult(
    /** sistema di logistica esterno */
    @SerialName("sistLog")
    val systemLog: Esse3SystemLog? = null,

    /** codice di ritorno */
    @SerialName("ret")
    val ret: Int? = null,

    /** messaggio di errore */
    @SerialName("errMsg")
    val errorMessage: String? = null,

    @SerialName("logs")
    val logs: List<Esse3SystemLogImportMessage> = emptyList()
)

@Serializable
data class Esse3DeletedLogistics(
    /** id univoco che consente di individuare una condivisione logistica */
    @SerialName("adLogId")
    val activityLogId: Long = 0L,

    /** Data di ultima modifica della Logistica e dettagli. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataModLog")
    val logModificationDate: String? = null
)

@Serializable
data class Esse3OptionalActivityLog(
    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ReducedContextualizedActivityKey,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3ReducedPartitionKey,

    /** id univoco facoltà */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice facoltà */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione facoltà */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** descrizione facoltà (in inglese) */
    @SerialName("facDesEng")
    val facultyDescriptionEnglish: String? = null,

    /** codice area disciplinare di afferenza della facoltà/dipartimento */
    @SerialName("areaDiscCod")
    val disciplinaryAreaCode: String? = null,

    /** descrizione area disciplinare di afferenza della facoltà/dipartimento */
    @SerialName("areaDiscDes")
    val disciplinaryAreaDescription: String? = null,

    /** descrizione area disciplinare di afferenza della facoltà/dipartimento (in inglese) */
    @SerialName("areaDiscDesEng")
    val disciplinaryAreaDescriptionEnglish: String? = null,

    /** integrato */
    @SerialName("integratoFlg")
    val integratedFlag: Int? = null,

    /** codice tipo corso */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** descrizione del corso */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** descrizione del corso (in inglese) */
    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null
)

@Serializable
data class Esse3Department(
    /** id del dipartimento */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** Codice del dipartimento */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** Descrizione del dipartimento */
    @SerialName("facDes")
    val facultyDescription: String? = null
)

@Serializable
data class Esse3EasystaffTeachingUnit(
    /** codice del modulo dell'attività didattica */
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    /** descrizione del modulo dell'attività didattica */
    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    /** flag master */
    @SerialName("masterFlg")
    val masterFlag: Int? = null,

    /** flag che indica se l'attività didattica è l'attività che eroga la lezione (fisicaFlg=1) oppure quella che la mutua (fisicaFlg=0) */
    @SerialName("fisicaFlg")
    val physicalFlag: Int? = null
)

@Serializable
data class Esse3EasystaffActivityLogWithDetails(
    /** Anno di offerta di erogazione della logistica */
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    /** chiave identificativa della logistica */
    @SerialName("adLogId")
    val activityLogId: Long? = null,

    /** codice fattore di partizione per la scomposizione delle partizioni logistiche */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** descrizione fattore di partizione per la scomposizione delle partizioni logistiche */
    @SerialName("fatPartDes")
    val invoicePartialDescription: String? = null,

    /** semestre di erogazione della logistica */
    @SerialName("partCod")
    val partialCode: String? = null,

    /** descrizione semestre di erogazione della logistica */
    @SerialName("partDes")
    val partialDescription: String? = null,

    @SerialName("docenti")
    val lecturers: List<Esse3EasystaffTeacher> = emptyList(),

    @SerialName("attivita")
    val activity: List<Esse3EasystaffActivityWithDetails> = emptyList()
)

@Serializable
data class Esse3TeacherWorkload(
    /** id univoco che consente di individuare una condivisione logistica */
    @SerialName("adLogId")
    val activityLogId: Long = 0L,

    /** id univoco che consente di individuare una condivisione logistica a livello di moduli */
    @SerialName("udLogId")
    val teachingUnitLogId: Long = 0L,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey,

    /** codice identificativo del tipo di credito del segmento corrente */
    @SerialName("tipoCreCod")
    val creditTypeCode: String = "",

    /** descrizione identificativo del tipo di credito del segmento corrente */
    @SerialName("tipoCreDes")
    val creditTypeDescription: String? = null,

    /** descrizione identificativo del tipo di credito in inglese */
    @SerialName("tipoCreDesEng")
    val creditTypeDescriptionEnglish: String? = null,

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

    /** Ore di lezione */
    @SerialName("ore")
    val hours: Float? = null,

    /** Frazione di carico in percentuale col complessivo delle ore per il tipo di credito */
    @SerialName("frazioneCarico")
    val fractionCharge: Float? = null,

    /** Flag che indica se la il docente � valutabile con i questionari */
    @SerialName("valDidFlg")
    val didacticEvaluationFlag: Int? = null,

    /** Ore di attività a supporto della didattica */
    @SerialName("oreAttSuppDid")
    val supplementaryDidacticHours: Float? = null,

    /** id univoco che consente di individuare una copertura >- Id tabella DI_COPER (copertura, incarico didattico) in U-Gov Didattica */
    @SerialName("uGovCoperId")
    val uGovCoverageId: Long? = null,

    /** codice identificativo del tipo di copertura del docente */
    @SerialName("tipoCoperturaCod")
    val coverageTypeCode: String? = null,

    /** descrizione del tipo di copertura del docente */
    @SerialName("tipoCoperturaDes")
    val coverageTypeDescription: String? = null,

    @SerialName("CaricoDocentiOpz")
    val optionalTeachingLoad: List<Esse3OptionalTeacherWorkload> = emptyList()
)

@Serializable
data class Esse3LogisticsPerTeacher(
    /** anno di erogazione della partizione */
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    /** chiave del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di erogazione dell'attività didattica */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** descrizione in inglese del corso di erogazione dell'attività didattica */
    @SerialName("cdsDesEng")
    val courseOfStudyDescriptionEnglish: String? = null,

    /** anno di ordinamento del corso di studio di erogazione dell'attività didattica */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    /** codice dell''ordinamento di erogazione dell'attività didattica */
    @SerialName("aaOrdCod")
    val academicYearOrderCode: String? = null,

    /** descrizione dell''ordinamento di erogazione dell'attività didattica */
    @SerialName("aaOrdDes")
    val academicYearOrderDescription: String? = null,

    /** chiave del percorso di studio di erogazione dell'attività didattica */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** codice del percorso di erogazione dell'attività didattica */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** descrizione del percorso di erogazione dell'attività didattica */
    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    /** chiave dell'attività didattica */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice dell''attività didattica */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell''attività didattica */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** descrizione in inglese dell''attività didattica */
    @SerialName("adDesEng")
    val activityEnglishDescription: String? = null,

    /** chiave del'unità (modulo) dell'attività didattica */
    @SerialName("udId")
    val teachingUnitId: Long? = null,

    /** codice dell'unità (modulo) dell'attività didattica */
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    /** descrizione dell'unità (modulo) dell'attività didattica */
    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    /** descrizione in inglese dell'unità (modulo) dell'attività didattica */
    @SerialName("udDesEng")
    val teachingUnitDescriptionEnglish: String? = null,

    /** Flag che indica se la UD corrente � la UD fisica della condivisione */
    @SerialName("masterFlg")
    val masterFlag: Int? = null,

    /** id univoco che consente di individuare una condivisione logistica a livello di moduli */
    @SerialName("uGovArId")
    val uGovAreaId: Long? = null,

    /** id univoco che consente di individuare una attivit� didattica offerta >- id della afId proveniente da U-Gov Didattica */
    @SerialName("uGovAfId")
    val uGovTeachingActivityId: Long? = null,

    /** id univoco che consente di individuare una condivisione logistica */
    @SerialName("adLogId")
    val activityLogId: Long? = null,

    /** id univoco che consente di individuare una condivisione logistica a livello di moduli */
    @SerialName("udLogId")
    val teachingUnitLogId: Long? = null,

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

    /** codice della partizione dell'anno accademico */
    @SerialName("partCod")
    val partialCode: String? = null,

    /** descrizione della partizione dell'anno accademico */
    @SerialName("partDes")
    val partialDescription: String? = null,

    /** codice identificativo del tipo di credito del segmento corrente */
    @SerialName("tipoCreCod")
    val creditTypeCode: String? = null,

    /** descrizione identificativo del tipo di credito del segmento corrente */
    @SerialName("tipoCreDes")
    val creditTypeDescription: String? = null,

    /** chiave del docente che fa lezione */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** codice fiscale del docente */
    @SerialName("docenteCodFis")
    val lecturerFiscalCode: String? = null,

    /** matricola del docente che fa lezione */
    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    /** nome del docente che fa lezione */
    @SerialName("docenteNome")
    val lecturerName: String? = null,

    /** cognome del docente che fa lezione */
    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    /** email del docente */
    @SerialName("docenteEMail")
    val lecturerEmail: String? = null,

    /** codice settore scientifico disciplinare */
    @SerialName("settCod")
    val sectorCode: String? = null,

    /** Frazione di carico in percentuale col complessivo delle ore per il tipo di credito */
    @SerialName("frazioneCarico")
    val fractionCharge: Float? = null,

    /** Ore di lezione */
    @SerialName("ore")
    val hours: Float? = null,

    /** Flag che indica se la il docente � valutabile con i questionari */
    @SerialName("valDidFlg")
    val didacticEvaluationFlag: Int? = null,

    @SerialName("oreAttSupDid")
    val supplementaryDidacticActivityHours: Float? = null,

    /** codice del tipo di copertura del docente */
    @SerialName("tipoCoperturaCod")
    val coverageTypeCode: String? = null,

    /** id univoco che consente di individuare una copertura >- Id tabella DI_COPER (copertura, incarico didattico) in U-Gov Didattica */
    @SerialName("uGovCoperId")
    val uGovCoverageId: Long? = null,

    /** Flag che indica se il docente � titolare per la AD corrente */
    @SerialName("titolareFlg")
    val holderFlag: Int? = null,

    /** Flag che indica se il docente � responsabile didattico per la UD corrente */
    @SerialName("respDidFlg")
    val didacticResponsibleFlag: Int? = null,

    /** id utente */
    @SerialName("userId")
    val userId: String? = null,

    /** data modifica informazioni sul docente */
    @SerialName("dataModDoc")
    val documentModificationDate: String? = null,

    /** data modifica informazioni relative alla ripartizione del carico didattico tra i docenti per tipo di attività */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Flag di AD fisica della condivisione */
    @SerialName("fisicaFlg")
    val physicalFlag: Int? = null,

    /** codice della tipologia del corso di erogazione dell'attività didattica */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** descrizione della tipologia del corso di erogazione dell'attività didattica */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** descrizione in inglese della tipologia del corso di erogazione dell'attività didattica */
    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    /** descrizione */
    @SerialName("claMCod")
    val classMCode: String? = null
)

@Serializable
data class Esse3EasystaffActivityLog(
    /** Anno di offerta di erogazione della logistica */
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    /** chiave identificativa della logistica */
    @SerialName("adLogId")
    val activityLogId: Long? = null,

    /** codice fattore di partizione per la scomposizione delle partizioni logistiche */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** descrizione fattore di partizione per la scomposizione delle partizioni logistiche */
    @SerialName("fatPartDes")
    val invoicePartialDescription: String? = null,

    /** semestre di erogazione della logistica */
    @SerialName("partCod")
    val partialCode: String? = null,

    /** descrizione semestre di erogazione della logistica */
    @SerialName("partDes")
    val partialDescription: String? = null
)

@Serializable
data class Esse3PartitionKey(
    /** anno di erogazione della partizione */
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    /** codice del fattore di partizione */
    @SerialName("fatPartCod")
    val invoicePartialCode: String = "",

    /** descrizione del fattore di partizione */
    @SerialName("fatPartDes")
    val invoicePartialDescription: String? = null,

    /** descrizione del fattore di partizione in inglese */
    @SerialName("fatPartDesEng")
    val invoicePartialDescriptionEnglish: String? = null,

    /** codice del dominio di partizione */
    @SerialName("domPartCod")
    val domicilePartialCode: String = "",

    /** desrizione del dominio di partizione */
    @SerialName("domPartDes")
    val domicilePartialDescription: String? = null,

    /** descrizione del dominio di partizione in inglese */
    @SerialName("domPartDesEng")
    val domicilePartialDescriptionEnglish: String? = null,

    /** codice della partizione dell'anno accademico */
    @SerialName("partCod")
    val partialCode: String = "",

    /** descrizione della partizione dell'anno accademico */
    @SerialName("partDes")
    val partialDescription: String? = null,

    /** descrizione della partizione dell'anno accademico in inglese */
    @SerialName("partDesEng")
    val partialDescriptionEnglish: String? = null,

    /** id del raggruppamento di logistica */
    @SerialName("adLogId")
    val activityLogId: Long? = null
)

@Serializable
data class Esse3EasystaffTeacher(
    /** chiave identificativa del modulo della logistica */
    @SerialName("udLogId")
    val teachingUnitLogId: Long? = null,

    /** codice del tipo di credito */
    @SerialName("tipoCreCod")
    val creditTypeCode: String? = null,

    /** descrizione del tipo di credito */
    @SerialName("tipoCreDes")
    val creditTypeDescription: String? = null,

    /** matricola del docente */
    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    /** codice fiscale del docente */
    @SerialName("docenteCodFis")
    val lecturerFiscalCode: String? = null,

    /** id di ugov del docente */
    @SerialName("docenteIdAb")
    val lecturerAbbreviatedId: Long? = null
)

@Serializable
data class Esse3SyllabusActivityPatch(
    /** anno di offerta di erogazione dell'attività didattica */
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

    /** codice dell''attività didattica */
    @SerialName("adCod")
    val activityCode: String = "",

    /** codice del fattore di partizione */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** codice del dominio di partizione */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** lista degli obiettivi per lo sviluppo sostenibile associati */
    @SerialName("obiettiviSvilSostenibileList")
    val sustainableDevelopmentObjectivesList: String? = null,

    /** Se impostato a 1 aggiorna a NULL il contenuto di obiettiviSvilSostenibileList indipendentemente dal valore contenuto nel campo */
    @SerialName("rimuoviObiettiviSvilSostenibileList")
    val removeSustainableDevelopmentObjectivesList: Int? = null,

    /** Flag che indica se le descrizioni delle attivit� didattiche sono pubblicabili */
    @SerialName("desAdPubblFlg")
    val teachingActivityPublicationFlag: Int? = null,

    /** Flag che indica se la AD corrente è la AD fisica della condivisione */
    @SerialName("fisicaFlg")
    val physicalFlag: Int? = null,

    @SerialName("campiSyllabus")
    val syllabusFields: List<Esse3SyllabusActivityPatchField> = emptyList()
)

@Serializable
data class Esse3TeachingUnitLogWithDetails(
    @SerialName("CaricoDocenti")
    val teachingLoad: List<Esse3TeacherWorkload> = emptyList(),

    @SerialName("SyllabusUD")
    val syllabusTeachingUnit: List<Esse3SyllabusTeachingUnit> = emptyList(),

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey? = null,

    @SerialName("chiaveUDMaster")
    val masterTeachingUnitKey: Esse3ContextualizedTeachingUnitKey? = null,

    /** id non univoco che consente di individuare una condivisione logistica a livello di moduli */
    @SerialName("udLogId")
    val teachingUnitLogId: Long? = null,

    /** id univoco che consente di individuare una condivisione logistica a livello di moduli */
    @SerialName("uGovArId")
    val uGovAreaId: Long? = null
)

@Serializable
data class Esse3SystemLogImportMessage(
    /** livello del messaggio */
    @SerialName("level")
    val level: Esse3Level? = null,

    /** messaggio di log */
    @SerialName("msg")
    val message: String? = null
)

@Serializable
data class Esse3EasystaffCourseOrder(
    /** codice del corso di erogazione dell'attività didattica */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di erogazione dell'attività didattica */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** codice dell'ordinamento di erogazione dell'attività didattica */
    @SerialName("cdsordCod")
    val courseOfStudyOrderCode: String? = null,

    /** descrizione dell'ordinamento di erogazione dell'attività didattica */
    @SerialName("cdsordDes")
    val courseOfStudyOrderDescription: String? = null,

    /** codice del dipartimento di erogazione dell'attività didattica */
    @SerialName("dipCod")
    val departmentCode: String? = null,

    /** codice CSA del dipartimento di erogazione dell'attività didattica */
    @SerialName("dipCsaCod")
    val departmentCsaCode: String? = null,

    /** codice ISTAT del dipartimento di erogazione dell'attività didattica */
    @SerialName("dipIstatCod")
    val departmentIstatCode: String? = null,

    /** descrizione del dipartimento di erogazione dell'attività didattica */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** durata legale dell'ordinamento del corso di studio */
    @SerialName("durataAnni")
    val durationYears: Int? = null,

    /** anno di scelta del percorso di studio */
    @SerialName("acScelta")
    val choiceActivity: Int? = null,

    /** codice del tipo di corso di studio */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null
)

@Serializable
data class Esse3EasystaffActivityWithDetails(
    /** codice del corso di erogazione dell'attività didattica */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di erogazione dell'attività didattica */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** codice dell'ordinamento di erogazione dell'attività didattica */
    @SerialName("cdsordCod")
    val courseOfStudyOrderCode: String? = null,

    /** descrizione dell'ordinamento di erogazione dell'attività didattica */
    @SerialName("cdsordDes")
    val courseOfStudyOrderDescription: String? = null,

    /** codice del percorso di erogazione dell'attività didattica */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** descrizione del percorso di erogazione dell'attività didattica */
    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    /** codice dell'attività didattica */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell'attività didattica */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** anno di corso in cui è prevista l'attività */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** codice del dipartimento di erogazione dell'attività didattica */
    @SerialName("dipCod")
    val departmentCode: String? = null,

    /** codice CSA del dipartimento di erogazione dell'attività didattica */
    @SerialName("dipCsaCod")
    val departmentCsaCode: String? = null,

    /** codice ISTAT del dipartimento di erogazione dell'attività didattica */
    @SerialName("dipIstatCod")
    val departmentIstatCode: String? = null,

    /** descrizione del dipartimento di erogazione dell'attività didattica */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** codice del tipo corso del corso di studio di erogazione dell'attività didattica */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** descrizione del tipo corso del corso di studio di erogazione dell'attività didattica */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** codice del tipo di specializzazione (tipoCorsoCod = S1) per le scuole di specializzazione */
    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    /** codice dominio di partizione per la scomposizione delle partizioni logistiche */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** descrizione dominio di partizione per la scomposizione delle partizioni logistiche */
    @SerialName("domPartDes")
    val domicilePartialDescription: String? = null,

    /** flag che indica se l'attività didattica è l'attività che eroga la lezione (fisicaFlg=1) oppure quella che la mutua (fisicaFlg=0) */
    @SerialName("fisicaFlg")
    val physicalFlag: Int? = null,

    /** id del docente */
    @SerialName("docenteTitolareId")
    val holderLecturerId: Long? = null,

    /** matricola del docente */
    @SerialName("docenteTitolareMatricola")
    val holderLecturerMatricola: String? = null,

    /** codice fiscale del docente */
    @SerialName("docenteTitolareCodFis")
    val holderLecturerFiscalCode: String? = null,

    /** id di ugov del docente */
    @SerialName("docenteTitolareIdAb")
    val holderLecturerAbbreviatedId: Long? = null,

    @SerialName("moduli")
    val modules: List<Esse3EasystaffTeachingUnit> = emptyList()
)

@Serializable
data class Esse3SyllabusTeachingUnit(
    /** id univoco che consente di individuare una condivisione logistica */
    @SerialName("adLogId")
    val activityLogId: Long = 0L,

    /** id univoco che consente di individuare una condivisione logistica a livello di moduli */
    @SerialName("udLogId")
    val teachingUnitLogId: Long = 0L,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey,

    @SerialName("chiaveUDContestualizzata")
    val contextualizedTeachingUnitKey: Esse3ContextualizedTeachingUnitKey,

    /** Flag che indica se le descrizioni delle unit� didattiche sono pubblicabili */
    @SerialName("desUdPubblFlg")
    val teachingUnitPublicationFlag: Int = 0,

    /** Flag che indica se la UD corrente è la UD fisica della condivisione */
    @SerialName("masterFlg")
    val masterFlag: Int? = null,

    /** Flag che indica se la UD corrente è la UD fisica della condivisione. Il flag non subisce delle elaborazioni e coincide con il valore preso dal db. */
    @SerialName("realMasterFlg")
    val realMasterFlag: Int? = null,

    /** contenuti del corso */
    @SerialName("contenuti")
    val contents: String? = null,

    /** contenuti del corso in inglese */
    @SerialName("contenutiEng")
    val contentsEnglish: String? = null,

    /** obiettivi formativi */
    @SerialName("obiettiviFormativi")
    val trainingObjectives: String? = null,

    /** obiettivi formativi in inglese */
    @SerialName("obiettiviFormativiEng")
    val trainingObjectivesEnglish: String? = null,

    /** prerequisiti */
    @SerialName("prerequisiti")
    val prerequisites: String? = null,

    /** prerequisiti in inglese */
    @SerialName("prerequisitiEng")
    val prerequisitesEnglish: String? = null,

    /** testi di riferimento */
    @SerialName("testiRiferimento")
    val referenceTexts: String? = null,

    /** testi di riferimento in inglese */
    @SerialName("testiRiferimentoEng")
    val referenceTextsEnglish: String? = null,

    /** id univoco che consente di individuare una attivit� didattica offerta >- id della afId proveniente da U-Gov Didattica */
    @SerialName("uGovAfId")
    val uGovTeachingActivityId: Long? = null,

    /** id univoco che consente di individuare una condivisione logistica a livello di moduli */
    @SerialName("uGovArId")
    val uGovAreaId: Long? = null,

    /** obiettivi per lo sviluppo sostenibile */
    @SerialName("obiettiviSvilSostenibileDes")
    val sustainableDevelopmentObjectivesDescription: String? = null,

    /** obiettivi per lo sviluppo sostenibile in inglese */
    @SerialName("obiettiviSvilSostenibileDesEng")
    val sustainableDevelopmentObjectivesDescriptionEnglish: String? = null,

    /** lista degli obiettivi per lo sviluppo sostenibile associati */
    @SerialName("obiettiviSvilSostenibileList")
    val sustainableDevelopmentObjectivesList: String? = null,

    @SerialName("obiettiviSvilSostenibile")
    val sustainableDevelopmentObjectives: List<Esse3SustainableDevelopmentGoals> = emptyList(),

    /** contenuti del corso in spagnolo */
    @SerialName("contenutiSpa")
    val contentsSpanish: String? = null,

    /** obiettivi formativi in spagnolo */
    @SerialName("obiettiviFormativiSpa")
    val trainingObjectivesSpanish: String? = null,

    /** prerequisiti in spagnolo */
    @SerialName("prerequisitiSpa")
    val prerequisitesSpanish: String? = null,

    /** testi di riferimento in spagnolo */
    @SerialName("testiRiferimentoSpa")
    val referenceTextsSpanish: String? = null,

    /** obiettivi per lo sviluppo sostenibile in spagnolo */
    @SerialName("obiettiviSvilSostenibileDesSpa")
    val sustainableDevelopmentObjectivesDescriptionSpanish: String? = null,

    /** contenuti del corso in francese */
    @SerialName("contenutiFra")
    val contentsFrench: String? = null,

    /** obiettivi formativi in francese */
    @SerialName("obiettiviFormativiFra")
    val trainingObjectivesFrench: String? = null,

    /** prerequisiti in francese */
    @SerialName("prerequisitiFra")
    val prerequisitesFrench: String? = null,

    /** testi di riferimento in francese */
    @SerialName("testiRiferimentoFra")
    val referenceTextsFrench: String? = null,

    /** obiettivi per lo sviluppo sostenibile in francese */
    @SerialName("obiettiviSvilSostenibileDesFra")
    val sustainableDevelopmentObjectivesDescriptionFrench: String? = null,

    /** contenuti del corso in tedesco */
    @SerialName("contenutiDeu")
    val contentsGerman: String? = null,

    /** obiettivi formativi in tedesco */
    @SerialName("obiettiviFormativiDeu")
    val trainingObjectivesGerman: String? = null,

    /** prerequisiti in tedesco */
    @SerialName("prerequisitiDeu")
    val prerequisitesGerman: String? = null,

    /** testi di riferimento in tedesco */
    @SerialName("testiRiferimentoDeu")
    val referenceTextsGerman: String? = null,

    /** obiettivi per lo sviluppo sostenibile in tedesco */
    @SerialName("obiettiviSvilSostenibileDesDeu")
    val sustainableDevelopmentObjectivesDescriptionGerman: String? = null
)

@Serializable
data class Esse3CoverageDeletable(
    /** codice che indica se la copertura è cancellabile */
    @SerialName("retCode")
    val returnCode: Int? = null,

    /** eventuale messaggio con informazioni aggiuntive */
    @SerialName("msg")
    val message: String? = null
)

@Serializable
data class Esse3ActivityLogWithSyllabus(
    @SerialName("SyllabusAD")
    val syllabusTeachingActivity: List<Esse3SyllabusActivity> = emptyList(),

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey? = null,

    @SerialName("chiaveADFisica")
    val physicalTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    /** Data inizio del periodo didattico. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataInizio")
    val startDate: String? = null,

    /** Data fine del periodo didattico. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataFine")
    val endDate: String? = null,

    /** Data di inizio compilazione questionari per la valutazione della didattica. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataIniValDid")
    val didacticEvaluationStartDate: String? = null,

    /** Data fine compilazione questionari per la valutazione della didattica. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataFinValDid")
    val didacticEvaluationEndDate: String? = null,

    /** codice del fattore di partizione effettivo */
    @SerialName("fatPartEffCod")
    val invoiceEffectivePartialCode: String? = null,

    /** descrizione del fattore di partizione effettivo */
    @SerialName("fatPartEffDes")
    val invoiceEffectivePartialDescription: String? = null,

    /** codice del dominio di partizione effettivo */
    @SerialName("domPartEffCod")
    val domicileEffectivePartialCode: String? = null,

    /** desrizione del dominio di partizione effettivo */
    @SerialName("domPartEffDes")
    val domicileEffectivePartialDescription: String? = null,

    /** codice della partizione effettiva dell'anno accademico */
    @SerialName("partEffCod")
    val effectivePartialCode: String? = null,

    /** descrizione della partizione effettiva dell'anno accademico */
    @SerialName("partEffDes")
    val effectivePartialDescription: String? = null,

    /** descrizione della partizione effettiva dell'anno accademico in inglese */
    @SerialName("partEffDesEng")
    val effectivePartialDescriptionEnglish: String? = null,

    /** id della lingua di erogazione della didattica */
    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    /** codice ISO6392 della lingua di erogazione della didattica */
    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    /** descrizione della lingua di erogazione della didattica */
    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    /** codice del tipo di didattica del corso di studio */
    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    /** descrizione del tipo di didattica del corso di studio */
    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    /** descrizione del tipo di didattica del corso di studio in inglese */
    @SerialName("tipoDidDesEng")
    val didacticTypeDescriptionEnglish: String? = null,

    /** ID chiave della sede */
    @SerialName("sedeId")
    val siteId: Long? = null,

    /** descrizione della sede */
    @SerialName("sedeDes")
    val siteDescription: String? = null,

    /** descrizione della sede in inglese */
    @SerialName("sedeDesEng")
    val siteDescriptionEnglish: String? = null,

    /** Data di ultima modifica dell'intera logistica. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataModLog")
    val logModificationDate: String? = null
)
