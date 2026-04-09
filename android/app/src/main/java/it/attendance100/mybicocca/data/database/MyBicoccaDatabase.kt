package it.attendance100.mybicocca.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import it.attendance100.mybicocca.data.database.dao.AppointmentDao
import it.attendance100.mybicocca.data.database.dao.AssignmentDao
import it.attendance100.mybicocca.data.database.dao.AttendanceDao
import it.attendance100.mybicocca.data.database.dao.BadgeDao
import it.attendance100.mybicocca.data.database.dao.CalendarDao
import it.attendance100.mybicocca.data.database.dao.CampusDao
import it.attendance100.mybicocca.data.database.dao.CareerDao
import it.attendance100.mybicocca.data.database.dao.CourseDao
import it.attendance100.mybicocca.data.database.dao.DegreeAwardDao
import it.attendance100.mybicocca.data.database.dao.EvaluationDao
import it.attendance100.mybicocca.data.database.dao.ExamDao
import it.attendance100.mybicocca.data.database.dao.ForumDao
import it.attendance100.mybicocca.data.database.dao.InternshipDao
import it.attendance100.mybicocca.data.database.dao.IseeDao
import it.attendance100.mybicocca.data.database.dao.MessagingDao
import it.attendance100.mybicocca.data.database.dao.QuizDao
import it.attendance100.mybicocca.data.database.dao.ReferenceDao
import it.attendance100.mybicocca.data.database.dao.StudyPlanDao
import it.attendance100.mybicocca.data.database.dao.TaxDao
import it.attendance100.mybicocca.data.database.dao.TeacherDao
import it.attendance100.mybicocca.data.database.dao.TranscriptDao
import it.attendance100.mybicocca.data.database.dao.UserDao
import it.attendance100.mybicocca.data.model.appointment.Appointment
import it.attendance100.mybicocca.data.model.assignment.Assignment
import it.attendance100.mybicocca.data.model.attendance.AttendanceRecord
import it.attendance100.mybicocca.data.model.badge.Badge
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.campus.Building
import it.attendance100.mybicocca.data.model.campus.Room
import it.attendance100.mybicocca.data.model.career.Career
import it.attendance100.mybicocca.data.model.course.Course
import it.attendance100.mybicocca.data.model.course.CourseGrade
import it.attendance100.mybicocca.data.model.degreeaward.CommitteeApplication
import it.attendance100.mybicocca.data.model.evaluation.EvaluationEntry
import it.attendance100.mybicocca.data.model.exam.ExamBooking
import it.attendance100.mybicocca.data.model.exam.ExamCall
import it.attendance100.mybicocca.data.model.forum.Forum
import it.attendance100.mybicocca.data.model.internship.InternshipApplication
import it.attendance100.mybicocca.data.model.isee.IseeDeclaration
import it.attendance100.mybicocca.data.model.messaging.Conversation
import it.attendance100.mybicocca.data.model.quiz.Quiz
import it.attendance100.mybicocca.data.model.reference.AcademicYear
import it.attendance100.mybicocca.data.model.reference.CourseType
import it.attendance100.mybicocca.data.model.reference.DidacticStructure
import it.attendance100.mybicocca.data.model.reference.StudyProgram
import it.attendance100.mybicocca.data.model.reference.TeachingArea
import it.attendance100.mybicocca.data.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.data.model.studyplan.StudyPlanHeader
import it.attendance100.mybicocca.data.model.tax.Invoice
import it.attendance100.mybicocca.data.model.tax.TaxCharge
import it.attendance100.mybicocca.data.model.teacher.Teacher
import it.attendance100.mybicocca.data.model.transcript.RecordBookRow
import it.attendance100.mybicocca.data.model.transcript.RecordBookStats
import it.attendance100.mybicocca.data.model.user.User

@Database(
    entities = [
        User::class,
        Course::class,
        CourseGrade::class,
        Assignment::class,
        Quiz::class,
        Forum::class,
        Badge::class,
        Conversation::class,
        Career::class,
        RecordBookRow::class,
        RecordBookStats::class,
        StudyPlanHeader::class,
        PlannedCourse::class,
        TaxCharge::class,
        Invoice::class,
        CalendarEvent::class,
        ExamCall::class,
        ExamBooking::class,
        Building::class,
        Room::class,
        Teacher::class,
        AttendanceRecord::class,
        CommitteeApplication::class,
        InternshipApplication::class,
        EvaluationEntry::class,
        Appointment::class,
        AcademicYear::class,
        StudyProgram::class,
        TeachingArea::class,
        CourseType::class,
        DidacticStructure::class,
        IseeDeclaration::class,
    ],
    version = 9,
    exportSchema = false,
)
@TypeConverters(MyBicoccaTypeConverters::class)
abstract class MyBicoccaDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun quizDao(): QuizDao
    abstract fun forumDao(): ForumDao
    abstract fun badgeDao(): BadgeDao
    abstract fun messagingDao(): MessagingDao
    abstract fun careerDao(): CareerDao
    abstract fun transcriptDao(): TranscriptDao
    abstract fun studyPlanDao(): StudyPlanDao
    abstract fun taxDao(): TaxDao
    abstract fun calendarDao(): CalendarDao
    abstract fun examDao(): ExamDao
    abstract fun campusDao(): CampusDao
    abstract fun teacherDao(): TeacherDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun degreeAwardDao(): DegreeAwardDao
    abstract fun internshipDao(): InternshipDao
    abstract fun evaluationDao(): EvaluationDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun referenceDao(): ReferenceDao
    abstract fun iseeDao(): IseeDao
}
