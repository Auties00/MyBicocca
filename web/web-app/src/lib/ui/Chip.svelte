<script lang="ts">
	import type { Snippet } from 'svelte';
	import { externalHref, internalHref, type Href, type Icon } from './types.ts';

	interface Props {
		children: Snippet;
		icon?: Icon;
		trailingIcon?: Icon;
		/** A link chip (suggestion, shortcut to a page). */
		href?: Href;
		/** An action chip. Without href or onclick the chip is static. */
		onclick?: (event: MouseEvent) => void;
		size?: 'sm' | 'md';
		disabled?: boolean;
	}

	let {
		children,
		icon: LeadingIcon,
		trailingIcon: TrailingIcon,
		href,
		onclick,
		size = 'md',
		disabled = false
	}: Props = $props();

	const internal = $derived(internalHref(href));
	const external = $derived(externalHref(href));
	const classes = $derived(['chip', size, { interactive: !!(href || onclick) }]);
</script>

{#snippet content()}
	{#if LeadingIcon}<LeadingIcon aria-hidden="true" />{/if}
	<span class="text">{@render children()}</span>
	{#if TrailingIcon}<TrailingIcon aria-hidden="true" />{/if}
{/snippet}

{#if external}
	<a class={classes} href={external} rel="external noopener noreferrer" target="_blank">
		{@render content()}
	</a>
{:else if internal}
	<a class={classes} href={internal}>{@render content()}</a>
{:else if onclick}
	<button type="button" class={classes} {onclick} {disabled}>{@render content()}</button>
{:else}
	<span class={classes}>{@render content()}</span>
{/if}

<style>
	.chip {
		--h: 32px;
		position: relative;
		display: inline-flex;
		align-items: center;
		gap: 6px;
		block-size: var(--h);
		padding-inline: 12px;
		border-radius: var(--r-pill);
		background: var(--surface);
		color: var(--text);
		box-shadow: inset 0 0 0 1px var(--border);
		font: var(--type-ui);
		text-decoration: none;
		white-space: nowrap;
		touch-action: manipulation;
		transition:
			transform var(--dur-spring) var(--spring-pop),
			background-color var(--dur-fast) var(--ease-out);
	}

	.chip::after {
		content: '';
		position: absolute;
		inset: min(0px, calc((var(--h) - var(--hit-min)) / 2));
	}

	.sm {
		--h: 26px;
		padding-inline: 10px;
		font-size: 0.8125rem;
	}

	.chip :global(svg) {
		inline-size: var(--icon-sm);
		block-size: var(--icon-sm);
		color: var(--text-muted);
	}

	.text {
		text-box: trim-both cap alphabetic;
	}

	@media (hover: hover) {
		.interactive:hover:not(:disabled) {
			background: var(--surface-hover);
			transition-duration: var(--dur-spring), 0s;
		}
	}

	.interactive:active:not(:disabled) {
		transform: scale(0.96);
		transition-duration: var(--dur-instant), 0s;
	}

	.chip:disabled {
		opacity: 0.45;
		cursor: not-allowed;
	}
</style>
