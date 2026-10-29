import { createSubscriber } from 'svelte/reactivity';

const MINUTE = 60_000;

/** Calls `wake` each time the page is shown again: timers sleep in a hidden tab. */
function onVisible(wake: () => void): () => void {
	const listener = () => {
		if (document.visibilityState === 'visible') wake();
	};
	document.addEventListener('visibilitychange', listener);
	return () => document.removeEventListener('visibilitychange', listener);
}

function midnight(now = new Date()): Date {
	return new Date(now.getFullYear(), now.getMonth(), now.getDate());
}

/**
 * Today's date at local midnight, as reactive state: it rolls over at midnight and when the tab
 * comes back after a night asleep, so "tomorrow" never goes stale on a page left open.
 */
class Today {
	#value = midnight();

	readonly #subscribe = createSubscriber((update) => {
		let timer: ReturnType<typeof setTimeout> | undefined;
		const schedule = () => {
			clearTimeout(timer);
			const now = new Date();
			const next = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1);
			timer = setTimeout(rollover, next.getTime() - now.getTime() + 1000);
		};
		const rollover = () => {
			if (midnight().getTime() !== this.#value.getTime()) update();
			schedule();
		};
		schedule();
		const offVisible = onVisible(rollover);
		return () => {
			clearTimeout(timer);
			offVisible();
		};
	});

	/** The same `Date` all day long, so what derives from it recomputes only when the day turns. */
	get current(): Date {
		this.#subscribe();
		const now = midnight();
		if (now.getTime() !== this.#value.getTime()) this.#value = now;
		return this.#value;
	}
}

export const today = new Today();

const minuteOf = (epochMs: number) => Math.floor(epochMs / MINUTE);

/**
 * The time to the minute, as reactive state, so "free until", "3 hours ago" and the now-line keep up
 * on a page left open. One timer serves every reader: it ticks as each minute turns while anything
 * reads the time, and catches up when the page is shown again.
 */
class Now {
	#value = new Date();

	readonly #subscribe = createSubscriber((update) => {
		let timer: ReturnType<typeof setTimeout> | undefined;
		// Just after the minute turns, so a time on screen changes with the device's clock.
		const schedule = () => {
			clearTimeout(timer);
			timer = setTimeout(tick, MINUTE - (Date.now() % MINUTE) + 50);
		};
		const tick = () => {
			this.#value = new Date();
			update();
			schedule();
		};
		schedule();
		const offVisible = onVisible(tick);
		return () => {
			clearTimeout(timer);
			offVisible();
		};
	});

	/** The same `Date` throughout a minute, so what derives from it recomputes once a minute. */
	get current(): Date {
		this.#subscribe();
		if (minuteOf(Date.now()) !== minuteOf(this.#value.getTime())) this.#value = new Date();
		return this.#value;
	}
}

export const now = new Now();

/**
 * A finer clock for countdowns and check-in windows: it ticks every `every` milliseconds while the
 * component that created it is mounted, and catches up at once when the page is shown again. A
 * function period is read again when its dependencies change, so a clock can speed up as a
 * deadline nears. Create it while a component initializes.
 */
export class Clock {
	/** Epoch milliseconds. */
	now = $state(Date.now());

	constructor(every: number | (() => number)) {
		$effect(() => {
			const period = typeof every === 'number' ? every : every();
			const tick = () => (this.now = Date.now());
			const timer = setInterval(tick, period);
			const offVisible = onVisible(tick);
			return () => {
				clearInterval(timer);
				offVisible();
			};
		});
	}
}
