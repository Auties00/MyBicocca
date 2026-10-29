<script lang="ts" module>
	import type { Href, Icon } from './types.ts';

	export type MenuEntry =
		| {
				label: string;
				icon?: Icon;
				/** Secondary line under the label. */
				description?: string;
				onselect?: () => void;
				href?: Href;
				tone?: 'default' | 'danger';
				/** Key names as in `Kbd`, shown on the right. */
				shortcut?: readonly string[];
				/** A toggle item; shows a check when true. */
				checked?: boolean;
				/** With `checked`: one choice of several (menuitemradio); the menu opens on it. */
				radio?: boolean;
				disabled?: boolean;
		  }
		| { separator: true }
		| { heading: string };
</script>

<script lang="ts">
	import IconCheck from '~icons/ph/check';
	import Kbd from './Kbd.svelte';
	import Popover, { type PopoverPlacement, type PopoverTriggerProps } from './Popover.svelte';
	import type { Snippet } from 'svelte';
	import { externalHref, internalHref } from './types.ts';

	interface Props {
		trigger: Snippet<[PopoverTriggerProps]>;
		items: readonly MenuEntry[];
		/** Accessible name of the menu. */
		label: string;
		placement?: PopoverPlacement;
		open?: boolean;
		width?: string;
	}

	let {
		trigger,
		items,
		label,
		placement = 'bottom-start',
		open = $bindable(false),
		width
	}: Props = $props();

	function menuItems(root: ParentNode): HTMLElement[] {
		return [
			...root.querySelectorAll<HTMLElement>('[role^="menuitem"]:not([aria-disabled="true"])')
		];
	}

	function keydown(event: KeyboardEvent) {
		const list = event.currentTarget as HTMLElement;
		const all = menuItems(list);
		const i = all.indexOf(document.activeElement as HTMLElement);
		let next: number | undefined;
		if (event.key === 'ArrowDown') next = (i + 1) % all.length;
		else if (event.key === 'ArrowUp') next = (i - 1 + all.length) % all.length;
		else if (event.key === 'Home') next = 0;
		else if (event.key === 'End') next = all.length - 1;
		else if (event.key === 'Tab') open = false;
		else if (event.key.length === 1 && /\S/.test(event.key)) {
			const key = event.key.toLocaleLowerCase();
			const from = all.slice(i + 1).concat(all.slice(0, i + 1));
			const match = from.find((el) => el.textContent?.trim().toLocaleLowerCase().startsWith(key));
			if (match) next = all.indexOf(match);
		}
		if (next !== undefined) {
			event.preventDefault();
			all[next]?.focus();
		}
	}

	function select(entry: Extract<MenuEntry, { label: string }>) {
		open = false;
		entry.onselect?.();
	}
</script>

<Popover
	{trigger}
	bind:open
	{placement}
	{width}
	kind="menu"
	onopen={(surface) =>
		(
			surface.querySelector<HTMLElement>('[role="menuitemradio"][aria-checked="true"]') ??
			menuItems(surface)[0]
		)?.focus()}
>
	<div class="menu" role="menu" aria-label={label} tabindex="-1" onkeydown={keydown}>
		{#each items as entry, i (i)}
			{#if 'separator' in entry}
				<div class="separator" role="separator"></div>
			{:else if 'heading' in entry}
				<div class="heading" role="presentation">{entry.heading}</div>
			{:else}
				{@const internal = internalHref(entry.href)}
				{@const external = externalHref(entry.href)}
				{@const role =
					entry.checked === undefined
						? 'menuitem'
						: entry.radio
							? 'menuitemradio'
							: 'menuitemcheckbox'}
				{@const classes = ['item', { danger: entry.tone === 'danger' }]}
				{#snippet content()}
					{#if entry.checked !== undefined}
						<span class="lead"
							>{#if entry.checked}<IconCheck aria-hidden="true" />{/if}</span
						>
					{:else if entry.icon}
						<span class="lead"><entry.icon aria-hidden="true" /></span>
					{/if}
					<span class="text">
						<span class="label">{entry.label}</span>
						{#if entry.description}<span class="description">{entry.description}</span>{/if}
					</span>
					{#if entry.shortcut}<Kbd keys={entry.shortcut} />{/if}
				{/snippet}
				{#if external && !entry.disabled}
					<a
						class={classes}
						{role}
						tabindex="-1"
						aria-checked={entry.checked}
						href={external}
						rel="external noopener noreferrer"
						target="_blank"
						onclick={() => select(entry)}
					>
						{@render content()}
					</a>
				{:else if internal && !entry.disabled}
					<a
						class={classes}
						{role}
						tabindex="-1"
						aria-checked={entry.checked}
						href={internal}
						onclick={() => select(entry)}
					>
						{@render content()}
					</a>
				{:else}
					<button
						type="button"
						class={classes}
						{role}
						tabindex="-1"
						aria-checked={entry.checked}
						aria-disabled={entry.disabled || undefined}
						onclick={() => !entry.disabled && select(entry)}
					>
						{@render content()}
					</button>
				{/if}
			{/if}
		{/each}
	</div>
</Popover>

<style>
	.menu {
		display: grid;
		gap: 1px;
		outline: none;
	}

	.item {
		display: flex;
		align-items: center;
		gap: 10px;
		min-block-size: var(--control-h);
		padding: 6px 10px;
		border-radius: var(--r-sm);
		color: var(--text);
		font: var(--type-ui);
		text-align: start;
		text-decoration: none;
		cursor: pointer;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	/* Roving focus is shown as the highlighted row, like a native menu. */
	.item:hover,
	.item:focus {
		background: var(--state-hover);
		outline: none;
		transition-duration: 0s;
	}

	.item:focus-visible {
		background: var(--container);
		color: var(--on-container);
	}

	.item[aria-disabled='true'] {
		opacity: 0.45;
		cursor: not-allowed;
	}

	.lead {
		display: grid;
		inline-size: var(--icon);
		color: var(--text-muted);
	}

	.lead :global(svg) {
		inline-size: var(--icon);
		block-size: var(--icon);
	}

	.text {
		flex: 1;
		display: grid;
	}

	.description {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.danger,
	.danger .lead {
		color: var(--danger);
	}

	.separator {
		block-size: 1px;
		margin: 5px 10px;
		background: var(--hairline);
	}

	.heading {
		padding: 10px 10px 4px;
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}
</style>
