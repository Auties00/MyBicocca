import { error } from '@sveltejs/kit';
import type { LayoutLoad } from './$types';

export const load: LayoutLoad = ({ params }) => {
	const assignId = Number(params.assign);
	if (!Number.isSafeInteger(assignId) || assignId <= 0) error(404, 'Assignment not found');
	return { assignId };
};
