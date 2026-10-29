import type { MoodleEnrolledCourse } from '@mybicocca/data-api/elearning';
import { parseCourseCode, periodLabel } from '#lib/features/elearning/courseCode.ts';

/** A Moodle edition of a course, as the course picker lists it. */
export interface CourseEdition {
	id: number;
	name: string;
	/** "2025/26", "2025/26 T2", or null for an edition with no academic year. */
	periodLabel: string | null;
	hidden: boolean;
}

/**
 * The e-learning editions of the course with `activityCode`: newest academic year first, visible
 * before hidden, the base course before its streams.
 */
export function editionsFor(
	courses: readonly MoodleEnrolledCourse[],
	activityCode: string
): CourseEdition[] {
	const wanted = activityCode.trim().toUpperCase();
	return courses
		.map((course) => ({ course, code: parseCourseCode(course.idNumber) }))
		.filter(({ code }) => code.code?.toUpperCase() === wanted)
		.sort(
			(a, b) =>
				(b.code.academicYear ?? 0) - (a.code.academicYear ?? 0) ||
				Number(a.course.hidden === true) - Number(b.course.hidden === true) ||
				(a.code.stream ?? 0) - (b.code.stream ?? 0)
		)
		.map(({ course, code }) => ({
			id: course.id,
			name: course.displayName?.trim() || course.fullName,
			periodLabel: periodLabel(code),
			hidden: course.hidden === true
		}));
}

/**
 * The turno of each course the student follows on e-learning, where its editions name exactly one
 * (`…-E3101Q103-T1`): the only record of it, as Esse3 and EasyStaff keep none.
 */
export function knownTurni(courses: readonly MoodleEnrolledCourse[]): Map<string, number> {
	const streams = new Map<string, Set<number>>();
	for (const course of courses) {
		const { code, stream } = parseCourseCode(course.idNumber);
		if (code === null || stream === null) continue;
		const key = code.toUpperCase();
		streams.set(key, (streams.get(key) ?? new Set()).add(stream));
	}
	const turni = new Map<string, number>();
	for (const [code, set] of streams) {
		if (set.size === 1) turni.set(code, [...set][0]);
	}
	return turni;
}
