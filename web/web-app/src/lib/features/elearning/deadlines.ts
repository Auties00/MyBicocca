import type {
	MoodleCalendarEvent,
	MoodleCalendarEventsResponse
} from '@mybicocca/data-api/elearning';

/** An assignment or quiz due date of an enrolled course, from Moodle's action events. */
export interface CourseDeadline {
	eventId: number;
	courseId: number;
	kind: 'assign' | 'quiz';
	/** The assignment's or quiz's own id, which their pages take. */
	instanceId: number;
	/** The course module's id. */
	cmId: number;
	title: string;
	/** Epoch milliseconds. */
	dueAt: number;
}

export const ACTION_EVENTS_PAGE = 50;
const MAX_PAGES = 40;

/**
 * Every action event, page by page. Moodle ignores an `aftereventid` it can't load and answers
 * the first page again, so the cursor isn't trusted alone: each page also starts at the
 * previous page's latest time, seen events are skipped, and paging stops at a page that brings
 * nothing new, a short page, or after 40 pages.
 */
export async function collectActionEvents(
	page: (from: number, afterEventId: number | null) => Promise<MoodleCalendarEventsResponse>
): Promise<MoodleCalendarEvent[]> {
	const seen = new Set<number>();
	const events: MoodleCalendarEvent[] = [];
	let from = 0;
	let afterEventId: number | null = null;
	for (let index = 0; index < MAX_PAGES; index++) {
		const response = await page(from, afterEventId);
		const fresh = response.events.filter((event) => !seen.has(event.id));
		for (const event of fresh) seen.add(event.id);
		events.push(...fresh);
		if (fresh.length === 0 || response.events.length < ACTION_EVENTS_PAGE) break;
		afterEventId = response.lastId;
		from = Math.max(...response.events.map((event) => event.timeSort));
	}
	return events;
}

type PendingDeadline = Omit<CourseDeadline, 'instanceId'>;

/**
 * The deadline an event announces; null unless it is an assignment's or a quiz's, of a course.
 * Its title is the activity's name: the event's own ("… è in scadenza") repeats what the due
 * label says.
 */
export function toDeadline(event: MoodleCalendarEvent): PendingDeadline | null {
	const courseId = event.course?.id;
	const kind = event.moduleName?.toLowerCase();
	if (!courseId || event.instance === null || (kind !== 'assign' && kind !== 'quiz')) return null;
	return {
		eventId: event.id,
		courseId,
		kind,
		cmId: event.instance,
		title: deadlineTitle(event.activityName?.trim() || event.name),
		dueAt: (event.timeSort > 0 ? event.timeSort : event.timeStart) * 1000
	};
}

/** The courses whose assignments, or quizzes, must be listed to translate `events`' ids. */
export function coursesToResolve(
	events: readonly MoodleCalendarEvent[],
	kind: CourseDeadline['kind']
): number[] {
	const ids = events.flatMap((event) => {
		const deadline = toDeadline(event);
		return deadline?.kind === kind ? [deadline.courseId] : [];
	});
	return [...new Set(ids)].sort((a, b) => a - b);
}

/**
 * The deadlines of `events` with their activities' ids: the calendar gives the course module's
 * id, the assignment and quiz lists translate it (`cmId → id`). Deadlines that don't translate
 * are dropped, as nothing could open them. Ordered by due date.
 */
export function resolveDeadlines(
	events: readonly MoodleCalendarEvent[],
	assignments: ReadonlyMap<number, number>,
	quizzes: ReadonlyMap<number, number>
): CourseDeadline[] {
	return events
		.flatMap((event) => {
			const deadline = toDeadline(event);
			if (deadline === null) return [];
			const instanceId = (deadline.kind === 'assign' ? assignments : quizzes).get(deadline.cmId);
			return instanceId === undefined ? [] : [{ ...deadline, instanceId }];
		})
		.toSorted((a, b) => a.dueAt - b.dueAt || a.eventId - b.eventId);
}

const DAY_MS = 24 * 60 * 60_000;

/** Deadlines worth showing on a card: due in the future or in the last two weeks. */
export function recentDeadlines(
	deadlines: readonly CourseDeadline[],
	now: number = Date.now()
): CourseDeadline[] {
	return deadlines.filter((deadline) => deadline.dueAt > now - 14 * DAY_MS);
}

/** Drops a "Type · " prefix some event names carry. */
export function deadlineTitle(title: string): string {
	const separator = title.indexOf(' · ');
	return separator < 0 ? title : title.slice(separator + 3);
}

/**
 * How close a deadline is, by calendar days in the local zone: due today, overdue, within three
 * days (urgent), or later.
 */
