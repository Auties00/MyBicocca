import { describe, expect, it, vi } from 'vitest';
import { installPreloadRecovery } from './preloadRecovery.ts';

function memoryStorage() {
	const map = new Map<string, string>();
	return {
		getItem: (key: string) => map.get(key) ?? null,
		setItem: (key: string, value: string) => void map.set(key, value)
	};
}

function preloadEvent(): Event {
	return new Event('vite:preloadError', { cancelable: true });
}

describe('installPreloadRecovery', () => {
	it('reloads once when online and prevents the unhandled preload error', () => {
		const target = new EventTarget();
		const storage = memoryStorage();
		const reload = vi.fn();

		const dispose = installPreloadRecovery({
			target,
			storage,
			isOnline: () => true,
			reload,
			now: () => 100_000,
			cooldownMs: 30_000
		});

		const event = preloadEvent();
		target.dispatchEvent(event);

		expect(event.defaultPrevented).toBe(true);
		expect(reload).toHaveBeenCalledTimes(1);
		dispose();
	});

	it('does not reload while offline', () => {
		const target = new EventTarget();
		const reload = vi.fn();

		const dispose = installPreloadRecovery({
			target,
			storage: memoryStorage(),
			isOnline: () => false,
			reload,
			now: () => 100_000
		});

		const event = preloadEvent();
		target.dispatchEvent(event);

		expect(event.defaultPrevented).toBe(false);
		expect(reload).not.toHaveBeenCalled();
		dispose();
	});

	it('suppresses repeated reloads within the cooldown window and allows recovery afterwards', () => {
		const target = new EventTarget();
		const storage = memoryStorage();
		const reload = vi.fn();
		let time = 100_000;

		const dispose = installPreloadRecovery({
			target,
			storage,
			isOnline: () => true,
			reload,
			now: () => time,
			cooldownMs: 30_000
		});

		const first = preloadEvent();
		target.dispatchEvent(first);
		expect(first.defaultPrevented).toBe(true);
		expect(reload).toHaveBeenCalledTimes(1);

		time += 10_000;
		const second = preloadEvent();
		target.dispatchEvent(second);
		expect(second.defaultPrevented).toBe(false);
		expect(reload).toHaveBeenCalledTimes(1);

		time += 25_000;
		const third = preloadEvent();
		target.dispatchEvent(third);
		expect(third.defaultPrevented).toBe(true);
		expect(reload).toHaveBeenCalledTimes(2);
		dispose();
	});

	it('tolerates unavailable sessionStorage without throwing', () => {
		const target = new EventTarget();
		const reload = vi.fn();
		const brokenStorage = {
			getItem: () => {
				throw new Error('denied');
			},
			setItem: () => {
				throw new Error('denied');
			}
		};

		const dispose = installPreloadRecovery({
			target,
			storage: brokenStorage,
			isOnline: () => true,
			reload,
			now: () => 100_000
		});

		const event = preloadEvent();
		target.dispatchEvent(event);

		expect(event.defaultPrevented).toBe(true);
		expect(reload).toHaveBeenCalledTimes(1);
		dispose();
	});
});
