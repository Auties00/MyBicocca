<script lang="ts">
	import type { Snippet } from 'svelte';
	import { externalHref, internalHref, type Href } from './types.ts';

	interface Props {
		children: Snippet;
		/** Small heading in the card's top row. */
		title?: string;
		/** Heading level of the title in the page outline. */
		level?: 2 | 3 | 4;
		/** Right side of the top row: a badge, a link, a menu. */
		actions?: Snippet;
		/** A tinted strip at the bottom (e.g. time and room of a booking). */
		footer?: Snippet;
		/** Makes the whole card a link. Don't nest other interactive elements in it. */
		href?: Href;
		/** surface (default) on the page; container = tonal accent fill; sunken = a well; outline = no fill. */
		tone?: 'surface' | 'container' | 'sunken' | 'outline';
		padding?: 'none' | 'sm' | 'md' | 'lg';
		/** Accessible name of the link when the card has no title. */
		label?: string;
	}

	let {
		children,
		title,
		level = 2,
		actions,
		footer,
		href,
		tone = 'surface',
		padding = 'md',
		label
	}: Props = $props();

	const internal = $derived(internalHref(href));
	const external = $derived(externalHref(href));
	const classes = $derived(['card', tone, `pad-${padding}`, { link: !!href }]);
</script>

{#snippet body()}
	{#if title || actions}
		<div class="head">
			{#if title}
				<svelte:element this={`h${level}`} class="title">{title}</svelte:element>
			{/if}
			{#if actions}<div class="actions">{@render actions()}</div>{/if}
		</div>
	{/if}
	<div class="body">{@render children()}</div>
	{#if footer}<div class="footer">{@render footer()}</div>{/if}
{/snippet}

{#if external}
	<a
		class={classes}
		href={external}
		rel="external noopener noreferrer"
		target="_blank"
		aria-label={label}
	>
		{@render body()}
	</a>
{:else if internal}
	<a class={classes} href={internal} aria-label={label}>{@render body()}</a>
{:else}
	<section class={classes} aria-label={title ? undefined : label}>{@render body()}</section>
{/if}

<style>
	.card {
		--pad: 16px;
		position: relative;
		display: flex;
		flex-direction: column;
		min-inline-size: 0;
		border-radius: var(--r-lg);
		background: var(--surface);
		color: var(--text);
		box-shadow: var(--elev-1), var(--inner-light);
		text-decoration: none;
		container-type: inline-size;
		isolation: isolate;
	}

	.container {
		background: var(--container);
		color: var(--on-container);
		box-shadow: var(--inner-light);
	}

	.sunken {
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline);
	}

	.outline {
		background: transparent;
		box-shadow: inset 0 0 0 1px var(--border);
	}

	.pad-none {
		--pad: 0px;
	}

	.pad-sm {
		--pad: 12px;
	}

	.pad-lg {
		--pad: 24px;
	}

	@container (width >= 480px) {
		.pad-md > * {
			--pad: 20px;
		}
	}

	.head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12px;
		padding: var(--pad) var(--pad) 0;
		min-block-size: 24px;
	}

	.title {
		font: var(--type-ui-strong);
		color: var(--text-muted);
	}

	.container .title {
		color: inherit;
		opacity: 0.8;
	}

	.actions {
		display: flex;
		align-items: center;
		gap: 8px;
		margin-block: -4px;
	}

	/* A column its content can grow into (a chart, a Stat tile) when the card is stretched. */
	.body {
		flex: 1;
		display: flex;
		flex-direction: column;
		padding: var(--pad);
	}

	.head + .body {
		padding-block-start: 12px;
	}

	.footer {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12px;
		padding: 10px max(var(--pad), 16px);
		border-radius: 0 0 var(--r-lg) var(--r-lg);
		background: color-mix(in oklch, var(--text) 4%, transparent);
		color: var(--text-muted);
		font: var(--type-ui);
	}

	/* Link cards lift on hover by fading in a stronger shadow (opacity only, no box-shadow animation). */
	.link::before {
		content: '';
		position: absolute;
		inset: 0;
		z-index: -1;
		border-radius: inherit;
		box-shadow: var(--elev-3);
		opacity: 0;
		transition: opacity var(--dur-base) var(--ease-out);
	}

	.link {
		transition: transform var(--dur-spring) var(--spring-pop);
		touch-action: manipulation;
	}

	@media (hover: hover) {
		.link:hover::before {
			opacity: 1;
			transition-duration: var(--dur-fast);
		}
	}

	.link:active {
		transform: scale(0.985);
		transition-duration: var(--dur-instant);
	}

	@supports (corner-shape: squircle) {
		.card {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}

		.footer {
			corner-shape: squircle;
			border-radius: 0 0 calc(var(--r-lg) * 1.5) calc(var(--r-lg) * 1.5);
		}
	}
</style>
