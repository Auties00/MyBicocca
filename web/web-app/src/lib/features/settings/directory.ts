import IconPalette from '~icons/ph/palette';
import IconTranslate from '~icons/ph/translate';
import IconLockSimple from '~icons/ph/lock-simple';
import IconFileArrowDown from '~icons/ph/file-arrow-down';
import IconInfo from '~icons/ph/info';
import IconShieldCheck from '~icons/ph/shield-check';
import IconScroll from '~icons/ph/scroll';
import IconVibrate from '~icons/ph/vibrate';
import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';
import { fileOpenPreferences } from '#lib/features/course/file/preference.svelte.ts';
import { m } from '#lib/paraglide/messages.js';
import { theme } from '#lib/ui/theme.svelte.ts';
import { haptics } from '#lib/ui/haptics.svelte.ts';
import type { Icon, Tone } from '#lib/ui/types.ts';
import { appVersion } from './about.ts';
import { language, languageName } from './language.svelte.ts';
import { appLock } from './lock/appLock.svelte.ts';
import { modeLabels, paletteNames } from './labels.ts';
import { timeoutLabel } from './lock/labels.ts';

interface EntryBase {
	icon: Icon;
	tone: Tone;
	title: () => string;
	/** The current value, read reactively. */
	description: () => string;
	/** Listed only while this holds. */
	available?: () => boolean;
}

/** A section of its own, opened by its address. */
export interface SettingsLink extends EntryBase {
	id: 'appearance' | 'language' | 'security' | 'files' | 'about' | 'privacy' | 'licences';
	href: ResolvedPathname;
}

/** A switch set right in the list. */
export interface SettingsToggle extends EntryBase {
	id: 'haptics';
	get: () => boolean;
	set: (on: boolean) => void;
}

export type SettingsEntry = SettingsLink | SettingsToggle;

export interface SettingsGroup {
	id: 'preferences' | 'information';
	title: () => string;
	description: () => string;
	entries: readonly SettingsEntry[];
}

function languageDescription(): string {
	const name = languageName(language.current, true);
	return language.choice === 'system' ? m.settings_language_system_named({ language: name }) : name;
}

function securityDescription(): string {
	const config = appLock.config;
	if (config === undefined) return m.settings_security_off();
	return config.timeout === 0
		? m.settings_security_on_immediately()
		: m.settings_security_on_after({ time: timeoutLabel(config.timeout) });
}

function filesDescription(): string {
	const count = Object.keys(fileOpenPreferences.choices).length;
	return count === 0 ? m.settings_files_all_ask() : m.settings_files_custom({ count });
}

/** Whether `entry` is listed now. */
export function listed(entry: SettingsEntry): boolean {
	return entry.available?.() ?? true;
}

export const settingsGroups: readonly SettingsGroup[] = [
	{
		id: 'preferences',
		title: m.settings_preferences_title,
		description: m.settings_preferences_subtitle,
		entries: [
			{
				id: 'appearance',
				href: resolve('settings/appearance'),
				icon: IconPalette,
				tone: 'accent',
				title: m.settings_appearance_title,
				description: () => `${paletteNames[theme.palette]} · ${modeLabels[theme.mode]()}`
			},
			{
				id: 'language',
				href: resolve('settings/language'),
				icon: IconTranslate,
				tone: 'accent',
				title: m.settings_language_title,
				description: languageDescription
			},
			{
				id: 'haptics',
				icon: IconVibrate,
				tone: 'accent',
				title: m.settings_haptics_title,
				description: m.settings_haptics_description,
				available: () => haptics.supported,
				get: () => haptics.enabled,
				set: (on) => {
					haptics.enabled = on;
					haptics.tap();
				}
			},
			{
				id: 'security',
				href: resolve('settings/security'),
				icon: IconLockSimple,
				tone: 'accent',
				title: m.settings_security_title,
				description: securityDescription
			},
			{
				id: 'files',
				href: resolve('settings/files'),
				icon: IconFileArrowDown,
				tone: 'accent',
				title: m.settings_files_title,
				description: filesDescription
			}
		]
	},
	{
		id: 'information',
		title: m.settings_information_title,
		description: m.settings_information_subtitle,
		entries: [
			{
				id: 'about',
				href: resolve('settings/about'),
				icon: IconInfo,
				tone: 'neutral',
				title: m.settings_about_title,
				description: () => m.settings_about_version({ version: appVersion })
			},
			{
				id: 'privacy',
				href: resolve('settings/privacy'),
				icon: IconShieldCheck,
				tone: 'neutral',
				title: m.settings_privacy_title,
				description: m.settings_privacy_subtitle
			},
			{
				id: 'licences',
				href: resolve('settings/licences'),
				icon: IconScroll,
				tone: 'neutral',
				title: m.settings_licences_title,
				description: m.settings_licences_subtitle
			}
		]
	}
];
