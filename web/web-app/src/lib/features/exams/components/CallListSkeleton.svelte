<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';

	interface Props {
		/** Whether the list will have its search field. */
		search?: boolean;
	}

	let { search = true }: Props = $props();

	/** Rows per course, as the usual summer session has them. */
	const GROUPS = [2, 2, 1];
</script>

<div class="groups" aria-hidden="true">
	{#if search}<Skeleton height="44px" radius="var(--r-pill)" />{/if}
	{#each GROUPS as rows, index (index)}
		<div class="group">
			<div class="head">
				<span class="title"><Skeleton variant="text" width="62%" /></span>
				<span class="meta"><Skeleton variant="text" width="38%" /></span>
			</div>
			{#each { length: rows }, row (row)}
				<div class="row">
					<Skeleton width="52px" height="60px" radius="15px" />
					<div class="text">
						<span class="title"><Skeleton variant="text" width="48%" /></span>
						<span class="meta"><Skeleton variant="text" width="70%" /></span>
					</div>
				</div>
			{/each}
		</div>
	{/each}
</div>

<style>
	.groups {
		display: grid;
		gap: var(--sp-4);
	}

	.group {
		display: grid;
		gap: 2px;
	}

	.head,
	.row {
		background: var(--surface);
		box-shadow: var(--inner-light);
	}

	.head {
		display: grid;
		gap: 2px;
		padding: 18px 16px 14px;
		border-radius: var(--r-lg) var(--r-lg) var(--r-2xs) var(--r-2xs);
	}

	.head .title {
		font: var(--type-title-m);
	}

	.meta {
		font: var(--type-meta);
	}

	.row {
		display: flex;
		align-items: center;
		gap: 12px;
		min-block-size: 80px;
		padding: 10px 16px;
		border-radius: var(--r-2xs);
	}

	.row:last-child {
		border-radius: var(--r-2xs) var(--r-2xs) var(--r-lg) var(--r-lg);
	}

	.text {
		display: grid;
		gap: 2px;
		flex: 1;
	}

	.text .title {
		font: var(--type-body-strong);
		font-size: 0.9375rem;
	}
</style>
