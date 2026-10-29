<script lang="ts">
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { formatDate, formatTimeRange, formatWeekday } from '#lib/format/dates.ts';
	import { toJsDate } from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import SegmentedControl from '#lib/ui/SegmentedControl.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { weekHours } from '../model.ts';
	import { weekTimetableQuery } from '../queries.ts';

	interface Props {
		siteId: string;
	}

	let { siteId }: Props = $props();

	let week = $state<'0' | '1'>('0');
	const timetable = query(
		() => session.current && weekTimetableQuery(session.current, siteId, Number(week))
	);
	const days = $derived(timetable.data ? weekHours(timetable.data) : undefined);
	const gate = new LoadingGate(() => days === undefined && !timetable.error);

	const capitalize = (text: string) => text.charAt(0).toLocaleUpperCase() + text.slice(1);
</script>

<section class="card" aria-labelledby="library-hours">
	<header class="head">
		<h2 class="title" id="library-hours">{m.library_opening_hours()}</h2>
		<SegmentedControl
			label={m.library_opening_hours()}
			bind:value={week}
			options={[
				{ value: '0', label: m.library_this_week() },
				{ value: '1', label: m.library_next_week() }
			]}
		/>
	</header>

	{#if timetable.error && !days}
		<ErrorState
			title={m.library_hours_failed()}
			message={errorMessage(timetable.error)}
			onretry={timetable.refresh}
			retrying={timetable.refreshing}
		/>
	{:else if !days}
		<ul class="days" aria-busy="true">
			{#each { length: 7 }, index (index)}
				<li class="day">
					{#if gate.visible}<Skeleton width="45%" /><Skeleton width="30%" />{/if}
				</li>
			{/each}
		</ul>
	{:else}
		<ul class="days">
			{#each days as day (day.day)}
				{@const date = toJsDate(day.day)}
				<li class={['day', { today: day.today, closed: day.ranges.length === 0 }]}>
					<span class="name">
						{capitalize(formatWeekday(date))}
						<span class="date">{formatDate(date, 'dayMonth')}</span>
					</span>
					<span class="hours">
						{#if day.ranges.length === 0}
							{m.library_closed()}
						{:else}
							{day.ranges
								.map((range) =>
									formatTimeRange(toJsDate(day.day, range.open), toJsDate(day.day, range.close))
								)
								.join(', ')}
						{/if}
					</span>
				</li>
			{/each}
		</ul>
	{/if}
</section>

<style>
	.card {
		display: grid;
		gap: var(--sp-3);
		padding: var(--sp-4) var(--sp-5) var(--sp-3);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.head {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: space-between;
		gap: var(--sp-3);
	}

	.title {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.days {
		display: grid;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.day {
		position: relative;
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		gap: var(--sp-3);
		min-block-size: 44px;
		padding: 10px 0;
		border-block-start: 1px solid var(--hairline);
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.day:first-child {
		border-block-start: 0;
	}

	.today {
		color: var(--text);
		font-weight: 600;
	}

	.today::before {
		content: '';
		position: absolute;
		inset-block: 12px;
		inset-inline-start: calc(-1 * var(--sp-5));
		inline-size: 3px;
		border-radius: 0 3px 3px 0;
		background: var(--accent);
	}

	.name {
		display: inline-flex;
		align-items: baseline;
		gap: 8px;
	}

	.date {
		color: var(--text-muted);
		font: var(--type-meta);
		font-weight: 400;
	}

	.hours {
		font-variant-numeric: tabular-nums;
		text-align: end;
	}

	.closed .hours {
		color: var(--text-muted);
	}
</style>
