import { deserialize, serialize } from './codec.ts';
import { idbDelete, idbDeletePrefix, idbEntries, idbGet, idbPut } from './idb.ts';

/** A value encrypted with the vault key. */
export interface Sealed {
	iv: Uint8Array<ArrayBuffer>;
	data: ArrayBuffer;
}

const KEY_ID = 'vault';
let key: Promise<CryptoKey> | undefined;

/**
 * The device's vault key: AES-GCM, generated once and kept in IndexedDB as a non-extractable
 * CryptoKey, so its bytes never reach script; what IndexedDB's files hold at rest is unreadable
 * without this browser profile. Android keeps the same secrets in EncryptedSharedPreferences.
 */
function vaultKey(): Promise<CryptoKey> {
	key ??= (async () => {
		const stored = await idbGet<CryptoKey>('keys', KEY_ID);
		if (stored !== undefined) return stored;
		const generated = await crypto.subtle.generateKey({ name: 'AES-GCM', length: 256 }, false, [
			'encrypt',
			'decrypt'
		]);
		await idbPut('keys', KEY_ID, generated);
		return generated;
	})().catch((error: unknown) => {
		key = undefined;
		throw error;
	});
	return key;
}

export async function seal(value: unknown): Promise<Sealed> {
	const iv = crypto.getRandomValues(new Uint8Array(12));
	const plain = new TextEncoder().encode(serialize(value));
	const data = await crypto.subtle.encrypt({ name: 'AES-GCM', iv }, await vaultKey(), plain);
	return { iv, data };
}

export async function unseal<T>(sealed: Sealed): Promise<T> {
	const plain = await crypto.subtle.decrypt(
		{ name: 'AES-GCM', iv: sealed.iv },
		await vaultKey(),
		sealed.data
	);
	return deserialize<T>(new TextDecoder().decode(plain));
}

/** Encrypted records by string key: accounts, passwords, tokens and cookie jars. */
export const vault = {
	async get<T>(key: string): Promise<T | undefined> {
		const sealed = await idbGet<Sealed>('vault', key);
		return sealed === undefined ? undefined : unseal<T>(sealed);
	},

	async set(key: string, value: unknown): Promise<void> {
		await idbPut('vault', key, await seal(value));
	},

	delete(key: string): Promise<void> {
		return idbDelete('vault', key);
	},

	deletePrefix(prefix: string): Promise<void> {
		return idbDeletePrefix('vault', prefix);
	},

	/** The records under `prefix`, leaving out any the key no longer opens. */
	async entries<T>(prefix: string): Promise<Array<[string, T]>> {
		const sealed = await idbEntries<Sealed>('vault', prefix);
		const opened = await Promise.allSettled(
			sealed.map(async ([key, value]) => [key, await unseal<T>(value)] as [string, T])
		);
		return opened.flatMap((result) => (result.status === 'fulfilled' ? [result.value] : []));
	}
};
