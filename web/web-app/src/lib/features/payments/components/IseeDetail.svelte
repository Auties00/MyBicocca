<script lang="ts">
	import { formatCurrency, formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Facts, { type Fact } from '#lib/ui/Facts.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import { useIsee, usePaymentsView } from '../context.ts';
	import DetailSkeleton from './DetailSkeleton.svelte';
	import IseeScale from './IseeScale.svelte';
	import Missing from './Missing.svelte';

	interface Props {
		year: string;
	}

	let { year: id }: Props = $props();

	const view = usePaymentsView();

	const isee = useIsee();
	const year = $derived(isee.find(id));
	const gate = new LoadingGate(() => isee.pending);
	/* The scale names the threshold when there is one to measure against. */
	const scaled = $derived(year !== undefined && year.threshold !== null && year.threshold > 0);

	const facts: Fact[] = $derived.by(() => {
		if (year === undefined) return [];
		const rows: Array<Fact | null> = [
			year.threshold === null || scaled
				? null
				: { label: m.payments_isee_threshold(), value: formatCurrency(year.threshold) },
			year.exemption === null
				? null
				: { label: m.payments_isee_exemption(), value: year.exemption },
			year.course === null ? null : { label: m.payments_isee_course(), value: year.course },
			year.enrollmentType === null
				? null
				: { label: m.payments_isee_enrollment(), value: year.enrollmentType },
			year.courseYear === null
				? null
				: { label: m.payments_isee_course_year(), value: formatNumber(year.courseYear) }
		];
		return rows.filter((fact) => fact !== null);
	});
</script>

{#if year}
	<div class="detail">
		<section class="hero" aria-labelledby="isee-indicator-{year.year}">
			<svelte:element this={view.level} class="label" id="isee-indicator-{year.year}"
				>{m.payments_isee_indicator()}</svelte:element
			>
			<p class="value">{formatCurrency(year.isee)}</p>
			{#if scaled && year.threshold !== null}
				<IseeScale isee={year.isee} threshold={year.threshold} />
			{/if}
		</section>
		{#if facts.length > 0}
			<section class="facts" aria-labelledby="isee-details-{year.year}">
				<svelte:element this={view.level} class="label" id="isee-details-{year.year}"
					>{m.payments_isee_details()}</svelte:element
				>
				<Facts items={facts} />
			</section>
		{/if}
	</div>
{:else if isee.loaded}
	<Missing
		title={m.payments_isee_missing_title()}
		body={m.payments_isee_missing_body()}
		back={{ href: shellHref('services/isee'), label: m.payments_back_to_isee() }}
	/>
{:else if gate.visible}
	<DetailSkeleton />
{/if}

<style>
	.detail {
		display: grid;
		gap: var(--sp-5);
	}

	.hero {
		display: grid;
		gap: var(--sp-3);
		padding: var(--sp-5) var(--sp-5) var(--sp-6);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.label {
		color: var(--accent-text);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.facts .label {
		color: var(--text-muted);
	}

	.value {
		margin-block-end: var(--sp-2);
		font: var(--type-numeral-xl);
		font-size: clamp(2.5rem, 2rem + 4cqi, 3.5rem);
		letter-spacing: var(--track-numeral);
		font-variant-numeric: lining-nums tabular-nums;
	}

	.facts {
		display: grid;
		gap: var(--sp-3);
	}
</style>
