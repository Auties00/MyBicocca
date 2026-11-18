import { toDataError } from '#lib/data/errors.ts';
import { accountKey } from '#lib/data/keys.ts';
import { cache, prefetch, setQueryData, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import {
	applySync,
	fromManaged,
	isSyncDue,
	reconcile,
	upsert,
	type PortalCheck,
	type Reservation
} from './reservation.ts';

const YEAR_MS = 365 * 24 * 60 * 60_000;

/**
 * The account's appointments booked on this browser. No server lists them, so nothing loads
 * them: they live in the encrypted query store, which signing out wipes, and a load only returns
 * what is kept (the demo starts with the persona's booking).
 */
export function reservationsQuery(scope: SessionScope): QueryOptions<Reservation[]> {
	const key = accountKey(scope.account, 'appointments.reservations');
	return {
		key,
		load: async () => {
			const kept = cache.peek<Reservation[]>(key).data;
			if (kept !== undefined) return kept;
			return __DEMO__ ? (await import('./demoReservations.ts')).demoReservations() : [];
		},
		policy: { staleMs: Number.POSITIVE_INFINITY, persist: true, encrypt: true, maxAgeMs: YEAR_MS }
	};
}

/** Changes the kept list once it is read from storage, so nothing kept is overwritten unseen. */
async function update(
	scope: SessionScope,
	change: (list: Reservation[]) => Reservation[]
): Promise<void> {
	const options = reservationsQuery(scope);
	await prefetch(options);
	setQueryData<Reservation[]>(options.key, (current = []) => change(current));
}

export function saveReservation(scope: SessionScope, reservation: Reservation): Promise<void> {
	return update(scope, (list) => upsert(list, reservation));
}

export function forgetReservation(scope: SessionScope, code: string): Promise<void> {
	return update(scope, (list) => list.filter((item) => item.code !== code));
}

const syncedAt = new Map<string, number>();

/**
 * Forgets the appointments that ended over a day ago, and those the portal no longer knows, such
 * as one cancelled from its email; fills in a QR the booking didn't return. At most every ten
 * minutes per account; see {@link reconcile} for why nothing is lost offline.
 */
export async function syncReservations(scope: SessionScope): Promise<void> {
	const now = Date.now();
	if (!isSyncDue(syncedAt.get(scope.account.id), now)) return;
	syncedAt.set(scope.account.id, now);
	const options = reservationsQuery(scope);
	await prefetch(options);
	const list = cache.peek<Reservation[]>(options.key).data ?? [];
	const changes = await reconcile(list, now, (reservation) =>
		scope.api.planning.reservations.getReservation(reservation.code, reservation.email).then(
			(managed): PortalCheck => ({ kind: 'found', qrCode: managed.qrCode }),
			(error: unknown): PortalCheck =>
				toDataError(error).kind === 'not_found' ? { kind: 'missing' } : { kind: 'failed' }
		)
	);
	if (changes.gone.size === 0 && changes.qrCodes.size === 0) return;
	setQueryData<Reservation[]>(options.key, (current = []) => applySync(current, changes));
}

/**
 * Finds a booking made elsewhere by its code and email, and keeps it here.
 *
 * @returns undefined When the portal knows no such booking.
 * @throws What the portal or the network threw otherwise.
 */
export async function recoverReservation(
	scope: SessionScope,
	code: string,
	email: string,
	serviceOf: (id: number) => { name: string; group: string | null } | undefined
): Promise<Reservation | undefined> {
	const planning = scope.api.planning.reservations;
	const managed = await planning.getReservation(code, email).catch((error: unknown) => {
		if (toDataError(error).kind === 'not_found') return undefined;
		throw error;
	});
	if (managed === undefined) return undefined;
	const serviceId = await planning.getReservationServiceId(code, email).catch(() => null);
	const service = serviceId === null ? undefined : serviceOf(serviceId);
	const reservation = fromManaged(
		code,
		email,
		managed,
		{ id: serviceId, name: service?.name ?? null, group: service?.group ?? null },
		Date.now()
	);
	if (reservation === undefined) return undefined;
	await saveReservation(scope, reservation);
	return reservation;
}
