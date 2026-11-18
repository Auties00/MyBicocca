import { DemoCareer, DemoStudent } from '../../persona.ts';
import { route, text } from '../../router.ts';
import { demoSubjects } from './schedule.calendar.ts';

/**
 * EasyStaff's catalogue of degree programmes (`combo.php?page=corsi`), a script assigning
 * `elenco_corsi`. The persona's programme lists its courses per year as EasyStaff does: each
 * course as one or more variants named after it, the ones taught this semester with lessons in
 * `schedule.calendar.ts`.
 */

const AGENDA = 'https://gestioneorari.didattica.unimib.it/PortaleStudentiUnimib';

interface WireSubject {
	label: string;
	valore: string;
	id: string;
	id_periodo: string;
	docente: string;
}

const FIRST_SEMESTER = '1533';
const SECOND_SEMESTER = '1534';

const taught = Object.values(demoSubjects);

/** The record book's courses of `year` that aren't taught this semester, under a lecture code. */
function others(year: number): WireSubject[] {
	const current = new Set(taught.map((subject) => subject.course.code));
	return DemoCareer.activities()
		.filter((activity) => activity.year === year && !current.has(activity.code))
		.map((activity, index) => ({
			label: `${activity.name} (TU FRONTALE TURNO UNICO)`,
			valore: `EC5${String(29_000 + year * 100 + index)}`,
			id: String(554_000 + year * 100 + index),
			id_periodo: SECOND_SEMESTER,
			docente: ''
		}));
}

function subjects(year: number): WireSubject[] {
	return [
		...taught
			.filter((subject) => subject.course.year === year)
			.map((subject, index) => ({
				label: subject.name,
				valore: subject.code,
				id: String(555_000 + year * 100 + index),
				id_periodo: FIRST_SEMESTER,
				docente: subject.course.teacher
			})),
		...others(year)
	];
}

function programme() {
	const code = DemoStudent.career.easyStaffProgramCode;
	return {
		label: DemoCareer.degree,
		tipo: 'Laurea',
		valore: code,
		cdl_id: '6165',
		scuola: 'AreaScientifica-Informatica',
		pub_type: 'cal',
		elenco_anni: [1, 2, 3].map((year) => ({
			label: `${year} - PERCORSO COMUNE`,
			valore: `GGG|${year}`,
			elenco_insegnamenti: subjects(year)
		})),
		periodi: [
			{ label: 'Primo Semestre (S1)', valore: 'S1', id: FIRST_SEMESTER },
			{ label: 'Secondo Semestre (S2)', valore: 'S2', id: SECOND_SEMESTER }
		]
	};
}

const script = (programmes: unknown[]) =>
	text(`var elenco_corsi = ${JSON.stringify(programmes)};\n`, 'text/javascript; charset=utf-8');

export const routes = [
	route(`GET ${AGENDA}/combo.php?sw=ec_&page=corsi`, () => script([programme()]), {
		empty: () => script([])
	})
];
