<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';

	interface Props {
		rows?: number;
		/** The dense preview, which has no week headings. */
		dense?: boolean;
	}

	let { rows = 4, dense = false }: Props = $props();
</script>

<div class={['skeleton', { dense }]} aria-hidden="true">
	{#if !dense}<span class="heading"><Skeleton variant="text" width="7rem" /></span>{/if}
	{#each { length: rows }, index (index)}
		<div class="row">
			<span class="date">
				<Skeleton width={dense ? '26px' : '34px'} height={dense ? '22px' : '28px'} radius="6px" />
				<Skeleton width="24px" height="9px" radius="3px" />
			</span>
			<span class="rail"></span>
			<span class="card">
				<Skeleton variant="text" width="9rem" />
				<Skeleton variant="text" width={index % 2 === 0 ? '70%' : '55%'} />
				<Skeleton variant="text" width="45%" />
			</span>
		</div>
	{/each}
</div>

<style>
	/* Every size mirrors DeadlineRow, so the timeline replaces the skeleton without a shift. */
	.skeleton {
		display: grid;
		animation: appear var(--dur-base) var(--ease-out) 150ms both;
	}

	.heading {
		display: block;
		padding-inline-start: 4px;
		font: var(--type-overline);
		margin-block-end: var(--sp-3);
	}

	.row {
		display: grid;
		grid-template-columns: 52px 24px minmax(0, 1fr);
		column-gap: 10px;
		padding-block-end: 10px;
	}

	.date {
		display: grid;
		justify-items: center;
		align-content: start;
		gap: 6px;
		padding-block-start: 14px;
	}

	.rail {
		justify-self: center;
		inline-size: 3px;
		border-radius: 2px;
		background: var(--hairline);
	}

	.card {
		display: grid;
		gap: 4px;
		padding: 13px 40px 14px 16px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
		font: var(--type-meta);
	}

	.card > :global(:first-child) {
		font: 700 0.6875rem / 1.3 var(--font-ui);
	}

	.card > :global(:nth-child(2)) {
		font: var(--type-title-s);
	}

	.dense .row {
		grid-template-columns: 40px 18px minmax(0, 1fr);
		column-gap: 8px;
		padding-block-end: 8px;
	}

	.dense .date {
		padding-block-start: 10px;
	}

	.dense .card {
		gap: 2px;
		padding: 10px 36px 11px 14px;
		border-radius: var(--r-md);
	}

	.dense .card > :global(:nth-child(2)) {
		font: var(--type-body-strong);
		font-size: 0.9375rem;
		line-height: 1.35;
	}

	@container (width < 360px) {
		.row {
			grid-template-columns: 40px 18px minmax(0, 1fr);
			column-gap: 8px;
		}
	}

	@keyframes appear {
		from {
			opacity: 0;
		}
	}
</style>
