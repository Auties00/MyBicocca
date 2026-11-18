<script lang="ts">
	import type { Snippet } from 'svelte';
	import { page } from '$app/state';
	import RefundsLayout from '#lib/features/payments/components/RefundsLayout.svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();
</script>

<!-- The list stays mounted while refunds open beside it, keeping its scroll and focus. -->
<RefundsLayout selected={page.params.id}>
	{@render children()}
</RefundsLayout>
