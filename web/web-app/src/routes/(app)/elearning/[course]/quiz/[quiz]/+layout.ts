import { error } from '@sveltejs/kit';
import type { LayoutLoad } from './$types';

export const load: LayoutLoad = ({ params }) => {
	const quizId = Number(params.quiz);
	if (!Number.isSafeInteger(quizId) || quizId <= 0) error(404, 'Quiz not found');
	return { quizId };
};
