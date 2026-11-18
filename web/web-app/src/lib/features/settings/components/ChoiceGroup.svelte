<script lang="ts">
	import type { Snippet } from 'svelte';

	interface Props {
		title: string;
		description?: string;
		/** `ChoiceCard`s of one radio group. */
		children: Snippet;
		/** How many cards share a row. */
		columns?: 2 | 3;
	}

	let { title, description, children, columns = 3 }: Props = $props();

	const id = $props.id();
</script>

<fieldset class="group" aria-describedby={description ? `${id}-description` : undefined}>
	<legend class="legend">
		<h2 class="title">{title}</h2>
	</legend>
	{#if description}<p class="description" id="{id}-description">{description}</p>{/if}
	<div class="cards" style:--columns={columns}>
		{@render children()}
	</div>
</fieldset>

<style>
	.group {
		display: grid;
		gap: 4px;
		min-inline-size: 0;
		margin: 0;
		padding: 0;
		border: 0;
	}

	.legend {
		padding: 0;
	}

	.title {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.description {
		max-inline-size: 60ch;
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	.cards {
		display: grid;
		grid-template-columns: repeat(var(--columns), minmax(0, 1fr));
		gap: 8px;
		margin-block-start: 12px;
		margin-inline: -6px;
	}

	@container page (width >= 600px) {
		.cards {
			gap: 16px;
		}
	}
</style>
