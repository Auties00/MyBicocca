<script lang="ts">
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Stat from '#lib/ui/Stat.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import type { AcademicTitle } from '../degrees.ts';
	import { categoryLabel, heroYearLabel, titleStatusLabel } from '../degreeView.ts';

	interface Props {
		title: AcademicTitle;
	}

	let { title }: Props = $props();

	const status = $derived(titleStatusLabel(title.status));
	const grade = $derived(title.grade);
	const decimals = $derived(grade === null || Number.isInteger(grade.value) ? 0 : 1);
</script>

<!-- The grade on its scale, the serif moment of the page, with where the title stands and when. -->
<section class={['hero', { honours: title.cumLaude }]} aria-label={categoryLabel(title.category)}>
	<p class="overline">{categoryLabel(title.category)}</p>
	{#if grade !== null || title.gradeText !== null}
		<Stat
			label={m.documents_degrees_field_grade()}
			value={grade?.value ?? title.gradeText ?? ''}
			{decimals}
			unit={grade?.base ? `/ ${formatNumber(grade.base)}` : undefined}
			caption={title.cumLaude ? m.documents_degrees_cum_laude() : undefined}
			tone={title.cumLaude ? 'gold' : 'default'}
			size="xl"
		/>
	{/if}
	<div class="facts">
		{#if status}
			<StatusChip status={title.status === 'awarded' ? 'success' : 'pending'} label={status} />
		{/if}
		{#if title.year}
			<span class="year"
				>{heroYearLabel(title.category)} <span class="value">{title.year}</span></span
			>
		{/if}
	</div>
</section>

<style>
	.hero {
		display: grid;
		gap: var(--sp-3);
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.honours {
		background: linear-gradient(160deg, var(--gold-container), var(--surface) 70%);
	}

	.overline {
		color: var(--accent-text);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.facts {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 6px 12px;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.value {
		color: var(--text);
		font-variant-numeric: tabular-nums;
	}

	@supports (corner-shape: squircle) {
		.hero {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}
</style>
