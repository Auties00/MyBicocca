import type { DataError } from '#lib/data/errors.ts';
import { query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { pickPlanHeader } from '#lib/features/calendar/subjects.ts';
import { planHeadersQuery, studentPlanQuery } from '#lib/features/services/sources.ts';
import type { StudyPath } from './path.ts';
import { toStudyPlan, type StudyPlan } from './plan.ts';
import { studyPathQuery, type DraftRequest } from './queries.ts';

/**
 * The career's study plan and path, live. The plan reads the same cached headers and plan as the
 * deadlines and the calendar, so it shows at once when they loaded it. The path loads beside it;
 * its failure only leaves the path name and editing out. Create it while a component
 * initializes, as `query`.
 */
export class StudyPlanData {
	readonly #headers = query(() => session.current && planHeadersQuery(session.current));
	readonly #header = $derived(
		this.#headers.data === undefined ? undefined : (pickPlanHeader(this.#headers.data) ?? null)
	);
	readonly #body = query(() => {
		const scope = session.current;
		const planId = this.#header?.planId;
		return scope && planId !== undefined && planId !== null && studentPlanQuery(scope, planId);
	});
	readonly #path = query(() => session.current && studyPathQuery(session.current));

	/** The plan; null when the career has none, undefined until known. */
	readonly plan: StudyPlan | null | undefined = $derived.by(() => {
		const header = this.#header;
		if (header === undefined) return undefined;
		if (header === null || header.planId === null) return null;
		const body = this.#body.data;
		return body === undefined ? undefined : toStudyPlan(header, body);
	});

	/** The path; null when there is none or it failed, undefined while it loads. */
	readonly path: StudyPath | null | undefined = $derived(
		this.#path.data !== undefined ? this.#path.data : this.#path.error ? null : undefined
	);

	readonly error: DataError | undefined = $derived(this.#headers.error ?? this.#body.error);

	/** Why the path couldn't be loaded, when it couldn't. */
	readonly pathError: DataError | undefined = $derived(
		this.#path.data === undefined ? this.#path.error : undefined
	);

	readonly status: 'pending' | 'success' | 'error' = $derived(
		this.plan !== undefined ? 'success' : this.error ? 'error' : 'pending'
	);

	readonly refreshing = $derived(this.#headers.refreshing || this.#body.refreshing);

	/** When the shown plan was loaded, in epoch milliseconds: the older of its two halves. */
	readonly updatedAt: number | undefined = $derived.by(() => {
		const times = [this.#headers.updatedAt, this.#body.updatedAt].filter(
			(time) => time !== undefined
		);
		return times.length === 0 ? undefined : Math.min(...times);
	});

	/**
	 * What the editor compiles against, when editing is open: the regulation and
	 * schema of the plan, else of the path; undefined when the plan can't be edited now.
	 */
	readonly request: DraftRequest | undefined = $derived.by(() => {
		const { plan, path } = this;
		const scope = session.current;
		if (!scope || plan === undefined || !path?.editingOpen) return undefined;
		const regulationId = plan?.regulationId ?? path.regulationId;
		if (regulationId === null) return undefined;
		return {
			studentId: plan?.studentId || scope.career.id,
			regulationId,
			schemaId: plan?.schemaId ?? path.currentSchemaId ?? 0,
			planId: plan?.planId ?? 0
		};
	});

	retry = async (): Promise<void> => {
		await Promise.all([this.#headers.refresh(), this.#body.refresh(), this.#path.refresh()]);
	};
}
