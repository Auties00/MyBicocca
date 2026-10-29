import {
	defineCustomClientStrategy,
	getLocale,
	locales,
	overwriteGetLocale,
	type Locale
} from '#lib/paraglide/runtime.js';

/** The language picked in Settings; absent while it follows the system. */
export const LANGUAGE_KEY = 'mybicocca:language';

/** Paraglide's name for the strategy below, which comes first in the configured `strategy`. */
const LANGUAGE_STRATEGY = 'custom-language';

export function storedLanguage(): Locale | undefined {
	try {
		const stored = localStorage.getItem(LANGUAGE_KEY);
		return locales.find((locale) => locale === stored);
	} catch {
		return undefined;
	}
}

/*
 * The language a pinned choice names, or none so that the next strategies (`preferredLanguage`,
 * then `baseLocale`) follow the browser's languages on every load. Its setLocale does nothing:
 * Paraglide calls it with whatever it resolved at start, which would pin "System" to today's
 * language. `language.set` writes the choice itself. hooks.client.ts must import this module
 * first, before any message renders.
 */
defineCustomClientStrategy(LANGUAGE_STRATEGY, {
	getLocale: storedLanguage,
	setLocale: () => {}
});

/*
 * The language changes only with a reload, so it is resolved once rather than by every message
 * (each resolution reads storage and parses the browser's languages).
 */
const locale = getLocale();
overwriteGetLocale(() => locale);
