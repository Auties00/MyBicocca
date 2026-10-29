import {
	baseLocale,
	extractLocaleFromNavigator,
	getLocale,
	type Locale
} from '#lib/paraglide/runtime.js';
import { LANGUAGE_KEY, storedLanguage } from './languageStrategy.ts';

export type LanguageChoice = 'system' | Locale;

/** The language the system asks for, if the app speaks it, else Italian. */
export function systemLocale(): Locale {
	return extractLocaleFromNavigator() ?? baseLocale;
}

/** The system's first language when the app does not speak any of the system's languages. */
export function untranslatedSystemLanguage(): string | undefined {
	return extractLocaleFromNavigator() === undefined ? navigator.language : undefined;
}

/**
 * A language's name in the app's current language, as in running text ("inglese", "German"), or
 * capitalized to stand alone as a value ("Inglese").
 */
export function languageName(
	tag: string,
	standalone = false,
	locale: Locale = getLocale()
): string {
	let name = tag;
	try {
		name = new Intl.DisplayNames([locale], { type: 'language' }).of(tag) ?? tag;
	} catch {
		// An unknown tag is shown as it is.
	}
	return standalone ? name.charAt(0).toLocaleUpperCase(locale) + name.slice(1) : name;
}

/**
 * The language picked in Settings, which languageStrategy.ts reads first on every load; with none
 * stored, Paraglide follows the device's languages.
 */
class LanguagePreference {
	/** The language the app speaks now; picking another reloads the app in it. */
	readonly current: Locale = getLocale();

	choice = $state<LanguageChoice>(storedLanguage() ?? 'system');

	/**
	 * Applies `choice`: when the language changes the app reloads in it (Paraglide's messages are
	 * compiled per language), keeping the page and the cached data.
	 */
	set(choice: LanguageChoice): void {
		this.choice = choice;
		try {
			if (choice === 'system') localStorage.removeItem(LANGUAGE_KEY);
			else localStorage.setItem(LANGUAGE_KEY, choice);
		} catch {
			// Storage refused: the language still changes for this visit.
		}
		// Paraglide reads the stored choice through languageStrategy.ts, so a reload is all it takes.
		if ((choice === 'system' ? systemLocale() : choice) !== this.current) location.reload();
	}
}

export const language = new LanguagePreference();
