import {
	calendarDays,
	formatDate,
	formatRelativeDays,
	formatTime,
	formatWeekday
} from '#lib/format/dates.ts';
import { capitalize } from '#lib/format/names.ts';
import { dueTone } from '#lib/format/due.ts';
import { formatGrade } from '#lib/format/grades.ts';
import { cached } from '#lib/format/locale.ts';
import { m } from '#lib/paraglide/messages.js';
import { getLocale } from '#lib/paraglide/runtime.js';
import type { Status } from '#lib/ui/types.ts';
import {
	daysToClose,
	windowStatus,
	type Booking,
	type CallType,
	type ExamCall,
	type ExamKind,
	type ExamLocation
} from './model.ts';

/** "Written", or "Written · partial exam". */
export function kindLabel(kind: ExamKind, type: CallType = 'final'): string {
	const label = {
		written: m.exams_kind_written,
		oral: m.exams_kind_oral,
		written_oral: m.exams_kind_written_oral,
		unknown: m.exams_kind_unknown
	}[kind]();
	if (type !== 'partial') return label;
	return kind === 'unknown' ? m.exams_partial() : m.exams_kind_with_partial({ kind: label });
}

/** "Written exam", for a call's heading. */
export function callKindLabel(kind: ExamKind, type: CallType = 'final'): string {
	const label = {
		written: m.exams_call_written,
		oral: m.exams_call_oral,
		written_oral: m.exams_call_written_oral,
		unknown: m.exams_call_unknown
	}[kind]();
	return type === 'partial' ? `${label} · ${m.exams_partial().toLocaleLowerCase()}` : label;
}

/** "Wednesday 1 July 2026 · 14:00", or the date alone when the time is unknown. */
export function longWhen(date: Date, timed: boolean): string {
	const day = capitalize(formatDate(date, 'full'));
	return timed ? `${day} · ${formatTime(date)}` : day;
}

/** "Wednesday 1 July 2026 at 14:00", lower-case for the middle of a sentence. */
export function sentenceWhen(date: Date, timed: boolean): string {
	const day = formatDate(date, 'full');
	return timed ? m.exams_date_at_time({ date: day, time: formatTime(date) }) : day;
}

/** "Fri · 09:30", the time line under a booking whose day is already shown. */
export function weekdayTime(date: Date, timed: boolean): string {
	const weekday = capitalize(formatWeekday(date, 'short').replace('.', ''));
	return timed ? `${weekday} · ${formatTime(date)}` : weekday;
}

/** How far a countdown reaches: further than two months ahead, a date reads better. */
const COUNTDOWN_DAYS = 60;

/** "Today", "Tomorrow", "In 8 days" up to two months ahead; nothing for the past or later. */
export function countdown(date: Date, now: Date = new Date()): string | undefined {
	const days = calendarDays(now, date);
	if (days < 0 || days > COUNTDOWN_DAYS) return undefined;
	return capitalize(formatRelativeDays(days));
}

/** "Exam in 8 days", where the countdown could be taken for the registrations' one. */
export function examCountdown(date: Date, now: Date = new Date()): string | undefined {
	const days = calendarDays(now, date);
	if (days < 0 || days > COUNTDOWN_DAYS) return undefined;
	return m.exams_exam_when({ when: formatRelativeDays(days) });
}

/** A queue position: "37th" in English, "37º" in Italian. */
export function ordinal(position: number): string {
	if (getLocale() === 'it') return `${position}º`;
	const rules = cached(
		'ordinal',
		'en-GB',
		{ type: 'ordinal' },
		() => new Intl.PluralRules('en-GB', { type: 'ordinal' })
	);
	const suffix: Partial<Record<Intl.LDMLPluralRule, string>> = { one: 'st', two: 'nd', few: 'rd' };
	return `${position}${suffix[rules.select(position)] ?? 'th'}`;
}

export function positionLabel(position: number, total: number | null | undefined): string {
	return total !== null && total !== undefined && total >= position
		? m.exams_position_of_total({ position: ordinal(position), total })
		: m.exams_position_alone({ position: ordinal(position) });
}

export interface CallState {
	status: Status;
	/** "Closing", for a chip in a list whose rows say by when. */
	label: string;
	/** The chip on the call's own page, where it stands alone: "Closes in 2 days". */
	long: string;
	/** One line on the registration window: "Book by 28 Jun", "Opens 3 Jun". */
	window: string;
}

/** Where the call's registrations stand, as a chip and a short line; never told by colour alone. */
export function callState(call: ExamCall, now: Date = new Date()): CallState {
	const status = windowStatus(call, now);
	if (status === 'upcoming') {
		const label = m.exams_status_upcoming();
		return {
			status: 'neutral',
			label,
			long: label,
			window: call.opensAt
				? m.exams_opens_on({ date: formatDate(call.opensAt, 'dayMonth') })
				: m.exams_status_upcoming()
		};
	}
	if (status === 'closed') {
		const label = m.exams_status_closed();
		return { status: 'neutral', label, long: label, window: m.exams_closed_short() };
	}
	const left = daysToClose(call, now);
	/* Closing as any deadline turns urgent: red on its last day, amber in the two before. */
	const tone = left === undefined ? 'muted' : dueTone(left);
	return {
		status: tone === 'muted' ? 'success' : tone,
		label: tone === 'muted' ? m.exams_status_open() : m.exams_status_closing(),
		long:
			left === undefined || tone === 'muted'
				? m.exams_status_open()
				: m.exams_closes_when({ when: formatRelativeDays(left) }),
		window:
			left === undefined || !call.closesAt
				? m.exams_status_open()
				: left === 0
					? m.exams_book_by_today()
					: left === 1
						? m.exams_book_by_tomorrow()
						: m.exams_book_by({ date: formatDate(call.closesAt, 'dayMonth') })
	};
}

/** The registration window as a sentence for the call's page. */
export function windowSentence(call: ExamCall, now: Date = new Date()): string {
	const status = windowStatus(call, now);
	const long = (date: Date) => formatDate(date, 'full');
	if (status === 'upcoming') {
		return call.opensAt
			? m.exams_window_opens({ date: relativeOrLong(call.opensAt, now) })
			: m.exams_window_closed();
	}
	if (status === 'closed') {
		return call.closesAt
			? m.exams_window_closed_on({ date: relativeOrLong(call.closesAt, now) })
			: m.exams_window_closed();
	}
	const left = daysToClose(call, now);
	if (left === 0) return m.exams_window_last_day();
	return call.closesAt
		? m.exams_window_open_until({ date: long(call.closesAt) })
		: m.exams_window_open();
}

/** "tomorrow", "yesterday" near today, else "on Monday 3 June 2026" style long dates. */
function relativeOrLong(date: Date, now: Date): string {
	const days = calendarDays(now, date);
	return Math.abs(days) <= 1 ? formatRelativeDays(days) : formatDate(date, 'full');
}

/** The registration window as a range: "From 1 Jun 2026 to 28 Jun 2026". */
export function windowRange(call: ExamCall): string | undefined {
	const start = call.opensAt && formatDate(call.opensAt, 'medium');
	const end = call.closesAt && formatDate(call.closesAt, 'medium');
	if (start && end) return m.exams_window_range({ start, end });
	if (start) return m.exams_window_from({ start });
	if (end) return m.exams_window_until({ end });
	return undefined;
}

/**
 * How an exam is held, from Esse3's Italian ("Esame in Presenza"): the two usual ways in the app's
 * language, anything else in Esse3's words, in sentence case.
 */
export function modeLabel(mode: string | null): string | null {
	if (!mode) return null;
	const inPerson = /presenza/i.test(mode);
	const remote = /distanza|online|remot|telematic/i.test(mode);
	if (inPerson && !remote) return m.exams_mode_in_person();
	if (remote && !inPerson) return m.exams_mode_remote();
	return capitalize(mode.toLocaleLowerCase('it-IT'));
}

/** "U4-08 · Piazza della Scienza 4": the room and where it is, as one line. */
export function locationLine(location: ExamLocation): string | null {
	return [location.room, location.place].filter(Boolean).join(' · ') || null;
}

/** A sat exam's outcome as a status chip: the grade once published, else "Awaiting result". */
export function outcomeStatus(booking: Pick<Booking, 'grade' | 'published'>): {
	status: Status;
	label: string;
} {
	const { grade } = booking;
	if (!booking.published || grade.kind === 'unknown') {
		return { status: 'pending', label: m.exams_awaiting_result() };
	}
	const label = formatGrade(grade, { full: true });
	switch (grade.kind) {
		case 'numeric':
			return { status: grade.cumLaude ? 'gold' : 'success', label };
		case 'passed':
			return { status: 'success', label };
		case 'not_passed':
			return { status: 'danger', label };
		default:
			return { status: 'neutral', label };
	}
}
