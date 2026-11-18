import { query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { libraryLinks } from './account.svelte.ts';
import { myReservationsQuery } from './queries.ts';

/** Reloads volatile data (occupancy) on a timer, only while the tab is seen and online. */
export function refreshWhileVisible(refresh: () => unknown, everyMs = 60_000): void {
	$effect(() => {
		const timer = setInterval(() => {
			if (document.visibilityState === 'visible' && navigator.onLine) void refresh();
		}, everyMs);
		return () => clearInterval(timer);
	});
}

/** The active account's library link and its bookings, for a component. */
export function linkedReservations() {
	const link = $derived(session.current && libraryLinks.of(session.current.account));
	const reservations = query(() => {
		const scope = session.current;
		return scope && link && myReservationsQuery(scope, link);
	});
	return {
		get link() {
			return link;
		},
		reservations
	};
}
