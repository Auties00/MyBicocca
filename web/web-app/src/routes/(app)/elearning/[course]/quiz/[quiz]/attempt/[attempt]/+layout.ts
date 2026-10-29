import { error } from '@sveltejs/kit';
import type { LayoutLoad } from './$types';

export const load: LayoutLoad = ({ params }) => {
	const attemptId = Number(params.attempt);
	if (!Number.isSafeInteger(attemptId) || attemptId <= 0) error(404, 'Attempt not found');
	return { attemptId };
};
