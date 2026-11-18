package it.attendance100.mybicocca.data.elearning

import it.attendance100.mybicocca.data.remote.elearning.api.MoodleApi
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleWebAttendanceApi
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleCourseModule
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleNamedValue
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleOption
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleTokenAuth
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleWebAttendanceMarkPage
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleWebAttendanceSessionForm
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleWebAttendanceStatusOption
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleWebAttendanceSummary
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleWebAttendanceView
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleWebScrapers
import org.jsoup.Jsoup
import java.util.concurrent.ConcurrentHashMap

/**
 * Attendance (`mod_attendance`). The plugin's `mod_attendance_*` web service functions are only
 * in its own "Attendance" service, not in the mobile app's, so the mobile token can't call them
 * (`accessexception`).
 *
 * The summary and the sessions open for marking are read from the activity's `view.php` page;
 * the take form and the marking itself go through the site-plugins bridge
 * (`tool_mobile_get_content`) the official Moodle app uses.
 *
 * Neither way of reading is free of side effects, so callers should read only when the user
 * asks to:
 * - Rendering the plugin's mobile view records the student's presence in every open session
 *   that assigns a status by itself. It is only used for marking.
 * - Every `view.php` load logs a `session_report_viewed` event in the course. When a teacher
 *   sets a session's automatic marking to "Yes", the plugin's cron marks present every user
 *   with any course log entry while the session runs, so reading the page then counts as
 *   attending, as opening the course in a browser or in the Moodle app would.
 *
 * @param moodle The web service functions the plugin's views are reached through.
 * @param pages The plugin's pages, sent with the user's browser session.
 */
class ElearningAttendance(
    private val moodle: MoodleApi,
    private val pages: MoodleWebAttendanceApi,
) {
    /** Localized future-session link texts, by language. */
    private val futureLinkTexts = ConcurrentHashMap<String, String>()

    /** The attendance activities of a course, possibly none. */
    suspend fun getAttendanceModules(wsToken: String, courseId: Int): List<MoodleCourseModule> {
        // Only attendance activities, and without their file listings, to keep the response small.
        val sections = moodle.course.getContents(
            courseId = courseId,
            options = listOf(MoodleOption("modname", MODULE_NAME), MoodleOption("excludecontents", "1")),
            auth = MoodleTokenAuth(wsToken),
        )
        return sections.flatMap { it.modules }.filter { it.modName == MODULE_NAME }
    }

    /**
     * Reads the user's statistics for an attendance activity, with the sessions open for
     * self-marking, from its `view.php` page. The page never records a status itself: marking
     * happens on the pages its links lead to. It does log a course event, which matters for
     * automatic marking, see [ElearningAttendance]. It is asked for in the web services'
     * language, so that its session times match the mobile templates'.
     *
     * @param wsToken The web service token, to look up the future-session link's text.
     * @return The page, [ElearningAttendanceViewResponse.RequiresReauth] when the browser session
     *   expired, or [ElearningAttendanceViewResponse.Unavailable] when Moodle sent the user
     *   elsewhere.
     */
    suspend fun getAttendanceSummary(wsToken: String, moduleId: Int): ElearningAttendanceViewResponse {
        val language = moodle.language
        val view = pages.getAttendanceView(moduleId, language)
        return classify(view, futureLinkText = runCatching { futureLinkText(wsToken, language) }.getOrNull())
    }

    /**
     * Looks up, through `core_get_component_strings` (a site-level read that logs nothing in a
     * course), the text of the link the session log shows for a session not open yet.
     */
    private suspend fun futureLinkText(wsToken: String, language: String): String? {
        futureLinkTexts[language]?.let { return it }
        val text = moodle.site.componentStrings(COMPONENT, language, MoodleTokenAuth(wsToken))
            .firstOrNull { it.stringId == FUTURE_LINK_STRING_ID }
            ?.string
            ?: return null
        futureLinkTexts[language] = text
        return text
    }

    /**
     * Describes the open sessions of an activity, refused ones included.
     *
     * Each candidate's take form comes from the plugin's mobile bridge (`mobile_user_form`, which
     * never records anything and logs nothing in the course), telling whether the session asks
     * for a password or QR code and which statuses it offers, or why it can't be marked right now
     * ([ElearningAttendanceMarkableSession.refusal]). A session assigning its status by itself
     * without a password comes back with no statuses.
     *
     * @param sessions The candidates, from [ElearningAttendanceViewResponse.Success.submittableSessions].
     * @return The described sessions, in the same order; one whose form can't be loaded is left out.
     */
    suspend fun getMarkableSessions(
        wsToken: String,
        courseId: Int,
        moduleId: Int,
        sessions: List<ElearningAttendanceSubmittableSession>,
    ): List<ElearningAttendanceMarkableSession> = sessions.mapNotNull { session ->
        val html = runCatching {
            template(
                wsToken,
                USER_FORM_METHOD,
                mapOf(
                    "cmid" to moduleId.toString(),
                    "courseid" to courseId.toString(),
                    "sessid" to session.sessionId,
                    // Below 44000 the plugin serves its "ionic5" form, which puts each status
                    // label beside its ion-radio instead of inside it.
                    "appversioncode" to APP_VERSION_CODE,
                ),
            )
        }.getOrNull() ?: return@mapNotNull null
        describe(session, MoodleWebScrapers.attendanceSessionForm(Jsoup.parse(html)))
    }

    /**
     * Records the student's presence at an open session through the mobile bridge, as the Moodle
     * app does when a student taps "submit attendance". Rendering that view also records the
     * presence in every other open session of the activity assigning a status by itself without
     * a password, as the Moodle app does.
     *
     * The password typed or read from the session's static QR code goes in [studentPass]; the
     * plugin's mobile handler doesn't accept the rotating QR codes. Sessions assigning their
     * status by themselves accept any [statusId].
     *
     * The view only lists sessions starting between 6 hours ago and 24 hours from now (the site's
     * defaults), and a session outside it is not marked; that comes back as
     * [ElearningAttendanceMarkResult.Unconfirmed], never as marked.
     *
     * @param session The session to mark, as found on `view.php`.
     */
    suspend fun markSession(
        wsToken: String,
        courseId: Int,
        moduleId: Int,
        session: ElearningAttendanceSubmittableSession,
        statusId: String,
        studentPass: String? = null,
    ): ElearningAttendanceMarkResult {
        val arguments = mapOf(
            "cmid" to moduleId.toString(),
            "courseid" to courseId.toString(),
            "sessid" to session.sessionId,
            "status" to statusId,
            "studentpass" to studentPass.orEmpty(),
            "appversioncode" to APP_VERSION_CODE,
        )
        val html = template(wsToken, VIEW_METHOD, arguments) ?: return ElearningAttendanceMarkResult.Failed(reason = null)
        return classify(MoodleWebScrapers.attendanceMarkPage(Jsoup.parse(html)), session)
    }

    private suspend fun template(wsToken: String, method: String, arguments: Map<String, String>): String? =
        moodle.mobile.getContent(
            component = COMPONENT,
            method = method,
            args = arguments.map { (name, value) -> MoodleNamedValue(name, value) },
            auth = MoodleTokenAuth(wsToken),
        ).templates.firstOrNull()?.html

    internal companion object {
        private const val MODULE_NAME = "attendance"
        private const val COMPONENT = "mod_attendance"
        private const val VIEW_METHOD = "mobile_view_activity"
        private const val USER_FORM_METHOD = "mobile_user_form"

        /** Selects the "latest" (Ionic 5+) templates the modern app expects. */
        private const val APP_VERSION_CODE = "45000"

        /** Id Moodle gives the guest user, whom an expired `MoodleSession` falls back to. */
        private const val GUEST_USER_ID = 1

        /** The string key of the link the session log shows for a session not open yet. */
        private const val FUTURE_LINK_STRING_ID = "submitattendancefuture"

        /** The keys of the plugin's messages for an attempt that recorded nothing. */
        private const val MESSAGE_WRONG_PASSWORD = "incorrectpasswordshort"
        private const val MESSAGE_SUBNET = "subnetwrong"
        private const val MESSAGE_SHARED_IP = "preventsharederror"
        private const val MESSAGE_NO_STATUS = "attendance_no_status"
        private const val MESSAGE_INVALID_STATUS = "invalidstatus"

        private val WHITESPACE_REGEX = Regex("\\s+")

        /**
         * Tells apart a read `view.php`, an expired session (Moodle serves the page, or the
         * course's enrol page it redirects to, as the guest user) and a logged-in user Moodle
         * sent elsewhere. Sessions not open yet, whose link reads [futureLinkText] ("Report
         * future absence") and leads to a form for reporting an absence in advance, are left out.
         *
         * @param futureLinkText The localized text of the future-session link, or null if unknown,
         *   in which case such sessions are kept.
         */
        fun classify(view: MoodleWebAttendanceView?, futureLinkText: String?): ElearningAttendanceViewResponse {
            val userId = view?.userId
            return when {
                view == null || userId == null || userId <= GUEST_USER_ID -> ElearningAttendanceViewResponse.RequiresReauth
                !view.shown -> ElearningAttendanceViewResponse.Unavailable
                else -> ElearningAttendanceViewResponse.Success(
                    summary = view.summary,
                    submittableSessions = view.submittableSessions
                        .filter { futureLinkText == null || it.linkText.trim() != futureLinkText.trim() }
                        .map { ElearningAttendanceSubmittableSession(sessionId = it.id, time = it.time.normalizeSpace()) }
                        .distinctBy { it.sessionId },
                )
            }
        }

        /**
         * Describes a session from its take form, which shows a message instead of the statuses
         * when marking is refused right now; the refusal is kept so that the caller can tell the
         * student why.
         */
        fun describe(
            session: ElearningAttendanceSubmittableSession,
            form: MoodleWebAttendanceSessionForm?,
        ): ElearningAttendanceMarkableSession {
            val refusal = form?.refusal
            return ElearningAttendanceMarkableSession(
                sessionId = session.sessionId,
                time = session.time,
                requiresPassword = refusal == null && form?.requiresPassword == true,
                statuses = if (refusal != null) emptyList() else form?.statuses.orEmpty(),
                refusal = refusal,
            )
        }

        /**
         * Reads the outcome of marking from the template of `mobile_view_activity`. The plugin
         * shows every blocking condition as a message. Otherwise the outcome is read from the
         * session's own item, found by its date and time, which `view.php` writes the same way:
         * - its button still there means it wasn't marked, as when it closed in the meantime;
         * - a status in its last heading, after the group's, means it was;
         * - no item at all means the session is outside the view's time window, so nothing
         *   confirms the marking.
         */
        fun classify(page: MoodleWebAttendanceMarkPage?, session: ElearningAttendanceSubmittableSession): ElearningAttendanceMarkResult {
            page ?: return ElearningAttendanceMarkResult.Failed(reason = null)
            val keys = page.messages.map { it.key }
            when {
                MESSAGE_WRONG_PASSWORD in keys -> return ElearningAttendanceMarkResult.WrongPassword
                MESSAGE_SHARED_IP in keys -> return ElearningAttendanceMarkResult.NotOpen(MESSAGE_SHARED_IP)
                MESSAGE_SUBNET in keys -> return ElearningAttendanceMarkResult.NotOpen(MESSAGE_SUBNET)
                keys.any { it == MESSAGE_NO_STATUS || it == MESSAGE_INVALID_STATUS } ->
                    return ElearningAttendanceMarkResult.Failed(keys.first())
            }
            val items = page.sessions.filter { it.time.normalizeSpace() == session.time }
            return when {
                items.isEmpty() -> ElearningAttendanceMarkResult.Unconfirmed
                items.any { item -> item.markActions.any { it.sessionId == session.sessionId } } ->
                    ElearningAttendanceMarkResult.NotOpen(reason = null)
                else -> items.firstNotNullOfOrNull { item -> item.headings.lastOrNull { it.isNotBlank() } }
                    ?.let { ElearningAttendanceMarkResult.Marked(it) }
                    ?: ElearningAttendanceMarkResult.Failed(reason = null)
            }
        }

        private fun String.normalizeSpace(): String = trim().replace(WHITESPACE_REGEX, " ")
    }
}

/** What reading an attendance activity's `view.php` page found. */
sealed interface ElearningAttendanceViewResponse {
    /**
     * @property summary The user's statistics, or null if the page showed none (or not the eight
     *   rows it is read by).
     * @property submittableSessions The sessions open right now that the page offers to submit
     *   attendance for, for [ElearningAttendance.getMarkableSessions].
     */
    data class Success(
        val summary: MoodleWebAttendanceSummary?,
        val submittableSessions: List<ElearningAttendanceSubmittableSession>,
    ) : ElearningAttendanceViewResponse

    /**
     * Moodle served the page as the guest user: the `MoodleSession` cookie expired (Moodle then
     * redirects to the course's enrol page). The caller should renew the session and retry once.
     */
    data object RequiresReauth : ElearningAttendanceViewResponse

    /**
     * The user is logged in but Moodle sent them elsewhere: the page for teachers
     * (`manage.php`) or report viewers (`report.php`), or an enrolment, policy or access
     * restriction page. A new session would not change that, so the caller should not renew it.
     */
    data object Unavailable : ElearningAttendanceViewResponse
}

/**
 * A session the `view.php` session log offers to submit attendance for right now.
 *
 * @property sessionId The `attendance_sessions` id (`sessid`).
 * @property time The session's date and time as the plugin renders it in the user's language
 *   (e.g. "mer 18 nov 2026 2PM - 4PM"), whitespace collapsed. The mobile view titles each session
 *   with the same text, which is how a marking result is matched to its session.
 */
data class ElearningAttendanceSubmittableSession(
    val sessionId: String,
    val time: String,
)

/**
 * A session open right now, as the plugin's mobile take form describes it.
 *
 * @property sessionId The session, as the `sessid` marking it.
 * @property time The session's date and time, see [ElearningAttendanceSubmittableSession.time].
 * @property requiresPassword Whether the teacher protected the session with a password or QR code.
 * @property statuses The statuses to choose from; none when the session assigns one by itself,
 *   or when it is refused.
 * @property refusal The plugin's message key when the form refuses marking right now:
 *   "subnetwrong" (not on the classroom network), "preventsharederror" (this network already
 *   recorded a presence), "closed" or "attendance_no_status". Null when the session can be marked.
 */
data class ElearningAttendanceMarkableSession(
    val sessionId: String,
    val time: String,
    val requiresPassword: Boolean,
    val statuses: List<MoodleWebAttendanceStatusOption>,
    val refusal: String? = null,
)

/** The outcome of marking a presence, read from the template the plugin renders after it. */
sealed interface ElearningAttendanceMarkResult {
    /** @property statusDescription The status the session assigned (e.g. "Presente"), when shown. */
    data class Marked(val statusDescription: String?) : ElearningAttendanceMarkResult

    /** The student had already marked this session. */
    data object AlreadyMarked : ElearningAttendanceMarkResult

    /** The password or QR code didn't match the session's. */
    data object WrongPassword : ElearningAttendanceMarkResult

    /**
     * The session can't be marked now: closed, outside its window, refused by the classroom
     * network check, or already marked from this network.
     *
     * @property reason The plugin's message key when known (e.g. "subnetwrong"), for logging.
     */
    data class NotOpen(val reason: String?) : ElearningAttendanceMarkResult

    /**
     * No error came back, but the returned view doesn't list the session, so the marking can't
     * be confirmed. The mobile view only lists sessions starting between 6 hours ago and 24
     * hours from now (the site's defaults), so a longer-running session falls outside it.
     */
    data object Unconfirmed : ElearningAttendanceMarkResult

    /**
     * The attempt failed for another reason, such as no status being available.
     *
     * @property reason The plugin's message key when known, for logging.
     */
    data class Failed(val reason: String?) : ElearningAttendanceMarkResult
}
