import { m } from '#lib/paraglide/messages.js';
import { cached, intlLocale, type AppLocale } from './locale.ts';

function numberFormat(locale: AppLocale | undefined, options: Intl.NumberFormatOptions) {
	const tag = intlLocale(locale);
	return cached('number', tag, options, () => new Intl.NumberFormat(tag, options));
}

export function formatNumber(
	value: number,
	options: Intl.NumberFormatOptions = {},
	locale?: AppLocale
): string {
	return numberFormat(locale, options).format(value);
}

/** `ratio` (0 to 1) as a percentage: 72%. */
export function formatPercent(ratio: number, locale?: AppLocale): string {
	return numberFormat(locale, { style: 'percent', maximumFractionDigits: 0 }).format(ratio);
}

/** An amount of money, euros by default: 156,00 €. */
export function formatCurrency(amount: number, currency = 'EUR', locale?: AppLocale): string {
	return numberFormat(locale, { style: 'currency', currency }).format(amount);
}

/** Credits, with a decimal only when there is one: 8 CFU, 7,5 CFU. */
export function formatCredits(credits: number, locale?: AppLocale): string {
	const value = numberFormat(locale, { maximumFractionDigits: 1 }).format(credits);
	return m.common_credits({ count: credits, value }, locale === undefined ? {} : { locale });
}

const BYTE_UNITS = ['byte', 'kilobyte', 'megabyte', 'gigabyte'] as const;

/**
 * A size in binary units, moving to the next one from 1000 so no size reads four digits: 900 byte,
 * 1 MB rather than 1021 kB, 1,5 GB. Bytes are spelled out, which pluralizes them in English.
 */
export function formatBytes(bytes: number, locale?: AppLocale): string {
	let value = bytes;
	let unit = 0;
	while (value >= 1000 && unit < BYTE_UNITS.length - 1) {
		value /= 1024;
		unit++;
	}
	return numberFormat(locale, {
		style: 'unit',
		unit: BYTE_UNITS[unit],
		unitDisplay: unit === 0 ? 'long' : 'short',
		maximumFractionDigits: value < 10 && unit > 0 ? 1 : 0
	}).format(value);
}
