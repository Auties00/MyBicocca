import type { EasyStaffCertifyAttendanceResult } from '@mybicocca/data-api/easystaff';
import { EasyStaffCertificationResult } from '@mybicocca/data-api/easystaff';
import type { MoodleWebAttendanceMarkPage } from '@mybicocca/data-api/elearning';
import type { DataError } from '#lib/data/errors.ts';

/** How a check-in ended. Messages are the services' own words, when they sent any. */
export type CheckInOutcome =
	| {
			kind: 'recorded';
			/** The status Moodle assigned ("Presente"). */
			status: string | null;
			/** EasyBadge's confirmation. */
			message: string | null;
	  }
	| { kind: 'already-recorded' }
	| { kind: 'wrong-credential'; message: string | null }
	| { kind: 'not-open'; reason: 'closed' | 'no-register' | 'not-in-courses' }
	/** Moodle accepts the session only from the classroom's network. */
	| { kind: 'network-restricted' }
	/** Moodle already recorded a presence from this network. */
	| { kind: 'device-already-used' }
	| {
			kind: 'failed';
			reason: 'generic' | 'unrecognized' | 'student-number';
			message: string | null;
			/** The request itself failed. */
			error?: DataError;
	  };

/**
 * EasyBadge answers only a flag and a message: a refusal that mentions the code ("codice") is a
 * wrong lesson code.
 */
export function certifyOutcome(result: EasyStaffCertifyAttendanceResult): CheckInOutcome {
	const message = result.message.trim() || null;
	if (result.result === EasyStaffCertificationResult.Ok) {
		return { kind: 'recorded', status: null, message };
	}
	if (message?.toLowerCase().includes('codice')) return { kind: 'wrong-credential', message };
	return { kind: 'failed', reason: 'generic', message };
}

/**
 * Reads marking's outcome off the template `mobile_view_activity` renders after it. Every
 * blocking condition is a message; otherwise marking drops the session's button and shows its
 * status, the last heading. A session still offering its button wasn't marked: it closed.
 */
export function markOutcome(page: MoodleWebAttendanceMarkPage, sessionId: string): CheckInOutcome {
	const keys = new Set(page.messages.map((message) => message.key));
	if (keys.has('incorrectpasswordshort')) return { kind: 'wrong-credential', message: null };
	if (keys.has('preventsharederror')) return { kind: 'device-already-used' };
	if (keys.has('subnetwrong')) return { kind: 'network-restricted' };
	if (keys.has('attendance_no_status') || keys.has('invalidstatus')) {
		return { kind: 'failed', reason: 'generic', message: null };
	}
	if (page.markActions.some((action) => action.sessionId === sessionId)) {
		return { kind: 'not-open', reason: 'closed' };
	}
	return { kind: 'recorded', status: page.headings.at(-1)?.trim() || null, message: null };
}
