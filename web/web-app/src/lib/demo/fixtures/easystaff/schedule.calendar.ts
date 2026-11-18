import { days, epochSeconds } from '../../dates.ts';
import { DemoCourses, DemoStudent, type DemoCourse } from '../../persona.ts';
import { json, route } from '../../router.ts';

/**
 * EasyStaff's timetable (`grid_call.php`) for the persona's second year, as Android's demo lays
 * it out: the same weekly slots, here for a whole semester around today. Like the real service,
 * a request for any week answers every lesson of the period, each course split into variants
 * named after it (lectures, a lab group, an exercise group, two turni) with their own codes, and
 * every lesson names its course's Esse3 code and its section.
 */

const AGENDA = 'https://gestioneorari.didattica.unimib.it/PortaleStudentiUnimib';

/**
 * A course's variant as EasyStaff lists it: its own code, the course name with a suffix, and the
 * section it teaches (`TU` for the single turno, `TUGEA` its exercise group A, `T1` turno 1).
 */
export interface DemoSubject {
	code: string;
	course: DemoCourse;
	name: string;
	section: string;
}

const variant = (
	code: string,
	course: DemoCourse,
	suffix: string,
	section: string
): DemoSubject => ({ code, course, name: `${course.name} (${suffix})`, section });

const { algorithms, databases, operatingSystems, statistics, languages } = DemoCourses;
const LECTURES = 'TU FRONTALE TURNO UNICO';

export const demoSubjects = {
	algorithms: variant('EC530106', algorithms, LECTURES, 'TU'),
	algorithmsExercises: variant('EC530126', algorithms, 'TUGEA ESER TURNO UNICO GRUPPO A', 'TUGEA'),
	databases: variant('EC530107', databases, LECTURES, 'TU'),
	operatingSystems: variant('EC530108', operatingSystems, LECTURES, 'TU'),
	operatingSystemsLab: variant(
		'EC530128',
		operatingSystems,
		'TUGLA LAB TURNO UNICO GRUPPO A',
		'TUGLA'
	),
	statistics: variant('EC530109', statistics, LECTURES, 'TU'),
	languagesFirst: variant('EC530110', languages, 'T1 FRONTALE TURNO 1', 'T1'),
	languagesSecond: variant('EC530111', languages, 'T2 FRONTALE TURNO 2', 'T2')
} as const;

/**
 * Where a lesson sits in the programme, as EasyStaff writes it (`CodiceCombinato`): the programme
 * with its regulation, the curriculum path, the course's Esse3 code, the section and the campus.
 */
const combinedCode = (subject: DemoSubject) =>
	[
		`${DemoStudent.career.easyStaffProgramCode}-11`,
		'GGG',
		subject.course.code,
		subject.section,
		'MILANO'
	].join('^');

interface Room {
	code: string;
	name: string;
	building: string;
	/** Longitude and latitude, as the embedded map writes them. */
	at: readonly [number, number];
}

const room = (code: string, name: string, building: string, at: [number, number]): Room => ({
	code,
	name,
	building,
	at
});

/* EasyStaff names sites with two digits (`U06`), as the rooms showcase does. */
const ROOMS = {
	u6: room('U6-06', 'Aula U6-06', 'U06', [9.2108, 45.5181]),
	u4: room('U4-08', 'Aula U4-08', 'U04', [9.2122, 45.5138]),
	u1: room('U1-09', 'Aula U1-09', 'U01', [9.2133, 45.5133]),
	u14: room('U14-T024', 'Aula T024', 'U14', [9.2116, 45.5235]),
	u24: room('U24-C2', 'Aula C2', 'U24', [9.2199, 45.5238]),
	u7: room('LAB 713', 'Laboratorio 713', 'U07', [9.2093, 45.5165])
};

interface Slot {
	weekday: number;
	subject: DemoSubject;
	start: [number, number];
	end: [number, number];
	room: Room;
}

const slot = (
	weekday: number,
	subject: DemoSubject,
	start: [number, number],
	end: [number, number],
	where: Room
): Slot => ({ weekday, subject, start, end, room: where });

const S = demoSubjects;

/** Monday is 0. Android's `DemoCalendarRepository` timetable. */
const WEEK: readonly Slot[] = [
	slot(0, S.algorithms, [8, 30], [10, 30], ROOMS.u6),
	slot(0, S.statistics, [10, 30], [12, 30], ROOMS.u4),
	slot(0, S.languagesFirst, [14, 30], [16, 30], ROOMS.u1),
	slot(1, S.operatingSystems, [9, 30], [12, 30], ROOMS.u14),
	slot(1, S.databases, [14, 30], [16, 30], ROOMS.u24),
	slot(2, S.algorithms, [10, 30], [12, 30], ROOMS.u6),
	slot(2, S.languagesSecond, [13, 30], [15, 30], ROOMS.u1),
	slot(3, S.databases, [9, 30], [11, 30], ROOMS.u24),
	slot(3, S.operatingSystemsLab, [13, 30], [15, 30], ROOMS.u7),
	slot(4, S.statistics, [8, 30], [10, 30], ROOMS.u4),
	slot(4, S.algorithmsExercises, [10, 30], [12, 30], ROOMS.u6)
];

const pad = (value: number) => String(value).padStart(2, '0');
const wireDate = (date: Date) =>
	`${pad(date.getDate())}-${pad(date.getMonth() + 1)}-${date.getFullYear()}`;
const wireTime = ([hour, minute]: readonly [number, number]) => `${pad(hour)}:${pad(minute)}`;

/** EasyStaff writes teachers surname first, in capitals. */
const teacherName = (course: DemoCourse) =>
	course.teacher.split(' ').reverse().join(' ').toLocaleUpperCase('it-IT');

const embed = ([lng, lat]: readonly [number, number]) =>
	`<iframe src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d1397.66!2d${lng}!3d${lat}!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!5e0!3m2!1sit!2sit" width="600" height="450" style="border:0;" allowfullscreen="" loading="lazy"></iframe>`;

/** The Monday `weeks` weeks from this week's. */
function monday(weeks: number): Date {
	const now = days(0);
	const offset = (now.getDay() + 6) % 7;
	return days(weeks * 7 - offset);
}

/** The semester: four weeks behind this one to eight ahead, as a semester runs in autumn. */
const FIRST_WEEK = -4;
const LAST_WEEK = 8;

/** A holiday of the period, if it falls in it: the Immacolata, closed like every 8 December. */
function closures(from: Date, to: Date): Date[] {
	return [...new Set([from.getFullYear(), to.getFullYear()])]
		.map((year) => new Date(year, 11, 8))
		.filter((day) => day >= from && day <= to);
}

function lessons(codes: ReadonlySet<string>) {
	const from = monday(FIRST_WEEK);
	const to = monday(LAST_WEEK + 1);
	const holidays = closures(from, to).map((day) => day.toDateString());
	/* Next week's Wednesday programming-languages lecture is called off, as some are. */
	const cancelled = `${monday(1).toDateString()}|2|${S.languagesSecond.code}`;
	const cells: Record<string, unknown>[] = [];
	let id = 1_460_000;
	for (let week = FIRST_WEEK; week <= LAST_WEEK; week++) {
		const start = monday(week);
		for (const entry of WEEK) {
			id++;
			if (!codes.has(entry.subject.code)) continue;
			const day = new Date(start.getFullYear(), start.getMonth(), start.getDate() + entry.weekday);
			if (holidays.includes(day.toDateString())) continue;
			const { subject } = entry;
			const off = `${start.toDateString()}|${entry.weekday}|${subject.code}` === cancelled;
			cells.push({
				tipo: 'Lezione',
				id: String(id),
				nome_insegnamento: subject.name,
				name_original: subject.name,
				codice_insegnamento: subject.code,
				CodiceGenerale: subject.course.code,
				CodiceCombinato: combinedCode(subject),
				data: wireDate(day),
				ora_inizio: wireTime(entry.start),
				ora_fine: wireTime(entry.end),
				orario: `${wireTime(entry.start)} - ${wireTime(entry.end)}`,
				timestamp: epochSeconds(
					new Date(day.getFullYear(), day.getMonth(), day.getDate(), ...entry.start)
				),
				codice_aula: entry.room.code,
				codice_sede: entry.room.building,
				aula: `${entry.room.building}-${entry.room.name}`,
				mostra_aula: true,
				Annullato: off ? '1' : '0',
				codice_docente: String(40000 + subject.course.moodleCourseId),
				docente: teacherName(subject.course),
				mail_docente: '',
				tel_docente: '',
				percorso_didattico: `INFORMATICA [E3101Q] Laurea - ${subject.course.year} - PERCORSO COMUNE`,
				insegnamento_tipo: ['Obbligatorio'],
				maps: embed(entry.room.at),
				highlighted: 0,
				from_esterno: 0
			});
		}
	}
	for (const day of closures(from, to)) {
		cells.push({ tipo: 'chiusura_type', nome: 'Immacolata Concezione', data: wireDate(day) });
	}
	return cells;
}

const programmeCodes = (year: string) =>
	new Set(
		Object.values(demoSubjects)
			.filter((subject) => String(subject.course.year) === year)
			.map((subject) => subject.code)
	);

export const routes = [
	route(
		`POST ${AGENDA}/grid_call.php`,
		async (request) => {
			const form = await request.form();
			const codes =
				form.get('form-type') === 'corso'
					? new Set(
							form
								.getAll('anno2[]')
								.flatMap((year) => [...programmeCodes(year.split('|')[1] ?? '')])
						)
					: new Set(form.getAll('attivita[]'));
			return json({ celle: lessons(codes), anno: form.get('anno') });
		},
		{ empty: () => json({ celle: [] }) }
	)
];
