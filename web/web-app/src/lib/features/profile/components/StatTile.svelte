<script lang="ts">
	import type { ResolvedPathname } from '$app/types';
	import { formatPercent } from '#lib/format/numbers.ts';
	import Card from '#lib/ui/Card.svelte';
	import Meter from '#lib/ui/Meter.svelte';
	import Stat from '#lib/ui/Stat.svelte';
	import type { Icon } from '#lib/ui/types.ts';

	interface Props {
		href: ResolvedPathname;
		label: string;
		/** Null when Esse3 has none yet: shown as a dash. */
		value: number | null;
		decimals?: number;
		/** Quiet suffix after the value ("/ 21"). */
		unit?: string;
		/** The tile's one footer line: a short caption, or a progress bar with its faint percentage. */
		footer: { caption: string } | { ratio: number; text: string };
		/** What following the tile does, for screen readers ("Calcola media ipotetica"). */
		action: string;
		icon?: Icon;
		countUp?: string;
	}

	let {
		href,
		label,
		value,
		decimals = 0,
		unit,
		footer: line,
		action,
		icon: Glyph,
		countUp
	}: Props = $props();

	const progress = $derived('ratio' in line ? line : undefined);
</script>

<Card {href} padding="none">
	<div class={['tile', { glyph: Glyph && value !== null }]}>
		<Stat
			layout="tile"
			size="md"
			sans
			{label}
			value={value ?? '—'}
			{decimals}
			unit={value === null ? undefined : unit}
			countUp={value === null ? undefined : countUp}
		>
			{#snippet footer()}
				{#if 'caption' in line}
					<span class="caption">{line.caption}</span>
				{:else}
					<Meter value={line.ratio} max={1} {label} valueText={line.text} hideLabel />
				{/if}
			{/snippet}
		</Stat>
		<span class="sr-only">{action}</span>
		{#if progress}
			<span class="clip" aria-hidden="true">
				<span class="watermark" data-text={formatPercent(progress.ratio)}></span>
			</span>
		{/if}
		{#if Glyph && value !== null}
			<span class="icon" aria-hidden="true"><Glyph /></span>
		{/if}
	</div>
</Card>

<style>
	.tile {
		display: flex;
		flex: 1;
		flex-direction: column;
		min-block-size: var(--tile-h, 118px);
		padding: var(--tile-pad, 16px);
		container: tile / inline-size;
	}

	.caption {
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	/* The calculator sits beside the figure and its caption, which keep clear of it. */
	.glyph .caption {
		padding-inline-end: 42px;
	}

	.icon {
		position: absolute;
		inset-block-end: var(--tile-pad, 16px);
		inset-inline-end: var(--tile-pad, 16px);
		display: grid;
		place-items: center;
		inline-size: 34px;
		block-size: 34px;
		border-radius: var(--r-sm);
		background: var(--accent);
		color: var(--on-accent);
		transition: transform var(--dur-spring) var(--spring-pop);
	}

	.icon :global(svg) {
		inline-size: 22px;
		block-size: 22px;
	}

	/* The card doesn't clip: this does, for the watermark that bleeds off its edge. */
	.clip {
		position: absolute;
		inset: 0;
		z-index: -1;
		overflow: hidden;
		border-radius: var(--r-lg);
		pointer-events: none;
	}

	@supports (corner-shape: squircle) {
		.clip {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}

	/* A narrow tile has no room for it beside the figure: tiles can then sit side by side. */
	@container tile (width < 260px) {
		.clip {
			display: none;
		}
	}

	/*
	 * Android's huge faded percentage bleeding off the trailing edge, in the figures' UI face.
	 * Generated content: it is decoration, not text to read or to contrast-check.
	 */
	.watermark::before {
		content: attr(data-text);
	}

	.watermark {
		position: absolute;
		inset-block-start: 22px;
		inset-inline-end: -0.12em;
		color: var(--text);
		font: 600 5.5rem / 1 var(--font-ui);
		letter-spacing: -0.04em;
		opacity: 0.055;
		transform: rotate(-12deg);
	}

	@media (hover: hover) {
		.tile:hover .icon {
			transform: scale(1.08) rotate(-6deg);
		}
	}

	/* Narrow tiles give the caption the whole line: the calculator rises beside the figure. */
	@container tile (width < 180px) {
		.glyph .caption {
			padding-inline-end: 0;
		}

		.icon {
			inset-block-end: calc(var(--tile-pad, 16px) + 26px);
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.icon {
			transition: none;
		}
	}
</style>
