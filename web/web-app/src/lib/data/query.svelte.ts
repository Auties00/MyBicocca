import { untrack } from 'svelte';
import { DataFailure, toDataError, type DataError } from './errors.ts';
import {
	QueryCache,
	type QueryKey,
	type QueryPolicy,
	type QuerySnapshot,
	type QueryStatus
} from './queryCache.ts';
import { idbPersistence } from './queryStore.ts';

export type { QueryKey, QueryPolicy, QueryStatus } from './queryCache.ts';

const MINUTE = 60_000;

/** The usual policies, after the Android caches (data-layer analysis §4.5). */
export const policies = {
	/** Personal data shown from cache and always revalidated: exams, fees, documents (the default). */
	live: { staleMs: 0, persist: true, encrypt: true },
	/** Personal data fresh for a while: calendar, transcript (10), Moodle courses (15), badges (60). */
	fresh: (minutes: number): QueryPolicy => ({
		staleMs: minutes * MINUTE,
		persist: true,
		encrypt: true
	}),
	/** Data identical for everyone: timetables, rooms, library sites. */
	shared: (minutes: number): QueryPolicy => ({ staleMs: minutes * MINUTE, persist: true }),
	/** Kept for this visit only, such as the study plan (30). */
	memory: (minutes: number): QueryPolicy => ({ staleMs: minutes * MINUTE }),
	/** Never cached: payment status, open attendance sessions, free seats and slots. */
	none: { staleMs: 0 }
} satisfies Record<string, QueryPolicy | ((minutes: number) => QueryPolicy)>;

/** The app's query cache; `query`, `mutate` and the helpers below are what components use. */
export const cache = new QueryCache({
	toError: toDataError,
	persistence: typeof indexedDB === 'undefined' ? undefined : idbPersistence
});

if (typeof document !== 'undefined') {
	document.addEventListener('visibilitychange', () => {
		if (document.visibilityState === 'visible') cache.revalidate();
	});
	window.addEventListener('online', () => cache.revalidate());
}

export interface QueryOptions<T> {
	key: QueryKey;
	load: () => Promise<T>;
	/**
	 * {@link policies.live} by default. Add `staleWhen` for answers not to trust, such as
	 * `{ ...policies.shared(60), staleWhen: (list) => list.length === 0 }`.
	 */
	policy?: QueryPolicy<NoInfer<T>>;
}

export interface Query<T> extends QuerySnapshot<T> {
	/** Reloads now, fresh or not. */
	refresh(): Promise<void>;
}

class QueryState<T> implements Query<T> {
	data = $state.raw<T | undefined>();
	error = $state.raw<DataError | undefined>();
	status = $state<QueryStatus>('pending');
	refreshing = $state(false);
	updatedAt = $state<number | undefined>();
	key: QueryKey | undefined;

	apply(snapshot: QuerySnapshot<T>): void {
		this.data = snapshot.data;
		this.error = snapshot.error;
		this.status = snapshot.status;
		this.refreshing = snapshot.refreshing;
		this.updatedAt = snapshot.updatedAt;
	}

	refresh = async (): Promise<void> => {
		if (this.key !== undefined) await cache.refresh(this.key);
	};
}

/**
 * Reactive data for a component: cached data renders at once and reloads when stale. Call it
 * while the component initializes; `options` re-runs when the state it reads changes, and
 * returning nothing pauses the query (signed out, or no career yet).
 *
 * ```ts
 * const booked = query(() => {
 *   const scope = session.current;
 *   return scope && {
 *     key: careerKey(scope, 'exams', 'booked'),
 *     load: () => scope.api.esse3.transcript.getBookingsByMatId(scope.career.enrollmentTraitId, …)
 *   };
 * });
 * // booked.data, booked.error, booked.status, booked.refreshing, booked.updatedAt, booked.refresh()
 * ```
 */
export function query<T>(options: () => QueryOptions<T> | undefined | null | false): Query<T> {
	const state = new QueryState<T>();
	const initial = untrack(options);
	if (initial) state.apply(cache.peek<T>(initial.key));
	$effect(() => {
		const current = options();
		state.key = current ? current.key : undefined;
		if (!current) {
			state.apply({
				data: undefined,
				error: undefined,
				status: 'pending',
				refreshing: false,
				updatedAt: undefined
			});
			return;
		}
		return cache.observe<T>(
			current.key,
			current.load,
			current.policy ?? policies.live,
			(snapshot) => state.apply(snapshot)
		);
	});
	return state;
}

/** Marks the queries under each prefix stale, reloading those on screen. */
export function invalidate(...prefixes: QueryKey[]): void {
	for (const prefix of prefixes) cache.invalidate(prefix);
}

/** Replaces a query's data ahead of the server, as an optimistic update; the next load wins. */
export function setQueryData<T>(key: QueryKey, update: (current: T | undefined) => T): void {
	cache.set(key, update);
}

/** Loads a query ahead of the page that shows it; failures are left for that page to show. */
export function prefetch<T>(options: QueryOptions<T>): Promise<void> {
	return cache
		.fetch(options.key, options.load, options.policy ?? policies.live)
		.then(() => undefined)
		.catch(() => undefined);
}

export interface MutateOptions {
	/** Prefixes to invalidate once the write ends, whether or not it succeeded. */
	invalidate?: readonly QueryKey[];
}

/**
 * Runs a write and invalidates what it changed. Writes are never queued offline: exam bookings
 * and attendance are time-sensitive, so they fail fast.
 *
 * @throws DataFailure Carrying the {@link DataError}.
 */
export async function mutate<T>(run: () => Promise<T>, options: MutateOptions = {}): Promise<T> {
	try {
		return await run();
	} catch (error) {
		throw new DataFailure(toDataError(error), { cause: error });
	} finally {
		for (const prefix of options.invalidate ?? []) cache.invalidate(prefix);
	}
}

export type MutationResult<T> = { ok: true; value: T } | { ok: false; error: DataError };

/** A write a component triggers, with its state: `pending` disables buttons, `error` explains. */
export class Mutation<A extends unknown[], T> {
	pending = $state(false);
	error = $state.raw<DataError | undefined>();

	readonly #run: (...args: A) => Promise<T>;
	readonly #options: MutateOptions | ((...args: A) => MutateOptions);

	constructor(
		run: (...args: A) => Promise<T>,
		options: MutateOptions | ((...args: A) => MutateOptions) = {}
	) {
		this.#run = run;
		this.#options = options;
	}

	run = async (...args: A): Promise<MutationResult<T>> => {
		this.pending = true;
		this.error = undefined;
		const options = typeof this.#options === 'function' ? this.#options(...args) : this.#options;
		try {
			return { ok: true, value: await mutate(() => this.#run(...args), options) };
		} catch (error) {
			this.error = toDataError(error);
			return { ok: false, error: this.error };
		} finally {
			this.pending = false;
		}
	};

	reset(): void {
		this.error = undefined;
	}
}

/**
 * ```ts
 * const book = mutation((call: ExamCall) => esse3.examsCalendar.postExamCallEnrolledList(…), {
 *   invalidate: [careerKey(scope, 'exams'), careerKey(scope, 'calendar')]
 * });
 * const result = await book.run(call); // book.pending, book.error
 * ```
 */
export function mutation<A extends unknown[], T>(
	run: (...args: A) => Promise<T>,
	options?: MutateOptions | ((...args: A) => MutateOptions)
): Mutation<A, T> {
	return new Mutation(run, options);
}
