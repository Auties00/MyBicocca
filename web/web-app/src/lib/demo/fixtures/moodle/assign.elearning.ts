import { days, epochSeconds } from '../../dates.ts';
import { DemoCourses, DemoElearning } from '../../persona.ts';
import { json, route } from '../../router.ts';
import { assignmentRecord } from './assign.assignment.ts';

/**
 * The assignments of the persona's courses (`mod_assign_get_assignments`), which turn the
 * calendar's course-module ids into assignment ids, with their briefs, rules and statements.
 */

const SERVER = 'https://elearning.unimib.it/webservice/rest/server.php';

/** Moodle's `courseids[0]=…&courseids[1]=…`; none means every course. */
async function courseIds(form: Promise<URLSearchParams>): Promise<Set<number> | null> {
	const ids = [...(await form).entries()]
		.filter(([name]) => name.startsWith('courseids['))
		.map(([, value]) => Number(value));
	return ids.length === 0 ? null : new Set(ids);
}

export const routes = [
	route(
		`POST ${SERVER}?wsfunction=mod_assign_get_assignments`,
		async (request) => {
			const wanted = await courseIds(request.form());
			const english = request.url.searchParams.get('moodlewssettinglang') === 'en';
			const assignments = DemoElearning.assignments();
			const courses = DemoCourses.all
				.filter((course) => wanted === null || wanted.has(course.moodleCourseId))
				.map((course) => ({
					id: course.moodleCourseId,
					fullname: course.name,
					shortname: `${course.academicYear}-${course.year}-${course.code}`,
					timemodified: epochSeconds(days(-90)),
					assignments: assignments
						.filter((assignment) => assignment.course === course)
						.map((assignment) => assignmentRecord(assignment, english))
				}));
			return json({ courses, warnings: [] });
		},
		{ empty: () => json({ courses: [], warnings: [] }) }
	)
];
