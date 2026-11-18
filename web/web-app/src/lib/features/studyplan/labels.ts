import type { Esse3State3 } from '@mybicocca/data-api/esse3';
import { errorMessage } from '#lib/data/errorMessage.ts';
import { formatNumber } from '#lib/format/numbers.ts';
import { m } from '#lib/paraglide/messages.js';
import { getLocale } from '#lib/paraglide/runtime.js';
import type { Status } from '#lib/ui/types.ts';
import { facetLabel, type Approval, type Facet, type PathOption, type StudyPath } from './path.ts';
import { sentenceCase, type PlanType, type PlanYear, type StudyPlan } from './plan.ts';
import {
	requirement,
	selection,
	type EditableRule,
	type RuleUnit,
	type SubmitFailure
} from './rules.ts';

const english = () => getLocale() === 'en';

export function yearTitle(year: number): string {
	return year === 0 ? m.studyplan_year_other() : m.studyplan_year_title({ year });
}

/** "7,5 CFU": the feature counts in CFU in both languages, as the regulations do. */
export function cfu(value: number): string {
	return m.studyplan_cfu({ value: formatNumber(value, { maximumFractionDigits: 1 }) });
}

/** "7 attività · 51 CFU": credits as Android shows them, without decimals. */
export function yearSummary(group: PlanYear): string {
	return m.studyplan_year_summary({
		activities: m.studyplan_activities_count({ count: group.courses.length }),
		credits: cfu(Math.trunc(group.credits))
	});
}

function facetText(value: Facet): string {
	return sentenceCase(facetLabel(value, english()));
}

/** Translated text of Esse3, when it sent the English version. */
export function localized(it: string | null, en: string | null): string | null {
	return (english() ? en : null) ?? it;
}

function planTypeText(type: PlanType): string {
	switch (type) {
		case 'standard':
			return m.studyplan_type_standard();
		case 'individual':
			return m.studyplan_type_individual();
		case 'unknown':
			return m.studyplan_type_generic();
	}
}

/** The path's name, or what a plan without one is: a single path, or a path still to pick. */
export function pathName(path: StudyPath | null): string {
	if (path?.percorso) return facetText(path.percorso);
	return path?.choiceAvailable ? m.studyplan_path_facet() : m.studyplan_path_single();
}

/** "Percorso comune · piano standard"; without a plan, only a path that has a name. */
export function planSubtitle(plan: StudyPlan | null, path: StudyPath | null): string | undefined {
	if (!plan) return path?.percorso ? facetText(path.percorso) : undefined;
	return m.studyplan_subtitle({ path: pathName(path), plan: planTypeText(plan.type) });
}

export interface StateLabel {
	status: Status;
	label: string;
}

export function planState(state: Esse3State3 | null): StateLabel | undefined {
	switch (state) {
		case 'A':
			return { status: 'success', label: m.studyplan_state_approved() };
		case 'P':
			return { status: 'pending', label: m.studyplan_state_proposed() };
		case 'V':
			return { status: 'pending', label: m.studyplan_state_validating() };
		case 'B':
			return { status: 'neutral', label: m.studyplan_state_draft() };
		case 'R':
			return { status: 'danger', label: m.studyplan_state_rejected() };
		case 'X':
			return { status: 'neutral', label: m.studyplan_state_cancelled() };
		default:
			return undefined;
	}
}

export function optionTitle(option: PathOption): string {
	if (option.percorso) return facetText(option.percorso);
	const schema = localized(option.schemaDescription, option.schemaDescriptionEn);
	return schema ?? option.schemaCode ?? m.studyplan_path_fallback();
}

/** Orientamento, profilo and part-time, whatever the option has. */
function facetsOf(option: PathOption): string[] {
	return [option.orientamento, option.profilo, option.partTime].flatMap((facet) =>
		facet ? [facetText(facet)] : []
	);
}

/** The option's title with what tells it apart: "Percorso comune · Tempo parziale". */
export function optionName(option: PathOption): string {
	return [optionTitle(option), ...facetsOf(option)].join(' · ');
}

/** Orientamento · profilo · part-time · languages, whatever the option has. */
export function optionDetails(option: PathOption): string {
	const languages = option.languages.map((language) =>
		english() ? (language.en ?? language.it) : language.it
	);
	return [...facetsOf(option), ...languages].join(' · ');
}

export function approvalText(approval: Approval): string | undefined {
	switch (approval) {
		case 'automatic':
			return m.studyplanedit_approval_automatic();
		case 'manual':
			return m.studyplanedit_approval_manual();
		case 'if-compliant':
			return m.studyplanedit_approval_automatic_if_compliant();
		case 'unknown':
			return undefined;
	}
}

export function submittedText(approval: Approval | undefined): string {
	switch (approval) {
		case 'automatic':
			return m.studyplanedit_submitted_automatic();
		case 'if-compliant':
			return m.studyplanedit_submitted_if_compliant();
		case 'manual':
			return m.studyplanedit_submitted_pending();
		default:
			return m.studyplanedit_submitted_generic();
	}
}

export function failureText(failure: SubmitFailure): string {
	switch (failure.kind) {
		case 'profile':
			return m.studyplanedit_submit_error_profile();
		case 'forbidden':
			return m.studyplanedit_submit_error_forbidden();
		case 'server':
			return failure.message;
		case 'generic':
			return m.studyplanedit_submit_error_generic();
		case 'data':
			return errorMessage(failure.error);
	}
}

const count = (value: number) => formatNumber(value);

function unitName(unit: RuleUnit, amount: number): string {
	return unit === 'credits'
		? m.studyplanedit_unit_credits()
		: m.studyplanedit_unit_activities({ count: amount });
}

/** The rule's requirement in a phrase: "Tra 12 e 16 CFU", "Fino a 2 attività opzionali". */
export function requirementText(rule: EditableRule): string {
	const need = requirement(rule);
	switch (need.kind) {
		case 'mandatory':
			return m.studyplanedit_req_mandatory({ count: count(need.count) });
		case 'optional-exact':
			return m.studyplanedit_req_optional_exact({
				count: count(need.count),
				unit: unitName(need.unit, need.count)
			});
		case 'optional-up-to':
			return m.studyplanedit_req_optional_up_to({
				count: count(need.count),
				unit: unitName(need.unit, need.count)
			});
		case 'optional':
			return need.unit === 'credits'
				? m.studyplanedit_req_optional_credits()
				: m.studyplanedit_req_optional_activities();
		case 'exact':
			return m.studyplanedit_req_exact({
				count: count(need.count),
				unit: unitName(need.unit, need.count)
			});
		case 'between':
			return m.studyplanedit_req_between({
				min: count(need.min),
				max: count(need.max),
				unit: unitName(need.unit, need.max)
			});
		case 'up-to':
			return m.studyplanedit_req_up_to({
				count: count(need.count),
				unit: unitName(need.unit, need.count)
			});
		case 'at-least':
			return m.studyplanedit_req_at_least({
				count: count(need.count),
				unit: unitName(need.unit, need.count)
			});
		case 'free':
			return need.unit === 'credits'
				? m.studyplanedit_req_free_credits()
				: m.studyplanedit_req_free_activities();
	}
}

/** The live selection: "14 di 16 CFU", or "3 attività scelte" when the rule has no bound. */
export function selectionText(rule: EditableRule): string {
	const { selected, target } = selection(rule);
	if (target !== null) {
		return m.studyplanedit_selection_of({
			selected: count(selected),
			target: count(target),
			unit: unitName(rule.unit, target)
		});
	}
	return rule.unit === 'credits'
		? m.studyplanedit_selection_credits({ count: count(selected) })
		: m.studyplanedit_selection_activities({ count: selected });
}

/** What the step is called: the rule's own description, which Android leaves out. */
export function ruleTitle(rule: EditableRule, index: number): string {
	const own = localized(rule.description, rule.descriptionEn)?.replace(/\s*:$/, '');
	return own || m.studyplanedit_rule_fallback({ number: index + 1 });
}
