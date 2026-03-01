package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3ContextualizedActivity(
    @SerialName("chiaveAdContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey,

    @SerialName("adDesEng")
    val activityEnglishDescription: String? = null,

    @SerialName("cdsDesEng")
    val courseOfStudyDescriptionEnglish: String? = null,

    @SerialName("aaOrdDesEng")
    val academicYearOrderEnglishDescription: String? = null,

    @SerialName("pdsDesEng")
    val studyPlanDescriptionEnglish: String? = null,

    @SerialName("linguaInsDes")
    val teachingLanguageDescription: String? = null,

    @SerialName("linguaInsDesEng")
    val teachingLanguageDescriptionEnglish: String? = null,

    @SerialName("nonErogabileOdFlg")
    val nonDeliverableOdFlag: Int? = null,

    @SerialName("tipoEsaCod")
    val graduationTypeCode: String? = null,

    @SerialName("tipoEsaDes")
    val graduationTypeDescription: String? = null,

    @SerialName("tipoEsaDesEng")
    val graduationTypeDescriptionEnglish: String? = null,

    @SerialName("tipoValCod")
    val evaluationTypeCode: String? = null,

    @SerialName("tipoValDes")
    val evaluationTypeDescription: String? = null,

    @SerialName("tipoValDesEng")
    val evaluationTypeDescriptionEnglish: String? = null,

    @SerialName("tipoInsCod")
    val insertionTypeCode: String? = null,

    @SerialName("tipoInsDes")
    val insertionTypeDescription: String? = null,

    @SerialName("gruppoGiudCod")
    val judgmentGroupCode: String? = null,

    @SerialName("gruppoGiudDes")
    val judgmentGroupDescription: String? = null,

    @SerialName("reiterabile")
    val repeatable: Int? = null,

    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    @SerialName("urlCorsoMoodle")
    val moodleCourseUrl: String? = null,

    @SerialName("adCapogruppo")
    val activityGroupLeader: Esse3ActivityGroupLeader? = null,

    @SerialName("capoGruppoFlg")
    val groupLeaderFlag: Int? = null,

    @SerialName("adWebViewFlg")
    val activityWebViewFlag: Int? = null
)

@Serializable
data class Esse3TeachersPerTeachingUnit(
    @SerialName("chiaveUdContestualizzata")
    val contextualizedTeachingUnitKey: Esse3ContextualizedTeachingUnitKey,

    @SerialName("tipoCoperturaCod")
    val coverageTypeCode: String? = null,

    @SerialName("tipoCoperturaDes")
    val coverageTypeDescription: String? = null,

    @SerialName("noTraspFlg")
    val noTransportFlag: Int? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("fatPartDes")
    val invoicePartialDescription: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("domPartDes")
    val domicilePartialDescription: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long,

    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("lezioneFlg")
    val lessonFlag: Int? = null,

    @SerialName("titolareFlg")
    val holderFlag: Int? = null,

    @SerialName("respDidFlg")
    val didacticResponsibleFlag: Int? = null
)

@Serializable
data class Esse3Teachers(
    @SerialName("dataIniRuolo")
    val roleStartDate: String? = null,

    @SerialName("docenteAppellativo")
    val lecturerTitle: String? = null,

    @SerialName("profilo")
    val profile: String? = null,

    @SerialName("ruoloDocDes")
    val lecturerRoleDescription: String? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("dipCod")
    val departmentCode: String? = null,

    @SerialName("dipId")
    val departmentId: Long? = null,

    @SerialName("settDes")
    val sectorDescription: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("dataModDoc")
    val documentModificationDate: String? = null,

    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    @SerialName("noteDocente")
    val lecturerNotes: String? = null,

    @SerialName("noteCurriculum")
    val curriculumNotes: String? = null,

    @SerialName("noteBiografiche")
    val biographicalNotes: String? = null,

    @SerialName("notePubblicazioni")
    val publicationsNotes: String? = null,

    @SerialName("p01ProvDes")
    val p01ProvinceDescription: String? = null,

    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    @SerialName("p01ComuCodIstatMiur")
    val p01MunicipalityMiurIstatCode: String? = null,

    @SerialName("p01ComuComuneCod")
    val p01MunicipalityCommonCode: String? = null,

    @SerialName("p01ComuCodIstat")
    val p01MunicipalityIstatCode: String? = null,

    @SerialName("p01ComuComuneId")
    val p01MunicipalityCommonId: Long? = null,

    @SerialName("p01NaziCod")
    val p01NationCode: String? = null,

    @SerialName("p01NaziNazioneCod")
    val p01NationNationCode: String? = null,

    @SerialName("p01NaziDes")
    val p01NationDescription: String? = null,

    @SerialName("p01NaziCodFisc")
    val p01NationFiscalCode: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("dataFinAtt")
    val activityEndDate: String? = null,

    @SerialName("dataIniAtt")
    val activityStartDate: String? = null,

    @SerialName("hyperlink")
    val hyperlink: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("ruoloDocCod")
    val lecturerRoleCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("emailDocenteLa")
    val lecturerLaEmail: String? = null,

    @SerialName("emailAte")
    val ateEmail: String? = null,

    @SerialName("eMail")
    val email: String? = null,

    @SerialName("badge")
    val badge: String? = null,

    @SerialName("settCod")
    val sectorCode: String? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null
)

@Serializable
data class Esse3ActivityParentGroup(
    @SerialName("adragoffId")
    val activityRaggruppamentoOfferId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("cdsDesEng")
    val courseOfStudyDescriptionEnglish: String? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("adDesEng")
    val activityEnglishDescription: String? = null,

    @SerialName("tipoRagCod")
    val groupingTypeCode: String? = null,

    @SerialName("tipoRagDes")
    val groupingTypeDescription: String? = null,

    @SerialName("tipoRagDesEng")
    val groupingTypeDescriptionEnglish: String? = null,

    @SerialName("annoCoorte")
    val cohortYear: Int? = null,

    @SerialName("adFiglie")
    val childActivities: List<Esse3ActivityChildGroups> = emptyList()
)

@Serializable
data class Esse3OfferTeachingUnitDeletable(
    @SerialName("aaOffId")
    val academicYearOfferId: Long,

    @SerialName("cdsCod")
    val courseOfStudyCode: String,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int,

    @SerialName("pdsCod")
    val studyPlanCode: String,

    @SerialName("adCod")
    val activityCode: String,

    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("cancellabile")
    val deletable: Int? = null
)

@Serializable
data class Esse3OfferWithDetails(
    @SerialName("aaOffId")
    val academicYearOfferId: Int,

    @SerialName("cdsOffId")
    val courseOfStudyOfferId: Long,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("statoAttCod")
    val activityStateCode: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipiCorsoCod")
    val courseTypesCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    @SerialName("dataModOd")
    val odModificationDate: String? = null,

    @SerialName("dipCod")
    val departmentCode: String? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("offertaExistsFlg")
    val offerExistsFlag: Int? = null,

    @SerialName("logisticaExistsFlg")
    val logisticsExistsFlag: Int? = null,

    @SerialName("ADContestConDettagli")
    val teachingActivityContestWithDetails: List<Esse3ActivityContestWithDetails> = emptyList()
)

@Serializable
data class Esse3TeachingUnitContestWithDetails(
    @SerialName("chiaveUdContestualizzata")
    val contextualizedTeachingUnitKey: Esse3ContextualizedTeachingUnitKey,

    @SerialName("udDesEng")
    val teachingUnitDescriptionEnglish: String? = null,

    @SerialName("tipoUdCod")
    val teachingUnitTypeCode: String? = null,

    @SerialName("SEGContestualizzato")
    val contextualizedSegment: List<Esse3ContextualizedSegment> = emptyList(),

    @SerialName("DocentiPerUD")
    val teachersPerTeachingUnit: List<Esse3TeachersPerTeachingUnit> = emptyList()
)

@Serializable
data class Esse3Offer(
    @SerialName("aaOffId")
    val academicYearOfferId: Int,

    @SerialName("cdsOffId")
    val courseOfStudyOfferId: Long,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("statoAttCod")
    val activityStateCode: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipiCorsoCod")
    val courseTypesCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    @SerialName("dataModOd")
    val odModificationDate: String? = null,

    @SerialName("dipCod")
    val departmentCode: String? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("offertaExistsFlg")
    val offerExistsFlag: Int? = null,

    @SerialName("logisticaExistsFlg")
    val logisticsExistsFlag: Int? = null
)

@Serializable
data class Esse3ContextualizedSegmentKey(
    @SerialName("chiaveUdContestualizzata")
    val contextualizedTeachingUnitKey: Esse3ContextualizedTeachingUnitKey,

    @SerialName("segId")
    val segmentId: Long
)

@Serializable
data class Esse3TeachersWithDetails(
    @SerialName("DocentiOrario")
    val teachersSchedule: List<Esse3TeachersTimetable> = emptyList(),

    @SerialName("dataIniRuolo")
    val roleStartDate: String? = null,

    @SerialName("docenteAppellativo")
    val lecturerTitle: String? = null,

    @SerialName("profilo")
    val profile: String? = null,

    @SerialName("ruoloDocDes")
    val lecturerRoleDescription: String? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("dipCod")
    val departmentCode: String? = null,

    @SerialName("dipId")
    val departmentId: Long? = null,

    @SerialName("settDes")
    val sectorDescription: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("dataModDoc")
    val documentModificationDate: String? = null,

    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    @SerialName("noteDocente")
    val lecturerNotes: String? = null,

    @SerialName("noteCurriculum")
    val curriculumNotes: String? = null,

    @SerialName("noteBiografiche")
    val biographicalNotes: String? = null,

    @SerialName("notePubblicazioni")
    val publicationsNotes: String? = null,

    @SerialName("p01ProvDes")
    val p01ProvinceDescription: String? = null,

    @SerialName("comuNascSigla")
    val birthMunicipalityAbbreviation: String? = null,

    @SerialName("citstraNasc")
    val birthForeignCity: String? = null,

    @SerialName("comuNascDes")
    val birthMunicipalityDescription: String? = null,

    @SerialName("p01ComuCodIstatMiur")
    val p01MunicipalityMiurIstatCode: String? = null,

    @SerialName("p01ComuComuneCod")
    val p01MunicipalityCommonCode: String? = null,

    @SerialName("p01ComuCodIstat")
    val p01MunicipalityIstatCode: String? = null,

    @SerialName("p01ComuComuneId")
    val p01MunicipalityCommonId: Long? = null,

    @SerialName("p01NaziCod")
    val p01NationCode: String? = null,

    @SerialName("p01NaziNazioneCod")
    val p01NationNationCode: String? = null,

    @SerialName("p01NaziDes")
    val p01NationDescription: String? = null,

    @SerialName("p01NaziCodFisc")
    val p01NationFiscalCode: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("dataFinAtt")
    val activityEndDate: String? = null,

    @SerialName("dataIniAtt")
    val activityStartDate: String? = null,

    @SerialName("hyperlink")
    val hyperlink: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("ruoloDocCod")
    val lecturerRoleCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("emailDocenteLa")
    val lecturerLaEmail: String? = null,

    @SerialName("emailAte")
    val ateEmail: String? = null,

    @SerialName("eMail")
    val email: String? = null,

    @SerialName("badge")
    val badge: String? = null,

    @SerialName("settCod")
    val sectorCode: String? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null
)

@Serializable
data class Esse3DeletedOffer(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("aaId")
    val academicYearId: Long,

    @SerialName("dataModOd")
    val odModificationDate: String? = null
)

@Serializable
data class Esse3ActivityDeletable(
    @SerialName("retCode")
    val returnCode: Int? = null,

    @SerialName("msg")
    val message: String? = null
)

@Serializable
data class Esse3OfferActivityDeletable(
    @SerialName("aaOffId")
    val academicYearOfferId: Long,

    @SerialName("cdsCod")
    val courseOfStudyCode: String,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int,

    @SerialName("pdsCod")
    val studyPlanCode: String,

    @SerialName("adCod")
    val activityCode: String,

    @SerialName("cancellabile")
    val deletable: Int? = null
)

@Serializable
data class Esse3ActivityChildGroups(
    @SerialName("adragoffId")
    val activityRaggruppamentoOfferId: Long? = null,

    @SerialName("adfiglioProgId")
    val childActivityProgressId: Long? = null,

    @SerialName("cdsFiglioId")
    val childCourseOfStudyId: Long? = null,

    @SerialName("cdsFiglioCod")
    val childCourseOfStudyCode: String? = null,

    @SerialName("cdsFiglioDes")
    val childCourseOfStudyDescription: String? = null,

    @SerialName("cdsFiglioDesEng")
    val childCourseOfStudyDescriptionEnglish: String? = null,

    @SerialName("adFiglioId")
    val childActivityId: Long? = null,

    @SerialName("adFiglioCod")
    val childActivityCode: String? = null,

    @SerialName("adFiglioDes")
    val childActivityDescription: String? = null,

    @SerialName("adFiglioDesEng")
    val childActivityEnglishDescription: String? = null
)

@Serializable
data class Esse3ActivitiesCountPlans(
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("conteggioPiani")
    val planCount: Int? = null
)

@Serializable
data class Esse3ContextualizedSegment(
    @SerialName("chiaveSegContestualizzato")
    val contextualizedSecretKey: Esse3ContextualizedSegmentKey,

    @SerialName("settCod")
    val sectorCode: String? = null,

    @SerialName("settDes")
    val sectorDescription: String? = null,

    @SerialName("discCod")
    val disciplineCode: String? = null,

    @SerialName("tipoCreCod")
    val creditTypeCode: String? = null,

    @SerialName("tipoCreDes")
    val creditTypeDescription: String? = null,

    @SerialName("tipoCreDesEng")
    val creditTypeDescriptionEnglish: String? = null,

    @SerialName("durUniVal")
    val universityValidityDuration: Float? = null,

    @SerialName("durStuInd")
    val individualStudyDuration: Float? = null,

    @SerialName("nota")
    val note: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipoAfCod")
    val teachingActivityTypeCode: String? = null,

    @SerialName("tipoAfDes")
    val teachingActivityTypeDescription: String? = null,

    @SerialName("tipoAfDesEng")
    val teachingActivityTypeDescriptionEnglish: String? = null,

    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int? = null,

    @SerialName("oreMinFreq")
    val minimumAttendanceHours: Float? = null,

    @SerialName("peso")
    val weight: Float? = null,

    @SerialName("ambId")
    val environmentId: Long? = null,

    @SerialName("ambDes")
    val environmentDescription: String? = null,

    @SerialName("ambDesEng")
    val environmentDescriptionEnglish: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipoAfReitCod")
    val repeatTeachingActivityTypeCode: String? = null,

    @SerialName("tipoAfReitDes")
    val repeatTeachingActivityTypeDescription: String? = null,

    @SerialName("tipoAfReitDesEng")
    val repeatTeachingActivityTypeDescriptionEnglish: String? = null,

    @SerialName("aaRegIni")
    val academicYearInitialRegulation: Int? = null,

    @SerialName("aaRegFin")
    val academicYearFinalRegulation: Int? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("interclaTipoAfCod")
    val interclassTeachingActivityTypeCode: String? = null,

    @SerialName("interclaTipoAfDes")
    val interclassTeachingActivityTypeDescription: String? = null,

    @SerialName("interclaTipoAfDesEng")
    val interclassTeachingActivityTypeDescriptionEnglish: String? = null,

    @SerialName("interclaAmbId")
    val interclassScopeId: Long? = null,

    @SerialName("interclaAmbDes")
    val interclassScopeDescription: String? = null,

    @SerialName("interclaAmbDesEng")
    val interclassScopeDescriptionEnglish: String? = null,

    @SerialName("liberaOdFlg")
    val freeOdFlag: Int? = null
)

@Serializable
data class Esse3FullPartitions(
    @SerialName("DominioDiPartizione")
    val partitionDomain: List<Esse3PartitionDomain> = emptyList(),

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("fatPartDes")
    val invoicePartialDescription: String? = null,

    @SerialName("fatPartDesEng")
    val invoicePartialDescriptionEnglish: String? = null,

    @SerialName("tipoFatt")
    val invoiceType: String? = null
)

@Serializable
data class Esse3TeachingLanguages(
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
data class Esse3ActivityContestWithDetails(
    @SerialName("chiaveAdContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey,

    @SerialName("adDesEng")
    val activityEnglishDescription: String? = null,

    @SerialName("cdsDesEng")
    val courseOfStudyDescriptionEnglish: String? = null,

    @SerialName("aaOrdDesEng")
    val academicYearOrderEnglishDescription: String? = null,

    @SerialName("pdsDesEng")
    val studyPlanDescriptionEnglish: String? = null,

    @SerialName("linguaInsDes")
    val teachingLanguageDescription: String? = null,

    @SerialName("linguaInsDesEng")
    val teachingLanguageDescriptionEnglish: String? = null,

    @SerialName("nonErogabileOdFlg")
    val nonDeliverableOdFlag: Int? = null,

    @SerialName("tipoEsaCod")
    val graduationTypeCode: String? = null,

    @SerialName("tipoEsaDes")
    val graduationTypeDescription: String? = null,

    @SerialName("tipoEsaDesEng")
    val graduationTypeDescriptionEnglish: String? = null,

    @SerialName("tipoValCod")
    val evaluationTypeCode: String? = null,

    @SerialName("tipoValDes")
    val evaluationTypeDescription: String? = null,

    @SerialName("tipoValDesEng")
    val evaluationTypeDescriptionEnglish: String? = null,

    @SerialName("tipoInsCod")
    val insertionTypeCode: String? = null,

    @SerialName("tipoInsDes")
    val insertionTypeDescription: String? = null,

    @SerialName("gruppoGiudCod")
    val judgmentGroupCode: String? = null,

    @SerialName("gruppoGiudDes")
    val judgmentGroupDescription: String? = null,

    @SerialName("reiterabile")
    val repeatable: Int? = null,

    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    @SerialName("urlCorsoMoodle")
    val moodleCourseUrl: String? = null,

    @SerialName("adCapogruppo")
    val activityGroupLeader: Esse3ActivityGroupLeader? = null,

    @SerialName("capoGruppoFlg")
    val groupLeaderFlag: Int? = null,

    @SerialName("adWebViewFlg")
    val activityWebViewFlag: Int? = null,

    @SerialName("UDContestConDettagli")
    val teachingUnitContestWithDetails: List<Esse3TeachingUnitContestWithDetails> = emptyList(),

    @SerialName("LinguaDidattiche")
    val teachingLanguages: List<Esse3TeachingLanguages> = emptyList()
)

@Serializable
data class Esse3PartitionDomain(
    @SerialName("fatPartCod")
    val invoicePartialCode: String,

    @SerialName("domPartCod")
    val domicilePartialCode: String,

    @SerialName("domPartDes")
    val domicilePartialDescription: String? = null,

    @SerialName("domPartDesEng")
    val domicilePartialDescriptionEnglish: String? = null
)

@Serializable
data class Esse3GenericActivity(
    @SerialName("adId")
    val activityId: Long,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("offertaExistsFlg")
    val offerExistsFlag: Int? = null
)

@Serializable
data class Esse3UpdateContextualizedActivity(
    @SerialName("urlCorsoMoodle")
    val moodleCourseUrl: String? = null
)

@Serializable
data class Esse3ActivitiesCountPlansFilters(
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    @SerialName("adCod")
    val activityCode: String? = null
)

@Serializable
data class Esse3ContextualizedTeachingUnit(
    @SerialName("chiaveUdContestualizzata")
    val contextualizedTeachingUnitKey: Esse3ContextualizedTeachingUnitKey,

    @SerialName("udDesEng")
    val teachingUnitDescriptionEnglish: String? = null,

    @SerialName("tipoUdCod")
    val teachingUnitTypeCode: String? = null
)

@Serializable
data class Esse3ActivityGroupLeader(
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("cdsDesEng")
    val courseOfStudyDescriptionEnglish: String? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("adDesEng")
    val activityEnglishDescription: String? = null,

    @SerialName("tipoRagCod")
    val groupingTypeCode: String? = null,

    @SerialName("tipoRagDes")
    val groupingTypeDescription: String? = null,

    @SerialName("tipoRagDesEng")
    val groupingTypeDescriptionEnglish: String? = null,

    @SerialName("annoCoorte")
    val cohortYear: Int? = null
)

@Serializable
data class Esse3PartitionFactor(
    @SerialName("tipoFatt")
    val invoiceType: String? = null,

    @SerialName("fatPartDesEng")
    val invoicePartialDescriptionEnglish: String? = null,

    @SerialName("fatPartDes")
    val invoicePartialDescription: String? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String
)
