<script lang="ts">
	import { formatDate } from '#lib/format/dates.ts';

	interface Props {
		/** Epoch milliseconds; null for an assignment with no due date. */
		due: number | null;
		/**
		 * pending: still to hand in (the course's colour); soon and overdue: urgent; quiet: handed
		 * in, closed, or not known yet.
		 */
		tone: 'pending' | 'soon' | 'overdue' | 'quiet';
		now: number;
		size?: 'sm' | 'md';
	}

	let { due, tone, now, size = 'md' }: Props = $props();

	const parts = $derived.by(() => {
		if (due === null) return null;
		const date = new Date(due);
		return {
			day: date.getDate(),
			month: formatDate(date, 'month').replace('.', ''),
			year:
				date.getFullYear() === new Date(now).getFullYear()
					? null
					: `’${String(date.getFullYear()).slice(-2)}`
		};
	});
</script>

<span class={['tile', tone, size]} aria-hidden="true">
	{#if parts}
		<span class="day">{parts.day}</span>
		<span class="month"
			>{parts.month}{#if parts.year}&nbsp;{parts.year}{/if}</span
		>
	{:else}
		<span class="day infinity">∞</span>
	{/if}
</span>

<style>
	.tile {
		display: inline-grid;
		place-content: center;
		justify-items: center;
		flex-shrink: 0;
		inline-size: 52px;
		block-size: 52px;
		border-radius: 15px;
		background: var(--t-bg);
		color: var(--t-fg);
		box-shadow: var(--inner-light);
		line-height: 1;
	}

	.sm {
		inline-size: 44px;
		block-size: 44px;
		border-radius: 13px;
	}

	.sm .day {
		font-size: 19px;
	}

	.sm .month {
		margin-block-start: 2px;
		font-size: 9px;
	}

	.pending {
		--t-bg: var(--course-container);
		--t-fg: var(--course-ink);
	}

	.soon {
		--t-bg: var(--warning-container);
		--t-fg: var(--on-warning-container);
	}

	.overdue {
		--t-bg: var(--danger-container);
		--t-fg: var(--on-danger-container);
	}

	.quiet {
		--t-bg: var(--surface-sunken);
		--t-fg: var(--text-muted);
	}

	.day {
		font: 500 22px / 1 var(--font-display);
		letter-spacing: -0.02em;
		font-variant-numeric: lining-nums tabular-nums;
	}

	.infinity {
		font-size: 26px;
	}

	.month {
		margin-block-start: 3px;
		font: 600 10px / 1.2 var(--font-ui);
		letter-spacing: 0.06em;
		text-transform: uppercase;
		white-space: nowrap;
	}
</style>
