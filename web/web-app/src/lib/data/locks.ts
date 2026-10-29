const flights = new Map<string, Promise<unknown>>();

/**
 * Runs `run` for `name` once at a time: callers in this tab share the run in flight, and other
 * tabs wait for it (Web Locks), so two tabs never log in or burn Moodle's six-minute autologin
 * window at once. A run must re-read the vault first: another tab may have renewed meanwhile.
 */
export function singleFlight<T>(name: string, run: () => Promise<T>): Promise<T> {
	const existing = flights.get(name);
	if (existing !== undefined) return existing as Promise<T>;
	const flight = withLock(name, run).finally(() => flights.delete(name));
	flights.set(name, flight);
	return flight;
}

/** Runs `run` holding the Web Lock `name`, across this browser's tabs. */
export function withLock<T>(name: string, run: () => Promise<T>): Promise<T> {
	if (typeof navigator === 'undefined' || navigator.locks === undefined) return run();
	return navigator.locks.request(`mybicocca:${name}`, run) as Promise<T>;
}
