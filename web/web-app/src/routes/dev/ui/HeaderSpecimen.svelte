<script lang="ts">
	import type { Snippet } from 'svelte';
	import { PageChrome, setPageChrome } from '#lib/ui/pageChrome.svelte.ts';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();

	/* A chrome of its own, so the headers shown here don't retitle the gallery's top bar. */
	const chrome = setPageChrome(new PageChrome());
</script>

<div class="specimen">
	{@render children()}
	<p class="chrome">
		Barra in alto: <code>{chrome.title ?? '—'}</code> · indietro:
		<code>{chrome.back?.label ?? '—'}</code>
	</p>
</div>

<style>
	.specimen {
		display: grid;
		gap: 8px;
		padding: 12px 16px 14px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.chrome {
		padding-block-start: 8px;
		border-block-start: 1px dashed var(--hairline-strong);
		color: var(--text-muted);
		font: var(--type-meta);
	}

	code {
		color: var(--text);
	}
</style>
