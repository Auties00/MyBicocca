import { untrack } from 'svelte';
import { session } from '#lib/data/session.svelte.ts';
import { hashPin, verifyPin, type PinHash } from './pin.ts';
import {
	FREE_ATTEMPTS,
	idleExpired,
	LOCK_TIMEOUTS,
	lockoutMs,
	type LockTimeout
} from './timing.ts';

export { LOCK_TIMEOUTS, type LockTimeout } from './timing.ts';

interface LockConfig {
	pin: PinHash;
	timeout: LockTimeout;
}

interface Failures {
	count: number;
	/** Epoch ms until which no PIN is checked. */
	until: number;
}

const CONFIG_KEY = 'mybicocca:app-lock';
const FAILURES_KEY = 'mybicocca:app-lock-failures';
/** Per tab: when the app was last in use, so a reload within the idle time stays unlocked. */
const ACTIVE_KEY = 'mybicocca:app-lock-active';

function readJson<T>(storage: () => Storage, key: string): T | undefined {
	try {
		const raw = storage().getItem(key);
		return raw === null ? undefined : (JSON.parse(raw) as T);
	} catch {
		return undefined;
	}
}

function writeJson(storage: () => Storage, key: string, value: unknown): void {
	try {
		if (value === undefined) storage().removeItem(key);
		else storage().setItem(key, JSON.stringify(value));
	} catch {
		// Storage refused (private window): the state holds for this visit.
	}
}

const local = () => localStorage;
const tab = () => sessionStorage;

function readConfig(): LockConfig | undefined {
	const config = readJson<Partial<LockConfig>>(local, CONFIG_KEY);
	const timeout = LOCK_TIMEOUTS.find((value) => value === config?.timeout);
	const pin = config?.pin;
	if (timeout === undefined || !pin || typeof pin.hash !== 'string') return undefined;
	return { pin, timeout };
}

export type UnlockResult = { ok: true } | { ok: false; waitUntil: number; attemptsLeft: number };

/**
 * The app lock: a PIN of this browser that hides the app when it opens and after it sits unused,
 * as Android's lock does with the fingerprint. The PIN is kept only as a PBKDF2 hash and is never
 * the university password. `AppLockGate` shows the lock screen while `locked` holds.
 */
class AppLock {
	config = $state.raw<LockConfig | undefined>();
	locked = $state(false);
	failures = $state.raw<Failures>({ count: 0, until: 0 });

	#lastActive = 0;

	constructor() {
		if (typeof window === 'undefined') return;
		this.config = readConfig();
		this.failures = readJson<Failures>(local, FAILURES_KEY) ?? { count: 0, until: 0 };
		const config = this.config;
		if (config !== undefined) {
			const last = readJson<number>(tab, ACTIVE_KEY);
			this.locked = idleExpired(last, Date.now(), config.timeout);
		}
		this.#lastActive = Date.now();
		// The PIN belongs to the people signed in on this browser: once the last of them signs out,
		// here or in another tab, the next student to sign in starts without it.
		$effect.root(() => {
			$effect(() => {
				if (session.loaded && session.accounts.length === 0) untrack(() => this.forget());
			});
		});
	}

	get enabled(): boolean {
		return this.config !== undefined;
	}

	async enable(pin: string, timeout: LockTimeout = this.config?.timeout ?? 0): Promise<void> {
		this.#save({ pin: await hashPin(pin), timeout });
		this.#setFailures({ count: 0, until: 0 });
		this.#touch();
	}

	/** Turns the lock off once `pin` proves it is the owner. */
	async disable(pin: string): Promise<UnlockResult> {
		const result = await this.#check(pin);
		if (result.ok) this.#save(undefined);
		return result;
	}

	async changePin(current: string, next: string): Promise<UnlockResult> {
		const result = await this.#check(current);
		if (result.ok && this.config) this.#save({ ...this.config, pin: await hashPin(next) });
		return result;
	}

	setTimeout(timeout: LockTimeout): void {
		if (this.config) this.#save({ ...this.config, timeout });
	}

	async unlock(pin: string): Promise<UnlockResult> {
		const result = await this.#check(pin);
		if (result.ok) {
			this.locked = false;
			this.#touch();
		}
		return result;
	}

	lock(): void {
		if (this.enabled) this.locked = true;
	}

	/** Drops the lock without its PIN, once every account has signed out of this browser. */
	forget(): void {
		this.#save(undefined);
		this.#setFailures({ count: 0, until: 0 });
		this.locked = false;
	}

	/**
	 * Watches the app being left and used: it locks on return after the idle time, and also when
	 * left open untouched that long. Returns the cleanup.
	 */
	watch(): () => void {
		const controller = new AbortController();
		const { signal } = controller;
		const away = () => writeJson(tab, ACTIVE_KEY, this.#lastActive);
		const check = () => {
			const config = this.config;
			if (config && !this.locked && idleExpired(this.#lastActive, Date.now(), config.timeout)) {
				this.locked = true;
			}
		};
		let throttle = 0;
		const active = () => {
			const now = Date.now();
			if (now - throttle < 5_000) return;
			throttle = now;
			if (!this.locked) this.#touch(now);
		};
		document.addEventListener(
			'visibilitychange',
			() => {
				if (document.visibilityState === 'hidden') {
					if (!this.locked) this.#touch();
					away();
					if (this.config?.timeout === 0) this.lock();
				} else {
					check();
				}
			},
			{ signal }
		);
		window.addEventListener('pagehide', away, { signal });
		for (const type of ['pointerdown', 'keydown', 'wheel', 'touchstart'] as const) {
			window.addEventListener(type, active, { signal, passive: true, capture: true });
		}
		const interval = setInterval(() => {
			if (this.config && this.config.timeout > 0 && document.visibilityState === 'visible') check();
		}, 15_000);
		signal.addEventListener('abort', () => clearInterval(interval));
		return () => controller.abort();
	}

	#touch(now = Date.now()): void {
		this.#lastActive = now;
		writeJson(tab, ACTIVE_KEY, now);
	}

	async #check(pin: string): Promise<UnlockResult> {
		const config = this.config;
		const now = Date.now();
		if (config === undefined) return { ok: true };
		if (this.failures.until > now) {
			return { ok: false, waitUntil: this.failures.until, attemptsLeft: 0 };
		}
		if (await verifyPin(pin, config.pin)) {
			this.#setFailures({ count: 0, until: 0 });
			return { ok: true };
		}
		const count = this.failures.count + 1;
		const until = Date.now() + lockoutMs(count);
		this.#setFailures({ count, until });
		return { ok: false, waitUntil: until, attemptsLeft: Math.max(0, FREE_ATTEMPTS - count) };
	}

	#save(config: LockConfig | undefined): void {
		this.config = config;
		writeJson(local, CONFIG_KEY, config);
		if (config === undefined) this.locked = false;
	}

	#setFailures(failures: Failures): void {
		this.failures = failures;
		writeJson(local, FAILURES_KEY, failures.count === 0 ? undefined : failures);
	}
}

export const appLock = new AppLock();
