/** The idle times the lock offers, in minutes; 0 locks whenever the app is left. */
export const LOCK_TIMEOUTS = [0, 1, 5, 10, 15, 30, 60, 240] as const;
export type LockTimeout = (typeof LOCK_TIMEOUTS)[number];

/**
 * Whether an app last in use at `lastActive` must lock at `now`, with `timeout` minutes of grace:
 * with none, as soon as it was left. A tab with no record of use (just opened) always locks.
 */
export function idleExpired(
	lastActive: number | undefined,
	now: number,
	timeout: LockTimeout
): boolean {
	if (lastActive === undefined) return true;
	return timeout === 0 || now - lastActive >= timeout * 60_000;
}

/** Wrong PINs allowed before the lock screen makes you wait. */
export const FREE_ATTEMPTS = 5;

/**
 * How long to wait after `failures` wrong PINs in a row: nothing for the first five, then 30
 * seconds doubling with each further one, up to 15 minutes.
 */
export function lockoutMs(failures: number): number {
	if (failures < FREE_ATTEMPTS) return 0;
	return Math.min(30_000 * 2 ** (failures - FREE_ATTEMPTS), 15 * 60_000);
}
