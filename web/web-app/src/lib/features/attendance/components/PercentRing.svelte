<script lang="ts">
	import { formatPercent } from '#lib/format/numbers.ts';
	import { rampHue } from '../model.ts';

	interface Props {
		/** 0–100, rounded; null when no source recorded anything. */
		percent: number | null;
	}

	let { percent }: Props = $props();
</script>

<!--
	The arc's hue runs vermilion → amber → green with the share. Screen readers hear the
	percentage in the row's caption, so the ring itself is silent.
-->
<span
	class={['ring', { empty: percent === null }]}
	style:--hue={percent === null ? undefined : rampHue(percent)}
	aria-hidden="true"
>
	<svg viewBox="0 0 44 44">
		<circle class="track" cx="22" cy="22" r="19.5" />
		{#if percent !== null && percent > 0}
			<circle
				class="arc"
				cx="22"
				cy="22"
				r="19.5"
				pathLength="100"
				stroke-dasharray="{Math.min(100, percent)} 100"
			/>
		{/if}
	</svg>
	<span class="value">{percent === null ? '–' : formatPercent(percent / 100)}</span>
</span>

<style>
	.ring {
		--arc: light-dark(oklch(54% 0.15 var(--hue)), oklch(76% 0.13 var(--hue)));
		position: relative;
		display: grid;
		place-items: center;
		inline-size: 44px;
		block-size: 44px;
		flex: none;
	}

	svg {
		position: absolute;
		inset: 0;
		inline-size: 100%;
		block-size: 100%;
		transform: rotate(-90deg);
		fill: none;
		stroke-width: 4;
	}

	.track {
		stroke: color-mix(in oklch, var(--arc) 18%, transparent);
	}

	.empty .track {
		stroke: var(--track);
		stroke-dasharray: 3 3.6;
	}

	.arc {
		stroke: var(--arc);
		stroke-linecap: round;
	}

	.value {
		position: relative;
		font: var(--type-overline);
		font-variant-numeric: tabular-nums;
		color: var(--text);
	}

	.empty .value {
		color: var(--text-subtle);
	}
</style>
