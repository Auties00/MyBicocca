<script lang="ts">
	import IconCalendarDots from '~icons/ph/calendar-dots';
	import { weekDays, type IsoDate } from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import type { CalendarController } from '../controller.svelte.ts';
	import { shortDate } from '../format.ts';
	import DayHeads from './DayHeads.svelte';
	import Timeline from './Timeline.svelte';
	import WeekHeads from './WeekHeads.svelte';

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
	<DayHeads days={page} today={ctl.today} onpick={(day) => ctl.goTo(day, 'day')} />
{/snippet}

{#snippet wideHeads()}
	<WeekHeads
		mode={ctl.view === 'month' ? 'week' : ctl.view}
		days={weekDays(ctl.date)}
		date={ctl.date}
		today={ctl.today}
		byDay={ctl.data.byDay}
		onpick={(day) => (ctl.view === 'week' ? ctl.goTo(day, 'day') : ctl.goTo(day))}
	/>
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
	header={compact ? (ctl.view === 'week' ? weekHeads : undefined) : wideHeads}
	loading={ctl.loading}
	empty={freePeriod}
	selectedId={ctl.nav.eventId}
	{bottomInset}
	label={m.calendar_timeline_label({ period: ctl.title })}
	onopen={ctl.open}
	onpage={ctl.step}
	{ondrag}
/>
