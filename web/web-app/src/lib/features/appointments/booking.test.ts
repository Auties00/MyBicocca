import {
	decodeEasyStaffPlanningFormField,
	decodeEasyStaffPlanningPortalConfig,
	decodeEasyStaffPlanningReservationResult,
	EasyStaffPlanningError,
	type EasyStaffPlanningApi,
	type EasyStaffPlanningReservationRequest
} from '@mybicocca/data-api/easystaff';
import { ApiFailure } from '@mybicocca/data-api/runtime';
import { describe, expect, it } from 'vitest';
import {
	BookingRefused,
	ConfirmationFailed,
	bookAppointment,
	cancelAppointment,
	entryIdOf,
	failureOutcome,
	type BookingRequest,
	type BookingStep
} from './booking.ts';

const decoded = <T>(value: T | undefined): T => {
	if (value === undefined) throw new Error('Undecodable fixture');
	return value;
};

const FIELDS = [
	{ id: 1, cliente: 2, label: 'Email', codice: 'email', tipo: 'email', primaria: true },
	{ id: 2, cliente: 2, label: 'Nome', codice: 'nome', tipo: 'text' },
	{ id: 3, cliente: 2, label: 'Motivo', codice: 'motivo', tipo: 'select', posizione: '2' },
	{ id: 4, cliente: 2, label: 'gdpr_label', codice: 'gdpr', tipo: 'checkbox', posizione: '100' }
].map((field) => decoded(decodeEasyStaffPlanningFormField(field)));

interface FakeOptions {
	captcha?: boolean;
	result?: Record<string, unknown>;
	confirmFails?: boolean;
	deleteError?: Error;
}

/** A portal that records what it is asked, in order. */
function portal(options: FakeOptions = {}) {
	const calls: string[] = [];
	let stored: EasyStaffPlanningReservationRequest | undefined;
	const api = {
		portals: {
			getServiceForm: async () => {
				calls.push('form');
				return FIELDS;
			},
			getPortal: async () => {
				calls.push('portal');
				return decoded(
					decodeEasyStaffPlanningPortalConfig({
						id: 2,
						Nome: 'Sportelli',
						Codice: 'unimib-segreteria',
						config: { general: [{ captcha_control: options.captcha === true }] }
					})
				);
			}
		},
		reservations: {
			createReservation: async (body: EasyStaffPlanningReservationRequest) => {
				calls.push('store');
				stored = body;
				return decoded(
					decodeEasyStaffPlanningReservationResult(
						options.result ?? {
							codice_prenotazione: 'BK-1',
							entry: { id: 77 },
							start_time: body.startTimeEpochSeconds,
							end_time: body.endTimeEpochSeconds,
							qr_code: 'data:image/png;base64,AAAA',
							web_conference_url: ''
						}
					)
				);
			},
			confirmReservation: async (entryId: number) => {
				calls.push(`confirm ${entryId}`);
				if (options.confirmFails) throw new TypeError('Failed to fetch');
				return { success: true };
			},
			deleteReservation: async (code: string, body: { force: boolean | null }, email: string) => {
				calls.push(`delete ${code} ${String(body.force)} ${email}`);
				if (options.deleteError) throw options.deleteError;
				return { success: true };
			}
		}
	};
	return {
		api: api as unknown as EasyStaffPlanningApi,
		calls,
		body: () => stored
	};
}

const request: BookingRequest = {
	service: { id: 101, name: 'Gestione carriere - Scienze', group: 'Carriere Studenti' },
	offering: {
		serviceName: 'Gestione carriere - Scienze',
		serviceGroup: 'Carriere Studenti',
		areaId: 11,
		areaName: 'Edificio U17',
		areaAddress: 'Piazzetta Difesa per le Donne',
		durationSeconds: 1200,
		virtual: false,
		description: null,
		horizonDays: 30
	},
	date: '2026-11-03',
	slot: { start: 14 * 60, end: 14 * 60 + 20, available: true },
	email: ' m.rossi99@campus.unimib.it ',
	values: { email: 'm.rossi99@campus.unimib.it', nome: 'Marco', motivo: 'altro', gdpr: 'true' }
};

describe('bookAppointment', () => {
	it('holds the slot then confirms it, as the portal’s page does', async () => {
		const fake = portal();
		const steps: BookingStep[] = [];
		const reservation = await bookAppointment(fake.api, request, (step) => steps.push(step));

		expect(steps).toEqual(['checking', 'holding', 'confirming']);
		expect(fake.calls).toEqual(['form', 'portal', 'store', 'confirm 77']);
		expect(fake.body()).toEqual({
			reservationNumber: 0,
			portalCode: 'unimib-segreteria',
			startTimeEpochSeconds: Date.UTC(2026, 10, 3, 13, 0) / 1000,
			endTimeEpochSeconds: Date.UTC(2026, 10, 3, 13, 20) / 1000,
			durationSeconds: 1200,
			serviceId: 101,
			areaId: 11,
			primaryValue: 'm.rossi99@campus.unimib.it',
			userFields: { email: 'm.rossi99@campus.unimib.it', nome: 'Marco' },
			serviceFields: { motivo: 'altro' },
			backofficeFields: {},
			resourceId: null,
			recaptchaToken: null,
			timeZone: 'Europe/Rome'
		});
		expect(reservation).toMatchObject({
			code: 'BK-1',
			email: 'm.rossi99@campus.unimib.it',
			entryId: 77,
			serviceName: 'Gestione carriere - Scienze',
			areaName: 'Edificio U17',
			start: Date.UTC(2026, 10, 3, 13, 0),
			end: Date.UTC(2026, 10, 3, 13, 20),
			qrCode: 'data:image/png;base64,AAAA',
			webConferenceUrl: null
		});
	});

	it('trusts the times the portal echoes over the picked ones', async () => {
		const fake = portal({
			result: { codice_prenotazione: 'BK-2', entry: 5, start_time: 1000, end_time: 1900 }
		});
		const reservation = await bookAppointment(fake.api, request);
		expect([reservation.start, reservation.end]).toEqual([1_000_000, 1_900_000]);
	});

	it('refuses before writing when the portal wants a captcha', async () => {
		const fake = portal({ captcha: true });
		await expect(bookAppointment(fake.api, request)).rejects.toEqual(new BookingRefused('captcha'));
		expect(fake.calls).toEqual(['form', 'portal']);
	});

	it('refuses without an email', async () => {
		const fake = portal();
		await expect(bookAppointment(fake.api, { ...request, email: '  ' })).rejects.toBeInstanceOf(
			BookingRefused
		);
		expect(fake.calls).toEqual([]);
	});

	const confirmationFailure = (promise: Promise<unknown>) =>
		promise.then(
			() => expect.unreachable('the booking should fail'),
			(error: unknown) => {
				expect(error).toBeInstanceOf(ConfirmationFailed);
				return error as ConfirmationFailed;
			}
		);

	it('releases the hold when the confirmation fails, and reports the failure', async () => {
		const fake = portal({ confirmFails: true });
		const failure = await confirmationFailure(bookAppointment(fake.api, request));
		expect(failure.released).toBe(true);
		expect(failure.cause).toEqual(new TypeError('Failed to fetch'));
		expect(fake.calls).toEqual([
			'form',
			'portal',
			'store',
			'confirm 77',
			'delete BK-1 true m.rossi99@campus.unimib.it'
		]);
	});

	it('says the slot may stay held when the release fails too', async () => {
		const fake = portal({ confirmFails: true, deleteError: new Error('gone') });
		const failure = await confirmationFailure(bookAppointment(fake.api, request));
		expect(failure.released).toBe(false);
		expect(failure.cause).toEqual(new TypeError('Failed to fetch'));
	});

	it('never confirms a hold that came back without a code', async () => {
		const fake = portal({ result: { entry: 9 } });
		await expect(bookAppointment(fake.api, request)).rejects.toEqual(new BookingRefused('no_code'));
		expect(fake.calls).not.toContain('confirm 9');
	});

	it('skips the confirmation the portal gives no entry for', async () => {
		const fake = portal({ result: { codice_prenotazione: 'BK-3' } });
		const reservation = await bookAppointment(fake.api, request);
		expect(fake.calls).toEqual(['form', 'portal', 'store']);
		expect(reservation.entryId).toBeNull();
	});
});

describe('entryIdOf', () => {
	it('reads a bare number, a numeric string or an object’s id', () => {
		expect(entryIdOf(48213)).toBe(48213);
		expect(entryIdOf('48213')).toBe(48213);
		expect(entryIdOf({ id: 7, start_time: 1 })).toBe(7);
		expect(entryIdOf(null)).toBeNull();
		expect(entryIdOf([1])).toBeNull();
	});
});

describe('cancelAppointment', () => {
	const notFound = new EasyStaffPlanningError(new ApiFailure(404, 'not_found', 'not_found'));

	it('counts a booking the portal no longer knows as cancelled', async () => {
		const fake = portal({ deleteError: notFound });
		await expect(
			cancelAppointment(fake.api, { code: 'BK-1', email: 'a@b.it' })
		).resolves.toBeUndefined();
		expect(fake.calls).toEqual(['delete BK-1 null a@b.it']);
	});

	it('reports any other failure', async () => {
		const fake = portal({ deleteError: new TypeError('Failed to fetch') });
		await expect(cancelAppointment(fake.api, { code: 'BK-1', email: 'a@b.it' })).rejects.toThrow();
	});
});

describe('failureOutcome', () => {
	const taken = new EasyStaffPlanningError(
		new ApiFailure(422, 'slot_not_available', 'slot_not_available')
	);
	const offline = new TypeError('Failed to fetch');

	it('offers Retry while the time is still free, saying what became of the hold', () => {
		expect(failureOutcome(offline, true)).toEqual({
			cause: offline,
			taken: false,
			note: null,
			retry: true
		});
		expect(failureOutcome(new ConfirmationFailed(offline, true), true)).toMatchObject({
			cause: offline,
			note: 'released',
			retry: true
		});
		expect(failureOutcome(new ConfirmationFailed(offline, false), true).note).toBe('held');
	});

	it('sends the student to another time once the day shows it gone', () => {
		expect(failureOutcome(taken, false)).toEqual({
			cause: taken,
			taken: true,
			note: 'pick_another',
			retry: false
		});
		expect(failureOutcome(new BookingRefused('slot'), false)).toMatchObject({
			taken: true,
			retry: false
		});
	});

	it('keeps the cause when the time went for another reason than a refusal', () => {
		expect(failureOutcome(offline, false)).toMatchObject({ taken: false, retry: false });
		expect(failureOutcome(new BookingRefused('captcha'), false).taken).toBe(false);
	});

	it('blames a time shown taken on the hold the portal kept, not on someone else', () => {
		expect(failureOutcome(new ConfirmationFailed(taken, false), false)).toEqual({
			cause: taken,
			taken: false,
			note: 'held',
			retry: false
		});
	});
});
