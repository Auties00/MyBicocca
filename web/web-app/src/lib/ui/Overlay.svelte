<script lang="ts">
	import type { Snippet } from 'svelte';
	import IconX from '~icons/ph/x';
	import IconArrowLeft from '~icons/ph/arrow-left';
	import { m } from '#lib/paraglide/messages.js';
	import IconButton from './IconButton.svelte';
	import { modal } from './modal.svelte.ts';
	import { compact } from './media.ts';

	interface Props {
		open: boolean;
		title: string;
		subtitle?: string;
		children: Snippet;
		/** Sticky actions at the bottom. */
		footer?: Snippet;
		/** Icon buttons next to the close button. */
		actions?: Snippet;
		/** Presentation from 600 px up: a centred dialog, or a pane docked to the right edge. */
		wide?: 'dialog' | 'side';
		size?: 'sm' | 'md' | 'lg';
		/** Shows a back arrow, for a page pushed inside the overlay. */
		onback?: () => void;
		dismissible?: boolean;
		/** The user dismissed it (close button, Escape, outside click, drag down). */
		onclose?: () => void;
	}

	let {
		open = $bindable(),
		title,
		subtitle,
		children,
		footer,
		actions,
		wide = 'dialog',
		size = 'md',
		onback,
		dismissible = true,
		onclose
	}: Props = $props();

	const id = $props.id();

	let sheet = $state<HTMLDialogElement>();
	let drag = $state(0);
	let dragging = $state(false);
	let startY = 0;
	let lastY = 0;
	let lastT = 0;
	let velocity = 0;

	function dismiss() {
		open = false;
		onclose?.();
	}

	function pointerdown(event: PointerEvent) {
		if (!compact.current || !dismissible || event.button !== 0) return;
		if (
			event.target instanceof Element &&
			event.target.closest('button, a, input, select, textarea')
		)
			return;
		dragging = true;
		startY = lastY = event.clientY;
		lastT = event.timeStamp;
		velocity = 0;
		if (event.currentTarget instanceof Element)
			event.currentTarget.setPointerCapture(event.pointerId);
	}

	function pointermove(event: PointerEvent) {
		if (!dragging) return;
		const dy = event.clientY - startY;
		/* Resist upward pulls: the sheet is already at full height. */
		drag = dy > 0 ? dy : -Math.min(24, Math.sqrt(-dy) * 2);
		const dt = event.timeStamp - lastT;
		if (dt > 0) velocity = (event.clientY - lastY) / dt;
		lastY = event.clientY;
		lastT = event.timeStamp;
	}

	function pointerup() {
		if (!dragging) return;
		dragging = false;
		const height = sheet?.offsetHeight ?? 1;
		if (drag > height * 0.3 || (drag > 24 && velocity > 0.5)) dismiss();
		drag = 0;
	}

	/* On phones the header is the sheet's grip: drag it down to dismiss. */
	function draggable(header: HTMLElement) {
		header.addEventListener('pointerdown', pointerdown);
		header.addEventListener('pointermove', pointermove);
		header.addEventListener('pointerup', pointerup);
		header.addEventListener('pointercancel', pointerup);
		return () => {
			header.removeEventListener('pointerdown', pointerdown);
			header.removeEventListener('pointermove', pointermove);
			header.removeEventListener('pointerup', pointerup);
			header.removeEventListener('pointercancel', pointerup);
		};
	}
</script>

<dialog
	bind:this={sheet}
	class={['overlay', `wide-${wide}`, size, { dragging }]}
	aria-labelledby="{id}-title"
	style:--drag="{drag}px"
	{@attach modal({ open: () => open, dismissible: () => dismissible, close: dismiss })}
>
	<header class={['head', { 'has-back': !!onback }]} {@attach draggable}>
		<span class="handle" aria-hidden="true"></span>
		{#if onback}
			<IconButton icon={IconArrowLeft} label={m.ui_back()} onclick={onback} />
		{/if}
		<div class="heading">
			<h2 id="{id}-title" class="title">{title}</h2>
			{#if subtitle}<p class="subtitle">{subtitle}</p>{/if}
		</div>
		<div class="actions">
			{#if actions}{@render actions()}{/if}
			{#if dismissible}
				<IconButton icon={IconX} label={m.ui_close()} onclick={dismiss} />
			{/if}
		</div>
	</header>
	<div class="body">{@render children()}</div>
	{#if footer}<footer class="foot">{@render footer()}</footer>{/if}
</dialog>

<style>
	.overlay {
		--w: 560px;
		position: fixed;
		margin: 0;
		background: var(--surface-raised);
		color: var(--text);
		box-shadow: var(--elev-overlay), var(--inner-light);
		overscroll-behavior: contain;
	}

	.overlay[open] {
		display: flex;
		flex-direction: column;
	}

	.overlay::backdrop {
		background: var(--scrim);
		opacity: 0;
		transition:
			opacity var(--dur-slow) var(--ease-out),
			overlay var(--dur-slow) allow-discrete,
			display var(--dur-slow) allow-discrete;
	}

	.overlay[open]::backdrop {
		opacity: 1;
	}

	@starting-style {
		.overlay[open]::backdrop {
			opacity: 0;
		}
	}

	.sm {
		--w: 420px;
	}

	.lg {
		--w: 760px;
	}

	.head {
		position: relative;
		display: flex;
		align-items: flex-start;
		gap: 8px;
		padding: 18px 12px 12px 24px;
		user-select: none;
	}

	.has-back {
		padding-inline-start: 12px;
	}

	.handle {
		display: none;
		position: absolute;
		inset-block-start: 8px;
		inset-inline-start: 50%;
		inline-size: 36px;
		block-size: 4px;
		margin-inline-start: -18px;
		border-radius: var(--r-pill);
		background: var(--g8);
	}

	.heading {
		flex: 1;
		display: grid;
		gap: 2px;
		min-inline-size: 0;
		padding-block: 6px;
	}

	.title {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.subtitle {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.actions {
		display: flex;
		gap: 4px;
	}

	.body {
		flex: 1;
		min-block-size: 0;
		overflow: auto;
		padding: 4px 24px 24px;
		scrollbar-width: thin;
	}

	.foot {
		display: flex;
		flex-wrap: wrap-reverse;
		justify-content: flex-end;
		gap: 8px;
		padding: 12px 24px 24px;
		box-shadow: 0 -1px 0 var(--hairline);
	}

	/* Compact: a bottom sheet that follows the finger. */
	@media (width < 600px) {
		.overlay {
			inset: auto 0 0;
			inline-size: 100%;
			max-inline-size: 100%;
			max-block-size: calc(100dvh - var(--safe-top) - 24px);
			border-radius: var(--r-xl) var(--r-xl) 0 0;
			transform: translateY(100%);
			transition:
				transform var(--dur-sheet) var(--ease-sheet),
				overlay var(--dur-sheet) allow-discrete,
				display var(--dur-sheet) allow-discrete;
		}

		.overlay[open] {
			transform: translateY(var(--drag));
		}

		@starting-style {
			.overlay[open] {
				transform: translateY(100%);
			}
		}

		.dragging {
			transition: none;
		}

		.head {
			padding-block-start: 22px;
			touch-action: none;
			cursor: grab;
		}

		.dragging .head {
			cursor: grabbing;
		}

		.handle {
			display: block;
		}

		.overlay:not(:has(.foot)) .body,
		.foot {
			padding-block-end: calc(16px + var(--safe-bottom));
		}
	}

	@media (width >= 600px) {
		.wide-dialog {
			inset: 0;
			margin: auto;
			inline-size: min(100% - 48px, var(--w));
			block-size: fit-content;
			max-block-size: min(100dvh - 48px, 800px);
			border-radius: var(--r-xl);
			opacity: 0;
			transform: translateY(8px) scale(0.97);
			transition:
				opacity 140ms var(--ease-out),
				transform 140ms var(--ease-out),
				overlay 140ms allow-discrete,
				display 140ms allow-discrete;
		}

		.wide-dialog[open] {
			opacity: 1;
			transform: none;
			transition-duration: var(--dur-slow);
			transition-timing-function: var(--ease-out-expo);
		}

		@starting-style {
			.wide-dialog[open] {
				opacity: 0;
				transform: translateY(8px) scale(0.97);
			}
		}

		.wide-side {
			--w: 440px;
			inset: 8px 8px 8px auto;
			inline-size: min(100% - 16px, var(--w));
			block-size: auto;
			max-block-size: none;
			border-radius: var(--r-xl);
			transform: translateX(calc(100% + 16px));
			transition:
				transform var(--dur-sheet) var(--ease-sheet),
				overlay var(--dur-sheet) allow-discrete,
				display var(--dur-sheet) allow-discrete;
		}

		.wide-side.lg {
			--w: 560px;
		}

		.wide-side[open] {
			transform: none;
		}

		@starting-style {
			.wide-side[open] {
				transform: translateX(calc(100% + 16px));
			}
		}
	}

	@media (width < 600px) {
		.body {
			padding-inline: 16px;
		}

		.head:not(.has-back) {
			padding-inline-start: 16px;
		}

		.foot {
			padding-inline: 16px;
		}

		.foot > :global(*) {
			flex: 1 1 auto;
		}
	}
</style>
