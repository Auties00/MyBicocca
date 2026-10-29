<script lang="ts">
	import { fly } from 'svelte/transition';
	import { monthOf } from '#lib/format/days.ts';
	import type { CalendarController } from '../controller.svelte.ts';
	import { displayTitle, fullDate } from '../format.ts';
	import { ZOOM_MAX, ZOOM_MIN } from '../layout.ts';
	import AgendaList from './AgendaList.svelte';
	import BusyLegend from './BusyLegend.svelte';
	import CalendarTimeline from './CalendarTimeline.svelte';
	import CalendarToolbar from './CalendarToolbar.svelte';
	import DetailPanel from './DetailPanel.svelte';
	import EventPopover from './EventPopover.svelte';
	import MiniMonth from './MiniMonth.svelte';
	import MonthGrid from './MonthGrid.svelte';
	import SourceLegend from './SourceLegend.svelte';
	import SyncStatus from './SyncStatus.svelte';
	import TurnoPrompt from './TurnoPrompt.svelte';
	import { reducedMotion } from '#lib/ui/media.ts';

	interface Props {
		ctl: CalendarController;
	}

	let { ctl }: Props = $props();

	const view = $derived(ctl.view);
	const date = $derived(ctl.date);
	const data = $derived(ctl.data);
	const docked = $derived(ctl.docked);
</script>

<div class="bar">
	<CalendarToolbar
		{view}
		title={ctl.barTitle}
		{date}
		today={ctl.today}
		range={ctl.period}
		byDay={data.byDay}
		showsToday={ctl.todayOnScreen}
		refreshing={data.refreshing && data.hydrated}
		onrefresh={data.refresh}
		zoom={ctl.zoom}
		zoomMin={ZOOM_MIN}
		zoomMax={ZOOM_MAX}
		onview={ctl.setView}
		onstep={ctl.step}
		ontoday={() => ctl.goTo(ctl.today)}
		onpick={(day) => ctl.goTo(day)}
		onzoom={ctl.zoomBy}
	/>
</div>
<SyncStatus {data} />
{#if !data.failed}
	<div class={['frame', { docked, sided: view !== 'month', large: ctl.size === 'large' }]}>
		{#if view !== 'month'}
			<aside class="side">
				<MiniMonth
					selected={date}
					today={ctl.today}
					range={ctl.period}
					byDay={data.byDay}
					onpick={(day) => ctl.goTo(day)}
				/>
				<SourceLegend events={ctl.periodEvents} libraryLinked={data.libraryLinked} />
			</aside>
		{/if}
		<section class="main" aria-label={ctl.title}>
			{#if view === 'month'}
				<div class={['month', { split: !docked }]}>
					<div class="month-column">
						<div class="month-grid">
							<MonthGrid
								month={monthOf(date)}
								byDay={data.byDay}
								selected={date}
								today={ctl.today}
								variant="wide"
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
							<BusyLegend variant="wide" />
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
		--pane-w: 340px;
		flex: 1;
		display: grid;
		grid-template-columns: minmax(0, 1fr);
		min-block-size: 0;
	}

	.frame.large {
		--pane-w: 360px;
	}

	.frame.docked {
		grid-template-columns: minmax(0, 1fr) var(--pane-w);
	}

	.side {
		display: none;
		flex-direction: column;
		gap: var(--sp-6);
		min-block-size: 0;
		overflow-y: auto;
		padding: var(--sp-1) var(--sp-3) var(--sp-6) var(--sp-5);
		scrollbar-width: thin;
	}

	@container calendar (width >= 1100px) {
		.frame.sided:not(.docked) {
			grid-template-columns: 264px minmax(0, 1fr);
		}

		.frame.sided:not(.docked) .side {
			display: flex;
		}
	}

	/* Beside a docked pane, the side column only once the week keeps columns wide enough to read. */
	@container calendar (width >= 1460px) {
		.frame.sided.docked {
			grid-template-columns: 264px minmax(0, 1fr) var(--pane-w);
		}

		.frame.sided.docked .side {
			display: flex;
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

	.pane {
		display: flex;
		flex-direction: column;
		min-block-size: 0;
		margin: 0 var(--sp-3) var(--sp-3) 0;
		border-radius: var(--r-xl);
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
