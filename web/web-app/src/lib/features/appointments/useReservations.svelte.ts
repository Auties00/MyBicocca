import { query, type Query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import type { Reservation } from './reservation.ts';
import { reservationsQuery, syncReservations } from './reservations.ts';

/** The account's kept appointments, for a component, checked with the portal now and then. */
export function useReservations(): Query<Reservation[]> {
	const reservations = query(() => session.current && reservationsQuery(session.current));
	$effect(() => {
		const scope = session.current;
		if (scope) void syncReservations(scope);
	});
	return reservations;
}
