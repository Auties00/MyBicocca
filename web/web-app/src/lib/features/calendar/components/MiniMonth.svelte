<script lang="ts">
	import { tick } from 'svelte';
	import IconCaretLeft from '~icons/ph/caret-left';
	import IconCaretRight from '~icons/ph/caret-right';
	import {
		addMonths,
		firstOfMonth,
		monthOf,
		monthWeeks,
		stepByKey,
		type IsoDate,
		type YearMonth
	} from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { fullDate, monthYear, weekdayNarrow } from '../format.ts';
	import type { CalendarEvent } from '../model.ts';

	interface Props {
		selected: IsoDate;
		today: IsoDate;
		/** Days shown in the main view, tinted as one band (the week on screen). */
		range?: readonly IsoDate[];
		/** Marks the days that hold something. */
		byDay?: ReadonlyMap<IsoDate, readonly CalendarEvent[]>;
		onpick: (date: IsoDate) => void;
		/** Moves focus to the selected day once shown, as a date picker should. */
		autofocus?: boolean;
	}

	let { selected, today, range = [], byDay, onpick, autofocus = false }: Props = $props();

	/* The picker follows the calendar: a new selection elsewhere brings its month into view. */
	let shown: YearMonth = $derived(monthOf(selected));
	let focused: IsoDate = $derived(selected);
	let grid = $state<HTMLElement>();

	const weeks = $derived(monthWeeks(shown, true));
	const inRange = $derived(new Set(range));

	async function focusDay(date: IsoDate) {
		focused = date;
		if (monthOf(date) !== shown) shown = monthOf(date);
		await tick();
		grid?.querySelector<HTMLElement>(`[data-date="${date}"]`)?.focus();
	}

	function keydown(event: KeyboardEvent) {
		const next = stepByKey(focused, event.key);
		if (next === null) return;
		event.preventDefault();
		void focusDay(next);
	}

	function page(delta: number) {
		shown = addMonths(shown, delta);
		focused = monthOf(focused) === shown ? focused : firstOfMonth(shown);
	}

	function focusOnMount(element: HTMLElement) {
		if (autofocus) element.querySelector<HTMLElement>('[tabindex="0"]')?.focus();
	}
</script>

<div class="mini">
	<div class="head">
		<p class="title" aria-live="polite">{monthYear(firstOfMonth(shown))}</p>
		<div class="nav">
			<IconButton
				icon={IconCaretLeft}
				label={m.calendar_previous_month()}
				size="sm"
				onclick={() => page(-1)}
			/>
			<IconButton
				icon={IconCaretRight}
				label={m.calendar_next_month()}
				size="sm"
				onclick={() => page(1)}
			/>
		</div>
	</div>
	<div
		class="grid"
		role="grid"
		aria-label={monthYear(firstOfMonth(shown))}
		bind:this={grid}
		{@attach focusOnMount}
	>
		<div class="row" role="row">
			{#each weeks[0] as day (day)}
				<span class="weekday" role="columnheader" aria-hidden="true">{weekdayNarrow(day)}</span>
			{/each}
		</div>
		{#each weeks as week (week[0])}
			<div class="row" role="row">
				{#each week as day (day)}
					{@const outside = monthOf(day) !== shown}
					{@const busy = (byDay?.get(day)?.length ?? 0) > 0}
					<span role="gridcell" aria-selected={day === selected}>
						<button
							type="button"
							class={[
								'day',
								{
									outside,
									today: day === today,
									selected: day === selected,
									band: inRange.has(day),
									'band-start': inRange.has(day) && !inRange.has(week[week.indexOf(day) - 1]),
									'band-end': inRange.has(day) && !inRange.has(week[week.indexOf(day) + 1])
								}
							]}
							data-date={day}
							tabindex={day === focused ? 0 : -1}
							aria-label={fullDate(day)}
							aria-current={day === today ? 'date' : undefined}
							onkeydown={keydown}
							onfocus={() => (focused = day)}
							onclick={() => onpick(day)}
						>
							{Number(day.slice(8))}
							{#if busy}<span class="dot" aria-hidden="true"></span>{/if}
						</button>
					</span>
				{/each}
			</div>
		{/each}
	</div>
</div>

<style>
	.mini {
		display: grid;
		gap: var(--sp-2);
		inline-size: 100%;
	}

	.head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: var(--sp-2);
		padding-inline-start: 6px;
	}

	.title {
		font: var(--type-ui-strong);
		text-transform: none;
	}

	.nav {
		display: flex;
		gap: 2px;
	}

	.grid {
		display: grid;
		gap: 2px;
	}

	.row {
		display: grid;
		grid-template-columns: repeat(7, minmax(0, 1fr));
	}

	.row > span {
		display: grid;
	}

	.weekday {
		padding-block: 4px;
		color: var(--text-subtle);
		font: var(--type-overline);
		font-size: 0.6875rem;
		text-align: center;
	}

	.day {
		position: relative;
		display: grid;
		place-items: center;
		aspect-ratio: 1;
		min-block-size: 32px;
		border-radius: var(--r-pill);
		color: var(--text);
		font: 500 0.8125rem / 1 var(--font-ui);
		font-variant-numeric: tabular-nums;
		cursor: pointer;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.band {
		border-radius: 0;
		background: var(--surface-sunken);
	}

	.band-start {
		border-start-start-radius: var(--r-pill);
		border-end-start-radius: var(--r-pill);
	}

	.band-end {
		border-start-end-radius: var(--r-pill);
		border-end-end-radius: var(--r-pill);
	}

	.outside {
		color: var(--text-subtle);
	}

	.today {
		color: var(--accent-text);
		font-weight: 700;
	}

	.selected,
	.selected.band {
		border-radius: var(--r-pill);
		background: var(--accent);
		color: var(--on-accent);
	}

	.dot {
		position: absolute;
		inset-block-end: 4px;
		inline-size: 4px;
		block-size: 4px;
		border-radius: 50%;
		background: currentColor;
		opacity: 0.55;
	}

	@media (hover: hover) {
		.day:not(.selected):hover {
			background: var(--surface-hover);
			transition-duration: 0s;
		}
	}

	.day:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 1px;
	}
</style>
