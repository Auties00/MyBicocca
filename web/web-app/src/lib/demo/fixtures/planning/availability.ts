import { addDays, lastOfMonth, weekdayIndex, type IsoDate } from '#lib/format/days.ts';
import { days, epochSeconds, isoDate } from '../../dates.ts';
import { json, route } from '../../router.ts';
import { PLANNING_API, demoService, type DemoService } from './catalog.ts';
import { isBooked } from './reservations.ts';

const pad = (value: number) => String(value).padStart(2, '0');
const clock = (minutes: number) => `${pad(Math.floor(minutes / 60))}:${pad(minutes % 60)}`;

/** The desk's opening windows on `date`, in minutes from midnight. */
function windows(item: DemoService, date: IsoDate): Array<[number, number]> {
	const tomorrow = isoDate(days(1));
	const weekday = weekdayIndex(date);
	if (item.availability === 'none' || weekday > 4 || date < tomorrow) return [];
	if (date > isoDate(days(item.maxRangeDays))) return [];
	if (item.availability === 'later' && date < isoDate(days(33))) return [];
	const mornings: [number, number] = [9 * 60, 12 * 60];
	const afternoons = item.availability === 'daily' || weekday === 1 || weekday === 3;
	return afternoons ? [mornings, [14 * 60, 15 * 60 + 30]] : [mornings];
}

/** Every other desk's bookings, the same on every visit: about one slot in four is taken. */
function takenByOthers(date: IsoDate, minutes: number): boolean {
	let hash = 0;
	for (const char of `${date}T${minutes}`) hash = (hash * 31 + char.charCodeAt(0)) >>> 0;
	return hash % 4 === 0;
}

/** The second working day from today is fully booked, to show a day with no free time. */
function fullDay(): IsoDate {
	let date = isoDate(days(1));
	for (let found = 0; ; date = addDays(date, 1)) {
		if (weekdayIndex(date) > 4) continue;
		if (++found === 2) return date;
	}
}

function slotsOf(item: DemoService, date: IsoDate) {
	const [year, month, day] = date.split('-').map(Number);
	const full = date === fullDay();
	const slots: Record<string, { disponibili: number; su: number; reserved: boolean }> = {};
	for (const [open, close] of windows(item, date)) {
		for (let start = open; start + item.minutes <= close; start += item.minutes) {
			const at = epochSeconds(new Date(year, month - 1, day, Math.floor(start / 60), start % 60));
			const taken = full || takenByOthers(date, start) || isBooked(item.id, at);
			slots[`${clock(start)}-${clock(start + item.minutes)}`] = {
				disponibili: taken ? 0 : 1,
				su: 1,
				reserved: false
			};
		}
	}
	return slots;
}

function monthSchedule(item: DemoService, month: string) {
	const schedule: Record<string, string[]> = {};
	let first: string | null = null;
	for (let date = `${month}-01`; date <= lastOfMonth(month); date = addDays(date, 1)) {
		const open = windows(item, date);
		schedule[date] = open.map(([from, to]) => `${clock(from)}-${clock(to)}`);
		if (first === null && open.length > 0) first = `${date} ${clock(open[0][0])}`;
	}
	return { prima_disp: first ?? '', schedule };
}

export const routes = [
	route(
		`GET ${PLANNING_API}/entry/:service/schedule/:period/:area/:duration`,
		(request) => {
			const item = demoService(Number(request.params.service));
			const period = request.params.period;
			if (item === undefined || item.area.id !== Number(request.params.area))
				return json({ message: 'not_found' }, { status: 404 });
			if (/^\d{4}-\d{2}$/.test(period)) return json(monthSchedule(item, period));
			return json({ schedule: { [period]: slotsOf(item, period) } });
		},
		{ empty: () => json({ prima_disp: '', schedule: {} }) }
	)
];
