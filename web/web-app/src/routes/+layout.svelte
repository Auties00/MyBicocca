<script lang="ts">
	import '#lib/styles/index.css';
	import { onMount, type Snippet } from 'svelte';
	import { getLocale } from '#lib/paraglide/runtime.js';
	import { registerServiceWorker } from '#lib/data/serviceWorker.ts';
	import { syncThemeColor, theme } from '#lib/ui/theme.svelte.ts';
	import ToastHost from '#lib/ui/ToastHost.svelte';
	import TooltipHost from '#lib/ui/TooltipHost.svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();

	document.documentElement.lang = getLocale();

	/*
	 * Read once the frame is painted and its styles are current: read as the app mounts, the
	 * page's background forces a style recalculation of everything just built.
	 */
	$effect(() => {
		void theme.resolved;
		void theme.palette;
		const frame = requestAnimationFrame(() => setTimeout(syncThemeColor));
		return () => cancelAnimationFrame(frame);
	});

	onMount(registerServiceWorker);
</script>

{@render children()}

<ToastHost />
<TooltipHost />
