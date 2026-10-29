<script lang="ts" module>
	/** create: choose a PIN to turn the lock on; change: replace it; disable: prove it to turn off. */
	export type PinDialogMode = 'create' | 'change' | 'disable';
</script>

<script lang="ts">
	import { formatTime } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import Dialog from '#lib/ui/Dialog.svelte';
	import { appLock, type UnlockResult } from '../../lock/appLock.svelte.ts';
	import { isValidPin, PIN_MAX, PIN_MIN } from '../../lock/pin.ts';
	import PinField from './PinField.svelte';

	interface Props {
		open: boolean;
		mode: PinDialogMode;
	}

	let { open = $bindable(), mode }: Props = $props();

	const formId = $props.id();

	let current = $state('');
	let next = $state('');
	let confirm = $state('');
	let busy = $state(false);
	let currentError = $state<string>();
	let nextError = $state<string>();
	let confirmError = $state<string>();

	const titles = {
		create: m.settings_pin_create_title,
		change: m.settings_pin_change_title,
		disable: m.settings_pin_disable_title
	};
	const descriptions = {
		create: m.settings_pin_create_description,
		change: m.settings_pin_change_description,
		disable: m.settings_pin_disable_description
	};
	const actions = {
		create: m.settings_pin_create_action,
		change: m.settings_pin_change_action,
		disable: m.settings_pin_disable_action
	};

	const asksCurrent = $derived(mode !== 'create');
	const asksNext = $derived(mode !== 'disable');

	function refused(result: Extract<UnlockResult, { ok: false }>): string {
		if (result.waitUntil > Date.now()) {
			return m.settings_pin_wait({ time: formatTime(result.waitUntil) });
		}
		return result.attemptsLeft > 0
			? m.settings_pin_wrong_attempts({ count: result.attemptsLeft })
			: m.settings_pin_wrong();
	}

	async function submit(event: SubmitEvent) {
		event.preventDefault();
		if (busy) return;
		currentError = nextError = confirmError = undefined;
		if (asksCurrent && current === '') currentError = m.settings_pin_required();
		if (asksNext) {
			if (!isValidPin(next)) {
				nextError = m.settings_pin_invalid({ min: PIN_MIN, max: PIN_MAX });
			} else if (confirm !== next) {
				confirmError = m.settings_pin_mismatch();
			}
		}
		if (currentError || nextError || confirmError) return;

		busy = true;
		try {
			const result =
				mode === 'create'
					? (await appLock.enable(next), { ok: true as const })
					: mode === 'change'
						? await appLock.changePin(current, next)
						: await appLock.disable(current);
			if (result.ok) {
				open = false;
			} else {
				currentError = refused(result);
				current = '';
			}
		} finally {
			busy = false;
		}
	}
</script>

<!-- Its fields start blank once: the opener creates one dialog per opening. -->
<Dialog
	bind:open
	title={titles[mode]()}
	description={descriptions[mode]()}
	size="sm"
	dismissible={!busy}
>
	<form id={formId} class="form" onsubmit={submit} novalidate>
		{#if asksCurrent}
			<PinField
				label={mode === 'change' ? m.settings_pin_current() : m.settings_pin_label()}
				bind:value={current}
				error={currentError}
				autofocus
			/>
		{/if}
		{#if asksNext}
			<PinField
				label={mode === 'change' ? m.settings_pin_new() : m.settings_pin_label()}
				bind:value={next}
				error={nextError}
				description={m.settings_pin_rules({ min: PIN_MIN, max: PIN_MAX })}
				autofocus={!asksCurrent}
			/>
			<PinField label={m.settings_pin_confirm()} bind:value={confirm} error={confirmError} />
		{/if}
	</form>
	{#snippet footer()}
		<Button variant="ghost" disabled={busy} onclick={() => (open = false)}>{m.ui_cancel()}</Button>
		<Button
			variant={mode === 'disable' ? 'danger' : 'solid'}
			type="submit"
			form={formId}
			loading={busy}
		>
			{actions[mode]()}
		</Button>
	{/snippet}
</Dialog>

<style>
	.form {
		display: grid;
		gap: var(--sp-4);
	}
</style>
