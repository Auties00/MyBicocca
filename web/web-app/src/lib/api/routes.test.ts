import { describe, expect, it } from 'vitest';
import { routeOf } from './routes.ts';

const route = (url: string) => routeOf(new URL(url));

describe('routeOf', () => {
	it('keeps Moodle web services, uploads and files direct', () => {
		expect(route('https://elearning.unimib.it/webservice/rest/server.php?wsfunction=x')).toBe(
			'direct'
		);
		expect(route('https://elearning.unimib.it/webservice/upload.php?token=t')).toBe('direct');
		expect(
			route('https://elearning.unimib.it/webservice/pluginfile.php/12/mod_resource/a.pdf')
		).toBe('direct');
	});

	it('relays Moodle pages and the login chain', () => {
		expect(route('https://elearning.unimib.it/lib/ajax/service.php')).toBe('relay');
		expect(route('https://elearning.unimib.it/mod/kalvidres/view.php?id=1')).toBe('relay');
		expect(route('https://elearning.unimib.it/webservice/rest/server.php.evil')).toBe('relay');
		expect(route('https://idp-idm.unimib.it/idp/profile/SAML2/Redirect/SSO')).toBe('relay');
		expect(
			route('https://sp.elearning.unimib.it/module.php/saml/sp/saml2-acs.php/unimib-idm-prod-sp')
		).toBe('relay');
	});

	it('relays the hosts without CORS', () => {
		expect(route('https://s3w.si.unimib.it/e3rest/api/login')).toBe('relay');
		expect(route('https://gestioneorari.didattica.unimib.it/PortaleStudentiUnimib/combo.php')).toBe(
			'relay'
		);
		expect(route('https://api.affluences.com/app/v4/sites/x')).toBe('relay');
	});

	it('relays Affluences reservations only', () => {
		expect(route('https://reservation.affluences.com/api/reserve/abc')).toBe('relay');
		expect(route('https://reservation.affluences.com/api/myreservations')).toBe('direct');
	});

	it('streams Kaltura directly', () => {
		expect(route('https://cdnapisec.kaltura.com/p/2351962/sp/235196200/playManifest/a.m3u8')).toBe(
			'direct'
		);
		expect(route('https://cfvod.kaltura.com/hls/x/seg-1.ts')).toBe('direct');
	});
});
