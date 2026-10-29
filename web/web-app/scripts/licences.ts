/**
 * Writes `src/lib/features/settings/legal/licences.json`: the open-source packages whose code ships
 * in the web app, with their licence texts, for Settings › Licences. Run after changing
 * dependencies: `npm run licences`.
 */
import { existsSync, readdirSync, readFileSync, writeFileSync } from 'node:fs';
import path from 'node:path';

interface Licence {
	name: string;
	version: string;
	licence: string;
	url?: string;
	text?: string;
}

const root = path.resolve(import.meta.dirname, '..');

/**
 * Packages the browser runs. A trailing `*` takes the package's whole dependency tree too, for
 * libraries shipped as one bundle (MapLibre); the framework's compiler-only dependencies
 * stay out by naming the runtime ones.
 */
const SHIPPED = [
	'svelte',
	'clsx',
	'esm-env',
	'devalue',
	'@sveltejs/kit',
	'@inlang/paraglide-js',
	'@fontsource-variable/instrument-sans',
	'@fontsource-variable/newsreader',
	'@iconify-json/ph',
	'maplibre-gl*',
	'hls.js',
	'pdfjs-dist'
];

/** Where `name` is installed for a package in `from`: nested first, then hoisted to the root. */
function packageDir(name: string, from: string): string {
	for (let dir = from; ; dir = path.dirname(dir)) {
		const candidate = path.join(dir, 'node_modules', name);
		if (existsSync(path.join(candidate, 'package.json'))) return candidate;
		if (dir === root || path.dirname(dir) === dir) throw new Error(`${name} is not installed`);
	}
}

function repositoryUrl(manifest: Record<string, unknown>): string | undefined {
	const repository = manifest.repository;
	const raw =
		typeof repository === 'string'
			? repository
			: typeof repository === 'object' && repository !== null && 'url' in repository
				? String(repository.url)
				: typeof manifest.homepage === 'string'
					? manifest.homepage
					: undefined;
	if (raw === undefined) return undefined;
	const github = /^(?:github:)?([\w.-]+\/[\w.-]+)$/.exec(raw);
	if (github) return `https://github.com/${github[1]}`;
	return raw
		.replace(/^git\+/, '')
		.replace(/^git:\/\//, 'https://')
		.replace(/^ssh:\/\/git@/, 'https://')
		.replace(/^git@([\w.-]+):/, 'https://$1/')
		.replace(/\.git$/, '');
}

function licenceText(dir: string): string | undefined {
	const file = readdirSync(dir).find((entry) => /^(licen[cs]e|copying)(\.(md|txt))?$/i.test(entry));
	return file === undefined ? undefined : readFileSync(path.join(dir, file), 'utf8').trim();
}

const found = new Map<string, Licence>();

function collect(name: string, deep: boolean, from = root): void {
	if (found.has(name) || name.startsWith('@types/')) return;
	const dir = packageDir(name, from);
	const manifest = JSON.parse(readFileSync(path.join(dir, 'package.json'), 'utf8')) as Record<
		string,
		unknown
	>;
	// Iconify's icon sets describe the icons' own licence in info.json.
	const info = path.join(dir, 'info.json');
	const icons = existsSync(info)
		? (JSON.parse(readFileSync(info, 'utf8')) as { license?: { spdx?: string; url?: string } })
				.license
		: undefined;
	found.set(name, {
		name,
		version: String(manifest.version),
		licence: icons?.spdx ?? (typeof manifest.license === 'string' ? manifest.license : 'UNKNOWN'),
		url: icons?.url ?? repositoryUrl(manifest),
		text: licenceText(dir)
	});
	if (!deep) return;
	const dependencies = manifest.dependencies as Record<string, string> | undefined;
	for (const dependency of Object.keys(dependencies ?? {})) collect(dependency, true, dir);
}

for (const entry of SHIPPED) collect(entry.replace(/\*$/, ''), entry.endsWith('*'));

const own = path.resolve(root, '../../LICENSE');
const app: Licence = {
	name: 'MyBicocca',
	version: String(JSON.parse(readFileSync(path.join(root, 'package.json'), 'utf8')).version),
	licence: 'MIT',
	url: 'https://github.com/Auties00/MyBicocca',
	text: existsSync(own) ? readFileSync(own, 'utf8').trim() : undefined
};

const list = [...found.values()].sort((a, b) =>
	a.name.replace(/^@/, '').localeCompare(b.name.replace(/^@/, ''))
);
const output = path.join(root, 'src/lib/features/settings/legal/licences.json');
writeFileSync(output, `${JSON.stringify({ app, packages: list }, null, '\t')}\n`);
process.stdout.write(`${list.length} packages → ${path.relative(root, output)}
`);
