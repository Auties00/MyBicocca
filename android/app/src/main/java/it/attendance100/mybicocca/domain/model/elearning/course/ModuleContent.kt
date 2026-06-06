package it.attendance100.mybicocca.domain.model.elearning.course

import java.time.Instant

data class ModuleContent(
    val type: String?,
    val fileName: String?,
    // Moodle virtual path within the module ("/", "/tutorial/", …). Needed to tell an HTML
    // entry point (index.html at "/") from its bundled image assets in a deeper folder.
    val filePath: String?,
    val fileUrl: String?,
    val mimeType: String?,
    val sizeBytes: Long?,
    val timeModified: Instant?,
)
