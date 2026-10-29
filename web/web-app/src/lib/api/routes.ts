/** How a request reaches its host: straight from the browser, or through the relay. */
export type Route = 'direct' | 'relay';

/** Moodle endpoints that answer browsers with `Access-Control-Allow-Origin: *`. */
const MOODLE_DIRECT = /^\/webservice\/(rest\/server\.php|upload\.php|pluginfile\.php)(\/|$)/;

/**
 * The route for `url`, by the analysis' routing matrix. Moodle's web services must stay direct:
 * attendance marking checks the student's subnet and refuses a shared IP. Video is never relayed.
 * The university's other hosts send no CORS headers, login chains included (Moodle's SAML
 * provider is `sp.elearning.unimib.it`), so every one of them goes through the relay, whose
 * allowlist decides what may pass.
 */
export function routeOf(url: URL): Route {
	const host = url.hostname;
	if (host === 'elearning.unimib.it') return MOODLE_DIRECT.test(url.pathname) ? 'direct' : 'relay';
	if (host === 'unimib.it' || host.endsWith('.unimib.it')) return 'relay';
	if (host === 'kaltura.com' || host.endsWith('.kaltura.com')) return 'direct';
	// Its preflight answers 403 for reservations only; the rest of the API allows browsers.
	if (host === 'reservation.affluences.com') {
		return url.pathname.startsWith('/api/reserve/') ? 'relay' : 'direct';
	}
	return host === 'api.affluences.com' ? 'relay' : 'direct';
}
