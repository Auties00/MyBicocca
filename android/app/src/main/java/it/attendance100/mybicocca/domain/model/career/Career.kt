package it.attendance100.mybicocca.domain.model.career

data class Career(
    val id: CareerId,
    val enrollmentTraitId: Long,
    val programId: Long,
    val easyStaffProgramCode: String?,
    val academicYearEnrollmentId: Long,
    val matricola: String,
    val description: String,
    val academicYear: Int,
    val status: CareerStatus,
)
