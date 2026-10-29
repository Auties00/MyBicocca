<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import HeroSkeleton from './HeroSkeleton.svelte';

	interface Props {
		/** A hero card above the sections (a degree's grade). */
		hero?: boolean;
	}

	let { hero = false }: Props = $props();
</script>

<!-- The detail's geometry: chips or a hero, then topics as headed cards of facts. -->
<div class="skeleton" aria-hidden="true">
	{#if hero}
		<HeroSkeleton />
	{:else}
		<div class="chips">
			<Skeleton width="5.5rem" height="28px" radius="var(--r-pill)" />
			<Skeleton width="6.5rem" height="28px" radius="var(--r-pill)" />
		</div>
	{/if}
	<div class="sections">
		{#each [3, 4, 2] as rows, index (index)}
			<div class="section">
				<div class="head">
					<Skeleton width="32px" height="32px" radius="10px" />
					<Skeleton variant="text" width="8rem" />
				</div>
				<div class="card">
					{#each Array.from({ length: rows }, (_, row) => row) as row (row)}
						<div class="fact">
							<Skeleton variant="text" width="35%" />
							<Skeleton variant="text" width="55%" />
						</div>
					{/each}
				</div>
			</div>
		{/each}
	</div>
</div>

<style>
	.skeleton {
		display: grid;
		gap: var(--sp-5);
	}

	.chips {
		display: flex;
		gap: var(--sp-2);
	}

	.sections {
		column-gap: var(--sp-5);
	}

	.section {
		display: grid;
		gap: var(--sp-3);
		margin-block-end: var(--sp-6);
		break-inside: avoid;
	}

	.head {
		display: flex;
		align-items: center;
		gap: 10px;
	}

	.card {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-4) var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
	}

	.fact {
		display: grid;
		gap: 6px;
	}

	@container documents (width >= 720px) {
		.sections {
			columns: 2;
		}
	}
</style>
