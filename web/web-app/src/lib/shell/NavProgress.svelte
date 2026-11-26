<script lang="ts">
	import { pendingPath } from './pending.svelte.ts';

	/** A page that loads at once never shows the bar: it appears only when the wait is noticeable. */
	const DELAY_MS = 150;

	let shown = $state(false);
	const pending = $derived(pendingPath() !== undefined);

	$effect(() => {
		if (!pending) {
			shown = false;
			return;
		}
		const timer = setTimeout(() => (shown = true), DELAY_MS);
		return () => clearTimeout(timer);
	});
</script>

<!-- Decoration: the busy state is said by the navigation itself, whose link keeps focus. -->
<div class={['progress', { shown }]} aria-hidden="true"><span class="run"></span></div>

<style>
	.progress {
		position: fixed;
		inset: 0 0 auto;
		z-index: calc(var(--z-band) + 2);
		block-size: 3px;
		overflow: clip;
		opacity: 0;
		pointer-events: none;
		transition: opacity var(--dur-base) var(--ease-out);
	}

	.shown {
		opacity: 1;
	}

	.run {
		position: absolute;
		inset-block: 0;
		inline-size: 40%;
		border-radius: var(--r-pill);
		background: var(--accent);
		animation: run 1.1s var(--ease-in-out) infinite;
	}

	@keyframes run {
		from {
			inset-inline-start: -40%;
		}
		to {
			inset-inline-start: 100%;
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.run {
			inset-inline: 0;
			inline-size: auto;
			border-radius: 0;
			animation: none;
			opacity: 0.7;
		}
	}
</style>
