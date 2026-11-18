<script lang="ts">
	interface Props {
		/** 0 to 1. */
		value: number;
		/** Accessible name ("Visto al 40%"). */
		label: string;
		/** The wave marks the one recording last watched; the others get a flat bar. */
		wavy?: boolean;
	}

	let { value, label, wavy = false }: Props = $props();

	const WIDTH = 240;
	const AMPLITUDE = 2.2;
	const WAVELENGTH = 16;

	/* One period drawn per wavelength, as cubic arcs; the track is the same path, dimmed. */
	const wave = (() => {
		let d = `M0 5`;
		for (let x = 0; x < WIDTH; x += WAVELENGTH) {
			const quarter = WAVELENGTH / 4;
			d += ` C${x + quarter} ${5 - AMPLITUDE},${x + quarter} ${5 - AMPLITUDE},${x + WAVELENGTH / 2} 5`;
			d += ` C${x + 3 * quarter} ${5 + AMPLITUDE},${x + 3 * quarter} ${5 + AMPLITUDE},${x + WAVELENGTH} 5`;
		}
		return d;
	})();

	const clamped = $derived(Math.min(1, Math.max(0, value)));
</script>

<span
	class={['progress', { wavy }]}
	role="progressbar"
	aria-label={label}
	aria-valuemin={0}
	aria-valuemax={100}
	aria-valuenow={Math.round(clamped * 100)}
	style:--value={clamped}
>
	{#if wavy}
		<svg viewBox="0 0 {WIDTH} 10" preserveAspectRatio="none" aria-hidden="true">
			<path class="track" d="M0 5 H{WIDTH}" pathLength="1" />
			<path class="fill" d={wave} pathLength="1" />
		</svg>
	{/if}
</span>

<style>
	.progress {
		position: relative;
		display: block;
		overflow: hidden;
		block-size: 4px;
		border-radius: var(--r-pill);
		background: var(--track);
	}

	.progress:not(.wavy)::after {
		content: '';
		position: absolute;
		inset: 0;
		border-radius: inherit;
		background: var(--course-bar, var(--accent));
		transform: scaleX(var(--value));
		transform-origin: left;
	}

	.wavy {
		overflow: visible;
		block-size: 10px;
		background: none;
	}

	svg {
		display: block;
		inline-size: 100%;
		block-size: 100%;
		overflow: visible;
	}

	path {
		fill: none;
		stroke-width: 3.5px;
		stroke-linecap: round;
		vector-effect: non-scaling-stroke;
	}

	.track {
		stroke: var(--track);
		stroke-dasharray: 1;
		stroke-dashoffset: calc(-1 * var(--value));
	}

	.fill {
		stroke: var(--course-bar, var(--accent));
		stroke-dasharray: var(--value) 1;
	}
</style>
