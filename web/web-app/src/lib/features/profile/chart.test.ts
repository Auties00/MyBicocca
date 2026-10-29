import { describe, expect, it } from 'vitest';
import { LocalDate } from '@mybicocca/data-api/runtime';
import { nearestIndex, yearSpans } from './chart.ts';
import type { LibrettoRow, TrendPoint } from './transcript.ts';

function point(date: string): TrendPoint {
	const day = LocalDate.parse(date);
	if (day === undefined) throw new Error('bad date');
	const grade = { value: 28, cumLaude: false };
	return { row: { id: 1 } as LibrettoRow, grade, date: day, value: 28 };
}

describe('yearSpans', () => {
	it('splits the timeline where the academic year turns, in September', () => {
		const spans = yearSpans([
			point('2025-02-10'),
			point('2025-07-18'),
			point('2025-09-09'),
			point('2026-01-20'),
			point('2026-09-15')
		]);
		expect(spans).toEqual([
			{ year: 2024, first: 0, last: 1 },
			{ year: 2025, first: 2, last: 3 },
			{ year: 2026, first: 4, last: 4 }
		]);
	});
});

describe('nearestIndex', () => {
	const xs = [10, 50, 90];

	it('picks the closest point', () => {
		expect(nearestIndex(xs, 62)).toBe(1);
		expect(nearestIndex(xs, -40)).toBe(0);
	});

	it('picks nothing beyond reach', () => {
		expect(nearestIndex(xs, 72, 10)).toBeUndefined();
		expect(nearestIndex(xs, 82, 10)).toBe(2);
	});
});
