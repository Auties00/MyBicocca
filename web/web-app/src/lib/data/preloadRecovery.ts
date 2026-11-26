const STORAGE_KEY = 'mybicocca:preload-reload-at';
const DEFAULT_COOLDOWN_MS = 30_000;

export interface PreloadRecoveryOptions {
	target?: Pick<Window, 'addEventListener' | 'removeEventListener'>;
	storage?: Pick<Storage, 'getItem' | 'setItem'> | null;
	isOnline?: () => boolean;
	reload?: () => void;
	now?: () => number;
	cooldownMs?: number;
}

/**
 * Recovers open tabs from asset version skew after a deploy or rollback. When a lazy chunk's old
 * hash is no longer served, Vite dispatches `vite:preloadError`; reloading once fetches the fresh
 * shell and asset manifest, while a cooldown prevents loops during real outages.
 */
export function installPreloadRecovery(options: PreloadRecoveryOptions = {}): () => void {
	if (typeof window === 'undefined' && !options.target) return () => undefined;

	const target = options.target ?? window;
	const storage = options.storage !== undefined ? options.storage : safeSessionStorage();
	const isOnline = options.isOnline ?? (() => navigator.onLine !== false);
	const reload = options.reload ?? (() => window.location.reload());
	const now = options.now ?? Date.now;
	const cooldownMs = options.cooldownMs ?? DEFAULT_COOLDOWN_MS;

	const onPreloadError = (event: Event) => {
		if (!isOnline()) return;

		const currentTime = now();
		const lastReload = readTimestamp(storage);
		if (lastReload !== null && currentTime - lastReload < cooldownMs) return;

		writeTimestamp(storage, currentTime);
		event.preventDefault();
		reload();
	};

	target.addEventListener('vite:preloadError', onPreloadError);
	return () => target.removeEventListener('vite:preloadError', onPreloadError);
}

function safeSessionStorage(): Storage | null {
	try {
		return window.sessionStorage;
	} catch {
		return null;
	}
}

function readTimestamp(storage: Pick<Storage, 'getItem'> | null): number | null {
	if (!storage) return null;
	try {
		const raw = storage.getItem(STORAGE_KEY);
		if (raw === null) return null;
		const parsed = Number(raw);
		return Number.isFinite(parsed) ? parsed : null;
	} catch {
		return null;
	}
}

function writeTimestamp(storage: Pick<Storage, 'setItem'> | null, timestamp: number): void {
	if (!storage) return;
	try {
		storage.setItem(STORAGE_KEY, String(timestamp));
	} catch {
		// Private browsing or blocked storage: still allow a single reload attempt.
	}
}
