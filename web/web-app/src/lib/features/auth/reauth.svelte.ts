import { untrack } from 'svelte';
import type { Account } from '#lib/data/account.ts';
import { accountPrefix } from '#lib/data/keys.ts';
import { invalidate } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import type { SignInFailure } from '#lib/data/signIn.ts';
import { m } from '#lib/paraglide/messages.js';
import { toast } from '#lib/ui/toast.svelte.ts';

/**
 * The prompt that signs the active account in again when its credentials stopped working, in place
 * of the page the student is on: the account, its career and its data all stay. Create it during a
 * component's init: it follows the session from then on.
 */
export class Reauth {
	/** The active account, while its stored credentials can't renew a session. */
	readonly account: Account | undefined = $derived.by(() => {
		const active = session.active;
		const required = session.events.some(
			(event) => event.type === 'reauth_required' && event.accountId === active?.id
		);
		return required ? active : undefined;
	});

	#opened = $state.raw<Account>();

	/** The account the prompt shows, kept while the prompt slides away once it is signed in. */
	readonly shown: Account | undefined = $derived(this.account ?? this.#opened);

	password = $state('');
	remember = $state(false);
	pending = $state(false);
	failure = $state<SignInFailure | null>(null);

	readonly canSubmit: boolean = $derived(
		!this.pending && this.account !== undefined && this.password !== ''
	);

	constructor() {
		const id = $derived(this.account?.id);

		/*
		 * Each time the prompt opens it starts clean, with the switch on the account's own choice:
		 * signing in again with it off would drop a remembered password that only needed replacing.
		 */
		$effect(() => {
			if (id === undefined) return;
			this.#opened = untrack(() => this.account);
			this.password = '';
			this.failure = null;
			let current = true;
			session.remembers(id).then(
				(remembers) => {
					if (current) this.remember = remembers;
				},
				() => undefined
			);
			return () => {
				current = false;
			};
		});
	}

	submit = async (): Promise<void> => {
		const account = this.account;
		if (account === undefined || !this.canSubmit) return;
		this.pending = true;
		this.failure = null;
		try {
			const result = await session.reauthenticate(account.id, this.password, this.remember);
			if (!result.ok) {
				this.failure = result.reason;
				return;
			}
			this.password = '';
			// The queries that failed for want of a session load again.
			invalidate(accountPrefix(account.id));
			toast.success(m.auth_reauth_done());
		} catch {
			this.failure = 'unknown';
		} finally {
			this.pending = false;
		}
	};

	/** Puts it off: the next request that needs a session asks again. */
	later = (): void => {
		const id = this.account?.id;
		for (const event of session.events) {
			if (event.type === 'reauth_required' && event.accountId === id) session.dismissEvent(event);
		}
	};

	edited = (): void => {
		if (this.failure === 'bad_credentials') this.failure = null;
	};

	/** Back online, a failure for want of a connection no longer applies. */
	reconnected = (): void => {
		if (this.failure === 'no_connection') this.failure = null;
	};
}
