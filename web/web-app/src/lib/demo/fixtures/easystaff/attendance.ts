import { DemoCourses, DemoStudent, type DemoCourse } from '../../persona.ts';
import { json, route } from '../../router.ts';
import { demoState } from '../../state.ts';

/**
 * EasyBadge, EasyStaff's classroom attendance: the student's history per course and the lesson
 * code check-in. Lessons attended out of those held so far are the Android demo's. Any code of 4
 * to 12 letters and digits checks in once to the lecture on now (Basi di dati); a shorter or
 * longer one is refused as a wrong code, and a code already used as a repeat.
 */

const BADGE = 'https://gestioneorari.didattica.unimib.it/easybadge/api';

/** The share of lessons a course requires, as the Android demo assumes. */
const REQUIRED = 75;

interface Lessons {
	present: number;
	held: number;
}

const lessons = () =>
	demoState(
		'easystaff.attendance',
		() =>
			new Map<DemoCourse, Lessons>([
				[DemoCourses.algorithms, { present: 38, held: 42 }],
				[DemoCourses.databases, { present: 25, held: 28 }],
				[DemoCourses.operatingSystems, { present: 19, held: 30 }],
				[DemoCourses.statistics, { present: 27, held: 28 }],
				[DemoCourses.languages, { present: 11, held: 29 }]
			])
	);

const usedCodes = () => demoState('easystaff.attendance.codes', () => new Set<string>());

const twoDecimals = (value: number) => Math.round(value * 100) / 100;

function record(course: DemoCourse, { present, held }: Lessons) {
	const percentage = (present * 100) / held;
	const state =
		percentage >= REQUIRED
			? 'frequentante'
			: percentage >= REQUIRED - 20
				? 'in_frequenza'
				: 'non_frequentante';
	return {
		codice: course.code,
		nome: course.name,
		Percentuale: twoDecimals(percentage),
		Frequentate: present,
		OreFatte: present * 2,
		percentuale_conseguimento: twoDecimals(Math.min(100, (percentage / REQUIRED) * 100)),
		Stato: state
	};
}

export const routes = [
	route(
		`POST ${BADGE}/corso_iscritti.php`,
		async (request) => {
			const { Matricola: student } = await request.json<{ Matricola: string }>();
			if (student !== DemoStudent.studentNumber) return json({ [student]: {} });
			const courses = [...lessons()].map(([course, count]) => [
				course.code,
				[record(course, count)]
			]);
			return json({ [student]: Object.fromEntries(courses) });
		},
		{ empty: () => json({ [DemoStudent.studentNumber]: {} }) }
	),
	route(`POST ${BADGE}/TimbratureApi.php`, async (request) => {
		const body = await request.json<{ matricola_studente: string; codice_lezione: string }>();
		const code = body.codice_lezione.trim().toUpperCase();
		if (body.matricola_studente !== DemoStudent.studentNumber) {
			return json({ result: 'failure', message: 'Matricola non riconosciuta' });
		}
		if (!/^[A-Z0-9-]{4,12}$/.test(code)) {
			return json({ result: 'failure', message: 'Codice lezione non valido' });
		}
		if (usedCodes().has(code)) {
			return json({ result: 'failure', message: 'Presenza già registrata per questa lezione' });
		}
		usedCodes().add(code);
		const now = lessons().get(DemoCourses.databases);
		if (now !== undefined) now.present = Math.min(now.held, now.present + 1);
		return json({ result: 'ok', message: '' });
	})
];
