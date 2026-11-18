import type { DataError } from '#lib/data/errors.ts';
import { untrack } from 'svelte';
import { query, type Query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { reservationsQuery, syncReservations } from '#lib/features/appointments/reservations.ts';
import { courseDeadlinesQuery, enrolledCoursesQuery } from '#lib/features/elearning/queries.ts';
import { bookingsQuery } from '#lib/features/exams/queries.ts';
import { libraryLinks } from '#lib/features/library/account.svelte.ts';
import { myReservationsQuery } from '#lib/features/library/queries.ts';
import { recordBookRowsQuery } from '#lib/features/profile/queries.ts';
import { addMonths, type IsoDate, type YearMonth } from '#lib/format/days.ts';
import {
	activityCodesByName,
	appointmentFromStore,
	deadlineFromCourseDeadline,
	examFromBooking,
	libraryFromReservation
} from './mappers.ts';
import {
	groupByDay,
	mergeEvents,
	type AppointmentEvent,
	type CalendarEvent,
	type DeadlineEvent,
	type ExamEvent,
	type LessonEvent,
	type LibraryEvent
} from './model.ts';
import { lessonsQuery, staleTimetable } from './queries.ts';
import { storedEvents } from './stored.ts';
import { TurnoChoices } from './turni.svelte.ts';

/**
 * Everything the calendar shows around `month`: the lessons of the month and of the
 * `neighbours`, by offset (by default both, so views crossing a month edge render whole and paging
 * is instant), booked exams, assignment deadlines, the seats of the library account linked on
 * this browser and the desk appointments booked here. The neighbours load once the month has, so
 * EasyStaff gets the month that is on screen first. Create it while a component initializes.
 */
export class CalendarData {
	readonly #lessons: Query<LessonEvent[]>[];
	readonly #bookings = query(() => session.current && bookingsQuery(session.current));
	readonly #records = query(() => session.current && recordBookRowsQuery(session.current));
	readonly #deadlines = query(() => session.current && courseDeadlinesQuery(session.current));
	readonly #courses = query(() => session.current && enrolledCoursesQuery(session.current));
	readonly #library = query(() => {
		const scope = session.current;
		const link = scope && libraryLinks.of(scope.account);
		return scope && link && myReservationsQuery(scope, link);
	});
	readonly #appointments = query(() => session.current && reservationsQuery(session.current));
	/** The best-effort sources had their time: the calendar shows without them. */
	#late = $state(false);
	/** What storage held for the first month shown, until its queries settle. */
	#stored = $state.raw<{ month: YearMonth; events: CalendarEvent[] }>();

	readonly exams: ExamEvent[];
	readonly deadlines: DeadlineEvent[];
	readonly library: LibraryEvent[] = $derived(
		(this.#library.data ?? []).flatMap((reservation) => libraryFromReservation(reservation) ?? [])
	);
	readonly appointments: AppointmentEvent[] = $derived(
		(this.#appointments.data ?? []).map(appointmentFromStore)
	);
	/**
	 * Whether this browser has a library account linked, whose seats the calendar shows; undefined
	 * while the link is read from storage.
	 */
	readonly libraryLinked: boolean | undefined = $derived.by(() => {
		const link = session.current && libraryLinks.of(session.current.account);
		return link && link.auth !== undefined;
	});
	/** The turno the student follows in each course split into turni; per career. */
	readonly turni: TurnoChoices | undefined;
	/** Every loaded event, sorted by day and time, the other turni's lessons included. */
	readonly all: CalendarEvent[];
	/** What the calendar shows: every loaded event but the lessons of turni not followed. */
	readonly events: CalendarEvent[];
	readonly byDay: Map<IsoDate, CalendarEvent[]>;
	/** Whether the month's lessons loaded once. */
	readonly hydrated: boolean;
	/**
	 * Storage was read for the first month shown: render from then on, so the grid takes the
	 * shape of the cached events (an early lesson, a Saturday seat) before it paints.
	 */
	readonly primed: boolean = $derived(this.#stored !== undefined);
	/**
	 * Every source answered once, or failed; the library and the desk get a moment more, not a
	 * veto. Until then the calendar shows its skeleton, so it lays out once instead of growing as
	 * each source arrives.
	 */
	readonly settled: boolean;
	/** The month never loaded and its last try failed: nothing to show but the error. */
	readonly failed: boolean;
	readonly refreshing: boolean;
	/** The first failure of a source the calendar needs; the library and the desk are best effort. */
	readonly error: DataError | undefined;
	/** When the month on screen was loaded. */
	readonly updatedAt: number | undefined;

	constructor(month: () => YearMonth, neighbours: readonly number[] = [-1, 1]) {
		const focus = query(() => {
			const scope = session.current;
			return scope && lessonsQuery(scope, month());
		});
		const neighbour = (offset: number) =>
			query(() => {
				const scope = session.current;
				return scope && focus.data !== undefined && lessonsQuery(scope, addMonths(month(), offset));
			});
		this.#lessons = [focus, ...neighbours.map(neighbour)];

		this.exams = $derived.by(() => {
			const codes = activityCodesByName(this.#records.data ?? []);
			return mergeEvents(
				(this.#bookings.data ?? []).flatMap((booking) => examFromBooking(booking, codes) ?? [])
			);
		});
		this.deadlines = $derived(
			(this.#deadlines.data ?? []).flatMap((deadline) => deadlineFromCourseDeadline(deadline) ?? [])
		);
		this.turni = $derived(session.current && new TurnoChoices(session.current.career.id));
		this.hydrated = $derived(focus.data !== undefined);
		const answered = (source: Query<unknown>) => source.status !== 'pending';
		const required = $derived(
			answered(focus) &&
				(focus.data === undefined || this.#lessons.every(answered)) &&
				answered(this.#bookings) &&
				answered(this.#deadlines)
		);
		/* The library link is read from storage first: unknown is not the same as unlinked. */
		const link = $derived(session.current && libraryLinks.of(session.current.account));
		const optional = $derived(
			answered(this.#appointments) &&
				link !== undefined &&
				(link.auth === undefined || answered(this.#library))
		);
		this.settled = $derived(required && (optional || this.#late));
		this.failed = $derived(!this.hydrated && focus.status === 'error');
		const loaded = $derived(
			mergeEvents<CalendarEvent>(
				...this.#lessons.map((lessons) => lessons.data ?? []),
				this.exams,
				this.deadlines,
				this.library,
				this.appointments
			)
		);
		this.all = $derived(
			!this.settled && !this.failed && this.#stored?.month === month()
				? this.#stored.events
				: loaded
		);
		this.events = $derived(this.all.filter((event) => !this.turni?.hides(event)));
		this.byDay = $derived(groupByDay(this.events));
		$effect(() => {
			this.#late = false;
			if (!required || optional) return;
			const timer = setTimeout(() => (this.#late = true), 1500);
			return () => clearTimeout(timer);
		});
		this.refreshing = $derived(
			focus.refreshing || this.#bookings.refreshing || this.#deadlines.refreshing
		);
		this.error = $derived(focus.error ?? this.#bookings.error ?? this.#deadlines.error);
		this.updatedAt = $derived(focus.updatedAt);

		$effect(() => {
			const scope = session.current;
			if (scope) void syncReservations(scope);
		});

		const [scope, first] = untrack(() => [session.current, month()] as const);
		if (scope === undefined) this.#stored = { month: first, events: [] };
		else
			void storedEvents(scope, [
				first,
				...neighbours.map((offset) => addMonths(first, offset))
			]).then((events) => (this.#stored = { month: first, events }));
	}

	/** The e-learning courses, for "Open course"; undefined until loaded. */
	get courses() {
		return this.#courses.data;
	}

	/** Asks every source again, the timetable included, as pull-to-refresh does on Android. */
	refresh = async (): Promise<void> => {
		staleTimetable();
		if (session.current) void syncReservations(session.current);
		await Promise.all([
			this.#lessons[0].refresh(),
			this.#bookings.refresh(),
			this.#deadlines.refresh(),
			this.#library.refresh(),
			this.#appointments.refresh()
		]);
	};

	find(id: string): CalendarEvent | undefined {
		return this.events.find((event) => event.id === id);
	}
}
