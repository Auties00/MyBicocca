/**
 * Demo dates, all from today: the demo has no fake clock, so it is always "now" in the persona's
 * life. Fixtures build `Date`s here and write them with the wire helpers below.
 */

const DAY_MS = 24 * 60 * 60_000;

/** The day the Android demo is frozen at; its dates keep their distance from it. */
const ANDROID_TODAY = new Date(2026, 5, 18);

/** Today at `hour:minute`, local time. */
export function today(hour = 0, minute = 0): Date {
	const now = new Date();
	return new Date(now.getFullYear(), now.getMonth(), now.getDate(), hour, minute);
}

/** `count` days from today (negative for the past), at `hour:minute`. */
export function days(count: number, hour = 0, minute = 0): Date {
	const base = today(hour, minute);
	return new Date(base.getFullYear(), base.getMonth(), base.getDate() + count, hour, minute);
}

/**
 * Where a date of the Android demo lands: as many days from today as it was from the Android
 * demo's frozen day, so the screens show the same "8 days left" and "passed 5 months ago".
 */
export function fromAndroid(year: number, month: number, day: number, hour = 0, minute = 0): Date {
	const offset = Math.round(
		(new Date(year, month - 1, day).getTime() - ANDROID_TODAY.getTime()) / DAY_MS
	);
	return days(offset, hour, minute);
}

const pad = (value: number) => String(value).padStart(2, '0');

/** Esse3's `dd/MM/yyyy`. */
export function esse3Date(date: Date): string {
	return `${pad(date.getDate())}/${pad(date.getMonth() + 1)}/${date.getFullYear()}`;
}

/** Esse3's `dd/MM/yyyy HH:mm:ss`. */
export function esse3DateTime(date: Date): string {
	return `${esse3Date(date)} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

/** `yyyy-MM-dd`, local. */
export function isoDate(date: Date): string {
	return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

/** Unix seconds, as Moodle and Planning send instants. */
export function epochSeconds(date: Date): number {
	return Math.floor(date.getTime() / 1000);
}
