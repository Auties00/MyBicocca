import { version as buildId } from '$app/env';
import { version } from '../../../../package.json';

/** The web app's own version. */
export const appVersion = version;

/** When this build was made: SvelteKit's build id is its timestamp unless configured otherwise. */
export const buildDate = /^\d{12,}$/.test(buildId) ? new Date(Number(buildId)) : undefined;

const COPYRIGHT_SINCE = 2025;
export const COPYRIGHT_HOLDER = '100% Attendance';

/** "2025–2026": from the first year to the current one, or the first year alone. */
export function copyrightYears(now = new Date()): string {
	const year = now.getFullYear();
	return year > COPYRIGHT_SINCE ? `${COPYRIGHT_SINCE}–${year}` : `${COPYRIGHT_SINCE}`;
}

export interface Credit {
	name: string;
	github: string;
}

/** The people behind MyBicocca, as the Android app credits them. */
export const credits: readonly Credit[] = [
	{ name: 'Alessandro Autiero', github: 'Auties00' },
	{ name: 'Lorenzo Angelo Lupi', github: 'LordLux' },
	{ name: 'Alessandro Ferrari Pagini', github: 'AleFerroExe' },
	{ name: 'Federico Giarrusso', github: 'Fedogia' }
];
