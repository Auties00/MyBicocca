package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3AttendanceProcedureParameters(
    @SerialName("forzaStudentiX")
    val forceStudentsX: Boolean,

    @SerialName("forzaDataFreq")
    val forceAttendanceDate: Boolean,

    @SerialName("forzaStatoF")
    val forceStateFFlag: Boolean,

    @SerialName("forzaNonPreviste")
    val forceUnplanned: Boolean,

    @SerialName("partCod")
    val partialCode: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("filtriComuni")
    val commonFilters: Esse3FilterParameters? = null
)

@Serializable
data class Esse3SubstitutionProcedureParameters(
    @SerialName("filtriComuni")
    val commonFilters: Esse3FilterParameters? = null,

    @SerialName("adIdDaSostituire")
    val activityIdToReplace: Long? = null,

    @SerialName("adCodDaSostituire")
    val activityCodeToReplace: String? = null,

    @SerialName("forzaStatoS")
    val forceStateSFlag: Boolean,

    @SerialName("chiaveADdaInserire")
    val teachingActivityKeyToInsert: Esse3ContextualizedActivityKey? = null,

    @SerialName("udId")
    val teachingUnitId: Long? = null
)

@Serializable
data class Esse3CareerUpdateRow(
    @SerialName("aggcarrId")
    val careerUpdateId: Long? = null,

    @SerialName("aggcarrdettId")
    val careerUpdateDetailId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("adregId")
    val activityRegulationId: Long? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("coorte")
    val cohort: Int? = null,

    @SerialName("cdsStuId")
    val courseOfStudyStudentId: Long? = null,

    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    @SerialName("aaOrdStuId")
    val academicYearOrderStudentId: Int? = null,

    @SerialName("pdsStuId")
    val studyPlanStudentId: Long? = null,

    @SerialName("pdsStuCod")
    val studyPlanStudentCode: String? = null,

    @SerialName("pdsStuDes")
    val studyPlanStudentDescription: String? = null,

    @SerialName("adId")
    val activityId: Int? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    @SerialName("dataSup")
    val supDate: String? = null,

    @SerialName("esito")
    val outcome: Esse3Result? = null,

    @SerialName("profCod")
    val professionCode: String? = null,

    @SerialName("elaboraFlg")
    val processFlag: Int? = null,

    @SerialName("errMsg")
    val errorMessage: String? = null
)

@Serializable
data class Esse3SegmentProcedureParameters(
    @SerialName("forzaStatoFS")
    val forceStateFSFlag: Boolean,

    @SerialName("forzaStudentiX")
    val forceStudentsX: Boolean,

    @SerialName("forzaADLibereFlg")
    val forceFreeTeachingActivitiesFlag: Boolean,

    @SerialName("aaOffId")
    val academicYearOfferId: Long? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("filtriComuni")
    val commonFilters: Esse3FilterParameters? = null
)

@Serializable
data class Esse3CareerUpdateHeaderParameter(
    @SerialName("aggcarrId")
    val careerUpdateId: Long? = null,

    @SerialName("paramCod")
    val parameterCode: String? = null,

    @SerialName("paramDes")
    val parameterDescription: String? = null,

    @SerialName("valAlfa")
    val alphanumericValue: String? = null,

    @SerialName("valNum")
    val numericValue: Float? = null
)

@Serializable
data class Esse3ProcedureUpdateActivityOfferParameters(
    @SerialName("filtriComuni")
    val commonFilters: Esse3FilterParameters? = null,

    @SerialName("forzaADLibereFlg")
    val forceFreeTeachingActivitiesFlag: Boolean,

    @SerialName("adIdDaAggiornare")
    val activityIdToUpdate: Long,

    @SerialName("adCodDaAggiornare")
    val activityCodeToUpdate: String? = null
)

@Serializable
data class Esse3CareerUpdateHeader(
    @SerialName("aggcarrId")
    val careerUpdateId: Long? = null,

    @SerialName("procCod")
    val procedureCode: String? = null,

    @SerialName("stato")
    val state: Int? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    @SerialName("aaRegId")
    val academicYearRegulationId: Long? = null,

    @SerialName("parametri")
    val parameters: List<Esse3CareerUpdateHeaderParameter> = emptyList()
)

@Serializable
data class Esse3CareerUpdateLog(
    @SerialName("aggcarrId")
    val careerUpdateId: Long? = null,

    @SerialName("logTId")
    val logTypeId: Long? = null,

    @SerialName("logDes")
    val logDescription: String? = null
)

@Serializable
data class Esse3FilterParameters(
    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("aaRegId")
    val academicYearRegulationId: Long? = null
)

@Serializable
data class Esse3RemoveAttendanceProcedureParameters(
    @SerialName("forzaStudentiX")
    val forceStudentsX: Boolean,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("partCod")
    val partialCode: String? = null,

    @SerialName("filtriComuni")
    val commonFilters: Esse3FilterParameters? = null
)
