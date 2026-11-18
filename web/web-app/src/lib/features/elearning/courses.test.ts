import { describe, expect, it } from 'vitest';
import { CourseCareers, type CareerLike } from './careers.ts';
import { parseCourseCode } from './courseCode.ts';
import {
	filterCourses,
	groupEditions,
	parseFilter,
	filterToken,
	sectionsByPeriod,
	type EnrolledCourse
} from './courses.ts';

function course(id: number, idNumber: string | null, fullName = `Course ${id}`): EnrolledCourse {
	return {
		id,
		shortName: idNumber ?? `S${id}`,
		fullName,
		displayName: fullName,
		idNumber,
		code: parseCourseCode(idNumber),
		progress: null,
		moodleFavourite: false,
		hidden: false,
		order: id,
		lastAccess: null
	};
}

const career = (
	id: number,
	academicYear: number,
	easyStaffProgramCode: string | null = null
): CareerLike => ({
	id,
	academicYear,
	easyStaffProgramCode,
	level: null,
	description: `Career ${id}`
});

const ids = (groups: { editions: EnrolledCourse[] }[]) =>
	groups.map((group) => group.editions.map((edition) => edition.id));

describe('CourseCareers.ownerOf', () => {
	const bachelor = career(1, 2021, 'E3101Q');
	const master = career(2, 2024, 'F1801Q');
	const careers = new CourseCareers([master, bachelor], 2);

	it('owns nothing without a year of study', () => {
		expect(careers.ownerOf(parseCourseCode('CDS-E3101Q-NEWS'))).toBeNull();
		expect(careers.ownerOf(parseCourseCode(null))).toBeNull();
	});

	it('gives every course to the only career', () => {
		expect(new CourseCareers([bachelor], 1).ownerOf(parseCourseCode('2526-2-X1'))).toBe(1);
		expect(new CourseCareers([], 9).ownerOf(parseCourseCode('2526-2-X1'))).toBe(9);
	});

	it('matches the programme code first, ignoring case', () => {
		expect(careers.ownerOf(parseCourseCode('2526-1-e3101q106'))).toBe(1);
		expect(careers.ownerOf(parseCourseCode('2223-1-F1801Q001'))).toBe(2);
	});

	it('else picks the latest career started by the course cohort', () => {
		// 2025/26, year 2: cohort 2024, the master's.
		expect(careers.ownerOf(parseCourseCode('2526-2-Z1'))).toBe(2);
		// 2023/24, year 3: cohort 2021, the bachelor's.
		expect(careers.ownerOf(parseCourseCode('2324-3-Z1'))).toBe(1);
		// Before every career: the earliest.
		expect(careers.ownerOf(parseCourseCode('1920-1-Z1'))).toBe(1);
	});
});

describe('groupEditions', () => {
	it('joins editions by code or by name, transitively, latest first', () => {
		const groups = groupEditions([
			course(10, '2425-2-E3101Q107', 'Basi di Dati'),
			course(11, '2526-2-E3101Q106', 'Algoritmi'),
			course(12, '2526-2-E3101Q107', 'Basi di Dati e Laboratorio'),
			course(13, '2324-2-E3101Q999', 'Basi di Dati'),
			course(14, null, 'Comunicazioni')
		]);
		expect(ids(groups)).toEqual([[12, 10, 13], [11], [14]]);
		expect(groups[0].key).toBe('code:E3101Q107');
		expect(groups[2].key).toBe('id:14');
	});

	it('puts the base course before its streams, and editions without a year last', () => {
		const groups = groupEditions([
			course(1, '2526-2-E3101Q130-T2', 'Reti'),
			course(2, null, 'Reti'),
			course(3, '2526-2-E3101Q130', 'Reti'),
			course(4, '2526-2-E3101Q130-T1', 'Reti')
		]);
		expect(ids(groups)).toEqual([[3, 4, 1, 2]]);
	});

	it('leaves blank names and codes alone', () => {
		expect(ids(groupEditions([course(1, null, ' '), course(2, null, ' ')]))).toEqual([[1], [2]]);
	});
});

describe('filterCourses', () => {
	const bachelor = career(1, 2022, 'E3101Q');
	const master = career(2, 2025, 'F1801Q');
	const careers = new CourseCareers([bachelor, master], 1);
	const courses = [
		course(1, '2526-1-F1801Q001', 'Master course'),
		course(2, '2324-2-E3101Q105', 'Year two'),
		course(3, '2223-1-E3101Q101', 'Year one'),
		course(4, null, 'Notice board'),
		{ ...course(5, '2526-3-E3101Q115', 'Hidden'), hidden: true },
		course(6, '2425-3-E3101Q116', 'Year three')
	];
	const none = () => false;

	it('puts the selected career first by year of study, then other careers, then no career', () => {
		const result = filterCourses(courses, { kind: 'all' }, careers, none);
		expect(ids(result.groups)).toEqual([[3], [2], [6], [1], [4]]);
		expect(result.options.years).toEqual([1, 2, 3]);
		expect(result.options.otherCareers.map((c) => c.id)).toEqual([2]);
	});

	it('keeps the selected career courses of one year, in Moodle order', () => {
		const result = filterCourses(courses, { kind: 'year', year: 1 }, careers, none);
		expect(ids(result.groups)).toEqual([[3]]);
	});

	it('keeps one career courses by year of study', () => {
		const result = filterCourses(courses, { kind: 'career', careerId: 2 }, careers, none);
		expect(ids(result.groups)).toEqual([[1]]);
	});

	it('keeps favourites, from the local choice', () => {
		const result = filterCourses(
			courses,
			{ kind: 'favourites' },
			careers,
			(course) => course.id === 6
		);
		expect(ids(result.groups)).toEqual([[6]]);
	});

	it('falls back to All when the chip is gone', () => {
		expect(filterCourses(courses, { kind: 'year', year: 5 }, careers, none).filter).toEqual({
			kind: 'all'
		});
		expect(filterCourses(courses, { kind: 'career', careerId: 1 }, careers, none).filter.kind).toBe(
			'all'
		);
	});
});

describe('filter tokens', () => {
	it('round-trips every filter', () => {
		for (const token of ['all', 'favourites', 'year:2', 'career:700001']) {
			const filter = parseFilter(token);
			expect(filter && filterToken(filter)).toBe(token);
		}
		expect(parseFilter('year:x')).toBeNull();
		expect(parseFilter(null)).toBeNull();
	});
});

describe('sectionsByPeriod', () => {
	it('splits by the latest edition year, newest first, cross-year spaces last', () => {
		const groups = groupEditions([
			course(1, '2425-1-A'),
			course(2, null, 'Board'),
			course(3, '2526-2-B'),
			course(4, '2526-2-A')
		]);
		const sections = sectionsByPeriod(groups);
		expect(sections.map((section) => section.label)).toEqual(['2025/26', null]);
		expect(sections.map((section) => ids(section.groups))).toEqual([[[4, 1], [3]], [[2]]]);
	});
});
