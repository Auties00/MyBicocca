<script lang="ts">
	import { on } from 'svelte/events';
	import { expanded } from './media.ts';
	import type { PopoverTriggerProps } from './Popover.svelte';
	import { internalHref, type Href, type Icon } from './types.ts';

	/** The trigger props a `Menu` hands its trigger, for a button that opens one. */
	interface Props extends Partial<Omit<PopoverTriggerProps, 'onclick'>> {
		/** The page's main action; from 1024 px up the page shows it in its header instead. */
		label: string;
		icon: Icon;
		href?: Href;
		onclick?: (event: MouseEvent) => void;
		/** Keyboard shortcut, as `aria-keyshortcuts` spells it. */
		shortcut?: string;
		/** A second action, smaller and icon-only, stacked above the main one. */
		secondary?: boolean;
	}

	let {
		label,
		icon: FabIcon,
		href,
		onclick,
		shortcut,
		secondary = false,
		...trigger
	}: Props = $props();

	const link = $derived(internalHref(href));
	/** Down the page the button folds to its icon, out of the reader's way; back up, it unfolds. */
	let scrolledDown = $state(false);
	const folded = $derived(secondary || scrolledDown);

	function foldOnScroll() {
		let last = window.scrollY;
		return on(
			window,
			'scroll',
			() => {
				const y = window.scrollY;
				if (Math.abs(y - last) < 8) return;
				scrolledDown = y > last && y > 0;
				last = y;
			},
			{ passive: true }
		);
	}
</script>

{#snippet content()}
	<FabIcon aria-hidden="true" />
	<span class="label" aria-hidden={folded}>{label}</span>
{/snippet}

{#if !expanded.current}
	{#if !secondary}
		<!-- Room under the last row, so the button never covers it. -->
		<div class="space" aria-hidden="true"></div>
	{/if}
	{#if link}
		<a
			class={['fab', { folded, secondary }]}
			href={link}
			aria-label={folded ? label : undefined}
			aria-keyshortcuts={shortcut}
			{onclick}
			{@attach secondary ? undefined : foldOnScroll}>{@render content()}</a
		>
	{:else}
		<button
			{...trigger}
			type="button"
			class={['fab', { folded, secondary }]}
			aria-label={folded ? label : undefined}
			aria-keyshortcuts={shortcut}
			{onclick}
			{@attach secondary ? undefined : foldOnScroll}>{@render content()}</button
		>
	{/if}
{/if}

<style>
	.space {
		block-size: var(--control-h-lg);
	}

	.fab {
		--fab-icon: 20px;
		position: fixed;
		inset-inline-end: var(--gutter);
		inset-block-end: calc(var(--shell-inset-bottom) + 12px);
		z-index: var(--z-nav);
		display: inline-flex;
		align-items: center;
		block-size: var(--control-h-lg);
		min-inline-size: var(--control-h-lg);
		padding-inline: calc((var(--control-h-lg) - var(--fab-icon)) / 2) 20px;
		border-radius: var(--r-md);
		background: var(--accent);
		color: var(--on-accent);
		box-shadow: var(--elev-3);
		font: var(--type-ui-strong);
		text-decoration: none;
		white-space: nowrap;
		touch-action: manipulation;
		transition:
			padding var(--dur-base) var(--ease-out),
			background-color var(--dur-fast) var(--ease-out),
			transform var(--dur-spring) var(--spring-pop);
	}

	.fab :global(svg) {
		flex: none;
		inline-size: var(--fab-icon);
		block-size: var(--fab-icon);
	}

	.label {
		overflow: hidden;
		max-inline-size: 16em;
		padding-inline-start: 8px;
		transition:
			max-inline-size var(--dur-base) var(--ease-out),
			padding var(--dur-base) var(--ease-out),
			opacity var(--dur-fast) var(--ease-out);
	}

	.folded {
		padding-inline-end: calc((var(--control-h-lg) - var(--fab-icon)) / 2);
	}

	/* Above the main button, on the same edge: the page's second action. */
	.secondary {
		--fab-size: var(--control-h);
		inset-block-end: calc(var(--shell-inset-bottom) + 12px + var(--control-h-lg) + 12px);
		block-size: var(--fab-size);
		min-inline-size: var(--fab-size);
		padding-inline: calc((var(--fab-size) - var(--fab-icon)) / 2);
		background: var(--container);
		color: var(--on-container);
		box-shadow: var(--elev-2);
	}

	@media (hover: hover) {
		.secondary:hover {
			background: var(--container-hover);
		}
	}

	.folded .label {
		max-inline-size: 0;
		padding-inline-start: 0;
		opacity: 0;
	}

	@media (hover: hover) {
		.fab:hover {
			background: var(--accent-hover);
		}
	}

	.fab:active {
		transform: scale(0.96);
		transition-duration: var(--dur-base), var(--dur-fast), var(--dur-instant);
	}

	.fab:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	@media (prefers-reduced-motion: reduce) {
		.fab,
		.label {
			transition: none;
		}
	}
</style>
