<script lang="ts">
	import { untrack } from 'svelte';
	import type { Attachment } from 'svelte/attachments';
	import { SvelteSet } from 'svelte/reactivity';
	import { m } from '#lib/paraglide/messages.js';
	import { CHUNK_LINES, decodeText, TextLines } from '../../file/text.ts';

	interface Props {
		bytes: Uint8Array;
		name: string;
		wrap: boolean;
		/** 1 is the base size. */
		scale: number;
	}

	let { bytes, name, wrap, scale }: Props = $props();

	const decoded = $derived(decodeText(bytes));
	const text = $derived(new TextLines(decoded.text, name));

	/* The first chunks show at once; the others are built, and coloured, as they near the screen,
	   so a long log costs only what is read. Built chunks stay. */
	const built = $derived(new SvelteSet<number>(text.chunks > 1 ? [0, 1] : [0]));

	let observer = $state.raw<IntersectionObserver>();

	function scroller(element: HTMLElement) {
		const watcher = new IntersectionObserver(
			(entries) => {
				for (const entry of entries) {
					if (!entry.isIntersecting) continue;
					built.add(Number((entry.target as HTMLElement).dataset.chunk));
					watcher.unobserve(entry.target);
				}
			},
			{ root: element, rootMargin: '150% 0px' }
		);
		observer = watcher;
		return () => watcher.disconnect();
	}

	/* Watches an unbuilt chunk again for each new text; a built one stays as it is. */
	function pending(chunk: number): Attachment<HTMLElement> {
		return (element) => {
			const watcher = observer;
			const chunks = built;
			if (watcher === undefined || untrack(() => chunks.has(chunk))) return;
			watcher.observe(element);
			return () => watcher.unobserve(element);
		};
	}
</script>

<!-- A scrolling region takes focus, so the keyboard can scroll it. -->
<!-- svelte-ignore a11y_no_noninteractive_tabindex -->
<div
	class={['text', { wrap }]}
	style:--scale={scale}
	style:--digits={String(text.lines.length).length}
	role="region"
	aria-label={name}
	tabindex="0"
	{@attach scroller}
>
	<pre><code
			>{#each { length: text.chunks }, chunk (chunk)}<span
					class={['chunk', { pending: !built.has(chunk) }]}
					style:--rows={text.rows(chunk)}
					data-chunk={chunk}
					{@attach pending(chunk)}
					>{#if built.has(chunk)}<span class="rows" style:counter-reset="line {chunk * CHUNK_LINES}"
							>{#each text.tokens(chunk) as tokens, row (row)}<span class="line"
									>{#each tokens as token, at (at)}{#if token.kind === 'text'}{token.text}{:else}<span
												class={token.kind}>{token.text}</span
											>{/if}{/each}</span
								>{/each}</span
						>{/if}</span
				>{/each}</code
		></pre>
	{#if decoded.truncated}
		<p class="truncated">{m.course_text_truncated()}</p>
	{/if}
</div>

<style>
	.text {
		block-size: 100%;
		overflow: auto;
		background: var(--surface);
		overscroll-behavior: contain;
	}

	pre {
		margin: 0;
		padding: 16px 20px 96px 0;
		font:
			400 calc(0.8125rem * var(--scale)) / 1.6 ui-monospace,
			'Cascadia Code',
			'SF Mono',
			Menlo,
			Consolas,
			monospace;
		font-variant-ligatures: none;
		font-feature-settings: 'calt' 0;
		tab-size: 4;
	}

	.text:focus-visible {
		outline-offset: -2px;
	}

	code {
		display: block;
		font: inherit;
		inline-size: max-content;
		min-inline-size: 100%;
	}

	.wrap code {
		inline-size: auto;
	}

	.chunk {
		display: block;
		content-visibility: auto;
		contain-intrinsic-block-size: auto calc(var(--rows) * 1lh);
	}

	.chunk.pending {
		block-size: calc(var(--rows) * 1lh);
	}

	/* A chunk's containment hides counters from outside: each starts its own count within. */
	.rows {
		display: block;
	}

	.line {
		display: block;
		padding-inline-start: calc(var(--digits) * 1ch + 36px);
		text-indent: calc(-1 * (var(--digits) * 1ch + 20px));
		white-space: pre;
	}

	.wrap .line {
		white-space: pre-wrap;
		overflow-wrap: anywhere;
	}

	.line::before {
		counter-increment: line;
		content: counter(line);
		display: inline-block;
		inline-size: calc(var(--digits) * 1ch);
		margin-inline-end: 20px;
		color: var(--text-subtle);
		text-align: end;
		text-indent: 0;
		user-select: none;
	}

	.keyword {
		color: var(--accent-text);
		font-weight: 600;
	}

	.string {
		color: var(--success);
	}

	.number {
		color: var(--gold);
	}

	.comment {
		color: var(--text-subtle);
		font-style: italic;
	}

	.truncated {
		padding: 0 20px 96px;
		color: var(--text-muted);
		font: var(--type-meta);
		font-style: italic;
	}
</style>
