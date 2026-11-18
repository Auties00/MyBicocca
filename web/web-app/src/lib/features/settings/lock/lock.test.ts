import { describe, expect, it } from 'vitest';
import { hashPin, isValidPin, verifyPin } from './pin.ts';
import { idleExpired, lockoutMs } from './timing.ts';

describe('PIN hashing', () => {
	it('verifies the PIN it was made from and nothing else', async () => {
		const stored = await hashPin('482915', 1_000);
		expect(stored.length).toBe(6);
		expect(stored.hash).not.toContain('482915');
		expect(await verifyPin('482915', stored)).toBe(true);
		expect(await verifyPin('482916', stored)).toBe(false);
		expect(await verifyPin('48291', stored)).toBe(false);
	});

	it('salts every hash', async () => {
		const [a, b] = await Promise.all([hashPin('1234', 1_000), hashPin('1234', 1_000)]);
		expect(a.hash).not.toBe(b.hash);
	});

	it('takes four to eight digits', () => {
		expect(['1234', '12345678'].every(isValidPin)).toBe(true);
		expect(['123', '123456789', '12a4', ' 1234'].some(isValidPin)).toBe(false);
	});
});

describe('idleExpired', () => {
	const minute = 60_000;

	it('locks after the idle time, at once with no grace, and on a fresh tab', () => {
		expect(idleExpired(0, 4 * minute, 5)).toBe(false);
		expect(idleExpired(0, 5 * minute, 5)).toBe(true);
		expect(idleExpired(0, 1, 0)).toBe(true);
		expect(idleExpired(undefined, 0, 240)).toBe(true);
	});
});

describe('lockoutMs', () => {
	it('lets five attempts through, then doubles the wait up to 15 minutes', () => {
		expect(lockoutMs(4)).toBe(0);
		expect(lockoutMs(5)).toBe(30_000);
		expect(lockoutMs(6)).toBe(60_000);
		expect(lockoutMs(20)).toBe(15 * 60_000);
	});
});
