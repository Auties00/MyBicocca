import { RelayHeader, type RelayErrorBody, type RelayErrorCode } from './protocol.ts';

/** A response the relay produced itself, which the client tells from upstream ones by its header. */
export function relayError(
	status: number,
	error: RelayErrorCode,
	message: string,
	extra: Partial<RelayErrorBody> = {}
): Response {
	const body: RelayErrorBody = { error, message, ...extra };
	return new Response(JSON.stringify(body), {
		status,
		headers: {
			'content-type': 'application/json',
			'cache-control': 'no-store',
			[RelayHeader.error]: error
		}
	});
}
