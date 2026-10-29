<script lang="ts">
	import type { Snippet } from 'svelte';
	import { page } from '$app/state';
	import IseeLayout from '#lib/features/payments/components/IseeLayout.svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();
</script>

<!-- The list stays mounted while years open beside it, keeping its scroll and focus. -->
<IseeLayout selected={page.params.year}>
	{@render children()}
</IseeLayout>
