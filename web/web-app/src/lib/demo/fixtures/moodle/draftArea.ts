import { DemoStudent } from '../../persona.ts';
import { json, route } from '../../router.ts';
import { DRAFT_CONTEXT, unusedDraftId } from './files.assignment.ts';

/**
 * `core_files_get_unused_draft_itemid`: a draft area of the student's that holds nothing yet. A
 * save naming it clears the files it replaces; an upload to it fills it.
 */
export const routes = [
	route(
		'POST https://elearning.unimib.it/webservice/rest/server.php?wsfunction=core_files_get_unused_draft_itemid',
		() =>
			json({
				component: 'user',
				contextid: DRAFT_CONTEXT,
				userid: DemoStudent.moodle.userId,
				filearea: 'draft',
				itemid: unusedDraftId(),
				warnings: []
			})
	)
];
