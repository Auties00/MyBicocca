<script lang="ts">
	import IconCaretDown from '~icons/ph/caret-down';
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import { formatDate } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Badge from '#lib/ui/Badge.svelte';
	import type { Release } from '../../releases/releases.ts';
	import ReleaseNotes from './ReleaseNotes.svelte';

	interface Props {
		release: Release;
		latest?: boolean;
		/** Folds into its header, open or not at first; otherwise always open. */
		collapsible?: boolean;
		open?: boolean;
	}

	let { release, latest = false, collapsible = false, open = false }: Props = $props();

	const id = $props.id();
	const published = $derived(
		release.publishedAt === null ? undefined : new Date(release.publishedAt)
	);
</script>

{#snippet head()}
	<span class="titles">
		<h2 class="title" id="{id}-title">{release.title}</h2>
		{#if published}
			<time class="meta" datetime={published.toISOString()}>{formatDate(published)}</time>
		{/if}
	</span>
	{#if latest}<Badge tone="accent">{m.settings_release_latest()}</Badge>{/if}
	{#if collapsible}<span class="caret"><IconCaretDown aria-hidden="true" /></span>{/if}
{/snippet}

{#snippet body()}
	<div class="body">
		{#if release.blocks.length > 0}
			<ReleaseNotes blocks={release.blocks} />
		{:else}
			<p class="empty">{m.settings_release_no_notes()}</p>
		{/if}
		<a class="source" href={release.url} rel="external noopener noreferrer" target="_blank">
			{m.settings_release_on_github()}
			<IconArrowSquareOut aria-hidden="true" />
		</a>
	</div>
{/snippet}

<article class="release" aria-labelledby="{id}-title">
	{#if collapsible}
		<details {open}>
			<summary class="head">{@render head()}</summary>
			{@render body()}
		</details>
	{:else}
		<header class="head">{@render head()}</header>
		{@render body()}
	{/if}
</article>

<style>
	.release {
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
		content-visibility: auto;
		contain-intrinsic-size: auto 96px;
	}

	.head {
		display: flex;
		align-items: center;
		gap: 12px;
		padding: 18px 20px;
	}

	summary.head {
		list-style: none;
		border-radius: var(--r-lg);
		cursor: pointer;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	summary.head::-webkit-details-marker {
		display: none;
	}

	summary.head:focus-visible {
		outline-offset: -2px;
	}

	@media (hover: hover) {
		summary.head:hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	details[open] > summary.head {
		border-end-start-radius: 0;
		border-end-end-radius: 0;
	}

	.titles {
		display: grid;
		flex: 1;
		gap: 2px;
		min-inline-size: 0;
	}

	.title {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.meta {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
		font-variant-numeric: tabular-nums;
	}

	.caret {
		display: flex;
		color: var(--text-subtle);
		transition: transform var(--dur-base) var(--ease-out);
	}

	.caret :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	details[open] .caret {
		transform: rotate(180deg);
	}

	.body {
		display: grid;
		gap: 16px;
		padding: 0 20px 20px;
	}

	.empty {
		color: var(--text-muted);
	}

	.source {
		display: inline-flex;
		align-items: center;
		justify-self: start;
		gap: 4px;
		color: var(--accent-text);
		font: var(--type-ui-strong);
		text-decoration: none;
	}

	.source :global(svg) {
		inline-size: var(--icon-sm);
		block-size: var(--icon-sm);
	}

	@media (hover: hover) {
		.source:hover {
			text-decoration: underline;
			text-underline-offset: 0.18em;
		}
	}
</style>
