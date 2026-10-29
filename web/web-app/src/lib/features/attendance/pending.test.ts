import {
	decodeEasyStaffAttendanceRecord,
	decodeEasyStaffStudyProgram
} from '@mybicocca/data-api/easystaff';
import { decodeMoodleEnrolledCourse } from '@mybicocca/data-api/elearning';
import { decodeEsse3StudyPlanActivity, decodeEsse3TranscriptRow } from '@mybicocca/data-api/esse3';
import { describe, expect, it } from 'vitest';
import type { PlanCourse } from '#lib/features/studyplan/plan.ts';
import type { CourseAttendance } from './model.ts';
import {
	classroomOf,
	compareCourses,
	editionOf,
	pendingSeeds,
	plannedCourses,
	type PlannedCourse
} from './pending.ts';

function decoded<T>(value: T | undefined): T {
	if (value === undefined) throw new Error('Fixture does not decode');
	return value;
}

/** 27 October 2026: academic year 2026/27, first semester. */
const AUTUMN = new Date(2026, 9, 27);
/** 20 March 2027: same academic year, second semester. */
const SPRING = new Date(2027, 2, 20);

const program = decoded(
	decodeEasyStaffStudyProgram({
		valore: 'E3101Q',
		label: 'INFORMATICA',
		periodi: [
			{ label: 'Primo Semestre', valore: 'S1', id: 'p1' },
			{ label: 'Secondo Semestre', valore: 'S2', id: 'p2' }
		],
		elenco_anni: [
			{
				label: '1',
				valore: 'GGG|1',
				elenco_insegnamenti: [
					{ label: 'ANALISI MATEMATICA', valore: 'EC1', id_periodo: 'p1', docente: 'Rita Neri' }
				]
			},
			{
				label: '2',
				valore: 'GGG|2',
				elenco_insegnamenti: [
					{
						label: 'BASI DI DATI (T1 FRONTALE TURNO 1)',
						valore: 'EC2',
						id_periodo: 'p1',
						docente: 'Paolo Ferri'
					},
					{
						label: 'BASI DI DATI (T2 FRONTALE TURNO 2)',
						valore: 'EC3',
						id_periodo: 'p1',
						docente: 'Anna Villa'
					},
					{ label: 'RETI', valore: 'E3101Q120', id_periodo: 'p2', docente: '' }
				]
			}
		]
	})
);

const activity = (code: string, name: string, year: number | null, credits = 8) =>
	decoded(
		decodeEsse3StudyPlanActivity({
			sceltaFlg: 1,
			adLibCod: code,
			adLibDes: name,
			annoCorso: year,
			peso: credits
		})
	);

const row = (code: string, name: string, state: 'S' | 'F' | 'P', year: number, attended: number) =>
	decoded(
		decodeEsse3TranscriptRow({
			adCod: code,
			adDes: name,
			stato: state,
			annoCorso: year,
			aaFreqId: attended
		})
	);

const planCourse = (code: string | null, name: string, year: number): PlanCourse => ({
	id: 1,
	code,
	name,
	credits: 8,
	year
});

describe('plannedCourses', () => {
	it('joins the plan by code, then by the name without its variants', () => {
		const planned = plannedCourses(
			[activity('E3101Q107', 'BASI DI DATI', 2), activity('E3101Q120', 'RETI', 2, 6)],
			program,
			2026,
			2025
		);
		expect(planned).toEqual<PlannedCourse[]>([
			{
				name: 'BASI DI DATI',
				normalizedName: 'BASI DI DATI',
				teacher: 'Paolo Ferri',
				semester: 'first',
				year: 2,
				credits: 8
			},
			{
				name: 'RETI',
				normalizedName: 'RETI',
				teacher: null,
				semester: 'second',
				year: 2,
				credits: 6
			}
		]);
	});

	it('without a plan, lists the current year of the programme, clamped to its years', () => {
		expect(plannedCourses([], program, 2026, 2026).map((course) => course.name)).toEqual([
			'ANALISI MATEMATICA'
		]);
		expect(plannedCourses([], program, 2026, 2020).map((course) => course.year)).toEqual([2, 2, 2]);
	});
});

describe('pendingSeeds', () => {
	const planned = plannedCourses(
		[activity('E3101Q107', 'BASI DI DATI', 2), activity('E3101Q120', 'RETI', 2)],
		program,
		2026,
		2025
	);
	const planCourses = [
		planCourse('E3101Q103', 'ANALISI MATEMATICA', 1),
		planCourse('E3101Q104', 'FISICA', 1),
		planCourse('E3101Q107', 'BASI DI DATI', 2),
		planCourse('E3101Q107', 'BASI DI DATI', 2),
		planCourse('E3101Q120', 'RETI', 2),
		planCourse('E3101Q130', 'SICUREZZA', 3)
	];
	const rows = [
		row('E3101Q103', 'ANALISI MATEMATICA', 'S', 1, 2025),
		row('ALTRO', 'FISICA', 'F', 1, 2025),
		row('E3101Q107', 'BASI DI DATI', 'F', 2, 2026)
	];

	it('drops passed courses, later years and next semester, once each', () => {
		const seeds = pendingSeeds({ planCourses, planned, rows, today: AUTUMN });
		expect(seeds.map((seed) => seed.name)).toEqual(['FISICA', 'BASI DI DATI']);
		expect(seeds[1]).toMatchObject({ semester: 'first', teacher: 'Paolo Ferri', year: 2 });
	});

	it('keeps the second semester once it starts', () => {
		const seeds = pendingSeeds({ planCourses, planned, rows, today: SPRING });
		expect(seeds.map((seed) => seed.name)).toEqual(['FISICA', 'BASI DI DATI', 'RETI']);
	});

	it('matches the record book by name when the code differs', () => {
		const passed = [...rows, row('X', ' fisica ', 'S', 1, 2025)];
		const seeds = pendingSeeds({ planCourses, planned, rows: passed, today: AUTUMN });
		expect(seeds.map((seed) => seed.name)).toEqual(['BASI DI DATI']);
	});

	it('falls back to the plan for the current year, and keeps everything when unknown', () => {
		const noRows = pendingSeeds({ planCourses, planned, rows: [], today: AUTUMN });
		expect(noRows.map((seed) => seed.name)).toContain('SICUREZZA');
		const yearless = [planCourse('E3101Q103', 'ANALISI MATEMATICA', 0)];
		const seeds = pendingSeeds({ planCourses: yearless, planned: [], rows: [], today: AUTUMN });
		expect(seeds.map((seed) => seed.name)).toEqual(['ANALISI MATEMATICA']);
	});

	it('takes the programme when the plan has no courses, and its year when the plan has none', () => {
		const fromProgramme = pendingSeeds({ planCourses: [], planned, rows, today: AUTUMN });
		expect(fromProgramme.map((seed) => [seed.name, seed.code])).toEqual([['BASI DI DATI', null]]);
		const yearless = pendingSeeds({
			planCourses: [planCourse('E3101Q120', 'RETI', 0)],
			planned,
			rows,
			today: AUTUMN
		});
		expect(yearless).toEqual([]);
	});
});

describe('classroomOf', () => {
	const record = (code: string, name: string, progress: number, state = 'frequentante') =>
		decoded(
			decodeEasyStaffAttendanceRecord({
				codice: code,
				nome: name,
				Percentuale: 80,
				Frequentate: 20,
				OreFatte: 40,
				percentuale_conseguimento: progress,
				Stato: state
			})
		);

	it('picks the record closest to the requirement, by code in any case or by name', () => {
		const records = [
			record('e3101q107', 'BASI DI DATI', 60, 'in_frequenza'),
			record('OTHER', 'Basi  di dati', 90),
			record('E3101Q108', 'SISTEMI OPERATIVI', 100)
		];
		expect(classroomOf(records, { code: 'E3101Q107', normalizedName: 'BASI DI DATI' })).toEqual({
			percentage: 80,
			lessonsAttended: 20,
			hours: 40,
			requirementProgress: 90,
			status: 'attending'
		});
		expect(classroomOf(records, { code: null, normalizedName: 'RETI' })).toBeNull();
	});

	it('reads any other state as not attending', () => {
		const records = [record('A', 'A', 10, 'sospeso')];
		expect(classroomOf(records, { code: 'A', normalizedName: 'A' })?.status).toBe('not-attending');
	});
});

describe('editionOf', () => {
	const course = (id: number, idnumber: string) =>
		decoded(decodeMoodleEnrolledCourse({ id, idnumber }));
	const enrolled = [
		course(1, '2526-2-E3101Q107'),
		course(2, '2627-2-E3101Q107-T1'),
		course(3, '2627-2-e3101q107'),
		course(4, '2627-2-E3101Q1070'),
		course(5, 'E3101Q107')
	];

	it("prefers this year's plain edition over a turno's", () => {
		expect(editionOf(enrolled, 'E3101Q107', AUTUMN)?.id).toBe(3);
		expect(editionOf(enrolled.slice(0, 2), 'E3101Q107', AUTUMN)?.id).toBe(2);
	});

	it('finds nothing without a code or an edition this year', () => {
		expect(editionOf(enrolled, null, AUTUMN)).toBeUndefined();
		expect(editionOf(enrolled.slice(0, 1), 'E3101Q107', AUTUMN)).toBeUndefined();
	});
});

describe('compareCourses', () => {
	const course = (name: string, year: number, semester: CourseAttendance['semester']) =>
		({ name, year, semester }) as CourseAttendance;

	it('orders by year, semester (unknown last) and name', () => {
		const sorted = [
			course('B', 2, 'unknown'),
			course('C', 2, 'first'),
			course('A', 2, 'second'),
			course('Z', 1, 'second'),
			course('A', 2, 'first')
		].sort(compareCourses);
		expect(sorted.map((item) => `${item.year}${item.semester[0]}${item.name}`)).toEqual([
			'1sZ',
			'2fA',
			'2fC',
			'2sA',
			'2uB'
		]);
	});
});
