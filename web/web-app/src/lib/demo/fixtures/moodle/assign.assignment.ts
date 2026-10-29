import { days, epochSeconds } from '../../dates.ts';
import {
	DemoCourses,
	DemoElearning,
	DemoStudent,
	type DemoAssignment,
	type DemoCourse
} from '../../persona.ts';
import { json, moodleError, route, type DemoRequest } from '../../router.ts';
import { DEMO_SESSION_PREFIX, demoState } from '../../state.ts';
import { DRAFT_CONTEXT, draftAreas, type DraftFile } from './files.assignment.ts';

/**
 * The student's side of the persona's assignments (`mod_assign_*`): the brief and settings each
 * one has, the submission status, and the writes (start, save, submit for grading, remove), which
 * change the submission in demo state so the pages stay interactive. Settings and briefs mirror
 * the Android demo: "Esercitazione 6" is handed in, "Progetto: interprete" waits for a
 * submission; Statistics' exercise is timed, its clock not started yet.
 */

const SERVER = 'https://elearning.unimib.it/webservice/rest/server.php';
const SITE = 'https://elearning.unimib.it';
const DAY_S = 86_400;

interface DemoFile {
	name: string;
	size: number;
	mime: string;
	modified: Date;
}

interface Settings {
	intro: string;
	attachments: readonly Omit<DemoFile, 'modified'>[];
	/** Shown only while submitting (Moodle's "activity instructions"). */
	activity?: string;
	opensDaysBefore: number;
	/** Days after the due date that late submissions are taken; null for always. */
	cutoffDays: number | null;
	maxAttempts: number;
	files: { max: number; maxBytes: number; types: string } | null;
	text: { wordLimit: number | null } | null;
	drafts: boolean;
	statement: boolean;
	/** What the assignment is marked out of. */
	grade: number;
	/** Seconds the student has once they start; none when untimed. */
	timeLimit?: number;
	/** The submission the student starts the demo with. */
	start: (assignment: DemoAssignment) => Submission;
}

interface Marking {
	value: number;
	feedback: string;
	files: DemoFile[];
	at: Date;
}

interface Submission {
	status: 'new' | 'reopened' | 'draft' | 'submitted';
	/** 0 for the first attempt. */
	attempt?: number;
	created: Date;
	modified: Date;
	/** When the student started a timed assignment's clock. */
	started?: Date;
	/** The online text as Moodle stores it, its own files as `@@PLUGINFILE@@/name`. */
	text: string;
	/** The files the online text embeds. */
	textFiles?: DemoFile[];
	files: DemoFile[];
	grade?: Marking;
	/** The marked attempt the teacher reopened the assignment after. */
	previous?: Marking;
}

const PDF = 'application/pdf';
const SVG = 'image/svg+xml';
const ZIP = 'application/zip';

const fresh = (): Submission => ({
	status: 'new',
	created: days(-3, 18, 12),
	modified: days(-3, 18, 12),
	text: '',
	files: []
});

const key = (course: DemoCourse, index: number) => `${course.code}:${index}`;

const AUTOMATON: DemoFile = {
	name: 'automa.svg',
	size: 3_412,
	mime: SVG,
	modified: days(-2, 21, 5)
};

/**
 * Each assignment's brief and settings, by its course and index in the persona's list: besides
 * the persona's two, a graded "Esercitazione 5" (Sistemi Operativi, 1), an overdue SQL exercise
 * without drafts reopened after a first attempt (Basi di Dati, 0) and a draft report whose online
 * text is formatted and embeds a picture (Linguaggi, 1) show the other states wherever the
 * persona lists them.
 */
const SETTINGS = new Map<string, Settings>([
	[
		key(DemoCourses.operatingSystems, 0),
		{
			intro:
				'<p>Svolgi gli esercizi allegati e carica un unico file PDF con le soluzioni commentate.</p>' +
				'<p>La consegna è individuale. Sono ammesse consegne in ritardo fino a 48 ore dopo la scadenza.</p>',
			attachments: [{ name: 'traccia.pdf', size: 214_300, mime: PDF }],
			opensDaysBefore: 14,
			cutoffDays: 2,
			maxAttempts: 1,
			files: { max: 3, maxBytes: 20 * 1024 * 1024, types: '.pdf,.zip' },
			text: { wordLimit: null },
			drafts: true,
			statement: true,
			grade: 30,
			start: () => ({
				status: 'submitted',
				created: days(-2, 17, 40),
				modified: days(-1, 10, 30),
				text: '',
				files: [
					{ name: 'consegna_rossi.pdf', size: 482_113, mime: PDF, modified: days(-1, 10, 30) }
				]
			})
		}
	],
	[
		key(DemoCourses.languages, 0),
		{
			intro:
				'<p>Realizza in OCaml un interprete per <em>MiniML</em>, il linguaggio funzionale descritto ' +
				'nella traccia: valutazione con ambienti, chiusure e <strong>let rec</strong>.</p>' +
				'<p>Si lavora in gruppi di massimo tre persone; ognuno consegna la propria copia.</p>' +
				'<ul><li>il codice sorgente, in un archivio <code>.zip</code>;</li>' +
				'<li>una relazione di al massimo cinque pagine, in PDF.</li></ul>',
			attachments: [
				{ name: 'traccia_progetto.pdf', size: 356_812, mime: PDF },
				{ name: 'miniml_esempi.zip', size: 18_240, mime: ZIP }
			],
			activity:
				'<p>Carica l’archivio con il codice e la relazione. Nel testo online indica i componenti ' +
				'del gruppo.</p>',
			opensDaysBefore: 30,
			cutoffDays: null,
			maxAttempts: -1,
			files: { max: 2, maxBytes: 50 * 1024 * 1024, types: '.zip,.pdf' },
			text: { wordLimit: 300 },
			drafts: true,
			statement: true,
			grade: 30,
			start: fresh
		}
	],
	[
		key(DemoCourses.operatingSystems, 1),
		{
			intro:
				'<p>Descrivi il ciclo di vita di un processo e confronta i modelli di thread visti a lezione.</p>',
			attachments: [{ name: 'esercitazione5.pdf', size: 188_402, mime: PDF }],
			opensDaysBefore: 14,
			cutoffDays: 2,
			maxAttempts: 1,
			files: { max: 3, maxBytes: 20 * 1024 * 1024, types: '.pdf,.zip' },
			text: null,
			drafts: true,
			statement: true,
			grade: 30,
			start: (assignment) => ({
				status: 'submitted',
				created: new Date(assignment.due.getTime() - 2 * DAY_S * 1000),
				modified: new Date(assignment.due.getTime() - DAY_S * 1000),
				text: '',
				files: [
					{
						name: 'esercitazione5_rossi.pdf',
						size: 391_220,
						mime: PDF,
						modified: new Date(assignment.due.getTime() - DAY_S * 1000)
					}
				],
				grade: {
					value: 27,
					feedback:
						'<p>Buona analisi del ciclo di vita dei processi, chiara e ben argomentata.</p>' +
						'<p>Nella parte sui thread manca il confronto tra i modelli <strong>1:1</strong> e ' +
						'<strong>N:M</strong>: trovi le correzioni nel PDF allegato.</p>',
					files: [
						{
							name: 'correzione_rossi.pdf',
							size: 402_005,
							mime: PDF,
							modified: new Date(assignment.due.getTime() + 5 * DAY_S * 1000)
						}
					],
					at: new Date(assignment.due.getTime() + 5 * DAY_S * 1000)
				}
			})
		}
	],
	[
		key(DemoCourses.databases, 0),
		{
			intro:
				'<p>Scrivi le interrogazioni SQL richieste sullo schema <code>biblioteca</code> della ' +
				'traccia. Ogni interrogazione va commentata.</p>',
			attachments: [{ name: 'schema_biblioteca.sql', size: 4_812, mime: 'application/sql' }],
			opensDaysBefore: 10,
			cutoffDays: 3,
			maxAttempts: -1,
			files: { max: 1, maxBytes: 5 * 1024 * 1024, types: '.sql,.txt,.pdf' },
			text: null,
			drafts: false,
			statement: false,
			grade: 10,
			start: (assignment) => ({
				...fresh(),
				status: 'reopened',
				attempt: 1,
				previous: {
					value: 4,
					feedback:
						'<p>Le interrogazioni 3 e 4 non usano correttamente <code>GROUP BY</code>: rivedile ' +
						'e consegna di nuovo.</p>',
					files: [],
					at: new Date(assignment.due.getTime() - 4 * DAY_S * 1000)
				}
			})
		}
	],
	[
		key(DemoCourses.statistics, 0),
		{
			intro:
				'<p>Risolvi i cinque esercizi sulle variabili aleatorie discrete e continue della traccia. ' +
				'Hai un’ora da quando inizi: carica le soluzioni in un unico PDF, anche scansionate.</p>',
			attachments: [{ name: 'traccia_variabili_aleatorie.pdf', size: 142_870, mime: PDF }],
			opensDaysBefore: 7,
			cutoffDays: null,
			maxAttempts: 1,
			files: { max: 1, maxBytes: 20 * 1024 * 1024, types: '.pdf' },
			text: { wordLimit: null },
			drafts: true,
			statement: false,
			grade: 10,
			timeLimit: 3600,
			start: fresh
		}
	],
	[
		key(DemoCourses.languages, 1),
		{
			intro:
				'<p>Descrivi l’analizzatore lessicale di MiniML: le classi di token, le espressioni ' +
				'regolari che le riconoscono e l’automa che ne risulta.</p>',
			attachments: [],
			opensDaysBefore: 21,
			cutoffDays: null,
			maxAttempts: -1,
			files: { max: 1, maxBytes: 10 * 1024 * 1024, types: '.pdf' },
			text: { wordLimit: 500 },
			drafts: true,
			statement: false,
			grade: 30,
			start: () => ({
				status: 'draft',
				created: days(-3, 18, 40),
				modified: days(-2, 21, 5),
				text:
					'<p>Il lessico di <strong>MiniML</strong> è generato con <code>ocamllex</code> e ' +
					'riconosce tre classi di token:</p><ul><li>parole chiave e identificatori;</li>' +
					'<li>letterali interi e booleani;</li><li>operatori e separatori.</li></ul>' +
					'<p><img src="@@PLUGINFILE@@/automa.svg" alt="Automa del lexer" width="320" ' +
					'height="200"></p>',
				textFiles: [AUTOMATON],
				files: []
			})
		}
	]
]);

/** Settings for assignments the demo describes no further: a file and a text, drafts on. */
const PLAIN: Settings = {
	intro: '',
	attachments: [],
	opensDaysBefore: 14,
	cutoffDays: null,
	maxAttempts: -1,
	files: { max: 1, maxBytes: 20 * 1024 * 1024, types: '' },
	text: { wordLimit: null },
	drafts: true,
	statement: false,
	grade: 30,
	start: fresh
};

const settingsOf = (assignment: DemoAssignment) =>
	SETTINGS.get(key(assignment.course, assignment.index)) ?? PLAIN;

const contextOf = (assignment: DemoAssignment) => 700_000 + assignment.cmId;

function wireFile(file: Omit<DemoFile, 'modified'> & { modified?: Date }, url: string) {
	return {
		filename: file.name,
		filepath: '/',
		filesize: file.size,
		fileurl: url,
		timemodified: epochSeconds(file.modified ?? days(-20, 9)),
		mimetype: file.mime,
		isexternalfile: false
	};
}

function introFiles(assignment: DemoAssignment) {
	return settingsOf(assignment).attachments.map((file) =>
		wireFile(
			file,
			`${SITE}/webservice/pluginfile.php/${contextOf(assignment)}/mod_assign/introattachment/0/${encodeURIComponent(file.name)}`
		)
	);
}

function cutoffOf(assignment: DemoAssignment): Date | null {
	const { cutoffDays } = settingsOf(assignment);
	return cutoffDays === null
		? null
		: new Date(assignment.due.getTime() + cutoffDays * DAY_S * 1000);
}

/** The statement Moodle's language pack words, in the app's language. */
function statementText(english: boolean): string {
	return english
		? 'This submission is my own work, except where I have acknowledged the use of the works of other people.'
		: 'Il compito è un mio lavoro originale, ad eccezione delle parti dove ho riconosciuto l’utilizzo di lavori altrui.';
}

/**
 * An assignment as `mod_assign_get_assignments` lists it, with its brief and its submission
 * settings: what the assignment pages show and the submission editor obeys. The route listing
 * the persona's assignments builds each one with it.
 */
export function assignmentRecord(assignment: DemoAssignment, english: boolean) {
	const settings = settingsOf(assignment);
	const cutoff = cutoffOf(assignment);
	const configs = [
		...(settings.files
			? [
					['file', 'enabled', '1'],
					['file', 'maxfilesubmissions', String(settings.files.max)],
					['file', 'maxsubmissionsizebytes', String(settings.files.maxBytes)],
					['file', 'filetypeslist', settings.files.types]
				]
			: []),
		...(settings.text
			? [
					['onlinetext', 'enabled', '1'],
					['onlinetext', 'wordlimit', String(settings.text.wordLimit ?? 0)],
					['onlinetext', 'wordlimitenabled', settings.text.wordLimit ? '1' : '0']
				]
			: []),
		['comments', 'enabled', '1']
	].map(([plugin, name, value]) => ({
		plugin,
		subtype: 'assignsubmission',
		name,
		value
	}));
	return {
		id: assignment.id,
		cmid: assignment.cmId,
		course: assignment.course.moodleCourseId,
		name: assignment.title,
		nosubmissions: 0,
		submissiondrafts: settings.drafts ? 1 : 0,
		sendnotifications: 0,
		sendlatenotifications: 0,
		sendstudentnotifications: 1,
		duedate: epochSeconds(assignment.due),
		allowsubmissionsfromdate: epochSeconds(
			new Date(assignment.due.getTime() - settings.opensDaysBefore * DAY_S * 1000)
		),
		grade: settings.grade,
		timemodified: epochSeconds(days(-15)),
		completionsubmit: 1,
		cutoffdate: cutoff === null ? 0 : epochSeconds(cutoff),
		gradingduedate: epochSeconds(new Date(assignment.due.getTime() + 10 * DAY_S * 1000)),
		teamsubmission: 0,
		requireallteammemberssubmit: 0,
		teamsubmissiongroupingid: 0,
		blindmarking: 0,
		hidegrader: 0,
		revealidentities: 0,
		attemptreopenmethod: 'untilpass',
		maxattempts: settings.maxAttempts,
		markingworkflow: 0,
		markingallocation: 0,
		markinganonymous: 0,
		requiresubmissionstatement: settings.statement ? 1 : 0,
		preventsubmissionnotingroup: 0,
		...(settings.statement
			? { submissionstatement: statementText(english), submissionstatementformat: 1 }
			: {}),
		configs,
		intro: settings.intro,
		introformat: 1,
		introfiles: [],
		introattachments: introFiles(assignment),
		...(settings.activity ? { activity: settings.activity, activityformat: 1 } : {}),
		timelimit: settings.timeLimit ?? 0,
		submissionattachments: 0
	};
}

/*
 * Moodle keeps submissions across page loads, so the demo keeps them for the tab's session: a
 * reload finds a started clock or a saved draft as it was left.
 */
const STORAGE_KEY = `${DEMO_SESSION_PREFIX}assign-submissions`;
/** The dates in a stored submission, as `JSON.stringify` writes them. */
const STORED_DATE = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/;

function restore(): Map<number, Submission> | undefined {
	try {
		const raw = sessionStorage.getItem(STORAGE_KEY);
		if (raw === null) return undefined;
		const entries = JSON.parse(raw, (_, value: unknown) =>
			typeof value === 'string' && STORED_DATE.test(value) ? new Date(value) : value
		) as [number, Submission][];
		return new Map(entries);
	} catch {
		return undefined;
	}
}

/** The student's submissions, by assignment id, started on first use. */
const submissions = () =>
	demoState('moodle.assign.submissions', () => restore() ?? new Map<number, Submission>());

/** Keeps a changed submission, for the session as for this page. */
function store(assignment: DemoAssignment, submission: Submission): void {
	const all = submissions();
	all.set(assignment.id, submission);
	try {
		sessionStorage.setItem(STORAGE_KEY, JSON.stringify([...all]));
	} catch {
		// Without storage the demo forgets its submissions on reload, nothing worse.
	}
}

function submissionOf(assignment: DemoAssignment): Submission {
	const all = submissions();
	let submission = all.get(assignment.id);
	if (submission === undefined) {
		submission = settingsOf(assignment).start(assignment);
		all.set(assignment.id, submission);
	}
	return submission;
}

/** Whether Moodle would let the student change the submission now. */
function canEdit(assignment: DemoAssignment, submission: Submission, now = Date.now()): boolean {
	const settings = settingsOf(assignment);
	const opens = assignment.due.getTime() - settings.opensDaysBefore * DAY_S * 1000;
	const cutoff = cutoffOf(assignment);
	if (submission.grade !== undefined || now < opens) return false;
	if (cutoff !== null && now > cutoff.getTime()) return false;
	return !(settings.drafts && submission.status === 'submitted');
}

function submissionFiles(assignment: DemoAssignment, submission: Submission) {
	return submission.files.map((file) =>
		wireFile(
			file,
			`${SITE}/webservice/pluginfile.php/${contextOf(assignment)}/assignsubmission_file/submission_files/${assignment.id}/${encodeURIComponent(file.name)}`
		)
	);
}

/** Where the web service shows the online text's own files. */
const textArea = (assignment: DemoAssignment) =>
	`${SITE}/webservice/pluginfile.php/${contextOf(assignment)}/assignsubmission_onlinetext/submissions_onlinetext/${500_000 + assignment.id}`;

function feedbackPlugins(assignment: DemoAssignment, marking: Marking, english: boolean) {
	return [
		{
			type: 'comments',
			name: english ? 'Feedback comments' : 'Commenti all’elaborato',
			editorfields: [
				{
					name: 'comments',
					description: english ? 'Feedback comments' : 'Commenti all’elaborato',
					text: marking.feedback,
					format: 1
				}
			]
		},
		{
			type: 'file',
			name: english ? 'Feedback files' : 'File di commento',
			fileareas: [
				{
					area: 'feedback_files',
					files: marking.files.map((file) =>
						wireFile(
							file,
							`${SITE}/webservice/pluginfile.php/${contextOf(assignment)}/assignfeedback_file/feedback_files/${600_000 + assignment.id}/${encodeURIComponent(file.name)}`
						)
					)
				}
			]
		}
	];
}

function gradeRecord(assignment: DemoAssignment, marking: Marking, attempt: number) {
	return {
		id: 600_000 + assignment.id + attempt,
		assignment: assignment.id,
		userid: DemoStudent.moodle.userId,
		attemptnumber: attempt,
		timecreated: epochSeconds(marking.at),
		timemodified: epochSeconds(marking.at),
		grader: 30_000 + assignment.course.moodleCourseId,
		grade: marking.value.toFixed(5)
	};
}

function status(assignment: DemoAssignment, english: boolean) {
	const settings = settingsOf(assignment);
	const submission = submissionOf(assignment);
	const editable = canEdit(assignment, submission);
	const graded = submission.grade;
	const attempt = submission.attempt ?? 0;
	const plugins = [
		...(settings.files
			? [
					{
						type: 'file',
						name: english ? 'File submissions' : 'Consegna file',
						fileareas: [
							{ area: 'submission_files', files: submissionFiles(assignment, submission) }
						]
					}
				]
			: []),
		...(settings.text
			? [
					{
						type: 'onlinetext',
						name: english ? 'Online text' : 'Testo online',
						fileareas: [
							{
								area: 'submissions_onlinetext',
								files: (submission.textFiles ?? []).map((file) =>
									wireFile(file, `${textArea(assignment)}/${encodeURIComponent(file.name)}`)
								)
							}
						],
						editorfields: [
							{
								name: 'onlinetext',
								description: english ? 'Online text submissions' : 'Consegne testo online',
								text: submission.text.replaceAll('@@PLUGINFILE@@', textArea(assignment)),
								format: 1
							}
						]
					}
				]
			: []),
		{ type: 'comments', name: english ? 'Submission comments' : 'Commenti alle consegne' }
	];
	return {
		lastattempt: {
			submission: {
				id: 500_000 + assignment.id,
				userid: DemoStudent.moodle.userId,
				attemptnumber: attempt,
				timecreated: epochSeconds(submission.created),
				timemodified: epochSeconds(submission.modified),
				...(submission.started ? { timestarted: epochSeconds(submission.started) } : {}),
				status: submission.status,
				groupid: 0,
				assignment: assignment.id,
				latest: 1,
				plugins,
				gradingstatus: graded ? 'graded' : 'notgraded'
			},
			submissiongroupmemberswhoneedtosubmit: [],
			submissionsenabled: true,
			locked: false,
			graded: graded !== undefined,
			canedit: editable,
			caneditowner: editable,
			cansubmit: editable && settings.drafts && submission.status === 'draft',
			extensionduedate: null,
			timelimit: settings.timeLimit ?? 0,
			blindmarking: false,
			gradingstatus: graded ? 'graded' : 'notgraded',
			usergroups: []
		},
		...(graded
			? {
					feedback: {
						grade: gradeRecord(assignment, graded, attempt),
						gradefordisplay: `${graded.value},00&nbsp;/&nbsp;${settings.grade},00`,
						gradeddate: epochSeconds(graded.at),
						plugins: feedbackPlugins(assignment, graded, english)
					}
				}
			: {}),
		previousattempts: submission.previous
			? [
					{
						attemptnumber: attempt - 1,
						submission: {
							id: 499_000 + assignment.id,
							userid: DemoStudent.moodle.userId,
							attemptnumber: attempt - 1,
							timecreated: epochSeconds(submission.previous.at),
							timemodified: epochSeconds(submission.previous.at),
							status: 'submitted',
							groupid: 0,
							assignment: assignment.id,
							latest: 0,
							plugins: []
						},
						grade: gradeRecord(assignment, submission.previous, attempt - 1),
						feedbackplugins: feedbackPlugins(assignment, submission.previous, english)
					}
				]
			: [],
		assignmentdata: {
			attachments: { intro: introFiles(assignment), activity: [] },
			...(settings.activity ? { activity: settings.activity, activityformat: 1 } : {})
		},
		warnings: []
	};
}

const isEnglish = (request: DemoRequest) =>
	request.url.searchParams.get('moodlewssettinglang') === 'en';

const missing = () =>
	moodleError('invalidrecord', 'Impossibile trovare il record nella tabella assign del database.');

/** Moodle's refusal of a write: a warning on a successful answer. */
function warning(assignment: DemoAssignment, code: string, message: string) {
	return json([{ item: assignment.title, itemid: assignment.id, warningcode: code, message }]);
}

function notice(english: boolean, it: string, en: string): string {
	return english ? en : it;
}

/** The persona's assignment a request names in `field`. */
async function assignmentFor(
	request: DemoRequest,
	field: string
): Promise<DemoAssignment | undefined> {
	const id = Number((await request.form()).get(field));
	return DemoElearning.assignments().find((assignment) => assignment.id === id);
}

/** The online text a save sent; `invalid` when it leaves out a key Moodle requires. */
function sentEditor(
	form: URLSearchParams
): { text: string; format: number; itemId: number } | 'invalid' | undefined {
	const field = (name: string) => form.get(`plugindata[onlinetext_editor][${name}]`);
	const [text, format, itemId] = [field('text'), field('format'), field('itemid')];
	if (text === null && format === null && itemId === null) return undefined;
	if (text === null || format === null || itemId === null) return 'invalid';
	return { text, format: Number(format), itemId: Number(itemId) };
}

/** Whether Moodle counts `html` as empty: no words, no picture or media. */
function blankText(html: string): boolean {
	if (/<(img|video|audio)\b/i.test(html)) return false;
	return (
		html
			.replace(/<[^>]*>/g, ' ')
			.replace(/&nbsp;|&#160;/g, ' ')
			.trim() === ''
	);
}

function savedFile(file: DraftFile): DemoFile {
	return {
		name: file.name,
		size: file.size,
		mime: file.mime || 'application/octet-stream',
		modified: file.uploadedAt
	};
}

/**
 * The online text and its files after a save, as Moodle's `file_postupdate_standard_editor`
 * makes them. Left out, the text reads as null: Moodle erases it. With item id 0 or -1
 * (IGNORE_FILE_MERGE) the text is stored as sent and its file area kept. With a draft area, the
 * draft's files the text doesn't link by draft address are deleted first, the rest replace the
 * area, and their addresses become placeholders.
 */
function savedText(
	submission: Submission,
	editor: { text: string; itemId: number } | undefined
): Pick<Submission, 'text' | 'textFiles'> {
	if (editor === undefined) return { text: '', textFiles: submission.textFiles };
	if (editor.itemId <= 0) return { text: editor.text, textFiles: submission.textFiles };
	const base = `${SITE}/draftfile.php/${DRAFT_CONTEXT}/user/draft/${editor.itemId}/`;
	const linked = (draftAreas().get(editor.itemId) ?? []).filter(
		(file) =>
			editor.text.includes(base + encodeURIComponent(file.name)) ||
			editor.text.includes(base + file.name)
	);
	return {
		text: editor.text.replaceAll(base, '@@PLUGINFILE@@/'),
		textFiles: linked.map(savedFile)
	};
}

export const routes = [
	route(
		`POST ${SERVER}?wsfunction=mod_assign_get_submission_status`,
		async (request) => {
			const assignment = await assignmentFor(request, 'assignid');
			return assignment === undefined ? missing() : json(status(assignment, isEnglish(request)));
		},
		{
			empty: async (request) => {
				const assignment = await assignmentFor(request, 'assignid');
				if (assignment === undefined) return missing();
				const answer = status(assignment, isEnglish(request));
				return json({ ...answer, lastattempt: { ...answer.lastattempt, submission: null } });
			}
		}
	),
	route(`POST ${SERVER}?wsfunction=mod_assign_save_submission`, async (request) => {
		const assignment = await assignmentFor(request, 'assignmentid');
		if (assignment === undefined) return missing();
		const english = isEnglish(request);
		const form = await request.form();
		const submission = submissionOf(assignment);
		const settings = settingsOf(assignment);
		if (!canEdit(assignment, submission)) {
			return warning(
				assignment,
				'couldnotsavesubmission',
				notice(
					english,
					'Non è più possibile modificare la consegna.',
					'This submission can no longer be changed.'
				)
			);
		}
		const area = form.get('plugindata[files_filemanager]');
		const editor = sentEditor(form);
		if (editor === 'invalid') {
			return moodleError(
				'invalidparameter',
				'Valore non valido per il parametro (Missing required key in single structure)'
			);
		}
		const draft = area === null ? [] : (draftAreas().get(Number(area)) ?? []);
		const sentText = settings.text ? editor : undefined;
		const nothing = warning(
			assignment,
			'couldnotsavesubmission',
			notice(english, 'Non hai consegnato nulla.', 'Nothing was submitted.')
		);
		if (draft.length === 0 && (sentText === undefined || blankText(sentText.text))) return nothing;
		const files = settings.files && area !== null ? draft.map(savedFile) : submission.files;
		const text = settings.text
			? savedText(submission, sentText)
			: { text: submission.text, textFiles: submission.textFiles };
		if (files.length === 0 && blankText(text.text)) return nothing;
		store(assignment, {
			...submission,
			status: settings.drafts ? 'draft' : 'submitted',
			modified: new Date(),
			files,
			...text
		});
		return json([]);
	}),
	route(`POST ${SERVER}?wsfunction=mod_assign_start_submission`, async (request) => {
		const assignment = await assignmentFor(request, 'assignid');
		if (assignment === undefined) return missing();
		const english = isEnglish(request);
		const submission = submissionOf(assignment);
		const refuse = (code: string, it: string, en: string) =>
			json({
				submissionid: 0,
				warnings: [
					{
						item: 'assign',
						itemid: assignment.id,
						warningcode: code,
						message: notice(english, it, en)
					}
				]
			});
		if (!canEdit(assignment, submission)) {
			return refuse(
				'submissionnotopen',
				'Questo compito non accetta consegne.',
				'This assignment is not open for submissions'
			);
		}
		if (!settingsOf(assignment).timeLimit) {
			return refuse(
				'timelimitnotenabled',
				'Il tempo limite non è attivo per questo compito.',
				'Time limit is not enabled for assignment.'
			);
		}
		if (submission.started && submission.status !== 'submitted') {
			return refuse(
				'opensubmissionexists',
				'Esiste già una consegna aperta.',
				'Open assignment submission already exists.'
			);
		}
		store(assignment, { ...submission, started: new Date() });
		return json({ submissionid: 500_000 + assignment.id, warnings: [] });
	}),
	route(`POST ${SERVER}?wsfunction=mod_assign_submit_for_grading`, async (request) => {
		const assignment = await assignmentFor(request, 'assignmentid');
		if (assignment === undefined) return missing();
		const english = isEnglish(request);
		const accepted = (await request.form()).get('acceptsubmissionstatement') === '1';
		const submission = submissionOf(assignment);
		if (submission.status !== 'draft' || !canEdit(assignment, submission)) {
			return warning(
				assignment,
				'couldnotsubmitforgrading',
				notice(
					english,
					'Non c’è una bozza da inviare per la valutazione.',
					'There is no draft to submit for grading.'
				)
			);
		}
		if (settingsOf(assignment).statement && !accepted) {
			return warning(
				assignment,
				'couldnotsubmitforgrading',
				notice(
					english,
					'Devi accettare la dichiarazione per inviare la consegna.',
					'You must accept the submission statement.'
				)
			);
		}
		store(assignment, {
			...submission,
			status: 'submitted',
			modified: new Date()
		});
		return json([]);
	}),
	route(`POST ${SERVER}?wsfunction=mod_assign_remove_submission`, async (request) => {
		const assignment = await assignmentFor(request, 'assignid');
		if (assignment === undefined) return missing();
		const form = await request.form();
		const submission = submissionOf(assignment);
		if (Number(form.get('userid')) !== DemoStudent.moodle.userId) {
			return moodleError('nopermissions', 'Non hai i permessi per eseguire questa operazione.');
		}
		if (
			!canEdit(assignment, submission) ||
			submission.status === 'new' ||
			submission.status === 'reopened'
		) {
			return json({
				status: false,
				warnings: [
					{
						item: assignment.title,
						itemid: assignment.id,
						warningcode: 'couldnotremovesubmission',
						message: notice(
							isEnglish(request),
							'Non c’è una consegna da rimuovere.',
							'There is no submission to remove.'
						)
					}
				]
			});
		}
		store(assignment, {
			...submission,
			status: 'new',
			modified: new Date(),
			files: [],
			text: '',
			textFiles: []
		});
		return json({ status: true, warnings: [] });
	})
];
