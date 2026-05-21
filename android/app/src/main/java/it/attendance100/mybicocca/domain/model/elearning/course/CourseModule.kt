package it.attendance100.mybicocca.domain.model.elearning.course

import java.time.Instant

data class CourseModule(
    val cmId: Int,
    val instanceId: Int?,
    val name: String,
    val type: ModuleType,
    val description: String?,
    val url: String?,
    val iconUrl: String?,
    val visible: Boolean,
    val contents: List<ModuleContent>,
    val completion: CompletionState?,
    val dueAt: Instant?,
)
