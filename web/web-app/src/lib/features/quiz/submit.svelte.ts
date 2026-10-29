import { toDataError, type DataError } from '#lib/data/errors.ts';
import type { NavItem, SubmitResult } from './attempt.svelte.ts';

interface Options {
	/** Saves what's pending and reads every question's state. */
	prepare: () => Promise<NavItem[]>;
	submit: () => Promise<SubmitResult>;
}

/**
 * The check before submitting: opening it saves what's pending and lists the questions left
 * unanswered or flagged. It runs when the student opens it or asks again, never on its own, so a
 * failed save waits for the student instead of repeating while the dialog shows.
 */
export class SubmitCheck {
	open = $state(false);
	items = $state.raw<NavItem[]>();
	failure = $state.raw<DataError>();
	checking = $state(false);
	submitting = $state(false);

	readonly #options: Options;
	/** Results of a check the student has since closed or repeated are dropped. */
	#run = 0;

	constructor(options: Options) {
		this.#options = options;
	}

	show(): void {
		if (this.open) return;
		this.open = true;
		this.items = undefined;
		void this.check();
	}

	close(): void {
		this.open = false;
		this.#run++;
		this.checking = false;
	}

	async check(): Promise<void> {
		const run = ++this.#run;
		this.failure = undefined;
		this.checking = true;
		try {
			const items = await this.#options.prepare();
			if (run === this.#run) this.items = items;
		} catch (error) {
			if (run === this.#run) this.failure = toDataError(error);
		} finally {
			if (run === this.#run) this.checking = false;
		}
	}

	async confirm(): Promise<void> {
		this.submitting = true;
		this.failure = undefined;
		const result = await this.#options.submit();
		this.submitting = false;
		if (result.ok) this.close();
		else this.failure = result.error;
	}
}
