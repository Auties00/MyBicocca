import type {
	MoodleAssignment,
	MoodleAssignmentsResponse,
	MoodleSubmissionStatus
} from '@mybicocca/data-api/elearning';
import type { DataError } from '#lib/data/errors.ts';
import { query, type Query } from '#lib/data/query.svelte.ts';
import { keyString } from '#lib/data/queryCache.ts';
import { session } from '#lib/data/session.svelte.ts';
import { m } from '#lib/paraglide/messages.js';
import {
	readConfig,
	readSchedule,
	readSubmission,
	type Schedule,
	type Submission,
	type SubmissionConfig
} from './model.ts';
import {
	courseAssignmentsQuery,
	isWithdrawn,
	statusQueue,
	submissionStatusQuery
} from './queries.ts';

/** The course's assignments from Moodle's answer, which lists them by course. */
export function assignmentsOf(
	response: MoodleAssignmentsResponse | undefined,
	courseId: number
): MoodleAssignment[] | undefined {
	return response && (response.courses.find((course) => course.id === courseId)?.assignments ?? []);
}

/** The site's upload cap, which applies where an assignment sets none. */
export function uploadLimit(): number | null {
	return session.current?.account.learning?.maxUploadBytes ?? null;
}

/**
 * Whether the e-learning site times assignments (its `enabletimelimit`), which no read reports:
 * an assignment keeps its time limit when the site turns timing off, and only starting one tells.
 */
export const siteTiming = $state({ off: false });

/** Leaves out a time limit the site doesn't apply. */
function applied<T extends { timeLimit: number | null }>(value: T): T {
	return siteTiming.off && value.timeLimit !== null ? { ...value, timeLimit: null } : value;
}

/**
 * One assignment with the student's submission: what its page and its editor read. The record
 * comes from the course's list (shared with the tab), the submission from its own status.
 */
export class AssignmentState {
	readonly list: Query<MoodleAssignmentsResponse>;
	readonly status: Query<MoodleSubmissionStatus>;

	readonly assignment: MoodleAssignment | undefined;
	/** The list has loaded and doesn't have it. */
	readonly missing: boolean;
	readonly submission: Submission | undefined;
	readonly schedule: Schedule | undefined;
	readonly config: SubmissionConfig | undefined;
	/** Why the status failed to load, if it did. */
	readonly statusError: DataError | undefined;

	/** Call while a component initializes. */
	constructor(courseId: () => number, assignId: () => number) {
		this.list = query(() => {
			const scope = session.current;
			return scope && courseAssignmentsQuery(scope, courseId());
		});
		this.status = query(() => {
			const scope = session.current;
			return scope && submissionStatusQuery(scope, assignId());
		});
		const scope = session.current;
		if (scope) statusQueue.promote(keyString(submissionStatusQuery(scope, assignId()).key));

		this.assignment = $derived(
			assignmentsOf(this.list.data, courseId())?.find((item) => item.id === assignId())
		);
		this.missing = $derived(this.list.data !== undefined && this.assignment === undefined);
		this.submission = $derived(
			this.status.data && applied(readSubmission(this.status.data, this.assignment))
		);
		this.schedule = $derived(
			this.assignment && readSchedule(this.assignment, this.submission?.extension)
		);
		this.config = $derived(
			this.assignment &&
				applied(
					readConfig(
						this.assignment,
						this.submission?.plugins ?? [],
						uploadLimit(),
						m.assignment_statement_default()
					)
				)
		);
		this.statusError = $derived(isWithdrawn(this.status.error) ? undefined : this.status.error);
	}

	refresh = async (): Promise<void> => {
		await Promise.all([this.list.refresh(), this.status.refresh()]);
	};
}

/** The assignment's page on the e-learning site. */
export function moodleAssignmentUrl(cmId: number): `https://${string}` {
	return `https://elearning.unimib.it/mod/assign/view.php?id=${cmId}`;
}
