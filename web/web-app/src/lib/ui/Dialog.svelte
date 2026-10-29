<script lang="ts">
	import type { Snippet } from 'svelte';
	import IconX from '~icons/ph/x';
	import { m } from '#lib/paraglide/messages.js';
	import IconButton from './IconButton.svelte';
	import { modal } from './modal.svelte.ts';

	interface Props {
		open: boolean;
		title: string;
		description?: string;
		children?: Snippet;
		/** Actions, right-aligned (stacked on narrow screens). Put the primary action last. */
		footer?: Snippet;
		size?: 'sm' | 'md' | 'lg';
		/** false while something must not be interrupted: no Escape, no outside click, no close button. */
		dismissible?: boolean;
		/** alertdialog for confirmations that interrupt the user. */
		role?: 'dialog' | 'alertdialog';
		/** The user dismissed it (close button, Escape, outside click); not called when `open` is set to false. */
		onclose?: () => void;
		/** It has closed, whatever closed it (also `open` set to false). */
		onclosed?: () => void;
		/**
		 * Where focus goes once it has closed, if the element that opened it is gone (a deleted
		 * post, an Enrol button replaced by a link) and focus would fall to the page.
		 */
		returnFocus?: () => HTMLElement | null | undefined;
	}

	let {
		open = $bindable(),
		title,
		description,
		children,
		footer,
		size = 'md',
		dismissible = true,
		role = 'dialog',
		onclose,
		onclosed,
		returnFocus
	}: Props = $props();

	const id = $props.id();

	function closed() {
		onclosed?.();
		if (!returnFocus) return;
		/* After the updates the closing set off, which may be what removes the opener. */
		requestAnimationFrame(() => {
			/* Still on a button of the closed (fading) dialog, or already fallen to the page. */
			const active = document.activeElement;
			const kept = active && active !== document.body && !active.closest('dialog:not([open])');
			if (!kept) returnFocus?.()?.focus();
		});
	}
</script>

<dialog
	class={['dialog', size]}
	{role}
	aria-labelledby="{id}-title"
	aria-describedby={description ? `${id}-description` : undefined}
	{@attach modal({
		open: () => open,
		dismissible: () => dismissible,
		close: () => {
			open = false;
			onclose?.();
		},
		closed
	})}
>
	<header class="head">
		<div class="heading">
			<h2 id="{id}-title" class="title">{title}</h2>
			{#if description}<p id="{id}-description" class="description">{description}</p>{/if}
		</div>
		{#if dismissible}
			<IconButton
				icon={IconX}
				label={m.ui_close()}
				size="sm"
				onclick={() => {
					open = false;
					onclose?.();
				}}
			/>
		{/if}
	</header>
	{#if children}<div class="body">{@render children()}</div>{/if}
	{#if footer}<footer class="foot">{@render footer()}</footer>{/if}
</dialog>

<style>
	.dialog {
		--w: 520px;
		position: fixed;
		inset: 0;
		margin: auto;
		inline-size: min(100% - 32px, var(--w));
		block-size: fit-content;
		max-block-size: min(100dvh - 32px, 760px);
		border-radius: var(--r-xl);
		background: var(--surface-raised);
		color: var(--text);
		box-shadow: var(--elev-overlay), var(--inner-light);
		overscroll-behavior: contain;
		opacity: 0;
		transform: translateY(6px) scale(0.97);
		transition:
			opacity 120ms var(--ease-out),
			transform 120ms var(--ease-out),
			overlay 120ms allow-discrete,
			display 120ms allow-discrete;
	}

	.dialog[open] {
		display: flex;
		flex-direction: column;
		opacity: 1;
		transform: none;
		transition-duration: var(--dur-base);
		transition-timing-function: var(--ease-out-expo);
	}

	@starting-style {
		.dialog[open] {
			opacity: 0;
			transform: translateY(6px) scale(0.97);
		}
	}

	.dialog::backdrop {
		background: var(--scrim);
		opacity: 0;
		transition:
			opacity var(--dur-base) var(--ease-out),
			overlay var(--dur-base) allow-discrete,
			display var(--dur-base) allow-discrete;
	}

	.dialog[open]::backdrop {
		opacity: 1;
	}

	@starting-style {
		.dialog[open]::backdrop {
			opacity: 0;
		}
	}

	.sm {
		--w: 420px;
	}

	.lg {
		--w: 720px;
	}

	.head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 12px;
		padding: 22px 16px 0 24px;
	}

	.heading {
		display: grid;
		gap: 6px;
		padding-block-start: 2px;
	}

	.title {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.description {
		color: var(--text-muted);
		font-size: 0.9375rem;
	}

	.body {
		flex: 1;
		min-block-size: 0;
		overflow: auto;
		padding: 16px 24px 24px;
	}

	.foot {
		display: flex;
		flex-wrap: wrap-reverse;
		justify-content: flex-end;
		gap: 8px;
		padding: 0 24px 24px;
	}

	.head:has(+ .foot) {
		padding-block-end: 20px;
	}

	@media (width < 600px) {
		.foot > :global(*) {
			flex: 1 1 auto;
		}
	}
</style>
