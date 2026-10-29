import type {
	MoodleCalendarEvent,
	MoodleCalendarEventsResponse
} from '@mybicocca/data-api/elearning';
import { describe, expect, it } from 'vitest';
import {
	ACTION_EVENTS_PAGE,
	collectActionEvents,
	coursesToResolve,
	deadlineTitle,
	recentDeadlines,
	resolveDeadlines
} from './deadlines.ts';

function event(
	id: number,
	fields: Partial<
		Pick<MoodleCalendarEvent, 'moduleName' | 'instance' | 'name' | 'activityName'>
	> & {
		course?: number | null;
		timeSort?: number;
		timeStart?: number;
	} = {}
): MoodleCalendarEvent {
	const { course = 100, timeSort = id * 10, timeStart = 0, ...rest } = fields;
	return {
		id,
		name: `Event ${id}`,
		activityName: null,
		moduleName: 'assign',
		instance: id + 1000,
		timeSort,
		timeStart,
		course: course === null ? null : { id: course },
		...rest
	} as unknown as MoodleCalendarEvent;
}

const page = (events: MoodleCalendarEvent[]): MoodleCalendarEventsResponse =>
	({ events, firstId: events[0]?.id ?? 0, lastId: events.at(-1)?.id ?? 0 }) as never;

describe('collectActionEvents', () => {
	it('pages by time and cursor until a short page', async () => {
		const all = Array.from({ length: ACTION_EVENTS_PAGE + 3 }, (_, i) => event(i + 1));
		const calls: [number, number | null][] = [];
		const events = await collectActionEvents(async (from, after) => {
			calls.push([from, after]);
			const start = after === null ? 0 : all.findIndex((e) => e.id === after) + 1;
			return page(all.slice(start, start + ACTION_EVENTS_PAGE));
		});
		expect(events).toHaveLength(all.length);
		expect(calls).toEqual([
			[0, null],
			[ACTION_EVENTS_PAGE * 10, ACTION_EVENTS_PAGE]
		]);
	});

	it('stops when Moodle repeats a page it could not continue', async () => {
		const first = Array.from({ length: ACTION_EVENTS_PAGE }, (_, i) => event(i + 1));
		let calls = 0;
		const events = await collectActionEvents(async () => {
			calls++;
			return page(first);
		});
		expect(events).toHaveLength(ACTION_EVENTS_PAGE);
		expect(calls).toBe(2);
	});
});

describe('resolveDeadlines', () => {
	const events = [
		event(3, { moduleName: 'quiz', instance: 30, timeSort: 300, activityName: 'Quiz 1' }),
		event(1, { instance: 10, timeSort: 100, name: 'Compito · Relazione' }),
		event(2, { instance: 20, timeSort: 0, timeStart: 50, course: 200 }),
		event(4, { moduleName: 'forum', instance: 40 }),
		event(5, { course: null }),
		event(6, { instance: 60 })
	];

	it('lists the courses to look up per kind', () => {
		expect(coursesToResolve(events, 'assign')).toEqual([100, 200]);
		expect(coursesToResolve(events, 'quiz')).toEqual([100]);
	});

	it('translates module ids, drops what does not translate, and orders by due time', () => {
		const deadlines = resolveDeadlines(
			events,
			new Map([
				[10, 1],
				[20, 2]
			]),
			new Map([[30, 3]])
		);
		expect(deadlines.map((d) => [d.instanceId, d.kind, d.title, d.dueAt])).toEqual([
			[2, 'assign', 'Event 2', 50_000],
			[1, 'assign', 'Relazione', 100_000],
			[3, 'quiz', 'Quiz 1', 300_000]
		]);
	});
});

describe('deadline helpers', () => {
	it('keeps the deadlines of the last two weeks and later', () => {
		const now = Date.UTC(2026, 9, 27);
		const day = 86_400_000;
		const at = (offset: number) => ({ dueAt: now + offset * day }) as never;
		expect(recentDeadlines([at(-15), at(-13), at(2)], now)).toHaveLength(2);
	});

	it('strips a type prefix from titles', () => {
		expect(deadlineTitle('Quiz · Esercizio 1')).toBe('Esercizio 1');
		expect(deadlineTitle('Esercizio 1')).toBe('Esercizio 1');
	});
});
