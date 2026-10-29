import { describe, expect, it } from 'vitest';
import { academicYear, academicYearCode, academicYearLabel } from './academicYear.ts';

describe('academicYear', () => {
	it('rolls over in September, or in the month given', () => {
		expect(academicYear(new Date(2026, 7, 31))).toBe(2025);
		expect(academicYear(new Date(2026, 8, 1))).toBe(2026);
		expect(academicYear(new Date(2026, 7, 1), 8)).toBe(2026);
	});

	it('writes the year as Esse3 and Moodle do', () => {
		expect(academicYearLabel(2026)).toBe('2026/27');
		expect(academicYearLabel(2099)).toBe('2099/00');
		expect(academicYearCode(2026)).toBe('2627');
	});
});
