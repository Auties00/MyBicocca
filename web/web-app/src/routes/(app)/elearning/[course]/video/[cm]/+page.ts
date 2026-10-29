import { error } from '@sveltejs/kit';
import type { PageLoad } from './$types';

export const load: PageLoad = ({ params }) => {
	const cmId = Number(params.cm);
	if (!Number.isSafeInteger(cmId) || cmId <= 0) error(404, 'Video not found');
	return { cmId };
};
