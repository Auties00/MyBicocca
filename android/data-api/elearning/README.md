# E-Learning API Module

Kotlin API client for the [E-Learning](https://elearning.unimib.it/) platform (Moodle).

## Overview

This module provides a type-safe Kotlin client for interacting with the university's Moodle e-learning platform. It uses the official [Moodle Web Services API](https://docs.moodle.org/dev/Web_service_API_functions) with additional HTML scraping for course catalog browsing.

## Authentication

The API requires a 32-character web service token obtained through SSO authentication:

```kotlin
val api = ElearningApi()

// 1. Get the SSO authentication URL
val authUrl = api.site.getAuthUrl()

// 2. Open authUrl in a browser - user logs in via university SSO
// 3. After login, browser redirects to: moodlemobile://token=BASE64_DATA
// 4. Decode BASE64_DATA to get: SITE_URL:::TOKEN:::OTHER_DATA
// 5. Extract the 32-character TOKEN

val wsToken = "..." // extracted token
val siteInfo = api.site.getSiteInfo(wsToken)
val userId = siteInfo.userId
```

## API Coverage

| Sub-API      | Description                                |
|--------------|--------------------------------------------|
| `site`       | SSO authentication, site info              |
| `users`      | User profiles and preferences              |
| `courses`    | Enrolled courses, contents, enrollment     |
| `quizzes`    | Quiz attempts, questions, results          |
| `assignments`| Assignment submissions, grades, feedback   |
| `forums`     | Forum discussions, posts, replies          |
| `calendar`   | Calendar events and deadlines              |
| `badges`     | Earned badges and criteria                 |
| `completion` | Course and activity completion status      |
| `grades`     | Grade items and course grades              |
| `messages`   | Private messaging and notifications        |

## Usage

```kotlin
val api = ElearningApi()
val wsToken = "..." // 32-character token

// Get site info and user ID
val siteInfo = api.site.getSiteInfo(wsToken)
val userId = siteInfo.userId

// Get enrolled courses
val courses = api.courses.getUserCourses(wsToken, userId)

// Get course contents
val contents = api.courses.getCourseContents(wsToken, courseId)

// Get assignments for a course
val assignments = api.assignments.getAssignments(wsToken, listOf(courseId))

// Get quiz attempts
val attempts = api.quizzes.getUserAttempts(wsToken, quizId, userId)

// Get calendar events
val events = api.calendar.getUpcomingEvents(wsToken)

// Get course grades
val grades = api.grades.getGradeItems(wsToken, courseId, userId)

// Get forum discussions
val discussions = api.forums.getDiscussions(wsToken, forumId)

// Don't forget to close when done
api.close()
```

## Implementation Details

### Moodle Web Services API

Most operations use the official Moodle Web Services REST API:

```
POST https://elearning.unimib.it/webservice/rest/server.php?moodlewsrestformat=json&wsfunction=FUNCTION_NAME
```

Request parameters are sent as form data with the web service token.

### HTML Scraping

Three operations use HTML scraping for course catalog browsing (no authentication required):

- `courses.getCoursesAreas()` - Scrapes course categories from the homepage
- `courses.getCourseCategoryContents()` - Scrapes courses within a category
- `courses.getCoursePublicInfo(courseId)` - Scrapes the public info page for a course (syllabus, staff, "Scheda del corso" metadata, enrolment methods); works for courses the user is not enrolled in
