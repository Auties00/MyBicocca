import { untrack } from 'svelte';
import { showsCountdown, timerPhase, type TimerPhase } from './model.ts';

interface Options {
	/** When the attempt ends, in epoch milliseconds; null when nothing ends it. */
	deadline: () => number | null;
	/** The quiz's limit in seconds, if any: a timed attempt always shows its countdown. */
	timeLimit: () => number | null;
	/** Time counts only while the attempt can still be answered. */
	running: () => boolean;
	/** The countdown entered a later phase: a warning, the last minute, or the end. */
	onphase: (phase: TimerPhase) => void;
}

/**
 * An attempt's clock. It ticks on the second, so what it tells (the time left, whether the
 * countdown shows) is never stale, and it reports each phase the countdown enters once, as it
 * enters it. Create it during component init: it ticks while the component is mounted.
 */
export class AttemptClock {
	now = $state(Date.now());

	readonly #options: Options;
	#phase: TimerPhase | undefined;

	constructor(options: Options) {
		this.#options = options;
		$effect(() => untrack(() => this.start()));
	}

	/** Starts ticking, as the component that created the clock mounts; returns the stop. */
	start(): () => void {
		let timer: ReturnType<typeof setTimeout>;
		// Ticks just after each second turns, so the display never skips or lingers one.
		const tick = () => {
			this.#tick();
			timer = setTimeout(tick, 1000 - (Date.now() % 1000) + 5);
		};
		tick();
		// Timers sleep in a hidden tab: coming back catches up at once.
		const wake = () => {
			if (document.visibilityState === 'visible') this.#tick();
		};
		document.addEventListener('visibilitychange', wake);
		return () => {
			clearTimeout(timer);
			document.removeEventListener('visibilitychange', wake);
		};
	}

	/** Milliseconds left, never below zero; null when nothing ends the attempt. */
	get left(): number | null {
		const deadline = this.#options.deadline();
		return deadline === null ? null : Math.max(0, deadline - this.now);
	}

	get phase(): TimerPhase | null {
		const deadline = this.#options.deadline();
		return deadline === null ? null : timerPhase(deadline - this.now);
	}

	/** Timed attempts always show the countdown; others in the hour before the quiz closes. */
	get visible(): boolean {
		return (
			this.#options.running() &&
			showsCountdown({ timeLimit: this.#options.timeLimit() }, this.#options.deadline(), this.now)
		);
	}

	#tick(): void {
		this.now = Date.now();
		if (!this.#options.running()) return;
		const phase = this.phase;
		const previous = this.#phase;
		this.#phase = phase ?? undefined;
		if (phase === null) return;
		// A page opened after time ran out still has it run out; earlier phases only as they come.
		if (previous === undefined ? phase === 'expired' : phase !== previous) {
			this.#options.onphase(phase);
		}
	}
}
