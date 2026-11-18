<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';

	interface Props {
		/** A tall card leads the list (the slip to pay, the total refunded). */
		lead?: boolean;
		rows?: number;
	}

	let { lead = true, rows = 3 }: Props = $props();
</script>

<!-- The lists' geometry: a heading, the leading card, then a group of rows. -->
<div class="skeleton" aria-hidden="true">
	{#if lead}
		<div class="section">
			<Skeleton variant="text" width="8rem" height="1.6rem" />
			<Skeleton variant="text" width="min(22rem, 80%)" />
			<div class="lead">
				<Skeleton variant="text" width="40%" />
				<Skeleton variant="text" width="70%" height="1.4rem" />
				<div class="split">
					<Skeleton variant="text" width="7rem" />
					<Skeleton width="7.5rem" height="2rem" radius="var(--r-xs)" />
				</div>
			</div>
		</div>
	{/if}
	<div class="section">
		<Skeleton variant="text" width="6rem" height="1.6rem" />
		<div class="rows">
			{#each { length: rows }, index (index)}
				<div class="row">
					<Skeleton width="40px" height="40px" radius="var(--r-sm)" />
					<div class="lines">
						<Skeleton variant="text" width="{[70, 56, 64, 48][index % 4]}%" />
						<Skeleton variant="text" width="40%" />
					</div>
					<Skeleton variant="text" width="4.5rem" />
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

	.lead {
		display: grid;
		gap: var(--sp-2);
		margin-block-start: var(--sp-2);
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
	}

	.split {
		display: flex;
		align-items: flex-end;
		justify-content: space-between;
		margin-block-start: var(--sp-3);
	}

	.rows {
		display: grid;
		gap: 2px;
		margin-block-start: var(--sp-2);
		border-radius: var(--r-lg);
		overflow: hidden;
	}

	.row {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr) auto;
		align-items: center;
		gap: var(--sp-3);
		padding: var(--sp-3) var(--sp-4);
		background: var(--surface);
	}

	.lines {
		display: grid;
		gap: 6px;
	}
</style>
