<script lang="ts">
	import IconCalendarDots from '~icons/ph/calendar-dots';
	import type { IsoDate } from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import type { CalendarController } from '../controller.svelte.ts';
	import { shortDate } from '../format.ts';
	import DayHeads from './DayHeads.svelte';
	import Timeline from './Timeline.svelte';
	import WeekStrip from './WeekStrip.svelte';

	interface Props {
		ctl: CalendarController;
		/** While the phone swipes between days: how far, in pages. */
		ondrag?: (fraction: number) => void;
	}

	let { ctl, ondrag }: Props = $props();

	const compact = $derived(ctl.compact);
	/* Room at the foot for what floats there: the phone's tab bar, and the turno prompt. */
	const prompt = $derived(ctl.prompt ? 72 : 0);
	const bottomInset = $derived(
		compact ? `calc(var(--shell-inset-bottom) + ${prompt + 10}px)` : `${prompt ? prompt + 16 : 0}px`
	);
</script>

{#snippet weekHeads(page: IsoDate[])}
	<DayHeads days={page} today={ctl.today} wide={!compact} onpick={(day) => ctl.goTo(day, 'day')} />
{/snippet}

{#snippet dayStrip(page: IsoDate[])}
	<div class="strip-head">
		<WeekStrip
			date={page[0]}
			today={ctl.today}
			byDay={ctl.data.byDay}
			wide
			onpick={(day) => ctl.goTo(day)}
		/>
	</div>
{/snippet}

{#snippet freePeriod()}
	{#if ctl.upcoming}
		{@const next = ctl.upcoming}
		<Button variant="tonal" icon={IconCalendarDots} onclick={() => ctl.goTo(next.date)}>
			{m.calendar_next_event({ date: shortDate(next.date) })}
		</Button>
	{/if}
{/snippet}

<Timeline
	bind:this={ctl.timeline}
	days={ctl.days}
	byDay={ctl.data.byDay}
	window={ctl.span}
	now={ctl.now}
	bind:zoom={ctl.zoom}
	swipe={compact}
	narrow={compact}
	header={ctl.view === 'week' ? weekHeads : compact ? undefined : dayStrip}
	loading={ctl.loading}
	empty={freePeriod}
	selectedId={ctl.nav.eventId}
	{bottomInset}
	label={m.calendar_timeline_label({ period: ctl.title })}
	onopen={ctl.open}
	onpage={ctl.step}
	{ondrag}
/>

<style>
	.strip-head {
		padding: var(--sp-1) 0 var(--sp-3);
	}
</style>
