import { DataFailure } from '#lib/data/errors.ts';
import type { SubmitResult } from './attempt.svelte.ts';
import { errorCode, quizErrorMessage } from './format.ts';

interface Options {
	/** Submits the attempt; `timeUp` when the timer ran out. */
	submit: (timeUp: boolean) => Promise<SubmitResult>;
	/** Closes whatever is open over the questions: time's up takes over the page. */
	interrupt: () => void;
	/** Leaves the attempt for good, for its review or its quiz. */
	leave: (to: 'review' | 'quiz') => void;
}

/**
 * How an attempt ends: submitted by the student, by the timer running out, or, once Moodle holds
 * it overdue for the quiz's grace period, submitted from there. A submission Moodle answers
 * with a closed attempt ends it all the same: it was submitted elsewhere.
 */
export class AttemptEnding {
	/** Submitted or gone: the page only leads on from here. */
	finished = $state(false);
	/** Time ran out on a quiz with a grace period: only the submission is left. */
	overdue = $state(false);
	/** Time ran out: the attempt is being submitted as it stands. */
	timeUp = $state(false);
	/** Why submitting at time-up failed, for the student to retry. */
	timeUpError = $state<string>();

	readonly #options: Options;

	constructor(options: Options) {
		this.#options = options;
	}

	finish(to: 'review' | 'quiz'): void {
		if (this.finished) return;
		this.finished = true;
		this.#options.leave(to);
	}

	/** The student's own submission, after the check. */
	async submit(): Promise<SubmitResult> {
		const result = await this.#options.submit(false);
		this.#settle(result);
		return result;
	}

	/** Time ran out: submits what's there, whatever the student was doing. */
	async expire(): Promise<void> {
		if (this.finished || (this.timeUp && !this.timeUpError)) return;
		this.#options.interrupt();
		this.timeUp = true;
		this.timeUpError = undefined;
		const result = await this.#options.submit(true);
		const outcome = this.#settle(result);
		// The dialog stays up while the review opens; an overdue attempt shows its own panel.
		if (outcome === 'overdue') this.timeUp = false;
		else if (outcome === 'failed' && !result.ok) this.timeUpError = quizErrorMessage(result.error);
	}

	/** Submits an overdue attempt; throws the failure, for the confirmation to show. */
	async submitOverdue(): Promise<void> {
		const result = await this.#options.submit(false);
		if (this.#settle(result) === 'failed' && !result.ok) throw new DataFailure(result.error);
	}

	/** Leads on from a submission; `failed` leaves the attempt where it was. */
	#settle(result: SubmitResult): 'overdue' | 'finished' | 'failed' {
		if (result.ok && result.overdue) {
			this.overdue = true;
			return 'overdue';
		}
		if (result.ok || errorCode(result.error) === 'attemptalreadyclosed') {
			this.finish('review');
			return 'finished';
		}
		return 'failed';
	}
}
