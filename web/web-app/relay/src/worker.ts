import { relay } from './handler.ts';

/** The Cloudflare Worker serving the relay; the static app is served from the same origin as its assets. */
export default {
	fetch: (request: Request) => relay(request)
};
