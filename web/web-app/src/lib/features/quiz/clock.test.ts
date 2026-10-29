import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AttemptClock } from './clock.svelte.ts';
import type { TimerPhase } from './model.ts';

const MINUTE = 60_000;

/** A clock for an attempt ending at `deadline` (ms from now), recording the phases it reports. */
function clock(deadline: number | null, timeLimit: number | null = 600) {
	const phases: TimerPhase[] = [];
	const end = deadline === null ? null : Date.now() + deadline;
	const subject = new AttemptClock({
		deadline: () => end,
		timeLimit: () => timeLimit,
		running: () => true,
		onphase: (phase) => phases.push(phase)
	});
	const stop = subject.start();
	return { subject, phases, stop };
}

describe('AttemptClock', () => {
	beforeEach(() => {
		vi.useFakeTimers();
		vi.setSystemTime(0);
		// The clock catches up when the tab shows again: Node has no document to tell it.
		vi.stubGlobal('document', Object.assign(new EventTarget(), { visibilityState: 'visible' }));
	});
	afterEach(() => {
		vi.useRealTimers();
		vi.unstubAllGlobals();
	});

	it('reports each phase once, as the countdown enters it', () => {
		const { phases, stop } = clock(6 * MINUTE);
		vi.advanceTimersByTime(6 * MINUTE + 2_000);
		expect(phases).toEqual(['warning', 'danger', 'expired']);
		stop();
	});

	it('has time run out at once on a page opened too late, and only then', () => {
		const late = clock(-MINUTE);
		expect(late.phases).toEqual(['expired']);
		late.stop();

		const close = clock(3 * MINUTE);
		vi.advanceTimersByTime(30_000);
		expect(close.phases).toEqual([]);
		close.stop();
	});

	it('shows an untimed countdown only in the last hour before the quiz closes', () => {
		const untimed = clock(2 * 60 * MINUTE, null);
		expect(untimed.subject.visible).toBe(false);
		untimed.stop();

		const closing = clock(50 * MINUTE, null);
		expect(closing.subject.visible).toBe(true);
		expect(closing.subject.left).toBe(50 * MINUTE);
		closing.stop();
	});
});
