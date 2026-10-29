import { academicYearLabel } from '#lib/data/academicYear.ts';
import { formatDate, formatRelative, formatRelativeDays, formatTime } from '#lib/format/dates.ts';
import { dueLabel } from '#lib/format/due.ts';
import { formatGrade } from '#lib/format/grades.ts';
import { cached, intlLocale } from '#lib/format/locale.ts';
import { formatCurrency } from '#lib/format/numbers.ts';
import { m } from '#lib/paraglide/messages.js';
import { capitalize, italianTitleCase, sentenceCase } from '#lib/format/names.ts';
import type { Deadline, DeadlineSource } from './deadlines.ts';
import type { DeadlineStream } from './streams.ts';

/** Esse3 names in capitals in title case; names someone typed in mixed case stay as written. */
export function displayName(name: string): string {
	return /\p{Ll}/u.test(name) ? name.trim() : italianTitleCase(name);
}

/** A room code such as "U4-08" never breaks at its hyphen. */
export function roomName(room: string): string {
	return room.replaceAll('-', '\u2011');
}

/**
 * "23 nov", which never breaks across lines, and its day: Italian elides the article before 1, 8
 * and 11 ("l’8 nov", "dall’11"), so the messages choose their wording by the day.
 */
export function onDay(date: Date): { date: string; day: number } {
	return { date: formatDate(date, 'dayMonth').replaceAll(' ', '\u00a0'), day: date.getDate() };
}

/** Courses are names, in title case; an invoice's name is a description, in sentence case. */
export function deadlineTitle(item: Deadline): string {
	if (item.title !== null)
		return item.source === 'fee' ? sentenceCase(item.title) : displayName(item.title);
	switch (item.source) {
		case 'result':
			return m.services_fallback_result();
		case 'exam':
			return m.services_fallback_exam();
		case 'booking':
			return m.services_fallback_booking();
		case 'fee':
			return item.academicYear === null
				? m.services_fallback_fee_id({ id: item.invoiceId })
				: m.services_fallback_fee_year({ year: academicYearLabel(item.academicYear) });
	}
}

const kinds: Record<DeadlineSource, () => string> = {
	result: m.services_kind_result,
	fee: m.services_kind_fee,
	exam: m.services_kind_exam,
	booking: m.services_kind_booking
};

/** "Esito", "Tasse": what the deadline is about. */
export function deadlineKind(item: Deadline): string {
	return kinds[item.source]();
}

/** Plural labels of the kind filters. */
export const kindFilters: Record<DeadlineSource, () => string> = {
	result: m.services_filter_result,
	fee: m.services_filter_fee,
	exam: m.services_filter_exam,
	booking: m.services_filter_booking
};

/**
 * How far the deadline is, worded by what it is: an exam happens "tomorrow", a result must be
 * decided "by tomorrow", an unpaid fee is "3 days overdue".
 */
export function deadlineWhen(item: Deadline): string {
	if (item.source === 'exam') {
		return capitalize(formatRelativeDays(item.days));
	}
	if (item.source === 'fee' && item.expired && item.days >= 0) return m.common_overdue();
	return dueLabel(item.days);
}

/** The second line: the grade to decide on, the amount, the time and room, the call. */
export function deadlineDetail(item: Deadline): string {
	switch (item.source) {
		case 'result': {
			const grade =
				item.grade.kind === 'numeric'
					? m.services_detail_grade({ grade: formatGrade(item.grade, { full: true }) })
					: item.grade.kind === 'unknown'
						? null
						: formatGrade(item.grade, { full: true });
			return [grade, m.services_detail_decide()].filter((part) => part !== null).join(' · ');
		}
		case 'fee':
			return [formatCurrency(item.amount), item.installment ?? 'pagoPA'].join(' · ');
		case 'exam':
			return [
				item.time === null ? null : m.services_detail_time({ time: formatTime(item.time) }),
				item.room === null ? null : roomName(item.room)
			]
				.filter((part) => part !== null)
				.join(' · ');
		case 'booking':
			if (item.opensAt !== null) return m.services_detail_booking_opens(onDay(item.opensAt));
			return [
				m.services_detail_booking_closes(),
				item.callDate === null ? null : m.services_detail_booking_call(onDay(item.callDate))
			]
				.filter((part) => part !== null)
				.join(' · ');
	}
}

export interface SummaryInput {
	status: 'pending' | 'success' | 'error';
	/** Every source is in the list, so an empty one means nothing is due. */
	complete: boolean;
	deadlines: readonly Deadline[] | undefined;
	/** The sources the list lacks; they make whatever it says provisional. */
	missing?: readonly DeadlineStream[];
}

/**
 * The hub banner's figures (spec §1.4), each a phrase of its own: "2 urgenti", "Prossima l’1 nov".
 * They never call an overdue date "next", and never say that nothing is due while a source is
 * missing.
 */
export function summaryParts({ status, complete, deadlines }: SummaryInput): string[] {
	if (status === 'pending') return [m.common_loading()];
	if (deadlines === undefined) return [m.services_summary_sync_failed()];
	if (deadlines.length === 0)
		return [complete ? m.services_summary_none() : m.services_summary_sync_failed()];
	const upcoming = deadlines.find((item) => item.days >= 0);
	const when = upcoming
		? m.services_summary_next(onDay(upcoming.date))
		: m.services_summary_overdue_on(onDay(deadlines[0].date));
	const urgent = deadlines.filter((item) => item.urgent).length;
	return urgent === 0 ? [when] : [m.services_summary_urgent({ count: urgent }), when];
}

/** The banner's line on one row, owning up to the sources it lacks. */
export function summaryLine(input: SummaryInput): string {
	const parts = summaryParts(input);
	const partial = (input.deadlines?.length ?? 0) > 0 && (input.missing?.length ?? 0) > 0;
	return [...parts, ...(partial ? [m.services_summary_partial()] : [])].join(' · ');
}

/**
 * The warning a refresh that failed over older data leaves: how old the data is, worded from `now`.
 * Data loaded within the minute is current, so only the failure is told.
 */
export function staleLine(updatedAt: number, now: Date): string {
	if (now.getTime() - updatedAt < 60_000) return m.services_stale_line_now();
	return m.services_stale_line({ time: formatRelative(updatedAt, now) });
}

const streams: Record<DeadlineStream, () => string> = {
	results: m.services_stream_results,
	fees: m.services_stream_fees,
	exams: m.services_stream_exams,
	bookings: m.services_stream_bookings
};

/** "gli esiti e le tasse": the streams a partial merge lacks. */
export function streamList(names: readonly DeadlineStream[]): string {
	const tag = intlLocale();
	const options = { style: 'long', type: 'conjunction' } as const;
	const format = cached('list', tag, options, () => new Intl.ListFormat(tag, options));
	return format.format(names.map((name) => streams[name]()));
}
