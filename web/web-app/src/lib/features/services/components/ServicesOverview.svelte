<script lang="ts">
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconCalendarCheck from '~icons/ph/calendar-check-duotone';
	import IconMagnifyingGlass from '~icons/ph/magnifying-glass';
	import { resolve } from '$app/paths';
	import { now } from '#lib/data/clock.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import SectionHeader from '#lib/ui/SectionHeader.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import type { DeadlinesQuery } from '../deadlines.svelte.ts';
	import { staleLine } from '../labels.ts';
	import DeadlineCalendar from './DeadlineCalendar.svelte';
	import DeadlinesError from './DeadlinesError.svelte';
	import DeadlinesSkeleton from './DeadlinesSkeleton.svelte';
	import DeadlineTimeline from './DeadlineTimeline.svelte';
	import PartialNotice from './PartialNotice.svelte';
	import StaleNotice from './StaleNotice.svelte';
	import StatusTiles from './StatusTiles.svelte';

	interface Props {
		deadlines: DeadlinesQuery;
	}

	let { deadlines }: Props = $props();

	/** About as long as the month beside it; the rest is a click away. */
	const PREVIEW = 8;

	const gate = new LoadingGate(() => deadlines.status === 'pending');
	/** Skeletons hold the final layout from the start; they fade in only after 150 ms. */
	const loading = $derived(deadlines.status === 'pending' || gate.visible);
	const all = $derived(deadlines.data ?? []);
	const upcoming = $derived(all.slice(0, PREVIEW));
	/** An empty list counts only when every source is in it. */
	const unchecked = $derived(
		deadlines.data === undefined || (all.length === 0 && !deadlines.complete)
	);
	/** The month holds its column while loading and whenever there is a deadline to put on it. */
	const month = $derived(loading || (!unchecked && all.length > 0));
	/** Always one line, so the header keeps its height. */
	const subtitle = $derived(
		!loading && deadlines.stale && deadlines.updatedAt !== undefined
			? staleLine(deadlines.updatedAt, now.current)
			: m.services_subtitle()
	);
</script>

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

<!--
	Beside the services column, which already lists every service with its badge: what stands at
	each group, then what is coming, with the month beside it on wide screens. Nothing sits below
	the timeline but its own notices, so whatever replaces a skeleton moves nothing.
-->
<Page width="dashboard">
	<PageHeader title={m.shell_services()} {subtitle} actions={refresh} />

	<section aria-labelledby="services-glance">
		<SectionHeader id="services-glance" title={m.services_glance_title()} />
		<StatusTiles {deadlines} {loading} />
	</section>

	<section aria-labelledby="services-upcoming">
		<SectionHeader
			id="services-upcoming"
			title={m.services_upcoming_title()}
			link={{ href: shellHref('services/deadlines'), label: m.services_upcoming_all() }}
		/>
		<div class={['board', { month }]}>
			<div class="list">
				{#if loading}
					<DeadlinesSkeleton rows={PREVIEW} dense />
				{:else if unchecked}
					<div class="card"><DeadlinesError {deadlines} /></div>
				{:else}
					{#if upcoming.length > 0}
						<PartialNotice {deadlines} />
						<DeadlineTimeline deadlines={upcoming} today={deadlines.today} dense anchors />
					{:else}
						<div class="card">
							<EmptyState
								icon={IconCalendarCheck}
								title={m.services_deadlines_none_title()}
								body={m.services_deadlines_none_body()}
							>
								{#snippet action()}
									<Button
										variant="tonal"
										icon={IconMagnifyingGlass}
										href={resolve('services/exams/book')}
									>
										{m.services_deadlines_none_action()}
									</Button>
								{/snippet}
							</EmptyState>
						</div>
					{/if}
					<StaleNotice {deadlines} />
				{/if}
			</div>
			{#if month}
				<aside class="aside" aria-label={m.services_calendar_label()}>
					{#if loading}
						<Skeleton height="372px" radius="var(--r-lg)" />
					{:else}
						<DeadlineCalendar deadlines={all} today={deadlines.today} />
					{/if}
				</aside>
			{/if}
		</div>
	</section>
</Page>

<style>
	section {
		display: grid;
		gap: var(--sp-3);
		align-content: start;
		min-inline-size: 0;
	}

	.board,
	.list {
		display: grid;
		gap: var(--sp-5);
		align-items: start;
		min-inline-size: 0;
	}

	.card {
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.aside {
		display: none;
	}

	/* Wide enough for the month beside the timeline, as on the deadlines page. */
	@container page (width >= 880px) {
		.board.month {
			grid-template-columns: minmax(0, 1fr) 320px;
			column-gap: var(--sp-7);
		}

		.aside {
			display: block;
			position: sticky;
			inset-block-start: calc(var(--shell-inset-top) + var(--sp-4));
		}
	}

	@container page (width >= 1100px) {
		.board.month {
			grid-template-columns: minmax(0, 1fr) 340px;
			column-gap: var(--sp-8);
		}
	}
</style>
