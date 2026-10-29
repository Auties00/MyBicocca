import type { ResolvedPathname } from '$app/types';
import { coursePath } from '#lib/features/course/links.ts';
import type { AssignmentFilter } from './model.ts';

/** The course's Assignments tab, filtered when a filter other than all is given. */
export function assignmentsPath(courseId: number, filter?: AssignmentFilter): ResolvedPathname {
	const path = coursePath(courseId, 'assignments');
	return filter && filter !== 'all' ? (`${path}?filter=${filter}` as ResolvedPathname) : path;
}

export function assignmentPath(courseId: number, assignId: number): ResolvedPathname {
	return coursePath(courseId, 'assignment', assignId);
}

export function editorPath(courseId: number, assignId: number): ResolvedPathname {
	return coursePath(courseId, 'assignment', assignId, 'submit');
}
