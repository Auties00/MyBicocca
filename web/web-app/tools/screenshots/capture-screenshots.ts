/**
 * Retakes the web app's screenshots from its demo build, as Android's capture tool does from its
 * demo flavour.
 *
 * Builds the app in demo mode, serves it with `vite preview` on a free port, pins the clock to
 * Tuesday, 27 October 2026 at 10:15 (Europe/Rome), then walks the catalogue in screenshots.json
 * and saves one PNG per shot, per language, per screen size, per palette and per light/dark mode.
 * Each of those combinations starts from a fresh browser profile, where the catalogue's `setup`
 * visits run first.
 *
 *     npm run screens
 *     npm run screens -- --sizes mobile --modes dark --only Screenshot_Fees Screenshot_Isee
 *     npm run screens -- --changed origin/main
 *     npm run screens -- --check-missing
 *     npm run screens -- --palettes bicocca oceano bosco --languages en
 *     npm run screens -- --skip-build
 *
 * Output goes to tools/screenshots/<language>/<size>/<light|dark>/, next to this script, with
 * other palettes in <language>/<size>/<palette>-<mode>/. Languages default to every one the app
 * declares. Problems (a step that found nothing, a page that never finished loading, a console
 * error) are listed at the end and make the run exit with 1.
 */
import { spawn, spawnSync, type ChildProcess } from 'node:child_process';
import { existsSync } from 'node:fs';
import { mkdir, readFile } from 'node:fs/promises';
import { createServer } from 'node:net';
import { dirname, relative, resolve } from 'node:path';
import type { ElementHandle, Page, Viewport } from 'puppeteer-core';

const HERE = import.meta.dirname;
const ROOT = resolve(HERE, '../..');
const REPO = resolve(ROOT, '../..');
const CATALOG = resolve(HERE, 'screenshots.json');
const VITE = resolve(ROOT, 'node_modules/vite/bin/vite.js');
const WEB_PREFIX = 'web/web-app/';
const ROUTES_PREFIX = 'src/routes/(app)/';
const FEATURES_PREFIX = 'src/lib/features/';
const DEFAULT_PALETTE = 'bicocca';
const PALETTES = ['bicocca', 'oceano', 'bosco'];
const MODES = ['light', 'dark'] as const;
const TIMEZONE = 'Europe/Rome';
/*
 * The demo's data follows the clock, so every run pins it to the same moment in Rome: Tuesday,
 * 27 October 2026 at 10:15, mid-morning with lessons on. Freezing the date keeps CI reruns
 * from churning every calendar and deadline image from one week to the next.
 */
const CAPTURE_AT = Date.parse('2026-10-27T10:15:00+01:00');
const SIZES = {
	desktop: { width: 1280, height: 800, deviceScaleFactor: 2 },
	tablet: { width: 820, height: 1180, deviceScaleFactor: 2, isMobile: true, hasTouch: true },
	mobile: { width: 390, height: 844, deviceScaleFactor: 3, isMobile: true, hasTouch: true }
} satisfies Record<string, Viewport>;
/** Longest wait for a page, or a step's result, to finish loading. */
const LOAD_TIMEOUT_MS = 30_000;

/** Maps feature folders (and message files of the same name) to the route prefixes they render. */
const FEATURE_ROUTES: Record<string, readonly string[]> = {
	appointments: ['services/appointments'],
	assignment: ['elearning/*/assignment'],
	attendance: ['services/attendance'],
	calendar: ['calendar'],
	course: ['elearning/*'],
	documents: ['services/enrollments', 'services/degrees', 'services/certificates'],
	elearning: ['elearning'],
	exams: ['services/exams'],
	forum: ['elearning/*/forum'],
	library: ['services/library'],
	map: ['map'],
	payments: ['services/fees', 'services/isee', 'services/refunds'],
	profile: ['profile'],
	questionnaires: ['services/questionnaires'],
	quiz: ['elearning/*/quiz'],
	results: ['services/results'],
	services: ['services'],
	settings: ['settings'],
	studyplan: ['services/study-plan']
};

/** Shell files whose UI is captured in the account switcher shot. */
const ACCOUNT_SHELL_FILES = new Set([
	'src/lib/shell/AccountMenu.svelte',
	'src/lib/shell/account.ts',
	'src/lib/shell/sessionAccounts.ts'
]);

type Size = keyof typeof SIZES;
type Mode = (typeof MODES)[number];

/** One action on the page, after it has loaded. `sizes` limits it to those screen sizes. */
interface Step {
	/** Clicks the first visible element the CSS selector matches. */
	click?: string;
	/** Clicks the first visible control whose text or label contains this. */
	text?: string;
	/** Scrolls the first visible element the CSS selector matches to the top. */
	scroll?: string;
	sizes?: Size[];
}

interface Visit {
	/** The route, relative to the app's root, with its search params. */
	route: string;
	steps?: Step[];
}

interface Shot extends Visit {
	name: string;
}

interface Catalogue {
	shots: Shot[];
	/** Visits made once in each fresh profile before its shots, for choices a student makes once. */
	setup?: Visit[];
}

interface Options {
	languages: string[];
	sizes: Size[];
	modes: Mode[];
	palettes: string[];
	only: string[] | undefined;
	changed: string | undefined;
	listChanged: string | undefined;
	checkMissing: boolean;
	out: string;
	skipBuild: boolean;
	settle: number;
}

/** `--name value value --flag`, as Android's tool takes them; `--name a,b` works too. */
function parseOptions(argv: string[], languages: string[]): Options {
	const values = new Map<string, string[]>();
	let current: string[] | undefined;
	for (const arg of argv) {
		if (arg.startsWith('--')) {
			current = [];
			values.set(arg.slice(2), current);
		} else if (current) {
			current.push(...arg.split(',').filter(Boolean));
		} else {
			fail(`Unexpected argument ${arg}`);
		}
	}
	const known = [
		'languages',
		'sizes',
		'modes',
		'palettes',
		'only',
		'changed',
		'list-changed',
		'check-missing',
		'out',
		'skip-build',
		'settle'
	];
	for (const name of values.keys()) if (!known.includes(name)) fail(`Unknown option --${name}`);

	const list = <T extends string>(name: string, allowed: readonly T[], fallback: T[]): T[] => {
		const given = values.get(name);
		if (given === undefined) return fallback;
		const wrong = given.filter((value) => !allowed.includes(value as T));
		if (given.length === 0 || wrong.length > 0) {
			fail(`--${name} takes ${allowed.join(', ')}; got ${given.join(', ') || 'nothing'}`);
		}
		return given as T[];
	};
	const single = (name: string): string | undefined => {
		const given = values.get(name);
		if (given === undefined) return undefined;
		if (given.length !== 1) fail(`--${name} takes a single value`);
		return given[0];
	};
	const sizes = Object.keys(SIZES) as Size[];
	return {
		languages: list('languages', languages, languages),
		sizes: list('sizes', sizes, sizes),
		modes: list('modes', MODES, [...MODES]),
		palettes: list('palettes', PALETTES, [DEFAULT_PALETTE]),
		only: values.get('only'),
		changed: single('changed'),
		listChanged: single('list-changed'),
		checkMissing: values.has('check-missing'),
		out: resolve(values.get('out')?.[0] ?? HERE),
		skipBuild: values.has('skip-build'),
		settle: Number(values.get('settle')?.[0] ?? 600)
	};
}

function fail(message: string): never {
	console.error(message);
	process.exit(2);
}

/** The languages the app declares, base language first, so a new translation is picked up. */
async function appLanguages(): Promise<string[]> {
	const settings = JSON.parse(
		await readFile(resolve(ROOT, 'project.inlang/settings.json'), 'utf8')
	) as { baseLocale: string; locales: string[] };
	return [settings.baseLocale, ...settings.locales.filter((l) => l !== settings.baseLocale)];
}

async function catalogue(only: string[] | undefined): Promise<Required<Catalogue>> {
	const { shots, setup = [] } = JSON.parse(await readFile(CATALOG, 'utf8')) as Catalogue;
	if (only === undefined) return { shots, setup };
	const unknown = only.filter((name) => !shots.some((shot) => shot.name === name));
	if (unknown.length > 0) fail(`Not in the catalogue: ${unknown.join(', ')}`);
	return { shots: shots.filter((shot) => only.includes(shot.name)), setup };
}

function shotFolder(
	out: string,
	language: string,
	size: Size,
	palette: string,
	mode: Mode
): string {
	const folder = palette === DEFAULT_PALETTE ? mode : `${palette}-${mode}`;
	return resolve(out, language, size, folder);
}

/** Shot names missing at least one PNG on disk for the requested languages, sizes, palettes and modes. */
function missingShots(shots: Shot[], options: Options): string[] {
	return shots
		.filter((shot) =>
			options.languages.some((language) =>
				options.sizes.some((size) =>
					options.palettes.some((palette) =>
						options.modes.some(
							(mode) =>
								!existsSync(
									resolve(
										shotFolder(options.out, language, size, palette, mode),
										`${shot.name}.png`
									)
								)
						)
					)
				)
			)
		)
		.map((shot) => shot.name);
}

function routePath(shot: Shot): string {
	return shot.route.split('?')[0];
}

function segmentMatches(pattern: string, segment: string): boolean {
	if (pattern === '*' || (pattern.startsWith('[') && pattern.endsWith(']'))) return true;
	return pattern === segment;
}

function routeMatchesPrefix(route: string, pattern: string, exact = false): boolean {
	const routeParts = route.split('/').filter(Boolean);
	const patternParts = pattern.split('/').filter(Boolean);
	if (exact && routeParts.length !== patternParts.length) return false;
	if (routeParts.length < patternParts.length) return false;
	return patternParts.every((part, index) => segmentMatches(part, routeParts[index]));
}

/** Shot names whose route or feature lives under a changed path in `web/web-app/`. */
function shotsForWebPath(shots: Shot[], relPath: string): string[] {
	if (relPath.startsWith(ROUTES_PREFIX)) {
		const inner = relPath.slice(ROUTES_PREFIX.length);
		const parts = inner.split('/');
		const file = parts.pop() ?? '';
		if (parts.length === 0) return shots.map((shot) => shot.name);
		const pattern = parts.join('/');
		const exact = file.startsWith('+page.');
		return shots
			.filter((shot) => routeMatchesPrefix(routePath(shot), pattern, exact))
			.map((shot) => shot.name);
	}

	if (ACCOUNT_SHELL_FILES.has(relPath)) {
		return shots
			.filter((shot) => shot.name === 'Screenshot_AccountSwitcher')
			.map((shot) => shot.name);
	}

	let area: string | undefined;
	if (relPath.startsWith(FEATURES_PREFIX)) {
		area = relPath.slice(FEATURES_PREFIX.length).split('/')[0];
	} else {
		const messageMatch = /^messages\/[^/]+\/([^/]+)\.json$/.exec(relPath);
		if (messageMatch) area = messageMatch[1];
	}
	if (!area) return [];

	const patterns = FEATURE_ROUTES[area];
	if (!patterns) return [];
	return shots
		.filter((shot) => {
			const route = routePath(shot);
			if (area === 'course' && route === 'elearning/catalog') return false;
			return patterns.some((pattern) => routeMatchesPrefix(route, pattern));
		})
		.map((shot) => shot.name);
}

/** Shots that are missing on disk, altered in screenshots.json, or backed by a changed route/feature. */
function changedShots(shots: Shot[], setup: Visit[], baseRef: string, options: Options): string[] {
	const needed = new Set(missingShots(shots, options));

	const catalogRel = relative(REPO, CATALOG).replaceAll('\\', '/');
	const baseCatalog = spawnSync('git', ['show', `${baseRef}:${catalogRel}`], {
		cwd: REPO,
		encoding: 'utf8'
	});
	if (baseCatalog.status === 0) {
		const parsed = JSON.parse(baseCatalog.stdout) as Catalogue;
		if (JSON.stringify(parsed.setup ?? []) !== JSON.stringify(setup)) {
			return shots.map((shot) => shot.name);
		}
		const before = new Map((parsed.shots ?? []).map((shot) => [shot.name, JSON.stringify(shot)]));
		for (const shot of shots) {
			if (before.get(shot.name) !== JSON.stringify(shot)) needed.add(shot.name);
		}
	}

	const diff = spawnSync('git', ['diff', '--name-only', `${baseRef}...HEAD`], {
		cwd: REPO,
		encoding: 'utf8'
	});
	if (diff.status === 0) {
		for (const raw of diff.stdout.split(/\r?\n/)) {
			const path = raw.trim().replaceAll('\\', '/');
			if (path.startsWith(WEB_PREFIX)) {
				for (const name of shotsForWebPath(shots, path.slice(WEB_PREFIX.length))) {
					needed.add(name);
				}
			}
		}
	}

	return shots.filter((shot) => needed.has(shot.name)).map((shot) => shot.name);
}

function build(): void {
	console.log('Building the demo...');
	const result = spawnSync(process.execPath, [VITE, 'build', '--mode', 'demo'], {
		cwd: ROOT,
		stdio: ['ignore', 'ignore', 'pipe'],
		encoding: 'utf8'
	});
	if (result.status !== 0) fail(`The demo build failed:\n${result.stderr}`);
}

function freePort(): Promise<number> {
	return new Promise((done, failed) => {
		const server = createServer();
		server.once('error', failed);
		server.listen(0, () => {
			const address = server.address();
			server.close(() =>
				typeof address === 'object' && address !== null
					? done(address.port)
					: failed(new Error('No port'))
			);
		});
	});
}

/** Serves the last demo build, answering once it is up. */
async function preview(): Promise<{ origin: string; server: ChildProcess }> {
	const port = await freePort();
	const server = spawn(
		process.execPath,
		[VITE, 'preview', '--mode', 'demo', '--port', String(port), '--strictPort'],
		{ cwd: ROOT, stdio: 'ignore' }
	);
	const origin = `http://localhost:${port}`;
	const deadline = Date.now() + 60_000;
	while (Date.now() < deadline && server.exitCode === null) {
		const up = await fetch(origin).then(
			(response) => response.ok,
			() => false
		);
		if (up) return { origin, server };
		await delay(300);
	}
	server.kill();
	return fail('The preview server did not start.');
}

const delay = (ms: number) => new Promise((done) => setTimeout(done, ms));

/** Runs in the page before its own scripts: the clock, the language and the palette. */
function prepareDocument(at: number, language: string, palette: string): void {
	const offset = at - Date.now();
	const RealDate = Date;
	class PinnedDate extends RealDate {
		constructor(...args: ConstructorParameters<typeof Date> | []) {
			if (args.length === 0) super(RealDate.now() + offset);
			else super(...(args as ConstructorParameters<typeof Date>));
		}

		static override now() {
			return RealDate.now() + offset;
		}
	}
	globalThis.Date = PinnedDate as DateConstructor;
	// The app's own choice (settings/languageStrategy.ts), Paraglide's, and the theme (ui/theme.svelte.ts).
	localStorage.setItem('mybicocca:language', language);
	localStorage.setItem('PARAGLIDE_LOCALE', language);
	localStorage.setItem('mybicocca:theme', JSON.stringify({ mode: 'system', palette }));
}

/**
 * Waits until the page shows its data: no skeleton (ui/Skeleton.svelte's `.sk`), a map past its
 * first load (map/components/MapCanvas.svelte marks it `.ready`), no request in flight.
 */
async function settle(page: Page, settleMs: number): Promise<void> {
	await page.waitForFunction(() => !document.querySelector('.sk, .maplibregl-map:not(.ready)'), {
		timeout: LOAD_TIMEOUT_MS
	});
	await page.waitForNetworkIdle({ idleTime: 400, timeout: LOAD_TIMEOUT_MS }).catch(() => undefined);
	await page.evaluate(() => document.fonts.ready.then(() => undefined));
	await delay(settleMs);
}

async function firstVisible(page: Page, selector: string): Promise<ElementHandle | undefined> {
	for (const handle of await page.$$(selector)) {
		if (await handle.evaluate((element) => element.checkVisibility())) return handle;
	}
	return undefined;
}

async function controlWithText(page: Page, text: string): Promise<ElementHandle | undefined> {
	const handle = await page.evaluateHandle(
		(wanted) =>
			[...document.querySelectorAll('a, button, [role="tab"], [role="option"], summary')].find(
				(element) =>
					element.checkVisibility() &&
					(element.textContent?.includes(wanted) ||
						element.getAttribute('aria-label')?.includes(wanted))
			) ?? null,
		text
	);
	const element = handle.asElement();
	return element === null ? undefined : (element as ElementHandle);
}

/** Runs the visit's steps for this size; returns what went wrong. */
async function runSteps(page: Page, visit: Visit, size: Size): Promise<string[]> {
	const problems: string[] = [];
	for (const step of visit.steps ?? []) {
		if (step.sizes && !step.sizes.includes(size)) continue;
		const target = step.click ?? step.scroll ?? step.text;
		const element =
			step.text !== undefined
				? await controlWithText(page, step.text)
				: await firstVisible(page, step.click ?? step.scroll ?? '');
		if (element === undefined) {
			problems.push(`nothing on screen matches "${target}"`);
			continue;
		}
		if (step.scroll !== undefined) {
			await element.evaluate((node) => node.scrollIntoView({ block: 'start' }));
		} else if ('hasTouch' in SIZES[size]) {
			// A mouse would leave whatever slides under it after the click hovered.
			await element.tap();
		} else {
			await element.click();
		}
	}
	return problems;
}

/** Loads the visit's route and runs its steps, each time until the page settles. */
async function open(page: Page, origin: string, visit: Visit, size: Size, settleMs: number) {
	const problems: string[] = [];
	try {
		await page.goto(`${origin}/${visit.route}`, { waitUntil: 'load' });
		await page.waitForSelector('main h1', { visible: true, timeout: LOAD_TIMEOUT_MS });
		await settle(page, settleMs);
		problems.push(...(await runSteps(page, visit, size)));
		if (visit.steps?.length) await settle(page, settleMs);
	} catch (error) {
		problems.push(`did not finish loading (${error instanceof Error ? error.message : error})`);
	}
	return problems;
}

async function main(): Promise<void> {
	const options = parseOptions(process.argv.slice(2), await appLanguages());
	let { shots } = await catalogue(options.only);
	const { setup } = await catalogue(options.only);

	if (options.checkMissing) {
		const missing = missingShots(shots, options);
		for (const name of missing) console.log(name);
		process.exit(missing.length > 0 ? 1 : 0);
	}

	if (options.listChanged) {
		for (const name of changedShots(shots, setup, options.listChanged, options)) {
			console.log(name);
		}
		return;
	}

	if (options.changed) {
		const names = changedShots(shots, setup, options.changed, options);
		if (names.length === 0) {
			console.log('No new, changed or missing screenshots.');
			return;
		}
		console.log(`Capturing ${names.length} changed/missing shot(s): ${names.join(', ')}`);
		const selected = new Set(names);
		shots = shots.filter((shot) => selected.has(shot.name));
	}

	if (!options.skipBuild) build();
	const { origin, server } = await preview();
	const { default: puppeteer } = await import('puppeteer-core');
	const executablePath = process.env.CHROME_PATH || process.env.PUPPETEER_EXECUTABLE_PATH;
	// On headless Linux CI runners without a GPU, SwiftShader keeps MapLibre WebGL rendering.
	const launchArgs =
		process.platform === 'linux'
			? [
					'--no-sandbox',
					'--disable-setuid-sandbox',
					'--disable-dev-shm-usage',
					'--use-gl=angle',
					'--use-angle=swiftshader',
					'--enable-unsafe-swiftshader'
				]
			: [];
	const browser = await puppeteer.launch({
		...(executablePath ? { executablePath } : { channel: 'chrome' }),
		headless: true,
		args: launchArgs
	});
	const problems: string[] = [];
	try {
		for (const language of options.languages) {
			for (const size of options.sizes) {
				for (const palette of options.palettes) {
					for (const mode of options.modes) {
						const folder = shotFolder(options.out, language, size, palette, mode);
						const where = `${language}/${size}/${palette === DEFAULT_PALETTE ? mode : `${palette}-${mode}`}`;
						console.log(`\n${where}`);
						// A fresh profile each time: the demo signs in anew, nothing carries over.
						const context = await browser.createBrowserContext();
						const page = await context.newPage();
						page.setDefaultTimeout(LOAD_TIMEOUT_MS);
						await page.setViewport(SIZES[size]);
						await page.emulateTimezone(TIMEZONE);
						await page.emulateMediaFeatures([
							{ name: 'prefers-color-scheme', value: mode },
							{ name: 'prefers-reduced-motion', value: 'reduce' }
						]);
						await page.evaluateOnNewDocument(prepareDocument, CAPTURE_AT, language, palette);
						let errors: string[] = [];
						page.on('console', (message) => {
							if (message.type() === 'error') errors.push(`console error: ${message.text()}`);
						});
						page.on('pageerror', (error) => {
							errors.push(`uncaught: ${error instanceof Error ? error.message : error}`);
						});
						try {
							for (const visit of setup) {
								errors = [];
								const found = await open(page, origin, visit, size, options.settle);
								for (const problem of [...found, ...errors]) {
									problems.push(`${where}/setup ${visit.route}: ${problem}`);
								}
							}
							for (const shot of shots) {
								errors = [];
								const found = await open(page, origin, shot, size, options.settle);
								const path = resolve(folder, `${shot.name}.png`);
								await mkdir(dirname(path), { recursive: true });
								await page.screenshot({ path, type: 'png' });
								const all = [...found, ...errors];
								for (const problem of all) problems.push(`${where}/${shot.name}: ${problem}`);
								console.log(`  ${shot.name}${all.length > 0 ? ' (with problems)' : ''}`);
							}
						} finally {
							await context.close();
						}
					}
				}
			}
		}
	} finally {
		await browser.close();
		server.kill();
	}

	if (problems.length > 0) {
		console.log('\nFinished with problems:');
		for (const problem of problems) console.log(`  ${problem}`);
		process.exit(1);
	}
	console.log('\nDone.');
}

await main();
