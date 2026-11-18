import { describe, expect, it } from 'vitest';
import { decodeEsse3TranscriptRow, decodeEsse3TranscriptStats } from '@mybicocca/data-api/esse3';
import {
	careerStats,
	gradeRollup,
	groupByYear,
	studyPlanRows,
	toLibrettoRow,
	trendPoints,
	type LibrettoRow
} from './transcript.ts';

let nextId = 1;

/** A record-book row in Esse3's wire format, passed with `grade` on `date` when given. */
function row(
	name: string,
	options: {
		year?: number;
		credits?: number;
		grade?: number | 'idoneo';
		laude?: boolean;
		date?: string;
		state?: 'S' | 'F' | 'P';
		plan?: boolean;
	} = {}
): LibrettoRow {
	const { grade, date } = options;
	const passed = grade !== undefined;
	const decoded = decodeEsse3TranscriptRow({
		adsceId: nextId++,
		adDes: ` ${name} `,
		adCod: `C${nextId}`,
		annoCorso: options.year ?? 1,
		stato: options.state ?? (passed ? 'S' : 'P'),
		peso: options.credits ?? 8,
		pianoId: options.plan === false ? null : 1,
		esito: passed
			? {
					modValCod: grade === 'idoneo' ? 'G' : 'V',
					supEsaFlg: 1,
					voto: grade === 'idoneo' ? null : grade,
					lodeFlg: options.laude ? 1 : 0,
					dataEsa: date ?? null
				}
			: null
	});
	if (decoded === undefined) throw new Error('bad fixture');
	return toLibrettoRow(decoded);
}

describe('toLibrettoRow', () => {
	it('keeps a grade only for activities graded out of thirty', () => {
		expect(row('ANALISI', { grade: 30, laude: true }).grade).toEqual({ value: 30, cumLaude: true });
		expect(row('INGLESE', { grade: 'idoneo' }).grade).toBeNull();
	});

	it('trims names and reads the exam date', () => {
		const passed = row('FISICA', { grade: 25, date: '20/01/2026 00:00:00' });
		expect(passed.name).toBe('FISICA');
		expect(passed.date?.toString()).toBe('2026-01-20');
	});

	it('counts year-less rows as part of the plan even without one', () => {
		expect(row('OFA', { year: 0, plan: false }).inStudyPlan).toBe(true);
		expect(row('EXTRA', { year: 2, plan: false }).inStudyPlan).toBe(false);
	});
});

describe('studyPlanRows', () => {
	it('drops supernumerary rows and puts undated ones last', () => {
		const later = row('B', { grade: 28, date: '10/02/2025' });
		const earlier = row('A', { grade: 27, date: '22/01/2025' });
		const pending = row('C');
		const extra = row('D', { grade: 30, date: '01/01/2025', plan: false });
		expect(studyPlanRows([pending, later, extra, earlier]).map((r) => r.name)).toEqual([
			'A',
			'B',
			'C'
		]);
	});
});

describe('gradeRollup', () => {
	it('sums passed graded rows of the whole career, laude as 30', () => {
		const rollup = gradeRollup([
			row('A', { grade: 28, credits: 8 }),
			row('B', { grade: 30, laude: true, credits: 6 }),
			row('C', { grade: 'idoneo', credits: 3 }),
			row('D', { credits: 8 }),
			row('E', { grade: 24, credits: 6, plan: false })
		]);
		expect(rollup).toEqual({
			gradedExams: 3,
			gradeSum: 82,
			weightedGradeSum: 28 * 8 + 30 * 6 + 24 * 6,
			gradedCredits: 20
		});
	});
});

describe('careerStats', () => {
	const stats = (body: Record<string, unknown>) => {
		const decoded = decodeEsse3TranscriptStats(body);
		if (decoded === undefined) throw new Error('bad fixture');
		return careerStats(decoded);
	};

	it('takes Esse3 averages on base 30 and the weighted one on base 110', () => {
		const result = stats({
			umPesoSuperato: 67.5,
			umPesoMin: 180,
			numAdSuperate: 9,
			numAdPiano: 21,
			numAdPianificate: 0,
			medie: [
				{ definizioneBase: 'CDS', tipoMediaCod: 'P', base: 110, media: 99.46 },
				{ definizioneBase: 'CDSORD', tipoMediaCod: 'P', base: 30, media: 27.13 },
				{ definizioneBase: 'CDSORD', tipoMediaCod: 'A', base: 30, media: 27.2 }
			]
		});
		expect(result).toEqual({
			passedCredits: 67.5,
			totalCredits: 180,
			arithmeticAverage: 27.2,
			weightedAverage: 27.13,
			weightedAverage110: 99.46,
			passedExams: 9,
			plannedExams: 21
		});
	});

	it('falls back to any average of the type, and to the booklet count', () => {
		const result = stats({
			umPesoMax: 120,
			numAdLibretto: 14,
			medie: [{ definizioneBase: 'CDS', tipoMediaCod: 'A', base: 110, media: 100 }]
		});
		expect(result.arithmeticAverage).toBe(100);
		expect(result.weightedAverage).toBeNull();
		expect(result.weightedAverage110).toBeNull();
		expect(result.totalCredits).toBe(120);
		expect(result.plannedExams).toBe(14);
	});
});

describe('trendPoints', () => {
	it('plots passed graded exams by date, laude one step above 30', () => {
		const points = trendPoints([
			row('LATE', { grade: 26, date: '18/07/2025' }),
			row('LAUDE', { grade: 30, laude: true, date: '17/06/2025' }),
			row('NO DATE', { grade: 29 }),
			row('IDONEO', { grade: 'idoneo', date: '09/09/2025' }),
			row('PENDING')
		]);
		expect(points.map((point) => [point.row.name, point.value])).toEqual([
			['LAUDE', 31],
			['LATE', 26]
		]);
	});
});

describe('groupByYear', () => {
	it('orders years, passed first then by name, and adds them up', () => {
		const groups = groupByYear([
			row('RETI', { year: 3, credits: 6 }),
			row('BASI DI DATI', { year: 2 }),
			row('ALGORITMI', { year: 2 }),
			row('FISICA', { year: 2, grade: 25 }),
			row('OFA MATEMATICA', { year: 0, credits: 0, grade: 'idoneo' })
		]);
		expect(groups.map((group) => [group.year, group.rows.map((r) => r.name)])).toEqual([
			[0, ['OFA MATEMATICA']],
			[2, ['FISICA', 'ALGORITMI', 'BASI DI DATI']],
			[3, ['RETI']]
		]);
		expect(groups[1]).toMatchObject({ passed: 1, credits: 24, passedCredits: 8 });
	});

	it('by date, puts the passed in the order they were passed and the rest by name', () => {
		const [group] = groupByYear(
			[
				row('RETI', { state: 'F' }),
				row('ALGEBRA', { grade: 26, date: '26/11/2025' }),
				row('ANALISI', { grade: 24, date: '05/07/2025' }),
				row('INGLESE', { grade: 'idoneo' }),
				row('BASI DI DATI'),
				row('PROGRAMMAZIONE', { grade: 28, date: '02/06/2025' })
			],
			'date'
		);
		expect(group.rows.map((r) => r.name)).toEqual([
			'PROGRAMMAZIONE',
			'ANALISI',
			'ALGEBRA',
			'INGLESE',
			'BASI DI DATI',
			'RETI'
		]);
	});
});
