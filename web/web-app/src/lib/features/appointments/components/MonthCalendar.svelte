<script lang="ts">
	import { tick } from 'svelte';
	import IconCaretLeft from '~icons/ph/caret-left';
	import IconCaretRight from '~icons/ph/caret-right';
	import { formatDate, formatWeekday } from '#lib/format/dates.ts';
	import {
		addDays,
		addMonths,
		firstOfMonth,
		monthGridCells,
		monthOf,
		stepByKey,
		toJsDate,
		type IsoDate,
		type YearMonth
	} from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import Spinner from '#lib/ui/Spinner.svelte';

	interface Props {
		month: YearMonth;
		/** The month's bookable days; undefined while they load. */
		available: readonly IsoDate[] | undefined;
		selected: IsoDate | undefined;
		/** Today where the desks are. */
		today: IsoDate;
		canGoBack: boolean;
		canGoForward: boolean;
		/** Why the month couldn't load, if it couldn't. */
		error: string | undefined;
		onretry: () => void;
		onmonth: (month: YearMonth) => void;
		onselect: (date: IsoDate) => void;
	}

	let {
		month,
		available,
		selected,
		today,
		canGoBack,
		canGoForward,
		error,
		onretry,
		onmonth,
		onselect
	}: Props = $props();

	/* A known Monday: the header's weekdays, Monday first, in the app's language. */
	const MONDAY = '2024-01-01';
	const weekdays = Array.from({ length: 7 }, (_, index) => toJsDate(addDays(MONDAY, index)));

	const id = $props.id();
	let grid = $state<HTMLElement>();
	let focused = $state<IsoDate>();

	/* Always six weeks, so the card keeps its height from month to month. */
	const weeks = $derived.by(() => {
		const cells = monthGridCells(month);
		const padded = [...cells, ...Array.from({ length: 42 - cells.length }, () => null)];
		return Array.from({ length: 6 }, (_, week) => padded.slice(week * 7, week * 7 + 7));
	});
	const bookable = $derived(new Set(available ?? []));
	const loading = $derived(available === undefined && error === undefined);
	/* The one day in the tab order: the focused one, else the picked one, else the first free. */
	const anchor = $derived(
		focused && monthOf(focused) === month
			? focused
			: selected && monthOf(selected) === month
				? selected
				: (available?.[0] ?? firstOfMonth(month))
	);
	const title = $derived(formatDate(toJsDate(firstOfMonth(month)), 'monthYear'));

	async function move(date: IsoDate) {
		const target = monthOf(date);
		if (target !== month) {
			if (target < month ? !canGoBack : !canGoForward) return;
			onmonth(target);
		}
		focused = date;
		await tick();
		grid?.querySelector<HTMLButtonElement>(`[data-date="${date}"]`)?.focus();
	}

	function keydown(event: KeyboardEvent & { currentTarget: HTMLButtonElement }) {
		if (event.altKey || event.ctrlKey || event.metaKey) return;
		const next = stepByKey(event.currentTarget.dataset.date ?? anchor, event.key);
		if (next === null) return;
		event.preventDefault();
		void move(next);
	}

	/* A day picked while its month loads is kept: it is selected if the month offers it. */
	function pick(date: IsoDate) {
		focused = date;
		if (bookable.has(date) || available === undefined) onselect(date);
	}
</script>

<section class="card" aria-labelledby="{id}-title">
	<header class="switcher">
		<h2 class="title" id="{id}-title" aria-live="polite">{title}</h2>
		<IconButton
			icon={IconCaretLeft}
			label={m.appointments_previous_month()}
			variant="tonal"
			disabled={!canGoBack}
			onclick={() => onmonth(addMonths(month, -1))}
		/>
		<IconButton
			icon={IconCaretRight}
			label={m.appointments_next_month()}
			variant="tonal"
			disabled={!canGoForward}
			onclick={() => onmonth(addMonths(month, 1))}
		/>
	</header>

	{#if error}
		<div class="problem" role="alert">
			<p>{error}</p>
			<Button size="sm" onclick={onretry}>{m.common_retry()}</Button>
		</div>
	{:else}
		<div class="body" aria-busy={loading}>
			<table class="grid" aria-labelledby="{id}-title" bind:this={grid}>
				<thead>
					<tr>
						{#each weekdays as weekday (weekday.getTime())}
							<th scope="col" abbr={formatWeekday(weekday, 'long')}>
								{formatWeekday(weekday, 'short').replace('.', '')}
							</th>
						{/each}
					</tr>
				</thead>
				<tbody>
					{#each weeks as week, index (index)}
						<tr>
							{#each week as date, column (column)}
								<td>
									{#if date}
										{@const free = bookable.has(date)}
										{@const full = formatDate(toJsDate(date), 'full')}
										<!-- While the month loads, a day is neither free nor taken: the grid is busy. -->
										<button
											type="button"
											class={['day', { free, picked: date === selected, today: date === today }]}
											data-date={date}
											tabindex={date === anchor ? 0 : -1}
											aria-pressed={date === selected}
											aria-disabled={loading ? undefined : !free}
											aria-current={date === today ? 'date' : undefined}
											aria-label={loading
												? full
												: free
													? m.appointments_day_available({ date: full })
													: m.appointments_day_unavailable({ date: full })}
											onclick={() => pick(date)}
											onkeydown={keydown}
										>
											{Number(date.slice(8))}
										</button>
									{/if}
								</td>
							{/each}
						</tr>
					{/each}
				</tbody>
			</table>
			{#if loading}
				<div class="loading"><Spinner size={28} label={m.appointments_month_loading()} /></div>
			{/if}
		</div>
		<p class="empty" role="status">
			{available !== undefined && available.length === 0 ? m.appointments_month_empty() : ''}
		</p>
	{/if}
</section>

<style>
	.card {
		display: grid;
		gap: var(--sp-3);
		padding: var(--sp-4) var(--sp-4) var(--sp-3);
		border-radius: var(--r-xl);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.switcher {
		display: grid;
		grid-template-columns: minmax(0, 1fr) auto auto;
		align-items: center;
		gap: var(--sp-2);
		padding-inline-start: var(--sp-2);
	}

	.title {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.title::first-letter {
		text-transform: uppercase;
	}

	.body {
		position: relative;
	}

	.grid {
		inline-size: 100%;
		border-collapse: separate;
		border-spacing: 0 2px;
		table-layout: fixed;
	}

	th {
		padding-block: 4px 6px;
		color: var(--text-muted);
		font: var(--type-label);
		text-transform: capitalize;
	}

	td {
		padding: 0;
		text-align: center;
	}

	.day {
		position: relative;
		display: inline-grid;
		place-items: center;
		inline-size: min(44px, 100%);
		aspect-ratio: 1;
		padding: 0;
		border: 0;
		border-radius: 50%;
		background: none;
		color: var(--text-subtle);
		font: var(--type-ui);
		font-variant-numeric: tabular-nums;
		cursor: default;
		transition:
			border-radius var(--dur-spring) var(--spring-standard),
			background-color var(--dur-fast) var(--ease-out),
			color var(--dur-fast) var(--ease-out);
	}

	.day.free {
		background: var(--surface-hover);
		color: var(--text);
		font-weight: 600;
		cursor: pointer;
	}

	@media (hover: hover) {
		.day.free:hover:not(.picked) {
			background: var(--container);
			color: var(--on-container);
		}
	}

	.day.today {
		box-shadow: inset 0 0 0 1.5px var(--accent);
	}

	/* The picked day turns from a circle into a rounded square. */
	.day.picked {
		border-radius: 32%;
		background: var(--accent);
		color: var(--on-accent);
		box-shadow: none;
	}

	.day:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.loading {
		position: absolute;
		inset: 0;
		display: grid;
		place-items: center;
		border-radius: var(--r-md);
		background: color-mix(in oklch, var(--surface) 55%, transparent);
	}

	.empty {
		min-block-size: 1lh;
		padding-inline: var(--sp-2);
		color: var(--text-muted);
		font: var(--type-meta);
		text-align: center;
	}

	.problem {
		display: grid;
		place-content: center;
		justify-items: center;
		gap: var(--sp-3);
		min-block-size: 300px;
		color: var(--text-muted);
		font: var(--type-meta);
		text-align: center;
	}

	@media (prefers-reduced-motion: reduce) {
		.day {
			transition: none;
		}
	}
</style>
