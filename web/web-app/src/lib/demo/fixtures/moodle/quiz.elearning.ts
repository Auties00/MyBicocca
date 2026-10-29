import { days, epochSeconds } from '../../dates.ts';
import { DemoCourses, DemoElearning } from '../../persona.ts';
import { json, route } from '../../router.ts';
import { QUIZ_TITLE } from './calendar.elearning.ts';
import { demoQuizIntro } from './course.course.ts';

/**
 * The self-assessment quiz of each persona course (`mod_quiz_get_quizzes_by_courses`), which
 * turns the calendar's course-module ids into quiz ids. Sistemi operativi gives an attempt that
 * runs out of time an hour's grace to be handed in.
 */

const GRACE_SECONDS = 3600;

/** What students see on review, as Moodle's defaults have it: all, the overall feedback once done. */
const ALL_TIMES = 0x11110;
export const REVIEW = {
	reviewattempt: ALL_TIMES,
	reviewcorrectness: ALL_TIMES,
	reviewmaxmarks: ALL_TIMES,
	reviewmarks: ALL_TIMES,
	reviewspecificfeedback: ALL_TIMES,
	reviewgeneralfeedback: ALL_TIMES,
	reviewrightanswer: ALL_TIMES,
	reviewoverallfeedback: 0x01110
};

const SERVER = 'https://elearning.unimib.it/webservice/rest/server.php';

export const routes = [
	route(
		`POST ${SERVER}?wsfunction=mod_quiz_get_quizzes_by_courses`,
		async (request) => {
			const ids = [...(await request.form()).entries()]
				.filter(([name]) => name.startsWith('courseids['))
				.map(([, value]) => Number(value));
			const quizzes = DemoCourses.all
				.filter((course) => ids.length === 0 || ids.includes(course.moodleCourseId))
				.map((course) => {
					const closes = DemoElearning.quizClosesAt(course);
					return {
						id: DemoElearning.quizId(course),
						coursemodule: DemoElearning.cmId(course, DemoElearning.QUIZ),
						course: course.moodleCourseId,
						name: QUIZ_TITLE,
						intro: demoQuizIntro(course),
						introformat: 1,
						introfiles: [],
						section: 1,
						visible: true,
						groupmode: 0,
						groupingid: 0,
						lang: '',
						timeopen: epochSeconds(days(-7, 9)),
						timeclose: closes === null ? 0 : epochSeconds(closes),
						timelimit: 1800,
						...(course === DemoCourses.operatingSystems
							? { overduehandling: 'graceperiod', graceperiod: GRACE_SECONDS }
							: { overduehandling: 'autosubmit', graceperiod: 0 }),
						preferredbehaviour: 'deferredfeedback',
						canredoquestions: 0,
						attempts: 0,
						attemptonlast: 0,
						grademethod: 1,
						decimalpoints: 2,
						questiondecimalpoints: -1,
						questionsperpage: 1,
						navmethod: 'free',
						shuffleanswers: 1,
						sumgrades: 10,
						grade: 10,
						timecreated: epochSeconds(days(-30)),
						timemodified: epochSeconds(days(-8)),
						// Moodle lists a quiz's review settings only while the student may still enter it.
						...(closes !== null && closes.getTime() <= Date.now() ? {} : REVIEW),
						hasfeedback: 1,
						hasquestions: 1
					};
				});
			return json({ quizzes, warnings: [] });
		},
		{ empty: () => json({ quizzes: [], warnings: [] }) }
	)
];
