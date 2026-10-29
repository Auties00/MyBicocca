<script lang="ts">
	import { formatNumber } from '#lib/format/numbers.ts';
	import type { Palette } from '#lib/ui/theme.svelte.ts';

	interface Props {
		palette: Palette;
		scheme: 'light' | 'dark';
	}

	let { palette, scheme }: Props = $props();

	const average = formatNumber(28.4, { minimumFractionDigits: 1 });
</script>

<!--
	A miniature of the app drawn with the real tokens: the subtree re-tints itself through
	data-palette and data-theme (see tokens.css). Narrow, it is a phone with a tab bar; wide, a
	window with a sidebar. The selection steps through the destinations, as Android's previews do.
-->
<div class="preview" data-palette={palette} data-theme={scheme} aria-hidden="true">
	<div class="screen">
		<div class="nav">
			<span class="logo"></span>
			<span class="items">
				<span class="indicator"></span>
				{#each [1, 2, 3, 4] as item (item)}
					<span class="item"><span class="glyph"></span><span class="label"></span></span>
				{/each}
			</span>
		</div>
		<div class="content">
			<div class="bar">
				<span class="heading"></span>
				<span class="avatar"></span>
			</div>
			<div class="card">
				<span class="numeral">{average}</span>
				<span class="lines">
					<span class="line"></span>
					<span class="line short"></span>
				</span>
			</div>
			<div class="rows">
				{#each [2, 5, 7] as slot (slot)}
					<span class="row" data-course={slot}>
						<span class="swatch"></span>
						<span class="line"></span>
					</span>
				{/each}
			</div>
			<span class="button"></span>
		</div>
	</div>
</div>

<style>
	.preview {
		container: preview / inline-size;
		inline-size: 100%;
		color: var(--text);
	}

	.screen {
		position: relative;
		display: grid;
		grid-template-rows: minmax(0, 1fr);
		aspect-ratio: 9 / 15;
		overflow: hidden;
		border-radius: 12px;
		background: var(--bg);
		box-shadow: inset 0 0 0 1px var(--hairline);
	}

	/* A dark screen on a dark page needs a firmer edge to read as a device rather than a fragment. */
	.preview[data-theme='dark'] .screen {
		box-shadow: inset 0 0 0 1px var(--hairline-strong);
	}

	.content {
		display: grid;
		grid-template-rows: auto auto 1fr;
		align-content: start;
		gap: 7cqi;
		padding: 9cqi 8cqi;
	}

	.bar {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.heading {
		inline-size: 52%;
		block-size: 7cqi;
		border-radius: 2cqi;
		background: var(--text);
		opacity: 0.85;
	}

	.avatar {
		inline-size: 11cqi;
		aspect-ratio: 1;
		border-radius: 50%;
		background: var(--accent);
	}

	.card {
		display: grid;
		gap: 4cqi;
		padding: 7cqi;
		border-radius: 7cqi;
		background: var(--surface);
		box-shadow: var(--elev-1);
	}

	.numeral {
		color: var(--accent-text);
		font: 500 17cqi / 1 var(--font-display);
		letter-spacing: -0.03em;
	}

	.lines {
		display: grid;
		gap: 3cqi;
	}

	.line {
		block-size: 3.5cqi;
		border-radius: 2cqi;
		background: var(--text-subtle);
		opacity: 0.5;
	}

	.line.short {
		inline-size: 60%;
	}

	.rows {
		display: grid;
		gap: 1.2cqi;
		align-content: start;
	}

	.row {
		display: flex;
		align-items: center;
		gap: 5cqi;
		padding: 4cqi;
		border-radius: 2cqi;
		background: var(--surface);
	}

	.row:first-child {
		border-start-start-radius: 6cqi;
		border-start-end-radius: 6cqi;
	}

	.row:last-child {
		border-end-start-radius: 6cqi;
		border-end-end-radius: 6cqi;
	}

	.swatch {
		flex-shrink: 0;
		inline-size: 10cqi;
		aspect-ratio: 1;
		border-radius: 3cqi;
		background: var(--course-container);
		box-shadow: inset 2cqi 0 0 var(--course-bar);
	}

	.row .line {
		flex: 1;
	}

	.button {
		position: absolute;
		inset-block-end: 28cqi;
		inset-inline-end: 8cqi;
		inline-size: 30cqi;
		block-size: 10cqi;
		border-radius: 999px;
		background: var(--accent);
	}

	/* Phone: a floating tab bar at the foot. */
	.nav {
		position: absolute;
		inset-inline: 6cqi;
		inset-block-end: 6cqi;
		z-index: 1;
		padding: 2.5cqi;
		border-radius: 999px;
		background: var(--surface-raised);
		box-shadow: var(--elev-2);
	}

	.logo,
	.label {
		display: none;
	}

	.items {
		position: relative;
		display: grid;
		grid-auto-flow: column;
		grid-auto-columns: 1fr;
	}

	.item {
		position: relative;
		display: grid;
		place-items: center;
		block-size: 11cqi;
	}

	.glyph {
		inline-size: 5cqi;
		aspect-ratio: 1;
		border-radius: 1.5cqi;
		background: var(--text-muted);
		opacity: 0.6;
	}

	.indicator {
		position: absolute;
		inset-block: 0;
		inset-inline-start: 0;
		inline-size: 25%;
		border-radius: 999px;
		background: var(--container);
		animation: step-x 9.6s var(--spring-standard) infinite;
	}

	.indicator::after {
		content: '';
		position: absolute;
		inset: 0;
		margin: auto;
		inline-size: 5cqi;
		block-size: 5cqi;
		border-radius: 1.5cqi;
		background: var(--accent);
	}

	/* Window: a sidebar on the left. */
	@container preview (width >= 180px) {
		.screen {
			grid-template-columns: 27% minmax(0, 1fr);
			aspect-ratio: 16 / 10.5;
			border-radius: 10px;
		}

		.nav {
			position: static;
			display: grid;
			grid-template-rows: auto 1fr;
			gap: 5cqi;
			padding: 4.5cqi 3cqi;
			border-radius: 0;
			background: var(--bg);
			box-shadow: inset -1px 0 0 var(--hairline);
		}

		.logo {
			display: block;
			inline-size: 5.5cqi;
			aspect-ratio: 1;
			margin-inline-start: 1.5cqi;
			border-radius: 1cqi;
			background: var(--brand);
		}

		.items {
			grid-auto-flow: row;
			grid-auto-rows: 7cqi;
			grid-auto-columns: auto;
			align-content: start;
		}

		.item {
			display: flex;
			gap: 2cqi;
			block-size: auto;
			padding-inline: 1.5cqi;
		}

		.glyph {
			flex-shrink: 0;
			inline-size: 2.6cqi;
			border-radius: 0.8cqi;
		}

		.label {
			display: block;
			flex: 1;
			block-size: 1.8cqi;
			border-radius: 1cqi;
			background: var(--text-muted);
			opacity: 0.45;
		}

		.indicator {
			inset-inline: 0;
			inset-block: 0 auto;
			inline-size: auto;
			block-size: 7cqi;
			border-radius: 1.6cqi;
			animation-name: step-y;
		}

		.indicator::after {
			inset: 0 auto 0 1.5cqi;
			inline-size: 2.6cqi;
			block-size: 2.6cqi;
			border-radius: 0.8cqi;
		}

		.content {
			gap: 3.2cqi;
			padding: 4.5cqi 5cqi;
		}

		.heading {
			block-size: 3.4cqi;
			border-radius: 1cqi;
		}

		.avatar {
			inline-size: 5cqi;
		}

		.card {
			grid-template-columns: auto 1fr;
			align-items: end;
			gap: 3cqi;
			padding: 3.5cqi;
			border-radius: 3cqi;
		}

		.numeral {
			font-size: 8cqi;
		}

		.lines {
			gap: 1.6cqi;
			padding-block-end: 0.6cqi;
		}

		.line {
			block-size: 1.6cqi;
			border-radius: 1cqi;
		}

		.rows {
			gap: 0.6cqi;
		}

		.row {
			gap: 2.5cqi;
			padding: 1.8cqi 2.4cqi;
			border-radius: 1cqi;
		}

		.row:first-child {
			border-start-start-radius: 2.6cqi;
			border-start-end-radius: 2.6cqi;
		}

		.row:last-child {
			border-end-start-radius: 2.6cqi;
			border-end-end-radius: 2.6cqi;
		}

		.swatch {
			inline-size: 4cqi;
			border-radius: 1.2cqi;
			box-shadow: inset 0.9cqi 0 0 var(--course-bar);
		}

		.button {
			inset-block-end: 4.5cqi;
			inset-inline-end: 5cqi;
			inline-size: 13cqi;
			block-size: 4.6cqi;
		}
	}

	@keyframes step-x {
		0%,
		22% {
			transform: translateX(0);
		}
		25%,
		47% {
			transform: translateX(100%);
		}
		50%,
		72% {
			transform: translateX(200%);
		}
		75%,
		97% {
			transform: translateX(300%);
		}
		100% {
			transform: translateX(0);
		}
	}

	@keyframes step-y {
		0%,
		22% {
			transform: translateY(0);
		}
		25%,
		47% {
			transform: translateY(100%);
		}
		50%,
		72% {
			transform: translateY(200%);
		}
		75%,
		97% {
			transform: translateY(300%);
		}
		100% {
			transform: translateY(0);
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.indicator {
			animation: none;
		}
	}
</style>
