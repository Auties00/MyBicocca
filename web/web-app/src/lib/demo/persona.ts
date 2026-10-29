import { academicYear, academicYearCode } from '#lib/data/academicYear.ts';
import { days, fromAndroid } from './dates.ts';

/**
 * The invented student every demo fixture describes, ported from the Android demo flavor
 * (`android/app/src/demo`) with the same names and data as its screenshots. Nothing here belongs
 * to a real person. Dates keep their distance from today (`fromAndroid`), and the academic years
 * follow from them: the career began in the year its earliest exams allow.
 */
export const DemoStudent = {
	firstName: 'Marco',
	lastName: 'Rossi',
	fullName: 'Marco Rossi',
	username: 'm.rossi99@campus.unimib.it',
	/** Esse3's `userId`. */
	recordUserId: 'm.rossi99',
	studentNumber: '900123',
	fiscalCode: 'RSSMRC03A01F205X',
	personId: 730001,
	/** Esse3's numeric user id. */
	esse3UserId: 760001,
	email: 'm.rossi99@campus.unimib.it',
	moodle: {
		userId: 40001,
		maxUploadBytes: 256 * 1024 * 1024,
		quotaBytes: 1024 * 1024 * 1024
	},
	career: {
		/** `stuId`. */
		id: 700001,
		/** `matId`. */
		enrollmentTraitId: 710001,
		/** `cdsId`. */
		programId: 10001,
		easyStaffProgramCode: 'E3101Q',
		/** `aaIscrId`. */
		academicYearEnrollmentId: 720001,
		description: 'INFORMATICA',
		/** `staStuCod`: active. */
		statusCode: 'A',
		/** `tipoCorsoCod`: bachelor. */
		courseTypeCode: 'L2',
		/**
		 * The academic year the career began in (its first September): the latest one in which every
		 * passed exam still falls in its degree year or later. The exam dates move with today while
		 * academic years turn each September, so it is one or two years before today's.
		 */
		get academicYear(): number {
			const starts = DemoCareer.activities().flatMap(({ passedOn, year }) =>
				passedOn === null ? [] : [academicYear(passedOn) - (year - 1)]
			);
			return Math.min(academicYear() - 1, ...starts);
		}
	}
} as const;

/** One taught course of the demo student's degree, shared by every feature's fixtures. */
export interface DemoCourse {
	code: string;
	name: string;
	shortLabel: string;
	cfu: number;
	teacher: string;
	tutor: string;
	moodleCourseId: number;
	/** The year of the degree it belongs to. */
	year: number;
	/** The Moodle edition's academic year, as idnumbers write it (`2627`). */
	academicYear: string;
}

function course(
	code: string,
	name: string,
	shortLabel: string,
	cfu: number,
	teacher: string,
	tutor: string,
	moodleCourseId: number,
	year = 2,
	yearsAgo = 0
): DemoCourse {
	return {
		code,
		name,
		shortLabel,
		cfu,
		teacher,
		tutor,
		moodleCourseId,
		year,
		academicYear: academicYearCode(academicYear() - yearsAgo)
	};
}

const algorithms = course(
	'E3101Q106',
	'ALGORITMI E STRUTTURE DATI',
	'ASD',
	8,
	'Laura Conti',
	'Matteo Riva',
	5101
);
const databases = course(
	'E3101Q107',
	'BASI DI DATI',
	'BD',
	8,
	'Paolo Ferri',
	'Chiara Fontana',
	5102
);
const operatingSystems = course(
	'E3101Q108',
	'SISTEMI OPERATIVI',
	'SO',
	8,
	'Elena Marchetti',
	'Luca Bassi',
	5103
);
const statistics = course(
	'E3101Q109',
	'PROBABILITÀ E STATISTICA',
	'PS',
	6,
	'Davide Gallo',
	'Anna Villa',
	5104
);
const languages = course(
	'E3101Q110',
	'LINGUAGGI DI PROGRAMMAZIONE',
	'LP',
	8,
	'Sara Colombo',
	'Simone Greco',
	5105
);
// One course each from the first and third year, so the e-learning year filters have something to show.
const programming = course(
	'E3101Q101',
	'PROGRAMMAZIONE 1',
	'PR1',
	8,
	'Marta Bruno',
	'Irene Costa',
	5001,
	1,
	1
);
const softwareEngineering = course(
	'E3101Q115',
	'INGEGNERIA DEL SOFTWARE',
	'IS',
	8,
	'Giorgio Testa',
	'Pietro Longo',
	5201,
	3
);

export const DemoCourses = {
	algorithms,
	databases,
	operatingSystems,
	statistics,
	languages,
	programming,
	softwareEngineering,
	/** The courses attended this semester: the ones with lectures, exam calls and attendance. */
	current: [algorithms, databases, operatingSystems, statistics, languages],
	all: [
		algorithms,
		databases,
		operatingSystems,
		statistics,
		languages,
		programming,
		softwareEngineering
	]
} as const;

/** One activity of the degree, as the Esse3 record book lists it. */
export interface DemoActivity {
	/** `adsceId`. */
	id: number;
	code: string;
	name: string;
	year: number;
	credits: number;
	grade: number | null;
	cumLaude: boolean;
	passedOn: Date | null;
	attending: boolean;
}

const COURSE_OF_STUDY_ID = 10001;
const WEB_LAB_ID = 20110;
const activityId = (of: DemoCourse) => 20000 + of.moodleCourseId;

function activity(
	id: number,
	code: string,
	name: string,
	year: number,
	credits: number,
	grade: number | null = null,
	passedOn: Date | null = null,
	cumLaude = false
): DemoActivity {
	return { id, code, name, year, credits, grade, cumLaude, passedOn, attending: false };
}

/**
 * The Esse3 side of the student: a three-year Informatica degree with the first year and a half
 * passed, the current courses being attended and the third year still to come.
 */
export const DemoCareer = {
	courseOfStudyId: COURSE_OF_STUDY_ID,
	degree: 'INFORMATICA',
	totalCredits: 180,
	/** Sat but not yet recorded: its result still waits for the student to accept it. */
	webLabId: WEB_LAB_ID,
	activityId,
	/** Built on each call: the dates move with today. */
	activities(): DemoActivity[] {
		return [
			activity(
				activityId(programming),
				'E3101Q101',
				'PROGRAMMAZIONE 1',
				1,
				8,
				28,
				fromAndroid(2025, 1, 22)
			),
			activity(
				20002,
				'E3101Q102',
				"FONDAMENTI DELL'INFORMATICA",
				1,
				8,
				27,
				fromAndroid(2025, 2, 10)
			),
			activity(20003, 'E3101Q103', 'ANALISI MATEMATICA', 1, 8, 24, fromAndroid(2025, 2, 24)),
			activity(
				20004,
				'E3101Q104',
				'ARCHITETTURA DEGLI ELABORATORI',
				1,
				8,
				30,
				fromAndroid(2025, 6, 17),
				true
			),
			activity(20005, 'E3101Q105', 'PROGRAMMAZIONE 2', 1, 8, 29, fromAndroid(2025, 7, 3)),
			activity(
				20006,
				'E3101Q111',
				'ALGEBRA LINEARE E GEOMETRIA',
				1,
				8,
				26,
				fromAndroid(2025, 7, 18)
			),
			activity(20007, 'E3101Q112', 'LINGUA INGLESE', 1, 3, null, fromAndroid(2025, 9, 9)),
			activity(20008, 'E3101Q113', 'FISICA', 2, 8, 25, fromAndroid(2026, 1, 20)),
			activity(20009, 'E3101Q114', 'RICERCA OPERATIVA', 2, 8, 28, fromAndroid(2026, 2, 5)),
			activity(WEB_LAB_ID, 'E3101Q119', 'LABORATORIO DI PROGRAMMAZIONE WEB', 2, 3),
			...DemoCourses.current.map((attended) => ({
				...activity(activityId(attended), attended.code, attended.name, 2, attended.cfu),
				attending: true
			})),
			activity(activityId(softwareEngineering), 'E3101Q115', 'INGEGNERIA DEL SOFTWARE', 3, 8),
			activity(20102, 'E3101Q116', 'RETI DI CALCOLATORI', 3, 8),
			activity(20103, 'E3101Q117', 'SICUREZZA INFORMATICA', 3, 6),
			activity(20104, 'E3101Q118', 'INTELLIGENZA ARTIFICIALE', 3, 8),
			activity(20105, 'E3101Q190', 'STAGE', 3, 12),
			activity(20106, 'E3101Q199', 'PROVA FINALE', 3, 4)
		];
	},
	/** The exam call key (`cdsId`, `adId`, `appId`) of `of`'s `call`-th call. */
	callKey(of: DemoCourse, call: number) {
		return { courseOfStudyId: COURSE_OF_STUDY_ID, activityId: activityId(of), callId: call };
	}
} as const;

/** A Moodle assignment of a course; `index` keeps a course's assignments apart. */
export interface DemoAssignment {
	course: DemoCourse;
	index: number;
	title: string;
	due: Date;
	submitted: boolean;
	id: number;
	cmId: number;
}

const cmId = (of: DemoCourse, index: number) => of.moodleCourseId * 100 + index;
const ASSIGNMENT = 80;

function assignment(
	of: DemoCourse,
	index: number,
	title: string,
	due: Date,
	submitted = false
): DemoAssignment {
	return {
		course: of,
		index,
		title,
		due,
		submitted,
		id: of.moodleCourseId * 10 + index,
		cmId: cmId(of, ASSIGNMENT + index)
	};
}

/** The Moodle side of the student: ids and the data several features share. */
export const DemoElearning = {
	/** Activity ids derive from the course's Moodle id, so they never collide. */
	cmId,
	forumId: (of: DemoCourse) => of.moodleCourseId + 2000,
	quizId: (of: DemoCourse) => of.moodleCourseId + 1100,
	FORUM: 1,
	SYLLABUS_PDF: 2,
	ASSIGNMENT,
	QUIZ: 91,
	categoryId: 310,
	topics: new Map<DemoCourse, readonly string[]>([
		[
			algorithms,
			[
				'Analisi asintotica',
				'Ordinamento',
				'Alberi binari di ricerca',
				'Grafi e visite',
				'Programmazione dinamica'
			]
		],
		[
			databases,
			[
				'Modello relazionale',
				'Algebra relazionale',
				'SQL',
				'Progettazione concettuale',
				'Normalizzazione'
			]
		],
		[
			operatingSystems,
			[
				'Processi e thread',
				'Scheduling della CPU',
				'Sincronizzazione',
				'Gestione della memoria',
				'File system'
			]
		],
		[
			statistics,
			[
				'Spazi di probabilità',
				'Variabili aleatorie',
				'Distribuzioni notevoli',
				'Stima puntuale',
				'Test di ipotesi'
			]
		],
		[
			languages,
			[
				'Paradigmi di programmazione',
				'Programmazione funzionale',
				'Sistemi di tipi',
				'Programmazione logica',
				'Interpreti'
			]
		],
		[
			programming,
			['Variabili e tipi', 'Strutture di controllo', 'Funzioni', 'Array e stringhe', 'Ricorsione']
		],
		[softwareEngineering, ['Processi di sviluppo', 'Requisiti', 'UML', 'Design pattern', 'Testing']]
	]),
	/** Share of each course's activities completed. */
	progress: new Map<DemoCourse, number>([
		[algorithms, 0.72],
		[databases, 0.64],
		[operatingSystems, 0.55],
		[statistics, 0.81],
		[languages, 0.38],
		[programming, 1],
		[softwareEngineering, 0.12]
	]),
	/** Courses left out have none, so the pages show that case too. Built on each call. */
	assignments(): DemoAssignment[] {
		return [
			assignment(operatingSystems, 0, 'Esercitazione 6: scheduling', today2359(0), true),
			assignment(operatingSystems, 1, 'Esercitazione 5: processi e thread', today2359(-9)),
			assignment(databases, 0, 'Esercitazione SQL: interrogazioni annidate', today2359(-1)),
			assignment(languages, 0, 'Progetto: interprete', today2359(15)),
			assignment(languages, 1, 'Relazione: analisi lessicale', today2359(6)),
			assignment(statistics, 0, 'Esercitazione a tempo: variabili aleatorie', today2359(3))
		];
	},
	/** Null where the quiz never closes, so that course lists no quiz deadline. */
	quizClosesAt(of: DemoCourse): Date | null {
		return of === databases || of === programming ? null : days(20, 10, 30);
	}
} as const;

function today2359(offset: number): Date {
	return days(offset, 23, 59);
}
