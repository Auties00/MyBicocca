<script lang="ts">
	import { resolve } from '$app/paths';
	import IconChalkboardTeacher from '~icons/ph/chalkboard-teacher-duotone';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import type { Query } from '#lib/data/query.svelte.ts';
	import { italianTitleCase } from '#lib/format/names.ts';
	import { m } from '#lib/paraglide/messages.js';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { courseCaption, yearLabel } from '../labels.ts';
	import { headlinePercent, type CourseAttendance } from '../model.ts';
	import PercentRing from './PercentRing.svelte';

	interface Props {
		courses: Query<CourseAttendance[]>;
		/** The open course's slug, beside the list. */
		selected?: string;
		/** Beside an open pane: rows of one line each, without chevrons. */
		dense?: boolean;
	}

	let { courses, selected, dense = false }: Props = $props();

	/*
	 * Only a first visit loads without a cached list: as many rows as a student usually has, each
	 * of a row's height, so the list and the check-in under it land where the skeleton stood.
	 */
	const SKELETON_ROWS = [62, 48, 70, 55, 66];

	const gate = new LoadingGate(() => courses.data === undefined && courses.status === 'pending');
	const updatedAt = $derived(courses.updatedAt ? new Date(courses.updatedAt) : undefined);

	/* The list comes sorted by year; a heading per year earns its place only when there are two. */
	const years = $derived(
		(courses.data ?? []).reduce<{ year: number; items: CourseAttendance[] }[]>((groups, course) => {
			const last = groups.at(-1);
			if (last?.year === course.year) last.items.push(course);
			else groups.push({ year: course.year, items: [course] });
			return groups;
		}, [])
	);
</script>

{#snippet rows(items: CourseAttendance[])}
	{#each items as course (course.slug)}
		{@const caption = courseCaption(course)}
		<ListRow
			title={italianTitleCase(course.name)}
			href={resolve(`services/attendance/${course.slug}`)}
			selected={course.slug === selected}
			{dense}
		>
			{#snippet leading()}<PercentRing percent={headlinePercent(course)} />{/snippet}
			<!-- The standing in its tone, as Android colours it: the word says it, the colour finds it. -->
			{#snippet description()}
				{#if caption.status}
					<span class={['status', caption.status.tone]}>{caption.status.label}</span> ·
				{/if}
				{caption.details}
			{/snippet}
		</ListRow>
	{/each}
{/snippet}

<div class="courses">
	{#if courses.error && courses.data}
		<ErrorState
			variant="stale"
			message={errorMessage(courses.error)}
			{updatedAt}
			onretry={courses.refresh}
			retrying={courses.refreshing}
		/>
	{/if}

	{#if courses.data === undefined && courses.status === 'error' && courses.error}
		<ErrorState
			title={m.attendance_load_failed()}
			message={errorMessage(courses.error)}
			onretry={courses.refresh}
			retrying={courses.refreshing}
		/>
	{:else if courses.data === undefined}
		<div class="loading" role="status">
			<span class="sr-only">{m.attendance_loading()}</span>
			{#if gate.visible}
				{#each SKELETON_ROWS as width (width)}
					<div class="skeleton-row" aria-hidden="true">
						<Skeleton variant="circle" width="44px" />
						<div class="skeleton-text">
							<Skeleton variant="text" width="{width}%" />
							<Skeleton variant="text" width="36%" />
						</div>
					</div>
				{/each}
			{/if}
		</div>
	{:else if courses.data.length === 0}
		<!-- Beside the pane it heads the column, level with the pane's own heading. -->
		<EmptyState
			size={dense ? 'section' : 'page'}
			icon={IconChalkboardTeacher}
			title={m.attendance_no_courses()}
			body={m.attendance_no_courses_body()}
		/>
	{:else if years.length === 1}
		<ListGroup>
			{@render rows(years[0].items)}
		</ListGroup>
	{:else}
		{#each years as group (group.year)}
			<ListGroup title={yearLabel(group.year)} level={2}>
				{@render rows(group.items)}
			</ListGroup>
		{/each}
	{/if}
</div>

<style>
	.courses {
		display: grid;
		gap: var(--sp-5);
	}

	.loading {
		--row: 64px;
		display: grid;
		gap: 2px;
		min-block-size: calc(5 * var(--row) + 4 * 2px);
		border-radius: var(--r-lg);
		overflow: hidden;
	}

	.skeleton-row {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: center;
		gap: 12px;
		block-size: var(--row);
		padding-inline: 16px;
		background: var(--surface);
	}

	.skeleton-text {
		display: grid;
		gap: 6px;
	}

	.status {
		font-weight: 600;
	}

	.success {
		color: var(--success);
	}

	.warning {
		color: var(--warning);
	}

	.danger {
		color: var(--danger);
	}
</style>
