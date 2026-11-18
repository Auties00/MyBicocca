<script lang="ts">
	interface Props {
		title: string;
		/** A count or other short fact on the right. */
		meta?: string;
		id?: string;
		/** Heading level: 3 for groups under a titled list. */
		level?: 2 | 3;
	}

	let { title, meta, id, level = 2 }: Props = $props();
</script>

<div class="head">
	<svelte:element this={`h${level}`} class="title" {id}>{title}</svelte:element>
	{#if meta}<span class="meta">{meta}</span>{/if}
</div>

<style>
	.head {
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		gap: var(--sp-3);
		padding: var(--sp-4) var(--sp-2) var(--sp-2);
	}

	.title {
		font: var(--type-ui-strong);
		font-size: 0.9375rem;
	}

	.meta {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}
</style>
