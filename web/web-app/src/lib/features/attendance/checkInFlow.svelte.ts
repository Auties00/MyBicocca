import { untrack } from 'svelte';
import { goto } from '$app/navigation';
import { resolve } from '$app/paths';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import { m } from '#lib/paraglide/messages.js';
import { toast, type ToastTone } from '#lib/ui/toast.svelte.ts';
import type { Tone } from '#lib/ui/types.ts';
import { checkIn } from './checkIn.ts';
import { outcomeView } from './labels.ts';
import type { CheckInOutcome } from './outcome.ts';
import { parsePresenceScan } from './scan.ts';

/** Where the payload came from: the lesson code field, the link field, or the camera. */
export type CheckInSource = 'code' | 'link' | 'scan';

export type CheckInPhase =
	| { name: 'form' }
	| { name: 'sending'; source: CheckInSource }
	/** `at`: when the outcome arrived, in epoch milliseconds. */
	| { name: 'result'; source: CheckInSource; outcome: CheckInOutcome; at: number };

const TOAST_TONES: Partial<Record<Tone, ToastTone>> = {
	success: 'success',
	info: 'info',
	danger: 'danger'
};

/**
 * Tells a student who went elsewhere how the check-in ended, with the way back to it. Anything
 * but a recorded presence stays until dismissed.
 */
function announce(outcome: CheckInOutcome): number {
	const shown = outcomeView(outcome);
	return toast.show({
		message: shown.title,
		description: shown.detail,
		tone: TOAST_TONES[shown.tone] ?? 'neutral',
		duration: outcome.kind === 'recorded' ? 8000 : 0,
		action: {
			label: m.attendance_view_outcome(),
			onclick: () => void goto(resolve('services/attendance/check-in'))
		}
	});
}

/**
 * The check-in under way: what was typed, the request on its way, its outcome. It lives outside
 * the components showing it, which the student may leave (or a rotation may rebuild) while the
 * request runs: an outcome arriving with nothing on screen to show it is announced instead, and
 * kept until it has been seen.
 */
export class CheckInFlow {
	phase = $state.raw<CheckInPhase>({ name: 'form' });
	code = $state('');
	link = $state('');
	/* Plain, not reactive: they're only looked at when an outcome arrives or the page changes. */
	#views: HTMLElement[] = [];
	#seen = false;
	#notice: number | undefined;

	get sending(): boolean {
		return this.phase.name === 'sending';
	}

	/** Attachment for an element showing this check-in. */
	readonly view = (node: HTMLElement) => {
		this.#views.push(node);
		untrack(() => this.noticed());
		return () => {
			this.#views = this.#views.filter((view) => view !== node);
		};
	};

	/** Whether the outcome has been on screen; one on screen now counts as seen. */
	noticed(): boolean {
		if (this.phase.name !== 'result' || this.#seen) return this.#seen;
		this.#seen = this.#views.some((node) => node.getClientRects().length > 0);
		if (this.#seen && this.#notice !== undefined) {
			toast.dismiss(this.#notice);
			this.#notice = undefined;
		}
		return this.#seen;
	}

	/**
	 * Sends the check-in. The student may go anywhere in the app meanwhile; leaving it (a reload,
	 * closing the tab) would lose an irreversible request's outcome, so the browser asks first.
	 */
	async submit(scope: SessionScope, source: CheckInSource): Promise<void> {
		const raw = source === 'code' ? this.code : this.link;
		if (this.sending || raw.trim() === '') return;
		this.phase = { name: 'sending', source };
		this.#seen = false;
		const guard = (event: BeforeUnloadEvent) => event.preventDefault();
		window.addEventListener('beforeunload', guard);
		let outcome: CheckInOutcome;
		try {
			outcome = await checkIn(scope, parsePresenceScan(raw));
		} finally {
			window.removeEventListener('beforeunload', guard);
		}
		this.phase = { name: 'result', source, outcome, at: Date.now() };
		if (!this.noticed()) this.#notice = announce(outcome);
	}

	/** Back to the form, keeping what was typed so it can be corrected. */
	edit(): void {
		if (!this.sending) this.phase = { name: 'form' };
	}

	/** A fresh form, once the outcome has been seen. */
	reset(): void {
		if (this.sending) return;
		this.phase = { name: 'form' };
		this.code = '';
		this.link = '';
	}
}

let current: { owner: string; flow: CheckInFlow } | undefined;

/** The check-in of this account's career; another career or account starts afresh. */
export function checkInFlow(scope: SessionScope): CheckInFlow {
	const owner = `${scope.account.id}:${scope.career.id}`;
	if (current?.owner !== owner) current = { owner, flow: new CheckInFlow() };
	return current.flow;
}
