import { en } from './en.ts';
import { it } from './it.ts';

export const locales = ['it', 'en'] as const;
export type Locale = (typeof locales)[number];

const dictionaries = { it, en } as const;

export function t(locale: Locale) {
	return dictionaries[locale];
}
