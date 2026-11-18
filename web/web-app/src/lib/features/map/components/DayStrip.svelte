<script lang="ts">
	import { romeClock, romeHour, romeTime } from '#lib/format/rome.ts';
	import { m } from '#lib/paraglide/messages.js';
	import type { ScheduleEntry } from '../status.ts';

	interface Props {
		entries: readonly ScheduleEntry[];
		now: number;
		/** The bookings aren't known (yet): a neutral track holds the strip's place. */
		pending?: boolean;
	}

	let { entries, now, pending = false }: Props = $props();

	/** The span shown, in Rome hours: the teaching day, 8 to 20, stretched to fit the bookings. */
	const range = $derived.by(() => {
		const bookings = entries.filter((entry) => !entry.closure);
		const from = Math.min(romeHour(now, 8), ...bookings.map((entry) => entry.start));
		const to = Math.max(romeHour(now, 20), ...bookings.map((entry) => entry.end));
		return { from: Math.max(romeHour(now, 0), from), to: Math.min(romeHour(now, 24), to) };
	});

	const span = $derived(range.to - range.from);
	const percent = (time: number) =>
		(Math.min(Math.max(time, range.from), range.to) - range.from) / span;

	const blocks = $derived(
		entries
			.filter((entry) => entry.end > range.from && entry.start < range.to)
			.map((entry) => ({
				entry,
				start: percent(entry.start),
				size: percent(entry.end) - percent(entry.start)
			}))
	);

	/** Every other hour, on the hour of Rome's clock. */
	const ticks = $derived.by(() => {
		const list: Array<{ at: number; hour: number }> = [];
		for (let hour = Math.ceil(romeClock(range.from).hour / 2) * 2; hour <= 24; hour += 2) {
			const time = romeHour(now, hour);
			if (time < range.from || time > range.to) continue;
			list.push({ at: percent(time), hour });
		}
		return list;
	});

	const nowAt = $derived(now >= range.from && now <= range.to ? percent(now) : undefined);
</script>

<div
	class="strip"
	role={pending ? undefined : 'img'}
	aria-hidden={pending || undefined}
	aria-label={pending
		? undefined
		: m.map_day_strip_label({ from: romeTime(range.from), to: romeTime(range.to) })}
>
	<div class={['track', { pending }]}>
		{#if nowAt !== undefined}
			<span class="past" style:inline-size={`${nowAt * 100}%`}></span>
		{/if}
		{#each blocks as block (block.entry.id)}
			<span
				class={['block', { closure: block.entry.closure }]}
				style:inset-inline-start={`${block.start * 100}%`}
				style:inline-size={`${block.size * 100}%`}
			></span>
		{/each}
		{#if nowAt !== undefined}
			<span class="now" style:inset-inline-start={`${nowAt * 100}%`}></span>
		{/if}
	</div>
	<div class="ticks" aria-hidden="true">
		{#each ticks as tick (tick.at)}
			<span
				class={{ start: tick.at === 0, end: tick.at === 1 }}
				style:inset-inline-start={`${tick.at * 100}%`}>{tick.hour}</span
			>
		{/each}
	</div>
</div>

<style>
	.strip {
		display: grid;
		gap: 6px;
		padding-inline: 2px;
	}

	.track {
		position: relative;
		block-size: 12px;
		border-radius: var(--r-pill);
		background: var(--success-container);
		box-shadow: inset 0 0 0 1px color-mix(in oklch, var(--success) 18%, transparent);
	}

	.pending {
		background: var(--track);
		box-shadow: none;
	}

	.past {
		position: absolute;
		inset-block: 0;
		inset-inline-start: 0;
		border-radius: var(--r-pill) 0 0 var(--r-pill);
		background: color-mix(in oklch, var(--surface) 55%, transparent);
		z-index: 1;
	}

	.block {
		position: absolute;
		inset-block: 0;
		min-inline-size: 3px;
		border-radius: 3px;
		background: var(--danger-solid);
	}

	.closure {
		background: repeating-linear-gradient(-45deg, var(--text-subtle) 0 3px, transparent 3px 6px);
	}

	.now {
		position: absolute;
		inset-block: -5px;
		inline-size: 2px;
		margin-inline-start: -1px;
		border-radius: 1px;
		background: var(--accent);
		z-index: 2;
	}

	.now::before {
		content: '';
		position: absolute;
		inset-block-start: -4px;
		inset-inline-start: -3px;
		inline-size: 8px;
		block-size: 8px;
		border-radius: 50%;
		background: var(--accent);
	}

	.ticks {
		position: relative;
		block-size: 1.2em;
		color: var(--text-muted);
		font: var(--type-label);
		font-variant-numeric: tabular-nums;
	}

	.ticks span {
		position: absolute;
		transform: translateX(-50%);
	}

	.ticks .start {
		transform: none;
	}

	.ticks .end {
		transform: translateX(-100%);
	}
</style>
