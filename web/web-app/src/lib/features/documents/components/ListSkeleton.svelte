<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';

	interface Props {
		/** Row title widths, one per row, in percent. */
		rows: readonly number[];
		/** Lines of each row's title and of its description, as the data will most often fill them. */
		lines?: readonly [number, number];
		/** The renewal card above the rows (enrolments). */
		card?: boolean;
		/** The group's heading, as the first segment of the card (degree categories). */
		titled?: boolean;
	}

	let { rows, lines = [1, 1], card = false, titled = false }: Props = $props();
</script>

<!-- The list's geometry: the renewal card, then ListGroup's connected rows with a chip and text. -->
<div class="skeleton" aria-hidden="true">
	{#if card}
		<div class="card">
			<Skeleton variant="circle" width="24px" />
			<div class="lines">
				<span class="card-title"><Skeleton variant="text" width="75%" /></span>
				<span class="meta"><Skeleton variant="text" lines={2} /></span>
			</div>
		</div>
	{/if}
	<div class="rows">
		{#if titled}
			<div class="group-title"><Skeleton variant="text" width="9rem" /></div>
		{/if}
		{#each rows as width, index (index)}
			<div class="row">
				<Skeleton width="40px" height="40px" radius="12px" />
				<div class="lines">
					<span class="title"><Skeleton variant="text" lines={lines[0]} width="{width}%" /></span>
					<span class="meta"><Skeleton variant="text" lines={lines[1]} width="55%" /></span>
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

	.card {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		gap: var(--sp-3);
		padding: var(--sp-4) var(--sp-5) var(--sp-5) var(--sp-4);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
	}

	.card .lines {
		gap: 4px;
		padding-block: 0;
	}

	.card-title {
		font: var(--type-title-s);
	}

	.lines {
		display: grid;
		gap: 2px;
		padding-block: 4px;
	}

	.title {
		font: var(--type-ui-strong);
	}

	.meta {
		font: var(--type-meta);
	}

	.rows {
		display: grid;
		gap: 2px;
		overflow: clip;
		border-radius: var(--r-lg);
	}

	.group-title {
		padding: 18px 16px 14px;
		background: var(--surface);
		font: var(--type-title-m);
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

	/* A list this wide fits most names and lines on one line, as its rows will. */
	@container documents (width >= 480px) {
		.lines :global(.line + .line) {
			display: none;
		}
	}

	@supports (corner-shape: squircle) {
		.card {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}
</style>
