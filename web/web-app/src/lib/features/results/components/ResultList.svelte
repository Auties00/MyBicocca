<script lang="ts">
	import IconChecks from '~icons/ph/checks';
	import { academicYearLabel } from '#lib/data/academicYear.ts';
	import { m } from '#lib/paraglide/messages.js';
	import FilterChip from '#lib/ui/FilterChip.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import SectionHeader from '#lib/ui/SectionHeader.svelte';
	import { archiveByYear } from '../model.ts';
	import type { Results } from '../results.svelte.ts';
	import ArchiveRow from './ArchiveRow.svelte';
	import PendingCard from './PendingCard.svelte';

	interface Props {
		results: Results;
		/** The result open beside the list. */
		selected?: string;
	}

	let { results, selected }: Props = $props();

	const split = $derived(results.split);
	const years = $derived(archiveByYear(results.archive));
	const pendingId = $props.id();
	const archiveId = `${pendingId}-archive`;
</script>

<div class="list">
	<section class="section" aria-labelledby={pendingId}>
		<SectionHeader
			id={pendingId}
			title={m.results_pending_heading()}
			description={split.pending.length > 0 ? m.results_pending_description() : undefined}
		/>
		{#if split.pending.length > 0}
			<ul class="cards" role="list">
				{#each split.pending as result (result.id)}
					<li><PendingCard {result} selected={result.id === selected} /></li>
				{/each}
			</ul>
		{:else}
			<p class="settled">
				<IconChecks aria-hidden="true" />
				<span>
					<span class="settled-title">{m.results_nothing_to_decide()}</span>
					<span class="settled-body">{m.results_nothing_to_decide_body()}</span>
				</span>
			</p>
		{/if}
	</section>

	{#if split.archived.length > 0}
		<section class="section" aria-labelledby={archiveId}>
			<SectionHeader
				id={archiveId}
				title={m.results_archive_heading()}
				description={m.results_archive_description()}
			/>
			{#if results.absences > 0}
				<div class="filters">
					<FilterChip
						label={m.results_hide_absences()}
						count={results.absences}
						bind:selected={results.hideAbsences}
					/>
				</div>
			{/if}
			{#if years.length === 0}
				<p class="only-absences">{m.results_only_absences()}</p>
			{/if}
			{#each years as group (group.year)}
				{@const label =
					group.year === null ? m.results_year_undated() : academicYearLabel(group.year)}
				<div class="year">
					<h3 class="year-label">{label}</h3>
					<ListGroup {label} level={3}>
						{#each group.results as result (result.id)}
							<ArchiveRow {result} selected={result.id === selected} />
						{/each}
					</ListGroup>
				</div>
			{/each}
		</section>
	{/if}
</div>

<style>
	.list {
		display: grid;
		gap: var(--sp-7);
	}

	.section {
		display: grid;
		gap: var(--sp-4);
	}

	.cards {
		display: grid;
		gap: var(--sp-3);
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.settled {
		display: flex;
		align-items: flex-start;
		gap: var(--sp-3);
		padding: var(--sp-4);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.settled :global(svg) {
		flex: none;
		inline-size: 22px;
		block-size: 22px;
		margin-block-start: 1px;
		color: var(--success);
	}

	.settled > span {
		display: grid;
		gap: 2px;
	}

	.settled-title {
		font: var(--type-body-strong);
	}

	.settled-body {
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}

	.filters {
		display: flex;
		flex-wrap: wrap;
		gap: var(--sp-2);
		margin-block-start: calc(-1 * var(--sp-1));
	}

	.only-absences {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.year {
		display: grid;
		gap: var(--sp-2);
	}

	.year-label {
		padding-inline: 4px;
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		font-variant-numeric: tabular-nums;
	}

	@supports (corner-shape: squircle) {
		.settled {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}
</style>
