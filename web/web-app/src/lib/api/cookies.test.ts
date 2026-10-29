import { describe, expect, it } from 'vitest';
import { CookieJar, parseSetCookie } from './cookies.ts';

const now = Date.UTC(2026, 9, 27, 12);
const moodle = new URL('https://elearning.unimib.it/login/index.php');

describe('parseSetCookie', () => {
	it('defaults to a host-only cookie on the request directory', () => {
		expect(
			parseSetCookie('a=b', new URL('https://s3w.si.unimib.it/auth/studente/X.do'), now)
		).toEqual({
			name: 'a',
			value: 'b',
			domain: 's3w.si.unimib.it',
			hostOnly: true,
			path: '/auth/studente',
			secure: false
		});
	});

	it('reads the attributes, Max-Age winning over Expires', () => {
		const cookie = parseSetCookie(
			'MoodleSession=x; Expires=Wed, 01 Jan 2031 00:00:00 GMT; Max-Age=60; path=/; Domain=.unimib.it; Secure; HttpOnly',
			moodle,
			now
		);
		expect(cookie).toMatchObject({
			domain: 'unimib.it',
			hostOnly: false,
			path: '/',
			secure: true,
			expires: now + 60_000
		});
	});

	it('ignores cookies for a domain the host is not part of, and nameless ones', () => {
		expect(parseSetCookie('a=b; Domain=example.com', moodle, now)).toBeUndefined();
		expect(parseSetCookie('=b', moodle, now)).toBeUndefined();
	});
});

describe('CookieJar', () => {
	it('sends a cookie only where a browser would', () => {
		const jar = new CookieJar();
		jar.store(
			['host=1; Path=/', 'wide=2; Domain=unimib.it; Path=/', 'deep=3; Path=/auth'],
			moodle,
			now
		);
		expect(jar.header(new URL('https://elearning.unimib.it/auth/x'), now)).toBe(
			'deep=3; host=1; wide=2'
		);
		expect(jar.header(new URL('https://idp-idm.unimib.it/idp'), now)).toBe('wide=2');
		expect(jar.header(new URL('https://elearning.unimib.it/authority'), now)).toBe(
			'host=1; wide=2'
		);
		expect(jar.header(new URL('https://unimib.it.example.com/'), now)).toBe('');
	});

	it('keeps secure cookies off plain http', () => {
		const jar = new CookieJar();
		jar.store(['s=1; Secure; Path=/'], moodle, now);
		expect(jar.header(new URL('http://elearning.unimib.it/'), now)).toBe('');
		expect(jar.get('s', moodle, now)).toBe('1');
	});

	it('replaces a cookie with the same name, domain and path, and deletes expired ones', () => {
		const changes: number[] = [];
		const jar = new CookieJar([], () => changes.push(1));
		jar.store(['a=1; Path=/'], moodle, now);
		jar.store(['a=2; Path=/'], moodle, now);
		jar.store(['a=2; Path=/'], moodle, now);
		expect(jar.header(moodle, now)).toBe('a=2');
		jar.store(['a=deleted; Max-Age=0; Path=/'], moodle, now);
		expect(jar.header(moodle, now)).toBe('');
		expect(changes).toHaveLength(3);
	});

	it('forgets cookies once they expire', () => {
		const jar = new CookieJar();
		jar.store(['a=1; Max-Age=10; Path=/'], moodle, now);
		expect(jar.cookies(now)).toHaveLength(1);
		expect(jar.cookies(now + 11_000)).toHaveLength(0);
		expect(jar.header(moodle, now + 11_000)).toBe('');
	});
});
