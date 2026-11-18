import { days, epochSeconds, fromAndroid } from '../../dates.ts';
import { DemoCourses, DemoElearning, type DemoCourse } from '../../persona.ts';
import { json, route } from '../../router.ts';
import { demoState } from '../../state.ts';
import { demoAttendanceModules } from '../moodleWeb/attendance.ts';
import { demoListedCourse } from './course.elearning.ts';

const SITE = 'https://elearning.unimib.it';
const SERVER = `${SITE}/webservice/rest/server.php`;

/** "ALGORITMI E STRUTTURE DATI" as Moodle names the course: "Algoritmi e Strutture Dati". */
export function demoCourseTitle(course: DemoCourse): string {
	const minor = new Set(['e', 'di', 'del', 'della']);
	return course.name
		.toLowerCase()
		.split(' ')
		.map((word) => (minor.has(word) ? word : word.charAt(0).toUpperCase() + word.slice(1)))
		.join(' ');
}

export function demoIdNumber(course: DemoCourse): string {
	return `${course.academicYear}-${course.year}-${course.code}`;
}

export function demoEmail(name: string): string {
	return `${name.toLowerCase().replace(/ /g, '.')}@unimib.it`;
}

/** The course of a Moodle id, from a request's `courseid` or `value`. */
export function demoCourseById(id: string | null): DemoCourse | undefined {
	return DemoCourses.all.find((course) => String(course.moodleCourseId) === id);
}

/** Weeks of a course outside the persona's, such as one enrolled from the catalog. */
const GENERIC_TOPICS = ['Introduzione al corso', 'Primo modulo', 'Secondo modulo', 'Esercitazioni'];

/**
 * Any other course id gets a plain course of its own, named as the course list names it, so a
 * course enrolled from the catalog opens like the persona's.
 */
function genericCourse(id: number): DemoCourse {
	const listed = demoListedCourse(id);
	return {
		code: listed?.idNumber.split('-').at(-1) || `C${id}`,
		name: (listed?.name ?? `Corso ${id}`).toUpperCase(),
		shortLabel: `C${id}`,
		cfu: 6,
		teacher: 'Giulia Ferrari',
		tutor: 'Andrea Moretti',
		moodleCourseId: id,
		year: 1,
		academicYear: listed?.idNumber.split('-')[0] || DemoCourses.algorithms.academicYear
	};
}

function courseOf(id: string | null): DemoCourse | undefined {
	const known = demoCourseById(id);
	if (known !== undefined) return known;
	const number = Number(id);
	return Number.isSafeInteger(number) && number > 0 ? genericCourse(number) : undefined;
}

/** The self-assessment quiz's description, as teachers write it: what it covers and how it goes. */
export function demoQuizIntro(course: DemoCourse): string {
	const topics = (DemoElearning.topics.get(course) ?? []).slice(0, 3).join(', ');
	return `<p>Dieci domande sugli argomenti delle prime settimane (${topics.toLowerCase()}), per verificare la preparazione prima dell’esame.</p><p>Il quiz non conta per il voto finale: puoi ripeterlo quante volte vuoi, e vedrai le risposte corrette dopo la consegna.</p>`;
}

/** The persona's courses whose quiz was handed in before (see the attempts in `quiz.quiz.ts`). */
const QUIZ_DONE: readonly DemoCourse[] = [
	DemoCourses.algorithms,
	DemoCourses.statistics,
	DemoCourses.programming
];

/** Completed activities by cmId; the completion fixture marks more of them done. */
export function demoCompletion(): Map<number, number> {
	return demoState('moodle.completion', () => {
		const done = new Map<number, number>();
		for (const course of DemoCourses.all) {
			const progress = DemoElearning.progress.get(course) ?? 0;
			const topics = DemoElearning.topics.get(course) ?? [];
			const weeks = Math.floor(progress * topics.length);
			done.set(DemoElearning.cmId(course, DemoElearning.SYLLABUS_PDF), epochSeconds(days(-40)));
			if (QUIZ_DONE.includes(course)) {
				done.set(DemoElearning.cmId(course, DemoElearning.QUIZ), epochSeconds(days(-6, 15, 40)));
			}
			for (let week = 0; week < weeks; week++) {
				// The third recording is the one the student is half-way through (see demoProgress).
				for (const offset of week < 2 ? [0, 1, 2] : [0, 2]) {
					const index = 10 + week * 10 + offset;
					done.set(DemoElearning.cmId(course, index), epochSeconds(days(index - 60)));
				}
			}
		}
		return done;
	});
}

interface ModuleSpec {
	index: number;
	name: string;
	modname: string;
	instance?: number;
	description?: string;
	tracked?: boolean;
	contents?: object[];
	dates?: object[];
	url?: string;
	userVisible?: boolean;
	availability?: string;
	afterlink?: string;
	groupmode?: number;
}

const PLURALS: Readonly<Record<string, string>> = {
	forum: 'Forum',
	resource: 'File',
	kalvidres: 'Video Kaltura',
	folder: 'Cartelle',
	url: 'URL',
	label: 'Etichette',
	page: 'Pagine',
	assign: 'Compiti',
	quiz: 'Quiz'
};

function fileContent(course: DemoCourse, cmId: number, name: string, mime: string, size: number) {
	const modified = epochSeconds(days((cmId % 40) - 60, 9));
	return {
		type: 'file',
		filename: name,
		filepath: '/',
		filesize: size,
		fileurl: `${SITE}/webservice/pluginfile.php/${900000 + cmId}/mod_resource/content/1/${encodeURIComponent(name)}?forcedownload=1`,
		timecreated: modified,
		timemodified: modified,
		sortorder: 1,
		mimetype: mime,
		isexternalfile: false,
		userid: 30000 + course.moodleCourseId,
		author: course.teacher,
		license: 'allrightsreserved'
	};
}

function pdf(course: DemoCourse, index: number, name: string, fileName: string): ModuleSpec {
	return {
		index,
		name,
		modname: 'resource',
		tracked: true,
		contents: [
			fileContent(
				course,
				DemoElearning.cmId(course, index),
				fileName,
				'application/pdf',
				900_000 + index * 73_000
			)
		]
	};
}

function module(course: DemoCourse, spec: ModuleSpec) {
	const id = DemoElearning.cmId(course, spec.index);
	const completedAt = demoCompletion().get(id);
	return {
		id,
		url: spec.url ?? `${SITE}/mod/${spec.modname}/view.php?id=${id}`,
		name: spec.name,
		instance: spec.instance ?? id,
		contextid: 900000 + id,
		...(spec.description === undefined ? {} : { description: spec.description }),
		visible: 1,
		uservisible: spec.userVisible ?? true,
		...(spec.availability === undefined ? {} : { availabilityinfo: spec.availability }),
		visibleoncoursepage: 1,
		modicon: `${SITE}/theme/image.php/boost/${spec.modname}/1/monologo`,
		modname: spec.modname,
		purpose: 'content',
		modplural: PLURALS[spec.modname] ?? spec.modname,
		indent: 0,
		onclick: '',
		afterlink: spec.afterlink ?? null,
		customdata: '""',
		noviewlink: spec.modname === 'label',
		completion: spec.tracked ? 1 : 0,
		...(spec.tracked
			? {
					completiondata: {
						state: completedAt === undefined ? 0 : 1,
						timecompleted: completedAt ?? 0,
						overrideby: null,
						valueused: false,
						hascompletion: true,
						isautomatic: false,
						istrackeduser: true,
						uservisible: true,
						details: []
					}
				}
			: {}),
		downloadcontent: 1,
		dates: spec.dates ?? [],
		groupmode: spec.groupmode ?? 0,
		contents: spec.contents ?? []
	};
}

function section(
	course: DemoCourse,
	number: number,
	name: string,
	specs: ModuleSpec[],
	summary = ''
) {
	return {
		id: course.moodleCourseId * 10 + number,
		name,
		visible: 1,
		summary,
		summaryformat: 1,
		section: number,
		hiddenbynumsections: 0,
		uservisible: true,
		modules: specs.map((spec) => module(course, spec))
	};
}

/** Extra material in each week, so every kind of activity appears somewhere. */
function extras(course: DemoCourse, week: number, base: number): ModuleSpec[] {
	const cm = (index: number) => DemoElearning.cmId(course, index);
	switch (week) {
		case 0:
			return [
				{
					index: base + 3,
					name: 'Esercizi svolti',
					modname: 'folder',
					description: '<p>Soluzioni commentate degli esercizi visti in aula.</p>',
					contents: [
						fileContent(course, cm(base + 3), 'soluzioni_1.pdf', 'application/pdf', 412_000),
						fileContent(course, cm(base + 3), 'ricerca_binaria.py', 'text/x-python', 1_240),
						fileContent(course, cm(base + 3), 'esempi.zip', 'application/zip', 3_400)
					]
				}
			];
		case 1:
			return [
				// Moodle copies a label's text into its name, as real courses show.
				{
					index: base + 4,
					name: 'Approfondimenti',
					modname: 'label',
					description: '<p dir="ltr"><strong>Approfondimenti</strong></p>'
				},
				{
					index: base + 3,
					name: 'VisuAlgo: algoritmi animati',
					modname: 'url',
					description: '<p>Animazioni interattive degli algoritmi visti a lezione.</p>',
					contents: [
						{
							type: 'url',
							filename: 'VisuAlgo',
							filepath: null,
							filesize: 0,
							fileurl: 'https://visualgo.net/it/sorting',
							timecreated: 0,
							timemodified: 0,
							sortorder: 0,
							userid: 0,
							author: null,
							license: null
						}
					]
				}
			];
		case 2:
			return [
				{
					index: base + 3,
					name: 'Schema riassuntivo',
					modname: 'resource',
					tracked: true,
					contents: [
						fileContent(course, cm(base + 3), 'schema_riassuntivo.svg', 'image/svg+xml', 2_800)
					]
				}
			];
		case 3:
			return [
				{
					index: base + 3,
					name: 'Traccia del laboratorio',
					modname: 'resource',
					tracked: true,
					contents: [
						fileContent(
							course,
							cm(base + 3),
							'traccia_laboratorio.docx',
							'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
							18_600
						)
					]
				}
			];
		default:
			return [
				{
					index: base + 3,
					name: 'Note sul progetto finale',
					modname: 'page',
					tracked: true,
					contents: [
						{
							...fileContent(course, cm(base + 3), 'index.html', 'text/html', 2_100),
							fileurl: `${SITE}/webservice/pluginfile.php/${900000 + cm(base + 3)}/mod_page/content/3/index.html`
						}
					]
				},
				{
					index: base + 4,
					name: 'Soluzioni della prova intermedia',
					modname: 'resource',
					userVisible: false,
					availability: `<div class="availabilityinfo">Non disponibile finché: <ul><li>La data è <strong>dal ${days(21).toLocaleDateString('it-IT', { day: 'numeric', month: 'long' })}</strong></li></ul></div>`,
					contents: []
				}
			];
	}
}

/**
 * A summary with a figure, as teachers paste them: the image sits in the section's files, behind
 * Moodle's session-only `pluginfile.php`, so the page fetches it with the token.
 */
function summaryWithFigure(course: DemoCourse, number: number): string {
	const id = course.moodleCourseId * 10 + number;
	return `<p>Lo schema visto a lezione, da tenere sotto mano per gli esercizi:</p><p><img src="${SITE}/pluginfile.php/${900000 + id}/course/section/${id}/schema_albero.svg" alt="Schema di un albero binario di ricerca" width="320" height="200"></p>`;
}

/** The forums of `course`'s page, as `forum.ts` answers them: Statistics has only announcements. */
function forums(course: DemoCourse): ModuleSpec[] {
	const specs: ModuleSpec[] = [
		{
			index: DemoElearning.FORUM,
			name: 'Annunci',
			modname: 'forum',
			instance: DemoElearning.forumId(course),
			afterlink: '2 messaggi non letti'
		}
	];
	if (course === DemoCourses.statistics) return specs;
	specs.push({
		index: 3,
		name: 'Forum di discussione',
		modname: 'forum',
		instance: course.moodleCourseId + 2400
	});
	if (course === DemoCourses.algorithms) {
		specs.push({
			index: 4,
			name: 'Domande sul progetto',
			modname: 'forum',
			instance: course.moodleCourseId + 2800,
			groupmode: 1
		});
	}
	return specs;
}

/** One course page, built like the Android demo's: general info, a week per topic, assessment. */
export function demoSections(course: DemoCourse) {
	const known = DemoCourses.all.includes(course);
	const topics = known ? (DemoElearning.topics.get(course) ?? []) : GENERIC_TOPICS;
	const general = section(
		course,
		0,
		'Informazioni generali',
		[
			...(known ? forums(course) : forums(course).slice(0, 1)),
			pdf(course, DemoElearning.SYLLABUS_PDF, 'Programma del corso', 'programma.pdf'),
			...demoAttendanceModules(course)
		],
		`<p>Benvenuti al corso di <strong>${demoCourseTitle(course)}</strong>. Le slide e le registrazioni delle lezioni sono pubblicate qui ogni settimana; le comunicazioni arrivano nel forum <em>Annunci</em>.</p>`
	);
	const weeks = topics.map((topic, week) => {
		const base = 10 + week * 10;
		return section(
			course,
			week + 1,
			topic,
			[
				pdf(course, base, `Slide ${week + 1}: ${topic}`, `slide_${week + 1}.pdf`),
				{
					index: base + 1,
					name: `Lezione ${week + 1}: registrazione`,
					modname: 'kalvidres',
					tracked: true
				},
				pdf(course, base + 2, `Esercizi ${week + 1}`, `esercizi_${week + 1}.pdf`),
				...extras(course, week, base)
			],
			week === 2 ? summaryWithFigure(course, week + 1) : ''
		);
	});
	const assessment = section(course, topics.length + 1, 'Consegne e verifiche', [
		...DemoElearning.assignments()
			.filter((item) => item.course === course)
			.map((item) => ({
				index: DemoElearning.ASSIGNMENT + item.index,
				name: item.title,
				modname: 'assign',
				instance: item.id,
				tracked: true,
				dates: [
					{
						label: 'Data di scadenza:',
						timestamp: epochSeconds(item.due),
						dataid: 'duedate'
					}
				]
			})),
		...(known
			? [
					{
						index: DemoElearning.QUIZ,
						name: 'Quiz di autovalutazione',
						modname: 'quiz',
						instance: DemoElearning.quizId(course),
						tracked: true,
						description: demoQuizIntro(course),
						dates: (() => {
							const closes = DemoElearning.quizClosesAt(course);
							return closes === null
								? []
								: [{ label: 'Chiusura:', timestamp: epochSeconds(closes), dataid: 'timeclose' }];
						})()
					}
				]
			: [])
	]);
	return [general, ...weeks, assessment];
}

function customField(name: string, shortname: string, value: string) {
	return { name, shortname, type: 'text', valueraw: value, value };
}

function courseRecord(course: DemoCourse) {
	const title = demoCourseTitle(course);
	return {
		id: course.moodleCourseId,
		fullname: title,
		displayname: title,
		shortname: demoIdNumber(course),
		categoryid: DemoElearning.categoryId,
		categoryname: 'Informatica',
		sortorder: 10000 + course.moodleCourseId,
		summary: '',
		summaryformat: 1,
		summaryfiles: [],
		overviewfiles: [],
		showactivitydates: true,
		showcompletionconditions: true,
		contacts: [{ id: 30000 + course.moodleCourseId, fullname: course.teacher }],
		enrollmentmethods: ['manual', 'self'],
		customfields: [
			customField('Settore disciplinare', 'settore', 'INF/01'),
			customField('CFU', 'cfu', String(course.cfu)),
			customField('Periodo', 'periodo', 'Secondo Semestre'),
			customField('Tipo', 'tipo', 'Obbligatorio'),
			customField('Ore', 'ore', String(course.cfu * 8)),
			customField('Tipologia CdS', 'tipologiacds', 'Laurea Triennale'),
			customField('Erogazione', 'erogazione', 'Convenzionale'),
			customField('Lingua', 'lingua', 'Italiano')
		],
		idnumber: demoIdNumber(course),
		format: 'topics',
		showgrades: 1,
		newsitems: 5,
		startdate: epochSeconds(fromAndroid(2026, 3, 2)),
		enddate: epochSeconds(fromAndroid(2026, 6, 26)),
		maxbytes: 0,
		showreports: 0,
		visible: 1,
		groupmode: 0,
		enablecompletion: 1,
		lang: ''
	};
}

export const routes = [
	route(
		`POST ${SERVER}?wsfunction=core_course_get_contents`,
		async (request) => {
			const course = courseOf((await request.form()).get('courseid'));
			return course === undefined
				? json({
						exception: 'moodle_exception',
						errorcode: 'invalidrecord',
						message: 'Impossibile trovare il record nella tabella course'
					})
				: json(demoSections(course));
		},
		{ empty: () => json([]) }
	),
	route(
		`POST ${SERVER}?wsfunction=core_course_get_courses_by_field`,
		async (request) => {
			const form = await request.form();
			const course = form.get('field') === 'id' ? courseOf(form.get('value')) : undefined;
			return json({ courses: course === undefined ? [] : [courseRecord(course)], warnings: [] });
		},
		{ empty: () => json({ courses: [], warnings: [] }) }
	)
];
