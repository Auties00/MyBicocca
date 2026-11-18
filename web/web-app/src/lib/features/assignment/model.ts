import type {
	MoodleAssignment,
	MoodleFile,
	MoodleSubmissionPlugin,
	MoodleSubmissionStatus
} from '@mybicocca/data-api/elearning';
import type { CourseFile } from '#lib/features/course/content.ts';
import { intlLocale } from '#lib/format/locale.ts';

const HOUR = 3_600_000;
const DAY = 24 * HOUR;

/** Android's window for "due soon": the row and the countdown turn amber. */
export const DUE_SOON_MS = 48 * HOUR;

/** A Moodle instant (seconds, 0 for "not set") in epoch milliseconds. */
export function instant(seconds: number | null | undefined): number | null {
	return seconds !== null && seconds !== undefined && seconds > 0 ? seconds * 1000 : null;
}

export function toCourseFile(file: MoodleFile): CourseFile | null {
	if (!file.fileUrl || !file.fileName) return null;
	return {
		url: file.fileUrl,
		name: file.fileName,
		folder: file.filePath ?? '/',
		mime: file.mimeType,
		size: file.fileSize,
		modified: instant(file.timeModified)
	};
}

function courseFiles(files: readonly MoodleFile[]): CourseFile[] {
	return files.flatMap((file) => toCourseFile(file) ?? []);
}

/** Whether Moodle HTML shows nothing: no text and no picture or media. */
export function isBlankHtml(html: string | null | undefined): boolean {
	if (!html) return true;
	if (/<(img|video|audio|iframe|object)\b/i.test(html)) return false;
	return (
		html
			.replace(/<[^>]*>/g, '')
			.replace(/&nbsp;|&#160;|\u00a0/g, ' ')
			.trim() === ''
	);
}

/**
 * The files the brief offers: the attachments, and the intro's own files that its text doesn't
 * already show (Moodle lists images embedded in the intro there too).
 */
export function briefFiles(
	assignment: MoodleAssignment,
	status: MoodleSubmissionStatus | undefined
): CourseFile[] {
	const attachments =
		assignment.introAttachments.length > 0
			? assignment.introAttachments
			: (status?.assignmentData?.attachments?.intro ?? []);
	const intro = assignment.intro ?? '';
	const loose = assignment.introFiles.filter(
		(file) =>
			file.fileName !== null &&
			file.fileSize !== 0 &&
			!intro.includes(encodeURIComponent(file.fileName)) &&
			!intro.includes(file.fileName)
	);
	const seen = new Set<string>();
	return courseFiles([...attachments, ...loose]).filter((file) => {
		const fresh = !seen.has(file.name);
		seen.add(file.name);
		return fresh;
	});
}

/** The instructions Moodle shows only while submitting, with their files. */
export function activityInstructions(
	assignment: MoodleAssignment,
	status: MoodleSubmissionStatus | undefined
): { html: string | null; files: CourseFile[] } {
	const html = assignment.activity ?? status?.assignmentData?.activity ?? null;
	const files =
		assignment.activityAttachments.length > 0
			? assignment.activityAttachments
			: (status?.assignmentData?.attachments?.activity ?? []);
	return { html: isBlankHtml(html) ? null : html, files: courseFiles(files) };
}

export type Stage = 'new' | 'draft' | 'submitted' | 'graded';

/** Where the submission itself stands, grade aside: what can still be done with it. */
export type SubmissionState = 'new' | 'draft' | 'submitted';

export interface Grade {
	/** The mark, when the assignment is marked out of a number. */
	value: number | null;
	/** What the mark is out of; null for scales and ungraded feedback. */
	max: number | null;
	/** Moodle's own wording of the grade ("Buono" on a scale), when there is no number. */
	display: string | null;
	gradedAt: number | null;
	/** The teacher's comments, HTML. */
	feedback: string | null;
	files: CourseFile[];
}

/** An earlier attempt the teachers marked before reopening the assignment for another. */
export interface PreviousAttempt {
	/** 1 for the first attempt. */
	number: number;
	grade: Grade;
}

export interface Submission {
	stage: Stage;
	state: SubmissionState;
	/** 1 for the first attempt; more once the teachers reopen the assignment. */
	attempt: number;
	/** When it was last saved or handed in; null while nothing was ever saved. */
	savedAt: number | null;
	/** The online text, HTML, when not blank. */
	text: string | null;
	/** The format Moodle gives the online text in (1 for HTML). */
	textFormat: number;
	files: CourseFile[];
	grade: Grade | null;
	/** The latest earlier attempt with a grade or feedback, while this one has none. */
	previous: PreviousAttempt | null;
	/** Moodle's word on whether the student may change the submission now. */
	canEdit: boolean;
	/** Whether a draft may be submitted for grading now. */
	canSubmit: boolean;
	/** A personal extension of the due date. */
	extension: number | null;
	/** When the student started a timed assignment's clock. */
	startedAt: number | null;
	/** The time limit that applies to the student (ms), overrides included, when Moodle says. */
	timeLimit: number | null;
	/** The submission's parts, by plugin type, as Moodle lists them. */
	plugins: readonly MoodleSubmissionPlugin[];
}

function plainText(html: string): string {
	return html
		.replace(/<[^>]*>/g, ' ')
		.replace(/&nbsp;|&#160;/g, ' ')
		.replace(/\s+/g, ' ')
		.trim();
}

interface Marking {
	/** Moodle's raw grade ("27.00000"; negative while not graded). */
	grade: string | null;
	display: string | null;
	gradedAt: number | null;
	plugins: readonly MoodleSubmissionPlugin[];
}

function readGrade(marking: Marking, assignment?: MoodleAssignment): Grade | null {
	const raw = Number.parseFloat(marking.grade ?? '');
	const value = Number.isFinite(raw) && raw >= 0 ? raw : null;
	const display = plainText(marking.display ?? '');
	const comments = marking.plugins.find((plugin) => plugin.type === 'comments');
	const text = comments?.editorFields[0]?.text ?? null;
	const files = courseFiles(
		marking.plugins.flatMap((plugin) => plugin.fileAreas.flatMap((area) => area.files))
	);
	const max = assignment !== undefined && assignment.grade > 0 ? assignment.grade : null;
	const shown = display === '' || display === '-' ? null : display;
	if (value === null && shown === null && isBlankHtml(text) && files.length === 0) return null;
	return {
		value: max === null ? null : value,
		max,
		display: shown,
		gradedAt: marking.gradedAt,
		feedback: isBlankHtml(text) ? null : text,
		files
	};
}

function filesOf(plugins: readonly MoodleSubmissionPlugin[], type: string): CourseFile[] {
	return courseFiles(
		plugins
			.filter((plugin) => plugin.type === type)
			.flatMap((plugin) => plugin.fileAreas.flatMap((area) => area.files))
	);
}

/** The latest attempt before `current` (0-based) that the teachers marked. */
function previousAttempt(
	status: MoodleSubmissionStatus,
	current: number,
	assignment?: MoodleAssignment
): PreviousAttempt | null {
	const earlier = status.previousAttempts
		.filter((item) => item.attemptNumber < current)
		.toSorted((a, b) => b.attemptNumber - a.attemptNumber);
	for (const item of earlier) {
		const grade = readGrade(
			{
				grade: item.grade?.grade ?? null,
				display: item.grade?.gradeForDisplay ?? null,
				gradedAt: instant(item.grade?.timeModified),
				plugins: item.feedbackPlugins
			},
			assignment
		);
		if (grade !== null) return { number: item.attemptNumber + 1, grade };
	}
	return null;
}

/**
 * The student's submission as the pages show it. A released grade or feedback makes it graded,
 * whatever the submission's own status: teachers grade work handed in on paper too.
 */
export function readSubmission(
	status: MoodleSubmissionStatus,
	assignment?: MoodleAssignment
): Submission {
	const attempt = status.lastAttempt;
	const submission = attempt?.teamSubmission ?? attempt?.submission ?? null;
	const plugins = submission?.plugins ?? [];
	const editor = plugins.find((plugin) => plugin.type === 'onlinetext')?.editorFields[0];
	const text = editor?.text;
	const feedback = status.feedback;
	const grade =
		feedback === null
			? null
			: readGrade(
					{
						grade: feedback.grade?.grade ?? null,
						display: feedback.grade?.gradeForDisplay ?? feedback.gradeForDisplay,
						gradedAt: instant(feedback.gradedDate),
						plugins: feedback.plugins
					},
					assignment
				);
	const raw = submission?.status;
	const state: SubmissionState =
		raw === 'submitted' ? 'submitted' : raw === 'draft' ? 'draft' : 'new';
	const number = submission?.attemptNumber ?? 0;
	return {
		stage: grade !== null ? 'graded' : state,
		state,
		attempt: number + 1,
		savedAt:
			submission !== null && state !== 'new'
				? (instant(submission.timeModified) ?? instant(submission.timeCreated))
				: null,
		text: isBlankHtml(text) ? null : (text ?? null),
		textFormat: editor?.format ?? 1,
		files: filesOf(plugins, 'file'),
		grade,
		previous: grade === null ? previousAttempt(status, number, assignment) : null,
		canEdit: attempt?.canEdit ?? false,
		canSubmit: attempt?.canSubmit ?? false,
		extension: instant(attempt?.extensionDueDate),
		startedAt: instant(submission?.timeStarted),
		timeLimit:
			attempt?.timeLimit != null && attempt.timeLimit > 0 ? attempt.timeLimit * 1000 : null,
		plugins
	};
}

/** Whether the work is with the teachers: submitted or graded. */
export function handedIn(stage: Stage | undefined): boolean {
	return stage === 'submitted' || stage === 'graded';
}

export interface FileRules {
	maxFiles: number;
	/** Per file; null when nothing says. */
	maxBytes: number | null;
	/** Moodle's accepted types as configured: extensions (".pdf") and groups ("document"). */
	types: string[];
}

export interface SubmissionConfig {
	text: { wordLimit: number | null } | null;
	files: FileRules | null;
	/** Parts of the submission only Moodle's own pages can fill, by name. */
	unsupported: string[];
	/** Whether a saved submission stays a draft until submitted for grading. */
	drafts: boolean;
	/** The statement to accept when submitting, when the assignment requires one. */
	statement: string | null;
	/** How long the student has once started (ms), for a timed assignment. */
	timeLimit: number | null;
}

/** Whether the student can hand anything in from here. */
export function submittable(config: SubmissionConfig | undefined): boolean {
	return config !== undefined && (config.text !== null || config.files !== null);
}

/** Whether only Moodle's own pages can take the submission: it is made of parts the app can't fill. */
export function onMoodleOnly(config: SubmissionConfig | undefined): boolean {
	return config !== undefined && !submittable(config) && config.unsupported.length > 0;
}

/** The time limit of a timed assignment (ms): the student's own when Moodle says, else the assignment's. */
export function timeLimitOf(
	config: Pick<SubmissionConfig, 'timeLimit'> | undefined,
	submission: Pick<Submission, 'timeLimit'> | undefined
): number | null {
	return submission?.timeLimit ?? config?.timeLimit ?? null;
}

/**
 * Whether the student must start a timed assignment's clock before working on it: Moodle asks
 * it of a submission not started yet that the student may work on now, and the clock, once
 * started, can't be paused.
 */
export function awaitsStart(
	config: SubmissionConfig | undefined,
	submission: Submission | undefined
): boolean {
	return (
		submission !== undefined &&
		submission.canEdit &&
		submittable(config) &&
		timeLimitOf(config, submission) !== null &&
		submission.state === 'new' &&
		submission.startedAt === null
	);
}

/**
 * When a started timed attempt's time runs out (ms), as Moodle's timer counts it: the start plus
 * the limit, or the due date (else the cut-off) when that comes first. Null for one not started
 * or not timed. Moodle still takes the work after it, marked as over the limit.
 */
export function timedEnd(
	schedule: Schedule,
	submission: Pick<Submission, 'startedAt' | 'timeLimit'>,
	config: Pick<SubmissionConfig, 'timeLimit'> | undefined
): number | null {
	const limit = timeLimitOf(config, submission);
	if (submission.startedAt === null || limit === null) return null;
	const end = submission.startedAt + limit;
	const closes = schedule.due ?? schedule.cutoff;
	return closes === null ? end : Math.min(end, closes);
}

/** Whether nothing is handed in online: on paper, in class, or not at all. */
export function nothingOnline(config: SubmissionConfig | undefined): boolean {
	return config !== undefined && !submittable(config) && !onMoodleOnly(config);
}

const EDITABLE = new Set(['file', 'onlinetext']);
/** Parts that hold nothing to hand in: comments are a conversation, not the work. */
const PASSIVE = new Set(['comments']);
/** Moodle's site default for `maxfilesubmissions`. */
const DEFAULT_MAX_FILES = 20;

/**
 * What the submission takes, from the assignment's settings; the parts the submission lists
 * stand in where an answer leaves the settings out. `uploadLimit` is the site's cap, which applies
 * when the assignment sets none.
 */
export function readConfig(
	assignment: MoodleAssignment,
	plugins: readonly MoodleSubmissionPlugin[],
	uploadLimit: number | null,
	defaultStatement: string
): SubmissionConfig {
	const settings = assignment.configs.filter((config) => config.subtype === 'assignsubmission');
	const setting = (plugin: string, name: string) =>
		settings.find((config) => config.plugin === plugin && config.name === name)?.value;
	const enabled = (plugin: string) =>
		settings.length > 0
			? setting(plugin, 'enabled') === '1'
			: plugins.some((part) => part.type === plugin);

	const wordLimit = Number(setting('onlinetext', 'wordlimit'));
	const maxFiles = Number(setting('file', 'maxfilesubmissions'));
	const maxBytes = Number(setting('file', 'maxsubmissionsizebytes'));
	const types = (setting('file', 'filetypeslist') ?? '')
		.split(/[\s,;]+/)
		.map((type) => type.trim().toLowerCase())
		.filter((type) => type !== '' && type !== '*');

	const others = new Map<string, string>();
	for (const part of plugins) {
		if (!EDITABLE.has(part.type) && !PASSIVE.has(part.type)) others.set(part.type, part.name);
	}
	for (const config of settings) {
		if (
			config.name === 'enabled' &&
			config.value === '1' &&
			!EDITABLE.has(config.plugin) &&
			!PASSIVE.has(config.plugin) &&
			!others.has(config.plugin)
		) {
			others.set(config.plugin, config.plugin);
		}
	}

	const statement = assignment.submissionStatement?.trim();
	return {
		text: enabled('onlinetext')
			? {
					wordLimit:
						setting('onlinetext', 'wordlimitenabled') === '1' && wordLimit > 0 ? wordLimit : null
				}
			: null,
		files: enabled('file')
			? {
					maxFiles: maxFiles > 0 ? maxFiles : DEFAULT_MAX_FILES,
					maxBytes: maxBytes > 0 ? maxBytes : uploadLimit && uploadLimit > 0 ? uploadLimit : null,
					types
				}
			: null,
		unsupported: [...others.values()],
		drafts: assignment.submissionDrafts === 1,
		statement:
			assignment.requireSubmissionStatement === 1
				? statement
					? statement
					: defaultStatement
				: null,
		timeLimit:
			assignment.timeLimit !== null && assignment.timeLimit > 0 ? assignment.timeLimit * 1000 : null
	};
}

export interface Schedule {
	/** When submissions open, if later than the assignment's creation. */
	opens: number | null;
	/** The due date that applies to the student: their extension, if they have one. */
	due: number | null;
	/** The assignment's own due date, when an extension moves it. */
	originalDue: number | null;
	/** The last moment late submissions are accepted; null when they always are. */
	cutoff: number | null;
}

export function readSchedule(assignment: MoodleAssignment, extension?: number | null): Schedule {
	const due = instant(assignment.dueDate);
	const cutoff = instant(assignment.cutoffDate);
	const extended =
		extension !== null && extension !== undefined && (due === null || extension > due);
	return {
		opens: instant(assignment.allowSubmissionsFromDate),
		due: extended ? extension : due,
		originalDue: extended ? due : null,
		cutoff: extended && cutoff !== null && extension > cutoff ? extension : cutoff
	};
}

/**
 * Where the assignment's submission window stands. `canEdit` is Moodle's answer, which knows
 * about locks and overrides; without it, the dates decide.
 */
export type Phase = 'upcoming' | 'open' | 'late' | 'closed';

export function phaseOf(schedule: Schedule, now: number, canEdit?: boolean): Phase {
	if (schedule.opens !== null && schedule.opens > now) return 'upcoming';
	if (canEdit === false) return 'closed';
	if (schedule.due === null || now <= schedule.due) return 'open';
	if (schedule.cutoff !== null && now > schedule.cutoff) return 'closed';
	return 'late';
}

export type Urgency = 'overdue' | 'soon' | 'calm';

/** How long a missed due date stays red when late submissions never close. */
export const OVERDUE_MS = 14 * DAY;

/**
 * How pressing the assignment is for a student who hasn't handed it in: overdue while it can
 * still be handed in late, soon within 48 hours of the due date. Missed and closed is calm, and
 * so is a due date long gone when Moodle takes late work forever (no cut-off): courses keep
 * such assignments open for months, and they shouldn't stay red all that time.
 */
export function urgencyOf(
	schedule: Schedule,
	stage: Stage | undefined,
	now: number,
	canEdit?: boolean
): Urgency {
	if (handedIn(stage) || schedule.due === null) return 'calm';
	const left = schedule.due - now;
	if (left >= 0) return left < DUE_SOON_MS ? 'soon' : 'calm';
	if (phaseOf(schedule, now, canEdit) !== 'late') return 'calm';
	return schedule.cutoff !== null || -left < OVERDUE_MS ? 'overdue' : 'calm';
}

/**
 * Where an assignment stands for the student, in one word: what its chip says and its filters
 * test. `lapsed` is a missed due date that late work is still taken for, long enough ago to be
 * calm (see {@link urgencyOf}); `missed` is closed without a submission.
 */
export type Standing =
	'graded' | 'submitted' | 'draft' | 'none' | 'upcoming' | 'todo' | 'late' | 'lapsed' | 'missed';

export function standingOf(
	schedule: Schedule,
	submission: Submission,
	config: SubmissionConfig | undefined,
	now: number
): Standing {
	if (submission.stage !== 'new') return submission.stage;
	if (nothingOnline(config)) return 'none';
	switch (phaseOf(schedule, now, submission.canEdit)) {
		case 'upcoming':
			return 'upcoming';
		case 'open':
			return 'todo';
		case 'late':
			return urgencyOf(schedule, submission.stage, now, submission.canEdit) === 'overdue'
				? 'late'
				: 'lapsed';
		case 'closed':
			return 'missed';
	}
}

/** Standings that still ask something of the student. */
const PENDING: ReadonlySet<Standing> = new Set(['upcoming', 'todo', 'late', 'draft']);

export type AssignmentFilter = 'all' | 'todo' | 'urgent';

/** What a filter reads of a row; the standing is unknown until the row's status arrives. */
export interface FilterSubject {
	schedule: Schedule;
	standing: Standing | undefined;
	urgency: Urgency;
}

/**
 * Whether a row belongs under `filter`. Until its status is known, the dates guess: an assignment
 * that can still be handed in is to do, and urgent when its deadline is close or passed.
 */
export function matchesFilter(filter: AssignmentFilter, row: FilterSubject, now: number): boolean {
	if (filter === 'all') return true;
	const urgency =
		row.standing === undefined ? urgencyOf(row.schedule, undefined, now) : row.urgency;
	if (filter === 'urgent') return urgency !== 'calm';
	if (row.standing !== undefined) return PENDING.has(row.standing);
	const { due, cutoff } = row.schedule;
	return due === null || due >= now || (cutoff !== null && cutoff > now);
}

/** The deadline still ahead of a pending assignment: the due date, or once past, the cut-off. */
export function deadlineOf(schedule: Schedule, now: number): number | null {
	if (schedule.due === null || schedule.due >= now) return schedule.due;
	return schedule.cutoff;
}

/**
 * The assignment to hand in first: of those still to do (open, late or drafted), the one whose
 * deadline comes soonest. Not-yet-open ones and those without a deadline don't count.
 */
export function nextDue<T extends FilterSubject>(rows: readonly T[], now: number): T | undefined {
	let best: { row: T; at: number } | undefined;
	for (const row of rows) {
		if (row.standing !== 'todo' && row.standing !== 'late' && row.standing !== 'draft') continue;
		const at = deadlineOf(row.schedule, now);
		if (at === null || at < now) continue;
		if (best === undefined || at < best.at) best = { row, at };
	}
	return best?.row;
}

export interface AssignmentGroups {
	/** Due from now on, or still taking late submissions: soonest first. */
	upcoming: MoodleAssignment[];
	undated: MoodleAssignment[];
	/** Most recent first. */
	past: MoodleAssignment[];
}

/** The tab's sections, from the dates alone so rows never move when their status arrives. */
export function groupAssignments(
	assignments: readonly MoodleAssignment[],
	now: number
): AssignmentGroups {
	const groups: AssignmentGroups = { upcoming: [], undated: [], past: [] };
	for (const assignment of assignments) {
		const schedule = readSchedule(assignment);
		if (schedule.due === null) groups.undated.push(assignment);
		else if (schedule.due >= now || (schedule.cutoff !== null && schedule.cutoff > now))
			groups.upcoming.push(assignment);
		else groups.past.push(assignment);
	}
	const collator = new Intl.Collator(intlLocale(), { numeric: true });
	const byName = (a: MoodleAssignment, b: MoodleAssignment) => collator.compare(a.name, b.name);
	groups.upcoming.sort((a, b) => a.dueDate - b.dueDate || byName(a, b));
	groups.undated.sort(
		(a, b) => a.allowSubmissionsFromDate - b.allowSubmissionsFromDate || byName(a, b)
	);
	groups.past.sort((a, b) => b.dueDate - a.dueDate || byName(a, b));
	return groups;
}

export interface Span {
	days: number;
	hours: number;
	minutes: number;
}

/**
 * A stretch of time in its two largest units, as countdowns read it: "2 days 4 hours", "3 hours
 * 20 minutes", "12 minutes". Under a minute it is zero minutes.
 */
export function spanOf(ms: number): Partial<Span> {
	const total = Math.max(0, Math.floor(Math.abs(ms) / 60_000));
	const days = Math.floor(total / (24 * 60));
	const hours = Math.floor((total % (24 * 60)) / 60);
	const minutes = total % 60;
	if (days > 0) return hours > 0 ? { days, hours } : { days };
	if (hours > 0) return minutes > 0 ? { hours, minutes } : { hours };
	return { minutes };
}

/** How long a countdown of `ms` may wait before its text could change. */
export function tickAfter(ms: number): number {
	return Math.abs(ms) > DAY ? 5 * 60_000 : 20_000;
}
