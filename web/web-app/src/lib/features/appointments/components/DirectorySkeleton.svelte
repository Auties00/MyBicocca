<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';

	interface Props {
		/** The sections beside their offices, as the loaded page lays them out. */
		wide: boolean;
	}

	let { wide }: Props = $props();
</script>

{#snippet rows(count: number)}
	<div class="rows">
		{#each { length: count }, index (index)}
			<div class="row">
				<Skeleton width="40px" height="40px" radius="12px" />
				<div class="lines">
					<Skeleton variant="text" width="48%" />
					<Skeleton variant="text" width="72%" />
				</div>
			</div>
		{/each}
	</div>
{/snippet}

<div class="skeleton" aria-hidden="true">
	<Skeleton width="min(100%, 480px)" height="44px" radius="12px" />
	{#if wide}
		<div class="split">
			{@render rows(4)}
			{@render rows(5)}
		</div>
	{:else}
		{@render rows(4)}
	{/if}
</div>

<style>
	.skeleton {
		display: grid;
		gap: var(--sp-4);
	}

	.split {
		display: grid;
		grid-template-columns: minmax(260px, 340px) minmax(0, 1fr);
		align-items: start;
		gap: var(--sp-7);
	}

	.rows {
		display: grid;
		gap: 2px;
	}

	.row {
		display: flex;
		align-items: center;
		gap: 14px;
		min-block-size: 72px;
		padding-inline: 16px;
		border-radius: var(--r-sm);
		background: var(--surface);
	}

	.lines {
		display: grid;
		flex: 1;
		gap: 6px;
		font: var(--type-meta);
	}
</style>
