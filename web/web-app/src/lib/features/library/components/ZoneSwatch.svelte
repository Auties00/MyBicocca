<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { ZoneColor } from '../model.ts';

	interface Props {
		color: ZoneColor;
		/** A glyph in the zone's colour; a solid square otherwise. */
		children?: Snippet;
	}

	let { color, children }: Props = $props();
</script>

<span class={['swatch', color]} aria-hidden="true">
	{#if children}{@render children()}{:else}<span class="chip"></span>{/if}
</span>

<style>
	/* Bicocca names its zones by colour; the hues are Android's, the same in both themes. */
	.swatch {
		--zone: var(--accent);
		display: grid;
		place-items: center;
		flex-shrink: 0;
		inline-size: 40px;
		block-size: 40px;
		border-radius: 12px;
		background: color-mix(in oklch, var(--zone) 16%, transparent);
		color: var(--zone);
	}

	.swatch :global(svg) {
		inline-size: 20px;
		block-size: 20px;
	}

	.chip {
		inline-size: 18px;
		block-size: 18px;
		border-radius: 6px;
		background: var(--zone);
	}

	.red {
		--zone: #d3302f;
	}

	.yellow {
		--zone: #e0a100;
	}

	.green {
		--zone: #2e9e55;
	}

	.lilac {
		--zone: #9a6bd8;
	}

	.blue {
		--zone: #2274d6;
	}

	.orange {
		--zone: #ec6a1e;
	}

	.carrel {
		--zone: #00897b;
	}
</style>
