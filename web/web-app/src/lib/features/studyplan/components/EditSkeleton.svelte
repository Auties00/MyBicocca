<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';

	interface Props {
		/** What is loading, for screen readers. */
		label: string;
		/** The editor's two-pane layout: the step list beside the step. */
		wide: boolean;
		rows?: number;
	}

	let { label, wide, rows = 4 }: Props = $props();
</script>

<div class={['skeleton', { wide }]} role="status" aria-label={label}>
	{#if wide}
		<div class="side">
			{#each { length: 4 }, item (item)}
				<div class="item">
					<Skeleton width="18px" height="18px" radius="50%" />
					<div class="lines">
						<span class="strong"><Skeleton variant="text" width={item % 2 ? '70%' : '85%'} /></span>
						<span class="meta"><Skeleton variant="text" width="5rem" /></span>
					</div>
				</div>
			{/each}
		</div>
	{/if}
	<div class="main">
		<div class="intro">
			<span class="title"><Skeleton variant="text" width="min(14rem, 70%)" /></span>
			<span class="ui"><Skeleton variant="text" width="12rem" /></span>
			<span class="meta"><Skeleton variant="text" width="min(24rem, 90%)" /></span>
		</div>
		<div class="rows">
			{#each { length: rows }, row (row)}
				<div class="row">
					<div class="lines">
						<span class="strong"><Skeleton variant="text" width={row % 2 ? '48%' : '62%'} /></span>
						<span class="meta"><Skeleton variant="text" width="9rem" /></span>
					</div>
					<Skeleton width="24px" height="24px" radius="50%" />
				</div>
			{/each}
		</div>
		<div class="bar"></div>
	</div>
</div>

<style>
	.skeleton {
		display: grid;
		gap: var(--sp-6);
		align-items: start;
	}

	/* The editor's grid, so nothing moves when it arrives. */
	.wide {
		grid-template-columns: clamp(232px, 26%, 280px) minmax(0, 760px);
		gap: var(--sp-8);
	}

	.side {
		display: grid;
		gap: 2px;
	}

	.item {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		gap: 10px;
		padding: 9px 10px;
	}

	.main {
		display: grid;
		gap: var(--sp-6);
	}

	.intro {
		display: grid;
		gap: 8px;
	}

	.title {
		font: var(--type-title-m);
		font-size: 1.375rem;
	}

	.ui {
		font: var(--type-ui);
	}

	.strong {
		font: var(--type-ui-strong);
	}

	.meta {
		font: var(--type-meta);
	}

	.rows {
		display: grid;
		gap: 2px;
	}

	.row {
		display: flex;
		align-items: center;
		gap: 14px;
		min-block-size: 72px;
		padding: 14px 16px;
		border-radius: var(--r-2xs);
		background: var(--surface);
	}

	.row:first-child {
		border-start-start-radius: var(--r-lg);
		border-start-end-radius: var(--r-lg);
	}

	.row:last-child {
		border-end-start-radius: var(--r-lg);
		border-end-end-radius: var(--r-lg);
	}

	.lines {
		flex: 1;
		display: grid;
		gap: 4px;
	}

	.bar {
		block-size: calc(var(--control-h) + 20px);
		border-radius: var(--r-xl);
		background: var(--surface);
	}

	/* The docked bar of phones, as wide as the tab bar under it. */
	@media (width < 600px) {
		.bar {
			block-size: calc(var(--control-h) + 12px);
			margin-inline: -6px;
		}
	}
</style>
