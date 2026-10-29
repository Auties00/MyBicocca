import { LoginError } from '../errors.ts';
import { postForm } from './forms.ts';
import { IDP_HOST, idpLogin } from './idp.ts';

export const PORTAL_URL = 'https://s3w.si.unimib.it/';
const LOGIN_URL = 'https://s3w.si.unimib.it/auth/studente/HomePageStudente.do';

/**
 * Logs into Esse3's web portal through the UniMiB IdP, for the pages its REST API doesn't cover:
 * the student home page sends the browser to the IdP, and the SAML response posted back to the
 * service provider sets its session cookies (`JSESSIONID`, `_shibsession_*`) in `send`'s jar.
 * `send` must follow redirects by default and carry a fresh jar.
 *
 * @throws LoginError
 */
export async function esse3WebLogin(
	send: typeof fetch,
	username: string,
	password: string
): Promise<void> {
	const entry = await send(LOGIN_URL);
	await entry.body?.cancel();
	if (entry.status !== 200) {
		throw new LoginError('esse3Web', `The portal answered ${entry.status}`);
	}
	const assertion = await idpLogin(send, 'esse3Web', username, password);
	const landing = await postForm(send, assertion.action, assertion.fields);
	await landing.body?.cancel();
	if (new URL(landing.url).hostname === IDP_HOST) {
		throw new LoginError('esse3Web', 'Still on the IdP after the SAML round-trip');
	}
}
