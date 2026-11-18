import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { Esse3ExamSessionEnrollment } from '@mybicocca/data-api/esse3';
import { DataFailure } from '#lib/data/errors.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import { decide } from './decide.ts';
import type { ExamResult } from './model.ts';

const cache = vi.hoisted(() => ({
	rows: [] as Esse3ExamSessionEnrollment[],
	invalidated: [] as unknown[][]
}));

vi.mock('#lib/data/query.svelte.ts', async (original) => ({
	...(await original<typeof import('#lib/data/query.svelte.ts')>()),
	async mutate<T>(run: () => Promise<T>, options: { invalidate?: unknown[][] } = {}) {
		try {
			return await run();
		} finally {
			cache.invalidated.push(...(options.invalidate ?? []));
		}
	},
	setQueryData(_key: unknown, update: (rows: Esse3ExamSessionEnrollment[]) => unknown) {
		cache.rows = update(cache.rows) as Esse3ExamSessionEnrollment[];
	}
}));

const row = (applicationListId: number) =>
	({ applicationListId, acknowledgmentOfReceipt: 'N' }) as Esse3ExamSessionEnrollment;

const result = { id: '901', applicationListId: 901 } as ExamResult;

/** A session whose Esse3 answers the acknowledgment with `reply`. */
function scope(reply: () => Promise<unknown>) {
	const put = vi.fn(reply);
	const value = {
		account: { id: 'a' },
		career: { id: 7, enrollmentTraitId: 42 },
		api: { esse3: { examsCalendar: { putAcknowledgmentOfReceiptApplicationList: put } } }
	} as unknown as SessionScope;
	return { value, put };
}

beforeEach(() => {
	cache.rows = [row(900), row(901)];
	cache.invalidated = [];
});

describe('decide', () => {
	it('sends the answer, marks the result answered and reloads the bookings and the record book', async () => {
		const { value, put } = scope(() => Promise.resolve({}));
		await decide(value, result, 'accepted');
		expect(put).toHaveBeenCalledWith(42, 901, 'A');
		expect(cache.rows.map((item) => item.acknowledgmentOfReceipt)).toEqual(['N', 'A']);
		expect(cache.invalidated.map((key) => key.at(-1))).toEqual([
			'esse3.transcript.getBookingsByMatId',
			'esse3.transcript.getRecordBookRows'
		]);
	});

	it('takes a reply that doesn’t decode as the answer recorded', async () => {
		const { value } = scope(() =>
			Promise.reject(new DataFailure({ kind: 'decode', detail: 'not a booking' }))
		);
		await expect(decide(value, result, 'rejected')).resolves.toBeUndefined();
		expect(cache.rows[1].acknowledgmentOfReceipt).toBe('R');
		expect(cache.invalidated).toHaveLength(1);
	});

	it('leaves the result waiting when Esse3 refuses', async () => {
		const refused = new DataFailure({
			kind: 'rejected',
			service: 'esse3',
			status: 422,
			message: 'Termine scaduto'
		});
		const { value } = scope(() => Promise.reject(refused));
		await expect(decide(value, result, 'rejected')).rejects.toBe(refused);
		expect(cache.rows[1].acknowledgmentOfReceipt).toBe('N');
	});

	it('refuses a result no booking id addresses, without calling Esse3', async () => {
		const { value, put } = scope(() => Promise.resolve({}));
		await expect(decide(value, { ...result, applicationListId: null }, 'accepted')).rejects.toThrow(
			DataFailure
		);
		expect(put).not.toHaveBeenCalled();
	});
});
