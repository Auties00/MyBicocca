<script lang="ts">
	import IconPaperPlaneTilt from '~icons/ph/paper-plane-tilt';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import Checkbox from '#lib/ui/Checkbox.svelte';
	import Dialog from '#lib/ui/Dialog.svelte';
	import Spinner from '#lib/ui/Spinner.svelte';

	interface Props {
		open: boolean;
		title: string;
		/** What happens, including what can't be undone. */
		message: string;
		/** That the hand-in will be late, and until when that is allowed; null on time. */
		late?: string | null;
		confirmLabel: string;
		/** The statement to accept, when the assignment requires one. */
		statement: string | null;
		/** Whether the statement is accepted; the opener clears it. */
		accepted: boolean;
		/** The step under way, while the submission goes; the dialog can't be closed meanwhile. */
		progress: string | null;
		/** Why the last attempt failed, shown in place. */
		error: string | null;
		online: boolean;
		onconfirm: (acceptStatement: boolean) => void;
		onclose: () => void;
	}

	let {
		open = $bindable(),
		title,
		message,
		late = null,
		confirmLabel,
		statement,
		accepted = $bindable(),
		progress,
		error,
		online,
		onconfirm,
		onclose
	}: Props = $props();

	const busy = $derived(progress !== null);
	const ready = $derived((statement === null || accepted) && online && !busy);
</script>

<Dialog bind:open {title} size="sm" role="alertdialog" dismissible={!busy} {onclose}>
	<p class="message">{message}</p>
	{#if late}<p class="late">{late}</p>{/if}
	{#if statement !== null}
		<div class="statement">
			<p class="label">{m.assignment_statement()}</p>
			<Checkbox label={statement} bind:checked={accepted} disabled={busy} />
		</div>
	{/if}
	{#if busy}
		<p class="progress" role="status"><Spinner size={16} />{progress}</p>
	{:else if error}
		<div class="error">
			<Banner tone="danger" title={m.assignment_submit_failed()} message={error} role="alert" />
		</div>
	{:else if !online}
		<p class="hint">{m.assignment_offline_hint()}</p>
	{:else if statement !== null && !accepted}
		<p class="hint">{m.assignment_statement_hint()}</p>
	{/if}
	{#snippet footer()}
		<Button variant="ghost" disabled={busy} onclick={onclose} data-autofocus>
			{m.ui_cancel()}
		</Button>
		<Button
			variant="solid"
			icon={IconPaperPlaneTilt}
			loading={busy}
			disabled={!ready && !busy}
			onclick={() => onconfirm(statement === null || accepted)}
		>
			{confirmLabel}
		</Button>
	{/snippet}
</Dialog>

<style>
	.message {
		color: var(--text-muted);
		font: var(--type-body);
	}

	.late {
		margin-block-start: var(--sp-3);
		color: var(--danger);
		font: var(--type-ui-strong);
	}

	.statement {
		display: grid;
		gap: 8px;
		margin-block-start: var(--sp-4);
		padding: 14px 16px;
		border-radius: var(--r-md);
		background: var(--surface-sunken);
	}

	.label {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.progress {
		display: flex;
		align-items: center;
		gap: 10px;
		margin-block-start: var(--sp-4);
		color: var(--text-muted);
		font: var(--type-ui);
		overflow-wrap: anywhere;
	}

	.error {
		margin-block-start: var(--sp-4);
	}

	.hint {
		margin-block-start: var(--sp-4);
		color: var(--text-muted);
		font: var(--type-meta);
	}
</style>
