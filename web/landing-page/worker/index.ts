/**
 * Cloudflare Worker redirecting legacy `/it` and `/en` paths cleanly to `/`
 * where the landing page picks a language from the browser preferences.
 */
interface Env {
	ASSETS: {
		fetch(request: Request): Promise<Response>;
	};
}

export default {
	async fetch(request: Request, env: Env): Promise<Response> {
		const url = new URL(request.url);
		if (/^\/(it|en)\/?$/i.test(url.pathname)) {
			url.pathname = '/';
			return Response.redirect(url.toString(), 301);
		}
		return env.ASSETS.fetch(request);
	}
};
