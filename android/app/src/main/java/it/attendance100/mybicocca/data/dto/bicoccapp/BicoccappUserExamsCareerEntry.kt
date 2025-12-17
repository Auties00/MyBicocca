package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappUserExamsCareerEntry(
    @SerializedName("yearFreqId")
    val yearFreqId: Int? = null,

    @SerializedName("activityCode")
    val activityCode: String? = null,

    @SerializedName("activityDescr")
    val activityDescr: String? = null,

    @SerializedName("courseYear")
    val courseYear: Int? = null,

    @SerializedName("order")
    val order: Int? = null,

    @SerializedName("cfu")
    val cfu: String? = null, // JSON sends "8.0" as a String

    @SerializedName("statusDescr")
    val statusDescr: String? = null,

    @SerializedName("typeExamCode")
    val typeExamCode: String? = null,

    @SerializedName("typeExamDescr")
    val typeExamDescr: String? = null,

    @SerializedName("typeCourseCode")
    val typeCourseCode: String? = null,

    @SerializedName("typeCourseDescr")
    val typeCourseDescr: String? = null,

    @SerializedName("status")
    val status: String? = null, // "S" for Superata, "F" for Frequentata/Future

    @SerializedName("year")
    val year: Int? = null,

    @SerializedName("dateExam")
    val dateExam: String? = null,

    @SerializedName("valueType")
    val valueType: String? = null,

    @SerializedName("grade")
    val grade: String? = null, // Note: In 'exams', grade is a String (e.g., "30L", "24")

    @SerializedName("laudFlag")
    val laudFlag: Int? = null,

    @SerializedName("evaluationCode")
    val evaluationCode: String? = null,

    @SerializedName("evaluationDescr")
    val evaluationDescr: String? = null,

    @SerializedName("editable")
    val editable: Boolean? = null,

    @SerializedName("teacherId")
    val teacherId: String? = null,

    @SerializedName("teacherCode")
    val teacherCode: String? = null,

    @SerializedName("teacherFiscalCode")
    val teacherFiscalCode: String? = null,

    @SerializedName("teacherFullname")
    val teacherFullname: String? = null,

    @SerializedName("teacherEmail")
    val teacherEmail: String? = null,

    @SerializedName("teacherKey")
    val teacherKey: String? = null
)