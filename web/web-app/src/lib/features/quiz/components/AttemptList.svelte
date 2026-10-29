<script lang="ts">
	import type { MoodleQuiz, MoodleQuizAttempt } from '@mybicocca/data-api/elearning';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import { attemptStateLabel, formatInstant, formatScore } from '../format.ts';
	import { attemptPath, reviewPath } from '../links.ts';
	import { isOpenAttempt, ownAttempts, scaledGrade } from '../model.ts';

	interface Props {
		quiz: MoodleQuiz;
		attempts: readonly MoodleQuizAttempt[];
		courseId: number;
		/** Moodle lets the student review finished attempts. */
		reviewable: boolean;
		/** Opening an attempt or a review needs the network. */
		online: boolean;
	}

	let { quiz, attempts, courseId, reviewable, online }: Props = $props();

	const rows = $derived(
		ownAttempts(attempts).toSorted((a, b) => (b.attempt ?? 0) - (a.attempt ?? 0))
	);

	function href(attempt: MoodleQuizAttempt) {
		if (!online || attempt.id === null) return undefined;
		if (isOpenAttempt(attempt)) {
			return attemptPath(courseId, quiz.id, attempt.id, attempt.currentPage ?? 0);
		}
		return reviewable ? reviewPath(courseId, quiz.id, attempt.id) : undefined;
	}

	function grade(attempt: MoodleQuizAttempt): string | undefined {
		if (attempt.state !== 'finished') return undefined;
		const value = scaledGrade(attempt.sumGrades, quiz);
		if (value === null) return m.quiz_attempt_not_graded();
		return quiz.grade === null
			? formatScore(value, quiz)
			: m.quiz_score_of({ score: formatScore(value, quiz), max: formatScore(quiz.grade, quiz) });
	}

	function when(attempt: MoodleQuizAttempt): string {
		const seconds = attempt.timeFinish || attempt.timeStart;
		const state = attemptStateLabel(attempt.state);
		return seconds ? `${state} · ${formatInstant(new Date(seconds * 1000))}` : state;
	}
</script>

<ListGroup title={m.quiz_attempts_title()} level={2}>
	{#each rows as attempt (attempt.id)}
		<ListRow
			title={m.quiz_attempt_number({ number: attempt.attempt ?? 0 })}
			description={when(attempt)}
			meta={grade(attempt)}
			href={href(attempt)}
			lines={1}
		>
			{#snippet leading()}
				<span class={['number', { open: isOpenAttempt(attempt) }]} aria-hidden="true">
					{formatNumber(attempt.attempt ?? 0)}
				</span>
			{/snippet}
		</ListRow>
	{/each}
</ListGroup>

<style>
	.number {
		display: grid;
		place-items: center;
		inline-size: 36px;
		block-size: 36px;
		border-radius: 50%;
		background: var(--course-container);
		color: var(--course-ink);
		font: var(--type-ui-strong);
		font-size: 0.9375rem;
		line-height: 1;
		font-variant-numeric: tabular-nums;
	}

	.open {
		background: var(--course-bar);
		color: var(--surface);
	}
</style>
