<script lang="ts">
	import type { Snippet } from 'svelte';
	import { afterNavigate, goto } from '$app/navigation';
	import { resolve } from '$app/paths';
	import { page } from '$app/state';
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconBooks from '~icons/ph/books-duotone';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import ListDetail from '#lib/ui/ListDetail.svelte';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import SegmentedControl from '#lib/ui/SegmentedControl.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { setCourseFrame } from '../courseFrame.ts';
	import { Libretto } from '../libretto.svelte.ts';
	import { prerequisitesQuery } from '../queries.ts';
	import { groupByYear } from '../transcript.ts';
	import { courseHref, creditsOf, staleMessage, type LibrettoView } from './labels.ts';
	import LibrettoTable from './LibrettoTable.svelte';
	import LibrettoYear from './LibrettoYear.svelte';
	import { expanded } from '#lib/ui/media.ts';

	interface Props {
		/** The open course's `adsceId`. */
		selected?: number;
		/** The open course: beside the list where there is room, instead of it otherwise. */
		detail?: Snippet;
	}

	let { selected, detail }: Props = $props();

	/* The table needs room for its columns, in the page's own width (the sidebar narrows it). */
	const TABLE_WIDTH = 640;

	const libretto = new Libretto();
	let width = $state(0);
	let twoPanes = $state<boolean>();
	/*
	 * Before ListDetail first measures its panes, its own guess (two panes from the expanded size
	 * class) decides the header and the course's frame, so neither changes after the first paint.
	 */
	const docked = $derived(twoPanes ?? expanded.current);
	const table = $derived(width >= TABLE_WIDTH);
	setCourseFrame({
		get docked() {
			return docked;
		}
	});

	const view = $derived<LibrettoView>(
		page.url.searchParams.get('view') === 'credits' ? 'credits' : 'grades'
	);
	/* One order for the table, the list and J/K: passed courses as on a paper libretto, by date. */
	const groups = $derived(libretto.plan && groupByYear(libretto.plan, 'date'));
	const pendingIds = $derived(
		libretto.plan?.filter((row) => row.state !== 'passed').map((row) => row.id) ?? []
	);
	const prerequisites = query(
		() =>
			session.current && pendingIds.length > 0 && prerequisitesQuery(session.current, pendingIds)
	);

	const subtitle = $derived.by(() => {
		const summary = libretto.summary;
		if (summary === undefined) return undefined;
		return m.profile_libretto_subtitle({
			passed: summary.passedExams,
			total: summary.plannedExams,
			credits: creditsOf(summary.passedCredits, summary.totalCredits)
		});
	});

	const listHref = $derived(
		view === 'credits' ? resolve('profile/exams?view=credits') : resolve('profile/exams')
	);
	const order = $derived(
		groups?.flatMap((group) => group.rows.map((row) => courseHref(row, view)))
	);

	let listRoot = $state<HTMLElement>();
	let overview = $state<HTMLElement>();
	/* Plain, not state: the course open before the last navigation. */
	let opened: number | undefined;
	/* Plain, not state: which course the docked list was last scrolled to. */
	let revealed: { pane: HTMLElement; id: number } | undefined;

	/*
	 * A course opened from a link or a reload brings its row into view in the docked list, once:
	 * refreshes of the list later leave the user's own scrolling alone.
	 */
	$effect(() => {
		// The list's own scroller: ListDetail's pane around it.
		const pane = twoPanes ? listRoot?.parentElement : undefined;
		if (!pane || selected === undefined || groups === undefined) return;
		if (revealed?.pane === pane && revealed.id === selected) return;
		const row = pane.querySelector('[aria-current="page"]');
		if (!row) return;
		revealed = { pane, id: selected };
		const top = row.getBoundingClientRect().top - pane.getBoundingClientRect().top;
		if (top < 0 || top > pane.clientHeight - 96) pane.scrollTop += top - pane.clientHeight / 3;
	});

	/*
	 * A course closed where the table has room gives way to the table rather than to the list, so
	 * the list can't give the focus back to its row: the table does, synchronously, so the shell
	 * leaves it there.
	 */
	afterNavigate(() => {
		const closed = opened;
		opened = selected;
		if (closed === undefined || selected !== undefined || !overview) return;
		const row = overview.querySelector<HTMLElement>(
			`a[href="${CSS.escape(courseHref({ id: closed }, view))}"]`
		);
		row?.focus({ preventScroll: true });
		row?.scrollIntoView({ block: 'nearest' });
	});

	function setView(next: LibrettoView) {
		let target;
		if (selected === undefined) {
			target =
				next === 'credits' ? resolve('profile/exams?view=credits') : resolve('profile/exams');
		} else {
			target = courseHref({ id: selected }, next);
		}
		void goto(target, { replace: true, reset: false });
	}
</script>

{#snippet body(asTable: boolean)}
	{#if libretto.staleError}
		<ErrorState
			variant="stale"
			message={staleMessage(libretto.staleError)}
			updatedAt={libretto.updatedAt}
			onretry={libretto.refresh}
			retrying={libretto.refreshing}
		/>
	{/if}

	{#if libretto.failed}
		<ErrorState
			title={m.profile_load_error()}
			message={libretto.error ? errorMessage(libretto.error) : m.profile_refresh_error()}
			onretry={libretto.refresh}
			retrying={libretto.refreshing}
		/>
	{:else}
		{#if libretto.statsFailed}
			<!-- The rows stand on their own: only the totals in the subtitle are missing. -->
			<ErrorState
				variant="stale"
				message={m.profile_stats_error()}
				onretry={libretto.stats.refresh}
				retrying={libretto.stats.refreshing}
			/>
		{/if}
		{#if groups === undefined}
			<div class="loading" aria-busy="true">
				{#each [7, 9, 6] as count, group (group)}
					<div class="skeleton-group">
						<Skeleton height={asTable ? '116px' : '76px'} radius="var(--r-lg)" />
						{#each { length: count }, index (index)}
							<Skeleton height="60px" radius="var(--r-2xs)" />
						{/each}
					</div>
				{/each}
			</div>
		{:else if groups.length === 0}
			<EmptyState
				size="page"
				icon={IconBooks}
				title={m.profile_libretto_empty_title()}
				body={m.profile_libretto_empty_body()}
			>
				{#snippet action()}
					<Button href={resolve('services/study-plan')}>{m.profile_libretto_empty_action()}</Button>
				{/snippet}
			</EmptyState>
		{:else if asTable}
			<LibrettoTable {groups} {view} prerequisites={prerequisites.data} />
		{:else}
			{#each groups as group (group.year)}
				<LibrettoYear {group} {view} {selected} prerequisites={prerequisites.data} />
			{/each}
		{/if}
	{/if}
{/snippet}

{#snippet list()}
	<div class="libretto" bind:this={listRoot}>
		<div class={['toolbar', { docked, idle: libretto.failed || groups?.length === 0 }]}>
			<SegmentedControl
				label={m.profile_view_label()}
				full
				options={[
					{ value: 'grades', label: m.profile_grades() },
					{ value: 'credits', label: m.profile_credits() }
				]}
				value={view}
				onchange={setView}
			/>
		</div>
		{@render body(false)}
	</div>
{/snippet}

<!-- With a course open, the course names the page. -->
<svelte:head>
	{#if detail === undefined}
		<title>{m.profile_libretto_title()} · {m.app_name()}</title>
	{/if}
</svelte:head>

<Page width="dashboard">
	{#if docked || detail === undefined}
		<PageHeader
			title={m.profile_libretto_title()}
			subtitle={subtitle ?? ''}
			loading={subtitle === undefined && !libretto.statsFailed ? { eyebrow: false } : false}
			back={{ href: resolve('profile'), label: m.profile_title() }}
		>
			{#snippet actions()}
				<IconButton
					icon={IconArrowClockwise}
					label={m.common_refresh()}
					variant="outline"
					loading={libretto.refreshing}
					onclick={libretto.refresh}
				/>
			{/snippet}
		</PageHeader>
	{/if}
	<div bind:clientWidth={width}>
		{#if detail || !table}
			<ListDetail
				{list}
				{detail}
				listWidth={380}
				detailMin={448}
				listLabel={m.profile_libretto_title()}
				detailLabel={m.profile_course_label()}
				current={selected === undefined ? undefined : courseHref({ id: selected }, view)}
				items={order}
				closeHref={listHref}
				bind:twoPanes
			/>
		{:else}
			<div class="overview" bind:this={overview}>{@render body(true)}</div>
		{/if}
	</div>
</Page>

<style>
	.libretto {
		display: grid;
		gap: var(--sp-6);
		container: libretto / inline-size;
	}

	.toolbar {
		position: sticky;
		inset-block-start: var(--shell-inset-top);
		z-index: 1;
		padding-block: 8px;
		background: var(--bg);
	}

	.toolbar.docked {
		inset-block-start: 0;
	}

	/* Nothing to switch between. */
	.toolbar.idle {
		display: none;
	}

	/* Wide lists show the grade and the credits side by side: no switch needed. */
	@container libretto (width >= 560px) {
		.toolbar {
			display: none;
		}
	}

	.overview,
	.loading {
		display: grid;
		gap: var(--sp-6);
	}

	.skeleton-group {
		display: grid;
		gap: 2px;
	}
</style>
