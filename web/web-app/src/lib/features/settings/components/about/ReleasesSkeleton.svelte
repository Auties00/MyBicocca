<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';
</script>

<!-- The shape of an open release card: title, version line, then a section of notes. -->
<div class="card" aria-hidden="true">
	<div class="head">
		<Skeleton variant="text" width="55%" height="1.4rem" />
		<Skeleton variant="text" width="30%" />
	</div>
	{#each [4, 3] as lines, index (index)}
		<div class="section">
			<Skeleton variant="text" width="40%" height="1.2rem" />
			<Skeleton variant="text" {lines} />
		</div>
	{/each}
</div>

<style>
	.card {
		display: grid;
		gap: 22px;
		padding: 18px 20px 24px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.head,
	.section {
		display: grid;
		gap: 8px;
	}
</style>
