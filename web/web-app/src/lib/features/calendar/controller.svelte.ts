import { onDestroy } from 'svelte';
import { monthOf, weekDays, type IsoDate } from '#lib/format/days.ts';
import { m } from '#lib/paraglide/messages.js';
import { isTypingTarget } from '#lib/ui/platform.ts';
import { toast } from '#lib/ui/toast.svelte.ts';
import { clock } from './clock.svelte.ts';
import { editionsFor } from './courses.ts';
import { CalendarData } from './data.svelte.ts';
import { displayTitle, fullDate, monthYear, weekTitle } from './format.ts';
import { clampZoom, timelineWindow } from './layout.ts';
import { nextBySource, nextEvent, type CalendarEvent } from './model.ts';
import { CalendarNav, type CalendarView } from './nav.svelte.ts';
import { periodDays, showsToday, stepPeriod } from './period.ts';
import { sizeClass } from '#lib/ui/media.ts';
import { setTurnoPicker, type TurnoChoice } from './turni.svelte.ts';
import { counterpart, splitCourses } from './turni.ts';

const ZOOM_KEY = 'mybicocca:calendar-zoom';

/** The density is kept for the visit, as Android keeps it until the app restarts. */
function readZoom(): number {
	try {
		const saved = Number(sessionStorage.getItem(ZOOM_KEY));
		return saved ? clampZoom(saved) : 1;
	} catch {
		return 1;
	}
}

/**
 * The calendar's state and behaviour, shared by the phone and the wide layouts: the place in the
 * URL, the data, the open event and where it shows, focus as details open and close, the turno
 * choice, keyboard shortcuts and zoom. Create it while a component initializes.
 */
export class CalendarController {
	readonly nav = new CalendarNav();
	readonly data = new CalendarData(() => monthOf(this.nav.date));
	/** Courses in turni, over everything loaded (hidden turni included), so the picker offers all. */
	readonly split = $derived(splitCourses(this.data.all));

	readonly now = $derived(clock.now);
	readonly today = $derived(this.now.date);
	readonly view = $derived(this.nav.view);
	readonly date = $derived(this.nav.date);
	readonly size = $derived(sizeClass.current);
	readonly compact = $derived(this.size === 'compact');
	readonly days = $derived(this.view === 'day' ? [this.date] : weekDays(this.date));
	readonly period = $derived(periodDays(this.view, this.date));
	readonly span = $derived(timelineWindow(this.data.events));
	readonly loading = $derived(!this.data.settled && !this.data.failed);
	readonly todayOnScreen = $derived(showsToday(this.view, this.date, this.today));

	/* Where an open event shows: a sheet or dialog, a popover on the card, or the docked pane. */
	readonly docked = $derived(
		this.size === 'large' || (this.size === 'expanded' && this.view === 'month')
	);
	readonly anchored = $derived(this.size === 'expanded' && this.view !== 'month');
	readonly modal = $derived(this.size === 'compact' || this.size === 'medium');

	readonly selected = $derived(this.nav.eventId ? this.data.find(this.nav.eventId) : undefined);
	/** An event the URL names but the calendar doesn't hold (any more): say so, don't vanish. */
	readonly missing = $derived(
		this.nav.eventId !== null && this.data.hydrated && this.selected === undefined
	);

	readonly title = $derived(
		this.view === 'day'
			? fullDate(this.date)
			: this.view === 'week'
				? weekTitle(this.date)
				: monthYear(this.date)
	);
	/** A week across two months abbreviates them, so the toolbar keeps to one row on a laptop. */
	readonly barTitle = $derived(this.view === 'week' ? weekTitle(this.date, 'short') : this.title);
	readonly periodEvents = $derived(this.period.flatMap((day) => this.data.byDay.get(day) ?? []));
	readonly dayEvents = $derived(this.data.byDay.get(this.date) ?? []);
	/** The first event after the period on screen, which an empty period offers to jump to. */
	readonly upcoming = $derived(
		nextEvent(this.data.events, this.period[this.period.length - 1], 24 * 60)
	);
	/** The next exam and deadline, under the day's agenda in the docked pane. */
	readonly comingUp = $derived(
		nextBySource(this.data.events, ['exam', 'deadline'], this.today, this.now.minutes).filter(
			(event) => event.date !== this.date
		)
	);

	/** The first course on screen in several turni that the student hasn't been asked about. */
	readonly prompt = $derived.by(() => {
		const turni = this.data.turni;
		if (this.view === 'month' || !turni) return undefined;
		const onScreen = splitCourses(
			this.data.all.filter((event) => this.period.includes(event.date))
		);
		return [...onScreen.values()].find((course) => turni.of(course.activityCode) === undefined);
	});

	/** How many e-learning editions the open event's course has. */
	readonly editions = $derived(
		this.selected && this.data.courses && this.selected.activityCode
			? editionsFor(this.data.courses, this.selected.activityCode).length
			: 0
	);

	zoom = $state(readZoom());
	/** The phone's date picker, opened by tapping the active segment again. */
	picking = $state(false);
	/** The page the phone's event sheet shows: the event, or its course editions. */
	modalPage = $state<'event' | 'courses'>('event');
	/** The timeline on screen, which zooms around its middle. */
	timeline = $state<{ zoomBy(factor: number): void }>();
	/** The calendar's element, to find event cards in. */
	root = $state<HTMLElement>();
	/** The docked pane, which takes focus when it shows an event opened from the keyboard. */
	pane = $state<HTMLElement>();

	/** What takes focus once the open event closes; set only when focus was in the details. */
	#focusAfterClose: (() => HTMLElement | null | undefined) | null = null;
	/** The docked pane takes focus once it shows the event just opened. */
	#focusPane = false;

	constructor() {
		setTurnoPicker({
			available: (code) => (code === null ? [] : (this.split.get(code)?.turni ?? [])),
			chosen: (code) => this.data.turni?.of(code),
			choose: (code, choice) => this.chooseTurno(code, choice)
		});

		$effect(() => {
			if (this.nav.eventId !== null || !this.#focusAfterClose) return;
			const target = this.#focusAfterClose;
			this.#focusAfterClose = null;
			target()?.focus({ preventScroll: true });
		});

		$effect(() => {
			if (!this.nav.eventId || !this.#focusPane || !this.pane) return;
			this.#focusPane = false;
			this.pane.querySelector<HTMLElement>('h2')?.focus({ preventScroll: true });
		});

		/* An event the URL names but the calendar doesn't hold has no card to anchor a popover. */
		$effect(() => {
			if (this.anchored && this.missing) this.close();
		});

		onDestroy(this.remember);
	}

	/** Kept when the page goes, not on every frame of a pinch. */
	remember = (): void => {
		try {
			sessionStorage.setItem(ZOOM_KEY, String(this.zoom));
		} catch {
			/* Not kept in private mode. */
		}
	};

	zoomBy = (factor: number): void => {
		if (this.timeline) this.timeline.zoomBy(factor);
		else this.zoom = clampZoom(this.zoom * factor);
	};

	cardOf(id: string): HTMLElement | null | undefined {
		const selector = `[data-event="${CSS.escape(id)}"]`;
		return (
			this.root?.querySelector<HTMLElement>(`.main ${selector}`) ??
			this.root?.querySelector<HTMLElement>(selector)
		);
	}

	open = (event: CalendarEvent): void => {
		this.modalPage = 'event';
		this.#focusPane = this.docked;
		void this.nav.go({ event: event.id });
	};

	/** Opens an event from the month grid, which also selects its day. */
	openOnDay = (event: CalendarEvent): void => {
		this.#focusPane = this.docked;
		void this.nav.go({ date: event.date, event: event.id });
	};

	/** Closes the open event; `refocus` returns focus to its card. */
	close = (refocus = false): void => {
		const id = this.nav.eventId;
		if (refocus && id) this.#focusAfterClose = () => this.cardOf(id);
		void this.nav.go({ event: null });
	};

	/**
	 * Records the turno followed in a course. When that hides the open lesson, the details move to
	 * the chosen turno's lesson in the same slot, or close with focus on the nearest card left.
	 */
	chooseTurno = (code: string, choice: TurnoChoice): void => {
		const turni = this.data.turni;
		if (!turni) return;
		const previous = turni.of(code);
		if (previous === choice) return;
		const shown = this.selected;
		turni.choose(code, choice);
		if (shown?.source === 'lesson' && turni.hides(shown)) {
			const swap =
				typeof choice === 'number' ? counterpart(shown, choice, this.data.events) : undefined;
			if (swap) {
				void this.nav.go({ event: swap.id });
			} else {
				this.#focusAfterClose = () => this.#nearestCard(shown);
				void this.nav.go({ event: null });
			}
		}
		if (typeof choice === 'number') {
			const course = this.split.get(code);
			toast.show({
				message: m.calendar_turno_only({
					turno: `T${choice}`,
					course: course ? displayTitle(course.lesson) : code
				}),
				undo: () => turni.choose(code, previous)
			});
		}
	};

	/** The card of the event closest in time to `event` on its day, else the period's title. */
	#nearestCard(event: CalendarEvent): HTMLElement | null | undefined {
		const near = (this.data.byDay.get(event.date) ?? []).reduce<CalendarEvent | undefined>(
			(best, other) =>
				!best || Math.abs(other.start - event.start) < Math.abs(best.start - event.start)
					? other
					: best,
			undefined
		);
		const card = near && this.cardOf(near.id);
		return card ?? this.root?.querySelector<HTMLElement>('.bar .title, .segments input:checked');
	}

	step = (delta: -1 | 1): void => {
		void this.nav.go({ date: stepPeriod(this.view, this.date, delta, this.today), event: null });
	};

	setView = (next: CalendarView): void => {
		void this.nav.go({ view: next, event: null });
	};

	goTo = (day: IsoDate, next?: CalendarView): void => {
		void this.nav.go({ date: day, view: next, event: null });
	};

	/** Arrows and j/k page, t goes to today, d/w/m switch the view, +/- zoom. */
	keydown = (event: KeyboardEvent): void => {
		if (event.defaultPrevented || event.ctrlKey || event.metaKey || event.altKey) return;
		if (isTypingTarget(event.target) || document.querySelector('dialog:modal')) return;
		const inGrid =
			event.target instanceof Element && event.target.closest('[role="grid"], [role="radiogroup"]');
		const key = event.key;
		const zooms = this.view !== 'month';
		let handled = true;
		if (key === 't' || key === 'T') this.goTo(this.today);
		else if (key === 'd' || key === 'D') this.setView('day');
		else if (key === 'w' || key === 'W') this.setView('week');
		else if (key === 'm' || key === 'M') this.setView('month');
		else if ((key === 'ArrowLeft' && !inGrid) || key === 'k') this.step(-1);
		else if ((key === 'ArrowRight' && !inGrid) || key === 'j') this.step(1);
		else if ((key === '+' || key === '=') && zooms) this.zoomBy(1.2);
		else if (key === '-' && zooms) this.zoomBy(1 / 1.2);
		else if (key === 'Escape' && this.nav.eventId && this.docked) this.close(true);
		else handled = false;
		if (handled) event.preventDefault();
	};
}
