import type {
	EasyStaffAttendanceRecord,
	EasyStaffStudyProgram,
	EasyStaffStudyProgramSubject
} from '@mybicocca/data-api/easystaff';
import { EasyStaffAttendanceStatus } from '@mybicocca/data-api/easystaff';
import type { MoodleEnrolledCourse } from '@mybicocca/data-api/elearning';
import type { Esse3StudyPlanActivity, Esse3TranscriptRow } from '@mybicocca/data-api/esse3';
import { Esse3State } from '@mybicocca/data-api/esse3';
import { academicYear, academicYearCode } from '#lib/data/academicYear.ts';
import { normalizeSubjectName, subjectBase } from '#lib/features/calendar/labels.ts';
import type { PlanCourse } from '#lib/features/studyplan/plan.ts';
import type { Classroom, ClassroomStatus, CourseAttendance, Semester } from './model.ts';

/** A course of the EasyStaff programme the plan joins to: it knows the semester and the teacher. */
export interface PlannedCourse {
	name: string;
	normalizedName: string;
	teacher: string | null;
	semester: Semester;
	/** 0 when unknown. */
	year: number;
	credits: number | null;
}

/** A course to attend, before its attendance is read. */
export interface Seed {
	name: string;
	code: string | null;
	normalizedName: string;
	credits: number;
	year: number;
	semester: Semester;
	teacher: string | null;
}

/** Bicocca's first semester runs September to January, the second February to August. */
export function semesterOf(today: Date): Exclude<Semester, 'unknown'> {
	const month = today.getMonth() + 1;
	return month >= 2 && month <= 8 ? 'second' : 'first';
}

function semesters(program: EasyStaffStudyProgram): Map<string, Semester> {
	return new Map(
		program.teachingPeriods.map((period) => {
			const code = period.code.toUpperCase();
			return [period.id, code === 'S1' ? 'first' : code === 'S2' ? 'second' : 'unknown'];
		})
	);
}

/** The year number of an EasyStaff programme year, from its code (`E3101Q|2` → 2). */
function yearNumber(code: string): number {
	return Number.parseInt(code.split('|')[1] ?? '', 10) || 0;
}

function teacherOf(subject: EasyStaffStudyProgramSubject): string | null {
	return subject.teacherName.trim() || null;
}

/**
 * The plan's activities joined to the programme's subjects, by code and then by name, so each
 * course learns its semester and teacher. EasyStaff names a course's variants after it ("… (T1
 * FRONTALE TURNO 1)"), so names are compared without the variant. Without a plan yet, every
 * subject of the student's current year of the programme.
 *
 * @param currentAcademicYear The first calendar year of today's academic year.
 * @param careerStartYear The first calendar year of the career's first academic year.
 */
export function plannedCourses(
	activities: readonly Esse3StudyPlanActivity[],
	program: EasyStaffStudyProgram,
	currentAcademicYear: number,
	careerStartYear: number
): PlannedCourse[] {
	const periods = semesters(program);
	const semesterOfSubject = (subject: EasyStaffStudyProgramSubject) =>
		periods.get(subject.periodId) ?? 'unknown';
	const seen = new Set<string>();
	const distinct = (code: string) => {
		if (seen.has(code)) return false;
		seen.add(code);
		return true;
	};

	if (activities.length === 0) {
		if (program.years.length === 0) return [];
		const numbers = program.years.map((year) => yearNumber(year.code));
		const elapsed = careerStartYear > 0 ? currentAcademicYear - careerStartYear + 1 : 1;
		const year = Math.min(Math.max(elapsed, Math.min(...numbers)), Math.max(...numbers));
		return program.years
			.filter((entry) => yearNumber(entry.code) === year)
			.flatMap((entry) => entry.subjects)
			.filter((subject) => distinct(subject.code))
			.map((subject) => ({
				name: subject.name,
				normalizedName: normalizeSubjectName(subject.name),
				teacher: teacherOf(subject),
				semester: semesterOfSubject(subject),
				year,
				credits: null
			}));
	}

	const subjects = program.years.flatMap((year) => year.subjects);
	const byCode = new Map(subjects.map((subject) => [subject.code, subject]));
	const byName = new Map<string, EasyStaffStudyProgramSubject>();
	for (const subject of subjects) {
		const name = normalizeSubjectName(subjectBase(subject.name));
		if (!byName.has(name)) byName.set(name, subject);
	}
	return activities.flatMap((activity) => {
		const name = activity.activityTranscriptDescription;
		if (name === null) return [];
		const code = activity.activityTranscriptCode;
		const subject =
			(code === null ? undefined : byCode.get(code)) ??
			byName.get(normalizeSubjectName(subjectBase(name)));
		if (subject === undefined || !distinct(subject.code)) return [];
		return [
			{
				name,
				normalizedName: normalizeSubjectName(name),
				teacher: teacherOf(subject),
				semester: semesterOfSubject(subject),
				year: activity.courseYear ?? 0,
				credits: activity.weight
			}
		];
	});
}

/** The academic year a record book row was attended in: its exam's, else its attendance's. */
function rowYear(row: Esse3TranscriptRow): number | null {
	return row.outcome?.academicYearSupervisorId ?? row.academicYearAttendanceId;
}

/**
 * The student's current year of study: the highest degree year among the record book rows of
 * this academic year, else the plan's deepest year; 0 when neither says.
 */
function currentYearOfStudy(
	rows: readonly Esse3TranscriptRow[],
	planCourses: readonly PlanCourse[],
	currentAcademicYear: number
): number {
	const years = rows.filter((row) => rowYear(row) === currentAcademicYear);
	const fromRows = years.length === 0 ? 0 : Math.max(...years.map((row) => row.courseYear));
	if (fromRows > 0) return fromRows;
	return Math.max(0, ...planCourses.map((course) => course.year));
}

/**
 * "Up to the current semester": earlier years, and this year's courses except the second
 * semester's while the first is on. Unknowns are kept: hiding a real course is worse than
 * showing one a semester early.
 */
function withinCurrentPosition(
	year: number,
	semester: Semester,
	currentYear: number,
	currentSemester: Semester
): boolean {
	if (currentYear === 0 || year === 0) return true;
	if (year < currentYear) return true;
	if (year > currentYear) return false;
	return currentSemester === 'second' || semester !== 'second';
}

export interface SeedSources {
	/** The plan's courses; empty without a plan. */
	planCourses: readonly PlanCourse[];
	planned: readonly PlannedCourse[];
	rows: readonly Esse3TranscriptRow[];
	today: Date;
}

/**
 * The courses still to attend: the plan's (the programme's when the plan has none) not yet
 * passed and up to the current semester. The plan is the backbone; the programme only adds the
 * semester, the teacher and, where the plan has none, the year.
 */
export function pendingSeeds({ planCourses, planned, rows, today }: SeedSources): Seed[] {
	const currentAcademicYear = academicYear(today);
	const currentSemester = semesterOf(today);
	const currentYear = currentYearOfStudy(rows, planCourses, currentAcademicYear);

	const plannedByName = new Map(planned.map((course) => [course.normalizedName, course]));
	const seed = (name: string, code: string | null, credits: number, year: number): Seed => {
		const normalizedName = normalizeSubjectName(name);
		const match = plannedByName.get(normalizedName);
		return {
			name,
			code,
			normalizedName,
			credits,
			year: year !== 0 ? year : (match?.year ?? 0),
			semester: match?.semester ?? 'unknown',
			teacher: match?.teacher ?? null
		};
	};
	const seeds =
		planCourses.length > 0
			? planCourses.map((course) => seed(course.name, course.code, course.credits, course.year))
			: planned.map((course) => seed(course.name, null, course.credits ?? 0, course.year));

	const rowsByCode = new Map<string, Esse3TranscriptRow>();
	const rowsByName = new Map<string, Esse3TranscriptRow>();
	for (const row of rows) {
		if (row.activityCode !== null) rowsByCode.set(row.activityCode, row);
		rowsByName.set(normalizeSubjectName(row.activityDescription), row);
	}

	const seen = new Set<string>();
	return seeds.filter((candidate) => {
		const identity = candidate.code ?? candidate.normalizedName;
		if (seen.has(identity)) return false;
		seen.add(identity);
		const row =
			(candidate.code === null ? undefined : rowsByCode.get(candidate.code)) ??
			rowsByName.get(candidate.normalizedName);
		if (row?.state === Esse3State.Passed) return false;
		return withinCurrentPosition(candidate.year, candidate.semester, currentYear, currentSemester);
	});
}

function classroomStatus(state: string | null): ClassroomStatus {
	if (state === EasyStaffAttendanceStatus.Attending) return 'attending';
	if (state === EasyStaffAttendanceStatus.InProgress) return 'in-progress';
	return 'not-attending';
}

/**
 * The badge record of a course, by code (any case) or name: a course with several (one per
 * turno or part) is summed up by the one closest to the requirement.
 */
export function classroomOf(
	records: readonly EasyStaffAttendanceRecord[],
	seed: Pick<Seed, 'code' | 'normalizedName'>
): Classroom | null {
	const code = seed.code?.toUpperCase();
	let best: EasyStaffAttendanceRecord | undefined;
	for (const record of records) {
		const matches =
			(code !== undefined && record.courseCode.toUpperCase() === code) ||
			normalizeSubjectName(record.courseName) === seed.normalizedName;
		if (
			matches &&
			(best === undefined ||
				record.requirementProgressPercentage > best.requirementProgressPercentage)
		) {
			best = record;
		}
	}
	if (best === undefined) return null;
	return {
		percentage: best.attendancePercentage,
		lessonsAttended: best.lessonsAttended,
		hours: best.totalHours,
		requirementProgress: best.requirementProgressPercentage,
		status: classroomStatus(best.state)
	};
}

/** Moodle course idnumbers read "aa-year-code", with an optional turno: `2526-2-E3101Q103-T1`. */
const ID_NUMBER = /^(\d{4})-(\d+)-(.+)$/;

/**
 * The e-learning course of this academic year's edition of the course: only it holds a live
 * register. A turno's own course (`…-T1`) is a fallback to the plain edition.
 */
export function editionOf(
	enrolled: readonly MoodleEnrolledCourse[],
	code: string | null,
	today: Date
): MoodleEnrolledCourse | undefined {
	if (code === null) return undefined;
	const prefix = academicYearCode(academicYear(today));
	const wanted = code.toUpperCase();
	let best: MoodleEnrolledCourse | undefined;
	for (const course of enrolled) {
		const match = ID_NUMBER.exec(course.idNumber);
		if (match === null || match[1] !== prefix) continue;
		if (match[3].split('-', 1)[0].toUpperCase() !== wanted) continue;
		if (best === undefined || course.idNumber.length < best.idNumber.length) best = course;
	}
	return best;
}

const SEMESTER_ORDER: Record<Semester, number> = { first: 0, second: 1, unknown: 2 };

/** By year, then semester (first, second, unknown), then name. */
export function compareCourses(a: CourseAttendance, b: CourseAttendance): number {
	return (
		a.year - b.year ||
		SEMESTER_ORDER[a.semester] - SEMESTER_ORDER[b.semester] ||
		a.name.localeCompare(b.name, 'it')
	);
}
