import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { MoodleQuizQuestion } from '@mybicocca/data-api/elearning';
import { DataFailure } from '#lib/data/errors.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import { AttemptSession } from './attempt.svelte.ts';
import { SubmitCheck } from './submit.svelte.ts';

vi.mock('./queries.ts', () => ({ invalidateAttempts: () => undefined }));

type Data = { name: string; value: string }[];

/** A two-page attempt, one multiple-choice question per page, as Moodle renders it. */
function question(slot: number, sequence = 1): MoodleQuizQuestion {
	const prefix = `q9:${slot}_`;
	return {
		slot,
		type: 'multichoice',
		page: slot - 1,
		questionNumber: String(slot),
		number: slot,
		html: `<div class="formulation"><input type="hidden" name="${prefix}:sequencecheck" value="${sequence}">
		<div class="qtext">Domanda ${slot}</div><div class="answer">
		<div class="r0"><input type="radio" name="${prefix}answer" value="0"><label>A</label></div>
		<div class="r1"><input type="radio" name="${prefix}answer" value="1"><label>B</label></div></div></div>`,
		sequenceCheck: sequence,
		flagged: false,
		state: 'todo',
		mark: null,
		maxMark: 1
	} as MoodleQuizQuestion;
}

/** Moodle's quiz calls, recorded in order; each answers at once unless a test holds it. */
function fakeMoodle() {
	const calls: { fn: string; data?: Data; page?: number }[] = [];
	const held: Record<string, PromiseWithResolvers<unknown> | undefined> = {};
	const answer = <T>(fn: string, value: T): Promise<T> => {
		const hold = held[fn];
		held[fn] = undefined;
		return hold ? (hold.promise as Promise<T>) : Promise.resolve(value);
	};
	const quiz = {
		getAttemptData: (_id: number, page: number) => {
			calls.push({ fn: 'getAttemptData', page });
			return answer('getAttemptData', {
				attempt: { id: 1, state: 'inprogress' },
				nextPage: page === 0 ? 1 : -1,
				questions: [question(page + 1)],
				messages: [],
				warnings: []
			});
		},
		saveAttempt: (_id: number, data: Data) => {
			calls.push({ fn: 'saveAttempt', data });
			return answer('saveAttempt', { status: true, warnings: [] });
		},
		processAttempt: (_id: number, _auth: unknown, options: { data: Data }) => {
			calls.push({ fn: 'processAttempt', data: options.data });
			return answer('processAttempt', { state: 'finished', warnings: [] });
		},
		getAttemptSummary: () => {
			calls.push({ fn: 'getAttemptSummary' });
			return answer('getAttemptSummary', {
				questions: [question(1, 3), question(2, 5)],
				totalUnanswered: 2,
				warnings: []
			});
		}
	};
	const scope = {
		api: { moodle: <T>(call: (moodle: unknown, auth: unknown) => Promise<T>) => call({ quiz }, {}) }
	} as unknown as SessionScope;
	/** The next call to `fn` waits until the test settles it. */
	const hold = (fn: string) => {
		const deferred = Promise.withResolvers<unknown>();
		held[fn] = deferred;
		return deferred;
	};
	return { calls, scope, hold };
}

function session(moodle: ReturnType<typeof fakeMoodle>, autoSavePeriod: number | null = 60) {
	return new AttemptSession({
		scope: () => moodle.scope,
		courseId: 5,
		quizId: 7,
		attemptId: 1,
		autoSavePeriod: () => autoSavePeriod
	});
}

const names = (data: Data | undefined) =>
	Object.fromEntries((data ?? []).map((f) => [f.name, f.value]));

describe('AttemptSession', () => {
	beforeEach(() => vi.useFakeTimers());
	afterEach(() => vi.useRealTimers());

	it('saves the changed answers before it loads another page', async () => {
		const moodle = fakeMoodle();
		const attempt = session(moodle);
		await attempt.load(0);
		attempt.answer(1, 'q9:1_answer', '1');
		await attempt.load(1);
		expect(moodle.calls.map((call) => call.fn)).toEqual([
			'getAttemptData',
			'saveAttempt',
			'getAttemptData'
		]);
		expect(names(moodle.calls[1].data)).toEqual({
			'q9:1_:sequencecheck': '1',
			'q9:1_answer': '1',
			'q9:1_:flagged': '0'
		});
		expect(attempt.dirty).toBe(false);
		expect(attempt.saveState).toBe('saved');
	});

	it('keeps answers a failed save lost, and sends them with the next one', async () => {
		const moodle = fakeMoodle();
		const attempt = session(moodle);
		await attempt.load(0);
		attempt.answer(1, 'q9:1_answer', '0');
		moodle
			.hold('saveAttempt')
			.reject(new DataFailure({ kind: 'unavailable', service: 'moodle', status: 503 }));
		await expect(attempt.flush()).rejects.toBeInstanceOf(DataFailure);
		expect(attempt.saveState).toBe('error');
		expect(attempt.dirty).toBe(true);
		await attempt.flush();
		expect(names(moodle.calls.at(-1)?.data)).toMatchObject({ 'q9:1_answer': '0' });
		expect(attempt.saveState).toBe('saved');
	});

	it('tells an offline save apart from a failed one', async () => {
		const moodle = fakeMoodle();
		const attempt = session(moodle);
		await attempt.load(0);
		attempt.answer(1, 'q9:1_answer', '0');
		moodle.hold('saveAttempt').reject(new DataFailure({ kind: 'offline' }));
		await attempt.flush().catch(() => undefined);
		expect(attempt.saveState).toBe('offline');
	});

	it('autosaves when the first change falls due; a flag only brings the save closer', async () => {
		const moodle = fakeMoodle();
		const attempt = session(moodle, 60);
		await attempt.load(0);
		attempt.answer(1, 'q9:1_answer', '0');
		await vi.advanceTimersByTimeAsync(30_000);
		attempt.answer(1, 'q9:1_answer', '1');
		await vi.advanceTimersByTimeAsync(29_000);
		expect(moodle.calls.filter((call) => call.fn === 'saveAttempt')).toHaveLength(0);
		await vi.advanceTimersByTimeAsync(1_000);
		expect(moodle.calls.filter((call) => call.fn === 'saveAttempt')).toHaveLength(1);

		attempt.answer(1, 'q9:1_answer', '0');
		await vi.advanceTimersByTimeAsync(10_000);
		attempt.toggleFlag(1);
		await vi.advanceTimersByTimeAsync(1_500);
		const saves = moodle.calls.filter((call) => call.fn === 'saveAttempt');
		expect(saves).toHaveLength(2);
		expect(names(saves[1].data)).toMatchObject({ 'q9:1_:flagged': '1' });
	});

	it('never autosaves where the site turned it off', async () => {
		const moodle = fakeMoodle();
		const attempt = session(moodle, 0);
		await attempt.load(0);
		attempt.answer(1, 'q9:1_answer', '0');
		await vi.advanceTimersByTimeAsync(600_000);
		expect(moodle.calls.filter((call) => call.fn === 'saveAttempt')).toHaveLength(0);
		expect(attempt.saveState).toBe('pending');
	});

	it('submits after the save in flight, with the answers changed since', async () => {
		const moodle = fakeMoodle();
		const attempt = session(moodle);
		await attempt.load(0);
		await attempt.load(1);
		attempt.answer(1, 'q9:1_answer', '1');
		const saving = moodle.hold('saveAttempt');
		const flushed = attempt.flush();
		attempt.answer(2, 'q9:2_answer', '0');
		const submitted = attempt.submit();
		await vi.advanceTimersByTimeAsync(0);
		expect(moodle.calls.at(-1)?.fn).toBe('saveAttempt');
		saving.resolve({ status: true, warnings: [] });
		await flushed;
		expect(await submitted).toEqual({ ok: true, overdue: false });
		const process = moodle.calls.at(-1);
		expect(process?.fn).toBe('processAttempt');
		expect(names(process?.data)).toEqual({
			'q9:2_:sequencecheck': '1',
			'q9:2_answer': '0',
			'q9:2_:flagged': '0'
		});
	});

	it('reports an attempt Moodle keeps open for its grace period as overdue', async () => {
		const moodle = fakeMoodle();
		const attempt = session(moodle);
		await attempt.load(0);
		moodle.hold('processAttempt').resolve({ state: 'overdue', warnings: [] });
		expect(await attempt.submit(true)).toEqual({ ok: true, overdue: true });
	});

	it('takes the sequence checks Moodle has now and saves again after a refusal', async () => {
		const moodle = fakeMoodle();
		const attempt = session(moodle);
		await attempt.load(0);
		attempt.answer(1, 'q9:1_answer', '1');
		moodle.hold('saveAttempt').reject(
			new DataFailure({
				kind: 'rejected',
				service: 'moodle',
				status: 200,
				code: 'submissionoutofsequence',
				message: ''
			})
		);
		await attempt.flush().catch(() => undefined);
		await attempt.resync();
		expect(names(moodle.calls.at(-1)?.data)).toMatchObject({
			'q9:1_:sequencecheck': '3',
			'q9:1_answer': '1'
		});
		expect(attempt.questions[0].fields['q9:1_:sequencecheck']).toBe('3');
	});

	it('checks before submitting with one save and one summary, and leaves a failed save to the student', async () => {
		const moodle = fakeMoodle();
		const attempt = session(moodle);
		const count = (fn: string) => moodle.calls.filter((call) => call.fn === fn).length;
		const check = new SubmitCheck({
			prepare: () => attempt.check(),
			submit: () => attempt.submit()
		});
		await attempt.load(0);
		attempt.answer(1, 'q9:1_answer', '1');
		check.show();
		await vi.advanceTimersByTimeAsync(0);
		expect([count('saveAttempt'), count('getAttemptSummary')]).toEqual([1, 1]);
		expect(check.items?.map((item) => item.answered)).toEqual(['complete', 'empty']);
		check.close();

		attempt.answer(1, 'q9:1_answer', '0');
		moodle
			.hold('saveAttempt')
			.reject(new DataFailure({ kind: 'unavailable', service: 'moodle', status: 503 }));
		check.show();
		await vi.advanceTimersByTimeAsync(600_000);
		expect([count('saveAttempt'), count('getAttemptSummary')]).toEqual([2, 1]);
		expect(check.failure?.kind).toBe('unavailable');
		expect(check.items).toBeUndefined();

		await check.check();
		expect([count('saveAttempt'), count('getAttemptSummary')]).toEqual([3, 2]);
		expect(check.failure).toBeUndefined();
	});

	it("lists the page's questions as answered locally, the others as Moodle says", async () => {
		const moodle = fakeMoodle();
		const attempt = session(moodle);
		await attempt.load(0);
		await attempt.summarize();
		attempt.answer(1, 'q9:1_answer', '0');
		attempt.toggleFlag(1);
		expect(attempt.navItems).toEqual([
			{ slot: 1, number: '1', page: 0, answered: 'complete', flagged: true },
			{ slot: 2, number: '2', page: 1, answered: 'empty', flagged: false }
		]);
	});
});
