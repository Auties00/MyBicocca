<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';
</script>

<div class="skeleton" aria-hidden="true">
	<div class="section">
		<Skeleton variant="text" width="9rem" height="1.6rem" />
		<Skeleton variant="text" width="16rem" />
		<div class="card">
			<Skeleton width="56px" height="56px" radius="var(--r-md)" />
			<div class="lines">
				<Skeleton variant="text" width="80%" />
				<Skeleton variant="text" width="45%" />
				<Skeleton width="11rem" height="24px" radius="var(--r-pill)" />
			</div>
		</div>
	</div>
	<div class="section">
		<Skeleton variant="text" width="7rem" height="1.6rem" />
		<Skeleton variant="text" width="14rem" />
		<Skeleton variant="text" width="4rem" />
		<div class="rows">
			{#each [72, 58, 66, 50] as width (width)}
				<div class="row">
					<Skeleton width="44px" height="44px" radius="var(--r-sm)" />
					<div class="lines">
						<Skeleton variant="text" width="{width}%" />
						<Skeleton variant="text" width="40%" />
					</div>
				</div>
			{/each}
		</div>
	</div>
</div>

<style>
	.skeleton {
		display: grid;
		gap: var(--sp-7);
	}

	.section {
		display: grid;
		gap: var(--sp-2);
	}

	.card {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: center;
		gap: var(--sp-4);
		margin-block-start: var(--sp-2);
		padding: var(--sp-4);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
	}

	.lines {
		display: grid;
		gap: 8px;
	}

	.rows {
		display: grid;
		gap: 2px;
		overflow: clip;
		border-radius: var(--r-lg);
	}

	.row {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: center;
		gap: 12px;
		min-block-size: 60px;
		padding: 10px 16px;
		background: var(--surface);
	}
</style>
