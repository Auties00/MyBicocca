import { NetworkError } from '#lib/api/errors.ts';
import { publicKey } from '#lib/data/keys.ts';
import { policies, type QueryOptions } from '#lib/data/query.svelte.ts';
import { GithubRateLimitError, readReleases, RELEASES_URL, type Release } from './releases.ts';

/** Until when GitHub refuses this network's requests, after its hourly limit was reached. */
export const githubLimit = $state<{ resetAt?: number }>({});

/**
 * Where the release list comes from: GitHub's API, straight from the browser (it allows any
 * origin), or the demo's fixture. Only `Accept` is sent, a header that needs no preflight.
 */
async function send(): Promise<Response> {
	const transport = __DEMO__
		? (await import('#lib/demo/transport.ts')).demoTransport.fetch()
		: fetch;
	try {
		return await transport(RELEASES_URL, { headers: { accept: 'application/vnd.github+json' } });
	} catch (error) {
		if (error instanceof TypeError) {
			throw new NetworkError(error.message, { cause: error, offline: !navigator.onLine });
		}
		throw error;
	}
}

/**
 * MyBicocca's stable releases. GitHub allows 60 unauthenticated requests an hour per address,
 * shared by everyone behind the campus network, so the list stays fresh for six hours and
 * persists; the browser's HTTP cache revalidates it with its ETag, and a 304 costs no request.
 */
export function releasesQuery(): QueryOptions<Release[]> {
	return {
		key: publicKey('github.releases'),
		load: async () => {
			try {
				const releases = await readReleases(await send());
				githubLimit.resetAt = undefined;
				return releases;
			} catch (error) {
				if (error instanceof GithubRateLimitError) githubLimit.resetAt = error.resetAt;
				throw error;
			}
		},
		policy: policies.shared(6 * 60)
	};
}
