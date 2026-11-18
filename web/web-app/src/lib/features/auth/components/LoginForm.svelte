<script lang="ts">
	import { onMount } from 'svelte';
	import IconArrowLeft from '~icons/ph/arrow-left';
	import IconUser from '~icons/ph/user';
	import IconWifiSlash from '~icons/ph/wifi-slash';
	import { signInForm as form } from '#lib/data/signInForm.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import PasswordField from '#lib/ui/PasswordField.svelte';
	import TextField from '#lib/ui/TextField.svelte';
	import type { Href } from '#lib/ui/types.ts';
	import { LoginFlow } from '../loginFlow.svelte.ts';
	import ForgotPassword from './ForgotPassword.svelte';
	import RememberToggle from './RememberToggle.svelte';
	import SignInAlert from './SignInAlert.svelte';

	interface Props {
		/**
		 * Someone is already signed in: the form adds another account, stands alone as the page's
		 * heading, and leads back to the app.
		 */
		back?: { href: Href; onclick: (event: MouseEvent) => void };
	}

	let { back }: Props = $props();

	const id = $props.id();
	const flow = new LoginFlow();

	let online = $state(navigator.onLine);
	let username = $state<HTMLInputElement>();

	const submitDisabled = $derived(!online || (!form.pending && !form.canSubmit));
	const waiting = $derived(
		flow.phase === 'working'
			? m.auth_wait_working()
			: flow.phase === 'slow'
				? m.auth_wait_slow()
				: ''
	);

	function password(): HTMLInputElement | undefined {
		const input = document.getElementById(`${id}-password`);
		return input instanceof HTMLInputElement ? input : undefined;
	}

	/* A mouse and keyboard can start typing at once; on touch the keyboard would cover the page. */
	onMount(() => {
		if (!matchMedia('(pointer: fine)').matches) return;
		if (form.username.trim() === '') username?.focus();
		else password()?.focus();
	});

	/** Enter in the username moves on to the password, as the keyboard's Next key does. */
	function next(event: KeyboardEvent) {
		if (event.key !== 'Enter' || form.password !== '') return;
		event.preventDefault();
		password()?.focus();
	}

	async function submit(event?: SubmitEvent) {
		event?.preventDefault();
		await flow.submit();
		// The form's own state: after a success this component is already gone.
		if (form.error === 'bad_credentials') {
			password()?.focus();
			password()?.select();
		}
	}
</script>

<svelte:window bind:online ononline={flow.reconnected} />

<section class="card" aria-labelledby="{id}-title">
	{#if back}
		<div class="back">
			<Button
				variant="ghost"
				size="sm"
				icon={IconArrowLeft}
				href={back.href}
				onclick={back.onclick}
			>
				{m.auth_back_to_app()}
			</Button>
		</div>
		<header class="head">
			<h1 id="{id}-title" class="title add" tabindex="-1">{m.auth_add_title()}</h1>
			<p class="tagline">{m.auth_add_tagline()}</p>
		</header>
	{:else}
		<h2 id="{id}-title" class="title">{m.auth_tagline()}</h2>
	{/if}

	{#if !online}
		<Banner tone="warning" icon={IconWifiSlash} message={m.auth_offline()} role="status" />
	{/if}

	<form class="form" novalidate aria-busy={form.pending} onsubmit={submit}>
		<TextField
			id="{id}-username"
			label={m.auth_username_label()}
			name="username"
			type="text"
			inputmode="email"
			autocomplete="username"
			autocapitalize="none"
			spellcheck={false}
			enterkeyhint="next"
			placeholder={m.auth_username_placeholder()}
			icon={IconUser}
			readonly={form.pending}
			invalid={form.error === 'bad_credentials'}
			bind:value={form.username}
			bind:input={username}
			oninput={flow.edited}
			onkeydown={next}
		/>

		<div class="password">
			<PasswordField
				id="{id}-password"
				label={m.common_password()}
				name="password"
				enterkeyhint="go"
				readonly={form.pending}
				error={form.error === 'bad_credentials' ? m.auth_error_bad_credentials() : undefined}
				bind:value={form.password}
				oninput={flow.edited}
			/>
			<ForgotPassword />
		</div>

		<RememberToggle bind:checked={form.remember} disabled={form.pending} />

		{#if online}
			<SignInAlert failure={form.error} onretry={() => void submit()} />
		{/if}

		<div class="submit">
			<Button
				type="submit"
				variant="solid"
				size="lg"
				full
				loading={form.pending}
				disabled={submitDisabled}
			>
				{form.pending ? m.auth_signing_in() : m.auth_sign_in_button()}
			</Button>
			<p class="wait" role="status">
				{form.pending ? waiting : ''}
			</p>
		</div>
	</form>
</section>

<style>
	.card {
		display: grid;
		gap: 24px;
		inline-size: 100%;
	}

	@media (width >= 600px) {
		.card {
			padding: 32px;
			border-radius: var(--r-xl);
			background: var(--surface);
			box-shadow: var(--elev-3), var(--inner-light);
		}
	}

	.back {
		margin-block-end: -8px;
		margin-inline-start: -12px;
	}

	.head {
		display: grid;
		gap: 4px;
	}

	.title {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.title.add {
		outline: none;
		font: var(--type-title-l);
		letter-spacing: var(--track-title-l);
	}

	.tagline {
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

	.submit {
		position: relative;
		display: grid;
		margin-block-start: 4px;
	}

	/*
	 * Out of the flow, in the gap under the button: the words that arrive after 3 and 15 seconds
	 * move nothing below. Both fit one line at 360 px.
	 */
	.wait {
		position: absolute;
		inset-block-start: calc(100% + 3px);
		inset-inline: 0;
		color: var(--text-muted);
		font: var(--type-meta);
		text-align: center;
	}
</style>
