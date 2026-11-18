import { describe, expect, it } from 'vitest';
import { LoginError } from '../errors.ts';
import { parseToken } from './moodleLogin.ts';

describe('parseToken', () => {
	it('reads the web service and private tokens', () => {
		const payload = btoa(`signature:::${'a'.repeat(32)}:::private123`);
		expect(parseToken(`moodlemobile://token=${payload}`)).toEqual({
			wsToken: 'a'.repeat(32),
			privateToken: 'private123'
		});
	});

	it('has no private token when Moodle reused a session', () => {
		const payload = btoa(`signature:::${'b'.repeat(32)}`);
		expect(parseToken(`moodlemobile://token=${payload}`).privateToken).toBeNull();
	});

	it('rejects a payload without a token', () => {
		expect(() => parseToken('moodlemobile://token=!!')).toThrow(LoginError);
		expect(() => parseToken(`moodlemobile://token=${btoa('only')}`)).toThrow(LoginError);
	});
});
