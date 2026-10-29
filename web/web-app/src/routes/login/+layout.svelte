<script lang="ts">
	import type { Snippet } from 'svelte';
	import { afterNavigate } from '$app/navigation';
	import { setupAuthTransitions } from '#lib/features/auth/authTransitions.ts';
	import AuthFrame from '#lib/features/auth/components/AuthFrame.svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();

	setupAuthTransitions();

	/* After a step, screen readers start from the new page's title unless the page took focus. */
	afterNavigate((navigation) => {
		if (navigation.type === 'enter' || document.activeElement !== document.body) return;
		document.querySelector<HTMLElement>('#main h1')?.focus({ preventScroll: true });
	});
</script>

<AuthFrame>
	{@render children()}
</AuthFrame>
