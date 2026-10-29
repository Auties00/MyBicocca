<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { HTMLAttributes } from 'svelte/elements';
	import Spinner from './Spinner.svelte';
	import { externalHref, internalHref, type Href, type Icon } from './types.ts';

	type Variant = 'solid' | 'tonal' | 'outline' | 'ghost' | 'danger';

	interface Base extends Omit<HTMLAttributes<HTMLElement>, 'children' | 'onclick' | 'class'> {
		/** solid = the one primary action of a view; tonal (default) = secondary; danger = destructive. */
		variant?: Variant;
		size?: 'sm' | 'md' | 'lg';
		icon?: Icon;
		trailingIcon?: Icon;
		/** Shows a spinner, keeps the width and focus, and ignores clicks. */
		loading?: boolean;
		disabled?: boolean;
		/** Stretches to the container width. */
		full?: boolean;
		/** Renders a link: an in-app path from `resolve()` or an external URL (opens in a new tab). */
		href?: Href;
		type?: 'button' | 'submit' | 'reset';
		form?: string;
		/** Toggle state, exposed as `aria-pressed`. */
		pressed?: boolean;
		onclick?: (event: MouseEvent) => void;
	}

	interface WithText extends Base {
		children: Snippet;
		/** Accessible name override; the visible text is used otherwise. */
		label?: string;
	}

	interface IconOnly extends Base {
		children?: undefined;
		/** Required for icon-only buttons: their only accessible name. */
		label: string;
		icon: Icon;
	}

	type Props = WithText | IconOnly;

	let {
		variant = 'tonal',
		size = 'md',
		icon: LeadingIcon,
		trailingIcon: TrailingIcon,
		loading = false,
		disabled = false,
		full = false,
		href,
		type = 'button',
		form,
		pressed,
		onclick,
		label,
		children,
		...rest
	}: Props = $props();

	const internal = $derived(disabled ? undefined : internalHref(href));
	const external = $derived(disabled ? undefined : externalHref(href));
	const classes = $derived([
		'btn',
		variant,
		size,
		{ 'icon-only': !children, full, loading, pressed }
	]);

	function click(event: MouseEvent) {
		if (loading) {
			event.preventDefault();
			return;
		}
		onclick?.(event);
	}
</script>

{#snippet content()}
	{#if loading}
		<span class="spinner"><Spinner size={size === 'sm' ? 14 : 18} /></span>
	{/if}
	{#if LeadingIcon}<LeadingIcon aria-hidden="true" />{/if}
	{#if children}<span class="text">{@render children()}</span>{/if}
	{#if TrailingIcon}<TrailingIcon aria-hidden="true" />{/if}
{/snippet}

{#if external}
	<a
		{...rest}
		class={classes}
		href={external}
		rel="external noopener noreferrer"
		target="_blank"
		aria-label={label}
		onclick={click}
	>
		{@render content()}
	</a>
{:else if internal}
	<a {...rest} class={classes} href={internal} aria-label={label} onclick={click}>
		{@render content()}
	</a>
{:else}
	<button
		{...rest}
		class={classes}
		{type}
		{form}
		{disabled}
		aria-label={label}
		aria-pressed={pressed}
		aria-busy={loading || undefined}
		aria-disabled={loading || undefined}
		onclick={click}
	>
		{@render content()}
	</button>
{/if}

<style>
	.btn {
		--h: var(--control-h);
		--bg: var(--container);
		--bg-hover: var(--container-hover);
		--bg-press: var(--container-press);
		--fg: var(--on-container);
		--ring: transparent;
		position: relative;
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 8px;
		min-block-size: var(--h);
		min-inline-size: var(--h);
		padding-inline: 16px;
		border-radius: var(--r-sm);
		background-color: var(--bg);
		color: var(--fg);
		box-shadow: inset 0 0 0 1px var(--ring);
		font: var(--type-ui-strong);
		letter-spacing: 0.005em;
		text-decoration: none;
		white-space: nowrap;
		user-select: none;
		touch-action: manipulation;
		transition:
			transform var(--dur-spring) var(--spring-pop),
			background-color var(--dur-fast) var(--ease-out);
	}

	/* Touch targets stay ≥ 44 px even when the visible button is smaller. */
	.btn::after {
		content: '';
		position: absolute;
		inset: min(0px, calc((var(--h) - var(--hit-min)) / 2));
	}

	.btn :global(svg) {
		inline-size: var(--icon);
		block-size: var(--icon);
	}

	.text {
		text-box: trim-both cap alphabetic;
	}

	.btn.full {
		display: flex;
		inline-size: 100%;
	}

	.sm {
		--h: var(--control-h-sm);
		--icon: var(--icon-sm);
		gap: 6px;
		padding-inline: 12px;
		font-size: 0.8125rem;
	}

	.lg {
		--h: var(--control-h-lg);
		padding-inline: 20px;
		font-size: 0.9375rem;
	}

	.icon-only {
		padding-inline: 0;
		border-radius: var(--r-pill);
	}

	.solid {
		--bg: var(--accent);
		--bg-hover: var(--accent-hover);
		--bg-press: var(--accent-hover);
		--fg: var(--on-accent);
	}

	.danger {
		--bg: var(--danger-solid);
		--bg-hover: var(--danger-solid-hover);
		--bg-press: var(--danger-solid-hover);
		--fg: oklch(100% 0 0);
	}

	.outline {
		--bg: transparent;
		--bg-hover: var(--state-hover);
		--bg-press: var(--state-press);
		--fg: var(--text);
		--ring: var(--border);
	}

	.ghost {
		--bg: transparent;
		--bg-hover: var(--state-hover);
		--bg-press: var(--state-press);
		--fg: var(--text);
	}

	.ghost.pressed,
	.outline.pressed {
		--bg: var(--container);
		--bg-hover: var(--container-hover);
		--fg: var(--on-container);
		--ring: transparent;
	}

	@media (hover: hover) {
		.btn:hover:not(:disabled, [aria-disabled='true']) {
			background-color: var(--bg-hover);
			transition-duration: var(--dur-spring), 0s;
		}
	}

	.btn:active:not(:disabled, [aria-disabled='true']) {
		background-color: var(--bg-press);
		transform: scale(0.97);
		transition-duration: var(--dur-instant), 0s;
		transition-timing-function: var(--ease-out);
	}

	.btn:disabled {
		cursor: not-allowed;
		--bg: color-mix(in oklch, var(--text) 6%, transparent);
		--fg: color-mix(in oklch, var(--text) 40%, transparent);
		--ring: transparent;
	}

	.ghost:disabled {
		--bg: transparent;
	}

	.loading {
		cursor: progress;
	}

	/* Text and icons (elements of other components) give way to the spinner; the width stays. */
	.loading > :global(:not(.spinner)) {
		opacity: 0;
	}

	.spinner {
		position: absolute;
		inset: 0;
		display: grid;
		place-items: center;
	}
</style>
