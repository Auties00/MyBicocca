import { DemoCourses } from '../../persona.ts';
import { json, route } from '../../router.ts';

/**
 * The persona's course groups (`core_group_get_course_user_groups`): Algoritmi splits its lab into
 * shifts, so a new discussion in one of its group forums names the student's shift.
 */

const SERVER = 'https://elearning.unimib.it/webservice/rest/server.php';

function groupsOf(courseId: number) {
	const algorithms = DemoCourses.algorithms.moodleCourseId;
	if (courseId !== algorithms) return [];
	return [
		{
			id: algorithms * 10 + 1,
			name: 'Laboratorio · turno A',
			description: '<p>Lunedì 14:30–16:30, laboratorio U14-T024.</p>',
			descriptionformat: 1,
			idnumber: '',
			courseid: algorithms
		}
	];
}

export const routes = [
	route(
		`POST ${SERVER}?wsfunction=core_group_get_course_user_groups`,
		async (request) => {
			const form = await request.form();
			return json({ groups: groupsOf(Number(form.get('courseid'))), warnings: [] });
		},
		{ empty: () => json({ groups: [], warnings: [] }) }
	)
];
