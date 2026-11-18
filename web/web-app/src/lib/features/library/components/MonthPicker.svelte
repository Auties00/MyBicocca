<script lang="ts">
	import { tick } from 'svelte';
	import IconCaretLeft from '~icons/ph/caret-left';
	import IconCaretRight from '~icons/ph/caret-right';
	import { formatDate, formatWeekday } from '#lib/format/dates.ts';
	import {
		addDays,
		addMonths,
		firstOfMonth,
		monthOf,
		monthWeeks,
		stepByKey,
		toJsDate,
		type IsoDate,
		type YearMonth
	} from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import IconButton from '#lib/ui/IconButton.svelte';

	interface Props {
		/** The bookable days, sorted. */
		days: readonly IsoDate[];
		selected: IsoDate | undefined;
		/** Today where the library is. */
		today: IsoDate;
		onselect: (day: IsoDate) => void;
	}

	let { days, selected, today, onselect }: Props = $props();

	const id = $props.id();
	const bookable = $derived(new Set(days));
	const first = $derived(days[0] ? monthOf(days[0]) : monthOf(today));
	const last = $derived(days.at(-1) ? monthOf(days.at(-1) ?? today) : first);

	let shownMonth = $state<YearMonth>();
	const month = $derived(shownMonth ?? monthOf(selected ?? days[0] ?? today));
	let focused = $state<IsoDate>();

	/* A known Monday: the header's weekdays, Monday first, in the app's language. */
	const MONDAY = '2024-01-01';
	const weekdays = Array.from({ length: 7 }, (_, index) => toJsDate(addDays(MONDAY, index)));

	const weeks = $derived(monthWeeks(month, true));
	const title = $derived.by(() => {
		const text = formatDate(toJsDate(firstOfMonth(month)), 'monthYear');
		return text.charAt(0).toLocaleUpperCase() + text.slice(1);
	});
	/* The one day in the tab order: the focused one, else the picked one, else the first bookable. */
	const anchor = $derived.by(() => {
		for (const candidate of [focused, selected, days.find((day) => monthOf(day) === month)]) {
			if (candidate && monthOf(candidate) === month) return candidate;
		}
		return firstOfMonth(month);
	});

	let grid = $state<HTMLElement>();

	async function move(day: IsoDate) {
		const target = monthOf(day);
		if (target < first || target > last) return;
		shownMonth = target;
		focused = day;
		await tick();
		grid?.querySelector<HTMLButtonElement>(`[data-day="${day}"]`)?.focus();
	}

	function keydown(event: KeyboardEvent & { currentTarget: HTMLButtonElement }) {
		if (event.altKey || event.ctrlKey || event.metaKey) return;
		const next = stepByKey(event.currentTarget.dataset.day ?? anchor, event.key);
		if (next === null) return;
		event.preventDefault();
		void move(next);
	}

	function pick(day: IsoDate) {
		focused = day;
		if (bookable.has(day)) onselect(day);
	}
</script>

<div class="picker">
	<header class="switcher">
		<h3 class="title" id="{id}-title" aria-live="polite">{title}</h3>
		<IconButton
			icon={IconCaretLeft}
			label={m.library_previous_month()}
			variant="tonal"
			size="sm"
			disabled={month <= first}
			onclick={() => (shownMonth = addMonths(month, -1))}
		/>
		<IconButton
			icon={IconCaretRight}
			label={m.library_next_month()}
			variant="tonal"
			size="sm"
			disabled={month >= last}
			onclick={() => (shownMonth = addMonths(month, 1))}
		/>
	</header>

	<table class="grid" aria-labelledby="{id}-title" bind:this={grid}>
		<thead>
			<tr>
				{#each weekdays as weekday (weekday.getTime())}
					<th scope="col" abbr={formatWeekday(weekday, 'long')}>
						{formatWeekday(weekday, 'narrow')}
					</th>
				{/each}
			</tr>
		</thead>
		<tbody>
			{#each weeks as week, index (index)}
				<tr>
					{#each week as day (day)}
						<td>
							{#if monthOf(day) === month}
								{@const free = bookable.has(day)}
								{@const label = formatDate(toJsDate(day), 'full')}
								<button
									type="button"
									class={['day', { free, picked: day === selected, today: day === today }]}
									data-day={day}
									tabindex={day === anchor ? 0 : -1}
									aria-pressed={day === selected}
									aria-disabled={!free}
									aria-current={day === today ? 'date' : undefined}
									aria-label={free
										? m.library_day_available({ date: label })
										: m.library_day_unavailable({ date: label })}
									onclick={() => pick(day)}
									onkeydown={keydown}
								>
									{Number(day.slice(8))}
								</button>
							{/if}
						</td>
					{/each}
				</tr>
			{/each}
		</tbody>
	</table>
</div>

<style>
	.picker {
		display: grid;
		gap: var(--sp-3);
	}

	.switcher {
		display: grid;
		grid-template-columns: minmax(0, 1fr) auto auto;
		align-items: center;
		gap: var(--sp-2);
	}

	.title {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.grid {
		inline-size: 100%;
		border-collapse: collapse;
		table-layout: fixed;
	}

	th {
		padding-block-end: 6px;
		color: var(--text-muted);
		font: var(--type-label);
		text-transform: uppercase;
	}

	td {
		padding: 2px;
		text-align: center;
	}

	.day {
		display: grid;
		place-items: center;
		inline-size: 100%;
		max-inline-size: 48px;
		aspect-ratio: 1;
		margin-inline: auto;
		border: 0;
		border-radius: 50%;
		background: transparent;
		color: var(--text-subtle);
		font: var(--type-ui);
		font-variant-numeric: tabular-nums;
		cursor: default;
		touch-action: manipulation;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			border-radius var(--dur-spring) var(--spring-pop),
			transform var(--dur-spring) var(--spring-pop);
	}

	.free {
		background: var(--surface-sunken);
		color: var(--text);
		font-weight: 600;
		cursor: pointer;
	}

	@media (hover: hover) {
		.free:not(.picked):hover {
			background: var(--container);
			color: var(--on-container);
			transition-duration: 0s;
		}
	}

	.free:active {
		transform: scale(0.94);
		transition-duration: var(--dur-instant);
	}

	.today:not(.picked) {
		box-shadow: inset 0 0 0 1.5px var(--accent);
		color: var(--accent-text);
	}

	/* The picked day squares off a little, as Android morphs it. */
	.picked {
		border-radius: 32%;
		background: var(--accent);
		color: var(--on-accent);
		font-weight: 700;
	}

	@media (prefers-reduced-motion: reduce) {
		.day {
			transition: none;
		}
	}
</style>
