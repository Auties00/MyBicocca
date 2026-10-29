import type { Career } from '#lib/data/account.ts';
import type { CourseCode } from './courseCode.ts';

/** The careers a course list is split by: which one each course belongs to. */
export type CareerLike = Pick<
	Career,
	'id' | 'easyStaffProgramCode' | 'academicYear' | 'level' | 'description'
>;

/**
 * Ties Moodle courses to the student's careers from their idnumber alone, offline. A course whose
 * activity code starts with a career's programme code belongs to it; otherwise its cohort (the
 * year its students started: academic year − year of study + 1) points to the latest career
 * started by then, else the earliest. A course without a year of study belongs to none.
 */
export class CourseCareers {
	readonly careers: readonly CareerLike[];
	readonly selected: number;
	readonly #byStart: readonly CareerLike[];

	constructor(careers: readonly CareerLike[], selected: number) {
		this.careers = careers;
		this.selected = selected;
		this.#byStart = careers
			.filter((career) => career.academicYear > 0)
			.toSorted((a, b) => a.academicYear - b.academicYear);
	}

	ownerOf(code: CourseCode): number | null {
		if (code.academicYear === null || code.courseYear === null) return null;
		if (this.careers.length <= 1) return this.careers[0]?.id ?? this.selected;
		const activity = code.code;
		if (activity !== null) {
			const upper = activity.toUpperCase();
			const byProgramme = this.careers.find((career) => {
				const programme = career.easyStaffProgramCode;
				return programme != null && programme !== '' && upper.startsWith(programme.toUpperCase());
			});
			if (byProgramme) return byProgramme.id;
		}
		const cohort = code.academicYear - code.courseYear + 1;
		const started = this.#byStart.findLast((career) => career.academicYear <= cohort);
		return (started ?? this.#byStart[0])?.id ?? this.selected;
	}
}
