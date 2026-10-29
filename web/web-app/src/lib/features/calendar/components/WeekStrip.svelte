<script lang="ts">
	import { weekDays, type IsoDate } from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { fullDate, weekdayNarrow, weekdayShort } from '../format.ts';
	import type { CalendarEvent } from '../model.ts';

	interface Props {
		/** The selected day: the pill sits on it. */
		date: IsoDate;
		today: IsoDate;
		byDay: ReadonlyMap<IsoDate, readonly CalendarEvent[]>;
		/** While the day pages are dragged: how far toward the next day (positive) or previous one. */
		drag?: number;
		/** Weekday names in full ("Lun") rather than initials. */
		wide?: boolean;
		onpick: (date: IsoDate) => void;
	}

	let { date, today, byDay, drag = 0, wide = false, onpick }: Props = $props();

	const days = $derived(weekDays(date));
	const index = $derived(days.indexOf(date));
	const position = $derived(Math.min(6, Math.max(0, index + drag)));

	function dayLabel(day: IsoDate): string {
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

<div class={['strip', { wide }]} style:--pos={position} style:--dragging={drag === 0 ? 0 : 1}>
	<span class="pill" aria-hidden="true"></span>
	{#each days as day (day)}
		{@const busy = (byDay.get(day)?.length ?? 0) > 0}
		<button
			type="button"
			class={['day', { selected: day === date, today: day === today }]}
			aria-pressed={day === date}
			onclick={() => onpick(day)}
		>
			<span class="sr-only">{dayLabel(day)}</span>
			<span class="weekday" aria-hidden="true">{wide ? weekdayShort(day) : weekdayNarrow(day)}</span
			>
			<span class="number" aria-hidden="true">{Number(day.slice(8))}</span>
			<span class={['dot', { busy }]} aria-hidden="true"></span>
		</button>
	{/each}
</div>

<style>
	.strip {
		position: relative;
		display: grid;
		grid-template-columns: repeat(7, minmax(0, 1fr));
		gap: 2px;
		isolation: isolate;
	}

	.pill {
		position: absolute;
		inset-block: 0;
		inset-inline-start: 0;
		inline-size: calc((100% - 12px) / 7);
		border-radius: var(--r-pill);
		background: var(--accent);
		transform: translateX(calc(var(--pos) * (100% + 2px)));
		transition: transform calc(var(--dur-spring) * (1 - var(--dragging))) var(--spring-standard);
		z-index: -1;
	}

	.wide .pill {
		border-radius: var(--r-md);
	}

	.day {
		display: grid;
		justify-items: center;
		align-content: center;
		gap: 2px;
		block-size: 60px;
		border-radius: var(--r-pill);
		color: var(--text);
		cursor: pointer;
		touch-action: manipulation;
		transition: color var(--dur-fast) var(--ease-out);
	}

	.wide .day {
		block-size: 56px;
		border-radius: var(--r-md);
	}

	.weekday {
		color: var(--text-muted);
		font: 600 0.625rem / 1.2 var(--font-ui);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.wide .weekday {
		font-size: 0.6875rem;
	}

	.number {
		font: 500 0.9375rem / 1.2 var(--font-ui);
		font-variant-numeric: tabular-nums;
	}

	.today .number {
		color: var(--accent-text);
		font-weight: 700;
	}

	.selected,
	.selected .weekday,
	.selected .number {
		color: var(--on-accent);
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

	@media (hover: hover) {
		.day:not(.selected):hover {
			background: var(--state-hover);
		}
	}

	.day:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}
</style>
