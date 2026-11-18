<script lang="ts">
	import type { HTMLInputAttributes } from 'svelte/elements';
	import IconEye from '~icons/ph/eye';
	import IconEyeSlash from '~icons/ph/eye-slash';
	import IconLock from '~icons/ph/lock-simple';
	import { m } from '#lib/paraglide/messages.js';
	import IconButton from './IconButton.svelte';
	import TextField from './TextField.svelte';

	interface Props extends Omit<
		HTMLInputAttributes,
		'value' | 'size' | 'class' | 'children' | 'id' | 'type'
	> {
		label: string;
		value?: string;
		description?: string;
		error?: string;
		id?: string;
		/** new-password for sign-up and change forms. */
		autocomplete?: 'current-password' | 'new-password';
	}

	let { value = $bindable(''), autocomplete = 'current-password', ...rest }: Props = $props();

	let visible = $state(false);
</script>

<TextField
	{...rest}
	bind:value
	type={visible ? 'text' : 'password'}
	icon={IconLock}
	{autocomplete}
	autocapitalize="off"
	spellcheck={false}
>
	{#snippet trailing()}
		<IconButton
			icon={visible ? IconEyeSlash : IconEye}
			label={visible ? m.common_hide_password() : m.common_show_password()}
			size="sm"
			hideTooltip
			onclick={() => (visible = !visible)}
		/>
	{/snippet}
</TextField>
