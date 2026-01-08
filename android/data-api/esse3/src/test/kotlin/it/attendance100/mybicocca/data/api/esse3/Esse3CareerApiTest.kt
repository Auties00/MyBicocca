package it.attendance100.mybicocca.data.api.esse3

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class Esse3CareerApiTest : Esse3TestBase() {

    @Test
    suspend fun getStudyPlan() {
        val studyPlan = api.career.getStudyPlan()
        assertNotNull(studyPlan.status)
        assertNotNull(studyPlan.type)
        assertNotNull(studyPlan.lastModified)
        assertNotNull(studyPlan.courses)
    }

    @Test
    suspend fun printStudyPlan() {
        val pdfChannel = api.career.printStudyPlan()
        assertNotNull(pdfChannel)
    }

    @Test
    suspend fun getAcademicRecord() {
        val academicRecord = api.career.getAcademicRecord()
        assertNotNull(academicRecord.courses)
        assertTrue(academicRecord.unweightedGpa >= 0)
        assertTrue(academicRecord.weightedGpa >= 0)
    }

    @Test
    suspend fun getCourseInfo() {
        val academicRecord = api.career.getAcademicRecord()
        if (academicRecord.courses.isEmpty()) return

        val course = academicRecord.courses.first()
        val courseDetails = api.career.getCourseInfo(course)
        assertNotNull(courseDetails.code)
        assertNotNull(courseDetails.name)
        assertNotNull(courseDetails.units)
    }

    @Test
    suspend fun getCourseExamAttempts() {
        val academicRecord = api.career.getAcademicRecord()
        if (academicRecord.courses.isEmpty()) return

        val course = academicRecord.courses.first()
        val examAttempts = api.career.getCourseExamAttempts(course)
        assertNotNull(examAttempts)
    }
}
