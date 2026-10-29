import type { CourseSlot } from './types.ts';

/**
 * A stable course colour slot (1–8) for any course key (Moodle id, activity code), so the same
 * course has the same colour wherever it is named by that key (E-learning, a course's pages,
 * Today's deadlines). Use it as `data-course` or as the `course` prop of IconChip, DateBadge,
 * ListRow and TimelineRow. The calendar tells its events apart with its own wider set of hues.
 */
export function courseSlot(key: string | number): CourseSlot {
	let hash = 2166136261;
	for (const char of String(key)) {
		hash ^= char.codePointAt(0) ?? 0;
		hash = Math.imul(hash, 16777619);
	}
	return ((Math.abs(hash) % 8) + 1) as CourseSlot;
}
