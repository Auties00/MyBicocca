<script lang="ts">
	import type { Snippet } from 'svelte';

	interface Props {
		/** `TimelineRow`s and at most one `NowLine`, in time order. */
		children: Snippet;
		label: string;
	}

	let { children, label }: Props = $props();
</script>

<ol class="timeline" role="list" aria-label={label}>
	{@render children()}
</ol>

<style>
	.timeline {
		display: grid;
		gap: 8px;
		margin: 0;
		padding: 0;
		list-style: none;
		container-type: inline-size;
	}
</style>
