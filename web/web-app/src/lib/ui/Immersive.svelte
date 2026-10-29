<script lang="ts">
	import { getPageChrome, hasPageChrome } from './pageChrome.svelte.ts';

	/* While mounted, the app chrome (top bar, tab bar) steps aside for a full-screen viewer. */
	const chrome = hasPageChrome() ? getPageChrome() : undefined;

	$effect(() => chrome?.holdImmersive());
</script>
