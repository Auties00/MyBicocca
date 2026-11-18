# E-Learning APIs

Kotlin clients for the university's Moodle, [elearning.unimib.it](https://elearning.unimib.it/),
and for Kaltura's CDN, which serves the lecture recordings embedded in it. Every file is generated
by `common/codegen` and must not be edited:

| Facade        | Spec                       | What it covers                                                                    |
|---------------|----------------------------|-----------------------------------------------------------------------------------|
| `MoodleApi`   | `common/specs/moodle`      | The web service functions, plus the file download and draft area upload endpoints |
| `MoodleWebApi`| `common/specs/moodle-web`  | The pages no web service exposes, read with the generated `MoodleWebScrapers`      |
| `KalturaApi`  | `common/specs/kaltura`     | The HLS playlists of Kaltura entries                                              |

The SDK neither logs in nor chains requests: the university SSO login, and flows such as
marking a presence or resolving a video's stream, live in the app (`data/elearning`). The app
passes the web service token to each call, and installs the `MoodleSession` cookie on the client
of the pages that show the user's own data, such as attendance and video resources.

## Web service functions

Most data comes from Moodle's REST web services, called with the user's token:

```kotlin
val moodle = MoodleApi()
val auth = MoodleTokenAuth(token)
val userId = moodle.site.getSiteInfo(auth = auth).userId
val courses = moodle.course.getUsersCourses(userId, auth = auth)
```

Moodle reports failures with HTTP 200, which become a `MoodleException` with Moodle's error code
(`MoodleException.INVALID_TOKEN`, …). `moodle.language` sets the langpack strings are formatted in.

## Pages

What no web service exposes is read out of Moodle's pages (see `common/specs/moodle-web`):

- `catalog`: a course's public page (syllabus, staff, enrolment methods), for any course, as
  Moodle renders it to anonymous visitors. The course sheet ("Scheda del corso") comes from the
  course's custom fields instead (`moodle.course.getCoursesByField`).
- `media`: the Kaltura player of a video resource, which names the video's Kaltura entry.
- `attendance`: the attendance plugin's `view.php`, which tells apart Moodle's login page, and
  the records of the templates its mobile app bridge renders. The plugin's mobile view of an
  activity is never rendered for reading: doing so marks the student present in open sessions
  that assign a status by themselves.

## Tests

The tests call the live sites and are tagged `live`. `MoodleWebApiTest` and `KalturaApiTest`
need nothing; the web service tests need a token in `MOODLE_TOKEN`:

```sh
MOODLE_TOKEN=... ./gradlew -p data-api test -PincludeLive --tests "*.MoodleApiTest" --tests "*.MoodleWebApiTest" --tests "*.KalturaApiTest"
```
