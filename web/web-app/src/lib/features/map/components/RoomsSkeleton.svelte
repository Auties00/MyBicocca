<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';

	interface Props {
		/** Floors to sketch, with how many rooms each; a typical building while the count is unknown. */
		floors?: readonly number[];
	}

	let { floors = [6, 4] }: Props = $props();
</script>

<div class="skeleton" aria-hidden="true">
	<div class="summary">
		<Skeleton variant="text" width="132px" />
		<Skeleton width="92px" height="32px" radius="999px" />
	</div>
	{#each floors as count, index (index)}
		<div class="head">
			<Skeleton variant="text" width="88px" />
			<Skeleton variant="text" width="48px" />
		</div>
		<div class="grid">
			{#each { length: count }, cell (cell)}
				<span class="cell">
					<span class="lines">
						<span class="name"><Skeleton variant="text" width="70%" /></span>
						<Skeleton variant="text" width="50%" />
						<span class="detail"><Skeleton variant="text" width="60%" /></span>
					</span>
				</span>
			{/each}
		</div>
	{/each}
</div>

<style>
	.summary {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: var(--sp-1) var(--sp-2) 0;
		font: var(--type-meta);
	}

	.head {
		display: flex;
		justify-content: space-between;
		padding: var(--sp-4) var(--sp-2) var(--sp-2);
		font: var(--type-ui-strong);
		font-size: 0.9375rem;
	}

	.grid {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 2px;
		overflow: clip;
		border-radius: var(--r-lg);
	}

	@container level (width < 300px) {
		.grid {
			grid-template-columns: minmax(0, 1fr);
		}
	}

	/* As RoomGrid's cells: name over status, a third line for the time in narrow cells. */
	.cell {
		display: grid;
		min-block-size: 68px;
		padding: 12px 14px;
		background: var(--surface);
		font: var(--type-meta);
		container: sketch-cell / inline-size;
	}

	.lines {
		display: grid;
		align-content: space-between;
		gap: 6px;
	}

	.name {
		display: flex;
		font: var(--type-ui-strong);
	}

	.detail {
		display: none;
	}

	@container sketch-cell (width < 160px) {
		.lines {
			row-gap: 4px;
		}

		.detail {
			display: flex;
		}
	}

	.cell:last-child:nth-child(odd) {
		grid-column: 1 / -1;
	}
</style>
