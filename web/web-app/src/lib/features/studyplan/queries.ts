import { LocalDate } from '@mybicocca/data-api/runtime';
import { careerKey } from '#lib/data/keys.ts';
import { cache, policies, type QueryKey, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import { pickPlanHeader } from '#lib/features/calendar/subjects.ts';
import { studentPlanQuery } from '#lib/features/services/sources.ts';
import { buildStudyPath, pickRegulation, type StudyPath } from './path.ts';
import { buildRules, type EditableRule } from './rules.ts';

/** The study plan's freshness on Android: 30 minutes in memory. */
const PLAN = policies.fresh(30);

function fetchQuery<T>(options: QueryOptions<T>): Promise<T> {
	return cache.fetch(options.key, options.load, options.policy ?? policies.live);
}

const today = () => {
	const now = new Date();
	return new LocalDate(now.getFullYear(), now.getMonth() + 1, now.getDate());
};

/**
 * The student's path and what the regulation allows. The header is asked again with
 * every optional field: the path names are optional fields. A career without a plan finds its
 * regulation by programme and cohort. Schemas, windows and the structure catalog are each
 * optional: without them the path is still shown, read-only.
 */
export function studyPathQuery(scope: SessionScope): QueryOptions<StudyPath | null> {
	const studentId = scope.career.id;
	return {
		key: careerKey(scope, 'studyplan.path'),
		load: async () => {
			const { plans, choiceRules, structure } = scope.api.esse3;
			const header = pickPlanHeader(
				await plans.getStudentPlanHeaders(studentId, { optionalFields: 'ALL' })
			);
			let regulationId = header?.choiceRegulationId ?? null;
			let courseOfStudyId = header?.courseOfStudyStudentId ?? null;
			let orderId = header?.academicYearOrderStudentId ?? null;
			if (header === undefined) {
				const { programId, academicYearEnrollmentId } = scope.career;
				if (programId <= 0) return null;
				const regulation = pickRegulation(
					await choiceRules
						.getChoiceRegulations({ courseOfStudyId: programId, cohort: academicYearEnrollmentId })
						.catch(() => [])
				);
				if (regulation === undefined) return null;
				regulationId = regulation.choiceRegulationId;
				courseOfStudyId = regulation.courseOfStudyId;
				orderId = regulation.academicYearOrderId;
			}
			const [schemas, windows, catalog] = await Promise.all([
				regulationId === null
					? []
					: choiceRules
							.getStudyPlanSchemas(regulationId, { optionalFields: 'ALL' })
							.catch(() => []),
				regulationId === null
					? []
					: choiceRules.getChoiceRegulationWindows(regulationId).catch(() => []),
				courseOfStudyId === null || orderId === null
					? []
					: structure.getPaths(courseOfStudyId, orderId).catch(() => [])
			]);
			return buildStudyPath({ header, regulationId, schemas, windows, catalog, today: today() });
		},
		policy: PLAN
	};
}

/** What the editor compiles against, from the plan, else the path. */
export interface DraftRequest {
	studentId: number;
	regulationId: number;
	/** The current schema; 0 when there is none yet. */
	schemaId: number;
	/** The plan whose activities start selected; 0 for a first plan. */
	planId: number;
}

/** A schema's rules, with the existing plan's activities selected. */
function draftQuery(
	scope: SessionScope,
	request: DraftRequest,
	schemaId: number
): QueryOptions<EditableRule[]> {
	return {
		key: careerKey(scope, 'studyplan.draft', request.regulationId, schemaId, request.planId),
		load: async () => {
			const [schema, plan] = await Promise.all([
				scope.api.esse3.choiceRules.getStudyPlanSchemaWithDetails(request.regulationId, schemaId),
				request.planId > 0 ? fetchQuery(studentPlanQuery(scope, request.planId)) : undefined
			]);
			return buildRules(schema, plan?.activity ?? []);
		},
		policy: PLAN
	};
}

export function loadDraft(
	scope: SessionScope,
	request: DraftRequest,
	schemaId: number
): Promise<EditableRule[]> {
	return fetchQuery(draftQuery(scope, request, schemaId));
}

/** What a new plan changes: the plan itself, the path, the drafts and the planned lessons. */
export function planInvalidations(scope: SessionScope): QueryKey[] {
	return [
		careerKey(scope, 'esse3.plans.getStudentPlanHeaders'),
		careerKey(scope, 'esse3.plans.getStudentPlan'),
		careerKey(scope, 'studyplan.path'),
		careerKey(scope, 'studyplan.draft'),
		careerKey(scope, 'calendar', 'subjects')
	];
}
