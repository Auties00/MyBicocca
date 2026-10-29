<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';
</script>

<div class="skeleton" aria-hidden="true">
	<div class="hero">
		<div class="score">
			<Skeleton variant="text" width="3rem" />
			<Skeleton width="7.5rem" height="4rem" radius="var(--r-sm)" />
		</div>
		<div class="facts">
			{#each ['60%', '45%', '35%'] as width (width)}
				<div class="fact">
					<Skeleton variant="text" width="5rem" />
					<Skeleton variant="text" {width} />
				</div>
			{/each}
		</div>
	</div>
	<div class="card">
		<Skeleton variant="text" width="9rem" height="1.5rem" />
		<Skeleton variant="text" lines={2} />
		<div class="actions">
			<Skeleton height="var(--control-h-lg)" radius="var(--r-sm)" />
			<Skeleton height="var(--control-h-lg)" radius="var(--r-sm)" />
		</div>
	</div>
</div>

<style>
	.skeleton {
		display: grid;
		gap: var(--sp-4);
		max-inline-size: var(--w-reading);
	}

	.hero,
	.card {
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
	}

	.hero {
		display: grid;
		overflow: hidden;
	}

	.score {
		display: grid;
		gap: var(--sp-3);
		padding: var(--sp-5);
	}

	.facts {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
		gap: var(--sp-3) var(--sp-5);
		padding: var(--sp-4) var(--sp-5) var(--sp-5);
		border-block-start: 1px solid var(--hairline);
	}

	.fact {
		display: grid;
		gap: 6px;
	}

	.card {
		display: grid;
		gap: var(--sp-3);
		padding: var(--sp-5);
	}

	.actions {
		display: grid;
		grid-template-columns: minmax(0, 1fr) minmax(0, 1.4fr);
		gap: var(--sp-2);
	}

	@container detail (width >= 560px) {
		.hero {
			grid-template-columns: minmax(0, 1fr) minmax(220px, 0.8fr);
		}

		.facts {
			grid-template-columns: minmax(0, 1fr);
			padding: var(--sp-5);
			border-block-start: none;
			border-inline-start: 1px solid var(--hairline);
		}
	}
</style>
