<script lang="ts">
	import IconCalendar from '~icons/ph/calendar-blank';
	import IconCalendarDots from '~icons/ph/calendar-dots';
	import IconClock from '~icons/ph/clock';
	import IconHourglass from '~icons/ph/hourglass-medium';
	import type {
		AffluencesAvailableResource,
		AffluencesResourceTypeFilters
	} from '@mybicocca/data-api/affluences';
	import type { ResolvedPathname } from '$app/types';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import type { Query } from '#lib/data/query.svelte.ts';
	import { formatTime } from '#lib/format/dates.ts';
	import type { IsoDate } from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { dayChip, durationLabel, siteDate } from '../format.ts';
	import { startTimes, toSeats } from '../model.ts';
	import MonthPicker from './MonthPicker.svelte';

	interface Props {
		filters: Query<AffluencesResourceTypeFilters>;
		seats: Query<AffluencesAvailableResource[]>;
		days: readonly IsoDate[];
		durations: readonly number[];
		day: IsoDate | undefined;
		duration: number | undefined;
		/** The start picked before, when coming back to this step. */
		start: number | undefined;
		today: IsoDate;
		onday: (day: IsoDate) => void;
		onduration: (duration: number) => void;
		/** The seat step for a start. */
		startHref: (start: number) => ResolvedPathname;
	}

	let {
		filters,
		seats,
		days,
		durations,
		day,
		duration,
		start,
		today,
		onday,
		onduration,
		startHref
	}: Props = $props();

	const id = $props.id();
	const times = $derived(seats.data ? startTimes(toSeats(seats.data)) : undefined);
	const constraintsGate = new LoadingGate(() => !filters.data && !filters.error);
	const timesGate = new LoadingGate(() => times === undefined && !seats.error);

	/* Compact: the bookable days as a strip, the whole month on demand. */
	let monthShown = $state(false);

	/* The picked day in view when the strip first shows, as after a reload on a later day. */
	function revealPicked(strip: HTMLElement) {
		const picked = strip.querySelector<HTMLElement>('input:checked')?.parentElement;
		if (picked)
			strip.scrollLeft = picked.offsetLeft - strip.clientWidth / 2 + picked.offsetWidth / 2;
	}
</script>

{#if filters.error && !filters.data}
	<ErrorState
		title={m.library_availability_loading_failed()}
		message={errorMessage(filters.error)}
		onretry={filters.refresh}
		retrying={filters.refreshing}
	/>
{:else if !filters.data}
	<div class="when" aria-busy="true">
		<div class="card duration-skeleton">
			{#if constraintsGate.visible}<Skeleton variant="block" height="100%" />{/if}
		</div>
		<div class="layout">
			<div class="card day-skeleton">
				{#if constraintsGate.visible}<Skeleton variant="block" height="100%" />{/if}
			</div>
			<div class="card times-skeleton">
				{#if constraintsGate.visible}<Skeleton variant="block" height="100%" />{/if}
			</div>
		</div>
	</div>
{:else if days.length === 0}
	<p class="card notice">{m.library_no_bookable_days()}</p>
{:else}
	<div class="when">
		{#if durations.length > 0}
			<fieldset class="card">
				<legend class="card-title">
					<IconHourglass aria-hidden="true" />{m.library_duration()}
				</legend>
				<div class="chips">
					{#each durations as option (option)}
						<label class="chip">
							<input
								type="radio"
								name="{id}-duration"
								value={option}
								checked={option === duration}
								onchange={() => onduration(option)}
							/>
							<span>{durationLabel(option)}</span>
						</label>
					{/each}
				</div>
			</fieldset>
		{/if}

		<div class="layout">
			<section class={['card', 'day', { 'month-shown': monthShown }]} aria-labelledby="{id}-day">
				<div class="day-head">
					<h2 class="card-title" id="{id}-day">
						<IconCalendar aria-hidden="true" />{m.library_day()}
					</h2>
					<span class="month-toggle">
						<IconButton
							icon={IconCalendarDots}
							label={m.library_month_view()}
							size="sm"
							pressed={monthShown}
							onclick={() => (monthShown = !monthShown)}
						/>
					</span>
				</div>

				<div class="strip" role="radiogroup" aria-labelledby="{id}-day" {@attach revealPicked}>
					{#each days as option (option)}
						{@const chip = dayChip(option, today)}
						<label class={['day-chip', { today: option === today }]}>
							<input
								type="radio"
								name="{id}-day"
								value={option}
								checked={option === day}
								aria-label={chip.label}
								onchange={() => onday(option)}
							/>
							<span class="face" aria-hidden="true">
								<span class="weekday">{chip.weekday}</span>
								<span class="date">{chip.date}</span>
								<span class="short-month">{chip.month}</span>
							</span>
						</label>
					{/each}
				</div>

				<div class="month">
					<MonthPicker {days} selected={day} {today} onselect={onday} />
				</div>
			</section>

			<section class="card" aria-labelledby="{id}-time" aria-busy={times === undefined}>
				<h2 class="card-title" id="{id}-time">
					<IconClock aria-hidden="true" />{m.library_start_time()}
				</h2>
				{#if seats.error && !times}
					<div class="problem" role="alert">
						<p>{m.library_times_loading_failed()} {errorMessage(seats.error)}</p>
						<Button size="sm" onclick={seats.refresh} loading={seats.refreshing}>
							{m.common_retry()}
						</Button>
					</div>
				{:else if times === undefined || !day}
					<ul class="times">
						{#each { length: 8 }, index (index)}
							<li class="slot-skeleton">
								{#if timesGate.visible}<Skeleton
										variant="block"
										height="100%"
										radius="var(--r-sm)"
									/>{/if}
							</li>
						{/each}
					</ul>
				{:else if times.length === 0}
					<div class="none">
						<p class="none-title">{m.library_no_available_times()}</p>
						<p class="none-body">{m.library_no_available_times_body()}</p>
					</div>
				{:else}
					<ul class={['times', { refreshing: seats.refreshing }]}>
						{#each times as time (time.minutes)}
							{@const label = formatTime(siteDate(day, time.minutes))}
							{@const free = m.library_free_count({ count: time.free })}
							<li>
								<a
									class={['time', { picked: time.minutes === start }]}
									href={startHref(time.minutes)}
									aria-label={m.library_time_option({ time: label, free })}
									aria-current={time.minutes === start ? 'true' : undefined}
								>
									<span class="clock">{label}</span>
									<span class="free">{free}</span>
								</a>
							</li>
						{/each}
					</ul>
				{/if}
			</section>
		</div>
	</div>
{/if}

<style>
	.when,
	.layout {
		display: grid;
		align-items: start;
		gap: var(--sp-4);
	}

	/*
	 * Wide: the duration across the top, where its options wrap, then the month beside the start
	 * times, so the times, the choice that matters, sit right of the calendar and not below it.
	 */
	@container page (width >= 620px) {
		.layout {
			grid-template-columns: minmax(280px, 360px) minmax(0, 1fr);
		}
	}

	.card {
		display: grid;
		gap: var(--sp-3);
		min-inline-size: 0;
		margin: 0;
		padding: var(--sp-4) var(--sp-4) var(--sp-5);
		border: 0;
		border-radius: var(--r-xl);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.duration-skeleton {
		block-size: 112px;
	}

	.day-skeleton,
	.times-skeleton {
		block-size: 360px;
	}

	@container page (width < 620px) {
		.times-skeleton {
			block-size: 240px;
		}
	}

	@media (pointer: coarse) {
		@container page (width < 620px) {
			.day-skeleton {
				block-size: 150px;
			}
		}
	}

	.notice {
		color: var(--text-muted);
	}

	.card-title {
		display: flex;
		align-items: center;
		gap: 8px;
		padding: 0;
		color: var(--text-muted);
		font: var(--type-ui-strong);
	}

	.card-title :global(svg) {
		color: var(--accent-text);
	}

	/* Floated, the legend lays out like the other cards' titles, inside the card. */
	legend.card-title {
		float: left;
		inline-size: 100%;
	}

	.chips {
		clear: both;
		display: flex;
		flex-wrap: wrap;
		gap: var(--sp-2);
	}

	.chip {
		position: relative;
		display: inline-grid;
	}

	.chip input,
	.day-chip input {
		position: absolute;
		inset: 0;
		margin: 0;
		opacity: 0;
		cursor: pointer;
	}

	.chip span {
		display: grid;
		place-items: center;
		min-inline-size: 64px;
		min-block-size: 40px;
		padding: 0 14px;
		border-radius: var(--r-pill);
		background: var(--surface-sunken);
		font: var(--type-ui);
		font-variant-numeric: tabular-nums;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.chip input:checked + span,
	.day-chip input:checked + .face {
		background: var(--accent);
		color: var(--on-accent);
	}

	.chip input:checked + span {
		font-weight: 600;
	}

	.chip input:focus-visible + span,
	.day-chip input:focus-visible + .face {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	@media (hover: hover) {
		.chip input:not(:checked):hover + span,
		.day-chip input:not(:checked):hover + .face {
			background: var(--container);
			color: var(--on-container);
			transition-duration: 0s;
		}
	}

	.day-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: var(--sp-2);
		min-block-size: 28px;
	}

	/* The strip and its switch are for touch screens at phone width; others show the month. */
	.strip,
	.month-toggle {
		display: none;
	}

	.strip {
		gap: var(--sp-2);
	}

	.day-chip {
		position: relative;
		display: grid;
		flex-shrink: 0;
	}

	.face {
		display: grid;
		justify-items: center;
		gap: 1px;
		min-inline-size: 58px;
		padding: 8px 6px;
		border-radius: var(--r-md);
		background: var(--surface-sunken);
		font-variant-numeric: tabular-nums;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.today input:not(:checked) + .face {
		box-shadow: inset 0 0 0 1.5px var(--accent);
	}

	.weekday,
	.short-month {
		font: var(--type-meta);
	}

	.day-chip input:not(:checked) + .face :is(.weekday, .short-month) {
		color: var(--text-muted);
	}

	.date {
		font: var(--type-ui-strong);
		font-size: 1.25rem;
		line-height: 1.2;
	}

	/*
	 * Touch at phone width: the days become a strip and the durations a row, each swiping
	 * sideways, with the month on demand. A mouse keeps the month, which it can point at.
	 */
	@media (pointer: coarse) {
		@container page (width < 620px) {
			.strip {
				display: flex;
			}

			.month-toggle {
				display: inline-flex;
			}

			.month,
			.month-shown .strip {
				display: none;
			}

			.month-shown .month {
				display: block;
			}

			.strip,
			.chips {
				flex-wrap: nowrap;
				margin-inline: calc(-1 * var(--sp-4));
				padding: 4px var(--sp-4);
				overflow-x: auto;
				overscroll-behavior-inline: contain;
				scroll-padding-inline: var(--sp-4);
				scroll-snap-type: x proximity;
				scrollbar-width: none;
			}

			.chip,
			.day-chip {
				flex-shrink: 0;
				scroll-snap-align: start;
			}
		}
	}

	.times {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(92px, 1fr));
		gap: var(--sp-2);
		margin: 0;
		padding: 0;
		list-style: none;
		transition: opacity var(--dur-base) var(--ease-out);
	}

	.times.refreshing {
		opacity: 0.6;
	}

	.slot-skeleton {
		block-size: 58px;
	}

	.time {
		display: grid;
		justify-items: center;
		gap: 2px;
		padding: 9px 8px;
		border-radius: var(--r-sm);
		background: var(--surface);
		box-shadow: inset 0 0 0 1px var(--border);
		color: var(--text);
		text-decoration: none;
		touch-action: manipulation;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			transform var(--dur-spring) var(--spring-pop);
	}

	@media (hover: hover) {
		.time:hover {
			background: var(--container);
			box-shadow: inset 0 0 0 1px var(--accent);
			color: var(--on-container);
			transition-duration: 0s, var(--dur-spring);
		}
	}

	.time:active {
		transform: scale(0.96);
		transition-duration: var(--dur-instant);
	}

	.time.picked {
		background: var(--accent);
		box-shadow: none;
		color: var(--on-accent);
	}

	.clock {
		font: var(--type-ui-strong);
		font-size: 0.9375rem;
		font-variant-numeric: tabular-nums;
	}

	.free {
		font: var(--type-meta);
		opacity: 0.75;
	}

	.problem {
		display: grid;
		justify-items: start;
		gap: var(--sp-2);
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.none {
		display: grid;
		gap: 2px;
		padding: var(--sp-3) 0;
	}

	.none-title {
		font: var(--type-ui-strong);
		font-size: 0.9375rem;
	}

	.none-body {
		color: var(--text-muted);
		font: var(--type-ui);
	}

	@media (prefers-reduced-motion: reduce) {
		.time,
		.chip span,
		.face {
			transition: none;
		}
	}
</style>
