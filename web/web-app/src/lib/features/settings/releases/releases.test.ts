import { describe, expect, it } from 'vitest';
import { GithubError, GithubRateLimitError, readReleases, toReleases } from './releases.ts';

const dto = (tag: string, published: string, extra: object = {}) => ({
	tag_name: tag,
	name: `MyBicocca v${tag}`,
	body: '',
	html_url: `https://github.com/Auties00/MyBicocca/releases/tag/${tag}`,
	published_at: published,
	draft: false,
	prerelease: false,
	...extra
});

describe('toReleases', () => {
	it('keeps stable releases, newest first, without APK download tips', () => {
		const releases = toReleases([
			dto('0.0.5', '2026-08-11T23:57:38Z'),
			dto('0.0.6-nightly.44', '2026-10-14T22:20:35Z', { prerelease: true }),
			dto('0.0.7', '2026-11-01T00:00:00Z', { draft: true }),
			dto('v0.0.6', '2026-10-14T23:05:48Z', {
				name: 'v0.0.6',
				body: '- New\n\n> [!TIP]\n> Go with `mybicocca-universal-v0.0.6.apk`\n\n> [!NOTE]\n> Kept'
			}),
			dto('', '2026-01-01T00:00:00Z')
		]);
		expect(releases.map((release) => release.version)).toEqual(['0.0.6', '0.0.5']);
		expect(releases[0].title).toBe('MyBicocca v0.0.6');
		expect(releases[0].blocks).toEqual([
			{ type: 'list', items: [{ text: 'New' }] },
			{ type: 'callout', kind: 'note', text: 'Kept' }
		]);
	});
});

describe('readReleases', () => {
	it('tells the hourly limit apart from other failures', async () => {
		const limited = new Response('{}', {
			status: 403,
			headers: { 'x-ratelimit-remaining': '0', 'x-ratelimit-reset': '1793105153' }
		});
		await expect(readReleases(limited)).rejects.toEqual(new GithubRateLimitError(1793105153000));
		await expect(readReleases(new Response('{}', { status: 502 }))).rejects.toBeInstanceOf(
			GithubError
		);
	});
});
