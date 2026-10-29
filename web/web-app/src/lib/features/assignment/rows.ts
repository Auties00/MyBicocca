import type { MoodleAssignment, MoodleSubmissionStatus } from '@mybicocca/data-api/elearning';
import {
	readConfig,
	readSchedule,
	readSubmission,
	standingOf,
	urgencyOf,
	type FilterSubject,
	type Submission,
	type SubmissionConfig
} from './model.ts';

/** An assignment as the tab lists it, with what its status says once loaded. */
export interface ListedAssignment extends FilterSubject {
	assignment: MoodleAssignment;
	submission: Submission | undefined;
	config: SubmissionConfig | undefined;
	/** The status failed to load and none is cached. */
	failed: boolean;
}

/** A row; urgency stays calm until the status says whether the work is in. */
export function rowOf(
	assignment: MoodleAssignment,
	status: MoodleSubmissionStatus | undefined,
	failed: boolean,
	now: number,
	uploadLimit: number | null
): ListedAssignment {
	const submission = status && readSubmission(status, assignment);
	const schedule = readSchedule(assignment, submission?.extension);
	const config = submission && readConfig(assignment, submission.plugins, uploadLimit, '');
	return {
		assignment,
		submission,
		config,
		failed: failed && submission === undefined,
		schedule,
		standing: submission && standingOf(schedule, submission, config, now),
		urgency: submission ? urgencyOf(schedule, submission.stage, now, submission.canEdit) : 'calm'
	};
}
