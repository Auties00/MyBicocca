import { describe, expect, it } from 'vitest';
import { parseDateQuery } from './dates.ts';

/** Wednesday 28 October 2026, mid-afternoon. */
const today = new Date(2026, 9, 28, 15, 30);

function day(query: string) {
	const found = parseDateQuery(query, today);
	return found && { date: found.date.toDateString(), offset: found.offset };
}

describe('parseDateQuery', () => {
	it('reads relative days in both languages', () => {
		expect(day('Oggi')).toEqual({ date: new Date(2026, 9, 28).toDateString(), offset: 0 });
		expect(day('tomorrow')).toEqual({ date: new Date(2026, 9, 29).toDateString(), offset: 1 });
		expect(day('dopodomani')).toEqual({ date: new Date(2026, 9, 30).toDateString(), offset: 2 });
	});

	it('takes a weekday as its next occurrence, today included, accents optional', () => {
		expect(day('mercoledì')?.date).toBe(new Date(2026, 9, 28).toDateString());
		expect(day('lunedi')?.date).toBe(new Date(2026, 10, 2).toDateString());
		expect(day('Tuesday')?.date).toBe(new Date(2026, 10, 3).toDateString());
	});

	it('reads day and month, moving a past day to next year', () => {
		expect(day('15/11')?.date).toBe(new Date(2026, 10, 15).toDateString());
		expect(day('22/6')?.date).toBe(new Date(2027, 5, 22).toDateString());
		expect(day('28.10')?.date).toBe(new Date(2026, 9, 28).toDateString());
	});

	it('keeps a written year, two digits in the 2000s', () => {
		expect(day('22/6/26')?.date).toBe(new Date(2026, 5, 22).toDateString());
		expect(day('1.2.2027')?.date).toBe(new Date(2027, 1, 1).toDateString());
	});

	it('refuses impossible dates and anything else', () => {
		expect(day('31/2')).toBeNull();
		expect(day('13/13')).toBeNull();
		expect(day('domani mattina')).toBeNull();
		expect(day('aula 12')).toBeNull();
	});
});
