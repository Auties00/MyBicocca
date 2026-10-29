import { describe, expect, it } from 'vitest';
import { mergeStatus, streamOf, type DeadlineStream, type StreamState } from './streams.ts';

const offline = { kind: 'offline' } as const;
const failed = { kind: 'unavailable', service: 'esse3', status: 500 } as const;

function stream(
	name: DeadlineStream,
	state: 'pending' | 'loaded' | 'failed' | 'stale'
): StreamState {
	return {
		name,
		settled: state !== 'pending',
		loaded: state === 'loaded' || state === 'stale',
		error: state === 'failed' || state === 'stale' ? offline : undefined,
		updatedAt: state === 'loaded' ? 2_000 : state === 'stale' ? 1_000 : undefined
	};
}

const all = (
	results: Parameters<typeof stream>[1],
	fees: Parameters<typeof stream>[1],
	exams: Parameters<typeof stream>[1],
	bookings: Parameters<typeof stream>[1]
) =>
	mergeStatus([
		stream('results', results),
		stream('fees', fees),
		stream('exams', exams),
		stream('bookings', bookings)
	]);

describe('mergeStatus', () => {
	it('waits for every stream, even when one has already failed', () => {
		expect(all('loaded', 'failed', 'loaded', 'pending')).toMatchObject({
			status: 'pending',
			missing: ['fees'],
			complete: false
		});
	});

	it('is complete once every stream is in the merge', () => {
		expect(all('loaded', 'loaded', 'loaded', 'loaded')).toMatchObject({
			status: 'success',
			failures: [],
			missing: [],
			complete: true,
			updatedAt: 2_000
		});
	});

	it('keeps a stream that failed over older data, and dates the merge by it', () => {
		const merge = all('loaded', 'stale', 'loaded', 'loaded');
		expect(merge).toMatchObject({ status: 'success', missing: [], complete: true });
		expect(merge.failures).toEqual([{ stream: 'fees', error: offline, stale: true }]);
		expect(merge.updatedAt).toBe(1_000);
	});

	it('lists what it lacks when some streams loaded and others failed', () => {
		expect(all('loaded', 'failed', 'loaded', 'failed')).toMatchObject({
			status: 'success',
			missing: ['fees', 'bookings'],
			complete: false
		});
	});

	it('is never complete when one stream loaded and three failed', () => {
		expect(all('failed', 'failed', 'failed', 'loaded')).toMatchObject({
			status: 'success',
			missing: ['results', 'fees', 'exams'],
			complete: false,
			updatedAt: 2_000
		});
	});

	it('fails when no stream has data', () => {
		expect(all('failed', 'failed', 'failed', 'failed')).toMatchObject({
			status: 'error',
			complete: false,
			updatedAt: undefined
		});
	});
});

describe('streamOf', () => {
	const invoices = { data: [1], error: undefined, updatedAt: 3_000 };
	const charges = { data: [2], error: undefined, updatedAt: 2_000 };

	it('waits for every query, and is as old as the oldest', () => {
		expect(streamOf('fees', [invoices, { ...charges, data: undefined }])).toMatchObject({
			settled: false
		});
		expect(streamOf('fees', [invoices, charges])).toEqual({
			name: 'fees',
			settled: true,
			loaded: true,
			error: undefined,
			updatedAt: 2_000
		});
	});

	it('fails when a query that only names the items fails, keeping the items', () => {
		const fees = streamOf('fees', [
			invoices,
			{ data: undefined, error: failed, updatedAt: undefined }
		]);
		expect(fees).toMatchObject({ settled: true, loaded: true, error: failed, updatedAt: 3_000 });
		expect(mergeStatus([fees]).failures).toEqual([{ stream: 'fees', error: failed, stale: true }]);
	});

	it('has no data without its first query, whatever the others hold', () => {
		const fees = streamOf('fees', [
			{ data: undefined, error: failed, updatedAt: undefined },
			charges
		]);
		expect(fees).toMatchObject({ settled: true, loaded: false, error: failed });
		expect(mergeStatus([fees]).missing).toEqual(['fees']);
	});

	it('holds back until what else it waits on is ready', () => {
		expect(streamOf('bookings', [invoices], false).settled).toBe(false);
	});
});
