<script lang="ts">
	import type { Snippet } from 'svelte';
	import { page } from '$app/state';
	import LibrettoPage from '#lib/features/profile/components/LibrettoPage.svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();

	const selected = $derived(page.params.id === undefined ? undefined : Number(page.params.id));
</script>

{#snippet course()}
	{@render children()}
{/snippet}

<!-- The list lives here, so it keeps its place while courses open beside it or over it. -->
<LibrettoPage {selected} detail={selected === undefined ? undefined : course} />
