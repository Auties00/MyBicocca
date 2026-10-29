import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';
import type { CourseFile } from './content.ts';

/** A page of a course: `coursePath(5101, 'quiz', 42)` is `/elearning/5101/quiz/42`. */
export function coursePath(courseId: number, ...segments: (string | number)[]): ResolvedPathname {
	return resolve(`elearning/${[courseId, ...segments].join('/')}`);
}

function withQuery(path: ResolvedPathname, params: Record<string, string | undefined>) {
	const query = new URLSearchParams();
	for (const [name, value] of Object.entries(params)) if (value) query.set(name, value);
	return `${path}?${query}` as ResolvedPathname;
}

/** The activity's row on the Content tab, which opens its section and scrolls to it. */
export function moduleAnchor(courseId: number, cmId: number): ResolvedPathname {
	return `${coursePath(courseId, 'content')}#cm-${cmId}` as ResolvedPathname;
}

/** Where a course page's file opens: located again from the course's contents, on reload too. */
export function moduleFileHref(
	courseId: number,
	cmId: number,
	file: Pick<CourseFile, 'name' | 'folder'>,
	entry?: string
): ResolvedPathname {
	return withQuery(coursePath(courseId, 'file'), {
		cm: String(cmId),
		name: file.name,
		dir: file.folder === '/' ? undefined : file.folder,
		entry
	});
}

/**
 * Where any other Moodle file opens (a forum or assignment attachment): its address without the
 * token, which the viewer adds to the request itself.
 */
export function attachmentHref(
	courseId: number,
	file: CourseFile,
	entry?: string
): ResolvedPathname {
	return withQuery(coursePath(courseId, 'file'), {
		url: file.url,
		name: file.name,
		mime: file.mime ?? undefined,
		size: file.size === null ? undefined : String(file.size),
		entry
	});
}
