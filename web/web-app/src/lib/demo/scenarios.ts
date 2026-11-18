/** What a demo request does instead of (or besides) its usual answer. */
export type Scenario = 'slow' | 'error' | 'offline' | 'empty';

export type DemoService =
	'esse3' | 'moodle' | 'moodleWeb' | 'kaltura' | 'easystaff' | 'planning' | 'affluences';

/** Where the scenario rules live; see the doc comment of `router.ts`. */
export const SCENARIOS_KEY = 'mybicocca:demo-scenarios';

const SCENARIOS: ReadonlySet<string> = new Set(['slow', 'error', 'offline', 'empty']);
const SERVICES: ReadonlySet<string> = new Set([
	'esse3',
	'moodle',
	'moodleWeb',
	'kaltura',
	'easystaff',
	'planning',
	'affluences'
]);

/** The service a request goes to, by host and path. */
export function serviceOf(url: URL): DemoService | undefined {
	const host = url.hostname;
	if (host === 's3w.si.unimib.it') return 'esse3';
	if (host === 'elearning.unimib.it' || host === 'sp.elearning.unimib.it') {
		return url.pathname.startsWith('/webservice/') ? 'moodle' : 'moodleWeb';
	}
	if (host === 'kaltura.com' || host.endsWith('.kaltura.com')) return 'kaltura';
	if (host === 'gestioneorari.didattica.unimib.it') {
		return url.pathname.startsWith('/portaleplanningAPI/') ? 'planning' : 'easystaff';
	}
	if (host === 'affluences.com' || host.endsWith('.affluences.com')) return 'affluences';
	return undefined;
}

/**
 * The scenario `rules` give a request: the most specific matcher wins. Patterns with a method
 * beat patterns without, which beat service names, which beat `*`; between patterns, the one
 * with more literal characters wins.
 */
export function scenarioFor(
	rules: Readonly<Record<string, unknown>>,
	method: string,
	url: URL
): Scenario | undefined {
	let best: { rank: number; scenario: Scenario } | undefined;
	for (const [matcher, scenario] of Object.entries(rules)) {
		if (typeof scenario !== 'string' || !SCENARIOS.has(scenario)) continue;
		const rank = rankOf(matcher.trim(), method, url);
		if (rank !== undefined && (best === undefined || rank > best.rank)) {
			best = { rank, scenario: scenario as Scenario };
		}
	}
	return best?.scenario;
}

/** How specifically `matcher` matches the request, or undefined when it doesn't. */
function rankOf(matcher: string, method: string, url: URL): number | undefined {
	if (matcher === '*') return 0;
	if (SERVICES.has(matcher)) return serviceOf(url) === matcher ? 1 : undefined;
	const [first, ...rest] = matcher.split(/\s+/);
	const hasMethod = rest.length > 0;
	if (hasMethod && first.toUpperCase() !== method.toUpperCase()) return undefined;
	const glob = hasMethod ? rest.join(' ') : first;
	const target = glob.includes('?')
		? `${url.origin}${url.pathname}${url.search}`
		: `${url.origin}${url.pathname}`;
	if (!globPattern(glob).test(target)) return undefined;
	const literal = glob.replace(/\*|:[^/?&]+/g, '').length;
	return (hasMethod ? 3_000_000 : 2_000_000) + literal;
}

/** `*` matches any text, `:name` one path segment; the rest is literal. */
function globPattern(glob: string): RegExp {
	const source = glob
		.split(/(\*|:[^/?&]+)/)
		.map((part) => {
			if (part === '*') return '.*';
			if (part.startsWith(':')) return '[^/?]+';
			return part.replace(/[.+?^${}()|[\]\\]/g, '\\$&');
		})
		.join('');
	// A query in the pattern only has to begin the request's, as Moodle adds its own settings.
	return new RegExp(`^${source}${glob.includes('?') ? '(&.*)?' : ''}$`);
}

/** The rules set from DevTools; anything unreadable counts as none. */
export function readScenarioRules(): Record<string, unknown> {
	try {
		const value: unknown = JSON.parse(localStorage.getItem(SCENARIOS_KEY) ?? '{}');
		return typeof value === 'object' && value !== null ? (value as Record<string, unknown>) : {};
	} catch {
		return {};
	}
}
