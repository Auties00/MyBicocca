import { describe, expect, it } from 'vitest';
import type { Deadline } from './deadlines.ts';
import { sectionsOf } from './sections.ts';

const deadline = (days: number): Deadline => ({
	source: 'booking',
	id: `booking:${days}`,
	date: new Date(2026, 9, 27 + days),
	days,
	urgent: days <= 7,
	title: null,
	key: { courseOfStudyId: 1, activityId: 2, callId: 3 },
	code: null,
	callDate: null,
	opensAt: null
});

const shape = (today: Date, days: number[]) =>
	sectionsOf(days.map(deadline), today).map((section) => [
		section.id,
		section.items.map((item) => item.days)
	]);

describe('sectionsOf', () => {
	it('splits by Monday-to-Sunday weeks from a Tuesday', () => {
		expect(shape(new Date(2026, 9, 27), [-2, 0, 5, 6, 12, 13])).toEqual([
			['overdue', [-2]],
			['week', [0, 5]],
			['next_week', [6, 12]],
			['later', [13]]
		]);
	});

	it('ends this week today on a Sunday', () => {
		expect(shape(new Date(2026, 10, 1), [0, 1, 7, 8])).toEqual([
			['week', [0]],
			['next_week', [1, 7]],
			['later', [8]]
		]);
	});
});
