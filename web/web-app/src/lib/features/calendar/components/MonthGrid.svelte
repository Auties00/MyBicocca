<script lang="ts">
	import { tick } from 'svelte';
	import {
		firstOfMonth,
		monthGridCells,
		monthOf,
		monthWeeks,
		stepByKey,
		type IsoDate,
		type YearMonth
	} from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { busyLevel } from '../busy.ts';
	import {
		clockTime,
		displayTitle,
		fullDate,
		monthYear,
		weekdayNarrow,
		weekdayShort
	} from '../format.ts';
	import { compactLabel, type CalendarEvent } from '../model.ts';
	import { eventSwatch } from '../palette.ts';

	interface Props {
		month: YearMonth;
		byDay: ReadonlyMap<IsoDate, readonly CalendarEvent[]>;
		selected: IsoDate;
		today: IsoDate;
		/** compact: Android's heat map of square days; wide: whole weeks with the events listed. */
		variant: 'compact' | 'wide';
		loading?: boolean;
		onselect: (date: IsoDate) => void;
		onopen?: (event: CalendarEvent, chip: HTMLElement) => void;
	}

	let {
		month,
		byDay,
		selected,
		today,
		variant,
		loading = false,
		onselect,
		onopen
	}: Props = $props();

	const wide = $derived(variant === 'wide');
	const weeks = $derived(wide ? monthWeeks(month) : chunk(monthGridCells(month)));
	const header = $derived(monthWeeks(month)[0]);
	let grid = $state<HTMLElement>();
	let cellHeight = $state(0);

	/* Lines of events a wide cell has room for, under its day number. */
	const lines = $derived(Math.max(0, Math.floor((cellHeight - 34) / 22)));

	function chunk(cells: (IsoDate | null)[]): (IsoDate | null)[][] {
		return Array.from({ length: Math.ceil(cells.length / 7) }, (_, index) =>
			cells.slice(index * 7, index * 7 + 7)
		);
	}

	function events(day: IsoDate): readonly CalendarEvent[] {
		return byDay.get(day) ?? [];
	}

	function label(day: IsoDate): string {
		const list = events(day);
		const parts = [
			m.calendar_day_label({
				date: fullDate(day),
				events:
					list.length === 0
						? m.calendar_no_events()
						: m.calendar_event_count({ count: list.length })
			})
		];
		if (list.some((event) => event.source === 'exam')) parts.push(m.calendar_has_exam());
		if (day === today) parts.push(m.calendar_today_suffix());
		return parts.join(', ');
	}

	/* The focused day is the selected one when it is in this month, else the 1st. */
	const focusable = $derived(monthOf(selected) === month ? selected : firstOfMonth(month));

	async function keydown(event: KeyboardEvent, day: IsoDate) {
		if (event.key === 'Enter' || event.key === ' ') {
			event.preventDefault();
			onselect(day);
			return;
		}
		const next = stepByKey(day, event.key);
		if (next === null) return;
		event.preventDefault();
		onselect(next);
		await tick();
		grid?.querySelector<HTMLElement>(`[data-date="${next}"]`)?.focus();
	}

	function measure(element: HTMLElement) {
		const observer = new ResizeObserver(([entry]) => {
			cellHeight = entry.contentRect.height;
		});
		observer.observe(element);
		return () => observer.disconnect();
	}

	function chipClick(event: MouseEvent, item: CalendarEvent) {
		event.stopPropagation();
		if (event.currentTarget instanceof HTMLElement) onopen?.(item, event.currentTarget);
	}
</script>

<div class={['month', variant]}>
	<div class="weekdays" aria-hidden="true">
		{#each header as day (day)}
			<span>{wide ? weekdayShort(day) : weekdayNarrow(day)}</span>
		{/each}
	</div>
	<div
		class="grid"
		role="grid"
		aria-label={m.calendar_month_label({ month: monthYear(firstOfMonth(month)) })}
		aria-busy={loading}
		bind:this={grid}
		style:--rows={weeks.length}
	>
		{#each weeks as week, row (row)}
			<div class="week" role="row">
				{#each week as day, column (day ?? `blank-${row}-${column}`)}
					{#if day === null}
						<span class="blank" role="gridcell"></span>
					{:else}
						{@const list = events(day)}
						{@const level = loading ? 0 : busyLevel(list)}
						{@const exam = list.some((event) => event.source === 'exam')}
						<div
							class={[
								'cell',
								`l${level}`,
								{
									outside: monthOf(day) !== month,
									today: day === today,
									selected: day === selected
								}
							]}
							role="gridcell"
							aria-selected={day === selected}
							{@attach wide && row === 0 && column === 0 ? measure : undefined}
						>
							<button
								type="button"
								class="day"
								data-date={day}
								tabindex={day === focusable ? 0 : -1}
								aria-label={label(day)}
								aria-current={day === today ? 'date' : undefined}
								onclick={() => onselect(day)}
								onkeydown={(event) => keydown(event, day)}
							>
								<span class="number">{Number(day.slice(8))}</span>
							</button>
							{#if exam && !loading}<span class="exam" aria-hidden="true"></span>{/if}
							{#if wide}
								{#if loading}
									{#if (row + column) % 3 === 0 && lines > 0}
										<span class="chip-skeleton"><Skeleton variant="text" width="80%" /></span>
									{/if}
								{:else if list.length > 0 && lines > 0}
									{@const fits = list.length <= lines ? list.length : lines - 1}
									<ul class="chips" aria-hidden="true">
										{#each list.slice(0, fits) as item (item.id)}
											{@const swatch = eventSwatch(item)}
											<li>
												<button
													type="button"
													class={['chip', 'calendar-swatch', { cancelled: item.cancelled }]}
													style:--sw-h={swatch.hue}
													style:--sw-c={swatch.chroma}
													tabindex={-1}
													data-event={item.id}
													onclick={(event) => chipClick(event, item)}
												>
													<span class="bar"></span>
													<span class="time">{clockTime(item.start)}</span>
													<span class="name" lang="it">{displayTitle(item)}</span>
													<span class="short">{compactLabel(item)}</span>
												</button>
											</li>
										{/each}
										{#if fits < list.length}
											<li>
												<button
													type="button"
													class="more"
													tabindex={-1}
													aria-label={m.calendar_more_events_label({ count: list.length - fits })}
													onclick={() => onselect(day)}
												>
													{m.calendar_more_events({ count: list.length - fits })}
												</button>
											</li>
										{/if}
									</ul>
								{/if}
							{/if}
						</div>
					{/if}
				{/each}
			</div>
		{/each}
	</div>
</div>

<style>
	.month {
		--heat: var(--accent);
		display: grid;
		gap: 6px;
		min-block-size: 0;
	}

	.weekdays,
	.week {
		display: grid;
		grid-template-columns: repeat(7, minmax(0, 1fr));
		gap: 4px;
	}

	.weekdays span {
		color: var(--text-muted);
		font: var(--type-overline);
		font-size: 0.625rem;
		letter-spacing: var(--track-overline);
		text-align: center;
		text-transform: uppercase;
	}

	.grid {
		display: grid;
		gap: 4px;
	}

	.cell {
		position: relative;
		display: grid;
		min-inline-size: 0;
		border-radius: 8px;
		background: var(--surface-sunken);
		color: var(--text);
		transition: background-color var(--dur-base) var(--ease-out);
	}

	/* Android's busyness scale: the accent mixed into the neutral fill, a quarter at a time. */
	.l1 {
		background: color-mix(in oklch, var(--heat) 25%, var(--track));
	}

	.l2 {
		background: color-mix(in oklch, var(--heat) 50%, var(--track));
	}

	.l3 {
		background: color-mix(in oklch, var(--heat) 75%, var(--track));
		color: var(--on-accent);
	}

	.l4 {
		background: var(--heat);
		color: var(--on-accent);
	}

	.compact .cell {
		aspect-ratio: 1 / 1.05;
	}

	.day {
		display: grid;
		place-items: center;
		inline-size: 100%;
		block-size: 100%;
		border-radius: inherit;
		color: inherit;
		font: 500 0.8125rem / 1 var(--font-ui);
		font-variant-numeric: tabular-nums;
		cursor: pointer;
		touch-action: manipulation;
	}

	.today .day {
		font-weight: 700;
	}

	.cell::after {
		content: '';
		position: absolute;
		inset: 0;
		border-radius: inherit;
		pointer-events: none;
	}

	.today::after {
		box-shadow: inset 0 0 0 1.5px color-mix(in oklch, var(--accent-text) 70%, transparent);
	}

	.selected::after {
		box-shadow: inset 0 0 0 2px var(--accent-text);
	}

	.l3.selected::after,
	.l4.selected::after {
		box-shadow:
			inset 0 0 0 2px var(--accent-text),
			inset 0 0 0 4px var(--surface);
	}

	.exam {
		position: absolute;
		inset-block-start: 4px;
		inset-inline-end: 4px;
		inline-size: 6px;
		block-size: 6px;
		border-radius: 50%;
		background: var(--danger-solid);
		box-shadow: 0 0 0 1.5px var(--surface);
		pointer-events: none;
	}

	.day:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	@media (hover: hover) {
		.compact .cell:not(.selected):hover {
			filter: brightness(0.97);
		}
	}

	/* Wide: a quieter tint, so the listed events stay legible on the busiest days. */
	.wide {
		block-size: 100%;
		grid-template-rows: auto minmax(0, 1fr);
	}

	.wide .weekdays span {
		padding-block-end: 2px;
		font-size: 0.6875rem;
	}

	.wide .grid {
		grid-template-rows: repeat(var(--rows), minmax(0, 1fr));
	}

	.wide .cell {
		container-type: inline-size;
		align-content: start;
		gap: 2px;
		padding: 4px;
		overflow: hidden;
		border-radius: var(--r-sm);
		background: var(--surface);
		box-shadow: inset 0 0 0 1px var(--hairline);
		color: var(--text);
		cursor: pointer;
	}

	.wide .l1 {
		background: color-mix(in oklch, var(--heat) 6%, var(--surface));
	}

	.wide .l2 {
		background: color-mix(in oklch, var(--heat) 12%, var(--surface));
	}

	.wide .l3 {
		background: color-mix(in oklch, var(--heat) 19%, var(--surface));
	}

	.wide .l4 {
		background: color-mix(in oklch, var(--heat) 27%, var(--surface));
	}

	.wide .outside {
		background: transparent;
		box-shadow: none;
	}

	/* Days of the next and previous month recede by their fill, not their ink: they stay links. */
	.wide .outside .day,
	.wide .outside .chip {
		color: var(--text-muted);
	}

	.wide .outside .chip .bar {
		opacity: 0.55;
	}

	.wide .day {
		justify-self: start;
		place-items: center;
		inline-size: 26px;
		block-size: 26px;
		border-radius: var(--r-pill);
		font-size: 0.8125rem;
	}

	.wide .day::before {
		content: '';
		position: absolute;
		inset: 0;
	}

	.wide .today .day {
		background: var(--accent);
		color: var(--on-accent);
	}

	.wide .exam {
		inset-block-start: 8px;
		inset-inline-end: 8px;
	}

	.chips {
		position: relative;
		display: grid;
		gap: 2px;
		min-inline-size: 0;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.chips li {
		min-inline-size: 0;
	}

	.chip,
	.more {
		display: flex;
		align-items: center;
		gap: 5px;
		inline-size: 100%;
		min-inline-size: 0;
		block-size: 20px;
		padding-inline: 4px 6px;
		border-radius: var(--r-2xs);
		color: var(--text);
		font: 500 0.75rem / 1 var(--font-ui);
		text-align: start;
		cursor: pointer;
	}

	.chip .bar {
		flex: none;
		inline-size: 3px;
		block-size: 12px;
		border-radius: 2px;
		background: var(--sw-accent);
	}

	/* Darker than the muted ink: it sits on the heat tint, where muted text loses contrast. */
	.chip .time {
		flex: none;
		color: color-mix(in oklch, var(--text-muted) 65%, var(--text));
		font-variant-numeric: tabular-nums;
	}

	.chip .name {
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.chip .short {
		display: none;
		font-weight: 600;
	}

	/* "09:30 Sistemi Operativi" where a column has room, "09:30 SO" or "SO" where it hasn't. */
	@container (width < 110px) {
		.chip .name {
			display: none;
		}

		.chip .short {
			display: inline;
		}
	}

	@container (width < 84px) {
		.chip .time {
			display: none;
		}
	}

	.chip.cancelled .name {
		text-decoration: line-through;
	}

	.more {
		color: var(--text-muted);
		font-weight: 600;
	}

	@media (hover: hover) {
		.chip:hover,
		.more:hover {
			background: var(--state-hover);
		}
	}

	.chip-skeleton {
		padding: 4px;
	}
</style>
