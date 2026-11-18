import type {
	EasyStaffPlanningApi,
	EasyStaffPlanningReservationResult
} from '@mybicocca/data-api/easystaff';
import type { JsonValue } from '@mybicocca/data-api/runtime';
import { toDataError } from '#lib/data/errors.ts';
import type { IsoDate } from '#lib/format/days.ts';
import { romeInstant } from '#lib/format/rome.ts';
import { sectionValues, type FieldValues } from './form.ts';
import type { Reservation } from './reservation.ts';
import type { Offering, Slot } from './slots.ts';

const PORTAL_CODE = 'unimib-segreteria';
const TIME_ZONE = 'Europe/Rome';

/** Why a booking stopped before the portal could refuse it. */
export type BookingRefusal =
	/** No time is picked: none was, or the one picked was taken meanwhile. */
	| 'slot'
	/** The form has no email to key the reservation by. */
	| 'email'
	/** The portal wants a reCAPTCHA, which only its own page can solve. */
	| 'captcha'
	/** The portal held the slot but sent no code, without which the booking couldn't be managed. */
	| 'no_code';

export class BookingRefused extends Error {
	override readonly name = 'BookingRefused';

	constructor(readonly reason: BookingRefusal) {
		super(`Booking refused: ${reason}`);
	}
}

/**
 * The portal held the slot but couldn't confirm it. `cause` is why; `released` tells whether
 * the hold was then let go, or still blocks the slot until it lapses, within half an hour.
 */
export class ConfirmationFailed extends Error {
	override readonly name = 'ConfirmationFailed';

	constructor(
		override readonly cause: unknown,
		readonly released: boolean
	) {
		super('Booking not confirmed', { cause });
	}
}

/** The phases a booking goes through, for the page to tell. */
export type BookingStep = 'checking' | 'holding' | 'confirming';

export interface BookingRequest {
	service: { id: number; name: string; group: string | null };
	offering: Offering;
	date: IsoDate;
	slot: Slot;
	email: string;
	values: FieldValues;
}

/** The entry id the portal returns, a bare number or an object with its id. */
export function entryIdOf(entry: JsonValue | null): number | null {
	if (typeof entry === 'number' && Number.isInteger(entry)) return entry;
	if (typeof entry === 'string' && /^\d+$/.test(entry)) return Number(entry);
	if (entry !== null && typeof entry === 'object' && !Array.isArray(entry)) {
		return entryIdOf(entry.id ?? null);
	}
	return null;
}

const blankToNull = (value: string | null) => value?.trim() || null;

function reservationFrom(
	request: BookingRequest,
	result: EasyStaffPlanningReservationResult,
	code: string,
	start: number,
	now: number
): Reservation {
	const echoedStart = result.startTimeEpochSeconds;
	const echoedEnd = result.endTimeEpochSeconds;
	return {
		code,
		email: request.email,
		entryId: entryIdOf(result.entry),
		serviceId: request.service.id,
		serviceName: request.service.name,
		serviceGroup: request.service.group,
		areaName: request.offering.areaName,
		areaAddress: request.offering.areaAddress,
		start: echoedStart === null ? start : echoedStart * 1000,
		end: echoedEnd === null ? start + request.offering.durationSeconds * 1000 : echoedEnd * 1000,
		qrCode: blankToNull(result.qrCode),
		webConferenceUrl: blankToNull(result.webConferenceUrl),
		createdAt: now
	};
}

/**
 * Books a slot as the portal's own page does: the form is read again for the section of each
 * field, the slot is held (reversible), then confirmed (irreversible). A confirmation that fails
 * releases the hold, so the slot doesn't stay blocked for half an hour. The SDK renews the guest
 * token and waits out the portal's 12 s anti-automation pause by itself: a booking can take that
 * long.
 *
 * @throws BookingRefused Before any write the portal can't take, or when it returned no code.
 * @throws ConfirmationFailed When the slot was held but not confirmed.
 * @throws What the portal or the network threw otherwise.
 */
export async function bookAppointment(
	planning: EasyStaffPlanningApi,
	request: BookingRequest,
	onStep: (step: BookingStep) => void = () => {}
): Promise<Reservation> {
	const email = request.email.trim();
	if (email === '') throw new BookingRefused('email');

	onStep('checking');
	const [fields, portal] = await Promise.all([
		planning.portals.getServiceForm(request.service.id),
		planning.portals.getPortal()
	]);
	if (portal.settings?.general?.captchaRequired === true) throw new BookingRefused('captcha');

	onStep('holding');
	const start = romeInstant({ date: request.date, minutes: request.slot.start });
	const startSeconds = Math.round(start / 1000);
	const sections = sectionValues(fields, request.values);
	const result = await planning.reservations.createReservation({
		reservationNumber: 0,
		portalCode: PORTAL_CODE,
		startTimeEpochSeconds: startSeconds,
		endTimeEpochSeconds: startSeconds + request.offering.durationSeconds,
		durationSeconds: request.offering.durationSeconds,
		serviceId: request.service.id,
		areaId: request.offering.areaId,
		primaryValue: email,
		userFields: sections.user,
		serviceFields: sections.service,
		backofficeFields: sections.backoffice,
		resourceId: null,
		recaptchaToken: null,
		timeZone: TIME_ZONE
	});
	const code = blankToNull(result.reservationCode);
	// Without a code the booking could never be shown or cancelled: the hold is left to expire.
	if (code === null) throw new BookingRefused('no_code');

	const entryId = entryIdOf(result.entry);
	if (entryId !== null) {
		onStep('confirming');
		try {
			await planning.reservations.confirmReservation(entryId);
		} catch (error) {
			const released = await planning.reservations
				.deleteReservation(code, { force: true }, email)
				.then(
					() => true,
					() => false
				);
			throw new ConfirmationFailed(error, released);
		}
	}
	return reservationFrom({ ...request, email }, result, code, start, Date.now());
}

/** The portal answered, and said no: to the time, the desk or a booking limit. */
export function isRefusal(error: unknown): boolean {
	const kind = toDataError(error).kind;
	return kind === 'rejected' || kind === 'forbidden' || kind === 'not_found';
}

/** What a failed booking leaves the student with, for the page to tell. */
export interface FailureOutcome {
	/** Why it failed: the error, or what the portal threw while confirming. */
	cause: unknown;
	/** Someone else has the time: say that rather than the cause. */
	taken: boolean;
	/** What became of the slot. */
	note: 'released' | 'held' | 'pick_another' | null;
	/** Booking the same time again may work. */
	retry: boolean;
}

/**
 * Reads a failure against the day reloaded after it. A time still free may be booked again; one
 * gone would fail again, so another must be picked. A hold the portal kept makes the time look
 * taken too, by this very booking, and is said so rather than blamed on someone else.
 */
export function failureOutcome(error: unknown, stillFree: boolean): FailureOutcome {
	const confirmation = error instanceof ConfirmationFailed ? error : undefined;
	const cause = confirmation ? confirmation.cause : error;
	const held = confirmation !== undefined && !confirmation.released;
	if (stillFree) {
		const note = confirmation ? (held ? 'held' : 'released') : null;
		return { cause, taken: false, note, retry: true };
	}
	const refusedTime = error instanceof BookingRefused && error.reason === 'slot';
	return {
		cause,
		taken: !held && (refusedTime || isRefusal(cause)),
		note: held ? 'held' : 'pick_another',
		retry: false
	};
}

/** Cancels a booking at the portal; one it no longer knows is as good as cancelled. */
export async function cancelAppointment(
	planning: EasyStaffPlanningApi,
	reservation: Pick<Reservation, 'code' | 'email'>
): Promise<void> {
	try {
		await planning.reservations.deleteReservation(
			reservation.code,
			{ force: null },
			reservation.email
		);
	} catch (error) {
		if (toDataError(error).kind !== 'not_found') throw error;
	}
}
