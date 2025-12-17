package it.attendance100.mybicocca.data.mapper

import it.attendance100.mybicocca.data.remote.dto.elearning.*
import it.attendance100.mybicocca.data.remote.dto.elearning.CourseModule
import it.attendance100.mybicocca.data.remote.dto.elearning.CourseSection
import it.attendance100.mybicocca.domain.model.*
import java.time.*
import it.attendance100.mybicocca.domain.model.CourseModule as DomainCourseModule
import it.attendance100.mybicocca.domain.model.CourseSection as DomainCourseSection

fun Course.toDomain(): ElearningCourse {
  return ElearningCourse(
    id = this.id ?: 0,
    fullname = this.fullName ?: "",
    shortname = this.shortName ?: "",
    idNumber = null,
    summary = this.summary,
    categoryId = this.categoryId ?: 0
  )
}

fun Assignment.toDomain(): ElearningAssignment {
  return ElearningAssignment(
    id = this.id ?: 0,
    cmId = this.cmId ?: 0,
    courseId = this.course ?: 0,
    name = this.name ?: "",
    intro = this.intro,
    dueDate = this.dueDate?.let {
      LocalDateTime.ofInstant(Instant.ofEpochSecond(it.toLong()), ZoneId.systemDefault())
    } ?: LocalDateTime.MIN,
    allowSubmissionsFrom = this.allowSubmissionsFromDate?.let {
      LocalDateTime.ofInstant(Instant.ofEpochSecond(it.toLong()), ZoneId.systemDefault())
    }
  )
}

fun CourseSection.toDomain(): DomainCourseSection {
  return DomainCourseSection(
    id = this.id ?: 0,
    name = this.name ?: "",
    summary = this.summary,
    modules = this.modules?.map { it.toDomain() } ?: emptyList()
  )
}

fun CourseModule.toDomain(): DomainCourseModule {
  return DomainCourseModule(
    id = this.id ?: 0,
    name = this.name ?: "",
    type = this.modName ?: "",
    url = this.url?.toString(),
    isCompleted = this.completionData?.state == 1
  )
}
