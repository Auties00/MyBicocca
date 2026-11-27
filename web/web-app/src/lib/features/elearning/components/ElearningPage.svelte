<script lang="ts">
	import { tick } from 'svelte';
	import { flip } from 'svelte/animate';
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconPlus from '~icons/ph/plus';
	import IconStar from '~icons/ph/star';
	import IconStarDuotone from '~icons/ph/star-duotone';
	import IconFunnelX from '~icons/ph/funnel-x-duotone';
	import IconGraduationCap from '~icons/ph/graduation-cap-duotone';
	import { now as minute } from '#lib/data/clock.svelte.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { formatRelative } from '#lib/format/dates.ts';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { getLocale } from '#lib/paraglide/runtime.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Fab from '#lib/ui/Fab.svelte';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import PullToRefresh from '#lib/ui/PullToRefresh.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { CourseCareers, type CareerLike } from '../careers.ts';
	import { storedFilter } from '../choices.svelte.ts';
	import {
		ALL,
		filterCourses,
		filterToken,
		parseFilter,
		toEnrolledCourses,
		type CourseFilter,
		type CourseGroup
	} from '../courses.ts';
	import { recentDeadlines } from '../deadlines.ts';
	import { favouritesOf } from '../favourites.svelte.ts';
	import { catalogHref, coursesHref } from '../links.ts';
	import { courseDeadlinesQuery, enrolledCoursesQuery, refreshCourses } from '../queries.ts';
	import { pendingReveal, revealDone } from '../reveal.ts';
	import CourseCard from './CourseCard.svelte';
	import CourseCardSkeleton from './CourseCardSkeleton.svelte';
	import CourseFilterBar, { type FilterOption } from './CourseFilterBar.svelte';
	import { expanded, reducedMotion } from '#lib/ui/media.ts';

	const courses = query(() => session.current && enrolledCoursesQuery(session.current));
	const deadlines = query(() => session.current && courseDeadlinesQuery(session.current));

	/*
	 * The cards wait for their deadlines too, as on Android, so they don't grow under the reader
	 * when the deadlines land; a failed deadlines load lets them through with a notice.
	 */
	const settled = $derived(
		courses.data !== undefined && (deadlines.data !== undefined || deadlines.status === 'error')
	);
	const loading = $derived(!settled && courses.status !== 'error');
	const gate = new LoadingGate(() => loading);

	const now = $derived(minute.current);

	const scope = $derived(session.current);
	const favourites = $derived(scope && favouritesOf(scope.account.id));
	const careers = $derived(scope && new CourseCareers(scope.account.careers, scope.career.id));
	const enrolled = $derived(courses.data && toEnrolledCourses(courses.data, getLocale()));
	const visible = $derived(enrolled?.filter((course) => !course.hidden) ?? []);
	const isFavourite = (course: { id: number; moodleFavourite: boolean }) =>
		favourites?.has(course) ?? course.moodleFavourite;

	const asked = $derived(parseFilter(page.url.searchParams.get('filter')) ?? storedFilter.get());
	const list = $derived(
		enrolled && careers ? filterCourses(enrolled, asked, careers, isFavourite) : undefined
	);

	const deadlinesByCourse = $derived(
		Map.groupBy(
			recentDeadlines(deadlines.data ?? [], now.getTime()),
			(deadline) => deadline.courseId
		)
	);

	/** A career's chip names its level, with its start year when two would read alike. */
	function careerLabels(others: readonly CareerLike[]): Map<number, string> {
		const base = (career: CareerLike) =>
			career.level === 'bachelor'
				? m.elearning_course_level_bachelor()
				: career.level === 'master'
					? m.elearning_course_level_master()
					: career.description;
		const labels = others.map(base);
		return new Map(
			others.map((career, index) => [
				career.id,
				labels.filter((label) => label === labels[index]).length > 1
					? m.elearning_career_chip({ label: labels[index], year: career.academicYear })
					: labels[index]
			])
		);
	}

	const filterOptions = $derived.by((): FilterOption[] => {
		if (!list || !enrolled || !careers) return [];
		const labels = careerLabels(list.options.otherCareers);
		const options: { filter: CourseFilter; label: string; icon?: FilterOption['icon'] }[] = [
			{ filter: ALL, label: m.elearning_filter_all() },
			{ filter: { kind: 'favourites' }, label: m.elearning_filter_favourites(), icon: IconStar },
			...list.options.years.map((year) => ({
				filter: { kind: 'year', year } as const,
				label: m.elearning_filter_year({ year })
			})),
			...list.options.otherCareers.map((career) => ({
				filter: { kind: 'career', careerId: career.id } as const,
				label: labels.get(career.id) ?? career.description
			}))
		];
		return options.map(({ filter, label, icon }) => ({ token: filterToken(filter), label, icon }));
	});

	function choose(token: string) {
		storedFilter.set(parseFilter(token) ?? ALL);
		void goto(coursesHref(token), { replace: true, reset: false });
	}

	/* The group of a course just enrolled in, briefly marked once it's brought into view. */
	let revealed = $state<string>();

	/*
	 * After an enrolment, the list brings the new course into view once a refresh lists it, showing
	 * every course first if the filter hides it, as Android does.
	 */
	$effect(() => {
		const courseId = pendingReveal();
		if (courseId === undefined || !settled || !list || !enrolled) return;
		if (!enrolled.some((course) => course.id === courseId && !course.hidden)) return;
		const group = list.groups.find((candidate) =>
			candidate.editions.some((edition) => edition.id === courseId)
		);
		if (!group) {
			choose(filterToken(ALL));
			return;
		}
		revealDone();
		revealed = group.key;
		void tick().then(() => {
			document
				.querySelector(`[data-group="${CSS.escape(group.key)}"]`)
				?.scrollIntoView({ block: 'center', behavior: reducedMotion.current ? 'auto' : 'smooth' });
		});
	});

	function showAll() {
		choose(filterToken(ALL));
	}

	function star(group: CourseGroup, starred: boolean) {
		void favourites?.set(
			group.editions.map((edition) => edition.id),
			starred
		);
	}

	function refresh() {
		if (scope) refreshCourses(scope);
	}
	const refreshing = $derived(courses.refreshing || deadlines.refreshing);
	/* The minute tick can trail a fresh load. */
	const clock = $derived(new Date(Math.max(now.getTime(), deadlines.updatedAt ?? 0)));
	const deadlinesUpdatedAt = $derived(
		deadlines.updatedAt ? new Date(deadlines.updatedAt) : undefined
	);
	/* "Loaded now" reads oddly in a notice of failure: under a minute, it says "moments ago". */
	const deadlinesNotice = $derived.by(() => {
		if (!deadlines.data || !deadlinesUpdatedAt) return m.elearning_deadlines_failed();
		return clock.getTime() - deadlinesUpdatedAt.getTime() < 60_000
			? m.elearning_deadlines_stale_now()
			: m.elearning_deadlines_stale({ time: formatRelative(deadlinesUpdatedAt, clock) });
	});
	/* With no course at all, the empty state carries the one way to add one. */
	const canAdd = $derived(courses.data === undefined || visible.length > 0);
	/* Below 1024 px adding a course is the floating button, and a pull refreshes. */
	const showRefresh = $derived(expanded.current);
	const showAdd = $derived(canAdd && expanded.current);
	const showFilters = $derived(!!list && visible.length > 0);
	const courseCount = $derived(
		list &&
			m.elearning_category_courses({
				count: list.groups.length,
				shown: formatNumber(list.groups.length)
			})
	);
	const motion = $derived(reducedMotion.current ? 0 : 1);
</script>

{#snippet filters()}
	{#if showFilters && list}
		<div class="filters">
			<CourseFilterBar
				options={filterOptions}
				value={filterToken(list.filter)}
				label={m.elearning_filter_label()}
				onchange={choose}
			/>
		</div>
	{:else}
		<div class="filters chips-skeleton" aria-hidden="true">
			{#each [64, 92, 72, 72] as width, index (index)}
				<Skeleton width="{width}px" height="36px" radius="999px" />
			{/each}
		</div>
	{/if}
{/snippet}

{#snippet headerActions()}
	{#if showRefresh}
		<IconButton
			icon={IconArrowClockwise}
			label={m.common_refresh()}
			variant="outline"
			loading={refreshing}
			disabled={!scope}
			onclick={refresh}
		/>
	{/if}
	{#if showAdd}
		<Button variant="solid" icon={IconPlus} href={catalogHref()}>
			{m.elearning_add_course()}
		</Button>
	{/if}
{/snippet}

<PullToRefresh onrefresh={refresh} {refreshing}>
	<Page width="dashboard">
		<PageHeader
			title={m.elearning_title()}
			subtitle={courseCount}
			loading={courseCount === undefined && loading ? { eyebrow: false } : false}
			actions={showRefresh || showAdd ? headerActions : undefined}
			meta={showFilters || loading ? filters : undefined}
			pinMeta
		/>

		{#if courses.error && courses.data}
			<ErrorState
				variant="stale"
				message={m.elearning_stale()}
				onretry={refresh}
				retrying={refreshing}
			/>
		{/if}

		{#if courses.data === undefined && courses.status === 'error' && courses.error}
			<ErrorState
				title={m.elearning_sync_failed()}
				message={errorMessage(courses.error)}
				onretry={courses.refresh}
				retrying={courses.refreshing}
			/>
		{:else if !settled}
			<div role="status" aria-busy="true">
				<span class="sr-only">{m.common_loading()}</span>
				{#if gate.visible}
					<div class="grid">
						{#each [true, false, true, false, false, false] as withDeadlines, index (index)}
							<CourseCardSkeleton deadlines={withDeadlines} />
						{/each}
					</div>
				{/if}
			</div>
		{:else if visible.length === 0}
			<EmptyState
				size="page"
				icon={IconGraduationCap}
				title={m.elearning_no_enrollment()}
				body={m.elearning_no_enrollment_hint()}
			>
				{#snippet action()}
					<Button variant="solid" icon={IconPlus} href={catalogHref()}>
						{m.elearning_add_course()}
					</Button>
				{/snippet}
			</EmptyState>
		{:else if list && list.groups.length === 0}
			<EmptyState
				size="page"
				icon={list.filter.kind === 'favourites' ? IconStarDuotone : IconFunnelX}
				title={list.filter.kind === 'favourites'
					? m.elearning_no_favourites()
					: m.elearning_filter_no_courses()}
				body={list.filter.kind === 'favourites'
					? m.elearning_no_favourites_hint()
					: m.elearning_filter_change_hint()}
			>
				{#snippet action()}
					<Button onclick={showAll}>{m.elearning_show_all()}</Button>
				{/snippet}
			</EmptyState>
		{:else}
			<!-- One notice when both fail together, as offline: the courses' says it. -->
			{#if deadlines.error && !courses.error}
				<Banner tone="warning" role="status" message={deadlinesNotice}>
					{#snippet actions()}
						<Button
							variant="ghost"
							size="sm"
							icon={IconArrowClockwise}
							loading={deadlines.refreshing}
							onclick={deadlines.refresh}
						>
							{m.common_retry()}
						</Button>
					{/snippet}
				</Banner>
			{/if}
			<ul class="grid" role="list">
				{#each list?.groups ?? [] as group (group.key)}
					<li
						class="cell view-enter"
						data-group={group.key}
						animate:flip={{ duration: 260 * motion }}
					>
						<CourseCard
							{group}
							deadlines={deadlinesByCourse}
							starred={group.editions.some(isFavourite)}
							onstar={star}
							{now}
							revealed={revealed === group.key}
						/>
					</li>
				{/each}
			</ul>
		{/if}
		{#if canAdd}
			<Fab icon={IconPlus} label={m.elearning_add_course()} href={catalogHref()} />
		{/if}
	</Page>
</PullToRefresh>

<style>
	.filters {
		flex: 1;
		min-inline-size: 0;
	}

	.chips-skeleton {
		display: flex;
		gap: 8px;
		overflow: hidden;
	}

	/*
	 * Each card spans four rows (tabs, box, deadlines, the gap below) and shares them with the
	 * cards beside it, so a row's boxes line up; the last gap is taken back.
	 */
	.grid {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(min(100%, 290px), 1fr));
		grid-auto-rows: auto auto auto var(--sp-5);
		column-gap: var(--sp-4);
		margin: 0 0 calc(-1 * var(--sp-5));
		padding: 0;
		list-style: none;
	}

	.cell {
		display: grid;
		grid-row: span 4;
		grid-template-rows: subgrid;
		row-gap: 0;
		min-inline-size: 0;
	}
</style>
