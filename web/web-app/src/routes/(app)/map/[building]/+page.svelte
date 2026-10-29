<script lang="ts">
	import { page } from '$app/state';
	import { findBuilding } from '#lib/features/map/catalog.ts';
	import { buildingTitle } from '#lib/features/map/names.ts';
	import { m } from '#lib/paraglide/messages.js';

	const building = $derived(findBuilding(page.params.building ?? ''));
</script>

<svelte:head>
	<title>
		{building ? `${buildingTitle(building)} · ` : ''}{m.map_title()} · {m.app_name()}
	</title>
</svelte:head>
