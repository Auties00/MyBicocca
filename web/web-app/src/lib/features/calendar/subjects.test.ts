import type { EasyStaffStudyProgram } from '@mybicocca/data-api/easystaff';
import type { Esse3StudyPlanActivity, Esse3StudyPlanHeader } from '@mybicocca/data-api/esse3';
import { describe, expect, it } from 'vitest';
import { pickPlanHeader, plannedSubjects } from './subjects.ts';

const subject = (code: string, name: string) => ({
	id: code,
	code,
	name,
	teacherName: '',
	periodId: ''
});

const program: EasyStaffStudyProgram = {
	code: 'E3101Q',
	name: 'INFORMATICA',
	degreeType: '',
	internalId: '',
	teachingAreaCode: '',
	displayMode: '',
	teachingPeriods: [],
	years: [
		{
			code: 'E3101Q|1',
			name: '1',
			trackName: '',
			subjects: [subject('ES101', 'Programmazione 1')]
		},
		{
			code: 'E3101Q|2',
			name: '2',
			trackName: '',
			subjects: [subject('ES106', 'Algoritmi e strutture dati'), subject('ES107', 'Basi di dati')]
		},
		{ code: 'E3101Q|3', name: '3', trackName: '', subjects: [subject('ES115', 'Reti')] }
	]
};

const activity = (code: string | null, name: string | null, courseYear: number | null = null) =>
	({
		activityTranscriptCode: code,
		activityTranscriptDescription: name,
		courseYear
	}) as Esse3StudyPlanActivity;

describe('plannedSubjects', () => {
	it('joins the plan to the program by code, else by name', () => {
		const planned = plannedSubjects(
			[
				activity('ES106', 'ALGORITMI E STRUTTURE DATI'),
				activity('E3101Q107', '  basi di  DATI '),
				activity('E3101Q999', 'Tirocinio')
			],
			program,
			2026,
			2025,
			new Set()
		);
		expect(planned).toEqual([
			{
				subjectCode: 'ES106',
				activityCode: 'ES106',
				normalizedName: 'ALGORITMI E STRUTTURE DATI'
			},
			{ subjectCode: 'ES107', activityCode: 'E3101Q107', normalizedName: 'BASI DI DATI' }
		]);
	});

	it('leaves out courses already passed', () => {
		const planned = plannedSubjects(
			[activity('ES106', 'Algoritmi e strutture dati'), activity('ES107', 'Basi di dati')],
			program,
			2026,
			2025,
			new Set(['BASI DI DATI'])
		);
		expect(planned.map((p) => p.subjectCode)).toEqual(['ES106']);
	});

	it('leaves out the courses of later years', () => {
		const planned = plannedSubjects(
			[activity('ES107', 'Basi di dati', 2), activity('ES115', 'Reti', 3)],
			program,
			2026,
			2025,
			new Set()
		);
		expect(planned.map((p) => p.subjectCode)).toEqual(['ES107']);
	});

	it('without a plan, takes the current year of the program, clamped to its years', () => {
		const second = plannedSubjects([], program, 2026, 2025, new Set());
		expect(second.map((p) => [p.subjectCode, p.activityCode])).toEqual([
			['ES106', null],
			['ES107', null]
		]);
		expect(plannedSubjects([], program, 2030, 2025, new Set()).map((p) => p.subjectCode)).toEqual([
			'ES115'
		]);
	});
});

describe('plannedSubjects with EasyStaff variants', () => {
	const variants: EasyStaffStudyProgram = {
		...program,
		years: [
			{
				code: 'GGG T1|3',
				name: '3',
				trackName: '',
				subjects: [
					subject('EC515869', 'ANALISI E PROGETTO DI ALGORITMI (T1 LEZ + ESE + LAB)'),
					subject('EC516079', 'RICERCA OPERATIVA (T1 FRONTALE TURNO 1)'),
					subject('EC516080', 'RICERCA OPERATIVA (T1GEA ESER TURNO 1 + T1GLA LAB TURNO 1)'),
					subject('EC516003-GR2', 'INGEGNERIA DEL SOFTWARE - LAB')
				]
			},
			{
				code: 'GGG T2|3',
				name: '3',
				trackName: '',
				subjects: [
					subject('EC515872', 'ANALISI E PROGETTO DI ALGORITMI (T2 LEZ + ESE + LAB)'),
					subject('EC516082', 'RICERCA OPERATIVA (T2 FRONTALE TURNO 2)')
				]
			}
		]
	};

	it('joins an activity to every variant of its name', () => {
		const planned = plannedSubjects(
			[
				activity('E3101Q113', 'ANALISI E PROGETTO DI ALGORITMI'),
				activity('E3101Q115', 'INGEGNERIA DEL SOFTWARE')
			],
			variants,
			2026,
			2023,
			new Set()
		);
		expect(planned.map((p) => [p.subjectCode, p.activityCode])).toEqual([
			['EC515869', 'E3101Q113'],
			['EC515872', 'E3101Q113'],
			['EC516003-GR2', 'E3101Q115']
		]);
	});

	it('keeps only the turno the e-learning enrolment names', () => {
		const planned = plannedSubjects(
			[activity('E3101Q128', 'RICERCA OPERATIVA')],
			variants,
			2026,
			2023,
			new Set(),
			new Map([['E3101Q128', 1]])
		);
		expect(planned.map((p) => p.subjectCode)).toEqual(['EC516079', 'EC516080']);
	});
});

describe('pickPlanHeader', () => {
	const header = (planId: number, state: string) => ({ planId, state }) as Esse3StudyPlanHeader;

	it('prefers the approved plan, else the latest', () => {
		expect(pickPlanHeader([header(1, 'A'), header(2, 'P')])?.planId).toBe(1);
		expect(pickPlanHeader([header(1, 'P'), header(3, 'B'), header(2, 'P')])?.planId).toBe(3);
		expect(pickPlanHeader([])).toBeUndefined();
	});
});
