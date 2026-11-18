<script lang="ts">
	import IconCaretDown from '~icons/ph/caret-down';
	import IconCheck from '~icons/ph/check-bold';
	import IconSealCheck from '~icons/ph/seal-check';
	import { m } from '#lib/paraglide/messages.js';
	import type { Compilation } from '../compilation.svelte.ts';
	import { isAnswered, NO_ANSWER, type Answers, type SurveyPage } from '../page.ts';
	import BeforeYouStart from './BeforeYouStart.svelte';

	interface Props {
		compilation: Compilation;
		online: boolean;
		/** Back to an earlier page, through Esse3 one page at a time. */
		onrevisit: (index: number) => void;
	}

	let { compilation, online, onrevisit }: Props = $props();

	const step = $derived(compilation.step);
	const onSummary = $derived(step.kind === 'summary');
	/* On the summary every visited page lies behind; on a page, those after it were seen before
	 * going back, and may change with the answers. */
	const current = $derived(step.kind === 'page' ? step.index : compilation.visited.length);

	function answered(page: SurveyPage, answers: Answers): number {
		return page.questions.filter((question) =>
			isAnswered(question, answers[question.id] ?? NO_ANSWER)
		).length;
	}

	const pages = $derived.by(() => {
		const count = Math.max(compilation.visited.length, step.kind === 'page' ? step.index + 1 : 0);
		return Array.from({ length: count }, (_, index) => {
			const live = step.kind === 'page' && step.index === index;
			const entry = live
				? { page: step.page, answers: compilation.answers }
				: compilation.visited[index];
			return {
				index,
				answered: entry ? answered(entry.page, entry.answers) : 0,
				total: entry?.page.questions.length ?? 0,
				state: index < current ? 'behind' : live ? 'current' : 'ahead'
			};
		});
	});
	const locked = $derived(compilation.working !== null || !online);
</script>

<aside class="rail" aria-labelledby="questionnaire-progress">
	<section class="card">
		<h2 class="heading" id="questionnaire-progress">{m.questionnaires_progress_heading()}</h2>
		<ol class="pages" role="list">
			{#each pages as item (item.index)}
				{@const label = m.questionnaires_page_number({ number: item.index + 1 })}
				{@const count = m.questionnaires_answered_count({
					answered: item.answered,
					total: item.total
				})}
				<li class={['item', item.state]}>
					{#if item.state === 'behind'}
						<button
							type="button"
							class="entry"
							disabled={locked}
							aria-label={`${m.questionnaires_progress_back({ number: item.index + 1 })}, ${count}`}
							onclick={() => onrevisit(item.index)}
						>
							<span class="marker" aria-hidden="true"><IconCheck /></span>
							<span class="words">
								<span class="name">{label}</span>
								<span class="count">{count}</span>
							</span>
						</button>
					{:else}
						<div class="entry" aria-current={item.state === 'current' ? 'step' : undefined}>
							<span class="marker" aria-hidden="true">{item.index + 1}</span>
							<span class="words">
								<span class="name">{label}</span>
								<span class="count">
									{item.state === 'current'
										? `${m.questionnaires_progress_current()} · ${count}`
										: count}
								</span>
							</span>
						</div>
					{/if}
				</li>
			{/each}
			<li class={['item', 'end', { current: onSummary }]}>
				<div class="entry" aria-current={onSummary ? 'step' : undefined}>
					<span class="marker" aria-hidden="true"><IconSealCheck /></span>
					<span class="words"><span class="name">{m.questionnaires_summary_title()}</span></span>
				</div>
			</li>
		</ol>
	</section>

	<!-- The notes the student read before starting, out of the way while answering. -->
	<details class="card notes">
		<summary>
			<span>{m.questionnaires_rail_facts()}</span>
			<IconCaretDown class="caret" aria-hidden="true" />
		</summary>
		<div class="notes-body">
			<BeforeYouStart anonymous={compilation.target.anonymous} bare />
		</div>
	</details>
</aside>

<style>
	.rail {
		position: sticky;
		inset-block-start: calc(var(--shell-inset-top) + var(--sp-6));
		display: grid;
		gap: var(--sp-4);
	}

	.card {
		display: grid;
		gap: var(--sp-3);
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.heading {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.pages {
		display: grid;
		margin: 0 -8px;
		padding: 0;
		list-style: none;
	}

	.item {
		position: relative;
	}

	/* The thread joining the pages, behind their markers. */
	.item:not(:last-child)::after {
		content: '';
		position: absolute;
		inset-block: 34px -2px;
		inset-inline-start: 21px;
		inline-size: 2px;
		border-radius: 1px;
		background: var(--hairline-strong);
	}

	.entry {
		display: flex;
		align-items: center;
		gap: var(--sp-3);
		inline-size: 100%;
		padding: 6px 8px;
		border: 0;
		border-radius: var(--r-md);
		background: none;
		color: inherit;
		font: inherit;
		text-align: start;
	}

	button.entry {
		cursor: pointer;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		button.entry:hover:not(:disabled) {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	button.entry:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 0;
	}

	button.entry:disabled {
		cursor: default;
	}

	.marker {
		position: relative;
		z-index: 1;
		display: grid;
		flex: none;
		place-items: center;
		inline-size: 28px;
		block-size: 28px;
		border-radius: 50%;
		background: var(--surface-sunken);
		color: var(--text-muted);
		font: var(--type-label);
		font-variant-numeric: tabular-nums;
	}

	.marker :global(svg) {
		inline-size: 14px;
		block-size: 14px;
	}

	.behind .marker {
		background: var(--container);
		color: var(--on-container);
	}

	.current .marker {
		background: var(--accent);
		color: var(--on-accent);
	}

	.words {
		display: grid;
		min-inline-size: 0;
	}

	.name {
		font: var(--type-ui-strong);
	}

	.ahead .name,
	.end:not(.current) .name {
		color: var(--text-muted);
	}

	.count {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.notes {
		gap: 0;
		padding: 0;
	}

	.notes summary {
		display: flex;
		align-items: center;
		gap: var(--sp-2);
		min-block-size: var(--hit-min);
		padding: 12px var(--sp-5);
		border-radius: inherit;
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
		list-style: none;
		cursor: pointer;
	}

	.notes summary::-webkit-details-marker {
		display: none;
	}

	.notes summary:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.notes :global(.caret) {
		margin-inline-start: auto;
		inline-size: 16px;
		block-size: 16px;
		transition: transform var(--dur-base) var(--ease-out);
	}

	.notes[open] :global(.caret) {
		transform: rotate(180deg);
	}

	.notes[open] summary {
		padding-block-end: var(--sp-3);
	}

	.notes-body {
		padding: 0 var(--sp-5) var(--sp-5);
	}
</style>
