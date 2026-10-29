import type {
	EasyStaffPlanningArea,
	EasyStaffPlanningDaySchedule,
	EasyStaffPlanningFormField,
	EasyStaffPlanningMonthSchedule,
	EasyStaffPlanningService
} from '@mybicocca/data-api/easystaff';
import { LocalDate } from '@mybicocca/data-api/runtime';
import { publicKey } from '#lib/data/keys.ts';
import { cache, policies, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import type { IsoDate, YearMonth } from '#lib/format/days.ts';
import { availableDays, firstMonthWithDays, type FirstMonth, type Offering } from './slots.ts';

/*
 * The portal is the same for everyone and needs no account, so its data is public. Availability
 * is never kept past the visit, and goes stale in half a minute: a grid that old may offer a slot
 * someone else just took, though the portal refuses it anyway.
 */
const AVAILABILITY = policies.memory(0.5);

/** The office directory. Fetching it also hands this browser the portal's guest token. */
export function servicesQuery(scope: SessionScope): QueryOptions<EasyStaffPlanningService[]> {
	return {
		key: publicKey('planning.catalog.getServices'),
		load: () => scope.api.planning.catalog.getServices(),
		/* The portal always has offices: an empty answer is a glitch, never kept as the truth. */
		policy: { ...policies.shared(60), staleWhen: (services) => services.length === 0 }
	};
}

/** The areas offering a service, each with its booking constraints. */
export function areasQuery(
	scope: SessionScope,
	serviceId: number
): QueryOptions<EasyStaffPlanningArea[]> {
	return {
		key: publicKey('planning.catalog.getAreas', serviceId),
		load: () =>
			scope.api.planning.catalog.getAreas({
				filters: { serviceGroup: null, service: { id: serviceId } }
			}),
		policy: policies.memory(10)
	};
}

export function serviceFormQuery(
	scope: SessionScope,
	serviceId: number
): QueryOptions<EasyStaffPlanningFormField[]> {
	return {
		key: publicKey('planning.portals.getServiceForm', serviceId),
		load: () => scope.api.planning.portals.getServiceForm(serviceId),
		policy: policies.memory(10)
	};
}

export function monthScheduleQuery(
	scope: SessionScope,
	serviceId: number,
	offering: Offering,
	month: YearMonth
): QueryOptions<EasyStaffPlanningMonthSchedule> {
	return {
		key: publicKey(
			'planning.availability.getMonthSchedule',
			serviceId,
			month,
			offering.areaId,
			offering.durationSeconds
		),
		load: () =>
			scope.api.planning.availability.getMonthSchedule(
				serviceId,
				month,
				offering.areaId,
				offering.durationSeconds
			),
		policy: AVAILABILITY
	};
}

export function dayScheduleQuery(
	scope: SessionScope,
	serviceId: number,
	offering: Offering,
	date: IsoDate
): QueryOptions<EasyStaffPlanningDaySchedule> {
	return {
		key: publicKey(
			'planning.availability.getDaySchedule',
			serviceId,
			date,
			offering.areaId,
			offering.durationSeconds
		),
		load: () => {
			const day = LocalDate.parse(date);
			if (day === undefined) throw new RangeError(`Not a date: ${date}`);
			return scope.api.planning.availability.getDaySchedule(
				serviceId,
				day,
				offering.areaId,
				offering.durationSeconds
			);
		},
		policy: AVAILABILITY
	};
}

/**
 * The month the picker opens on: the first from `first` to `last` with a bookable day. Each
 * month it looks at is cached under its own key, so the picker shows it without asking again.
 */
export function firstMonthQuery(
	scope: SessionScope,
	serviceId: number,
	offering: Offering,
	first: YearMonth,
	last: YearMonth
): QueryOptions<FirstMonth> {
	return {
		key: publicKey(
			'appointments.firstMonth',
			serviceId,
			offering.areaId,
			offering.durationSeconds,
			first,
			last
		),
		load: () =>
			firstMonthWithDays(first, last, async (month) => {
				const options = monthScheduleQuery(scope, serviceId, offering, month);
				const schedule = await cache.fetch(options.key, options.load, AVAILABILITY);
				return availableDays(schedule);
			}),
		policy: AVAILABILITY
	};
}
