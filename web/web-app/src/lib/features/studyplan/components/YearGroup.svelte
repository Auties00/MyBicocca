<script lang="ts">
	import { resolve } from '$app/paths';
	import IconCaretRight from '~icons/ph/caret-right';
	import { italianTitleCase } from '#lib/format/names.ts';
	import { gradeText, rowGrade } from '#lib/features/profile/format.ts';
	import type { LibrettoRow } from '#lib/features/profile/transcript.ts';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Badge from '#lib/ui/Badge.svelte';
	import Grade from '#lib/ui/Grade.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import { yearSummary, yearTitle } from '../labels.ts';
	import type { PlanYear } from '../plan.ts';
	import CreditChip from './CreditChip.svelte';

	interface Props {
		group: PlanYear;
		/** The record-book row of an activity, when the record book has loaded. */
		recordOf: (code: string | null) => LibrettoRow | undefined;
		/** Whether the record book has loaded: only then can credits be counted as earned. */
		known: boolean;
	}

	let { group, recordOf, known }: Props = $props();

	const complete = $derived(known && group.earned >= group.credits && group.credits > 0);
</script>

<ListGroup title={yearTitle(group.year)} description={yearSummary(group)}>
	{#snippet actions()}
		{#if complete}
			<Badge tone="success">{m.studyplan_year_complete()}</Badge>
		{:else if known && group.earned > 0}
			<span class="earned">
				{m.studyplan_year_earned({
					earned: formatNumber(Math.trunc(group.earned)),
					total: formatNumber(Math.trunc(group.credits))
				})}
			</span>
		{/if}
	{/snippet}
	{#each group.courses as course (course.id)}
		{@const record = recordOf(course.code)}
		{#snippet status()}
			{#if record?.state === 'passed'}
				<span class="grade">
					<Grade
						grade={rowGrade(record.grade, true)}
						spoken={record.grade
							? m.studyplan_activity_passed({ grade: gradeText(record.grade, true) })
							: undefined}
					/>
				</span>
			{:else}
				<span class="attended">{m.studyplan_activity_attended()}</span>
			{/if}
			<span class="chevron"><IconCaretRight aria-hidden="true" /></span>
		{/snippet}
		<ListRow
			title={italianTitleCase(course.name)}
			description={course.code ?? undefined}
			href={record ? resolve(`profile/exams/${record.id}`) : undefined}
			trailing={record?.state === 'passed' || record?.state === 'attended' ? status : undefined}
		>
			{#snippet leading()}<CreditChip credits={course.credits} />{/snippet}
		</ListRow>
	{/each}
</ListGroup>

<style>
	.earned {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
	}

	.grade {
		min-inline-size: 2.5ch;
		text-align: end;
	}

	.attended {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.chevron {
		display: flex;
		margin-inline-end: -4px;
		color: var(--text-subtle);
	}

	.chevron :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}
</style>
