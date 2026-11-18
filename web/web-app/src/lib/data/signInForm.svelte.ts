import { goto } from '$app/navigation';
import { resolve } from '$app/paths';
import { m } from '#lib/paraglide/messages.js';
import { session } from './session.svelte.ts';
import type { SignInFailure } from './signIn.ts';

/** The login form's state and actions; the page only lays it out. */
class SignInForm {
	username = $state('');
	password = $state('');
	/** Opt-in: the password then stays in this browser, encrypted, for silent re-logins. */
	remember = $state(false);
	showPassword = $state(false);
	pending = $state(false);
	error = $state<SignInFailure | null>(null);
	/** The last sign-in succeeded without its Moodle half: e-learning signs in when first opened. */
	moodleFailed = $state(false);

	readonly canSubmit = $derived(
		!this.pending && this.username.trim() !== '' && this.password !== ''
	);

	readonly errorMessage = $derived.by(() => {
		switch (this.error) {
			case 'bad_credentials':
				return m.auth_error_bad_credentials();
			case 'no_connection':
				return m.auth_error_no_connection();
			case 'timeout':
				return m.auth_error_timeout();
			case 'unknown':
				return m.auth_error_unknown();
			case null:
				return null;
		}
	});

	submit = async (): Promise<void> => {
		if (!this.canSubmit) return;
		this.pending = true;
		this.error = null;
		this.moodleFailed = false;
		try {
			const result = await session.signIn(this.username, this.password, this.remember);
			if (!result.ok) {
				this.error = result.reason;
				return;
			}
			this.password = '';
			this.moodleFailed = result.moodleFailed;
			await goto(resolve(result.needsCareerPick ? 'login/career' : 'calendar'), { replace: true });
		} catch {
			this.error = 'unknown';
		} finally {
			this.pending = false;
		}
	};
}

/**
 * One form for the app, outside any component: a page remounted mid-sign-in (a hot reload, a
 * navigation back) shows the sign-in still running and its outcome, with what was typed.
 */
export const signInForm = new SignInForm();
