package it.attendance100.mybicocca.data.local.account

import androidx.room.Database
import androidx.room.RoomDatabase
import it.attendance100.mybicocca.data.local.calendar.CalendarDao
import it.attendance100.mybicocca.data.local.calendar.CalendarEventEntity
import it.attendance100.mybicocca.data.local.calendar.CalendarSyncStateDao
import it.attendance100.mybicocca.data.local.calendar.CalendarSyncStateEntity
import it.attendance100.mybicocca.data.local.elearning.assignment.AssignmentDao
import it.attendance100.mybicocca.data.local.elearning.assignment.AssignmentEntity
import it.attendance100.mybicocca.data.local.elearning.badge.BadgeDao
import it.attendance100.mybicocca.data.local.elearning.badge.BadgeEntity
import it.attendance100.mybicocca.data.local.elearning.course.ActivityCompletionEntity
import it.attendance100.mybicocca.data.local.elearning.course.CourseDao
import it.attendance100.mybicocca.data.local.elearning.course.CourseModuleEntity
import it.attendance100.mybicocca.data.local.elearning.course.CourseSectionEntity
import it.attendance100.mybicocca.data.local.elearning.course.CourseStaffEntity
import it.attendance100.mybicocca.data.local.elearning.course.CourseSyllabusEntity
import it.attendance100.mybicocca.data.local.elearning.course.EnrolledCourseEntity
import it.attendance100.mybicocca.data.local.elearning.deadline.DeadlineDao
import it.attendance100.mybicocca.data.local.elearning.deadline.DeadlineEntity
import it.attendance100.mybicocca.data.local.elearning.forum.DiscussionEntity
import it.attendance100.mybicocca.data.local.elearning.forum.ForumDao
import it.attendance100.mybicocca.data.local.elearning.forum.ForumEntity
import it.attendance100.mybicocca.data.local.elearning.forum.PostEntity
import it.attendance100.mybicocca.data.local.elearning.grade.CourseGradeOverviewEntity
import it.attendance100.mybicocca.data.local.elearning.grade.GradeDao
import it.attendance100.mybicocca.data.local.elearning.grade.GradeItemEntity
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizAttemptAnswerEntity
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizAttemptEntity
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizBestGradeEntity
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizDao
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizEntity
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateEntity
import it.attendance100.mybicocca.data.local.elearning.video.VideoProgressDao
import it.attendance100.mybicocca.data.local.elearning.video.VideoProgressEntity
import it.attendance100.mybicocca.data.local.map.MapBuildingDao
import it.attendance100.mybicocca.data.local.map.MapBuildingEntity
import it.attendance100.mybicocca.data.local.map.MapRoomDao
import it.attendance100.mybicocca.data.local.map.MapRoomEntity
import it.attendance100.mybicocca.data.local.map.MapRoomSyncStateDao
import it.attendance100.mybicocca.data.local.map.MapRoomSyncStateEntity
import it.attendance100.mybicocca.data.local.transcript.TranscriptDao
import it.attendance100.mybicocca.data.local.transcript.TranscriptRowEntity
import it.attendance100.mybicocca.data.local.transcript.TranscriptStatsEntity
import it.attendance100.mybicocca.data.local.transcript.TranscriptSyncStateDao
import it.attendance100.mybicocca.data.local.transcript.TranscriptSyncStateEntity
import it.attendance100.mybicocca.data.local.appointment.AppointmentReservationDao
import it.attendance100.mybicocca.data.local.appointment.AppointmentReservationEntity
import it.attendance100.mybicocca.data.local.library.LibraryReservationDao
import it.attendance100.mybicocca.data.local.library.LibraryReservationEntity
import it.attendance100.mybicocca.data.local.exam.BookedExamEntity
import it.attendance100.mybicocca.data.local.exam.ExamCacheDao
import it.attendance100.mybicocca.data.local.exam.ExamCallEntity
import it.attendance100.mybicocca.data.local.exam.ExamResultEntity
import it.attendance100.mybicocca.data.local.tax.IseeDeclarationEntity
import it.attendance100.mybicocca.data.local.tax.RefundEntity
import it.attendance100.mybicocca.data.local.tax.TaxCacheDao
import it.attendance100.mybicocca.data.local.tax.TaxChargeItemEntity
import it.attendance100.mybicocca.data.local.tax.TaxInvoiceEntity
import it.attendance100.mybicocca.data.local.tax.TaxSummaryEntity
import it.attendance100.mybicocca.data.local.enrollment.AnnualEnrollmentEntity
import it.attendance100.mybicocca.data.local.enrollment.EnrollmentCacheDao
import it.attendance100.mybicocca.data.local.questionnaire.ActivityQuestionnairesEntity
import it.attendance100.mybicocca.data.local.questionnaire.QuestionnaireActivityEntity
import it.attendance100.mybicocca.data.local.questionnaire.QuestionnaireCacheDao
import it.attendance100.mybicocca.data.local.questionnaire.QuestionnaireUnitEntity
import it.attendance100.mybicocca.data.local.document.AcademicTitleEntity
import it.attendance100.mybicocca.data.local.document.BadgeImageEntity
import it.attendance100.mybicocca.data.local.document.DocumentCacheDao
import it.attendance100.mybicocca.data.local.document.StudentBadgeEntity
import it.attendance100.mybicocca.data.local.document.TitleAttributeEntity
import it.attendance100.mybicocca.data.local.certificate.CertificateCacheDao
import it.attendance100.mybicocca.data.local.certificate.CertificateEntity

/**
 * The app's single Room database — the local source of truth every offline-first screen reads.
 *
 * Entity groups it hosts:
 * - accounts and careers: the saved Esse3 identities behind multi-account support
 * - calendar: merged events plus per-source sync state (EasyStaff lessons, Esse3 exams, and the
 *   other calendar sources)
 * - elearning: courses with sections/modules/staff/syllabi, activity completion, deadlines,
 *   assignments, quizzes (attempts, answers, best grades), forums (discussions, posts), grades,
 *   badges, video progress, and per-resource sync state
 * - transcript: Esse3 exam rows and aggregate stats with sync state
 * - map: campus buildings and rooms with sync state
 * - reservations: appointment (Portale Planning) and library-seat (Affluences) bookings, stored
 *   device-locally rather than per account
 * - offline mirrors of the live-first registry features (exam booking, taxes, enrollment
 *   history, questionnaires, badge/titles, certificates): the last successful read of each,
 *   served only when the device has no network
 *
 * Most tables are scoped by account id and/or career id. Schema export is disabled, so there is
 * no recorded schema history to validate incremental migrations against.
 */
@Database(
    entities = [
        AccountEntity::class,
        CareerEntity::class,
        CalendarEventEntity::class,
        CalendarSyncStateEntity::class,
        EnrolledCourseEntity::class,
        CourseSectionEntity::class,
        CourseModuleEntity::class,
        CourseStaffEntity::class,
        CourseSyllabusEntity::class,
        ActivityCompletionEntity::class,
        DeadlineEntity::class,
        AssignmentEntity::class,
        QuizEntity::class,
        QuizAttemptEntity::class,
        QuizAttemptAnswerEntity::class,
        QuizBestGradeEntity::class,
        ForumEntity::class,
        DiscussionEntity::class,
        PostEntity::class,
        GradeItemEntity::class,
        CourseGradeOverviewEntity::class,
        BadgeEntity::class,
        ElearningSyncStateEntity::class,
        VideoProgressEntity::class,
        TranscriptRowEntity::class,
        TranscriptStatsEntity::class,
        TranscriptSyncStateEntity::class,
        MapBuildingEntity::class,
        MapRoomEntity::class,
        MapRoomSyncStateEntity::class,
        AppointmentReservationEntity::class,
        LibraryReservationEntity::class,
        BookedExamEntity::class,
        ExamCallEntity::class,
        ExamResultEntity::class,
        TaxInvoiceEntity::class,
        TaxChargeItemEntity::class,
        TaxSummaryEntity::class,
        IseeDeclarationEntity::class,
        RefundEntity::class,
        AnnualEnrollmentEntity::class,
        QuestionnaireActivityEntity::class,
        ActivityQuestionnairesEntity::class,
        QuestionnaireUnitEntity::class,
        StudentBadgeEntity::class,
        AcademicTitleEntity::class,
        TitleAttributeEntity::class,
        BadgeImageEntity::class,
        CertificateEntity::class,
    ],
    version = 33,
    exportSchema = false,
)
abstract class MyBicoccaDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun calendarDao(): CalendarDao
    abstract fun calendarSyncStateDao(): CalendarSyncStateDao
    abstract fun transcriptDao(): TranscriptDao
    abstract fun transcriptSyncStateDao(): TranscriptSyncStateDao

    abstract fun elearningCourseDao(): CourseDao
    abstract fun elearningDeadlineDao(): DeadlineDao
    abstract fun elearningAssignmentDao(): AssignmentDao
    abstract fun elearningQuizDao(): QuizDao
    abstract fun elearningForumDao(): ForumDao
    abstract fun elearningGradeDao(): GradeDao
    abstract fun elearningBadgeDao(): BadgeDao
    abstract fun elearningSyncStateDao(): ElearningSyncStateDao
    abstract fun elearningVideoProgressDao(): VideoProgressDao

    abstract fun mapBuildingDao(): MapBuildingDao
    abstract fun mapRoomDao(): MapRoomDao
    abstract fun mapRoomSyncStateDao(): MapRoomSyncStateDao

    abstract fun appointmentReservationDao(): AppointmentReservationDao

    abstract fun libraryReservationDao(): LibraryReservationDao

    abstract fun examCacheDao(): ExamCacheDao
    abstract fun taxCacheDao(): TaxCacheDao
    abstract fun enrollmentCacheDao(): EnrollmentCacheDao
    abstract fun questionnaireCacheDao(): QuestionnaireCacheDao
    abstract fun documentCacheDao(): DocumentCacheDao
    abstract fun certificateCacheDao(): CertificateCacheDao
}
