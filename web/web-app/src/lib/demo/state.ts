const stores = new Map<string, unknown>();

/**
 * The prefix of the `sessionStorage` keys where fixtures keep what must outlive a reload of the
 * tab, such as `mybicocca:demo-planning`; the demo's sign-out clears them all.
 */
export const DEMO_SESSION_PREFIX = 'mybicocca:demo-';

/**
 * Mutable demo state, built by `init` on first use and kept until the demo account signs out:
 * fixtures mutate it on writes so the UI stays interactive. Name it after the fixture's module.
 */
export function demoState<T>(name: string, init: () => T): T {
	if (!stores.has(name)) stores.set(name, init());
	return stores.get(name) as T;
}

/** Forgets every demo state, in memory and under {@link DEMO_SESSION_PREFIX}. */
export function resetDemoState(): void {
	stores.clear();
	try {
		const keys = Array.from({ length: sessionStorage.length }, (_, index) =>
			sessionStorage.key(index)
		);
		for (const key of keys) {
			if (key?.startsWith(DEMO_SESSION_PREFIX)) sessionStorage.removeItem(key);
		}
	} catch {
		// Without storage there is nothing kept to forget.
	}
}
