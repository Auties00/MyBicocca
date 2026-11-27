<script lang="ts">
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconCaretLeft from '~icons/ph/caret-left';
	import IconCaretRight from '~icons/ph/caret-right';
	import IconMagnifyingGlassMinus from '~icons/ph/magnifying-glass-minus';
	import IconMagnifyingGlassPlus from '~icons/ph/magnifying-glass-plus';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { expanded } from '#lib/ui/media.ts';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import SegmentedControl from '#lib/ui/SegmentedControl.svelte';
	import type { CalendarView } from '../nav.svelte.ts';

	interface Props {
		view: CalendarView;
		/** The period on screen, under the page title: "Ottobre 2026". */
		title: string;
		/** Whether today is on screen: the Today button rests then. */
		showsToday: boolean;
		refreshing: boolean;
		/** A panel is docked beside the calendar: the views then span the panel's width. */
		docked: boolean;
		zoom: number;
		zoomMin: number;
		zoomMax: number;
		onview: (view: CalendarView) => void;
		onstep: (delta: -1 | 1) => void;
		ontoday: () => void;
		onzoom: (factor: number) => void;
		onrefresh: () => void;
	}

	let {
		view,
		title,
		showsToday,
		refreshing,
		docked,
		zoom,
		zoomMin,
		zoomMax,
		onview,
		onstep,
		ontoday,
		onzoom,
		onrefresh
	}: Props = $props();

	const views = $derived([
		{ value: 'day' as const, label: m.calendar_view_day() },
		{ value: 'week' as const, label: m.calendar_view_week() },
		{ value: 'month' as const, label: m.calendar_view_month() }
	]);

	const previous = $derived(
		view === 'day'
			? m.calendar_previous_day()
			: view === 'week'
				? m.calendar_previous_week()
				: m.calendar_previous_month()
	);
	const next = $derived(
		view === 'day'
			? m.calendar_next_day()
			: view === 'week'
				? m.calendar_next_week()
				: m.calendar_next_month()
	);
</script>

<PageHeader title={m.calendar_title()}>
	{#snippet subtitle()}
		<!-- Plain text: the side column and, in month, the grid are the date pickers. -->
		<span aria-live="polite">{title}</span>
	{/snippet}
	{#snippet actions()}
		<div class={['controls', { docked }]}>
			<div class="nav">
				<!-- Below 1024 px a pull refreshes instead. -->
				{#if expanded.current}
					<IconButton
						icon={IconArrowClockwise}
						label={refreshing ? m.calendar_refreshing() : m.calendar_refresh()}
						loading={refreshing}
						onclick={onrefresh}
					/>
				{/if}
				<Button variant={showsToday ? 'ghost' : 'tonal'} onclick={ontoday} aria-keyshortcuts="T">
					{m.calendar_today()}
				</Button>
				<div class="steps">
					<IconButton
						icon={IconCaretLeft}
						label={previous}
						shortcut={['left']}
						onclick={() => onstep(-1)}
					/>
					<IconButton
						icon={IconCaretRight}
						label={next}
						shortcut={['right']}
						onclick={() => onstep(1)}
					/>
				</div>
			</div>
			<!-- Where the side column has no room, the density is here; otherwise it is at its foot. -->
			<div
				class={['zoom', { idle: view === 'month' }]}
				role="group"
				aria-label={m.calendar_density()}
			>
				<IconButton
					icon={IconMagnifyingGlassMinus}
					label={m.calendar_zoom_out()}
					shortcut={['-']}
					disabled={zoom <= zoomMin}
					onclick={() => onzoom(1 / 1.2)}
				/>
				<IconButton
					icon={IconMagnifyingGlassPlus}
					label={m.calendar_zoom_in()}
					shortcut={['+']}
					disabled={zoom >= zoomMax}
					onclick={() => onzoom(1.2)}
				/>
			</div>
			<div class="views">
				<SegmentedControl
					options={views}
					value={view}
					label={m.calendar_view_label()}
					full
					onchange={(value) => onview(value)}
				/>
			</div>
		</div>
	{/snippet}
</PageHeader>

<style>
	.controls {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 8px 16px;
	}

	.nav {
		display: flex;
		align-items: center;
		gap: 8px;
	}

	/* --views-w is set by WideCalendar beside a docked panel. */
	.controls.docked .views {
		inline-size: var(--views-w, auto);
	}

	.steps,
	.zoom {
		display: flex;
		gap: 2px;
	}

	/* The density sits at the foot of the side column; this copy is for when that column is hidden. */
	.zoom {
		display: none;
		padding-inline: 4px;
		border-inline: 1px solid var(--hairline);
	}

	@container calendar (width < 1100px) {
		.controls:not(.docked) .zoom {
			display: flex;
		}
	}

	@container calendar (width < 1460px) {
		.controls.docked .zoom {
			display: flex;
		}
	}

	.zoom.idle {
		visibility: hidden;
	}
</style>
