import { toDataError, type DataError } from '#lib/data/errors.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import type { PathOption, StudyPath } from './path.ts';
import { loadDraft, type DraftRequest } from './queries.ts';
import { hasChanges, isComplete, pickCourse, type EditableRule } from './rules.ts';

/**
 * A plan being compiled: the path picked and the activities selected, rule by rule. It outlives
 * the page (a resize across the two-pane breakpoint remounts it) until it is sent or discarded.
 * Picks are kept per path, so switching path and back restores them without a reload.
 */
export class PlanEditor {
	readonly request: DraftRequest;
	readonly options: readonly PathOption[];
	readonly currentSchemaId: number | null;

	/** No path is preselected, not even the current one: choosing it is deliberate. */
	selectedSchemaId = $state<number | null>(null);
	/** The selected path's rules; undefined until loaded. */
	rules = $state.raw<readonly EditableRule[] | undefined>();
	loading = $state(false);
	error = $state.raw<DataError | undefined>();

	readonly #scope: SessionScope;
	readonly #drafts: Record<number, readonly EditableRule[]> = {};
	/** Bumped by every pick and load, so a load that lands late doesn't overwrite a newer pick. */
	#ticket = 0;

	constructor(scope: SessionScope, request: DraftRequest, path: StudyPath | null) {
		this.#scope = scope;
		this.request = request;
		this.options = path?.options ?? [];
		this.currentSchemaId = path?.currentSchemaId ?? null;
		if (this.options.length === 0 && request.schemaId > 0) this.selectedSchemaId = request.schemaId;
	}

	/** A first step to pick the path, when the regulation offers any. */
	get choosesPath(): boolean {
		return this.options.length > 0;
	}

	get chosen(): PathOption | undefined {
		return this.options.find((option) => option.schemaId === this.selectedSchemaId);
	}

	get dirty(): boolean {
		const schemaChanged =
			this.currentSchemaId !== null &&
			this.selectedSchemaId !== null &&
			this.selectedSchemaId !== this.currentSchemaId;
		return schemaChanged || hasChanges(this.rules ?? []);
	}

	get complete(): boolean {
		return isComplete(this.rules ?? []);
	}

	select(schemaId: number): void {
		const previous = this.selectedSchemaId;
		if (previous === schemaId) return;
		if (previous !== null && this.rules) this.#drafts[previous] = this.rules;
		this.#ticket++;
		this.selectedSchemaId = schemaId;
		this.rules = this.#drafts[schemaId];
		this.loading = false;
		this.error = undefined;
	}

	/** Loads the selected path's rules; true once they are there. */
	async load(): Promise<boolean> {
		const schemaId = this.selectedSchemaId;
		if (schemaId === null) return false;
		if (this.rules) return true;
		const ticket = ++this.#ticket;
		this.loading = true;
		this.error = undefined;
		try {
			const rules = await loadDraft(this.#scope, this.request, schemaId);
			if (ticket !== this.#ticket) return false;
			this.rules = rules;
			return true;
		} catch (error) {
			if (ticket === this.#ticket) this.error = toDataError(error);
			return false;
		} finally {
			if (ticket === this.#ticket) this.loading = false;
		}
	}

	toggle(ruleId: number, courseId: number): void {
		if (this.rules) this.rules = pickCourse(this.rules, ruleId, courseId);
	}
}

let current: { key: string; editor: PlanEditor } | undefined;

function keyOf(scope: SessionScope, request: DraftRequest): string {
	const { regulationId, schemaId, planId } = request;
	return JSON.stringify([scope.account.id, scope.career.id, regulationId, schemaId, planId]);
}

/** The editor of this plan: the one in progress, or a new one. */
export function planEditor(
	scope: SessionScope,
	request: DraftRequest,
	path: StudyPath | null
): PlanEditor {
	const key = keyOf(scope, request);
	if (current?.key !== key) current = { key, editor: new PlanEditor(scope, request, path) };
	return current.editor;
}

/** Drops the plan in progress: it was sent, or the student left it. */
export function discardEditor(editor: PlanEditor): void {
	if (current?.editor === editor) current = undefined;
}
