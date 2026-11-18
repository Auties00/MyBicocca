import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';
import { choicesQuery, type WizardChoices } from './wizard.ts';

const withQuery = (path: ResolvedPathname, query: string) => `${path}${query}` as ResolvedPathname;

export function homeHref(): ResolvedPathname {
	return resolve('services/library');
}

/** The email check; `next` is where to go once the email is verified. */
export function loginHref(next?: ResolvedPathname): ResolvedPathname {
	const path = resolve('services/library/login');
	return next ? withQuery(path, `?next=${encodeURIComponent(next)}`) : path;
}

/** Only a page of this app may follow the email check. */
export function safeNext(value: string | null): ResolvedPathname | undefined {
	return value && value.startsWith(homeHref()) && !value.startsWith('//')
		? (value as ResolvedPathname)
		: undefined;
}

export function libraryHref(siteId: string): ResolvedPathname {
	return resolve('/(app)/services/library/[site]', { site: siteId });
}

export function bookHref(siteId: string, choices: WizardChoices = {}): ResolvedPathname {
	return withQuery(
		resolve('/(app)/services/library/[site]/book', { site: siteId }),
		choicesQuery(choices)
	);
}

export function reservationHref(id: number): ResolvedPathname {
	return resolve('/(app)/services/library/bookings/[id]', { id: String(id) });
}

/** The booking's outcome on the wizard's own URL, so a reload never offers the seat again. */
export function bookedHref(
	siteId: string,
	choices: WizardChoices,
	reservation: number | null
): ResolvedPathname {
	const href = bookHref(siteId, choices);
	return withQuery(href, `${href.includes('?') ? '&' : '?'}booked=${reservation ?? 'yes'}`);
}

/** The page an emailed cancellation link stands for; without a token it asks for the link. */
export function cancelHref(token?: string): ResolvedPathname {
	const path = resolve('services/library/cancel');
	return token ? withQuery(path, `?reservationToken=${encodeURIComponent(token)}`) : path;
}
