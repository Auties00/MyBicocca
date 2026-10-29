import type { EasyStaffAttendanceRecord } from '@mybicocca/data-api/easystaff';
import type { MoodleOption, MoodleWebAttendanceView } from '@mybicocca/data-api/elearning';
import type { Esse3StudyPlan, Esse3StudyPlanHeader } from '@mybicocca/data-api/esse3';
import { academicYear } from '#lib/data/academicYear.ts';
import { toDataError } from '#lib/data/errors.ts';
import { careerKey } from '#lib/data/keys.ts';
import { cache, policies, type QueryKey, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import { studyProgramsQuery } from '#lib/features/calendar/queries.ts';
import { pickPlanHeader } from '#lib/features/calendar/subjects.ts';
import { enrolledCoursesQuery } from '#lib/features/elearning/queries.ts';
import { recordBookRowsQuery } from '#lib/features/profile/queries.ts';
import { planHeadersQuery, studentPlanQuery } from '#lib/features/services/sources.ts';
import { toStudyPlan } from '#lib/features/studyplan/plan.ts';
import {
	courseSlug,
	type AttendanceModule,
	type CourseAttendance,
	type Register
} from './model.ts';
import { classroomOf, compareCourses, editionOf, pendingSeeds, plannedCourses } from './pending.ts';

/*
 * The plan, the record book, the e-learning courses and the programme catalogue are other
 * features' queries, so their cache entries are shared. The list derived from them is cached as
 * a whole: opening a course reads it, and a few minutes on the page reloads it (Android reloads
 * on every visit); a check-in invalidates it.
 */

/** Attendance changes a lesson at a time. */
const ATTENDANCE = policies.fresh(5);

function fetchQuery<T>(options: QueryOptions<T>): Promise<T> {
	return cache.fetch(options.key, options.load, options.policy ?? policies.live);
}

/**
 * Offline nothing loaded is trustworthy: the load fails, so the list cached before stays on
 * screen. Any other failure leaves only the source's own part out.
 */
function unlessOffline(error: unknown): void {
	if (toDataError(error).kind === 'offline') throw error;
}

/** A source the list can do without: when it fails, what it last answered, else `fallback`. */
async function optional<T>(options: QueryOptions<T>, fallback: T): Promise<T> {
	try {
		return await fetchQuery(options);
	} catch (error) {
		unlessOffline(error);
		return cache.peek<T>(options.key).data ?? fallback;
	}
}

/** Everything this page caches, for invalidating it after a check-in. */
export function attendancePrefix(scope: SessionScope): QueryKey {
	return careerKey(scope, 'attendance');
}

/** The student's EasyBadge records, every course's; read live, as the page itself is cached. */
function badgeQuery(scope: SessionScope): QueryOptions<EasyStaffAttendanceRecord[]> {
	const studentId = scope.career.studentNumber;
	return {
		key: careerKey(scope, 'easystaff.attendance.getAttendanceHistory', studentId),
		load: async () => {
			const history = await scope.api.easyStaff.attendance.getAttendanceHistory({ studentId });
			return Object.values(history[studentId] ?? {}).flat();
		},
		policy: policies.none
	};
}

/** The approved plan, else the latest; undefined when the student has none yet. */
async function loadPlan(
	scope: SessionScope
): Promise<{ header: Esse3StudyPlanHeader; plan: Esse3StudyPlan } | undefined> {
	let headers: Esse3StudyPlanHeader[];
	try {
		headers = await fetchQuery(planHeadersQuery(scope));
	} catch (error) {
		// Esse3 answers a career without plans with a 404.
		if (toDataError(error).kind === 'not_found') return undefined;
		throw error;
	}
	const header = pickPlanHeader(headers);
	if (header?.planId == null) return undefined;
	return { header, plan: await fetchQuery(studentPlanQuery(scope, header.planId)) };
}

/**
 * An attendance activity's page, which only reads (marking happens on the pages it links to).
 * Null when Moodle shows another page even with a renewed session: the course isn't the user's.
 */
export function attendanceView(
	scope: SessionScope,
	cmId: number
): Promise<MoodleWebAttendanceView | null> {
	return scope.api.moodleWeb((web) =>
		web.attendance.getAttendanceView(cmId).then((view) => (view.shown ? view : null))
	);
}

/** Only attendance activities, without file listings, to keep the answer small. */
const ATTENDANCE_ONLY: MoodleOption[] = [
	{ name: 'modname', value: 'attendance' },
	{ name: 'excludecontents', value: '1' }
];

/** The registers of an e-learning course; a failure leaves the course without them. */
async function registersOf(
	scope: SessionScope,
	courseId: number
): Promise<{ modules: AttendanceModule[]; registers: Register[] }> {
	try {
		const sections = await scope.api.moodle((moodle, auth) =>
			moodle.course.getContents(courseId, auth, { options: ATTENDANCE_ONLY })
		);
		const modules = sections
			.flatMap((section) => section.modules)
			.filter((module) => module.modName === 'attendance')
			.map((module) => ({ courseId, cmId: module.id, name: module.name }));
		const views = await Promise.all(
			modules.map((module) =>
				attendanceView(scope, module.cmId).catch((error: unknown) => {
					unlessOffline(error);
					return null;
				})
			)
		);
		const registers = modules.flatMap((module, index): Register[] => {
			const summary = views[index]?.summary;
			if (!summary) return [];
			return [
				{
					cmId: module.cmId,
					name: module.name,
					attended: summary.attendedSessions,
					recordedPercentage: summary.takenSessionsPercentage
				}
			];
		});
		return { modules, registers };
	} catch (error) {
		unlessOffline(error);
		return { modules: [], registers: [] };
	}
}

/**
 * The courses the student still has to attend, each with its EasyBadge record and Moodle
 * registers. The study plan is the backbone: its failure is the page's. The other sources only
 * add to it, so theirs leave their part out.
 */
async function loadCourses(scope: SessionScope): Promise<CourseAttendance[]> {
	const today = new Date();
	const year = academicYear(today);
	const { career } = scope;
	const programs = career.easyStaffProgramCode
		? optional(studyProgramsQuery(scope, String(year)), [])
		: Promise.resolve([]);
	const rows = optional(recordBookRowsQuery(scope), []);
	const badge = career.studentNumber ? optional(badgeQuery(scope), []) : Promise.resolve([]);
	const enrolled = optional(enrolledCoursesQuery(scope), []);
	// Awaited below; a rejection meanwhile must not surface as unhandled.
	for (const source of [programs, rows, badge, enrolled]) source.catch(() => undefined);

	const plan = await loadPlan(scope);
	const program = (await programs).find((entry) => entry.code === career.easyStaffProgramCode);
	const planned =
		program === undefined
			? []
			: plannedCourses(plan?.plan.activity ?? [], program, year, career.academicYear);
	const seeds = pendingSeeds({
		planCourses: plan === undefined ? [] : toStudyPlan(plan.header, plan.plan).courses,
		planned,
		rows: await rows,
		today
	});

	const [records, courses] = await Promise.all([badge, enrolled]);
	const attendance = await Promise.all(
		seeds.map(async (seed): Promise<CourseAttendance> => {
			const edition = editionOf(courses, seed.code, today);
			const moodle =
				edition === undefined
					? { modules: [], registers: [] }
					: await registersOf(scope, edition.id);
			return {
				slug: courseSlug(seed.code, seed.normalizedName),
				name: seed.name,
				code: seed.code,
				year: seed.year,
				semester: seed.semester,
				credits: seed.credits,
				teacher: seed.teacher,
				classroom: classroomOf(records, seed),
				registers: moodle.registers,
				modules: moodle.modules,
				elearningId: edition?.id ?? null
			};
		})
	);
	return attendance.sort(compareCourses);
}

export function attendanceQuery(scope: SessionScope): QueryOptions<CourseAttendance[]> {
	return {
		key: careerKey(scope, 'attendance', 'courses'),
		load: () => loadCourses(scope),
		policy: ATTENDANCE
	};
}

/** The attendance activities of the student's courses, loading the list when none is known. */
export async function knownModules(scope: SessionScope): Promise<AttendanceModule[]> {
	const options = attendanceQuery(scope);
	const modulesOf = (courses: readonly CourseAttendance[] | undefined) =>
		courses?.flatMap((course) => course.modules) ?? [];
	const cached = modulesOf(cache.peek<CourseAttendance[]>(options.key).data);
	if (cached.length > 0) return cached;
	return modulesOf(await fetchQuery(options));
}
