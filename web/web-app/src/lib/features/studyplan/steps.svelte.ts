import { tick } from 'svelte';
import { afterNavigate, goto } from '$app/navigation';
import { page } from '$app/state';
import type { ResolvedPathname } from '$app/types';

/** What a history entry of the editor remembers. */
interface StepState {
	/** The step of the entry below, when Next added this one. */
	studyplanFrom?: number;
	/** How many entries Next added below this one since the editor opened. */
	studyplanDepth?: number;
}

interface Options {
	/** How many steps there are: the URL's step is clamped into them. */
	total: () => number;
	/** Whether the steps are known, so a step out of range can be corrected in the URL. */
	settled: () => boolean;
	href: (step: number) => ResolvedPathname;
	/** A new step is on screen. */
	onarrive: () => void;
}

/**
 * The editor's step, kept in the URL so Back steps back and a reload keeps it. Next adds a
 * history entry that remembers the step it came from, so in-app Back returns to that entry
 * instead of adding one; any other move replaces the current entry and keeps its marks.
 * Create it while a component initializes.
 */
export class WizardSteps {
	readonly #options: Options;
	#steppingBack = false;
	/** Resolves when the history walk of `finish` lands. */
	#landing: (() => void) | undefined;

	readonly index: number = $derived.by(() => {
		const requested = Math.trunc(Number(page.url.searchParams.get('step') ?? 0)) || 0;
		return Math.min(Math.max(0, requested), this.#options.total() - 1);
	});

	constructor(options: Options) {
		this.#options = options;

		afterNavigate(({ type }) => {
			if (type !== 'popstate') return;
			const landing = this.#landing;
			this.#landing = undefined;
			if (landing) {
				landing();
				return;
			}
			if (this.#steppingBack) window.scrollTo({ top: 0 });
			this.#steppingBack = false;
			options.onarrive();
		});

		/* A step out of range (a reload forgets the chosen path) is corrected in the URL too. */
		$effect(() => {
			const requested = page.url.searchParams.get('step');
			if (options.settled() && requested !== null && Number(requested) !== this.index) {
				void goto(options.href(this.index), {
					replace: true,
					reset: false,
					state: page.state
				});
			}
		});
	}

	async go(step: number, push: boolean): Promise<void> {
		const { studyplanFrom, studyplanDepth = 0 } = page.state as StepState;
		const state: StepState = push
			? { studyplanFrom: this.index, studyplanDepth: studyplanDepth + 1 }
			: { studyplanFrom, studyplanDepth };
		await goto(this.#options.href(step), {
			replace: !push,
			reset: false,
			state: state as App.PageState
		});
		await tick();
		window.scrollTo({ top: 0 });
		this.#options.onarrive();
	}

	back(): void {
		if ((page.state as StepState).studyplanFrom !== this.index - 1) {
			void this.go(this.index - 1, false);
			return;
		}
		this.#steppingBack = true;
		history.back();
	}

	/**
	 * Closes a finished flow: walks back to the entry the editor opened on and turns it into
	 * `href` with `state`, so Back from there leaves the editor instead of replaying its steps.
	 */
	async finish(href: ResolvedPathname, state: App.PageState): Promise<void> {
		const depth = (page.state as StepState).studyplanDepth ?? 0;
		if (depth > 0) {
			await new Promise<void>((resolve) => {
				this.#landing = resolve;
				history.go(-depth);
			});
		}
		await goto(href, { replace: true, reset: false, state });
	}
}
