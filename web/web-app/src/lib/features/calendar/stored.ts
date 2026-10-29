import { reservationsQuery } from '#lib/features/appointments/reservations.ts';
import { courseDeadlinesQuery } from '#lib/features/elearning/queries.ts';
import { bookingsQuery } from '#lib/features/exams/queries.ts';
import { libraryLinks } from '#lib/features/library/account.svelte.ts';
import { myReservationsQuery } from '#lib/features/library/queries.ts';
import { recordBookRowsQuery } from '#lib/features/profile/queries.ts';
import { cache, policies, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import type { YearMonth } from '#lib/format/days.ts';
import {
	activityCodesByName,
	appointmentFromStore,
	deadlineFromCourseDeadline,
	examFromBooking,
	libraryFromReservation
} from './mappers.ts';
import { mergeEvents, type CalendarEvent } from './model.ts';
import { lessonsQuery } from './queries.ts';

/** Storage slower than this is not waited for: the calendar shows its skeleton instead. */
const STORED_WAIT_MS = 300;

function peek<T>(options: QueryOptions<T> | undefined): Promise<T | undefined> {
	return options
		? cache.peekStored(options.key, options.policy ?? policies.live)
		: Promise.resolve(undefined);
}

async function read(scope: SessionScope, months: readonly YearMonth[]): Promise<CalendarEvent[]> {
	const link = libraryLinks.of(scope.account);
	const [bookings, records, deadlines, library, appointments, ...lessons] = await Promise.all([
		peek(bookingsQuery(scope)),
		peek(recordBookRowsQuery(scope)),
		peek(courseDeadlinesQuery(scope)),
		peek(link && myReservationsQuery(scope, link)),
		peek(reservationsQuery(scope)),
		...months.map((month) => peek(lessonsQuery(scope, month)))
	]);
	const codes = activityCodesByName(records ?? []);
	return mergeEvents<CalendarEvent>(
		...lessons.map((list) => list ?? []),
		mergeEvents((bookings ?? []).flatMap((booking) => examFromBooking(booking, codes) ?? [])),
		(deadlines ?? []).flatMap((deadline) => deadlineFromCourseDeadline(deadline) ?? []),
		(library ?? []).flatMap((reservation) => libraryFromReservation(reservation) ?? []),
		(appointments ?? []).map(appointmentFromStore)
	);
}

/**
 * The events storage holds for `months`, as the calendar's queries will show them once they
 * hydrate, read without a request; empty when storage takes longer than a few frames.
 */
export function storedEvents(
	scope: SessionScope,
	months: readonly YearMonth[]
): Promise<CalendarEvent[]> {
	return Promise.race([
		read(scope, months).catch(() => []),
		new Promise<CalendarEvent[]>((resolve) => setTimeout(() => resolve([]), STORED_WAIT_MS))
	]);
}
