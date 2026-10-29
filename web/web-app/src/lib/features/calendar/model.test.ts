import { describe, expect, it } from 'vitest';
import { mergeEvents, nextBySource, type CalendarEvent } from './model.ts';

const event = (id: string, date: string, start: number, source = 'lesson') =>
	({ id, date, start, end: start + 60, source, cancelled: false }) as CalendarEvent;

describe('mergeEvents', () => {
	it('keeps one copy of an event two month slices both hold, sorted by day and time', () => {
		const october = [event('lesson_1', '2026-10-30', 600), event('lesson_2', '2026-10-31', 540)];
		const stale = [event('lesson_2', '2026-10-31', 540), event('lesson_1', '2026-10-30', 600)];
		const november = [event('lesson_3', '2026-11-02', 510)];
		const merged = mergeEvents(november, october, stale, [event('exam_1', '2026-10-30', 540)]);
		expect(merged.map((item) => item.id)).toEqual(['exam_1', 'lesson_1', 'lesson_2', 'lesson_3']);
	});

	it('keeps the first copy of an id', () => {
		const fresh = { ...event('lesson_1', '2026-10-30', 600), title: 'fresh' };
		const old = { ...event('lesson_1', '2026-10-30', 600), title: 'old' };
		expect(mergeEvents([fresh], [old])[0].title).toBe('fresh');
	});
});

describe('nextBySource', () => {
	it('finds the next event of each source still to start', () => {
		const sorted = [
			event('exam_1', '2026-10-27', 540, 'exam'),
			event('deadline_1', '2026-10-28', 1439, 'deadline'),
			event('exam_2', '2026-11-12', 540, 'exam')
		];
		const next = nextBySource(sorted, ['exam', 'deadline', 'library'], '2026-10-27', 600);
		expect(next.map((item) => item.id)).toEqual(['exam_2', 'deadline_1']);
	});
});
