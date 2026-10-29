<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';

	/* Rows of the "to fill in" section, then of the "completed" one. */
	const ROWS = [3, 2];
	const WIDTHS = [62, 74, 54, 68];
</script>

<div class="skeleton" aria-hidden="true">
	{#each ROWS as count, section (section)}
		<div class="section">
			<div class="head">
				<span class="heading"><Skeleton variant="text" width="8rem" /></span>
				{#if section === 0}<span class="meta"><Skeleton variant="text" width="15rem" /></span>{/if}
			</div>
			<div class="rows">
				{#each { length: count }, row (row)}
					<div class="row">
						<Skeleton variant="circle" width="40px" />
						<div class="lines">
							<span class="title">
								<Skeleton variant="text" width="{WIDTHS[(row + section) % WIDTHS.length]}%" />
							</span>
							<span class="meta"><Skeleton variant="text" width="38%" /></span>
						</div>
					</div>
				{/each}
			</div>
		</div>
	{/each}
</div>

<style>
	.skeleton {
		display: grid;
		gap: var(--sp-7);
	}

	.section {
		display: grid;
		gap: var(--sp-4);
	}

	.head {
		display: grid;
		gap: 2px;
	}

	/* Each line as tall as the text it stands for, so nothing moves when the list arrives. */
	.heading {
		font: var(--type-title-m);
	}

	.title {
		font: var(--type-body-strong);
		font-size: 0.9375rem;
		line-height: 1.35;
	}

	.meta {
		font: var(--type-meta);
	}

	.lines {
		display: grid;
		gap: 2px;
		padding-block: 4px;
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

	@media (pointer: fine) {
		.row {
			min-block-size: 52px;
			padding-block: 8px;
		}
	}
</style>
