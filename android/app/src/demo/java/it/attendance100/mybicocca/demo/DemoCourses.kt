package it.attendance100.mybicocca.demo

import java.time.Instant
import java.time.LocalDateTime

/**
 * One taught course of the demo student's year. Shared by every demo repository so the same
 * courses appear in the timetable, on Moodle, among the exams and in the study plan.
 */
data class DemoCourse(
    val code: String,
    val name: String,
    val shortLabel: String,
    val cfu: Int,
    val teacher: String,
    val moodleCourseId: Int,
    val year: Int = 2,
    val academicYear: String = "2526",
)

object DemoCourses {
    val algorithms = DemoCourse("E3101Q106", "ALGORITMI E STRUTTURE DATI", "ASD", 8, "Laura Conti", 5101)
    val databases = DemoCourse("E3101Q107", "BASI DI DATI", "BD", 8, "Paolo Ferri", 5102)
    val operatingSystems = DemoCourse("E3101Q108", "SISTEMI OPERATIVI", "SO", 8, "Elena Marchetti", 5103)
    val statistics = DemoCourse("E3101Q109", "PROBABILITÀ E STATISTICA", "PS", 6, "Davide Gallo", 5104)
    val languages = DemoCourse("E3101Q110", "LINGUAGGI DI PROGRAMMAZIONE", "LP", 8, "Sara Colombo", 5105)

    // One course each from the first and third year, so the e-learning year filters have something to show.
    val programming = DemoCourse("E3101Q101", "PROGRAMMAZIONE 1", "PR1", 8, "Marta Bruno", 5001, year = 1, academicYear = "2425")
    val softwareEngineering = DemoCourse("E3101Q115", "INGEGNERIA DEL SOFTWARE", "IS", 8, "Giorgio Testa", 5201, year = 3)

    /** The courses being attended this semester: the ones with lectures, exam calls and attendance. */
    val current = listOf(algorithms, databases, operatingSystems, statistics, languages)

    val all = current + listOf(programming, softwareEngineering)
}

/** A Moodle assignment of [course]; [index] orders the ones a course has and keeps their ids apart. */
data class DemoAssignment(
    val course: DemoCourse,
    val index: Int,
    val title: String,
    val due: LocalDateTime,
    val submitted: Boolean = false,
) {
    val id: Int get() = course.moodleCourseId * 10 + index
    val cmId: Int get() = DemoElearning.cmId(course, DemoElearning.ASSIGNMENT + index)
    val dueAt: Instant get() = DemoClock.instant(due)
}
