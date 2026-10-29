import { DataFailure, type DataError } from '#lib/data/errors.ts';
import { errorMessage } from '#lib/data/errorMessage.ts';
import { accountKey } from '#lib/data/keys.ts';
import { mutation } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';

/**
 * Moodle's self-enrolment answers a wrong or missing enrolment key with a warning instead of
 * enrolling: 2 for a group key, 3 for a wrong key with a hint, 4 for a wrong key.
 */
const KEY_WARNINGS = new Set(['2', '3', '4']);

/**
 * Self-enrolment in a catalog course (`enrol_self_enrol_user`), with the enrolment key some
 * courses ask for. A refusal comes back as a `rejected` error carrying Moodle's warning; the
 * course list reloads either way.
 */
export function enrolMutation(scope: SessionScope) {
	return mutation(
		async (courseId: number, password: string) => {
			const answer = await scope.api.moodle((moodle, auth) =>
				moodle.course.enrolUser(courseId, auth, password === '' ? {} : { password })
			);
			if (answer.status) return;
			const warning = answer.warnings[0];
			throw new DataFailure({
				kind: 'rejected',
				service: 'moodle',
				status: 200,
				...(warning ? { code: warning.warningCode } : {}),
				message: warning?.message ?? ''
			});
		},
		{ invalidate: [accountKey(scope.account, 'moodle.course.getUsersCourses')] }
	);
}

/** Whether Moodle wants an enrolment key, or another one. */
export function needsKey(error: DataError | undefined): boolean {
	return error?.kind === 'rejected' && error.code !== undefined && KEY_WARNINGS.has(error.code);
}

/**
 * Why an enrolment failed. Moodle words its refusals in the language the app asks for, and they
 * say what the student can do ("self-enrolment is closed"), so they are shown as they are.
 */
export function enrolFailure(error: DataError): string {
	const reason =
		error.kind === 'rejected' && error.service === 'moodle' ? error.message.trim() : '';
	return reason === '' ? errorMessage(error) : reason;
}
