<script lang="ts">
	import IconQrCode from '~icons/ph/qr-code';
	import IconScan from '~icons/ph/scan';
	import { tick } from 'svelte';
	import { session } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import QrScanner from '#lib/qr/QrScanner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import Dialog from '#lib/ui/Dialog.svelte';
	import TextField from '#lib/ui/TextField.svelte';
	import type { LibraryLink } from '../account.svelte.ts';
	import { checkIn, checkInFailure } from '../writes.ts';

	interface Props {
		link: LibraryLink;
		/** Whether this browser can read a QR code with the camera. */
		canScan: boolean;
		/** Attendance was recorded. */
		ondone: () => void;
	}

	let { link, canScan, ondone }: Props = $props();

	const formId = $props.id();
	let open = $state(false);
	let code = $state('');
	let error = $state<string>();
	let busy = $state(false);
	let scanning = $state(false);
	let input = $state<HTMLInputElement>();

	/** Opens the dialog on an empty form. */
	export function show() {
		code = '';
		error = undefined;
		open = true;
	}

	async function submit(event?: SubmitEvent) {
		event?.preventDefault();
		const scope = session.current;
		if (!scope || busy) return;
		if (code.trim() === '') {
			error = m.library_code_required();
			input?.focus();
			return;
		}
		busy = true;
		error = undefined;
		try {
			await checkIn(scope, link, code);
			open = false;
			ondone();
		} catch (failure) {
			error = checkInFailure(failure);
		} finally {
			busy = false;
		}
		if (error) {
			// Enabled again only once the field has rendered without `disabled`.
			await tick();
			input?.select();
		}
	}

	function scanned(raw: string) {
		scanning = false;
		code = raw.trim();
		void submit();
	}
</script>

<Dialog
	bind:open
	title={m.library_check_in_title()}
	description={m.library_check_in_description()}
	size="sm"
	dismissible={!busy}
>
	<form id={formId} class="form" novalidate onsubmit={submit}>
		<TextField
			label={m.library_code_label()}
			bind:value={code}
			bind:input
			icon={IconQrCode}
			{error}
			autocomplete="one-time-code"
			autocapitalize="characters"
			spellcheck={false}
			disabled={busy}
			data-autofocus
			oninput={() => (error = undefined)}
		/>
		{#if canScan}
			<Button variant="ghost" icon={IconScan} disabled={busy} onclick={() => (scanning = true)}>
				{m.library_scan_qr()}
			</Button>
		{/if}
	</form>
	{#snippet footer()}
		<Button variant="ghost" disabled={busy} onclick={() => (open = false)}>{m.ui_cancel()}</Button>
		<Button type="submit" form={formId} variant="solid" loading={busy}>
			{m.library_check_in_submit()}
		</Button>
	{/snippet}
</Dialog>

{#if scanning}
	<QrScanner
		title={m.library_scanner_title()}
		hint={m.library_scanner_hint()}
		manualLabel={m.library_type_code()}
		onscan={scanned}
		onclose={() => (scanning = false)}
	/>
{/if}

<style>
	.form {
		display: grid;
		justify-items: start;
		gap: var(--sp-3);
	}

	.form > :global(:first-child) {
		justify-self: stretch;
	}
</style>
