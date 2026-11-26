import { redirect } from '@sveltejs/kit';
import { resolve } from '$app/paths';
import { prefetchPage } from '#lib/data/prefetchPage.ts';
import { bookingsQuery, examCallsQuery } from '#lib/features/exams/queries.ts';
import type { PageLoad } from './$types';

const opening = prefetchPage((scope) => [bookingsQuery(scope), examCallsQuery(scope)]);

/**
 * `?course=` belongs to the booking page: links that carry it land there. Otherwise hovering the
 * link starts loading the bookings and the open calls.
 */
export const load: PageLoad = ({ url, parent }) => {
	const course = url.searchParams.get('course');
	if (course) {
		redirect(307, `${resolve('services/exams/book')}?course=${encodeURIComponent(course)}`);
	}
	return opening({ parent });
};
