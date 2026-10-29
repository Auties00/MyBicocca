import { LOOK_AHEAD_DAYS, LOOK_BACK_DAYS, type Deadline } from './deadlines.ts';

/** `2026-10-27`, local: a day's key and its `datetime`. */
export function isoDay(date: Date): string {
	const pad = (value: number) => String(value).padStart(2, '0');
	return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

/** The id of a day's first deadline on the timeline, for jumping to it. */
export function dayAnchor(iso: string): string {
	return `deadline-${iso}`;
}

/** Strongest first. */
export const DAY_TONES = ['overdue', 'urgent', 'calm'] as const;

export type DayTone = (typeof DAY_TONES)[number];

export interface CalendarDay {
	date: Date;
	/** `2026-10-27`: the day's key. */
	iso: string;
	/** Within the deadlines' window, a week back to a month ahead; the rest pads the weeks. */
	inWindow: boolean;
	isToday: boolean;
	/** One per deadline that day, in list order. */
	tones: DayTone[];
	/** The strongest of them, which tints the day; null without deadlines. */
	tone: DayTone | null;
}

/** The window's days in Monday-to-Sunday weeks, each day with its deadlines' tones. */
export function calendarWeeks(deadlines: readonly Deadline[], today: Date): CalendarDay[][] {
	const at = (offset: number) =>
		new Date(today.getFullYear(), today.getMonth(), today.getDate() + offset);
	const tones = new Map<string, DayTone[]>();
	for (const item of deadlines) {
		const key = isoDay(item.date);
		const tone: DayTone = item.days < 0 ? 'overdue' : item.urgent ? 'urgent' : 'calm';
		tones.set(key, [...(tones.get(key) ?? []), tone]);
	}
	const lead = (at(-LOOK_BACK_DAYS).getDay() + 6) % 7;
	const trail = (7 - at(LOOK_AHEAD_DAYS).getDay()) % 7;
	const days: CalendarDay[] = [];
	for (let offset = -LOOK_BACK_DAYS - lead; offset <= LOOK_AHEAD_DAYS + trail; offset++) {
		const date = at(offset);
		const iso = isoDay(date);
		const dayTones = tones.get(iso) ?? [];
		days.push({
			date,
			iso,
			inWindow: offset >= -LOOK_BACK_DAYS && offset <= LOOK_AHEAD_DAYS,
			isToday: offset === 0,
			tones: dayTones,
			tone: DAY_TONES.find((tone) => dayTones.includes(tone)) ?? null
		});
	}
	return Array.from({ length: days.length / 7 }, (_, week) => days.slice(week * 7, week * 7 + 7));
}
