<script lang="ts">
	import type { Snippet } from 'svelte';
	import { page } from '$app/state';
	import QuestionnairesLayout from '#lib/features/questionnaires/components/QuestionnairesLayout.svelte';
	import { routeNumber } from '#lib/features/questionnaires/links.ts';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();
</script>

<!-- The list stays mounted while courses open beside it, keeping its scroll and focus. -->
<QuestionnairesLayout
	selected={routeNumber(page.params.activity)}
	compiling={page.params.unit !== undefined}
>
	{@render children()}
</QuestionnairesLayout>
