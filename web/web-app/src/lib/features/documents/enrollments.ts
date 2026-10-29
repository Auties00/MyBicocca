import type { Esse3AnnualEnrollment } from '@mybicocca/data-api/esse3';
import type { LocalDate } from '@mybicocca/data-api/runtime';
import { academicYear } from '#lib/data/academicYear.ts';
import { esse3Date } from '#lib/data/esse3/dates.ts';

export type EnrollmentType = 'in_progress' | 'out_of_course' | 'repeating' | 'unknown';
export type EnrollmentStatus = 'active' | 'canceled' | 'suspended' | 'unknown';

export interface PartTime {
	credits: number | null;
	extraCredits: number | null;
	locked: boolean;
}

/** One academic year of the career, as `/carriere/{stuId}/iscrizioni` records it. */
export interface AnnualEnrollment {
	/** The September it starts: 2026 for 2026/27. It identifies the year in URLs. */
	academicYear: number;
	/** 0 when Esse3 doesn't say. */
	courseYear: number;
	outOfCourseYears: number;
	type: EnrollmentType;
	typeDescription: string | null;
	status: EnrollmentStatus;
	statusReasonCode: string | null;
	conditional: boolean;
	/** Recorded afterwards rather than enrolled, as for the years before an incoming transfer. */
	reconstructed: boolean;
	partTime: PartTime | null;
	/** Set when the year was suspended, with Esse3's reason code if any. */
	suspension: { reasonCode: string | null } | null;
	awaitingDegree: boolean;
	degreeAwardDate: LocalDate | null;
	studentTypeDescription: string | null;
	exemptionDescription: string | null;
	/** Bands are Esse3 ids: the registry publishes no labels for them. */
	incomeBandId: number | null;
	canteenBandId: number | null;
	meritBandId: number | null;
	meritNote: string | null;
	enrollmentNote: string | null;
	disabilityPercentage: number | null;
	disabilityTypeDescription: string | null;
	courseDescription: string | null;
	courseTypeDescription: string | null;
	degreeClassCode: string | null;
	degreeClassDescription: string | null;
	orientation: string | null;
	path: string | null;
	studyOrder: string | null;
	minimumCredits: number | null;
	courseDuration: number | null;
	teachingLanguage: string | null;
	regulationCode: string | null;
	universityDescription: string | null;
	siteDescription: string | null;
	enrollmentDate: LocalDate | null;
	insertionDate: LocalDate | null;
	modificationDate: LocalDate | null;
}

/** What the renewal card says about the current academic year. */
export type Renewal =
	| { kind: 'enrolled'; academicYear: number }
	| { kind: 'renewable'; academicYear: number }
	/** Graduating: nothing to renew. */
	| { kind: 'none' };

/**
 * Where annual re-enrolment happens: the portal page behind the student's SSO. Esse3's REST API
 * lets only the registry staff create enrolments, and completing one issues the first fee.
 */
export const RENEWAL_URL =
	'https://s3w.si.unimib.it/auth/studente/Iscrizioni/IscrizioniAnniSuccessivi.do';

/** Esse3 names the shared path of programmes without specialisations this way. */
const COMMON_PATH = 'percorso comune';

const text = (value: string | null): string | null => {
	const trimmed = value?.trim();
	return trimmed ? trimmed : null;
};

const positive = (value: number | null): number | null =>
	value !== null && value > 0 ? value : null;

function enrollmentType(code: string | null): EnrollmentType {
	switch (code?.trim().toUpperCase()) {
		case 'IC':
			return 'in_progress';
		case 'FC':
			return 'out_of_course';
		case 'RI':
			return 'repeating';
		default:
			return 'unknown';
	}
}

function enrollmentStatus(code: string | null): EnrollmentStatus {
	switch (code?.trim().toUpperCase()) {
		case 'A':
			return 'active';
		case 'X':
			return 'canceled';
		case 'S':
			return 'suspended';
		default:
			return 'unknown';
	}
}

/** `valoreMin` is a decimal string, such as "180" or "180.0". */
function credits(value: string | null): number | null {
	const parsed = Number.parseFloat(value?.replace(',', '.') ?? '');
	return Number.isFinite(parsed) && parsed > 0 ? Math.trunc(parsed) : null;
}

/** One row of the answer; null for a row without its academic year, which nothing can show. */
export function toEnrollment(row: Esse3AnnualEnrollment): AnnualEnrollment | null {
	if (row.academicYearEnrollmentId === null) return null;
	const path = text(row.addressDescription);
	const exemption = text(row.exemptionTypeDescription);
	return {
		academicYear: row.academicYearEnrollmentId,
		courseYear: row.courseYear ?? 0,
		outOfCourseYears: row.fcYears ?? 0,
		type: enrollmentType(row.enrollmentTypeCode),
		typeDescription: text(row.enrollmentTypeDescription),
		status: enrollmentStatus(row.enrollmentStatusCode),
		statusReasonCode: text(row.enrollmentStatusReasonCode),
		conditional: row.conditionFlag === 1,
		reconstructed: row.searchFlag === 1,
		partTime:
			row.ptFlag === 1
				? {
						credits: positive(row.ptCredits),
						extraCredits: positive(row.ptExtraCredits),
						locked: row.ptBlockedFlag === 1
					}
				: null,
		suspension: row.suspensionFlag === 1 ? { reasonCode: text(row.suspensionReasonCode) } : null,
		awaitingDegree: row.degreeAwardFlag === 1,
		degreeAwardDate: esse3Date(row.degreeAwardDate),
		studentTypeDescription: text(row.studentTypeDescription),
		exemptionDescription: row.exemptionTypeCode?.trim().toUpperCase() === 'N' ? null : exemption,
		incomeBandId: row.bandId,
		canteenBandId: row.canteenBandId,
		meritBandId: row.meritBandId,
		meritNote: text(row.meritNote),
		enrollmentNote: text(row.enrollmentNote),
		disabilityPercentage: positive(row.disabilityPercentage),
		disabilityTypeDescription: text(row.handicapTypeDescription),
		courseDescription: text(row.courseOfStudyDescription),
		courseTypeDescription: text(row.courseTypeDescription),
		degreeClassCode: text(row.enrollmentClassCode) ?? text(row.classMurstCode),
		degreeClassDescription: text(row.classMurstDescription) ?? text(row.classUniversityDescription),
		orientation: text(row.orientationDescription),
		path: path?.toLowerCase() === COMMON_PATH ? null : path,
		studyOrder: text(row.studyOrderDescription),
		minimumCredits: credits(row.minimumValue),
		courseDuration: positive(row.courseDuration),
		teachingLanguage: text(row.teachingLanguage),
		regulationCode: text(row.normCode),
		universityDescription: text(row.universityDescription),
		siteDescription: text(row.siteDescription),
		enrollmentDate: esse3Date(row.enrollmentDate),
		insertionDate: esse3Date(row.insertionDate),
		modificationDate: esse3Date(row.modificationDate)
	};
}

/**
 * The career's years, newest first, one per academic year: a year enrolled twice (cancelled,
 * then enrolled again) shows its active enrolment, else the first Esse3 lists.
 */
export function toEnrollments(rows: readonly Esse3AnnualEnrollment[]): AnnualEnrollment[] {
	const byYear = new Map<number, AnnualEnrollment>();
	for (const row of rows) {
		const enrollment = toEnrollment(row);
		if (enrollment === null) continue;
		const known = byYear.get(enrollment.academicYear);
		if (known === undefined || (known.status !== 'active' && enrollment.status === 'active')) {
			byYear.set(enrollment.academicYear, enrollment);
		}
	}
	return [...byYear.values()].sort((a, b) => b.academicYear - a.academicYear);
}

/**
 * Whether the student is enrolled for the current academic year, which turns in August as on
 * Android: from then on a career without that year's active enrolment is offered the renewal,
 * unless its latest year awaits graduation or the career is over (graduated, interrupted).
 */
export function renewalState(
	years: readonly AnnualEnrollment[],
	today: Date,
	careerOpen = true
): Renewal {
	const current = academicYear(today, 8);
	if (!careerOpen || (years.length > 0 && years[0].awaitingDegree)) return { kind: 'none' };
	const enrolled = years.some((year) => year.academicYear === current && year.status === 'active');
	return { kind: enrolled ? 'enrolled' : 'renewable', academicYear: current };
}
