import { describe, expect, it } from 'vitest';
import { scenarioFor, serviceOf } from './scenarios.ts';

const bookings = new URL(
	'https://s3w.si.unimib.it/e3rest/api/libretto-service-v2/libretti/710001/prenotazioni'
);
const careers = new URL('https://s3w.si.unimib.it/e3rest/api/carriere-service-v1/carriere');
const siteInfo = new URL(
	'https://elearning.unimib.it/webservice/rest/server.php?wsfunction=core_webservice_get_site_info&moodlewsrestformat=json'
);

describe('scenarioFor', () => {
	const rules = {
		'*': 'error',
		esse3: 'slow',
		'*/libretti/*/prenotazioni': 'offline',
		'GET */libretto-service-v2/libretti/*/prenotazioni': 'empty'
	};

	it('lets the most specific matcher win', () => {
		expect(scenarioFor(rules, 'GET', bookings)).toBe('empty');
		expect(scenarioFor(rules, 'POST', bookings)).toBe('offline');
		expect(scenarioFor(rules, 'GET', careers)).toBe('slow');
		expect(scenarioFor(rules, 'POST', siteInfo)).toBe('error');
	});

	it('prefers the pattern with more literal text', () => {
		const patterns = { 'GET *': 'slow', 'GET https://s3w.si.unimib.it/e3rest/api/*': 'error' };
		expect(scenarioFor(patterns, 'GET', careers)).toBe('error');
		expect(scenarioFor(patterns, 'GET', siteInfo)).toBe('slow');
	});

	it('reads route patterns with named segments and queries', () => {
		const patterns = {
			'GET https://s3w.si.unimib.it/e3rest/api/libretto-service-v2/libretti/:matId/prenotazioni':
				'empty',
			'POST *server.php?wsfunction=core_webservice_get_site_info': 'error'
		};
		expect(scenarioFor(patterns, 'GET', bookings)).toBe('empty');
		expect(scenarioFor(patterns, 'POST', siteInfo)).toBe('error');
	});

	it('ignores unknown scenarios and matches nothing without rules', () => {
		expect(scenarioFor({ esse3: 'broken' }, 'GET', careers)).toBeUndefined();
		expect(scenarioFor({}, 'GET', careers)).toBeUndefined();
	});
});

describe('serviceOf', () => {
	it('tells Moodle web services from its pages, and Planning from the agenda', () => {
		expect(serviceOf(siteInfo)).toBe('moodle');
		expect(serviceOf(new URL('https://elearning.unimib.it/mod/kalvidres/view.php'))).toBe(
			'moodleWeb'
		);
		expect(
			serviceOf(new URL('https://gestioneorari.didattica.unimib.it/portaleplanningAPI/api/aree/2'))
		).toBe('planning');
		expect(
			serviceOf(
				new URL('https://gestioneorari.didattica.unimib.it/PortaleStudentiUnimib/combo.php')
			)
		).toBe('easystaff');
	});
});
