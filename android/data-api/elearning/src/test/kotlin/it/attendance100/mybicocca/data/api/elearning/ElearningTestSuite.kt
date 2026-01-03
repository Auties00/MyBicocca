package it.attendance100.mybicocca.data.api.elearning

import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

@Suite
@SelectClasses(
    ElearningAssignApiTest::class,
    ElearningBadgeApiTest::class,
    ElearningCalendarApiTest::class,
    ElearningCompletionApiTest::class,
    ElearningCourseApiTest::class,
    ElearningForumApiTest::class,
    ElearningGradeApiTest::class,
    ElearningMessageApiTest::class,
    ElearningQuizApiTest::class,
    ElearningSiteApiTest::class,
    ElearningUserApiTest::class
)
class ElearningTestSuite
