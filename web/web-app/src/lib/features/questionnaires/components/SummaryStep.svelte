<script lang="ts">
	import IconCaretDown from '~icons/ph/caret-down';
	import IconSealCheck from '~icons/ph/seal-check';
	import IconWarning from '~icons/ph/warning';
	import { m } from '#lib/paraglide/messages.js';
	import type { VisitedPage } from '../compilation.svelte.ts';
	import { NO_ANSWER, type Question } from '../page.ts';
	import { BADGE } from '../shapes.ts';

	interface Props {
		complete: boolean;
		anonymous: boolean;
		visited: readonly VisitedPage[];
	}

	let { complete, anonymous, visited }: Props = $props();

	const help = $derived(
		!complete
			? m.questionnaires_summary_incomplete_help()
			: anonymous
				? m.questionnaires_summary_anonymous_help()
				: m.questionnaires_summary_help()
	);

	const count = $derived(visited.reduce((sum, entry) => sum + entry.page.questions.length, 0));

	function answerText(question: Question, entry: VisitedPage): string {
		const answer = entry.answers[question.id] ?? NO_ANSWER;
		if (question.kind.type === 'text') return answer.text.trim() || m.questionnaires_review_none();
		const words = question.options
			.filter((option) => answer.selected.includes(option.id))
			.map((option) => {
				const label = option.text || m.questionnaires_other_option();
				const extra = option.needsText ? answer.text.trim() : '';
				return extra ? `${label}: ${extra}` : label;
			});
		return words.length > 0 ? words.join(' · ') : m.questionnaires_review_none();
	}
</script>

<section class="summary" aria-labelledby="questionnaire-summary">
	<div class={['badge', { complete }]} aria-hidden="true">
		<svg class="shape" viewBox="0 0 40 40"><path d={BADGE} /></svg>
		<span class="icon"
			>{#if complete}<IconSealCheck />{:else}<IconWarning />{/if}</span
		>
	</div>
	<h2 class="title" id="questionnaire-summary" tabindex="-1" data-step-heading>
		{complete ? m.questionnaires_summary_complete() : m.questionnaires_summary_incomplete()}
	</h2>
	<p class="help">{help}</p>
</section>

{#if count > 0}
	<details class="review">
		<summary>
			<span>{m.questionnaires_review_heading()}</span>
			<span class="count">{count}</span>
			<IconCaretDown class="caret" aria-hidden="true" />
		</summary>
		<div class="pages">
			{#each visited as entry (entry.page.id)}
				{#each entry.page.sections as section (section.id)}
					<div class="section">
						{#if section.title}<h3 class="section-title">{section.title}</h3>{/if}
						<dl class="answers">
							{#each section.questions as question (question.id)}
								<div class="pair">
									<dt>{question.text}</dt>
									<dd>{answerText(question, entry)}</dd>
								</div>
							{/each}
						</dl>
					</div>
				{/each}
			{/each}
		</div>
	</details>
{/if}

<style>
	.summary {
		display: grid;
		justify-items: center;
		gap: var(--sp-2);
		padding: var(--sp-8) var(--sp-5) var(--sp-7);
		border-radius: var(--r-xl);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
		text-align: center;
	}

	.badge {
		position: relative;
		display: grid;
		place-items: center;
		inline-size: 112px;
		block-size: 112px;
		margin-block-end: var(--sp-3);
		color: var(--on-warning-container);
		animation: arrive var(--dur-sheet) var(--spring-pop) both;
	}

	.shape {
		position: absolute;
		inset: 0;
		inline-size: 100%;
		block-size: 100%;
		fill: var(--warning-container);
	}

	.badge.complete {
		color: var(--on-container);
	}

	.complete .shape {
		fill: var(--container-press);
	}

	.icon {
		position: relative;
		display: grid;
	}

	.icon :global(svg) {
		inline-size: 48px;
		block-size: 48px;
	}

	.title {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
		outline: none;
	}

	.help {
		max-inline-size: 44ch;
		color: var(--text-muted);
		font-size: 0.9375rem;
		text-wrap: pretty;
	}

	.review {
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.review summary {
		display: flex;
		align-items: center;
		gap: var(--sp-2);
		min-block-size: var(--hit-min);
		padding: 14px var(--sp-5);
		border-radius: inherit;
		font: var(--type-ui-strong);
		list-style: none;
		cursor: pointer;
	}

	.review summary::-webkit-details-marker {
		display: none;
	}

	.count {
		padding: 1px 8px;
		border-radius: var(--r-pill);
		background: var(--surface-sunken);
		color: var(--text-muted);
		font: var(--type-label);
		font-variant-numeric: tabular-nums;
	}

	.review :global(.caret) {
		margin-inline-start: auto;
		inline-size: 18px;
		block-size: 18px;
		color: var(--text-muted);
		transition: transform var(--dur-base) var(--ease-out);
	}

	.review[open] :global(.caret) {
		transform: rotate(180deg);
	}

	.pages {
		display: grid;
		gap: var(--sp-5);
		padding: var(--sp-1) var(--sp-5) var(--sp-5);
	}

	.section {
		display: grid;
		gap: var(--sp-2);
	}

	.section-title {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.answers {
		display: grid;
		gap: 2px;
		margin: 0;
	}

	.pair {
		display: grid;
		gap: 2px;
		padding: 10px 0;
		border-block-end: 1px solid var(--hairline);
	}

	.pair:last-child {
		border-block-end: 0;
	}

	dt {
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}

	dd {
		margin: 0;
		font: var(--type-ui-strong);
		overflow-wrap: anywhere;
	}

	@container (width >= 560px) {
		.pair {
			grid-template-columns: minmax(0, 3fr) minmax(0, 2fr);
			gap: var(--sp-5);
		}
	}

	@keyframes arrive {
		from {
			opacity: 0;
			transform: scale(0.7) rotate(-20deg);
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.badge {
			animation: none;
		}
	}
</style>
