package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3MandatoryTitles(
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    @SerialName("tipiTititDes")
    val titleTypesDescription: String? = null,

    @SerialName("tipiTititLivello")
    val titleTypesLevel: Int? = null,

    @SerialName("rifFlg")
    val referenceFlag: Int? = null,

    @SerialName("statoRichiesto")
    val requestedState: String? = null,

    @SerialName("dettagli")
    val details: List<Esse3TitleDetail> = emptyList()
)

@Serializable
data class Esse3InternalCourseFeatures(
    @SerialName("annoAccademico")
    val academicYear: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Int,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("testoClob")
    val textClob: String? = null,

    @SerialName("carattId")
    val characteristicId: Int? = null,

    @SerialName("tipoCarattCod")
    val characteristicTypeCode: String? = null,

    @SerialName("tipoTestoProgDidCod")
    val didacticProgramTextTypeCode: String? = null,

    @SerialName("ordine")
    val order: String? = null,

    @SerialName("sdrTip")
    val siteType: String? = null
)

@Serializable
data class Esse3InternalConsortiumCourses(
    @SerialName("cdsId")
    val courseOfStudyId: Int,

    @SerialName("ateneoId")
    val universityId: Int? = null,

    @SerialName("ateneoDes")
    val universityDescription: String? = null,

    @SerialName("cdsAteId")
    val courseOfStudyAteId: Int? = null,

    @SerialName("cdsAteneiItaDes")
    val italianUniversitiesCourseOfStudyDescription: String? = null,

    @SerialName("atestraId")
    val foreignTestId: Int? = null,

    @SerialName("atestraDes")
    val foreignTestDescription: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("cdsStraDes")
    val foreignCourseOfStudyDescription: String? = null
)

@Serializable
data class Esse3CourseStructures(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("facId")
    val facultyId: Long,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("facDesEng")
    val facultyDescriptionEnglish: String? = null,

    @SerialName("facCitta")
    val facultyCity: String? = null,

    @SerialName("defAmmFlg")
    val adminDefinitionFlag: Int? = null,

    @SerialName("oldDefAmmFlg")
    val oldAdminDefinitionFlag: Int? = null,

    @SerialName("defStatFlg")
    val statutoryDefinitionFlag: Int? = null,

    @SerialName("defRegCtFlg")
    val committeeRegulationDefinitionFlag: Int? = null,

    @SerialName("oldDefRegCtFlg")
    val oldCommitteeRegulationDefinitionFlag: Int? = null,

    @SerialName("racFlg")
    val recommendationFlag: Int? = null,

    @SerialName("annFlg")
    val yearFlag: Int? = null,

    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    @SerialName("csaCod")
    val csaCode: String? = null
)

@Serializable
data class Esse3PhDSectors(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int,

    @SerialName("settCod")
    val sectorCode: String,

    @SerialName("settDes")
    val sectorDescription: String? = null,

    @SerialName("settDesEng")
    val sectorDescriptionEnglish: String? = null,

    @SerialName("prevalenteFlg")
    val prevalentFlag: Int? = null,

    @SerialName("areaCod")
    val areaCode: String? = null,

    @SerialName("areaDes")
    val areaDescription: String? = null,

    @SerialName("areaDesEng")
    val areaDescriptionEnglish: String? = null
)

@Serializable
data class Esse3DeletedStudyCourse(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("dataModCds")
    val courseOfStudyModificationDate: String? = null
)

@Serializable
data class Esse3ConsortiumCourses(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("ateneoId")
    val universityId: Long? = null,

    @SerialName("ateneoDes")
    val universityDescription: String? = null,

    @SerialName("ateneoDesEng")
    val universityDescriptionEnglish: String? = null,

    @SerialName("cdsAteId")
    val courseOfStudyAteId: Long? = null,

    @SerialName("cdsAteneiItaDes")
    val italianUniversitiesCourseOfStudyDescription: String? = null,

    @SerialName("atestraId")
    val foreignTestId: Long? = null,

    @SerialName("atestraDes")
    val foreignTestDescription: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("cdsStraDes")
    val foreignCourseOfStudyDescription: String? = null
)

@Serializable
data class Esse3OptionalCombinations(
    @SerialName("combTaDes")
    val teachingActivityCombinationDescription: String? = null,

    @SerialName("combTaNota")
    val teachingActivityCombinationNote: String? = null,

    @SerialName("tipiTitolo")
    val titleTypes: List<Esse3TitleTypes> = emptyList()
)

@Serializable
data class Esse3InternalStudyCourses(
    @SerialName("linguaCod")
    val languageCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Int,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("istatCod")
    val istatCode: String? = null,

    @SerialName("umPesoCod")
    val measurementUnitWeightCode: String? = null,

    @SerialName("umPesoDes")
    val measurementUnitWeightDescription: String? = null,

    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    @SerialName("normCod")
    val normCode: String? = null,

    @SerialName("claAteneoCod")
    val classUniversityCode: String? = null,

    @SerialName("claMurstCod")
    val classMurstCode: String? = null,

    @SerialName("tipoAccesso")
    val accessType: String? = null,

    @SerialName("aaOrdAttivoId")
    val academicYearActiveOrderId: Int? = null,

    @SerialName("ordAttivoDurataAnni")
    val orderActiveDurationYears: Int? = null,

    @SerialName("tcDurataAnni")
    val tcDurationYears: Int? = null,

    @SerialName("tipoCatalogoCod")
    val catalogTypeCode: String? = null,

    @SerialName("tipoCicloFormCod")
    val trainingCycleTypeCode: String? = null,

    @SerialName("maxPunti")
    val maxPoints: Int? = null,

    @SerialName("facIdAmm")
    val facultyAdminId: Int? = null,

    @SerialName("facCodAmm")
    val facultyAdminCode: String? = null,

    @SerialName("facIdCt")
    val facultyCommitteeId: Int? = null,

    @SerialName("facCtCod")
    val facultyCommitteeCode: String? = null,

    @SerialName("acronimo")
    val acronym: String? = null,

    @SerialName("codExt")
    val externalCode: String? = null,

    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    @SerialName("csaCod")
    val csaCode: String? = null,

    @SerialName("tipoMasterCod")
    val masterTypeCode: String? = null
)

@Serializable
data class Esse3InternalStudyCourse(
    @SerialName("cdsId")
    val courseOfStudyId: Int,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("istatCod")
    val istatCode: String? = null,

    @SerialName("settFlg")
    val sectorFlag: Int? = null,

    @SerialName("umPesoCod")
    val measurementUnitWeightCode: String? = null,

    @SerialName("umPesoDes")
    val measurementUnitWeightDescription: String? = null,

    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    @SerialName("normCod")
    val normCode: String? = null,

    @SerialName("claAteneoCod")
    val classUniversityCode: String? = null,

    @SerialName("claMurstCod")
    val classMurstCode: String? = null,

    @SerialName("interclaMurstCod")
    val interclassMurstCode: String? = null,

    @SerialName("tipoAccesso")
    val accessType: String? = null,

    @SerialName("p07ClaAreeIscedCod")
    val p07ClassIscedAreasCode: String? = null,

    @SerialName("aaOrdAttivoId")
    val academicYearActiveOrderId: Int? = null,

    @SerialName("ordAttivoDurataAnni")
    val orderActiveDurationYears: Int? = null,

    @SerialName("tcDurataAnni")
    val tcDurationYears: Int? = null,

    @SerialName("tipoCatalogoCod")
    val catalogTypeCode: String? = null,

    @SerialName("tipoCicloFormCod")
    val trainingCycleTypeCode: String? = null,

    @SerialName("maxPunti")
    val maxPoints: Int? = null,

    @SerialName("trasmAlmaFlg")
    val almaTransmissionFlag: Int? = null,

    @SerialName("obSvilSosFlg")
    val sustainableDevelopmentObjectiveFlag: Int? = null,

    @SerialName("facIdAmm")
    val facultyAdminId: Int? = null,

    @SerialName("facCodAmm")
    val facultyAdminCode: String? = null,

    @SerialName("facIdCt")
    val facultyCommitteeId: Int? = null,

    @SerialName("facCtCod")
    val facultyCommitteeCode: String? = null,

    @SerialName("gruppoTcCod")
    val tcGroupCode: String? = null,

    @SerialName("acronimo")
    val acronym: String? = null,

    @SerialName("codExt")
    val externalCode: String? = null,

    @SerialName("codStatMiur")
    val miurStatisticalCode: String? = null,

    @SerialName("postiStatFlg")
    val statutorySeatsFlag: Int? = null,

    @SerialName("webImmatFlg")
    val webEnrollmentFlag: Int? = null,

    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    @SerialName("csaCod")
    val csaCode: String? = null,

    @SerialName("tipoMasterCod")
    val masterTypeCode: String? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("tiroTutorPsicoFlg")
    val psychologicalTutorInternshipFlag: Int? = null,

    @SerialName("statMiurFlg")
    val miurStatisticalFlag: Int? = null,

    @SerialName("ccRaggrCod")
    val currentAccountGroupCode: String? = null,

    @SerialName("ccMasterFlg")
    val masterCurrentAccountFlag: Int? = null
)

@Serializable
data class Esse3RegulationWithPaths(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int,

    @SerialName("cdsOrdCod")
    val courseOfStudyOrderCode: String? = null,

    @SerialName("cdsOrdDes")
    val courseOfStudyOrderDescription: String? = null,

    @SerialName("cdsOrdDesEng")
    val courseOfStudyOrderDescriptionEnglish: String? = null,

    @SerialName("cdsOrdDesCert")
    val courseOfStudyOrderCertificateDescription: String? = null,

    @SerialName("cdsOrdDesCertEng")
    val courseOfStudyOrderCertificateDescriptionEnglish: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("statoCod")
    val stateCode: String? = null,

    @SerialName("aaOrdCessId")
    val academicYearOrderCessationId: Int? = null,

    @SerialName("valoreMin")
    val minimumValue: Long? = null,

    @SerialName("durataAnni")
    val durationYears: Int? = null,

    @SerialName("durataEffettiva")
    val effectiveDuration: Int? = null,

    @SerialName("umDurata")
    val measurementUnitDuration: String? = null,

    @SerialName("scuolaDottId")
    val phdSchoolId: Int? = null,

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
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("caricaNome")
    val positionName: String? = null,

    @SerialName("caricaCognome")
    val positionSurname: String? = null,

    @SerialName("caricaDes")
    val positionDescription: String? = null,

    @SerialName("caricaDesEng")
    val positionDescriptionEnglish: String? = null,

    @SerialName("ordine")
    val order: Long? = null,

    @SerialName("tipoUtilizzoId")
    val usageTypeId: Long? = null,

    @SerialName("caricaIdAb")
    val positionAbbreviatedId: Long? = null,

    @SerialName("caricaId")
    val positionId: Long? = null,

    @SerialName("caricaMatricola")
    val positionStudentId: String? = null,

    @SerialName("caricaCodFis")
    val positionFiscalCode: String? = null
)

@Serializable
data class Esse3AdmissionTitlesWithCode(
    @SerialName("cdsCod")
    val courseOfStudyCode: String,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @Serializable(with = Esse3EnumValueSerializer::class)
    @SerialName("tipologiaCod")
    val typologyCode: String,

    @SerialName("tipologiaDes")
    val typologyDescription: String? = null,

    @SerialName("titoliObbligatori")
    val mandatoryTitles: List<Esse3MandatoryTitles> = emptyList(),

    @SerialName("combinazioniOpzionali")
    val optionalCombinations: List<Esse3OptionalCombinations> = emptyList()
)

@Serializable
data class Esse3RegulationWithPhDSectors(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int,

    @SerialName("cdsOrdCod")
    val courseOfStudyOrderCode: String? = null,

    @SerialName("cdsOrdDes")
    val courseOfStudyOrderDescription: String? = null,

    @SerialName("cdsOrdDesEng")
    val courseOfStudyOrderDescriptionEnglish: String? = null,

    @SerialName("cdsOrdDesCert")
    val courseOfStudyOrderCertificateDescription: String? = null,

    @SerialName("cdsOrdDesCertEng")
    val courseOfStudyOrderCertificateDescriptionEnglish: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("statoCod")
    val stateCode: String? = null,

    @SerialName("aaOrdCessId")
    val academicYearOrderCessationId: Int? = null,

    @SerialName("valoreMin")
    val minimumValue: Long? = null,

    @SerialName("durataAnni")
    val durationYears: Int? = null,

    @SerialName("durataEffettiva")
    val effectiveDuration: Int? = null,

    @SerialName("umDurata")
    val measurementUnitDuration: String? = null,

    @SerialName("scuolaDottId")
    val phdSchoolId: Int? = null,

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

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("cdsDesEng")
    val courseOfStudyDescriptionEnglish: String? = null,

    @SerialName("cdsDesCert")
    val courseOfStudyCertificateDescription: String? = null,

    @SerialName("cdsDesCertEng")
    val courseOfStudyCertificateDescriptionEnglish: String? = null,

    @SerialName("cdsDesCertBis")
    val courseOfStudyCertificateDescriptionBis: String? = null,

    @SerialName("cdsDesCertBisEng")
    val courseOfStudyCertificateDescriptionBisEnglish: String? = null,

    @SerialName("cdsDesCertAlt")
    val courseOfStudyAlternativeCertificateDescription: String? = null,

    @SerialName("cdsDesCertAltEng")
    val courseOfStudyAlternativeCertificateDescriptionEnglish: String? = null,

    @SerialName("ateneoId")
    val universityId: Long? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    @SerialName("tipoDidDesEng")
    val didacticTypeDescriptionEnglish: String? = null,

    @SerialName("istatCod")
    val istatCode: String? = null,

    @SerialName("rifFlg")
    val referenceFlag: Int? = null,

    @SerialName("codicione")
    val bigCode: String? = null,

    @SerialName("umPesoCod")
    val measurementUnitWeightCode: String? = null,

    @SerialName("umPesoDes")
    val measurementUnitWeightDescription: String? = null,

    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    @SerialName("tipoSpecDes")
    val specializationTypeDescription: String? = null,

    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    @SerialName("tipoTititDesEng")
    val titleTypeDescriptionEnglish: String? = null,

    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    @SerialName("normCod")
    val normCode: String? = null,

    @SerialName("normDes")
    val normDescription: String? = null,

    @SerialName("normDesEng")
    val normDescriptionEnglish: String? = null,

    @SerialName("claCod")
    val classCode: String? = null,

    @SerialName("claDes")
    val classDescription: String? = null,

    @SerialName("claDesEng")
    val classDescriptionEnglish: String? = null,

    @SerialName("interClaCod")
    val interclassCode: String? = null,

    @SerialName("interClaDes")
    val interclassDescription: String? = null,

    @SerialName("interClaDesEng")
    val interclassDescriptionEnglish: String? = null,

    @SerialName("tipoAccesso")
    val accessType: String? = null,

    @SerialName("dataModCds")
    val courseOfStudyModificationDate: String? = null,

    @SerialName("sdrFlg")
    val siteFlag: Int? = null,

    @SerialName("iscedCod")
    val iscedCode: String? = null,

    @SerialName("iscedDes")
    val iscedDescription: String? = null,

    @SerialName("ordAttivoFlg")
    val orderActiveFlag: Int? = null,

    @SerialName("ordAbilImmaFlg")
    val orderEnableEnrollmentFlag: Int? = null,

    @SerialName("tipoCatalogoCod")
    val catalogTypeCode: String? = null,

    @SerialName("tipoCatalogoDes")
    val catalogTypeDescription: String? = null,

    @SerialName("tipoCicloFormCod")
    val trainingCycleTypeCode: String? = null,

    @SerialName("tipoCicloFormDes")
    val trainingCycleTypeDescription: String? = null,

    @SerialName("abilImmaWeb")
    val webEnrollmentAuthorization: Int? = null,

    @SerialName("ccRaggrCod")
    val currentAccountGroupCode: String? = null,

    @SerialName("ccMasterFlg")
    val masterCurrentAccountFlag: Int? = null,

    @SerialName("urlInfoWeb")
    val webInfoUrl: String? = null,

    @SerialName("facIdDef")
    val facultyDefaultId: Long? = null,

    @SerialName("maxPunti")
    val maxPoints: Int? = null,

    @SerialName("trasmAlmaFlg")
    val almaTransmissionFlag: Int? = null,

    @SerialName("obSvilSosFlg")
    val sustainableDevelopmentObjectiveFlag: Int? = null,

    @SerialName("aaOrdAttivo")
    val academicYearActiveOrder: Int? = null,

    @SerialName("durataAnniOrdAttivo")
    val durationYearsActiveOrder: Int? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("gruppoTcCod")
    val tcGroupCode: String? = null,

    @SerialName("gruppoTcDes")
    val tcGroupDescription: String? = null
)

@Serializable
data class Esse3CourseCohort(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("aaRegId")
    val academicYearRegulationId: Int
)

@Serializable
data class Esse3Structures(
    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    @SerialName("facId")
    val facultyId: Int? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Int,

    @SerialName("defRegCtFlg")
    val committeeRegulationDefinitionFlag: Int? = null,

    @SerialName("defAmmFlg")
    val adminDefinitionFlag: Int? = null,

    @SerialName("defStatFlg")
    val statutoryDefinitionFlag: Int? = null,

    @SerialName("aaIniVal")
    val academicYearStartValidity: String? = null,

    @SerialName("aaFineVal")
    val academicYearEndValidity: String? = null,

    @SerialName("annFlg")
    val yearFlag: Int? = null,

    @SerialName("citta")
    val city: String? = null,

    @SerialName("cod")
    val code: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("oldDefAmmFlg")
    val oldAdminDefinitionFlag: Int? = null,

    @SerialName("oldDefRegCtFlg")
    val oldCommitteeRegulationDefinitionFlag: Int? = null,

    @SerialName("oldDefStatFlg")
    val oldStatutoryDefinitionFlag: Int? = null,

    @SerialName("racFlg")
    val recommendationFlag: Int? = null,

    @SerialName("csaCod")
    val csaCode: String? = null
)

@Serializable
data class Esse3Paths(
    @SerialName("cdsId")
    val courseOfStudyId: Int,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int,

    @SerialName("pdsId")
    val studyPlanId: Int? = null,

    @SerialName("pdsordId")
    val studyPlanOrderId: Int? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    @SerialName("statoCod")
    val stateCode: String? = null,

    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    @SerialName("acSceltaOri")
    val originalChoiceActivity: String? = null,

    @SerialName("webImmatFlg")
    val webEnrollmentFlag: Int? = null,

    @SerialName("webScePdsFlg")
    val webStudyPlanChoiceFlag: Int? = null,

    @SerialName("valoreMin")
    val minimumValue: Int? = null
)

@Serializable
data class Esse3EnrolledPerCoursePerYear(
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("tipoIscrDes")
    val enrollmentTypeDescription: String? = null,

    @SerialName("numIscritti")
    val enrolledNumber: Int? = null
)

@Serializable
data class Esse3StudyCourseWithDetails(
    @SerialName("gruppoTcDes")
    val tcGroupDescription: String? = null,

    @SerialName("gruppoTcCod")
    val tcGroupCode: String? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("durataAnniOrdAttivo")
    val durationYearsActiveOrder: Int? = null,

    @SerialName("aaOrdAttivo")
    val academicYearActiveOrder: Int? = null,

    @SerialName("obSvilSosFlg")
    val sustainableDevelopmentObjectiveFlag: Int? = null,

    @SerialName("trasmAlmaFlg")
    val almaTransmissionFlag: Int? = null,

    @SerialName("maxPunti")
    val maxPoints: Int? = null,

    @SerialName("facIdDef")
    val facultyDefaultId: Long? = null,

    @SerialName("urlInfoWeb")
    val webInfoUrl: String? = null,

    @SerialName("ccMasterFlg")
    val masterCurrentAccountFlag: Int? = null,

    @SerialName("ccRaggrCod")
    val currentAccountGroupCode: String? = null,

    @SerialName("abilImmaWeb")
    val webEnrollmentAuthorization: Int? = null,

    @SerialName("tipoCicloFormDes")
    val trainingCycleTypeDescription: String? = null,

    @SerialName("tipoCicloFormCod")
    val trainingCycleTypeCode: String? = null,

    @SerialName("tipoCatalogoDes")
    val catalogTypeDescription: String? = null,

    @SerialName("tipoCatalogoCod")
    val catalogTypeCode: String? = null,

    @SerialName("ordAbilImmaFlg")
    val orderEnableEnrollmentFlag: Int? = null,

    @SerialName("ordAttivoFlg")
    val orderActiveFlag: Int? = null,

    @SerialName("iscedDes")
    val iscedDescription: String? = null,

    @SerialName("iscedCod")
    val iscedCode: String? = null,

    @SerialName("sdrFlg")
    val siteFlag: Int? = null,

    @SerialName("dataModCds")
    val courseOfStudyModificationDate: String? = null,

    @SerialName("tipoAccesso")
    val accessType: String? = null,

    @SerialName("interClaDesEng")
    val interclassDescriptionEnglish: String? = null,

    @SerialName("interClaDes")
    val interclassDescription: String? = null,

    @SerialName("interClaCod")
    val interclassCode: String? = null,

    @SerialName("claDesEng")
    val classDescriptionEnglish: String? = null,

    @SerialName("claDes")
    val classDescription: String? = null,

    @SerialName("claCod")
    val classCode: String? = null,

    @SerialName("normDesEng")
    val normDescriptionEnglish: String? = null,

    @SerialName("normDes")
    val normDescription: String? = null,

    @SerialName("normCod")
    val normCode: String? = null,

    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    @SerialName("tipoTititDesEng")
    val titleTypeDescriptionEnglish: String? = null,

    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    @SerialName("tipoSpecDes")
    val specializationTypeDescription: String? = null,

    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    @SerialName("umPesoDes")
    val measurementUnitWeightDescription: String? = null,

    @SerialName("umPesoCod")
    val measurementUnitWeightCode: String? = null,

    @SerialName("codicione")
    val bigCode: String? = null,

    @SerialName("rifFlg")
    val referenceFlag: Int? = null,

    @SerialName("istatCod")
    val istatCode: String? = null,

    @SerialName("tipoDidDesEng")
    val didacticTypeDescriptionEnglish: String? = null,

    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("ateneoId")
    val universityId: Long? = null,

    @SerialName("cdsDesCertAltEng")
    val courseOfStudyAlternativeCertificateDescriptionEnglish: String? = null,

    @SerialName("cdsDesCertAlt")
    val courseOfStudyAlternativeCertificateDescription: String? = null,

    @SerialName("cdsDesCertBisEng")
    val courseOfStudyCertificateDescriptionBisEnglish: String? = null,

    @SerialName("cdsDesCertBis")
    val courseOfStudyCertificateDescriptionBis: String? = null,

    @SerialName("cdsDesCertEng")
    val courseOfStudyCertificateDescriptionEnglish: String? = null,

    @SerialName("cdsDesCert")
    val courseOfStudyCertificateDescription: String? = null,

    @SerialName("cdsDesEng")
    val courseOfStudyDescriptionEnglish: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

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
    @SerialName("minPuntiEsa")
    val minGraduationPoints: Int? = null,

    @SerialName("maxPuntiEsa")
    val maxGraduationPoints: Int? = null,

    @SerialName("genAdVerbFlg")
    val generateTeachingActivityVerbFlag: Int? = null,

    @SerialName("autoSovrSFlg")
    val autoOverrideSFlag: Int? = null,

    @SerialName("autoSovrFFlg")
    val autoOverrideFFlag: Int? = null,

    @SerialName("freqObblFlg")
    val mandatoryAttendanceFlag: Int? = null,

    @SerialName("autoFreqFlg")
    val autoAttendanceFlag: Int? = null,

    @SerialName("acScelta")
    val choiceActivity: Int? = null,

    @SerialName("pdsFlg")
    val studyPlanFlag: Int? = null,

    @SerialName("cdsExt")
    val courseOfStudyExternal: String? = null,

    @SerialName("cdsRad")
    val courseOfStudyRoot: String? = null,

    @SerialName("cdsordId")
    val courseOfStudyOrderId: Int? = null,

    @SerialName("umDurata")
    val measurementUnitDuration: String? = null,

    @SerialName("durataEffettiva")
    val effectiveDuration: Int? = null,

    @SerialName("scuolaDottDurata")
    val phdSchoolDuration: String? = null,

    @SerialName("scuolaDottDurataEffettiva")
    val phdSchoolEffectiveDuration: String? = null,

    @SerialName("scuolaDottAaFineVal")
    val phdSchoolAcademicYearEndValidity: Int? = null,

    @SerialName("scuolaDottAaIniVal")
    val phdSchoolAcademicYearStartValidity: Int? = null,

    @SerialName("scuolaDottSedeId")
    val phdSchoolSiteId: Int? = null,

    @SerialName("scuolaDottNote")
    val phdSchoolNotes: String? = null,

    @SerialName("scuolaDottUrlSitoWeb")
    val phdSchoolWebsiteUrl: String? = null,

    @SerialName("scuolaDottEmail")
    val phdSchoolEmail: String? = null,

    @SerialName("scuolaDottFax")
    val phdSchoolFax: String? = null,

    @SerialName("scuolaDottTel")
    val phdSchoolPhone: String? = null,

    @SerialName("scuolaDottNazioneId")
    val phdSchoolNationId: Int? = null,

    @SerialName("scuolaDottComuneId")
    val phdSchoolMunicipalityId: Int? = null,

    @SerialName("scuolaDottVia")
    val phdSchoolStreet: String? = null,

    @SerialName("scuolaDottCap")
    val phdSchoolPostalCode: String? = null,

    @SerialName("scuolaDottDes")
    val phdSchoolDescription: String? = null,

    @SerialName("scuolaDottId")
    val phdSchoolId: Int? = null,

    @SerialName("immaOrdCh")
    val closedEnrollmentOrder: String? = null,

    @SerialName("durataAnni")
    val durationYears: Int? = null,

    @SerialName("valoreMin")
    val minimumValue: String? = null,

    @SerialName("aaOrdCessId")
    val academicYearOrderCessationId: Int? = null,

    @SerialName("statoCod")
    val stateCode: String? = null,

    @SerialName("cdsOrdDes")
    val courseOfStudyOrderDescription: String? = null,

    @SerialName("cdsOrdCod")
    val courseOfStudyOrderCode: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int,

    @SerialName("cdsId")
    val courseOfStudyId: Int,

    @SerialName("linguaCod")
    val languageCode: String? = null
)

@Serializable
data class Esse3ConsortiumUniversity(
    @SerialName("sdrId")
    val siteId: Long,

    @SerialName("ateneoId")
    val universityId: Long? = null,

    @SerialName("ateneoCod")
    val universityCode: String? = null,

    @SerialName("ateneoDes")
    val universityDescription: String? = null,

    @SerialName("atestraFlg")
    val foreignTestFlag: Int? = null
)

@Serializable
data class Esse3CompetitionCourseList(
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("concorsoDes")
    val competitionDescription: String? = null,

    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    @SerialName("tipoTestDes")
    val testTypeDescription: String? = null,

    @SerialName("dataIniAmmWeb")
    val webAdminStartDate: String? = null,

    @SerialName("dataFinAmmWeb")
    val webAdminEndDate: String? = null,

    @SerialName("dataScad")
    val deadline: String? = null,

    @SerialName("dataScadPreim")
    val preEnrollmentDeadline: String? = null
)

@Serializable
data class Esse3CourseLocations(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("ateneoId")
    val universityId: Int? = null,

    @SerialName("sedeId")
    val siteId: Long,

    @SerialName("sedeDes")
    val siteDescription: String? = null,

    @SerialName("sedeDesEng")
    val siteDescriptionEnglish: String? = null,

    @SerialName("defDidFlg")
    val didacticDefinitionFlag: Int? = null,

    @SerialName("codStatMiur")
    val miurStatisticalCode: String? = null,

    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null
)

@Serializable
data class Esse3CourseTypes(
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    @SerialName("durataAnni")
    val durationYears: Int? = null,

    @SerialName("umDurata")
    val measurementUnitDuration: String? = null,

    @SerialName("durataEffettiva")
    val effectiveDuration: Int? = null,

    @SerialName("valoreMin")
    val minimumValue: Int? = null,

    @SerialName("rifFlg")
    val referenceFlag: Int? = null,

    @SerialName("livello")
    val level: Int? = null,

    @SerialName("dottoratoFlg")
    val phdFlag: Int? = null,

    @SerialName("scuolaSpecFlg")
    val specializationSchoolFlag: Int? = null,

    @SerialName("domCtFlg")
    val domicileCommitteeFlag: Int? = null,

    @SerialName("gruppoTcCod")
    val tcGroupCode: String? = null,

    @SerialName("gruppoTcDes")
    val tcGroupDescription: String? = null
)

@Serializable
data class Esse3CourseCharacteristics(
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("carattId")
    val characteristicId: Long? = null,

    @SerialName("tipoCarattCod")
    val characteristicTypeCode: String? = null,

    @SerialName("ordine")
    val order: Long? = null,

    @SerialName("annoAccademico")
    val academicYear: Int? = null,

    @SerialName("sdrTip")
    val siteType: String? = null,

    @SerialName("tipoTestoProgDidCod")
    val didacticProgramTextTypeCode: String? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("titoloEng")
    val titleEnglish: String? = null,

    @SerialName("testo")
    val text: String? = null,

    @SerialName("testoEng")
    val textEnglish: String? = null
)

@Serializable
data class Esse3Regulations(
    @SerialName("linguaCod")
    val languageCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Int,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    @SerialName("cdsOrdCod")
    val courseOfStudyOrderCode: String? = null,

    @SerialName("cdsOrdDes")
    val courseOfStudyOrderDescription: String? = null,

    @SerialName("statoCod")
    val stateCode: String? = null,

    @SerialName("aaOrdCessId")
    val academicYearOrderCessationId: Int? = null,

    @SerialName("valoreMin")
    val minimumValue: String? = null,

    @SerialName("durataAnni")
    val durationYears: Int? = null,

    @SerialName("immaOrdCh")
    val closedEnrollmentOrder: String? = null,

    @SerialName("scuolaDottId")
    val phdSchoolId: Int? = null,

    @SerialName("scuolaDottDes")
    val phdSchoolDescription: String? = null,

    @SerialName("scuolaDottCap")
    val phdSchoolPostalCode: String? = null,

    @SerialName("scuolaDottVia")
    val phdSchoolStreet: String? = null,

    @SerialName("scuolaDottComuneId")
    val phdSchoolMunicipalityId: Int? = null,

    @SerialName("scuolaDottNazioneId")
    val phdSchoolNationId: Int? = null,

    @SerialName("scuolaDottTel")
    val phdSchoolPhone: String? = null,

    @SerialName("scuolaDottFax")
    val phdSchoolFax: String? = null,

    @SerialName("scuolaDottEmail")
    val phdSchoolEmail: String? = null,

    @SerialName("scuolaDottUrlSitoWeb")
    val phdSchoolWebsiteUrl: String? = null,

    @SerialName("scuolaDottNote")
    val phdSchoolNotes: String? = null,

    @SerialName("scuolaDottSedeId")
    val phdSchoolSiteId: Int? = null,

    @SerialName("scuolaDottAaIniVal")
    val phdSchoolAcademicYearStartValidity: Int? = null,

    @SerialName("scuolaDottAaFineVal")
    val phdSchoolAcademicYearEndValidity: Int? = null,

    @SerialName("scuolaDottDurataEffettiva")
    val phdSchoolEffectiveDuration: String? = null,

    @SerialName("scuolaDottDurata")
    val phdSchoolDuration: String? = null,

    @SerialName("durataEffettiva")
    val effectiveDuration: Int? = null,

    @SerialName("umDurata")
    val measurementUnitDuration: String? = null,

    @SerialName("cdsordId")
    val courseOfStudyOrderId: Int? = null,

    @SerialName("cdsRad")
    val courseOfStudyRoot: String? = null,

    @SerialName("cdsExt")
    val courseOfStudyExternal: String? = null,

    @SerialName("pdsFlg")
    val studyPlanFlag: Int? = null,

    @SerialName("acScelta")
    val choiceActivity: Int? = null,

    @SerialName("autoFreqFlg")
    val autoAttendanceFlag: Int? = null,

    @SerialName("freqObblFlg")
    val mandatoryAttendanceFlag: Int? = null,

    @SerialName("autoSovrFFlg")
    val autoOverrideFFlag: Int? = null,

    @SerialName("autoSovrSFlg")
    val autoOverrideSFlag: Int? = null,

    @SerialName("genAdVerbFlg")
    val generateTeachingActivityVerbFlag: Int? = null,

    @SerialName("maxPuntiEsa")
    val maxGraduationPoints: Int? = null,

    @SerialName("minPuntiEsa")
    val minGraduationPoints: Int? = null
)

@Serializable
data class Esse3CourseTuition(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("causale")
    val reason: String? = null,

    @SerialName("importo")
    val amount: Float? = null,

    @SerialName("scadenzaDta")
    val expirationDate: String? = null
)

@Serializable
data class Esse3ExternalEntity(
    @SerialName("enteId")
    val entityId: Long,

    @SerialName("enteCod")
    val entityCode: String,

    @SerialName("des")
    val description: String? = null,

    @SerialName("desEstesa")
    val extendedDescription: String? = null,

    @SerialName("via")
    val street: String? = null,

    @SerialName("cap")
    val postalCode: String? = null,

    @SerialName("cf")
    val fiscalCode: String? = null,

    @SerialName("piva")
    val vatNumber: String? = null,

    @SerialName("direttore")
    val director: String? = null,

    @SerialName("tipoEnteCod")
    val entityTypeCode: String,

    @SerialName("tipiEnteRic")
    val entityResearchTypes: String? = null,

    @SerialName("nazioneId")
    val nationId: Long? = null,

    @SerialName("nazioneCodFisc")
    val nationFiscalCode: String? = null,

    @SerialName("nazioneDes")
    val nationDescription: String? = null,

    @SerialName("comuneId")
    val municipalityId: Long? = null,

    @SerialName("comuneCod")
    val municipalityCode: String? = null,

    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    @SerialName("aziendaFlg")
    val companyFlag: Long? = null,

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

    @SerialName("gruppoTcDes")
    val tcGroupDescription: String? = null,

    @SerialName("gruppoTcCod")
    val tcGroupCode: String? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("durataAnniOrdAttivo")
    val durationYearsActiveOrder: Int? = null,

    @SerialName("aaOrdAttivo")
    val academicYearActiveOrder: Int? = null,

    @SerialName("obSvilSosFlg")
    val sustainableDevelopmentObjectiveFlag: Int? = null,

    @SerialName("trasmAlmaFlg")
    val almaTransmissionFlag: Int? = null,

    @SerialName("maxPunti")
    val maxPoints: Int? = null,

    @SerialName("facIdDef")
    val facultyDefaultId: Long? = null,

    @SerialName("urlInfoWeb")
    val webInfoUrl: String? = null,

    @SerialName("ccMasterFlg")
    val masterCurrentAccountFlag: Int? = null,

    @SerialName("ccRaggrCod")
    val currentAccountGroupCode: String? = null,

    @SerialName("abilImmaWeb")
    val webEnrollmentAuthorization: Int? = null,

    @SerialName("tipoCicloFormDes")
    val trainingCycleTypeDescription: String? = null,

    @SerialName("tipoCicloFormCod")
    val trainingCycleTypeCode: String? = null,

    @SerialName("tipoCatalogoDes")
    val catalogTypeDescription: String? = null,

    @SerialName("tipoCatalogoCod")
    val catalogTypeCode: String? = null,

    @SerialName("ordAbilImmaFlg")
    val orderEnableEnrollmentFlag: Int? = null,

    @SerialName("ordAttivoFlg")
    val orderActiveFlag: Int? = null,

    @SerialName("iscedDes")
    val iscedDescription: String? = null,

    @SerialName("iscedCod")
    val iscedCode: String? = null,

    @SerialName("sdrFlg")
    val siteFlag: Int? = null,

    @SerialName("dataModCds")
    val courseOfStudyModificationDate: String? = null,

    @SerialName("tipoAccesso")
    val accessType: String? = null,

    @SerialName("interClaDesEng")
    val interclassDescriptionEnglish: String? = null,

    @SerialName("interClaDes")
    val interclassDescription: String? = null,

    @SerialName("interClaCod")
    val interclassCode: String? = null,

    @SerialName("claDesEng")
    val classDescriptionEnglish: String? = null,

    @SerialName("claDes")
    val classDescription: String? = null,

    @SerialName("claCod")
    val classCode: String? = null,

    @SerialName("normDesEng")
    val normDescriptionEnglish: String? = null,

    @SerialName("normDes")
    val normDescription: String? = null,

    @SerialName("normCod")
    val normCode: String? = null,

    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    @SerialName("tipoTititDesEng")
    val titleTypeDescriptionEnglish: String? = null,

    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    @SerialName("tipoSpecDes")
    val specializationTypeDescription: String? = null,

    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    @SerialName("umPesoDes")
    val measurementUnitWeightDescription: String? = null,

    @SerialName("umPesoCod")
    val measurementUnitWeightCode: String? = null,

    @SerialName("codicione")
    val bigCode: String? = null,

    @SerialName("rifFlg")
    val referenceFlag: Int? = null,

    @SerialName("istatCod")
    val istatCode: String? = null,

    @SerialName("tipoDidDesEng")
    val didacticTypeDescriptionEnglish: String? = null,

    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("ateneoId")
    val universityId: Long? = null,

    @SerialName("cdsDesCertAltEng")
    val courseOfStudyAlternativeCertificateDescriptionEnglish: String? = null,

    @SerialName("cdsDesCertAlt")
    val courseOfStudyAlternativeCertificateDescription: String? = null,

    @SerialName("cdsDesCertBisEng")
    val courseOfStudyCertificateDescriptionBisEnglish: String? = null,

    @SerialName("cdsDesCertBis")
    val courseOfStudyCertificateDescriptionBis: String? = null,

    @SerialName("cdsDesCertEng")
    val courseOfStudyCertificateDescriptionEnglish: String? = null,

    @SerialName("cdsDesCert")
    val courseOfStudyCertificateDescription: String? = null,

    @SerialName("cdsDesEng")
    val courseOfStudyDescriptionEnglish: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null
)

@Serializable
data class Esse3Regulation(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int,

    @SerialName("cdsOrdCod")
    val courseOfStudyOrderCode: String? = null,

    @SerialName("cdsOrdDes")
    val courseOfStudyOrderDescription: String? = null,

    @SerialName("cdsOrdDesEng")
    val courseOfStudyOrderDescriptionEnglish: String? = null,

    @SerialName("cdsOrdDesCert")
    val courseOfStudyOrderCertificateDescription: String? = null,

    @SerialName("cdsOrdDesCertEng")
    val courseOfStudyOrderCertificateDescriptionEnglish: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("statoCod")
    val stateCode: String? = null,

    @SerialName("aaOrdCessId")
    val academicYearOrderCessationId: Int? = null,

    @SerialName("valoreMin")
    val minimumValue: Long? = null,

    @SerialName("durataAnni")
    val durationYears: Int? = null,

    @SerialName("durataEffettiva")
    val effectiveDuration: Int? = null,

    @SerialName("umDurata")
    val measurementUnitDuration: String? = null,

    @SerialName("scuolaDottId")
    val phdSchoolId: Int? = null,

    @SerialName("scuolaDottDes")
    val phdSchoolDescription: String? = null,

    @SerialName("lingueDidattica")
    val teachingLanguages: List<Esse3TeachingLanguagesRegulation> = emptyList(),

    @SerialName("regolamentiDidattici")
    val didacticRegulations: List<Esse3TeachingRegulation> = emptyList()
)

@Serializable
data class Esse3CoursePeriods(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("partCod")
    val partialCode: String? = null,

    @SerialName("partDes")
    val partialDescription: String? = null,

    @SerialName("partDesEng")
    val partialDescriptionEnglish: String? = null,

    @SerialName("dataInizio")
    val startDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null,

    @SerialName("durata")
    val duration: Float? = null
)

@Serializable
data class Esse3StructureWithLocations(
    @SerialName("facId")
    val facultyId: Long,

    @SerialName("istatCod")
    val istatCode: String? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("facDesEng")
    val facultyDescriptionEnglish: String? = null,

    @SerialName("ateneoId")
    val universityId: Long? = null,

    @SerialName("citta")
    val city: String? = null,

    @SerialName("via")
    val street: String? = null,

    @SerialName("prov")
    val province: String? = null,

    @SerialName("cap")
    val postalCode: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    @SerialName("tel")
    val phone: String? = null,

    @SerialName("fax")
    val fax: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("codStatMiur")
    val miurStatisticalCode: String? = null,

    @SerialName("csaCod")
    val csaCode: String? = null,

    @SerialName("sdrTip")
    val siteType: String? = null,

    @SerialName("areaDiscCod")
    val disciplinaryAreaCode: String? = null,

    @SerialName("areaDiscDesEng")
    val disciplinaryAreaDescriptionEnglish: String? = null,

    @SerialName("sediStruttura")
    val structureSites: List<Esse3StructureLocations> = emptyList(),

    @SerialName("tipiCorsoStruttura")
    val courseTypesStructure: List<Esse3CourseStructureTypes> = emptyList()
)

@Serializable
data class Esse3TitleDetail(
    @SerialName("tititCod")
    val titleTypeCode: String? = null,

    @SerialName("tititDes")
    val titleTypeDescription: String? = null
)

@Serializable
data class Esse3CourseSessionDeadlines(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("scadenzaDes")
    val expirationDescription: String? = null,

    @SerialName("scadenzaDesEng")
    val expirationDescriptionEnglish: String? = null,

    @SerialName("scadenzaDtaInizio")
    val expirationStartDate: String? = null,

    @SerialName("scadenzaDtaFine")
    val expirationEndDate: String? = null
)

@Serializable
data class Esse3PathLanguages(
    @SerialName("linguaCod")
    val languageCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Int,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    @SerialName("linguaDidId")
    val teachingLanguageId: Int? = null,

    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    @SerialName("pdsId")
    val studyPlanId: Int
)

@Serializable
data class Esse3TeachingTypes(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    @SerialName("tipoDidDesEng")
    val didacticTypeDescriptionEnglish: String? = null
)

@Serializable
data class Esse3CourseStructureTypes(
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("facId")
    val facultyId: Long,

    @SerialName("cdsId")
    val courseOfStudyId: Int? = null
)

@Serializable
data class Esse3TeachingLanguagesRegulation(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int,

    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    @SerialName("linguaDidDesEng")
    val teachingLanguageDescriptionEnglish: String? = null
)

@Serializable
data class Esse3StructureLocations(
    @SerialName("facId")
    val facultyId: Long,

    @SerialName("sedeId")
    val siteId: Long,

    @SerialName("sedeDes")
    val siteDescription: String? = null,

    @SerialName("sedeDesEng")
    val siteDescriptionEnglish: String? = null,

    @SerialName("defAmmFlg")
    val adminDefinitionFlag: Int? = null
)

@Serializable
data class Esse3TitleTypes(
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    @SerialName("tipiTititDes")
    val titleTypesDescription: String? = null,

    @SerialName("tipiTititLivello")
    val titleTypesLevel: Int? = null,

    @SerialName("votoMinimo")
    val minimumGrade: Int? = null,

    @SerialName("rifFlg")
    val referenceFlag: Int? = null,

    @SerialName("statoRichiesto")
    val requestedState: String? = null,

    @SerialName("dettagli")
    val details: List<Esse3TitleDetail> = emptyList()
)

@Serializable
data class Esse3CoursePositions(
    @SerialName("annoAccademico")
    val academicYear: Int? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("caricaId")
    val positionId: Long? = null,

    @SerialName("caricaDes")
    val positionDescription: String? = null,

    @SerialName("caricaDesEng")
    val positionDescriptionEnglish: String? = null,

    @SerialName("caricaNome")
    val positionName: String? = null,

    @SerialName("caricaCognome")
    val positionSurname: String? = null,

    @SerialName("caricaIdAb")
    val positionAbbreviatedId: Long? = null,

    @SerialName("tipoUtilizzoId")
    val usageTypeId: Long? = null,

    @SerialName("ordine")
    val order: Long? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("caricaMatricola")
    val positionStudentId: String? = null,

    @SerialName("caricaCodFis")
    val positionFiscalCode: String? = null,

    @SerialName("dataInizioVal")
    val evaluationStartDate: String? = null,

    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null
)

@Serializable
data class Esse3Location(
    @SerialName("sedeId")
    val siteId: Long,

    @SerialName("ateneoId")
    val universityId: Long? = null,

    @SerialName("sedeDes")
    val siteDescription: String? = null,

    @SerialName("sedeDesEng")
    val siteDescriptionEnglish: String? = null,

    @SerialName("cap")
    val postalCode: String? = null,

    @SerialName("via")
    val street: String? = null,

    @SerialName("direttore")
    val director: String? = null,

    @SerialName("citta")
    val city: String? = null,

    @SerialName("cistra")
    val foreignCitizenship: String? = null,

    @SerialName("istatCod")
    val istatCode: String? = null,

    @SerialName("tel")
    val phone: String? = null,

    @SerialName("fax")
    val fax: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    @SerialName("sedePrincipaleFlg")
    val mainSiteFlag: Int? = null,

    @SerialName("provincia")
    val province: String? = null,

    @SerialName("nazione")
    val nation: String? = null
)

@Serializable
data class Esse3InternalStudyCourseWithDetails(
    @SerialName("cdsId")
    val courseOfStudyId: Int,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("istatCod")
    val istatCode: String? = null,

    @SerialName("settFlg")
    val sectorFlag: Int? = null,

    @SerialName("umPesoCod")
    val measurementUnitWeightCode: String? = null,

    @SerialName("umPesoDes")
    val measurementUnitWeightDescription: String? = null,

    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    @SerialName("normCod")
    val normCode: String? = null,

    @SerialName("claAteneoCod")
    val classUniversityCode: String? = null,

    @SerialName("claMurstCod")
    val classMurstCode: String? = null,

    @SerialName("interclaMurstCod")
    val interclassMurstCode: String? = null,

    @SerialName("tipoAccesso")
    val accessType: String? = null,

    @SerialName("p07ClaAreeIscedCod")
    val p07ClassIscedAreasCode: String? = null,

    @SerialName("aaOrdAttivoId")
    val academicYearActiveOrderId: Int? = null,

    @SerialName("ordAttivoDurataAnni")
    val orderActiveDurationYears: Int? = null,

    @SerialName("tcDurataAnni")
    val tcDurationYears: Int? = null,

    @SerialName("tipoCatalogoCod")
    val catalogTypeCode: String? = null,

    @SerialName("tipoCicloFormCod")
    val trainingCycleTypeCode: String? = null,

    @SerialName("maxPunti")
    val maxPoints: Int? = null,

    @SerialName("trasmAlmaFlg")
    val almaTransmissionFlag: Int? = null,

    @SerialName("obSvilSosFlg")
    val sustainableDevelopmentObjectiveFlag: Int? = null,

    @SerialName("facIdAmm")
    val facultyAdminId: Int? = null,

    @SerialName("facCodAmm")
    val facultyAdminCode: String? = null,

    @SerialName("facIdCt")
    val facultyCommitteeId: Int? = null,

    @SerialName("facCtCod")
    val facultyCommitteeCode: String? = null,

    @SerialName("gruppoTcCod")
    val tcGroupCode: String? = null,

    @SerialName("acronimo")
    val acronym: String? = null,

    @SerialName("codExt")
    val externalCode: String? = null,

    @SerialName("codStatMiur")
    val miurStatisticalCode: String? = null,

    @SerialName("postiStatFlg")
    val statutorySeatsFlag: Int? = null,

    @SerialName("webImmatFlg")
    val webEnrollmentFlag: Int? = null,

    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    @SerialName("csaCod")
    val csaCode: String? = null,

    @SerialName("tipoMasterCod")
    val masterTypeCode: String? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("tiroTutorPsicoFlg")
    val psychologicalTutorInternshipFlag: Int? = null,

    @SerialName("statMiurFlg")
    val miurStatisticalFlag: Int? = null,

    @SerialName("ccRaggrCod")
    val currentAccountGroupCode: String? = null,

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
    @SerialName("facId")
    val facultyId: Long,

    @SerialName("istatCod")
    val istatCode: String? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("facDesEng")
    val facultyDescriptionEnglish: String? = null,

    @SerialName("ateneoId")
    val universityId: Long? = null,

    @SerialName("citta")
    val city: String? = null,

    @SerialName("via")
    val street: String? = null,

    @SerialName("prov")
    val province: String? = null,

    @SerialName("cap")
    val postalCode: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("aaAttId")
    val academicYearActivityId: Int? = null,

    @SerialName("aaDisId")
    val academicYearDisciplineId: Int? = null,

    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    @SerialName("tel")
    val phone: String? = null,

    @SerialName("fax")
    val fax: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("codStatMiur")
    val miurStatisticalCode: String? = null,

    @SerialName("csaCod")
    val csaCode: String? = null,

    @SerialName("sdrTip")
    val siteType: String? = null,

    @SerialName("areaDiscCod")
    val disciplinaryAreaCode: String? = null,

    @SerialName("areaDiscDesEng")
    val disciplinaryAreaDescriptionEnglish: String? = null
)

@Serializable
data class Esse3InternalCourseRoles(
    @SerialName("annoAccademico")
    val academicYear: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Int,

    @SerialName("caricaId")
    val positionId: Int? = null,

    @SerialName("caricaDes")
    val positionDescription: String? = null,

    @SerialName("caricaCognome")
    val positionSurname: String? = null,

    @SerialName("caricaNome")
    val positionName: String? = null,

    @SerialName("caricaIdAb")
    val positionAbbreviatedId: String? = null,

    @SerialName("caricaMatricola")
    val positionStudentId: String? = null,

    @SerialName("caricaCodFis")
    val positionFiscalCode: String? = null,

    @SerialName("tipoUtilizzoId")
    val usageTypeId: Int? = null,

    @SerialName("ordine")
    val order: String? = null,

    @SerialName("docenteId")
    val lecturerId: Int? = null
)

@Serializable
data class Esse3DisciplinaryArea(
    @SerialName("areaDiscCod")
    val disciplinaryAreaCode: String,

    @SerialName("areaDiscDes")
    val disciplinaryAreaDescription: String? = null,

    @SerialName("areaDiscDesEng")
    val disciplinaryAreaDescriptionEnglish: String? = null,

    @SerialName("ateneoId")
    val universityId: Long? = null
)

@Serializable
data class Esse3CourseTuitionFees(
    @SerialName("aa_id")
    val academicYear_Id: Int? = null,

    @SerialName("tipologia")
    val typology: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("voce_id")
    val itemId: Long? = null,

    @SerialName("codice_versamento")
    val paymentCode: String? = null,

    @SerialName("descrizione_versamento")
    val paymentDescription: String? = null,

    @SerialName("importo")
    val amount: Float? = null,

    @SerialName("scadenza")
    val expiration: String? = null
)

@Serializable
data class Esse3TeachingRegulation(
    @SerialName("regdidId")
    val didacticRegulationId: Long? = null,

    @SerialName("aaRegdidId")
    val academicYearTeachingRegulationId: Int,

    @SerialName("regdidCod")
    val didacticRegulationCode: String? = null,

    @SerialName("regdidDes")
    val didacticRegulationDescription: String? = null,

    @SerialName("radCod")
    val rootCode: String? = null,

    @SerialName("ateneiConsorziati")
    val consortiumUniversities: List<Esse3ConsortiumUniversity> = emptyList()
)

@Serializable
data class Esse3CourseDeadlines(
    @SerialName("annoAccademico")
    val academicYear: Int? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("scadenzaCod")
    val expirationCode: String? = null,

    @SerialName("scadenzaDes")
    val expirationDescription: String? = null,

    @SerialName("scadenzaDesEng")
    val expirationDescriptionEnglish: String? = null,

    @SerialName("dataDa")
    val dateFrom: String? = null,

    @SerialName("dataA")
    val dateTo: String? = null
)

@Serializable
data class Esse3AccessTitles(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    @SerialName("combtitPrg")
    val combinationTitleProgram: Int? = null,

    @SerialName("tipoTititCod")
    val titleCategoryCode: String,

    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    @SerialName("tipoTititDesEng")
    val titleTypeDescriptionEnglish: String? = null
)

@Serializable
data class Esse3CourseAccessTitles(
    @SerialName("aaOrdCdsCdsordId")
    val academicYearOrderCourseOrderId: Int? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Int,

    @SerialName("combtitPrg")
    val combinationTitleProgram: Int? = null,

    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null
)

@Serializable
data class Esse3TeachingLanguagesPath(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int,

    @SerialName("pdsId")
    val studyPlanId: Long,

    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    @SerialName("linguaDidDesEng")
    val teachingLanguageDescriptionEnglish: String? = null
)

@Serializable
data class Esse3InternalCoursePeriods(
    @SerialName("cdsId")
    val courseOfStudyId: Int,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("partCod")
    val partialCode: String? = null,

    @SerialName("partDes")
    val partialDescription: String? = null,

    @SerialName("dataInizio")
    val startDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null,

    @SerialName("durata")
    val duration: String? = null
)

@Serializable
data class Esse3InternalCourseLocations(
    @SerialName("sedeId")
    val siteId: Int? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Int,

    @SerialName("ateneoId")
    val universityId: Int? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("codStatMiur")
    val miurStatisticalCode: String? = null,

    @SerialName("defDidFlg")
    val didacticDefinitionFlag: Int? = null,

    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null
)

@Serializable
data class Esse3CourseFeatures(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("carattTitolo")
    val characteristicTitle: String? = null,

    @SerialName("carattTitoloEng")
    val characteristicTitleEnglish: String? = null,

    @SerialName("carattTesto")
    val characteristicText: String? = null,

    @SerialName("carattTestoEng")
    val characteristicTextEnglish: String? = null,

    @SerialName("ordine")
    val order: Long? = null,

    @SerialName("tipoTestoProgDidCod")
    val didacticProgramTextTypeCode: String? = null,

    @SerialName("tipoCarattCod")
    val characteristicTypeCode: String? = null
)

@Serializable
data class Esse3RegulationLanguages(
    @SerialName("linguaCod")
    val languageCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Int,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    @SerialName("linguaDidId")
    val teachingLanguageId: Int? = null,

    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null
)

@Serializable
data class Esse3StudyPath(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int,

    @SerialName("pdsId")
    val studyPlanId: Long,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    @SerialName("pdsDesEng")
    val studyPlanDescriptionEnglish: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("statoCod")
    val stateCode: String? = null,

    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    @SerialName("lingueDidattica")
    val teachingLanguages: List<Esse3TeachingLanguagesPath> = emptyList()
)

@Serializable
data class Esse3InternalTeachingTypes(
    @SerialName("cdsId")
    val courseOfStudyId: Int,

    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null
)

@Serializable
data class Esse3AdmissionTitles(
    @SerialName("combinazioniOpzionali")
    val optionalCombinations: List<Esse3OptionalCombinations> = emptyList(),

    @SerialName("titoliObbligatori")
    val mandatoryTitles: List<Esse3MandatoryTitles> = emptyList(),

    @SerialName("tipologiaDes")
    val typologyDescription: String? = null,

    @Serializable(with = Esse3EnumValueSerializer::class)
    @SerialName("tipologiaCod")
    val typologyCode: String,

    @SerialName("cdsId")
    val courseOfStudyId: Long
)
