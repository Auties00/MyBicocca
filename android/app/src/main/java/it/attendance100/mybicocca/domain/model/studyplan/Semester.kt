package it.attendance100.mybicocca.domain.model.studyplan

/**
 * Teaching semester of a planned course, decoded from EasyStaff period codes
 * ("S1"/"S2"). [Unknown] is the sentinel for periods that map to neither semester
 * (e.g. annual or custom periods).
 */
enum class Semester {
    First,
    Second,
    Unknown,
}
