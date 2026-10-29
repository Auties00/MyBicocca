import type {
	AffluencesAvailableResource,
	AffluencesForecastSlot,
	AffluencesLiveData,
	AffluencesMyReservation
} from '@mybicocca/data-api/affluences';
import { describe, expect, it } from 'vitest';
import {
	activeReservations,
	buildingReference,
	cancelTokenOf,
	checkInPhase,
	cleanAddress,
	cleanPlace,
	endMinutes,
	forecastSlots,
	occupancyLevel,
	parseClock,
	seatsAt,
	startTimes,
	toCancelPreview,
	toReservation,
	toSeats,
	zoneColor
} from './model.ts';

function reservation(overrides: Partial<AffluencesMyReservation> = {}): AffluencesMyReservation {
	return {
		reservationId: 1,
		resourceId: 10,
		resourceName: ' R 42 - Posto con presa elettrica ',
		siteName: 'Biblioteca Centrale',
		siteSecondaryName: 'Ed. U6 Agorà (Unimib)',
		siteTimeZone: 'Europe/Rome',
		reservationToken: 'A7K2',
		cancellationToken: 'cancel-1',
		personCount: 1,
		note: '  ',
		startDateTime: '2026-10-28T14:00:00.000Z',
		endDateTime: '2026-10-28T16:30:00.000Z',
		requestDateTime: null,
		state: 'UPCOMING',
		validated: false,
		confirmationMandatory: false,
		userValidation: false,
		ticketPayload: null,
		...overrides
	};
}

describe('toReservation', () => {
	it('shows UTC times on the library clock and tidies the names', () => {
		const booking = toReservation(reservation());
		expect(booking).toMatchObject({
			library: 'Biblioteca Centrale',
			place: 'Ed. U6 Agorà',
			seat: 'R 42 - Posto con presa elettrica',
			seatShort: 'R 42',
			outlet: true,
			note: null,
			code: 'A7K2',
			state: 'upcoming'
		});
		expect(booking?.start.getHours()).toBe(15);
		expect(booking?.end.getMinutes()).toBe(30);
	});

	it('leaves out cancelled and undated bookings', () => {
		expect(toReservation(reservation({ state: 'CANCELLED' }))).toBeNull();
		expect(toReservation(reservation({ startDateTime: null }))).toBeNull();
		expect(toReservation(reservation({ endDateTime: 'soon' }))).toBeNull();
	});

	it('knows a booking whose attendance was recorded', () => {
		expect(toReservation(reservation())?.checkedIn).toBe(false);
		expect(toReservation(reservation({ validated: true }))?.checkedIn).toBe(true);
	});
});

describe('activeReservations', () => {
	it('keeps the bookings not over yet, soonest first', () => {
		const now = Date.parse('2026-10-28T15:00:00Z');
		const list = activeReservations(
			[
				reservation({
					reservationId: 1,
					startDateTime: '2026-10-29T08:00:00Z',
					endDateTime: '2026-10-29T12:00:00Z'
				}),
				reservation({ reservationId: 2 }),
				reservation({
					reservationId: 3,
					startDateTime: '2026-10-27T08:00:00Z',
					endDateTime: '2026-10-27T12:00:00Z'
				})
			],
			now
		);
		expect(list.map((item) => item.id)).toEqual([2, 1]);
	});
});

describe('checkInPhase', () => {
	const booking = toReservation(reservation());
	if (booking === null) throw new Error('fixture');

	it('opens ten minutes before the start and closes at the end', () => {
		expect(checkInPhase(booking, Date.parse('2026-10-28T13:49:00Z'))).toBe('early');
		expect(checkInPhase(booking, Date.parse('2026-10-28T13:50:00Z'))).toBe('open');
		expect(checkInPhase(booking, Date.parse('2026-10-28T16:30:00Z'))).toBe('open');
		expect(checkInPhase(booking, Date.parse('2026-10-28T16:31:00Z'))).toBe('ended');
	});
});

describe('occupancyLevel', () => {
	it('reads green below 40 and red from 75', () => {
		expect(occupancyLevel(39)).toBe('low');
		expect(occupancyLevel(40)).toBe('mid');
		expect(occupancyLevel(74)).toBe('mid');
		expect(occupancyLevel(75)).toBe('high');
	});
});

describe('parseClock', () => {
	it('reads times alone and inside stamps', () => {
		expect(parseClock('08:30')).toBe(510);
		expect(parseClock('2026-10-27 22:00:00')).toBe(1320);
		expect(parseClock('9:05:00')).toBe(545);
		expect(parseClock('25:00')).toBeUndefined();
		expect(parseClock(null)).toBeUndefined();
	});
});

function slot(range: string[] | null, occupancy: number | null): AffluencesForecastSlot {
	return {
		hourRange: range ?? [],
		flow: null,
		flowEvolution: null,
		occupancyPercentage: occupancy,
		occupancyEvolution: null,
		waitingTimeMinutes: null,
		waitingTimeEvolution: null
	};
}

function live(
	today: AffluencesForecastSlot[],
	fallback: AffluencesForecastSlot[] = []
): AffluencesLiveData {
	return {
		status: {
			isOpen: true,
			openingText: null,
			openingAt: null,
			closingText: null,
			closingAt: null,
			isOpeningSoon: null,
			isClosingSoon: null
		},
		notices: [],
		liveAttendance: null,
		todayForecasts: fallback,
		forecasts: [{ day: '2026-10-27', todayOffset: 0, forecasts: today }]
	};
}

describe('forecastSlots', () => {
	it('drops the day summary and keeps each slot its own length', () => {
		const slots = forecastSlots(
			live([
				slot(null, 45),
				slot(['2026-10-27 14:30:00', '2026-10-27 17:30:00'], 60),
				slot(['2026-10-27 13:30:00', '2026-10-27 14:00:00'], 45),
				slot(['2026-10-27 18:00:00'], null)
			])
		);
		expect(slots).toEqual([
			{ start: 810, end: 840, occupancy: 45 },
			{ start: 870, end: 1050, occupancy: 60 }
		]);
	});

	it('lets a slot without an end last until the next, and falls back to today’s list', () => {
		const slots = forecastSlots(
			live(
				[],
				[
					slot(['2026-10-27 09:00:00'], 20),
					slot(['2026-10-27 10:00:00', '2026-10-27 10:30:00'], 30)
				]
			)
		);
		expect(slots[0]).toEqual({ start: 540, end: 600, occupancy: 20 });
	});
});

function resource(
	id: number,
	name: string,
	hours: Array<[string, number]>
): AffluencesAvailableResource {
	return {
		resourceId: id,
		resourceName: name,
		resourceTypeId: 518,
		granularityMinutes: 60,
		timeSlotCount: hours.length,
		staticTimeSlot: false,
		noteAvailable: true,
		noteRequired: true,
		noteDescription: 'Cognome e nome',
		description: null,
		descriptionHtml: null,
		capacity: 0,
		siteTimeZone: 'Europe/Rome',
		userNameRequired: false,
		userPhoneRequired: false,
		userNameAvailable: false,
		userPhoneAvailable: false,
		timeBeforeReservationsClosed: null,
		minPlacesPerReservation: null,
		maxPlacesPerReservation: null,
		imageUrl: null,
		slotsState: null,
		hours: hours.map(([hour, bookable]) => ({
			hour,
			state: null,
			granularityMinutes: 60,
			personCount: 0,
			placesAvailable: bookable,
			placesBookable: bookable
		}))
	};
}

describe('seats and start times', () => {
	const seats = toSeats([
		resource(3, 'R 10', [
			['08:30', 1],
			['09:30', 0]
		]),
		resource(1, 'R 2 - Posto con presa elettrica', [
			['08:30', 1],
			['09:30', 1]
		]),
		resource(2, 'R 1', [])
	]);

	it('orders seats naturally, drops those without slots and reads the outlet', () => {
		expect(seats.map((seat) => seat.short)).toEqual(['R 2', 'R 10']);
		expect(seats[0].outlet).toBe(true);
		expect(seats[0].noteLabel).toBe('Cognome e nome');
	});

	it('offers the starts some seat is free at, counting the free seats', () => {
		expect(startTimes(seats)).toEqual([
			{ minutes: 510, free: 2 },
			{ minutes: 570, free: 1 }
		]);
		expect(seatsAt(seats, 570).map((seat) => seat.id)).toEqual([1]);
	});

	it('ends a stay past midnight on the next day’s clock', () => {
		expect(endMinutes(23 * 60, 120)).toBe(60);
	});
});

describe('names', () => {
	it('matches the colour Bicocca names its zones after', () => {
		expect(zoneColor('Posto zona  VERDE - Celle A-E')).toBe('green');
		expect(zoneColor('Posto zona ROSSA ')).toBe('red');
		expect(zoneColor('Carrels')).toBe('carrel');
		expect(zoneColor('Sala lettura')).toBe('other');
	});

	it('tidies places, addresses and finds the campus building', () => {
		expect(cleanPlace('Ed. U8 Asclepio (Unimib)')).toBe('Ed. U8 Asclepio');
		expect(cleanPlace('  ')).toBeNull();
		expect(cleanAddress("1 Piazza dell'Ateneo Nuovo, 20126 Milano, Italie")).toBe(
			"1 Piazza dell'Ateneo Nuovo, 20126 Milano"
		);
		expect(buildingReference('Ed. U6 Agorà')).toBe('U6');
		expect(buildingReference('Sede di Monza')).toBeUndefined();
	});
});

describe('toCancelPreview', () => {
	it('reads the link’s day and its "16h00" times', () => {
		expect(
			toCancelPreview({
				reservationDay: '28/10/2026',
				startTime: '16h00',
				endTime: '18h30',
				duration: '02h30',
				siteName: 'Biblioteca Centrale',
				resourceName: 'R 42 - Posto con presa elettrica',
				state: 'confirmed',
				cancellable: true,
				staticTimeSlot: false
			})
		).toEqual({
			day: '2026-10-28',
			start: 960,
			end: 1110,
			library: 'Biblioteca Centrale',
			seat: 'R 42',
			outlet: true,
			cancellable: true
		});
	});

	it('finds the token in a pasted link, and nothing in anything else', () => {
		expect(
			cancelTokenOf(' https://affluences.com/it/reservation/cancel?reservationToken=abc123 ')
		).toBe('abc123');
		expect(cancelTokenOf('https://affluences.com/it/reservation/cancel')).toBeUndefined();
		expect(cancelTokenOf('abc123')).toBeUndefined();
	});
});
