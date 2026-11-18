import { describe, expect, it } from 'vitest';
import { looksLikeLink, parsePresenceScan } from './scan.ts';

const MOODLE = 'https://elearning.unimib.it/mod/attendance/attendance.php';

describe('parsePresenceScan', () => {
	it('reads a Moodle session link with its QR password', () => {
		expect(parsePresenceScan(`  ${MOODLE}?qrpass=Ab3x&sessid=482913  `)).toEqual({
			kind: 'session',
			sessionId: '482913',
			password: 'Ab3x'
		});
	});

	it('matches the session page whatever its case, and works without a password', () => {
		expect(
			parsePresenceScan('HTTPS://ELEARNING.UNIMIB.IT/MOD/ATTENDANCE/ATTENDANCE.PHP?SessId=7')
		).toEqual({ kind: 'session', sessionId: '7', password: null });
	});

	it('refuses a session link without a session', () => {
		expect(parsePresenceScan(`${MOODLE}?qrpass=Ab3x`)).toEqual({ kind: 'unrecognized' });
		expect(parsePresenceScan(MOODLE)).toEqual({ kind: 'unrecognized' });
	});

	it('decodes escapes up to the last character and leaves malformed ones', () => {
		expect(parsePresenceScan(`${MOODLE}?sessid=1&qrpass=a+b%21`)).toMatchObject({
			password: 'a b!'
		});
		expect(parsePresenceScan(`${MOODLE}?sessid=1&qrpass=caff%C3%A8`)).toMatchObject({
			password: 'caffè'
		});
		expect(parsePresenceScan(`${MOODLE}?sessid=1&qrpass=100%`)).toMatchObject({
			password: '100%'
		});
	});

	it('keeps a fragment out of the last value', () => {
		expect(parsePresenceScan(`${MOODLE}?qrpass=x&sessid=12#top`)).toMatchObject({
			sessionId: '12'
		});
	});

	it('reads a lesson code from other links, in order of preference', () => {
		expect(parsePresenceScan('https://badge.example/?code=B&codice_lezione=A')).toEqual({
			kind: 'lesson',
			code: 'A'
		});
		expect(parsePresenceScan('http://badge.example/check?lezione=L9')).toEqual({
			kind: 'lesson',
			code: 'L9'
		});
		expect(parsePresenceScan('https://badge.example/check?other=1')).toEqual({
			kind: 'unrecognized'
		});
	});

	it('takes bare text as a lesson code, and nothing as nothing', () => {
		expect(parsePresenceScan(' 48A-2913 ')).toEqual({ kind: 'lesson', code: '48A-2913' });
		expect(parsePresenceScan('   ')).toEqual({ kind: 'unrecognized' });
	});
});

describe('looksLikeLink', () => {
	it('tells links, with or without a scheme for Moodle sessions, from typed codes', () => {
		expect(looksLikeLink(` ${MOODLE}?sessid=1 `)).toBe(true);
		expect(looksLikeLink('elearning.unimib.it/mod/attendance/attendance.php?sessid=1')).toBe(true);
		expect(looksLikeLink('HTTP://badge.example/?code=A')).toBe(true);
		expect(looksLikeLink('48A-2913')).toBe(false);
		expect(looksLikeLink('www.unimib.it')).toBe(false);
	});
});
