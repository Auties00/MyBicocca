<script lang="ts">
	import { MediaQuery } from 'svelte/reactivity';
	import { m } from '#lib/paraglide/messages.js';
	import ServicesHub from '#lib/features/services/components/ServicesHub.svelte';
	import ServicesOverview from '#lib/features/services/components/ServicesOverview.svelte';
	import { useDeadlines } from '#lib/features/services/deadlines.svelte.ts';
	import { useFrame } from '#lib/features/services/frame.svelte.ts';

	const deadlines = useDeadlines();
	const frame = useFrame();
	/* The layout's answer comes with its first layout, before any paint: until then, the window's. */
	const wide = new MediaQuery('(width >= 1376px)', true);
	const twoPanes = $derived(frame?.twoPanes ?? wide.current);
</script>

<svelte:head>
	<title>{m.shell_services()} · {m.app_name()}</title>
</svelte:head>

<!-- Beside the services list the page is an overview; alone, it is the list itself. -->
{#if twoPanes}
	<ServicesOverview {deadlines} />
{:else}
	<ServicesHub {deadlines} />
{/if}
