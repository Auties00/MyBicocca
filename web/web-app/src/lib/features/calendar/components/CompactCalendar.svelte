<script lang="ts">
	import { fly } from 'svelte/transition';
	import IconCalendarDots from '~icons/ph/calendar-dots';
	import { monthOf } from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import Overlay from '#lib/ui/Overlay.svelte';
	import SegmentedControl from '#lib/ui/SegmentedControl.svelte';
	import type { CalendarController } from '../controller.svelte.ts';
	import { monthName, weekSpan, weekdayDay } from '../format.ts';
	import AgendaList from './AgendaList.svelte';
	import AgendaSheet from './AgendaSheet.svelte';
	import BusyLegend from './BusyLegend.svelte';
	import CalendarTimeline from './CalendarTimeline.svelte';
	import MiniMonth from './MiniMonth.svelte';
	import MonthGrid from './MonthGrid.svelte';
	import PullToRefresh from './PullToRefresh.svelte';
	import SyncStatus from './SyncStatus.svelte';
	import TurnoPrompt from './TurnoPrompt.svelte';
	import WeekStrip from './WeekStrip.svelte';
	import { reducedMotion } from '#lib/ui/media.ts';

	interface Props {
		ctl: CalendarController;
	}

	let { ctl }: Props = $props();

	const slide = $derived(reducedMotion.current ? 0 : 1);
	const view = $derived(ctl.view);
	const date = $derived(ctl.date);
	const data = $derived(ctl.data);

	let drag = $state(0);
	let agendaProgress = $state(0);
	let sheetRoom = $state(220);

	const segments = $derived([
		{ value: 'day' as const, caption: m.calendar_view_day(), label: weekdayDay(date) },
		{ value: 'week' as const, caption: m.calendar_view_week(), label: weekSpan(date) },
		{ value: 'month' as const, caption: m.calendar_view_month(), label: monthName(date) }
	]);

	/* Re-tapping the active segment opens the date picker, as on Android. */
	let tappedActive = false;
	function segmentDown(event: PointerEvent) {
		const input = (event.target as Element).closest('label')?.querySelector('input');
		tappedActive = input?.value === view;
	}
	function segmentClick() {
		if (tappedActive) ctl.picking = true;
		tappedActive = false;
	}
	function segmentKey(event: KeyboardEvent) {
		if (event.key === 'Enter' && (event.target as HTMLInputElement).value === view) {
			ctl.picking = true;
		}
	}

	/* The agenda sheet takes the room the month grid leaves, measured from the legend down. */
	function measureRoom(legend: HTMLElement) {
		const update = () => {
			sheetRoom = Math.max(160, window.innerHeight - legend.getBoundingClientRect().bottom - 16);
		};
		const observer = new ResizeObserver(update);
		observer.observe(legend);
		observer.observe(document.documentElement);
		update();
		return () => observer.disconnect();
	}

	function pick(day: string) {
		ctl.picking = false;
		ctl.goTo(day);
	}
</script>

<h1 class="sr-only">{m.calendar_title()}</h1>
<!-- svelte-ignore a11y_no_static_element_interactions -->
<div class="segments" onpointerdown={segmentDown} onclick={segmentClick} onkeydown={segmentKey}>
	<SegmentedControl
		options={segments}
		value={view}
		label={m.calendar_view_label()}
		size="lg"
		full
		onchange={(value) => ctl.setView(value)}
	/>
</div>
<SyncStatus {data} />
{#if !data.failed}
	<PullToRefresh onrefresh={data.refresh}>
		{#key view}
			<div
				class="mode"
				in:fly={{ x: (view === 'month' ? 1 : -1) * 40 * slide, duration: 280 * slide, opacity: 0 }}
			>
				{#if view === 'day'}
					<div class="day-strip">
						<WeekStrip
							{date}
							today={ctl.today}
							byDay={data.byDay}
							{drag}
							onpick={(day) => ctl.goTo(day)}
						/>
					</div>
					<CalendarTimeline {ctl} ondrag={(fraction) => (drag = fraction)} />
				{:else if view === 'week'}
					<CalendarTimeline {ctl} />
				{:else}
					<div class="month">
						<MonthGrid
							month={monthOf(date)}
							byDay={data.byDay}
							selected={date}
							today={ctl.today}
							variant="compact"
							loading={ctl.loading}
							onselect={(day) => ctl.goTo(day)}
						/>
						<div {@attach measureRoom}><BusyLegend /></div>
					</div>
					<AgendaSheet collapsed={sheetRoom} bind:progress={agendaProgress}>
						{#snippet children(expanded)}
							<AgendaList
								{date}
								today={ctl.today}
								minute={ctl.now.minutes}
								events={ctl.dayEvents}
								loading={ctl.loading}
								expandable={expanded}
								courses={data.courses}
								onopen={ctl.open}
							/>
						{/snippet}
					</AgendaSheet>
				{/if}
			</div>
		{/key}
	</PullToRefresh>
{/if}
<!-- Floating over the timeline's foot, above the tab bar: arriving late, they move nothing. -->
<div class="dock">
	{#if !ctl.todayOnScreen && !data.failed}
		<div
			class="fab"
			style:opacity={view === 'month' ? 1 - agendaProgress : 1}
			style:--fab-scale={view === 'month' ? 1 - agendaProgress * 0.2 : 1}
			transition:fly={{ y: 16, duration: 200 * slide }}
		>
			<Button variant="solid" size="lg" icon={IconCalendarDots} onclick={() => ctl.goTo(ctl.today)}>
				{m.calendar_go_to_today()}
			</Button>
		</div>
	{/if}
	{#if ctl.prompt && !data.failed}
		{@const course = ctl.prompt}
		<div class="prompt" transition:fly={{ y: 16, duration: 200 * slide }}>
			<TurnoPrompt
				{course}
				compact
				onchoose={(choice) => ctl.chooseTurno(course.activityCode, choice)}
			/>
		</div>
	{/if}
</div>

<Overlay bind:open={ctl.picking} title={m.calendar_pick_day()} size="sm">
	{#if ctl.picking}
		<MiniMonth
			selected={date}
			today={ctl.today}
			range={ctl.period}
			byDay={data.byDay}
			autofocus
			onpick={pick}
		/>
	{/if}
	{#snippet footer()}
		<Button variant="tonal" onclick={() => pick(ctl.today)}>{m.calendar_today()}</Button>
	{/snippet}
</Overlay>

<style>
	.segments {
		flex: none;
		padding: var(--sp-3) var(--sp-4);
	}

	.dock {
		position: fixed;
		inset-inline: 16px;
		inset-block-end: calc(var(--shell-inset-bottom) + 12px);
		z-index: var(--z-nav);
		display: flex;
		flex-direction: column;
		align-items: flex-end;
		gap: 12px;
		pointer-events: none;
	}

	.dock > * {
		pointer-events: auto;
	}

	.prompt {
		align-self: stretch;
		border-radius: var(--r-md);
		box-shadow: var(--elev-3);
	}

	.mode {
		flex: 1;
		display: flex;
		flex-direction: column;
		min-block-size: 0;
	}

	.day-strip {
		flex: none;
		padding: 0 var(--sp-2) var(--sp-2) calc(38px + var(--sp-1));
	}

	.month {
		display: grid;
		gap: var(--sp-5);
		padding: var(--sp-1) 14px 0;
	}

	.fab {
		transform: scale(var(--fab-scale, 1));
		transform-origin: bottom right;
	}

	.fab :global(.btn) {
		box-shadow: var(--elev-3);
	}
</style>
