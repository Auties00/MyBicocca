<script lang="ts">
	import { tick, type Snippet } from 'svelte';
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
	import { reducedMotion } from '#lib/ui/media.ts';
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
		/** Wide: the weekday names above the grid, which the week's head draws and animates. */
		heads?: Snippet;
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
		heads,
		onselect,
		onopen
	}: Props = $props();

	const wide = $derived(variant === 'wide');
	const weeks = $derived(wide ? monthWeeks(month) : chunk(monthGridCells(month)));
	const header = $derived(monthWeeks(month)[0]);
	let grid = $state<HTMLElement>();
	let stage = $state<HTMLElement>();
	let cellHeight = $state(0);

	/* Lines of events a wide cell has room for, under its day number. */
	const lines = $derived(Math.max(0, Math.floor((cellHeight - 46) / 22)));

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
		/* Up and down page through the months there, as the wheel does: the calendar handles them. */
		if (wide && (event.key === 'ArrowUp' || event.key === 'ArrowDown')) return;
		const next = stepByKey(day, event.key);
		if (next === null) return;
		event.preventDefault();
		onselect(next);
		await tick();
		grid?.querySelector<HTMLElement>(`[data-date="${next}"]`)?.focus();
	}

	/*
	 * Changing month scrolls: the grid that was there is copied just before the new one replaces it,
	 * and both move up (the next month) or down (the previous one), the copy out and the new in.
	 */
	let shown: YearMonth | undefined;
	let leaving: { copy: HTMLElement; direction: 1 | -1 } | undefined;
	let scrolling: Animation[] = [];

	function finishScroll() {
		scrolling.forEach((animation) => animation.cancel());
		scrolling = [];
		leaving?.copy.remove();
		leaving = undefined;
		stage?.classList.remove('scrolling');
	}

	$effect.pre(() => {
		const current = month;
		const previous = shown;
		shown = current;
		if (!wide || previous === undefined || previous === current || !grid || reducedMotion.current)
			return;
		finishScroll();
		const copy = grid.cloneNode(true) as HTMLElement;
		copy.setAttribute('inert', '');
		copy.setAttribute('aria-hidden', 'true');
		copy.classList.add('leaving');
		leaving = { copy, direction: current > previous ? 1 : -1 };
	});

	$effect(() => {
		void month;
		if (!leaving || !grid || !stage) return;
		const { copy, direction } = leaving;
		stage.classList.add('scrolling');
		// The outgoing month is a copy outside Svelte's tree, removed when the scroll ends.
		// eslint-disable-next-line svelte/no-dom-manipulating
		stage.append(copy);
		const timing = {
			duration: 420,
			easing: 'cubic-bezier(0.16, 1, 0.3, 1)',
			fill: 'both'
		} as const;
		// The outline fades on a gentler curve than the scroll, so the fade can be seen.
		const fade = { duration: 420, easing: 'ease-in-out', fill: 'both' } as const;
		scrolling = [
			grid.animate(
				[{ transform: `translateY(${direction * 100}%)` }, { transform: 'translateY(0)' }],
				timing
			),
			copy.animate(
				[{ transform: 'translateY(0)' }, { transform: `translateY(${-direction * 100}%)` }],
				timing
			),
			grid.animate([{ '--edge': 0 }, { '--edge': 1 }], fade),
			copy.animate([{ '--edge': 1 }, { '--edge': 0 }], fade)
		];
		scrolling[0].finished.then(finishScroll, () => {});
	});

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
	{#if wide && heads}
		{@render heads()}
	{:else}
		<div class="weekdays" aria-hidden="true">
			{#each header as day (day)}
				<span>{wide ? weekdayShort(day) : weekdayNarrow(day)}</span>
			{/each}
		</div>
	{/if}
	<div class="stage" bind:this={stage}>
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
</div>

<style>
	.month {
		--heat: var(--accent);
		display: grid;
		gap: 6px;
		min-block-size: 0;
	}

	.stage {
		position: relative;
		min-block-size: 0;
	}

	/* While two months pass each other, the stage shows only what is inside it. */
	.stage:global(.scrolling) {
		overflow: clip;
	}

	.wide .stage > .grid {
		block-size: 100%;
	}

	.grid.leaving {
		position: absolute;
		inset: 0;
		pointer-events: none;
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

	/* The same columns as the head above and the timeline: Saturday and Sunday narrow when empty. */
	.wide .week {
		grid-template-columns: var(--wk-tracks, repeat(7, minmax(0, 1fr)));
		column-gap: var(--wk-gap, 4px);
	}

	.wide .cell {
		container-type: inline-size;
		align-content: start;
		gap: 2px;
		padding: 4px;
		overflow: hidden;
		border-radius: var(--r-sm);
		background: var(--surface);
		box-shadow: inset 0 0 0 1px
			color-mix(in oklch, var(--hairline) calc(var(--edge, 1) * 100%), transparent);
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

	/* Days of the next and previous month recede: fainter ink and fainter events, still links. */
	.wide .outside .day {
		color: var(--text-subtle);
	}

	.wide .outside .chips {
		opacity: 0.5;
	}

	.wide .day {
		justify-self: start;
		place-items: center;
		inline-size: 32px;
		block-size: 32px;
		/* The same inset as the events' colour bars (the cell's 4px and the chip's own 4px). */
		margin-block-start: 4px;
		margin-inline-start: 4px;
		border-radius: var(--r-pill);
		font-size: 1.0625rem;
	}

	.wide .day::before {
		content: '';
		position: absolute;
		inset: 0;
	}

	/*
	 * A rounded square whose corners follow the cell's (its 10px radius less the 4px of padding).
	 * The digits are centred on their own height: the trim goes on the number's box, since the
	 * button around it is a grid and has no lines of its own to trim.
	 */
	.wide .today .day {
		border-radius: calc(var(--r-sm) - 4px);
		background: var(--accent);
		color: var(--on-accent);
		font: 500 1.25rem / 1 var(--font-display);
	}

	.wide .today .number {
		text-box: trim-both cap alphabetic;
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
