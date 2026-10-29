<script lang="ts">
	import { page } from '$app/state';
	import { catalogClient } from '../../catalog/client.ts';
	import CatalogView from './CatalogView.svelte';

	const node = $derived(page.url.searchParams.get('node'));

	// The catalog worker lives while the page does, and a minute more for a quick return.
	$effect(() => catalogClient.use());
</script>

{#key node}
	<CatalogView {node} />
{/key}
