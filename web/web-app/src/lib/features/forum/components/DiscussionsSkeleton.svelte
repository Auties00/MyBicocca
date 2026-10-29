<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Skeleton from '#lib/ui/Skeleton.svelte';

	/* Shown only while loading: the rows keep their space at once, their bones after a moment. */
	const gate = new LoadingGate(() => true);
</script>

<ol class="rows" aria-busy="true" aria-label={m.forum_discussions_loading()}>
	{#each { length: 5 }, index (index)}
		<li class="row">
			{#if gate.visible}
				<Skeleton variant="circle" width="40px" />
				<span class="lines">
					<Skeleton variant="text" width="{82 - index * 9}%" />
					<Skeleton variant="text" lines={2} height="0.85em" />
					<Skeleton variant="text" width="50%" height="0.75em" />
				</span>
			{/if}
		</li>
	{/each}
</ol>

<style>
	.rows {
		display: grid;
		gap: 8px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	/* As tall as a typical row, before and after the bones show. */
	.row {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		gap: 12px;
		block-size: 118px;
		overflow: clip;
		padding: 14px;
		border-radius: var(--r-lg);
		background: var(--surface);
	}

	.lines {
		display: grid;
		align-content: start;
		gap: 4px;
	}
</style>
