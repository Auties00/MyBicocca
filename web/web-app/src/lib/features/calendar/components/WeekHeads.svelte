<script lang="ts">
	import type { IsoDate } from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { fullDate, weekdayShort } from '../format.ts';
	import type { CalendarEvent } from '../model.ts';

	interface Props {
		/** What the calendar shows: month keeps the names and folds the numbers away. */
		mode: 'day' | 'week' | 'month';
		/** The seven days of the selected day's week. */
		days: readonly IsoDate[];
		/** The selected day: the pill sits on it. */
		date: IsoDate;
		today: IsoDate;
		byDay: ReadonlyMap<IsoDate, readonly CalendarEvent[]>;
		onpick: (date: IsoDate) => void;
	}

	let { mode, days, date, today, byDay, onpick }: Props = $props();

	function label(day: IsoDate): string {
		if (mode === 'week') return m.calendar_open_day({ date: fullDate(day) });
		const count = byDay.get(day)?.length ?? 0;
		return [
			fullDate(day),
			day === today ? m.calendar_today_suffix() : null,
			count > 0 ? m.calendar_event_count({ count }) : m.calendar_no_events()
		]
			.filter(Boolean)
			.join(', ');
	}
</script>

<!-- In month the grid below names its own days: this row is only the weekday names there. -->
<div
	class={['heads', mode]}
	inert={mode === 'month'}
	aria-hidden={mode === 'month' ? true : undefined}
>
	{#each days as day (day)}
		{@const selected = day === date}
		<button
			type="button"
			class={['head', { selected, today: day === today }]}
			aria-current={day === today ? 'date' : undefined}
			aria-pressed={mode === 'day' ? selected : undefined}
			onclick={() => onpick(day)}
		>
			<span class="sr-only">{label(day)}</span>
			<span class="weekday" aria-hidden="true">{weekdayShort(day)}</span>
			<span class="figures" aria-hidden="true">
				<span class="number">{Number(day.slice(8))}</span>
				<span class={['dot', { busy: (byDay.get(day)?.length ?? 0) > 0 }]}></span>
			</span>
		</button>
	{/each}
</div>

<style>
	.heads {
		display: grid;
		grid-template-columns: var(--wk-tracks, repeat(7, minmax(0, 1fr)));
		column-gap: var(--wk-gap, 0px);
		padding-block: 4px calc(10px * var(--wk-figure, 1));
	}

	.head {
		display: grid;
		justify-items: center;
		align-content: center;
		min-inline-size: 0;
		padding-block: 8px;
		border-radius: var(--r-md);
		color: var(--text);
		cursor: pointer;
		touch-action: manipulation;
	}

	.weekday {
		color: var(--text-muted);
		font: 600 0.6875rem / 1.2 var(--font-ui);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	/* The day numbers fold away as the month arrives, and unfold as it leaves. */
	.figures {
		display: grid;
		justify-items: center;
		gap: 4px;
		block-size: calc(var(--wk-figure, 1) * 26px);
		margin-block-start: calc(var(--wk-figure, 1) * 2px);
		overflow: clip;
		opacity: var(--wk-figure, 1);
	}

	.number {
		font: 500 0.9375rem / 1.2 var(--font-ui);
		font-variant-numeric: tabular-nums;
	}

	.today .number {
		color: var(--accent-text);
		font-weight: 700;
	}

	.dot {
		inline-size: 4px;
		block-size: 4px;
		border-radius: 50%;
	}

	.dot.busy {
		background: currentColor;
		opacity: 0.5;
	}

	/* The selected day: Android's accent pill, in every view that has numbers to show. */
	.selected {
		background: color-mix(in oklch, var(--accent) calc(var(--wk-figure, 1) * 100%), transparent);
	}

	.selected .weekday {
		color: color-mix(
			in oklch,
			var(--on-accent) calc(var(--wk-figure, 1) * 100%),
			var(--text-muted)
		);
	}

	.selected .number {
		color: var(--on-accent);
	}

	.selected .dot.busy {
		background: var(--on-accent);
	}

	@media (hover: hover) {
		.head:not(.selected):hover {
			background: var(--state-hover);
		}
	}

	.head:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: -2px;
	}
</style>
