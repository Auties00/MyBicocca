import { decodeEsse3AnnualEnrollment } from '@mybicocca/data-api/esse3';
import { describe, expect, it } from 'vitest';
import { renewalState, toEnrollment, toEnrollments, type AnnualEnrollment } from './enrollments.ts';

/** A year as `/carriere/{stuId}/iscrizioni` sends it, regular and active unless overridden. */
function row(fields: Record<string, unknown> = {}) {
	const value = decodeEsse3AnnualEnrollment({
		aaIscrId: 2026,
		annoCorso: 2,
		tipoIscrCod: 'IC',
		tipoIscrDes: 'In corso',
		staIscrCod: 'A',
		...fields
	});
	if (value === undefined) throw new Error('Undecodable enrolment');
	return value;
}

function year(fields: Record<string, unknown> = {}): AnnualEnrollment {
	const value = toEnrollment(row(fields));
	if (value === null) throw new Error('No academic year');
	return value;
}

describe('toEnrollment', () => {
	it('drops a row without its academic year', () => {
		expect(toEnrollment(row({ aaIscrId: null }))).toBeNull();
	});

	it('reads the type and status codes, keeping an unknown type’s own name', () => {
		expect(year({ tipoIscrCod: 'FC', anniFc: 2 })).toMatchObject({
			type: 'out_of_course',
			outOfCourseYears: 2
		});
		expect(year({ staIscrCod: 'X' }).status).toBe('canceled');
		expect(year({ staIscrCod: 'S' }).status).toBe('suspended');
		expect(year({ tipoIscrCod: 'ZZ', tipoIscrDes: 'Singoli corsi' })).toMatchObject({
			type: 'unknown',
			typeDescription: 'Singoli corsi'
		});
	});

	it('reads part-time, suspension and graduation only when their flags are set', () => {
		const regular = year({ ptCfu: 30, motSospCod: 'MAT' });
		expect(regular.partTime).toBeNull();
		expect(regular.suspension).toBeNull();

		const partTime = year({ ptFlg: 1, ptCfu: 30, ptCfuExtra: 0, ptBloccatoFlg: 1 });
		expect(partTime.partTime).toEqual({ credits: 30, extraCredits: null, locked: true });

		expect(year({ sospFlg: 1, motSospCod: ' ' }).suspension).toEqual({ reasonCode: null });
		expect(year({ attlauFlg: 1, dataAttlau: '15/03/2027 00:00:00' })).toMatchObject({
			awaitingDegree: true,
			degreeAwardDate: { year: 2027, month: 3, day: 15 }
		});
	});

	it('hides the shared path, a "no exemption" exemption, and blank text', () => {
		const value = year({
			indirizzoDes: 'Percorso Comune',
			tipoEsoCod: 'N',
			tipoEsoDes: 'Nessun esonero',
			notaIscr: '  '
		});
		expect(value.path).toBeNull();
		expect(value.exemptionDescription).toBeNull();
		expect(value.enrollmentNote).toBeNull();
		expect(year({ indirizzoDes: 'Data science' }).path).toBe('Data science');
	});

	it('reads the credits for the degree from their decimal string', () => {
		expect(year({ valoreMin: '180.0' }).minimumCredits).toBe(180);
		expect(year({ valoreMin: 'n/d' }).minimumCredits).toBeNull();
	});

	it('prefers the enrolment class code, then the ministry one', () => {
		expect(year({ codiceClasseIscrizione: 'L-31', claMurstCod: 'X' }).degreeClassCode).toBe('L-31');
		expect(year({ claMurstCod: 'L-31' }).degreeClassCode).toBe('L-31');
	});
});

describe('toEnrollments', () => {
	it('sorts the years newest first', () => {
		const years = toEnrollments([
			row({ aaIscrId: 2024 }),
			row({ aaIscrId: 2026 }),
			row({ aaIscrId: 2025 })
		]);
		expect(years.map((item) => item.academicYear)).toEqual([2026, 2025, 2024]);
	});

	it('keeps one enrolment per year, the active one when a year was enrolled again', () => {
		const years = toEnrollments([
			row({ aaIscrId: 2026, staIscrCod: 'X', annoCorso: 1 }),
			row({ aaIscrId: 2026, staIscrCod: 'A', annoCorso: 2 }),
			row({ aaIscrId: 2026, staIscrCod: 'A', annoCorso: 3 })
		]);
		expect(years).toHaveLength(1);
		expect(years[0]).toMatchObject({ status: 'active', courseYear: 2 });
	});
});

describe('renewalState', () => {
	const july = new Date(2026, 6, 31);
	const august = new Date(2026, 7, 1);

	it('turns the academic year in August', () => {
		const years = toEnrollments([row({ aaIscrId: 2025 })]);
		expect(renewalState(years, july)).toEqual({ kind: 'enrolled', academicYear: 2025 });
		expect(renewalState(years, august)).toEqual({ kind: 'renewable', academicYear: 2026 });
	});

	it('needs an active enrolment for the current year', () => {
		const cancelled = toEnrollments([row({ aaIscrId: 2026, staIscrCod: 'X' })]);
		expect(renewalState(cancelled, august).kind).toBe('renewable');
	});

	it('offers the renewal to a career with no year yet', () => {
		expect(renewalState([], august)).toEqual({ kind: 'renewable', academicYear: 2026 });
	});

	it('offers nothing while graduating or once the career is over', () => {
		const graduating = toEnrollments([row({ aaIscrId: 2025, attlauFlg: 1 })]);
		expect(renewalState(graduating, august).kind).toBe('none');
		const years = toEnrollments([row({ aaIscrId: 2025 })]);
		expect(renewalState(years, august, false).kind).toBe('none');
	});
});
