<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { Icon, Tone } from './types.ts';

	interface Props {
		/** One serif line: what is (not) here. "Nessun esame in programma." */
		title: string;
		/** What happens next, or how to fill it. */
		body?: string;
		/** A duotone glyph (`~icons/ph/<name>-duotone`), kept small. */
		icon?: Icon;
		/** Colour of the glyph: success for a done outcome, neutral for a quiet one. */
		tone?: Tone;
		/** One action, usually a single Button. */
		action?: Snippet;
		/**
		 * page: fills a view; cover: page-sized under a cover or hero, without the page-high
		 * minimum; section: inside a card or list; panel: compact, in the UI face, for a panel
		 * whose own title is already the view's serif line.
		 */
		size?: 'page' | 'cover' | 'section' | 'panel';
	}

	let { title, body, icon: Glyph, tone = 'accent', action, size = 'section' }: Props = $props();
</script>

<div class={['empty', size]}>
	{#if Glyph}<span class={['glyph', tone]}><Glyph aria-hidden="true" /></span>{/if}
	<p class="title">{title}</p>
	{#if body}<p class="body">{body}</p>{/if}
	{#if action}<div class="action">{@render action()}</div>{/if}
</div>

<style>
	.empty {
		display: grid;
		justify-items: center;
		gap: 6px;
		padding: var(--sp-7) var(--sp-4);
		text-align: center;
	}

	.page {
		min-block-size: min(60dvh, 480px);
		align-content: center;
		padding-block: var(--sp-9);
	}

	.cover {
		padding-block: var(--sp-7) var(--sp-8);
	}

	.panel {
		gap: 4px;
		padding: var(--sp-6) var(--sp-4);
	}

	.glyph {
		display: grid;
		margin-block-end: 10px;
		color: var(--accent-text);
	}

	.glyph :global(svg) {
		inline-size: 36px;
		block-size: 36px;
	}

	.page .glyph :global(svg),
	.cover .glyph :global(svg) {
		inline-size: 44px;
		block-size: 44px;
	}

	.panel .glyph {
		margin-block-end: 6px;
	}

	.panel .glyph :global(svg) {
		inline-size: 28px;
		block-size: 28px;
	}

	.neutral {
		color: var(--text-subtle);
	}

	.info {
		color: var(--info);
	}

	.success {
		color: var(--success);
	}

	.warning {
		color: var(--warning);
	}

	.danger {
		color: var(--danger);
	}

	.gold {
		color: var(--gold);
	}

	.title {
		max-inline-size: 28ch;
		font: italic 400 1.375rem / 1.25 var(--font-display);
		letter-spacing: -0.01em;
		text-wrap: balance;
	}

	.page .title,
	.cover .title {
		font-size: clamp(1.5rem, 1.2rem + 1vw, 1.875rem);
	}

	.panel .title {
		max-inline-size: 32ch;
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.body {
		max-inline-size: 44ch;
		color: var(--text-muted);
		font: var(--type-body);
		font-size: 0.9375rem;
		text-wrap: pretty;
	}

	.panel .body {
		max-inline-size: 40ch;
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	.action {
		display: flex;
		flex-wrap: wrap;
		justify-content: center;
		gap: 8px;
		margin-block-start: 14px;
	}

	.panel .action {
		margin-block-start: var(--sp-3);
	}
</style>
