import type {
	Esse3TeachingUnitWithQuestionnaire,
	Esse3TranscriptRowWithQuestionnaireStatus
} from '@mybicocca/data-api/esse3';
import { careerKey } from '#lib/data/keys.ts';
import { policies, type QueryKey, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';

/**
 * The teaching-evaluation event Bicocca configures. Every questionnaire call takes it: with any
 * other value the units come back without a questionnaire, as if the window had closed.
 */
export const EVALUATION_EVENT = 'EV_VAL_DID';

/** `P` lists every activity with a questionnaire, filled in or not; `C` only those still open. */
const EVERY_QUESTIONNAIRE = 'P';

const RECORD_BOOK = 'esse3.questionnaires.getRecordBookQuestionnaires';
const UNITS = 'esse3.questionnaires.getTeachingUnitQuestionnaireEvaluation';

/**
 * The record book's activities with the state of their questionnaires. Today and the services
 * hub can read it for the count of questionnaires to fill in.
 */
export function recordBookQuestionnairesQuery(
	scope: SessionScope
): QueryOptions<Esse3TranscriptRowWithQuestionnaireStatus[]> {
	const matId = scope.career.enrollmentTraitId;
	return {
		key: careerKey(scope, RECORD_BOOK, matId, EVERY_QUESTIONNAIRE),
		load: () =>
			scope.api.esse3.questionnaires.getRecordBookQuestionnaires(matId, {
				questionFilter: EVERY_QUESTIONNAIRE
			}),
		policy: policies.live
	};
}

export function teachingUnitsQuery(
	scope: SessionScope,
	activityChoiceId: number
): QueryOptions<Esse3TeachingUnitWithQuestionnaire> {
	return {
		key: careerKey(scope, UNITS, activityChoiceId, EVALUATION_EVENT),
		load: () =>
			scope.api.esse3.questionnaires.getTeachingUnitQuestionnaireEvaluation(
				activityChoiceId,
				EVALUATION_EVENT
			),
		policy: policies.live
	};
}

/** What a sent questionnaire changes: every activity's status, and its own activity's units. */
export function sentQuestionnaireKeys(scope: SessionScope, activityChoiceId: number): QueryKey[] {
	return [careerKey(scope, RECORD_BOOK), careerKey(scope, UNITS, activityChoiceId)];
}
