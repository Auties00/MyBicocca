import { AffluencesError } from '@mybicocca/data-api/affluences';
import { LocalDate, LocalTime } from '@mybicocca/data-api/runtime';
import { errorMessage } from '#lib/data/errorMessage.ts';
import { DataFailure, toDataError, type DataError } from '#lib/data/errors.ts';
import { mutate } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import type { IsoDate } from '#lib/format/days.ts';
import { m } from '#lib/paraglide/messages.js';
import { libraryLinks, type LibraryLink } from './account.svelte.ts';
import { endMinutes } from './model.ts';
import { reservationWrites } from './queries.ts';

const notLinked = () =>
	new DataFailure({ kind: 'auth', service: 'affluences', reason: 'reauth_required' });

const time = (minutes: number) => new LocalTime(Math.floor(minutes / 60), minutes % 60);

export interface BookingRequest {
	siteId: string;
	seatId: number;
	day: IsoDate;
	/** Minutes of the day, site time. */
	start: number;
	duration: number;
	/** Sent only when the seat asks for one. */
	note: string | null;
}

/**
 * Books a seat with the linked account, which Affluences confirms at once (no email step). The
 * linked email books it: the token belongs to that address.
 *
 * @returns The new booking's id, when Affluences gives it.
 * @throws DataFailure When the seat was taken meanwhile, the account isn't linked, or the network fails.
 */
export async function bookSeat(
	scope: SessionScope,
	link: LibraryLink,
	request: BookingRequest
): Promise<number | null> {
	const { auth, email } = link;
	const day = LocalDate.parse(request.day);
	if (!auth || !email || day === undefined) throw notLinked();
	const result = await mutate(
		() =>
			scope.api.affluences.reservations.createReservation(
				request.seatId,
				{
					email,
					date: day,
					startTime: time(request.start),
					endTime: time(endMinutes(request.start, request.duration)),
					personCount: 1,
					authType: null,
					note: request.note,
					userFirstName: null,
					userLastName: null,
					userPhone: null,
					userDeviceToken: null
				},
				{ auth }
			),
		{ invalidate: reservationWrites(scope, request.siteId) }
	);
	return result.reservationId;
}

const alreadyGone = (error: DataError) =>
	error.kind === 'not_found' || (error.kind === 'rejected' && !!error.code?.includes('not_found'));

/**
 * Cancels the booking a cancellation token stands for; one that is already gone counts as
 * cancelled. The token is the credential: no account is needed.
 *
 * @throws DataFailure When Affluences refuses or can't be reached.
 */
export async function cancelBooking(scope: SessionScope, cancellationToken: string): Promise<void> {
	try {
		await mutate(() => scope.api.affluences.reservations.cancelReservation(cancellationToken), {
			invalidate: reservationWrites(scope)
		});
	} catch (error) {
		if (!alreadyGone(toDataError(error))) throw error;
	}
}

/**
 * Records attendance with the code the library shows; Affluences matches it with the linked email.
 *
 * @throws DataFailure With code `invalid_validation_code` when the code doesn't match.
 */
export async function checkIn(scope: SessionScope, link: LibraryLink, code: string): Promise<void> {
	const email = link.email;
	if (!email) throw notLinked();
	await mutate(
		() =>
			scope.api.affluences.reservations.validateReservation({
				validationCode: code.trim(),
				email
			}),
		{ invalidate: reservationWrites(scope) }
	);
}

/** The sentence for a failed check-in. */
export function checkInFailure(error: unknown): string {
	const failure = toDataError(error);
	if (failure.kind === 'rejected' && failure.code === AffluencesError.InvalidValidationCode) {
		return m.library_invalid_code();
	}
	return failure.kind === 'rejected' ? m.library_presence_failed() : errorMessage(failure);
}

/** The sentence for a failed booking or cancellation: the cause when it's the network's. */
export function writeFailure(fallback: () => string) {
	return (error: unknown): string => {
		const failure = toDataError(error);
		return failure.kind === 'rejected' || failure.kind === 'unknown' || failure.kind === 'decode'
			? fallback()
			: errorMessage(failure);
	};
}

/**
 * Emails `email` a validation link; once it is opened, the email belongs to the signed-in account.
 *
 * @returns The request to poll until the link is opened.
 * @throws DataFailure When Affluences refuses or can't be reached.
 */
export async function requestLink(scope: SessionScope, email: string): Promise<string> {
	const auth = await libraryLinks.deviceAuth(scope.api.affluences);
	const answer = await mutate(() =>
		scope.api.affluences.account.requestEmailValidation({ email }, auth)
	);
	if (!answer.requestUuid) {
		throw new DataFailure({ kind: 'decode', detail: 'Affluences sent no request uuid' });
	}
	return answer.requestUuid;
}

/**
 * Whether the emailed link was opened; if so `email` is linked to the signed-in account.
 *
 * @throws DataFailure When Affluences refuses or can't be reached.
 */
export async function pollLink(
	scope: SessionScope,
	email: string,
	requestUuid: string
): Promise<boolean> {
	const auth = await libraryLinks.deviceAuth(scope.api.affluences);
	let token: string | null;
	try {
		token = (await scope.api.affluences.account.pollEmailValidation(requestUuid, auth)).authToken;
	} catch (error) {
		const failure = toDataError(error);
		if (failure.kind === 'not_found') return false;
		throw new DataFailure(failure, { cause: error });
	}
	if (!token) return false;
	await libraryLinks.link(scope.account, email, token);
	return true;
}
