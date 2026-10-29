import { LocalDate, LocalDateTime, LocalTime } from '@mybicocca/data-api/runtime';
import { describe, expect, it } from 'vitest';
import { deserialize, serialize } from './codec.ts';

describe('codec', () => {
	it('restores the SDK date classes, dates and bytes as instances', () => {
		const value = {
			day: new LocalDate(2026, 10, 27),
			at: new LocalDateTime(new LocalDate(2026, 1, 2), 9, 30),
			opens: new LocalTime(8, 0),
			seen: new Date(1_700_000_000_000),
			photo: new Uint8Array([0, 255, 128]),
			list: [new LocalDate(2025, 1, 22), null, 'x']
		};
		const restored = deserialize<typeof value>(serialize(value));
		expect(restored.day).toBeInstanceOf(LocalDate);
		expect(restored.day.equals(value.day)).toBe(true);
		expect(restored.at).toBeInstanceOf(LocalDateTime);
		expect(restored.at.toString()).toBe('2026-01-02T09:30:00');
		expect(restored.opens).toBeInstanceOf(LocalTime);
		expect(restored.seen.getTime()).toBe(1_700_000_000_000);
		expect([...restored.photo]).toEqual([0, 255, 128]);
		expect(restored.list[0]).toBeInstanceOf(LocalDate);
	});

	it('keeps plain objects that use the tag key as they were', () => {
		const value = { '#t': 'LocalDate', '#v': 'not a date', nested: { '#t': 'Bytes' } };
		expect(deserialize(serialize(value))).toEqual(value);
	});

	it('drops undefined fields as JSON does', () => {
		expect(deserialize(serialize({ a: undefined, b: 1 }))).toEqual({ b: 1 });
	});
});
