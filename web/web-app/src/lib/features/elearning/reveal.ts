/** How long after an enrolment the course list still brings the new course into view. */
const WINDOW_MS = 10 * 60_000;

let pending: { courseId: number; until: number } | undefined;

/**
 * Asks the course list to bring a course just enrolled in into view the next time it lists it,
 * however the reader gets back there (Back, the toast, the header), as Android does on return.
 */
export function revealLater(courseId: number): void {
	pending = { courseId, until: Date.now() + WINDOW_MS };
}

/** The course waiting to be brought into view, while there is still time. */
export function pendingReveal(now = Date.now()): number | undefined {
	if (pending && pending.until < now) pending = undefined;
	return pending?.courseId;
}

export function revealDone(): void {
	pending = undefined;
}
