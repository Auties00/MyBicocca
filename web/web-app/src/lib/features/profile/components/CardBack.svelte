<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import { fitText } from '../fitText.ts';
	import LogoGlyph from './LogoGlyph.svelte';

	interface Props {
		/** The holder's name as signed ("Marco Rossi"). */
		signature: string;
		email: string;
		photo: string | null;
		/** Set once the back may be seen: only then the script face and the photo load. */
		revealed: boolean;
	}

	let { signature, email, photo, revealed }: Props = $props();

	/* Six marks scattered at different depths: the further ones drift more. */
	const marks = [
		{ x: 61.8, y: 186.7, size: 92, turn: -7.5, alpha: 0.2, dx: 45, dy: 15 },
		{ x: 25.3, y: 100.3, size: 32.2, turn: 20, alpha: 0.17, dx: 57, dy: 30 },
		{ x: 143.4, y: 175.4, size: 36.8, turn: 20, alpha: 0.135, dx: 75, dy: 30 },
		{ x: 80.2, y: 115.2, size: 18.4, turn: -20, alpha: 0.125, dx: 90, dy: 48 },
		{ x: 175.1, y: 215.1, size: 13.8, turn: -20, alpha: 0.1, dx: 90, dy: 45 },
		{ x: 127.6, y: 122.6, size: 6.9, turn: 40, alpha: 0.05, dx: 150, dy: 60 }
	];
</script>

<div class="texture" aria-hidden="true"></div>
{#each marks as mark, index (index)}
	<div
		class="scatter"
		aria-hidden="true"
		style:--x={mark.x}
		style:--y={mark.y}
		style:--size={mark.size}
		style:--turn="{mark.turn}deg"
		style:--dx={mark.dx}
		style:--dy={mark.dy}
		style:opacity={mark.alpha}
	>
		<LogoGlyph />
	</div>
{/each}
<div class="stripe" aria-hidden="true"></div>
<div class="lower">
	<div class="signature">
		<p class="label">{m.profile_badge_signature()}</p>
		<div class="box">
			<span class="rule"></span><span class="rule"></span><span class="rule"></span>
			{#if revealed}
				<p class="script" {@attach fitText(signature, 15 / 38)}>{signature}</p>
			{/if}
		</div>
	</div>
	<div class="photo">
		{#if revealed && photo}
			<img src={photo} alt={m.profile_badge_photo()} decoding="async" draggable="false" />
		{/if}
	</div>
	<div class="email">
		<p class="label">{m.profile_badge_email()}</p>
		<p class="address" translate="no">{email}</p>
	</div>
</div>

<style>
	@font-face {
		font-family: 'Mrs Saint Delafield';
		font-style: normal;
		font-weight: 400;
		font-display: swap;
		src: url('../assets/mrs-saint-delafield.woff2') format('woff2');
	}

	.texture {
		position: absolute;
		inset: 0;
		background: url('../assets/card-texture.webp') 0 0 / 100% 100% no-repeat;
	}

	.scatter {
		position: absolute;
		inset-inline-start: calc((var(--x) - var(--size) / 2) * var(--u));
		inset-block-start: calc((var(--y) - var(--size) / 2) * var(--u));
		inline-size: calc(var(--size) * var(--u));
		aspect-ratio: 1;
		transform: translate(
				calc((0.5 - var(--tx)) * var(--dx) * var(--u)),
				calc((0.5 - var(--ty)) * var(--dy) * var(--u))
			)
			rotate(var(--turn));
	}

	.stripe {
		position: absolute;
		inset-inline: 0;
		inset-block-start: calc(24 * var(--u));
		block-size: calc(55 * var(--u));
		background: oklch(0% 0 0 / 0.95);
	}

	.lower {
		position: absolute;
		inset: calc(79 * var(--u)) calc(12 * var(--u)) calc(16 * var(--u)) calc(16 * var(--u));
		display: grid;
		grid-template-columns: minmax(0, 1fr) calc(67.5 * var(--u));
		grid-template-rows: minmax(0, 1fr) auto;
		column-gap: calc(12 * var(--u));
		row-gap: calc(8 * var(--u));
	}

	.label {
		font: 500 calc(10 * var(--u)) / 1.3 var(--font-ui);
	}

	.signature {
		display: grid;
		align-content: start;
		gap: calc(1 * var(--u));
		padding-block-start: calc(10 * var(--u));
	}

	.box {
		position: relative;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		block-size: calc(40 * var(--u));
		padding-block: calc(5 * var(--u));
		background: rgb(166 166 166 / 0.75);
	}

	.rule {
		block-size: calc(7 * var(--u));
		background: rgb(184 184 184 / 0.75);
	}

	.script {
		position: absolute;
		inset: 0;
		display: flex;
		align-items: center;
		overflow: hidden;
		padding: calc(1.75 * var(--u)) calc(10 * var(--u)) 0;
		color: #000;
		font:
			400 calc(38 * var(--u) * var(--fit, 1)) / 1 'Mrs Saint Delafield',
			cursive;
		white-space: nowrap;
	}

	.photo {
		grid-row: 1;
		grid-column: 2;
		margin-block-start: calc(12 * var(--u));
		overflow: hidden;
	}

	.photo img {
		display: block;
		inline-size: 100%;
		block-size: 100%;
		object-fit: cover;
		filter: grayscale(1);
	}

	.email {
		grid-column: 1 / -1;
		padding-inline: calc(8 * var(--u));
	}

	.address {
		overflow: hidden;
		font: 500 calc(12 * var(--u)) / 1.35 var(--font-ui);
		text-overflow: ellipsis;
		white-space: nowrap;
	}
</style>
