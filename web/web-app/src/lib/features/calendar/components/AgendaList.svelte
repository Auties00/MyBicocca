<script lang="ts">
	import type { MoodleEnrolledCourse } from '@mybicocca/data-api/elearning';
	import IconCaretDown from '~icons/ph/caret-down';
	import IconCaretRight from '~icons/ph/caret-right';
	import type { IsoDate } from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { clockTime, dayMonth, displayTitle, shortDate, timeRange } from '../format.ts';
	import { isInProgress, isPointInTime, locationLine, type CalendarEvent } from '../model.ts';
	import { eventSwatch } from '../palette.ts';
	import EventDetail from './EventDetail.svelte';

	interface Props {
		date: IsoDate;
		today: IsoDate;
		/** The minute of the day now, for what is in progress. */
		minute: number;
		events: readonly CalendarEvent[];
		loading?: boolean;
		/** Rows unfold their details in place instead of opening them (the expanded agenda sheet). */
		expandable?: boolean;
		courses?: readonly MoodleEnrolledCourse[];
		/** The event whose details are open beside the list. */
		selectedId?: string | null;
		/** The serif date of the phone sheet, or a quieter one beside the toolbar's serif title. */
		heading?: 'large' | 'medium';
		/** What comes next beyond the day, such as the next exam and deadline. */
		upcoming?: readonly CalendarEvent[];
		onopen: (event: CalendarEvent, row: HTMLElement) => void;
	}

	let {
		date,
		today,
		minute,
		events,
		loading = false,
		expandable = false,
		courses,
		selectedId = null,
		heading = 'large',
		upcoming = [],
		onopen
	}: Props = $props();

	const headingId = $props.id();
	const upcomingId = `${headingId}-upcoming`;

	/* One row open at a time; collapsing the sheet or changing day folds it. */
	let expandedId = $derived.by<string | null>(() => {
		void date;
		void expandable;
		return null;
	});
	let detailPage = $state<'event' | 'courses'>('event');

	function subtitle(event: CalendarEvent, withDate: boolean): string {
		const time = isPointInTime(event)
			? m.calendar_detail_by_time({ time: clockTime(event.start) })
			: timeRange(event);
		return [withDate ? shortDate(event.date) : '', time, locationLine(event)]
			.filter(Boolean)
			.join(' · ');
	}

	function activate(event: CalendarEvent, row: HTMLElement) {
		if (!expandable) {
			onopen(event, row);
			return;
		}
		detailPage = 'event';
		expandedId = expandedId === event.id ? null : event.id;
	}
</script>

{#snippet row(event: CalendarEvent, withDate: boolean)}
	{@const swatch = eventSwatch(event, isInProgress(event, today, minute))}
	{@const open = expandable && !withDate && expandedId === event.id}
	<li
		class={['row', 'calendar-swatch', { open, selected: event.id === selectedId }]}
		style:--sw-h={swatch.hue}
		style:--sw-c={swatch.chroma}
	>
		<button
			type="button"
			class="summary"
			aria-expanded={expandable && !withDate ? open : undefined}
			aria-current={event.id === selectedId ? 'true' : undefined}
			data-event={withDate ? undefined : event.id}
			onclick={(click) => activate(event, click.currentTarget)}
		>
			<span class="text">
				<span class={['title', { struck: event.cancelled }]} lang="it">{displayTitle(event)}</span>
				<span class="sub">
					{#if event.cancelled}<span class="cancelled">{m.calendar_detail_cancelled()}</span>
						·
					{/if}{subtitle(event, withDate)}
				</span>
			</span>
			{#if expandable && !withDate}
				<IconCaretDown class="chevron" aria-hidden="true" />
			{:else}
				<IconCaretRight class="chevron" aria-hidden="true" />
			{/if}
		</button>
		{#if open}
			<div class="inline">
				<EventDetail {event} {courses} bind:page={detailPage} dense inset />
			</div>
		{/if}
	</li>
{/snippet}

<div class="agenda">
	<section class="day" aria-labelledby={headingId}>
		<header class="head">
			<p class="overline">
				{date === today ? m.calendar_agenda_today() : m.calendar_agenda_selected()}
			</p>
			<h2 id={headingId} class={['date', heading]}>{dayMonth(date)}</h2>
			<p class="count">
				{#if loading}
					<Skeleton variant="text" width="5em" />
				{:else}
					{m.calendar_event_count({ count: events.length })}
				{/if}
			</p>
		</header>
		{#if loading}
			<div class="rows" aria-hidden="true">
				{#each [0, 1, 2] as index (index)}
					<div class="row skeleton"><Skeleton variant="text" lines={2} /></div>
				{/each}
			</div>
		{:else if events.length === 0}
			<p class="none">{m.calendar_no_events()}</p>
		{:else}
			<ul class="rows">
				{#each events as event (event.id)}
					{@render row(event, false)}
				{/each}
			</ul>
		{/if}
	</section>
	{#if upcoming.length > 0 && !loading}
		<section class="upcoming" aria-labelledby={upcomingId}>
			<h3 id={upcomingId} class="overline">{m.calendar_upcoming()}</h3>
			<ul class="rows">
				{#each upcoming as event (event.id)}
					{@render row(event, true)}
				{/each}
			</ul>
		</section>
	{/if}
</div>

<style>
	.agenda {
		display: grid;
		align-content: start;
		gap: var(--sp-6);
	}

	.day,
	.upcoming {
		display: grid;
		align-content: start;
		gap: var(--sp-4);
	}

	.upcoming .overline {
		color: var(--text-muted);
	}

	.head {
		display: grid;
		gap: 2px;
	}

	.overline {
		color: var(--accent-text);
		font: var(--type-overline);
		font-size: 0.6875rem;
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.date.large {
		font: var(--type-title-l);
		letter-spacing: var(--track-title-l);
	}

	.date.medium {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.count {
		color: var(--text-muted);
		font: var(--type-label);
		font-weight: 600;
	}

	.none {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.rows {
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	/* A step above the sheet or pane they sit on, in both themes. */
	.row {
		position: relative;
		overflow: hidden;
		border-radius: var(--r-2xs);
		background: light-dark(var(--g3), var(--g4));
		transition:
			border-radius var(--dur-spring) var(--spring-standard),
			background-color var(--dur-base) var(--ease-out);
	}

	.row:first-child {
		border-start-start-radius: var(--r-lg);
		border-start-end-radius: var(--r-lg);
	}

	.row:last-child {
		border-end-start-radius: var(--r-lg);
		border-end-end-radius: var(--r-lg);
	}

	.row.open {
		margin-block: 4px;
		border-radius: var(--r-lg);
		background: light-dark(var(--g4), var(--g5));
	}

	.row.selected {
		background: var(--sw-container);
	}

	.row::before {
		content: '';
		position: absolute;
		inset-block: 12px;
		inset-inline-start: 0;
		inline-size: 3px;
		border-radius: 0 3px 3px 0;
		background: var(--sw-accent);
	}

	.skeleton {
		padding: 14px 16px 14px 18px;
	}

	.skeleton::before {
		display: none;
	}

	.summary {
		display: flex;
		align-items: center;
		gap: 12px;
		inline-size: 100%;
		min-block-size: 64px;
		padding: 12px 14px 12px 18px;
		color: var(--text);
		text-align: start;
		cursor: pointer;
	}

	@media (hover: hover) {
		.summary:hover {
			background: var(--state-hover);
		}
	}

	.summary:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: -2px;
		border-radius: inherit;
	}

	.text {
		flex: 1;
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.title {
		overflow: hidden;
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.struck {
		text-decoration: line-through;
	}

	.sub {
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.cancelled {
		color: var(--danger);
		font-weight: 600;
	}

	.summary :global(.chevron) {
		flex: none;
		inline-size: 18px;
		block-size: 18px;
		color: var(--text-muted);
		transition: transform var(--dur-spring) var(--spring-standard);
	}

	.open .summary :global(.chevron) {
		transform: rotate(180deg);
	}

	.inline {
		padding: 0 14px 16px 18px;
	}
</style>
