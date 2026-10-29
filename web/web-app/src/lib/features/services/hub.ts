import { formatTime } from '#lib/format/dates.ts';
import { m } from '#lib/paraglide/messages.js';
import { serviceCatalog, type ServiceEntry, type ServiceGroup } from '#lib/shell/catalog.ts';
import type { Tone } from '#lib/ui/types.ts';
import type { ServiceStatus } from './deadlines.ts';
import type { DeadlineStream, MergeStatus } from './streams.ts';

export const serviceGroups: readonly ServiceGroup[] = ['teaching', 'bookings', 'documents', 'fees'];

export const groupCaptions: Record<ServiceGroup, () => string> = {
	teaching: m.services_group_teaching_caption,
	bookings: m.services_group_bookings_caption,
	documents: m.services_group_documents_caption,
	fees: m.services_group_fees_caption
};

export function servicesOf(group: ServiceGroup): ServiceEntry[] {
	return serviceCatalog.filter((entry) => entry.group === group);
}

export function serviceEntry(id: string): ServiceEntry {
	const entry = serviceCatalog.find((item) => item.id === id);
	if (entry === undefined) throw new Error(`The service catalog lists no ${id} page`);
	return entry;
}

export interface ServiceBadge {
	label: string;
	tone: Tone;
}

/** What the badges read: the deadlines' state, and what other features flag. */
export interface BadgeInput {
	status: MergeStatus['status'];
	services: ServiceStatus | undefined;
	missing: readonly DeadlineStream[];
	/** Questionnaires still to fill in; undefined while unknown. */
	questionnaires?: number;
	/** Today's library seat, unless it has ended. */
	seat?: { startsAt: number; start: Date };
	/** When the badges are read, for a seat already taken. */
	now?: number;
}

/** The streams each flagged service is checked against. */
const serviceStreams: Record<string, readonly DeadlineStream[]> = {
	results: ['results'],
	exams: ['exams', 'bookings'],
	fees: ['fees']
};

/**
 * What a hub entry flags: only states the deadlines' data already tells, once it all arrived. A
 * service whose source failed says so rather than nothing, which would read as all clear.
 */
export function serviceBadge(id: string, input: BadgeInput): ServiceBadge | undefined {
	if (id === 'questionnaires')
		return input.questionnaires
			? {
					label: m.services_badge_questionnaires({ count: input.questionnaires }),
					tone: 'accent'
				}
			: undefined;
	if (id === 'library' && input.seat !== undefined)
		return input.seat.startsAt <= (input.now ?? Date.now())
			? { label: m.services_badge_seat_now(), tone: 'success' }
			: {
					label: m.services_badge_seat_today({ time: formatTime(input.seat.start) }),
					tone: 'neutral'
				};
	const streams = serviceStreams[id];
	if (streams === undefined || input.status === 'pending') return undefined;
	if (streams.some((stream) => input.missing.includes(stream)))
		return { label: m.services_badge_unverified(), tone: 'neutral' };
	const status = input.services;
	if (status === undefined) return undefined;
	switch (id) {
		case 'results':
			return status.pendingResults > 0
				? { label: m.services_badge_results({ count: status.pendingResults }), tone: 'accent' }
				: undefined;
		case 'fees':
			if (status.overdueFees > 0)
				return {
					label: m.services_badge_fees_overdue({ count: status.overdueFees }),
					tone: 'danger'
				};
			return status.owedFees > 0
				? { label: m.services_badge_fees_due({ count: status.owedFees }), tone: 'warning' }
				: undefined;
		case 'exams':
			if (status.closingBookings > 0)
				return { label: m.services_badge_exams_closing(), tone: 'warning' };
			return status.bookedExams > 0
				? { label: m.services_badge_exams_booked({ count: status.bookedExams }), tone: 'neutral' }
				: undefined;
		default:
			return undefined;
	}
}
