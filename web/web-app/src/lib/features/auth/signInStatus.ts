/** How a running sign-in is described: silent at first, then what it is doing, then reassurance. */
export type WaitPhase = 'quick' | 'working' | 'slow';

const WORKING_AFTER_MS = 3_000;
const SLOW_AFTER_MS = 15_000;

export function waitPhase(elapsedMs: number): WaitPhase {
	if (elapsedMs < WORKING_AFTER_MS) return 'quick';
	return elapsedMs < SLOW_AFTER_MS ? 'working' : 'slow';
}
