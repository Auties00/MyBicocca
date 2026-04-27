package it.attendance100.mybicocca.ui.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoutes {
    // Auth flow
    @Serializable data object Splash : AppRoutes
    @Serializable data object Login : AppRoutes

    // Bottom nav tabs
    @Serializable data object Calendar : AppRoutes
    @Serializable data object Elearning : AppRoutes
    @Serializable data object Map : AppRoutes
    @Serializable data object Segreterie : AppRoutes

    // Profile & Settings
    @Serializable data object Profile : AppRoutes
    @Serializable data object Settings : AppRoutes
    @Serializable
    data object SettingsAppearance : AppRoutes
    @Serializable
    data object SettingsGeneral : AppRoutes
    @Serializable
    data object SettingsBehaviour : AppRoutes
    @Serializable
    data object SettingsSecurity : AppRoutes
    @Serializable
    data object SettingsDeveloper : AppRoutes
    @Serializable data object AppInfo : AppRoutes
    @Serializable data object LoginManager : AppRoutes

    // Segreterie sub-screens
    @Serializable data object Booking : AppRoutes
    @Serializable data class BookingDetail(val sessionId: Long) : AppRoutes
    @Serializable data object Booked : AppRoutes
    @Serializable data object Taxes : AppRoutes
    @Serializable data class TaxDetail(val chargeId: Long) : AppRoutes
    @Serializable data object StudyPlan : AppRoutes
    @Serializable
    data class StudyPlanEdit(
        val studentId: Long,
        val choiceRegulationId: Long,
        val schemaId: Long,
        val planId: Long,
    ) : AppRoutes
    @Serializable data class Transcript(val careerId: Long) : AppRoutes
    @Serializable data object ExamResults : AppRoutes
    @Serializable data object Attendance : AppRoutes
    @Serializable data object Internships : AppRoutes
    @Serializable data object Questionnaires : AppRoutes
    @Serializable data object DegreeAward : AppRoutes
    @Serializable data object SelfCertificates : AppRoutes
    @Serializable data object Reservations : AppRoutes
    @Serializable data object Isee : AppRoutes

    // Elearning sub-screens
    @Serializable data class CourseDetail(val courseId: Int) : AppRoutes
    @Serializable data class QuizDetail(val quizId: Int, val courseId: Int) : AppRoutes
    @Serializable data class AssignmentDetail(val assignId: Int, val courseId: Int) : AppRoutes
    @Serializable data class ForumDetail(val forumId: Int, val courseId: Int) : AppRoutes
    @Serializable data class DiscussionDetail(val discussionId: Int) : AppRoutes
    @Serializable data object Messaging : AppRoutes
    @Serializable data class ConversationDetail(val conversationId: Int) : AppRoutes

    // Map sub-screens
    @Serializable data class Room360View(val url: String, val roomName: String) : AppRoutes

    // NavHost idle destination (tab root visible behind)
    @Serializable data object TabRoot : AppRoutes

    // Detail screens
    @Serializable data class TeacherDetail(val teacherCode: String) : AppRoutes
    @Serializable data class EventDetail(val eventId: String) : AppRoutes
}
