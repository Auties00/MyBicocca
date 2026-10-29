import { days } from '../../dates.ts';
import { DemoStudent } from '../../persona.ts';
import { json, route, type DemoRequest } from '../../router.ts';
import { demoState } from '../../state.ts';

const APP = 'https://api.affluences.com/app/v4';
const RESERVATION = 'https://reservation.affluences.com/api';

/** The two libraries of the real system, with their real ids, so links match production. */
export const DemoLibraries = {
	central: {
		id: '4912615d-501b-4c4e-a2a7-ccaec81c5429',
		name: 'Biblioteca Centrale',
		secondary: 'Ed. U6 Agorà (Unimib)'
	},
	medicine: {
		id: '455e0d1d-2236-4e49-8e76-f1b931b08974',
		name: 'Biblioteca di Medicina',
		secondary: 'Ed. U8 Asclepio (Unimib)'
	}
} as const;

/**
 * What the demo account has linked on first use: the student's email, already validated, as in
 * the Android demo. The library feature keeps it like a link made through the email flow, and
 * forgets it when the demo signs out.
 */
export const DemoAffluencesLink = {
	apiKey: 'demo-affluences-api-key',
	authToken: 'demo-affluences-token',
	email: DemoStudent.email
} as const;

export interface DemoReservation {
	reservation_id: number;
	resource_id: number;
	resource_name: string;
	site_name: string;
	site_secondary_name: string;
	site_timezone: string;
	reservation_token: string;
	cancellation_token: string;
	person_count: number;
	note: string | null;
	reservation_start_datetime: string;
	reservation_end_datetime: string;
	request_datetime: string;
	state: 'UPCOMING' | 'AWAITING_CONFIRMATION' | 'ONGOING' | 'PAST' | 'CANCELLED';
	validated: boolean;
	confirmation_mandatory: boolean;
	user_validation: boolean;
}

const HALF_HOUR_MS = 30 * 60_000;

/**
 * The student's seats: one under way now (so check-in is open whenever the demo is opened), one
 * tomorrow morning in the other library, one booked on Affluences' own site that still waits for
 * its emailed confirmation, and two that the list leaves out (past, cancelled).
 */
function initialReservations(): DemoReservation[] {
	const started = new Date(Math.floor(Date.now() / HALF_HOUR_MS) * HALF_HOUR_MS - HALF_HOUR_MS);
	const booking = (
		id: number,
		library: (typeof DemoLibraries)[keyof typeof DemoLibraries],
		seat: [resourceId: number, name: string],
		start: Date,
		end: Date,
		state: DemoReservation['state'],
		code: string
	): DemoReservation => ({
		reservation_id: id,
		resource_id: seat[0],
		resource_name: seat[1],
		site_name: library.name,
		site_secondary_name: library.secondary,
		site_timezone: 'Europe/Rome',
		reservation_token: code,
		cancellation_token: `demo-cancel-${id}`,
		person_count: 1,
		note: DemoStudent.fullName,
		reservation_start_datetime: start.toISOString(),
		reservation_end_datetime: end.toISOString(),
		request_datetime: days(-3, 18, 12).toISOString(),
		state,
		validated: state === 'PAST',
		confirmation_mandatory: state === 'AWAITING_CONFIRMATION',
		user_validation: false
	});
	// Seat ids are the zone's resource type × 1000 + the seat's number, as `reservations.ts` numbers them.
	return [
		booking(
			9001,
			DemoLibraries.central,
			[518_042, 'R 42 - Posto con presa elettrica'],
			started,
			new Date(started.getTime() + 5 * HALF_HOUR_MS),
			'ONGOING',
			'A7K2'
		),
		booking(
			9002,
			DemoLibraries.medicine,
			[701_017, 'V 17'],
			days(1, 9),
			days(1, 13),
			'UPCOMING',
			'M3P9'
		),
		booking(
			9003,
			DemoLibraries.central,
			[519_007, 'G 7'],
			days(3, 15),
			days(3, 18),
			'AWAITING_CONFIRMATION',
			'T5H3'
		),
		booking(
			8990,
			DemoLibraries.medicine,
			[701_008, 'V 8'],
			days(-2, 9),
			days(-2, 13),
			'PAST',
			'K8D1'
		),
		booking(
			8995,
			DemoLibraries.central,
			[519_012, 'G 12'],
			days(2, 14),
			days(2, 16),
			'CANCELLED',
			'Q2W6'
		)
	];
}

/** The account's reservations, shared with the reservation routes that book and cancel. */
export const demoReservations = () =>
	demoState('affluences.reservations', () => initialReservations());

/** Email validations requested in this demo, by request uuid, with when they were sent. */
const validations = () =>
	demoState('affluences.validations', () => new Map<string, { email: string; sentAt: number }>());

/** The demo pretends the student opens the emailed link this long after it is sent. */
const LINK_OPENED_AFTER_MS = 6_000;

/** Affluences' reservation API errors: `{ error, error_message }`. */
export function affluencesError(status: number, code: string, message: string): Response {
	return json({ error: code, error_message: message }, { status });
}

/** Whether the request carries a token the demo handed out. */
export function accountToken(request: DemoRequest): boolean {
	return request.headers.get('authorization')?.startsWith('Bearer demo-') ?? false;
}

const listed = (email: string, empty: boolean) =>
	json({
		email,
		results: empty
			? []
			: demoReservations().map((reservation) => ({
					...reservation,
					state:
						reservation.state !== 'CANCELLED' &&
						Date.parse(reservation.reservation_end_datetime) < Date.now()
							? 'PAST'
							: reservation.state
				}))
	});

export const routes = [
	route(`POST ${APP}/apps/checkin`, () =>
		json({
			data: {
				apiKey: `demo-${crypto.randomUUID().replace(/-/g, '').slice(0, 24)}`,
				latestVersion: '202605190',
				buildExpired: false
			}
		})
	),
	route(`POST ${RESERVATION}/myreservations/token`, async (request) => {
		const { email } = await request.json<{ email?: string }>();
		if (!email || !request.headers.get('user-identifier')) {
			return affluencesError(400, 'invalid_token', 'No user found for this token.');
		}
		const uuid = crypto.randomUUID();
		validations().set(uuid, { email, sentAt: Date.now() });
		return json({ message: 'Email sent', request_uuid: uuid });
	}),
	route(`GET ${RESERVATION}/myreservations/token`, (request) => {
		const sent = validations().get(request.url.searchParams.get('request_uuid') ?? '');
		if (sent === undefined || Date.now() - sent.sentAt < LINK_OPENED_AFTER_MS) {
			return affluencesError(404, 'does_not_exist', 'The request has not been validated yet.');
		}
		return json({ auth_token: `demo-${crypto.randomUUID()}`, message: 'Validated' });
	}),
	route(
		`GET ${RESERVATION}/myreservations`,
		(request) =>
			accountToken(request)
				? listed(DemoStudent.email, false)
				: affluencesError(401, 'missing_auth_token', 'The authentication token is missing.'),
		{
			empty: (request) =>
				accountToken(request)
					? listed(DemoStudent.email, true)
					: affluencesError(401, 'missing_auth_token', 'The authentication token is missing.')
		}
	)
];
