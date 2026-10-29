import type {
	EasyStaffPlanningArea,
	EasyStaffPlanningDaySchedule,
	EasyStaffPlanningMonthSchedule
} from '@mybicocca/data-api/easystaff';
import { addDays, addMonths, monthOf, type IsoDate, type YearMonth } from '#lib/format/days.ts';
import { romeDay } from '#lib/format/rome.ts';

/** Where and how long a service is booked: the portal offers each service at one or more areas. */
export interface Offering {
	/** The service as the area lists it, which the booking keeps. */
	serviceName: string;
	serviceGroup: string | null;
	areaId: number;
	areaName: string;
	areaAddress: string | null;
	/** The area's own duration, used for every availability call and for the booking. */
	durationSeconds: number;
	virtual: boolean;
	description: string | null;
	/** How many days ahead can be booked; 60 when the portal doesn't say. */
	horizonDays: number;
}

const DEFAULT_HORIZON_DAYS = 60;

/** The offerings of `serviceId`: hidden areas are left out. */
export function offeringsOf(
	areas: readonly EasyStaffPlanningArea[],
	serviceId: number
): Offering[] {
	return areas.flatMap((area) => {
		if (area.publicHidden) return [];
		const service = area.services.find((item) => item.id === serviceId);
		if (service === undefined) return [];
		return [
			{
				serviceName: service.name.trim(),
				serviceGroup: service.group,
				areaId: area.id,
				areaName: area.name,
				areaAddress: area.address,
				durationSeconds: service.durationSeconds,
				virtual: service.virtual,
				description: service.description,
				horizonDays:
					service.maxRangeDays !== null && service.maxRangeDays > 0
						? service.maxRangeDays
						: DEFAULT_HORIZON_DAYS
			}
		];
	});
}

/** The days of a month schedule with at least one open window, in order. */
export function availableDays(schedule: EasyStaffPlanningMonthSchedule): IsoDate[] {
	const days: IsoDate[] = [];
	for (const [date, windows] of schedule.days) if (windows.length > 0) days.push(date.toString());
	return days.sort();
}

/** A bookable time of a day, in minutes from midnight, Europe/Rome. */
export interface Slot {
	start: number;
	end: number;
	available: boolean;
}

/** The slots of the only day a day schedule holds, by start time; taken ones are kept. */
export function daySlots(schedule: EasyStaffPlanningDaySchedule): Slot[] {
	const [first] = schedule.days.values();
	if (first === undefined) return [];
	const slots: Slot[] = [];
	for (const [range, availability] of first) {
		slots.push({
			start: range.start.hour * 60 + range.start.minute,
			end: range.end.hour * 60 + range.end.minute,
			available: availability.availableCount > 0
		});
	}
	return slots.sort((a, b) => a.start - b.start);
}

/** The afternoon starts at 13:00, as the office's own grid splits it. */
const AFTERNOON = 13 * 60;

export function splitByHalfDay(slots: readonly Slot[]): { morning: Slot[]; afternoon: Slot[] } {
	return {
		morning: slots.filter((slot) => slot.start < AFTERNOON),
		afternoon: slots.filter((slot) => slot.start >= AFTERNOON)
	};
}

/** The months a booking can fall in: from today's, where the university is, to the horizon's. */
export function bookingMonths(
	now: number,
	horizonDays: number
): { first: YearMonth; last: YearMonth } {
	const today = romeDay(now);
	return { first: monthOf(today), last: monthOf(addDays(today, horizonDays)) };
}

export interface FirstMonth {
	month: YearMonth;
	days: IsoDate[];
}

/**
 * The first month from `first` to `last` with a bookable day, asking one month at a time; when
 * none has any, `last` with no days. A failure stops the scan.
 */
export async function firstMonthWithDays(
	first: YearMonth,
	last: YearMonth,
	load: (month: YearMonth) => Promise<IsoDate[]>
): Promise<FirstMonth> {
	let month = first;
	for (;;) {
		const days = await load(month);
		if (days.length > 0 || month >= last) return { month, days };
		month = addMonths(month, 1);
	}
}
