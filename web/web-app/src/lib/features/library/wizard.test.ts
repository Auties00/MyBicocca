import { describe, expect, it } from 'vitest';
import { choicesBefore, choicesQuery, parseChoices, stepOf } from './wizard.ts';

const parse = (query: string) => parseChoices(new URLSearchParams(query));

describe('booking wizard URL', () => {
	it('round-trips the choices and finds the step', () => {
		const choices = parse('zone=518&day=2026-10-28&duration=120&time=16:30&seat=4823');
		expect(choices).toEqual({
			zone: 518,
			day: '2026-10-28',
			duration: 120,
			start: 990,
			seat: 4823
		});
		expect(stepOf(choices)).toBe('confirm');
		expect(choicesQuery(choices)).toBe(
			'?zone=518&day=2026-10-28&duration=120&time=16%3A30&seat=4823'
		);
	});

	it('drops what depends on a missing or broken earlier choice', () => {
		expect(parse('day=2026-10-28&time=16:30')).toEqual({});
		expect(parse('zone=518&day=2026-13-40&duration=60&time=16:30&seat=1')).toEqual({
			zone: 518,
			duration: 60
		});
		expect(stepOf(parse('zone=518&day=2026-10-28&duration=60'))).toBe('when');
		expect(stepOf(parse('zone=518&day=2026-10-28&duration=60&time=09:30'))).toBe('seat');
	});

	it('goes back to a step keeping only the choices before it', () => {
		const choices = parse('zone=518&day=2026-10-28&duration=120&time=16:30&seat=4823');
		expect(choicesBefore(choices, 'when')).toEqual({ zone: 518, day: '2026-10-28', duration: 120 });
		expect(stepOf(choicesBefore(choices, 'seat'))).toBe('seat');
		expect(choicesBefore(choices, 'zone')).toEqual({});
	});
});
