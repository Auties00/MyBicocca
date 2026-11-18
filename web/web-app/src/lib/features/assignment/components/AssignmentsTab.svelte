<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import IconClipboardText from '~icons/ph/clipboard-text-duotone';
	import { Clock } from '#lib/data/clock.svelte.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { getCourse } from '#lib/features/course/course.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import ProgressBar from '#lib/ui/ProgressBar.svelte';
	import SegmentedControl from '#lib/ui/SegmentedControl.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { assignmentsOf, uploadLimit } from '../assignment.svelte.ts';
	import { gradeText } from '../labels.ts';
	import { assignmentsPath } from '../links.ts';
	import { groupAssignments, matchesFilter, nextDue, type AssignmentFilter } from '../model.ts';
	import { courseAssignmentsQuery } from '../queries.ts';
	import { rowOf, type ListedAssignment } from '../rows.ts';
	import { SubmissionStatuses } from '../statuses.svelte.ts';
	import AssignmentRow from './AssignmentRow.svelte';
	import NextAssignment from './NextAssignment.svelte';

	/** From this many assignments, filters help find what is left to do. */
	const FILTERS_FROM = 6;
	/** Below this many, the next deadline is the list's own first row. */
	const NEXT_FROM = 3;
	const FILTERS: readonly AssignmentFilter[] = ['all', 'todo', 'urgent'];

	const course = getCourse();
	const clock = new Clock(() => 60_000);

	const list = query(() => {
		const scope = session.current;
		return scope && courseAssignmentsQuery(scope, course.id);
	});

	const assignments = $derived(assignmentsOf(list.data, course.id));
	const statuses = new SubmissionStatuses(() => assignments?.map((item) => item.id) ?? []);

	const rows = $derived.by(() => {
		const limit = uploadLimit();
		return new Map(
			(assignments ?? []).map((assignment): [number, ListedAssignment] => {
				const status = statuses.get(assignment.id);
				return [
					assignment.id,
					rowOf(assignment, status.data, status.error !== undefined, clock.now, limit)
				];
			})
		);
	});
	const all = $derived([...rows.values()]);
	const settled = $derived(all.every((row) => row.submission !== undefined || row.failed));
	const failed = $derived(all.filter((row) => row.failed));
	const next = $derived(settled && all.length >= NEXT_FROM ? nextDue(all, clock.now) : undefined);
	const progress = $derived.by(() => {
		const counted = all.filter((row) => row.standing !== undefined && row.standing !== 'none');
		return {
			done: counted.filter((row) => row.standing === 'submitted' || row.standing === 'graded')
				.length,
			total: counted.length
		};
	});

	const filter = $derived.by((): AssignmentFilter => {
		const value = page.url.searchParams.get('filter');
		return FILTERS.includes(value as AssignmentFilter) ? (value as AssignmentFilter) : 'all';
	});
	const filtering = $derived(all.length >= FILTERS_FROM);
	const sections = $derived.by(() => {
		if (assignments === undefined) return [];
		const groups = groupAssignments(assignments, clock.now);
		const shown = (items: typeof assignments) =>
			items.flatMap((assignment) => {
				const row = rows.get(assignment.id);
				return row && (!filtering || matchesFilter(filter, row, clock.now)) ? [row] : [];
			});
		return [
			{ id: 'upcoming', title: m.assignment_group_upcoming(), rows: shown(groups.upcoming) },
			{ id: 'undated', title: m.assignment_group_undated(), rows: shown(groups.undated) },
			{ id: 'past', title: m.assignment_group_past(), rows: shown(groups.past) }
		].filter((section) => section.rows.length > 0);
	});

	/* A grade column only while some row in view has a grade. */
	const grades = $derived(
		sections.some((section) =>
			section.rows.some((row) => row.submission?.grade && gradeText(row.submission.grade) !== null)
		)
	);

	function setFilter(value: AssignmentFilter) {
		void goto(assignmentsPath(course.id, value), { replace: true, reset: false });
	}

	const filterOptions = $derived([
		{ value: 'all' as const, label: m.assignment_filter_all() },
		{ value: 'todo' as const, label: m.assignment_filter_todo() },
		{ value: 'urgent' as const, label: m.assignment_filter_urgent() }
	]);

	const gate = new LoadingGate(() => list.data === undefined && list.status === 'pending');
	const updatedAt = $derived(list.updatedAt ? new Date(list.updatedAt) : undefined);
</script>

<svelte:head>
	<title>{m.course_tab_assignments()} · {course.title ?? m.course_loading()} · {m.app_name()}</title
	>
</svelte:head>

{#if list.error && list.data}
	<div class="notice">
		<ErrorState
			variant="stale"
			message={errorMessage(list.error)}
			{updatedAt}
			onretry={list.refresh}
			retrying={list.refreshing}
		/>
	</div>
{:else if failed.length > 0}
	<div class="notice">
		<ErrorState
			variant="stale"
			message={m.assignment_statuses_failed({ count: failed.length })}
			onretry={() => void statuses.retry(failed.map((row) => row.assignment.id))}
		/>
	</div>
{/if}

{#if list.data === undefined && list.error}
	<ErrorState
		title={m.assignment_list_failed()}
		message={errorMessage(list.error)}
		onretry={list.refresh}
		retrying={list.refreshing}
	/>
{:else if assignments === undefined}
	<div class="layout" aria-busy="true" aria-label={m.course_loading()}>
		{#if gate.visible}
			<div class="aside skeleton-aside">
				<Skeleton width="40%" height="0.9em" />
				<Skeleton width="100%" height="8px" radius="4px" />
			</div>
			<div class="assignments">
				<div class="group">
					<Skeleton width="96px" height="0.8em" />
					<ol class="rows">
						{#each { length: 4 }, index (index)}
							<li class="skeleton">
								<Skeleton width="52px" height="52px" radius="15px" />
								<span class="lines">
									<Skeleton width="{64 - index * 7}%" />
									<Skeleton width="36%" height="0.8em" />
								</span>
							</li>
						{/each}
					</ol>
				</div>
			</div>
		{/if}
	</div>
{:else if assignments.length === 0}
	<EmptyState
		size="cover"
		icon={IconClipboardText}
		title={m.assignment_empty_title()}
		body={m.assignment_empty_body()}
	/>
{:else}
	<div class="layout">
		<aside class="aside" aria-label={m.assignment_summary_label()}>
			<div class="summary">
				<p class="count">{m.assignment_count({ count: all.length })}</p>
				{#if !settled || progress.total > 0}
					<div class="meter">
						{#if settled}
							<ProgressBar
								value={progress.done}
								max={progress.total}
								label={m.assignment_progress_label()}
								valueText={m.assignment_progress_value(progress)}
								size="sm"
							/>
						{:else}
							<span class="meter-skeleton">
								<Skeleton width="60%" height="14px" />
								<Skeleton width="100%" height="6px" radius="3px" />
							</span>
						{/if}
					</div>
				{/if}
			</div>
			{#if next}
				<div class="next-slot">
					<NextAssignment row={next} courseId={course.id} now={clock.now} />
				</div>
			{/if}
			{#if filtering}
				<div class="tools">
					<SegmentedControl
						options={filterOptions}
						value={filter}
						label={m.assignment_filter_label()}
						full
						onchange={setFilter}
					/>
				</div>
			{/if}
		</aside>

		<div class="assignments">
			{#if sections.length === 0}
				<EmptyState title={m.assignment_filter_empty()}>
					{#snippet action()}
						<Button variant="tonal" onclick={() => setFilter('all')}>
							{m.assignment_filter_all()}
						</Button>
					{/snippet}
				</EmptyState>
			{/if}
			{#each sections as section, index (section.id)}
				<section class="group" aria-labelledby="assignments-{section.id}">
					<div class={['head', { grades }]}>
						<h2 class="label" id="assignments-{section.id}">
							{section.title}
							<span class="count">{section.rows.length}</span>
						</h2>
						{#if index === 0}
							<span class="column" aria-hidden="true">{m.assignment_column_status()}</span>
							{#if grades}
								<span class="column end" aria-hidden="true">{m.assignment_column_grade()}</span>
							{/if}
						{/if}
					</div>
					<ol class="rows">
						{#each section.rows as row (row.assignment.id)}
							<li>
								<AssignmentRow {row} courseId={course.id} now={clock.now} {grades} />
							</li>
						{/each}
					</ol>
				</section>
			{/each}
		</div>
	</div>
{/if}

<style>
	.notice {
		margin-block-end: var(--sp-5);
	}

	.layout {
		display: grid;
		gap: var(--sp-5);
	}

	.aside {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 16px 24px;
		min-inline-size: 0;
	}

	.summary {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 8px 20px;
		flex: 1 1 260px;
		padding-inline: 4px;
	}

	.summary .count {
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.meter {
		flex: 1 1 200px;
		max-inline-size: 320px;
	}

	.meter-skeleton {
		display: grid;
		gap: 10px;
		block-size: 30px;
		align-content: center;
	}

	/* The list's first row already says it on narrow screens. */
	.next-slot {
		display: none;
	}

	.tools {
		display: flex;
		flex: 1 1 280px;
		max-inline-size: 420px;
	}

	@container page (width >= 1000px) {
		.layout {
			grid-template-columns: 260px minmax(0, 1fr);
			align-items: start;
			gap: var(--sp-7);
		}

		.aside {
			position: sticky;
			inset-block-start: calc(var(--topbar-h) + var(--control-h-lg) + 32px);
			flex-direction: column;
			flex-wrap: nowrap;
			align-items: stretch;
			gap: 20px;
		}

		.summary {
			flex: none;
			flex-direction: column;
			align-items: stretch;
		}

		.meter {
			flex: none;
			max-inline-size: none;
		}

		.next-slot {
			display: grid;
		}

		.tools {
			flex: none;
			max-inline-size: none;
		}
	}

	.assignments {
		container: assignments / inline-size;
		display: grid;
		gap: var(--sp-6);
		min-inline-size: 0;
	}

	.group {
		display: grid;
		gap: 10px;
	}

	.label {
		display: flex;
		align-items: baseline;
		gap: 8px;
		padding-inline: 4px;
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.label .count {
		font-variant-numeric: tabular-nums;
	}

	.rows {
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.rows > li {
		border-radius: var(--r-2xs);
		box-shadow: var(--inner-light);
	}

	.rows > li:first-child {
		border-start-start-radius: var(--r-lg);
		border-start-end-radius: var(--r-lg);
	}

	.rows > li:last-child {
		border-end-start-radius: var(--r-lg);
		border-end-end-radius: var(--r-lg);
	}

	.rows > li > :global(a) {
		border-radius: inherit;
	}

	.skeleton-aside {
		display: grid;
		gap: 10px;
		align-content: start;
	}

	.skeleton {
		display: flex;
		align-items: center;
		gap: 14px;
		min-block-size: 76px;
		padding: 12px;
		background: var(--surface);
	}

	.lines {
		display: grid;
		flex: 1;
		gap: 8px;
	}

	.column {
		display: none;
	}

	/* The first group's heading names the table's columns, once they have room. */
	@container assignments (width >= 640px) {
		.head {
			display: grid;
			grid-template-columns: minmax(0, 1fr) 168px 18px;
			gap: 18px;
			align-items: baseline;
			padding-inline-end: 14px;
		}

		.head.grades {
			grid-template-columns: minmax(0, 1fr) 168px 72px 18px;
		}

		.column {
			display: block;
			color: var(--text-muted);
			font: var(--type-label);
		}

		.end {
			text-align: end;
		}
	}
</style>
