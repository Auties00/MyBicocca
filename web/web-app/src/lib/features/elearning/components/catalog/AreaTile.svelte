<script lang="ts">
	import IconArrowRight from '~icons/ph/arrow-right-bold';
	import type { ResolvedPathname } from '$app/types';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { areaTint, areaVisual } from '../../catalog/areas.ts';
	import type { CatalogArea } from '../../catalog/model.ts';

	interface Props {
		area: CatalogArea;
		href: ResolvedPathname;
	}

	let { area, href }: Props = $props();

	const visual = $derived(areaVisual(area.name));
	const tint = $derived(areaTint(area.name));
</script>

<a
	class={['tile', visual.kind, visual.kind === 'brand' && visual.ink]}
	{href}
	style:--brand={visual.kind === 'brand' ? visual.color : undefined}
	style:--area-h={tint.hue}
	style:--area-c={tint.chroma}
>
	<span class={['disc', visual.kind === 'artwork' && visual.fit]} aria-hidden="true">
		{#if visual.kind === 'artwork'}
			<img src={visual.src} width="160" height="160" alt="" loading="lazy" decoding="async" />
		{:else}
			<svg class="logo" viewBox="0 0 41.4 41.4">
				<path
					d="M36.18,4.93l-0.03,0.21 -0.54,4.28 -17.63,9.03 -1.11,0.57 1.03,0.71 17.74,12.35 -0.08,4.33v0.11h-1.13l-0.96,-0.7 -17.9,-14.08 -0.7,-0.55 -0.42,0.79 -7.35,13.68h-2.25v-5.43l0.67,-0.94 6.98,-9.73 0.38,-0.52 -0.46,-0.45 -6.98,-6.81 -0.61,-0.59v-5.42h0.1l9.63,10.65 0.43,0.47 0.54,-0.35 16.9,-10.91 0.99,-0.69h2.77M36.3,4.18h-3.13l-1.19,0.82L15.1,15.89 5.25,5h-1.18s0,6.49 0,6.49l0.83,0.81 6.98,6.81 -6.98,9.73 -0.81,1.13v6.43h0.81s2.64,0 2.64,0l7.56,-14.08 17.9,14.08 1.18,0.86h2.12v-0.86l0.09,-4.71 -18.07,-12.58 17.99,-9.21 0.59,-4.67 0.86,-0.74 -1.46,-0.32h0Z"
				/>
				<path
					d="M0,0v41.4h41.4V0H0ZM36.1,5l-8.27,12.36L10.5,5h25.6ZM35.4,36.4H12.8l15.03,-10.63 7.57,10.63ZM4.9,7l18.73,14.56 -18.73,13.94V7ZM36.3,27.3l-4.26,-5.74 4.26,-7.66v13.4Z"
				/>
			</svg>
		{/if}
	</span>
	<span class="arrow" aria-hidden="true"><IconArrowRight /></span>
	<span class="label">
		<span class="name">{area.name}</span>
		<span class="count"
			>{m.elearning_category_courses({
				count: area.courses,
				shown: formatNumber(area.courses)
			})}</span
		>
	</span>
</a>

<style>
	/*
	 * Every tile is a plate with a disc tucked into its top corner: the university's logo on a
	 * teaching area's brand colour, a school's own artwork on a deep plate of its hue. The label
	 * always sits on the solid plate, never on a picture. A tile keeps its shape from its width
	 * (its cell is the size container) but grows for a long name, which is never cut: a grown tile
	 * stretches its row, as an aspect ratio wouldn't.
	 */
	.tile {
		--ratio: 1.3;
		--ink: oklch(100% 0 0);
		--fill: light-dark(
			oklch(40% var(--area-c) var(--area-h)),
			oklch(33% calc(var(--area-c) * 0.85) var(--area-h))
		);
		--disc: light-dark(oklch(100% 0 0), oklch(94% 0.008 var(--area-h)));
		position: relative;
		isolation: isolate;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		gap: 10px;
		min-block-size: calc(100cqi / var(--ratio));
		min-inline-size: 0;
		padding: 12px 14px 14px;
		border-radius: var(--r-lg);
		background: var(--fill);
		color: var(--ink);
		text-decoration: none;
		overflow: clip;
		box-shadow: var(--inner-light);
		transition:
			transform var(--dur-spring) var(--spring-pop),
			box-shadow var(--dur-fast) var(--ease-out);
		touch-action: manipulation;
		-webkit-tap-highlight-color: transparent;
	}

	/* A light sheen from the top, as on paper under a lamp. */
	.tile::before {
		content: '';
		position: absolute;
		inset: 0;
		z-index: -1;
		background: linear-gradient(to bottom, oklch(100% 0 0 / 0.1), transparent 60%);
	}

	.brand {
		--fill: var(--brand);
	}

	.brand.dark {
		--ink: oklch(22% 0.02 var(--area-h));
	}

	.disc {
		position: absolute;
		inset-block-start: -20%;
		inset-inline-end: -9%;
		z-index: -1;
		display: grid;
		place-items: center;
		inline-size: 44%;
		aspect-ratio: 1;
		border-radius: 50%;
		background: var(--disc);
		color: var(--brand);
		overflow: clip;
	}

	.logo {
		inline-size: 64%;
		block-size: 64%;
		rotate: -45deg;
		translate: -8% 10%;
		fill: currentColor;
	}

	/* The visible part of the disc is its lower left: the artwork sits there, clear of the edges. */
	.disc img {
		inline-size: 100%;
		block-size: 100%;
		object-fit: cover;
		transition: transform var(--dur-slow) var(--ease-out);
	}

	.disc.contain img {
		inline-size: 104%;
		block-size: 104%;
		translate: -11% 16%;
		object-fit: contain;
	}

	.disc.cover img {
		inline-size: 82%;
		block-size: 82%;
		translate: -8% 10%;
		border-radius: 50%;
	}

	.arrow {
		position: relative;
		display: grid;
		place-items: center;
		inline-size: 28px;
		block-size: 28px;
		border-radius: 50%;
		background: color-mix(in oklch, var(--ink) 20%, transparent);
		transition: transform var(--dur-base) var(--spring-pop);
	}

	.arrow :global(svg) {
		inline-size: 14px;
		block-size: 14px;
	}

	.label {
		position: relative;
		display: grid;
		gap: 2px;
	}

	.name {
		font: 650 0.9375rem / 1.18 var(--font-ui);
		letter-spacing: -0.01em;
		text-wrap: balance;
		overflow-wrap: break-word;
	}

	.count {
		font: var(--type-label);
		opacity: 0.86;
		font-variant-numeric: tabular-nums;
	}

	/* Each tile answers to its own width: the grid has two columns on phones, four from tablets. */
	@container area-tile (width >= 200px) {
		.tile {
			--ratio: 1.45;
		}

		.name {
			font-size: 1rem;
		}

		.label {
			padding-inline-end: 14%;
		}
	}

	@container area-tile (width >= 260px) {
		.tile {
			--ratio: 1.65;
			padding: 14px 16px 16px;
		}

		.name {
			font-size: 1.125rem;
		}
	}

	@media (hover: hover) {
		.tile:hover {
			transform: translateY(-2px);
			box-shadow: var(--elev-2);
			transition-duration: var(--dur-spring), 0s;
		}

		.tile:hover .arrow {
			transform: translateX(3px);
		}

		.tile:hover .disc img {
			transform: scale(1.06);
		}
	}

	.tile:active {
		transform: scale(0.98);
		transition-duration: var(--dur-instant);
	}

	@media (prefers-reduced-motion: reduce) {
		.tile:hover .disc img {
			transform: none;
		}
	}
</style>
