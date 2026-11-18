import { describe, expect, it } from 'vitest';
import {
	parseCredits,
	parseGrade,
	parseTarget,
	projectAverage,
	requiredGrade,
	type SimulationBase
} from './simulator.ts';

/** The demo student: 8 graded exams over 64 CFU, averaging 27.125. */
const rollup = { gradedExams: 8, gradeSum: 217, weightedGradeSum: 1736, gradedCredits: 64 };
const arithmetic: SimulationBase = { kind: 'arithmetic', current: 27.125, rollup };
const weighted: SimulationBase = { kind: 'weighted', current: 27.125, rollup };

describe('parseGrade', () => {
	it('accepts 18 to 31, 31 standing for 30 cum laude', () => {
		expect(parseGrade('18')).toEqual({ kind: 'valid', value: 18 });
		expect(parseGrade('31')).toEqual({ kind: 'valid', value: 31 });
	});

	it('tells a failing grade from one out of range, and ignores the rest', () => {
		expect(parseGrade('17')).toEqual({ kind: 'low' });
		expect(parseGrade('32')).toEqual({ kind: 'high' });
		expect(parseGrade('')).toEqual({ kind: 'empty' });
		expect(parseGrade('2a')).toEqual({ kind: 'empty' });
	});
});

describe('parseCredits', () => {
	it('rejects zero credits', () => {
		expect(parseCredits('0')).toEqual({ kind: 'zero' });
		expect(parseCredits('6')).toEqual({ kind: 'valid', value: 6 });
		expect(parseCredits(' ')).toEqual({ kind: 'empty' });
	});
});

describe('projectAverage', () => {
	it('adds one exam to the arithmetic average', () => {
		expect(projectAverage(arithmetic, 30)).toBeCloseTo((27.125 * 8 + 30) / 9, 10);
	});

	it('weighs the exam by its credits, which the weighted average requires', () => {
		expect(projectAverage(weighted, 18, 6)).toBeCloseTo((27.125 * 64 + 18 * 6) / 70, 10);
		expect(projectAverage(weighted, 18)).toBeNull();
		expect(projectAverage(weighted, 18, 0)).toBeNull();
	});

	it('counts the typed 31 as is', () => {
		expect(projectAverage(arithmetic, 31)).toBeCloseTo((27.125 * 8 + 31) / 9, 10);
	});

	it('projects nothing without an average to start from', () => {
		expect(projectAverage({ ...arithmetic, current: null }, 28)).toBeNull();
		expect(projectAverage({ ...arithmetic, rollup: null }, 28)).toBeNull();
	});
});

describe('requiredGrade', () => {
	it('inverts the projection, rounding up to a whole grade', () => {
		// 27.5 · 9 − 27.125 · 8 = 30.5: only 30 cum laude gets there.
		expect(requiredGrade(arithmetic, 27.5)).toEqual({ kind: 'grade', value: 31 });
		expect(requiredGrade(arithmetic, 27.3)).toEqual({ kind: 'grade', value: 29 });
		// (27.4 · 72 − 27.125 · 64) / 8 = 29.6
		expect(requiredGrade(weighted, 27.4, 8)).toEqual({ kind: 'grade', value: 30 });
	});

	it('says when any pass or no grade at all gets there', () => {
		expect(requiredGrade(arithmetic, 26)).toEqual({ kind: 'any' });
		expect(requiredGrade(arithmetic, 28)).toEqual({ kind: 'unreachable' });
	});

	it('gives a projection that reaches the target back', () => {
		const result = requiredGrade(weighted, 27.2, 6);
		expect(result?.kind).toBe('grade');
		if (result?.kind !== 'grade') return;
		expect(projectAverage(weighted, result.value, 6)).toBeGreaterThanOrEqual(27.2);
		expect(projectAverage(weighted, result.value - 1, 6)).toBeLessThan(27.2);
	});

	it('needs credits for the weighted average', () => {
		expect(requiredGrade(weighted, 28)).toBeNull();
	});
});

describe('parseTarget', () => {
	it('reads Italian and English decimals within 18 and 30', () => {
		expect(parseTarget('27,5')).toBe(27.5);
		expect(parseTarget('28.25')).toBe(28.25);
		expect(parseTarget('31')).toBeNull();
		expect(parseTarget('abc')).toBeNull();
	});
});
