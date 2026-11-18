<script lang="ts">
	import type { Snippet } from 'svelte';

	interface Props {
		children: Snippet;
		label: string;
		/** The detail beside the directory on wide screens. */
		detail?: boolean;
	}

	let { children, label, detail = false }: Props = $props();
</script>

<section class={['panel', { detail }]} aria-label={label}>
	{@render children()}
</section>

<style>
	.panel {
		display: flex;
		flex-direction: column;
		inline-size: var(--panel-w);
		max-block-size: 100%;
		min-block-size: 0;
		padding-block-start: var(--sp-2);
		border-radius: var(--r-xl);
		background: var(--bg);
		box-shadow: var(--elev-3), var(--inner-light);
		overflow: hidden;
		pointer-events: auto;
	}

	.detail {
		inline-size: var(--detail-w);
	}
</style>
