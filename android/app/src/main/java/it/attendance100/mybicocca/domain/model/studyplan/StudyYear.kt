package it.attendance100.mybicocca.domain.model.studyplan

/**
 * Year of the course (anno di corso) a plan activity belongs to, 1-based.
 *
 * @property value The raw year; 0 is the [Unknown] sentinel.
 */
@JvmInline
value class StudyYear(val value: Int) : Comparable<StudyYear> {
    override fun compareTo(other: StudyYear): Int = value.compareTo(other.value)

    companion object {
        /**
         * Generic / info bucket. Esse3 emits `courseYear == 0` for activities that are
         * not tied to a specific year of study; also the fallback when the Esse3 plan
         * omits the course year.
         */
        val Unknown = StudyYear(0)
    }
}
