<script lang="ts" module>
	/* Count-ups play once per key per session: revisiting a page shows the number at rest. */
	const played: string[] = [];
</script>

<script lang="ts">
	import type { Snippet } from 'svelte';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { reducedMotion } from './media.ts';

	interface Props {
		label: string;
		/** A number is formatted for the locale with `decimals`; a string is shown as is ("30 e lode"). */
		value: number | string;
		decimals?: number;
		/** Quiet suffix after the value ("/ 30", "CFU"). */
		unit?: string;
		/** A line under the value: a comparison, a source. */
		caption?: string;
		/** Under everything: a Meter, a sparkline. */
		footer?: Snippet;
		size?: 'md' | 'lg' | 'xl';
		/** gold for honours. */
		tone?: 'default' | 'gold';
		/** The figure in the UI face, for a view whose serif moment is elsewhere (tiles, rows of stats). */
		sans?: boolean;
		/**
		 * stack: label, figure, caption, footer from the top. tile: label at the top, the figure
		 * at the bottom over one footer line (the caption or the footer), so figures of tiles in
		 * a row share a baseline; the tile fills the height it is given.
		 */
		layout?: 'stack' | 'tile';
		/** Counts up from zero the first time this key is shown in the session. */
		countUp?: string;
	}

	let {
		label,
		value,
		decimals = 0,
		unit,
		caption,
		footer,
		size = 'lg',
		tone = 'default',
		sans = false,
		layout = 'stack',
		countUp
	}: Props = $props();

	const format = (n: number) =>
		formatNumber(n, { minimumFractionDigits: decimals, maximumFractionDigits: decimals });
	const final = $derived(typeof value === 'number' ? format(value) : value);

	let shown = $state<number>();

	$effect(() => {
		if (typeof value !== 'number' || !countUp || played.includes(countUp)) return;
		if (reducedMotion.current) return;
		played.push(countUp);
		const target = value;
		const duration = 700;
		const start = performance.now();
		let frame = requestAnimationFrame(function step(now) {
			/* A frame's timestamp can precede the start taken just before it. */
			const t = Math.min(1, Math.max(0, (now - start) / duration));
			shown = t < 1 ? target * (1 - Math.pow(1 - t, 4)) : undefined;
			if (t < 1) frame = requestAnimationFrame(step);
		});
		return () => {
			cancelAnimationFrame(frame);
			shown = undefined;
		};
	});
</script>

<div class={['stat', size, tone, layout, { sans }]}>
	<p class="label">{label}</p>
	<p class="figure">
		<!-- The final text keeps its width while the count runs over it: nothing beside it moves. -->
		<span class="value">
			<span class={['final', { counting: shown !== undefined }]}>{final}</span>
			{#if shown !== undefined}<span class="count" aria-hidden="true">{format(shown)}</span>{/if}
		</span>
		{#if unit}<span class="unit">{unit}</span>{/if}
	</p>
	{#if layout === 'tile'}
		<div class="line">
			{#if footer}{@render footer()}{:else if caption}<p class="caption">{caption}</p>{/if}
		</div>
	{:else}
		{#if caption}<p class="caption">{caption}</p>{/if}
		{#if footer}<div class="footer">{@render footer()}</div>{/if}
	{/if}
</div>

<style>
	.stat {
		display: grid;
		gap: 6px;
		min-inline-size: 0;
	}

	.tile {
		flex: 1;
		grid-template-rows: auto minmax(8px, 1fr) auto auto;
		grid-template-areas: 'label' '.' 'figure' 'line';
		gap: 0;
		min-block-size: 100%;
	}

	.label {
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.tile .label {
		grid-area: label;
	}

	.figure {
		display: flex;
		align-items: baseline;
		flex-wrap: wrap;
		gap: 0 8px;
	}

	.tile .figure {
		grid-area: figure;
		flex-wrap: nowrap;
		min-inline-size: 0;
	}

	.value {
		display: inline-grid;
		font: var(--type-numeral-l);
		letter-spacing: var(--track-numeral);
		font-variant-numeric: lining-nums tabular-nums;
		font-feature-settings: 'lnum', 'tnum';
		white-space: nowrap;
	}

	.value > * {
		grid-area: 1 / 1;
	}

	/* Transparent, not hidden: screen readers read the final figure, never the count. */
	.final.counting {
		opacity: 0;
	}

	.md .value {
		font-size: 1.75rem;
		letter-spacing: -0.02em;
	}

	.xl .value {
		font: var(--type-numeral-xl);
		font-size: clamp(3rem, 2.2rem + 3vw, 4rem);
		letter-spacing: var(--track-numeral);
	}

	.sans .value {
		font-family: var(--font-ui);
		font-weight: 600;
		letter-spacing: -0.02em;
	}

	.gold .value {
		color: var(--gold);
	}

	.unit {
		color: var(--text-muted);
		font: var(--type-ui);
		font-size: 0.9375rem;
		white-space: nowrap;
	}

	.caption {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.footer {
		margin-block-start: 6px;
	}

	/* One line tall in every tile, a caption or a bar, never more. */
	.line {
		grid-area: line;
		display: grid;
		align-content: center;
		block-size: 20px;
		margin-block-start: 6px;
	}

	.line .caption {
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
</style>
