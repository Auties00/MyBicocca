<script lang="ts">
	import type { Snippet } from 'svelte';
	import IconWarningCircle from '~icons/ph/warning-circle-fill';
	import { m } from '#lib/paraglide/messages.js';
	import Button from './Button.svelte';
	import Dialog from './Dialog.svelte';

	interface Props {
		open: boolean;
		title: string;
		/** What will happen, in plain words, including what can't be undone. */
		message: string;
		/** Under the message: a note field, a choice that goes with the action. */
		children?: Snippet;
		/** Verb for the action ("Annulla prenotazione"), never a bare "OK". */
		confirmLabel: string;
		cancelLabel?: string;
		/** danger for destructive or irreversible actions. */
		tone?: 'default' | 'danger';
		/**
		 * Runs the action. While it runs the dialog can't be dismissed; if it throws, the error is
		 * shown beside the buttons and the dialog stays open so the user can retry or cancel.
		 */
		onconfirm: () => void | Promise<void>;
		/** Turns a thrown error into a sentence for the user. */
		errorMessage?: (error: unknown) => string;
		oncancel?: () => void;
		/** It has closed, confirmed or not. */
		onclosed?: () => void;
		/** Where focus goes once it has closed, if the action removed the element that opened it. */
		returnFocus?: () => HTMLElement | null | undefined;
	}

	let {
		open = $bindable(),
		title,
		message,
		children,
		confirmLabel,
		cancelLabel,
		tone = 'default',
		onconfirm,
		errorMessage,
		oncancel,
		onclosed,
		returnFocus
	}: Props = $props();

	let busy = $state(false);
	let error = $state<string>();

	async function confirm() {
		busy = true;
		error = undefined;
		try {
			await onconfirm();
			open = false;
		} catch (e) {
			error = errorMessage?.(e) ?? m.ui_error_generic();
		} finally {
			busy = false;
		}
	}

	function dismissed() {
		error = undefined;
		oncancel?.();
	}

	function cancel() {
		open = false;
		dismissed();
	}
</script>

<Dialog
	bind:open
	{title}
	size="sm"
	role="alertdialog"
	dismissible={!busy}
	onclose={dismissed}
	{onclosed}
	{returnFocus}
>
	<p class="message">{message}</p>
	{#if children}<div class="extra">{@render children()}</div>{/if}
	{#snippet footer()}
		<!-- The error sits where the eye already is, by the button that failed. -->
		<div class="foot">
			{#if error}
				<p class="error" role="alert">
					<IconWarningCircle aria-hidden="true" />
					<span>{error}</span>
				</p>
			{/if}
			<div class="buttons">
				<Button variant="ghost" onclick={() => !busy && cancel()} data-autofocus>
					{cancelLabel ?? m.ui_cancel()}
				</Button>
				<Button variant={tone === 'danger' ? 'danger' : 'solid'} loading={busy} onclick={confirm}>
					{confirmLabel}
				</Button>
			</div>
		</div>
	{/snippet}
</Dialog>

<style>
	.message {
		color: var(--text-muted);
	}

	.extra {
		display: grid;
		gap: 12px;
		margin-block-start: 16px;
	}

	.foot {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: flex-end;
		gap: 12px 16px;
		inline-size: 100%;
	}

	.error {
		display: flex;
		flex: 1 1 180px;
		align-items: flex-start;
		gap: 6px;
		color: var(--danger);
		font: var(--type-meta);
		font-weight: 500;
	}

	.error :global(svg) {
		flex: none;
		inline-size: 16px;
		block-size: 16px;
		margin-block-start: 1px;
	}

	.buttons {
		display: flex;
		flex-wrap: wrap-reverse;
		justify-content: flex-end;
		gap: 8px;
	}

	@media (width < 600px) {
		.buttons {
			flex: 1 1 100%;
		}

		.buttons > :global(*) {
			flex: 1 1 auto;
		}
	}
</style>
