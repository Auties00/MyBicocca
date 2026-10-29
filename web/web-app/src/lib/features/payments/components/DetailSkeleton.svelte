<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';
</script>

<!-- A detail's geometry: the hero card with the amount, the facts. -->
<div class="skeleton" aria-hidden="true">
	<div class="card hero">
		<Skeleton variant="text" width="45%" />
		<Skeleton width="11rem" height="3rem" radius="var(--r-xs)" />
		<Skeleton variant="text" width="60%" />
	</div>
	<div class="card">
		{#each [55, 40, 62, 48] as width (width)}
			<div class="line">
				<Skeleton variant="text" width="{width}%" />
				<Skeleton variant="text" width="5rem" />
			</div>
		{/each}
	</div>
</div>

<style>
	.skeleton {
		display: grid;
		gap: var(--sp-5);
	}

	.card {
		display: grid;
		gap: var(--sp-3);
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
	}

	.hero {
		gap: var(--sp-3);
		padding-block: var(--sp-6);
	}

	.line {
		display: flex;
		justify-content: space-between;
		gap: var(--sp-4);
	}
</style>
