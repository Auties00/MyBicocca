<script lang="ts">
	import { session } from '#lib/data/session.svelte.ts';
	import { formatTime } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Wordmark from '#lib/ui/Wordmark.svelte';
	import Avatar from '#lib/ui/Avatar.svelte';
	import Button from '#lib/ui/Button.svelte';
	import { appLock } from '../../lock/appLock.svelte.ts';
	import { PIN_MAX } from '../../lock/pin.ts';

	const titleId = 'app-lock-title';
	const length = appLock.config?.pin.length ?? 4;

	let pin = $state('');
	let busy = $state(false);
	let error = $state<string>();
	let shake = $state(0);
	let now = $state(Date.now());
	let forgetting = $state(false);
	let signingOut = $state(false);

	const waitUntil = $derived(appLock.failures.until);
	const waiting = $derived(waitUntil > now);
	const name = $derived(session.active?.displayName);

	// Counts the pause down, then lets the PIN be typed again.
	$effect(() => {
		if (!waiting) return;
		const timer = setInterval(() => (now = Date.now()), 1000);
		return () => clearInterval(timer);
	});

	async function submit(event?: SubmitEvent) {
		event?.preventDefault();
		if (busy || waiting || pin === '') return;
		busy = true;
		const result = await appLock.unlock(pin);
		busy = false;
		if (result.ok) return;
		pin = '';
		shake++;
		now = Date.now();
		error =
			result.waitUntil > now
				? undefined
				: result.attemptsLeft > 0
					? m.settings_pin_wrong_attempts({ count: result.attemptsLeft })
					: m.settings_pin_wrong();
	}

	function typed(event: Event & { currentTarget: HTMLInputElement }) {
		pin = event.currentTarget.value.replace(/\D/g, '').slice(0, PIN_MAX);
		error = undefined;
		if (pin.length === length) void submit();
	}

	/**
	 * Forgetting the PIN costs the accounts it protects: signing in again proves who you are. The
	 * lock goes with the last account (see `AppLock`).
	 */
	async function signOutEverywhere() {
		signingOut = true;
		try {
			for (const account of [...session.accounts]) await session.signOut(account.id);
		} finally {
			signingOut = false;
		}
	}
</script>

<div class="screen" role="group" aria-labelledby={titleId}>
	<div class="panel">
		<span class="logo"><Wordmark variant="mark" size={44} /></span>
		<h1 class="title" id={titleId}>{m.settings_lock_title()}</h1>
		{#if name}
			<p class="who">
				<Avatar {name} size={24} />
				{m.settings_lock_signed_in({ name })}
			</p>
		{/if}

		{#if !forgetting}
			<form class="form" onsubmit={submit}>
				<label class="field">
					<span class="sr-only">{m.settings_pin_label()}</span>
					<!-- Re-created after each wrong PIN, so the shake plays again. -->
					{#key shake}
						<span class={['dots', { shaking: shake > 0 }]} aria-hidden="true">
							{#each { length }, index (index)}
								<span class={['dot', { filled: index < pin.length }]}></span>
							{/each}
						</span>
					{/key}
					<input
						class="input"
						type="password"
						inputmode="numeric"
						pattern="[0-9]*"
						maxlength={PIN_MAX}
						autocomplete="off"
						data-1p-ignore
						data-lpignore="true"
						data-autofocus
						value={pin}
						disabled={waiting}
						aria-invalid={error ? 'true' : undefined}
						aria-describedby="app-lock-message"
						oninput={typed}
					/>
				</label>
				<p class="message" id="app-lock-message" role="status">
					{#if waiting}
						{m.settings_pin_wait({ time: formatTime(waitUntil) })}
					{:else if error}
						{error}
					{:else}
						{m.settings_lock_hint()}
					{/if}
				</p>
				<Button variant="solid" type="submit" full loading={busy} disabled={waiting || pin === ''}>
					{m.settings_lock_unlock()}
				</Button>
			</form>
			<Button variant="ghost" size="sm" onclick={() => (forgetting = true)}>
				{m.settings_lock_forgot()}
			</Button>
		{:else}
			<div class="forget">
				<p>{m.settings_lock_forgot_body()}</p>
				<div class="actions">
					<Button variant="ghost" disabled={signingOut} onclick={() => (forgetting = false)}>
						{m.ui_back()}
					</Button>
					<Button variant="danger" loading={signingOut} onclick={signOutEverywhere}>
						{m.settings_lock_sign_out()}
					</Button>
				</div>
			</div>
		{/if}
	</div>
</div>

<style>
	.screen {
		display: grid;
		place-items: center;
		min-block-size: 100%;
		padding: calc(var(--sp-6) + var(--safe-top)) var(--gutter)
			calc(var(--sp-6) + var(--safe-bottom));
		background: radial-gradient(90% 60% at 50% 0%, var(--container), transparent 70%), var(--bg);
	}

	.panel {
		display: grid;
		justify-items: center;
		gap: var(--sp-3);
		inline-size: min(100%, 360px);
		text-align: center;
	}

	.logo {
		display: flex;
		border-radius: 6px;
		box-shadow: var(--elev-2);
	}

	.title {
		margin-block-start: var(--sp-3);
		font: var(--type-title-l);
		letter-spacing: var(--track-title-l);
		text-wrap: balance;
	}

	.who {
		display: inline-flex;
		align-items: center;
		gap: 8px;
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.form {
		display: grid;
		gap: var(--sp-3);
		inline-size: 100%;
		margin-block-start: var(--sp-5);
	}

	.field {
		position: relative;
		display: grid;
		justify-items: center;
		padding: 18px 12px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
		cursor: text;
	}

	.field:focus-within {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.dots {
		display: flex;
		gap: 14px;
	}

	.shaking {
		animation: shake 360ms var(--ease-out);
	}

	.dot {
		inline-size: 14px;
		block-size: 14px;
		border: 1.5px solid var(--border-strong);
		border-radius: 50%;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			border-color var(--dur-fast) var(--ease-out),
			transform var(--dur-spring) var(--spring-pop);
	}

	.dot.filled {
		border-color: var(--accent);
		background: var(--accent);
		transform: scale(1.1);
	}

	/* The real input covers the dots, invisible: it takes the taps, the keys and the focus. */
	.input {
		position: absolute;
		inset: 0;
		inline-size: 100%;
		block-size: 100%;
		border: 0;
		background: transparent;
		color: transparent;
		caret-color: transparent;
		font-size: 16px;
		opacity: 0;
		cursor: text;
	}

	.message {
		min-block-size: 1lh;
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	.field:has([aria-invalid='true']) + .message {
		color: var(--danger);
	}

	.forget {
		display: grid;
		gap: var(--sp-4);
		margin-block-start: var(--sp-5);
		color: var(--text-muted);
		text-wrap: pretty;
	}

	.actions {
		display: flex;
		justify-content: center;
		gap: 8px;
	}

	@keyframes shake {
		20%,
		60% {
			transform: translateX(-8px);
		}
		40%,
		80% {
			transform: translateX(8px);
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.shaking {
			animation: none;
		}
	}
</style>
