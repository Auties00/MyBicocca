<script lang="ts">
	import type { Snippet } from 'svelte';
	import { page } from '$app/state';
	import MapView from '#lib/features/map/components/MapView.svelte';
	import Page from '#lib/ui/Page.svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();
</script>

<Page width="full" bleed fill="viewport">
	<MapView building={page.params.building} room={page.params.room} />
</Page>
{@render children()}
