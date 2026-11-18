import type { EasyStaffPlanningManagedReservation } from '@mybicocca/data-api/easystaff';

/**
 * An appointment booked from this browser. The portal is anonymous and lists nobody's bookings:
 * the code and the email typed in the form are the only handle to one, so the app keeps them.
 */
export interface Reservation {
	code: string;
	/** The address the portal keys the reservation by. */
	email: string;
	/** The portal's entry, for the PDF reminder. */
	entryId: number | null;
	serviceId: number | null;
	/** The portal's service name, as booked. */
	serviceName: string;
	serviceGroup: string | null;
	areaName: string | null;
	areaAddress: string | null;
	/** Epoch milliseconds. */
	start: number;
	end: number;
	/** The check-in QR, as the data URL the portal sends. */
	qrCode: string | null;
	webConferenceUrl: string | null;
	/** Epoch milliseconds of the booking on this browser. */
	createdAt: number;
}

const DAY_MS = 24 * 60 * 60_000;

/** Whether `reservation` ended long enough ago to forget, without asking the portal. */
export function isExpired(reservation: Reservation, now: number): boolean {
	return reservation.end < now - DAY_MS;
}

/** What the portal said about a kept booking. */
export type PortalCheck =
	| { kind: 'found'; qrCode: string | null }
	/** It no longer knows it: cancelled from its email, or by the office. */
	| { kind: 'missing' }
	/** Anything else, such as being offline: nothing can be concluded. */
	| { kind: 'failed' };

/** What a sync round changes in the kept list. */
export interface SyncChanges {
	/** Codes to forget. */
	gone: Set<string>;
	/** QR codes the booking didn't come with, by code. */
	qrCodes: Map<string, string>;
}

const SYNC_EVERY_MS = 10 * 60_000;

/** Whether the kept list is due a check with the portal, at most every ten minutes. */
export function isSyncDue(lastSync: number | undefined, now: number): boolean {
	return lastSync === undefined || now - lastSync >= SYNC_EVERY_MS;
}

/**
 * Checks the kept bookings one at a time. Those that ended over a day ago are forgotten without
 * asking; those the portal no longer knows go too. A check that fails for any other reason ends
 * the round and keeps the rest as they are: being offline must not lose a booking.
 */
export async function reconcile(
	list: readonly Reservation[],
	now: number,
	check: (reservation: Reservation) => Promise<PortalCheck>
): Promise<SyncChanges> {
	const changes: SyncChanges = { gone: new Set(), qrCodes: new Map() };
	for (const reservation of list) {
		if (isExpired(reservation, now)) {
			changes.gone.add(reservation.code);
			continue;
		}
		const result = await check(reservation);
		if (result.kind === 'failed') break;
		if (result.kind === 'missing') changes.gone.add(reservation.code);
		else if (!reservation.qrCode && result.qrCode) {
			changes.qrCodes.set(reservation.code, result.qrCode);
		}
	}
	return changes;
}

/** `list` after a sync round: what it found gone dropped, missing QR codes filled in. */
export function applySync(list: readonly Reservation[], changes: SyncChanges): Reservation[] {
	return list
		.filter((item) => !changes.gone.has(item.code))
		.map((item) => {
			const qrCode = changes.qrCodes.get(item.code);
			return qrCode && !item.qrCode ? { ...item, qrCode } : item;
		});
}

export function byStart(a: Reservation, b: Reservation): number {
	return a.start - b.start || a.code.localeCompare(b.code);
}

/** `list` with `reservation` added, or replacing the one with its code, in start order. */
export function upsert(list: readonly Reservation[], reservation: Reservation): Reservation[] {
	return [...list.filter((item) => item.code !== reservation.code), reservation].sort(byStart);
}

export function split(
	list: readonly Reservation[],
	now: number
): { upcoming: Reservation[]; past: Reservation[] } {
	return {
		upcoming: list.filter((item) => item.end >= now),
		past: list.filter((item) => item.end < now).reverse()
	};
}

const blankToNull = (value: string | null | undefined) => value?.trim() || null;

/**
 * A booking made elsewhere, as the portal's management page describes it; undefined when the
 * page lacks its time, which nothing else could supply.
 */
export function fromManaged(
	code: string,
	email: string,
	managed: EasyStaffPlanningManagedReservation,
	service: { id: number | null; name: string | null; group: string | null },
	now: number
): Reservation | undefined {
	const start = managed.entry?.startTimeEpochSeconds;
	const end = managed.entry?.endTimeEpochSeconds;
	if (start === null || start === undefined) return undefined;
	return {
		code: managed.reservationCode?.trim() || code,
		email,
		entryId: managed.entry?.id ?? null,
		serviceId: service.id,
		serviceName: blankToNull(managed.service?.name) ?? blankToNull(service.name) ?? code,
		serviceGroup: service.group,
		areaName: blankToNull(managed.area?.name),
		areaAddress: blankToNull(managed.area?.address),
		start: start * 1000,
		end: (end ?? start) * 1000,
		qrCode: blankToNull(managed.qrCode),
		webConferenceUrl: null,
		createdAt: now
	};
}
