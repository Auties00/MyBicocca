import { parseReleaseNotes, type ReleaseBlock } from './notes.ts';

export const REPOSITORY_URL = 'https://github.com/Auties00/MyBicocca';
export const RELEASES_URL = 'https://api.github.com/repos/Auties00/MyBicocca/releases?per_page=30';

/** The fields of GitHub's release object this page reads. */
export interface GithubReleaseDto {
	tag_name?: string | null;
	name?: string | null;
	body?: string | null;
	html_url?: string | null;
	published_at?: string | null;
	draft?: boolean;
	prerelease?: boolean;
}

/** A stable release of MyBicocca (the Android app) with its notes. */
export interface Release {
	/** The tag without its `v`: `0.0.6`. */
	version: string;
	/** "MyBicocca v0.0.6". */
	title: string;
	url: string;
	/** Epoch milliseconds. */
	publishedAt: number | null;
	blocks: ReleaseBlock[];
}

/** A callout that only concerns Android's APK downloads, which the web has none of. */
function aboutApks(block: ReleaseBlock): boolean {
	return block.type === 'callout' && /\.apk\b/i.test(block.text);
}

/**
 * The stable releases, newest first. Drafts and pre-releases (Android's nightly channel) are left
 * out, as are releases without a tag or a page.
 */
export function toReleases(dtos: readonly GithubReleaseDto[]): Release[] {
	return dtos
		.flatMap((dto): Release[] => {
			const tag = dto.tag_name?.trim();
			const url = dto.html_url?.trim();
			if (!tag || !url || dto.draft || dto.prerelease) return [];
			const version = tag.replace(/^[vV]/, '');
			const name = dto.name?.trim() || tag;
			const published = dto.published_at ? Date.parse(dto.published_at) : NaN;
			return [
				{
					version,
					title: /mybicocca/i.test(name) ? name : `MyBicocca ${name}`,
					url,
					publishedAt: Number.isNaN(published) ? null : published,
					blocks: parseReleaseNotes(dto.body ?? '').filter((block) => !aboutApks(block))
				}
			];
		})
		.sort(
			(a, b) =>
				(b.publishedAt ?? 0) - (a.publishedAt ?? 0) ||
				b.version.localeCompare(a.version, 'en', { numeric: true })
		);
}

/** GitHub refused for its hourly limit of unauthenticated requests, until `resetAt`. */
export class GithubRateLimitError extends Error {
	override readonly name = 'GithubRateLimitError';

	constructor(readonly resetAt: number | undefined) {
		super('GitHub rate limit reached');
	}
}

/** GitHub answered with an error status. */
export class GithubError extends Error {
	override readonly name = 'GithubError';

	constructor(readonly status: number) {
		super(`GitHub answered ${status}`);
	}
}

/** Reads the release list's answer, telling the hourly limit apart from other failures. */
export async function readReleases(response: Response): Promise<Release[]> {
	if (response.ok) return toReleases((await response.json()) as GithubReleaseDto[]);
	const remaining = response.headers.get('x-ratelimit-remaining');
	if ((response.status === 403 || response.status === 429) && remaining === '0') {
		const reset = Number(response.headers.get('x-ratelimit-reset'));
		throw new GithubRateLimitError(Number.isFinite(reset) && reset > 0 ? reset * 1000 : undefined);
	}
	throw new GithubError(response.status);
}
