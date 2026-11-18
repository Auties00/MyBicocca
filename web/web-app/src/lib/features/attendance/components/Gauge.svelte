<script lang="ts">
	import { formatNumber } from '#lib/format/numbers.ts';

	interface Props {
		/** 0–100. */
		percent: number;
		/** What the share is of ("frequenza", "sulle registrate"), under the number; large only. */
		caption?: string;
		/** success or warning against the course's minimum; neutral for a share with none to meet. */
		tone: 'success' | 'warning' | 'neutral';
		/** The course's minimum: the stretch of the ring from it to the end is the one to reach. */
		minimum?: number;
		/** The accessible reading, in full. */
		label: string;
		/** large: the view's headline, its numeral in the serif; small: one source among others. */
		size?: 'large' | 'small';
	}

	let { percent, caption, tone, minimum, label, size = 'large' }: Props = $props();

	const RADIUS = 84;
	const shown = $derived(Math.min(100, Math.max(0, percent)));

	/*
	 * The minimum cuts the track, and a thin arc outside it runs from there to the end: the zone to
	 * reach, read as part of the ring rather than as a stroke beside the number.
	 */
	const target = $derived.by(() => {
		if (minimum === undefined || size === 'small') return undefined;
		const share = Math.min(100, Math.max(0, minimum)) / 100;
		const angle = share * 2 * Math.PI - Math.PI / 2;
		const at = (radius: number) =>
			`${(100 + radius * Math.cos(angle)).toFixed(2)} ${(100 + radius * Math.sin(angle)).toFixed(2)}`;
		const zone = Math.max(0, 1 - share) * 100;
		return { cut: { from: at(RADIUS - 8), to: at(RADIUS + 8) }, start: share * 100, zone };
	});
</script>

<div class={['gauge', tone, size]} role="img" aria-label={label}>
	<svg viewBox="0 0 200 200" aria-hidden="true">
		<g transform="rotate(-90 100 100)">
			<circle class="track" cx="100" cy="100" r={RADIUS} />
			{#if shown > 0}
				<circle
					class="arc"
					cx="100"
					cy="100"
					r={RADIUS}
					pathLength="100"
					stroke-dasharray="{shown} 100"
				/>
			{/if}
			{#if target && target.zone > 0}
				<circle
					class="zone"
					cx="100"
					cy="100"
					r="97"
					pathLength="100"
					stroke-dasharray="{target.zone} 100"
					stroke-dashoffset={-target.start}
				/>
			{/if}
		</g>
		{#if target}
			<path class="cut" d="M{target.cut.from}L{target.cut.to}" />
		{/if}
	</svg>
	<div class="reading" aria-hidden="true">
		<span class="value">{formatNumber(Math.round(percent))}<span class="unit">%</span></span>
		{#if caption && size === 'large'}<span class="caption">{caption}</span>{/if}
	</div>
</div>

<style>
	.gauge {
		--arc: var(--accent);
		--ground: var(--surface);
		position: relative;
		display: grid;
		place-items: center;
		aspect-ratio: 1;
		flex: none;
		animation: settle var(--dur-spring) var(--spring-pop) both;
	}

	.large {
		inline-size: var(--gauge-size, min(100%, 212px));
	}

	.small {
		inline-size: 88px;
	}

	.success {
		--arc: var(--success);
	}

	.neutral {
		--arc: var(--text-muted);
	}

	/* An arc is a graphic, held to 3:1 on the surface rather than to the text's contrast. */
	.warning {
		--arc: var(--warning-graphic);
	}

	svg {
		position: absolute;
		inset: 0;
		inline-size: 100%;
		block-size: 100%;
		fill: none;
	}

	.track {
		stroke: color-mix(in oklch, var(--arc) 14%, var(--track));
		stroke-width: 12;
	}

	.small .track,
	.small .arc {
		stroke-width: 16;
	}

	.arc {
		stroke: var(--arc);
		stroke-width: 12;
		stroke-linecap: round;
	}

	.zone {
		stroke: var(--text-muted);
		stroke-width: 2.5;
		stroke-linecap: round;
	}

	.cut {
		stroke: var(--ground);
		stroke-width: 3.5;
	}

	.reading {
		position: relative;
		display: grid;
		justify-items: center;
		gap: 6px;
	}

	.value {
		font: var(--type-numeral-xl);
		font-size: clamp(2.75rem, 2.2rem + 2cqi, 3.75rem);
		font-variant-numeric: lining-nums tabular-nums;
		letter-spacing: var(--track-numeral);
		color: var(--text);
	}

	.unit {
		margin-inline-start: 2px;
		font-size: 0.42em;
		color: var(--text-muted);
		vertical-align: 0.95em;
	}

	/* One source among others: the number in the interface's face, the serif kept for the headline. */
	.small .value {
		font: var(--type-title-m);
		font-variant-numeric: tabular-nums;
		letter-spacing: var(--track-title-m);
	}

	.small .unit {
		margin-inline-start: 1px;
		font-size: 0.75em;
		vertical-align: baseline;
	}

	.caption {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	@keyframes settle {
		from {
			opacity: 0;
			transform: scale(0.94);
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.gauge {
			animation: none;
		}
	}
</style>
