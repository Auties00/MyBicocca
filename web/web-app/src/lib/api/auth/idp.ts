import { LoginError, type AuthService } from '../errors.ts';
import { formAction, formFields, postForm, readPage } from './forms.ts';

export const IDP_HOST = 'idp-idm.unimib.it';
const IDP_URL = `https://${IDP_HOST}/idp/profile/SAML2/Redirect/SSO`;
const IDP_STEP_1_URL = `${IDP_URL}?execution=e1s1`;
const IDP_STEP_2_URL = `${IDP_URL}?execution=e1s2`;
const IDP_STEP_3_URL = `${IDP_URL}?execution=e1s3`;

/** The auto-submitting form carrying the IdP's SAML response to the service provider. */
export interface SamlAssertion {
	action: string;
	fields: Array<[string, string]>;
}

/**
 * The UniMiB Shibboleth IdP's steps once a service provider has sent the browser to its
 * local-storage probe (`e1s1`): answer the probe, pick the password flow over SPID, submit the
 * credentials. Shared by the Moodle and Esse3 portal logins, which only differ in the provider.
 * The conversation ids are fixed by the IdP's flow definition; `send` must carry one cookie jar.
 *
 * @throws LoginError With `badCredentials` when the IdP answers the credentials without a SAML response.
 */
export async function idpLogin(
	send: typeof fetch,
	service: AuthService,
	username: string,
	password: string
): Promise<SamlAssertion> {
	const storage = await postForm(send, IDP_STEP_1_URL, [
		['shib_idp_ls_exception.shib_idp_session_ss', ''],
		['shib_idp_ls_success.shib_idp_session_ss', 'true'],
		['shib_idp_ls_value.shib_idp_session_ss', ''],
		['shib_idp_ls_exception.shib_idp_persistent_ss', ''],
		['shib_idp_ls_success.shib_idp_persistent_ss', 'true'],
		['shib_idp_ls_value.shib_idp_persistent_ss', ''],
		['shib_idp_ls_supported', 'true'],
		['_eventId_proceed', '']
	]);
	expectStatus(storage, 302, service, 'the storage probe');

	const methodPage = await send(IDP_STEP_2_URL);
	expectStatus(methodPage, 200, service, 'the method page');
	await methodPage.body?.cancel();

	const method = await postForm(send, IDP_STEP_2_URL, [
		['_eventId_routing', ''],
		['selected_flow', ''],
		['auth_ctx', 'authn/Password'],
		['spid_idp', '']
	]);
	expectStatus(method, 302, service, 'the method choice');

	const credentials = await postForm(send, IDP_STEP_3_URL, [
		['j_username', username],
		['j_password', password],
		['_eventId_proceed', '']
	]);
	const page = await readPage(credentials);
	// On bad credentials the IdP renders its login form again, which carries no SAML response.
	const form = page.querySelector('input[name=SAMLResponse]')?.closest('form');
	if (form === null || form === undefined) {
		throw new LoginError(service, 'The IdP returned no SAML response', true);
	}
	return { action: formAction(form, page), fields: formFields(form) };
}

function expectStatus(
	response: Response,
	status: number,
	service: AuthService,
	step: string
): void {
	if (response.status !== status) {
		void response.body?.cancel();
		throw new LoginError(service, `Unexpected status ${response.status} at ${step}`);
	}
}
