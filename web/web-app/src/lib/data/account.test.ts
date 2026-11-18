import { describe, expect, it } from 'vitest';
import {
	careerLevel,
	careerStatus,
	defaultCareerId,
	needsCareerPick,
	normalizeUsername,
	reconcileCareers,
	type Career,
	type CareerStatus
} from './account.ts';

function career(id: number, status: CareerStatus, academicYear: number): Career {
	return {
		id,
		enrollmentTraitId: id + 1,
		programId: 1,
		easyStaffProgramCode: null,
		academicYearEnrollmentId: 1,
		studentNumber: String(id),
		description: '',
		academicYear,
		status,
		level: null
	};
}

describe('normalizeUsername', () => {
	it('appends the campus domain to a short username only', () => {
		expect(normalizeUsername(' m.rossi99 ')).toBe('m.rossi99@campus.unimib.it');
		expect(normalizeUsername('mario.rossi@unimib.it')).toBe('mario.rossi@unimib.it');
	});
});

describe('careerStatus and careerLevel', () => {
	it('decode Esse3 codes', () => {
		expect(careerStatus('A')).toBe('active');
		expect(careerStatus('i')).toBe('provisional');
		expect(careerStatus('LAU')).toBe('graduated');
		expect(careerStatus('TRA')).toBe('interrupted');
		expect(careerStatus(null)).toBe('other');
		expect(careerLevel(' lm5 ')).toBe('master');
		expect(careerLevel('L2')).toBe('bachelor');
		expect(careerLevel('D')).toBeNull();
	});
});

describe('defaultCareerId', () => {
	it('prefers the open career started most recently', () => {
		expect(
			defaultCareerId([
				career(1, 'active', 2021),
				career(2, 'graduated', 2024),
				career(3, 'suspended', 2023)
			])
		).toBe(3);
	});

	it('falls back to the latest career when none is open', () => {
		expect(defaultCareerId([career(1, 'graduated', 2019), career(2, 'interrupted', 2022)])).toBe(2);
	});
});

describe('needsCareerPick', () => {
	it('asks only when more than one career is open', () => {
		expect(needsCareerPick([career(1, 'active', 2024), career(2, 'graduated', 2020)])).toBe(false);
		expect(needsCareerPick([career(1, 'active', 2024), career(2, 'provisional', 2025)])).toBe(true);
	});
});

describe('reconcileCareers', () => {
	it('reports new open careers and a selected career that ended or vanished', () => {
		const before = [career(1, 'active', 2022)];
		expect(
			reconcileCareers(
				'a',
				before,
				[career(1, 'graduated', 2022), career(2, 'active', 2025), career(3, 'graduated', 2019)],
				1
			)
		).toEqual([
			{ type: 'new_career', accountId: 'a', career: career(2, 'active', 2025) },
			{ type: 'selected_career_ended', accountId: 'a', career: career(1, 'graduated', 2022) }
		]);
		expect(reconcileCareers('a', before, [career(2, 'active', 2025)], 1)).toContainEqual({
			type: 'selected_career_missing',
			accountId: 'a'
		});
	});
});
