import { describe, expect, it } from 'vitest';
import {
	cardDensity,
	layoutDay,
	oddHourAlpha,
	openingMinute,
	paddedRange,
	pinchScroll,
	timelineWindow
} from './layout.ts';

const at = (hour: number, minute = 0) => hour * 60 + minute;
const span = (start: number, end: number, id = `${start}-${end}`) => ({ id, start, end });

describe('paddedRange', () => {
	it('keeps blocks of at least 24 minutes as they are', () => {
		expect(paddedRange(span(at(9), at(11)))).toEqual({ start: at(9), end: at(11) });
	});

	it('pads a point in time to 24 minutes', () => {
		expect(paddedRange(span(at(14), at(14)))).toEqual({ start: at(14), end: at(14, 24) });
	});

	it('grows a 23:59 deadline backwards from midnight', () => {
		expect(paddedRange(span(at(23, 59), at(23, 59)))).toEqual({
			start: at(23, 36),
			end: 24 * 60
		});
	});
});

describe('timelineWindow', () => {
	it('defaults to 08:00–22:00', () => {
		expect(timelineWindow([])).toEqual({ start: at(8), end: at(22) });
		expect(timelineWindow([span(at(9), at(18))])).toEqual({ start: at(8), end: at(22) });
	});

	it('grows to whole hours around early and late events', () => {
		expect(timelineWindow([span(at(7, 30), at(9)), span(at(21, 40), at(22, 10))])).toEqual({
			start: at(7),
			end: at(23)
		});
	});

	it('reaches midnight for a 23:59 deadline', () => {
		expect(timelineWindow([span(at(23, 59), at(23, 59))]).end).toBe(24 * 60);
	});
});

describe('layoutDay', () => {
	it('gives a lone event the whole column', () => {
		const [placed] = layoutDay([span(at(9), at(11))]);
		expect(placed).toMatchObject({ lane: 0, x: 0, width: 1 });
	});

	it('splits two overlapping events in halves', () => {
		const placed = layoutDay([span(at(10), at(12), 'b'), span(at(9), at(11), 'a')]);
		expect(placed.map((p) => [p.item.id, p.lane, p.x, p.width])).toEqual([
			['a', 0, 0, 0.5],
			['b', 1, 0.5, 0.5]
		]);
	});

	it('reuses a lane once it is free and sizes each cluster on its own', () => {
		const placed = layoutDay([
			span(at(9), at(10), 'a'),
			span(at(9, 30), at(11), 'b'),
			span(at(10), at(11), 'c'),
			span(at(14), at(15), 'd')
		]);
		const byId = Object.fromEntries(placed.map((p) => [p.item.id, p]));
		expect(byId.a).toMatchObject({ lane: 0, width: 0.5 });
		expect(byId.b).toMatchObject({ lane: 1, width: 0.5 });
		expect(byId.c).toMatchObject({ lane: 0, x: 0, width: 0.5 });
		expect(byId.d).toMatchObject({ lane: 0, width: 1 });
	});

	it('treats back-to-back events as not overlapping', () => {
		const placed = layoutDay([span(at(8, 30), at(10, 30)), span(at(10, 30), at(12, 30))]);
		expect(placed.map((p) => p.width)).toEqual([1, 1]);
	});

	it('keeps a narrow lane for a chain of three overlapping events', () => {
		const placed = layoutDay([
			span(at(9), at(12), 'long'),
			span(at(9), at(10), 'first'),
			span(at(10), at(11), 'second'),
			span(at(10, 30), at(11, 30), 'third')
		]);
		const byId = Object.fromEntries(placed.map((p) => [p.item.id, p]));
		expect(byId.long.lane).toBe(1);
		expect(byId.third.lane).toBe(2);
		expect(placed.every((p) => p.width === 1 / 3)).toBe(true);
	});

	it('places padded deadlines among lessons', () => {
		const placed = layoutDay([span(at(9), at(9), 'due'), span(at(9, 10), at(10), 'lesson')]);
		expect(placed.map((p) => [p.item.id, p.start, p.end, p.width])).toEqual([
			['due', at(9), at(9, 24), 0.5],
			['lesson', at(9, 10), at(10), 0.5]
		]);
	});
});

describe('oddHourAlpha', () => {
	it('hides odd hours on a dense grid and shows them on a roomy one', () => {
		expect(oddHourAlpha(0.55)).toBe(0);
		expect(oddHourAlpha(1)).toBe(1);
		expect(oddHourAlpha(0.725)).toBeCloseTo(0.5);
	});
});

describe('cardDensity', () => {
	/* "Algoritmi" at 14 px, "10:30–12:30" at 11 px, "ASD" and "10:30" at 10 and 9 px. */
	const text = { word: 66, range: 64, label: 20, start: 24 };

	it('picks dot and mini by height first', () => {
		expect(cardDensity(200, 10, text)).toBe('dot');
		expect(cardDensity(200, 20, text)).toBe('mini');
		expect(cardDensity(200, 27, text)).toBe('mini');
	});

	it('prints the title where its longest word and the time range fit whole', () => {
		expect(cardDensity(200, 30, text)).toBe('compact');
		expect(cardDensity(200, 118, text)).toBe('full');
		expect(cardDensity(89, 118, text)).toBe('full');
	});

	it('sets the title smaller where only the smaller type fits', () => {
		const long = { ...text, word: 105 };
		expect(cardDensity(120, 118, long)).toBe('tight');
		expect(cardDensity(120, 30, long)).toBe('compact');
		expect(cardDensity(128, 118, long)).toBe('full');
	});

	it('falls back to the short label and start time where a word would be cut', () => {
		expect(cardDensity(66, 118, text)).toBe('icon');
		expect(cardDensity(66, 30, text)).toBe('icon');
		expect(cardDensity(77, 118, text)).toBe('icon');
		expect(cardDensity(75, 118, { ...text, word: 66, range: 64 })).toBe('icon');
	});

	it("keeps Android's 50 px floor for titles", () => {
		expect(cardDensity(48, 120, { word: 20, range: 24, label: 20, start: 24 })).toBe('icon');
	});

	it('drops the time, then the label, as a lane narrows', () => {
		expect(cardDensity(33, 120, text)).toBe('icon');
		expect(cardDensity(30, 120, text)).toBe('label');
		expect(cardDensity(26, 120, text)).toBe('dot');
	});
});

describe('pinchScroll', () => {
	it('keeps the focal point still and stays in bounds', () => {
		expect(pinchScroll(100, 200, 2, 10_000)).toBe(400);
		expect(pinchScroll(0, 50, 0.5, 10_000)).toBe(0);
		expect(pinchScroll(900, 100, 2, 1_000)).toBe(1_000);
	});
});

describe('openingMinute', () => {
	const window = { start: at(8), end: at(24) };
	const week = [span(at(8, 30), at(10, 30)), span(at(11, 30), at(13, 30)), span(at(16), at(18))];
	const today = (now: number, events: ReturnType<typeof span>[] = week) => ({ now, events });

	it('opens an hour before now while today has more to come', () => {
		expect(openingMinute({ window, events: week, today: today(at(12)), visible: at(4) })).toBe(
			at(11)
		);
		expect(openingMinute({ window, events: week, today: today(at(12, 20)), visible: at(4) })).toBe(
			at(11)
		);
	});

	it('opens at the start of what is under way when that is earlier', () => {
		const events = [span(at(13), at(17))];
		const opening = { window, events, today: today(at(14), events), visible: at(4) };
		expect(openingMinute(opening)).toBe(at(12, 50));
	});

	it('keeps now on screen under a long block', () => {
		const events = [span(at(9), at(22))];
		const opening = { window, events, today: today(at(21), events), visible: at(4) };
		expect(openingMinute(opening)).toBe(at(18));
	});

	it("ignores other days' blocks when anchoring on now", () => {
		const mine = [span(at(20), at(22, 30))];
		const events = [...mine, span(at(9), at(13, 30)), span(at(10, 30), at(12, 30))];
		const opening = { window, events, today: today(at(21), mine), visible: at(5) };
		expect(openingMinute(opening)).toBe(at(19));
	});

	it('lands a cut on a whole hour', () => {
		const mine = [span(at(16), at(18))];
		const events = [...mine, span(at(15, 30), at(17, 30))];
		const opening = { window, events, today: today(at(17, 10), mine), visible: at(6) };
		expect(openingMinute(opening)).toBe(at(15));
	});

	it('takes the next hour when the one before would push now off screen', () => {
		const mine = [span(at(9), at(22))];
		const events = [...mine, span(at(17), at(19))];
		const opening = { window, events, today: today(at(21, 40), mine), visible: at(4, 30) };
		expect(openingMinute(opening)).toBe(at(18));
	});

	it('opens before the first event when today is over or not on screen', () => {
		const done = [span(at(9), at(11)), span(at(14), at(15))];
		expect(
			openingMinute({ window, events: done, today: today(at(18), done), visible: at(3) })
		).toBe(at(8, 30));
		expect(openingMinute({ window, events: done, today: null, visible: at(3) })).toBe(at(8, 30));
	});

	it('moves up so the screen ends with the last block, not empty hours', () => {
		const late = [span(at(14), at(16)), span(at(19), at(21))];
		expect(openingMinute({ window, events: late, today: null, visible: at(6) })).toBe(at(13, 30));
		expect(openingMinute({ window, events: late, today: null, visible: at(9) })).toBe(at(12, 30));
	});

	it('lets a 23:59 deadline wait below the fold', () => {
		const events = [...week, span(at(23, 59), at(23, 59))];
		expect(openingMinute({ window, events, today: null, visible: at(8) })).toBe(at(8));
	});

	it('stays inside the window', () => {
		expect(openingMinute({ window, events: week, today: today(at(9)), visible: at(6) })).toBe(
			at(8)
		);
		expect(openingMinute({ window, events: [], today: today(at(23), []), visible: at(6) })).toBe(
			at(18)
		);
		expect(openingMinute({ window, events: [], today: null, visible: at(20) })).toBe(at(8));
	});
});
