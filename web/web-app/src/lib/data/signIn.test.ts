import { Esse3Error } from '@mybicocca/data-api/esse3';
import { ApiFailure } from '@mybicocca/data-api/runtime';
import { describe, expect, it } from 'vitest';
import { LoginError, NetworkError, RelayError } from '#lib/api/errors.ts';
import { classifySignInFailure } from './signIn.ts';

describe('classifySignInFailure', () => {
	it('blames the credentials when either service refused them', () => {
		expect(classifySignInFailure([new Esse3Error(new ApiFailure(401, undefined, 'x'))])).toBe(
			'bad_credentials'
		);
		expect(
			classifySignInFailure([
				new Esse3Error(new ApiFailure(500, '-1', 'Credenziali errate', 'Credenziali errate')),
				new NetworkError('x')
			])
		).toBe('bad_credentials');
		expect(
			classifySignInFailure([new NetworkError('x'), new LoginError('moodle', 'x', true)])
		).toBe('bad_credentials');
	});

	it('blames the connection for network failures and servers down', () => {
		expect(classifySignInFailure([new NetworkError('x')])).toBe('no_connection');
		expect(classifySignInFailure([new Esse3Error(new ApiFailure(503, undefined, 'x'))])).toBe(
			'no_connection'
		);
	});

	it('tells servers that took too long apart, unless the device is offline', () => {
		expect(classifySignInFailure([new RelayError('upstream_timeout', 504, 'x')])).toBe('timeout');
		expect(classifySignInFailure([new DOMException('Sign-in took too long', 'TimeoutError')])).toBe(
			'timeout'
		);
		expect(
			classifySignInFailure([
				new DOMException('x', 'TimeoutError'),
				new NetworkError('x', { offline: true })
			])
		).toBe('no_connection');
	});

	it('reports anything else as unknown', () => {
		expect(classifySignInFailure([new LoginError('moodle', 'Missing login button')])).toBe(
			'unknown'
		);
	});
});
