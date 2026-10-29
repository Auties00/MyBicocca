import type { EasyStaffScheduleCell, EasyStaffStudyProgram } from '@mybicocca/data-api/easystaff';
import { Esse3State, type Esse3StudyPlan } from '@mybicocca/data-api/esse3';
import { LocalDate } from '@mybicocca/data-api/runtime';
import { academicYear } from '#lib/data/academicYear.ts';
import { careerKey, publicKey } from '#lib/data/keys.ts';
import { cache, policies, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import { enrolledCoursesQuery } from '#lib/features/elearning/queries.ts';
import { recordBookRowsQuery } from '#lib/features/profile/queries.ts';
import { planHeadersQuery, studentPlanQuery } from '#lib/features/services/sources.ts';
import { firstOfMonth, lastOfMonth, toJsDate, type YearMonth } from '#lib/format/days.ts';
import { knownTurni } from './courses.ts';
import { normalizeSubjectName } from './labels.ts';
import { lessonActivity, lessonFromCell } from './mappers.ts';
import type { LessonEvent } from './model.ts';
import { pickPlanHeader, plannedSubjects, type PlannedSubject } from './subjects.ts';

/*
 * Bookings, the record book, the study plan, the e-learning courses and their deadlines, the
 * library seats and the desk appointments are other features' queries, imported where they are
 * used: one cache entry per SDK call, whoever asks.
 * The `calendar` keys below derive from several calls; `careerKey(scope, 'calendar')` covers
 * them all, which is what an exam booking invalidates.
 */

/** Every calendar source is fresh for ten minutes, as on Android. */
const CALENDAR = policies.fresh(10);

function fetchQuery<T>(options: QueryOptions<T>): Promise<T> {
	return cache.fetch(options.key, options.load, options.policy ?? policies.live);
}

/** Every degree programme EasyStaff knows in `year`: a large page, kept for the visit only. */
export function studyProgramsQuery(
	scope: SessionScope,
	year: string
): QueryOptions<EasyStaffStudyProgram[]> {
	return {
		key: publicKey('easystaff.core.getStudyPrograms', year),
		load: () => scope.api.easyStaff.core.getStudyPrograms(year),
		policy: policies.memory(30)
	};
}

/** The career's study plan: the approved one, else the latest; none before the student files one. */
async function fetchPlan(scope: SessionScope): Promise<Esse3StudyPlan | undefined> {
	const planId = pickPlanHeader(await fetchQuery(planHeadersQuery(scope)))?.planId ?? null;
	return planId === null ? undefined : fetchQuery(studentPlanQuery(scope, planId));
}

/**
 * The courses whose lessons the calendar shows: the study plan joined to the EasyStaff programme,
 * minus what the record book says is passed. The record book and the e-learning courses (which
 * may name the student's turno) only refine the list, so their failure doesn't stop the timetable.
 */
function subjectsQuery(scope: SessionScope): QueryOptions<PlannedSubject[]> {
	return {
		key: careerKey(scope, 'calendar', 'subjects'),
		load: async () => {
			const programCode = scope.career.easyStaffProgramCode;
			if (!programCode) return [];
			const current = academicYear();
			const [plan, programs, rows, courses] = await Promise.all([
				fetchPlan(scope),
				fetchQuery(studyProgramsQuery(scope, String(current))),
				fetchQuery(recordBookRowsQuery(scope)).catch(() => []),
				fetchQuery(enrolledCoursesQuery(scope)).catch(() => [])
			]);
			const program = programs.find((entry) => entry.code === programCode);
			if (!program) return [];
			const passed = new Set(
				rows
					.filter((row) => row.state === Esse3State.Passed)
					.map((row) => normalizeSubjectName(row.activityDescription))
			);
			return plannedSubjects(
				plan?.activity ?? [],
				program,
				current,
				scope.career.academicYear,
				passed,
				knownTurni(courses)
			);
		},
		policy: policies.fresh(30)
	};
}

/**
 * The half of the academic year a month falls in, and a day inside it to ask EasyStaff about:
 * September to January is the first semester, February to August the second.
 */
function halfOf(month: YearMonth): { year: string; half: 1 | 2; day: LocalDate } {
	const year = String(academicYear(toJsDate(firstOfMonth(month))));
	const value = Number(month.slice(5));
	return value >= 9 || value === 1
		? { year, half: 1, day: new LocalDate(Number(year), 10, 1) }
		: { year, half: 2, day: new LocalDate(Number(year) + 1, 3, 1) };
}

/**
 * The given subjects' lessons for half an academic year. EasyStaff answers a week request with
 * every lesson of the period (the form asks for all events), so one request serves every month of
 * it; the answer is large, so it stays in memory only.
 */
function timetableQuery(
	scope: SessionScope,
	month: YearMonth,
	codes: readonly string[]
): QueryOptions<EasyStaffScheduleCell[]> {
	const { year, half, day } = halfOf(month);
	return {
		key: publicKey('easystaff.schedule.getScheduleBySubjects', year, half, codes.join(',')),
		load: () => scope.api.easyStaff.schedule.getScheduleBySubjects(year, [...codes], day),
		policy: policies.memory(10)
	};
}

/** Marks every timetable stale, so the next month load asks EasyStaff again. */
export function staleTimetable(): void {
	cache.invalidate(publicKey('easystaff.schedule.getScheduleBySubjects'));
}

/** One month of lessons, cut out of its half-year timetable and de-duplicated. */
export function lessonsQuery(scope: SessionScope, month: YearMonth): QueryOptions<LessonEvent[]> {
	return {
		key: careerKey(scope, 'calendar', 'lessons', month),
		load: async () => {
			const subjects = await fetchQuery(subjectsQuery(scope));
			if (subjects.length === 0) return [];
			const codes = [...new Set(subjects.map((subject) => subject.subjectCode))].sort();
			const joined = new Map(
				subjects.flatMap((subject) =>
					subject.activityCode ? [[subject.subjectCode, subject.activityCode] as const] : []
				)
			);
			/* The plan is cached by now: the subjects were read from it. */
			const plan = await fetchPlan(scope).catch(() => undefined);
			const careerCodes = new Set([
				...joined.values(),
				...(plan?.activity ?? []).flatMap((activity) =>
					activity.activityTranscriptCode === null ? [] : [activity.activityTranscriptCode]
				)
			]);
			const cells = await fetchQuery(timetableQuery(scope, month, codes));
			const first = firstOfMonth(month);
			const last = lastOfMonth(month);
			const lessons = new Map<string, LessonEvent>();
			for (const cell of cells) {
				const lesson = lessonFromCell(cell, lessonActivity(cell, joined, careerCodes));
				if (lesson && lesson.date >= first && lesson.date <= last && !lessons.has(lesson.id)) {
					lessons.set(lesson.id, lesson);
				}
			}
			return [...lessons.values()];
		},
		policy: CALENDAR
	};
}
