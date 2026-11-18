import { redirect } from '@sveltejs/kit';
import { resolve } from '$app/paths';
import type { PageLoad } from './$types';

/** `?course=` belongs to the booking page: links that carry it land there. */
export const load: PageLoad = ({ url }) => {
	const course = url.searchParams.get('course');
	if (course) {
		redirect(307, `${resolve('services/exams/book')}?course=${encodeURIComponent(course)}`);
	}
};
