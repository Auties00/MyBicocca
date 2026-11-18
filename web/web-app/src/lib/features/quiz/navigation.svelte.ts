import { beforeNavigate, goto } from '$app/navigation';
import { on } from 'svelte/events';
import { attemptPath, quizPath, reviewPath, sameAppHref } from './links.ts';

/*
 * Every page of an attempt is a history entry, so Back and reload work while answering. Each
 * entry records how many of the attempt's entries lie below it; once the attempt is over they are
 * stepped back over, so Back from the review leaves the attempt instead of returning to a page of
 * it that can no longer be answered.
 */

interface AttemptState {
	quizAttemptDepth?: number;
}

export interface AttemptIds {
	courseId: number;
	quizId: number;
	attemptId: number;
}

/** How many entries of this attempt the history holds below the current one. */
export function attemptDepth(state: App.PageState): number {
	const depth = (state as AttemptState).quizAttemptDepth;
	return typeof depth === 'number' && Number.isInteger(depth) && depth > 0 ? depth : 0;
}

/** Opens another page of the attempt as a new entry, one deeper than `depth`. */
export function pushAttemptPage(ids: AttemptIds, page: number, depth: number): Promise<void> {
	const state: AttemptState = { quizAttemptDepth: depth + 1 };
	return goto(attemptPath(ids.courseId, ids.quizId, ids.attemptId, page), {
		state,
		persistState: true
	});
}

/** Moves to another page of the attempt in place, keeping the entry's depth. */
export function replaceAttemptPage(ids: AttemptIds, page: number, depth: number): Promise<void> {
	const state: AttemptState = { quizAttemptDepth: depth };
	return goto(attemptPath(ids.courseId, ids.quizId, ids.attemptId, page), {
		replace: true,
		state,
		persistState: true
	});
}

/**
 * Leaves the attempt for its review or its quiz: back to the attempt's first entry, which the
 * destination then replaces. The new navigation starts as soon as the browser has traversed,
 * superseding the one Back began.
 */
export async function leaveAttempt(
	ids: AttemptIds,
	to: 'review' | 'quiz',
	depth: number
): Promise<void> {
	if (depth > 0) {
		await new Promise<void>((resolve) => {
			const off = on(window, 'popstate', () => {
				off();
				resolve();
			});
			history.go(-depth);
		});
	}
	if (to === 'review') {
		await goto(reviewPath(ids.courseId, ids.quizId, ids.attemptId), { replace: true });
	} else {
		await goto(quizPath(ids.courseId, ids.quizId), { replace: true });
	}
}

interface Options {
	/** Whether the guard holds: not once the attempt is over. */
	active: () => boolean;
	/** Answers not saved yet: closing the tab with them asks the browser's own question. */
	dirty: () => boolean;
	/** Saves what's pending; failures are the page's to show. */
	flush: () => Promise<void>;
	/** The attempt's own pages, which come and go freely. */
	inside: (url: URL) => boolean;
}

/**
 * Keeps the student from leaving an attempt by accident. Moving between its pages only saves;
 * going anywhere else in the app asks first ({@link open}), and closing or reloading the tab with
 * answers unsaved gets the browser's prompt. Create it during component init.
 */
export class LeaveGuard {
	/** The confirmation is showing. */
	open = $state(false);

	readonly #options: Options;
	#target: { url: URL; delta?: number } | undefined;
	#released = false;

	constructor(options: Options) {
		this.#options = options;
		beforeNavigate((navigation) => {
			if (this.#released || !this.#options.active()) return;
			if (navigation.type === 'leave') {
				if (this.#options.dirty()) {
					void this.#options.flush().catch(() => undefined);
					navigation.cancel();
				}
				return;
			}
			const to = navigation.to;
			if (to && this.#options.inside(to.url)) {
				void this.#options.flush().catch(() => undefined);
				return;
			}
			navigation.cancel();
			if (to) {
				this.#target = {
					url: to.url,
					delta: navigation.type === 'popstate' ? navigation.delta : undefined
				};
				this.open = true;
			}
		});
	}

	/** Lets every navigation through from now on: the attempt is over, or the student chose to go. */
	release(): void {
		this.#released = true;
		this.open = false;
	}

	/** Goes where the student was headed, once what's pending is saved. */
	async leave(): Promise<void> {
		await this.#options.flush().catch(() => undefined);
		this.release();
		const target = this.#target;
		if (target?.delta !== undefined) history.go(target.delta);
		else if (target) await goto(sameAppHref(target.url));
	}
}
