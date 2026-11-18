<script lang="ts">
	import type { Snippet } from 'svelte';
	import { page } from '$app/state';
	import ResultsLayout from '#lib/features/results/components/ResultsLayout.svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();
</script>

<!-- The list stays mounted while results open beside it, keeping its scroll and focus. -->
<ResultsLayout selected={page.params.id}>
	{@render children()}
</ResultsLayout>
