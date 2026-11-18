import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';
import { allDestinations, type DestinationId } from '#lib/ui/destinations.ts';

/*
 * Every app path the shell links to (see ia.md), resolved once. One `resolve` call per literal keeps
 * each checked against the generated routes, which a call over the whole union can't be.
 */
const hrefs = {
	calendar: resolve('calendar'),
	elearning: resolve('elearning'),
	map: resolve('map'),
	services: resolve('services'),
	profile: resolve('profile'),
	'profile/exams': resolve('profile/exams'),
	'profile/simulator': resolve('profile/simulator'),
	settings: resolve('settings'),
	'settings/appearance': resolve('settings/appearance'),
	'settings/language': resolve('settings/language'),
	'settings/security': resolve('settings/security'),
	'settings/files': resolve('settings/files'),
	'settings/about': resolve('settings/about'),
	'settings/about/releases': resolve('settings/about/releases'),
	'settings/licences': resolve('settings/licences'),
	'settings/privacy': resolve('settings/privacy'),
	'services/deadlines': resolve('services/deadlines'),
	'services/study-plan': resolve('services/study-plan'),
	'services/attendance': resolve('services/attendance'),
	'services/attendance/check-in': resolve('services/attendance/check-in'),
	'services/results': resolve('services/results'),
	'services/questionnaires': resolve('services/questionnaires'),
	'services/exams': resolve('services/exams'),
	'services/appointments': resolve('services/appointments'),
	'services/library': resolve('services/library'),
	'services/enrollments': resolve('services/enrollments'),
	'services/degrees': resolve('services/degrees'),
	'services/certificates': resolve('services/certificates'),
	'services/fees': resolve('services/fees'),
	'services/isee': resolve('services/isee'),
	'services/refunds': resolve('services/refunds'),
	login: resolve('login')
} satisfies Record<string, ResolvedPathname>;

export type ShellPath = keyof typeof hrefs;

export function shellHref(path: ShellPath): ResolvedPathname {
	return hrefs[path];
}

/** The destination a pathname belongs to, by prefix. */
export function activeDestination(pathname: string): DestinationId | undefined {
	return allDestinations.find((d) => {
		const href = shellHref(d.path);
		return pathname === href || pathname.startsWith(`${href}/`);
	})?.id;
}
