import type { DataError } from './errors.ts';

/**
 * Identifies a query: `[scope, account | '-', career | '-', resource, ...params]`, so a prefix
 * selects what to invalidate or wipe (see `keys.ts`).
 */
export type QueryKey = readonly (string | number | boolean)[];

export interface QueryPolicy<T = unknown> {
	/** How long data counts as fresh, with no reload; 0 reloads whenever a page shows it. */
	staleMs: number;
	/** Whether IndexedDB keeps the data, so the next visit renders it at once. */
	persist?: boolean;
	/** Whether it is encrypted there: every personal record. */
	encrypt?: boolean;
	/** How old persisted data may be and still be shown; a week by default. */
	maxAgeMs?: number;
	/**
	 * Whether an answer is suspect, such as an empty list from a flaky upstream: it is shown, but
	 * never persisted and never fresh, so the next look at it asks again.
	 */
	staleWhen?(data: T): boolean;
}

export type QueryStatus = 'pending' | 'success' | 'error';

export interface QuerySnapshot<T> {
	data: T | undefined;
	/** The last load's failure; stale `data` stays alongside it. */
	error: DataError | undefined;
	/** `success` while there is data, even stale; `error` when a load failed with none; else `pending`. */
	status: QueryStatus;
	/** Whether a load is in flight. */
	refreshing: boolean;
	/** When `data` was loaded, in epoch milliseconds. */
	updatedAt: number | undefined;
}

export interface PersistedEntry {
	data: unknown;
	updatedAt: number;
}

/** Where persisted queries live; IndexedDB in the app, memory in tests. */
export interface QueryPersistence {
	read(key: string, encrypted: boolean): Promise<PersistedEntry | undefined>;
	write(key: string, entry: PersistedEntry, encrypted: boolean): Promise<void>;
	remove(prefix: string): Promise<void>;
}

export interface QueryCacheOptions {
	toError: (error: unknown) => DataError;
	persistence?: QueryPersistence;
	now?: () => number;
}

type Listener = (snapshot: QuerySnapshot<unknown>) => void;

interface Entry {
	readonly key: string;
	data: unknown;
	error: DataError | undefined;
	updatedAt: number | undefined;
	/** Set by an invalidation, until a load that started after it ends. */
	invalidated: boolean;
	/** Bumped by every invalidation, so a load that started before one is known to be stale. */
	version: number;
	fetching: Promise<unknown> | undefined;
	hydration: Promise<void> | undefined;
	load: (() => Promise<unknown>) | undefined;
	policy: QueryPolicy;
	readonly listeners: Set<Listener>;
}

const WEEK_MS = 7 * 24 * 60 * 60_000;
const MAX_INVALIDATIONS = 200;
/** How long a load waits for persisted data before it goes to the network anyway. */
const HYDRATION_WAIT_MS = 2_000;
const ignore = () => {};

/** The string form of `key`; one key's string never prefixes another's unless the key does. */
export function keyString(key: QueryKey): string {
	return key.map((part) => encodeURIComponent(String(part))).join('/');
}

function within(key: string, prefix: string): boolean {
	return key === prefix || key.startsWith(`${prefix}/`);
}

/**
 * Stale-while-revalidate query cache: data in memory or IndexedDB is served at once and reloaded
 * when stale; a failed reload keeps the stale data with the error attached; concurrent loads of
 * one key share one request; invalidating a key prefix reloads what is on screen and marks the
 * rest stale.
 */
export class QueryCache {
	readonly #entries = new Map<string, Entry>();
	#invalidations: Array<{ prefix: string; at: number }> = [];
	readonly #options: QueryCacheOptions;

	constructor(options: QueryCacheOptions) {
		this.#options = options;
	}

	/** What `key` holds now, without loading anything. */
	peek<T>(key: QueryKey): QuerySnapshot<T> {
		const entry = this.#entries.get(keyString(key));
		return (entry === undefined ? EMPTY : snapshot(entry)) as QuerySnapshot<T>;
	}

	/**
	 * Calls `listener` with `key`'s snapshot now and on every change, loading it when missing or
	 * stale. Returns the function that stops listening.
	 */
	observe<T>(
		key: QueryKey,
		load: () => Promise<T>,
		policy: QueryPolicy<NoInfer<T>>,
		listener: (snapshot: QuerySnapshot<T>) => void
	): () => void {
		const entry = this.#use(key, load, policy);
		const callback = listener as Listener;
		entry.listeners.add(callback);
		callback(snapshot(entry));
		void this.#revalidate(entry);
		return () => entry.listeners.delete(callback);
	}

	/** `key`'s data, loaded unless fresh: for prefetching and for reading outside components. */
	async fetch<T>(
		key: QueryKey,
		load: () => Promise<T>,
		policy: QueryPolicy<NoInfer<T>>
	): Promise<T> {
		const entry = this.#use(key, load, policy);
		await this.#hydrate(entry);
		if (!this.#isStale(entry)) return entry.data as T;
		return (await this.#load(entry)) as T;
	}

	/** Reloads `key` now, fresh or not, sharing a load already in flight. */
	async refresh(key: QueryKey): Promise<void> {
		const entry = this.#entries.get(keyString(key));
		if (entry?.load !== undefined) await this.#load(entry).catch(ignore);
	}

	/** Marks every query under `prefix` stale, reloading those on screen. */
	invalidate(prefix: QueryKey): void {
		const path = keyString(prefix);
		this.#invalidations = [
			...this.#invalidations.slice(-MAX_INVALIDATIONS),
			{ prefix: path, at: this.#now() }
		];
		for (const entry of this.#entries.values()) {
			if (!within(entry.key, path)) continue;
			entry.version++;
			entry.invalidated = true;
			if (entry.listeners.size > 0) this.#load(entry).catch(ignore);
		}
	}

	/** Replaces `key`'s data, as an optimistic update does; the next load overwrites it. */
	set<T>(key: QueryKey, update: (current: T | undefined) => T): void {
		const path = keyString(key);
		const entry = this.#entries.get(path) ?? this.#create(path, { staleMs: 0 });
		entry.data = update(entry.data as T | undefined);
		entry.updatedAt = this.#now();
		entry.error = undefined;
		this.#emit(entry);
		this.#persist(entry);
	}

	/**
	 * Forgets every query under `prefix`, in memory and in IndexedDB, as signing out does. A
	 * storage that fails to forget never fails the caller: memory is what pages show.
	 */
	async remove(prefix: QueryKey): Promise<void> {
		const path = keyString(prefix);
		for (const entry of [...this.#entries.values()]) {
			if (!within(entry.key, path)) continue;
			this.#entries.delete(entry.key);
			if (entry.listeners.size === 0) continue;
			// Whoever still watches sees the data go, and nothing reloads it for a removed account.
			const replacement = this.#create(entry.key, entry.policy);
			for (const listener of entry.listeners) replacement.listeners.add(listener);
			this.#emit(replacement);
		}
		await this.#options.persistence?.remove(path).catch(ignore);
	}

	/**
	 * `key`'s data as last stored, still within the policy's age, without loading it or keeping
	 * it in memory: for a reader that wants what is there, never a request.
	 */
	async peekStored<T>(key: QueryKey, policy: QueryPolicy<T>): Promise<T | undefined> {
		const persistence = this.#options.persistence;
		if (persistence === undefined || policy.persist !== true) return undefined;
		const record = await persistence
			.read(keyString(key), policy.encrypt === true)
			.catch(() => undefined);
		const maxAge = policy.maxAgeMs ?? WEEK_MS;
		return record && this.#now() - record.updatedAt <= maxAge ? (record.data as T) : undefined;
	}

	/** Reloads the stale queries on screen, as returning to the app or reconnecting should. */
	revalidate(): void {
		for (const entry of this.#entries.values()) {
			if (entry.listeners.size > 0 && entry.fetching === undefined) void this.#revalidate(entry);
		}
	}

	#use(key: QueryKey, load: () => Promise<unknown>, policy: QueryPolicy): Entry {
		const path = keyString(key);
		const entry = this.#entries.get(path) ?? this.#create(path, policy);
		entry.load = load;
		entry.policy = policy;
		return entry;
	}

	#create(key: string, policy: QueryPolicy): Entry {
		const entry: Entry = {
			key,
			data: undefined,
			error: undefined,
			updatedAt: undefined,
			invalidated: false,
			version: 0,
			fetching: undefined,
			hydration: undefined,
			load: undefined,
			policy,
			listeners: new Set()
		};
		this.#entries.set(key, entry);
		return entry;
	}

	async #revalidate(entry: Entry): Promise<void> {
		await this.#hydrate(entry);
		if (entry.listeners.size > 0 && this.#isStale(entry)) await this.#load(entry).catch(ignore);
	}

	/**
	 * Serves persisted data, waiting for it at most {@link HYDRATION_WAIT_MS}: storage that fails
	 * or hangs never fails or stalls a load. Data that arrives later still shows while one runs.
	 */
	#hydrate(entry: Entry): Promise<void> {
		const persistence = this.#options.persistence;
		if (persistence === undefined || entry.policy.persist !== true) return Promise.resolve();
		if (entry.hydration !== undefined) return entry.hydration;
		const read = Promise.resolve()
			.then(() => persistence.read(entry.key, entry.policy.encrypt === true))
			.then((record) => {
				const maxAge = entry.policy.maxAgeMs ?? WEEK_MS;
				if (record === undefined || entry.data !== undefined) return;
				if (this.#now() - record.updatedAt > maxAge || this.#entries.get(entry.key) !== entry)
					return;
				entry.data = record.data;
				entry.updatedAt = record.updatedAt;
				if (this.#invalidatedSince(entry.key, record.updatedAt)) entry.invalidated = true;
				this.#emit(entry);
			})
			.catch(ignore);
		entry.hydration = new Promise<void>((resolve) => {
			const timer = setTimeout(resolve, HYDRATION_WAIT_MS);
			void read.then(() => {
				clearTimeout(timer);
				resolve();
			});
		});
		return entry.hydration;
	}

	#load(entry: Entry): Promise<unknown> {
		if (entry.fetching !== undefined) return entry.fetching;
		const load = entry.load;
		if (load === undefined) return Promise.resolve(entry.data);
		const version = entry.version;
		const current = () => this.#entries.get(entry.key) === entry;
		entry.fetching = (async () => {
			try {
				const data = await load();
				if (current()) {
					entry.data = data;
					entry.error = undefined;
					entry.updatedAt = this.#now();
					if (entry.version === version) entry.invalidated = false;
					this.#persist(entry);
				}
				return data;
			} catch (error) {
				if (current()) entry.error = this.#options.toError(error);
				throw error;
			} finally {
				entry.fetching = undefined;
				if (current()) {
					this.#emit(entry);
					// An invalidation arrived while loading: what was loaded may predate it.
					if (entry.version !== version && entry.listeners.size > 0)
						this.#load(entry).catch(ignore);
				}
			}
		})();
		this.#emit(entry);
		return entry.fetching;
	}

	#isStale(entry: Entry): boolean {
		if (entry.data === undefined || entry.updatedAt === undefined || entry.invalidated) return true;
		if (this.#suspect(entry)) return true;
		return this.#now() - entry.updatedAt > entry.policy.staleMs;
	}

	#suspect(entry: Entry): boolean {
		return entry.data !== undefined && entry.policy.staleWhen?.(entry.data) === true;
	}

	#invalidatedSince(key: string, time: number): boolean {
		return this.#invalidations.some((item) => item.at >= time && within(key, item.prefix));
	}

	#persist(entry: Entry): void {
		const persistence = this.#options.persistence;
		if (
			persistence === undefined ||
			entry.policy.persist !== true ||
			entry.updatedAt === undefined ||
			this.#suspect(entry)
		)
			return;
		persistence
			.write(
				entry.key,
				{ data: entry.data, updatedAt: entry.updatedAt },
				entry.policy.encrypt === true
			)
			.catch(ignore);
	}

	#emit(entry: Entry): void {
		const value = snapshot(entry);
		for (const listener of entry.listeners) listener(value);
	}

	#now(): number {
		return (this.#options.now ?? Date.now)();
	}
}

const EMPTY: QuerySnapshot<unknown> = {
	data: undefined,
	error: undefined,
	status: 'pending',
	refreshing: false,
	updatedAt: undefined
};

function snapshot(entry: Entry): QuerySnapshot<unknown> {
	const status: QueryStatus =
		entry.data !== undefined ? 'success' : entry.error !== undefined ? 'error' : 'pending';
	return {
		data: entry.data,
		error: entry.error,
		status,
		refreshing: entry.fetching !== undefined,
		updatedAt: entry.updatedAt
	};
}
