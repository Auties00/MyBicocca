<script lang="ts">
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconCalendarCheck from '~icons/ph/calendar-check-duotone';
	import IconFunnelX from '~icons/ph/funnel-x-duotone';
	import IconMagnifyingGlass from '~icons/ph/magnifying-glass';
	import { goto } from '$app/navigation';
	import { resolve } from '$app/paths';
	import { page } from '$app/state';
	import { now } from '#lib/data/clock.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import FilterChip from '#lib/ui/FilterChip.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import { useDeadlines } from '../deadlines.svelte.ts';
	import { DEADLINE_SOURCES, parseKinds, type DeadlineSource } from '../deadlines.ts';
	import { kindFilters, staleLine } from '../labels.ts';
	import DeadlineCalendar from './DeadlineCalendar.svelte';
	import DeadlinesError from './DeadlinesError.svelte';
	import DeadlinesSkeleton from './DeadlinesSkeleton.svelte';
	import DeadlineTimeline from './DeadlineTimeline.svelte';
	import PartialNotice from './PartialNotice.svelte';
	import StaleNotice from './StaleNotice.svelte';
	import { compact } from '#lib/ui/media.ts';

	const deadlines = useDeadlines();
	const gate = new LoadingGate(() => deadlines.status === 'pending');
	/**
	 * The skeleton holds the timeline's place from the start and fades in after 150 ms. Everything
	 * that depends on the data (subtitle, filters, list) follows this flag, so it all changes in
	 * the frame that removes the skeleton and nothing on screen shifts.
	 */
	const loading = $derived(deadlines.status === 'pending' || gate.visible);

	const kinds = $derived(parseKinds(page.url.searchParams.get('kind')));
	const all = $derived(deadlines.data ?? []);
	const shown = $derived(kinds.size === 0 ? all : all.filter((item) => kinds.has(item.source)));
	/** Only the kinds there are, each with its count. */
	const present = $derived(
		DEADLINE_SOURCES.flatMap((source) => {
			const count = all.filter((item) => item.source === source).length;
			return count === 0 && !kinds.has(source) ? [] : [{ source, count }];
		})
	);
	/** An empty list counts only when every source is in it. */
	const unchecked = $derived(
		deadlines.data === undefined || (all.length === 0 && !deadlines.complete)
	);
	const filterable = $derived(!loading && present.length > 1);
	/*
	 * Beside a phone's title a button would wrap under the longer subtitles and move the list, so
	 * there a failed refresh is retried from the notice below it.
	 */

	/**
	 * Always one line, so the header keeps its height through every state. It turns into the
	 * warning when a refresh fails over the data shown; with nothing listed, the empty or error
	 * state below says it once.
	 */
	const subtitle = $derived.by(() => {
		if (loading) return m.common_loading();
		if (unchecked || all.length === 0) return m.services_deadlines_subtitle_unknown();
		if (deadlines.stale && deadlines.updatedAt !== undefined)
			return staleLine(deadlines.updatedAt, now.current);
		if (shown.length < all.length)
			return m.services_deadlines_subtitle_filtered({ shown: shown.length, count: all.length });
		return m.services_deadlines_subtitle({ count: all.length });
	});

	/** The filter lives in the URL, so a reload keeps it; changing it replaces the history entry. */
	function setKinds(next: readonly DeadlineSource[]) {
		const kind = DEADLINE_SOURCES.filter((source) => next.includes(source)).join(',');
		const target =
			kind === '' ? resolve('services/deadlines') : resolve(`services/deadlines?kind=${kind}`);
		void goto(target, { replace: true, reset: false });
	}

	function toggle(source: DeadlineSource, selected: boolean) {
		const rest = [...kinds].filter((item) => item !== source);
		setKinds(selected ? [...rest, source] : rest);
	}
</script>

<svelte:head>
	<title>{m.shell_service_deadlines()} · {m.app_name()}</title>
</svelte:head>

<!-- The row keeps its height in every state, chips or not, so the list below never moves. -->
{#snippet filters()}
	<div
		class="filters"
		role={filterable ? 'group' : undefined}
		aria-label={filterable ? m.services_deadlines_filters() : undefined}
	>
		{#if filterable}
			{#each present as { source, count } (source)}
				<FilterChip
					label={kindFilters[source]()}
					{count}
					selected={kinds.has(source)}
					onchange={(selected) => toggle(source, selected)}
				/>
			{/each}
		{/if}
	</div>
{/snippet}

{#snippet refresh()}
	<IconButton
		icon={IconArrowClockwise}
		label={m.services_refresh()}
		variant="outline"
		loading={deadlines.refreshing}
		disabled={loading}
		onclick={() => void deadlines.refresh()}
	/>
{/snippet}

<Page width="dashboard">
	<PageHeader
		title={m.shell_service_deadlines()}
		{subtitle}
		back={{ href: shellHref('services'), label: m.shell_services() }}
		actions={!compact.current ? refresh : undefined}
		meta={filters}
	/>

	<div class="layout">
		<div class="main">
			{#if loading}
				<DeadlinesSkeleton rows={5} />
			{:else if unchecked}
				<DeadlinesError {deadlines} />
			{:else}
				<PartialNotice {deadlines} />
				{#if shown.length > 0}
					<DeadlineTimeline deadlines={shown} today={deadlines.today} />
				{:else if all.length > 0}
					<EmptyState icon={IconFunnelX} title={m.services_deadlines_filtered_none()}>
						{#snippet action()}
							<Button onclick={() => setKinds([])}>{m.services_deadlines_show_all()}</Button>
						{/snippet}
					</EmptyState>
				{:else}
					<EmptyState
						size="page"
						icon={IconCalendarCheck}
						title={m.services_deadlines_none_title()}
						body={m.services_deadlines_none_body()}
					>
						{#snippet action()}
							<Button icon={IconMagnifyingGlass} href={resolve('services/exams/book')}>
								{m.services_deadlines_none_action()}
							</Button>
						{/snippet}
					</EmptyState>
				{/if}
				{#if all.length > 0}<p class="scope">{m.services_deadlines_intro()}</p>{/if}
				<StaleNotice {deadlines} />
			{/if}
		</div>

		{#if !loading && all.length > 0}
			<aside class="aside" aria-label={m.services_calendar_label()}>
				<DeadlineCalendar deadlines={shown} today={deadlines.today} />
			</aside>
		{/if}
	</div>
</Page>

<style>
	/*
	 * On phones the chips scroll sideways, edge to edge. The width comes from the header (a size
	 * container), not from the chips, so a long row scrolls instead of widening the page. The block
	 * padding leaves room for the focus ring, which the scroller would otherwise clip.
	 */
	.filters {
		display: flex;
		gap: 8px;
		/* A FilterChip's 32 px and the padding. */
		min-block-size: 40px;
		inline-size: calc(100cqi + 2 * var(--gutter));
		margin: -4px calc(-1 * var(--gutter));
		padding: 4px var(--gutter);
		overflow-x: auto;
		scrollbar-width: none;
	}

	.layout {
		display: grid;
		gap: var(--sp-7);
		align-items: start;
	}

	.main {
		display: grid;
		gap: var(--sp-5);
		min-inline-size: 0;
		max-inline-size: var(--w-reading);
	}

	.scope {
		max-inline-size: 60ch;
		padding-inline: 4px;
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}

	.aside {
		display: none;
	}

	@container page (width >= 900px) {
		.layout {
			grid-template-columns: minmax(0, 1fr) 280px;
		}

		.main {
			max-inline-size: none;
		}

		.aside {
			display: block;
			position: sticky;
			inset-block-start: calc(var(--shell-inset-top) + var(--sp-4));
		}
	}

	/* The dashboard's full width, as the overview beside the same column. */
	@container page (width >= 1100px) {
		.layout {
			grid-template-columns: minmax(0, 1fr) 340px;
			column-gap: var(--sp-8);
		}
	}
</style>
