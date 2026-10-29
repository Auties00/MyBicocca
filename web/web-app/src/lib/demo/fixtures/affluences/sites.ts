import { days, isoDate } from '../../dates.ts';
import { json, route } from '../../router.ts';
import { DemoLibraries } from './account.ts';

const APP = 'https://api.affluences.com/app/v4';
const ROOT = '61aa6184-f707-4dbd-9acb-45ff4e20c6b7';

type LibraryKey = keyof typeof DemoLibraries;

/** Opening hours in minutes of the day, per weekday (0 = Sunday), as the real libraries keep. */
const WEEK: Record<LibraryKey, ReadonlyArray<readonly [number, number] | null>> = {
	central: [null, ...Array<[number, number]>(5).fill([510, 1320]), [540, 825]],
	medicine: [null, ...Array<[number, number]>(5).fill([525, 1140]), null]
};

/** How full each library tends to be through the day: hour → percent of the busiest. */
const CURVE: ReadonlyArray<readonly [number, number]> = [
	[8, 6],
	[9, 18],
	[10, 38],
	[11, 55],
	[12, 60],
	[13, 50],
	[14, 62],
	[15, 70],
	[16, 74],
	[17, 64],
	[18, 46],
	[19, 30],
	[20, 18],
	[21, 8]
];

const PEAK: Record<LibraryKey, number> = { central: 1, medicine: 0.62 };

const pad = (value: number) => String(value).padStart(2, '0');
const clock = (minutes: number) => `${pad(Math.floor(minutes / 60))}:${pad(minutes % 60)}:00`;

/** The library's opening hours on `date`, or null when it is closed all day. */
export function demoHours(key: LibraryKey, date: Date): { open: number; close: number } | null {
	const hours = WEEK[key][date.getDay()];
	return hours ? { open: hours[0], close: hours[1] } : null;
}

export function libraryKey(id: string): LibraryKey | undefined {
	return (Object.keys(DemoLibraries) as LibraryKey[]).find((key) => DemoLibraries[key].id === id);
}

/** The occupancy at `minutes`, rounded to 5 as the real forecasts are. */
function occupancyAt(key: LibraryKey, minutes: number): number {
	const hour = minutes / 60;
	const index = CURVE.findIndex(([at]) => at > hour);
	const [h0, v0] = CURVE[Math.max(0, index - 1)];
	const [h1, v1] = index < 0 ? CURVE[CURVE.length - 1] : CURVE[index];
	const value = h1 === h0 ? v0 : v0 + ((v1 - v0) * (hour - h0)) / (h1 - h0);
	return Math.max(5, Math.round((value * PEAK[key]) / 5) * 5);
}

const nowMinutes = () => {
	const now = new Date();
	return now.getHours() * 60 + now.getMinutes();
};

function status(key: LibraryKey) {
	const now = new Date();
	const hours = demoHours(key, now);
	const minutes = nowMinutes();
	const open = hours !== null && minutes >= hours.open && minutes < hours.close;
	if (open) {
		return {
			isOpen: true,
			openingText: null,
			openingAt: null,
			isOpeningSoon: false,
			closingText: `Chiude alle ${clock(hours.close).slice(0, 5)}`,
			closingAt: `${isoDate(now)} ${clock(hours.close)}`,
			isClosingSoon: hours.close - minutes <= 30
		};
	}
	const next = [0, 1, 2, 3, 4, 5, 6, 7]
		.map((offset) => days(offset))
		.find((day, offset) => {
			const dayHours = demoHours(key, day);
			return dayHours !== null && (offset > 0 || minutes < dayHours.open);
		});
	const nextHours = next ? demoHours(key, next) : null;
	return {
		isOpen: false,
		openingText: nextHours ? `Apre alle ${clock(nextHours.open).slice(0, 5)}` : null,
		openingAt: next && nextHours ? `${isoDate(next)} ${clock(nextHours.open)}` : null,
		isOpeningSoon: false,
		closingText: null,
		closingAt: null,
		isClosingSoon: false
	};
}

function attendance(key: LibraryKey) {
	return status(key).isOpen
		? {
				flow: null,
				occupancy: occupancyAt(key, nowMinutes()),
				waitingTime: null,
				maxWaitingTime: null
			}
		: null;
}

/**
 * The rest of today in half hours, merged where the occupancy holds, as the real forecasts come:
 * "13:30–14:00 45", "14:30–17:30 60".
 */
function forecasts(key: LibraryKey) {
	const today = new Date();
	const hours = demoHours(key, today);
	if (hours === null) return [];
	const date = isoDate(today);
	const slots: { start: number; end: number; occupancy: number }[] = [];
	for (
		let start = Math.max(hours.open, Math.floor(nowMinutes() / 30) * 30);
		start < hours.close;
		start += 30
	) {
		const occupancy = occupancyAt(key, start);
		const last = slots.at(-1);
		if (last && last.occupancy === occupancy) last.end = Math.min(start + 30, hours.close);
		else slots.push({ start, end: Math.min(start + 30, hours.close), occupancy });
	}
	return slots.map((slot, index) => ({
		hourRange: [`${date} ${clock(slot.start)}`, `${date} ${clock(slot.end)}`],
		flow: null,
		flowEvolution: null,
		occupancy: slot.occupancy,
		occupancyEvolution:
			index === 0 || slot.occupancy === slots[index - 1].occupancy
				? 'STABLE'
				: slot.occupancy > slots[index - 1].occupancy
					? 'INCREASE'
					: 'DECREASE',
		waitingTime: null,
		waitingTimeEvolution: null
	}));
}

const PICTURES: Record<LibraryKey, string> = {
	central: 'https://static.affluences.media/sites-pictures/sites/PR_gUgzUN.jpeg',
	medicine: 'https://static.affluences.media/sites-pictures/sites/ktdmpsvtG.jpeg'
};

const SLUGS: Record<LibraryKey, string> = {
	central: 'biblioteca-centrale-edificio-u6',
	medicine: 'biblioteca-di-scienze-edificio-u2-1'
};

function card(key: LibraryKey) {
	const library = DemoLibraries[key];
	return {
		id: library.id,
		slug: SLUGS[key],
		primaryName: library.name,
		secondaryName: library.secondary,
		pictures: [PICTURES[key]],
		status: { isOpen: status(key).isOpen },
		liveAttendance: attendance(key) ?? { flow: null, occupancy: null, waitingTime: null }
	};
}

const DETAILS: Record<
	LibraryKey,
	{ address: string; latitude: number; longitude: number; phone: string }
> = {
	central: {
		address: "1 Piazza dell'Ateneo Nuovo, 20126 Milano, Italie",
		latitude: 45.5182,
		longitude: 9.21392,
		phone: '+390264486251'
	},
	medicine: {
		address: '48 Via Cadore, 20900 Monza, Italie',
		latitude: 45.6025,
		longitude: 9.25953,
		phone: '+390264488201'
	}
};

const notFound = () =>
	json({ error: { code: 'site_not_found', message: 'Site not found' } }, { status: 404 });

export const routes = [
	route(
		`GET ${APP}/sites/${ROOT}/children`,
		() => json({ data: [card('central'), card('medicine')] }),
		{ empty: () => json({ data: [] }) }
	),
	route(`GET ${APP}/sites/:id/live-data`, (request) => {
		const key = libraryKey(request.params.id);
		if (key === undefined) return notFound();
		const slots = forecasts(key);
		const live = attendance(key);
		return json({
			data: {
				status: status(key),
				notices: [],
				liveAttendance: live,
				todayForecasts: slots,
				forecasts: [
					{
						day: isoDate(new Date()),
						todayOffset: 0,
						forecasts: [
							{ hourRange: null, occupancy: live?.occupancy ?? null, occupancyEvolution: null },
							...slots
						]
					}
				]
			}
		});
	}),
	route(`GET ${APP}/sites/:id/timetables`, (request) => {
		const key = libraryKey(request.params.id);
		if (key === undefined) return notFound();
		const offset = Number(request.url.searchParams.get('weekOffset') ?? 0) || 0;
		const today = new Date();
		const monday = days(-((today.getDay() + 6) % 7) + offset * 7);
		const week = Array.from(
			{ length: 7 },
			(_, index) => new Date(monday.getFullYear(), monday.getMonth(), monday.getDate() + index)
		);
		return json({
			data: {
				startDate: isoDate(week[0]),
				endDate: isoDate(week[6]),
				entries: week.map((day) => {
					const hours = demoHours(key, day);
					const date = isoDate(day);
					return {
						day: date,
						isToday: date === isoDate(today),
						openingHours: hours
							? [
									{
										openingHour: `${date} ${clock(hours.open)}`,
										closingHour: `${date} ${clock(hours.close)}`
									}
								]
							: []
					};
				}),
				information: null
			}
		});
	}),
	route(`GET ${APP}/sites/:id`, (request) => {
		const key = libraryKey(request.params.id);
		if (key === undefined) return notFound();
		const library = DemoLibraries[key];
		const details = DETAILS[key];
		return json({
			data: {
				id: library.id,
				slug: SLUGS[key],
				primaryName: library.name,
				secondaryName: library.secondary,
				lang: 'it',
				timeZone: 'Europe/Rome',
				email: 'biblioteca@unimib.it',
				phone: details.phone,
				websiteUrl: 'https://www.biblio.unimib.it',
				booking: { hasBooking: true, bookingUrl: null, ticketUrl: null },
				publicationStatus: 'PUBLIC',
				address: details.address,
				latitude: details.latitude,
				longitude: details.longitude,
				categories: [
					{ id: 20, name: 'Biblioteca universitaria', namePlural: 'Biblioteche universitarie' }
				],
				pictures: [PICTURES[key]],
				pageLayout: null,
				parent: {
					id: ROOT,
					slug: 'universita-bicocca-biblioteca-di-ateneo',
					primaryName: 'Università Bicocca. Biblioteca di Ateneo'
				}
			}
		});
	})
];
