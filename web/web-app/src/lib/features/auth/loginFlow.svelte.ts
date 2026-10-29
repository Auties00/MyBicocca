import { signInForm as form } from '#lib/data/signInForm.svelte.ts';
import { m } from '#lib/paraglide/messages.js';
import { toast } from '#lib/ui/toast.svelte.ts';
import { waitPhase, type WaitPhase } from './signInStatus.ts';

/**
 * Tells the student, once in the app, that the Moodle half of the sign-in failed: the session
 * signs in without it and e-learning logs in on first use.
 */
function noticeMissingLearning(): void {
	if (!form.moodleFailed) return;
	toast.show({
		message: m.auth_elearning_failed(),
		description: m.auth_elearning_failed_detail(),
		tone: 'warning',
		duration: 9000
	});
}

/** What the login page adds around the shared sign-in form: wait wording and notices. */
export class LoginFlow {
	/** How long the running sign-in has taken so far, in steps of a second. */
	elapsed = $state(0);

	readonly phase: WaitPhase = $derived(form.pending ? waitPhase(this.elapsed) : 'quick');

	submit = async (): Promise<void> => {
		if (!form.canSubmit) return;
		const started = performance.now();
		const ticker = setInterval(() => (this.elapsed = performance.now() - started), 1000);
		try {
			await form.submit();
		} finally {
			clearInterval(ticker);
			this.elapsed = 0;
		}
		noticeMissingLearning();
	};

	/** Editing either field after refused credentials clears the error, as on Android. */
	edited = (): void => {
		if (form.error === 'bad_credentials') form.error = null;
	};

	/** Back online, a failure for want of a connection no longer applies. */
	reconnected = (): void => {
		if (form.error === 'no_connection') form.error = null;
	};
}
