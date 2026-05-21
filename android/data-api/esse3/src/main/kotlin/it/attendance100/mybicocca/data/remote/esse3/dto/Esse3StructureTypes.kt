package it.attendance100.mybicocca.data.remote.esse3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3MandatoryTitles(
    /** codice titoli italiano */
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    /** descrizione titolo italiano */
    @SerialName("tipiTititDes")
    val titleTypesDescription: String? = null,

    /** livello */
    @SerialName("tipiTititLivello")
    val titleTypesLevel: Int? = null,

    /** flag riforma */
    @SerialName("rifFlg")
    val referenceFlag: Int? = null,

    /** stato richiesto */
    @SerialName("statoRichiesto")
    val requestedState: String? = null,

    /** dettaglio titolo */
    @SerialName("dettagli")
    val details: List<Esse3TitleDetail> = emptyList()
)

@Serializable
data class Esse3InternalCourseFeatures(
    /** descrizione annoAccademico */
    @SerialName("annoAccademico")
    val academicYear: String? = null,

    /** descrizione cdsId */
    @SerialName("cdsId")
    val courseOfStudyId: Int = 0,

    /** descrizione cdsCod */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione titolo */
    @SerialName("titolo")
    val title: String? = null,

    /** descrizione testoClob */
    @SerialName("testoClob")
    val textClob: String? = null,

    /** descrizione carattId */
    @SerialName("carattId")
    val characteristicId: Int? = null,

    /** descrizione tipoCarattCod */
    @SerialName("tipoCarattCod")
    val characteristicTypeCode: String? = null,

    /** descrizione tipoTestoProgDidCod */
    @SerialName("tipoTestoProgDidCod")
    val didacticProgramTextTypeCode: String? = null,

    /** descrizione ordine */
    @SerialName("ordine")
    val order: String? = null,

    /** descrizione sdrTip */
    @SerialName("sdrTip")
    val siteType: String? = null
)

@Serializable
data class Esse3InternalConsortiumCourses(
    /** descrizione cdsId */
    @SerialName("cdsId")
    val courseOfStudyId: Int = 0,

    /** descrizione ateneoId */
    @SerialName("ateneoId")
    val universityId: Int? = null,

    /** descrizione ateneoDes */
    @SerialName("ateneoDes")
    val universityDescription: String? = null,

    /** descrizione cdsAteId */
    @SerialName("cdsAteId")
    val courseOfStudyAteId: Int? = null,

    /** descrizione cdsAteneiItaDes */
    @SerialName("cdsAteneiItaDes")
    val italianUniversitiesCourseOfStudyDescription: String? = null,

    /** descrizione atestraId */
    @SerialName("atestraId")
    val foreignTestId: Int? = null,

    /** descrizione atestraDes */
    @SerialName("atestraDes")
    val foreignTestDescription: String? = null,

    /** descrizione cdsDes */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** descrizione cdsStraDes */
    @SerialName("cdsStraDes")
    val foreignCourseOfStudyDescription: String? = null
)

@Serializable
data class Esse3CourseStructures(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** chiave della struttura di riferimento */
    @SerialName("facId")
    val facultyId: Long = 0L,

    /** codice della struttura di riferimento */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione della struttura di riferimento */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** descrizione della struttura di riferimento in inglese */
    @SerialName("facDesEng")
    val facultyDescriptionEnglish: String? = null,

    /** città della facoltà */
    @SerialName("facCitta")
    val facultyCity: String? = null,

    /** indicatore struttura di default area amministrativa per il CDS */
    @SerialName("defAmmFlg")
    val adminDefinitionFlag: Int? = null,

    /** campo di storicizzazione, ulizzato per facoltà annullata per il default amministrativo */
    @SerialName("oldDefAmmFlg")
    val oldAdminDefinitionFlag: Int? = null,

    /** indicatore struttura di default area statistiche per il CDS */
    @SerialName("defStatFlg")
    val statutoryDefinitionFlag: Int? = null,

    /** indicatore struttura di default per le regole conseguimento titolo */
    @SerialName("defRegCtFlg")
    val committeeRegulationDefinitionFlag: Int? = null,

    /** campo di storicizzazione, ulizzato per facoltà annullata per il default per le regole conseguimento titolo */
    @SerialName("oldDefRegCtFlg")
    val oldCommitteeRegulationDefinitionFlag: Int? = null,

    /** indica che si tratta di una struttura di raccordo o scuola, ossia una struttura interdipartimentale il cui scopo è raccordare più dipartimenti in Ateneo */
    @SerialName("racFlg")
    val recommendationFlag: Int? = null,

    /** indica se la struttura non � pi� valida */
    @SerialName("annFlg")
    val yearFlag: Int? = null,

    /** anno di attivazione della struttura */
    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    /** anno di disattivazione della struttura */
    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    /** Codice CSA, utilizzato per mappare le nazioni durante l'allineamento docenti dal sistema CSA. */
    @SerialName("csaCod")
    val csaCode: String? = null
)

@Serializable
data class Esse3PhDSectors(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** anno di ordinamento del corso di studio */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int = 0,

    /** codice identificativo del settore scientifico disciplinare di riferimento */
    @SerialName("settCod")
    val sectorCode: String = "",

    /** descrizione del settore scientifico disciplinare di riferimento */
    @SerialName("settDes")
    val sectorDescription: String? = null,

    /** descrizione in inglese del settore scientifico disciplinare di riferimento */
    @SerialName("settDesEng")
    val sectorDescriptionEnglish: String? = null,

    /** indica se il settore è quello prevalente */
    @SerialName("prevalenteFlg")
    val prevalentFlag: Int? = null,

    /** codice identificativo dell'area del settore scientifico disciplinare di riferimento */
    @SerialName("areaCod")
    val areaCode: String? = null,

    /** descrizione dell'area del settore scientifico disciplinare di riferimento */
    @SerialName("areaDes")
    val areaDescription: String? = null,

    /** descrizione in inglese dell'area del settore scientifico disciplinare di riferimento */
    @SerialName("areaDesEng")
    val areaDescriptionEnglish: String? = null
)

@Serializable
data class Esse3DeletedStudyCourse(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** Data di ultima modifica del corso di studio e dettagli. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataModCds")
    val courseOfStudyModificationDate: String? = null
)

@Serializable
data class Esse3ConsortiumCourses(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** chiave dell'ateneo di riferimento */
    @SerialName("ateneoId")
    val universityId: Long? = null,

    /** descrizione ateneo. */
    @SerialName("ateneoDes")
    val universityDescription: String? = null,

    /** descrizione ateneo in inglese. */
    @SerialName("ateneoDesEng")
    val universityDescriptionEnglish: String? = null,

    /** chiave del corso dell'ateneo di riferimento */
    @SerialName("cdsAteId")
    val courseOfStudyAteId: Long? = null,

    /** descrizione del corso dell'ateneo di riferimento. */
    @SerialName("cdsAteneiItaDes")
    val italianUniversitiesCourseOfStudyDescription: String? = null,

    /** chiave dell'ateneo straniero di riferimento */
    @SerialName("atestraId")
    val foreignTestId: Long? = null,

    /** descrizione ateneo straniero. */
    @SerialName("atestraDes")
    val foreignTestDescription: String? = null,

    /** descrizione corso. */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** descrizione corso straniero. */
    @SerialName("cdsStraDes")
    val foreignCourseOfStudyDescription: String? = null
)

@Serializable
data class Esse3OptionalCombinations(
    /** codici combinazione */
    @SerialName("combTaDes")
    val teachingActivityCombinationDescription: String? = null,

    /** descrizione combinazione */
    @SerialName("combTaNota")
    val teachingActivityCombinationNote: String? = null,

    /** struttura titoli */
    @SerialName("tipiTitolo")
    val titleTypes: List<Esse3TitleTypes> = emptyList()
)

@Serializable
data class Esse3InternalStudyCourses(
    /** descrizione linguaCod */
    @SerialName("linguaCod")
    val languageCode: String? = null,

    /** descrizione cdsId */
    @SerialName("cdsId")
    val courseOfStudyId: Int = 0,

    /** descrizione cdsCod */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione cdsDes */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** descrizione tipoCorsoCod */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** descrizione tipoCorsoDes */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** descrizione istatCod */
    @SerialName("istatCod")
    val istatCode: String? = null,

    /** descrizione umPesoCod */
    @SerialName("umPesoCod")
    val measurementUnitWeightCode: String? = null,

    /** descrizione umPesoDes */
    @SerialName("umPesoDes")
    val measurementUnitWeightDescription: String? = null,

    /** descrizione aaAttId */
    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    /** descrizione aaDisId */
    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    /** descrizione tipoSpecCod */
    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    /** descrizione tipoTititCod */
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    /** descrizione tipoTititDes */
    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    /** descrizione normCod */
    @SerialName("normCod")
    val normCode: String? = null,

    /** descrizione claAteneoCod */
    @SerialName("claAteneoCod")
    val classUniversityCode: String? = null,

    /** descrizione claMurstCod */
    @SerialName("claMurstCod")
    val classMurstCode: String? = null,

    /** descrizione tipoAccesso */
    @SerialName("tipoAccesso")
    val accessType: String? = null,

    /** descrizione aaOrdAttivoId */
    @SerialName("aaOrdAttivoId")
    val academicYearActiveOrderId: Int? = null,

    /** descrizione ordAttivoDurataAnni */
    @SerialName("ordAttivoDurataAnni")
    val orderActiveDurationYears: Int? = null,

    /** descrizione tcDurataAnni */
    @SerialName("tcDurataAnni")
    val tcDurationYears: Int? = null,

    /** descrizione tipoCatalogoCod */
    @SerialName("tipoCatalogoCod")
    val catalogTypeCode: String? = null,

    /** descrizione tipoCicloFormCod */
    @SerialName("tipoCicloFormCod")
    val trainingCycleTypeCode: String? = null,

    /** descrizione maxPunti */
    @SerialName("maxPunti")
    val maxPoints: Int? = null,

    /** descrizione facIdAmm */
    @SerialName("facIdAmm")
    val facultyAdminId: Int? = null,

    /** descrizione facCodAmm */
    @SerialName("facCodAmm")
    val facultyAdminCode: String? = null,

    /** descrizione facIdCt */
    @SerialName("facIdCt")
    val facultyCommitteeId: Int? = null,

    /** descrizione facCtCod */
    @SerialName("facCtCod")
    val facultyCommitteeCode: String? = null,

    /** descrizione acronimo */
    @SerialName("acronimo")
    val acronym: String? = null,

    /** descrizione codExt */
    @SerialName("codExt")
    val externalCode: String? = null,

    /** descrizione urlSitoWeb */
    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    /** descrizione csaCod */
    @SerialName("csaCod")
    val csaCode: String? = null,

    /** descrizione tipoMasterCod */
    @SerialName("tipoMasterCod")
    val masterTypeCode: String? = null
)

@Serializable
data class Esse3InternalStudyCourse(
    /** descrizione cdsId */
    @SerialName("cdsId")
    val courseOfStudyId: Int = 0,

    /** descrizione cdsCod */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione cdsDes */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** descrizione tipoCorsoCod */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** descrizione tipoCorsoDes */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** descrizione istatCod */
    @SerialName("istatCod")
    val istatCode: String? = null,

    /** descrizione settFlg */
    @SerialName("settFlg")
    val sectorFlag: Int? = null,

    /** descrizione umPesoCod */
    @SerialName("umPesoCod")
    val measurementUnitWeightCode: String? = null,

    /** descrizione umPesoDes */
    @SerialName("umPesoDes")
    val measurementUnitWeightDescription: String? = null,

    /** descrizione aaAttId */
    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    /** descrizione aaDisId */
    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    /** descrizione tipoSpecCod */
    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    /** descrizione tipoTititCod */
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    /** descrizione tipoTititDes */
    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    /** descrizione webViewFlg */
    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    /** descrizione normCod */
    @SerialName("normCod")
    val normCode: String? = null,

    /** descrizione claAteneoCod */
    @SerialName("claAteneoCod")
    val classUniversityCode: String? = null,

    /** descrizione claMurstCod */
    @SerialName("claMurstCod")
    val classMurstCode: String? = null,

    /** descrizione interclaMurstCod */
    @SerialName("interclaMurstCod")
    val interclassMurstCode: String? = null,

    /** descrizione tipoAccesso */
    @SerialName("tipoAccesso")
    val accessType: String? = null,

    /** descrizione p07ClaAreeIscedCod */
    @SerialName("p07ClaAreeIscedCod")
    val p07ClassIscedAreasCode: String? = null,

    /** descrizione aaOrdAttivoId */
    @SerialName("aaOrdAttivoId")
    val academicYearActiveOrderId: Int? = null,

    /** descrizione ordAttivoDurataAnni */
    @SerialName("ordAttivoDurataAnni")
    val orderActiveDurationYears: Int? = null,

    /** descrizione tcDurataAnni */
    @SerialName("tcDurataAnni")
    val tcDurationYears: Int? = null,

    /** descrizione tipoCatalogoCod */
    @SerialName("tipoCatalogoCod")
    val catalogTypeCode: String? = null,

    /** descrizione tipoCicloFormCod */
    @SerialName("tipoCicloFormCod")
    val trainingCycleTypeCode: String? = null,

    /** descrizione maxPunti */
    @SerialName("maxPunti")
    val maxPoints: Int? = null,

    /** descrizione trasmAlmaFlg */
    @SerialName("trasmAlmaFlg")
    val almaTransmissionFlag: Int? = null,

    /** descrizione obSvilSosFlg */
    @SerialName("obSvilSosFlg")
    val sustainableDevelopmentObjectiveFlag: Int? = null,

    /** descrizione facIdAmm */
    @SerialName("facIdAmm")
    val facultyAdminId: Int? = null,

    /** descrizione facCodAmm */
    @SerialName("facCodAmm")
    val facultyAdminCode: String? = null,

    /** descrizione facIdCt */
    @SerialName("facIdCt")
    val facultyCommitteeId: Int? = null,

    /** descrizione facCtCod */
    @SerialName("facCtCod")
    val facultyCommitteeCode: String? = null,

    /** descrizione gruppoTcCod */
    @SerialName("gruppoTcCod")
    val tcGroupCode: String? = null,

    /** descrizione acronimo */
    @SerialName("acronimo")
    val acronym: String? = null,

    /** descrizione codExt */
    @SerialName("codExt")
    val externalCode: String? = null,

    /** descrizione codStatMiur */
    @SerialName("codStatMiur")
    val miurStatisticalCode: String? = null,

    /** descrizione postiStatFlg */
    @SerialName("postiStatFlg")
    val statutorySeatsFlag: Int? = null,

    /** descrizione webImmatFlg */
    @SerialName("webImmatFlg")
    val webEnrollmentFlag: Int? = null,

    /** descrizione urlSitoWeb */
    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    /** descrizione csaCod */
    @SerialName("csaCod")
    val csaCode: String? = null,

    /** descrizione tipoMasterCod */
    @SerialName("tipoMasterCod")
    val masterTypeCode: String? = null,

    /** descrizione note */
    @SerialName("note")
    val notes: String? = null,

    /** descrizione tiroTutorPsicoFlg */
    @SerialName("tiroTutorPsicoFlg")
    val psychologicalTutorInternshipFlag: Int? = null,

    /** descrizione statMiurFlg */
    @SerialName("statMiurFlg")
    val miurStatisticalFlag: Int? = null,

    /** descrizione ccRaggrCod */
    @SerialName("ccRaggrCod")
    val currentAccountGroupCode: String? = null,

    /** descrizione ccMasterFlg */
    @SerialName("ccMasterFlg")
    val masterCurrentAccountFlag: Int? = null
)

@Serializable
data class Esse3RegulationWithPaths(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** anno di ordinamento del corso di studioa */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int = 0,

    /** codice dell'ordinamento */
    @SerialName("cdsOrdCod")
    val courseOfStudyOrderCode: String? = null,

    /** descrizione dell'ordinamento */
    @SerialName("cdsOrdDes")
    val courseOfStudyOrderDescription: String? = null,

    /** descrizione dell'ordinamento in inglese */
    @SerialName("cdsOrdDesEng")
    val courseOfStudyOrderDescriptionEnglish: String? = null,

    /** descrizione dell'ordinamento per i certificati */
    @SerialName("cdsOrdDesCert")
    val courseOfStudyOrderCertificateDescription: String? = null,

    /** descrizione dell'ordinamento per i certificati in inglese */
    @SerialName("cdsOrdDesCertEng")
    val courseOfStudyOrderCertificateDescriptionEnglish: String? = null,

    /** stato dell'ordinamneto */
    @SerialName("statoCod")
    val stateCode: Esse3StateCode? = null,

    /** anno di cessazione dell'ordinamento del corso di studioa */
    @SerialName("aaOrdCessId")
    val academicYearOrderCessationId: Int? = null,

    /** valore minimo di crediti, che devono essere ottenuti per poter conseguire il titolo di studio */
    @SerialName("valoreMin")
    val minimumValue: Long? = null,

    /** numero di anni di durata del corso di studio effettiva */
    @SerialName("durataAnni")
    val durationYears: Int? = null,

    /** Durata effettiva del corso espressa in base al um (valore del campo umDurata). */
    @SerialName("durataEffettiva")
    val effectiveDuration: Int? = null,

    /** Unità di misura della durata effettiva del corso (valore del campo durataEffettiva). */
    @SerialName("umDurata")
    val measurementUnitDuration: String? = null,

    /** identificativo scuola dottorato */
    @SerialName("scuolaDottId")
    val phdSchoolId: Int? = null,

    /** descrizione della scuola di dottorato */
    @SerialName("scuolaDottDes")
    val phdSchoolDescription: String? = null,

    @SerialName("lingueDidattica")
    val teachingLanguages: List<Esse3TeachingLanguagesRegulation> = emptyList(),

    @SerialName("regolamentiDidattici")
    val didacticRegulations: List<Esse3TeachingRegulation> = emptyList(),

    @SerialName("percorsiDiStudio")
    val studyPaths: List<Esse3StudyPath> = emptyList()
)

@Serializable
data class Esse3CourseRoles(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** nome della carica */
    @SerialName("caricaNome")
    val positionName: String? = null,

    /** cognome della carica */
    @SerialName("caricaCognome")
    val positionSurname: String? = null,

    /** descrizione carica */
    @SerialName("caricaDes")
    val positionDescription: String? = null,

    /** descrizione carica in inglese */
    @SerialName("caricaDesEng")
    val positionDescriptionEnglish: String? = null,

    /** ordinamento della carica */
    @SerialName("ordine")
    val order: Long? = null,

    /** identificativo tipo di utilizzo */
    @SerialName("tipoUtilizzoId")
    val usageTypeId: Long? = null,

    /** ID address book della persona in UGOV */
    @SerialName("caricaIdAb")
    val positionAbbreviatedId: Long? = null,

    /** ID della carica */
    @SerialName("caricaId")
    val positionId: Long? = null,

    /** Matricola della carica */
    @SerialName("caricaMatricola")
    val positionStudentId: String? = null,

    /** Codice fiscale della carica */
    @SerialName("caricaCodFis")
    val positionFiscalCode: String? = null
)

@Serializable
data class Esse3AdmissionTitlesWithCode(
    /** codice del corso di studio */
    @SerialName("cdsCod")
    val courseOfStudyCode: String = "",

    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** tipologia del corso di studio */
    @SerialName("tipologiaCod")
    val typologyCode: Esse3TypologyCode,

    /** descrizione della tipologia di corso di studio */
    @SerialName("tipologiaDes")
    val typologyDescription: String? = null,

    @SerialName("titoliObbligatori")
    val mandatoryTitles: List<Esse3MandatoryTitles> = emptyList(),

    @SerialName("combinazioniOpzionali")
    val optionalCombinations: List<Esse3OptionalCombinations> = emptyList()
)

@Serializable
data class Esse3RegulationWithPhDSectors(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** anno di ordinamento del corso di studioa */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int = 0,

    /** codice dell'ordinamento */
    @SerialName("cdsOrdCod")
    val courseOfStudyOrderCode: String? = null,

    /** descrizione dell'ordinamento */
    @SerialName("cdsOrdDes")
    val courseOfStudyOrderDescription: String? = null,

    /** descrizione dell'ordinamento in inglese */
    @SerialName("cdsOrdDesEng")
    val courseOfStudyOrderDescriptionEnglish: String? = null,

    /** descrizione dell'ordinamento per i certificati */
    @SerialName("cdsOrdDesCert")
    val courseOfStudyOrderCertificateDescription: String? = null,

    /** descrizione dell'ordinamento per i certificati in inglese */
    @SerialName("cdsOrdDesCertEng")
    val courseOfStudyOrderCertificateDescriptionEnglish: String? = null,

    /** stato dell'ordinamneto */
    @SerialName("statoCod")
    val stateCode: Esse3StateCode? = null,

    /** anno di cessazione dell'ordinamento del corso di studioa */
    @SerialName("aaOrdCessId")
    val academicYearOrderCessationId: Int? = null,

    /** valore minimo di crediti, che devono essere ottenuti per poter conseguire il titolo di studio */
    @SerialName("valoreMin")
    val minimumValue: Long? = null,

    /** numero di anni di durata del corso di studio effettiva */
    @SerialName("durataAnni")
    val durationYears: Int? = null,

    /** Durata effettiva del corso espressa in base al um (valore del campo umDurata). */
    @SerialName("durataEffettiva")
    val effectiveDuration: Int? = null,

    /** Unità di misura della durata effettiva del corso (valore del campo durataEffettiva). */
    @SerialName("umDurata")
    val measurementUnitDuration: String? = null,

    /** identificativo scuola dottorato */
    @SerialName("scuolaDottId")
    val phdSchoolId: Int? = null,

    /** descrizione della scuola di dottorato */
    @SerialName("scuolaDottDes")
    val phdSchoolDescription: String? = null,

    @SerialName("lingueDidattica")
    val teachingLanguages: List<Esse3TeachingLanguagesRegulation> = emptyList(),

    @SerialName("regolamentiDidattici")
    val didacticRegulations: List<Esse3TeachingRegulation> = emptyList(),

    @SerialName("settoriDottorato")
    val phdSectors: List<Esse3PhDSectors> = emptyList()
)

@Serializable
data class Esse3StudyCourseWithStructure(
    @SerialName("struttureCorso")
    val courseStructures: List<Esse3CourseStructures> = emptyList(),

    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di studio */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** descrizione del corso di studio in inglese */
    @SerialName("cdsDesEng")
    val courseOfStudyDescriptionEnglish: String? = null,

    /** descrizione del corso di studio per i certificati */
    @SerialName("cdsDesCert")
    val courseOfStudyCertificateDescription: String? = null,

    /** descrizione del corso di studio per i certificati in inglese */
    @SerialName("cdsDesCertEng")
    val courseOfStudyCertificateDescriptionEnglish: String? = null,

    /** descrizione aggiuntiva del corso di studio per i certificati */
    @SerialName("cdsDesCertBis")
    val courseOfStudyCertificateDescriptionBis: String? = null,

    /** descrizione aggiuntiva del corso di studio per i certificati in inglese */
    @SerialName("cdsDesCertBisEng")
    val courseOfStudyCertificateDescriptionBisEnglish: String? = null,

    /** descrizione alternativa del corso di studio */
    @SerialName("cdsDesCertAlt")
    val courseOfStudyAlternativeCertificateDescription: String? = null,

    /** descrizione alternativa del corso di studio in inglese */
    @SerialName("cdsDesCertAltEng")
    val courseOfStudyAlternativeCertificateDescriptionEnglish: String? = null,

    /** chiave dell'ateneo di riferimento */
    @SerialName("ateneoId")
    val universityId: Long? = null,

    /** codice del tipo di corso di studio */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** descrizione del tipo di corso di studio */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** descrizione del tipo di corso di studio */
    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    /** codice del tipo di didattica del corso di studio */
    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    /** descrizione del tipo di didattica del corso di studio */
    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    /** descrizione del tipo di didattica del corso di studio in inglese */
    @SerialName("tipoDidDesEng")
    val didacticTypeDescriptionEnglish: String? = null,

    /** codice ISTAT che distingue il corso di studio */
    @SerialName("istatCod")
    val istatCode: String? = null,

    /** indica se il CDS � stato istituito in accordo con la riforma e quindi pu� essere associato ad una classe di laurea. 0 = NUOVO abilita associazione con classe 1 = VECCHIO disabilita associazione con classe */
    @SerialName("rifFlg")
    val referenceFlag: Int? = null,

    /** Codice Off. F. Ministeriale del Corso di Studio dell'Ateneo. */
    @SerialName("codicione")
    val bigCode: String? = null,

    /** codice che indica la unit� di misura del Corso di studio. In caso di corso post riforma viene sempre valorizzato a Crediti */
    @SerialName("umPesoCod")
    val measurementUnitWeightCode: String? = null,

    /** descrizione del codice che indica la unit� di misura */
    @SerialName("umPesoDes")
    val measurementUnitWeightDescription: String? = null,

    /** anno di attivazione del corso */
    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    /** anno di disattivazione del corso */
    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    /** codice valudo solo per le scuole di specializzazione (tipo_corso_cod = S1) e indica il Tipo di specializzazione */
    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    /** descrizione del tipo di specializzazione del corso di studio */
    @SerialName("tipoSpecDes")
    val specializationTypeDescription: String? = null,

    /** codice del tipo di titolo del corso di studio */
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    /** descrizione del tipo di titolo del corso di studio */
    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    /** descrizione del tipo di titolo del corso di studio in inglese */
    @SerialName("tipoTititDesEng")
    val titleTypeDescriptionEnglish: String? = null,

    /** URL del sito web del corso di studio */
    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    /** indica se del corso di studio deve essere visibile nel CC */
    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    /** codice del della normativa del corso di studio */
    @SerialName("normCod")
    val normCode: String? = null,

    /** descrizione della normativa del corso di studio */
    @SerialName("normDes")
    val normDescription: String? = null,

    /** descrizione della normativa del corso di studio in inglese */
    @SerialName("normDesEng")
    val normDescriptionEnglish: String? = null,

    /** codice della classe MIUR del corso di studio */
    @SerialName("claCod")
    val classCode: String? = null,

    /** descrizione della classe MIUR del corso di studio */
    @SerialName("claDes")
    val classDescription: String? = null,

    /** descrizione della classe MIUR del corso di studio in inglese */
    @SerialName("claDesEng")
    val classDescriptionEnglish: String? = null,

    /** codice della classe interclasse del corso di studio */
    @SerialName("interClaCod")
    val interclassCode: String? = null,

    /** descrizione della classe interclasse del corso di studio */
    @SerialName("interClaDes")
    val interclassDescription: String? = null,

    /** descrizione della classe interclasse del corso di studio in inglese */
    @SerialName("interClaDesEng")
    val interclassDescriptionEnglish: String? = null,

    /** codice del tipo di accesso al corso di studio */
    @SerialName("tipoAccesso")
    val accessType: String? = null,

    /** Data di ultima modifica del corso di studio e dettagli. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataModCds")
    val courseOfStudyModificationDate: String? = null,

    /** indica se il CDS è associato a una struttura di ateneo */
    @SerialName("sdrFlg")
    val siteFlag: Int? = null,

    /** Codice area ISCED */
    @SerialName("iscedCod")
    val iscedCode: String? = null,

    /** Descrizione area ISCED */
    @SerialName("iscedDes")
    val iscedDescription: String? = null,

    /** flag che indica se l'ordinamento del corso è attivo */
    @SerialName("ordAttivoFlg")
    val orderActiveFlag: Int? = null,

    /** flag che indica se l'ordinamento del corso è abilitato all'immatricolazione */
    @SerialName("ordAbilImmaFlg")
    val orderEnableEnrollmentFlag: Int? = null,

    /** Codice tipi corso catalogo */
    @SerialName("tipoCatalogoCod")
    val catalogTypeCode: String? = null,

    /** Descrizione tipi corso catalogo */
    @SerialName("tipoCatalogoDes")
    val catalogTypeDescription: String? = null,

    /** Codice tipi ciclo formativo */
    @SerialName("tipoCicloFormCod")
    val trainingCycleTypeCode: String? = null,

    /** Descrizione tipi ciclo formativo */
    @SerialName("tipoCicloFormDes")
    val trainingCycleTypeDescription: String? = null,

    /** flag che indica se allo stato attuale il corso risulta aperto all'immatricolazione. */
    @SerialName("abilImmaWeb")
    val webEnrollmentAuthorization: Int? = null,

    /** Codice del raggruppamento nei raggruppamenti di corsi di studio visualizzati nel Course Catalogue */
    @SerialName("ccRaggrCod")
    val currentAccountGroupCode: String? = null,

    /** Flag di corso master nei raggruppamenti di corsi di studio visualizzati nel Course Catalogue */
    @SerialName("ccMasterFlg")
    val masterCurrentAccountFlag: Int? = null,

    /** URL informativo web sul corso di studio */
    @SerialName("urlInfoWeb")
    val webInfoUrl: String? = null,

    /** ID Facoltà/Dipartimento di default in ambito area didattica */
    @SerialName("facIdDef")
    val facultyDefaultId: Long? = null,

    /** Punteggio Massimo (base di valutazione, differenzia se il voto è espresso in centesimi, settantesimi o 110-esimi) */
    @SerialName("maxPunti")
    val maxPoints: Int? = null,

    /** indica se i laureati in questo corso devono essere trasmessi ad Alma Laurea (1=trasmessi 0=no). */
    @SerialName("trasmAlmaFlg")
    val almaTransmissionFlag: Int? = null,

    /** Flag di visibilità dell'Agenda 2030 per lo sviluppo sostenibile. */
    @SerialName("obSvilSosFlg")
    val sustainableDevelopmentObjectiveFlag: Int? = null,

    /** Anno di ordimanento attivo. */
    @SerialName("aaOrdAttivo")
    val academicYearActiveOrder: Int? = null,

    /** durata in anni nell'anno di ordimanento attivo. */
    @SerialName("durataAnniOrdAttivo")
    val durationYearsActiveOrder: Int? = null,

    /** note */
    @SerialName("note")
    val notes: String? = null,

    /** Codice raggruppamento di tipi corso. */
    @SerialName("gruppoTcCod")
    val tcGroupCode: String? = null,

    /** Descrizione raggruppamento di tipi corso. */
    @SerialName("gruppoTcDes")
    val tcGroupDescription: String? = null
)

@Serializable
data class Esse3CourseCohort(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** anno di coorte per cui è presente il corso di studio */
    @SerialName("aaRegId")
    val academicYearRegulationId: Int = 0
)

@Serializable
data class Esse3Structures(
    /** descrizione aaAttId */
    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    /** descrizione aaDisId */
    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    /** descrizione facId */
    @SerialName("facId")
    val facultyId: Int? = null,

    /** descrizione cdsId */
    @SerialName("cdsId")
    val courseOfStudyId: Int = 0,

    /** descrizione defRegCtFlg */
    @SerialName("defRegCtFlg")
    val committeeRegulationDefinitionFlag: Int? = null,

    /** descrizione defAmmFlg */
    @SerialName("defAmmFlg")
    val adminDefinitionFlag: Int? = null,

    /** descrizione defStatFlg */
    @SerialName("defStatFlg")
    val statutoryDefinitionFlag: Int? = null,

    /** descrizione aaIniVal */
    @SerialName("aaIniVal")
    val academicYearStartValidity: String? = null,

    /** descrizione aaFineVal */
    @SerialName("aaFineVal")
    val academicYearEndValidity: String? = null,

    /** descrizione annFlg */
    @SerialName("annFlg")
    val yearFlag: Int? = null,

    /** descrizione citta */
    @SerialName("citta")
    val city: String? = null,

    /** descrizione cod */
    @SerialName("cod")
    val code: String? = null,

    /** descrizione des */
    @SerialName("des")
    val description: String? = null,

    /** descrizione oldDefAmmFlg */
    @SerialName("oldDefAmmFlg")
    val oldAdminDefinitionFlag: Int? = null,

    /** descrizione oldDefRegCtFlg */
    @SerialName("oldDefRegCtFlg")
    val oldCommitteeRegulationDefinitionFlag: Int? = null,

    /** descrizione oldDefStatFlg */
    @SerialName("oldDefStatFlg")
    val oldStatutoryDefinitionFlag: Int? = null,

    /** descrizione racFlg */
    @SerialName("racFlg")
    val recommendationFlag: Int? = null,

    /** descrizione csaCod */
    @SerialName("csaCod")
    val csaCode: String? = null
)

@Serializable
data class Esse3Paths(
    /** descrizione cdsId */
    @SerialName("cdsId")
    val courseOfStudyId: Int = 0,

    /** descrizione aaOrdId */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int = 0,

    /** descrizione pdsId */
    @SerialName("pdsId")
    val studyPlanId: Int? = null,

    /** descrizione pdsordId */
    @SerialName("pdsordId")
    val studyPlanOrderId: Int? = null,

    /** descrizione pdsCod */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** descrizione pdsDes */
    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    /** descrizione statoCod */
    @SerialName("statoCod")
    val stateCode: String? = null,

    /** descrizione webViewFlg */
    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    /** descrizione acSceltaOri */
    @SerialName("acSceltaOri")
    val originalChoiceActivity: String? = null,

    /** descrizione webImmatFlg */
    @SerialName("webImmatFlg")
    val webEnrollmentFlag: Int? = null,

    /** descrizione webScePdsFlg */
    @SerialName("webScePdsFlg")
    val webStudyPlanChoiceFlag: Int? = null,

    /** Valore minimo di crediti o annualità, che devono essere ottenuti per poter conseguire il titolo di studio. Il valore deve essere uguale o maggiore del valore minimo dello Ordinamento di CDS (campo P06_CDSORD.VALORE_MIN). */
    @SerialName("valoreMin")
    val minimumValue: Int? = null
)

@Serializable
data class Esse3EnrolledPerCoursePerYear(
    /** anno di corso */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** codice tipo iscrizion */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    /** descrizione tipo iscrizion. */
    @SerialName("tipoIscrDes")
    val enrollmentTypeDescription: String? = null,

    /** numero di iscritti */
    @SerialName("numIscritti")
    val enrolledNumber: Int? = null
)

@Serializable
data class Esse3StudyCourseWithDetails(
    /** Descrizione raggruppamento di tipi corso. */
    @SerialName("gruppoTcDes")
    val tcGroupDescription: String? = null,

    /** Codice raggruppamento di tipi corso. */
    @SerialName("gruppoTcCod")
    val tcGroupCode: String? = null,

    /** note */
    @SerialName("note")
    val notes: String? = null,

    /** durata in anni nell'anno di ordimanento attivo. */
    @SerialName("durataAnniOrdAttivo")
    val durationYearsActiveOrder: Int? = null,

    /** Anno di ordimanento attivo. */
    @SerialName("aaOrdAttivo")
    val academicYearActiveOrder: Int? = null,

    /** Flag di visibilità dell'Agenda 2030 per lo sviluppo sostenibile. */
    @SerialName("obSvilSosFlg")
    val sustainableDevelopmentObjectiveFlag: Int? = null,

    /** indica se i laureati in questo corso devono essere trasmessi ad Alma Laurea (1=trasmessi 0=no). */
    @SerialName("trasmAlmaFlg")
    val almaTransmissionFlag: Int? = null,

    /** Punteggio Massimo (base di valutazione, differenzia se il voto è espresso in centesimi, settantesimi o 110-esimi) */
    @SerialName("maxPunti")
    val maxPoints: Int? = null,

    /** ID Facoltà/Dipartimento di default in ambito area didattica */
    @SerialName("facIdDef")
    val facultyDefaultId: Long? = null,

    /** URL informativo web sul corso di studio */
    @SerialName("urlInfoWeb")
    val webInfoUrl: String? = null,

    /** Flag di corso master nei raggruppamenti di corsi di studio visualizzati nel Course Catalogue */
    @SerialName("ccMasterFlg")
    val masterCurrentAccountFlag: Int? = null,

    /** Codice del raggruppamento nei raggruppamenti di corsi di studio visualizzati nel Course Catalogue */
    @SerialName("ccRaggrCod")
    val currentAccountGroupCode: String? = null,

    /** flag che indica se allo stato attuale il corso risulta aperto all'immatricolazione. */
    @SerialName("abilImmaWeb")
    val webEnrollmentAuthorization: Int? = null,

    /** Descrizione tipi ciclo formativo */
    @SerialName("tipoCicloFormDes")
    val trainingCycleTypeDescription: String? = null,

    /** Codice tipi ciclo formativo */
    @SerialName("tipoCicloFormCod")
    val trainingCycleTypeCode: String? = null,

    /** Descrizione tipi corso catalogo */
    @SerialName("tipoCatalogoDes")
    val catalogTypeDescription: String? = null,

    /** Codice tipi corso catalogo */
    @SerialName("tipoCatalogoCod")
    val catalogTypeCode: String? = null,

    /** flag che indica se l'ordinamento del corso è abilitato all'immatricolazione */
    @SerialName("ordAbilImmaFlg")
    val orderEnableEnrollmentFlag: Int? = null,

    /** flag che indica se l'ordinamento del corso è attivo */
    @SerialName("ordAttivoFlg")
    val orderActiveFlag: Int? = null,

    /** Descrizione area ISCED */
    @SerialName("iscedDes")
    val iscedDescription: String? = null,

    /** Codice area ISCED */
    @SerialName("iscedCod")
    val iscedCode: String? = null,

    /** indica se il CDS è associato a una struttura di ateneo */
    @SerialName("sdrFlg")
    val siteFlag: Int? = null,

    /** Data di ultima modifica del corso di studio e dettagli. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataModCds")
    val courseOfStudyModificationDate: String? = null,

    /** codice del tipo di accesso al corso di studio */
    @SerialName("tipoAccesso")
    val accessType: String? = null,

    /** descrizione della classe interclasse del corso di studio in inglese */
    @SerialName("interClaDesEng")
    val interclassDescriptionEnglish: String? = null,

    /** descrizione della classe interclasse del corso di studio */
    @SerialName("interClaDes")
    val interclassDescription: String? = null,

    /** codice della classe interclasse del corso di studio */
    @SerialName("interClaCod")
    val interclassCode: String? = null,

    /** descrizione della classe MIUR del corso di studio in inglese */
    @SerialName("claDesEng")
    val classDescriptionEnglish: String? = null,

    /** descrizione della classe MIUR del corso di studio */
    @SerialName("claDes")
    val classDescription: String? = null,

    /** codice della classe MIUR del corso di studio */
    @SerialName("claCod")
    val classCode: String? = null,

    /** descrizione della normativa del corso di studio in inglese */
    @SerialName("normDesEng")
    val normDescriptionEnglish: String? = null,

    /** descrizione della normativa del corso di studio */
    @SerialName("normDes")
    val normDescription: String? = null,

    /** codice del della normativa del corso di studio */
    @SerialName("normCod")
    val normCode: String? = null,

    /** indica se del corso di studio deve essere visibile nel CC */
    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    /** URL del sito web del corso di studio */
    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    /** descrizione del tipo di titolo del corso di studio in inglese */
    @SerialName("tipoTititDesEng")
    val titleTypeDescriptionEnglish: String? = null,

    /** descrizione del tipo di titolo del corso di studio */
    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    /** codice del tipo di titolo del corso di studio */
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    /** descrizione del tipo di specializzazione del corso di studio */
    @SerialName("tipoSpecDes")
    val specializationTypeDescription: String? = null,

    /** codice valudo solo per le scuole di specializzazione (tipo_corso_cod = S1) e indica il Tipo di specializzazione */
    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    /** anno di disattivazione del corso */
    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    /** anno di attivazione del corso */
    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    /** descrizione del codice che indica la unit� di misura */
    @SerialName("umPesoDes")
    val measurementUnitWeightDescription: String? = null,

    /** codice che indica la unit� di misura del Corso di studio. In caso di corso post riforma viene sempre valorizzato a Crediti */
    @SerialName("umPesoCod")
    val measurementUnitWeightCode: String? = null,

    /** Codice Off. F. Ministeriale del Corso di Studio dell'Ateneo. */
    @SerialName("codicione")
    val bigCode: String? = null,

    /** indica se il CDS � stato istituito in accordo con la riforma e quindi pu� essere associato ad una classe di laurea. 0 = NUOVO abilita associazione con classe 1 = VECCHIO disabilita associazione con classe */
    @SerialName("rifFlg")
    val referenceFlag: Int? = null,

    /** codice ISTAT che distingue il corso di studio */
    @SerialName("istatCod")
    val istatCode: String? = null,

    /** descrizione del tipo di didattica del corso di studio in inglese */
    @SerialName("tipoDidDesEng")
    val didacticTypeDescriptionEnglish: String? = null,

    /** descrizione del tipo di didattica del corso di studio */
    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    /** codice del tipo di didattica del corso di studio */
    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    /** descrizione del tipo di corso di studio */
    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    /** descrizione del tipo di corso di studio */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** codice del tipo di corso di studio */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** chiave dell'ateneo di riferimento */
    @SerialName("ateneoId")
    val universityId: Long? = null,

    /** descrizione alternativa del corso di studio in inglese */
    @SerialName("cdsDesCertAltEng")
    val courseOfStudyAlternativeCertificateDescriptionEnglish: String? = null,

    /** descrizione alternativa del corso di studio */
    @SerialName("cdsDesCertAlt")
    val courseOfStudyAlternativeCertificateDescription: String? = null,

    /** descrizione aggiuntiva del corso di studio per i certificati in inglese */
    @SerialName("cdsDesCertBisEng")
    val courseOfStudyCertificateDescriptionBisEnglish: String? = null,

    /** descrizione aggiuntiva del corso di studio per i certificati */
    @SerialName("cdsDesCertBis")
    val courseOfStudyCertificateDescriptionBis: String? = null,

    /** descrizione del corso di studio per i certificati in inglese */
    @SerialName("cdsDesCertEng")
    val courseOfStudyCertificateDescriptionEnglish: String? = null,

    /** descrizione del corso di studio per i certificati */
    @SerialName("cdsDesCert")
    val courseOfStudyCertificateDescription: String? = null,

    /** descrizione del corso di studio in inglese */
    @SerialName("cdsDesEng")
    val courseOfStudyDescriptionEnglish: String? = null,

    /** descrizione del corso di studio */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** codice del corso di studio */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("ordinamentiConPercorsi")
    val studyOrdersWithPaths: List<Esse3RegulationWithPaths> = emptyList(),

    @SerialName("sediCorso")
    val courseSites: List<Esse3CourseLocations> = emptyList(),

    @SerialName("struttureCorso")
    val courseStructures: List<Esse3CourseStructures> = emptyList(),

    @SerialName("caricheCorso")
    val coursePositions: List<Esse3CourseRoles> = emptyList(),

    @SerialName("caratteristicheCorso")
    val courseCharacteristics: List<Esse3CourseFeatures> = emptyList(),

    @SerialName("scadenzeCorso")
    val courseDeadlines: List<Esse3CourseSessionDeadlines> = emptyList(),

    @SerialName("tasseCorso")
    val courseTaxes: List<Esse3CourseTuition> = emptyList(),

    @SerialName("titoliAccesso")
    val accessTitles: List<Esse3AccessTitles> = emptyList(),

    @SerialName("periodiCorso")
    val coursePeriods: List<Esse3CoursePeriods> = emptyList(),

    @SerialName("corsiConsorziati")
    val consortiumCourses: List<Esse3ConsortiumCourses> = emptyList(),

    @SerialName("coortiCorso")
    val courseCohorts: List<Esse3CourseCohort> = emptyList(),

    @SerialName("tipiDidattica")
    val teachingTypes: List<Esse3TeachingTypes> = emptyList(),

    @SerialName("iscrittiCdsPerAnno")
    val enrolledCourseOfStudyPerYear: List<Esse3EnrolledPerCoursePerYear> = emptyList(),

    @SerialName("listaConc")
    val competitionList: List<Esse3CompetitionCourseList> = emptyList()
)

@Serializable
data class Esse3RegulationDetail(
    /** Voto minimo di esame, ovvero il punteggio minimo per essere valido un esame. Per i corsi post riforma il valore è fisso a 18, infatti viene stabilito dal decreto 509/99. Per i corsi definiti prima della Riforma il valore è variabile e deve essere valorizzato. */
    @SerialName("minPuntiEsa")
    val minGraduationPoints: Int? = null,

    /** Base voto esami, ovvero il punteggio massimo di un singolo esame. Per i corsi post riforma il valore è fisso a 30, infatti viene stabilito dal decreto 509/99. Per i corsi definiti prima della Riforma il valore è variabile e deve essere valorizzato. */
    @SerialName("maxPuntiEsa")
    val maxGraduationPoints: Int? = null,

    /** Indicatore utilizzato in fase di verbalizzazione, consente la generazione automatica delle attività didattiche nel libretto 0 - generazione non abiltata 1 - generazione abilitata 2 - generazione abilitata solo per le attività didattiche presenti nel piano ma non nel libretto */
    @SerialName("genAdVerbFlg")
    val generateTeachingActivityVerbFlag: Int? = null,

    /** Indicatore utilizzato durante attuazione piani se = 1 inserisce nel libretto le atttività fuori piano come sovranumerarie. (Valido solo per le AD con stato S) */
    @SerialName("autoSovrSFlg")
    val autoOverrideSFlag: Int? = null,

    /** Indicatore utilizzato durante attuazione piani se = 1 inserisce nel libretto le atttività fuori piano come sovranumerarie. (Valido solo per le AD con stato F) */
    @SerialName("autoSovrFFlg")
    val autoOverrideFFlag: Int? = null,

    /** Indica se la frequenza è obbligatoria. */
    @SerialName("freqObblFlg")
    val mandatoryAttendanceFlag: Int? = null,

    /** Indicatore utilizzato durante inserimento attività nel libretto e attuazione del piano: il sistema mette in automatico a Frequentato le attività del libretto, ovvero P11_AD_SCE.STA_SCE_COD = 'F', se si verificano le seguenti condizioni: anno di corso attività del piano/libretto =anno di corso ultima iscrizione attiva dello studente (P11_AD_PIANI.ANNO_CORSO = P04_ISCR_ANN.ANNO_CORSO) */
    @SerialName("autoFreqFlg")
    val autoAttendanceFlag: Int? = null,

    /** Indica l'anno di corso in cui deve essere effettuata la scelta di un PDS diverso da quello comune (PDS_ID = 9999). Valorizzato se ordinamento suddiviso IN percorsi (flag PDS_FLG = 1), IN tal caso deve essere > 1 e < durata anni del corso */
    @SerialName("acScelta")
    val choiceActivity: Int? = null,

    /** Indica se l´ordinamento è suddiviso in percorsi, tabella P06_PDSORD. Se previsto solo il percorso comune (PDS_ID=9999) allora PDS_FLG = 0 */
    @SerialName("pdsFlg")
    val studyPlanFlag: Int? = null,

    /** Codice utilizzato per comunicazioni sistema esterno */
    @SerialName("cdsExt")
    val courseOfStudyExternal: String? = null,

    /** Codice del RAD presentato in Offerta Formativa (un RAD rappresenta l'istituzione di un corso di studio o la creazione di un nuovo ordinamento). */
    @SerialName("cdsRad")
    val courseOfStudyRoot: String? = null,

    /** Identificativo univoco degli ordinamenti. Viene utilizzato nella struttura didattica responsabile (tabella P06_SDR). */
    @SerialName("cdsordId")
    val courseOfStudyOrderId: Int? = null,

    /** Unità di misura della durata effettiva del corso (valore del campo DURATA_EFFETTIVA). Sono ammessi i valori: A=anni, M=mesi, G=giorni. Viene proposto il corrispondente valore dei tipi corso (tipi_corso.um_durata). Per i tipi corso L2, LS, LC5 e LC6 il valore è sempre A, come stabilito dal decreto 509/99. */
    @SerialName("umDurata")
    val measurementUnitDuration: String? = null,

    /** Durata effettiva del corso espressa in base all¿UM (campo UM_DURATA). IN base al valore inserito IN questo campo viene valorizzato il campo DURATA_ANNI, il quale indica il numero di anni (di corso) di durata del corso. Viene proposto il corrispondente valore dei tipi corso (TIPI_CORSO.durata_effettiva). Per i tipi corso L2, LS, LC5 e LC6 il valore è fisso, ovvero stabilito dal descreto 509/99. */
    @SerialName("durataEffettiva")
    val effectiveDuration: Int? = null,

    /** descrizione scuolaDottDurata */
    @SerialName("scuolaDottDurata")
    val phdSchoolDuration: String? = null,

    /** descrizione scuolaDottDurataEffettiva */
    @SerialName("scuolaDottDurataEffettiva")
    val phdSchoolEffectiveDuration: String? = null,

    /** anno accademico fine validità scuola dottorato */
    @SerialName("scuolaDottAaFineVal")
    val phdSchoolAcademicYearEndValidity: Int? = null,

    /** anno accademico inizio validità scuola dottorato */
    @SerialName("scuolaDottAaIniVal")
    val phdSchoolAcademicYearStartValidity: Int? = null,

    /** sede */
    @SerialName("scuolaDottSedeId")
    val phdSchoolSiteId: Int? = null,

    /** note scuola dottorato */
    @SerialName("scuolaDottNote")
    val phdSchoolNotes: String? = null,

    /** url sito scuola dottorato */
    @SerialName("scuolaDottUrlSitoWeb")
    val phdSchoolWebsiteUrl: String? = null,

    /** email scuola dottorato */
    @SerialName("scuolaDottEmail")
    val phdSchoolEmail: String? = null,

    /** fax scuola dottorato */
    @SerialName("scuolaDottFax")
    val phdSchoolFax: String? = null,

    /** telefono scuola dottorato */
    @SerialName("scuolaDottTel")
    val phdSchoolPhone: String? = null,

    /** id nazione scuola dottorato */
    @SerialName("scuolaDottNazioneId")
    val phdSchoolNationId: Int? = null,

    /** id comune scuola dottorato */
    @SerialName("scuolaDottComuneId")
    val phdSchoolMunicipalityId: Int? = null,

    /** via indirizzo scuola dottorato */
    @SerialName("scuolaDottVia")
    val phdSchoolStreet: String? = null,

    /** cap scuola dottorato */
    @SerialName("scuolaDottCap")
    val phdSchoolPostalCode: String? = null,

    /** descrizione scuola dottorato */
    @SerialName("scuolaDottDes")
    val phdSchoolDescription: String? = null,

    /** id scuola dottorato */
    @SerialName("scuolaDottId")
    val phdSchoolId: Int? = null,

    /** descrizione immaOrdCh */
    @SerialName("immaOrdCh")
    val closedEnrollmentOrder: String? = null,

    /** descrizione durataAnni */
    @SerialName("durataAnni")
    val durationYears: Int? = null,

    /** descrizione valoreMin */
    @SerialName("valoreMin")
    val minimumValue: String? = null,

    /** descrizione aaOrdCessId */
    @SerialName("aaOrdCessId")
    val academicYearOrderCessationId: Int? = null,

    /** descrizione statoCod */
    @SerialName("statoCod")
    val stateCode: String? = null,

    /** descrizione cdsOrdDes */
    @SerialName("cdsOrdDes")
    val courseOfStudyOrderDescription: String? = null,

    /** descrizione cdsOrdCod */
    @SerialName("cdsOrdCod")
    val courseOfStudyOrderCode: String? = null,

    /** descrizione aaOrdId */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int = 0,

    /** descrizione cdsId */
    @SerialName("cdsId")
    val courseOfStudyId: Int = 0,

    /** descrizione linguaCod */
    @SerialName("linguaCod")
    val languageCode: String? = null
)

@Serializable
data class Esse3ConsortiumUniversity(
    /** ID della struttura dell'ateneo consorziato */
    @SerialName("sdrId")
    val siteId: Long = 0L,

    /** chiave del ateneo */
    @SerialName("ateneoId")
    val universityId: Long? = null,

    /** codice del ateneo */
    @SerialName("ateneoCod")
    val universityCode: String? = null,

    /** descrizione ateneo. */
    @SerialName("ateneoDes")
    val universityDescription: String? = null,

    /** indica se ateno è straniero */
    @SerialName("atestraFlg")
    val foreignTestFlag: Int? = null
)

@Serializable
data class Esse3CompetitionCourseList(
    /** Identificativo cds */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** Codice della tipologia del corso */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** Identificativo anno */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Descrizione del concorso */
    @SerialName("concorsoDes")
    val competitionDescription: String? = null,

    /** Descrizione del concorso */
    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    /** Descrizione della tipologia del concorso */
    @SerialName("tipoTestDes")
    val testTypeDescription: String? = null,

    /** Data d''inizio del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataIniAmmWeb")
    val webAdminStartDate: String? = null,

    /** Data di fine del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataFinAmmWeb")
    val webAdminEndDate: String? = null,

    /** Data scadenza della posizione utile (Ammesso) in graduatoria ai fini della preimmatricolazione */
    @SerialName("dataScad")
    val deadline: String? = null,

    /** Data d''inizio del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataScadPreim")
    val preEnrollmentDeadline: String? = null
)

@Serializable
data class Esse3CourseLocations(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** id univoco ateneo. */
    @SerialName("ateneoId")
    val universityId: Int? = null,

    /** ID chiave della sede */
    @SerialName("sedeId")
    val siteId: Long = 0L,

    /** descrizione della sede */
    @SerialName("sedeDes")
    val siteDescription: String? = null,

    /** descrizione della sede in inglese */
    @SerialName("sedeDesEng")
    val siteDescriptionEnglish: String? = null,

    /** indicatore sede di default area didattica per il CDS */
    @SerialName("defDidFlg")
    val didacticDefinitionFlag: Int? = null,

    /** codice Miur che identifica una sede utilizzato nelle statistiche ministeriali (in particolare nelle statistiche relative al post-laurea) */
    @SerialName("codStatMiur")
    val miurStatisticalCode: String? = null,

    /** Data in cui inizia a valere una sede per un certo corso di studio. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    /** Data di fine validità di una sede per un certo corso di studio. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null
)

@Serializable
data class Esse3CourseTypes(
    /** codice del tipo corso */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String = "",

    /** descrizione dell'area disciplinare */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** descrizione dell'area disciplinare in inglese */
    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    /** numero di anni di durata del corso di studio effettivo */
    @SerialName("durataAnni")
    val durationYears: Int? = null,

    /** Unità di misura della durata */
    @SerialName("umDurata")
    val measurementUnitDuration: String? = null,

    /** Numero di anni di durata del corso di studio effettivo */
    @SerialName("durataEffettiva")
    val effectiveDuration: Int? = null,

    /** numero minimo di crediti, che devono essere ottenuti per poter conseguire il titolo di studio effettivo */
    @SerialName("valoreMin")
    val minimumValue: Int? = null,

    /** indica se il CDS è stato istituito prima o dopo (e quindi in accordo) con la riforma */
    @SerialName("rifFlg")
    val referenceFlag: Int? = null,

    /** indica qual e il livello del Corso di Studio. */
    @SerialName("livello")
    val level: Int? = null,

    /** Indica se il tipo corso rappresenta un Dottorato di Ricerca, ossia un titolo accademico italiano post lauream, corrispondente al 3° ciclo dell'istruzione superiore in molti paesi del mondo. È stato introdotto nel sistema universitario italiano nel 1980 e rappresenta il più alto grado di istruzione universitaria, come dettato dal Processo di Bologna. */
    @SerialName("dottoratoFlg")
    val phdFlag: Int? = null,

    /** Indica se il tipo corso rappresenta una scuola di specializzazione, ossia un titolo accademico italiano post lauream a carattere professionalizzante, che ha l’obiettivo di fornire conoscenze e abilità per lo svolgimento di funzioni altamente qualificate, richieste per l’esercizio di particolari attività professionali, esclusivamente in applicazione di direttive europee o di specifiche norme di legge. */
    @SerialName("scuolaSpecFlg")
    val specializationSchoolFlag: Int? = null,

    /** Indica se per questo tipo corso si applica il processo di conseguimento titolo con relativa domanda */
    @SerialName("domCtFlg")
    val domicileCommitteeFlag: Int? = null,

    /** codice raggruppamento tipo corso */
    @SerialName("gruppoTcCod")
    val tcGroupCode: String? = null,

    /** descrizione raggruppamento tipo corso */
    @SerialName("gruppoTcDes")
    val tcGroupDescription: String? = null
)

@Serializable
data class Esse3CourseCharacteristics(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** ID della caratteristica */
    @SerialName("carattId")
    val characteristicId: Long? = null,

    /** codice della tipologia di caratteristica */
    @SerialName("tipoCarattCod")
    val characteristicTypeCode: String? = null,

    /** ordinamento della carica */
    @SerialName("ordine")
    val order: Long? = null,

    /** anno della caratteristica */
    @SerialName("annoAccademico")
    val academicYear: Int? = null,

    /** Codice tipo di struttura didattica responsabile */
    @SerialName("sdrTip")
    val siteType: String? = null,

    /** codice del tipo testo utilizzato nel sistema di programmazione didattica */
    @SerialName("tipoTestoProgDidCod")
    val didacticProgramTextTypeCode: String? = null,

    /** titolo della caratteristica */
    @SerialName("titolo")
    val title: String? = null,

    /** titolo della caratteristica in inglese */
    @SerialName("titoloEng")
    val titleEnglish: String? = null,

    /** testo della caratteristica */
    @SerialName("testo")
    val text: String? = null,

    /** testo della caratteristica in inglese */
    @SerialName("testoEng")
    val textEnglish: String? = null
)

@Serializable
data class Esse3Regulations(
    /** descrizione linguaCod */
    @SerialName("linguaCod")
    val languageCode: String? = null,

    /** descrizione cdsId */
    @SerialName("cdsId")
    val courseOfStudyId: Int = 0,

    /** descrizione aaOrdId */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    /** descrizione cdsOrdCod */
    @SerialName("cdsOrdCod")
    val courseOfStudyOrderCode: String? = null,

    /** descrizione cdsOrdDes */
    @SerialName("cdsOrdDes")
    val courseOfStudyOrderDescription: String? = null,

    /** descrizione statoCod */
    @SerialName("statoCod")
    val stateCode: String? = null,

    /** descrizione aaOrdCessId */
    @SerialName("aaOrdCessId")
    val academicYearOrderCessationId: Int? = null,

    /** descrizione valoreMin */
    @SerialName("valoreMin")
    val minimumValue: String? = null,

    /** descrizione durataAnni */
    @SerialName("durataAnni")
    val durationYears: Int? = null,

    /** descrizione immaOrdCh */
    @SerialName("immaOrdCh")
    val closedEnrollmentOrder: String? = null,

    /** id scuola dottorato */
    @SerialName("scuolaDottId")
    val phdSchoolId: Int? = null,

    /** descrizione scuola dottorato */
    @SerialName("scuolaDottDes")
    val phdSchoolDescription: String? = null,

    /** cap scuola dottorato */
    @SerialName("scuolaDottCap")
    val phdSchoolPostalCode: String? = null,

    /** via indirizzo scuola dottorato */
    @SerialName("scuolaDottVia")
    val phdSchoolStreet: String? = null,

    /** id comune scuola dottorato */
    @SerialName("scuolaDottComuneId")
    val phdSchoolMunicipalityId: Int? = null,

    /** id nazione scuola dottorato */
    @SerialName("scuolaDottNazioneId")
    val phdSchoolNationId: Int? = null,

    /** telefono scuola dottorato */
    @SerialName("scuolaDottTel")
    val phdSchoolPhone: String? = null,

    /** fax scuola dottorato */
    @SerialName("scuolaDottFax")
    val phdSchoolFax: String? = null,

    /** email scuola dottorato */
    @SerialName("scuolaDottEmail")
    val phdSchoolEmail: String? = null,

    /** url sito scuola dottorato */
    @SerialName("scuolaDottUrlSitoWeb")
    val phdSchoolWebsiteUrl: String? = null,

    /** note scuola dottorato */
    @SerialName("scuolaDottNote")
    val phdSchoolNotes: String? = null,

    /** sede */
    @SerialName("scuolaDottSedeId")
    val phdSchoolSiteId: Int? = null,

    /** anno accademico inizio validità scuola dottorato */
    @SerialName("scuolaDottAaIniVal")
    val phdSchoolAcademicYearStartValidity: Int? = null,

    /** anno accademico fine validità scuola dottorato */
    @SerialName("scuolaDottAaFineVal")
    val phdSchoolAcademicYearEndValidity: Int? = null,

    /** descrizione scuolaDottDurataEffettiva */
    @SerialName("scuolaDottDurataEffettiva")
    val phdSchoolEffectiveDuration: String? = null,

    /** descrizione scuolaDottDurata */
    @SerialName("scuolaDottDurata")
    val phdSchoolDuration: String? = null,

    /** Durata effettiva del corso espressa in base all¿UM (campo UM_DURATA). IN base al valore inserito IN questo campo viene valorizzato il campo DURATA_ANNI, il quale indica il numero di anni (di corso) di durata del corso. Viene proposto il corrispondente valore dei tipi corso (TIPI_CORSO.durata_effettiva). Per i tipi corso L2, LS, LC5 e LC6 il valore è fisso, ovvero stabilito dal descreto 509/99. */
    @SerialName("durataEffettiva")
    val effectiveDuration: Int? = null,

    /** Unità di misura della durata effettiva del corso (valore del campo DURATA_EFFETTIVA). Sono ammessi i valori: A=anni, M=mesi, G=giorni. Viene proposto il corrispondente valore dei tipi corso (tipi_corso.um_durata). Per i tipi corso L2, LS, LC5 e LC6 il valore è sempre A, come stabilito dal decreto 509/99. */
    @SerialName("umDurata")
    val measurementUnitDuration: String? = null,

    /** Identificativo univoco degli ordinamenti. Viene utilizzato nella struttura didattica responsabile (tabella P06_SDR). */
    @SerialName("cdsordId")
    val courseOfStudyOrderId: Int? = null,

    /** Codice del RAD presentato in Offerta Formativa (un RAD rappresenta l'istituzione di un corso di studio o la creazione di un nuovo ordinamento). */
    @SerialName("cdsRad")
    val courseOfStudyRoot: String? = null,

    /** Codice utilizzato per comunicazioni sistema esterno */
    @SerialName("cdsExt")
    val courseOfStudyExternal: String? = null,

    /** Indica se l´ordinamento è suddiviso in percorsi, tabella P06_PDSORD. Se previsto solo il percorso comune (PDS_ID=9999) allora PDS_FLG = 0 */
    @SerialName("pdsFlg")
    val studyPlanFlag: Int? = null,

    /** Indica l'anno di corso in cui deve essere effettuata la scelta di un PDS diverso da quello comune (PDS_ID = 9999). Valorizzato se ordinamento suddiviso IN percorsi (flag PDS_FLG = 1), IN tal caso deve essere > 1 e < durata anni del corso */
    @SerialName("acScelta")
    val choiceActivity: Int? = null,

    /** Indicatore utilizzato durante inserimento attività nel libretto e attuazione del piano: il sistema mette in automatico a Frequentato le attività del libretto, ovvero P11_AD_SCE.STA_SCE_COD = 'F', se si verificano le seguenti condizioni: anno di corso attività del piano/libretto =anno di corso ultima iscrizione attiva dello studente (P11_AD_PIANI.ANNO_CORSO = P04_ISCR_ANN.ANNO_CORSO) */
    @SerialName("autoFreqFlg")
    val autoAttendanceFlag: Int? = null,

    /** Indica se la frequenza è obbligatoria. */
    @SerialName("freqObblFlg")
    val mandatoryAttendanceFlag: Int? = null,

    /** Indicatore utilizzato durante attuazione piani se = 1 inserisce nel libretto le atttività fuori piano come sovranumerarie. (Valido solo per le AD con stato F) */
    @SerialName("autoSovrFFlg")
    val autoOverrideFFlag: Int? = null,

    /** Indicatore utilizzato durante attuazione piani se = 1 inserisce nel libretto le atttività fuori piano come sovranumerarie. (Valido solo per le AD con stato S) */
    @SerialName("autoSovrSFlg")
    val autoOverrideSFlag: Int? = null,

    /** Indicatore utilizzato in fase di verbalizzazione, consente la generazione automatica delle attività didattiche nel libretto 0 - generazione non abiltata 1 - generazione abilitata 2 - generazione abilitata solo per le attività didattiche presenti nel piano ma non nel libretto */
    @SerialName("genAdVerbFlg")
    val generateTeachingActivityVerbFlag: Int? = null,

    /** Base voto esami, ovvero il punteggio massimo di un singolo esame. Per i corsi post riforma il valore è fisso a 30, infatti viene stabilito dal decreto 509/99. Per i corsi definiti prima della Riforma il valore è variabile e deve essere valorizzato. */
    @SerialName("maxPuntiEsa")
    val maxGraduationPoints: Int? = null,

    /** Voto minimo di esame, ovvero il punteggio minimo per essere valido un esame. Per i corsi post riforma il valore è fisso a 18, infatti viene stabilito dal decreto 509/99. Per i corsi definiti prima della Riforma il valore è variabile e deve essere valorizzato. */
    @SerialName("minPuntiEsa")
    val minGraduationPoints: Int? = null
)

@Serializable
data class Esse3CourseTuition(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** anno di della tassa */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** causale della tassa */
    @SerialName("causale")
    val reason: String? = null,

    /** importo della tassa */
    @SerialName("importo")
    val amount: Float? = null,

    /** data di scadenza per il pagamento della tassa */
    @SerialName("scadenzaDta")
    val expirationDate: String? = null
)

@Serializable
data class Esse3ExternalEntity(
    /** ID dell ente esterno */
    @SerialName("enteId")
    val entityId: Long = 0L,

    /** codice ente esterno */
    @SerialName("enteCod")
    val entityCode: String = "",

    /** descrizione */
    @SerialName("des")
    val description: String? = null,

    /** descrizione estesa */
    @SerialName("desEstesa")
    val extendedDescription: String? = null,

    /** via indirizzo ente */
    @SerialName("via")
    val street: String? = null,

    /** cap indirizzo ente */
    @SerialName("cap")
    val postalCode: String? = null,

    /** codice fiscale ente */
    @SerialName("cf")
    val fiscalCode: String? = null,

    /** partita iva ente */
    @SerialName("piva")
    val vatNumber: String? = null,

    /** direttore */
    @SerialName("direttore")
    val director: String? = null,

    /** codice tipo ente */
    @SerialName("tipoEnteCod")
    val entityTypeCode: String = "",

    /** descrizione tipo ente */
    @SerialName("tipiEnteRic")
    val entityResearchTypes: String? = null,

    /** ID della nazione */
    @SerialName("nazioneId")
    val nationId: Long? = null,

    /** codice fiscale nazione */
    @SerialName("nazioneCodFisc")
    val nationFiscalCode: String? = null,

    /** descrizione nazione */
    @SerialName("nazioneDes")
    val nationDescription: String? = null,

    /** ID del comune */
    @SerialName("comuneId")
    val municipalityId: Long? = null,

    /** codice comune */
    @SerialName("comuneCod")
    val municipalityCode: String? = null,

    /** descrizione comune */
    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    /** flag azienda */
    @SerialName("aziendaFlg")
    val companyFlag: Long? = null,

    /** flag agenzia */
    @SerialName("agenziaFlg")
    val agencyFlag: Long? = null
)

@Serializable
data class Esse3StudyCourseWithStructureAndPaths(
    @SerialName("caricheCorso")
    val coursePositions: List<Esse3CourseRoles> = emptyList(),

    @SerialName("iscrittiCdsPerAnno")
    val enrolledCourseOfStudyPerYear: List<Esse3EnrolledPerCoursePerYear> = emptyList(),

    @SerialName("ordinamentiConPercorsi")
    val studyOrdersWithPaths: List<Esse3RegulationWithPaths> = emptyList(),

    @SerialName("struttureCorso")
    val courseStructures: List<Esse3CourseStructures> = emptyList(),

    /** Descrizione raggruppamento di tipi corso. */
    @SerialName("gruppoTcDes")
    val tcGroupDescription: String? = null,

    /** Codice raggruppamento di tipi corso. */
    @SerialName("gruppoTcCod")
    val tcGroupCode: String? = null,

    /** note */
    @SerialName("note")
    val notes: String? = null,

    /** durata in anni nell'anno di ordimanento attivo. */
    @SerialName("durataAnniOrdAttivo")
    val durationYearsActiveOrder: Int? = null,

    /** Anno di ordimanento attivo. */
    @SerialName("aaOrdAttivo")
    val academicYearActiveOrder: Int? = null,

    /** Flag di visibilità dell'Agenda 2030 per lo sviluppo sostenibile. */
    @SerialName("obSvilSosFlg")
    val sustainableDevelopmentObjectiveFlag: Int? = null,

    /** indica se i laureati in questo corso devono essere trasmessi ad Alma Laurea (1=trasmessi 0=no). */
    @SerialName("trasmAlmaFlg")
    val almaTransmissionFlag: Int? = null,

    /** Punteggio Massimo (base di valutazione, differenzia se il voto è espresso in centesimi, settantesimi o 110-esimi) */
    @SerialName("maxPunti")
    val maxPoints: Int? = null,

    /** ID Facoltà/Dipartimento di default in ambito area didattica */
    @SerialName("facIdDef")
    val facultyDefaultId: Long? = null,

    /** URL informativo web sul corso di studio */
    @SerialName("urlInfoWeb")
    val webInfoUrl: String? = null,

    /** Flag di corso master nei raggruppamenti di corsi di studio visualizzati nel Course Catalogue */
    @SerialName("ccMasterFlg")
    val masterCurrentAccountFlag: Int? = null,

    /** Codice del raggruppamento nei raggruppamenti di corsi di studio visualizzati nel Course Catalogue */
    @SerialName("ccRaggrCod")
    val currentAccountGroupCode: String? = null,

    /** flag che indica se allo stato attuale il corso risulta aperto all'immatricolazione. */
    @SerialName("abilImmaWeb")
    val webEnrollmentAuthorization: Int? = null,

    /** Descrizione tipi ciclo formativo */
    @SerialName("tipoCicloFormDes")
    val trainingCycleTypeDescription: String? = null,

    /** Codice tipi ciclo formativo */
    @SerialName("tipoCicloFormCod")
    val trainingCycleTypeCode: String? = null,

    /** Descrizione tipi corso catalogo */
    @SerialName("tipoCatalogoDes")
    val catalogTypeDescription: String? = null,

    /** Codice tipi corso catalogo */
    @SerialName("tipoCatalogoCod")
    val catalogTypeCode: String? = null,

    /** flag che indica se l'ordinamento del corso è abilitato all'immatricolazione */
    @SerialName("ordAbilImmaFlg")
    val orderEnableEnrollmentFlag: Int? = null,

    /** flag che indica se l'ordinamento del corso è attivo */
    @SerialName("ordAttivoFlg")
    val orderActiveFlag: Int? = null,

    /** Descrizione area ISCED */
    @SerialName("iscedDes")
    val iscedDescription: String? = null,

    /** Codice area ISCED */
    @SerialName("iscedCod")
    val iscedCode: String? = null,

    /** indica se il CDS è associato a una struttura di ateneo */
    @SerialName("sdrFlg")
    val siteFlag: Int? = null,

    /** Data di ultima modifica del corso di studio e dettagli. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataModCds")
    val courseOfStudyModificationDate: String? = null,

    /** codice del tipo di accesso al corso di studio */
    @SerialName("tipoAccesso")
    val accessType: String? = null,

    /** descrizione della classe interclasse del corso di studio in inglese */
    @SerialName("interClaDesEng")
    val interclassDescriptionEnglish: String? = null,

    /** descrizione della classe interclasse del corso di studio */
    @SerialName("interClaDes")
    val interclassDescription: String? = null,

    /** codice della classe interclasse del corso di studio */
    @SerialName("interClaCod")
    val interclassCode: String? = null,

    /** descrizione della classe MIUR del corso di studio in inglese */
    @SerialName("claDesEng")
    val classDescriptionEnglish: String? = null,

    /** descrizione della classe MIUR del corso di studio */
    @SerialName("claDes")
    val classDescription: String? = null,

    /** codice della classe MIUR del corso di studio */
    @SerialName("claCod")
    val classCode: String? = null,

    /** descrizione della normativa del corso di studio in inglese */
    @SerialName("normDesEng")
    val normDescriptionEnglish: String? = null,

    /** descrizione della normativa del corso di studio */
    @SerialName("normDes")
    val normDescription: String? = null,

    /** codice del della normativa del corso di studio */
    @SerialName("normCod")
    val normCode: String? = null,

    /** indica se del corso di studio deve essere visibile nel CC */
    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    /** URL del sito web del corso di studio */
    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    /** descrizione del tipo di titolo del corso di studio in inglese */
    @SerialName("tipoTititDesEng")
    val titleTypeDescriptionEnglish: String? = null,

    /** descrizione del tipo di titolo del corso di studio */
    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    /** codice del tipo di titolo del corso di studio */
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    /** descrizione del tipo di specializzazione del corso di studio */
    @SerialName("tipoSpecDes")
    val specializationTypeDescription: String? = null,

    /** codice valudo solo per le scuole di specializzazione (tipo_corso_cod = S1) e indica il Tipo di specializzazione */
    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    /** anno di disattivazione del corso */
    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    /** anno di attivazione del corso */
    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    /** descrizione del codice che indica la unit� di misura */
    @SerialName("umPesoDes")
    val measurementUnitWeightDescription: String? = null,

    /** codice che indica la unit� di misura del Corso di studio. In caso di corso post riforma viene sempre valorizzato a Crediti */
    @SerialName("umPesoCod")
    val measurementUnitWeightCode: String? = null,

    /** Codice Off. F. Ministeriale del Corso di Studio dell'Ateneo. */
    @SerialName("codicione")
    val bigCode: String? = null,

    /** indica se il CDS � stato istituito in accordo con la riforma e quindi pu� essere associato ad una classe di laurea. 0 = NUOVO abilita associazione con classe 1 = VECCHIO disabilita associazione con classe */
    @SerialName("rifFlg")
    val referenceFlag: Int? = null,

    /** codice ISTAT che distingue il corso di studio */
    @SerialName("istatCod")
    val istatCode: String? = null,

    /** descrizione del tipo di didattica del corso di studio in inglese */
    @SerialName("tipoDidDesEng")
    val didacticTypeDescriptionEnglish: String? = null,

    /** descrizione del tipo di didattica del corso di studio */
    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    /** codice del tipo di didattica del corso di studio */
    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    /** descrizione del tipo di corso di studio */
    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    /** descrizione del tipo di corso di studio */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** codice del tipo di corso di studio */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** chiave dell'ateneo di riferimento */
    @SerialName("ateneoId")
    val universityId: Long? = null,

    /** descrizione alternativa del corso di studio in inglese */
    @SerialName("cdsDesCertAltEng")
    val courseOfStudyAlternativeCertificateDescriptionEnglish: String? = null,

    /** descrizione alternativa del corso di studio */
    @SerialName("cdsDesCertAlt")
    val courseOfStudyAlternativeCertificateDescription: String? = null,

    /** descrizione aggiuntiva del corso di studio per i certificati in inglese */
    @SerialName("cdsDesCertBisEng")
    val courseOfStudyCertificateDescriptionBisEnglish: String? = null,

    /** descrizione aggiuntiva del corso di studio per i certificati */
    @SerialName("cdsDesCertBis")
    val courseOfStudyCertificateDescriptionBis: String? = null,

    /** descrizione del corso di studio per i certificati in inglese */
    @SerialName("cdsDesCertEng")
    val courseOfStudyCertificateDescriptionEnglish: String? = null,

    /** descrizione del corso di studio per i certificati */
    @SerialName("cdsDesCert")
    val courseOfStudyCertificateDescription: String? = null,

    /** descrizione del corso di studio in inglese */
    @SerialName("cdsDesEng")
    val courseOfStudyDescriptionEnglish: String? = null,

    /** descrizione del corso di studio */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** codice del corso di studio */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null
)

@Serializable
data class Esse3Regulation(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** anno di ordinamento del corso di studioa */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int = 0,

    /** codice dell'ordinamento */
    @SerialName("cdsOrdCod")
    val courseOfStudyOrderCode: String? = null,

    /** descrizione dell'ordinamento */
    @SerialName("cdsOrdDes")
    val courseOfStudyOrderDescription: String? = null,

    /** descrizione dell'ordinamento in inglese */
    @SerialName("cdsOrdDesEng")
    val courseOfStudyOrderDescriptionEnglish: String? = null,

    /** descrizione dell'ordinamento per i certificati */
    @SerialName("cdsOrdDesCert")
    val courseOfStudyOrderCertificateDescription: String? = null,

    /** descrizione dell'ordinamento per i certificati in inglese */
    @SerialName("cdsOrdDesCertEng")
    val courseOfStudyOrderCertificateDescriptionEnglish: String? = null,

    /** stato dell'ordinamneto */
    @SerialName("statoCod")
    val stateCode: Esse3StateCode? = null,

    /** anno di cessazione dell'ordinamento del corso di studioa */
    @SerialName("aaOrdCessId")
    val academicYearOrderCessationId: Int? = null,

    /** valore minimo di crediti, che devono essere ottenuti per poter conseguire il titolo di studio */
    @SerialName("valoreMin")
    val minimumValue: Long? = null,

    /** numero di anni di durata del corso di studio effettiva */
    @SerialName("durataAnni")
    val durationYears: Int? = null,

    /** Durata effettiva del corso espressa in base al um (valore del campo umDurata). */
    @SerialName("durataEffettiva")
    val effectiveDuration: Int? = null,

    /** Unità di misura della durata effettiva del corso (valore del campo durataEffettiva). */
    @SerialName("umDurata")
    val measurementUnitDuration: String? = null,

    /** identificativo scuola dottorato */
    @SerialName("scuolaDottId")
    val phdSchoolId: Int? = null,

    /** descrizione della scuola di dottorato */
    @SerialName("scuolaDottDes")
    val phdSchoolDescription: String? = null,

    @SerialName("lingueDidattica")
    val teachingLanguages: List<Esse3TeachingLanguagesRegulation> = emptyList(),

    @SerialName("regolamentiDidattici")
    val didacticRegulations: List<Esse3TeachingRegulation> = emptyList()
)

@Serializable
data class Esse3CoursePeriods(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** Anno di della scadenza */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** Codice della partizione AA */
    @SerialName("partCod")
    val partialCode: String? = null,

    /** Desdcrizione della partizione AA */
    @SerialName("partDes")
    val partialDescription: String? = null,

    /** Desdcrizione della partizione AA in inglese */
    @SerialName("partDesEng")
    val partialDescriptionEnglish: String? = null,

    /** Data di inizio del ciclo */
    @SerialName("dataInizio")
    val startDate: String? = null,

    /** Data di fine del ciclo */
    @SerialName("dataFine")
    val endDate: String? = null,

    /** Durata in mesi */
    @SerialName("durata")
    val duration: Float? = null
)

@Serializable
data class Esse3StructureWithLocations(
    /** chiave della struttura di riferimento */
    @SerialName("facId")
    val facultyId: Long = 0L,

    /** codice ISTAT che distingue la tipologia della struttura. */
    @SerialName("istatCod")
    val istatCode: String? = null,

    /** codice della struttura di riferimento */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione della struttura di riferimento */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** descrizione della struttura di riferimento in inglese */
    @SerialName("facDesEng")
    val facultyDescriptionEnglish: String? = null,

    /** chiave dell'ateneo di riferimento */
    @SerialName("ateneoId")
    val universityId: Long? = null,

    /** descrizione del comune */
    @SerialName("citta")
    val city: String? = null,

    /** descrizione della via */
    @SerialName("via")
    val street: String? = null,

    /** descrizione della provincia */
    @SerialName("prov")
    val province: String? = null,

    /** descrizione del CAP */
    @SerialName("cap")
    val postalCode: String? = null,

    /** codice fiscale */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** anno di attivazione della struttura */
    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    /** anno di disattivazione della struttura */
    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    /** URL del sito web della struttura */
    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    /** indica se la struttura deve essere visibile nel CC */
    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    /** numero di telefono della struttura */
    @SerialName("tel")
    val phone: String? = null,

    /** numero di fax della struttura */
    @SerialName("fax")
    val fax: String? = null,

    /** email della struttura */
    @SerialName("email")
    val email: String? = null,

    /** codice Miur che identifica un Facolt� utilizzato nelle statistiche ministeriali (in particolare nelle statistiche relative al post-laurea) */
    @SerialName("codStatMiur")
    val miurStatisticalCode: String? = null,

    /** codice CSA, utilizzato per mappare le nazioni durante l�allineamento docenti dal sistema CSA */
    @SerialName("csaCod")
    val csaCode: String? = null,

    /** codice tipo di struttura didattica responsabile. Codici di sistema FAC Facolt�, DIP Dipartimenti. */
    @SerialName("sdrTip")
    val siteType: String? = null,

    /** codice dell'Area disciplinare di afferenza della facolt�/dipartimento. */
    @SerialName("areaDiscCod")
    val disciplinaryAreaCode: String? = null,

    /** descrizione dell'area disciplinare in inglese */
    @SerialName("areaDiscDesEng")
    val disciplinaryAreaDescriptionEnglish: String? = null,

    @SerialName("sediStruttura")
    val structureSites: List<Esse3StructureLocations> = emptyList(),

    @SerialName("tipiCorsoStruttura")
    val courseTypesStructure: List<Esse3CourseStructureTypes> = emptyList()
)

@Serializable
data class Esse3TitleDetail(
    /** codice titolo italiano */
    @SerialName("tititCod")
    val titleTypeCode: String? = null,

    /** descrizione titolo italiano */
    @SerialName("tititDes")
    val titleTypeDescription: String? = null
)

@Serializable
data class Esse3CourseSessionDeadlines(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** anno di della scadenza */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** desdcrizione della scadenza */
    @SerialName("scadenzaDes")
    val expirationDescription: String? = null,

    /** desdcrizione della scadenza in inglese */
    @SerialName("scadenzaDesEng")
    val expirationDescriptionEnglish: String? = null,

    /** data di inizio della scadenza */
    @SerialName("scadenzaDtaInizio")
    val expirationStartDate: String? = null,

    /** data di fine della scadenza */
    @SerialName("scadenzaDtaFine")
    val expirationEndDate: String? = null
)

@Serializable
data class Esse3PathLanguages(
    /** descrizione linguaCod */
    @SerialName("linguaCod")
    val languageCode: String? = null,

    /** descrizione cdsId */
    @SerialName("cdsId")
    val courseOfStudyId: Int = 0,

    /** descrizione aaOrdId */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    /** descrizione linguaDidId */
    @SerialName("linguaDidId")
    val teachingLanguageId: Int? = null,

    /** descrizione linguaDidCod */
    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    /** descrizione linguaDidDes */
    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    /** descrizione pdsId */
    @SerialName("pdsId")
    val studyPlanId: Int = 0
)

@Serializable
data class Esse3TeachingTypes(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** Codice tipologia didattica */
    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    /** Descrizione tipologia didattica */
    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    /** Desctizione tipologia didattinca in inglese */
    @SerialName("tipoDidDesEng")
    val didacticTypeDescriptionEnglish: String? = null
)

@Serializable
data class Esse3CourseStructureTypes(
    /** codice del tipo corso */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String = "",

    /** descrizione dell'area disciplinare */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** ID della struttura di riferimento */
    @SerialName("facId")
    val facultyId: Long = 0L,

    /** indica l id del CDS */
    @SerialName("cdsId")
    val courseOfStudyId: Int? = null
)

@Serializable
data class Esse3TeachingLanguagesRegulation(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** anno di ordinamento del corso di studioa */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int = 0,

    /** ID Lingua didattica */
    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    /** Data di fine del ciclo */
    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    /** Data di fine del ciclo */
    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    /** Data di fine del ciclo */
    @SerialName("linguaDidDesEng")
    val teachingLanguageDescriptionEnglish: String? = null
)

@Serializable
data class Esse3StructureLocations(
    /** ID della struttura di riferimento */
    @SerialName("facId")
    val facultyId: Long = 0L,

    /** ID chiave della sede */
    @SerialName("sedeId")
    val siteId: Long = 0L,

    /** descrizione della sede */
    @SerialName("sedeDes")
    val siteDescription: String? = null,

    /** descrizione della sede in inglese */
    @SerialName("sedeDesEng")
    val siteDescriptionEnglish: String? = null,

    /** indicatore struttura di default area amministrativa per il CDS */
    @SerialName("defAmmFlg")
    val adminDefinitionFlag: Int? = null
)

@Serializable
data class Esse3TitleTypes(
    /** codice titoli italiano */
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    /** descrizione titolo italiano */
    @SerialName("tipiTititDes")
    val titleTypesDescription: String? = null,

    /** livello */
    @SerialName("tipiTititLivello")
    val titleTypesLevel: Int? = null,

    @SerialName("votoMinimo")
    val minimumGrade: Int? = null,

    /** flag riforma */
    @SerialName("rifFlg")
    val referenceFlag: Int? = null,

    /** stato richiesto */
    @SerialName("statoRichiesto")
    val requestedState: String? = null,

    /** dettaglio titolo */
    @SerialName("dettagli")
    val details: List<Esse3TitleDetail> = emptyList()
)

@Serializable
data class Esse3CoursePositions(
    /** anno accademico */
    @SerialName("annoAccademico")
    val academicYear: Int? = null,

    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** ID della carica */
    @SerialName("caricaId")
    val positionId: Long? = null,

    /** descrizione carica */
    @SerialName("caricaDes")
    val positionDescription: String? = null,

    /** descrizione carica in inglese */
    @SerialName("caricaDesEng")
    val positionDescriptionEnglish: String? = null,

    /** nome della carica */
    @SerialName("caricaNome")
    val positionName: String? = null,

    /** cognome della carica */
    @SerialName("caricaCognome")
    val positionSurname: String? = null,

    /** ID address book della persona in UGOV */
    @SerialName("caricaIdAb")
    val positionAbbreviatedId: Long? = null,

    /** identificativo tipo di utilizzo */
    @SerialName("tipoUtilizzoId")
    val usageTypeId: Long? = null,

    /** ordinamento della carica */
    @SerialName("ordine")
    val order: Long? = null,

    /** ID del docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** Matricola della carica */
    @SerialName("caricaMatricola")
    val positionStudentId: String? = null,

    /** Codice fiscale della carica */
    @SerialName("caricaCodFis")
    val positionFiscalCode: String? = null,

    /** Data di inizio validità della carica. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataInizioVal")
    val evaluationStartDate: String? = null,

    /** Data di fine validità della carica. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null
)

@Serializable
data class Esse3Location(
    /** chiave della sede */
    @SerialName("sedeId")
    val siteId: Long = 0L,

    /** chiave dell'ateneo di riferimento */
    @SerialName("ateneoId")
    val universityId: Long? = null,

    /** descrizione della sede */
    @SerialName("sedeDes")
    val siteDescription: String? = null,

    /** descrizione della sede in inglese */
    @SerialName("sedeDesEng")
    val siteDescriptionEnglish: String? = null,

    /** descrizione del CAP */
    @SerialName("cap")
    val postalCode: String? = null,

    /** descrizione della via */
    @SerialName("via")
    val street: String? = null,

    /** direttore della sede */
    @SerialName("direttore")
    val director: String? = null,

    /** descrizione del comune */
    @SerialName("citta")
    val city: String? = null,

    /** c�tta straniera della sede */
    @SerialName("cistra")
    val foreignCitizenship: String? = null,

    /** codice ISTAT che distingue la sede */
    @SerialName("istatCod")
    val istatCode: String? = null,

    /** numero di telefono della sede */
    @SerialName("tel")
    val phone: String? = null,

    /** numero di fax della sede */
    @SerialName("fax")
    val fax: String? = null,

    /** email della struttura */
    @SerialName("email")
    val email: String? = null,

    /** URL del sito web della struttura */
    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    /** indica se la sede deve essere visibile nel CC */
    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    /** indica se la sede � quella principale */
    @SerialName("sedePrincipaleFlg")
    val mainSiteFlag: Int? = null,

    /** provincia */
    @SerialName("provincia")
    val province: String? = null,

    /** nazione */
    @SerialName("nazione")
    val nation: String? = null
)

@Serializable
data class Esse3InternalStudyCourseWithDetails(
    /** descrizione cdsId */
    @SerialName("cdsId")
    val courseOfStudyId: Int = 0,

    /** descrizione cdsCod */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione cdsDes */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** descrizione tipoCorsoCod */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** descrizione tipoCorsoDes */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** descrizione istatCod */
    @SerialName("istatCod")
    val istatCode: String? = null,

    /** descrizione settFlg */
    @SerialName("settFlg")
    val sectorFlag: Int? = null,

    /** descrizione umPesoCod */
    @SerialName("umPesoCod")
    val measurementUnitWeightCode: String? = null,

    /** descrizione umPesoDes */
    @SerialName("umPesoDes")
    val measurementUnitWeightDescription: String? = null,

    /** descrizione aaAttId */
    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    /** descrizione aaDisId */
    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    /** descrizione tipoSpecCod */
    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    /** descrizione tipoTititCod */
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    /** descrizione tipoTititDes */
    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    /** descrizione webViewFlg */
    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    /** descrizione normCod */
    @SerialName("normCod")
    val normCode: String? = null,

    /** descrizione claAteneoCod */
    @SerialName("claAteneoCod")
    val classUniversityCode: String? = null,

    /** descrizione claMurstCod */
    @SerialName("claMurstCod")
    val classMurstCode: String? = null,

    /** descrizione interclaMurstCod */
    @SerialName("interclaMurstCod")
    val interclassMurstCode: String? = null,

    /** descrizione tipoAccesso */
    @SerialName("tipoAccesso")
    val accessType: String? = null,

    /** descrizione p07ClaAreeIscedCod */
    @SerialName("p07ClaAreeIscedCod")
    val p07ClassIscedAreasCode: String? = null,

    /** descrizione aaOrdAttivoId */
    @SerialName("aaOrdAttivoId")
    val academicYearActiveOrderId: Int? = null,

    /** descrizione ordAttivoDurataAnni */
    @SerialName("ordAttivoDurataAnni")
    val orderActiveDurationYears: Int? = null,

    /** descrizione tcDurataAnni */
    @SerialName("tcDurataAnni")
    val tcDurationYears: Int? = null,

    /** descrizione tipoCatalogoCod */
    @SerialName("tipoCatalogoCod")
    val catalogTypeCode: String? = null,

    /** descrizione tipoCicloFormCod */
    @SerialName("tipoCicloFormCod")
    val trainingCycleTypeCode: String? = null,

    /** descrizione maxPunti */
    @SerialName("maxPunti")
    val maxPoints: Int? = null,

    /** descrizione trasmAlmaFlg */
    @SerialName("trasmAlmaFlg")
    val almaTransmissionFlag: Int? = null,

    /** descrizione obSvilSosFlg */
    @SerialName("obSvilSosFlg")
    val sustainableDevelopmentObjectiveFlag: Int? = null,

    /** descrizione facIdAmm */
    @SerialName("facIdAmm")
    val facultyAdminId: Int? = null,

    /** descrizione facCodAmm */
    @SerialName("facCodAmm")
    val facultyAdminCode: String? = null,

    /** descrizione facIdCt */
    @SerialName("facIdCt")
    val facultyCommitteeId: Int? = null,

    /** descrizione facCtCod */
    @SerialName("facCtCod")
    val facultyCommitteeCode: String? = null,

    /** descrizione gruppoTcCod */
    @SerialName("gruppoTcCod")
    val tcGroupCode: String? = null,

    /** descrizione acronimo */
    @SerialName("acronimo")
    val acronym: String? = null,

    /** descrizione codExt */
    @SerialName("codExt")
    val externalCode: String? = null,

    /** descrizione codStatMiur */
    @SerialName("codStatMiur")
    val miurStatisticalCode: String? = null,

    /** descrizione postiStatFlg */
    @SerialName("postiStatFlg")
    val statutorySeatsFlag: Int? = null,

    /** descrizione webImmatFlg */
    @SerialName("webImmatFlg")
    val webEnrollmentFlag: Int? = null,

    /** descrizione urlSitoWeb */
    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    /** descrizione csaCod */
    @SerialName("csaCod")
    val csaCode: String? = null,

    /** descrizione tipoMasterCod */
    @SerialName("tipoMasterCod")
    val masterTypeCode: String? = null,

    /** descrizione note */
    @SerialName("note")
    val notes: String? = null,

    /** descrizione tiroTutorPsicoFlg */
    @SerialName("tiroTutorPsicoFlg")
    val psychologicalTutorInternshipFlag: Int? = null,

    /** descrizione statMiurFlg */
    @SerialName("statMiurFlg")
    val miurStatisticalFlag: Int? = null,

    /** descrizione ccRaggrCod */
    @SerialName("ccRaggrCod")
    val currentAccountGroupCode: String? = null,

    /** descrizione ccMasterFlg */
    @SerialName("ccMasterFlg")
    val masterCurrentAccountFlag: Int? = null,

    @SerialName("ordinamenti")
    val studyOrders: List<Esse3Regulations> = emptyList(),

    @SerialName("lingueOrdinamenti")
    val orderLanguages: Esse3RegulationLanguages? = null,

    @SerialName("Percorsi")
    val paths: List<Esse3Paths> = emptyList(),

    @SerialName("RegolamentoDidattico")
    val teachingRegulation: List<Esse3TeachingRegulation> = emptyList(),

    @SerialName("linguePercorsi")
    val pathLanguages: Esse3PathLanguages? = null,

    @SerialName("strutture")
    val structures: Esse3Structures? = null,

    @SerialName("sediCorso")
    val courseSites: List<Esse3InternalCourseLocations> = emptyList(),

    @SerialName("caricheCorso")
    val coursePositions: List<Esse3InternalCourseRoles> = emptyList(),

    @SerialName("caratteristicheCorso")
    val courseCharacteristics: List<Esse3InternalCourseFeatures> = emptyList(),

    @SerialName("tasseCorso")
    val courseTaxes: Esse3CourseTuition? = null,

    @SerialName("titoliAccesso")
    val accessTitles: List<Esse3CourseAccessTitles> = emptyList(),

    @SerialName("periodi")
    val periods: List<Esse3InternalCoursePeriods> = emptyList(),

    @SerialName("corsiConsorziati")
    val consortiumCourses: List<Esse3InternalConsortiumCourses> = emptyList(),

    @SerialName("coortiCorso")
    val courseCohorts: List<Esse3CourseCohort> = emptyList(),

    @SerialName("tipiDidattica")
    val teachingTypes: List<Esse3InternalTeachingTypes> = emptyList(),

    @SerialName("iscrittiCdsPerAnno")
    val enrolledCourseOfStudyPerYear: List<Esse3EnrolledPerCoursePerYear> = emptyList()
)

@Serializable
data class Esse3TeachingStructure(
    /** chiave della struttura di riferimento */
    @SerialName("facId")
    val facultyId: Long = 0L,

    /** codice ISTAT che distingue la tipologia della struttura. */
    @SerialName("istatCod")
    val istatCode: String? = null,

    /** codice della struttura di riferimento */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione della struttura di riferimento */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** descrizione della struttura di riferimento in inglese */
    @SerialName("facDesEng")
    val facultyDescriptionEnglish: String? = null,

    /** chiave dell'ateneo di riferimento */
    @SerialName("ateneoId")
    val universityId: Long? = null,

    /** descrizione del comune */
    @SerialName("citta")
    val city: String? = null,

    /** descrizione della via */
    @SerialName("via")
    val street: String? = null,

    /** descrizione della provincia */
    @SerialName("prov")
    val province: String? = null,

    /** descrizione del CAP */
    @SerialName("cap")
    val postalCode: String? = null,

    /** codice fiscale */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** anno di attivazione della struttura */
    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    /** anno di disattivazione della struttura */
    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    /** URL del sito web della struttura */
    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    /** indica se la struttura deve essere visibile nel CC */
    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    /** numero di telefono della struttura */
    @SerialName("tel")
    val phone: String? = null,

    /** numero di fax della struttura */
    @SerialName("fax")
    val fax: String? = null,

    /** email della struttura */
    @SerialName("email")
    val email: String? = null,

    /** codice Miur che identifica un Facolt� utilizzato nelle statistiche ministeriali (in particolare nelle statistiche relative al post-laurea) */
    @SerialName("codStatMiur")
    val miurStatisticalCode: String? = null,

    /** codice CSA, utilizzato per mappare le nazioni durante l�allineamento docenti dal sistema CSA */
    @SerialName("csaCod")
    val csaCode: String? = null,

    /** codice tipo di struttura didattica responsabile. Codici di sistema FAC Facolt�, DIP Dipartimenti. */
    @SerialName("sdrTip")
    val siteType: String? = null,

    /** codice dell'Area disciplinare di afferenza della facolt�/dipartimento. */
    @SerialName("areaDiscCod")
    val disciplinaryAreaCode: String? = null,

    /** descrizione dell'area disciplinare in inglese */
    @SerialName("areaDiscDesEng")
    val disciplinaryAreaDescriptionEnglish: String? = null
)

@Serializable
data class Esse3InternalCourseRoles(
    /** descrizione annoAccademico */
    @SerialName("annoAccademico")
    val academicYear: String? = null,

    /** descrizione cdsId */
    @SerialName("cdsId")
    val courseOfStudyId: Int = 0,

    /** descrizione caricaId */
    @SerialName("caricaId")
    val positionId: Int? = null,

    /** descrizione caricaDes */
    @SerialName("caricaDes")
    val positionDescription: String? = null,

    /** descrizione caricaCognome */
    @SerialName("caricaCognome")
    val positionSurname: String? = null,

    /** descrizione caricaNome */
    @SerialName("caricaNome")
    val positionName: String? = null,

    /** descrizione caricaIdAb */
    @SerialName("caricaIdAb")
    val positionAbbreviatedId: String? = null,

    /** descrizione caricaMatricola */
    @SerialName("caricaMatricola")
    val positionStudentId: String? = null,

    /** descrizione caricaCodFis */
    @SerialName("caricaCodFis")
    val positionFiscalCode: String? = null,

    /** descrizione tipoUtilizzoId */
    @SerialName("tipoUtilizzoId")
    val usageTypeId: Int? = null,

    /** descrizione ordine */
    @SerialName("ordine")
    val order: String? = null,

    /** descrizione docenteId */
    @SerialName("docenteId")
    val lecturerId: Int? = null
)

@Serializable
data class Esse3DisciplinaryArea(
    /** codice dell'area disciplinare */
    @SerialName("areaDiscCod")
    val disciplinaryAreaCode: String = "",

    /** descrizione dell'area disciplinare */
    @SerialName("areaDiscDes")
    val disciplinaryAreaDescription: String? = null,

    /** descrizione dell'area disciplinare in inglese */
    @SerialName("areaDiscDesEng")
    val disciplinaryAreaDescriptionEnglish: String? = null,

    /** chiave dell'ateneo di riferimento */
    @SerialName("ateneoId")
    val universityId: Long? = null
)

@Serializable
data class Esse3CourseTuitionFees(
    /** anno accademico */
    @SerialName("aa_id")
    val academicYear_Id: Int? = null,

    /** tipologia */
    @SerialName("tipologia")
    val typology: String? = null,

    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** ID della voce della tassa */
    @SerialName("voce_id")
    val itemId: Long? = null,

    /** codice del versamento */
    @SerialName("codice_versamento")
    val paymentCode: String? = null,

    /** descrizione del versamento */
    @SerialName("descrizione_versamento")
    val paymentDescription: String? = null,

    /** importo della tassa */
    @SerialName("importo")
    val amount: Float? = null,

    /** Data di scadenza */
    @SerialName("scadenza")
    val expiration: String? = null
)

@Serializable
data class Esse3TeachingRegulation(
    /** ID del regolamento didattico */
    @SerialName("regdidId")
    val didacticRegulationId: Long? = null,

    /** anno di regolamento didattico */
    @SerialName("aaRegdidId")
    val academicYearTeachingRegulationId: Int = 0,

    /** codice del regolamennto */
    @SerialName("regdidCod")
    val didacticRegulationCode: String? = null,

    /** descrizione del percorso di studio */
    @SerialName("regdidDes")
    val didacticRegulationDescription: String? = null,

    /** codice del RAD */
    @SerialName("radCod")
    val rootCode: String? = null,

    @SerialName("ateneiConsorziati")
    val consortiumUniversities: List<Esse3ConsortiumUniversity> = emptyList()
)

@Serializable
data class Esse3CourseDeadlines(
    /** anno accademico */
    @SerialName("annoAccademico")
    val academicYear: Int? = null,

    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** codice della scadenza */
    @SerialName("scadenzaCod")
    val expirationCode: String? = null,

    /** descrizione della scadenza */
    @SerialName("scadenzaDes")
    val expirationDescription: String? = null,

    /** descrizione della scadenza in inglese */
    @SerialName("scadenzaDesEng")
    val expirationDescriptionEnglish: String? = null,

    /** data di inizio della scadenza */
    @SerialName("dataDa")
    val dateFrom: String? = null,

    /** data di fine della scadenza */
    @SerialName("dataA")
    val dateTo: String? = null
)

@Serializable
data class Esse3AccessTitles(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** anno di ordinamento del corso di studioa */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    /** progressivo del titolo di accesso */
    @SerialName("combtitPrg")
    val combinationTitleProgram: Int? = null,

    /** codice del tipo titolo di accesso */
    @SerialName("tipoTititCod")
    val titleCategoryCode: String = "",

    /** descrizione del tipo titolo di accesso */
    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    /** descrizione del tipo titolo di accesso in inglese */
    @SerialName("tipoTititDesEng")
    val titleTypeDescriptionEnglish: String? = null
)

@Serializable
data class Esse3CourseAccessTitles(
    /** descrizione annoAccademico */
    @SerialName("aaOrdCdsCdsordId")
    val academicYearOrderCourseOrderId: Int? = null,

    /** descrizione cdsId */
    @SerialName("cdsId")
    val courseOfStudyId: Int = 0,

    /** descrizione cdsCod */
    @SerialName("combtitPrg")
    val combinationTitleProgram: Int? = null,

    /** codice titolo */
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    /** descrizione titolo */
    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null
)

@Serializable
data class Esse3TeachingLanguagesPath(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** anno di ordinamento del corso di studioa */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int = 0,

    /** ID del percorso di studio */
    @SerialName("pdsId")
    val studyPlanId: Long = 0L,

    /** ID Lingua didattica */
    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    /** Data di fine del ciclo */
    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    /** Data di fine del ciclo */
    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    /** Data di fine del ciclo */
    @SerialName("linguaDidDesEng")
    val teachingLanguageDescriptionEnglish: String? = null
)

@Serializable
data class Esse3InternalCoursePeriods(
    /** descrizione cdsId */
    @SerialName("cdsId")
    val courseOfStudyId: Int = 0,

    /** descrizione aaId */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** descrizione partCod */
    @SerialName("partCod")
    val partialCode: String? = null,

    /** descrizione partDes */
    @SerialName("partDes")
    val partialDescription: String? = null,

    /** descrizione dataInizio */
    @SerialName("dataInizio")
    val startDate: String? = null,

    /** descrizione dataFine */
    @SerialName("dataFine")
    val endDate: String? = null,

    /** descrizione durata */
    @SerialName("durata")
    val duration: String? = null
)

@Serializable
data class Esse3InternalCourseLocations(
    /** descrizione sedeId */
    @SerialName("sedeId")
    val siteId: Int? = null,

    /** descrizione cdsId */
    @SerialName("cdsId")
    val courseOfStudyId: Int = 0,

    /** descrizione ateneoId */
    @SerialName("ateneoId")
    val universityId: Int? = null,

    /** descrizione des */
    @SerialName("des")
    val description: String? = null,

    /** descrizione codStatMiur */
    @SerialName("codStatMiur")
    val miurStatisticalCode: String? = null,

    /** descrizione defDidFlg */
    @SerialName("defDidFlg")
    val didacticDefinitionFlag: Int? = null,

    /** descrizione dataIniVal */
    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    /** descrizione dataFineVal */
    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null
)

@Serializable
data class Esse3CourseFeatures(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** anno di della caratteristica */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** titolo della caratteristica */
    @SerialName("carattTitolo")
    val characteristicTitle: String? = null,

    /** titolo della caratteristica in inglese */
    @SerialName("carattTitoloEng")
    val characteristicTitleEnglish: String? = null,

    /** testo della caratteristica */
    @SerialName("carattTesto")
    val characteristicText: String? = null,

    /** testo della caratteristica in inglese */
    @SerialName("carattTestoEng")
    val characteristicTextEnglish: String? = null,

    /** ordinamento della carica */
    @SerialName("ordine")
    val order: Long? = null,

    /** codice del tipo testo utilizzato nel sistema di programmazione didattica */
    @SerialName("tipoTestoProgDidCod")
    val didacticProgramTextTypeCode: String? = null,

    /** codice della tipologia di caratteristica */
    @SerialName("tipoCarattCod")
    val characteristicTypeCode: String? = null
)

@Serializable
data class Esse3RegulationLanguages(
    /** descrizione linguaCod */
    @SerialName("linguaCod")
    val languageCode: String? = null,

    /** descrizione cdsId */
    @SerialName("cdsId")
    val courseOfStudyId: Int = 0,

    /** descrizione aaOrdId */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    /** descrizione linguaDidId */
    @SerialName("linguaDidId")
    val teachingLanguageId: Int? = null,

    /** descrizione linguaDidCod */
    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    /** descrizione linguaDidDes */
    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null
)

@Serializable
data class Esse3StudyPath(
    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** anno di ordinamento del corso di studioa */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int = 0,

    /** ID del percorso di studio */
    @SerialName("pdsId")
    val studyPlanId: Long = 0L,

    /** codice del percorso di studio */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** descrizione del percorso di studio */
    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    /** descrizione del percorso di studio in inglese */
    @SerialName("pdsDesEng")
    val studyPlanDescriptionEnglish: String? = null,

    /** stato del percorso di studio */
    @SerialName("statoCod")
    val stateCode: Esse3StateCode? = null,

    /** indica se del percorso di studio deve essere visibile nel CC */
    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    @SerialName("lingueDidattica")
    val teachingLanguages: List<Esse3TeachingLanguagesPath> = emptyList()
)

@Serializable
data class Esse3InternalTeachingTypes(
    /** descrizione cdsId */
    @SerialName("cdsId")
    val courseOfStudyId: Int = 0,

    /** descrizione ateneoId */
    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    /** descrizione ateneoDes */
    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null
)

@Serializable
data class Esse3AdmissionTitles(
    @SerialName("combinazioniOpzionali")
    val optionalCombinations: List<Esse3OptionalCombinations> = emptyList(),

    @SerialName("titoliObbligatori")
    val mandatoryTitles: List<Esse3MandatoryTitles> = emptyList(),

    /** descrizione della tipologia di corso di studio */
    @SerialName("tipologiaDes")
    val typologyDescription: String? = null,

    /** tipologia del corso di studio */
    @SerialName("tipologiaCod")
    val typologyCode: Esse3TypologyCode,

    /** ID del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L
)
