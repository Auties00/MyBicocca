<script lang="ts">
	import { displayName } from '#lib/features/course/content.ts';
	import { m } from '#lib/paraglide/messages.js';
	import DateBadge from '#lib/ui/DateBadge.svelte';
	import { closingLabel } from '../format.ts';
	import { quizPath } from '../links.ts';
	import { closesAt, closingSoon, type QuizItem } from '../model.ts';

	interface Props {
		/** An open quiz with a closing date. */
		item: QuizItem;
		courseId: number;
		language: string;
		now: Date;
	}

	let { item, courseId, language, now }: Props = $props();

	const closes = $derived(closesAt(item.quiz));
	const soon = $derived(closingSoon(item.quiz, now));
</script>

{#if closes}
	<a class="next" href={quizPath(courseId, item.quiz.id)}>
		<DateBadge date={closes} size="sm" tone={soon ? 'accent' : 'neutral'} />
		<span class="text">
			<span class="overline">{m.quiz_next_closing()}</span>
			<span class="name">{displayName(item.quiz.name, language) || item.quiz.name}</span>
			<span class={['when', { soon }]}>{closingLabel(closes, now)}</span>
		</span>
	</a>
{/if}

<style>
	.next {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: center;
		gap: 12px;
		padding: 12px 14px;
		border-radius: var(--r-md);
		background: var(--surface);
		box-shadow: var(--elev-1);
		color: inherit;
		text-decoration: none;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.next:hover {
			background: var(--surface-hover);
			transition-duration: 0s;
		}
	}

	.text {
		display: grid;
		gap: 1px;
		min-inline-size: 0;
	}

	.overline {
		color: var(--course-ink);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.name {
		overflow: hidden;
		font: var(--type-ui-strong);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.when {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.soon {
		color: var(--warning);
		font-weight: 550;
	}
</style>
