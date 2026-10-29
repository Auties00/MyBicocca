<script lang="ts">
	import { formatTime } from '#lib/format/dates.ts';
	import { toJsDate, type IsoDate } from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { crowdLabel, occupancyText } from '../format.ts';
	import { occupancyLevel, slotAt, type ForecastSlot } from '../model.ts';

	interface Props {
		slots: readonly ForecastSlot[];
		/** The library's day, for labelling times. */
		day: IsoDate;
		/** Minutes of the day on the library's clock. */
		now: number;
	}

	let { slots, day, now }: Props = $props();

	const from = $derived(slots[0]?.start ?? 0);
	const to = $derived(slots.at(-1)?.end ?? 0);
	const span = $derived(Math.max(1, to - from));
	const current = $derived(slotAt(slots, now));

	/* The slot the caption describes: the one pointed or stepped to, else the current one. */
	let picked = $state<number>();
	const shown = $derived(
		picked !== undefined && picked < slots.length ? picked : Math.max(0, current)
	);
	const slot = $derived(slots[shown]);

	const time = (minutes: number) => formatTime(toJsDate(day, minutes));
	const describe = (item: ForecastSlot) =>
		m.library_forecast_slot({
			start: time(item.start),
			end: time(item.end),
			percent: occupancyText(item.occupancy)
		});

	/* Even hours along the axis, as Android labels every other bar. */
	const ticks = $derived.by(() => {
		const list: number[] = [];
		for (let hour = Math.ceil(from / 120) * 2; hour * 60 <= to; hour += 2) list.push(hour * 60);
		return list;
	});

	const at = (minutes: number) => `${((minutes - from) / span) * 100}%`;

	function keydown(event: KeyboardEvent) {
		const last = slots.length - 1;
		const next =
			event.key === 'ArrowRight'
				? Math.min(last, shown + 1)
				: event.key === 'ArrowLeft'
					? Math.max(0, shown - 1)
					: event.key === 'Home'
						? 0
						: event.key === 'End'
							? last
							: undefined;
		if (next === undefined) return;
		event.preventDefault();
		picked = next;
	}
</script>

{#if slot}
	<div class="chart">
		<p class="caption" aria-hidden="true">
			<span class="when">
				{#if shown === current}<span class="now-tag">{m.library_forecast_now()}</span>{/if}
				{time(slot.start)}–{time(slot.end)}
			</span>
			<span class="reading">
				<strong class={['percent', occupancyLevel(slot.occupancy)]}>
					{occupancyText(slot.occupancy)}
				</strong>
				{crowdLabel(slot.occupancy)}
			</span>
		</p>

		<div
			class="plot"
			role="slider"
			tabindex="0"
			aria-label={m.library_today_occupancy()}
			aria-valuemin={0}
			aria-valuemax={slots.length - 1}
			aria-valuenow={shown}
			aria-valuetext={describe(slot)}
			onkeydown={keydown}
			onpointerleave={() => (picked = undefined)}
		>
			<div class="grid" aria-hidden="true">
				{#each [100, 50] as level (level)}
					<span style:--y="{level}%"><small>{occupancyText(level)}</small></span>
				{/each}
			</div>
			<ol class="bars" aria-hidden="true">
				{#each slots as item, index (item.start)}
					<li
						class={[
							'bar',
							occupancyLevel(item.occupancy),
							{ past: item.end <= now, picked: index === shown }
						]}
						style:--w={item.end - item.start}
						style:--h={Math.max(0.03, item.occupancy / 100)}
						onpointerenter={() => (picked = index)}
						onpointerdown={() => (picked = index)}
					>
						<span class="fill"></span>
					</li>
				{/each}
			</ol>
			{#if current >= 0}
				<span class="marker" style:--x={at(now)} aria-hidden="true"></span>
			{/if}
		</div>

		<div class="axis" aria-hidden="true">
			{#each ticks as tick (tick)}
				<span style:--x={at(tick)}>{time(tick).slice(0, 2)}</span>
			{/each}
		</div>
	</div>
{:else}
	<p class="empty">{m.library_forecast_empty()}</p>
{/if}

<style>
	.chart {
		display: grid;
		gap: var(--sp-3);
	}

	/* The left gutter holds the capacity labels of the grid lines. */
	.plot,
	.axis {
		margin-inline-start: 40px;
	}

	.caption {
		display: flex;
		flex-wrap: wrap;
		align-items: baseline;
		justify-content: space-between;
		gap: 4px 12px;
		min-block-size: 1.5rem;
		font: var(--type-ui);
	}

	.when {
		display: inline-flex;
		align-items: center;
		gap: 8px;
		color: var(--text-muted);
		font-variant-numeric: tabular-nums;
	}

	.now-tag {
		padding: 2px 8px;
		border-radius: var(--r-pill);
		background: var(--container);
		color: var(--on-container);
		font: var(--type-label);
	}

	.reading {
		color: var(--text-muted);
	}

	.percent {
		margin-inline-end: 4px;
		color: var(--text);
		font: var(--type-ui-strong);
		font-variant-numeric: tabular-nums;
	}

	.plot {
		position: relative;
		block-size: 120px;
		border-radius: var(--r-sm);
		outline-offset: 4px;
		touch-action: pan-y;
	}

	.grid span {
		position: absolute;
		inset-inline: 0;
		inset-block-end: var(--y);
		border-block-start: 1px dashed var(--hairline-strong);
	}

	.grid small {
		position: absolute;
		inset-inline-end: calc(100% + 8px);
		translate: 0 -50%;
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
	}

	.bars {
		position: absolute;
		inset: 0;
		display: flex;
		align-items: flex-end;
		gap: 3px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.bar {
		flex: var(--w) 1 0;
		display: flex;
		align-items: flex-end;
		block-size: 100%;
		min-inline-size: 4px;
		cursor: default;
	}

	/* The status fills, alike in both themes, as Android keeps "how busy" reading the same. */
	.low {
		--tone: var(--success-solid);
	}

	.mid {
		--tone: var(--warning-solid);
	}

	.high {
		--tone: var(--danger-solid);
	}

	.fill {
		inline-size: 100%;
		block-size: 100%;
		border-radius: 6px 6px 2px 2px;
		background: var(--tone);
		transform: scaleY(var(--h));
		transform-origin: bottom;
		transition:
			transform var(--dur-slow) var(--ease-out),
			background-color var(--dur-fast) var(--ease-out);
	}

	@starting-style {
		.fill {
			transform: scaleY(0);
		}
	}

	/* Slots gone by step back, softened toward the card so their hue still reads. */
	.past:not(.picked) .fill {
		background: color-mix(in oklch, var(--tone) 40%, var(--surface));
	}

	/* The pointed slot stands in a faint column, so it reads even when it is the current one. */
	.bar.picked {
		border-radius: 8px 8px 3px 3px;
		background: color-mix(in oklch, var(--text) 7%, transparent);
	}

	.marker {
		position: absolute;
		inset-block: -6px 0;
		inset-inline-start: var(--x);
		inline-size: 2px;
		margin-inline-start: -1px;
		border-radius: 1px;
		background: var(--accent);
		pointer-events: none;
	}

	.marker::before {
		content: '';
		position: absolute;
		inset-block-start: -4px;
		inset-inline-start: -4px;
		inline-size: 10px;
		block-size: 10px;
		border-radius: 50%;
		background: var(--accent);
	}

	.axis {
		position: relative;
		block-size: 1rem;
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.axis span {
		position: absolute;
		inset-inline-start: var(--x);
		translate: -50% 0;
	}

	.empty {
		color: var(--text-muted);
		font: var(--type-ui);
	}

	@media (prefers-reduced-motion: reduce) {
		.fill {
			transition: none;
		}
	}
</style>
