<script lang="ts">
	import IconWifiSlash from '~icons/ph/wifi-slash';
	import { personName } from '#lib/format/names.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Avatar from '#lib/ui/Avatar.svelte';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import Overlay from '#lib/ui/Overlay.svelte';
	import PasswordField from '#lib/ui/PasswordField.svelte';
	import { Reauth } from '../reauth.svelte.ts';
	import ForgotPassword from './ForgotPassword.svelte';
	import RememberToggle from './RememberToggle.svelte';
	import SignInAlert from './SignInAlert.svelte';

	const id = $props.id();
	const reauth = new Reauth();
	/* With a keyboard the password field takes focus; on touch the keyboard would hide the prompt. */
	const finePointer = matchMedia('(pointer: fine)').matches;

	let online = $state(navigator.onLine);

	const account = $derived(reauth.shown);
	/* Esse3 sends names in capitals; a name typed in mixed case is kept as it is. */
	const name = $derived.by(() => {
		const raw = account?.displayName ?? '';
		return raw === raw.toLocaleUpperCase('it') ? personName(raw) : raw;
	});

	async function submit(event?: SubmitEvent) {
		event?.preventDefault();
		await reauth.submit();
		if (reauth.failure === 'bad_credentials') {
			const input = document.getElementById(`${id}-password`);
			if (input instanceof HTMLInputElement) {
				input.focus();
				input.select();
			}
		}
	}
</script>

<svelte:window bind:online ononline={reauth.reconnected} />

<Overlay
	open={reauth.account !== undefined}
	title={m.auth_reauth_title()}
	size="sm"
	dismissible={!reauth.pending}
	onclose={reauth.later}
>
	{#if account}
		<div class="body">
			<div class="who">
				<Avatar {name} size={44} />
				<div class="identity">
					<span class="name">{name}</span>
					<span class="email">{account.username}</span>
				</div>
			</div>

			<p class="message">
				{m.auth_reauth_body()}
			</p>

			<form id="{id}-form" class="form" novalidate onsubmit={submit}>
				<!-- Password managers match the saved password by this username. -->
				<input
					class="sr-only"
					type="email"
					name="username"
					autocomplete="username"
					value={account.username}
					readonly
					tabindex="-1"
					aria-hidden="true"
				/>
				<div class="password">
					<PasswordField
						id="{id}-password"
						label={m.common_password()}
						name="password"
						enterkeyhint="go"
						readonly={reauth.pending}
						data-autofocus={finePointer || undefined}
						error={reauth.failure === 'bad_credentials'
							? m.auth_error_bad_credentials()
							: undefined}
						bind:value={reauth.password}
						oninput={reauth.edited}
					/>
					<ForgotPassword />
				</div>
				<RememberToggle bind:checked={reauth.remember} disabled={reauth.pending} />
				{#if online}
					<SignInAlert failure={reauth.failure} onretry={() => void submit()} />
				{:else}
					<Banner tone="warning" icon={IconWifiSlash} message={m.auth_offline()} role="status" />
				{/if}
			</form>
		</div>
	{/if}

	{#snippet footer()}
		<Button variant="ghost" disabled={reauth.pending} onclick={reauth.later}>
			{m.auth_reauth_later()}
		</Button>
		<Button
			type="submit"
			form="{id}-form"
			variant="solid"
			loading={reauth.pending}
			disabled={!online || (!reauth.pending && !reauth.canSubmit)}
		>
			{m.auth_sign_in_button()}
		</Button>
	{/snippet}
</Overlay>

<style>
	.body {
		display: grid;
		gap: 16px;
	}

	.who {
		display: flex;
		align-items: center;
		gap: 12px;
		padding: 12px;
		border-radius: var(--r-md);
		background: var(--surface-sunken);
	}

	.identity {
		display: grid;
		min-inline-size: 0;
	}

	.name {
		font: var(--type-ui-strong);
	}

	.email {
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.message {
		color: var(--text-muted);
		font: var(--type-ui);
		font-weight: 400;
		text-wrap: pretty;
	}

	.form {
		display: grid;
		gap: 16px;
	}

	.password {
		display: grid;
		gap: 10px;
	}
</style>
