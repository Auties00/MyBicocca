<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import { fitText } from '../fitText.ts';
	import LogoGlyph from './LogoGlyph.svelte';

	interface Props {
		name: string;
		number: string;
		chip: string;
	}

	let { name, number, chip }: Props = $props();
</script>

<div class="texture" aria-hidden="true"></div>
<div class="art" aria-hidden="true"><LogoGlyph /></div>
<div class="mark" aria-hidden="true"><LogoGlyph /></div>
<div class="heading">
	<p class="university" lang="it" translate="no">{m.profile_badge_university()}</p>
	<svg class="wordmark" viewBox="0 0 41.44 7.97" role="img" aria-label="MyBicocca">
		<path
			fill="currentColor"
			d="M4.28,6.27V2.23l-1.35,2.94h-0.66L0.89,2.23v4.04h-0.89V0.23h0.89l1.71,3.76L4.28,0.23h0.89v6.04h-0.89ZM7.9,7.13c-0.08,0.24 -0.18,0.41 -0.3,0.54 -0.23,0.24 -0.54,0.31 -0.86,0.31h-0.34v-0.78h0.21c0.35,0 0.48,-0.11 0.6,-0.46l0.23,-0.67 -1.46,-4.1h0.89l1,3.03 0.97,-3.03h0.9l-1.84,5.16ZM10.41,6.27V0.33h2.67c1.04,0 1.79,0.66 1.79,1.65 0,0.5 -0.19,0.91 -0.67,1.23 0.66,0.25 0.97,0.79 0.97,1.42 0,0.97 -0.71,1.65 -1.79,1.65h-2.97ZM11.68,2.78h1.23c0.45,0 0.71,-0.3 0.71,-0.69 0,-0.43 -0.32,-0.69 -0.76,-0.69h-1.18v1.38ZM11.68,5.23h1.45c0.45,0 0.73,-0.31 0.73,-0.71 0,-0.43 -0.27,-0.75 -0.73,-0.75h-1.45v1.46ZM15.87,0.72c0,-0.4 0.31,-0.72 0.71,-0.72s0.71,0.31 0.71,0.72 -0.31,0.72 -0.71,0.72 -0.71,-0.3 -0.71,-0.72ZM15.97,6.27V2.07h1.22v4.2h-1.22ZM22.25,4.61c-0.18,1.05 -1.03,1.75 -2.1,1.75 -1.27,0 -2.19,-0.93 -2.19,-2.19s0.91,-2.19 2.19,-2.19c1.02,0 1.93,0.66 2.08,1.7h-1.16c-0.15,-0.38 -0.5,-0.63 -0.91,-0.63 -0.59,0 -1.03,0.48 -1.03,1.11s0.44,1.11 1.03,1.11c0.43,0 0.77,-0.25 0.92,-0.67h1.17ZM24.89,1.98c1.29,0 2.21,0.92 2.21,2.19s-0.92,2.19 -2.21,2.19 -2.22,-0.92 -2.22,-2.19 0.92,-2.19 2.22,-2.19ZM25.93,4.17c0,-0.64 -0.43,-1.1 -1.03,-1.1s-1.04,0.47 -1.04,1.1 0.43,1.09 1.04,1.09 1.03,-0.47 1.03,-1.09ZM31.86,4.61c-0.18,1.05 -1.03,1.75 -2.1,1.75 -1.27,0 -2.19,-0.93 -2.19,-2.19s0.91,-2.19 2.19,-2.19c1.02,0 1.93,0.66 2.08,1.7h-1.16c-0.15,-0.38 -0.5,-0.63 -0.91,-0.63 -0.59,0 -1.03,0.48 -1.03,1.11s0.44,1.11 1.03,1.11c0.43,0 0.77,-0.25 0.92,-0.67h1.17ZM36.58,4.61c-0.18,1.05 -1.03,1.75 -2.1,1.75 -1.27,0 -2.19,-0.93 -2.19,-2.19s0.91,-2.19 2.19,-2.19c1.02,0 1.93,0.66 2.08,1.7h-1.16c-0.15,-0.38 -0.5,-0.63 -0.91,-0.63 -0.59,0 -1.03,0.48 -1.03,1.11s0.44,1.11 1.03,1.11c0.43,0 0.77,-0.25 0.92,-0.67h1.17ZM40.38,6.27l-0.1,-0.47c-0.28,0.33 -0.69,0.56 -1.32,0.56 -1.13,0 -1.96,-0.94 -1.96,-2.2s0.83,-2.18 1.96,-2.18c0.63,0 1.04,0.22 1.32,0.55l0.09,-0.47h1.06v4.2h-1.05ZM40.27,4.16c0,-0.63 -0.43,-1.1 -1.05,-1.1s-1.04,0.47 -1.04,1.1 0.42,1.13 1.04,1.13 1.05,-0.49 1.05,-1.13Z"
		/>
	</svg>
</div>
<img class="chip" src={chip} alt="" width="51" height="42" decoding="async" draggable="false" />
<svg class="contactless" viewBox="0 0 73.77 93.49" aria-hidden="true">
	<path
		fill="currentColor"
		fill-rule="evenodd"
		d="M56.53,93.27c-1.5,-0.43 -3.02,-2.22 -3.34,-3.92 -0.21,-1.11 0.05,-1.88 1.64,-4.96 6.43,-12.44 9.22,-23.88 9.22,-37.77s-2.47,-23.87 -8.62,-36.39c-1.32,-2.69 -2.4,-5.18 -2.4,-5.54 0,-1.33 1.11,-3.26 2.32,-4.02 1.66,-1.04 4.43,-0.84 5.76,0.4 0.51,0.48 1.84,2.66 2.95,4.84 4.4,8.63 7.41,18.1 8.9,27.96 0.92,6.1 1.1,17.66 0.37,23.25 -1.36,10.3 -4.08,19.5 -8.43,28.5 -2.63,5.44 -4.28,7.68 -5.67,7.68 -0.28,0 -0.73,0.07 -1,0.15 -0.28,0.08 -1.04,0 -1.69,-0.19h0ZM38.64,83.91c-2.3,-0.67 -3.96,-3.49 -3.3,-5.61 0.16,-0.51 1.24,-2.87 2.41,-5.24 4.92,-10.04 6.97,-20.8 5.97,-31.37 -0.82,-8.68 -2.38,-14.16 -6.3,-22.17 -2.63,-5.38 -2.75,-6.21 -1.22,-8.37 1.45,-2.06 5.08,-2.39 6.99,-0.64 1.32,1.21 5.43,9.8 7.02,14.66 5.25,16.09 4.45,33.35 -2.28,49.16 -3.45,8.09 -5.87,10.6 -9.28,9.6h0ZM20.19,74.42c-1.42,-0.64 -2.65,-2.14 -2.98,-3.66 -0.2,-0.92 0.15,-1.94 1.83,-5.36 6.2,-12.55 6.26,-24.64 0.2,-36.9 -2.66,-5.38 -2.72,-6.46 -0.47,-8.58 1.11,-1.05 1.58,-1.23 3.14,-1.23 2.62,0 3.97,1.31 6.31,6.12 3.6,7.39 5.07,13.8 5.06,22.01 -0.01,8.44 -1.39,14.23 -5.23,22.02 -2.2,4.46 -3.29,5.64 -5.49,5.94 -0.67,0.09 -1.74,-0.07 -2.38,-0.35h0ZM2.97,65.58c-1.13,-0.47 -2.41,-2 -2.8,-3.33 -0.43,-1.49 -0.08,-2.67 1.61,-5.45 2.18,-3.58 2.68,-5.5 2.68,-10.17s-0.51,-6.59 -2.68,-10.17c-0.74,-1.21 -1.48,-2.67 -1.65,-3.23 -0.7,-2.36 1.26,-5.25 3.9,-5.74 2.24,-0.42 3.77,0.58 5.83,3.79 5.95,9.29 5.95,21.71 0,30.92 -2.1,3.25 -4.44,4.4 -6.9,3.38h0Z"
	/>
</svg>
<div class="holder">
	<p class="name" {@attach fitText(name)}>{name}</p>
	{#if number}<p class="number" translate="no">{number}</p>{/if}
</div>

<style>
	.texture {
		position: absolute;
		inset: 0;
		background: url('../assets/card-texture.webp') 0 0 / 100% 100% no-repeat;
		pointer-events: none;
	}

	/* The big mark drifts against the pointer: the parallax that gives the card depth. */
	.art {
		position: absolute;
		inset-inline-start: calc(195 * var(--u));
		inset-block-start: calc(112 * var(--u));
		inline-size: calc(186 * var(--u));
		aspect-ratio: 1;
		opacity: 0.2;
		transform: translate(
				calc((0.5 - var(--tx)) * 60 * var(--u)),
				calc((0.5 - var(--ty)) * 24 * var(--u))
			)
			rotate(7.5deg);
	}

	.mark {
		position: absolute;
		inset-inline-start: calc(302 * var(--u));
		inset-block-start: calc(38 * var(--u));
		inline-size: calc(41.4 * var(--u));
		aspect-ratio: 1;
	}

	.heading {
		position: absolute;
		inset-inline-end: calc(86 * var(--u));
		inset-block-start: calc(36 * var(--u));
		display: grid;
		justify-items: end;
		gap: calc(3 * var(--u));
	}

	.university {
		font: 400 calc(17 * var(--u)) / 1.2 var(--font-ui);
		letter-spacing: 0.01em;
		white-space: nowrap;
	}

	.wordmark {
		inline-size: calc(100 * var(--u));
		block-size: auto;
	}

	.chip {
		position: absolute;
		inset-inline-start: calc(40 * var(--u));
		inset-block-start: calc(95 * var(--u));
		inline-size: calc(51.2 * var(--u));
		block-size: calc(41.6 * var(--u));
		border-radius: calc(7.6 * var(--u));
		user-select: none;
	}

	.contactless {
		position: absolute;
		inset-inline-start: calc(108 * var(--u));
		inset-block-start: calc(100 * var(--u));
		inline-size: calc(23.7 * var(--u));
		block-size: calc(30 * var(--u));
	}

	.holder {
		position: absolute;
		inset-inline: calc(40 * var(--u)) calc(24 * var(--u));
		inset-block-start: calc(160 * var(--u));
		display: grid;
		gap: calc(4 * var(--u));
	}

	.name {
		overflow: hidden;
		font: 700 calc(18 * var(--u) * var(--fit, 1)) / 1.3 var(--font-ui);
		letter-spacing: calc(2 * var(--u) * var(--fit, 1));
		text-transform: uppercase;
		white-space: nowrap;
	}

	.number {
		opacity: 0.8;
		font: 500 calc(16 * var(--u)) / 1.3 var(--font-ui);
		letter-spacing: calc(1.5 * var(--u));
		font-variant-numeric: tabular-nums;
	}
</style>
