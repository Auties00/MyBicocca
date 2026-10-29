<script lang="ts">
	import type { Snippet } from 'svelte';
	import { page } from '$app/state';
	import DegreesLayout from '#lib/features/documents/components/DegreesLayout.svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();
</script>

<!-- The list stays mounted while titles open beside it, keeping its scroll and focus. -->
<DegreesLayout id={page.params.id}>
	{@render children()}
</DegreesLayout>
