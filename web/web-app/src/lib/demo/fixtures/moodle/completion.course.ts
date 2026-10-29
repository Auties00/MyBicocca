import { epochSeconds } from '../../dates.ts';
import { json, route } from '../../router.ts';
import { demoCompletion, demoCourseById, demoSections } from './course.course.ts';

const SERVER = 'https://elearning.unimib.it/webservice/rest/server.php';

export const routes = [
	route(
		`POST ${SERVER}?wsfunction=core_completion_get_activities_completion_status`,
		async (request) => {
			const course = demoCourseById((await request.form()).get('courseid'));
			const done = demoCompletion();
			const statuses = (course === undefined ? [] : demoSections(course))
				.flatMap((section) => section.modules)
				.filter((module) => module.completion > 0)
				.map((module) => ({
					cmid: module.id,
					modname: module.modname,
					instance: module.instance,
					state: done.has(module.id) ? 1 : 0,
					timecompleted: done.get(module.id) ?? 0,
					tracking: module.completion,
					valueused: false,
					hascompletion: true,
					isautomatic: false,
					istrackeduser: true,
					uservisible: true,
					details: []
				}));
			return json({ statuses, warnings: [] });
		},
		{ empty: () => json({ statuses: [], warnings: [] }) }
	),
	route(
		`POST ${SERVER}?wsfunction=core_completion_update_activity_completion_status_manually`,
		async (request) => {
			const form = await request.form();
			const cmId = Number(form.get('cmid'));
			if (form.get('completed') === '1') demoCompletion().set(cmId, epochSeconds(new Date()));
			else demoCompletion().delete(cmId);
			return json({ status: true, warnings: [] });
		}
	)
];
