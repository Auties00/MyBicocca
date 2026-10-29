import type { DataError } from '#lib/data/errors.ts';
import { errorMessage } from '#lib/data/errorMessage.ts';
import { calendarDays, formatDate, formatRelative, formatTime } from '#lib/format/dates.ts';
import { cached, intlLocale } from '#lib/format/locale.ts';
import { formatBytes, formatNumber } from '#lib/format/numbers.ts';
import { m } from '#lib/paraglide/messages.js';
import type { Status } from '#lib/ui/types.ts';
import type { FileProblem } from './draft.ts';
import { parseToken, type FileTypeGroup } from './filetypes.ts';
import {
	nothingOnline,
	phaseOf,
	spanOf,
	type FileRules,
	type Grade,
	type Phase,
	type Schedule,
	type Standing,
	type Submission,
	type SubmissionConfig
} from './model.ts';

/** "gio 18 giu, 23:59", as the Android sheets write instants. */
export function formatMoment(at: number): string {
	return `${formatDate(at, 'weekdayDayMonth')}, ${formatTime(at)}`;
}

/** "18 giu", or "18 giu 2025" outside the current year. */
export function formatDay(at: number, now: number): string {
	return new Date(at).getFullYear() === new Date(now).getFullYear()
		? formatDate(at, 'dayMonth')
		: formatDate(at, 'medium');
}

/** "2 giorni e 4 ore", "12 minuti", "meno di un minuto". */
export function formatSpan(ms: number): string {
	if (Math.abs(ms) < 60_000) return m.assignment_less_than_minute();
	const tag = intlLocale();
	const format = cached(
		'duration',
		tag,
		{ style: 'long' },
		() => new Intl.DurationFormat(tag, { style: 'long' })
	);
	return format.format(spanOf(ms));
}

/** "a.pdf, b.zip e c.py". */
export function formatList(items: readonly string[]): string {
	const tag = intlLocale();
	const format = cached(
		'list',
		tag,
		{ type: 'conjunction' },
		() => new Intl.ListFormat(tag, { type: 'conjunction' })
	);
	return format.format(items);
}

/** The page's subtitle: when it is due, or that it never is. */
export function dueLine(schedule: Schedule, now: number): string {
	if (schedule.due === null) return m.assignment_no_deadline();
	return schedule.due < now
		? m.assignment_due_passed({ moment: formatMoment(schedule.due) })
		: m.assignment_due_moment({ moment: formatMoment(schedule.due) });
}

/**
 * The editor's subtitle: the due date with how long is left (on a timed assignment's clock, when
 * it runs), or once it has passed, until when late work is taken.
 */
export function editorContext(
	schedule: Schedule,
	phase: Phase,
	now: number,
	timerEnd: number | null = null
): string {
	const due = dueLine(schedule, now);
	if (timerEnd !== null && phase === 'open') {
		return `${due} · ${
			timerEnd > now
				? m.assignment_context_timer({ span: formatSpan(timerEnd - now) })
				: m.assignment_context_timer_over()
		}`;
	}
	if (schedule.due === null) return due;
	if (phase === 'open') {
		return `${due} · ${m.assignment_context_left({ span: formatSpan(schedule.due - now) })}`;
	}
	if (phase === 'late') {
		return `${due} · ${
			schedule.cutoff === null
				? m.assignment_context_late_open()
				: m.assignment_context_late_until({ moment: formatMoment(schedule.cutoff) })
		}`;
	}
	return due;
}

/** What a hand-in after the due date adds to its confirmation; null before it. */
export function lateNote(schedule: Schedule, phase: Phase): string | null {
	if (phase !== 'late') return null;
	return schedule.cutoff === null
		? m.assignment_submit_late_note_open()
		: m.assignment_submit_late_note({ moment: formatMoment(schedule.cutoff) });
}

/**
 * The countdown under the status card's headline, while there is something to do: on a timed
 * assignment whose clock runs, the time left on it.
 */
export function countdownOf(
	schedule: Schedule,
	submission: Submission,
	phase: Phase,
	now: number,
	timerEnd: number | null = null
): { text: string; late?: string } | null {
	if (submission.stage === 'submitted' || submission.stage === 'graded') return null;
	if (phase === 'upcoming' && schedule.opens !== null) {
		return { text: m.assignment_opens_in({ span: formatSpan(schedule.opens - now) }) };
	}
	if (timerEnd !== null && phase === 'open') {
		return timerEnd > now
			? { text: m.assignment_timer_left({ span: formatSpan(timerEnd - now) }) }
			: { text: m.assignment_timer_over({ span: formatSpan(now - timerEnd) }) };
	}
	if (schedule.due === null) return null;
	if (phase === 'open') {
		return { text: m.assignment_time_left({ span: formatSpan(schedule.due - now) }) };
	}
	if (phase === 'late') {
		return {
			text: m.assignment_overdue_by({ span: formatSpan(now - schedule.due) }),
			late:
				schedule.cutoff !== null
					? m.assignment_late_left({ span: formatSpan(schedule.cutoff - now) })
					: m.assignment_late_forever()
		};
	}
	return null;
}

/** "27/30", or the scale's word, or null when there is feedback but no grade. */
export function gradeText(grade: Grade): string | null {
	if (grade.value !== null && grade.max !== null) {
		const value = formatNumber(grade.value, { maximumFractionDigits: 2 });
		return `${value}/${formatNumber(grade.max, { maximumFractionDigits: 2 })}`;
	}
	return grade.display;
}

/** The grade as its two halves, the mark set large and what it is out of small. */
export function gradeParts(grade: Grade): { value: string; max: string | null } | null {
	const text = gradeText(grade);
	if (text === null) return null;
	const slash = text.indexOf('/');
	return slash < 0
		? { value: text, max: null }
		: { value: text.slice(0, slash), max: text.slice(slash + 1) };
}

function dueWhen(due: number, now: number): string {
	const days = calendarDays(new Date(now), new Date(due));
	return days > 14
		? m.assignment_due_on({ date: formatDay(due, now) })
		: m.assignment_due_relative({
				when: formatRelative(due, new Date(now))
			});
}

const GROUP_NAMES: Readonly<Record<FileTypeGroup, () => string>> = {
	archive: m.assignment_types_archive,
	document: m.assignment_types_document,
	spreadsheet: m.assignment_types_spreadsheet,
	presentation: m.assignment_types_presentation,
	image: m.assignment_types_image,
	web_image: m.assignment_types_image,
	optimised_image: m.assignment_types_image,
	audio: m.assignment_types_audio,
	web_audio: m.assignment_types_audio,
	html_audio: m.assignment_types_audio,
	video: m.assignment_types_video,
	web_video: m.assignment_types_video,
	html_video: m.assignment_types_video,
	web_file: m.assignment_types_web
};

/** The accepted types as students read them: "PDF, ZIP, immagini". */
export function typesText(types: readonly string[]): string {
	const names = types.map((type) => {
		const token = parseToken(type);
		switch (token.kind) {
			case 'extension':
				return token.extension.toUpperCase();
			case 'group':
				return GROUP_NAMES[token.group]();
			case 'mime':
				return token.mime;
			case 'unknown':
				return token.token;
		}
	});
	return [...new Set(names)].join(', ');
}

/** The file rules in one line: "Fino a 3 file · massimo 20 MB ciascuno · PDF, ZIP". */
export function rulesText(rules: FileRules): string {
	return [
		m.assignment_editor_limit_files({ count: rules.maxFiles }),
		rules.maxBytes === null
			? null
			: rules.maxFiles === 1
				? m.assignment_editor_limit_size_one({ size: formatBytes(rules.maxBytes) })
				: m.assignment_editor_limit_size({ size: formatBytes(rules.maxBytes) }),
		rules.types.length > 0 ? typesText(rules.types) : null
	]
		.filter((part) => part !== null)
		.join(' · ');
}

/** Why only the e-learning site can take the submission (see `onMoodleOnly`). */
export function onMoodleReason(config: SubmissionConfig): string {
	return m.assignment_on_moodle_body({ tools: formatList(config.unsupported) });
}

/** What the submission takes, a line per part: the text, the files, the statement. */
export function partsText(config: SubmissionConfig): string[] {
	const parts: string[] = [];
	if (config.text !== null) {
		parts.push(
			config.text.wordLimit === null
				? m.assignment_part_text()
				: m.assignment_part_text_limit({ limit: config.text.wordLimit })
		);
	}
	if (config.files !== null) parts.push(rulesText(config.files));
	if (config.statement !== null) parts.push(m.assignment_part_statement());
	return parts;
}

/** Why a picked file was left out: "script.py: tipo di file non accettato". */
export function problemText(name: string, problem: FileProblem): string {
	switch (problem.kind) {
		case 'too-large':
			return m.assignment_editor_rejected_size({ name, size: formatBytes(problem.limit) });
		case 'empty':
			return m.assignment_editor_rejected_empty({ name });
		case 'type':
			return m.assignment_editor_rejected_type({ name });
	}
}

/**
 * The row's status line, as the Android list words it, with the dates that matter: when it is
 * due, how late it is and until when it can still go in, when it was handed in.
 */
export function rowStatus(
	schedule: Schedule,
	submission: Submission,
	config: SubmissionConfig | undefined,
	now: number
): string {
	const join = (...parts: (string | null)[]) => parts.filter((part) => part).join(' · ');
	switch (submission.stage) {
		case 'graded': {
			const at = submission.grade?.gradedAt;
			return at ? m.assignment_graded_on({ date: formatDay(at, now) }) : m.assignment_graded();
		}
		case 'submitted': {
			const at = submission.savedAt;
			const late = at !== null && schedule.due !== null && at > schedule.due;
			return join(
				at ? m.assignment_submitted_on({ date: formatDay(at, now) }) : m.assignment_submitted(),
				late ? m.assignment_late() : null
			);
		}
		case 'draft':
			return join(
				m.assignment_draft_saved(),
				schedule.due !== null && schedule.due > now ? dueWhen(schedule.due, now) : null
			);
		case 'new':
			break;
	}
	const phase = phaseOf(schedule, now, submission.canEdit);
	if (phase === 'upcoming' && schedule.opens !== null) {
		return m.assignment_opens_on({ date: formatDay(schedule.opens, now) });
	}
	if (nothingOnline(config)) return m.assignment_nothing_online();
	if (schedule.due === null) {
		return phase === 'closed' ? m.assignment_not_submitted() : m.assignment_no_deadline();
	}
	if (schedule.due >= now) return dueWhen(schedule.due, now);
	if (phase === 'late') {
		return join(
			m.assignment_was_due({ when: formatRelative(schedule.due, new Date(now)) }),
			schedule.cutoff !== null
				? m.assignment_late_until({ date: formatDay(schedule.cutoff, now) })
				: m.assignment_late_open()
		);
	}
	return m.assignment_closed_on({ date: formatDay(schedule.cutoff ?? schedule.due, now) });
}

/** The short state a chip or a table column shows. */
export interface StageLabel {
	label: string;
	tone: Status;
}

export function stageLabel(standing: Standing): StageLabel {
	switch (standing) {
		case 'graded':
			return { label: m.assignment_stage_graded(), tone: 'success' };
		case 'submitted':
			return { label: m.assignment_stage_submitted(), tone: 'success' };
		case 'draft':
			return { label: m.assignment_stage_draft(), tone: 'info' };
		case 'none':
			return { label: m.assignment_stage_none(), tone: 'neutral' };
		case 'upcoming':
			return { label: m.assignment_stage_upcoming(), tone: 'pending' };
		case 'todo':
			return { label: m.assignment_stage_todo(), tone: 'accent' };
		case 'late':
			return { label: m.assignment_stage_late(), tone: 'danger' };
		case 'lapsed':
		case 'missed':
			return { label: m.assignment_stage_missed(), tone: 'neutral' };
	}
}

/**
 * What to tell the student about a failed write: Moodle's own notice when it refused (it comes
 * in the app's language, and says why: "the due date has passed"), else the kind of failure.
 */
export function failureText(error: DataError): string {
	if (error.kind === 'rejected' && error.service === 'moodle') {
		const notice = error.message
			.replace(/<[^>]*>/g, ' ')
			.replace(/\s+/g, ' ')
			.trim();
		if (notice !== '') return notice;
	}
	return errorMessage(error);
}
