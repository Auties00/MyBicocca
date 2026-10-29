<script lang="ts">
	import IconAt from '~icons/ph/at';
	import IconCheck from '~icons/ph/check-bold';
	import IconEnvelope from '~icons/ph/envelope-simple';
	import IconEnvelopeOpen from '~icons/ph/envelope-simple-open';
	import IconLockOpen from '~icons/ph/lock-simple-open';
	import IconPaperPlane from '~icons/ph/paper-plane-tilt';
	import { goto } from '$app/navigation';
	import type { ResolvedPathname } from '$app/types';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { toDataError } from '#lib/data/errors.ts';
	import { network } from '#lib/data/network.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import Spinner from '#lib/ui/Spinner.svelte';
	import { libraryLinks } from '../account.svelte.ts';
	import { homeHref } from '../links.ts';
	import { pollLink, requestLink } from '../writes.ts';

	interface Props {
		/** Where to go once the email is verified. */
		next?: ResolvedPathname;
	}

	let { next }: Props = $props();

	/** Polled this often while the tab is seen: the student opens the link elsewhere. */
	const POLL_MS = 4_000;
	/** After this, only the button checks: the link is likely forgotten. */
	const POLL_FOR_MS = 10 * 60_000;
	/** A sent link is resumed after a reload while it is this fresh. */
	const REQUEST_TTL_MS = 30 * 60_000;
	const SUCCESS_MS = 1_500;

	interface SentLink {
		uuid: string;
		email: string;
		sentAt: number;
	}

	const storageKey = (accountId: string) => `mybicocca:library-login:${accountId}`;

	function readSent(accountId: string, email: string): SentLink | undefined {
		try {
			const sent = JSON.parse(
				sessionStorage.getItem(storageKey(accountId)) ?? 'null'
			) as SentLink | null;
			return sent && sent.email === email && Date.now() - sent.sentAt < REQUEST_TTL_MS
				? sent
				: undefined;
		} catch {
			return undefined;
		}
	}

	const scope = $derived(session.current);
	const accountId = $derived(scope?.account.id);
	const email = $derived(scope?.account.username ?? '');
	const link = $derived(scope ? libraryLinks.of(scope.account) : undefined);

	let sent = $state<SentLink>();
	let phase = $state<'idle' | 'sending' | 'waiting' | 'done'>('idle');
	let checking = $state(false);
	let notOpened = $state(false);
	let failure = $state<string>();
	let now = $state(Date.now());

	$effect(() => {
		if (accountId && phase === 'idle' && sent === undefined) {
			const resumed = readSent(accountId, email);
			if (resumed) {
				sent = resumed;
				phase = 'waiting';
			}
		}
	});

	const polling = $derived(
		phase === 'waiting' && sent !== undefined && now - sent.sentAt < POLL_FOR_MS
	);

	$effect(() => {
		if (!polling) return;
		const timer = setInterval(() => {
			now = Date.now();
			if (document.visibilityState === 'visible' && navigator.onLine) void check(false);
		}, POLL_MS);
		return () => clearInterval(timer);
	});

	async function send() {
		if (!scope || !email || phase === 'sending') return;
		phase = 'sending';
		failure = undefined;
		notOpened = false;
		try {
			const uuid = await requestLink(scope, email);
			sent = { uuid, email, sentAt: Date.now() };
			now = sent.sentAt;
			try {
				sessionStorage.setItem(storageKey(scope.account.id), JSON.stringify(sent));
			} catch {
				// Private windows may refuse: the link then lasts as long as the page.
			}
			phase = 'waiting';
		} catch (error) {
			failure = `${m.library_send_failed()} ${errorMessage(toDataError(error))}`;
			phase = sent ? 'waiting' : 'idle';
		}
	}

	async function check(manual: boolean) {
		if (!scope || !sent || checking || phase !== 'waiting') return;
		checking = manual;
		if (manual) failure = undefined;
		try {
			const linked = await pollLink(scope, sent.email, sent.uuid);
			if (linked) return succeed(scope.account.id);
			if (manual) notOpened = true;
		} catch (error) {
			if (manual) failure = `${m.library_login_failed()} ${errorMessage(toDataError(error))}`;
		} finally {
			checking = false;
		}
	}

	function succeed(accountId: string) {
		phase = 'done';
		try {
			sessionStorage.removeItem(storageKey(accountId));
		} catch {
			// Storage refused: the request was never kept either.
		}
	}

	/* The success shows a moment, then the page moves on, unless the student already left it. */
	$effect(() => {
		if (phase !== 'done') return;
		const timer = setTimeout(() => void goto(next ?? homeHref(), { replace: true }), SUCCESS_MS);
		return () => clearTimeout(timer);
	});
</script>

<svelte:head>
	<title>{m.library_login_page_title()} · {m.app_name()}</title>
</svelte:head>

<svelte:document
	onvisibilitychange={() => {
		if (document.visibilityState === 'visible' && polling) void check(false);
	}}
/>

<Page width="reading">
	<PageHeader
		title={m.library_login_page_title()}
		back={{ href: homeHref(), label: m.library_title() }}
	/>

	<div class="panel">
		{#if phase === 'done' || (phase === 'idle' && link?.auth && sent === undefined)}
			<div class="outcome" role="status">
				<span class="hero success"><IconCheck aria-hidden="true" /></span>
				<h2 class="headline">{m.library_login_success()}</h2>
				<p class="lead">
					{phase === 'done'
						? m.library_bookings_synced()
						: m.library_linked_as({ email: link?.email ?? email })}
				</p>
				{#if phase !== 'done'}
					<Button variant="solid" href={next ?? homeHref()}>{m.library_title()}</Button>
				{/if}
			</div>
		{:else}
			<span class={['hero', { waiting: phase === 'waiting' }]}>
				{#if phase === 'waiting'}
					<IconEnvelopeOpen aria-hidden="true" />
				{:else}
					<IconLockOpen aria-hidden="true" />
				{/if}
			</span>
			<p class="lead intro">
				{phase === 'waiting' ? m.library_link_sent() : m.library_will_send_link()}
			</p>

			<div class="email">
				<span class="email-label">{m.library_email_label()}</span>
				<span class="email-value">
					<IconAt aria-hidden="true" />
					<span translate="no">{email || '—'}</span>
				</span>
			</div>

			{#if failure}
				<div class="notice"><Banner tone="danger" message={failure} role="alert" /></div>
			{/if}
			{#if !network.online}
				<div class="notice"><Banner tone="warning" message={m.common_error_offline()} /></div>
			{/if}

			{#if phase === 'waiting'}
				<p class="status" role="status">
					{#if notOpened}
						<strong>{m.library_link_not_opened()}.</strong>
						{m.library_open_link_again()}
					{:else if polling}
						<Spinner size={16} />
						{m.library_waiting()}
					{:else}
						{m.library_polling_paused()}
					{/if}
				</p>
				<div class="actions">
					<Button
						variant="solid"
						icon={IconEnvelope}
						loading={checking}
						disabled={!network.online}
						onclick={() => check(true)}
					>
						{m.library_link_opened()}
					</Button>
					<Button variant="ghost" icon={IconPaperPlane} disabled={!network.online} onclick={send}>
						{m.library_resend()}
					</Button>
				</div>
				<p class="fine">{m.library_check_spam()}</p>
			{:else}
				<div class="actions">
					<Button
						variant="solid"
						icon={IconPaperPlane}
						loading={phase === 'sending'}
						disabled={!email || !network.online}
						onclick={send}
					>
						{m.library_send_link()}
					</Button>
				</div>
				<p class="fine">{m.library_no_password()}</p>
			{/if}
		{/if}
	</div>
</Page>

<style>
	.panel {
		display: grid;
		justify-items: center;
		gap: var(--sp-4);
		padding: var(--sp-7) var(--sp-5);
		border-radius: var(--r-xl);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
		text-align: center;
	}

	.outcome {
		display: grid;
		justify-items: center;
		gap: var(--sp-4);
	}

	.hero {
		display: grid;
		place-items: center;
		inline-size: 84px;
		block-size: 84px;
		border-radius: 28px;
		background: var(--container);
		color: var(--on-container);
		box-shadow: var(--inner-light);
	}

	.hero :global(svg) {
		inline-size: 36px;
		block-size: 36px;
	}

	.hero.success {
		background: var(--success-container);
		color: var(--on-success-container);
		animation: pop var(--dur-spring) var(--spring-pop) both;
	}

	.hero.waiting::after {
		content: '';
		position: absolute;
		inset: -6px;
		border-radius: 34px;
		box-shadow: 0 0 0 2px var(--accent);
		opacity: 0;
		animation: breathe 2.4s var(--ease-in-out) infinite;
	}

	.hero.waiting {
		position: relative;
	}

	@keyframes pop {
		from {
			transform: scale(0.6);
			opacity: 0;
		}
	}

	@keyframes breathe {
		50% {
			opacity: 0.5;
			transform: scale(1.04);
		}
	}

	/* The UI face: the page title is the view's serif line. */
	.headline {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
		text-wrap: balance;
	}

	.lead {
		max-inline-size: 46ch;
		color: var(--text-muted);
		text-wrap: pretty;
	}

	.intro {
		color: var(--text);
		font: var(--type-body);
	}

	.email {
		display: grid;
		gap: 4px;
		inline-size: min(100%, 420px);
		padding: var(--sp-3) var(--sp-4);
		border-radius: var(--r-md);
		background: var(--surface-sunken);
		text-align: start;
	}

	.email-label {
		color: var(--text-muted);
		font: var(--type-label);
	}

	.email-value {
		display: flex;
		align-items: center;
		gap: 8px;
		min-inline-size: 0;
		font: var(--type-body-strong);
		overflow-wrap: anywhere;
	}

	.email-value :global(svg) {
		flex-shrink: 0;
		color: var(--text-muted);
	}

	.notice {
		inline-size: min(100%, 420px);
		text-align: start;
	}

	.status {
		display: inline-flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: center;
		gap: 4px 8px;
		max-inline-size: 46ch;
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.status strong {
		color: var(--text);
	}

	.actions {
		display: flex;
		flex-wrap: wrap;
		justify-content: center;
		gap: var(--sp-2);
	}

	.fine {
		max-inline-size: 46ch;
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}

	@media (prefers-reduced-motion: reduce) {
		.hero.success,
		.hero.waiting::after {
			animation: none;
		}
	}
</style>
