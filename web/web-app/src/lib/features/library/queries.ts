import type {
	AffluencesAgreement,
	AffluencesAvailableResource,
	AffluencesLiveData,
	AffluencesMyReservation,
	AffluencesReservationSiteInfo,
	AffluencesReservationToCancel,
	AffluencesResourceTypeFilters,
	AffluencesSite,
	AffluencesSiteCard,
	AffluencesWeekTimetable
} from '@mybicocca/data-api/affluences';
import { LocalDate } from '@mybicocca/data-api/runtime';
import { toDataError } from '#lib/data/errors.ts';
import { publicKey } from '#lib/data/keys.ts';
import { policies, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import type { IsoDate } from '#lib/format/days.ts';
import { libraryLinks, myReservationsKey, type LibraryLink } from './account.svelte.ts';
import { LIBRARY_ROOT_ID } from './model.ts';

/*
 * Each query wraps one SDK call and is named after it, so another feature asking the same thing
 * shares the cache entry. Occupancy is volatile (a minute); the site's own pages change rarely.
 */

const MINUTE = 1;
const HOUR = 60;
const DAY = 24 * HOUR;

/** A `yyyy-MM-dd` the SDK takes. */
function localDate(day: IsoDate): LocalDate {
	const date = LocalDate.parse(day);
	if (date === undefined) throw new Error(`Not a date: ${day}`);
	return date;
}

export function librariesQuery(scope: SessionScope): QueryOptions<AffluencesSiteCard[]> {
	return {
		key: publicKey('affluences.sites.getChildren', LIBRARY_ROOT_ID),
		load: () => scope.api.affluences.sites.getChildren(LIBRARY_ROOT_ID),
		policy: policies.shared(MINUTE)
	};
}

export function liveDataQuery(
	scope: SessionScope,
	siteId: string
): QueryOptions<AffluencesLiveData> {
	return {
		key: publicKey('affluences.sites.getLiveData', siteId),
		load: () => scope.api.affluences.sites.getLiveData(siteId),
		policy: policies.shared(MINUTE)
	};
}

export function weekTimetableQuery(
	scope: SessionScope,
	siteId: string,
	weekOffset: number
): QueryOptions<AffluencesWeekTimetable> {
	return {
		key: publicKey('affluences.sites.getWeekTimetable', siteId, weekOffset),
		load: () => scope.api.affluences.sites.getWeekTimetable(siteId, { weekOffset }),
		policy: policies.shared(HOUR)
	};
}

/** Address, contacts and coordinates, which the children listing leaves out. */
export function siteQuery(scope: SessionScope, siteId: string): QueryOptions<AffluencesSite> {
	return {
		key: publicKey('affluences.sites.getSite', siteId),
		load: () => scope.api.affluences.sites.getSite(siteId),
		policy: policies.shared(DAY)
	};
}

export function reservationInfoQuery(
	scope: SessionScope,
	siteId: string
): QueryOptions<AffluencesReservationSiteInfo> {
	return {
		key: publicKey('affluences.reservations.getReservationInfo', siteId),
		load: () => scope.api.affluences.reservations.getReservationInfo(siteId),
		policy: policies.shared(HOUR)
	};
}

export function agreementsQuery(
	scope: SessionScope,
	siteId: string
): QueryOptions<AffluencesAgreement[]> {
	return {
		key: publicKey('affluences.reservations.getAgreements', siteId),
		load: () => scope.api.affluences.reservations.getAgreements(siteId),
		policy: policies.shared(DAY)
	};
}

/** The zone's bookable days and durations; Affluences answers the same whatever the day asked. */
export function resourceTypeFiltersQuery(
	scope: SessionScope,
	siteId: string,
	zoneId: number,
	today: IsoDate
): QueryOptions<AffluencesResourceTypeFilters> {
	return {
		key: publicKey('affluences.reservations.getResourceTypeFilters', siteId, zoneId, today),
		load: () =>
			scope.api.affluences.reservations.getResourceTypeFilters(siteId, zoneId, localDate(today)),
		policy: policies.memory(5)
	};
}

/** Free seats are never cached: they are what a booking competes for. */
export function availableResourcesQuery(
	scope: SessionScope,
	siteId: string,
	day: IsoDate,
	zoneId: number,
	duration: number
): QueryOptions<AffluencesAvailableResource[]> {
	return {
		key: publicKey('affluences.reservations.getAvailableResources', siteId, day, zoneId, duration),
		load: () =>
			scope.api.affluences.reservations.getAvailableResources(siteId, localDate(day), zoneId, {
				durationMinutes: duration
			}),
		policy: policies.none
	};
}

/** What an emailed cancellation link points to. */
export function reservationToCancelQuery(
	scope: SessionScope,
	token: string
): QueryOptions<AffluencesReservationToCancel> {
	return {
		key: publicKey('affluences.reservations.getReservationToCancel', token),
		load: () => scope.api.affluences.reservations.getReservationToCancel(token),
		policy: policies.none
	};
}

/**
 * The bookings of the account's linked email. A refused token marks the link expired: the email is
 * kept, so verifying it again is one step.
 */
export function myReservationsQuery(
	scope: SessionScope,
	link: LibraryLink
): QueryOptions<AffluencesMyReservation[]> | undefined {
	const auth = link.auth;
	if (auth === undefined) return undefined;
	return {
		key: myReservationsKey(scope.account),
		load: async () => {
			try {
				return (await scope.api.affluences.account.getMyReservations(auth)).results;
			} catch (error) {
				if (toDataError(error).kind === 'auth') await libraryLinks.expire(scope.account);
				throw error;
			}
		},
		policy: policies.live
	};
}

/** What a booking, a cancellation or a check-in changes. */
export function reservationWrites(scope: SessionScope, siteId?: string) {
	return [
		myReservationsKey(scope.account),
		publicKey('affluences.reservations.getAvailableResources', ...(siteId ? [siteId] : []))
	];
}
