import { days, epochSeconds } from '../../dates.ts';
import { DemoCourses, DemoElearning, DemoStudent, type DemoCourse } from '../../persona.ts';
import { json, moodleError, route, type DemoRequest } from '../../router.ts';
import { demoState } from '../../state.ts';
import { demoCompletion } from './course.course.ts';
import { REVIEW } from './quiz.elearning.ts';
import {
	demoBank,
	demoLayout,
	demoPages,
	type DemoGrade,
	type DemoQuestion,
	type DemoResponse
} from './quiz.quiz.questions.ts';

/**
 * The persona's attempts at each course's self-assessment quiz (`mod_quiz_*` but the quiz list,
 * which `quiz.elearning.ts` answers): Algorithms has two graded attempts, Databases one running
 * against the clock, Statistics and Programming finished ones, the rest none. Starting, saving
 * and submitting change them, and a submitted quiz counts as completed on the course page. Each
 * quiz words a band of grades with the teacher's overall feedback.
 */

const SERVER = 'https://elearning.unimib.it/webservice/rest/server.php';
/** As the quiz list says: every persona quiz runs 30 minutes and is out of 10. */
const TIME_LIMIT = 1800;
const GRADE = 10;
/** How long Moodle still takes answers once time is up, before its cron closes the attempt. */
const GRACE_SECONDS = 60;
/**
 * How long an overdue attempt (a quiz with a grace period whose time ran out) waits to be
 * submitted before Moodle abandons it. The persona's quizzes submit on time, so an attempt is
 * overdue only when a test stores one so.
 */
const OVERDUE_SECONDS = 86_400;

type AttemptState = 'inprogress' | 'overdue' | 'finished' | 'abandoned';

interface ManualGrade {
	fraction: number;
	comment: string;
}

interface DemoAttempt {
	id: number;
	course: DemoCourse;
	number: number;
	state: AttemptState;
	timeStart: number;
	timeFinish: number;
	currentPage: number;
	/** By slot - 1. */
	responses: DemoResponse[];
	flagged: Set<number>;
	/** Moodle's sequence check per slot: real steps taken, which autosaves don't add to. */
	sequence: number[];
	/** Essays the teacher graded. */
	manual: Map<number, ManualGrade>;
}

const now = () => epochSeconds(new Date());
const quizIdOf = (course: DemoCourse) => DemoElearning.quizId(course);
const usageOf = (attempt: DemoAttempt) => 7000 + attempt.id;
const prefixOf = (attempt: DemoAttempt, slot: number) => `q${usageOf(attempt)}:${slot}_`;

function attempt(
	course: DemoCourse,
	number: number,
	state: AttemptState,
	start: Date,
	minutes: number,
	responses: DemoResponse[],
	manual: [number, ManualGrade][] = []
): DemoAttempt {
	const bank = demoBank(course);
	const timeStart = epochSeconds(start);
	return {
		id: quizIdOf(course) * 10 + number,
		course,
		number,
		state,
		timeStart,
		timeFinish: state === 'finished' ? timeStart + minutes * 60 : 0,
		currentPage: 0,
		responses: bank.map((_, index) => ({ ...responses[index] })),
		flagged: new Set(),
		sequence: bank.map((_, index) => (responses[index] === undefined ? 1 : 2)),
		manual: new Map(manual)
	};
}

function initialAttempts(): DemoAttempt[] {
	const { algorithms, databases, statistics, programming } = DemoCourses;
	const running = attempt(
		databases,
		1,
		'inprogress',
		new Date(Date.now() - (TIME_LIMIT - 8 * 60) * 1000),
		0,
		[{ answer: '1' }, { choice0: '1', choice1: '0', choice2: '1' }]
	);
	running.currentPage = 2;
	running.flagged.add(2);
	return [
		attempt(
			algorithms,
			1,
			'finished',
			days(-6, 15, 10),
			18,
			[
				{},
				{ answer: '0' },
				{ choice0: '1', choice2: '1' },
				{ answer: '1' },
				{ answer: 'pila' },
				{ answer: '10' },
				{ sub1_answer: '1', sub2_answer: 'O(n)' },
				{ sub0: '1', sub1: '2', sub2: '3' },
				{ p1: '1', p2: '3' },
				{ answer: '<p>Perché salva i risultati.</p>' },
				{ p1: '2', p2: '1' }
			],
			[
				[
					10,
					{
						fraction: 0.5,
						comment: '<p>Giusto, ma spiega anche perché i sottoproblemi si ripetono.</p>'
					}
				]
			]
		),
		attempt(
			algorithms,
			2,
			'finished',
			days(-2, 21, 40),
			24,
			[
				{},
				{ answer: '1' },
				{ choice0: '1', choice1: '1' },
				{ answer: '1' },
				{ answer: 'coda' },
				{ answer: '11' },
				{ sub1_answer: '1', sub2_answer: 'lineare' },
				{ sub0: '2', sub1: '1', sub2: '3' },
				{ p1: '1', p2: '1' },
				{
					answer:
						'<p>Ogni valore viene calcolato una sola volta e riusato, invece di ricalcolare gli stessi sottoproblemi un numero esponenziale di volte.</p>'
				},
				{ p1: '1', p2: '0' }
			],
			[[10, { fraction: 1, comment: '<p>Ottima spiegazione.</p>' }]]
		),
		running,
		attempt(statistics, 1, 'finished', days(-5, 11, 5), 21, [
			{ answer: '5' },
			{ answer: '0' },
			{ answer: '1' },
			{ answer: 'errore di primo tipo' },
			{ choice0: '1', choice2: '1' }
		]),
		attempt(programming, 1, 'finished', days(-6, 18), 15, [
			{ answer: '1' },
			{ answer: '0' },
			{ answer: '7' },
			{ sub1_answer: 'ricorsiva', sub2_answer: '0' },
			{ choice0: '1' }
		]),
		attempt(programming, 2, 'finished', days(-3, 9, 30), 12, [
			{ answer: '0' },
			{ answer: '0' },
			{ answer: '7' },
			{ sub1_answer: 'ricorsiva', sub2_answer: '0' },
			{ choice0: '1', choice1: '1' }
		])
	];
}

/*
 * Moodle keeps attempts across page loads, so the demo keeps them for the tab's session: a
 * reload resumes an attempt where it was, as it would live.
 */
const STORAGE_KEY = 'mybicocca:demo-quiz-attempts';

interface StoredAttempt extends Omit<DemoAttempt, 'course' | 'flagged' | 'manual'> {
	course: string;
	flagged: number[];
	manual: [number, ManualGrade][];
}

function restore(): DemoAttempt[] | undefined {
	try {
		const raw = sessionStorage.getItem(STORAGE_KEY);
		if (raw === null) return undefined;
		return (JSON.parse(raw) as StoredAttempt[]).flatMap((item) => {
			const course = DemoCourses.all.find((candidate) => candidate.code === item.course);
			return course === undefined
				? []
				: [{ ...item, course, flagged: new Set(item.flagged), manual: new Map(item.manual) }];
		});
	} catch {
		return undefined;
	}
}

function persist(): void {
	const stored: StoredAttempt[] = attempts().map((item) => ({
		...item,
		course: item.course.code,
		flagged: [...item.flagged],
		manual: [...item.manual]
	}));
	try {
		sessionStorage.setItem(STORAGE_KEY, JSON.stringify(stored));
	} catch {
		// Without storage the demo forgets its attempts on reload, nothing worse.
	}
}

function attempts(): DemoAttempt[] {
	const all = demoState('moodle.quiz.attempts', () => {
		const list = restore() ?? initialAttempts();
		for (const item of list) if (item.state === 'finished') complete(item);
		return list;
	});
	// Moodle's cron closes attempts whose time ran out: the quiz submits them as they are, and
	// abandons overdue ones never submitted.
	for (const item of all) {
		if (item.state === 'inprogress' && now() > deadlineOf(item) + GRACE_SECONDS) {
			finish(item, deadlineOf(item));
		} else if (item.state === 'overdue' && now() > deadlineOf(item) + OVERDUE_SECONDS) {
			item.state = 'abandoned';
		}
	}
	return all;
}

function deadlineOf(item: DemoAttempt): number {
	const closes = DemoElearning.quizClosesAt(item.course);
	const end = item.timeStart + TIME_LIMIT;
	return closes === null ? end : Math.min(end, epochSeconds(closes));
}

function complete(item: DemoAttempt): void {
	demoCompletion().set(DemoElearning.cmId(item.course, DemoElearning.QUIZ), item.timeFinish);
}

function finish(item: DemoAttempt, at = now()): void {
	item.state = 'finished';
	item.timeFinish = at;
	item.sequence = item.sequence.map((value) => value + 1);
	complete(item);
}

function courseOfQuiz(quizId: number): DemoCourse | undefined {
	return DemoCourses.all.find((course) => quizIdOf(course) === quizId);
}

/** The slot's outcome: a teacher's grade for an essay, else the automatic one. */
function outcomeOf(item: DemoAttempt, question: DemoQuestion, slot: number): DemoGrade {
	const graded = question.grade(item.responses[slot - 1]);
	const manual = item.manual.get(slot);
	return graded === 'manual' && manual ? { fraction: manual.fraction } : graded;
}

/** Raw marks of a finished attempt; null while a teacher still has to grade something. */
function sumGrades(item: DemoAttempt): number | null {
	if (item.state !== 'finished') return null;
	let sum = 0;
	for (const [index, question] of demoBank(item.course).entries()) {
		const outcome = outcomeOf(item, question, index + 1);
		if (outcome === 'manual') return null;
		if (outcome !== 'unanswered') sum += outcome.fraction * question.maxMark;
	}
	return Math.round(sum * 100) / 100;
}

const italian = (value: number) => value.toFixed(2).replace('.', ',');

function wireAttempt(item: DemoAttempt) {
	const bank = demoBank(item.course);
	const open = item.state === 'inprogress' || item.state === 'overdue';
	return {
		id: item.id,
		quiz: quizIdOf(item.course),
		userid: DemoStudent.moodle.userId,
		attempt: item.number,
		uniqueid: usageOf(item),
		layout: demoLayout(bank),
		currentpage: item.currentPage,
		preview: 0,
		state: item.state,
		timestart: item.timeStart,
		timefinish: item.timeFinish,
		timemodified: item.timeFinish || item.timeStart,
		timemodifiedoffline: 0,
		timecheckstate: open
			? deadlineOf(item) + (item.state === 'overdue' ? OVERDUE_SECONDS : 0)
			: null,
		sumgrades: sumGrades(item),
		gradednotificationsenttime: null
	};
}

const STATUS: Record<string, string> = {
	todo: 'Risposta non ancora data',
	complete: 'Risposta salvata',
	invalid: 'Risposta incompleta',
	gradedright: 'Risposta corretta',
	gradedpartial: 'Parzialmente corretta',
	gradedwrong: 'Risposta errata',
	mangrright: 'Risposta corretta',
	mangrpartial: 'Parzialmente corretta',
	mangrwrong: 'Risposta errata',
	gaveup: 'Risposta non data',
	needsgrading: 'Da valutare',
	finished: 'Completo'
};

function reviewState(question: DemoQuestion, outcome: DemoGrade, manual: boolean): string {
	if (question.type === 'description') return 'finished';
	if (outcome === 'unanswered') return 'gaveup';
	if (outcome === 'manual') return 'needsgrading';
	const kind = outcome.fraction >= 1 ? 'right' : outcome.fraction > 0 ? 'partial' : 'wrong';
	return `${manual ? 'mangr' : 'graded'}${kind}`;
}

/** A question as `get_attempt_data` (review false) and `get_attempt_review` send it. */
function wireQuestion(
	item: DemoAttempt,
	slot: number,
	page: number,
	review: boolean,
	html: boolean
) {
	const bank = demoBank(item.course);
	const question = bank[slot - 1];
	const prefix = prefixOf(item, slot);
	const numbered = bank.slice(0, slot).filter((entry) => entry.type !== 'description').length;
	const number = question.type === 'description' ? null : numbered;
	const outcome = review ? outcomeOf(item, question, slot) : null;
	const manual = item.manual.get(slot);
	const state = outcome
		? reviewState(question, outcome, manual !== undefined)
		: question.type === 'description'
			? 'todo'
			: question.answered(item.responses[slot - 1]);
	const flagged = item.flagged.has(slot);
	const { formulation, outcome: feedback } = question.render({
		prefix,
		sequence: item.sequence[slot - 1],
		response: item.responses[slot - 1],
		review: outcome
	});
	const comment = manual ? `<div class="comment">${manual.comment}</div>` : '';
	const mark =
		outcome !== null && outcome !== 'manual' && question.maxMark > 0
			? italian(outcome === 'unanswered' ? 0 : outcome.fraction * question.maxMark)
			: undefined;
	const markup = `<div id="question-${usageOf(item)}-${slot}" class="que ${question.type} deferredfeedback ${state}"><div class="info"><h3 class="no">${number === null ? 'Informazione' : `Domanda <span class="qno">${number}</span>`}</h3><div class="state">${STATUS[state] ?? ''}</div><div class="grade">Punteggio max.: ${italian(question.maxMark)}</div><div class="questionflag editable"><input type="hidden" name="${prefix}:flagged" value="${flagged ? 1 : 0}"><input type="checkbox" id="${prefix}:flaggedcheckbox" name="${prefix}:flagged" value="1"${flagged ? ' checked="checked"' : ''}><label for="${prefix}:flaggedcheckbox">Contrassegna domanda</label></div></div><div class="content"><div class="formulation clearfix"><h4 class="accesshide">Testo della domanda</h4><input type="hidden" name="${prefix}:sequencecheck" value="${item.sequence[slot - 1]}">${formulation}</div>${feedback}${comment}</div></div>`;
	return {
		slot,
		type: question.type,
		page,
		questionnumber: number === null ? 'i' : String(number),
		...(number === null ? {} : { number }),
		html: html ? markup : '',
		responsefileareas: [],
		sequencecheck: item.sequence[slot - 1],
		lastactiontime: item.timeStart,
		hasautosavedstep: false,
		flagged,
		state,
		status: STATUS[state] ?? '',
		blockedbyprevious: false,
		...(mark === undefined ? {} : { mark }),
		maxmark: question.maxMark,
		settings: '{}'
	};
}

function questionsOf(item: DemoAttempt, review: boolean, html: boolean, onlyPage?: number) {
	return demoPages(demoBank(item.course)).flatMap((slots, page) =>
		onlyPage === undefined || onlyPage === page
			? slots.map((slot) => wireQuestion(item, slot, page, review, html))
			: []
	);
}

async function attemptOf(request: DemoRequest): Promise<DemoAttempt | undefined> {
	const id = Number((await request.form()).get('attemptid'));
	return attempts().find((item) => item.id === id);
}

/** The teacher's overall feedback, by the band of grades (out of 10) it covers. */
const FEEDBACK: readonly { from: number; text: string }[] = [
	{
		from: 0,
		text: '<p>Rivedi le lezioni e gli esercizi svolti in aula prima di riprovare: ci sono ancora lacune sugli argomenti di base.</p>'
	},
	{
		from: 6,
		text: '<p>Buon lavoro: sei sulla strada giusta. Ripassa le domande sbagliate con la revisione del tentativo.</p>'
	},
	{
		from: 8.5,
		text: '<p><strong>Ottimo!</strong> Hai una preparazione solida su questa parte del corso.</p>'
	}
];

function feedbackFor(grade: number): string {
	return FEEDBACK.findLast((band) => Math.max(grade, 0) >= band.from)?.text ?? '';
}

/** Moodle's review times, as the bits of a quiz's review settings. */
const DURING = 0x10000;
const IMMEDIATELY_AFTER = 0x01000;
const LATER_WHILE_OPEN = 0x00100;
const AFTER_CLOSE = 0x00010;

/** The review time an attempt is in now, as Moodle's `quiz_attempt_state` tells them apart. */
function reviewTimeOf(item: DemoAttempt): number {
	if (item.state === 'inprogress' || item.state === 'overdue') return DURING;
	const closes = DemoElearning.quizClosesAt(item.course);
	if (closes !== null && now() >= epochSeconds(closes)) return AFTER_CLOSE;
	return now() < item.timeFinish + 120 ? IMMEDIATELY_AFTER : LATER_WHILE_OPEN;
}

/**
 * `mod_quiz_get_combined_review_options` as Moodle's `quiz_get_combined_reviewoptions` works it
 * out: each option on for some of the student's attempts, and on for all of them; none without
 * attempts.
 */
function combinedReview(own: readonly DemoAttempt[]) {
	const fields = {
		feedback: REVIEW.reviewspecificfeedback,
		generalfeedback: REVIEW.reviewgeneralfeedback,
		rightanswer: REVIEW.reviewrightanswer,
		overallfeedback: REVIEW.reviewoverallfeedback
	};
	const times = own.map(reviewTimeOf);
	const shown = (bits: number) => times.map((time) => (bits & time) !== 0);
	const marks = times.map((time) =>
		(REVIEW.reviewmarks & time) !== 0 ? 2 : (REVIEW.reviewmaxmarks & time) !== 0 ? 1 : 0
	);
	const options = (some: boolean) => [
		...Object.entries(fields).map(([name, bits]) => {
			const each = shown(bits);
			return {
				name,
				value: Number(own.length > 0 && (some ? each.some(Boolean) : each.every(Boolean)))
			};
		}),
		{ name: 'marks', value: own.length === 0 ? 0 : some ? Math.max(...marks) : Math.min(...marks) }
	];
	return { someoptions: options(true), alloptions: options(false), warnings: [] };
}

const notFound = () => moodleError('invalidrecord', 'Impossibile trovare il record nel database');
const closed = () => moodleError('attemptalreadyclosed', 'Questo tentativo è già stato completato');
const overdue = () =>
	moodleError('stateoverdue', 'Il tempo è scaduto: il tentativo va solo inviato');

/**
 * Applies the `data[i][name]`/`data[i][value]` fields to the attempt, as Moodle's autosave and
 * process do; refuses the lot if a sequence check is stale. `steps` adds a real step to every
 * slot that changed, as processing does and autosaving doesn't.
 */
function applyData(item: DemoAttempt, form: URLSearchParams, steps: boolean): Response | null {
	const fields = new Map<number, Map<string, string>>();
	for (let index = 0; form.has(`data[${index}][name]`); index++) {
		const name = form.get(`data[${index}][name]`) ?? '';
		const match = /^q(\d+):(\d+)_(.*)$/.exec(name);
		if (match === null || Number(match[1]) !== usageOf(item)) continue;
		const slot = Number(match[2]);
		if (!fields.has(slot)) fields.set(slot, new Map());
		fields.get(slot)?.set(match[3], form.get(`data[${index}][value]`) ?? '');
	}
	for (const [slot, values] of fields) {
		const check = values.get(':sequencecheck');
		if (check !== undefined && Number(check) !== item.sequence[slot - 1]) {
			return moodleError(
				'submissionoutofsequence',
				'Accesso a un tentativo non nella sequenza attesa'
			);
		}
	}
	for (const [slot, values] of fields) {
		const response = item.responses[slot - 1];
		if (response === undefined) continue;
		let changed = false;
		for (const [field, value] of values) {
			if (field === ':flagged') {
				if (value === '1') item.flagged.add(slot);
				else item.flagged.delete(slot);
			} else if (!field.startsWith(':') && field !== 'answerformat' && response[field] !== value) {
				response[field] = value;
				changed = true;
			}
		}
		if (steps && changed) item.sequence[slot - 1]++;
	}
	return null;
}

export const routes = [
	route(
		`POST ${SERVER}?wsfunction=mod_quiz_get_user_attempts`,
		async (request) => {
			const quizId = Number((await request.form()).get('quizid'));
			const list = attempts()
				.filter((item) => quizIdOf(item.course) === quizId)
				.map(wireAttempt);
			return json({ attempts: list, warnings: [] });
		},
		{ empty: () => json({ attempts: [], warnings: [] }) }
	),
	route(`POST ${SERVER}?wsfunction=mod_quiz_get_user_best_grade`, async (request) => {
		const quizId = Number((await request.form()).get('quizid'));
		const course = courseOfQuiz(quizId);
		const grades = attempts()
			.filter((item) => item.course === course)
			.map(sumGrades)
			.filter((value) => value !== null);
		const best = grades.length === 0 ? null : Math.max(...grades);
		return json({
			hasgrade: best !== null,
			...(best === null ? {} : { grade: best }),
			gradetopass: 6,
			warnings: []
		});
	}),
	route(`POST ${SERVER}?wsfunction=mod_quiz_get_quiz_access_information`, async (request) => {
		const course = courseOfQuiz(Number((await request.form()).get('quizid')));
		if (course === undefined) return notFound();
		const closes = DemoElearning.quizClosesAt(course);
		return json({
			canattempt: true,
			canmanage: false,
			canpreview: false,
			canreviewmyattempts: true,
			canviewreports: false,
			accessrules: [
				'Il quiz ha un tempo limite di 30 min. Il tempo inizia a scorrere quando avvii il tentativo.',
				...(closes === null
					? []
					: [
							`Il quiz chiuderà il ${closes.toLocaleString('it-IT', { dateStyle: 'full', timeStyle: 'short' })}`
						])
			],
			activerulenames: ['quizaccess_openclosedate', 'quizaccess_timelimit'],
			preventaccessreasons: [],
			warnings: []
		});
	}),
	route(`POST ${SERVER}?wsfunction=mod_quiz_get_attempt_access_information`, async (request) => {
		const form = await request.form();
		const course = courseOfQuiz(Number(form.get('quizid')));
		if (course === undefined) return notFound();
		const own = attempts().filter((item) => item.course === course);
		const requested = Number(form.get('attemptid') ?? 0);
		const asked = requested > 0 ? own.find((item) => item.id === requested) : undefined;
		if (requested > 0 && asked === undefined) return notFound();
		const unfinished = own.find((item) => item.state === 'inprogress' || item.state === 'overdue');
		// Asking runs Moodle's time check: an attempt whose time ran out is submitted as it stands.
		if (unfinished?.state === 'inprogress' && now() >= deadlineOf(unfinished)) {
			finish(unfinished, deadlineOf(unfinished));
			persist();
		}
		const checked = asked ?? unfinished ?? own.at(-1);
		return json({
			isfinished: false,
			preventnewattemptreasons: [],
			// Only the time limit asks a check, and only before an attempt starts.
			...(checked === undefined
				? {}
				: { endtime: deadlineOf(checked), ispreflightcheckrequired: unfinished === undefined }),
			warnings: []
		});
	}),
	route(`POST ${SERVER}?wsfunction=mod_quiz_get_combined_review_options`, async (request) => {
		const course = courseOfQuiz(Number((await request.form()).get('quizid')));
		if (course === undefined) return notFound();
		return json(combinedReview(attempts().filter((item) => item.course === course)));
	}),
	route(`POST ${SERVER}?wsfunction=mod_quiz_get_quiz_feedback_for_grade`, async (request) => {
		const form = await request.form();
		if (courseOfQuiz(Number(form.get('quizid'))) === undefined) return notFound();
		const text = feedbackFor(Number(form.get('grade')));
		return json({
			feedbacktext: text,
			feedbacktextformat: 1,
			feedbackinlinefiles: [],
			warnings: []
		});
	}),
	route(`POST ${SERVER}?wsfunction=mod_quiz_start_attempt`, async (request) => {
		const course = courseOfQuiz(Number((await request.form()).get('quizid')));
		if (course === undefined) return notFound();
		const all = attempts();
		const own = all.filter((item) => item.course === course);
		if (own.some((item) => item.state === 'inprogress' || item.state === 'overdue')) {
			return moodleError('attemptstillinprogress', 'Hai già un tentativo in corso');
		}
		const created = attempt(course, own.length + 1, 'inprogress', new Date(), 0, []);
		all.push(created);
		persist();
		return json({ attempt: wireAttempt(created), warnings: [] });
	}),
	route(`POST ${SERVER}?wsfunction=mod_quiz_get_attempt_data`, async (request) => {
		const item = await attemptOf(request);
		if (item === undefined) return notFound();
		if (item.state === 'overdue') return overdue();
		if (item.state !== 'inprogress') return closed();
		const page = Number((await request.form()).get('page'));
		const pages = demoPages(demoBank(item.course)).length;
		if (!Number.isInteger(page) || page < 0 || page >= pages) {
			return moodleError('invalidpagenumber', 'Numero di pagina non valido');
		}
		item.currentPage = page;
		persist();
		return json({
			attempt: wireAttempt(item),
			messages: [],
			nextpage: page + 1 < pages ? page + 1 : -1,
			questions: questionsOf(item, false, true, page),
			warnings: []
		});
	}),
	route(`POST ${SERVER}?wsfunction=mod_quiz_get_attempt_summary`, async (request) => {
		const item = await attemptOf(request);
		if (item === undefined) return notFound();
		const questions = questionsOf(item, false, false);
		return json({
			questions,
			totalunanswered: questions.filter(
				(question) => question.type !== 'description' && question.state === 'todo'
			).length,
			warnings: []
		});
	}),
	route(`POST ${SERVER}?wsfunction=mod_quiz_save_attempt`, async (request) => {
		const item = await attemptOf(request);
		if (item === undefined) return notFound();
		if (item.state === 'overdue') return overdue();
		if (item.state !== 'inprogress' || now() > deadlineOf(item) + GRACE_SECONDS) return closed();
		const refused = applyData(item, await request.form(), false);
		persist();
		return refused ?? json({ status: true, warnings: [] });
	}),
	route(`POST ${SERVER}?wsfunction=mod_quiz_process_attempt`, async (request) => {
		const item = await attemptOf(request);
		if (item === undefined) return notFound();
		if (item.state !== 'inprogress' && item.state !== 'overdue') return closed();
		const form = await request.form();
		const refused = item.state === 'overdue' ? null : applyData(item, form, true);
		if (refused) return refused;
		if (form.get('finishattempt') === '1') {
			finish(item, form.get('timeup') === '1' ? Math.min(now(), deadlineOf(item)) : now());
		}
		persist();
		return json({ state: item.state, warnings: [] });
	}),
	route(`POST ${SERVER}?wsfunction=mod_quiz_get_attempt_review`, async (request) => {
		const item = await attemptOf(request);
		if (item === undefined) return notFound();
		if (item.state !== 'finished') {
			return moodleError('noreview', 'Non è possibile rivedere questo tentativo');
		}
		const sum = sumGrades(item);
		const bank = demoBank(item.course);
		const total = bank.reduce((value, question) => value + question.maxMark, 0);
		return json({
			grade: sum === null ? 'notyetgraded' : String(Math.round((sum / total) * GRADE * 100) / 100),
			attempt: wireAttempt(item),
			additionaldata: [],
			questions: questionsOf(item, true, true),
			warnings: []
		});
	})
];
