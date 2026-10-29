import { LocalDate, LocalDateTime } from '@mybicocca/data-api/runtime';
import { formatTime } from './dates.ts';
import { addDays, toJsDate, type IsoDate } from './days.ts';

/*
 * The university keeps Rome's clock: Esse3, EasyStaff, Affluences and the booking portal send times
 * without a zone that mean Europe/Rome. These read and write that clock whatever zone the device is
 * in, so times, "today" and the now-line hold abroad too.
 */

/** A day and the minute of the day on a wall clock. */
export interface WallClock {
	date: IsoDate;
	minutes: number;
}

const ROME = new Intl.DateTimeFormat('en-CA', {
	timeZone: 'Europe/Rome',
	year: 'numeric',
	month: '2-digit',
	day: '2-digit',
	hour: '2-digit',
	minute: '2-digit',
	second: '2-digit',
	hourCycle: 'h23'
});

type Field = 'year' | 'month' | 'day' | 'hour' | 'minute' | 'second';

function romeFields(epochMs: number): Record<Field, string> {
	const fields: Partial<Record<Intl.DateTimeFormatPartTypes, string>> = {};
	for (const part of ROME.formatToParts(epochMs)) fields[part.type] = part.value;
	const {
		year = '1970',
		month = '01',
		day = '01',
		hour = '00',
		minute = '00',
		second = '00'
	} = fields;
	return { year, month, day, hour, minute, second };
}

/** Rome's wall clock at `epochMs`, as a day and the minute of the day. */
export function romeWallClock(epochMs: number): WallClock {
	const { year, month, day, hour, minute } = romeFields(epochMs);
	return { date: `${year}-${month}-${day}`, minutes: Number(hour) * 60 + Number(minute) };
}

/** Rome's wall clock at `epochMs`, as the SDK's date-time without a zone. */
export function romeClock(epochMs: number): LocalDateTime {
	const { year, month, day, hour, minute, second } = romeFields(epochMs);
	const date = new LocalDate(Number(year), Number(month), Number(day));
	return new LocalDateTime(date, Number(hour), Number(minute), Number(second));
}

/** A local `Date` showing Rome's wall clock at `epochMs`, for the `Intl` helpers to format. */
export function romeDate(epochMs: number): Date {
	const { date, minutes } = romeWallClock(epochMs);
	return toJsDate(date, minutes);
}

/** The day it is at the university at `epochMs`. */
export function romeDay(epochMs: number): IsoDate {
	return romeWallClock(epochMs).date;
}

/** The time at `epochMs` as Rome's clock shows it, in the app's locale: "14:30". */
export function romeTime(epochMs: number): string {
	return formatTime(romeDate(epochMs));
}

/** The fields of a time without a zone, read as if they were UTC: the scale offsets apply to. */
function fieldsAsUtc(wall: LocalDateTime | WallClock | Date): number {
	if (wall instanceof Date) {
		return Date.UTC(
			wall.getFullYear(),
			wall.getMonth(),
			wall.getDate(),
			wall.getHours(),
			wall.getMinutes(),
			wall.getSeconds()
		);
	}
	if (wall instanceof LocalDateTime) {
		const { date } = wall;
		return Date.UTC(date.year, date.month - 1, date.day, wall.hour, wall.minute, wall.second);
	}
	const [year, month, day] = wall.date.split('-').map(Number);
	return Date.UTC(year, month - 1, day) + wall.minutes * 60_000;
}

/** How far Rome's clock is ahead of UTC at `epochMs`, in milliseconds. */
function offset(epochMs: number): number {
	const { year, month, day, hour, minute, second } = romeFields(epochMs);
	const shown = Date.UTC(+year, +month - 1, +day, +hour, +minute, +second);
	return shown - Math.floor(epochMs / 1000) * 1000;
}

/**
 * The instant at which Rome's clock reads `wall`: an SDK date-time, a day and minute, or the local
 * fields of a `Date` such as {@link romeDate} returns. The offset is read twice: the first guess can
 * sit on the other side of a daylight-saving change from the answer.
 */
export function romeInstant(wall: LocalDateTime | WallClock | Date): number {
	const fields = fieldsAsUtc(wall);
	return fields - offset(fields - offset(fields));
}

/** `hour`:00 in Rome on the day it is there at `epochMs`; 24 is the end of that day. */
export function romeHour(epochMs: number, hour: number): number {
	const date = addDays(romeDay(epochMs), Math.floor(hour / 24));
	return romeInstant({ date, minutes: (hour % 24) * 60 });
}
