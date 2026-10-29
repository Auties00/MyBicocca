<script lang="ts">
	import { page } from '$app/state';
	import CompilePage from '#lib/features/questionnaires/components/CompilePage.svelte';
	import { routeNumber } from '#lib/features/questionnaires/links.ts';

	const activity = $derived(routeNumber(page.params.activity));
	const unit = $derived(routeNumber(page.params.unit));
</script>

<!-- Each unit gets a compilation of its own: moving to another one starts afresh. -->
{#key `${activity}/${unit}`}
	<CompilePage {activity} {unit} />
{/key}
