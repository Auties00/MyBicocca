import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';
import type { SectionId } from './directory.ts';

export function appointmentsHref(): ResolvedPathname {
	return resolve('/(app)/services/appointments');
}

export function reservationHref(code: string): ResolvedPathname {
	return resolve('/(app)/services/appointments/[code]', { code });
}

/** The office directory, opened on a section. */
export function directoryHref(section?: SectionId): ResolvedPathname {
	const path = resolve('/(app)/services/appointments/book');
	return (section ? `${path}?section=${section}` : path) as ResolvedPathname;
}

/** A desk's booking page; `details` opens on the form where the two steps don't fit side by side. */
export function bookingHref(serviceId: number, step?: 'details'): ResolvedPathname {
	const path = resolve('/(app)/services/appointments/book/[service]', {
		service: String(serviceId)
	});
	return (step ? `${path}?step=${step}` : path) as ResolvedPathname;
}
