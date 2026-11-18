<script lang="ts">
	import { goto } from '$app/navigation';
	import { resolve } from '$app/paths';
	import { formatDate } from '#lib/format/dates.ts';
	import { cached, intlLocale } from '#lib/format/locale.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { calendarWeeks, dayAnchor, DAY_TONES, type DayTone } from '../calendar.ts';
	import { LOOK_AHEAD_DAYS, LOOK_BACK_DAYS, type Deadline } from '../deadlines.ts';
	import { reducedMotion } from '#lib/ui/media.ts';

	interface Props {
		deadlines: readonly Deadline[];
		today: Date;
	}

	let { deadlines, today }: Props = $props();

	const weeks = $derived(calendarWeeks(deadlines, today));

	const at = (offset: number) =>
		new Date(today.getFullYear(), today.getMonth(), today.getDate() + offset);

	/** Each weekday's initial, with its full name for screen readers. */
	const weekdays = $derived.by(() => {
		const tag = intlLocale();
		const formatter = (weekday: 'long' | 'narrow') =>
			cached('dtf', tag, { weekday }, () => new Intl.DateTimeFormat(tag, { weekday }));
		return weeks[0].map((day) => ({
			long: formatter('long').format(day.date),
			narrow: formatter('narrow').format(day.date)
		}));
	});

	const legend: Record<DayTone, () => string> = {
		overdue: m.services_legend_overdue,
		urgent: m.services_legend_urgent,
		calm: m.services_legend_later
	};

	/** Only the tones on show. */
	const tones = $derived(
		DAY_TONES.filter((tone) => weeks.some((week) => week.some((day) => day.tones.includes(tone))))
	);

	const range = $derived.by(() => {
		const tag = intlLocale();
		const options = { month: 'long', year: 'numeric' } as const;
		const format = cached('dtf', tag, options, () => new Intl.DateTimeFormat(tag, options));
		return format.formatRange(at(-LOOK_BACK_DAYS), at(LOOK_AHEAD_DAYS));
	});

	/**
	 * Brings the day's first deadline into view and focuses it, as a jump link would; a day the page
	 * doesn't list (beyond a preview) opens the deadlines page there.
	 */
	function jump(iso: string) {
		const target = document.getElementById(dayAnchor(iso));
		if (!target) {
			void goto(resolve(`services/deadlines#${dayAnchor(iso)}`));
			return;
		}
		const reduced = reducedMotion.current;
		target.scrollIntoView({ block: 'center', behavior: reduced ? 'auto' : 'smooth' });
		target.focus({ preventScroll: true });
	}
</script>

<div class="calendar">
	<table class="grid">
		<caption class="range">{range}</caption>
		<thead>
			<tr>
				{#each weekdays as weekday (weekday.long)}
					<th scope="col" abbr={weekday.long}>{weekday.narrow}</th>
				{/each}
			</tr>
		</thead>
		<tbody>
			{#each weeks as week (week[0].iso)}
				<tr>
					{#each week as day (day.iso)}
						<td class={[day.tone, { today: day.isToday }]}>
							<!-- Days outside the window only pad the weeks: their cells stay blank. -->
							{#if day.inWindow && day.tones.length > 0}
								<button
									type="button"
									class="day"
									aria-current={day.isToday ? 'date' : undefined}
									aria-label={m.services_calendar_day({
										date: formatDate(day.date, 'full'),
										count: day.tones.length
									})}
									onclick={() => jump(day.iso)}
								>
									<span class="number">{day.date.getDate()}</span>
									<span class="dots" aria-hidden="true">
										{#each day.tones.slice(0, 3) as tone, index (index)}
											<span class={['dot', tone]}></span>
										{/each}
									</span>
								</button>
							{:else if day.inWindow && day.isToday}
								<span class="day" aria-current="date">
									<span class="number">{day.date.getDate()}</span>
								</span>
							{:else if day.inWindow}
								<span class="day">{day.date.getDate()}</span>
							{/if}
						</td>
					{/each}
				</tr>
			{/each}
		</tbody>
	</table>
	<ul class="legend" role="list">
		{#each tones as tone (tone)}
			<li><span class={['dot', tone]} aria-hidden="true"></span>{legend[tone]()}</li>
		{/each}
	</ul>
</div>

<style>
	.calendar {
		display: grid;
		gap: 10px;
		padding: 16px 14px 14px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.range {
		padding: 0 4px 10px;
		font: var(--type-ui-strong);
		text-align: start;
	}

	.range::first-letter {
		text-transform: uppercase;
	}

	.grid {
		inline-size: 100%;
		border-collapse: collapse;
		table-layout: fixed;
	}

	th {
		padding-block-end: 4px;
		color: var(--text-subtle);
		font: var(--type-overline);
		letter-spacing: 0;
		text-align: center;
	}

	td {
		padding: 1px;
		text-align: center;
	}

	.day {
		position: relative;
		display: grid;
		place-items: center;
		inline-size: 100%;
		aspect-ratio: 1;
		max-block-size: 40px;
		margin: 0;
		padding: 0;
		border: 0;
		border-radius: var(--r-sm);
		background: transparent;
		color: var(--text);
		font: var(--type-ui);
		font-variant-numeric: tabular-nums;
	}

	button.day {
		cursor: pointer;
		font-weight: 600;
		background: var(--surface-sunken);
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		td button.day:hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	/* Days with an urgent or overdue deadline take a wash of its colour: the strip reads as heat. */
	.urgent button.day {
		background: color-mix(in oklch, var(--accent) 14%, var(--surface));
	}

	.overdue button.day {
		background: color-mix(in oklch, var(--danger) 14%, var(--surface));
	}

	/* Today is a filled numeral, so a ring on the grid only ever means keyboard focus. */
	.today .number {
		display: grid;
		place-items: center;
		inline-size: 24px;
		block-size: 24px;
		border-radius: 50%;
		background: var(--accent);
		color: var(--on-accent);
		font-weight: 700;
	}

	.dots {
		position: absolute;
		inset-block-end: 3px;
		display: flex;
		gap: 3px;
	}

	.dot {
		display: inline-block;
		inline-size: 6px;
		block-size: 6px;
		border-radius: 50%;
		background: var(--text-muted);
	}

	.dot.urgent {
		background: var(--accent);
	}

	/* A ring, so overdue reads apart from urgent without relying on the two reds. */
	.dot.overdue {
		background: transparent;
		box-shadow: inset 0 0 0 1.75px var(--danger);
	}

	.legend {
		display: flex;
		flex-wrap: wrap;
		gap: 4px 14px;
		margin: 2px 0 0;
		padding: 10px 4px 0;
		border-block-start: 1px solid var(--hairline);
		color: var(--text-muted);
		font: var(--type-meta);
		list-style: none;
	}

	.legend li {
		display: inline-flex;
		align-items: center;
		gap: 6px;
	}

	.legend .dot {
		inline-size: 8px;
		block-size: 8px;
	}
</style>
