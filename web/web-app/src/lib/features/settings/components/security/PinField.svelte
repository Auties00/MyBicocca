<script lang="ts">
	import IconLockKey from '~icons/ph/lock-key';
	import { m } from '#lib/paraglide/messages.js';
	import TextField from '#lib/ui/TextField.svelte';
	import { PIN_MAX } from '../../lock/pin.ts';

	interface Props {
		label: string;
		value: string;
		description?: string;
		error?: string;
		/** Takes the focus when its dialog opens. */
		autofocus?: boolean;
		input?: HTMLInputElement;
	}

	let {
		label,
		value = $bindable(''),
		description,
		error,
		autofocus = false,
		input = $bindable()
	}: Props = $props();
</script>

<!-- Digits only, hidden as typed, and kept out of the browser's password manager. -->
<TextField
	{label}
	bind:value
	bind:input
	{description}
	{error}
	icon={IconLockKey}
	type="password"
	inputmode="numeric"
	pattern="[0-9]*"
	maxlength={PIN_MAX}
	autocomplete="off"
	data-1p-ignore
	data-lpignore="true"
	spellcheck={false}
	placeholder={m.settings_pin_placeholder()}
	data-autofocus={autofocus || undefined}
	oninput={(event) => {
		const digits = event.currentTarget.value.replace(/\D/g, '');
		if (digits !== event.currentTarget.value) value = digits;
	}}
/>
