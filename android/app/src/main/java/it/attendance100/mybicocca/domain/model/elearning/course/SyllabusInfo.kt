package it.attendance100.mybicocca.domain.model.elearning.course

data class SyllabusInfo(
    val type: String?,
    val credits: Int?,
    val hours: Int?,
    val language: String?,
    val level: CourseLevel?,
    val semester: Semester?,
    val disciplinarySector: String?,
    val objectives: String?,
    val summary: String?,
    val extendedProgramme: List<ProgrammeSection>,
    val prerequisites: String?,
    val teachingMethod: String?,
    val referenceMaterial: String?,
    val assessment: String?,
    val officeHours: String?,
) {
    val hasInfoTile: Boolean
        get() = credits != null || hours != null ||
            !language.isNullOrBlank() || level != null || semester != null

    companion object {
        val Empty = SyllabusInfo(
            type = null,
            credits = null,
            hours = null,
            language = null,
            level = null,
            semester = null,
            disciplinarySector = null,
            objectives = null,
            summary = null,
            extendedProgramme = emptyList(),
            prerequisites = null,
            teachingMethod = null,
            referenceMaterial = null,
            assessment = null,
            officeHours = null,
        )
    }
}
