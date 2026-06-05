package it.attendance100.mybicocca.domain.model.internship

import java.time.LocalDate

data class InternshipAttachment(
    val id: Long,
    val title: String?,
    val description: String?,
    val fileName: String?,
    val sizeBytes: Long?,
    val modifiedOn: LocalDate?,
    val typeCode: String?,
    val ownedByStudent: Boolean,
)
