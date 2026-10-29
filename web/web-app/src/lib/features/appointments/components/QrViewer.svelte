<script lang="ts">
	import IconX from '~icons/ph/x';
	import { m } from '#lib/paraglide/messages.js';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { modal } from '#lib/ui/modal.svelte.ts';

	interface Props {
		open: boolean;
		qrCode: string;
		code: string;
		title: string;
	}

	let { open = $bindable(), qrCode, code, title }: Props = $props();

	const id = $props.id();
</script>

<!--
	The QR as large as the screen allows, black on white in both themes, for the desk's scanner.
	A tap anywhere closes it, as Escape does.
-->
<dialog
	class="viewer appointments-qr"
	aria-labelledby="{id}-title"
	{@attach modal({
		open: () => open,
		dismissible: () => true,
		close: () => (open = false)
	})}
>
	<div class="close">
		<IconButton icon={IconX} label={m.ui_close()} onclick={() => (open = false)} />
	</div>
	<button class="sheet" type="button" onclick={() => (open = false)}>
		<span class="title" id="{id}-title">{title}</span>
		<img src={qrCode} alt={m.appointments_qr_label()} width="320" height="320" />
		<span class="code" translate="no">{code}</span>
		<span class="hint">{m.appointments_qr_hint()}</span>
	</button>
</dialog>

<style>
	.viewer {
		position: fixed;
		inset: 0;
		inline-size: 100%;
		max-inline-size: none;
		block-size: 100%;
		max-block-size: none;
		margin: 0;
		padding: 0;
		border: 0;
		background: #fff;
		color: #1a1414;
		opacity: 0;
		transition:
			opacity var(--dur-base) var(--ease-out),
			overlay var(--dur-base) allow-discrete,
			display var(--dur-base) allow-discrete;
	}

	.viewer[open] {
		display: grid;
		opacity: 1;
	}

	@starting-style {
		.viewer[open] {
			opacity: 0;
		}
	}

	.viewer::backdrop {
		background: #fff;
	}

	/*
	 * The root keeps a gutter for its scrollbar, outside the viewport the sheet covers, painted
	 * with the canvas: white too while the QR is shown, so no band of page shows beside it.
	 */
	:global(:root:has(dialog.appointments-qr[open])) {
		background: #fff;
	}

	.close {
		position: absolute;
		inset-block-start: calc(var(--safe-top) + var(--sp-3));
		inset-inline-end: var(--sp-3);
		z-index: 1;
		color: #1a1414;
		--text: #1a1414;
	}

	.sheet {
		display: grid;
		place-content: center;
		justify-items: center;
		gap: var(--sp-4);
		padding: var(--sp-8) var(--sp-5);
		border: 0;
		background: none;
		color: inherit;
		font: inherit;
		text-align: center;
		cursor: zoom-out;
	}

	.sheet:focus-visible {
		outline: none;
	}

	.title {
		max-inline-size: 30ch;
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
		text-wrap: balance;
	}

	img {
		inline-size: min(78vw, 58dvh, 420px);
		block-size: auto;
		aspect-ratio: 1;
		image-rendering: pixelated;
	}

	.code {
		font: 600 1.75rem / 1.2 var(--font-ui);
		font-variant-numeric: tabular-nums slashed-zero;
		letter-spacing: 0.08em;
	}

	.hint {
		color: #5c5252;
		font: var(--type-meta);
	}

	@media (prefers-reduced-motion: reduce) {
		.viewer {
			transition: none;
		}
	}
</style>
