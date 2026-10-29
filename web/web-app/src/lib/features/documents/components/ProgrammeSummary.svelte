<script lang="ts">
	import { academicYearLabel } from '#lib/data/academicYear.ts';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import type { AnnualEnrollment } from '../enrollments.ts';
	import { joinParts, sentenceCase } from '../text.ts';

	interface Props {
		/** The latest year, whose programme is the current one; none while the years load. */
		latest?: AnnualEnrollment;
		/** The first year on record. */
		first?: AnnualEnrollment;
		/** Under the list on one pane: a card. */
		compact?: boolean;
	}

	let { latest, first, compact = false }: Props = $props();

	const headingId = $props.id();
	const programme = $derived(
		latest &&
			joinParts([
				latest.courseTypeDescription && sentenceCase(latest.courseTypeDescription),
				latest.degreeClassCode,
				latest.degreeClassDescription && sentenceCase(latest.degreeClassDescription)
			])
	);
</script>

<!-- The programme the latest year belongs to, and where the student stands in it. -->
<section
	class={['summary', { compact }]}
	aria-labelledby={headingId}
	aria-busy={!latest || undefined}
>
	<div class="lead">
		<h2 class="overline" id={headingId}>{m.documents_enrollments_overview_overline()}</h2>
		{#if !latest}
			<p class="course"><Skeleton variant="text" width="45%" /></p>
			<p class="programme"><Skeleton variant="text" width="75%" /></p>
		{:else}
			{#if latest.courseDescription}
				<p class="course">{sentenceCase(latest.courseDescription)}</p>
			{/if}
			{#if programme}<p class="programme">{programme}</p>{/if}
		{/if}
	</div>

	{#if !latest || !first}
		<!-- Three figures to come, in the card that will hold them. -->
		<div class="stats" aria-hidden="true">
			{#each [55, 70, 60] as width (width)}
				<div class="stat">
					<span class="label"><Skeleton variant="text" width="{width}%" /></span>
					<span class="value"><Skeleton variant="text" width="40%" /></span>
				</div>
			{/each}
		</div>
	{:else}
		<dl class="stats">
			{#if latest.courseYear > 0}
				<div class="stat">
					<dt class="label">{m.documents_enrollments_stat_year()}</dt>
					<dd class="value">
						{#if latest.courseDuration}
							{m.documents_enrollments_stat_year_value({
								year: formatNumber(latest.courseYear),
								total: formatNumber(latest.courseDuration)
							})}
						{:else}
							{formatNumber(latest.courseYear)}
						{/if}
					</dd>
				</div>
			{/if}
			{#if latest.minimumCredits}
				<div class="stat">
					<dt class="label">{m.documents_enrollments_stat_credits()}</dt>
					<dd class="value">{formatNumber(latest.minimumCredits)}</dd>
				</div>
			{/if}
			<div class="stat">
				<dt class="label">{m.documents_enrollments_stat_since()}</dt>
				<dd class="value">{academicYearLabel(first.academicYear)}</dd>
			</div>
		</dl>
	{/if}
</section>

<style>
	.summary {
		display: grid;
		gap: var(--sp-5);
		container-type: inline-size;
	}

	.compact {
		gap: var(--sp-4);
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.lead {
		display: grid;
		gap: var(--sp-1);
	}

	.overline {
		color: var(--accent-text);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	/* In the UI face: the page's title is the view's serif line. */
	.course {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
		text-wrap: balance;
	}

	.programme {
		color: var(--text-muted);
		text-wrap: pretty;
	}

	.compact .programme {
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	/* One card of figures: side by side when there is room, else one per line. */
	.stats {
		display: grid;
		margin: 0;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
		overflow: hidden;
	}

	.compact .stats {
		border-radius: 0;
		background: none;
		box-shadow: none;
		border-block-start: 1px solid var(--hairline);
	}

	.stat {
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		gap: var(--sp-3);
		padding: 12px 16px;
		box-shadow: 0 1px 0 var(--hairline);
	}

	.compact .stat {
		padding-inline: 0;
	}

	.label {
		color: var(--text-muted);
		font: var(--type-label);
	}

	/* Placeholders take the room their text will. */
	span.label,
	span.value {
		flex: 1;
		align-self: stretch;
	}

	.value {
		margin: 0;
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
	}

	@container (width >= 440px) {
		.stats {
			grid-auto-columns: minmax(0, 1fr);
			grid-auto-flow: column;
		}

		.stat {
			flex-direction: column;
			justify-content: flex-start;
			gap: 6px;
			padding: var(--sp-4) var(--sp-5);
			box-shadow: 1px 0 0 var(--hairline);
		}

		.compact .stat {
			padding: var(--sp-4) var(--sp-4) 0 0;
			box-shadow: none;
		}

		.value {
			font: var(--type-title-m);
			letter-spacing: var(--track-title-m);
			font-variant-numeric: tabular-nums;
		}
	}

	@supports (corner-shape: squircle) {
		.compact,
		.stats {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}

		.compact .stats {
			border-radius: 0;
		}
	}
</style>
