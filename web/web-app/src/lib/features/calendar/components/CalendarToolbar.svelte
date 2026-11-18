<script lang="ts">
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconCaretLeft from '~icons/ph/caret-left';
	import IconCaretRight from '~icons/ph/caret-right';
	import IconMagnifyingGlassMinus from '~icons/ph/magnifying-glass-minus';
	import IconMagnifyingGlassPlus from '~icons/ph/magnifying-glass-plus';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import SegmentedControl from '#lib/ui/SegmentedControl.svelte';
	import type { CalendarView } from '../nav.svelte.ts';

	interface Props {
		view: CalendarView;
		/** The period on screen, as a title: "Ottobre 2026". */
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

<div class="toolbar">
	<div class={['layout', { docked }]}>
		<div class="period">
			<h1 class="sr-only">{m.calendar_title()}</h1>
			<!-- Only a title: the side column and, in month, the grid are the date pickers. -->
			<span class="title" aria-live="polite">{title}</span>
			<IconButton
				icon={IconArrowClockwise}
				label={refreshing ? m.calendar_refreshing() : m.calendar_refresh()}
				size="sm"
				loading={refreshing}
				onclick={onrefresh}
			/>
		</div>
		<div class="nav">
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
</div>

<style>
	.toolbar {
		container: calendar-toolbar / inline-size;
	}

	/* Two rows, the same in every view: the period (and the density, where it is here), then the
	   paging and the views. One row once the whole set fits. */
	.layout {
		display: grid;
		grid-template-columns: minmax(0, 1fr) auto;
		grid-template-areas:
			'period zoom'
			'nav views';
		align-items: center;
		gap: 8px 16px;
	}

	/* The views take the width of the docked panel (set by the calendar), from the handle's line to
	   the panel's right edge; without a panel they take what they need. */
	@container calendar-toolbar (width >= 900px) {
		.layout {
			grid-template-columns: minmax(0, 1fr) auto auto var(--views-w, auto);
			grid-template-areas: 'period nav zoom views';
			column-gap: 0;
		}

		.nav {
			margin-inline-end: 16px;
		}
	}

	.period {
		grid-area: period;
		display: flex;
		align-items: center;
		gap: 8px;
		min-inline-size: 0;
	}

	.nav {
		grid-area: nav;
		display: flex;
		align-items: center;
		gap: 8px;
	}

	.views {
		grid-area: views;
		justify-self: stretch;
	}

	.title {
		min-inline-size: 0;
		padding-block: 4px;
		overflow: hidden;
		color: var(--text);
		font: 650 1.625rem / 1.2 var(--font-ui);
		letter-spacing: -0.02em;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.steps,
	.zoom {
		display: flex;
		gap: 2px;
	}

	/* The density sits at the foot of the side column; this copy is for when that column is hidden. */
	.zoom {
		grid-area: zoom;
		justify-self: end;
		display: none;
	}

	@container calendar (width < 1100px) {
		.layout:not(.docked) .zoom {
			display: flex;
		}
	}

	@container calendar (width < 1460px) {
		.layout.docked .zoom {
			display: flex;
		}
	}

	.zoom.idle {
		visibility: hidden;
	}

	@container calendar-toolbar (width >= 900px) {
		.zoom {
			margin-inline-end: 16px;
			padding-inline: 4px;
			border-inline: 1px solid var(--hairline);
		}
	}
</style>
