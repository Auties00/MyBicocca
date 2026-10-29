<script lang="ts">
	import type { Snippet } from 'svelte';
	import { page } from '$app/state';
	import EnrollmentsLayout from '#lib/features/documents/components/EnrollmentsLayout.svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();
</script>

<!-- The list stays mounted while years open beside it, keeping its scroll and focus. -->
<EnrollmentsLayout year={page.params.year}>
	{@render children()}
</EnrollmentsLayout>
