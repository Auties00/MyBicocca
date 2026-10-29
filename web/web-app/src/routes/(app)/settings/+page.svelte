<script lang="ts">
	import { goto } from '$app/navigation';
	import { resolve } from '$app/paths';
	import SettingsDirectory from '#lib/features/settings/components/SettingsDirectory.svelte';
	import { getSettingsLayout } from '#lib/features/settings/layout.ts';

	const layout = getSettingsLayout();

	// Beside the list a section is always open: a window widened to two panes opens the first one.
	$effect(() => {
		if (layout.twoPanes) void goto(resolve('settings/appearance'), { replace: true });
	});
</script>

{#if layout.twoPanes === false}
	<SettingsDirectory />
{/if}
