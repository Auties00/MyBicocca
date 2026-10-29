import type { Esse3EnrollmentForTuition } from '@mybicocca/data-api/esse3';
import { romeDay } from '#lib/format/rome.ts';
import { nonBlank } from './esse3.ts';

/** Esse3 files a year without an ISEE in the top band, as an infinite income. */
const NOT_PRESENTED = 99_999_999;

export interface IseeYear {
	/** The academic year, by its September. */
	year: number;
	isee: number;
	/** The ISEE up to which benefits apply. */
	threshold: number | null;
	course: string | null;
	exemption: string | null;
	enrollmentType: string | null;
	courseYear: number | null;
}

type Declared = Esse3EnrollmentForTuition & { isee: number; academicYearEnrollmentId: number };

function declared(row: Esse3EnrollmentForTuition): row is Declared {
	return row.isee !== null && row.isee < NOT_PRESENTED && row.academicYearEnrollmentId !== null;
}

/** The years with an ISEE on file, newest first; one row per year. */
export function iseeYears(rows: readonly Esse3EnrollmentForTuition[]): IseeYear[] {
	const years = new Map<number, IseeYear>();
	for (const row of rows) {
		if (!declared(row) || years.has(row.academicYearEnrollmentId)) continue;
		years.set(row.academicYearEnrollmentId, {
			year: row.academicYearEnrollmentId,
			isee: row.isee,
			threshold: row.iseeThreshold,
			course: nonBlank(row.courseOfStudyDescription),
			exemption: nonBlank(row.description),
			enrollmentType: nonBlank(row.enrollmentTypeCodeDescription),
			courseYear: row.courseYear
		});
	}
	return [...years.values()].sort((a, b) => b.year - a.year);
}

/**
 * The academic year whose ISEE can still be declared at INPS, or null. INPS hands Bicocca the DSU
 * for Y/Y+1 only if it is signed during calendar year Y: so the latest enrolment, when it is this
 * year's and has no ISEE yet.
 */
export function declareYear(
	rows: readonly Esse3EnrollmentForTuition[],
	today: Date
): number | null {
	let latest: number | null = null;
	for (const row of rows) {
		const year = row.academicYearEnrollmentId;
		if (year !== null && (latest === null || year > latest)) latest = year;
	}
	/* The calendar year in Italy, where the DSU deadlines run. */
	if (latest === null || latest !== Number(romeDay(today.getTime()).slice(0, 4))) return null;
	return rows.some((row) => declared(row) && row.academicYearEnrollmentId === latest)
		? null
		: latest;
}

/** How far below the threshold the ISEE is (negative above it); null without a threshold. */
export function iseeMargin(year: Pick<IseeYear, 'isee' | 'threshold'>): number | null {
	return year.threshold === null || year.threshold <= 0 ? null : year.threshold - year.isee;
}
