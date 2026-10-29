import type {
	EasyStaffPlanningArea,
	EasyStaffPlanningDaySchedule,
	EasyStaffPlanningFormField,
	EasyStaffPlanningMonthSchedule
} from '@mybicocca/data-api/easystaff';
import { query, type Query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import type { IsoDate, YearMonth } from '#lib/format/days.ts';
import { personName } from '#lib/format/names.ts';
import { formFields, prefill, type FormField, type Identity } from './form.ts';
import {
	areasQuery,
	dayScheduleQuery,
	firstMonthQuery,
	monthScheduleQuery,
	serviceFormQuery
} from './queries.ts';
import {
	availableDays,
	bookingMonths,
	daySlots,
	offeringsOf,
	type FirstMonth,
	type Offering,
	type Slot
} from './slots.ts';

function identity(): Identity {
	const scope = session.current;
	const username = scope?.account.username ?? '';
	return {
		email: username.includes('@') ? username : null,
		fullName: personName(scope?.account.displayName ?? ''),
		fiscalCode: scope?.account.fiscalCode ?? null,
		studentNumber: scope?.career.studentNumber ?? null
	};
}

/**
 * One booking of a desk, from the day to the form: what the portal offers, loaded live, and what
 * the student picked. The month opens on the first with a free day; a day the month no longer
 * offers, or a time the day no longer has free, falls back by itself. Create it while the page
 * initializes.
 */
export class BookingFlow {
	readonly areas: Query<EasyStaffPlanningArea[]>;
	readonly form: Query<EasyStaffPlanningFormField[]>;
	readonly firstMonth: Query<FirstMonth>;
	readonly monthSchedule: Query<EasyStaffPlanningMonthSchedule>;
	readonly daySchedule: Query<EasyStaffPlanningDaySchedule>;

	#areaId = $state<number>();
	#month = $state<YearMonth>();
	#day = $state<IsoDate>();
	#slot = $state<{ date: IsoDate; start: number }>();
	/** What the student typed, over what the account prefilled. */
	edits = $state<Record<string, string>>({});
	/** When the page opened: the booking window doesn't move while it is open. */
	readonly #openedAt = Date.now();

	readonly offerings: Offering[] | undefined = $derived.by(() =>
		this.areas.data ? offeringsOf(this.areas.data, this.serviceId) : undefined
	);
	readonly offering: Offering | undefined = $derived.by(
		() => this.offerings?.find((item) => item.areaId === this.#areaId) ?? this.offerings?.[0]
	);
	readonly window = $derived.by(() =>
		this.offering ? bookingMonths(this.#openedAt, this.offering.horizonDays) : undefined
	);
	readonly month: YearMonth | undefined = $derived.by(
		() => this.#month ?? this.firstMonth.data?.month
	);
	/** Nothing free anywhere within the booking horizon. */
	readonly noAvailability = $derived.by(
		() => this.#month === undefined && this.firstMonth.data?.days.length === 0
	);
	readonly days: IsoDate[] | undefined = $derived.by(() =>
		this.monthSchedule.data ? availableDays(this.monthSchedule.data) : undefined
	);
	readonly day: IsoDate | undefined = $derived.by(() => {
		const days = this.days;
		if (!days || days.length === 0) return undefined;
		return this.#day && days.includes(this.#day) ? this.#day : days[0];
	});
	readonly slots: Slot[] | undefined = $derived.by(() =>
		this.daySchedule.data ? daySlots(this.daySchedule.data) : undefined
	);
	readonly slot: Slot | undefined = $derived.by(() =>
		this.#slot?.date === this.day
			? this.slots?.find((item) => item.available && item.start === this.#slot?.start)
			: undefined
	);
	/** The time picked on this day was taken meanwhile: the slots reloaded without it. */
	readonly slotGone = $derived.by(
		() =>
			this.#slot !== undefined &&
			this.#slot.date === this.day &&
			this.slots !== undefined &&
			this.slot === undefined
	);
	readonly fields: FormField[] | undefined = $derived.by(() =>
		this.form.data ? formFields(this.form.data) : undefined
	);
	readonly prefilled: Record<string, string> = $derived.by(() =>
		this.fields ? prefill(this.fields, identity()) : {}
	);
	readonly values: Record<string, string> = $derived.by(() => ({
		...this.prefilled,
		...this.edits
	}));
	readonly canGoBack = $derived.by(
		() => this.month !== undefined && this.window !== undefined && this.month > this.window.first
	);
	readonly canGoForward = $derived.by(
		() => this.month !== undefined && this.window !== undefined && this.month < this.window.last
	);

	constructor(readonly serviceId: number) {
		this.areas = query(() => session.current && areasQuery(session.current, serviceId));
		this.form = query(() => session.current && serviceFormQuery(session.current, serviceId));
		this.firstMonth = query(() => {
			const scope = session.current;
			const offering = this.offering;
			const window = this.window;
			return (
				scope &&
				offering &&
				window &&
				firstMonthQuery(scope, serviceId, offering, window.first, window.last)
			);
		});
		this.monthSchedule = query(() => {
			const scope = session.current;
			const offering = this.offering;
			const month = this.month;
			return scope && offering && month
				? monthScheduleQuery(scope, serviceId, offering, month)
				: undefined;
		});
		this.daySchedule = query(() => {
			const scope = session.current;
			const offering = this.offering;
			const day = this.day;
			return scope && offering && day
				? dayScheduleQuery(scope, serviceId, offering, day)
				: undefined;
		});
	}

	selectArea(areaId: number): void {
		this.#areaId = areaId;
		this.#month = undefined;
		this.#day = undefined;
		this.#slot = undefined;
	}

	/** Shows another month, on its first free day. */
	showMonth(month: YearMonth): void {
		if (!this.window || month < this.window.first || month > this.window.last) return;
		this.#month = month;
		this.#day = undefined;
	}

	selectDay(date: IsoDate): void {
		this.#day = date;
	}

	selectSlot(slot: Slot): void {
		if (this.day !== undefined && slot.available)
			this.#slot = { date: this.day, start: slot.start };
	}

	setValue(code: string, value: string): void {
		this.edits = { ...this.edits, [code]: value };
	}

	/** Reloads the setup: the desk's areas and its form. */
	retrySetup(): void {
		void this.areas.refresh();
		void this.form.refresh();
	}
}
