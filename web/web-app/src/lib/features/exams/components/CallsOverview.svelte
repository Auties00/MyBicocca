<script lang="ts">
	import { tick } from 'svelte';
	import type { ResolvedPathname } from '$app/types';
	import IconCaretLeft from '~icons/ph/caret-left';
	import IconCaretRight from '~icons/ph/caret-right';
	import IconCheckCircle from '~icons/ph/check-circle-fill';
	import { formatDate, formatTime, formatWeekday } from '#lib/format/dates.ts';
	import { toIsoDate, toJsDate } from '#lib/format/days.ts';
	import { capitalize } from '#lib/format/names.ts';
	import { m } from '#lib/paraglide/messages.js';
	import IconButton from '#lib/ui/IconButton.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import type { Status } from '#lib/ui/types.ts';
	import { callKindLabel, callState } from '../format.ts';
	import { bookingHref } from '../links.ts';
	import { callLabel, type Booking, type ExamCall } from '../model.ts';
	import {
		byDay,
		dayTone,
		firstDay,
		monthKey,
		monthsBetween,
		monthWeeks,
		monthWithDays,
		weekStep,
		type DayTone
	} from '../overview.ts';

	interface Props {
		calls: readonly ExamCall[];
		/** Upcoming bookings of the same courses. */
		booked: readonly Booking[];
		now: Date;
		href: (call: ExamCall) => ResolvedPathname;
		/** The picked day ("2026-11-07"), kept by the page while a call covers the calendar. */
		picked?: string;
	}

	let { calls, booked, now, href, picked = $bindable() }: Props = $props();

	interface Entry {
		id: string;
		at: Date;
		/** "09:30", or null when Esse3 gives no time. */
		time: string | null;
		tone: DayTone;
		course: string;
		line: string;
		/** Where it stands in a word, for the day's name on a screen reader. */
		status: string;
		/** What the row marks beside it, as the list does: a call closing soon, or a booking. */
		mark?: 'closing' | 'booked';
		href: ResolvedPathname;
	}

	const TONES: Partial<Record<Status, DayTone>> = {
		success: 'open',
		warning: 'closing',
		neutral: 'upcoming',
		danger: 'closed'
	};

	const entries = $derived.by(() => {
		const items: Entry[] = [];
		for (const call of calls) {
			if (!call.date) continue;
			const state = callState(call, now);
			const tone = TONES[state.status] ?? 'open';
			items.push({
				id: `call:${call.slug}`,
				at: call.date,
				time: call.timed ? formatTime(call.date) : null,
				tone,
				course: call.title || m.exams_call_unknown(),
				line: [
					callKindLabel(call.kind, call.callType),
					callLabel(call.description, call.title),
					state.window
				]
					.filter(Boolean)
					.join(' · '),
				status: state.label,
				mark: tone === 'closing' ? 'closing' : undefined,
				href: href(call)
			});
		}
		for (const booking of booked) {
			if (!booking.at) continue;
			items.push({
				id: `booking:${booking.slug}`,
				at: booking.at,
				time: booking.timed ? formatTime(booking.at) : null,
				tone: 'booked',
				course: booking.title || m.exams_call_unknown(),
				line: [m.exams_booked_chip(), callKindLabel(booking.kind, booking.callType), booking.room]
					.filter(Boolean)
					.join(' · '),
				status: m.exams_booked_chip(),
				mark: 'booked',
				href: bookingHref(booking.slug)
			});
		}
		return items.sort((a, b) => a.at.getTime() - b.at.getTime());
	});

	const days = $derived(byDay(entries));
	const keys = $derived([...days.keys()]);
	const months = $derived.by(() => {
		const first = entries[0]?.at;
		const last = entries.at(-1)?.at;
		return first && last
			? monthsBetween(first, last)
			: [new Date(now.getFullYear(), now.getMonth(), 1)];
	});

	/* A month paged to that has no exams, where no day can be picked. */
	let paged = $state<string>();

	const day = $derived(picked !== undefined && days.has(picked) ? picked : firstDay(days));
	const month = $derived(
		months.find((item) => monthKey(item) === paged) ??
			months.find((item) => day !== undefined && day.startsWith(monthKey(item))) ??
			months[0]
	);
	const index = $derived(months.indexOf(month));
	const shownDay = $derived(day?.startsWith(monthKey(month)) ? day : undefined);
	const shownEntries = $derived(shownDay ? (days.get(shownDay) ?? []) : []);
	const weeks = $derived(monthWeeks(month));
	const today = $derived(toIsoDate(now));

	const LEGEND: Record<DayTone, (inputs: { count: number }) => string> = {
		open: m.exams_legend_open,
		closing: m.exams_legend_closing,
		upcoming: m.exams_legend_upcoming,
		closed: m.exams_legend_closed,
		booked: m.exams_booked_count
	};

	const counts = $derived(
		(['open', 'closing', 'upcoming', 'closed', 'booked'] as const)
			.map((tone) => {
				const count = entries.filter((entry) => entry.tone === tone).length;
				return { tone, count, label: LEGEND[tone]({ count }) };
			})
			.filter((item) => item.count > 0)
	);

	/* A known Monday, to name the columns in the app's language. */
	const weekdays = $derived(
		Array.from({ length: 7 }, (_, offset) => {
			const date = new Date(2024, 0, 1 + offset);
			return {
				short: formatWeekday(date, 'short').replace('.', ''),
				long: formatWeekday(date, 'long')
			};
		})
	);

	const headingId = $props.id();
	// eslint-disable-next-line svelte/prefer-svelte-reactivity -- read only by key handlers: nothing renders from it
	const buttons = new Map<string, HTMLButtonElement>();
	let monthName = $state<HTMLElement>();

	const register = (key: string) => (node: HTMLButtonElement) => {
		buttons.set(key, node);
		return () => {
			if (buttons.get(key) === node) buttons.delete(key);
		};
	};

	function dayLabel(key: string, list: readonly Entry[]): string {
		const items = list.map((entry) => [entry.course, entry.status].join(', '));
		return `${formatDate(toJsDate(key), 'full')}: ${items.join('; ')}`;
	}

	/*
	 * The month buttons turn one month at a time, quiet ones included. Reaching the first or last
	 * month disables the button just pressed, which would drop the focus: the month's picked day,
	 * else its name, takes it.
	 */
	async function turn(step: number, event: MouseEvent) {
		const button = event.currentTarget as HTMLButtonElement;
		const next = months[index + step];
		if (!next) return;
		paged = monthKey(next);
		picked = firstDay(days, paged);
		await tick();
		if (!button.disabled) return;
		(shownDay ? buttons.get(shownDay) : monthName)?.focus();
	}

	async function focusDay(key: string) {
		picked = key;
		paged = undefined;
		await tick();
		buttons.get(key)?.focus();
	}

	/**
	 * Left and Right step through the days with exams, Up and Down by weeks over them, across
	 * months; Page Up and Down turn to the nearest month with exams, so a day keeps the focus.
	 */
	function keydown(event: KeyboardEvent) {
		if (!shownDay) return;
		const position = keys.indexOf(shownDay);
		const inMonth = keys.filter((key) => key.startsWith(monthKey(month)));
		let target: string | undefined;
		switch (event.key) {
			case 'ArrowRight':
				target = keys[position + 1];
				break;
			case 'ArrowLeft':
				target = position > 0 ? keys[position - 1] : undefined;
				break;
			case 'ArrowDown':
			case 'ArrowUp':
				target = weekStep(keys, shownDay, event.key === 'ArrowDown' ? 1 : -1);
				break;
			case 'Home':
				target = inMonth[0];
				break;
			case 'End':
				target = inMonth.at(-1);
				break;
			case 'PageUp':
			case 'PageDown': {
				const next = monthWithDays(keys, monthKey(month), event.key === 'PageDown' ? 1 : -1);
				target = next && firstDay(days, next);
				break;
			}
			default:
				return;
		}
		event.preventDefault();
		if (target) void focusDay(target);
	}
</script>

<section class="overview" aria-labelledby={headingId}>
	<header class="head">
		<h2 id={headingId} class="title">{m.exams_overview_title()}</h2>
		<p class="hint">{m.exams_overview_hint()}</p>
	</header>

	<div class="body">
		<div class="month">
			<div class="month-head">
				<IconButton
					icon={IconCaretLeft}
					label={m.exams_previous_month()}
					size="sm"
					disabled={index <= 0}
					onclick={(event) => turn(-1, event)}
				/>
				<p class="month-name" aria-live="polite" tabindex="-1" bind:this={monthName}>
					{capitalize(formatDate(month, 'monthYear'))}
				</p>
				<IconButton
					icon={IconCaretRight}
					label={m.exams_next_month()}
					size="sm"
					disabled={index >= months.length - 1}
					onclick={(event) => turn(1, event)}
				/>
			</div>

			<table class="grid">
				<caption class="sr-only">{capitalize(formatDate(month, 'monthYear'))}</caption>
				<thead>
					<tr>
						{#each weekdays as weekday (weekday.long)}
							<th scope="col" abbr={weekday.long}>{weekday.short}</th>
						{/each}
					</tr>
				</thead>
				<tbody>
					{#each weeks as week, row (row)}
						<tr>
							{#each week as date, column (column)}
								<td>
									{#if date}
										{@const key = toIsoDate(date)}
										{@const list = days.get(key)}
										{#if list}
											<button
												type="button"
												class={['day', dayTone(list), { selected: key === shownDay }]}
												tabindex={key === shownDay ? 0 : -1}
												aria-current={key === today
													? 'date'
													: key === shownDay
														? 'true'
														: undefined}
												aria-label={dayLabel(key, list)}
												onclick={() => (picked = key)}
												onkeydown={keydown}
												{@attach register(key)}
											>
												<span class={['number', { today: key === today }]}>{date.getDate()}</span>
												<span class="dots" aria-hidden="true">
													{#each list.slice(0, 3) as entry (entry.id)}<span class="dot"
														></span>{/each}
												</span>
											</button>
										{:else}
											<span
												class={['day', 'empty', { past: key < today }]}
												aria-current={key === today ? 'date' : undefined}
											>
												<span class={['number', { today: key === today }]}>{date.getDate()}</span>
											</span>
										{/if}
									{/if}
								</td>
							{/each}
						</tr>
					{/each}
				</tbody>
			</table>

			{#if counts.length > 0}
				<ul class="legend" role="list">
					{#each counts as item (item.tone)}
						<li><span class={['swatch', item.tone]} aria-hidden="true"></span>{item.label}</li>
					{/each}
				</ul>
			{/if}
		</div>

		<div class="agenda">
			{#if shownDay && shownEntries.length > 0}
				{@const title = capitalize(formatDate(toJsDate(shownDay), 'full'))}
				<h3 class="agenda-title">{title}</h3>
				<ListGroup label={title}>
					{#each shownEntries as entry (entry.id)}
						<ListRow
							title={entry.course}
							description={entry.line}
							href={entry.href}
							lines={3}
							trailing={entry.mark ? mark : undefined}
							chevron
						>
							{#snippet leading()}
								<span
									class={['time', { untimed: !entry.time }]}
									aria-hidden={!entry.time || undefined}
								>
									{entry.time ?? '—'}
								</span>
							{/snippet}
						</ListRow>
						{#snippet mark()}
							{#if entry.mark === 'closing'}
								<span class="flag">
									<StatusChip status="warning" label={m.exams_status_closing()} size="sm" />
								</span>
							{:else}
								<span class="booked-mark"><IconCheckCircle aria-hidden="true" /></span>
							{/if}
						{/snippet}
					{/each}
				</ListGroup>
			{:else}
				<p class="quiet">{m.exams_overview_quiet_month()}</p>
			{/if}
		</div>
	</div>
</section>

<style>
	.overview {
		display: grid;
		gap: var(--sp-5);
		align-content: start;
		container: overview / inline-size;
	}

	.head {
		display: grid;
		gap: 4px;
	}

	.title {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.hint,
	.quiet {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	.body {
		display: grid;
		gap: var(--sp-5);
		align-items: start;
	}

	@container overview (width >= 600px) {
		.body {
			grid-template-columns: minmax(300px, 340px) minmax(0, 1fr);
		}
	}

	.month {
		display: grid;
		gap: var(--sp-3);
		padding: var(--sp-3) var(--sp-4) var(--sp-4);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.month-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: var(--sp-2);
		margin-inline: -6px;
	}

	.month-name {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		outline: none;
	}

	.grid {
		inline-size: 100%;
		border-collapse: separate;
		border-spacing: 2px;
		table-layout: fixed;
	}

	th {
		padding-block-end: 4px;
		color: var(--text-muted);
		font: var(--type-label);
		text-align: center;
	}

	td {
		padding: 0;
	}

	.day {
		--t-bg: transparent;
		--t-fg: var(--text);
		position: relative;
		display: grid;
		place-content: center;
		justify-items: center;
		gap: 3px;
		inline-size: 100%;
		block-size: 44px;
		border: none;
		border-radius: 12px;
		background: var(--t-bg);
		color: var(--t-fg);
		font: var(--type-ui);
		font-variant-numeric: tabular-nums;
	}

	button.day {
		cursor: pointer;
		touch-action: manipulation;
		transition: box-shadow var(--dur-fast) var(--ease-out);
	}

	button.day::before {
		content: '';
		position: absolute;
		inset: 0;
		border-radius: inherit;
		background: var(--state-hover);
		opacity: 0;
		transition: opacity var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		button.day:hover::before {
			opacity: 1;
		}
	}

	button.day:active::before {
		background: var(--state-press);
		opacity: 1;
	}

	.empty {
		color: var(--text-muted);
	}

	.empty.past {
		color: var(--text-subtle);
	}

	.open,
	.closing {
		--t-bg: var(--container);
		--t-fg: var(--on-container);
		font-weight: 600;
	}

	.booked {
		--t-bg: var(--success-container);
		--t-fg: var(--on-success-container);
		font-weight: 600;
	}

	/* Not bookable yet: an outline, not a fill, so it differs in shape as well as colour. */
	.upcoming {
		border: 1.5px dashed var(--hairline-strong);
	}

	/* No longer bookable: hatched, as calendars mark what can't be had. */
	.closed {
		--t-fg: var(--text-muted);
		background-image: repeating-linear-gradient(
			135deg,
			var(--hairline-strong) 0 1px,
			transparent 1px 6px
		);
	}

	/* Registrations about to close: a warning dot on the corner, as on the legend. */
	.closing::after,
	.swatch.closing::after {
		content: '';
		position: absolute;
		inset-block-start: 4px;
		inset-inline-end: 4px;
		inline-size: 7px;
		block-size: 7px;
		border-radius: 50%;
		background: var(--warning);
		box-shadow: 0 0 0 1.5px var(--surface);
	}

	.selected {
		box-shadow:
			0 0 0 2px var(--surface),
			0 0 0 4px var(--accent);
	}

	.number {
		line-height: 1;
	}

	.number.today {
		color: var(--accent-text);
		font-weight: 700;
		text-decoration: underline 2px;
		text-underline-offset: 3px;
	}

	.dots {
		display: flex;
		gap: 3px;
		block-size: 4px;
	}

	.dot {
		inline-size: 4px;
		block-size: 4px;
		border-radius: 50%;
		background: currentColor;
		opacity: 0.7;
	}

	.legend {
		display: flex;
		flex-wrap: wrap;
		gap: 6px var(--sp-4);
		margin: 0;
		padding: var(--sp-3) 0 0;
		border-block-start: 1px solid var(--hairline);
		list-style: none;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.legend li {
		display: inline-flex;
		align-items: center;
		gap: 6px;
	}

	.swatch {
		position: relative;
		inline-size: 14px;
		block-size: 14px;
		border-radius: 4px;
	}

	.swatch.open,
	.swatch.closing {
		background: var(--container);
	}

	.swatch.booked {
		background: var(--success-container);
	}

	.swatch.closed {
		border: 1px solid var(--hairline);
	}

	.swatch.closing::after {
		inset-block-start: -3px;
		inset-inline-end: -3px;
	}

	.agenda {
		display: grid;
		gap: var(--sp-3);
		min-inline-size: 0;
	}

	.agenda-title {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		text-wrap: balance;
	}

	.time {
		display: block;
		min-inline-size: 3.25em;
		font: var(--type-ui-strong);
		font-variant-numeric: tabular-nums;
	}

	.untimed {
		color: var(--text-subtle);
	}

	.booked-mark {
		display: flex;
		color: var(--success);
	}

	.booked-mark :global(svg) {
		inline-size: 20px;
		block-size: 20px;
	}

	/* The line already says by when: the chip is a highlight a narrow row can spare. */
	@container (width < 300px) {
		.flag {
			display: none;
		}
	}

	@media (prefers-reduced-motion: reduce) {
		button.day,
		button.day::before {
			transition: none;
		}
	}

	@supports (corner-shape: squircle) {
		.month {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}
</style>
