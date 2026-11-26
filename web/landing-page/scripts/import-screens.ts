/**
 * Imports curated product screenshots from each platform's automated screenshot folder into
 * `src/assets/screens/` as sharp, metadata-free WebP images sized for high-DPI rendering.
 *
 *     npm run screens
 */
import { access, mkdir, rm, stat } from 'node:fs/promises';
import { dirname, relative, resolve } from 'node:path';
import sharp from 'sharp';

const ROOT = resolve(import.meta.dirname, '..');
const REPO = resolve(ROOT, '../..');
const OUT = resolve(ROOT, 'src/assets/screens');

const LOCALES = ['it', 'en'] as const;
const THEMES = ['dark'] as const;

type Platform = 'web' | 'android' | 'ios';
type Shot = 'calendar' | 'elearning' | 'map' | 'exams' | 'library' | 'profile';

interface Variant {
	size?: 'desktop' | 'tablet' | 'mobile';
	width: number;
}

const PLATFORMS: Record<Platform, { source: string; variants: Variant[] }> = {
	web: {
		source: 'web/web-app/tools/screenshots',
		variants: [
			{ size: 'desktop', width: 1680 },
			{ size: 'tablet', width: 920 },
			{ size: 'mobile', width: 640 }
		]
	},
	android: {
		source: 'android/tools/screenshots',
		variants: [{ width: 640 }]
	},
	ios: {
		source: 'ios/tools/screenshots',
		variants: [{ width: 640 }]
	}
};

const SHOTS: Record<Shot, string> = {
	calendar: 'Calendar1',
	elearning: 'Elearning',
	map: 'Map',
	exams: 'BookedExams',
	library: 'Library',
	profile: 'Profile'
};

interface Job {
	from: string;
	to: string;
	width: number;
}

const exists = (path: string) =>
	access(path).then(
		() => true,
		() => false
	);

function buildJobs(platform: Platform, source: string): Job[] {
	return PLATFORMS[platform].variants.flatMap(({ size, width }) =>
		LOCALES.flatMap((locale) =>
			THEMES.flatMap((theme) =>
				(Object.entries(SHOTS) as [Shot, string][]).map(([shot, name]) => ({
					from: resolve(source, locale, size ?? '', theme, `Screenshot_${name}.png`),
					to: resolve(OUT, platform, size ?? '', locale, theme, `${shot}.webp`),
					width
				}))
			)
		)
	);
}

async function convert({ from, to, width }: Job) {
	await mkdir(dirname(to), { recursive: true });
	await sharp(from)
		.resize({ width, withoutEnlargement: true })
		.webp({ quality: 92, alphaQuality: 100, effort: 5 })
		.toFile(to);
	const [before, after] = await Promise.all([stat(from), stat(to)]);
	return { before: before.size, after: after.size };
}

const mb = (bytes: number) => `${(bytes / 1024 ** 2).toFixed(1)} MB`;

const missing: string[] = [];
const pending: [Platform, Job[]][] = [];

for (const platform of Object.keys(PLATFORMS) as Platform[]) {
	const source = resolve(REPO, PLATFORMS[platform].source);
	if (!(await exists(source))) {
		console.log(`${platform}: skipped (no ${PLATFORMS[platform].source} yet)`);
		continue;
	}
	const jobs = buildJobs(platform, source);
	for (const job of jobs) {
		if (!(await exists(job.from))) missing.push(relative(REPO, job.from));
	}
	pending.push([platform, jobs]);
}

if (missing.length > 0) {
	console.error(`Missing source screenshots:\n  ${missing.join('\n  ')}`);
	process.exit(1);
}

for (const [platform, jobs] of pending) {
	await rm(resolve(OUT, platform), { recursive: true, force: true });
	const results = await Promise.all(jobs.map(convert));
	const before = results.reduce((sum, item) => sum + item.before, 0);
	const after = results.reduce((sum, item) => sum + item.after, 0);
	console.log(`${platform}: ${jobs.length} screenshots (${mb(before)} -> ${mb(after)})`);
}
