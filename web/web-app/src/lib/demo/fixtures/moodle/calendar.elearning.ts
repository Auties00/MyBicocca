import { epochSeconds } from '../../dates.ts';
import { DemoCourses, DemoElearning, type DemoCourse } from '../../persona.ts';
import { json, route } from '../../router.ts';

/**
 * Moodle's action events (`core_calendar_get_action_events_by_timesort`): the persona's
 * assignment due dates and quiz closings, which the course cards, Calendar and Today list.
 */

const SERVER = 'https://elearning.unimib.it/webservice/rest/server.php';
const SITE = 'https://elearning.unimib.it';
export const QUIZ_TITLE = 'Quiz di autovalutazione';

interface DemoEvent {
	id: number;
	course: DemoCourse;
	module: 'assign' | 'quiz';
	cmId: number;
	title: string;
	due: Date;
}

function demoEvents(): DemoEvent[] {
	const assignments = DemoElearning.assignments().map((assignment) => ({
		id: 900_000 + assignment.id,
		course: assignment.course,
		module: 'assign' as const,
		cmId: assignment.cmId,
		title: assignment.title,
		due: assignment.due
	}));
	const quizzes = DemoCourses.all.flatMap((course) => {
		const closes = DemoElearning.quizClosesAt(course);
		return closes === null
			? []
			: [
					{
						id: 910_000 + DemoElearning.quizId(course),
						course,
						module: 'quiz' as const,
						cmId: DemoElearning.cmId(course, DemoElearning.QUIZ),
						title: QUIZ_TITLE,
						due: closes
					}
				];
	});
	// Events already past drop out, as Moodle stops listing them once nothing is left to do.
	return [...assignments, ...quizzes]
		.filter((event) => event.due.getTime() > Date.now() - 60_000)
		.toSorted((a, b) => a.due.getTime() - b.due.getTime() || a.id - b.id);
}

function wireEvent(event: DemoEvent, english: boolean) {
	const due = epochSeconds(event.due);
	const { course } = event;
	const idNumber = `${course.academicYear}-${course.year}-${course.code}`;
	const name =
		event.module === 'assign'
			? english
				? `${event.title} is due`
				: `${event.title} è in scadenza`
			: english
				? `${event.title} closes`
				: `${event.title} si chiude`;
	return {
		id: event.id,
		name,
		description: '',
		descriptionformat: 1,
		location: '',
		categoryid: null,
		groupid: null,
		userid: null,
		repeatid: null,
		eventcount: null,
		component: `mod_${event.module}`,
		modulename: event.module,
		activityname: event.title,
		activitystr: english ? 'Go to activity' : "Vai all'attività",
		instance: event.cmId,
		eventtype: event.module === 'assign' ? 'due' : 'close',
		timestart: due,
		timeduration: 0,
		timesort: due,
		timeusermidnight: epochSeconds(
			new Date(event.due.getFullYear(), event.due.getMonth(), event.due.getDate())
		),
		visible: 1,
		timemodified: due - 30 * 86_400,
		overdue: false,
		icon: {
			key: 'monologo',
			component: event.module,
			alttext: event.module === 'assign' ? 'Compito' : 'Quiz',
			iconurl: `${SITE}/theme/image.php/boost/${event.module}/1/monologo`,
			iconclass: ''
		},
		course: {
			id: course.moodleCourseId,
			fullname: course.name,
			shortname: idNumber,
			idnumber: idNumber,
			summary: '',
			summaryformat: 1,
			startdate: 0,
			enddate: 0,
			visible: true,
			showactivitydates: true,
			showcompletionconditions: true,
			pdfexportfont: '',
			fullnamedisplay: course.name,
			viewurl: `${SITE}/course/view.php?id=${course.moodleCourseId}`,
			courseimage: '',
			hasprogress: false,
			isfavourite: false,
			hidden: false,
			showshortname: false,
			coursecategory: 'Informatica'
		},
		subscription: { displayeventsource: false },
		canedit: false,
		candelete: false,
		deleteurl: '',
		editurl: '',
		viewurl: `${SITE}/mod/${event.module}/view.php?id=${event.cmId}`,
		formattedtime: '',
		formattedlocation: '',
		isactionevent: true,
		iscourseevent: false,
		iscategoryevent: false,
		groupname: null,
		normalisedeventtype: 'course',
		normalisedeventtypetext: english ? 'Course event' : 'Evento del corso',
		action: {
			name:
				event.module === 'assign'
					? english
						? 'Add submission'
						: 'Aggiungi consegna'
					: english
						? 'Attempt quiz now'
						: 'Tenta il quiz adesso',
			url: `${SITE}/mod/${event.module}/view.php?id=${event.cmId}`,
			itemcount: 1,
			actionable: true,
			showitemcount: false
		},
		purpose: 'assessment',
		branded: false,
		url: `${SITE}/mod/${event.module}/view.php?id=${event.cmId}`
	};
}

export const routes = [
	route(
		`POST ${SERVER}?wsfunction=core_calendar_get_action_events_by_timesort`,
		async (request) => {
			const form = await request.form();
			const from = Number(form.get('timesortfrom') ?? 0);
			const after = Number(form.get('aftereventid') ?? 0);
			const limit = Number(form.get('limitnum') ?? 20) || 20;
			const english = request.url.searchParams.get('moodlewssettinglang') === 'en';
			let events = demoEvents().filter((event) => epochSeconds(event.due) >= from);
			const afterIndex = events.findIndex((event) => event.id === after);
			if (afterIndex >= 0) events = events.slice(afterIndex + 1);
			const page = events.slice(0, limit).map((event) => wireEvent(event, english));
			return json({
				events: page,
				firstid: page[0]?.id ?? 0,
				lastid: page.at(-1)?.id ?? 0
			});
		},
		{ empty: () => json({ events: [], firstid: 0, lastid: 0 }) }
	)
];
