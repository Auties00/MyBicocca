<script lang="ts">
	import { on } from 'svelte/events';
	import { fly } from 'svelte/transition';
	import IconDotsSixVertical from '~icons/ph/dots-six-vertical';
	import IconMagnifyingGlassMinus from '~icons/ph/magnifying-glass-minus';
	import IconMagnifyingGlassPlus from '~icons/ph/magnifying-glass-plus';
	import { monthOf, monthWeeks, weekDays } from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import type { CalendarController } from '../controller.svelte.ts';
	import { displayTitle, fullDate } from '../format.ts';
	import { ZOOM_MAX, ZOOM_MIN } from '../layout.ts';
	import { weekendWeights } from '../weekColumns.ts';
	import AgendaList from './AgendaList.svelte';
	import CalendarTimeline from './CalendarTimeline.svelte';
	import CalendarToolbar from './CalendarToolbar.svelte';
	import DetailPanel from './DetailPanel.svelte';
	import EventPopover from './EventPopover.svelte';
	import MiniMonth from './MiniMonth.svelte';
	import MonthGrid from './MonthGrid.svelte';
	import SourceLegend from './SourceLegend.svelte';
	import SyncStatus from './SyncStatus.svelte';
	import TurnoPrompt from './TurnoPrompt.svelte';
	import WeekHeads from './WeekHeads.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { reducedMotion } from '#lib/ui/media.ts';

	interface Props {
		ctl: CalendarController;
	}

	let { ctl }: Props = $props();

	const view = $derived(ctl.view);
	const date = $derived(ctl.date);
	const data = $derived(ctl.data);
	const docked = $derived(ctl.docked);

	/*
	 * Saturday and Sunday take a full column where something is on them, else half of one: over the
	 * week in day and week, over every week shown in month. They animate as the view changes, and the
	 * head, the timeline and the month grid read them (weekColumns.css).
	 */
	const weekend = $derived(
		weekendWeights(
			view === 'month' ? monthWeeks(monthOf(date)).flat() : weekDays(date),
			data.byDay
		)
	);

	/*
	 * The wheel pages through the months, one at a time: a notch or a flick moves one, and what
	 * follows (a trackpad's glide) waits out the move.
	 */
	function pagesMonths(element: HTMLElement) {
		let settledAt = 0;
		let travelled = 0;
		let lastAt = 0;
		return on(
			element,
			'wheel',
			(event) => {
				if (event.ctrlKey || event.metaKey || Math.abs(event.deltaY) < Math.abs(event.deltaX)) return;
				event.preventDefault();
				const now = performance.now();
				if (now < settledAt) return;
				travelled = now - lastAt > 180 ? event.deltaY : travelled + event.deltaY;
				lastAt = now;
				if (Math.abs(travelled) < 48) return;
				ctl.step(travelled > 0 ? 1 : -1);
				travelled = 0;
				settledAt = now + 520;
			},
			{ passive: false }
		);
	}

	/* The details panel's width: the least it was, to a third of the window, kept between visits. */
	const PANE_KEY = 'mybicocca:calendar-pane';
	const PANE_STEP = 16;
	let frame = $state<HTMLElement>();
	let paneWidth = $state<number | undefined>(storedPane());
	let resizing = $state(false);
	/** The width the person dragged the panel to, for the bar above it too. */
	const paneUser = $derived(paneWidth === undefined ? undefined : `${paneWidth}px`);

	function storedPane(): number | undefined {
		try {
			const value = Number(localStorage.getItem(PANE_KEY));
			return Number.isFinite(value) && value > 0 ? value : undefined;
		} catch {
			return undefined;
		}
	}

	function keepPane() {
		try {
			if (paneWidth === undefined) localStorage.removeItem(PANE_KEY);
			else localStorage.setItem(PANE_KEY, String(paneWidth));
		} catch {
			// Not kept: the panel starts at its least next time.
		}
	}

	function paneBounds(): { min: number; max: number } {
		const min = frame ? parseFloat(getComputedStyle(frame).getPropertyValue('--pane-min')) : 340;
		return { min, max: Math.max(min, Math.floor(window.innerWidth / 3)) };
	}

	function setPane(width: number) {
		const { min, max } = paneBounds();
		paneWidth = Math.round(Math.min(max, Math.max(min, width)));
	}

	function startResize(event: PointerEvent) {
		if (event.button !== 0) return;
		event.preventDefault();
		(event.currentTarget as HTMLElement).setPointerCapture(event.pointerId);
		resizing = true;
	}

	function resize(event: PointerEvent) {
		if (!resizing || !frame) return;
		setPane(frame.getBoundingClientRect().right - event.clientX - GRIP / 2);
	}

	function endResize() {
		if (!resizing) return;
		resizing = false;
		keepPane();
	}

	function resizeKey(event: KeyboardEvent) {
		const { min, max } = paneBounds();
		const current = paneWidth ?? min;
		const next =
			event.key === 'ArrowLeft'
				? current + PANE_STEP
				: event.key === 'ArrowRight'
					? current - PANE_STEP
					: event.key === 'Home'
						? min
						: event.key === 'End'
							? max
							: undefined;
		if (next === undefined) return;
		event.preventDefault();
		setPane(next);
		keepPane();
	}

	function resetPane() {
		paneWidth = undefined;
		keepPane();
	}

	/** The handle's width, which the pane's left margin leaves free. */
	const GRIP = 14;
</script>

{#snippet monthHeads()}
	<WeekHeads
		mode="month"
		days={weekDays(date)}
		{date}
		today={ctl.today}
		byDay={data.byDay}
		onpick={(day) => ctl.goTo(day)}
	/>
{/snippet}

<div class={['bar', 'panes', { docked }]} style:--pane-user={paneUser}>
	<CalendarToolbar
		{view}
		title={ctl.barTitle}
		showsToday={ctl.todayOnScreen}
		refreshing={data.refreshing && data.hydrated}
		onrefresh={data.refresh}
		{docked}
		zoom={ctl.zoom}
		zoomMin={ZOOM_MIN}
		zoomMax={ZOOM_MAX}
		onview={ctl.setView}
		onstep={ctl.step}
		ontoday={() => ctl.goTo(ctl.today)}
		onzoom={ctl.zoomBy}
	/>
</div>
<SyncStatus {data} />
{#if !data.failed}
	<div
		class={['frame', 'panes', { docked, sided: view !== 'month', large: ctl.size === 'large', resizing }]}
		bind:this={frame}
		style:--pane-user={paneUser}
	>
		<!-- Always there: in month its column folds away and it fades, rather than vanishing. -->
		<div class="side-clip" inert={view === 'month'}>
			<aside class="side">
				<MiniMonth
					selected={date}
					today={ctl.today}
					range={ctl.period}
					byDay={data.byDay}
					onpick={(day) => ctl.goTo(day)}
				/>
				<SourceLegend events={ctl.periodEvents} libraryLinked={data.libraryLinked} />
				<div class="zoom" role="group" aria-label={m.calendar_density()}>
					<IconButton
						icon={IconMagnifyingGlassMinus}
						label={m.calendar_zoom_out()}
						shortcut={['-']}
						disabled={ctl.zoom <= ZOOM_MIN}
						onclick={() => ctl.zoomBy(1 / 1.2)}
					/>
					<IconButton
						icon={IconMagnifyingGlassPlus}
						label={m.calendar_zoom_in()}
						shortcut={['+']}
						disabled={ctl.zoom >= ZOOM_MAX}
						onclick={() => ctl.zoomBy(1.2)}
					/>
				</div>
			</aside>
		</div>
		<section
			class="main wk-scope"
			aria-label={ctl.title}
			style:--wk-sat={weekend[0]}
			style:--wk-sun={weekend[1]}
			style:--wk-figure={view === 'month' ? 0 : 1}
			style:--wk-gap={view === 'month' ? '4px' : '0px'}
		>
			{#if view === 'month'}
				<div class={['month', { split: !docked }]}>
					<div class="month-column">
						<div class="month-grid" {@attach pagesMonths}>
							<MonthGrid
								month={monthOf(date)}
								byDay={data.byDay}
								selected={date}
								today={ctl.today}
								variant="wide"
								heads={monthHeads}
								loading={ctl.loading}
								onselect={(day) => ctl.goTo(day)}
								onopen={ctl.openOnDay}
							/>
						</div>
						<div class="legends">
							<SourceLegend
								events={ctl.periodEvents}
								libraryLinked={data.libraryLinked}
								variant="inline"
							/>
						</div>
					</div>
					{#if !docked}
						<div class="month-agenda">
							<AgendaList
								{date}
								today={ctl.today}
								minute={ctl.now.minutes}
								events={ctl.dayEvents}
								loading={ctl.loading}
								courses={data.courses}
								selectedId={ctl.nav.eventId}
								heading="medium"
								onopen={ctl.open}
							/>
						</div>
					{/if}
				</div>
			{:else}
				<CalendarTimeline {ctl} />
				{#if ctl.prompt}
					{@const course = ctl.prompt}
					<div class="prompt" transition:fly={{ y: 16, duration: reducedMotion.current ? 0 : 200 }}>
						<TurnoPrompt
							{course}
							onchoose={(choice) => ctl.chooseTurno(course.activityCode, choice)}
						/>
					</div>
				{/if}
			{/if}
		</section>
		{#if docked}
			<!-- The gap between the calendar and the panel is the handle: any height of it can be grabbed. -->
			<!-- A focusable separator is a window splitter, which takes keys and pointer. -->
			<!-- svelte-ignore a11y_no_noninteractive_tabindex, a11y_no_noninteractive_element_interactions -->
			<div
				class="resizer"
				role="separator"
				aria-orientation="vertical"
				aria-label={m.calendar_pane_resize()}
				aria-valuenow={paneWidth ?? undefined}
				tabindex="0"
				onpointerdown={startResize}
				onpointermove={resize}
				onpointerup={endResize}
				onpointercancel={endResize}
				onkeydown={resizeKey}
				ondblclick={resetPane}
			>
				<span class="grip" aria-hidden="true"><IconDotsSixVertical /></span>
			</div>
			<aside
				class="pane"
				aria-label={ctl.selected ? displayTitle(ctl.selected) : fullDate(date)}
				bind:this={ctl.pane}
			>
				{#if ctl.nav.eventId}
					<DetailPanel
						event={ctl.selected}
						courses={data.courses}
						onclose={() => ctl.close(true)}
						onback={() => ctl.close(true)}
					/>
				{:else}
					<div class="pane-agenda">
						<AgendaList
							{date}
							today={ctl.today}
							minute={ctl.now.minutes}
							events={ctl.dayEvents}
							loading={ctl.loading}
							courses={data.courses}
							heading="medium"
							upcoming={ctl.comingUp}
							onopen={ctl.open}
						/>
					</div>
				{/if}
			</aside>
		{/if}
	</div>
{/if}

{#if ctl.anchored}
	<EventPopover
		eventId={ctl.nav.eventId}
		event={ctl.selected}
		courses={data.courses}
		onclose={ctl.close}
	/>
{/if}

<style>
	/* The toolbar spans the calendar, so it is the same in every view; under it the side column
	   (day and week, when there is room), the view, and the docked pane. */
	.bar {
		flex: none;
		padding: var(--sp-4) var(--gutter) var(--sp-3) var(--sp-5);
	}

	/*
	 * Beside a docked panel the views are as wide as the panel's least: from the handle's line (7px
	 * into the 14px gap) to the panel's right edge, which is the page's own margin short of the
	 * window's. They keep that width, and stay put, while the panel is dragged wider.
	 */
	.bar.docked {
		--views-w: calc(var(--pane-min) - 14px - var(--sp-3) + 7px - 2px);
		padding-inline-end: var(--sp-3);
	}

	/* The panel's size, which the bar above it needs too: its least, what was dragged, a third of the window. */
	.panes {
		/*
		 * The panel's least width, with the handle's 14px gap beside it. At 369px the handle's line falls
		 * where the toolbar's controls used to be separated, in every view; each pixel off this number
		 * moves the panel's edge by a pixel.
		 */
		--pane-min: 369px;
		--pane-w: clamp(
			var(--pane-min),
			var(--pane-user, var(--pane-min)),
			max(var(--pane-min), 33.333vw)
		);
	}

	/* It floats over the grid's foot: arriving with the lessons, it moves nothing. */
	.prompt {
		position: absolute;
		inset-inline: var(--sp-5) var(--gutter);
		inset-block-end: var(--sp-4);
		z-index: 6;
		max-inline-size: 720px;
		margin-inline: auto;
		border-radius: var(--r-md);
		box-shadow: var(--elev-3);
	}

	.frame {
		--side-w: 0px;
		position: relative;
		flex: 1;
		display: grid;
		grid-template-columns: minmax(0, 1fr);
		min-block-size: 0;
		transition: --side-w var(--dur-sheet) var(--ease-out-expo);
	}

	.frame.docked {
		grid-template-columns: minmax(0, 1fr) var(--pane-w);
	}

	.frame.sided {
		--side-w: 264px;
	}

	/* Clips the side column while its width animates, so what is inside never reflows. */
	.side-clip {
		display: none;
		min-inline-size: 0;
		min-block-size: 0;
		overflow: clip;
		opacity: 0;
		visibility: hidden;
		transition:
			opacity var(--dur-sheet) var(--ease-out),
			visibility var(--dur-sheet);
	}

	.sided .side-clip {
		opacity: 1;
		visibility: visible;
	}

	.side {
		display: flex;
		flex-direction: column;
		gap: var(--sp-6);
		inline-size: 264px;
		block-size: 100%;
		min-block-size: 0;
		overflow-y: auto;
		padding: var(--sp-1) var(--sp-3) var(--sp-3) var(--sp-5);
		scrollbar-width: thin;
	}

	/* The density, at the foot of the column however short the window. */
	.zoom {
		position: sticky;
		inset-block-end: 0;
		display: flex;
		gap: 2px;
		margin-block-start: auto;
		margin-inline-start: -8px;
		padding-block: var(--sp-2) 0;
		background: var(--calendar-bg, var(--bg));
	}

	@container calendar (width >= 1100px) {
		.frame:not(.docked) {
			grid-template-columns: var(--side-w) minmax(0, 1fr);
		}

		.frame:not(.docked) .side-clip {
			display: block;
		}
	}

	/* Beside a docked pane, the side column only once the week keeps columns wide enough to read. */
	@container calendar (width >= 1460px) {
		.frame.docked {
			grid-template-columns: var(--side-w) minmax(0, 1fr) var(--pane-w);
		}

		.frame.docked .side-clip {
			display: block;
		}
	}

	.main {
		position: relative;
		display: flex;
		flex-direction: column;
		min-inline-size: 0;
		min-block-size: 0;
	}

	.month {
		flex: 1;
		display: grid;
		min-block-size: 0;
		padding: 0 var(--sp-5) var(--sp-4);
	}

	.month.split {
		grid-template-rows: minmax(0, 3fr) minmax(0, 2fr);
		gap: var(--sp-5);
	}

	@container calendar (width >= 880px) {
		.month.split {
			grid-template-rows: none;
			grid-template-columns: minmax(0, 1fr) 300px;
		}
	}

	.month-column {
		display: grid;
		grid-template-rows: minmax(0, 1fr) auto;
		gap: var(--sp-3);
		min-block-size: 0;
	}

	.month-grid {
		min-block-size: 0;
	}

	.legends {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: space-between;
		gap: var(--sp-2) var(--sp-6);
	}

	.month-agenda {
		min-block-size: 0;
		overflow-y: auto;
		scrollbar-width: thin;
	}

	.resizer {
		position: absolute;
		inset-block: 0;
		inset-inline-end: calc(var(--pane-w) - 14px);
		z-index: 7;
		display: grid;
		place-items: center;
		inline-size: 14px;
		cursor: col-resize;
		touch-action: none;
		user-select: none;
		outline: none;
	}

	/* A hairline down the gap that shows where it is, and thickens under the pointer. */
	.resizer::before {
		content: '';
		position: absolute;
		inset-block: 8px;
		inset-inline: 6px;
		border-radius: var(--r-pill);
		background: var(--accent);
		opacity: 0;
		transition: opacity var(--dur-fast) var(--ease-out);
	}

	/*
	 * The icon, on a patch of the page's own colour so the handle's line doesn't run through it,
	 * shown while the pointer is over the handle (or it has focus or is dragged).
	 */
	.grip {
		position: relative;
		display: grid;
		place-items: center;
		/* Narrower than the 14px gap, so it stays clear of the calendar and of the panel's edge. */
		inline-size: 10px;
		padding-block: 10px;
		background: var(--calendar-bg, var(--bg));
		color: var(--text-muted);
		opacity: 0;
		transition:
			opacity var(--dur-fast) var(--ease-out),
			color var(--dur-fast) var(--ease-out);
	}

	.grip :global(svg) {
		inline-size: 16px;
		block-size: 16px;
	}

	@media (hover: hover) {
		.resizer:hover::before {
			opacity: 0.45;
		}

		.resizer:hover .grip {
			opacity: 1;
			color: var(--text);
		}
	}

	.resizing .resizer::before,
	.resizer:focus-visible::before {
		opacity: 0.9;
	}

	.resizer:focus-visible .grip,
	.resizing .grip {
		opacity: 1;
		color: var(--accent-text);
	}

	.resizing {
		cursor: col-resize;
		user-select: none;
	}

	.pane {
		display: flex;
		flex-direction: column;
		min-block-size: 0;
		margin: 0 var(--sp-3) var(--sp-3) 14px;
		/* Rounder than the tiles inside it, which take their radius from here. */
		--tile-radius: var(--r-md);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
		overflow: hidden;
	}

	.pane-agenda {
		overflow-y: auto;
		padding: var(--sp-5);
		scrollbar-width: thin;
	}
</style>
