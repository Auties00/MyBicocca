<script lang="ts">
	import type { IsoDate } from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { fullDate, weekdayNarrow, weekdayShort } from '../format.ts';

	interface Props {
		days: readonly IsoDate[];
		today: IsoDate;
		/** Weekday names in full ("Lun") rather than initials. */
		wide?: boolean;
		/** Opens the day on its own. */
		onpick: (date: IsoDate) => void;
	}

	let { days, today, wide = false, onpick }: Props = $props();
</script>

<div class={['heads', { wide }]} style:--cols={days.length}>
	{#each days as day (day)}
		<button
			type="button"
			class={['head', { today: day === today }]}
			aria-current={day === today ? 'date' : undefined}
			onclick={() => onpick(day)}
		>
			<span class="sr-only">{m.calendar_open_day({ date: fullDate(day) })}</span>
			<span class="weekday" aria-hidden="true">{wide ? weekdayShort(day) : weekdayNarrow(day)}</span
			>
			<span class="number" aria-hidden="true">{Number(day.slice(8))}</span>
		</button>
	{/each}
</div>

<style>
	.heads {
		display: grid;
		/* The timeline's columns, where an empty weekend day takes half of one. */
		grid-template-columns: var(--day-cols, repeat(var(--cols), minmax(0, 1fr)));
		padding-block: 4px 12px;
	}

	.head {
		display: grid;
		justify-items: center;
		gap: 4px;
		padding-block: 6px;
		border-radius: var(--r-md);
		color: var(--text);
		cursor: pointer;
		touch-action: manipulation;
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
		display: grid;
		place-items: center;
		inline-size: 32px;
		block-size: 32px;
		border-radius: var(--r-pill);
		font: 500 1rem / 1 var(--font-ui);
		font-variant-numeric: tabular-nums;
	}

	.wide .number {
		inline-size: 36px;
		block-size: 36px;
		font: 450 1.375rem / 1 var(--font-display);
	}

	.today .weekday {
		color: var(--accent-text);
	}

	.today .number {
		background: var(--accent);
		color: var(--on-accent);
		font-weight: 600;
	}

	@media (hover: hover) {
		.head:hover {
			background: var(--state-hover);
		}
	}

	.head:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: -2px;
	}
</style>
