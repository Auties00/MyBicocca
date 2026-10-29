import IconClipboard from '~icons/ph/clipboard-text';
import IconExam from '~icons/ph/exam';
import IconGraduationCap from '~icons/ph/graduation-cap';
import IconMagnifyingGlass from '~icons/ph/magnifying-glass';
import { foldQuery } from '#lib/features/elearning/search/fold.ts';
import { periodLabel } from '#lib/features/elearning/courseCode.ts';
import type { EnrolledCourse } from '#lib/features/elearning/courses.ts';
import type { CourseDeadline } from '#lib/features/elearning/deadlines.ts';
import { catalogHref, courseHref, deadlineHref } from '#lib/features/elearning/links.ts';
import { formatDate, formatTime } from '#lib/format/dates.ts';
import { m } from '#lib/paraglide/messages.js';
import type { PaletteItem } from '#lib/shell/palette.svelte.ts';
import { category } from '../categories.ts';
import { aliasList, searchDoc, type SearchDoc } from '../rank.ts';

/**
 * The enrolled courses (hidden ones out), titled as the course list titles them and found by their
 * other names and idnumber too.
 */
export function courseDocs(courses: readonly EnrolledCourse[]): SearchDoc[] {
	return courses
		.filter((course) => !course.hidden)
		.map((course) =>
			searchDoc(
				{
					id: `course:${course.id}`,
					title: course.fullName,
					subtitle: [m.shell_elearning(), periodLabel(course.code)]
						.filter((part) => part !== null)
						.join(' · '),
					icon: IconGraduationCap,
					href: courseHref(course.id),
					...category('courses')
				},
				[course.shortName, course.displayName, course.idNumber]
			)
		);
}

/** Moodle keeps offering activities left undone years ago; past this, they are out. */
const OVERDUE_FOR = 30 * 24 * 60 * 60_000;

/**
 * The assignments and quizzes with a due date, those overdue for up to a month included (with
 * their year), found by their course and their kind ("compiti"), and by either joined to their
 * course: "compiti analisi", "consegna 2 analisi".
 */
export function assignmentDocs(
	deadlines: readonly CourseDeadline[],
	courses: readonly EnrolledCourse[],
	today: Date
): SearchDoc[] {
	const names = new Map(courses.map((course) => [course.id, course.fullName]));
	const since = today.getTime() - OVERDUE_FOR;
	return deadlines
		.filter((deadline) => deadline.dueAt >= since)
		.map((deadline) => {
			const course = names.get(deadline.courseId);
			const due =
				deadline.dueAt < today.getTime()
					? m.search_overdue({ date: formatDate(deadline.dueAt, 'medium') })
					: m.search_due({
							date: formatDate(deadline.dueAt, 'weekdayDayMonth'),
							time: formatTime(deadline.dueAt)
						});
			const assignment = deadline.kind === 'assign';
			const kinds = aliasList(assignment ? m.search_assignment_aliases() : m.search_quiz_aliases());
			return searchDoc(
				{
					id: `${assignment ? 'assignment' : 'quiz'}:${deadline.instanceId}`,
					title: deadline.title,
					subtitle: course ? `${course} · ${due}` : due,
					icon: assignment ? IconClipboard : IconExam,
					href: deadlineHref(deadline),
					...category(assignment ? 'assignments' : 'quizzes')
				},
				[
					course,
					...kinds,
					...(course
						? [`${deadline.title} ${course}`, ...kinds.map((kind) => `${kind} ${course}`)]
						: [])
				]
			);
		});
}

/** The same words in the course catalog: offered under any query of three letters or more. */
export function catalogRow(query: string): PaletteItem {
	return {
		id: `catalog:${foldQuery(query)}`,
		title: m.search_catalog_title({ query }),
		subtitle: m.search_catalog_subtitle(),
		icon: IconMagnifyingGlass,
		href: catalogHref(undefined, query),
		fallback: true,
		...category('elsewhere')
	};
}
