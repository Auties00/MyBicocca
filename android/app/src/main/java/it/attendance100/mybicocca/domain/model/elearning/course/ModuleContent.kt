package it.attendance100.mybicocca.domain.model.elearning.course

import java.time.Instant

data class ModuleContent(
    val type: String?,
    val fileName: String?,
    val fileUrl: String?,
    val mimeType: String?,
    val sizeBytes: Long?,
    val timeModified: Instant?,
)
