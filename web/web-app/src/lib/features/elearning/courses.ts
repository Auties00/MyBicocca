import type { MoodleEnrolledCourse } from '@mybicocca/data-api/elearning';
import type { CourseCareers, CareerLike } from './careers.ts';
import { parseCourseCode, periodLabel, type CourseCode } from './courseCode.ts';
import { stripMlang } from './mlang.ts';

/** A Moodle course the student is enrolled in, as the course list shows it. */
export interface EnrolledCourse {
	id: number;
	shortName: string;
	fullName: string;
	displayName: string;
	/** Blank idnumbers are null. */
	idNumber: string | null;
	code: CourseCode;
	/** Completion percentage 0–100, when the course tracks it. */
	progress: number | null;
	/** Moodle's own flag; the list shows the local choice over it (see `favourites`). */
	moodleFavourite: boolean;
	hidden: boolean;
	/** Moodle's order: the position in its answer. */
	order: number;
	/** Epoch milliseconds. */
	lastAccess: number | null;
}

/**
 * The list's view of Moodle's answer: names in `lang` from their `{mlang}` blocks, the
 * idnumber parsed, Moodle's order kept.
 */
export function toEnrolledCourses(
	courses: readonly MoodleEnrolledCourse[],
	lang: string
): EnrolledCourse[] {
	return courses.map((course, order) => {
		const fullName = stripMlang(course.fullName, lang);
		const idNumber = course.idNumber.trim() === '' ? null : course.idNumber;
		return {
			id: course.id,
			shortName: stripMlang(course.shortName, lang),
			fullName,
			displayName: course.displayName ? stripMlang(course.displayName, lang) : fullName,
			idNumber,
			code: parseCourseCode(idNumber),
			progress:
				course.enableCompletion === false || course.progress === null
					? null
					: Math.min(100, Math.max(0, course.progress)),
			moodleFavourite: course.isFavourite === true,
			hidden: course.hidden === true,
			order,
			lastAccess: course.lastAccess ? course.lastAccess * 1000 : null
		};
	});
}

const NO_YEAR = Number.MIN_SAFE_INTEGER;

export type CourseFilter =
	| { kind: 'all' }
	| { kind: 'favourites' }
	| { kind: 'year'; year: number }
	| { kind: 'career'; careerId: number };

export const ALL: CourseFilter = { kind: 'all' };

/** `all`, `favourites`, `year:2`, `career:700001`: the filter in URLs and storage. */
export function filterToken(filter: CourseFilter): string {
	switch (filter.kind) {
		case 'all':
		case 'favourites':
			return filter.kind;
		case 'year':
			return `year:${filter.year}`;
		case 'career':
			return `career:${filter.careerId}`;
	}
}

export function parseFilter(token: string | null | undefined): CourseFilter | null {
	if (token === 'all') return ALL;
	if (token === 'favourites') return { kind: 'favourites' };
	const match = /^(year|career):(\d+)$/.exec(token ?? '');
	if (!match) return null;
	const value = Number(match[2]);
	return match[1] === 'year' ? { kind: 'year', year: value } : { kind: 'career', careerId: value };
}

/** The chips beyond All and Favourites. */
export interface FilterOptions {
	/** The selected career's years of study that have courses. */
	years: number[];
	/** Other careers that own a course, most recent first. */
	otherCareers: CareerLike[];
}

export function filterOptions(
	courses: readonly EnrolledCourse[],
	careers: CourseCareers
): FilterOptions {
	const owned = courses
		.filter((course) => !course.hidden)
		.map((course) => ({ course, owner: careers.ownerOf(course.code) }));
	const years = [
		...new Set(
			owned.flatMap(({ course, owner }) =>
				owner === careers.selected && course.code.courseYear !== null
					? [course.code.courseYear]
					: []
			)
		)
	].sort((a, b) => a - b);
	const owners = new Set(owned.map(({ owner }) => owner));
	const otherCareers = careers.careers
		.filter((career) => career.id !== careers.selected && owners.has(career.id))
		.toSorted((a, b) => b.academicYear - a.academicYear);
	return { years, otherCareers };
}

/** The filter if its chip still exists, else All: a year or career can vanish after a switch. */
export function resolveFilter(filter: CourseFilter, options: FilterOptions): CourseFilter {
	switch (filter.kind) {
		case 'all':
		case 'favourites':
			return filter;
		case 'year':
			return options.years.includes(filter.year) ? filter : ALL;
		case 'career':
			return options.otherCareers.some((career) => career.id === filter.careerId) ? filter : ALL;
	}
}

/** One teaching: its editions, latest first. */
export interface CourseGroup {
	/** Stable across refreshes: the shared activity code, else the latest edition's id. */
	key: string;
	editions: EnrolledCourse[];
}

export function latestEdition(group: CourseGroup): EnrolledCourse {
	return group.editions[0];
}

/**
 * Groups editions of one teaching: two courses join when they share the parsed code or the
 * exact trimmed full name, transitively (Esse3 reissues codes across years and rewords names).
 * Groups keep Moodle's order of first appearance; editions go latest academic year first (none
 * last), the base course before its streams.
 */
export function groupEditions(courses: readonly EnrolledCourse[]): CourseGroup[] {
	const parent = courses.map((_, index) => index);
	const find = (index: number): number => {
		let root = index;
		while (parent[root] !== root) root = parent[root];
		let node = index;
		while (parent[node] !== root) {
			const next = parent[node];
			parent[node] = root;
			node = next;
		}
		return root;
	};
	const union = (a: number, b: number) => {
		parent[find(a)] = find(b);
	};
	const byCode = new Map<string, number>();
	const byName = new Map<string, number>();
	courses.forEach((course, index) => {
		const code = course.code.code;
		if (code !== null) {
			const seed = byCode.get(code);
			if (seed === undefined) byCode.set(code, index);
			else union(seed, index);
		}
		const name = course.fullName.trim();
		if (name !== '') {
			const seed = byName.get(name);
			if (seed === undefined) byName.set(name, index);
			else union(seed, index);
		}
	});
	const components = new Map<number, EnrolledCourse[]>();
	courses.forEach((course, index) => {
		const root = find(index);
		const members = components.get(root);
		if (members) members.push(course);
		else components.set(root, [course]);
	});
	return [...components.values()].map((members) => {
		const editions = members.toSorted(
			(a, b) =>
				(b.code.academicYear ?? NO_YEAR) - (a.code.academicYear ?? NO_YEAR) ||
				(a.code.stream ?? 0) - (b.code.stream ?? 0)
		);
		const latest = editions[0];
		return {
			key: latest.code.code === null ? `id:${latest.id}` : `code:${latest.code.code}`,
			editions
		};
	});
}

export interface FilteredCourses {
	/** The filter applied: the asked one, or All when its chip is gone. */
	filter: CourseFilter;
	options: FilterOptions;
	groups: CourseGroup[];
}

/**
 * The course list: hidden courses out, the filter applied, editions grouped. All puts the
 * selected career first, then the other careers most recent first, then courses of no career,
 * each by year of study; a career chip sorts by year of study; Favourites and years keep
 * Moodle's order. Sorts are stable.
 */
export function filterCourses(
	courses: readonly EnrolledCourse[],
	asked: CourseFilter,
	careers: CourseCareers,
	favourite: (course: EnrolledCourse) => boolean
): FilteredCourses {
	const options = filterOptions(courses, careers);
	const filter = resolveFilter(asked, options);
	const visible = courses.filter((course) => {
		if (course.hidden) return false;
		switch (filter.kind) {
			case 'all':
				return true;
			case 'favourites':
				return favourite(course);
			case 'year':
				return (
					course.code.courseYear === filter.year &&
					careers.ownerOf(course.code) === careers.selected
				);
			case 'career':
				return careers.ownerOf(course.code) === filter.careerId;
		}
	});
	const groups = groupEditions(visible);
	const rank = new Map(
		[careers.selected, ...options.otherCareers.map((career) => career.id)].map((id, index) => [
			id,
			index
		])
	);
	const byYear = (group: CourseGroup) =>
		latestEdition(group).code.courseYear ?? Number.MAX_SAFE_INTEGER;
	const byCareer = (group: CourseGroup) => {
		const owner = careers.ownerOf(latestEdition(group).code);
		return (owner === null ? undefined : rank.get(owner)) ?? Number.MAX_SAFE_INTEGER;
	};
	switch (filter.kind) {
		case 'all':
			return {
				filter,
				options,
				groups: groups.toSorted((a, b) => byCareer(a) - byCareer(b) || byYear(a) - byYear(b))
			};
		case 'career':
			return { filter, options, groups: groups.toSorted((a, b) => byYear(a) - byYear(b)) };
		default:
			return { filter, options, groups };
	}
}

/** Course groups of one academic year, or of none (`year` null: cross-year spaces). */
export interface PeriodSection {
	/** The September the year starts. */
	year: number | null;
	/** "2025/26"; null for cross-year spaces. */
	label: string | null;
	groups: CourseGroup[];
}

/**
 * Splits the list by its latest editions' academic year, newest first, cross-year spaces last;
 * each section keeps the list's order.
 */
export function sectionsByPeriod(groups: readonly CourseGroup[]): PeriodSection[] {
	const sections = new Map<number | null, PeriodSection>();
	for (const group of groups) {
		const { code } = latestEdition(group);
		const section = sections.get(code.academicYear);
		if (section) section.groups.push(group);
		else
			sections.set(code.academicYear, {
				year: code.academicYear,
				label: periodLabel({ ...code, stream: null }),
				groups: [group]
			});
	}
	return [...sections.values()].toSorted((a, b) => (b.year ?? NO_YEAR) - (a.year ?? NO_YEAR));
}
