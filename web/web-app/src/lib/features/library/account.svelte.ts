import {
	AffluencesAccountAuth,
	AffluencesDeviceAuth,
	type AffluencesApi
} from '@mybicocca/data-api/affluences';
import { SvelteMap } from 'svelte/reactivity';
import type { Account } from '#lib/data/account.ts';
import { DataFailure } from '#lib/data/errors.ts';
import { accountKey } from '#lib/data/keys.ts';
import { cache } from '#lib/data/query.svelte.ts';
import { vault } from '#lib/data/vault.ts';

type Owner = Pick<Account, 'id'>;

/** This browser's Affluences device, checked in once and shared by the accounts signed in here. */
interface Device {
	/** Random, made once: what `checkIn` registers. */
	deviceId: string;
	apiKey: string | null;
}

/**
 * An account's validated email and its bearer token. They live under the account's vault prefix,
 * so signing out forgets them: on a shared library computer the next student never sees, cancels
 * or books with them.
 */
interface StoredLink {
	/** The email the token belongs to; bookings are made with it. */
	email: string | null;
	authToken: string | null;
	/** The token stopped working: the page asks to verify the email again. */
	expired: boolean;
}

export interface LibraryLink {
	email: string | null;
	/** Present once an email is validated. */
	auth: AffluencesAccountAuth | undefined;
	expired: boolean;
}

const DEVICE_VAULT_KEY = 'library/live';
const linkVaultKey = (account: Owner) => `acct/${account.id}/library`;

/** The account's bookings, named after the SDK call so the calendar shares the entry. */
export const myReservationsKey = (account: Owner) =>
	accountKey(account, 'affluences.account.getMyReservations');

const UNLINKED: StoredLink = { email: null, authToken: null, expired: false };

async function initialLink(): Promise<StoredLink> {
	if (!__DEMO__) return UNLINKED;
	// The demo student has validated their email already, as in the Android demo.
	const { DemoAffluencesLink } = await import('#lib/demo/fixtures/affluences/account.ts');
	return {
		email: DemoAffluencesLink.email,
		authToken: DemoAffluencesLink.authToken,
		expired: false
	};
}

async function initialDevice(): Promise<Device> {
	const deviceId = crypto.randomUUID().replace(/-/g, '').slice(0, 16);
	if (!__DEMO__) return { deviceId, apiKey: null };
	const { DemoAffluencesLink } = await import('#lib/demo/fixtures/affluences/account.ts');
	return { deviceId, apiKey: DemoAffluencesLink.apiKey };
}

/** Reads a vault record once, writing its initial value the first time. */
function remembered<T>(
	reads: Map<string, Promise<T>>,
	key: string,
	initial: () => Promise<T>
): Promise<T> {
	let read = reads.get(key);
	if (read === undefined) {
		read = vault
			.get<T>(key)
			.then(async (found) => {
				if (found !== undefined) return found;
				const value = await initial();
				await vault.set(key, value);
				return value;
			})
			.catch((error: unknown) => {
				reads.delete(key);
				throw error;
			});
		reads.set(key, read);
	}
	return read;
}

class LibraryLinks {
	readonly #views = new SvelteMap<string, LibraryLink>();
	readonly #links = new Map<string, Promise<StoredLink>>();
	readonly #devices = new Map<string, Promise<Device>>();

	/** The account's link; undefined while it is read, which starts on first ask. */
	of(account: Owner): LibraryLink | undefined {
		const view = this.#views.get(account.id);
		if (view === undefined) this.#refreshView(account).catch(() => undefined);
		return view;
	}

	/** The device's credentials, checking the device in with Affluences the first time. */
	async deviceAuth(api: AffluencesApi): Promise<AffluencesDeviceAuth> {
		const device = await this.#device();
		if (device.apiKey) return new AffluencesDeviceAuth(device.apiKey);
		const checkIn = await api.account.checkIn({
			deviceId: device.deviceId,
			deviceLang: 'it',
			lastCheckin: null,
			lastCheckinAdvertPopup: null,
			firebaseToken: null
		});
		if (!checkIn.apiKey) {
			throw new DataFailure({ kind: 'decode', detail: 'Affluences check-in gave no API key' });
		}
		const next = { ...device, apiKey: checkIn.apiKey };
		await vault.set(DEVICE_VAULT_KEY, next);
		this.#devices.set(DEVICE_VAULT_KEY, Promise.resolve(next));
		return new AffluencesDeviceAuth(checkIn.apiKey);
	}

	/** Keeps the token the email validation gave; another email's cached bookings are dropped. */
	async link(account: Owner, email: string, authToken: string): Promise<void> {
		const stored = await this.#link(account);
		if (stored.email !== email) await cache.remove(myReservationsKey(account));
		await this.#write(account, { email, authToken, expired: false });
	}

	/** Forgets the email, its token and its bookings; the device stays checked in. */
	async unlink(account: Owner): Promise<void> {
		await cache.remove(myReservationsKey(account));
		await this.#write(account, UNLINKED);
	}

	/** The server refused the token: it is dropped, the email kept to verify it again. */
	async expire(account: Owner): Promise<void> {
		const stored = await this.#link(account);
		if (stored.authToken === null) return;
		await this.#write(account, { ...stored, authToken: null, expired: true });
	}

	#link(account: Owner): Promise<StoredLink> {
		return remembered(this.#links, linkVaultKey(account), initialLink);
	}

	#device(): Promise<Device> {
		return remembered(this.#devices, DEVICE_VAULT_KEY, initialDevice);
	}

	async #refreshView(account: Owner): Promise<void> {
		const [stored, device] = await Promise.all([this.#link(account), this.#device()]);
		this.#views.set(account.id, {
			email: stored.email,
			auth:
				device.apiKey && stored.authToken
					? new AffluencesAccountAuth(device.apiKey, stored.authToken)
					: undefined,
			expired: stored.expired
		});
	}

	async #write(account: Owner, link: StoredLink): Promise<void> {
		await vault.set(linkVaultKey(account), link);
		this.#links.set(linkVaultKey(account), Promise.resolve(link));
		await this.#refreshView(account);
	}
}

export const libraryLinks = new LibraryLinks();
