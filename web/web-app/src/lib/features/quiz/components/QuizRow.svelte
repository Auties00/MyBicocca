<script lang="ts">
	import IconCheck from '~icons/ph/check-bold';
	import IconClock from '~icons/ph/clock';
	import IconExam from '~icons/ph/exam';
	import IconLock from '~icons/ph/lock-simple';
	import { displayName } from '#lib/features/course/content.ts';
	import { shapePath } from '#lib/features/elearning/shapes.ts';
	import ListRow from '#lib/ui/ListRow.svelte';
	import { quizStatus } from '../format.ts';
	import { quizPath } from '../links.ts';
	import { isCompleted, quizState, type QuizItem } from '../model.ts';

	interface Props {
		item: QuizItem;
		courseId: number;
		language: string;
		now: Date;
	}

	let { item, courseId, language, now }: Props = $props();

	const state = $derived(quizState(item, now));
	const done = $derived(isCompleted(item.module));
	const name = $derived(displayName(item.quiz.name, language) || item.quiz.name);
	/* Spaced in the text itself, so the parts read apart to screen readers too. */
	const SEPARATOR = ' · ';
	const status = $derived(quizStatus(item, now));
	const Glyph = $derived(
		state === 'upcoming'
			? IconClock
			: state === 'closed' || state === 'restricted'
				? IconLock
				: state === 'completed'
					? IconCheck
					: IconExam
	);
	const shape = $derived(shapePath(done ? 'cookie' : 'puffy'));
</script>

<ListRow
	href={quizPath(courseId, item.quiz.id)}
	title={name}
	muted={state === 'closed' || state === 'restricted' || state === 'upcoming'}
>
	{#snippet leading()}
		<span class={['badge', state]} aria-hidden="true">
			<svg class="shape" viewBox="0 0 100 100"><path d={shape} /></svg>
			<Glyph class="glyph" />
		</span>
	{/snippet}
	{#snippet description()}
		{#each status as part, index (index)}
			<span>{index > 0 ? SEPARATOR : ''}<span class={part.tone}>{part.text}</span></span>
		{/each}
	{/snippet}
</ListRow>

<style>
	.badge {
		position: relative;
		display: grid;
		place-items: center;
		inline-size: 42px;
		block-size: 42px;
	}

	.shape {
		position: absolute;
		inset: 0;
		fill: var(--course-container);
		transition: transform var(--dur-spring) var(--spring-pop);
	}

	@media (hover: hover) {
		:global([data-row-link]:hover) .badge .shape {
			transform: rotate(30deg);
		}
	}

	.badge :global(.glyph) {
		position: relative;
		inline-size: var(--icon);
		block-size: var(--icon);
		color: var(--course-ink);
	}

	.completed .shape {
		fill: var(--course-bar);
	}

	.completed :global(.glyph) {
		color: var(--surface);
	}

	:is(.closed, .restricted, .upcoming) .shape {
		fill: var(--surface-sunken);
	}

	:is(.closed, .restricted, .upcoming) :global(.glyph) {
		color: var(--text-muted);
	}

	.warning {
		color: var(--warning);
		font-weight: 550;
	}

	.danger {
		color: var(--danger);
		font-weight: 550;
	}

	@media (prefers-reduced-motion: reduce) {
		.shape {
			transition: none;
		}
	}
</style>
