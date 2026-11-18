import { describe, expect, it } from 'vitest';
import { bookingGrade, gradePoints, outcomeGrade, recordGrade } from './grades.ts';

describe('outcomeGrade', () => {
	it('lets absence and withdrawal win over a grade', () => {
		expect(outcomeGrade({ assenteFlg: 1, votoEsa: 28 })).toEqual({ kind: 'absent' });
		expect(outcomeGrade({ ritiratoFlg: '1', votoEsa: 28 })).toEqual({ kind: 'withdrew' });
	});

	it('reads a positive grade, 31 and the laude flag as 30 cum laude', () => {
		expect(outcomeGrade({ votoEsa: 27, superatoFlg: 1 })).toEqual({
			kind: 'numeric',
			value: 27,
			cumLaude: false
		});
		expect(outcomeGrade({ votoEsa: 31 })).toEqual({ kind: 'numeric', value: 30, cumLaude: true });
		expect(outcomeGrade({ votoEsa: '30', lodeFlg: 1 })).toEqual({
			kind: 'numeric',
			value: 30,
			cumLaude: true
		});
		expect(outcomeGrade({ votoEsa: 28, lodeFlg: 1 })).toEqual({
			kind: 'numeric',
			value: 28,
			cumLaude: false
		});
	});

	it('takes a 0 grade for no grade, leaving the pass flag to decide', () => {
		expect(outcomeGrade({ votoEsa: 0, superatoFlg: 0 })).toEqual({ kind: 'not_passed' });
		expect(outcomeGrade({ votoEsa: null, superatoFlg: 1 })).toEqual({ kind: 'passed' });
		expect(outcomeGrade({})).toEqual({ kind: 'unknown' });
		expect(outcomeGrade(null)).toEqual({ kind: 'unknown' });
	});
});

describe('bookingGrade', () => {
	it('decodes a booking only once its outcome is published', () => {
		const outcome = { votoEsa: 0, superatoFlg: 0 };
		expect(bookingGrade({ publicationId: null, outcome })).toEqual({ kind: 'unknown' });
		expect(bookingGrade({ publicationId: 9, outcome })).toEqual({ kind: 'not_passed' });
	});
});

describe('recordGrade', () => {
	it('reads the grade only under evaluation mode V', () => {
		expect(recordGrade({ evaluationModeCode: 'V', grade: 30, cumLaudeFlag: 1 })).toEqual({
			value: 30,
			cumLaude: true
		});
		expect(recordGrade({ evaluationModeCode: 'G', grade: 30, cumLaudeFlag: null })).toBeNull();
		expect(recordGrade(null)).toBeNull();
	});
});

describe('gradePoints', () => {
	it('counts 30 cum laude as 30 unless told otherwise', () => {
		expect(gradePoints({ value: 30, cumLaude: true })).toBe(30);
		expect(gradePoints({ value: 30, cumLaude: true }, { laude: 31 })).toBe(31);
		expect(gradePoints({ value: 25, cumLaude: false }, { laude: 31 })).toBe(25);
	});
});
