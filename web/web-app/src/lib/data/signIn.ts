import { MoodleTokenAuth, createMoodleApi } from '@mybicocca/data-api/elearning';
import { createEsse3Api, type Esse3User } from '@mybicocca/data-api/esse3';
import {
	Esse3Token,
	refusesCredentials,
	withEsse3Auth,
	type Esse3Jwt
} from '#lib/api/auth/esse3Token.ts';
import type { MoodleSignIn } from '#lib/api/auth/moodleLogin.ts';
import { CookieJar } from '#lib/api/cookies.ts';
import { LoginError } from '#lib/api/errors.ts';
import type { Transport } from '#lib/api/transport.ts';
import { getLocale } from '#lib/paraglide/runtime.js';
import { learningIdentity, toCareers, type Career, type LearningIdentity } from './account.ts';
import { toDataError } from './errors.ts';

/** `timeout`: the servers took too long, as opposed to a network that is down (`no_connection`). */
export type SignInFailure = 'bad_credentials' | 'no_connection' | 'timeout' | 'unknown';

/** The longest the Esse3 half of a sign-in may take, whatever the servers do. */
const ESSE3_DEADLINE_MS = 90_000;
/** The longest the Moodle half may take: about a dozen relayed hops. */
const MOODLE_DEADLINE_MS = 60_000;

/** A Moodle sign-in: the tokens, the session cookies for the Moodle pages, the user. */
export interface MoodleCredentials extends MoodleSignIn {
	learning: LearningIdentity;
}

/** What signing in yields, before anything is stored. */
export interface Credentials {
	user: Esse3User;
	careers: Career[];
	esse3Jwt: Esse3Jwt | undefined;
	/** Missing when the Moodle half failed: e-learning then logs in on first use. */
	moodle: MoodleCredentials | undefined;
}

/**
 * Signs into Esse3 and Moodle in parallel, each within a deadline so a sign-in always ends.
 * Esse3 must succeed: it checks the password and lists the careers, the account's identity. Its
 * JWT is minted with the password, then the user, careers and (best effort) degree levels are read
 * with it. Moodle's SSO chain is the fragile part, so its failure only leaves e-learning to log in
 * on first use: the rest of the app works without it.
 */
export async function authenticate(
	transport: Transport,
	username: string,
	password: string
): Promise<{ ok: true; credentials: Credentials } | { ok: false; reason: SignInFailure }> {
	const esse3Deadline = AbortSignal.timeout(ESSE3_DEADLINE_MS);
	const moodleDeadline = AbortSignal.timeout(MOODLE_DEADLINE_MS);
	const send = transport.fetch({ jar: new CookieJar(), timeoutMs: 60_000, signal: esse3Deadline });
	const token = new Esse3Token({ send, credentials: async () => ({ username, password }) });
	const esse3 = createEsse3Api({ fetch: withEsse3Auth(send, token) });

	const [academic, learning] = await Promise.allSettled([
		(async () => {
			await token.authorization();
			const [session, careers, segments] = await Promise.all([
				esse3.auth.login(),
				esse3.careers.getCareers(),
				esse3.transcript.getCareerSegments({ optionalFields: 'tipoCorsoCod' }).catch(() => [])
			]);
			return { user: session.user, careers: toCareers(careers, segments) };
		})(),
		(async (): Promise<MoodleCredentials> => {
			const signIn = await transport.moodleSignIn(username, password, moodleDeadline);
			const api = createMoodleApi(
				{ fetch: transport.fetch({ timeoutMs: 30_000, signal: moodleDeadline }) },
				getLocale() === 'it' ? 'it' : 'en'
			);
			const site = await api.site.getSiteInfo(new MoodleTokenAuth(signIn.tokens.wsToken));
			return { ...signIn, learning: learningIdentity(site) };
		})()
	]);

	if (academic.status === 'rejected') {
		const errors = [academic.reason, ...(learning.status === 'rejected' ? [learning.reason] : [])];
		return { ok: false, reason: classifySignInFailure(errors) };
	}
	if (academic.value.careers.length === 0) return { ok: false, reason: 'unknown' };
	return {
		ok: true,
		credentials: {
			user: academic.value.user,
			careers: academic.value.careers,
			esse3Jwt: token.current,
			moodle: learning.status === 'fulfilled' ? learning.value : undefined
		}
	};
}

/**
 * Why a sign-in failed, as the login page says it: refused credentials first (an Esse3 401 or a
 * message about "credenziali", an IdP without SAML response), then a device offline, servers too
 * slow, servers unreachable or down, else unknown.
 */
export function classifySignInFailure(errors: readonly unknown[]): SignInFailure {
	const refused = errors.some(
		(error) => refusesCredentials(error) || (error instanceof LoginError && error.badCredentials)
	);
	if (refused) return 'bad_credentials';
	// A login chain that broke is no connectivity problem: retrying may not help.
	const kinds = new Set(
		errors.flatMap((error) => (error instanceof LoginError ? [] : [toDataError(error).kind]))
	);
	if (kinds.has('offline')) return 'no_connection';
	if (kinds.has('timeout')) return 'timeout';
	return kinds.has('network') || kinds.has('unavailable') ? 'no_connection' : 'unknown';
}
