import { DemoCourses, DemoElearning, type DemoCourse } from '../../persona.ts';
import { html, route } from '../../router.ts';
import { demoState } from '../../state.ts';

/**
 * The Moodle attendance registers (mod_attendance) of three courses, read from each activity's
 * `view.php` as the SDK scrapes it. Basi di dati has a session open now that asks no password;
 * Linguaggi di programmazione one whose QR carries the password `BICOCCA`. Marking them (the
 * mobile web service, `moodle/mobile.ts`) updates the counts here.
 */

const SITE = 'https://elearning.unimib.it';

/** Where each course page lists its attendance activity, after the forums (indices 1, 3, 4). */
const MODULE_INDEX = 5;

export interface DemoSession {
	id: string;
	/** The static QR's password; null when the session asks none. */
	password: string | null;
	marked: boolean;
}

export interface DemoRegister {
	course: DemoCourse;
	cmId: number;
	name: string;
	/** Sessions attended, sessions taken so far, sessions planned. */
	attended: number;
	taken: number;
	total: number;
	sessions: DemoSession[];
}

interface RegisterSpec {
	course: DemoCourse;
	name: string;
	/** Attended, taken and planned sessions. */
	counts: [number, number, number];
	open: Omit<DemoSession, 'marked'>[];
}

const SPECS: readonly RegisterSpec[] = [
	{ course: DemoCourses.algorithms, name: 'Presenze', counts: [38, 42, 48], open: [] },
	{
		course: DemoCourses.databases,
		name: 'Presenze laboratorio',
		counts: [5, 6, 10],
		open: [{ id: '70001', password: null }]
	},
	{
		course: DemoCourses.languages,
		name: 'Presenze esercitazioni',
		counts: [3, 7, 12],
		open: [{ id: '70002', password: 'BICOCCA' }]
	}
];

/**
 * The attendance activities of `course`'s page, as `core_course_get_contents` should list them
 * (a module spec of `moodle/course.course.ts`).
 */
export function demoAttendanceModules(course: DemoCourse) {
	return SPECS.filter((spec) => spec.course === course).map((spec) => ({
		index: MODULE_INDEX,
		name: spec.name,
		modname: 'attendance'
	}));
}

export function demoRegisters(): DemoRegister[] {
	return demoState('moodleWeb.attendance', () =>
		SPECS.map(({ course, name, counts: [attended, taken, total], open }) => ({
			course,
			cmId: DemoElearning.cmId(course, MODULE_INDEX),
			name,
			attended,
			taken,
			total,
			sessions: open.map((session) => ({ ...session, marked: false }))
		}))
	);
}

export function demoRegister(cmId: string | null | undefined): DemoRegister | undefined {
	return demoRegisters().find((register) => String(register.cmId) === cmId);
}

const percent = (part: number, whole: number) =>
	`${(whole === 0 ? 0 : (part * 100) / whole).toFixed(1).replace('.', ',')}%`;

function statsTable(register: DemoRegister): string {
	const { attended, taken, total } = register;
	const remaining = total - taken;
	const rows: [string, string][] = [
		['Sessioni registrate', String(attended)],
		['Punti sulle sessioni registrate', `${attended} / ${taken}`],
		['Percentuale sulle sessioni registrate', percent(attended, taken)],
		['Totale sessioni', String(total)],
		['Punti su tutte le sessioni', `${attended} / ${total}`],
		['Percentuale su tutte le sessioni', percent(attended, total)],
		['Punteggio massimo possibile', `${attended + remaining} / ${total}`],
		['Percentuale massima possibile', percent(attended + remaining, total)]
	];
	return `<table class="attlist"><tbody>${rows
		.map(
			([label, value]) =>
				`<tr><td class="cell c0">${label}</td><td class="cell c1">${value}</td></tr>`
		)
		.join('')}</tbody></table>`;
}

function sessionsTable(register: DemoRegister): string {
	const open = register.sessions
		.filter((session) => !session.marked)
		.map(
			(session) =>
				`<tr><td class="datecol">Oggi</td><td class="desccol">Laboratorio</td><td colspan="3"><a href="${SITE}/mod/attendance/attendance.php?sessid=${session.id}&amp;sesskey=demo">Invia presenza</a></td></tr>`
		)
		.join('');
	const past = `<tr><td class="datecol">Settimana scorsa</td><td class="desccol">Laboratorio</td><td class="statuscol">Presente</td><td class="pointscol">1 / 1</td><td class="remarkscol"></td></tr>`;
	return `<table class="generaltable attwidth boxaligncenter"><thead><tr><th>Data</th><th>Descrizione</th><th>Stato</th><th>Punti</th><th>Note</th></tr></thead><tbody>${open}${past}</tbody></table>`;
}

export const routes = [
	route(`GET ${SITE}/mod/attendance/view.php`, ({ url }) => {
		const register = demoRegister(url.searchParams.get('id'));
		if (register === undefined) {
			return html(
				`<!DOCTYPE html><html lang="it"><head><title>Errore</title></head><body id="page-site-index"><div role="main"><p class="errormessage">Modulo del corso non valido</p></div></body></html>`
			);
		}
		return html(`<!DOCTYPE html><html lang="it"><head><title>${register.name}</title></head>
			<body id="page-mod-attendance-view" class="path-mod path-mod-attendance"><div role="main">
				<h2>${register.name}</h2>
				${statsTable(register)}
				${sessionsTable(register)}
			</div></body></html>`);
	})
];
