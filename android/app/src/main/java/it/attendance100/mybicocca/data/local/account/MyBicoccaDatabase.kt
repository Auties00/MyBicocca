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
import it.attendance100.mybicocca.data.local.elearning.message.ConversationEntity
import it.attendance100.mybicocca.data.local.elearning.message.ConversationMemberEntity
import it.attendance100.mybicocca.data.local.elearning.message.MessageDao
import it.attendance100.mybicocca.data.local.elearning.message.MessageEntity
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
        ConversationEntity::class,
        ConversationMemberEntity::class,
        MessageEntity::class,
        BadgeEntity::class,
        ElearningSyncStateEntity::class,
        VideoProgressEntity::class,
        TranscriptRowEntity::class,
        TranscriptStatsEntity::class,
        TranscriptSyncStateEntity::class,
        MapBuildingEntity::class,
        MapRoomEntity::class,
        MapRoomSyncStateEntity::class,
    ],
    version = 11,
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
    abstract fun elearningMessageDao(): MessageDao
    abstract fun elearningBadgeDao(): BadgeDao
    abstract fun elearningSyncStateDao(): ElearningSyncStateDao
    abstract fun elearningVideoProgressDao(): VideoProgressDao

    abstract fun mapBuildingDao(): MapBuildingDao
    abstract fun mapRoomDao(): MapRoomDao
    abstract fun mapRoomSyncStateDao(): MapRoomSyncStateDao
}
