import {
	AffluencesReservationState,
	type AffluencesAvailableResource,
	type AffluencesForecastSlot,
	type AffluencesLiveData,
	type AffluencesMyReservation,
	type AffluencesReservationToCancel,
	type AffluencesResourceTypeInfo,
	type AffluencesSiteCard,
	type AffluencesWeekTimetable
} from '@mybicocca/data-api/affluences';
import { isIsoDate, type IsoDate } from '#lib/format/days.ts';
import { romeDate } from '#lib/format/rome.ts';

/** The site of the university's library system, whose children are the bookable libraries. */
export const LIBRARY_ROOT_ID = '61aa6184-f707-4dbd-9acb-45ff4e20c6b7';

/** How long before the start the library accepts a check-in (observed on Android). */
export const CHECK_IN_LEAD_MINUTES = 10;

const MINUTE_MS = 60_000;
const DAY_MINUTES = 24 * 60;

export type ReservationState = 'upcoming' | 'awaiting' | 'ongoing' | 'past' | 'other';

/** A seat booked with the linked Affluences account. */
export interface Reservation {
	id: number;
	library: string;
	/** The library's building, as "Ed. U6 Agorà". */
	place: string | null;
	seat: string;
	/** The seat's name before its description: "R 42" for "R 42 - Posto con presa elettrica". */
	seatShort: string;
	outlet: boolean;
	/** Epoch milliseconds, for comparing with now. */
	startsAt: number;
	endsAt: number;
	/** The same instants as the library's wall clock (Europe/Rome), for display. */
	start: Date;
	end: Date;
	note: string | null;
	/** The short code the library asks for: not a credential. */
	code: string | null;
	/** What cancels the booking; without it the app can't. */
	cancellationToken: string | null;
	state: ReservationState;
	/** Attendance was recorded at the library. */
	checkedIn: boolean;
}

const STATES: Record<string, ReservationState> = {
	[AffluencesReservationState.Upcoming]: 'upcoming',
	[AffluencesReservationState.AwaitingConfirmation]: 'awaiting',
	[AffluencesReservationState.Ongoing]: 'ongoing',
	[AffluencesReservationState.Past]: 'past'
};

const blankToNull = (value: string | null | undefined) => value?.trim() || null;

/** A seat's name before " - ": "R 100 - Posto con presa elettrica" is "R 100". */
function shortSeatName(name: string): string {
	return name.split(' -')[0].trim() || name;
}

/** The libraries say so in the seat's name. */
function hasOutlet(name: string): boolean {
	return /presa elettrica/i.test(name);
}

/** The building of a library without the "(Unimib)" every Bicocca site carries. */
export function cleanPlace(secondary: string | null | undefined): string | null {
	return blankToNull(secondary?.replace(/\s*\(unimib\)\s*$/i, ''));
}

/** The campus building a place names ("Ed. U6 Agorà" is "U6"), as the map looks it up. */
export function buildingReference(place: string | null): string | undefined {
	const match = /\bU\s?(\d{1,2})\b/i.exec(place ?? '');
	return match ? `U${Number(match[1])}` : undefined;
}

/** Affluences writes the country in French ("…, Milano, Italie"); every library is in Italy. */
export function cleanAddress(address: string | null): string | null {
	return blankToNull(address?.replace(/,\s*(italie|italia|italy)\s*$/i, ''));
}

/** An Italian landline in its usual groups: "+390264486251" reads "+39 02 6448 6251". */
export function phoneText(phone: string): string {
	const match = /^\+39(0\d)(\d{4})(\d{4})$/.exec(phone.replace(/\s+/g, ''));
	return match ? `+39 ${match[1]} ${match[2]} ${match[3]}` : phone;
}

/** A booking from the account's list; null when cancelled or without valid times. */
export function toReservation(wire: AffluencesMyReservation): Reservation | null {
	if (wire.state === AffluencesReservationState.Cancelled) return null;
	const startsAt = Date.parse(wire.startDateTime ?? '');
	const endsAt = Date.parse(wire.endDateTime ?? '');
	if (Number.isNaN(startsAt) || Number.isNaN(endsAt)) return null;
	const seat = wire.resourceName?.trim() ?? '';
	return {
		id: wire.reservationId,
		library: wire.siteName?.trim() ?? '',
		place: cleanPlace(wire.siteSecondaryName),
		seat,
		seatShort: shortSeatName(seat),
		outlet: hasOutlet(seat),
		startsAt,
		endsAt,
		start: romeDate(startsAt),
		end: romeDate(endsAt),
		note: blankToNull(wire.note),
		code: blankToNull(wire.reservationToken),
		cancellationToken: blankToNull(wire.cancellationToken),
		state: (wire.state && STATES[wire.state]) || 'other',
		checkedIn: wire.validated
	};
}

export function activeReservations(
	wires: readonly AffluencesMyReservation[],
	now: number
): Reservation[] {
	return wires
		.flatMap((wire) => toReservation(wire) ?? [])
		.filter((reservation) => reservation.endsAt > now)
		.sort((a, b) => a.startsAt - b.startsAt);
}

export type CheckInPhase = 'early' | 'open' | 'ended';

/** Where `now` falls in the check-in window, which opens shortly before the start. */
export function checkInPhase(reservation: Reservation, now: number): CheckInPhase {
	if (now < reservation.startsAt - CHECK_IN_LEAD_MINUTES * MINUTE_MS) return 'early';
	return now > reservation.endsAt ? 'ended' : 'open';
}

/** When the check-in window opens, on the library's wall clock. */
export function checkInOpensAt(reservation: Reservation): Date {
	return romeDate(reservation.startsAt - CHECK_IN_LEAD_MINUTES * MINUTE_MS);
}

/** A bookable library of the university, with the occupancy its listing carries. */
export interface Library {
	id: string;
	name: string;
	place: string | null;
	picture: string | null;
	open: boolean | null;
	/** 0–100; only meaningful while open. */
	occupancy: number | null;
}

export function toLibrary(card: AffluencesSiteCard): Library {
	return {
		id: card.id,
		name: card.primaryName.trim(),
		place: cleanPlace(card.secondaryName),
		picture: card.pictures[0] ?? null,
		open: card.status?.isOpen ?? null,
		occupancy: card.liveAttendance?.occupancyPercentage ?? null
	};
}

/** The children of the library system that take bookings; a site that doesn't say is kept. */
export function bookableLibraries(cards: readonly AffluencesSiteCard[]): Library[] {
	return cards.filter((card) => card.booking?.hasBooking ?? true).map(toLibrary);
}

export type OccupancyLevel = 'low' | 'mid' | 'high';

/** How busy an occupancy reads: the thresholds Android colours green, amber and red. */
export function occupancyLevel(percent: number): OccupancyLevel {
	if (percent < 40) return 'low';
	return percent < 75 ? 'mid' : 'high';
}

/** Minutes of the day in "HH:mm", "HH:mm:ss" or "yyyy-MM-dd HH:mm:ss"; undefined otherwise. */
export function parseClock(text: string | null | undefined): number | undefined {
	const match = /(?:^|[ T])(\d{1,2}):(\d{2})(?::\d{2})?$/.exec(text?.trim() ?? '');
	if (!match) return undefined;
	const hours = Number(match[1]);
	const minutes = Number(match[2]);
	return hours < 24 && minutes < 60 ? hours * 60 + minutes : undefined;
}

/** The day of a "yyyy-MM-dd HH:mm:ss" stamp. */
function dayOf(text: string | null | undefined): IsoDate | undefined {
	return /^(\d{4}-\d{2}-\d{2})/.exec(text?.trim() ?? '')?.[1];
}

/** "HH:mm" for minutes of the day, as Affluences reads and writes times. */
export function clockText(minutes: number): string {
	const wrapped = ((minutes % DAY_MINUTES) + DAY_MINUTES) % DAY_MINUTES;
	return `${String(Math.floor(wrapped / 60)).padStart(2, '0')}:${String(wrapped % 60).padStart(2, '0')}`;
}

/** When a site that is closed opens again, or when an open one closes. */
export interface StatusChange {
	day: IsoDate | undefined;
	minutes: number;
}

export interface LiveStatus {
	open: boolean;
	occupancy: number | null;
	closes: StatusChange | undefined;
	opens: StatusChange | undefined;
	/** The site's own sentence, in Italian, when no time is given. */
	text: string | null;
}

export function toLiveStatus(live: AffluencesLiveData): LiveStatus {
	const { status } = live;
	const change = (stamp: string | null): StatusChange | undefined => {
		const minutes = parseClock(stamp);
		return minutes === undefined ? undefined : { day: dayOf(stamp), minutes };
	};
	return {
		open: status.isOpen,
		occupancy: live.liveAttendance?.occupancyPercentage ?? null,
		closes: status.isOpen ? change(status.closingAt) : undefined,
		opens: status.isOpen ? undefined : change(status.openingAt),
		text: blankToNull(status.isOpen ? status.closingText : status.openingText)
	};
}

/** One slot of the day's forecast; slots differ in length. */
export interface ForecastSlot {
	start: number;
	end: number;
	occupancy: number;
}

/**
 * Today's forecast, from now to closing: the day of the per-day forecasts whose offset is 0, else
 * `todayForecasts`. The whole-day summary (no hours) and slots without a figure are left out; a
 * slot without an end lasts until the next one.
 */
export function forecastSlots(live: AffluencesLiveData): ForecastSlot[] {
	const today = live.forecasts.find((day) => day.todayOffset === 0)?.forecasts;
	const source: readonly AffluencesForecastSlot[] =
		today && today.length > 0 ? today : live.todayForecasts;
	const parsed = source
		.flatMap((slot) => {
			const start = parseClock(slot.hourRange[0]);
			if (start === undefined || slot.occupancyPercentage === null) return [];
			return [{ start, end: parseClock(slot.hourRange[1]), occupancy: slot.occupancyPercentage }];
		})
		.sort((a, b) => a.start - b.start);
	return parsed.map((slot, index) => {
		const next = parsed[index + 1]?.start;
		const end =
			slot.end !== undefined && slot.end > slot.start ? slot.end : (next ?? slot.start + 60);
		return { start: slot.start, end, occupancy: Math.max(0, Math.min(100, slot.occupancy)) };
	});
}

export function slotAt(slots: readonly ForecastSlot[], minutes: number): number {
	return slots.findIndex((slot) => slot.start <= minutes && minutes < slot.end);
}

export interface OpeningRange {
	open: number;
	close: number;
}

export interface DayHours {
	day: IsoDate;
	today: boolean;
	ranges: OpeningRange[];
}

/** The week's opening hours; ranges that can't be read are dropped. */
export function weekHours(timetable: AffluencesWeekTimetable): DayHours[] {
	return timetable.entries.map((entry) => ({
		day: entry.day,
		today: entry.isToday,
		ranges: entry.openingHours.flatMap((range) => {
			const open = parseClock(range.openingHour);
			const close = parseClock(range.closingHour);
			return open === undefined || close === undefined ? [] : [{ open, close }];
		})
	}));
}

export type ZoneColor =
	'red' | 'yellow' | 'green' | 'lilac' | 'blue' | 'orange' | 'carrel' | 'other';

/** Bicocca's zones are named after colours ("Posto zona ROSSA"), and the app honours them. */
const ZONE_KEYWORDS: ReadonlyArray<readonly [string, ZoneColor]> = [
	['ROSSA', 'red'],
	['GIALLA', 'yellow'],
	['VERDE', 'green'],
	['LILLA', 'lilac'],
	['BLU', 'blue'],
	['ARANCIO', 'orange'],
	['CARREL', 'carrel']
];

export function zoneColor(description: string): ZoneColor {
	const upper = description.toUpperCase();
	return ZONE_KEYWORDS.find(([keyword]) => upper.includes(keyword))?.[1] ?? 'other';
}

export interface Zone {
	id: number;
	/** Null when the site gives none: the page names it generically. */
	name: string | null;
	detail: string | null;
	color: ZoneColor;
}

const tidy = (text: string | null | undefined) => blankToNull(text?.replace(/\s+/g, ' '));

/** A bookable zone, in the server's order. */
export function toZone(type: AffluencesResourceTypeInfo): Zone {
	const name = tidy(type.description);
	return {
		id: type.resourceTypeId,
		name,
		detail: tidy(type.subdescription),
		color: zoneColor(name ?? '')
	};
}

export interface SeatSlot {
	start: number;
	bookable: number;
}

export interface Seat {
	id: number;
	name: string;
	/** The name before " - ": "R 100 - Posto con presa elettrica" is "R 100". */
	short: string;
	outlet: boolean;
	description: string | null;
	noteRequired: boolean;
	/** What the note is for ("Cognome e nome"); a seat with one asks for it. */
	noteLabel: string | null;
	slots: SeatSlot[];
}

export function toSeat(resource: AffluencesAvailableResource): Seat {
	const name = resource.resourceName?.trim() ?? '';
	return {
		id: resource.resourceId,
		name,
		short: shortSeatName(name),
		outlet: hasOutlet(name),
		description: blankToNull(resource.description),
		noteRequired: resource.noteRequired,
		noteLabel: blankToNull(resource.noteDescription),
		slots: resource.hours.flatMap((slot) => {
			const start = parseClock(slot.hour);
			return start === undefined ? [] : [{ start, bookable: slot.placesBookable }];
		})
	};
}

const collator = new Intl.Collator('it', { numeric: true, sensitivity: 'base' });

/** Seats with any slot, in natural order ("R 2" before "R 10"). */
export function toSeats(resources: readonly AffluencesAvailableResource[]): Seat[] {
	return resources
		.filter((resource) => resource.hours.length > 0)
		.map(toSeat)
		.sort((a, b) => collator.compare(a.short, b.short));
}

export interface StartTime {
	minutes: number;
	/** Seats bookable from this start for the chosen duration. */
	free: number;
}

/** The starts at which some seat is bookable, earliest first, with how many seats are. */
export function startTimes(seats: readonly Seat[]): StartTime[] {
	const counts = new Map<number, number>();
	for (const seat of seats) {
		for (const slot of seat.slots) {
			if (slot.bookable > 0) counts.set(slot.start, (counts.get(slot.start) ?? 0) + 1);
		}
	}
	return [...counts]
		.map(([minutes, free]) => ({ minutes, free }))
		.sort((a, b) => a.minutes - b.minutes);
}

export function isFreeAt(seat: Seat, start: number): boolean {
	return seat.slots.some((slot) => slot.start === start && slot.bookable > 0);
}

export function seatsAt(seats: readonly Seat[], start: number): Seat[] {
	return seats.filter((seat) => isFreeAt(seat, start));
}

/** The end of a booking as minutes of the day, wrapping past midnight. */
export function endMinutes(start: number, duration: number): number {
	return (start + duration) % DAY_MINUTES;
}

export function durationParts(duration: number): { hours: number; minutes: number } {
	return { hours: Math.floor(duration / 60), minutes: duration % 60 };
}

export function openDays(days: readonly string[]): IsoDate[] {
	return days.filter((day) => isIsoDate(day)).sort();
}

/** What an emailed cancellation link points to. */
export interface CancelPreview {
	day: IsoDate | undefined;
	start: number | undefined;
	end: number | undefined;
	library: string | null;
	/** The seat's short name, as the other pages show it. */
	seat: string | null;
	outlet: boolean;
	cancellable: boolean;
}

/** Affluences writes these as "28/10/2026" and "16h00". */
export function toCancelPreview(preview: AffluencesReservationToCancel): CancelPreview {
	const day = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(preview.reservationDay?.trim() ?? '');
	const iso = day ? `${day[3]}-${day[2]}-${day[1]}` : undefined;
	const clock = (text: string | null) => parseClock(text?.trim().replace(/h/i, ':'));
	const seat = blankToNull(preview.resourceName);
	return {
		day: isIsoDate(iso) ? iso : undefined,
		start: clock(preview.startTime),
		end: clock(preview.endTime),
		library: blankToNull(preview.siteName),
		seat: seat && shortSeatName(seat),
		outlet: seat !== null && hasOutlet(seat),
		cancellable: preview.cancellable
	};
}

/** The token of a cancellation link Affluences emails (`…/reservation/cancel?reservationToken=…`). */
export function cancelTokenOf(link: string): string | undefined {
	try {
		return new URL(link.trim()).searchParams.get('reservationToken')?.trim() || undefined;
	} catch {
		return undefined;
	}
}
