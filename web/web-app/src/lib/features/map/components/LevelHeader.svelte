<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { ResolvedPathname } from '$app/types';
	import IconArrowLeft from '~icons/ph/arrow-left';
	import { compact } from '#lib/ui/media.ts';
	import { getPageChrome, hasPageChrome } from '#lib/ui/pageChrome.svelte.ts';

	interface Props {
		title: string;
		subtitle?: string;
		/** The level above; the directory, at the top, has none. */
		back?: { href: ResolvedPathname; label: string };
		/** 2 where another level beside this one holds the page's h1. */
		level?: 1 | 2;
		leading?: Snippet;
		actions?: Snippet;
	}

	let { title, subtitle, back, level = 1, leading, actions }: Props = $props();

	/* A building or a room is a page under the map: on phones the top bar names it and goes Back. */
	const chrome = hasPageChrome() ? getPageChrome() : undefined;
	const heading = chrome?.watchHeading(() => ({ title, back, inBar: compact.current }));
</script>

<header class="header">
	{#if back}
		<a class="back" href={back.href}>
			<IconArrowLeft aria-hidden="true" />
			<span>{back.label}</span>
		</a>
	{/if}
	<div class="identity">
		{#if leading}{@render leading()}{/if}
		<div class="names">
			<svelte:element
				this={`h${level}`}
				class="title"
				tabindex="-1"
				{@attach back && level === 1 ? heading : undefined}>{title}</svelte:element
			>
			{#if subtitle}<p class="subtitle">{subtitle}</p>{/if}
		</div>
	</div>
	{#if actions}<div class="actions">{@render actions()}</div>{/if}
</header>

<style>
	.header {
		display: grid;
		gap: var(--sp-2);
	}

	.back {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		justify-self: start;
		max-inline-size: 100%;
		min-block-size: 32px;
		margin-inline-start: -8px;
		padding: 4px 10px 4px 8px;
		border-radius: var(--r-pill);
		color: var(--text-muted);
		font: var(--type-ui);
		text-decoration: none;
		touch-action: manipulation;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	/* On phones the top bar's Back takes its place. */
	@media (width < 600px) {
		.back {
			display: none;
		}
	}

	.back span {
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.back :global(svg) {
		flex: none;
		inline-size: 16px;
		block-size: 16px;
	}

	@media (hover: hover) {
		.back:hover {
			background: var(--state-hover);
			color: var(--text);
			transition-duration: 0s;
		}
	}

	.back:active {
		background: var(--state-press);
	}

	.identity {
		display: flex;
		align-items: center;
		gap: var(--sp-3);
		min-inline-size: 0;
	}

	/* Mirrors PageHeader's title and subtitle type so the map's panel header reads like every other page's; keep the two in sync. */
	.names {
		display: grid;
		gap: 6px;
		min-inline-size: 0;
	}

	.title {
		font: var(--type-display-m);
		letter-spacing: var(--track-display-m);
		overflow-wrap: anywhere;
		text-wrap: balance;
		outline: none;
	}

	.subtitle {
		color: var(--text-muted);
		font-size: 1rem;
	}

	.actions {
		display: flex;
		flex-wrap: wrap;
		gap: var(--sp-2);
		margin-block-start: var(--sp-1);
	}
</style>
