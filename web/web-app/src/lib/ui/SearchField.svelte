<script lang="ts">
	import type { HTMLInputAttributes } from 'svelte/elements';
	import IconMagnifyingGlass from '~icons/ph/magnifying-glass';
	import IconX from '~icons/ph/x-circle-fill';
	import { m } from '#lib/paraglide/messages.js';
	import Kbd from './Kbd.svelte';

	interface Props extends Omit<
		HTMLInputAttributes,
		'value' | 'size' | 'class' | 'children' | 'type'
	> {
		/** Accessible name (the field has no visible label). */
		label: string;
		value?: string;
		placeholder?: string;
		/** Keycaps shown while the field is empty and unfocused (e.g. ['/'] or ['mod', 'k']). */
		shortcut?: readonly string[];
		/** Enter in the field. */
		onsearch?: (value: string) => void;
		input?: HTMLInputElement;
		size?: 'md' | 'lg';
	}

	let {
		label,
		value = $bindable(''),
		placeholder,
		shortcut,
		onsearch,
		input = $bindable(),
		size = 'md',
		onkeydown,
		...rest
	}: Props = $props();

	function clear() {
		value = '';
		input?.focus();
	}
</script>

<div class={['search', size]} role="search">
	<IconMagnifyingGlass class="glass" aria-hidden="true" />
	<input
		{...rest}
		bind:this={input}
		bind:value
		type="search"
		aria-label={label}
		placeholder={placeholder ?? label}
		autocomplete="off"
		spellcheck={false}
		enterkeyhint="search"
		onkeydown={(e) => {
			onkeydown?.(e);
			if (e.defaultPrevented) return;
			if (e.key === 'Enter') onsearch?.(value);
			if (e.key === 'Escape' && value) {
				e.preventDefault();
				e.stopPropagation();
				value = '';
			}
		}}
	/>
	{#if value}
		<button class="clear" type="button" aria-label={m.ui_clear()} onclick={clear}>
			<IconX aria-hidden="true" />
		</button>
	{:else if shortcut}
		<span class="hint"><Kbd keys={shortcut} /></span>
	{/if}
</div>

<style>
	.search {
		--h: var(--control-h);
		position: relative;
		display: flex;
		align-items: center;
		gap: 8px;
		min-block-size: var(--h);
		padding-inline: 12px 8px;
		border-radius: var(--r-pill);
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline);
		color: var(--text-muted);
		transition: box-shadow var(--dur-fast) var(--ease-out);
	}

	.lg {
		--h: var(--control-h-lg);
		padding-inline-start: 16px;
	}

	@media (hover: hover) {
		.search:hover {
			box-shadow: inset 0 0 0 1px var(--hairline-strong);
			transition-duration: 0s;
		}
	}

	.search:focus-within {
		outline: 2px solid var(--focus);
		outline-offset: -1px;
	}

	.search :global(.glass) {
		inline-size: 18px;
		block-size: 18px;
		flex-shrink: 0;
	}

	input {
		flex: 1;
		min-inline-size: 0;
		block-size: var(--h);
		padding: 0;
		border: 0;
		background: none;
		outline: none;
		color: var(--text);
		font: var(--type-body);
		-webkit-appearance: none;
		appearance: none;
	}

	input::-webkit-search-cancel-button {
		display: none;
	}

	@media (pointer: fine) {
		input {
			font-size: 0.9375rem;
		}
	}

	.clear {
		display: grid;
		place-items: center;
		inline-size: 28px;
		block-size: 28px;
		border-radius: var(--r-pill);
		color: var(--text-subtle);
	}

	.clear :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	@media (hover: hover) {
		.clear:hover {
			color: var(--text-muted);
		}
	}

	.hint {
		display: flex;
		padding-inline-end: 4px;
		pointer-events: none;
	}

	.search:focus-within .hint {
		display: none;
	}
</style>
