<script lang="ts">
	import IconTimer from '~icons/ph/timer';
	import { formatClock, formatClockLong } from '#lib/features/course/format.ts';
	import { m } from '#lib/paraglide/messages.js';
	import type { TimerPhase } from '../model.ts';

	interface Props {
		/** Milliseconds left. */
		left: number;
		phase: TimerPhase;
	}

	let { left, phase }: Props = $props();

	const seconds = $derived(Math.ceil(left / 1000));
</script>

<span
	class={['timer', phase]}
	role="timer"
	aria-label={m.quiz_time_left({ time: formatClockLong(seconds) })}
>
	<IconTimer aria-hidden="true" />
	<span class="clock" aria-hidden="true">{formatClock(seconds)}</span>
</span>

<style>
	.timer {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		min-block-size: 32px;
		padding: 4px 12px 4px 10px;
		border-radius: var(--r-pill);
		background: var(--surface-sunken);
		color: var(--text);
		font: var(--type-ui-strong);
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
		transition:
			background-color var(--dur-base) var(--ease-out),
			color var(--dur-base) var(--ease-out);
	}

	.timer :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	.warning {
		background: var(--warning-container);
		color: var(--on-warning-container);
	}

	.danger,
	.expired {
		background: var(--danger-solid);
		color: oklch(100% 0 0);
	}

	.danger :global(svg) {
		animation: tick 1s steps(2, jump-none) infinite;
	}

	@keyframes tick {
		to {
			opacity: 0.35;
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.danger :global(svg) {
			animation: none;
		}
	}
</style>
