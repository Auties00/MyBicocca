<script lang="ts">
	import type { Snippet } from 'svelte';

	interface Props {
		children: Snippet;
		/** accent marks the open item; danger an overdue one. */
		ring?: 'default' | 'accent' | 'danger';
		/** Tooth width of the torn edges, in px. */
		tooth?: number;
	}

	let { children, ring = 'default', tooth = 14 }: Props = $props();
</script>

<!--
	A slip of paper with torn (zig-zag) top and bottom edges, as Android's receipts. The edges are a
	mask, so the outline is drawn by drop-shadows on the wrapper, which follow the teeth.
-->
<div class={['paper', ring]} style:--tooth="{tooth}px">
	<div class="sheet">{@render children()}</div>
</div>

<style>
	/*
	 * On a dark page plain paper sinks into the background: there the slip is one surface up and
	 * edged in crimson ink, as Android's.
	 */
	.paper {
		--paper-ring: light-dark(
			oklch(0% 0 0 / 0.18),
			color-mix(in oklch, var(--accent) 45%, transparent)
		);
		filter: drop-shadow(0 0 0.6px var(--paper-ring)) drop-shadow(0 0 0.6px var(--paper-ring))
			drop-shadow(0 2px 3px color-mix(in oklch, var(--shadow-color) 8%, transparent));
	}

	/* The open slip: full ink, a heavier edge. */
	.accent {
		--paper-ring: var(--accent);
		filter: drop-shadow(0 0 0.8px var(--paper-ring)) drop-shadow(0 0 0.8px var(--paper-ring))
			drop-shadow(0 0 0.8px var(--paper-ring))
			drop-shadow(0 2px 3px color-mix(in oklch, var(--shadow-color) 8%, transparent));
	}

	.danger {
		--paper-ring: var(--danger);
	}

	.sheet {
		padding-block: calc(var(--tooth) / 2);
		background: var(--paper-fill, light-dark(var(--surface), var(--surface-raised)));
		transition: background-color var(--dur-fast) var(--ease-out);
		mask:
			conic-gradient(from 135deg at top, #0000, #000 1deg 89deg, #0000 90deg) top / var(--tooth) 51%
				round no-repeat,
			conic-gradient(from -45deg at bottom, #0000, #000 1deg 89deg, #0000 90deg) bottom /
				var(--tooth) 51% round no-repeat;
	}

	@media (forced-colors: active) {
		.sheet {
			mask: none;
			border: 1px solid CanvasText;
		}
	}
</style>
