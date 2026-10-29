import { describe, expect, it } from 'vitest';
import { json, match, route } from './router.ts';

const answer = () => json({});

describe('match', () => {
	it('matches method, host and path, reading named segments', () => {
		const candidate = route(
			'GET https://s3w.si.unimib.it/e3rest/api/persone/:personId/foto',
			answer
		);
		expect(
			match(candidate, 'GET', new URL('https://s3w.si.unimib.it/e3rest/api/persone/73%2001/foto'))
		).toEqual({
			personId: '73 01'
		});
		expect(
			match(candidate, 'POST', new URL('https://s3w.si.unimib.it/e3rest/api/persone/1/foto'))
		).toBeUndefined();
		expect(
			match(candidate, 'GET', new URL('https://s3w.si.unimib.it/e3rest/api/persone/1'))
		).toBeUndefined();
		expect(
			match(candidate, 'GET', new URL('https://s3w.si.unimib.it/e3rest/api/persone/1/foto/x'))
		).toBeUndefined();
	});

	it('requires the query the pattern names, ignoring the rest', () => {
		const candidate = route(
			'POST https://elearning.unimib.it/webservice/rest/server.php?wsfunction=core_webservice_get_site_info',
			answer
		);
		const url = (wsfunction: string) =>
			new URL(
				`https://elearning.unimib.it/webservice/rest/server.php?wsfunction=${wsfunction}&moodlewsrestformat=json`
			);
		expect(match(candidate, 'POST', url('core_webservice_get_site_info'))).toEqual({});
		expect(match(candidate, 'POST', url('core_course_get_contents'))).toBeUndefined();
	});

	it('lets a trailing star take the rest of the path', () => {
		const candidate = route('GET https://elearning.unimib.it/webservice/pluginfile.php/*', answer);
		expect(
			match(
				candidate,
				'GET',
				new URL('https://elearning.unimib.it/webservice/pluginfile.php/12/mod_resource/a.pdf')
			)
		).toEqual({});
	});
});
