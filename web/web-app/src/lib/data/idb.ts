/**
 * The app's IndexedDB database, with string keys throughout so a prefix (`acct/{id}/`) selects
 * everything of one account:
 * - `keys`: the vault's non-extractable CryptoKey;
 * - `vault`: encrypted records (accounts, passwords, tokens, cookie jars);
 * - `meta`: small plain values (active account, pending career picks);
 * - `cache`: the query cache's persisted entries.
 *
 * The demo build keeps a database of its own, so it never mixes with a real account's data.
 *
 * When IndexedDB can't be opened (private modes that refuse it, a full disk, a profile path past
 * Windows' 260 characters, an open that never settles), this tab keeps the stores in memory
 * instead: the app works and signs in as usual, and only forgets it all on reload.
 */
export type StoreName = 'keys' | 'vault' | 'meta' | 'cache';

const NAME = __DEMO__ ? 'mybicocca-demo' : 'mybicocca';
const VERSION = 1;
const STORES: readonly StoreName[] = ['keys', 'vault', 'meta', 'cache'];
/** Far beyond a healthy open, which takes milliseconds; past it the boot goes on in memory. */
const OPEN_TIMEOUT_MS = 8_000;

interface Backend {
	get<T>(store: StoreName, key: string): Promise<T | undefined>;
	put(store: StoreName, key: string, value: unknown): Promise<void>;
	delete(store: StoreName, key: string): Promise<void>;
	deletePrefix(store: StoreName, prefix: string): Promise<void>;
	entries<T>(store: StoreName, prefix: string): Promise<Array<[string, T]>>;
}

let opening: Promise<Backend> | undefined;

function backend(): Promise<Backend> {
	opening ??= openDatabase().then(
		(db) => {
			// Another tab upgrading the schema needs this one to let go; the next call opens it again.
			db.onversionchange = () => {
				db.close();
				opening = undefined;
			};
			return idbBackend(db);
		},
		() => memoryBackend()
	);
	return opening;
}

function openDatabase(): Promise<IDBDatabase> {
	return new Promise<IDBDatabase>((resolve, reject) => {
		// Some browsers throw here rather than fail the request, and some have no IndexedDB at all.
		const request = indexedDB.open(NAME, VERSION);
		let late = false;
		const timer = setTimeout(() => {
			late = true;
			reject(new Error('IndexedDB did not open in time'));
		}, OPEN_TIMEOUT_MS);
		request.onupgradeneeded = () => {
			for (const store of STORES) {
				if (!request.result.objectStoreNames.contains(store))
					request.result.createObjectStore(store);
			}
		};
		request.onsuccess = () => {
			clearTimeout(timer);
			// The tab went on in memory: an idle connection would only block other tabs' upgrades.
			if (late) request.result.close();
			else resolve(request.result);
		};
		request.onerror = () => {
			clearTimeout(timer);
			reject(request.error ?? new Error('IndexedDB failed to open'));
		};
	});
}

function idbBackend(db: IDBDatabase): Backend {
	const run = <T>(
		store: StoreName,
		mode: IDBTransactionMode,
		operation: (objects: IDBObjectStore) => IDBRequest<T> | void
	) =>
		new Promise<T>((resolve, reject) => {
			const transaction = db.transaction(store, mode);
			const request = operation(transaction.objectStore(store));
			transaction.oncomplete = () => resolve(request?.result as T);
			transaction.onerror = () => reject(transaction.error);
			transaction.onabort = () => reject(transaction.error);
		});
	const range = (prefix: string) => IDBKeyRange.bound(prefix, `${prefix}￿`);
	return {
		get: (store, key) => run(store, 'readonly', (objects) => objects.get(key)),
		put: async (store, key, value) => {
			await run(store, 'readwrite', (objects) => objects.put(value, key));
		},
		delete: async (store, key) => {
			await run(store, 'readwrite', (objects) => objects.delete(key));
		},
		deletePrefix: async (store, prefix) => {
			await run(store, 'readwrite', (objects) => objects.delete(range(prefix)));
		},
		entries: <T>(store: StoreName, prefix: string) =>
			new Promise<Array<[string, T]>>((resolve, reject) => {
				const entries: Array<[string, T]> = [];
				const transaction = db.transaction(store, 'readonly');
				const request = transaction.objectStore(store).openCursor(range(prefix));
				request.onsuccess = () => {
					const cursor = request.result;
					if (cursor === null) return;
					entries.push([String(cursor.key), cursor.value as T]);
					cursor.continue();
				};
				transaction.oncomplete = () => resolve(entries);
				transaction.onerror = () => reject(transaction.error);
			})
	};
}

function memoryBackend(): Backend {
	const stores = new Map<StoreName, Map<string, unknown>>(
		STORES.map((store) => [store, new Map()])
	);
	const objects = (store: StoreName) => stores.get(store) ?? new Map<string, unknown>();
	const matching = (store: StoreName, prefix: string) =>
		[...objects(store).keys()].filter((key) => key.startsWith(prefix)).sort();
	return {
		get: async <T>(store: StoreName, key: string) => objects(store).get(key) as T | undefined,
		put: async (store, key, value) => {
			objects(store).set(key, value);
		},
		delete: async (store, key) => {
			objects(store).delete(key);
		},
		deletePrefix: async (store, prefix) => {
			for (const key of matching(store, prefix)) objects(store).delete(key);
		},
		entries: async <T>(store: StoreName, prefix: string) =>
			matching(store, prefix).map((key): [string, T] => [key, objects(store).get(key) as T])
	};
}

export async function idbGet<T>(store: StoreName, key: string): Promise<T | undefined> {
	return (await backend()).get<T>(store, key);
}

export async function idbPut(store: StoreName, key: string, value: unknown): Promise<void> {
	await (await backend()).put(store, key, value);
}

export async function idbDelete(store: StoreName, key: string): Promise<void> {
	await (await backend()).delete(store, key);
}

export async function idbDeletePrefix(store: StoreName, prefix: string): Promise<void> {
	await (await backend()).deletePrefix(store, prefix);
}

/** The records whose key starts with `prefix`, by key. */
export async function idbEntries<T>(store: StoreName, prefix: string): Promise<Array<[string, T]>> {
	return (await backend()).entries<T>(store, prefix);
}
