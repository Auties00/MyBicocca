import type { Esse3QuestionnairePage } from '@mybicocca/data-api/esse3';
import { DecodeError } from '@mybicocca/data-api/runtime';
import { DataFailure, toDataError, type DataError } from '#lib/data/errors.ts';
import { mutate } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import type { TeachingUnit } from './model.ts';
import {
	missingAnswers,
	NO_ANSWER,
	pageAnswers,
	savedAnswers,
	toggleOption,
	toSurveyPage,
	type Answer,
	type Answers,
	type Question,
	type SurveyPage
} from './page.ts';
import { EVALUATION_EVENT, sentQuestionnaireKeys } from './queries.ts';

export interface CompileTarget {
	activityChoiceId: number;
	questionnaireId: number;
	configId: number;
	/** The unit as it was when the compilation began: once sent, the reloaded units mark it done. */
	unit: TeachingUnit;
	anonymous: boolean;
}

export type Step =
	/** Nothing asked of Esse3 yet: the student hasn't started. */
	| { kind: 'ready' }
	| { kind: 'starting' }
	| { kind: 'start-failed'; error: DataError }
	/** `index` counts pages from 0 for display; Esse3 branches, so the total is unknown. */
	| { kind: 'page'; page: SurveyPage; index: number }
	| { kind: 'summary'; complete: boolean }
	| { kind: 'sent' };

/** The server-side compilation: Esse3 numbers it twice. */
interface Session {
	compilationId: number;
	userCompilationId: number;
}

export type Advance = 'moved' | 'missing' | 'failed' | 'busy';

export interface VisitedPage {
	page: SurveyPage;
	/** As they were saved on the way forward. */
	answers: Answers;
}

/** A move between pages that failed; the page stays as it was. */
export interface MoveError {
	move: 'next' | 'back';
	error: DataError;
}

/**
 * One compilation of a teaching unit's questionnaire, from the first page to the sending. Esse3
 * keeps the answers of each page as the student moves on, but a compilation left unsent is
 * never offered again.
 */
export class Compilation {
	step = $state.raw<Step>({ kind: 'ready' });
	answers = $state.raw<Answers>({});
	/** Questions a blocked "Next" left unanswered. */
	invalid = $state.raw<readonly number[]>([]);
	/** The round trip in flight, which locks every other move. */
	working = $state<'next' | 'back' | 'send' | null>(null);
	moveError = $state.raw<MoveError>();
	/** The pages saved so far, in order: what the summary recaps and Back returns to. */
	visited = $state.raw<readonly VisitedPage[]>([]);

	readonly target: CompileTarget;
	readonly #scope: SessionScope;
	#session: Session | undefined;

	constructor(scope: SessionScope, target: CompileTarget) {
		this.#scope = scope;
		this.target = target;
	}

	/** Answers would be lost by leaving: a compilation is running and isn't sent. */
	get open(): boolean {
		return this.#session !== undefined && this.step.kind !== 'sent';
	}

	get #api() {
		return this.#scope.api.esse3.questionnaires;
	}

	/** Creates the compilation on Esse3, which can't be undone: only on the student's say-so. */
	async start(): Promise<void> {
		if (this.step.kind !== 'ready' && this.step.kind !== 'start-failed') return;
		this.step = { kind: 'starting' };
		const { activityChoiceId, questionnaireId, configId, unit } = this.target;
		try {
			const first = await this.#api.setNewSurvey(
				String(questionnaireId),
				activityChoiceId,
				this.#scope.career.id,
				{ tags: unit.tags },
				EVALUATION_EVENT,
				configId
			);
			const { questionComponentId, userComponentId } = first;
			if (questionComponentId === null || userComponentId === null) {
				throw new DataFailure({ kind: 'decode', detail: 'Esse3 started no compilation' });
			}
			this.#session = { compilationId: questionComponentId, userCompilationId: userComponentId };
			this.#enter(first, 0);
		} catch (error) {
			this.step = { kind: 'start-failed', error: toDataError(error) };
		}
	}

	answerOf(question: Question): Answer {
		return this.answers[question.id] ?? NO_ANSWER;
	}

	pick(question: Question, optionId: number): void {
		this.#set(question, toggleOption(question, this.answerOf(question), optionId));
	}

	write(question: Question, text: string): void {
		this.#set(question, { ...this.answerOf(question), text });
	}

	#set(question: Question, answer: Answer): void {
		this.answers = { ...this.answers, [question.id]: answer };
		if (this.invalid.includes(question.id)) {
			this.invalid = this.invalid.filter((id) => id !== question.id);
		}
	}

	/** Saves the page and moves on: to the next page, or to the summary after the last. */
	async next(): Promise<Advance> {
		const step = this.step;
		const session = this.#session;
		if (step.kind !== 'page' || !session || this.working) return 'busy';
		const missing = missingAnswers(step.page, this.answers);
		if (missing.length > 0) {
			this.invalid = missing.map((question) => question.id);
			return 'missing';
		}
		this.working = 'next';
		this.moveError = undefined;
		try {
			await this.#save(session, step.page);
			this.visited = [
				...this.visited.slice(0, step.index),
				{ page: step.page, answers: this.answers }
			];
			const next = await this.#api.getNextPage(
				this.#scope.career.id,
				this.target.activityChoiceId,
				String(this.target.questionnaireId),
				session.compilationId,
				step.page.id,
				session.userCompilationId
			);
			if (toSurveyPage(next) === null) {
				const summary = await this.#api.getComponentSummary(
					this.#scope.career.id,
					this.target.activityChoiceId,
					session.compilationId,
					String(this.target.questionnaireId),
					this.target.configId,
					session.userCompilationId,
					EVALUATION_EVENT
				);
				this.step = { kind: 'summary', complete: summary.completeFlag === '1' };
			} else {
				this.#enter(next, step.index + 1);
			}
			return 'moved';
		} catch (error) {
			this.moveError = { move: 'next', error: toDataError(error) };
			return 'failed';
		} finally {
			this.working = null;
		}
	}

	/** Whether there is anywhere to go back to inside the questionnaire. */
	get canGoBack(): boolean {
		const step = this.step;
		return step.kind === 'summary' || (step.kind === 'page' && step.index > 0);
	}

	/**
	 * Back to the page at `index` (the previous one by default). From the summary the last page
	 * comes back from memory as it was left; Esse3 then serves each earlier page in turn, with
	 * what it holds. The page being left is saved if it can be: a half-filled page mustn't trap
	 * the student.
	 */
	async back(index?: number): Promise<void> {
		const session = this.#session;
		if (this.working || !session) return;
		if (this.step.kind === 'summary') {
			const last = this.visited.length - 1;
			const page = this.visited[last];
			if (!page) return;
			this.answers = page.answers;
			this.invalid = [];
			this.moveError = undefined;
			this.step = { kind: 'page', page: page.page, index: last };
			if (index === undefined || index >= last) return;
		}
		const step = this.step;
		if (step.kind !== 'page') return;
		const target = index ?? step.index - 1;
		if (target < 0 || target >= step.index) return;
		this.working = 'back';
		this.moveError = undefined;
		try {
			await this.#save(session, step.page).catch(() => undefined);
			let page = step.page;
			let response: Esse3QuestionnairePage | undefined;
			for (let at = step.index; at > target; at--) {
				response = await this.#api.getPreviousPage(
					this.#scope.career.id,
					this.target.activityChoiceId,
					String(this.target.questionnaireId),
					session.compilationId,
					page.id,
					session.userCompilationId
				);
				const previous = toSurveyPage(response);
				if (previous === null) throw new DataFailure({ kind: 'decode', detail: 'No page' });
				page = previous;
			}
			if (response) this.#enter(response, target);
		} catch (error) {
			this.moveError = { move: 'back', error: toDataError(error) };
		} finally {
			this.working = null;
		}
	}

	/**
	 * Sends the compilation for good. Throws a {@link DataFailure} when Esse3 refuses, for the
	 * confirmation dialog to explain.
	 */
	async send(): Promise<void> {
		const session = this.#session;
		if (this.step.kind !== 'summary' || !this.step.complete || !session || this.working) return;
		this.working = 'send';
		try {
			await mutate(
				() =>
					acknowledged(() =>
						this.#api.putQuestionnaireComponentConfirm(
							this.target.activityChoiceId,
							String(this.target.questionnaireId),
							session.compilationId,
							this.#scope.career.id,
							this.target.configId,
							session.userCompilationId,
							EVALUATION_EVENT
						)
					),
				{ invalidate: sentQuestionnaireKeys(this.#scope, this.target.activityChoiceId) }
			);
			this.step = { kind: 'sent' };
		} finally {
			this.working = null;
		}
	}

	async #save(session: Session, page: SurveyPage): Promise<void> {
		const answers = pageAnswers(page, this.answers);
		if (answers.length === 0) return;
		await acknowledged(() =>
			this.#api.savePageAnswers(
				this.#scope.career.id,
				String(this.target.questionnaireId),
				session.compilationId,
				page.id,
				answers,
				EVALUATION_EVENT
			)
		);
	}

	#enter(response: Esse3QuestionnairePage, index: number): void {
		const page = toSurveyPage(response);
		if (page === null) throw new DataFailure({ kind: 'decode', detail: 'Esse3 sent no page' });
		this.answers = savedAnswers(page);
		this.invalid = [];
		this.step = { kind: 'page', page, index };
	}
}

/**
 * Saving and sending answer "OK", which the SDK reads as JSON. Errors from the server come with a
 * 4xx or 5xx before any decoding, so a body that doesn't decode still came with a success.
 */
async function acknowledged(call: () => Promise<unknown>): Promise<void> {
	try {
		await call();
	} catch (error) {
		if (error instanceof DecodeError || error instanceof SyntaxError) return;
		throw error;
	}
}

/*
 * The running compilations, by account, career and unit. A page can be remounted without the
 * student leaving it (the services layout swaps its panes at 1024 px), and the compilation it
 * started must survive that: Esse3 can't hand it back, and starting another would leave the
 * first one stranded.
 */
// eslint-disable-next-line svelte/prefer-svelte-reactivity -- read once as a page mounts: nothing renders from it
const running = new Map<string, Compilation>();

export function compilationKey(scope: SessionScope, activity: number, unit: number): string {
	return `${scope.account.id}/${scope.career.id}/${activity}/${unit}`;
}

export function resumeCompilation(key: string): Compilation | undefined {
	return running.get(key);
}

export function keepCompilation(key: string, compilation: Compilation): void {
	running.set(key, compilation);
}

export function dropCompilation(key: string): void {
	running.delete(key);
}

/*
 * "Fill in" on the units page asks the compile page to start at once. Anything else that lands
 * there (a reload, a link, Forward) finds no request and waits for the student to start, so no
 * compilation is ever created behind their back.
 */
let requested: string | undefined;

export function requestStart(key: string): void {
	requested = key;
}

/** Whether the compile page for `key` was asked to start; the request is used up. */
export function takeStartRequest(key: string): boolean {
	const asked = requested === key;
	requested = undefined;
	return asked;
}
