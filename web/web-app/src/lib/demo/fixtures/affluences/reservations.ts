import { DemoStudent } from '../../persona.ts';
import { days, isoDate } from '../../dates.ts';
import { empty, json, route } from '../../router.ts';
import {
	DemoLibraries,
	accountToken,
	affluencesError,
	demoReservations,
	type DemoReservation
} from './account.ts';
import { demoHours, libraryKey } from './sites.ts';

const API = 'https://reservation.affluences.com/api';

type LibraryKey = keyof typeof DemoLibraries;

interface DemoZone {
	id: number;
	library: LibraryKey;
	description: string;
	subdescription: string | null;
	/** Seat names start with it: "R 42". */
	prefix: string;
	seats: number;
	image: string;
}

const CARREL = 'https://static.affluences.media/reservation/types/carrel.png';
const STUDY = 'https://static.affluences.media/reservation/types/self_training.png';

/** The zones of the real Biblioteca Centrale, as its site lists them, and three at Medicina. */
const ZONES: readonly DemoZone[] = [
	{
		id: 518,
		library: 'central',
		description: 'Posto zona ROSSA ',
		subdescription: "Lato Piazza d'Ateneo ",
		prefix: 'R',
		seats: 48,
		image: CARREL
	},
	{
		id: 519,
		library: 'central',
		description: 'Posto zona GIALLA ',
		subdescription: ' Corridoio Celle A-E',
		prefix: 'G',
		seats: 30,
		image: CARREL
	},
	{
		id: 544,
		library: 'central',
		description: 'Posto zona  VERDE - Celle A-E',
		subdescription: null,
		prefix: 'V',
		seats: 24,
		image: CARREL
	},
	{
		id: 545,
		library: 'central',
		description: 'Posto zona LILLA  - Celle F- L',
		subdescription: null,
		prefix: 'L',
		seats: 24,
		image: CARREL
	},
	{
		id: 546,
		library: 'central',
		description: 'Posto zona BLU ',
		subdescription: 'Corridoio Celle F - L',
		prefix: 'B',
		seats: 30,
		image: CARREL
	},
	{
		id: 689,
		library: 'central',
		description: 'Carrels',
		subdescription: 'Spazi per lo studio individuale',
		prefix: 'C',
		seats: 12,
		image: STUDY
	},
	{
		id: 3162,
		library: 'central',
		description: 'Posto zona ARANCIO - Terzo piano',
		subdescription: null,
		prefix: 'A',
		seats: 36,
		image: CARREL
	},
	{
		id: 701,
		library: 'medicine',
		description: 'Posto zona VERDE',
		subdescription: 'Sala lettura, piano terra',
		prefix: 'V',
		seats: 30,
		image: CARREL
	},
	{
		id: 702,
		library: 'medicine',
		description: 'Posto zona BLU',
		subdescription: 'Sala silenziosa, primo piano',
		prefix: 'B',
		seats: 24,
		image: CARREL
	},
	{
		id: 703,
		library: 'medicine',
		description: 'Carrels',
		subdescription: 'Spazi per lo studio individuale',
		prefix: 'C',
		seats: 8,
		image: STUDY
	}
];

/** Seats are numbered 1… within their zone; every third has a power outlet. */
const seatName = (zone: DemoZone, number: number) =>
	`${zone.prefix} ${number}${number % 3 === 0 ? ' - Posto con presa elettrica' : ''}`;

const resourceId = (zone: DemoZone, number: number) => zone.id * 1000 + number;

function seatOf(id: number): { zone: DemoZone; number: number } | undefined {
	const zone = ZONES.find((item) => item.id === Math.floor(id / 1000));
	const number = id % 1000;
	return zone && number >= 1 && number <= zone.seats ? { zone, number } : undefined;
}

const pad = (value: number) => String(value).padStart(2, '0');
const clock = (minutes: number) => `${pad(Math.floor(minutes / 60))}:${pad(minutes % 60)}`;
const toMinutes = (text: string) => {
	const [hours, minutes] = text.split(':').map(Number);
	return hours * 60 + minutes;
};

/** A stable pseudo-random 0–99 per seat, day and hour: who else booked what. */
function noise(seed: string): number {
	let hash = 2166136261;
	for (let index = 0; index < seed.length; index++) {
		hash ^= seed.charCodeAt(index);
		hash = Math.imul(hash, 16777619);
	}
	return (hash >>> 0) % 100;
}

/** How likely an hour is taken by others: busier through the afternoon. */
const BUSY_BY_HOUR = [
	0, 0, 0, 0, 0, 0, 0, 0, 10, 25, 45, 55, 60, 50, 60, 68, 72, 60, 45, 30, 20, 10, 5, 0
];

function takenByOthers(zone: DemoZone, number: number, date: string, hour: number): boolean {
	return noise(`${zone.id}/${number}/${date}/${hour}`) < BUSY_BY_HOUR[hour];
}

function overlapsOwn(id: number, start: Date, end: Date): boolean {
	return demoReservations().some(
		(reservation) =>
			reservation.resource_id === id &&
			reservation.state !== 'CANCELLED' &&
			Date.parse(reservation.reservation_start_datetime) < end.getTime() &&
			Date.parse(reservation.reservation_end_datetime) > start.getTime()
	);
}

const atMinutes = (date: string, minutes: number) => {
	const [year, month, day] = date.split('-').map(Number);
	return new Date(year, month - 1, day, Math.floor(minutes / 60), minutes % 60);
};

/** Whether the seat is free for the whole stay, now that the student may have booked some. */
function bookable(zone: DemoZone, number: number, date: string, start: number, duration: number) {
	const begin = atMinutes(date, start);
	if (begin.getTime() < Date.now()) return false;
	for (let at = start; at < start + duration; at += 60) {
		if (takenByOthers(zone, number, date, Math.floor(at / 60))) return false;
	}
	return !overlapsOwn(resourceId(zone, number), begin, atMinutes(date, start + duration));
}

/** The starts a stay of `duration` fits in, every hour from opening, as Affluences offers them. */
function starts(library: LibraryKey, date: string, duration: number): number[] {
	const hours = demoHours(library, atMinutes(date, 0));
	if (hours === null) return [];
	const list: number[] = [];
	for (let start = hours.open; start + duration <= hours.close; start += 60) list.push(start);
	return list;
}

function availableResources(zone: DemoZone, date: string, duration: number) {
	const hours = starts(zone.library, date, duration);
	return Array.from({ length: zone.seats }, (_, index) => {
		const number = index + 1;
		return {
			resource_id: resourceId(zone, number),
			resource_name: seatName(zone, number),
			resource_type: zone.id,
			granularity: 60,
			time_slot_count: hours.length,
			static_time_slot: false,
			note_available: true,
			note_required: true,
			note_description: 'Cognome e nome',
			description: 'Posto a sedere per studio e consultazione',
			description_html: '<p>Posto a sedere per studio e consultazione</p>',
			capacity: 0,
			site_timezone: 'Europe/Rome',
			user_name_required: false,
			user_phone_required: false,
			user_name_available: false,
			user_phone_available: false,
			image_url: null,
			slots_state: 'available',
			hours: hours.map((start) => {
				const free = bookable(zone, number, date, start, duration);
				return {
					hour: clock(start),
					state: free ? 'available' : 'full',
					reservations: [],
					granularity: 60,
					person_count: free ? 0 : 1,
					places_available: free ? 1 : 0,
					places_bookable: free ? 1 : 0
				};
			})
		};
	});
}

/** The days a zone takes bookings: three weeks ahead, the days its library opens. */
function openDays(library: LibraryKey): string[] {
	const now = new Date();
	return Array.from({ length: 21 }, (_, offset) => days(offset)).flatMap((day, offset) => {
		const hours = demoHours(library, day);
		const minutes = now.getHours() * 60 + now.getMinutes();
		if (hours === null || (offset === 0 && minutes > hours.close - 60)) return [];
		return [isoDate(day)];
	});
}

const twoDigits = (minutes: number) => `${pad(Math.floor(minutes / 60))}h${pad(minutes % 60)}`;

function cancelPreview(reservation: DemoReservation) {
	const start = new Date(reservation.reservation_start_datetime);
	const end = new Date(reservation.reservation_end_datetime);
	const minutesOf = (date: Date) => date.getHours() * 60 + date.getMinutes();
	const live = reservation.state !== 'CANCELLED' && end.getTime() > Date.now();
	return {
		reservation_day: `${pad(start.getDate())}/${pad(start.getMonth() + 1)}/${start.getFullYear()}`,
		start_time: twoDigits(minutesOf(start)),
		end_time: twoDigits(minutesOf(end)),
		duration: twoDigits(Math.round((end.getTime() - start.getTime()) / 60_000)),
		site_name: reservation.site_name,
		resource_name: reservation.resource_name,
		state: reservation.state === 'CANCELLED' ? 'cancelled' : live ? 'confirmed' : 'past',
		cancellable: live,
		static_time_slot: false
	};
}

const CODE_ALPHABET = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';

function newCode(): string {
	return Array.from(crypto.getRandomValues(new Uint8Array(4)), (byte) =>
		CODE_ALPHABET.charAt(byte % CODE_ALPHABET.length)
	).join('');
}

export const routes = [
	route(
		`GET ${API}/sites/:id/infos`,
		(request) => {
			const key = libraryKey(request.params.id);
			if (key === undefined) return affluencesError(400, 'invalid_site_id', 'Invalid site id');
			return json({
				types: ZONES.filter((zone) => zone.library === key).map((zone) => ({
					resource_type: zone.id,
					image: zone.image,
					localized_description: zone.description,
					localized_subdescription: zone.subdescription,
					rank: null,
					map: null,
					time_limit: new Date().toISOString()
				})),
				contact_email: null
			});
		},
		{ empty: () => json({ types: [], contact_email: null }) }
	),
	route(`GET ${API}/sites/:id/agreements`, () =>
		json({
			agreements: [
				{
					agreements_id: 1,
					agreements_details_id: 9,
					latest_change: '2018-05-22T15:00:00.000Z',
					name: "Condizioni d'uso",
					url: 'https://www.affluences.com/reservation-agreements',
					mandatory_consent: true
				}
			]
		})
	),
	route(
		`GET ${API}/sites/:id/types/:type/filters`,
		(request) => {
			const zone = ZONES.find((item) => item.id === Number(request.params.type));
			if (zone === undefined) return affluencesError(400, 'invalid_type', 'Invalid type');
			return json({
				resource_type_id: zone.id,
				open_days: openDays(zone.library),
				open_hours: starts(zone.library, isoDate(days(1)), 60).map(clock),
				durations: Array.from({ length: 14 }, (_, index) => (index + 1) * 60),
				capacities: [1],
				resources: Array.from({ length: zone.seats }, (_, index) => ({
					resource_id: resourceId(zone, index + 1),
					resource_name: seatName(zone, index + 1)
				}))
			});
		},
		{
			empty: (request) =>
				json({
					resource_type_id: Number(request.params.type),
					open_days: [],
					open_hours: [],
					durations: [],
					capacities: [],
					resources: []
				})
		}
	),
	route(
		`GET ${API}/resources/:id/available`,
		(request) => {
			const query = request.url.searchParams;
			const zone = ZONES.find((item) => item.id === Number(query.get('type')));
			const date = query.get('date') ?? '';
			if (zone === undefined) return json([]);
			return json(availableResources(zone, date, Number(query.get('duration')) || 60));
		},
		{ empty: () => json([]) }
	),
	route(`POST ${API}/reserve/:resource`, async (request) => {
		if (!accountToken(request)) {
			return affluencesError(401, 'missing_auth_token', 'The authentication token is missing.');
		}
		const id = Number(request.params.resource);
		const seat = seatOf(id);
		const body = await request.json<{
			email?: string;
			date?: string;
			start_time?: string;
			end_time?: string;
			note?: string | null;
		}>();
		if (seat === undefined || !body.date || !body.start_time || !body.end_time) {
			return affluencesError(400, 'invalid_parameters', 'Invalid parameters');
		}
		if (!body.note?.trim())
			return affluencesError(400, 'note_required', 'Il campo nota è obbligatorio');
		const start = toMinutes(body.start_time);
		const duration = (toMinutes(body.end_time) - start + 24 * 60) % (24 * 60);
		if (!bookable(seat.zone, seat.number, body.date, start, duration)) {
			return affluencesError(
				400,
				'no_place_available',
				'Nessun posto disponibile per questo orario'
			);
		}
		const reservations = demoReservations();
		const library = DemoLibraries[seat.zone.library];
		const reservationId = 9100 + reservations.length;
		const reservation: DemoReservation = {
			reservation_id: reservationId,
			resource_id: id,
			resource_name: seatName(seat.zone, seat.number),
			site_name: library.name,
			site_secondary_name: library.secondary,
			site_timezone: 'Europe/Rome',
			reservation_token: newCode(),
			cancellation_token: `demo-cancel-${reservationId}`,
			person_count: 1,
			note: body.note.trim(),
			reservation_start_datetime: atMinutes(body.date, start).toISOString(),
			reservation_end_datetime: atMinutes(body.date, start + duration).toISOString(),
			request_datetime: new Date().toISOString(),
			state: 'UPCOMING',
			validated: false,
			confirmation_mandatory: false,
			user_validation: false
		};
		reservations.push(reservation);
		return json({
			reservation_id: reservationId,
			auth_token: null,
			user_validation: false,
			person_count: 1,
			email: body.email ?? DemoStudent.email,
			success: 'reservation_confirmed',
			successMessage: 'Prenotazione confermata',
			cancellation_token: reservation.cancellation_token
		});
	}),
	route(`GET ${API}/cancelReservation/:token`, (request) => {
		const reservation = demoReservations().find(
			(item) => item.cancellation_token === request.params.token
		);
		return reservation ? json(cancelPreview(reservation)) : empty(400);
	}),
	route(`DELETE ${API}/cancelReservation/`, (request) => {
		const token = request.url.searchParams.get('email_token');
		const reservation = demoReservations().find((item) => item.cancellation_token === token);
		if (reservation === undefined || reservation.state === 'CANCELLED') {
			return affluencesError(404, 'reservation_not_found', 'Reservation not found');
		}
		reservation.state = 'CANCELLED';
		return json({ successMessage: 'La prenotazione è stata cancellata' });
	}),
	route(`POST ${API}/validateReservation`, async (request) => {
		const { validationCode } = await request.json<{ validationCode?: string; email?: string }>();
		const now = Date.now();
		const current = demoReservations().find(
			(item) =>
				item.state !== 'CANCELLED' &&
				Date.parse(item.reservation_start_datetime) - 10 * 60_000 <= now &&
				Date.parse(item.reservation_end_datetime) >= now
		);
		if (!validationCode || validationCode.trim().length < 4 || current === undefined) {
			return affluencesError(400, 'invalid_validation_code', 'Codice di validazione non valido');
		}
		current.validated = true;
		return json({ validateReservationWithCode: [{ reservation_id: current.reservation_id }] });
	})
];
