import IconCheck from '~icons/ph/check-bold';
import IconClock from '~icons/ph/clock-bold';
import IconInfo from '~icons/ph/info-bold';
import IconKey from '~icons/ph/key-bold';
import IconProhibit from '~icons/ph/prohibit-bold';
import IconWarning from '~icons/ph/warning-bold';
import IconWifiX from '~icons/ph/wifi-x-bold';
import { errorMessage } from '#lib/data/errorMessage.ts';
import { italianTitleCase, personName } from '#lib/format/names.ts';
import { formatTime } from '#lib/format/dates.ts';
import { formatCredits, formatPercent } from '#lib/format/numbers.ts';
import { m } from '#lib/paraglide/messages.js';
import type { OutcomeTone } from '#lib/ui/Outcome.svelte';
import type { Icon, Tone } from '#lib/ui/types.ts';
import type { CheckInPhase } from './checkInFlow.svelte.ts';
import { headlinePercent, type ClassroomStatus, type CourseAttendance } from './model.ts';
import type { CheckInOutcome } from './outcome.ts';

export const statusLabel: Record<ClassroomStatus, () => string> = {
	attending: m.attendance_status_attending,
	'in-progress': m.attendance_status_in_progress,
	'not-attending': m.attendance_status_not_attending
};

export const statusTone: Record<ClassroomStatus, Tone> = {
	attending: 'success',
	'in-progress': 'warning',
	'not-attending': 'danger'
};

/** A rounded percentage, 0–100, as the locale writes it. */
export function percentText(value: number): string {
	return formatPercent(Math.round(value) / 100);
}

export interface CourseCaption {
	/** The classroom standing, in its tone. */
	status?: { label: string; tone: Tone };
	/** The rest, already joined. */
	details: string;
}

/**
 * What a row says under the course, as Android's caption: the classroom standing, else the first
 * register's count, then the percentage its ring shows (which is hidden from screen readers).
 */
export function courseCaption(course: CourseAttendance): CourseCaption {
	const { classroom } = course;
	if (classroom) {
		return {
			status: { label: statusLabel[classroom.status](), tone: statusTone[classroom.status] },
			details: [
				m.attendance_lessons({ count: classroom.lessonsAttended }),
				percentText(classroom.percentage)
			].join(' · ')
		};
	}
	const percent = headlinePercent(course);
	const attended = course.registers.find((item) => item.attended !== null)?.attended;
	const parts = [
		attended == null ? undefined : m.attendance_presences({ count: attended }),
		percent === null ? undefined : m.attendance_recorded_percent({ percent: percentText(percent) })
	];
	return {
		details: parts.filter((part) => part !== undefined).join(' · ') || m.attendance_no_records()
	};
}

export function yearLabel(year: number): string {
	return year > 0 ? m.attendance_year({ year }) : m.attendance_year_other();
}

export interface CourseHeading {
	/** The Esse3 name, which comes in capitals, in title case. */
	title: string;
	/** Year, semester and credits. */
	eyebrow: string;
	teacher: string | undefined;
}

export function courseHeading(course: CourseAttendance): CourseHeading {
	const semester =
		course.semester === 'first'
			? m.attendance_semester_first()
			: course.semester === 'second'
				? m.attendance_semester_second()
				: undefined;
	const credits = course.credits > 0 ? formatCredits(course.credits) : undefined;
	return {
		title: italianTitleCase(course.name),
		eyebrow: [yearLabel(course.year), semester, credits].filter(Boolean).join(' · '),
		teacher: course.teacher ? personName(course.teacher) : undefined
	};
}

/** The line under the check-in's title follows it, as Android's header: the ask, then the wait, then the time. */
export function checkInSubtitle(phase: CheckInPhase): string {
	switch (phase.name) {
		case 'form':
			return m.attendance_checkin_subtitle();
		case 'sending':
			return m.attendance_progress_subtitle();
		case 'result':
			return m.attendance_result_time({ time: formatTime(phase.at) });
	}
}

export interface OutcomeView {
	icon: Icon;
	tone: OutcomeTone;
	title: string;
	/** Hidden when it would repeat the title. */
	detail: string | undefined;
	/**
	 * A failure the student can do something about: a code to correct, or a request to send
	 * again. Any other outcome only ends the check-in.
	 */
	retry: boolean;
}

function notOpenDetail(reason: Extract<CheckInOutcome, { kind: 'not-open' }>['reason']): string {
	switch (reason) {
		case 'closed':
			return m.attendance_msg_session_closed();
		case 'no-register':
			return m.attendance_msg_no_register_found();
		case 'not-in-courses':
			return m.attendance_msg_session_not_in_courses();
	}
}

function failedDetail(outcome: Extract<CheckInOutcome, { kind: 'failed' }>): string {
	if (outcome.error) return errorMessage(outcome.error);
	if (outcome.message) return outcome.message;
	switch (outcome.reason) {
		case 'unrecognized':
			return m.attendance_msg_code_unrecognized();
		case 'student-number':
			return m.attendance_msg_student_number_unavailable();
		case 'generic':
			return m.attendance_msg_failed();
	}
}

function view(
	icon: Icon,
	tone: OutcomeTone,
	title: string,
	detail: string,
	retry = false
): OutcomeView {
	return {
		icon,
		tone,
		title,
		detail:
			detail.localeCompare(title, undefined, { sensitivity: 'base' }) === 0 ? undefined : detail,
		retry
	};
}

export function outcomeView(outcome: CheckInOutcome): OutcomeView {
	switch (outcome.kind) {
		case 'recorded':
			return view(
				IconCheck,
				'success',
				m.attendance_presence_recorded(),
				outcome.status
					? m.attendance_status_label({ status: outcome.status })
					: (outcome.message ?? m.attendance_msg_recorded())
			);
		case 'already-recorded':
			return view(
				IconInfo,
				'info',
				m.attendance_already_recorded(),
				m.attendance_msg_already_recorded()
			);
		case 'wrong-credential':
			return view(
				IconKey,
				'danger',
				m.attendance_wrong_credential(),
				outcome.message ?? m.attendance_msg_wrong_credential(),
				true
			);
		case 'not-open':
			return view(
				IconClock,
				'info',
				m.attendance_session_unavailable(),
				notOpenDetail(outcome.reason)
			);
		case 'network-restricted':
			return view(
				IconWifiX,
				'danger',
				m.attendance_registration_failed(),
				m.attendance_msg_network_wrong()
			);
		case 'device-already-used':
			return view(
				IconProhibit,
				'danger',
				m.attendance_registration_failed(),
				m.attendance_msg_network_prevent_shared()
			);
		case 'failed':
			return view(
				IconWarning,
				'danger',
				m.attendance_registration_failed(),
				failedDetail(outcome),
				outcome.error !== undefined || outcome.reason === 'unrecognized'
			);
	}
}
