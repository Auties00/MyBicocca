package it.attendance100.mybicocca.data.remote.esse3.api

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
        assertTrue(pdfChannel.isPdf())
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
        for(course in academicRecord.courses) {
            val courseDetails = api.career.getCourseInfo(course)
            assertNotNull(courseDetails.code)
            assertNotNull(courseDetails.name)
            assertNotNull(courseDetails.units)
        }
    }

    @Test
    suspend fun getCourseExamAttempts() {
        val academicRecord = api.career.getAcademicRecord()
        for(course in academicRecord.courses) {
            val examAttempts = api.career.getCourseExamAttempts(course)
            assertNotNull(examAttempts)
        }
    }
}
