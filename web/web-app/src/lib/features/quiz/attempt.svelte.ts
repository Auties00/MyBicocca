import { SvelteMap, SvelteSet } from 'svelte/reactivity';
import type {
	MoodleAttemptDataResponse,
	MoodleAttemptSummaryResponse,
	MoodleQuizAttempt,
	MoodleQuizQuestion
} from '@mybicocca/data-api/elearning';
import { DataFailure, toDataError, type DataError } from '#lib/data/errors.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import { invalidateAttempts } from './queries.ts';
import {
	answered,
	parseQuestion,
	questionFields,
	toKeyValues,
	type Answered,
	type Question,
	type Response
} from './questions.ts';

/** Moodle's default autosave period, for a quiz whose own isn't known. */
const DEFAULT_AUTOSAVE_SECONDS = 60;
/** A flag is a deliberate click: it is saved almost at once, as Moodle's own page does. */
const FLAG_SAVE_MS = 1500;

export type SaveState = 'idle' | 'pending' | 'saving' | 'saved' | 'error' | 'offline';

/**
 * `overdue`: time ran out on a quiz with a grace period, and Moodle waits for the submission
 * instead of closing the attempt.
 */
export type SubmitResult = { ok: true; overdue: boolean } | { ok: false; error: DataError };

/** A question as the navigator and the submit check show it. */
export interface NavItem {
	slot: number;
	number: string;
	/** 0-based. */
	page: number;
	answered: Answered;
	flagged: boolean;
}

/** What Moodle's own state says of an answer the page doesn't show. */
function answeredOf(question: MoodleQuizQuestion): Answered {
	if (question.state === 'todo') return 'empty';
	if (question.state === 'invalid') return 'partial';
	return 'complete';
}

interface Options {
	scope: () => SessionScope | undefined;
	courseId: number;
	quizId: number;
	attemptId: number;
	/** Seconds between autosaves; 0 turns autosaving off, as a site may. */
	autoSavePeriod: () => number | null;
}

/**
 * A running attempt: loads its pages, keeps the student's answers and flags, autosaves them, and
 * submits. Answers are saved before any page loads, so a load never races the save before it,
 * and every save sends the questions' hidden fields back as Moodle rendered them.
 */
export class AttemptSession {
	readonly #options: Options;

	/** The page shown, 0-based; the one loading while `loading`. */
	page = $state<number>();
	loading = $state(false);
	loadError = $state.raw<DataError>();
	data = $state.raw<MoodleAttemptDataResponse>();
	questions = $state.raw<Question[]>([]);

	readonly responses = new SvelteMap<number, Response>();
	readonly flags = new SvelteMap<number, boolean>();
	readonly #dirty = new SvelteSet<number>();
	/** Every question seen so far, by slot, for saving slots of other pages. */
	readonly #known: Record<number, Question> = {};

	saveState = $state<SaveState>('idle');
	saveError = $state.raw<DataError>();
	summary = $state.raw<MoodleAttemptSummaryResponse>();

	#saving: Promise<void> | null = null;
	#timer: ReturnType<typeof setTimeout> | undefined;
	#due = 0;
	#loadToken = 0;

	constructor(options: Options) {
		this.#options = options;
	}

	#scope(): SessionScope {
		const scope = this.#options.scope();
		if (scope === undefined) {
			throw new DataFailure({ kind: 'auth', service: 'moodle', reason: 'reauth_required' });
		}
		return scope;
	}

	get attemptId(): number {
		return this.#options.attemptId;
	}

	/** The attempt as the last loaded page described it. */
	get attempt(): MoodleQuizAttempt | undefined {
		return this.data?.attempt;
	}

	get dirty(): boolean {
		return this.#dirty.size > 0;
	}

	async load(page: number): Promise<void> {
		const token = ++this.#loadToken;
		this.page = page;
		this.loading = true;
		this.loadError = undefined;
		await this.flush().catch(() => undefined);
		try {
			const data = await this.#scope().api.moodle((moodle, auth) =>
				moodle.quiz.getAttemptData(this.attemptId, page, auth, { preflightData: [] })
			);
			if (token !== this.#loadToken) return;
			const questions = data.questions.map(parseQuestion);
			for (const question of questions) {
				this.#known[question.slot] = question;
				if (!this.#dirty.has(question.slot)) {
					this.flags.set(question.slot, question.flagged);
					this.responses.delete(question.slot);
				}
			}
			this.data = data;
			this.questions = questions;
		} catch (error) {
			if (token === this.#loadToken) this.loadError = toDataError(error);
		} finally {
			if (token === this.#loadToken) this.loading = false;
		}
	}

	answer(slot: number, name: string, value: string): void {
		this.responses.set(slot, { ...this.responses.get(slot), [name]: value });
		this.#touch(slot, this.#autosaveMs());
	}

	toggleFlag(slot: number): void {
		this.flags.set(slot, !this.flags.get(slot));
		this.#touch(slot, FLAG_SAVE_MS);
	}

	/** Whether a question of this page is answered, counting what hasn't been saved yet. */
	answered(question: Question): Answered {
		return answered(question, this.responses.get(question.slot) ?? {});
	}

	/**
	 * Every question of the attempt, descriptions left out: the shown page's as the student left
	 * them, the others as Moodle last reported them.
	 */
	get navItems(): NavItem[] {
		const source = this.summary?.questions ?? this.data?.questions ?? [];
		return source
			.filter((question) => question.type !== 'description' && question.number !== null)
			.map((question) => {
				const local = this.questions.find((candidate) => candidate.slot === question.slot);
				return {
					slot: question.slot,
					number: question.questionNumber || String(question.number),
					page: question.page,
					answered: local ? this.answered(local) : answeredOf(question),
					flagged: this.flags.get(question.slot) ?? question.flagged
				};
			});
	}

	#autosaveMs(): number | null {
		const period = this.#options.autoSavePeriod() ?? DEFAULT_AUTOSAVE_SECONDS;
		return period > 0 ? period * 1000 : null;
	}

	#touch(slot: number, delay: number | null): void {
		this.#dirty.add(slot);
		if (this.saveState !== 'offline' && this.saveState !== 'error') this.saveState = 'pending';
		if (delay === null) return;
		// The first change sets the time, as Moodle's autosave does; a flag only brings it closer.
		const due = Date.now() + delay;
		if (this.#timer !== undefined && this.#due <= due) return;
		clearTimeout(this.#timer);
		this.#due = due;
		this.#timer = setTimeout(() => {
			this.#timer = undefined;
			void this.flush().catch(() => undefined);
		}, delay);
	}

	#fields(slots: Iterable<number>): Record<string, string> {
		const fields: Record<string, string> = {};
		for (const slot of slots) {
			const question = this.#known[slot];
			if (question === undefined) continue;
			Object.assign(
				fields,
				questionFields(question, this.responses.get(slot) ?? {}, this.flags.get(slot))
			);
		}
		return fields;
	}

	/**
	 * Saves the changed answers now (Moodle's autosave, which adds no real step, so the sequence
	 * checks stay valid). Resolves once saved; rejects with the failure, keeping the changes.
	 */
	flush(): Promise<void> {
		clearTimeout(this.#timer);
		this.#timer = undefined;
		if (this.#dirty.size === 0) return this.#saving ?? Promise.resolve();
		const slots = [...this.#dirty];
		const data = toKeyValues(this.#fields(slots));
		this.#dirty.clear();
		this.saveState = 'saving';
		const run = this.#save(this.#saving, slots, data);
		this.#saving = run;
		run.then(
			() => this.#settle(run),
			() => this.#settle(run)
		);
		return run;
	}

	async #save(
		previous: Promise<void> | null,
		slots: number[],
		data: { name: string; value: string }[]
	): Promise<void> {
		await previous?.catch(() => undefined);
		try {
			await this.#scope().api.moodle((moodle, auth) =>
				moodle.quiz.saveAttempt(this.attemptId, data, auth, { preflightData: [] })
			);
			this.saveError = undefined;
			this.saveState = this.#dirty.size > 0 ? 'pending' : 'saved';
		} catch (error) {
			for (const slot of slots) this.#dirty.add(slot);
			this.saveError = toDataError(error);
			this.saveState = this.saveError.kind === 'offline' ? 'offline' : 'error';
			throw error;
		}
	}

	#settle(run: Promise<void>): void {
		if (this.#saving === run) this.#saving = null;
	}

	/** Every question's state and flag, for the navigator and the submit check. */
	async summarize(): Promise<MoodleAttemptSummaryResponse> {
		const summary = await this.#scope().api.moodle((moodle, auth) =>
			moodle.quiz.getAttemptSummary(this.attemptId, auth, { preflightData: [] })
		);
		this.summary = summary;
		return summary;
	}

	/** What the submit check lists: every question as Moodle has it once the answers are saved. */
	async check(): Promise<NavItem[]> {
		await this.flush();
		await this.summarize();
		return this.navItems;
	}

	/**
	 * After Moodle refused a save as out of sequence (the attempt moved on in another window):
	 * takes the sequence checks Moodle has now and saves again, keeping the student's answers.
	 */
	async resync(): Promise<void> {
		const summary = await this.summarize();
		for (const { slot, sequenceCheck } of summary.questions) {
			const question = this.#known[slot];
			if (question === undefined || question.prefix === null || sequenceCheck === null) continue;
			this.#known[slot] = {
				...question,
				fields: { ...question.fields, [`${question.prefix}:sequencecheck`]: String(sequenceCheck) }
			};
		}
		this.questions = this.questions.map((question) => this.#known[question.slot] ?? question);
		await this.flush();
	}

	/**
	 * Submits the attempt with the answers not saved yet. `timeUp` tells Moodle the timer ran out,
	 * so it accepts the attempt as it stands.
	 */
	async submit(timeUp = false): Promise<SubmitResult> {
		clearTimeout(this.#timer);
		await this.#saving?.catch(() => undefined);
		const slots = [...this.#dirty];
		const { courseId, quizId } = this.#options;
		const scope = this.#options.scope();
		try {
			if (scope === undefined)
				throw new DataFailure({ kind: 'auth', service: 'moodle', reason: 'reauth_required' });
			const result = await scope.api.moodle((moodle, auth) =>
				moodle.quiz.processAttempt(this.attemptId, auth, {
					data: toKeyValues(this.#fields(slots)),
					finishAttempt: true,
					timeUp,
					preflightData: []
				})
			);
			this.#dirty.clear();
			this.saveState = 'saved';
			if (result.state === 'finished') return { ok: true, overdue: false };
			if (result.state === 'overdue') return { ok: true, overdue: true };
			const error: DataError = {
				kind: 'rejected',
				service: 'moodle',
				status: 200,
				code: 'notfinished',
				message: result.state
			};
			return { ok: false, error };
		} catch (error) {
			return { ok: false, error: toDataError(error) };
		} finally {
			if (scope !== undefined) invalidateAttempts(scope, courseId, quizId);
		}
	}

	dispose(): void {
		clearTimeout(this.#timer);
		this.#loadToken++;
	}
}
