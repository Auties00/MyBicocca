import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';
import IconCalendarBlank from '~icons/ph/calendar-blank';
import IconCalendarDots from '~icons/ph/calendar-dots';
import { displayTitle, kindLabel, shortDate } from '#lib/features/calendar/format.ts';
import { locationLine, type LessonEvent } from '#lib/features/calendar/model.ts';
import { withQuery } from '#lib/features/elearning/links.ts';
import { formatDate, formatTime } from '#lib/format/dates.ts';
import { toIsoDate, toJsDate, type IsoDate } from '#lib/format/days.ts';
import { capitalize } from '#lib/format/names.ts';
import type { WallClock } from '#lib/format/rome.ts';
import { m } from '#lib/paraglide/messages.js';
import type { PaletteItem } from '#lib/shell/palette.svelte.ts';
import { category } from '../categories.ts';
import { parseDateQuery } from '../dates.ts';
import { UNRECORDED } from '../history.ts';
import { searchDoc, type SearchDoc } from '../rank.ts';

/** As on Android: the next hundred events, before a course's lessons fold into one. */
const UPCOMING = 100;

function calendarHref(date: IsoDate, event?: string): ResolvedPathname {
	return withQuery(resolve('calendar'), { date, event });
}

/**
 * The lessons still to come, each course once: its next lesson, titled as the calendar titles it
 * and found by its short label, subject code and teachers. Lessons of the turni the student
 * doesn't follow are left out, as the calendar hides them.
 */
export function lessonDocs(
	lessons: readonly LessonEvent[],
	now: WallClock,
	hidden: (lesson: LessonEvent) => boolean
): SearchDoc[] {
	const upcoming = lessons
		.filter(
			(lesson) =>
				!lesson.cancelled &&
				(lesson.date > now.date || (lesson.date === now.date && lesson.end > now.minutes)) &&
				!hidden(lesson)
		)
		.sort((a, b) => (a.date < b.date ? -1 : a.date > b.date ? 1 : a.start - b.start))
		.slice(0, UPCOMING);
	const seen = new Set<string>();
	const docs: SearchDoc[] = [];
	for (const lesson of upcoming) {
		const title = displayTitle(lesson);
		const course = lesson.activityCode ?? lesson.subjectCode ?? title.toLocaleLowerCase();
		if (seen.has(course)) continue;
		seen.add(course);
		const when = `${shortDate(lesson.date)}, ${formatTime(toJsDate(lesson.date, lesson.start))}`;
		docs.push(
			searchDoc(
				{
					id: `event:${lesson.id}`,
					title,
					subtitle: [when, lesson.group ?? kindLabel(lesson), locationLine(lesson)]
						.filter((part) => part !== '')
						.join(' · '),
					icon: IconCalendarBlank,
					href: calendarHref(lesson.date, lesson.id),
					...category('calendar')
				},
				[lesson.shortLabel, lesson.subjectCode, ...lesson.teachers]
			)
		);
	}
	return docs;
}

const RELATIVE: readonly (() => string)[] = [
	m.search_day_today,
	m.search_day_tomorrow,
	m.search_day_after_tomorrow
];

/** "Lunedì 2 novembre", with the year only when it isn't this one. */
function dayName(date: Date, today: Date): string {
	const style = date.getFullYear() === today.getFullYear() ? 'weekdayDayMonthLong' : 'full';
	return capitalize(formatDate(date, style));
}

/** "domani", "lunedì", "22/6": the day, to open in the calendar, ranked 0.95 as on Android. */
export function dayRow(query: string, today: Date): PaletteItem | undefined {
	const day = parseDateQuery(query, today);
	if (day === null) return undefined;
	const named = dayName(day.date, today);
	const iso = toIsoDate(day.date);
	return {
		id: `${UNRECORDED}day:${iso}`,
		title: day.offset === null ? named : RELATIVE[day.offset](),
		subtitle:
			day.offset === null
				? m.search_open_in_calendar()
				: `${named} · ${m.search_open_in_calendar()}`,
		icon: IconCalendarDots,
		href: calendarHref(iso),
		score: 0.95,
		...category('calendar')
	};
}
