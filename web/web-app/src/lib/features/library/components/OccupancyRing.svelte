<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import { occupancyText } from '../format.ts';
	import { occupancyLevel } from '../model.ts';

	interface Props {
		/** 0–100. */
		percent: number;
		size?: 'sm' | 'lg';
	}

	let { percent, size = 'sm' }: Props = $props();

	const RADIUS = 42;
	const LENGTH = 2 * Math.PI * RADIUS;

	const level = $derived(occupancyLevel(percent));
	const text = $derived(occupancyText(percent));
	const offset = $derived(LENGTH * (1 - Math.max(0, Math.min(100, percent)) / 100));
</script>

<span class={['ring', size, level]} role="img" aria-label={m.library_occupancy({ percent: text })}>
	<svg viewBox="0 0 100 100" aria-hidden="true">
		<circle class="track" cx="50" cy="50" r={RADIUS} />
		<circle
			class="arc"
			cx="50"
			cy="50"
			r={RADIUS}
			stroke-dasharray={LENGTH}
			style:--offset={offset}
			style:--length={LENGTH}
		/>
	</svg>
	<span class="value" aria-hidden="true">{text}</span>
</span>

<style>
	.ring {
		--size: 52px;
		--stroke: 9;
		position: relative;
		display: inline-grid;
		place-items: center;
		flex-shrink: 0;
		inline-size: var(--size);
		block-size: var(--size);
	}

	.lg {
		--size: 112px;
		--stroke: 7;
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

	svg {
		position: absolute;
		inset: 0;
		inline-size: 100%;
		block-size: 100%;
		transform: rotate(-90deg);
	}

	circle {
		fill: none;
		stroke-width: var(--stroke);
	}

	.track {
		stroke: color-mix(in oklch, var(--tone) 16%, var(--surface-sunken));
	}

	.arc {
		stroke: var(--tone);
		stroke-linecap: round;
		stroke-dashoffset: var(--offset);
		transition: stroke-dashoffset var(--dur-slow) var(--ease-out);
	}

	@starting-style {
		.arc {
			stroke-dashoffset: var(--length);
		}
	}

	.value {
		font: 600 0.8125rem / 1 var(--font-ui);
		font-variant-numeric: tabular-nums;
		letter-spacing: -0.01em;
	}

	.lg .value {
		font: 600 1.625rem / 1 var(--font-ui);
		font-variant-numeric: tabular-nums;
		letter-spacing: -0.02em;
	}

	@media (prefers-reduced-motion: reduce) {
		.arc {
			transition: none;
		}
	}
</style>
