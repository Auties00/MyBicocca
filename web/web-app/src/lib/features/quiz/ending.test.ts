import { describe, expect, it } from 'vitest';
import { DataFailure, type DataError } from '#lib/data/errors.ts';
import type { SubmitResult } from './attempt.svelte.ts';
import { AttemptEnding } from './ending.svelte.ts';

const unavailable: DataError = { kind: 'unavailable', service: 'moodle', status: 503 };
const closedElsewhere: DataError = {
	kind: 'rejected',
	service: 'moodle',
	status: 200,
	code: 'attemptalreadyclosed',
	message: ''
};

/** An ending whose submissions answer `results` in turn, recording what it asked of the page. */
function ending(...results: SubmitResult[]) {
	const calls = { submits: [] as boolean[], interrupts: 0, leaves: [] as string[] };
	const subject = new AttemptEnding({
		submit: async (timeUp) => {
			calls.submits.push(timeUp);
			return results.shift() ?? { ok: true, overdue: false };
		},
		interrupt: () => calls.interrupts++,
		leave: (to) => calls.leaves.push(to)
	});
	return { subject, calls };
}

describe('AttemptEnding', () => {
	it('submits at time-up over whatever is open, once, and leads to the review', async () => {
		const { subject, calls } = ending({ ok: true, overdue: false });
		await subject.expire();
		await subject.expire();
		expect(calls).toEqual({ submits: [true], interrupts: 1, leaves: ['review'] });
		expect(subject.finished).toBe(true);
	});

	it('waits for the submission when Moodle holds the attempt for its grace period', async () => {
		const { subject, calls } = ending({ ok: true, overdue: true });
		await subject.expire();
		expect(subject.overdue).toBe(true);
		expect(subject.timeUp).toBe(false);
		expect(calls.leaves).toEqual([]);
	});

	it('lets a failed time-up submission be retried, and takes an attempt closed elsewhere as submitted', async () => {
		const { subject, calls } = ending(
			{ ok: false, error: unavailable },
			{ ok: false, error: closedElsewhere }
		);
		await subject.expire();
		expect(subject.timeUpError).toBeTypeOf('string');
		expect(calls.leaves).toEqual([]);
		await subject.expire();
		expect(calls).toEqual({ submits: [true, true], interrupts: 2, leaves: ['review'] });
	});

	it('throws a refused overdue submission, for its confirmation to show', async () => {
		const { subject, calls } = ending({ ok: false, error: unavailable });
		await expect(subject.submitOverdue()).rejects.toBeInstanceOf(DataFailure);
		expect(calls.submits).toEqual([false]);
		expect(subject.finished).toBe(false);
	});
});
