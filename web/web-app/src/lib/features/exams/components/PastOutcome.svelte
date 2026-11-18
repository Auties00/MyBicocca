<script lang="ts">
	import { formatDate } from '#lib/format/dates.ts';
	import { formatGradeValue } from '#lib/format/grades.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { resultHref } from '#lib/features/results/format.ts';
	import { shellHref } from '#lib/shell/nav.ts';
	import Button from '#lib/ui/Button.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import { outcomeStatus } from '../format.ts';
	import type { Booking } from '../model.ts';

	interface Props {
		booking: Booking;
	}

	let { booking }: Props = $props();

	const result = $derived(outcomeStatus(booking));
	/* A published outcome has its own page among the results, keyed as the results list keys it. */
	const results = $derived(
		booking.published
			? resultHref(
					booking.applicationListId === null ? booking.slug : String(booking.applicationListId)
				)
			: shellHref('services/results')
	);
	const numeral = $derived(
		booking.published && booking.grade.kind === 'numeric' ? booking.grade : undefined
	);
</script>

<section class="outcome" aria-label={m.exams_fact_outcome()}>
	<p class="label">{m.exams_fact_outcome()}</p>
	<div class="result">
		{#if numeral}
			<p class={['grade', { gold: numeral.cumLaude }]}>
				<span aria-hidden="true">{formatGradeValue(numeral)}</span>
				<span class="sr-only">{result.label}</span>
			</p>
			<StatusChip status={result.status} label={m.exams_outcome_passed()} />
		{:else}
			<StatusChip status={result.status} label={result.label} />
		{/if}
	</div>
	<p class="when">
		{booking.at ? m.exams_taken_on({ date: formatDate(booking.at, 'long') }) : m.exams_time_tbc()}
	</p>
	{#if booking.publishedNote}
		<blockquote class="note">
			<p class="note-label">{m.exams_published_note()}</p>
			<p class="note-text">{booking.publishedNote}</p>
		</blockquote>
	{:else if !booking.published}
		<p class="pending">{m.exams_result_pending()}</p>
	{/if}
	<div class="link">
		<Button variant="ghost" size="sm" href={results}>
			{booking.published ? m.exams_see_result() : m.exams_see_results()}
		</Button>
	</div>
</section>

<style>
	.outcome {
		display: grid;
		justify-items: start;
		gap: var(--sp-3);
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.label {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.result {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: var(--sp-4);
	}

	.grade {
		font: var(--type-numeral-xl);
		letter-spacing: var(--track-numeral);
		font-variant-numeric: lining-nums tabular-nums;
	}

	.grade.gold {
		color: var(--gold);
	}

	.when,
	.pending {
		color: var(--text-muted);
	}

	.note {
		display: grid;
		gap: 4px;
		inline-size: 100%;
		margin: 0;
		padding: var(--sp-3) var(--sp-4);
		border-inline-start: 3px solid var(--accent);
		border-radius: var(--r-2xs) var(--r-sm) var(--r-sm) var(--r-2xs);
		background: var(--surface-sunken);
	}

	.note-label {
		color: var(--text-muted);
		font: var(--type-label);
	}

	.note-text {
		white-space: pre-line;
		overflow-wrap: anywhere;
	}

	.link {
		margin-inline-start: -12px;
	}

	@supports (corner-shape: squircle) {
		.outcome {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}
</style>
