<script lang="ts">
	import { formatGrade } from '#lib/format/grades.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { gradeGlyph, gradeTone } from '#lib/ui/Grade.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import { gradeInWords, lapsedLine, resultFacts, resultStatus } from '../format.ts';
	import { visibleChoice, type ExamResult } from '../model.ts';

	interface Props {
		result: ExamResult;
		/** Off while the answer just given is reported below. */
		chip?: boolean;
	}

	let { result, chip = true }: Props = $props();

	const grade = $derived(result.grade);
	/*
	 * Pending, the decision card right below says so; a failure, an absence or a pass is already
	 * the hero's word, and a grade left standing has its sentence. The chip speaks only to add.
	 */
	const status = $derived(
		chip && !result.pending && !result.lapsed && (grade.kind === 'numeric' || visibleChoice(result))
			? resultStatus(result)
			: undefined
	);
	const rejected = $derived(visibleChoice(result) === 'rejected');
	const facts = $derived(resultFacts(result));
	const id = $props.id();
</script>

<section
	class={['hero', gradeTone(grade), { split: facts.length > 1 }]}
	aria-labelledby="{id}-label"
>
	<div class="score">
		<div class="top">
			<p class="label" id="{id}-label">{m.results_grade_label()}</p>
			{#if status}<StatusChip status={status.status} label={status.label} size="sm" />{/if}
		</div>

		<p class={['grade', { rejected }]}>
			<span class="sr-only">{gradeInWords(grade)}</span>
			{#if grade.kind === 'numeric'}
				<span class="numeral" aria-hidden="true">{grade.value}</span>
				{#if grade.cumLaude}
					<span class="laude" aria-hidden="true">{m.results_cum_laude()}</span>
				{:else}
					<span class="of" aria-hidden="true">/30</span>
				{/if}
			{:else if grade.kind === 'unknown'}
				<span class="numeral" aria-hidden="true">—</span>
			{:else}
				{@const Glyph = gradeGlyph(grade.kind)}
				<span class="word" aria-hidden="true">
					<Glyph />
					{formatGrade(grade, { full: true })}
				</span>
			{/if}
		</p>

		{#if result.lapsed && result.deadline}
			<p class="lapsed">{lapsedLine(result.deadline)}</p>
		{/if}
	</div>

	{#if facts.length > 0}
		<dl class="facts">
			{#each facts as fact (fact.label)}
				<div class="fact">
					<dt>{fact.label}</dt>
					<dd>{fact.value}</dd>
				</div>
			{/each}
		</dl>
	{/if}
</section>

<style>
	.hero {
		display: grid;
		overflow: hidden;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.score {
		display: grid;
		align-content: start;
		gap: var(--sp-2);
		padding: var(--sp-5) var(--sp-5) var(--sp-4);
	}

	.gold .score {
		background: radial-gradient(
			120% 140% at 0% 0%,
			color-mix(in oklch, var(--gold-container) 90%, transparent),
			transparent 70%
		);
	}

	.top {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: var(--sp-3);
		min-block-size: 26px;
	}

	.label {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.grade {
		display: flex;
		align-items: baseline;
		gap: 6px;
		margin-block: var(--sp-1) 0;
	}

	.numeral {
		font: var(--type-numeral-xl);
		font-size: clamp(3.5rem, 2.8rem + 3cqi, 4.5rem);
		letter-spacing: var(--track-numeral);
		font-variant-numeric: lining-nums tabular-nums;
	}

	.of {
		color: var(--text-muted);
		font-family: var(--font-display);
		font-size: 1.5rem;
		font-variant-numeric: lining-nums;
	}

	.gold .numeral,
	.laude {
		color: var(--gold);
	}

	.rejected .numeral {
		color: var(--text-subtle);
		text-decoration: line-through 3px color-mix(in oklch, var(--text-muted) 60%, transparent);
	}

	.laude {
		font-family: var(--font-display);
		font-size: 1.75rem;
		font-style: italic;
	}

	.word {
		display: inline-flex;
		align-items: center;
		gap: 10px;
		font: var(--type-title-l);
		font-size: 2.25rem;
		letter-spacing: var(--track-title-l);
	}

	.word :global(svg) {
		flex: none;
		inline-size: 30px;
		block-size: 30px;
		padding: 6px;
		border-radius: var(--r-pill);
		background: var(--surface-sunken);
		color: var(--text-muted);
		box-sizing: content-box;
	}

	.success .word :global(svg) {
		background: var(--success-container);
		color: var(--on-success-container);
	}

	.danger .word :global(svg) {
		background: var(--danger-container);
		color: var(--on-danger-container);
	}

	.lapsed {
		max-inline-size: 60ch;
		color: var(--text-muted);
		text-wrap: pretty;
	}

	.facts {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
		gap: var(--sp-3) var(--sp-5);
		margin: 0;
		padding: var(--sp-4) var(--sp-5) var(--sp-5);
		border-block-start: 1px solid var(--hairline);
	}

	.fact {
		display: grid;
		align-content: start;
		gap: 2px;
		min-inline-size: 0;
	}

	dt {
		color: var(--text-muted);
		font: var(--type-label);
	}

	dd {
		margin: 0;
		font: var(--type-body-strong);
		font-size: 0.9375rem;
		overflow-wrap: anywhere;
		text-wrap: pretty;
	}

	/* With room, two facts or more stand beside the grade, in a quieter column of their own. */
	@container detail (width >= 560px) {
		.split {
			grid-template-columns: minmax(0, 1fr) minmax(220px, 0.8fr);
		}

		.split .score {
			padding-block-end: var(--sp-5);
		}

		.split .facts {
			grid-template-columns: minmax(0, 1fr);
			align-content: center;
			padding: var(--sp-5);
			border-block-start: none;
			border-inline-start: 1px solid var(--hairline);
			background: color-mix(in oklch, var(--surface-sunken) 55%, var(--surface));
		}
	}

	@supports (corner-shape: squircle) {
		.hero {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}
</style>
