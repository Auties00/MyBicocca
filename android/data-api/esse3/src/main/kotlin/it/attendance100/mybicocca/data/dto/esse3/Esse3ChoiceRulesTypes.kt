package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3ChoiceCondition(
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipoCondCod")
    val conditionTypeCode: String? = null,

    @SerialName("valSelId")
    val selectedValueId: Long? = null,

    @SerialName("valSelCod")
    val selectedValueCode: String? = null,

    @SerialName("valSelDes")
    val selectedValueDescription: String? = null,

    @SerialName("notFlg")
    val noteFlag: Int? = null,

    @SerialName("extId")
    val externalId: Long? = null
)

@Serializable
data class Esse3RulePropertyAND(
    @SerialName("regpropAndId")
    val andProposalRuleId: Long? = null,

    @SerialName("regpropOrId")
    val orProposalRuleId: Long? = null,

    @SerialName("regpropVincId")
    val proposalRuleWinnerId: Long? = null,

    @SerialName("regpropId")
    val proposalRuleId: Long? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipoVincoloRegola")
    val ruleConstraintType: String? = null,

    @SerialName("numAd")
    val teachingActivityNumber: Int? = null,

    @SerialName("peso")
    val weight: Float? = null,

    @SerialName("raggruppamentoFigli")
    val childrenGrouping: Int? = null,

    @SerialName("figli")
    val children: List<Esse3RulePropertyChild> = emptyList()
)

@Serializable
data class Esse3RulePropertyChild(
    @SerialName("regpropPropId")
    val proposalRuleProposalId: Long? = null,

    @SerialName("regpropAndId")
    val andProposalRuleId: Long? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipoElemento")
    val elementType: String? = null,

    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("settCod")
    val sectorCode: String? = null
)

@Serializable
data class Esse3ChoiceRuleBlockCondition(
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipoCondCod")
    val conditionTypeCode: String? = null,

    @SerialName("valSelId")
    val selectedValueId: Long? = null,

    @SerialName("valSelCod")
    val selectedValueCode: String? = null,

    @SerialName("valSelDes")
    val selectedValueDescription: String? = null,

    @SerialName("notFlg")
    val noteFlag: Int? = null,

    @SerialName("extId")
    val externalId: Long? = null,

    @SerialName("condBlkId")
    val conditionBlockId: Long? = null,

    @SerialName("blkId")
    val blockId: Int? = null,

    @SerialName("manualeFlg")
    val manualFlag: Int? = null
)

@Serializable
data class Esse3ChoiceRuleWithSchemas(
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("sceId")
    val choiceRuleId: Int? = null,

    @SerialName("ordNum")
    val orderNumber: Int? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("desEng")
    val descriptionEnglish: String? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("aptId")
    val aptId: Long? = null,

    @SerialName("aptCod")
    val aptCode: String? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipSce")
    val choiceType: String? = null,

    @SerialName("tipSceDes")
    val choiceTypeDescription: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipUnt")
    val teachingUnitType: String? = null,

    @SerialName("minUnt")
    val minTeachingUnit: Float? = null,

    @SerialName("maxUnt")
    val maxTeachingUnit: Float? = null,

    @SerialName("vinId")
    val constraintId: Int? = null,

    @SerialName("livello")
    val level: Int? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("modTAF")
    val tafMode: String? = null,

    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    @SerialName("opzFlg")
    val optionalFlag: Int? = null,

    @SerialName("sovranFlg")
    val overrideFlag: Int? = null,

    @SerialName("tesorettoFlg")
    val treasureFlag: Int? = null,

    @SerialName("pesoRosaRist")
    val restrictedListWeight: Long? = null,

    @SerialName("assegnazionePostiFlg")
    val seatAssignmentFlag: Int? = null,

    @SerialName("deliberaFlg")
    val resolutionFlag: Int? = null,

    @SerialName("azzeraCfuFlg")
    val resetCreditsFlag: Int? = null,

    @SerialName("parametriLogisticaFlg")
    val logisticsParametersFlag: Int? = null,

    @SerialName("notaPre")
    val preNote: String? = null,

    @SerialName("notaPreEng")
    val preNoteEnglish: String? = null,

    @SerialName("notaPreVisPubblFlg")
    val preNotePublicVisibleFlag: Int? = null,

    @SerialName("notaPost")
    val postNote: String? = null,

    @SerialName("notaPostEng")
    val postNoteEnglish: String? = null,

    @SerialName("notaPostVisPubblFlg")
    val postNotePublicVisibleFlag: Int? = null,

    @SerialName("extId")
    val externalId: Long? = null,

    @SerialName("extCod")
    val externalCode: String? = null,

    @SerialName("schemi")
    val schemas: List<Esse3PlanSchemaChoiceRule> = emptyList()
)

@Serializable
data class Esse3RulePropertyOR(
    @SerialName("regpropOrId")
    val orProposalRuleId: Long? = null,

    @SerialName("regpropVincId")
    val proposalRuleWinnerId: Long? = null,

    @SerialName("regpropId")
    val proposalRuleId: Long? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("regolePropAND")
    val andProposalRules: List<Esse3RulePropertyAND> = emptyList()
)

@Serializable
data class Esse3ChoiceRegulationWindow(
    @SerialName("finregsceId")
    val finalRegulationChoiceId: Long? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("tipoFinestra")
    val windowType: String? = null,

    @SerialName("dataInizio")
    val startDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null,

    @SerialName("extId")
    val externalId: Long? = null
)

@Serializable
data class Esse3PrerequisitesRegulationWithConstraints(
    @SerialName("regpropId")
    val proposalRuleId: Long? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("coorte")
    val cohort: Int? = null,

    @SerialName("aaRevisioneId")
    val academicYearRevisionId: Int? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("stato")
    val state: String? = null,

    @SerialName("extId")
    val externalId: Long? = null,

    @SerialName("vincoli")
    val constraints: List<Esse3PrerequisiteConstraint> = emptyList()
)

@Serializable
data class Esse3StudyPlanHeader(
    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("pianoId")
    val planId: Int? = null,

    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("schemaId")
    val schemaId: Long? = null,

    @SerialName("finregsceId")
    val finalRegulationChoiceId: Long? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("stato")
    val state: String? = null,

    @SerialName("statoDes")
    val stateDescription: String? = null,

    @SerialName("dataUltimaVarStato")
    val lastStateChangeDate: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipoPiano")
    val planType: String? = null,

    @SerialName("statutarioFlg")
    val statutoryFlag: Int? = null,

    @SerialName("coorte")
    val cohort: Int? = null,

    @SerialName("aaRevisioneId")
    val academicYearRevisionId: Int? = null,

    @SerialName("cdsStuId")
    val courseOfStudyStudentId: Long? = null,

    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    @SerialName("aaOrdStuId")
    val academicYearOrderStudentId: Long? = null,

    @SerialName("pdsStuId")
    val studyPlanStudentId: Long? = null,

    @SerialName("pdsStuCod")
    val studyPlanStudentCode: String? = null,

    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    @SerialName("pdsSceId")
    val studyPlanChoiceId: Long? = null,

    @SerialName("pdsSceCod")
    val studyPlanChoiceCode: String? = null,

    @SerialName("pdsSceDes")
    val studyPlanChoiceDescription: String? = null,

    @SerialName("aaDefId")
    val academicYearDefinitionId: Int? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    @SerialName("aptId")
    val aptId: Long? = null,

    @SerialName("aptCod")
    val aptCode: String? = null,

    @SerialName("aptDes")
    val aptDescription: String? = null,

    @SerialName("aaUltimaAttuazioneId")
    val academicYearLastImplementationId: Int? = null,

    @SerialName("dataUltimaAttuazione")
    val lastImplementationDate: String? = null,

    @SerialName("userUltimaAttuazione")
    val lastImplementationUser: String? = null,

    @SerialName("userControllo")
    val controlUser: String? = null,

    @SerialName("notaControllo")
    val controlNote: String? = null,

    @SerialName("noteSistema")
    val systemNotes: String? = null,

    @SerialName("noteUtente")
    val userNotes: String? = null,

    @SerialName("extCod")
    val externalCode: String? = null
)

@Serializable
data class Esse3ChoiceRuleCondition(
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipoCondCod")
    val conditionTypeCode: String? = null,

    @SerialName("valSelId")
    val selectedValueId: Long? = null,

    @SerialName("valSelCod")
    val selectedValueCode: String? = null,

    @SerialName("valSelDes")
    val selectedValueDescription: String? = null,

    @SerialName("notFlg")
    val noteFlag: Int? = null,

    @SerialName("extId")
    val externalId: Long? = null,

    @SerialName("condSceId")
    val conditionChoiceId: Long? = null
)

@Serializable
data class Esse3ChoiceRule(
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("sceId")
    val choiceRuleId: Int? = null,

    @SerialName("ordNum")
    val orderNumber: Int? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("desEng")
    val descriptionEnglish: String? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("aptId")
    val aptId: Long? = null,

    @SerialName("aptCod")
    val aptCode: String? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipSce")
    val choiceType: String? = null,

    @SerialName("tipSceDes")
    val choiceTypeDescription: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipUnt")
    val teachingUnitType: String? = null,

    @SerialName("minUnt")
    val minTeachingUnit: Float? = null,

    @SerialName("maxUnt")
    val maxTeachingUnit: Float? = null,

    @SerialName("vinId")
    val constraintId: Int? = null,

    @SerialName("livello")
    val level: Int? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("modTAF")
    val tafMode: String? = null,

    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    @SerialName("opzFlg")
    val optionalFlag: Int? = null,

    @SerialName("sovranFlg")
    val overrideFlag: Int? = null,

    @SerialName("tesorettoFlg")
    val treasureFlag: Int? = null,

    @SerialName("pesoRosaRist")
    val restrictedListWeight: Long? = null,

    @SerialName("assegnazionePostiFlg")
    val seatAssignmentFlag: Int? = null,

    @SerialName("deliberaFlg")
    val resolutionFlag: Int? = null,

    @SerialName("azzeraCfuFlg")
    val resetCreditsFlag: Int? = null,

    @SerialName("parametriLogisticaFlg")
    val logisticsParametersFlag: Int? = null,

    @SerialName("notaPre")
    val preNote: String? = null,

    @SerialName("notaPreEng")
    val preNoteEnglish: String? = null,

    @SerialName("notaPreVisPubblFlg")
    val preNotePublicVisibleFlag: Int? = null,

    @SerialName("notaPost")
    val postNote: String? = null,

    @SerialName("notaPostEng")
    val postNoteEnglish: String? = null,

    @SerialName("notaPostVisPubblFlg")
    val postNotePublicVisibleFlag: Int? = null,

    @SerialName("extId")
    val externalId: Long? = null,

    @SerialName("extCod")
    val externalCode: String? = null
)

@Serializable
data class Esse3StudyPlanHeaderPerActivity(
    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("pianoId")
    val planId: Int? = null,

    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("schemaId")
    val schemaId: Long? = null,

    @SerialName("finregsceId")
    val finalRegulationChoiceId: Long? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("stato")
    val state: String? = null,

    @SerialName("statoDes")
    val stateDescription: String? = null,

    @SerialName("dataUltimaVarStato")
    val lastStateChangeDate: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipoPiano")
    val planType: String? = null,

    @SerialName("statutarioFlg")
    val statutoryFlag: Int? = null,

    @SerialName("coorte")
    val cohort: Int? = null,

    @SerialName("aaRevisioneId")
    val academicYearRevisionId: Int? = null,

    @SerialName("cdsStuId")
    val courseOfStudyStudentId: Long? = null,

    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    @SerialName("aaOrdStuId")
    val academicYearOrderStudentId: Long? = null,

    @SerialName("pdsStuId")
    val studyPlanStudentId: Long? = null,

    @SerialName("pdsStuCod")
    val studyPlanStudentCode: String? = null,

    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    @SerialName("pdsSceId")
    val studyPlanChoiceId: Long? = null,

    @SerialName("pdsSceCod")
    val studyPlanChoiceCode: String? = null,

    @SerialName("pdsSceDes")
    val studyPlanChoiceDescription: String? = null,

    @SerialName("aaDefId")
    val academicYearDefinitionId: Int? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    @SerialName("aptId")
    val aptId: Long? = null,

    @SerialName("aptCod")
    val aptCode: String? = null,

    @SerialName("aptDes")
    val aptDescription: String? = null,

    @SerialName("aaUltimaAttuazioneId")
    val academicYearLastImplementationId: Int? = null,

    @SerialName("dataUltimaAttuazione")
    val lastImplementationDate: String? = null,

    @SerialName("userUltimaAttuazione")
    val lastImplementationUser: String? = null,

    @SerialName("userControllo")
    val controlUser: String? = null,

    @SerialName("notaControllo")
    val controlNote: String? = null,

    @SerialName("noteSistema")
    val systemNotes: String? = null,

    @SerialName("noteUtente")
    val userNotes: String? = null,

    @SerialName("extCod")
    val externalCode: String? = null,

    @SerialName("sceltaId")
    val choiceId: Long? = null,

    @SerialName("sceltaAdId")
    val teachingActivityChoiceId: Int? = null
)

@Serializable
data class Esse3ChoiceRuleSchemaWithDetails(
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("sceId")
    val choiceRuleId: Int? = null,

    @SerialName("ordNum")
    val orderNumber: Int? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("desEng")
    val descriptionEnglish: String? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("aptId")
    val aptId: Long? = null,

    @SerialName("aptCod")
    val aptCode: String? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipSce")
    val choiceType: String? = null,

    @SerialName("tipSceDes")
    val choiceTypeDescription: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipUnt")
    val teachingUnitType: String? = null,

    @SerialName("minUnt")
    val minTeachingUnit: Float? = null,

    @SerialName("maxUnt")
    val maxTeachingUnit: Float? = null,

    @SerialName("vinId")
    val constraintId: Int? = null,

    @SerialName("livello")
    val level: Int? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("modTAF")
    val tafMode: String? = null,

    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    @SerialName("opzFlg")
    val optionalFlag: Int? = null,

    @SerialName("sovranFlg")
    val overrideFlag: Int? = null,

    @SerialName("tesorettoFlg")
    val treasureFlag: Int? = null,

    @SerialName("pesoRosaRist")
    val restrictedListWeight: Long? = null,

    @SerialName("assegnazionePostiFlg")
    val seatAssignmentFlag: Int? = null,

    @SerialName("deliberaFlg")
    val resolutionFlag: Int? = null,

    @SerialName("azzeraCfuFlg")
    val resetCreditsFlag: Int? = null,

    @SerialName("parametriLogisticaFlg")
    val logisticsParametersFlag: Int? = null,

    @SerialName("notaPre")
    val preNote: String? = null,

    @SerialName("notaPreEng")
    val preNoteEnglish: String? = null,

    @SerialName("notaPreVisPubblFlg")
    val preNotePublicVisibleFlag: Int? = null,

    @SerialName("notaPost")
    val postNote: String? = null,

    @SerialName("notaPostEng")
    val postNoteEnglish: String? = null,

    @SerialName("notaPostVisPubblFlg")
    val postNotePublicVisibleFlag: Int? = null,

    @SerialName("extId")
    val externalId: Long? = null,

    @SerialName("extCod")
    val externalCode: String? = null,

    @SerialName("schemaId")
    val schemaId: Long? = null,

    @SerialName("schemaCod")
    val schemaCode: String? = null,

    @SerialName("ptSlotId")
    val ptSlotId: Long? = null,

    @SerialName("ptSlotCod")
    val ptSlotCode: String? = null,

    @SerialName("ptSlotDes")
    val ptSlotDescription: String? = null,

    @SerialName("blocchi")
    val blocks: List<Esse3ChoiceRuleBlock> = emptyList(),

    @SerialName("condizioni")
    val conditions: List<Esse3ChoiceRuleCondition> = emptyList(),

    @SerialName("filtriOD")
    val orFilters: List<Esse3ChoiceRuleOrFilter> = emptyList()
)

@Serializable
data class Esse3StudentsStatistics(
    @SerialName("numPianiStandardValidi")
    val validStandardPlansNumber: Int? = null,

    @SerialName("numPianiIndividualiValidi")
    val validIndividualPlansNumber: Int? = null,

    @SerialName("numPiani")
    val planNumber: Int? = null
)

@Serializable
data class Esse3PlansLinkedToActivities(
    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String? = null,

    @SerialName("cdsAdDes")
    val courseOfStudyTeachingActivityDescription: String? = null,

    @SerialName("aaOrdAdId")
    val academicYearOrderActivityId: Int? = null,

    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

    @SerialName("pdsAdCod")
    val studyPlanTeachingActivityCode: String? = null,

    @SerialName("pdsAdDes")
    val studyPlanTeachingActivityDescription: String? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("sceltaAdId")
    val teachingActivityChoiceId: Long? = null,

    @SerialName("sceId")
    val choiceRuleId: Int? = null,

    @SerialName("blkId")
    val blockId: Int? = null,

    @SerialName("conteggio")
    val count: Int? = null
)

@Serializable
data class Esse3StudyPlanHeaderPerRule(
    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("pianoId")
    val planId: Int? = null,

    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("schemaId")
    val schemaId: Long? = null,

    @SerialName("finregsceId")
    val finalRegulationChoiceId: Long? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("stato")
    val state: String? = null,

    @SerialName("statoDes")
    val stateDescription: String? = null,

    @SerialName("dataUltimaVarStato")
    val lastStateChangeDate: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipoPiano")
    val planType: String? = null,

    @SerialName("statutarioFlg")
    val statutoryFlag: Int? = null,

    @SerialName("coorte")
    val cohort: Int? = null,

    @SerialName("aaRevisioneId")
    val academicYearRevisionId: Int? = null,

    @SerialName("cdsStuId")
    val courseOfStudyStudentId: Long? = null,

    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    @SerialName("aaOrdStuId")
    val academicYearOrderStudentId: Long? = null,

    @SerialName("pdsStuId")
    val studyPlanStudentId: Long? = null,

    @SerialName("pdsStuCod")
    val studyPlanStudentCode: String? = null,

    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    @SerialName("pdsSceId")
    val studyPlanChoiceId: Long? = null,

    @SerialName("pdsSceCod")
    val studyPlanChoiceCode: String? = null,

    @SerialName("pdsSceDes")
    val studyPlanChoiceDescription: String? = null,

    @SerialName("aaDefId")
    val academicYearDefinitionId: Int? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    @SerialName("aptId")
    val aptId: Long? = null,

    @SerialName("aptCod")
    val aptCode: String? = null,

    @SerialName("aptDes")
    val aptDescription: String? = null,

    @SerialName("aaUltimaAttuazioneId")
    val academicYearLastImplementationId: Int? = null,

    @SerialName("dataUltimaAttuazione")
    val lastImplementationDate: String? = null,

    @SerialName("userUltimaAttuazione")
    val lastImplementationUser: String? = null,

    @SerialName("userControllo")
    val controlUser: String? = null,

    @SerialName("notaControllo")
    val controlNote: String? = null,

    @SerialName("noteSistema")
    val systemNotes: String? = null,

    @SerialName("noteUtente")
    val userNotes: String? = null,

    @SerialName("extCod")
    val externalCode: String? = null,

    @SerialName("sceltaId")
    val choiceId: Long? = null
)

@Serializable
data class Esse3PrerequisitesRegulation(
    @SerialName("regpropId")
    val proposalRuleId: Long? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("coorte")
    val cohort: Int? = null,

    @SerialName("aaRevisioneId")
    val academicYearRevisionId: Int? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("stato")
    val state: String? = null,

    @SerialName("extId")
    val externalId: Long? = null
)

@Serializable
data class Esse3PlansCountsFilters(
    @SerialName("conteggiSchemi")
    val schemaCounts: Boolean? = null,

    @SerialName("conteggiRegole")
    val ruleCounts: Boolean? = null,

    @SerialName("conteggiAttivita")
    val activityCounts: Boolean? = null,

    @SerialName("conteggiFinestre")
    val windowCounts: Boolean? = null,

    @SerialName("filtroSchemaCod")
    val schemaFilterCode: String? = null,

    @SerialName("filtroRegolaOrdNum")
    val ruleOrderNumberFilter: Int? = null,

    @SerialName("filtroADCod")
    val teachingActivityFilterCode: String? = null,

    @SerialName("filtroADCdsCod")
    val teachingActivityCourseOfStudyFilterCode: String? = null,

    @SerialName("filtroADAaOrdId")
    val teachingActivityAcademicYearOrderFilterId: Int? = null,

    @SerialName("filtroADPdsCod")
    val teachingActivityStudyPlanFilterCode: String? = null,

    @SerialName("filtroADAaOffId")
    val teachingActivityAcademicYearOfferFilterId: Int? = null,

    @SerialName("filtroFinregsceCod")
    val finalRegulationChoiceFilterCode: String? = null
)

@Serializable
data class Esse3PlansLinkedToRule(
    @SerialName("ordNum")
    val orderNumber: Int? = null,

    @SerialName("sceltaId")
    val choiceId: Long? = null,

    @SerialName("sceId")
    val choiceRuleId: Int? = null,

    @SerialName("sceDes")
    val choiceDescription: String? = null,

    @SerialName("tipSce")
    val choiceType: String? = null,

    @SerialName("tipSceDes")
    val choiceTypeDescription: String? = null,

    @SerialName("conteggio")
    val count: Int? = null
)

@Serializable
data class Esse3TeachingUnitChoiceRuleRegulation(
    @SerialName("regUdId")
    val teachingUnitRegistrationId: Long? = null,

    @SerialName("blkId")
    val blockId: Int? = null,

    @SerialName("sceltaAdId")
    val teachingActivityChoiceId: Long? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("tipoUdCod")
    val teachingUnitTypeCode: String? = null,

    @SerialName("tipo1")
    val type1: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("um1")
    val measurementUnit1: String? = null,

    @SerialName("qta1")
    val quantity1: Int? = null,

    @SerialName("tipo2")
    val type2: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("um2")
    val measurementUnit2: String? = null,

    @SerialName("qta2")
    val quantity2: Int? = null,

    @SerialName("tipo3")
    val type3: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("um3")
    val measurementUnit3: String? = null,

    @SerialName("qta3")
    val quantity3: Int? = null,

    @SerialName("moduli")
    val modules: List<Esse3TeachingUnitChoiceRule> = emptyList()
)

@Serializable
data class Esse3ChoiceRegulationWithSchemas(
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("coorte")
    val cohort: Int? = null,

    @SerialName("aaRevisioneId")
    val academicYearRevisionId: Int? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("stato")
    val state: String? = null,

    @SerialName("dataUltimaModifica")
    val lastModificationDate: String? = null,

    @SerialName("dataInserimento")
    val insertionDate: String? = null,

    @SerialName("finestreDiCompilazione")
    val compilationWindows: List<Esse3ChoiceRegulationWindow> = emptyList(),

    @SerialName("schemiDiPiano")
    val planSchemas: List<Esse3PlanSchema> = emptyList()
)

@Serializable
data class Esse3ActivityChoiceRule(
    @SerialName("sceltaAdId")
    val teachingActivityChoiceId: Long? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("sceltaId")
    val choiceId: Long? = null,

    @SerialName("regUdDesAuto")
    val teachingUnitAutoDescription: String? = null,

    @SerialName("regUdDesUte")
    val teachingUnitUserDescription: String? = null,

    @SerialName("blkId")
    val blockId: Int? = null,

    @SerialName("peso")
    val weight: Float? = null,

    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    @SerialName("regoleUD")
    val teachingUnitRules: List<Esse3TeachingUnitChoiceRuleRegulation> = emptyList()
)

@Serializable
data class Esse3ChoiceRegulation(
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("coorte")
    val cohort: Int? = null,

    @SerialName("aaRevisioneId")
    val academicYearRevisionId: Int? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("stato")
    val state: String? = null,

    @SerialName("dataUltimaModifica")
    val lastModificationDate: String? = null,

    @SerialName("dataInserimento")
    val insertionDate: String? = null
)

@Serializable
data class Esse3ContextualizedTeachingUnitKey(
    @SerialName("chiaveAdContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey,

    @SerialName("udId")
    val teachingUnitId: Long,

    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("udDesEng")
    val teachingUnitDescriptionEnglish: String? = null
)

@Serializable
data class Esse3ChoiceRuleAndFilter(
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    @SerialName("orId")
    val orId: Int? = null,

    @SerialName("andId")
    val andId: Int? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("tipoFiltroCod")
    val filterTypeCode: String? = null,

    @SerialName("tipoFiltroDes")
    val filterTypeDescription: String? = null,

    @SerialName("valSelId")
    val selectedValueId: Long? = null,

    @SerialName("valSelCod")
    val selectedValueCode: String? = null,

    @SerialName("valSelDes")
    val selectedValueDescription: String? = null,

    @SerialName("notFlg")
    val noteFlag: Int? = null,

    @SerialName("extId")
    val externalId: Long? = null
)

@Serializable
data class Esse3PrerequisiteConstraint(
    @SerialName("regpropVincId")
    val proposalRuleWinnerId: Long? = null,

    @SerialName("regpropId")
    val proposalRuleId: Long? = null,

    @SerialName("tipoVincolo")
    val constraintType: String? = null,

    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("settCod")
    val sectorCode: String? = null,

    @SerialName("regolePropOR")
    val orProposalRules: List<Esse3RulePropertyOR> = emptyList(),

    @SerialName("extId")
    val externalId: Long? = null
)

@Serializable
data class Esse3ChoiceRuleBlock(
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    @SerialName("blkId")
    val blockId: Int? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("linguaId")
    val languageId: Long? = null,

    @SerialName("linguaCod")
    val languageCode: String? = null,

    @SerialName("linguaNum")
    val languageNumber: Long? = null,

    @SerialName("defFlg")
    val definitionFlag: Int? = null,

    @SerialName("ctrlFlg")
    val controlFlag: Int? = null,

    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

    @SerialName("attivita")
    val activity: List<Esse3ActivityChoiceRule> = emptyList(),

    @SerialName("condizioni")
    val conditions: List<Esse3ChoiceRuleBlockCondition> = emptyList(),

    @SerialName("extId")
    val externalId: Long? = null
)

@Serializable
data class Esse3PlansLinkedToSchema(
    @SerialName("schemaCod")
    val schemaCode: String? = null,

    @SerialName("schemaDes")
    val schemaDescription: String? = null,

    @SerialName("schemaId")
    val schemaId: Long? = null,

    @SerialName("conteggio")
    val count: Int? = null
)

@Serializable
data class Esse3ChoiceRuleOrFilter(
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    @SerialName("orId")
    val orId: Int? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("filtriAND")
    val andFilters: List<Esse3ChoiceRuleAndFilter> = emptyList(),

    @SerialName("extId")
    val externalId: Long? = null
)

@Serializable
data class Esse3TeachingUnitChoiceRule(
    @SerialName("udId")
    val teachingUnitId: Long? = null,

    @SerialName("regUdId")
    val teachingUnitRegistrationId: Long? = null,

    @SerialName("blkId")
    val blockId: Int? = null,

    @SerialName("sceltaAdId")
    val teachingActivityChoiceId: Long? = null,

    @SerialName("statutFlg")
    val statuteFlag: Int? = null,

    @SerialName("obbligFlg")
    val mandatoryFlag: Int? = null,

    @SerialName("chiaveUDContestualizzata")
    val contextualizedTeachingUnitKey: Esse3ContextualizedTeachingUnitKey? = null,

    @SerialName("extId")
    val externalId: Long? = null
)

@Serializable
data class Esse3ChoiceRegulationWithCounts(
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("coorte")
    val cohort: Int? = null,

    @SerialName("aaRevisioneId")
    val academicYearRevisionId: Int? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("stato")
    val state: String? = null,

    @SerialName("dataUltimaModifica")
    val lastModificationDate: String? = null,

    @SerialName("dataInserimento")
    val insertionDate: String? = null,

    @SerialName("schemi")
    val schemas: List<Esse3PlansLinkedToSchema> = emptyList(),

    @SerialName("regole")
    val rules: List<Esse3PlansLinkedToRule> = emptyList(),

    @SerialName("attivita")
    val activity: List<Esse3PlansLinkedToActivities> = emptyList(),

    @SerialName("finestre")
    val windows: List<Esse3PlansLinkedToWindow> = emptyList()
)

@Serializable
data class Esse3ChoiceRuleWithDetails(
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("sceId")
    val choiceRuleId: Int? = null,

    @SerialName("ordNum")
    val orderNumber: Int? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("desEng")
    val descriptionEnglish: String? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("aptId")
    val aptId: Long? = null,

    @SerialName("aptCod")
    val aptCode: String? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipSce")
    val choiceType: String? = null,

    @SerialName("tipSceDes")
    val choiceTypeDescription: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipUnt")
    val teachingUnitType: String? = null,

    @SerialName("minUnt")
    val minTeachingUnit: Float? = null,

    @SerialName("maxUnt")
    val maxTeachingUnit: Float? = null,

    @SerialName("vinId")
    val constraintId: Int? = null,

    @SerialName("livello")
    val level: Int? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("modTAF")
    val tafMode: String? = null,

    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    @SerialName("opzFlg")
    val optionalFlag: Int? = null,

    @SerialName("sovranFlg")
    val overrideFlag: Int? = null,

    @SerialName("tesorettoFlg")
    val treasureFlag: Int? = null,

    @SerialName("pesoRosaRist")
    val restrictedListWeight: Long? = null,

    @SerialName("assegnazionePostiFlg")
    val seatAssignmentFlag: Int? = null,

    @SerialName("deliberaFlg")
    val resolutionFlag: Int? = null,

    @SerialName("azzeraCfuFlg")
    val resetCreditsFlag: Int? = null,

    @SerialName("parametriLogisticaFlg")
    val logisticsParametersFlag: Int? = null,

    @SerialName("notaPre")
    val preNote: String? = null,

    @SerialName("notaPreEng")
    val preNoteEnglish: String? = null,

    @SerialName("notaPreVisPubblFlg")
    val preNotePublicVisibleFlag: Int? = null,

    @SerialName("notaPost")
    val postNote: String? = null,

    @SerialName("notaPostEng")
    val postNoteEnglish: String? = null,

    @SerialName("notaPostVisPubblFlg")
    val postNotePublicVisibleFlag: Int? = null,

    @SerialName("extId")
    val externalId: Long? = null,

    @SerialName("extCod")
    val externalCode: String? = null,

    @SerialName("schemi")
    val schemas: List<Esse3PlanSchemaChoiceRule> = emptyList(),

    @SerialName("blocchi")
    val blocks: List<Esse3ChoiceRuleBlock> = emptyList(),

    @SerialName("condizioni")
    val conditions: List<Esse3ChoiceRuleCondition> = emptyList(),

    @SerialName("filtriOD")
    val orFilters: List<Esse3ChoiceRuleOrFilter> = emptyList()
)

@Serializable
data class Esse3PlansLinkedToWindow(
    @SerialName("finregsceCod")
    val finalRegulationChoiceCode: String? = null,

    @SerialName("finregsceDes")
    val finalRegulationChoiceDescription: String? = null,

    @SerialName("finregsceId")
    val finalRegulationChoiceId: Long? = null,

    @SerialName("conteggio")
    val count: Int? = null
)

@Serializable
data class Esse3PlanSchema(
    @SerialName("schemaId")
    val schemaId: Long? = null,

    @SerialName("schemaCod")
    val schemaCode: String? = null,

    @SerialName("schemaDes")
    val schemaDescription: String? = null,

    @SerialName("schemaDesEng")
    val schemaDescriptionEnglish: String? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    @SerialName("tipoApprovazione")
    val approvalType: Int? = null,

    @SerialName("gestAnnoCorsoFlg")
    val manageCourseYearFlag: Int? = null,

    @SerialName("statutarioFlg")
    val statutoryFlag: Int? = null,

    @SerialName("dataInizioVal")
    val evaluationStartDate: String? = null,

    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    @SerialName("condId")
    val conditionId: Long? = null,

    @SerialName("condCod")
    val conditionCode: String? = null,

    @SerialName("condDes")
    val conditionDescription: String? = null,

    @SerialName("orientId")
    val orientationId: Long? = null,

    @SerialName("orientCod")
    val orientationCode: String? = null,

    @SerialName("orientDes")
    val orientationDescription: String? = null,

    @SerialName("profCod")
    val professionCode: String? = null,

    @SerialName("profDes")
    val professionDescription: String? = null,

    @SerialName("aptId")
    val aptId: Long? = null,

    @SerialName("aptCod")
    val aptCode: String? = null,

    @SerialName("aptDes")
    val aptDescription: String? = null,

    @SerialName("webViewFlg")
    val webViewFlag: Int? = null
)

@Serializable
data class Esse3PlanSchemaWithDetails(
    @SerialName("schemaId")
    val schemaId: Long? = null,

    @SerialName("schemaCod")
    val schemaCode: String? = null,

    @SerialName("schemaDes")
    val schemaDescription: String? = null,

    @SerialName("schemaDesEng")
    val schemaDescriptionEnglish: String? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    @SerialName("tipoApprovazione")
    val approvalType: Int? = null,

    @SerialName("gestAnnoCorsoFlg")
    val manageCourseYearFlag: Int? = null,

    @SerialName("statutarioFlg")
    val statutoryFlag: Int? = null,

    @SerialName("dataInizioVal")
    val evaluationStartDate: String? = null,

    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    @SerialName("condId")
    val conditionId: Long? = null,

    @SerialName("condCod")
    val conditionCode: String? = null,

    @SerialName("condDes")
    val conditionDescription: String? = null,

    @SerialName("orientId")
    val orientationId: Long? = null,

    @SerialName("orientCod")
    val orientationCode: String? = null,

    @SerialName("orientDes")
    val orientationDescription: String? = null,

    @SerialName("profCod")
    val professionCode: String? = null,

    @SerialName("profDes")
    val professionDescription: String? = null,

    @SerialName("aptId")
    val aptId: Long? = null,

    @SerialName("aptCod")
    val aptCode: String? = null,

    @SerialName("aptDes")
    val aptDescription: String? = null,

    @SerialName("webViewFlg")
    val webViewFlag: Int? = null,

    @SerialName("regoleDiScelta")
    val choiceRules: List<Esse3ChoiceRuleSchemaWithDetails> = emptyList()
)

@Serializable
data class Esse3PlanSchemaChoiceRule(
    @SerialName("sceltaId")
    val choiceId: Long? = null,

    @SerialName("schemaId")
    val schemaId: Long? = null,

    @SerialName("schemaCod")
    val schemaCode: String? = null,

    @SerialName("ordNum")
    val orderNumber: Int? = null,

    @SerialName("ptSlotId")
    val ptSlotId: Long? = null,

    @SerialName("ptSlotCod")
    val ptSlotCode: String? = null,

    @SerialName("ptSlotDes")
    val ptSlotDescription: String? = null
)

@Serializable
data class Esse3UpdateChoiceRegulation(
    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("stato")
    val state: String? = null
)

@Serializable
data class Esse3ChoiceRegulationWithDetails(
    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("coorte")
    val cohort: Int? = null,

    @SerialName("aaRevisioneId")
    val academicYearRevisionId: Int? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("stato")
    val state: String? = null,

    @SerialName("dataUltimaModifica")
    val lastModificationDate: String? = null,

    @SerialName("dataInserimento")
    val insertionDate: String? = null,

    @SerialName("finesteDiCompilazione")
    val compilationWindows: List<Esse3ChoiceRegulationWindow> = emptyList(),

    @SerialName("schemiDiPiano")
    val planSchemas: List<Esse3PlanSchemaWithDetails> = emptyList()
)
