import { beforeNavigate, goto, type NavigationType } from '$app/navigation';
import type { ResolvedPathname } from '$app/types';

/** Where the student was heading when asked whether to leave. */
export type Destination =
	| { kind: 'history'; delta: number }
	| { kind: 'app'; href: string }
	| { kind: 'away'; href: string };

export type LeaveDecision =
	/** Nothing would be lost. */
	| { action: 'go' }
	/** Stay put without asking. */
	| { action: 'stay' }
	/** Back inside the questionnaire returns to its previous page. */
	| { action: 'previous' }
	| { action: 'ask'; destination: Destination };

export interface LeaveAttempt {
	type: NavigationType;
	/** Steps through history, for a `popstate`: negative going back. */
	delta?: number;
	to: URL | null;
}

export interface CompileState {
	/** A compilation is running and unsent. */
	open: boolean;
	/** Something has to finish first: a send in flight, or a dialog already open. */
	busy: boolean;
	canGoBack: boolean;
}

/** What a navigation away from a running compilation does. */
export function decideLeave(
	attempt: LeaveAttempt,
	state: CompileState,
	origin: string
): LeaveDecision {
	if (!state.open) return { action: 'go' };
	// Cancelling a tab close has the browser ask, in its own words.
	if (state.busy || attempt.type === 'leave') return { action: 'stay' };
	if (attempt.type === 'popstate') {
		const delta = attempt.delta ?? 0;
		if (delta < 0 && state.canGoBack) return { action: 'previous' };
		return { action: 'ask', destination: { kind: 'history', delta } };
	}
	const to = attempt.to;
	if (!to) return { action: 'stay' };
	return {
		action: 'ask',
		destination:
			to.origin === origin
				? { kind: 'app', href: `${to.pathname}${to.search}${to.hash}` }
				: { kind: 'away', href: to.href }
	};
}

/**
 * Asks before any way out of a running compilation, whose answers would be lost: links, Back,
 * typed addresses and closing the tab. Create it while a component initialises.
 */
export class LeaveGuard {
	/** The dialog asking whether to leave is open. */
	asking = $state(false);
	#destination: Destination | undefined;
	#released = false;

	constructor(state: () => CompileState, onprevious: () => void) {
		beforeNavigate((navigation) => {
			if (this.#released) return;
			const current = state();
			const decision = decideLeave(
				{
					type: navigation.type,
					delta: navigation.type === 'popstate' ? navigation.delta : undefined,
					to: navigation.to?.url ?? null
				},
				{ ...current, busy: current.busy || this.asking },
				location.origin
			);
			if (decision.action === 'go') return;
			navigation.cancel();
			if (decision.action === 'previous') {
				onprevious();
			} else if (decision.action === 'ask') {
				this.#destination = decision.destination;
				this.asking = true;
			}
		});
	}

	/** Leaves for where the student was heading, or for `fallback` when leaving was their idea. */
	leave(fallback: ResolvedPathname): void {
		this.#released = true;
		const target = this.#destination ?? { kind: 'app', href: fallback };
		this.#destination = undefined;
		if (target.kind === 'history') history.go(target.delta);
		else if (target.kind === 'away') location.assign(target.href);
		else void goto(target.href as ResolvedPathname);
	}

	stay(): void {
		this.#destination = undefined;
	}

	/** Lets every navigation through from now on, without asking. */
	release(): void {
		this.#released = true;
	}
}
