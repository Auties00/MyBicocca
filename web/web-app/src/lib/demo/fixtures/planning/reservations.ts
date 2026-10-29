import { romeDay, romeWallClock } from '#lib/format/rome.ts';
import { days, epochSeconds } from '../../dates.ts';
import { demoPdf } from '../../pdf.ts';
import { DemoStudent } from '../../persona.ts';
import { bytes, json, route, type DemoRequest } from '../../router.ts';
import { DEMO_SESSION_PREFIX, demoState } from '../../state.ts';
import { DemoAreas, PLANNING_API, demoService, type DemoArea } from './catalog.ts';
import { qrDataUrl } from './qr.ts';

/** A booking the demo portal knows: held until confirmed, as the real one. */
export interface DemoBooking {
	code: string;
	email: string;
	entryId: number;
	serviceId: number;
	serviceName: string;
	area: DemoArea;
	/** Epoch seconds. */
	start: number;
	end: number;
	confirmed: boolean;
	webConferenceUrl: string | null;
}

/** Tomorrow, or the Monday after when tomorrow falls on a weekend, when the desks are closed. */
function nextOpenDay(): number {
	let offset = 1;
	while (days(offset).getDay() % 6 === 0) offset++;
	return offset;
}

/**
 * The persona's booking from the Android demo: Segreteria studenti, tomorrow at 14:00, at desk 3
 * (a slot of the desk's afternoon, which it then shows as taken). The demo account starts with it
 * among its appointments.
 */
export function personaBooking(): DemoBooking {
	const day = nextOpenDay();
	return {
		code: 'BK-48213',
		email: DemoStudent.email,
		entryId: 48213,
		serviceId: 1,
		serviceName: 'Segreteria studenti',
		area: { ...DemoAreas.careers, name: 'Sportello 3' },
		start: epochSeconds(days(day, 14, 0)),
		end: epochSeconds(days(day, 14, 20)),
		confirmed: true,
		webConferenceUrl: null
	};
}

interface DemoPortal {
	bookings: DemoBooking[];
	/** Entry ids are never reused, as the portal's aren't: a released hold keeps its number. */
	nextEntry: number;
}

/*
 * The portal keeps its bookings across page loads, so the demo keeps them for the tab's session:
 * after a reload the app still finds what it booked. A demo sign-out clears the key.
 */
const STORAGE_KEY = `${DEMO_SESSION_PREFIX}planning`;

function initialPortal(): DemoPortal {
	try {
		const raw = sessionStorage.getItem(STORAGE_KEY);
		if (raw !== null) return JSON.parse(raw) as DemoPortal;
	} catch {
		// Without storage the demo forgets its bookings on reload, nothing worse.
	}
	return { bookings: [personaBooking()], nextEntry: 48301 };
}

const portal = () => demoState('planning.portal', initialPortal);

export const demoBookings = () => portal().bookings;

function save(): void {
	try {
		sessionStorage.setItem(STORAGE_KEY, JSON.stringify(portal()));
	} catch {
		// As above: the change holds until the page reloads.
	}
}

const notFound = (code = 'not_found') => json({ message: code }, { status: 404 });

function find(request: DemoRequest): DemoBooking | undefined {
	const email = request.url.searchParams.get('chiave')?.trim().toLowerCase();
	return demoBookings().find(
		(booking) => booking.code === request.params.code && booking.email.toLowerCase() === email
	);
}

/** Whether a booking holds the slot starting at `start` (epoch seconds) of `serviceId`. */
export function isBooked(serviceId: number, start: number): boolean {
	return demoBookings().some(
		(booking) => booking.serviceId === serviceId && booking.start === start
	);
}

interface StoreBody {
	start_time: number;
	end_time: number;
	durata: number;
	entry_type: number;
	area: number;
	public_primary: string;
	utente: Record<string, string>;
}

const wallTime = (seconds: number) => {
	const clock = romeWallClock(seconds * 1000);
	const pad = (value: number) => String(value).padStart(2, '0');
	return `${clock.date.split('-').reverse().join('/')} ${pad(Math.floor(clock.minutes / 60))}:${pad(clock.minutes % 60)}`;
};

export const routes = [
	route(`POST ${PLANNING_API}/entry/store`, async (request) => {
		const body = await request.json<StoreBody>();
		const offered = demoService(body.entry_type);
		if (offered === undefined || offered.area.id !== body.area)
			return json({ message: 'service_not_available' }, { status: 422 });
		if (isBooked(offered.id, body.start_time))
			return json({ message: 'slot_not_available' }, { status: 422 });
		const entryId = portal().nextEntry++;
		const booking: DemoBooking = {
			code: `BK-${entryId}`,
			email: body.public_primary,
			entryId,
			serviceId: offered.id,
			serviceName: offered.name,
			area: offered.area,
			start: body.start_time,
			end: body.end_time,
			confirmed: false,
			webConferenceUrl: offered.virtual ? `https://unimib.zoom.us/j/8412${entryId}` : null
		};
		demoBookings().push(booking);
		save();
		return json({
			codice_prenotazione: booking.code,
			user_primary: booking.email,
			entry: { id: entryId },
			user_identifier: body.utente.cognome_nome ?? '',
			start_time: booking.start,
			end_time: booking.end,
			repeat_id: entryId + 1000,
			public_fields: { email: 'Email', cognome_nome: 'Cognome e Nome', matricola: 'Matricola' },
			web_conference_url: booking.webConferenceUrl ?? '',
			qr_code: qrDataUrl(booking.code)
		});
	}),
	route(`POST ${PLANNING_API}/entry/confirm/:entryId`, (request) => {
		const booking = demoBookings().find((item) => item.entryId === Number(request.params.entryId));
		if (booking === undefined) return notFound();
		booking.confirmed = true;
		save();
		return json({ success: true });
	}),
	route(
		`GET ${PLANNING_API}/entry/manage/:code`,
		(request) => {
			const booking = find(request);
			if (booking === undefined) return notFound();
			return json({
				success: true,
				codice: booking.code,
				primaria: booking.email,
				public_fields: { email: booking.email },
				area: { name: booking.area.name, address: booking.area.address },
				entry: {
					id: booking.entryId,
					start_time: booking.start,
					end_time: booking.end,
					day_time: romeDay(booking.start * 1000)
				},
				servizio: { type: booking.serviceName, servizio_breve: false },
				libera_posto: false,
				risorsa: booking.area.name,
				confermata: booking.confirmed,
				stato: booking.confirmed ? 'confermata' : 'provvisoria',
				qr_code: qrDataUrl(booking.code)
			});
		},
		{ empty: () => notFound() }
	),
	route(
		`GET ${PLANNING_API}/entry/:code/info`,
		(request) => {
			const booking = find(request);
			return booking === undefined ? notFound('reservation_not_found') : json(booking.serviceId);
		},
		{ empty: () => notFound('reservation_not_found') }
	),
	route(`POST ${PLANNING_API}/entry/delete/:code`, (request) => {
		const booking = find(request);
		if (booking === undefined) return notFound();
		const list = demoBookings();
		list.splice(list.indexOf(booking), 1);
		save();
		return json({ success: true });
	}),
	route(`GET ${PLANNING_API}/entry/pdf/:entryId`, (request) => {
		const booking = demoBookings().find((item) => item.entryId === Number(request.params.entryId));
		if (booking === undefined) return notFound();
		return bytes(
			demoPdf('Promemoria appuntamento', [
				booking.serviceName,
				`Codice prenotazione: ${booking.code}`,
				`Data e ora: ${wallTime(booking.start)}`,
				booking.webConferenceUrl
					? `In videoconferenza: ${booking.webConferenceUrl}`
					: `Sede: ${booking.area.name}`,
				`Intestata a: ${booking.email}`
			]),
			'application/pdf'
		);
	})
];
