import { error } from '@sveltejs/kit';
import { prefetch } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import {
	courseByIdQuery,
	courseContentsQuery,
	courseInfoQuery
} from '#lib/features/course/queries.ts';
import type { LayoutLoad } from './$types';

/** Hovering a course link already starts loading the course, so the page opens with it. */
export const load: LayoutLoad = ({ params, route }) => {
	const courseId = Number(params.course);
	if (!Number.isSafeInteger(courseId) || courseId <= 0) error(404, 'Course not found');
	const scope = session.current;
	if (scope !== undefined) {
		void prefetch(courseContentsQuery(scope, courseId));
		void prefetch(courseByIdQuery(scope, courseId));
		if (route.id === '/(app)/elearning/[course]') void prefetch(courseInfoQuery(scope, courseId));
	}
	return { courseId };
};
