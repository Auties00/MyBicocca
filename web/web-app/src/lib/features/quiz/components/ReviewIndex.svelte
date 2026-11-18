<script lang="ts">
	import IconCheck from '~icons/ph/check-bold';
	import IconCircleHalf from '~icons/ph/circle-half-fill';
	import IconHourglass from '~icons/ph/hourglass-medium-fill';
	import IconX from '~icons/ph/x-bold';
	import { m } from '#lib/paraglide/messages.js';
	import { verdictLabels } from '../format.ts';
	import { verdictOf } from '../model.ts';
	import type { Question } from '../questions.ts';

	interface Props {
		questions: readonly Question[];
	}

	let { questions }: Props = $props();

	/* Each verdict has its glyph too: colour is never the only cue. */
	const GLYPHS = {
		correct: IconCheck,
		partial: IconCircleHalf,
		wrong: IconX,
		pending: IconHourglass
	} as const;

	const items = $derived(
		questions
			.filter((question) => question.number !== null && question.body.kind !== 'info')
			.map((question) => ({ question, verdict: verdictOf(question.state) }))
	);
</script>

<ol class="tiles">
	{#each items as { question, verdict } (question.slot)}
		<li>
			<a
				class={['tile', verdict]}
				href="#question-{question.slot}"
				aria-label={[
					m.quiz_question({ number: question.number ?? '' }),
					verdict && verdictLabels[verdict]()
				]
					.filter(Boolean)
					.join(', ')}
			>
				{question.number}
				{#if verdict && verdict !== 'unanswered'}
					{@const Glyph = GLYPHS[verdict]}
					<span class="glyph" aria-hidden="true"><Glyph /></span>
				{/if}
			</a>
		</li>
	{/each}
</ol>

<style>
	.tiles {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(44px, 1fr));
		gap: 6px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.tile {
		position: relative;
		display: grid;
		place-items: center;
		aspect-ratio: 1;
		max-inline-size: 52px;
		border-radius: var(--r-sm);
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline-strong);
		color: var(--text);
		font: var(--type-ui-strong);
		font-size: 0.9375rem;
		line-height: 1;
		font-variant-numeric: tabular-nums;
		text-decoration: none;
		transition: transform var(--dur-instant) var(--ease-out);
	}

	.glyph {
		position: absolute;
		inset: -4px -4px auto auto;
		display: grid;
		place-items: center;
		inline-size: 16px;
		block-size: 16px;
		border-radius: 50%;
		background: currentColor;
	}

	.glyph :global(svg) {
		inline-size: 10px;
		block-size: 10px;
		color: var(--surface);
	}

	.tile:active {
		transform: scale(0.94);
	}

	.correct {
		background: var(--success-container);
		box-shadow: inset 0 0 0 1.5px var(--success);
		color: var(--on-success-container);
	}

	.partial {
		background: var(--warning-container);
		box-shadow: inset 0 0 0 1.5px var(--warning);
		color: var(--on-warning-container);
	}

	.wrong {
		background: var(--danger-container);
		box-shadow: inset 0 0 0 1.5px var(--danger);
		color: var(--on-danger-container);
	}

	.pending {
		background: var(--info-container);
		box-shadow: inset 0 0 0 1.5px var(--info);
		color: var(--on-info-container);
	}
</style>
