package it.attendance100.mybicocca.data.repository

import androidx.lifecycle.LiveData
import it.attendance100.mybicocca.domain.model.CourseEvent
import it.attendance100.mybicocca.domain.repository.ElearningRepository as IElearningRepository

class ElearningRepository : IElearningRepository {
    override fun observeCourses(): LiveData<List<CourseEvent>> {
        TODO("Not yet implemented")
    }

    override suspend fun syncCourses() {
        TODO("Not yet implemented")
    }
}