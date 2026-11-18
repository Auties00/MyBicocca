<script lang="ts">
	import type { Snippet } from 'svelte';

	interface Props {
		/** Rich text to style: rendered markup from a trusted, sanitised source (Moodle HTML after cleaning). */
		children: Snippet;
		size?: 'sm' | 'md';
	}

	let { children, size = 'md' }: Props = $props();
</script>

<div class={['prose', size]}>{@render children()}</div>

<style>
	.prose {
		max-inline-size: 72ch;
		color: var(--text);
		font: var(--type-body);
		overflow-wrap: break-word;
	}

	.sm {
		font-size: 0.9375rem;
	}

	.prose :global(:where(p, ul, ol, blockquote, pre, table, figure) + *) {
		margin-block-start: 0.85em;
	}

	.prose :global(:where(h1, h2, h3, h4)) {
		margin-block: 1.4em 0.4em;
		font: var(--type-title-s);
	}

	.prose :global(:where(h1, h2, h3, h4):first-child) {
		margin-block-start: 0;
	}

	.prose :global(:where(ul, ol)) {
		padding-inline-start: 1.4em;
	}

	.prose :global(li + li) {
		margin-block-start: 0.3em;
	}

	.prose :global(a) {
		color: var(--accent-text);
		font-weight: 550;
	}

	.prose :global(img),
	.prose :global(video) {
		block-size: auto;
		border-radius: var(--r-md);
	}

	.prose :global(blockquote) {
		padding-inline-start: 1em;
		border-inline-start: 3px solid var(--border);
		color: var(--text-muted);
	}

	.prose :global(:where(code, pre)) {
		border-radius: var(--r-xs);
		background: var(--surface-sunken);
		font-size: 0.9em;
	}

	.prose :global(code) {
		padding: 0.1em 0.35em;
	}

	.prose :global(pre) {
		padding: 12px 14px;
		overflow-x: auto;
	}

	.prose :global(table) {
		display: block;
		max-inline-size: 100%;
		overflow-x: auto;
		font-size: 0.9375em;
	}

	.prose :global(:where(th, td)) {
		padding: 6px 10px;
		border-block-end: 1px solid var(--hairline);
		text-align: start;
	}

	.prose :global(hr) {
		margin-block: 1.5em;
		border: 0;
		border-block-start: 1px solid var(--hairline);
	}
</style>
