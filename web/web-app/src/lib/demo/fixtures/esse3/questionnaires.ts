import { academicYear } from '#lib/data/academicYear.ts';
import { DemoCareer, DemoCourses, DemoStudent, type DemoCourse } from '../../persona.ts';
import { esse3Error, json, route, type DemoRequest } from '../../router.ts';
import { demoState } from '../../state.ts';

const API = 'https://s3w.si.unimib.it/e3rest/api/questionari-service-v1/questionari';
const COMPILATION = `${API}/compilazione/:stuId/:adsceId/quest/:questId/:compId`;

const QUESTIONNAIRE_ID = 7001;
const CONFIG_ID = 7101;
const EVENT = 'EV_VAL_DID';

interface DemoUnit {
	course: DemoCourse;
	id: number;
	/** `udDes`: the course itself, or one of its modules. */
	name: string;
	firstName: string;
	lastName: string;
	partition: { code: string; name: string };
	tags: string;
	done: boolean;
}

const WHOLE_CLASS = { code: 'N0', name: 'NESSUN PARTIZIONAMENTO' };

function unit(
	course: DemoCourse,
	id: number,
	lecturer: string,
	options: { name?: string; partition?: DemoUnit['partition']; done?: boolean } = {}
): DemoUnit {
	const [firstName = '', ...rest] = lecturer.toUpperCase().split(' ');
	return {
		course,
		id,
		name: options.name ?? course.name,
		firstName,
		lastName: rest.join(' '),
		partition: options.partition ?? WHOLE_CLASS,
		tags: `UD_VALDID=${id};DOC=${id + 500}`,
		done: options.done ?? false
	};
}

/**
 * The questionnaires of the courses being attended, as on Android: algorithms and statistics are
 * filled in, the other three wait. Databases is evaluated module by module and operating systems
 * by class, so the units page shows both cases.
 */
const units = () =>
	demoState('esse3.questionnaires.units', (): DemoUnit[] => {
		const { algorithms, databases, operatingSystems, statistics, languages } = DemoCourses;
		return [
			unit(algorithms, 61001, algorithms.teacher, { done: true }),
			unit(databases, 61002, databases.teacher, { name: 'BASI DI DATI - TEORIA' }),
			unit(databases, 61003, databases.tutor, { name: 'BASI DI DATI - LABORATORIO' }),
			unit(operatingSystems, 61004, operatingSystems.teacher, {
				partition: { code: 'T2', name: 'Turno M-Z' }
			}),
			unit(statistics, 61005, statistics.teacher, { done: true }),
			unit(languages, 61006, languages.teacher)
		];
	});

function unitsOf(activityId: number): DemoUnit[] {
	return units().filter((item) => DemoCareer.activityId(item.course) === activityId);
}

/** Linguaggi di programmazione asks for a named questionnaire; the others are anonymous. */
const NAMED = new Set<DemoCourse>([DemoCourses.languages]);

/**
 * The web lab's questionnaire is one Esse3 reports misconfigured (`statoLink` 4): listed for the
 * student to check, with no questionnaire to open on its page.
 */
const MISCONFIGURED = {
	id: DemoCareer.webLabId,
	code: 'E3101Q119',
	name: 'LABORATORIO DI PROGRAMMAZIONE WEB',
	cfu: 3,
	year: 2
};

/** Esse3's `statoLink`: 1 all filled in, 2 some, 3 none. */
function linkState(items: readonly DemoUnit[]): number {
	const done = items.filter((item) => item.done).length;
	return done === items.length ? 1 : done > 0 ? 2 : 3;
}

/** Whether every questionnaire of a course is filled in, for the exam calls that require it. */
export function evaluated(activityId: number): boolean {
	const items = unitsOf(activityId);
	return items.length > 0 && items.every((item) => item.done);
}

// The questionnaire: Bicocca's teaching evaluation, with its branch for students who attended
// less than half of the lectures.

type Format = 'TL_DOM_DFS' | 'TL_DOM_OFS' | 'TL_DOM_DFM' | 'TL_DOM_LIB';

interface DemoQuestion {
	id: number;
	text: string;
	note?: string;
	mandatory: boolean;
	format: Format;
	max?: number;
	/** Each option's label; `null` for an "other" option that asks for words. */
	options: ReadonlyArray<string | null>;
	/** An unlabelled "other" option, to show the app's fallback label. */
	unlabelledOther?: boolean;
}

interface DemoPage {
	id: number;
	section: string;
	questions: DemoQuestion[];
}

const AGREEMENT = ['Decisamente no', 'Più no che sì', 'Più sì che no', 'Decisamente sì'];

function agreement(id: number, text: string): DemoQuestion {
	return { id, text, mandatory: true, format: 'TL_DOM_OFS', options: AGREEMENT };
}

const ATTENDED = 110101;

const PAGES: Record<number, DemoPage> = {
	11: {
		id: 11,
		section: 'Frequenza',
		questions: [
			{
				id: 1101,
				text: 'Quante lezioni di questo insegnamento hai frequentato?',
				note: 'Le domande successive dipendono dalla risposta.',
				mandatory: true,
				format: 'TL_DOM_DFS',
				options: ['Più della metà', 'Meno della metà']
			}
		]
	},
	12: {
		id: 12,
		section: 'Insegnamento',
		questions: [
			agreement(
				1201,
				'Le conoscenze preliminari possedute sono risultate sufficienti per la comprensione degli argomenti previsti nel programma d’esame?'
			),
			agreement(
				1202,
				'Il carico di studio dell’insegnamento è proporzionato ai crediti assegnati?'
			),
			agreement(
				1203,
				'Il materiale didattico (indicato e disponibile) è adeguato per lo studio della materia?'
			),
			agreement(1204, 'Le modalità di esame sono state definite in modo chiaro?')
		]
	},
	13: {
		id: 13,
		section: 'Docenza',
		questions: [
			agreement(
				1301,
				'Gli orari di svolgimento di lezioni, esercitazioni e altre eventuali attività didattiche sono rispettati?'
			),
			agreement(1302, 'Il docente stimola o motiva l’interesse verso la disciplina?'),
			agreement(1303, 'Il docente espone gli argomenti in modo chiaro?'),
			agreement(
				1304,
				'Le attività didattiche integrative (esercitazioni, tutorati, laboratori) sono utili all’apprendimento della materia?'
			),
			agreement(
				1305,
				'L’insegnamento è stato svolto in maniera coerente con quanto dichiarato sul sito web del corso di studio?'
			),
			agreement(1306, 'Il docente è reperibile per chiarimenti e spiegazioni?')
		]
	},
	14: {
		id: 14,
		section: 'Interesse e suggerimenti',
		questions: [
			{
				id: 1401,
				text: 'Sei complessivamente soddisfatto di come è stato svolto questo insegnamento?',
				note: 'Da 1 (per nulla) a 10 (del tutto).',
				mandatory: true,
				format: 'TL_DOM_OFS',
				options: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10']
			},
			{
				id: 1402,
				text: 'Che cosa suggeriresti per migliorare l’insegnamento?',
				note: 'Al massimo tre suggerimenti.',
				mandatory: false,
				format: 'TL_DOM_DFM',
				max: 3,
				options: [
					'Alleggerire il carico didattico complessivo',
					'Aumentare l’attività di supporto didattico',
					'Fornire più conoscenze di base',
					'Migliorare il coordinamento con altri insegnamenti',
					'Migliorare la qualità del materiale didattico',
					'Fornire in anticipo il materiale didattico',
					'Inserire prove d’esame intermedie',
					null
				]
			},
			{
				id: 1403,
				text: 'Vuoi aggiungere altre osservazioni?',
				note: 'Le leggono i docenti e il corso di studio, in forma anonima.',
				mandatory: false,
				format: 'TL_DOM_LIB',
				options: ['']
			}
		]
	},
	15: {
		id: 15,
		section: 'Frequenza ridotta',
		questions: [
			{
				id: 1501,
				text: 'Qual è il motivo principale della frequenza ridotta alle lezioni?',
				mandatory: true,
				format: 'TL_DOM_DFS',
				options: [
					'Lavoro',
					'Frequenza di lezioni di altri insegnamenti',
					'Frequenza poco utile ai fini della preparazione dell’esame',
					'Le strutture dedicate alla didattica non consentono la frequenza',
					null
				],
				unlabelledOther: true
			}
		]
	}
};

const optionId = (question: DemoQuestion, index: number) => question.id * 100 + index + 1;

/** One compiled answer: an option, with the words of a free-text one. */
interface SavedAnswer {
	question: number;
	option: number;
	text: string;
}

interface DemoCompilation {
	id: number;
	userId: number;
	unit: DemoUnit;
	answers: Map<number, SavedAnswer[]>;
}

const compilations = () =>
	demoState('esse3.questionnaires.compilations', () => ({
		next: 980001,
		open: new Map<number, DemoCompilation>()
	}));

/** The pages in order: who attended less than half skips the lecturer's page. */
function sequence(compilation: DemoCompilation): number[] {
	const attendance = compilation.answers.get(11)?.find((answer) => answer.question === 1101);
	if (attendance === undefined) return [11];
	return attendance.option === ATTENDED ? [11, 12, 13, 14] : [11, 15, 12, 14];
}

function questionnaireTitle(): string {
	const year = academicYear();
	return `Valutazione della didattica ${year}/${year + 1}`;
}

function pageBody(compilation: DemoCompilation, page: DemoPage | null) {
	const order = sequence(compilation);
	const at = page === null ? -1 : order.indexOf(page.id);
	const saved = page === null ? [] : (compilation.answers.get(page.id) ?? []);
	return {
		paginaId: page?.id ?? null,
		questCompId: compilation.id,
		userCompId: compilation.userId,
		questionarioId: QUESTIONNAIRE_ID,
		questionarioCod: 'VAL_DID',
		pQuestionarioDes: questionnaireTitle(),
		pPaginaPrecId: at > 0 ? order[at - 1] : null,
		pPaginaSuccId: at >= 0 && at < order.length - 1 ? order[at + 1] : null,
		paragrafi:
			page === null
				? []
				: [
						{
							paginaId: page.id,
							paragrafoId: page.id * 10,
							elementiDes: page.section,
							obbligatorioFlg: 0,
							domande: page.questions.map((question) => ({
								paragrafoId: page.id * 10,
								domandaId: question.id,
								elementiDes: question.text,
								elementiNota: question.note ?? null,
								obbligatorioFlg: question.mandatory ? 1 : 0,
								numMaxSce: question.max ?? (question.format === 'TL_DOM_DFM' ? null : 1),
								tipoFormatoCod: question.format,
								rispDisponibili: question.options.map((label, index) => ({
									domandaId: question.id,
									rispostaId: optionId(question, index),
									elementiDes: label ?? (question.unlabelledOther ? '' : 'Altro (specificare)'),
									rispostaFormatoCod:
										label === null || question.format === 'TL_DOM_LIB' ? 'TL_RSP_ALF' : 'TL_RSP_FIX'
								})),
								rispComplete: saved
									.filter((answer) => answer.question === question.id)
									.map((answer) => ({
										domandaId: question.id,
										quesitoId: answer.option,
										testoLibero: answer.text || null
									}))
							}))
						}
					]
	};
}

function compilationOf(request: DemoRequest): DemoCompilation | Response {
	if (Number(request.params.stuId) !== DemoStudent.career.id) {
		return esse3Error(403, 'Carriera non accessibile');
	}
	const compilation = compilations().open.get(Number(request.params.compId));
	return compilation ?? esse3Error(404, 'Questionario compilato non trovato');
}

/** Every mandatory question on the pages the answers lead through has an answer. */
function complete(compilation: DemoCompilation): boolean {
	return sequence(compilation).every((id) => {
		const saved = compilation.answers.get(id) ?? [];
		return PAGES[id].questions.every(
			(question) => !question.mandatory || saved.some((answer) => answer.question === question.id)
		);
	});
}

/** The questionnaire's error for a malformed answer, as Esse3 words it. */
function invalidAnswer(page: DemoPage, answers: readonly SavedAnswer[]): string | undefined {
	for (const answer of answers) {
		const question = page.questions.find((item) => item.id === answer.question);
		if (!question) return 'QUESTION_DOES_NOT_EXIST';
		const index = answer.option - question.id * 100 - 1;
		if (index < 0 || index >= question.options.length) return 'QUESTION_DOES_NOT_EXIST';
	}
	for (const question of page.questions) {
		const picked = answers.filter((answer) => answer.question === question.id).length;
		if (picked > (question.max ?? (question.format === 'TL_DOM_DFM' ? Infinity : 1))) {
			return 'TOO_VALUES';
		}
	}
	return undefined;
}

/** The web lab's units: one to evaluate, but no questionnaire Esse3 can hand out. */
function misconfiguredUnits() {
	const { id, code, name, cfu } = MISCONFIGURED;
	return {
		adsceId: id,
		stato: 0,
		questConfigId: null,
		questionarioId: null,
		des: name,
		questionarioDes: null,
		anonimoFlg: 1,
		udLogPdsListWeb: [
			{
				adsceId: id,
				udId: 61010,
				udCod: `${code}-1`,
				udDes: name,
				docenteId: null,
				docentiNome: null,
				docentiCognome: null,
				domPartCod: WHOLE_CLASS.code,
				domPartDes: WHOLE_CLASS.name,
				statoLink: 4,
				adDes: name,
				adCod: code,
				cfu,
				tagsValdid: 'UD_VALDID=61010'
			}
		]
	};
}

export const routes = [
	route(
		`GET ${API}/libretto/:matId`,
		(request) => {
			if (Number(request.params.matId) !== DemoStudent.career.enrollmentTraitId) {
				return esse3Error(404, 'Libretto non trovato');
			}
			const year = academicYear();
			const row = (
				ord: number,
				activity: { id: number; code: string; name: string; cfu: number; year: number },
				statoLink: number
			) => ({
				matId: DemoStudent.career.enrollmentTraitId,
				ord,
				adsceId: activity.id,
				stuId: DemoStudent.career.id,
				adCod: activity.code,
				adDes: activity.name,
				annoCorso: activity.year,
				stato: 'P',
				statoDes: 'Pianificata',
				peso: activity.cfu,
				aaFreqId: year,
				freqUffFlg: 0,
				freqObbligFlg: 0,
				sovranFlg: 0,
				debitoFlg: 0,
				genAutoFlg: 0,
				genRicSpecFlg: 0,
				ricId: 0,
				statoLink
			});
			return json([
				row(99, MISCONFIGURED, 4),
				...DemoCourses.current.map((course, index) => {
					const id = DemoCareer.activityId(course);
					return row(100 + index, { ...course, id }, linkState(unitsOf(id)));
				})
			]);
		},
		{ empty: () => esse3Error(404, 'Nessuna attività con questionari') }
	),
	route(
		`GET ${API}/libretto/:adsceId/unitadidattiche`,
		(request) => {
			const activityId = Number(request.params.adsceId);
			if (activityId === MISCONFIGURED.id) return json(misconfiguredUnits());
			const items = unitsOf(activityId);
			if (items.length === 0) return esse3Error(404, 'Attività didattica non trovata');
			const open = request.url.searchParams.get('eventCompId') === EVENT;
			const course = items[0].course;
			return json({
				adsceId: activityId,
				stato: linkState(items) === 1 ? 1 : 0,
				questConfigId: open ? CONFIG_ID : null,
				questionarioId: open ? QUESTIONNAIRE_ID : null,
				des: course.name,
				questionarioDes: questionnaireTitle(),
				anonimoFlg: NAMED.has(course) ? 0 : 1,
				udLogPdsListWeb: items.map((item) => ({
					adsceId: activityId,
					udId: item.id,
					udCod: `${course.code}-${item.id % 100}`,
					udDes: item.name,
					docenteId: item.id + 500,
					docentiNome: item.firstName,
					docentiCognome: item.lastName,
					domPartCod: item.partition.code,
					domPartDes: item.partition.name,
					statoLink: item.done ? 1 : 3,
					adDes: course.name,
					adCod: course.code,
					cfu: course.cfu,
					tagsValdid: item.tags
				}))
			});
		},
		{
			empty: (request) =>
				json({
					adsceId: Number(request.params.adsceId),
					stato: 1,
					questConfigId: null,
					questionarioId: null,
					anonimoFlg: 1,
					udLogPdsListWeb: []
				})
		}
	),
	route(`PUT ${API}/compilazione/:stuId/:adsceId/quest/:questId/start`, async (request) => {
		if (Number(request.params.stuId) !== DemoStudent.career.id) {
			return esse3Error(403, 'Carriera non accessibile');
		}
		const { tags } = await request.json<{ tags?: string }>();
		const target = unitsOf(Number(request.params.adsceId)).find((item) => item.tags === tags);
		const query = request.url.searchParams;
		if (
			!target ||
			Number(request.params.questId) !== QUESTIONNAIRE_ID ||
			query.get('eventCompId') !== EVENT ||
			Number(query.get('questConfigId')) !== CONFIG_ID
		) {
			return esse3Error(422, 'Questionario non compilabile per i parametri indicati');
		}
		if (target.done) return esse3Error(422, 'Questionario già compilato');
		const store = compilations();
		const compilation: DemoCompilation = {
			id: store.next,
			userId: store.next + 10000,
			unit: target,
			answers: new Map()
		};
		store.next += 1;
		store.open.set(compilation.id, compilation);
		return json(pageBody(compilation, PAGES[11]));
	}),
	route(`PUT ${API}/compilazione/:stuId/quest/:questId/:compId/save/:pageId`, async (request) => {
		const compilation = compilationOf(request);
		if (compilation instanceof Response) return compilation;
		const page = PAGES[Number(request.params.pageId)];
		if (!page) return esse3Error(404, 'Pagina non trovata');
		const body =
			await request.json<
				{ domandaId?: number; rispostaId?: number; corpoRisposta?: string | null }[]
			>();
		if (body.some((answer) => typeof answer.corpoRisposta !== 'string')) {
			return esse3Error(422, 'NO_TEXT');
		}
		const answers = body.map((answer) => ({
			question: Number(answer.domandaId),
			option: Number(answer.rispostaId),
			text: answer.corpoRisposta ?? ''
		}));
		const invalid = invalidAnswer(page, answers);
		if (invalid) return esse3Error(400, invalid);
		compilation.answers.set(page.id, answers);
		return json('OK');
	}),
	route(`GET ${COMPILATION}/pagina/:pageId/next`, (request) => {
		const compilation = compilationOf(request);
		if (compilation instanceof Response) return compilation;
		const order = sequence(compilation);
		const at = order.indexOf(Number(request.params.pageId));
		if (at < 0) return esse3Error(404, 'Pagina non trovata');
		const next = order[at + 1];
		return json(pageBody(compilation, next === undefined ? null : PAGES[next]));
	}),
	route(`GET ${COMPILATION}/pagina/:pageId/prev`, (request) => {
		const compilation = compilationOf(request);
		if (compilation instanceof Response) return compilation;
		const order = sequence(compilation);
		const at = order.indexOf(Number(request.params.pageId));
		if (at <= 0) return esse3Error(404, 'Pagina non trovata');
		return json(pageBody(compilation, PAGES[order[at - 1]]));
	}),
	route(`GET ${COMPILATION}/summary`, (request) => {
		const compilation = compilationOf(request);
		if (compilation instanceof Response) return compilation;
		return json({
			questCompId: compilation.id,
			questionarioId: QUESTIONNAIRE_ID,
			stato: 'B',
			completoFlg: complete(compilation) ? '1' : '0',
			eventoCompCod: EVENT,
			questionarioCod: 'VAL_DID',
			pQuestionarioDes: questionnaireTitle(),
			pPrimaPaginaId: 11,
			pagine: []
		});
	}),
	route(`PUT ${COMPILATION}/conferma`, (request) => {
		const compilation = compilationOf(request);
		if (compilation instanceof Response) return compilation;
		if (!complete(compilation)) {
			return esse3Error(422, 'MANDATORY_MISSING');
		}
		compilation.unit.done = true;
		compilations().open.delete(compilation.id);
		return json('OK');
	})
];
