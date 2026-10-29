import { academicYear, academicYearCode } from '#lib/data/academicYear.ts';
import { days, epochSeconds } from '../../dates.ts';
import { DemoCourses, DemoElearning, type DemoCourse } from '../../persona.ts';
import { json, moodleError, route } from '../../router.ts';
import { demoState } from '../../state.ts';

/**
 * The e-learning course list (`core_enrol_get_users_courses`) and self-enrolment
 * (`enrol_self_enrol_user`). Enrolling in a course of the catalog adds it to the list. To try
 * the other outcomes, every course of the catalog whose id is a multiple of 7 asks for an
 * enrolment key (`bicocca`), and every multiple of 11 has self-enrolment switched off.
 */

const SERVER = 'https://elearning.unimib.it/webservice/rest/server.php';
const ENROLMENT_KEY = 'bicocca';

type WireCourse = Record<string, unknown> & { id: number };

const SMALL_WORDS = new Set(['e', 'di', 'del', 'della']);

/** "ALGORITMI E STRUTTURE DATI" as Moodle titles it: "Algoritmi e Strutture Dati". */
function titleCase(name: string): string {
	return name
		.toLowerCase()
		.split(' ')
		.map((word) => (SMALL_WORDS.has(word) ? word : word.charAt(0).toUpperCase() + word.slice(1)))
		.join(' ');
}

function wireCourse(
	course: {
		id: number;
		fullName: string;
		shortName: string;
		idNumber: string;
		progress: number | null;
		favourite: boolean;
		yearsAgo: number;
		lastAccess: Date | null;
	},
	category: number = DemoElearning.categoryId
): WireCourse {
	const shift = course.yearsAgo * 365;
	return {
		id: course.id,
		shortname: course.shortName,
		fullname: course.fullName,
		displayname: course.fullName,
		enrolledusercount: 180,
		idnumber: course.idNumber,
		visible: 1,
		summary: '',
		summaryformat: 1,
		format: 'topics',
		courseimage: '',
		showgrades: true,
		lang: '',
		enablecompletion: course.progress !== null,
		completionhascriteria: false,
		completionusertracked: course.progress !== null,
		category,
		...(course.progress === null ? {} : { progress: course.progress }),
		completed: course.progress === 100,
		startdate: epochSeconds(days(-56 - shift)),
		enddate: epochSeconds(days(63 - shift)),
		marker: 0,
		lastaccess: course.lastAccess ? epochSeconds(course.lastAccess) : 0,
		isfavourite: course.favourite,
		hidden: false,
		overviewfiles: [],
		showactivitydates: true,
		showcompletionconditions: true,
		timemodified: epochSeconds(days(-90 - shift))
	};
}

function idNumberOf(course: DemoCourse): string {
	return `${course.academicYear}-${course.year}-${course.code}`;
}

/**
 * The persona's courses, in the order Moodle lists them. Only courses the course pages' fixtures
 * answer for are listed, so every card opens a working page.
 */
function initialCourses(): WireCourse[] {
	const now = Date.now();
	return DemoCourses.all.map((course, index) =>
		wireCourse({
			id: course.moodleCourseId,
			fullName: titleCase(course.name),
			shortName: idNumberOf(course),
			idNumber: idNumberOf(course),
			progress: Math.round((DemoElearning.progress.get(course) ?? 0) * 100),
			favourite: index < 2,
			yearsAgo: course.academicYear === academicYearCode(academicYear()) ? 0 : 1,
			lastAccess: new Date(now - (2 + index * 19) * 3_600_000)
		})
	);
}

const enrolled = () => demoState('moodle.course.enrolled', initialCourses);

/** The name and idnumber of a course in the list, such as one enrolled from the catalog. */
export function demoListedCourse(id: number): { name: string; idNumber: string } | undefined {
	const course = enrolled().find((candidate) => candidate.id === id);
	return course && { name: String(course.fullname), idNumber: String(course.idnumber) };
}

/** A course of the bundled catalog, as the course list would show it once enrolled. */
async function catalogCourse(id: number): Promise<WireCourse | undefined> {
	const { loadCatalog } = await import('#lib/features/elearning/catalog/source.ts');
	const catalog = await loadCatalog();
	type Node = (typeof catalog.areas)[number]['categories'][number];
	const find = (nodes: readonly Node[]): Node['courses'][number] | undefined => {
		for (const node of nodes) {
			const course = node.courses.find((candidate) => candidate.id === id) ?? find(node.categories);
			if (course) return course;
		}
		return undefined;
	};
	const course = find(catalog.areas.flatMap((area) => area.categories));
	if (!course) return undefined;
	return wireCourse(
		{
			id: course.id,
			fullName: course.name,
			shortName: course.code,
			idNumber: /^\d{4}-\d-/.test(course.code) ? course.code : '',
			progress: 0,
			favourite: false,
			yearsAgo: 0,
			lastAccess: null
		},
		0
	);
}

const english = (url: URL) => url.searchParams.get('moodlewssettinglang') === 'en';

export const routes = [
	route(`POST ${SERVER}?wsfunction=core_enrol_get_users_courses`, () => json(enrolled()), {
		empty: () => json([])
	}),
	route(`POST ${SERVER}?wsfunction=enrol_self_enrol_user`, async (request) => {
		const form = await request.form();
		const id = Number(form.get('courseid'));
		const en = english(request.url);
		const warning = (code: string, message: string) =>
			json({
				status: false,
				warnings: [{ item: 'instance', itemid: id * 10, warningcode: code, message }]
			});
		if (enrolled().some((course) => course.id === id)) {
			return warning(
				'1',
				en ? 'You are already enrolled in this course' : 'Sei già iscritto a questo corso'
			);
		}
		if (id % 11 === 0) {
			return moodleError(
				'canntenrol',
				en ? 'Enrolment is disabled or inactive' : 'Iscrizione disabilitata o non attiva'
			);
		}
		if (id % 7 === 0 && form.get('password') !== ENROLMENT_KEY) {
			return warning(
				'4',
				en ? 'Incorrect enrolment key, please try again' : 'Chiave di iscrizione errata, riprova'
			);
		}
		const course = await catalogCourse(id);
		if (!course) {
			return moodleError(
				'invalidrecord',
				en
					? 'Cannot find data record in database table course.'
					: 'Impossibile trovare il record nella tabella course.'
			);
		}
		enrolled().push(course);
		return json({ status: true, warnings: [] });
	})
];
