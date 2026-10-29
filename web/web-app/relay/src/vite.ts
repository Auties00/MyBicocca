import type { IncomingMessage, ServerResponse } from 'node:http';
import { Readable } from 'node:stream';
import { pipeline } from 'node:stream/promises';
import type { ReadableStream as NodeReadableStream } from 'node:stream/web';
import type { Plugin } from 'vite';
import { relayError } from './errors.ts';
import { relay } from './handler.ts';
import { RELAY_PREFIX } from './protocol.ts';

/** Serves the relay from the Vite dev and preview servers, as the Worker does in production. */
export function relayPlugin(): Plugin {
	const middleware = (request: IncomingMessage, response: ServerResponse, next: () => void) => {
		if (!isRelayed(request.url)) return next();
		relay(toRequest(request))
			.catch(() => relayError(502, 'upstream_unreachable', 'The relay failed'))
			.then((relayed) => send(relayed, response));
	};
	return {
		name: 'mybicocca-relay',
		configureServer: (server) => void server.middlewares.use(middleware),
		configurePreviewServer: (server) => void server.middlewares.use(middleware)
	};
}

/**
 * Whether the relay answers `url`: a host (`/relay/s3w.si.unimib.it/…`) or a composite
 * (`/relay/x/…`). The dev server also serves this package's own sources under `/relay/src/`,
 * which the app imports, so those pass through.
 */
function isRelayed(url: string | undefined): boolean {
	if (!url?.startsWith(RELAY_PREFIX)) return false;
	const segment = url.slice(RELAY_PREFIX.length).split(/[/?]/, 1)[0];
	return segment === 'x' || segment.includes('.');
}

function toRequest(request: IncomingMessage): Request {
	const headers = new Headers();
	for (const [name, value] of Object.entries(request.headers)) {
		if (value === undefined) continue;
		for (const item of Array.isArray(value) ? value : [value]) headers.append(name, item);
	}
	const hasBody = request.method !== 'GET' && request.method !== 'HEAD';
	const init: RequestInit & { duplex: 'half' } = {
		method: request.method,
		headers,
		body: hasBody ? (Readable.toWeb(request) as ReadableStream<Uint8Array>) : undefined,
		duplex: 'half'
	};
	return new Request(new URL(request.url ?? '/', `http://${request.headers.host}`), init);
}

function send(relayed: Response, response: ServerResponse): void {
	response.statusCode = relayed.status;
	relayed.headers.forEach((value, name) => response.setHeader(name, value));
	if (relayed.body === null) {
		response.end();
		return;
	}
	// A body cut off upstream, or a client gone, ends this response, never the server.
	pipeline(Readable.fromWeb(relayed.body as NodeReadableStream), response).catch(() => {});
}
