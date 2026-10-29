import type {
	EasyStaffStudyProgram,
	EasyStaffStudyProgramSubject
} from '@mybicocca/data-api/easystaff';
import type { Esse3StudyPlanActivity, Esse3StudyPlanHeader } from '@mybicocca/data-api/esse3';
import { Esse3State3 } from '@mybicocca/data-api/esse3';
import { normalizeSubjectName, subjectBase, subjectTurno } from './labels.ts';

/** A course whose lessons the calendar shows. */
export interface PlannedSubject {
	/** The EasyStaff code the timetable is searched by. */
	subjectCode: string;
	/** The Esse3 activity code, when the study plan names it. */
	activityCode: string | null;
	normalizedName: string;
}

/** The plan to read: the approved one, else the latest. */
export function pickPlanHeader(
	headers: readonly Esse3StudyPlanHeader[]
): Esse3StudyPlanHeader | undefined {
	const approved = headers.find((header) => header.state === Esse3State3.Approved);
	if (approved) return approved;
	return headers.reduce<Esse3StudyPlanHeader | undefined>(
		(best, header) =>
			best === undefined || (header.planId ?? -Infinity) > (best.planId ?? -Infinity)
				? header
				: best,
		undefined
	);
}

/** The year number of an EasyStaff program year, from its code (`E3101Q|2` → 2). */
function yearNumber(code: string): number {
	return Number.parseInt(code.split('|')[1] ?? '', 10) || 0;
}

function distinctByCode(subjects: PlannedSubject[]): PlannedSubject[] {
	const seen = new Set<string>();
	return subjects.filter((subject) => {
		if (seen.has(subject.subjectCode)) return false;
		seen.add(subject.subjectCode);
		return true;
	});
}

/**
 * The courses to show lessons of: the study plan's activities joined to the programme's subjects
 * (by code, else by name); without a plan yet, every subject of the student's current year of
 * the programme. Courses already passed, and those of later years, are left out: their lessons
 * are noise.
 *
 * EasyStaff splits a course into variants named after it, one per turno, group or kind of
 * class ("… (T1 FRONTALE TURNO 1)", "… (TUGLA LAB TURNO UNICO GRUPPO A)"), and keeps no record of
 * which one a student follows: an activity joins every variant of its name, narrowed to one
 * turno when the student's e-learning enrolment names it.
 *
 * @param currentAcademicYear The first calendar year of today's academic year.
 * @param careerStartYear The first calendar year of the career's first academic year.
 * @param turni Activity code → the turno the student's e-learning course belongs to.
 */
export function plannedSubjects(
	activities: readonly Esse3StudyPlanActivity[],
	program: EasyStaffStudyProgram,
	currentAcademicYear: number,
	careerStartYear: number,
	passedNames: ReadonlySet<string>,
	turni: ReadonlyMap<string, number> = new Map()
): PlannedSubject[] {
	const year = careerStartYear > 0 ? currentAcademicYear - careerStartYear + 1 : Infinity;
	const current = activities.filter((activity) => (activity.courseYear ?? 0) <= year);
	const planned =
		activities.length === 0
			? currentYearSubjects(program, currentAcademicYear, careerStartYear)
			: intersect(current, program, turni);
	return planned.filter((subject) => !passedNames.has(subject.normalizedName));
}

function currentYearSubjects(
	program: EasyStaffStudyProgram,
	currentAcademicYear: number,
	careerStartYear: number
): PlannedSubject[] {
	if (program.years.length === 0) return [];
	const numbers = program.years.map((year) => yearNumber(year.code));
	const elapsed = careerStartYear > 0 ? currentAcademicYear - careerStartYear + 1 : 1;
	const year = Math.min(Math.max(elapsed, Math.min(...numbers)), Math.max(...numbers));
	return distinctByCode(
		program.years
			.filter((entry) => yearNumber(entry.code) === year)
			.flatMap((entry) => entry.subjects)
			.map((subject) => ({
				subjectCode: subject.code,
				activityCode: null,
				normalizedName: normalizeSubjectName(subjectBase(subject.name))
			}))
	);
}

function intersect(
	activities: readonly Esse3StudyPlanActivity[],
	program: EasyStaffStudyProgram,
	turni: ReadonlyMap<string, number>
): PlannedSubject[] {
	const byCode = new Map<string, EasyStaffStudyProgramSubject>();
	const byName = new Map<string, EasyStaffStudyProgramSubject[]>();
	for (const subject of program.years.flatMap((year) => year.subjects)) {
		byCode.set(subject.code, subject);
		const name = normalizeSubjectName(subjectBase(subject.name));
		byName.set(name, [...(byName.get(name) ?? []), subject]);
	}
	const planned: PlannedSubject[] = [];
	for (const activity of activities) {
		const name = activity.activityTranscriptDescription;
		if (name === null) continue;
		const code = activity.activityTranscriptCode;
		const exact = code === null ? undefined : byCode.get(code);
		const variants = exact ? [exact] : (byName.get(normalizeSubjectName(subjectBase(name))) ?? []);
		const turno = code === null ? undefined : turni.get(code);
		const followed =
			turno === undefined
				? variants
				: variants.filter((subject) => [null, turno].includes(subjectTurno(subject.name)));
		for (const subject of followed) {
			planned.push({
				subjectCode: subject.code,
				activityCode: code,
				normalizedName: normalizeSubjectName(name)
			});
		}
	}
	return distinctByCode(planned);
}
