package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappUserExamsCareerEntry(
    @SerialName("yearFreqId")
    val yearFreqId: Int? = null,

    @SerialName("activityCode")
    val activityCode: String? = null,

    @SerialName("activityDescr")
    val activityDescr: String? = null,

    @SerialName("courseYear")
    val courseYear: Int? = null,

    @SerialName("order")
    val order: Int? = null,

    @SerialName("cfu")
    val cfu: String? = null, // JSON sends "8.0" as a String

    @SerialName("statusDescr")
    val statusDescr: String? = null,

    @SerialName("typeExamCode")
    val typeExamCode: String? = null,

    @SerialName("typeExamDescr")
    val typeExamDescr: String? = null,

    @SerialName("typeCourseCode")
    val typeCourseCode: String? = null,

    @SerialName("typeCourseDescr")
    val typeCourseDescr: String? = null,

    @SerialName("status")
    val status: String? = null, // "S" for Superata, "F" for Frequentata/Future

    @SerialName("year")
    val year: Int? = null,

    @SerialName("dateExam")
    val dateExam: String? = null,

    @SerialName("valueType")
    val valueType: String? = null,

    @SerialName("grade")
    val grade: String? = null, // Note: In 'exams', grade is a String (e.g., "30L", "24")

    @SerialName("laudFlag")
    val laudFlag: Int? = null,

    @SerialName("evaluationCode")
    val evaluationCode: String? = null,

    @SerialName("evaluationDescr")
    val evaluationDescr: String? = null,

    @SerialName("editable")
    val editable: Boolean? = null,

    @SerialName("teacherId")
    val teacherId: String? = null,

    @SerialName("teacherCode")
    val teacherCode: String? = null,

    @SerialName("teacherFiscalCode")
    val teacherFiscalCode: String? = null,

    @SerialName("teacherFullname")
    val teacherFullname: String? = null,

    @SerialName("teacherEmail")
    val teacherEmail: String? = null,

    @SerialName("teacherKey")
    val teacherKey: String? = null
)