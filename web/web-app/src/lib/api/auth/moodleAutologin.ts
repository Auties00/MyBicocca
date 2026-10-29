import {
	AUTOLOGIN_PATH,
	type AutologinRequest,
	type AutologinResponse
} from '@mybicocca/relay/protocol';
import { NetworkError, RelayError } from '../errors.ts';

/**
 * A new `MoodleSession` minted from the mobile tokens by the relay's composite autologin, or
 * undefined when Moodle refuses (wrong tokens, the six-minute window, a login page): the caller
 * then falls back to the full login.
 */
export async function moodleAutologin(
	request: AutologinRequest,
	send: typeof fetch = (input, init) => fetch(input, init)
): Promise<string | undefined> {
	let response: Response;
	try {
		response = await send(AUTOLOGIN_PATH, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(request),
			credentials: 'omit',
			signal: AbortSignal.timeout(45_000)
		});
	} catch (error) {
		if (error instanceof TypeError) {
			throw new NetworkError(error.message, { cause: error, offline: navigator.onLine === false });
		}
		throw error;
	}
	if (response.ok) return ((await response.json()) as AutologinResponse).moodleSession;
	const error = await RelayError.from(response);
	if (error.code === 'autologin_refused' || error.code === 'autologin_too_soon') return undefined;
	throw error;
}
