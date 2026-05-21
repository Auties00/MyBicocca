package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state

import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogCourse
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogNode
import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogSection

sealed interface AddCourseLevel {
    data class Root(val sections: List<CatalogSection>) : AddCourseLevel
    data class Inside(
        val node: CatalogNode,
        val children: List<CatalogNode>,
        val courses: List<CourseItem>,
    ) : AddCourseLevel
}

data class CourseItem(
    val course: CatalogCourse,
    val status: EnrolmentStatus,
)
