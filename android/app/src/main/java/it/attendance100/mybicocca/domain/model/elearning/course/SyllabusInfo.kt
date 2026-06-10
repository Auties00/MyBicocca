package it.attendance100.mybicocca.domain.model.elearning.course

/**
 * Structured facts extracted from a Moodle course sheet ("scheda dell'insegnamento"):
 * the metadata sidebar plus the parsed free-form fields. Every field is null or empty
 * when the sheet doesn't state it. Backs the tiles and sections of the course detail
 * syllabus page.
 *
 * @property type Kind of teaching activity (e.g. lecture vs. lab), first word of the
 * sheet's activity-type field.
 * @property credits CFU credit value.
 * @property hours Total teaching hours.
 * @property language Teaching language as written on the sheet.
 * @property level Degree level matched from the sheet's degree-type text.
 * @property semester Teaching period matched from the sheet's period text.
 * @property disciplinarySector Italian SSD disciplinary sector code.
 * @property objectives Learning objectives, plain text with newlines preserved.
 * @property summary Short course contents ("contenuti sintetici"), plain text.
 * @property extendedProgramme The detailed programme parsed into titled topic sections.
 * @property prerequisites Required prior knowledge, plain text.
 * @property teachingMethod How the course is delivered, plain text.
 * @property referenceMaterial Textbooks and study material, plain text.
 * @property assessment How the exam works ("verifica del profitto"), plain text.
 * @property officeHours Teacher availability, plain text.
 * @property sustainableDevelopmentGoals UN sustainable development goals the course
 * declares, plain text.
 */
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
    val sustainableDevelopmentGoals: String?,
) {
    /** Whether enough at-a-glance facts exist to render the info tile. */
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
            sustainableDevelopmentGoals = null,
        )
    }
}
