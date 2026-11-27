<script lang="ts">
	import type { Snippet } from 'svelte';
	import { m } from '#lib/paraglide/messages.js';
	import ListDetail from '#lib/ui/ListDetail.svelte';
	import ServicesNav from '#lib/features/services/components/ServicesNav.svelte';
	import { provideDeadlines } from '#lib/features/services/deadlines.svelte.ts';
	import { NAV_WIDTH, PAGE_MIN, provideFrame } from '#lib/features/services/frame.svelte.ts';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();

	provideDeadlines();
	const frame = provideFrame();
</script>

<!--
	Where the area is wide enough, the services list stays on the left and every service page opens
	beside it; otherwise the hub is a page of its own (the index page) and each service replaces it.
	The page is always the detail, so crossing the threshold never remounts it.
-->
<div class={['services', { beside: frame.twoPanes }]}>
	<ListDetail
		navigation
		listWidth={NAV_WIDTH}
		detailMin={PAGE_MIN}
		listLabel={m.shell_services()}
		bind:twoPanes={frame.twoPanes}
		detail={children}
	>
		{#snippet list()}
			<!-- Mounted only beside a page, so a phone never loads what the column shows. -->
			{#if frame.twoPanes}
				<ServicesNav />
			{/if}
		{/snippet}
	</ListDetail>
</div>

<style>
	/* Stands in for the shell's main column, which pages lay themselves out in. */
	.services {
		display: flex;
		flex-direction: column;
		flex: 1;
		min-inline-size: 0;
	}

	/* Beside the column, a page's link back to the hub repeats it. */
	.beside :global(header > a.back[href$='/services']) {
		display: none;
	}
</style>
