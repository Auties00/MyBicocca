<script lang="ts">
	import { tick } from 'svelte';
	import { MediaQuery } from 'svelte/reactivity';
	import { flip } from 'svelte/animate';
	import { fly } from 'svelte/transition';
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconPlus from '~icons/ph/plus';
	import IconStar from '~icons/ph/star';
	import IconStarDuotone from '~icons/ph/star-duotone';
	import IconFunnelX from '~icons/ph/funnel-x-duotone';
	import IconGraduationCap from '~icons/ph/graduation-cap-duotone';
	import IconArrowDown from '~icons/ph/arrow-down';
	import { academicYear } from '#lib/data/academicYear.ts';
	import { now as minute } from '#lib/data/clock.svelte.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { formatRelative } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { getLocale } from '#lib/paraglide/runtime.js';
	import Badge from '#lib/ui/Badge.svelte';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { CourseCareers, type CareerLike } from '../careers.ts';
	import { storedFilter } from '../choices.svelte.ts';
	import {
		ALL,
		filterCourses,
		filterToken,
		parseFilter,
		sectionsByPeriod,
		toEnrolledCourses,
		type CourseFilter,
		type CourseGroup
	} from '../courses.ts';
	import { recentDeadlines } from '../deadlines.ts';
	import { favouritesOf } from '../favourites.svelte.ts';
	import { catalogHref, coursesHref } from '../links.ts';
	import { PullToRefresh } from '../pullToRefresh.svelte.ts';
	import { courseDeadlinesQuery, enrolledCoursesQuery, refreshCourses } from '../queries.ts';
	import { pendingReveal, revealDone } from '../reveal.ts';
	import CourseCard from './CourseCard.svelte';
	import CourseCardSkeleton from './CourseCardSkeleton.svelte';
	import CourseFilterBar, { type FilterOption } from './CourseFilterBar.svelte';
	import { compact, reducedMotion } from '#lib/ui/media.ts';

	const courses = query(() => session.current && enrolledCoursesQuery(session.current));
	const deadlines = query(() => session.current && courseDeadlinesQuery(session.current));

	const coarse = new MediaQuery('(pointer: coarse)', false);
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
	const sections = $derived(list ? sectionsByPeriod(list.groups) : []);
	const groupCount = $derived(
		enrolled && careers ? filterCourses(enrolled, ALL, careers, isFavourite).groups.length : 0
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
		const count = (filter: CourseFilter) =>
			filterCourses(enrolled, filter, careers, isFavourite).groups.length;
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
		return options.map(({ filter, label, icon }) => ({
			token: filterToken(filter),
			label,
			icon,
			count: count(filter)
		}));
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

	/* Whether the running refresh came from a pull, which shows its own spinner. */
	let pulled = $state(false);

	function refresh() {
		pulled = false;
		if (scope) refreshCourses(scope);
	}

	const pull = new PullToRefresh(() => {
		refresh();
		pulled = true;
	});
	const refreshing = $derived(courses.refreshing || deadlines.refreshing);
	const updatedAt = $derived(courses.updatedAt ? new Date(courses.updatedAt) : undefined);
	/* The minute tick can trail a fresh load: "updated" never reads as in the future. */
	const clock = $derived(
		new Date(Math.max(now.getTime(), courses.updatedAt ?? 0, deadlines.updatedAt ?? 0))
	);
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
	/* On phones adding a course is the floating button, and a pull refreshes on touch. */
	const showRefresh = $derived(!(compact.current && coarse.current));
	const showAdd = $derived(canAdd && !compact.current);
	const subtitle = $derived.by(() => {
		if (courses.data === undefined) return undefined;
		const time = updatedAt && formatRelative(updatedAt, clock);
		if (groupCount === 0) return time && m.ui_updated_ago({ time });
		const count = m.elearning_course_count({ count: groupCount });
		return time ? `${count} · ${m.elearning_updated({ time })}` : count;
	});
	const currentYear = academicYear();
	const motion = $derived(reducedMotion.current ? 0 : 1);
</script>

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

{#if pull.distance > 0 || (pulled && refreshing)}
	<div
		class={['pull', { armed: pull.armed, spinning: pull.distance === 0 }]}
		style:--pull="{pull.distance}px"
		role="status"
		aria-label={pull.armed ? m.elearning_pull_to_refresh() : m.common_refresh()}
	>
		{#if pull.distance > 0}
			<IconArrowDown aria-hidden="true" />
		{:else}
			<IconArrowClockwise aria-hidden="true" />
		{/if}
	</div>
{/if}

<div class="pull-area" {@attach pull.attach}>
	<Page width="dashboard">
		<PageHeader
			title={m.elearning_title()}
			{subtitle}
			loading={courses.data === undefined && courses.status !== 'error' && { eyebrow: false }}
			actions={headerActions}
		>
			{#snippet meta()}
				{#if list && visible.length > 0}
					<CourseFilterBar
						options={filterOptions}
						value={filterToken(list.filter)}
						label={m.elearning_filter_label()}
						onchange={choose}
					/>
				{:else if loading}
					<div class="chips-skeleton" aria-hidden="true">
						{#each [64, 92, 72, 72] as width, index (index)}
							<Skeleton width="{width}px" height="32px" radius="999px" />
						{/each}
					</div>
				{/if}
			{/snippet}
		</PageHeader>

		<!-- Fixed over the list on phones, it comes right after the header for the keyboard too. -->
		{#if canAdd}
			<div class="fab">
				<Button variant="solid" size="lg" icon={IconPlus} href={catalogHref()}>
					{m.elearning_add_course()}
				</Button>
			</div>
		{/if}

		<!-- The header's subtitle already says how old the list is. -->
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
			<div class="period" role="status" aria-busy="true">
				<span class="sr-only">{m.common_loading()}</span>
				{#if gate.visible}
					<div class="period-head"><Skeleton width="220px" height="24px" /></div>
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
			{#each sections as section (section.year)}
				{@const headingId = `period-${section.year ?? 'none'}`}
				<section class="period" aria-labelledby={headingId}>
					<header class="period-head">
						<h2 class="period-title" id={headingId}>
							{section.label === null
								? m.elearning_period_none_section()
								: m.elearning_period_section({ year: section.label })}
						</h2>
						{#if section.year === currentYear}
							<Badge tone="accent">{m.elearning_period_current()}</Badge>
						{/if}
						<span class="period-count"
							>{m.elearning_course_count({ count: section.groups.length })}</span
						>
						{#if section.year === null}
							<p class="period-hint">{m.elearning_period_none_hint()}</p>
						{/if}
					</header>
					<ul class="grid" role="list">
						{#each section.groups as group (group.key)}
							<li
								class="cell"
								data-group={group.key}
								animate:flip={{ duration: 260 * motion }}
								in:fly={{ y: 12, duration: 220 * motion }}
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
				</section>
			{/each}
		{/if}
		{#if canAdd}
			<div class="fab-space" aria-hidden="true"></div>
		{/if}
	</Page>
</div>

<style>
	.pull-area {
		display: contents;
	}

	.chips-skeleton {
		display: flex;
		gap: 8px;
		padding-block: 4px;
		overflow: hidden;
	}

	.period {
		display: grid;
		gap: var(--sp-4);
	}

	.period-head {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 4px 10px;
	}

	.period-title {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
		font-variant-numeric: tabular-nums;
	}

	.period-count {
		margin-inline-start: auto;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	/* On phones the chips already count the courses: the heading keeps its line. */
	@media (width < 600px) {
		.period-count {
			display: none;
		}
	}

	.period-hint {
		flex-basis: 100%;
		color: var(--text-muted);
		font: var(--type-meta);
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

	.fab,
	.fab-space {
		display: none;
	}

	@media (width < 600px) {
		.fab {
			position: fixed;
			inset-inline-end: 16px;
			inset-block-end: calc(var(--shell-inset-bottom) + 12px);
			z-index: var(--z-nav);
			display: block;
		}

		.fab :global(.btn) {
			box-shadow: var(--elev-3);
		}

		/* Room for the button over the last card. */
		.fab-space {
			display: block;
			block-size: 40px;
		}
	}

	.pull {
		position: fixed;
		inset-block-start: calc(var(--shell-inset-top) + 8px);
		inset-inline-start: 50%;
		z-index: var(--z-band);
		display: grid;
		place-items: center;
		inline-size: 40px;
		block-size: 40px;
		border-radius: var(--r-pill);
		background: var(--surface-raised);
		color: var(--text-muted);
		box-shadow: var(--elev-2);
		translate: -50% calc(var(--pull) - 24px);
		rotate: calc(var(--pull) * 2.5deg);
		pointer-events: none;
	}

	.pull :global(svg) {
		inline-size: 20px;
		block-size: 20px;
	}

	.pull.armed {
		color: var(--accent-text);
	}

	.pull.spinning {
		translate: -50% 24px;
		rotate: 0deg;
		color: var(--accent-text);
		animation: spin 900ms linear infinite;
	}

	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.pull.spinning {
			animation: none;
		}
	}
</style>
