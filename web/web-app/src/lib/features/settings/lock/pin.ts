/** A PIN as stored: never the digits, only a slow salted hash of them. */
export interface PinHash {
	/** Base64. */
	salt: string;
	/** Base64 of the PBKDF2-SHA-256 output. */
	hash: string;
	iterations: number;
	/** How many digits it has, so the lock screen can submit once they are all typed. */
	length: number;
}

/** OWASP's figure for PBKDF2-HMAC-SHA-256: about a quarter of a second on a phone. */
export const PIN_ITERATIONS = 600_000;
export const PIN_MIN = 4;
export const PIN_MAX = 8;

export function isValidPin(pin: string): boolean {
	return new RegExp(`^\\d{${PIN_MIN},${PIN_MAX}}$`).test(pin);
}

const toBase64 = (bytes: Uint8Array) => btoa(String.fromCharCode(...bytes));
const fromBase64 = (text: string) => Uint8Array.from(atob(text), (char) => char.charCodeAt(0));

async function derive(pin: string, salt: Uint8Array<ArrayBuffer>, iterations: number) {
	const key = await crypto.subtle.importKey('raw', new TextEncoder().encode(pin), 'PBKDF2', false, [
		'deriveBits'
	]);
	const bits = await crypto.subtle.deriveBits(
		{ name: 'PBKDF2', hash: 'SHA-256', salt, iterations },
		key,
		256
	);
	return new Uint8Array(bits);
}

export async function hashPin(pin: string, iterations = PIN_ITERATIONS): Promise<PinHash> {
	const salt = crypto.getRandomValues(new Uint8Array(16));
	const hash = await derive(pin, salt, iterations);
	return { salt: toBase64(salt), hash: toBase64(hash), iterations, length: pin.length };
}

/** Whether `pin` is the one `stored` was made from, compared in constant time. */
export async function verifyPin(pin: string, stored: PinHash): Promise<boolean> {
	const expected = fromBase64(stored.hash);
	const actual = await derive(pin, fromBase64(stored.salt), stored.iterations);
	let difference = expected.length ^ actual.length;
	for (let index = 0; index < expected.length; index++) {
		difference |= expected[index] ^ (actual[index] ?? 0);
	}
	return difference === 0;
}
