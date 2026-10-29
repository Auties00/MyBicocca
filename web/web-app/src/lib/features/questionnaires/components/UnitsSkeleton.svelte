<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';

	interface Props {
		/** The notes before starting follow, as they do while something is left to fill in. */
		facts?: boolean;
	}

	let { facts = true }: Props = $props();
</script>

<div class="skeleton" aria-hidden="true">
	<div class="group">
		<div class="head heading"><Skeleton variant="text" width="9rem" /></div>
		<!-- Most courses have one lecturer to evaluate. -->
		<div class="row">
			<Skeleton variant="circle" width="40px" />
			<div class="lines">
				<span class="title"><Skeleton variant="text" width="62%" /></span>
				<span class="meta"><Skeleton variant="text" width="42%" /></span>
			</div>
			<Skeleton width="76px" height="var(--control-h-sm)" radius="var(--r-sm)" />
		</div>
	</div>
	{#if facts}
		<div class="facts">
			<span class="overline"><Skeleton variant="text" width="7rem" /></span>
			<div class="items">
				{#each [[64, 2], [52, 3], [58, 2]] as [width, lines] (width)}
					<div class="fact">
						<Skeleton variant="circle" width="32px" />
						<div class="fact-lines">
							<span class="strong"><Skeleton variant="text" width="{width}%" /></span>
							<span class="meta"><Skeleton variant="text" width="94%" {lines} /></span>
						</div>
					</div>
				{/each}
			</div>
		</div>
	{/if}
</div>

<style>
	.skeleton {
		display: grid;
		gap: var(--sp-5);
	}

	.group {
		display: grid;
		gap: 2px;
		overflow: clip;
		border-radius: var(--r-lg);
	}

	.head {
		padding: 18px 16px 14px;
		background: var(--surface);
	}

	.row {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr) auto;
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

	/* Each line as tall as the text it stands for, so nothing moves when the units arrive. */
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

	.overline {
		font: var(--type-overline);
	}

	.strong {
		font: var(--type-ui-strong);
	}

	.lines {
		display: grid;
		gap: 2px;
		padding-block: 4px;
	}

	.fact-lines {
		display: grid;
		gap: 1px;
		padding-block-start: 1px;
	}

	.facts {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
	}

	.items {
		display: grid;
		gap: var(--sp-4);
	}

	.fact {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: start;
		gap: var(--sp-3);
	}

	@container (width >= 560px) {
		.items {
			grid-template-columns: repeat(3, minmax(0, 1fr));
		}

		.fact {
			grid-template-columns: minmax(0, 1fr);
			gap: var(--sp-2);
		}
	}
</style>
