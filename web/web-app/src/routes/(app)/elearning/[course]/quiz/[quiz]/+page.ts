import { prefetch } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import {
	accessQuery,
	attemptsQuery,
	bestGradeQuery,
	courseQuizzesQuery
} from '#lib/features/quiz/queries.ts';
import type { PageLoad } from './$types';

/** Hovering a quiz's link already loads what its page shows. */
export const load: PageLoad = async ({ parent }) => {
	const { courseId, quizId } = await parent();
	const scope = session.current;
	if (scope !== undefined) {
		void prefetch(courseQuizzesQuery(scope, courseId));
		void prefetch(attemptsQuery(scope, quizId));
		void prefetch(bestGradeQuery(scope, quizId));
		void prefetch(accessQuery(scope, quizId));
	}
};
