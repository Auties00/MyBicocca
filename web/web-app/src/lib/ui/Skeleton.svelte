<script lang="ts">
	interface Props {
		/** text: one line of the current font (pass `lines` for a paragraph); block: a box; circle: an avatar. */
		variant?: 'text' | 'block' | 'circle';
		/** CSS length. Text lines default to 100% (the last of several to 60%). */
		width?: string;
		/** CSS length; blocks default to 1lh, circles to their width. */
		height?: string;
		radius?: string;
		lines?: number;
	}

	let { variant = 'block', width, height, radius, lines = 1 }: Props = $props();
</script>

{#if variant === 'text'}
	<span class="lines" aria-hidden="true">
		{#each { length: lines }, i (i)}
			<span class="line" style:inline-size={lines > 1 && i === lines - 1 ? '60%' : width}>
				<span class="sk"></span>
			</span>
		{/each}
	</span>
{:else}
	<span
		class={['sk', 'box', variant]}
		aria-hidden="true"
		style:inline-size={width}
		style:block-size={height ?? (variant === 'circle' ? width : undefined)}
		style:border-radius={radius}
	></span>
{/if}

<style>
	.lines {
		display: grid;
		inline-size: 100%;
		animation: appear var(--dur-base) var(--ease-out) 150ms both;
	}

	.line {
		display: flex;
		align-items: center;
		block-size: 1lh;
	}

	.line .sk {
		inline-size: 100%;
		block-size: 0.72em;
		border-radius: var(--r-2xs);
	}

	.sk {
		position: relative;
		display: block;
		overflow: hidden;
		background: var(--track);
	}

	.box {
		block-size: 1lh;
		border-radius: var(--r-sm);
		animation: appear var(--dur-base) var(--ease-out) 150ms both;
	}

	.circle {
		inline-size: 40px;
		aspect-ratio: 1;
		block-size: auto;
		border-radius: 50%;
	}

	.sk::after {
		content: '';
		position: absolute;
		inset: 0;
		background: linear-gradient(
			90deg,
			transparent,
			color-mix(in oklch, var(--surface) 55%, transparent),
			transparent
		);
		transform: translateX(-100%);
		animation: sweep 1.6s var(--ease-in-out) 400ms infinite;
	}

	@media (prefers-reduced-motion: reduce) {
		.sk::after {
			display: none;
		}
	}

	@keyframes appear {
		from {
			opacity: 0;
		}
	}

	@keyframes sweep {
		60%,
		to {
			transform: translateX(100%);
		}
	}
</style>
