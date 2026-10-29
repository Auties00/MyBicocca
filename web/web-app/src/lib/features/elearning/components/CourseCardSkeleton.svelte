<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';

	interface Props {
		/** Draws a deadline box under the hero, as some cards have. */
		deadlines?: boolean;
	}

	let { deadlines = false }: Props = $props();
</script>

<div class="card" aria-hidden="true">
	<div class="tab"><Skeleton width="64px" height="22px" radius="10px 10px 0 0" /></div>
	<div class={['hero', { joined: deadlines }]}>
		<Skeleton variant="text" width="70%" />
		<Skeleton variant="text" width="45%" />
		<span class="code"><Skeleton variant="text" width="38%" /></span>
	</div>
	{#if deadlines}
		<div class="body">
			<Skeleton variant="text" width="80%" />
		</div>
	{/if}
</div>

<style>
	/* The rows of a course card, the gap below included: the list's grid lines them up. */
	.card {
		display: grid;
		grid-row: span 4;
		grid-template-rows: subgrid;
		row-gap: 0;
	}

	.tab {
		display: flex;
		align-items: flex-end;
		min-block-size: 28px;
		padding-inline: 18px;
	}

	.hero {
		display: grid;
		align-content: start;
		gap: 8px;
		min-block-size: 112px;
		padding: 24px 18px 20px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
		font: var(--type-title-s);
	}

	.hero.joined {
		border-end-start-radius: var(--r-2xs);
		border-end-end-radius: var(--r-2xs);
	}

	.code {
		margin-block-start: 8px;
		font: var(--type-meta);
	}

	.body {
		margin-block-start: 2px;
		padding: 16px 18px;
		border-radius: var(--r-2xs) var(--r-2xs) var(--r-lg) var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
		font: var(--type-ui);
	}
</style>
