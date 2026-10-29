<script lang="ts">
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import TeacherHtml from '#lib/features/course/components/TeacherHtml.svelte';
	import { isRenderableHtml } from '#lib/format/html.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { gradeFeedbackQuery } from '../queries.ts';

	interface Props {
		quizId: number;
		/** On the quiz's scale. */
		grade: number;
	}

	let { quizId, grade }: Props = $props();

	const feedback = query(
		() => session.current && gradeFeedbackQuery(session.current, quizId, grade)
	);
	const text = $derived(
		feedback.data && isRenderableHtml(feedback.data.feedbacktext)
			? feedback.data.feedbacktext
			: null
	);
	/* A quiz with feedback almost always has some for every band: its place is held meanwhile. */
	const loading = $derived(feedback.data === undefined && feedback.status === 'pending');
</script>

{#if text !== null || loading}
	<div class="feedback" aria-busy={loading}>
		<p class="label">{m.quiz_overall_feedback()}</p>
		{#if text !== null}
			<TeacherHtml html={text} size="sm" />
		{:else}
			<Skeleton variant="text" lines={2} />
		{/if}
	</div>
{/if}

<style>
	.feedback {
		display: grid;
		gap: 6px;
		padding-block-start: 14px;
		border-block-start: 1px solid color-mix(in oklch, var(--course-bar) 24%, transparent);
	}

	.label {
		color: var(--course-ink);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}
</style>
