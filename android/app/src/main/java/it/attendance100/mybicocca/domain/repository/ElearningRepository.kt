package it.attendance100.mybicocca.domain.repository

import androidx.lifecycle.LiveData
import it.attendance100.mybicocca.domain.model.CourseEvent

interface ElearningRepository {
    fun observeCourses(): LiveData<List<CourseEvent>>
    suspend fun syncCourses()
}