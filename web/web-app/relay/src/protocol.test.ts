import { describe, expect, it } from 'vitest';
import {
	acceptsTunneled,
	decodeSetCookies,
	encodeSetCookies,
	refusal,
	relayPath,
	relayTarget
} from './protocol.ts';

describe('relayTarget', () => {
	it('maps a relay path back to the upstream URL, query untouched', () => {
		const target = relayTarget('/relay/s3w.si.unimib.it/e3rest/api/login', '?a=%2F&b=1');
		expect(target?.href).toBe('https://s3w.si.unimib.it/e3rest/api/login?a=%2F&b=1');
	});

	it('refuses hosts outside the allowlist', () => {
		expect(relayTarget('/relay/example.com/x', '')).toBeUndefined();
		expect(relayTarget('/relay/constructor/x', '')).toBeUndefined();
		expect(relayTarget('/other/s3w.si.unimib.it/x', '')).toBeUndefined();
	});

	it('round-trips with relayPath', () => {
		const url = new URL('https://idp-idm.unimib.it/idp/profile/SAML2/Redirect/SSO?execution=e1s1');
		const path = relayPath(url);
		const [pathname, search] = path.split('?');
		expect(relayTarget(pathname, `?${search}`)?.href).toBe(url.href);
	});
});

describe('refusal', () => {
	const allowed = (method: string, url: string) => refusal(method, new URL(url));

	it('allows the routes the app needs', () => {
		expect(allowed('GET', 'https://s3w.si.unimib.it/e3rest/api/login')).toBeUndefined();
		expect(allowed('PATCH', 'https://s3w.si.unimib.it/e3rest/api/x')).toBeUndefined();
		expect(
			allowed('POST', 'https://idp-idm.unimib.it/idp/profile/SAML2/Redirect/SSO')
		).toBeUndefined();
		expect(allowed('POST', 'https://elearning.unimib.it/lib/ajax/service.php')).toBeUndefined();
		expect(
			allowed('POST', 'https://elearning.unimib.it/auth/unimibsaml/sp/acs.php')
		).toBeUndefined();
		expect(
			allowed(
				'POST',
				'https://sp.elearning.unimib.it/module.php/saml/sp/saml2-acs.php/unimib-idm-prod-sp'
			)
		).toBeUndefined();
		expect(allowed('POST', 'https://s3w.si.unimib.it/Shibboleth.sso/SAML2/POST')).toBeUndefined();
		expect(
			allowed(
				'POST',
				'https://gestioneorari.didattica.unimib.it/PortaleStudentiUnimib/grid_call.php'
			)
		).toBeUndefined();
		expect(allowed('POST', 'https://reservation.affluences.com/api/reserve/42')).toBeUndefined();
		expect(allowed('GET', 'https://api.affluences.com/app/v4/sites/x')).toBeUndefined();
	});

	it('keeps Moodle web services and files off the relay', () => {
		expect(allowed('POST', 'https://elearning.unimib.it/webservice/rest/server.php')).toBe(
			'path_not_allowed'
		);
		expect(allowed('GET', 'https://elearning.unimib.it/webservice/pluginfile.php/1/a.pdf')).toBe(
			'path_not_allowed'
		);
		expect(allowed('GET', 'https://elearning.unimib.it/admin/tool/mobile/autologin.php')).toBe(
			'path_not_allowed'
		);
	});

	it('checks the method against the matching rule', () => {
		expect(allowed('POST', 'https://s3w.si.unimib.it/auth/studente/Home.do')).toBe(
			'method_not_allowed'
		);
		expect(allowed('GET', 'https://elearning.unimib.it/lib/ajax/service.php')).toBe(
			'method_not_allowed'
		);
		expect(
			allowed(
				'GET',
				'https://gestioneorari.didattica.unimib.it/PortaleStudentiUnimib/grid_call.php'
			)
		).toBe('method_not_allowed');
	});

	it('refuses unknown hosts, other schemes and ports', () => {
		expect(allowed('GET', 'https://example.com/')).toBe('host_not_allowed');
		expect(allowed('GET', 'http://s3w.si.unimib.it/e3rest/api/login')).toBe('host_not_allowed');
		expect(allowed('GET', 'https://s3w.si.unimib.it:8443/e3rest/api/login')).toBe(
			'host_not_allowed'
		);
	});

	it('normalizes dot segments before matching', () => {
		expect(allowed('GET', 'https://s3w.si.unimib.it/e3rest/api/../../admin')).toBe(
			'path_not_allowed'
		);
	});
});

describe('acceptsTunneled', () => {
	const planning = new URL('https://gestioneorari.didattica.unimib.it/portaleplanningAPI/api/x');
	const esse3 = new URL('https://s3w.si.unimib.it/e3rest/api/login');
	const affluences = new URL('https://api.affluences.com/app/v4/apps/checkin');

	it('sends Origin only with the fixed per-host value', () => {
		expect(acceptsTunneled('origin', 'https://gestioneorari.didattica.unimib.it', planning)).toBe(
			true
		);
		expect(acceptsTunneled('origin', 'https://evil.example', planning)).toBe(false);
		expect(acceptsTunneled('origin', 'https://gestioneorari.didattica.unimib.it', esse3)).toBe(
			false
		);
	});

	it('sends a User-Agent only from the enum, to its host', () => {
		const chrome =
			'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36';
		expect(acceptsTunneled('user-agent', chrome, affluences)).toBe(true);
		expect(acceptsTunneled('user-agent', chrome, esse3)).toBe(false);
		expect(acceptsTunneled('user-agent', 'MoodleMobile 4.5.0', esse3)).toBe(false);
	});

	it('sends a Referer only for an allowlisted https page', () => {
		expect(acceptsTunneled('referer', 'https://idp-idm.unimib.it/idp/x', esse3)).toBe(true);
		expect(acceptsTunneled('referer', 'https://evil.example/', esse3)).toBe(false);
		expect(acceptsTunneled('referer', 'not a url', esse3)).toBe(false);
	});

	it('passes cookies and refuses anything else', () => {
		expect(acceptsTunneled('cookie', 'a=b', esse3)).toBe(true);
		expect(acceptsTunneled('x-forwarded-for', '1.2.3.4', esse3)).toBe(false);
	});
});

describe('Set-Cookie envelope', () => {
	it('round-trips cookies, escaping what a header value can not hold', () => {
		const cookies = ['MoodleSession=abc; path=/; secure', 'name=caffè; Path=/'];
		const header = encodeSetCookies(cookies);
		expect(header).toMatch(/^[\x20-\x7e]+$/);
		expect(decodeSetCookies(header)).toEqual(cookies);
	});

	it('reads a missing or broken envelope as no cookies', () => {
		expect(decodeSetCookies(null)).toEqual([]);
		expect(decodeSetCookies('{')).toEqual([]);
		expect(decodeSetCookies('[1,"a=b"]')).toEqual(['a=b']);
	});
});
