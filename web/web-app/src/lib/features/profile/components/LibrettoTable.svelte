<script lang="ts">
	import { italianTitleCase } from '#lib/format/names.ts';
	import { formatDate } from '#lib/format/dates.ts';
	import { formatCredits, formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Grade from '#lib/ui/Grade.svelte';
	import IconChip from '#lib/ui/IconChip.svelte';
	import Meter from '#lib/ui/Meter.svelte';
	import type { PrerequisiteStatus } from '../attempts.ts';
	import { gradeText, rowGrade } from '../format.ts';
	import type { YearGroup } from '../transcript.ts';
	import {
		courseHref,
		rowStatus,
		yearAnchor,
		yearProgress,
		yearSummary,
		yearTitle,
		type LibrettoView
	} from './labels.ts';

	interface Props {
		/** Grouped by date: the table has a date column. */
		groups: readonly YearGroup[];
		/** Kept on the course links, for the list beside the course. */
		view: LibrettoView;
		prerequisites?: Record<number, PrerequisiteStatus>;
	}

	let { groups, view, prerequisites }: Props = $props();

	const ids = $props.id();
</script>

<!--
	The whole libretto as one table, by degree year: every column at once, where a narrow list
	needs the Grades/Credits switch. Rows are links, so the column heads are for the eye only and
	each row reads in full.
-->
<div class="table">
	{#each groups as group (group.year)}
		{@const title = yearTitle(group.year)}
		{@const summary = yearSummary(group)}
		<section class="year" id={yearAnchor(group.year)} aria-labelledby="{ids}-{group.year}">
			<div class="sticky">
				<header class="head">
					<div class="heading">
						<h2 class="title" id="{ids}-{group.year}">{title}</h2>
						<p class="summary">{summary}</p>
					</div>
					<span class="meter">
						<Meter
							value={yearProgress(group)}
							max={1}
							label={title}
							valueText={summary}
							hideLabel
						/>
					</span>
				</header>
				<div class="line columns" aria-hidden="true">
					<span class="course">{m.profile_column_course()}</span>
					<span class="date">{m.profile_column_date()}</span>
					<span class="credits">{m.profile_column_credits()}</span>
					<span class="grade">{m.profile_column_grade()}</span>
				</div>
			</div>
			<ul class="rows" role="list">
				{#each group.rows as row (row.id)}
					{@const status = rowStatus(row, prerequisites)}
					{@const passed = row.state === 'passed'}
					<li>
						<a class="line" href={courseHref(row, view)}>
							<span class="chip"><IconChip icon={status.icon} tone={status.tone} /></span>
							<!-- The check says "passed"; only what is still open gets words under the name. -->
							<span class="course">
								<span class="name">{italianTitleCase(row.name)}</span>
								{#if passed}
									<span class="sr-only">, {status.label}</span>
								{:else}
									<span class="status">{status.label}</span>
								{/if}
							</span>
							<span class="date">
								{#if passed && row.date}
									{formatDate(row.date)}
								{:else}
									<span class="none" aria-hidden="true">—</span>
								{/if}
							</span>
							<span class="credits">
								<span aria-hidden="true"
									>{formatNumber(row.credits, { maximumFractionDigits: 1 })}</span
								>
								<span class="sr-only">{formatCredits(row.credits)}</span>
							</span>
							<span class="grade">
								{#if row.grade || passed}
									<Grade
										grade={rowGrade(row.grade, passed)}
										spoken={row.grade
											? m.profile_grade_label({ grade: gradeText(row.grade, true) })
											: undefined}
									/>
								{:else}
									<span class="none" aria-hidden="true">—</span>
								{/if}
							</span>
						</a>
					</li>
				{/each}
			</ul>
		</section>
	{/each}
</div>

<style>
	.table {
		display: grid;
		gap: var(--sp-6);
		container: libretto-table / inline-size;
	}

	.year {
		--outer: var(--r-lg);
		--inner: var(--r-2xs);
		display: grid;
		gap: 2px;
		scroll-margin-block-start: calc(var(--shell-inset-top) + 8px);
	}

	/* The year and the column heads stay in view while that year's rows scroll under them. */
	.sticky {
		position: sticky;
		inset-block-start: var(--shell-inset-top);
		z-index: 1;
		display: grid;
		gap: 2px;
		background: var(--bg);
	}

	.head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: var(--sp-5);
		padding: 18px 20px 14px;
		border-radius: var(--outer) var(--outer) var(--inner) var(--inner);
		background: var(--surface);
		box-shadow: var(--inner-light);
	}

	.title {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.summary {
		margin-block-start: 2px;
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.meter {
		flex: none;
		inline-size: 160px;
	}

	/* One grid for the heads and every row, so the columns line up. */
	.line {
		display: grid;
		grid-template-columns: 40px minmax(0, 1fr) 7.5rem 3.5rem 4rem;
		grid-template-areas: 'chip course date credits grade';
		align-items: center;
		column-gap: 16px;
		padding-inline: 20px;
	}

	.columns {
		grid-template-areas: 'course course date credits grade';
		padding-block: 10px;
		background: var(--surface-sunken);
		color: var(--text-muted);
		font: var(--type-meta);
		font-weight: 600;
	}

	.chip {
		grid-area: chip;
		display: flex;
	}

	.course {
		grid-area: course;
		display: grid;
		min-inline-size: 0;
	}

	.date {
		grid-area: date;
		text-align: end;
	}

	.credits {
		grid-area: credits;
		text-align: end;
	}

	.grade {
		grid-area: grade;
		text-align: end;
	}

	.rows {
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.rows > li {
		border-radius: var(--inner);
		background: var(--surface);
		box-shadow: var(--inner-light);
		overflow: clip;
	}

	.rows > li:last-child {
		border-end-start-radius: var(--outer);
		border-end-end-radius: var(--outer);
	}

	.rows .line {
		min-block-size: 60px;
		padding-block: 8px;
		color: var(--text);
		text-decoration: none;
		transition: background-color var(--dur-fast) var(--ease-out);
		touch-action: manipulation;
	}

	.rows .line:focus-visible {
		outline-offset: -2px;
	}

	@media (hover: hover) {
		.rows .line:hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	.rows .line:active {
		background: var(--state-press);
		transition-duration: 0s;
	}

	.name,
	.status {
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.name {
		font: var(--type-body-strong);
		font-size: 0.9375rem;
		line-height: 1.35;
	}

	.status {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.rows .date,
	.rows .credits {
		color: var(--text-muted);
		font: var(--type-ui);
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
	}

	.none {
		color: var(--text-subtle);
	}

	@container libretto-table (width >= 860px) {
		.line {
			grid-template-columns: 40px minmax(0, 1fr) 9rem 5rem 5rem;
			column-gap: 20px;
		}

		.rows .line {
			min-block-size: 56px;
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.rows .line {
			transition: none;
		}
	}
</style>
