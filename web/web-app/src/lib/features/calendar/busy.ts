import { durationMinutes, type CalendarEvent } from './model.ts';

/** How full a day is, for the month heat map: none, ≤2 h, ≤4 h, ≤6 h, more. */
export type BusyLevel = 0 | 1 | 2 | 3 | 4;

/**
 * Scheduled hours decide the level, overlaps and cancelled lessons included; a day holding only
 * point-in-time events (a deadline) still counts as light.
 */
export function busyLevel(events: readonly CalendarEvent[]): BusyLevel {
	if (events.length === 0) return 0;
	const hours = events.reduce((sum, event) => sum + durationMinutes(event), 0) / 60;
	if (hours <= 2) return 1;
	if (hours <= 4) return 2;
	if (hours <= 6) return 3;
	return 4;
}
