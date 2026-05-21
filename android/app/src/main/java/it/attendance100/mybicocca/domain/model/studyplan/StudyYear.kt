package it.attendance100.mybicocca.domain.model.studyplan

@JvmInline
value class StudyYear(val value: Int) : Comparable<StudyYear> {
    override fun compareTo(other: StudyYear): Int = value.compareTo(other.value)

    companion object {
        // Generic / info bucket. Esse3 emits courseYear == 0 for activities that
        // are not tied to a specific year of study (also used as the fallback
        // when the Esse3 plan omits courseYear).
        val Unknown = StudyYear(0)
    }
}
