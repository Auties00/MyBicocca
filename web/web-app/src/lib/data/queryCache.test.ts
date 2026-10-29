import { describe, expect, it, vi } from 'vitest';
import type { DataError } from './errors.ts';
import {
	QueryCache,
	type PersistedEntry,
	type QueryPersistence,
	type QuerySnapshot
} from './queryCache.ts';

const toError = (error: unknown): DataError => ({ kind: 'unknown', detail: String(error) });

function memoryPersistence(initial: Record<string, PersistedEntry> = {}) {
	const records = new Map(Object.entries(initial));
	const persistence: QueryPersistence = {
		read: async (key) => records.get(key),
		write: async (key, entry) => void records.set(key, entry),
		remove: async (prefix) => {
			for (const key of [...records.keys()]) {
				if (key === prefix || key.startsWith(`${prefix}/`)) records.delete(key);
			}
		}
	};
	return { persistence, records };
}

/** A load whose calls resolve or reject when the test says so. */
function deferredLoads<T>() {
	const pending: Array<{ resolve: (value: T) => void; reject: (error: unknown) => void }> = [];
	const load = () => new Promise<T>((resolve, reject) => pending.push({ resolve, reject }));
	return { load, pending };
}

const settle = () => new Promise((resolve) => setTimeout(resolve, 0));

function record<T>(list: Array<QuerySnapshot<T>>) {
	return (snapshot: QuerySnapshot<T>) => list.push(snapshot);
}

describe('QueryCache', () => {
	it('goes from pending to success, flagging the load in flight', async () => {
		const cache = new QueryCache({ toError });
		const { load, pending } = deferredLoads<string>();
		const seen: Array<QuerySnapshot<string>> = [];
		cache.observe(['acct', 'a', '-', 'exams'], load, { staleMs: 60_000 }, record(seen));
		await settle();
		pending[0].resolve('data');
		await settle();
		expect(seen.map(({ status, refreshing, data }) => [status, refreshing, data])).toEqual([
			['pending', false, undefined],
			['pending', true, undefined],
			['success', false, 'data']
		]);
	});

	it('serves persisted data at once, then revalidates it when stale', async () => {
		const { persistence } = memoryPersistence({
			'acct/a/-/exams': { data: 'cached', updatedAt: 0 }
		});
		const cache = new QueryCache({ toError, persistence, now: () => 120_000 });
		const { load, pending } = deferredLoads<string>();
		const seen: Array<QuerySnapshot<string>> = [];
		cache.observe(
			['acct', 'a', '-', 'exams'],
			load,
			{ staleMs: 60_000, persist: true },
			record(seen)
		);
		await settle();
		expect(seen.at(-1)).toMatchObject({ status: 'success', data: 'cached', refreshing: true });
		pending[0].resolve('live');
		await settle();
		expect(seen.at(-1)).toMatchObject({ data: 'live', updatedAt: 120_000, refreshing: false });
	});

	it('keeps fresh persisted data without reloading it', async () => {
		const { persistence } = memoryPersistence({
			'public/-/-/rooms': { data: 'cached', updatedAt: 100 }
		});
		const cache = new QueryCache({ toError, persistence, now: () => 1_000 });
		let loads = 0;
		const seen: Array<QuerySnapshot<string>> = [];
		cache.observe(
			['public', '-', '-', 'rooms'],
			async () => `live${++loads}`,
			{ staleMs: 60_000, persist: true },
			record(seen)
		);
		await settle();
		expect(loads).toBe(0);
		expect(seen.at(-1)?.data).toBe('cached');
	});

	it('keeps stale data when a reload fails, with the error attached', async () => {
		let now = 0;
		const cache = new QueryCache({ toError, now: () => now });
		let fail = false;
		const load = async () => {
			if (fail) throw new Error('offline');
			return 'first';
		};
		await cache.fetch(['k'], load, { staleMs: 0 });
		fail = true;
		now = 10;
		const seen: Array<QuerySnapshot<string>> = [];
		cache.observe(['k'], load, { staleMs: 0 }, record(seen));
		await settle();
		expect(seen.at(-1)).toMatchObject({
			status: 'success',
			data: 'first',
			error: { kind: 'unknown', detail: 'Error: offline' }
		});
	});

	it('reports an error status when a first load fails', async () => {
		const cache = new QueryCache({ toError });
		const seen: Array<QuerySnapshot<string>> = [];
		cache.observe(['k'], () => Promise.reject(new Error('x')), { staleMs: 0 }, record(seen));
		await settle();
		expect(seen.at(-1)).toMatchObject({ status: 'error', data: undefined });
	});

	it('shares one load between concurrent observers and fetches', async () => {
		const cache = new QueryCache({ toError });
		const { load, pending } = deferredLoads<number>();
		cache.observe(['k'], load, { staleMs: 0 }, () => {});
		cache.observe(['k'], load, { staleMs: 0 }, () => {});
		await settle();
		const fetched = cache.fetch(['k'], load, { staleMs: 0 });
		expect(pending).toHaveLength(1);
		pending[0].resolve(7);
		expect(await fetched).toBe(7);
	});

	it('reloads only the invalidated prefix, and marks unobserved entries stale', async () => {
		const cache = new QueryCache({ toError });
		const loads: string[] = [];
		const loader = (name: string) => async () => {
			loads.push(name);
			return name;
		};
		const policy = { staleMs: 60_000 };
		cache.observe(['acct', 'a', '1', 'exams', 'booked'], loader('booked'), policy, () => {});
		cache.observe(['acct', 'a', '1', 'fees'], loader('fees'), policy, () => {});
		await cache.fetch(['acct', 'a', '1', 'exams', 'calls'], loader('calls'), policy);
		await settle();
		loads.length = 0;

		cache.invalidate(['acct', 'a', '1', 'exams']);
		await settle();
		expect(loads).toEqual(['booked']);

		await cache.fetch(['acct', 'a', '1', 'exams', 'calls'], loader('calls'), policy);
		expect(loads).toEqual(['booked', 'calls']);
	});

	it('reloads again when invalidated during a load', async () => {
		const cache = new QueryCache({ toError });
		const { load, pending } = deferredLoads<string>();
		const seen: Array<QuerySnapshot<string>> = [];
		cache.observe(['k'], load, { staleMs: 60_000 }, record(seen));
		await settle();
		cache.invalidate(['k']);
		pending[0].resolve('before the write');
		await settle();
		expect(pending).toHaveLength(2);
		pending[1].resolve('after the write');
		await settle();
		expect(seen.at(-1)?.data).toBe('after the write');
	});

	it('wipes a prefix from memory and persistence, ignoring loads still in flight', async () => {
		const { persistence, records } = memoryPersistence();
		const cache = new QueryCache({ toError, persistence });
		const policy = { staleMs: 60_000, persist: true };
		await cache.fetch(['acct', 'a', '-', 'x'], async () => 'a', policy);
		await cache.fetch(['acct', 'b', '-', 'x'], async () => 'b', policy);
		const { load, pending } = deferredLoads<string>();
		const seen: Array<QuerySnapshot<string>> = [];
		cache.observe(['acct', 'a', '-', 'y'], load, policy, record(seen));
		await settle();

		await cache.remove(['acct', 'a']);
		pending[0].resolve('late');
		await settle();
		expect(cache.peek(['acct', 'a', '-', 'x']).data).toBeUndefined();
		expect(cache.peek(['acct', 'a', '-', 'y']).data).toBeUndefined();
		expect(seen.at(-1)?.data).toBeUndefined();
		expect([...records.keys()]).toEqual(['acct/b/-/x']);
	});

	it('loads as usual when the storage fails to read, write or forget', async () => {
		const broken: QueryPersistence = {
			read: () => Promise.reject(new DOMException('Quota exceeded', 'QuotaExceededError')),
			write: () => Promise.reject(new DOMException('Quota exceeded', 'QuotaExceededError')),
			remove: () => Promise.reject(new Error('Internal error opening database'))
		};
		const cache = new QueryCache({ toError, persistence: broken });
		const policy = { staleMs: 60_000, persist: true, encrypt: true };
		const seen: Array<QuerySnapshot<string>> = [];
		cache.observe(['acct', 'a', '-', 'k'], async () => 'live', policy, record(seen));
		await settle();
		expect(seen.at(-1)).toMatchObject({ status: 'success', data: 'live', error: undefined });
		await expect(cache.remove(['acct', 'a'])).resolves.toBeUndefined();
		expect(cache.peek(['acct', 'a', '-', 'k']).data).toBeUndefined();
	});

	it('goes to the network when the storage never answers, and still shows what it brings', async () => {
		vi.useFakeTimers();
		try {
			let answer: (entry: PersistedEntry) => void = () => {};
			const slow: QueryPersistence = {
				read: () => new Promise((resolve) => (answer = resolve)),
				write: async () => {},
				remove: async () => {}
			};
			const cache = new QueryCache({ toError, persistence: slow, now: () => 1_000 });
			const { load, pending } = deferredLoads<string>();
			const seen: Array<QuerySnapshot<string>> = [];
			cache.observe(['k'], load, { staleMs: 60_000, persist: true }, record(seen));
			await vi.advanceTimersByTimeAsync(1_000);
			expect(pending).toHaveLength(0);
			await vi.advanceTimersByTimeAsync(1_500);
			expect(pending).toHaveLength(1);
			answer({ data: 'cached', updatedAt: 500 });
			await vi.advanceTimersByTimeAsync(0);
			expect(seen.at(-1)).toMatchObject({ data: 'cached', refreshing: true });
			pending[0].resolve('live');
			await vi.advanceTimersByTimeAsync(0);
			expect(seen.at(-1)).toMatchObject({ data: 'live', refreshing: false });
		} finally {
			vi.useRealTimers();
		}
	});

	it('shows a suspect answer, but neither keeps it nor counts it fresh', async () => {
		const { persistence, records } = memoryPersistence();
		const cache = new QueryCache({ toError, persistence });
		const answers = [[], ['desk']];
		let loads = 0;
		const policy = {
			staleMs: 60_000,
			persist: true,
			staleWhen: (list: string[]) => list.length === 0
		};
		const load = async () => answers[loads++];
		expect(await cache.fetch(['public', '-', '-', 'services'], load, policy)).toEqual([]);
		expect(records.size).toBe(0);
		expect(await cache.fetch(['public', '-', '-', 'services'], load, policy)).toEqual(['desk']);
		expect(await cache.fetch(['public', '-', '-', 'services'], load, policy)).toEqual(['desk']);
		expect(loads).toBe(2);
		expect([...records.keys()]).toEqual(['public/-/-/services']);
	});

	it('applies optimistic updates to observers', async () => {
		const cache = new QueryCache({ toError });
		await cache.fetch(['k'], async () => [1], { staleMs: 60_000 });
		const seen: Array<QuerySnapshot<number[]>> = [];
		cache.observe(['k'], async () => [1], { staleMs: 60_000 }, record(seen));
		cache.set<number[]>(['k'], (list) => [...(list ?? []), 2]);
		expect(seen.at(-1)?.data).toEqual([1, 2]);
	});
});
