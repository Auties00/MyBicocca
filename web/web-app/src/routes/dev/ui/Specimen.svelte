<script lang="ts">
	import type { Snippet } from 'svelte';
	import SectionHeader from '#lib/ui/SectionHeader.svelte';

	interface Props {
		id: string;
		title: string;
		description?: string;
		children: Snippet;
		/** Lays the children out in a responsive grid of cards. */
		grid?: boolean;
	}

	let { id, title, description, children, grid = false }: Props = $props();
</script>

<section class="specimen" aria-labelledby={id}>
	<SectionHeader {id} {title} {description} />
	<div class={['content', { grid }]}>{@render children()}</div>
</section>

<style>
	.specimen {
		display: grid;
		gap: 16px;
		scroll-margin-block-start: 72px;
	}

	.content {
		display: grid;
		gap: 16px;
		container-type: inline-size;
	}

	.grid {
		grid-template-columns: repeat(auto-fill, minmax(min(100%, 320px), 1fr));
		align-items: start;
	}
</style>
