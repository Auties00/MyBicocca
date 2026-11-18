<script lang="ts">
	import type { Snippet } from 'svelte';
	import { page } from '$app/state';
	import FeesLayout from '#lib/features/payments/components/FeesLayout.svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();
</script>

<!-- The list stays mounted while invoices open beside it, keeping its scroll and focus. -->
<FeesLayout selected={page.params.id}>
	{@render children()}
</FeesLayout>
