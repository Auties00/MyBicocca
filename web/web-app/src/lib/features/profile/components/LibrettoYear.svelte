<script lang="ts">
	import { italianTitleCase } from '#lib/format/names.ts';
	import { formatDate } from '#lib/format/dates.ts';
	import { formatCredits } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Grade from '#lib/ui/Grade.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import type { PrerequisiteStatus } from '../attempts.ts';
	import { rowGrade } from '../format.ts';
	import type { LibrettoRow, YearGroup } from '../transcript.ts';
	import {
		courseHref,
		rowStatus,
		yearAnchor,
		yearSummary,
		yearTitle,
		type LibrettoView,
		type RowStatus
	} from './labels.ts';

	interface Props {
		group: YearGroup;
		view: LibrettoView;
		selected?: number;
		prerequisites?: Record<number, PrerequisiteStatus>;
	}

	let { group, view, selected, prerequisites }: Props = $props();

	function description(row: LibrettoRow, status: RowStatus): string {
		return row.state === 'passed' && row.date
			? m.profile_passed_on({ date: formatDate(row.date) })
			: status.label;
	}
</script>

<div class="year" id={yearAnchor(group.year)}>
	<ListGroup title={yearTitle(group.year)} description={yearSummary(group)}>
		{#each group.rows as row (row.id)}
			{@const status = rowStatus(row, prerequisites)}
			<ListRow
				title={italianTitleCase(row.name)}
				description={description(row, status)}
				icon={status.icon}
				iconTone={status.tone}
				href={courseHref(row, view)}
				selected={row.id === selected}
			>
				{#snippet trailing()}
					<span class={['values', `view-${view}`]}>
						<span class="credits">{formatCredits(row.credits)}</span>
						{#if row.grade || row.state === 'passed'}
							<span class="grade">
								<Grade grade={rowGrade(row.grade, row.state === 'passed')} />
							</span>
						{:else}
							<span class="grade none" aria-hidden="true">—</span>
						{/if}
					</span>
				{/snippet}
			</ListRow>
		{/each}
	</ListGroup>
</div>

<style>
	.year {
		scroll-margin-block-start: calc(var(--shell-inset-top) + 64px);
	}

	.values {
		display: inline-flex;
		align-items: baseline;
		gap: 16px;
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
	}

	.credits {
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.grade {
		min-inline-size: 2.5ch;
		text-align: end;
	}

	.grade.none {
		color: var(--text-subtle);
		font: 600 1.0625rem / 1.2 var(--font-ui);
	}

	.view-grades .credits,
	.view-credits .grade {
		display: none;
	}

	.view-credits .credits {
		color: var(--text);
		font: 600 0.9375rem / 1.2 var(--font-ui);
	}

	/* Wide lists show both, the grade last, as a table would. */
	@container libretto (width >= 560px) {
		.values .credits,
		.values .grade {
			display: inline;
		}

		.values .grade {
			min-inline-size: 3em;
		}

		.values .credits {
			min-inline-size: 7ch;
			color: var(--text-muted);
			font: var(--type-ui);
			text-align: end;
		}
	}
</style>
