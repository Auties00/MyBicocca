<script lang="ts">
	import IconCaretRight from '~icons/ph/caret-right';
	import IconHourglass from '~icons/ph/hourglass-medium-fill';
	import { dueLabel, dueTone } from '#lib/format/due.ts';
	import Grade from '#lib/ui/Grade.svelte';
	import { decideByLine, resultHref, resultTitle, spokenGrade, takenOnLine } from '../format.ts';
	import type { ExamResult } from '../model.ts';

	interface Props {
		result: ExamResult;
		/** The open result of the list beside its detail. */
		selected?: boolean;
	}

	let { result, selected = false }: Props = $props();

	const days = $derived(Math.max(0, result.daysLeft ?? 0));
</script>

<a
	class={['card', { selected }]}
	href={resultHref(result.id)}
	aria-current={selected ? 'page' : undefined}
>
	<Grade grade={result.grade} variant="tile" size="md" spoken={false} />
	<span class="main">
		<span class="title">{resultTitle(result)}</span>
		<span class="sr-only">{spokenGrade(result.grade)}</span>
		{#if result.takenOn}
			<span class="taken">{takenOnLine(result.takenOn)}</span>
		{/if}
		<span class="due">
			<span class={['countdown', dueTone(days)]}>
				<IconHourglass aria-hidden="true" />
				{dueLabel(days)}
			</span>
			{#if result.deadline}
				<span class="by">
					{decideByLine(result.deadline)}
				</span>
			{/if}
		</span>
	</span>
	<IconCaretRight class="chevron" aria-hidden="true" />
</a>

<style>
	.card {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr) auto;
		align-items: center;
		column-gap: var(--sp-4);
		padding: var(--sp-4);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow:
			var(--elev-1),
			inset 0 0 0 1px color-mix(in oklch, var(--accent) 22%, transparent),
			var(--inner-light);
		color: var(--text);
		text-decoration: none;
		touch-action: manipulation;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			transform var(--dur-instant) var(--ease-out);
	}

	.card:focus-visible {
		outline-offset: 2px;
	}

	@media (hover: hover) {
		.card:not(.selected):hover {
			background: color-mix(in oklch, var(--container) 45%, var(--surface));
			transition-duration: 0s;
		}
	}

	.card:active {
		transform: scale(0.985);
		transition-duration: 0s;
	}

	.selected {
		background: var(--container);
		color: var(--on-container);
		box-shadow:
			inset 0 0 0 1px color-mix(in oklch, var(--accent) 35%, transparent),
			var(--inner-light);
	}

	.main {
		display: grid;
		gap: 4px;
		min-inline-size: 0;
	}

	.title {
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		overflow: hidden;
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		text-wrap: pretty;
	}

	.taken {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	.selected .taken,
	.selected .by {
		color: inherit;
		opacity: 0.8;
	}

	.due {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 4px 10px;
		margin-block-start: 4px;
	}

	.countdown {
		display: inline-flex;
		align-items: center;
		gap: 5px;
		padding: 3px 9px 3px 7px;
		border-radius: var(--r-pill);
		background: color-mix(in oklch, var(--text) 7%, transparent);
		color: var(--text-muted);
		font: var(--type-label);
		white-space: nowrap;
	}

	.countdown :global(svg) {
		inline-size: 14px;
		block-size: 14px;
	}

	.warning {
		background: var(--warning-container);
		color: var(--on-warning-container);
	}

	.danger {
		background: var(--danger-container);
		color: var(--on-danger-container);
	}

	.by {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.card :global(.chevron) {
		inline-size: 18px;
		block-size: 18px;
		color: var(--text-subtle);
	}

	.selected :global(.chevron) {
		color: inherit;
		opacity: 0.7;
	}

	/* Beside a detail the list is narrow: the name keeps the room. */
	@container results (width < 320px) {
		.card {
			column-gap: var(--sp-3);
			padding: var(--sp-3) var(--sp-3) var(--sp-3) var(--sp-3);
		}

		.title {
			font: var(--type-body-strong);
			font-size: 0.9375rem;
			line-height: 1.35;
		}

		.card :global(.chevron) {
			display: none;
		}
	}

	@supports (corner-shape: squircle) {
		.card {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.card:active {
			transform: none;
		}
	}
</style>
