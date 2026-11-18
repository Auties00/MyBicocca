<script lang="ts" module>
	import type { Icon } from '#lib/ui/types.ts';

	export interface OpenOption<T extends string> {
		value: T;
		label: string;
		description: string;
		icon: Icon;
	}
</script>

<script lang="ts" generics="T extends string">
	import IconCheck from '~icons/ph/check-bold';

	interface Props {
		/** Accessible name of the list: "How to open: PDF". */
		label: string;
		options: readonly OpenOption<T>[];
		value: T;
		onchoose: (value: T) => void;
	}

	let { label, options, value, onchoose }: Props = $props();

	function keydown(event: KeyboardEvent) {
		const items = [
			...(event.currentTarget as HTMLElement).querySelectorAll<HTMLElement>(
				'[role="menuitemradio"]'
			)
		];
		const index = items.indexOf(document.activeElement as HTMLElement);
		let next: number | undefined;
		if (event.key === 'ArrowDown') next = (index + 1) % items.length;
		else if (event.key === 'ArrowUp') next = (index - 1 + items.length) % items.length;
		else if (event.key === 'Home') next = 0;
		else if (event.key === 'End') next = items.length - 1;
		if (next !== undefined) {
			event.preventDefault();
			items[next]?.focus();
		}
	}
</script>

<!--
	The phone's chooser, in a bottom sheet: one choice among three, picked and closed in one go as in
	a native menu. Arrows move, Enter or Space picks, and the current choice holds focus first.
-->
<div class="list" role="menu" aria-label={label} tabindex="-1" onkeydown={keydown}>
	{#each options as option (option.value)}
		{@const checked = option.value === value}
		<button
			type="button"
			class={['item', { checked }]}
			role="menuitemradio"
			aria-checked={checked}
			tabindex="-1"
			data-autofocus={checked || undefined}
			onclick={() => onchoose(option.value)}
		>
			<span class="glyph"><option.icon aria-hidden="true" /></span>
			<span class="text">
				<span class="label">{option.label}</span>
				<span class="description">{option.description}</span>
			</span>
			<span class="check"
				>{#if checked}<IconCheck aria-hidden="true" />{/if}</span
			>
		</button>
	{/each}
</div>

<style>
	.list {
		display: grid;
		gap: 2px;
		outline: none;
	}

	.item {
		display: flex;
		align-items: center;
		gap: 12px;
		min-block-size: 64px;
		padding: 10px 14px;
		border-radius: var(--r-md);
		color: var(--text);
		text-align: start;
		cursor: pointer;
		-webkit-tap-highlight-color: transparent;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.item:hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	.item:active {
		background: var(--state-press);
	}

	.item:focus {
		outline: none;
	}

	.item:focus-visible {
		background: var(--state-hover);
		outline: 2px solid var(--focus);
		outline-offset: -2px;
	}

	.glyph,
	.check {
		display: flex;
		flex-shrink: 0;
		color: var(--text-muted);
	}

	.glyph :global(svg) {
		inline-size: var(--icon);
		block-size: var(--icon);
	}

	.check {
		inline-size: var(--icon-sm);
		color: var(--accent-text);
	}

	.check :global(svg) {
		inline-size: var(--icon-sm);
		block-size: var(--icon-sm);
	}

	.checked .glyph,
	.checked .label {
		color: var(--accent-text);
	}

	.text {
		display: grid;
		flex: 1;
		gap: 1px;
		min-inline-size: 0;
	}

	.label {
		font: var(--type-ui-strong);
	}

	.description {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}
</style>
