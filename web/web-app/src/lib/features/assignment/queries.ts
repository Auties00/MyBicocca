import type {
	MoodleAssignmentsResponse,
	MoodleSubmissionStatus
} from '@mybicocca/data-api/elearning';
import { DataFailure, type DataError } from '#lib/data/errors.ts';
import { accountKey } from '#lib/data/keys.ts';
import { invalidate, policies, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';

/* Named after their SDK calls, so every feature asking the same thing shares one entry. */

const TEN_MINUTES = policies.fresh(10);

/** A course's assignments (`mod_assign_get_assignments`), fresh for 10 minutes as on Android. */
export function courseAssignmentsQuery(
	scope: SessionScope,
	courseId: number
): QueryOptions<MoodleAssignmentsResponse> {
	return {
		key: accountKey(scope.account, 'moodle.assign.getAssignments', courseId),
		load: () =>
			scope.api.moodle((moodle, auth) =>
				moodle.assign.getAssignments(auth, { courseIds: [courseId] })
			),
		policy: TEN_MINUTES
	};
}

/** The student's submission, grade and feedback for one assignment (`mod_assign_get_submission_status`). */
export function submissionStatusQuery(
	scope: SessionScope,
	assignId: number
): QueryOptions<MoodleSubmissionStatus> {
	return {
		key: accountKey(scope.account, 'moodle.assign.getSubmissionStatus', assignId),
		load: () =>
			scope.api.moodle((moodle, auth) => moodle.assign.getSubmissionStatus(assignId, auth)),
		policy: TEN_MINUTES
	};
}

/**
 * The failure of a list's queued load that nobody wanted any more by its turn: not a failure to
 * show, and the next look at that status loads it again.
 */
const WITHDRAWN = { kind: 'unknown', detail: 'withdrawn' } satisfies DataError;

export function isWithdrawn(error: DataError | undefined): boolean {
	return error?.kind === WITHDRAWN.kind && error.detail === WITHDRAWN.detail;
}

interface Task {
	id: string;
	start: () => void;
	drop: () => void;
}

/**
 * Loads one by one what a list needs many of, a few at a time: Moodle answers one submission
 * status per call. A load starts once a slot is free, and only if something still wants it.
 */
export class LoadQueue {
	readonly #parallel: number;
	#running = 0;
	readonly #waiting: Task[] = [];
	readonly #interest = new Map<string, number>();

	constructor(parallel: number) {
		this.#parallel = parallel;
	}

	/** Declares that `id` is wanted until the returned function is called. */
	want(id: string): () => void {
		this.#interest.set(id, (this.#interest.get(id) ?? 0) + 1);
		let released = false;
		return () => {
			if (released) return;
			released = true;
			const left = (this.#interest.get(id) ?? 1) - 1;
			if (left > 0) this.#interest.set(id, left);
			else this.#interest.delete(id);
		};
	}

	/** Runs `load` in turn; rejects with a withdrawn failure if `id` is no longer wanted by then. */
	run<T>(id: string, load: () => Promise<T>): Promise<T> {
		return new Promise<T>((resolve, reject) => {
			this.#waiting.push({
				id,
				start: () => {
					this.#running++;
					load()
						.then(resolve, reject)
						.finally(() => {
							this.#running--;
							this.#next();
						});
				},
				drop: () => reject(new DataFailure(WITHDRAWN))
			});
			this.#next();
		});
	}

	/** Starts `id`'s waiting load now, ahead of the line: a page shows it and shouldn't wait. */
	promote(id: string): void {
		const index = this.#waiting.findIndex((task) => task.id === id);
		if (index < 0) return;
		const [task] = this.#waiting.splice(index, 1);
		task.start();
	}

	#next(): void {
		while (this.#running < this.#parallel) {
			const task = this.#waiting.shift();
			if (task === undefined) return;
			if ((this.#interest.get(task.id) ?? 0) > 0) task.start();
			else task.drop();
		}
	}
}

/** The queue every list's submission statuses share. */
export const statusQueue = new LoadQueue(4);

/**
 * Reloads what a submission changes: its status, the course's completion (in its contents) and
 * the deadlines, which drop an assignment once it is handed in.
 */
export function afterSubmissionChange(scope: SessionScope, courseId: number, assignId: number) {
	invalidate(
		accountKey(scope.account, 'moodle.assign.getSubmissionStatus', assignId),
		accountKey(scope.account, 'moodle.course.getContents', courseId),
		accountKey(scope.account, 'moodle.calendar.getActionEventsByTimesort'),
		accountKey(scope.account, 'elearning', 'deadlines')
	);
}
