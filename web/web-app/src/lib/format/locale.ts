import { getLocale } from '#lib/paraglide/runtime.js';

export type AppLocale = ReturnType<typeof getLocale>;

/**
 * The Intl locale for the app's language: British English keeps day-month dates and the
 * 24-hour clock, as students of an Italian university expect.
 */
export function intlLocale(locale: AppLocale = getLocale()): string {
	return locale === 'it' ? 'it-IT' : 'en-GB';
}

const formats = new Map<string, unknown>();

/** One Intl formatter per kind, locale and options: they are costly to build. */
export function cached<T>(kind: string, locale: string, options: object, build: () => T): T {
	const key = `${kind}|${locale}|${JSON.stringify(options)}`;
	let format = formats.get(key) as T | undefined;
	if (format === undefined) {
		format = build();
		formats.set(key, format);
	}
	return format;
}
