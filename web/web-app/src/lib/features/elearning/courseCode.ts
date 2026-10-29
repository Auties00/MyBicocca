import { academicYearLabel } from '#lib/data/academicYear.ts';

/**
 * A Moodle course idnumber, `XXYY-N-CODE[-T<stream>]`: the academic year (`2526` for 2025/26),
 * the year of study and the activity code, with an optional lecture stream ("turno"). Courses
 * outside the scheme (announcements, tutoring, Erasmus pages) parse to nulls.
 */
export interface CourseCode {
	/** The September the academic year starts: 2025 for `2526`. */
	academicYear: number | null;
	/** 1–9; only on the canonical form, after an academic year. */
	courseYear: number | null;
	/** The activity code with any stream stripped, shared by a course's editions and streams. */
	code: string | null;
	stream: number | null;
}

export const EMPTY_CODE: CourseCode = {
	academicYear: null,
	courseYear: null,
	code: null,
	stream: null
};

export function parseCourseCode(idNumber: string | null | undefined): CourseCode {
	if (idNumber == null || idNumber.trim() === '') return EMPTY_CODE;
	const firstDash = idNumber.indexOf('-');
	const first = firstDash < 0 ? idNumber : idNumber.slice(0, firstDash);
	const hasYear = /^\d{4}$/.test(first);
	const academicYear = hasYear ? 2000 + Number(first.slice(0, 2)) : null;
	const afterYear = firstDash < 0 ? '' : idNumber.slice(firstDash + 1);
	const secondDash = afterYear.indexOf('-');
	let courseYear: number | null = null;
	if (academicYear !== null && secondDash >= 0) {
		const year = afterYear.slice(0, secondDash);
		if (/^\+?\d+$/.test(year) && Number(year) >= 1 && Number(year) <= 9) courseYear = Number(year);
	}
	const rest = secondDash < 0 ? '' : afterYear.slice(secondDash + 1);
	const fullCode = rest.trim() === '' ? null : rest;
	return { academicYear, courseYear, ...splitStream(fullCode) };
}

/** A trailing `-T<digits>` with a base before it is a stream; a code that is itself "T123" isn't. */
function splitStream(code: string | null): Pick<CourseCode, 'code' | 'stream'> {
	if (code === null) return { code: null, stream: null };
	const lastDash = code.lastIndexOf('-');
	if (lastDash <= 0) return { code, stream: null };
	const suffix = code.slice(lastDash + 1);
	if (!/^T\d+$/.test(suffix)) return { code, stream: null };
	return { code: code.slice(0, lastDash), stream: Number(suffix.slice(1)) };
}

/** "2025/26", or "2025/26 T1" for a stream; null without an academic year (a cross-year space). */
export function periodLabel(code: CourseCode): string | null {
	if (code.academicYear === null) return null;
	const year = academicYearLabel(code.academicYear);
	return code.stream === null ? year : `${year} T${code.stream}`;
}

/** The short code a catalog lists, without its leading `yyyy-n-` edition prefix. */
export function bareCode(shortName: string): string {
	return shortName.replace(/^\d{4}-\d-/, '');
}
