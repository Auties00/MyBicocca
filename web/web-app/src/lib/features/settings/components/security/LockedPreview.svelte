<script lang="ts">
	import IconLockSimple from '~icons/ph/lock-simple-fill';
	import { theme } from '#lib/ui/theme.svelte.ts';
	import ThemePreview from '../appearance/ThemePreview.svelte';
</script>

<!-- The app as the lock leaves it: out of focus behind the PIN prompt. -->
<span class="locked" aria-hidden="true">
	<span class="app"><ThemePreview palette={theme.palette} scheme={theme.resolved} /></span>
	<span class="prompt">
		<span class="badge"><IconLockSimple /></span>
		<span class="dots">
			{#each [1, 2, 3, 4] as dot (dot)}<span class={['dot', { filled: dot <= 2 }]}></span>{/each}
		</span>
	</span>
</span>

<style>
	.locked {
		display: grid;
		overflow: hidden;
		border-radius: 12px;
		container-type: inline-size;
	}

	.locked > * {
		grid-area: 1 / 1;
	}

	.app {
		filter: blur(5px) saturate(0.7);
		transform: scale(1.06);
	}

	/* Positioned, so it paints over the transformed app below rather than under it. */
	.prompt {
		position: relative;
		display: grid;
		place-content: center;
		justify-items: center;
		gap: 7cqi;
		background: color-mix(in oklch, var(--bg) 55%, transparent);
	}

	.badge {
		display: grid;
		place-items: center;
		inline-size: 20cqi;
		max-inline-size: 44px;
		aspect-ratio: 1;
		border-radius: 30%;
		background: var(--accent);
		color: var(--on-accent);
		box-shadow: var(--elev-2);
	}

	.badge :global(svg) {
		inline-size: 55%;
		block-size: 55%;
	}

	.dots {
		display: flex;
		gap: min(4cqi, 8px);
	}

	.dot {
		inline-size: min(4.5cqi, 9px);
		aspect-ratio: 1;
		border: 1.5px solid var(--border-strong);
		border-radius: 50%;
	}

	.dot.filled {
		border-color: var(--accent);
		background: var(--accent);
	}
</style>
