import { academicYear } from '#lib/data/academicYear.ts';
import { toDate } from '#lib/format/dates.ts';
import type { TrendPoint } from './transcript.ts';

/** The grade axis: 18 at the bottom, 31 (30 cum laude) at the top. */
export const GRADE_MIN = 18;
export const GRADE_MAX = 31;

/**
 * The academic year an exam's date falls in. Not the record's own year: late sessions count for
 * the year before, which would break the runs of a timeline sorted by date.
 */
export function examYear(point: TrendPoint): number {
	return academicYear(toDate(point.date));
}

/** A run of consecutive points passed in the same academic year. */
export interface YearSpan {
	year: number;
	first: number;
	last: number;
}

/** The points, sorted by date, grouped into runs by academic year. */
export function yearSpans(points: readonly TrendPoint[]): YearSpan[] {
	const spans: YearSpan[] = [];
	points.forEach((point, index) => {
		const year = examYear(point);
		const current = spans.at(-1);
		if (current?.year === year) current.last = index;
		else spans.push({ year, first: index, last: index });
	});
	return spans;
}

/** The index of the x closest to `x`, if within `reach` pixels. */
export function nearestIndex(
	xs: readonly number[],
	x: number,
	reach = Infinity
): number | undefined {
	let best: number | undefined;
	let distance = reach;
	xs.forEach((candidate, index) => {
		const d = Math.abs(candidate - x);
		if (d <= distance) {
			best = index;
			distance = d;
		}
	});
	return best;
}
