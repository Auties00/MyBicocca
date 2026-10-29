import { describe, expect, it } from 'vitest';
import { bareCode, parseCourseCode, periodLabel } from './courseCode.ts';

describe('parseCourseCode', () => {
	it('reads the academic year, year of study and code', () => {
		expect(parseCourseCode('2526-2-E1805M002')).toEqual({
			academicYear: 2025,
			courseYear: 2,
			code: 'E1805M002',
			stream: null
		});
	});

	it('splits a trailing lecture stream off the code', () => {
		expect(parseCourseCode('2425-2-E3101Q130-T1')).toMatchObject({
			code: 'E3101Q130',
			stream: 1
		});
		expect(parseCourseCode('2425-2-E3101Q130-T12').stream).toBe(12);
		// A code that is itself "T…" has no base before a dash: no stream.
		expect(parseCourseCode('2425-2-T123')).toMatchObject({ code: 'T123', stream: null });
		expect(parseCourseCode('2425-2-A-TX')).toMatchObject({ code: 'A-TX', stream: null });
	});

	it('keeps dashes inside the code', () => {
		expect(parseCourseCode('2526-1-A-B')).toMatchObject({ code: 'A-B', courseYear: 1 });
	});

	it('only counts a year of study after an academic year, from 1 to 9', () => {
		expect(parseCourseCode('2526-0-X').courseYear).toBeNull();
		expect(parseCourseCode('2526-10-X').courseYear).toBeNull();
		expect(parseCourseCode('2526-x-X').courseYear).toBeNull();
		expect(parseCourseCode('2526-+3-X').courseYear).toBe(3);
		expect(parseCourseCode('ABCD-2-X')).toMatchObject({ academicYear: null, courseYear: null });
	});

	it('parses what the scheme leaves, and nothing from blanks', () => {
		// Notice boards and other spaces keep the part after their second dash as a code.
		expect(parseCourseCode('CDS-E3101Q-NEWS')).toEqual({
			academicYear: null,
			courseYear: null,
			code: 'NEWS',
			stream: null
		});
		expect(parseCourseCode('2526')).toMatchObject({ academicYear: 2025, code: null });
		expect(parseCourseCode('2526-2')).toMatchObject({ courseYear: null, code: null });
		expect(parseCourseCode('  ')).toMatchObject({ academicYear: null, code: null });
		expect(parseCourseCode(null).code).toBeNull();
	});
});

describe('periodLabel', () => {
	it('writes the academic year as Esse3 does, with the stream', () => {
		expect(periodLabel(parseCourseCode('2526-2-X'))).toBe('2025/26');
		expect(periodLabel(parseCourseCode('2425-2-X-T1'))).toBe('2024/25 T1');
		expect(periodLabel(parseCourseCode('0910-1-X'))).toBe('2009/10');
		expect(periodLabel(parseCourseCode('CDS-X-Y'))).toBeNull();
	});
});

describe('bareCode', () => {
	it('drops the edition prefix of catalog short names only', () => {
		expect(bareCode('2627-2-E3101Q106')).toBe('E3101Q106');
		expect(bareCode('CDS-E1805M-DOC')).toBe('CDS-E1805M-DOC');
	});
});
