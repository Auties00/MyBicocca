import type {
	MoodleQuiz,
	MoodleQuizGetCombinedReviewOptionsResponse
} from '@mybicocca/data-api/elearning';
import { query, type Query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { combinedOverallFeedback, type CombinedOverallFeedback } from './model.ts';
import { combinedReviewQuery } from './queries.ts';

type FeedbackQuiz = Pick<MoodleQuiz, 'id' | 'hasFeedback' | 'reviewOverallFeedback'>;

/**
 * Moodle's overall-feedback option over the student's attempts, for `overallFeedbackShown`. It is
 * asked only for a quiz with feedback that the list gives no review settings for. Call it while a
 * component initializes.
 */
export class CombinedFeedback {
	readonly #quiz: () => FeedbackQuiz | undefined;
	readonly #options: Query<MoodleQuizGetCombinedReviewOptionsResponse>;

	constructor(quiz: () => FeedbackQuiz | undefined) {
		this.#quiz = quiz;
		this.#options = query(() => {
			const scope = session.current;
			const current = quiz();
			return (
				scope &&
				current?.hasFeedback === 1 &&
				current.reviewOverallFeedback === null &&
				combinedReviewQuery(scope, current.id)
			);
		});
	}

	get value(): CombinedOverallFeedback | undefined {
		return this.#options.data && combinedOverallFeedback(this.#options.data);
	}

	/** Nothing left to wait for: the quiz states its settings, or Moodle answered (or failed to). */
	get ready(): boolean {
		const quiz = this.#quiz();
		if (quiz === undefined) return false;
		if (quiz.hasFeedback !== 1 || quiz.reviewOverallFeedback !== null) return true;
		return this.#options.data !== undefined || this.#options.status === 'error';
	}
}
