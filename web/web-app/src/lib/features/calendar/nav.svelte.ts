import { goto } from '$app/navigation';
import { resolve } from '$app/paths';
import { page } from '$app/state';
import { isIsoDate, type IsoDate } from '#lib/format/days.ts';
import { clock } from './clock.svelte.ts';
import type { CalendarView } from './period.ts';
import { sizeClass } from '#lib/ui/media.ts';

export type { CalendarView } from './period.ts';

const calendarViews: readonly CalendarView[] = ['day', 'week', 'month'];

function isView(value: string | null): value is CalendarView {
	return value !== null && (calendarViews as readonly string[]).includes(value);
}

/* Phones and wider screens remember their own view: Week on the desk must not force it on the phone. */
function storageKey(): string {
	return `mybicocca:calendar-view:${sizeClass.current === 'compact' ? 'compact' : 'wide'}`;
}

function rememberedView(): CalendarView {
	try {
		const saved = localStorage.getItem(storageKey());
		if (isView(saved)) return saved;
	} catch {
		/* Private mode: the default below. */
	}
	return sizeClass.current === 'compact' ? 'day' : 'week';
}

/** The calendar's query string, in a fixed order: `view=week&date=2026-10-27&event=…`. */
function query(view: string | null, date: string | null, event: string | null): string {
	return [
		['view', view],
		['date', date],
		['event', event]
	]
		.flatMap(([name, value]) => (value ? [`${name}=${encodeURIComponent(value)}`] : []))
		.join('&');
}

/** Marks the history entry an opened event pushed, so closing it goes back instead. */
interface EventState {
	calendarEvent?: boolean;
}

export interface CalendarTarget {
	view?: CalendarView;
	date?: IsoDate;
	/** An event id to open, or null to close the open one. */
	event?: string | null;
}

/**
 * The calendar's place, kept in the URL (`?view=&date=&event=`) so Back, reload and links work:
 * a date or view change replaces the history entry, opening an event pushes one, so Back closes it.
 */
export class CalendarNav {
	readonly view: CalendarView = $derived.by(() => {
		const value = page.url.searchParams.get('view');
		return isView(value) ? value : rememberedView();
	});

	readonly date: IsoDate = $derived.by(() => {
		const value = page.url.searchParams.get('date');
		return isIsoDate(value) ? value : clock.now.date;
	});

	readonly eventId: string | null = $derived(page.url.searchParams.get('event'));

	go(target: CalendarTarget): Promise<void> {
		const current = page.url.searchParams;
		if (target.view) {
			try {
				localStorage.setItem(storageKey(), target.view);
			} catch {
				/* Not remembered in private mode. */
			}
		}
		const event = target.event === undefined ? current.get('event') : target.event;
		const search = query(
			target.view ?? current.get('view'),
			target.date ?? current.get('date'),
			event
		);
		if (search === query(current.get('view'), current.get('date'), current.get('event'))) {
			return Promise.resolve();
		}
		const moving = Boolean(target.view || target.date);
		const pushed = (page.state as EventState).calendarEvent === true && !moving;
		if (target.event === null && pushed) {
			history.back();
			return Promise.resolve();
		}
		const opening = typeof target.event === 'string' && this.eventId === null;
		const state: EventState = event && (opening || pushed) ? { calendarEvent: true } : {};
		return goto(resolve(`calendar?${search}`), {
			replace: !opening,
			reset: false,
			state: state as App.PageState
		});
	}
}
