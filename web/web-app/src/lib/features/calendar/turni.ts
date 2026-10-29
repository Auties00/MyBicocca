import type { CalendarEvent, LessonEvent } from './model.ts';

/** A course whose lessons come in more than one turno. */
export interface SplitCourse {
	activityCode: string;
	/** One of its lessons, to name the course by. */
	lesson: LessonEvent;
	turni: number[];
}

/** The courses among `events` whose lessons come in more than one turno, in order of appearance. */
export function splitCourses(events: readonly CalendarEvent[]): Map<string, SplitCourse> {
	const found = new Map<string, { lesson: LessonEvent; turni: Set<number> }>();
	for (const event of events) {
		if (event.source !== 'lesson' || event.activityCode === null) continue;
		const { turno } = event;
		if (turno === null) continue;
		const entry = found.get(event.activityCode);
		if (entry) entry.turni.add(turno);
		else found.set(event.activityCode, { lesson: event, turni: new Set([turno]) });
	}
	return new Map(
		[...found]
			.filter(([, entry]) => entry.turni.size > 1)
			.map(([activityCode, entry]) => [
				activityCode,
				{ activityCode, lesson: entry.lesson, turni: [...entry.turni].sort((a, b) => a - b) }
			])
	);
}

/**
 * The lesson of `turno` that stands in for `lesson` once the student follows that turno: the same
 * course's lesson on the same day that overlaps it most, if any.
 */
export function counterpart(
	lesson: LessonEvent,
	turno: number,
	events: readonly CalendarEvent[]
): LessonEvent | undefined {
	let best: LessonEvent | undefined;
	let bestOverlap = 0;
	for (const event of events) {
		if (event.source !== 'lesson' || event.date !== lesson.date) continue;
		if (event.activityCode !== lesson.activityCode || event.turno !== turno) continue;
		const overlap = Math.min(event.end, lesson.end) - Math.max(event.start, lesson.start);
		if (overlap > bestOverlap) {
			best = event;
			bestOverlap = overlap;
		}
	}
	return best;
}
