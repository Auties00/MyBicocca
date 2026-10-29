import {
	scrapeMoodleWebAttendanceMarkPage,
	scrapeMoodleWebAttendanceSessionForm,
	type MoodleWebAttendanceStatusOption
} from '@mybicocca/data-api/elearning';
import { scrape } from '@mybicocca/data-api/runtime';
import { toDataError } from '#lib/data/errors.ts';
import { invalidate } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import { deviceToken, knownPosition } from './device.ts';
import type { AttendanceModule } from './model.ts';
import { certifyOutcome, markOutcome, type CheckInOutcome } from './outcome.ts';
import { attendancePrefix, attendanceView, knownModules } from './queries.ts';
import type { PresenceScan } from './scan.ts';

const SITE = 'https://elearning.unimib.it/';

/** Selects the plugin's current templates, the ones the Moodle app of today renders. */
const APP_VERSION_CODE = '45000';

/** Sessions assigning their status by themselves accept any; the plugin needs one sent. */
const ANY_STATUS = '1';

/** A template of mod_attendance's mobile views, through the web service the Moodle app uses. */
async function template(
	scope: SessionScope,
	method: 'mobile_user_form' | 'mobile_view_activity',
	args: Record<string, string>
): Promise<string | undefined> {
	const content = await scope.api.moodle((moodle, auth) =>
		moodle.mobile.getContent('mod_attendance', method, auth, {
			args: Object.entries(args).map(([name, value]) => ({ name, value }))
		})
	);
	return content.templates[0]?.html;
}

interface OpenSession {
	module: AttendanceModule;
	statuses: MoodleWebAttendanceStatusOption[];
}

/**
 * The module holding `sessionId` open for self-marking: a QR carries only the session, so each
 * register's page is read for the sessions it offers, and the matching one's form (which never
 * records anything) tells whether it still accepts the student.
 */
async function openSession(
	scope: SessionScope,
	modules: readonly AttendanceModule[],
	sessionId: string
): Promise<OpenSession | undefined> {
	for (const module of modules) {
		try {
			const view = await attendanceView(scope, module.cmId);
			if (!view?.submittableSessions.some((session) => session.id === sessionId)) continue;
			const html = await template(scope, 'mobile_user_form', {
				cmid: String(module.cmId),
				courseid: String(module.courseId),
				sessid: sessionId,
				appversioncode: APP_VERSION_CODE
			});
			const form =
				html === undefined
					? undefined
					: scrapeMoodleWebAttendanceSessionForm(scrape.document(html, SITE));
			if (form && !form.refused) return { module, statuses: form.statuses };
		} catch {
			// A register that can't be read can't hold the session.
		}
	}
	return undefined;
}

/**
 * Marks the student present at a Moodle session. Rendering the marking view records the
 * presence, and also in every other open session of the activity that asks no password.
 */
async function markSession(
	scope: SessionScope,
	sessionId: string,
	password: string | null
): Promise<CheckInOutcome> {
	const modules = await knownModules(scope);
	if (modules.length === 0) return { kind: 'not-open', reason: 'no-register' };
	const target = await openSession(scope, modules, sessionId);
	if (target === undefined) return { kind: 'not-open', reason: 'not-in-courses' };
	const html = await template(scope, 'mobile_view_activity', {
		cmid: String(target.module.cmId),
		courseid: String(target.module.courseId),
		sessid: sessionId,
		status: target.statuses[0]?.id ?? ANY_STATUS,
		studentpass: password ?? '',
		appversioncode: APP_VERSION_CODE
	});
	const page =
		html === undefined ? undefined : scrapeMoodleWebAttendanceMarkPage(scrape.document(html, SITE));
	if (page === undefined) return { kind: 'failed', reason: 'generic', message: null };
	return markOutcome(page, sessionId);
}

/** Certifies a lesson with EasyBadge, which knows the student by number and this browser. */
async function certifyLesson(scope: SessionScope, lessonCode: string): Promise<CheckInOutcome> {
	const studentId = scope.career.studentNumber.trim();
	if (studentId === '') return { kind: 'failed', reason: 'student-number', message: null };
	const position = await knownPosition();
	const result = await scope.api.easyStaff.attendance.certifyAttendance({
		studentId,
		lessonCode,
		deviceToken: deviceToken(),
		seat: '',
		language: 'en',
		isAuthenticated: true,
		action: 'CreateFromJSON',
		additionalData: { timestamp: Date.now(), ...position }
	});
	return certifyOutcome(result);
}

/**
 * Records the student's presence: irreversible. A lesson code goes to EasyBadge, a session link
 * to Moodle's web services, straight from the browser (Moodle checks the classroom's network).
 */
export async function checkIn(scope: SessionScope, scan: PresenceScan): Promise<CheckInOutcome> {
	if (scan.kind === 'unrecognized')
		return { kind: 'failed', reason: 'unrecognized', message: null };
	let outcome: CheckInOutcome;
	try {
		outcome =
			scan.kind === 'lesson'
				? await certifyLesson(scope, scan.code)
				: await markSession(scope, scan.sessionId, scan.password);
	} catch (error) {
		return { kind: 'failed', reason: 'generic', message: null, error: toDataError(error) };
	}
	if (outcome.kind === 'recorded') invalidate(attendancePrefix(scope));
	return outcome;
}
