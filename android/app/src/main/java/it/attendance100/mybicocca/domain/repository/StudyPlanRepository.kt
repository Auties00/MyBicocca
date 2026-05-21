package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear

interface StudyPlanRepository {
    suspend fun getPlannedCoursesForActiveCareer(careerId: CareerId): List<PlannedCourse>
    suspend fun getActivityYearByCodeForCareer(careerId: CareerId): Map<String, StudyYear>
}
