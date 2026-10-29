import type {
	Esse3ChoiceRuleSchemaWithDetails,
	Esse3PlanSchemaWithDetails,
	Esse3PostPlanBody,
	Esse3StudyPlanActivity
} from '@mybicocca/data-api/esse3';
import type { DataError } from '#lib/data/errors.ts';
import type { PathOption } from './path.ts';

/** What a rule's bounds count: credits, or picks for block (`BLK`) rules. */
export type RuleUnit = 'credits' | 'activities';

/** An activity a rule offers. */
export interface EditableCourse {
	/** `sceltaAdId`, unique within the schema. */
	choiceId: number;
	code: string;
	name: string;
	credits: number;
	/** Carried back untouched on submit, as Esse3 requires. */
	courseOfStudyCode: string | null;
	studyPlanCode: string | null;
	academicYearOfferId: number | null;
	selected: boolean;
	/** Activities of mandatory rules: always selected, never toggled. */
	mandatory: boolean;
	/** The selection when the draft was loaded, to tell unsent changes. */
	initiallySelected: boolean;
}

/** A choice rule of the plan schema: one step of the editor. */
export interface EditableRule {
	choiceId: number;
	orderNumber: number;
	description: string | null;
	descriptionEn: string | null;
	/** 0 for rules tied to no year. */
	courseYear: number;
	mandatory: boolean;
	unit: RuleUnit;
	min: number | null;
	max: number | null;
	/** Satisfied with nothing selected (`opzFlg`). */
	optional: boolean;
	courses: EditableCourse[];
	preNote: string | null;
	preNoteEn: string | null;
	postNote: string | null;
	postNoteEn: string | null;
}

const text = (value: string | null) => value?.trim() || null;

/** The codes of the activities the existing plan has chosen. */
function chosenCodes(existing: readonly Esse3StudyPlanActivity[]): Set<string> {
	return new Set(
		existing.flatMap((activity) => {
			const code = activity.contextualizedTeachingActivityKey?.activityCode;
			return activity.choiceFlag !== 0 && code ? [code] : [];
		})
	);
}

/**
 * One editable rule. Mandatory rules (`O`) select everything and always count credits, even
 * when Esse3 marks them `BLK`. Elsewhere an activity starts selected when the existing plan has
 * it, no earlier rule claimed it and it still fits the rule's maximum. An activity claims its
 * code even when it can't be offered (no choice id), as Android does.
 */
function toRule(
	rule: Esse3ChoiceRuleSchemaWithDetails,
	inPlan: ReadonlySet<string>,
	claimed: Set<string>
): EditableRule | undefined {
	if (rule.choiceId === null) return undefined;
	const mandatory = rule.choiceType === 'O';
	const unit: RuleUnit = !mandatory && rule.teachingUnitType === 'BLK' ? 'activities' : 'credits';
	const max = rule.maxTeachingUnit;
	let units = 0;
	const courses: EditableCourse[] = [];
	for (const block of rule.blocks) {
		for (const activity of block.activity) {
			const key = activity.contextualizedTeachingActivityKey;
			const code = key?.activityCode;
			if (!key || !code) continue;
			const credits = activity.weight ?? 0;
			const weight = unit === 'credits' ? credits : 1;
			const selected =
				mandatory ||
				(!claimed.has(code) && inPlan.has(code) && (max === null || units + weight <= max));
			if (selected) {
				claimed.add(code);
				if (!mandatory) units += weight;
			}
			if (activity.teachingActivityChoiceId === null) continue;
			courses.push({
				choiceId: activity.teachingActivityChoiceId,
				code,
				name: key.activityDescription?.trim() || code,
				credits,
				courseOfStudyCode: key.courseOfStudyCode,
				studyPlanCode: key.studyPlanCode,
				academicYearOfferId: key.academicYearOfferId,
				selected,
				mandatory,
				initiallySelected: selected
			});
		}
	}
	if (courses.length === 0) return undefined;
	return {
		choiceId: rule.choiceId,
		orderNumber: rule.orderNumber ?? 0,
		description: text(rule.description),
		descriptionEn: text(rule.descriptionEnglish),
		courseYear: rule.courseYear ?? 0,
		mandatory,
		unit,
		min: rule.minTeachingUnit,
		max,
		optional: rule.optionalFlag === 1,
		courses,
		preNote: text(rule.preNote),
		preNoteEn: text(rule.preNoteEnglish),
		postNote: text(rule.postNote),
		postNoteEn: text(rule.postNoteEnglish)
	};
}

/**
 * The editable rules of `schema`, with the existing plan's activities preselected: years in
 * order, the year-less rules last, each year in the schema's rule order. Rules that offer
 * nothing are left out.
 */
export function buildRules(
	schema: Esse3PlanSchemaWithDetails,
	existing: readonly Esse3StudyPlanActivity[]
): EditableRule[] {
	const inPlan = chosenCodes(existing);
	const claimed = new Set<string>();
	const rules = schema.choiceRules.flatMap((rule) => toRule(rule, inPlan, claimed) ?? []);
	const rank = (year: number) => (year === 0 ? Number.POSITIVE_INFINITY : year);
	return rules.sort(
		(a, b) => rank(a.courseYear) - rank(b.courseYear) || a.orderNumber - b.orderNumber
	);
}

const sum = (courses: readonly EditableCourse[]) =>
	courses.reduce((total, course) => total + course.credits, 0);

/** The selection in the rule's own unit: credits, or the number of picks. */
function selectedUnits(rule: EditableRule): number {
	const selected = rule.courses.filter((course) => course.selected);
	return rule.unit === 'credits' ? sum(selected) : selected.length;
}

function mandatoryCredits(rule: EditableRule): number {
	return sum(rule.courses.filter((course) => course.mandatory));
}

/** The lower bound in force: a mandatory rule asks exactly its activities' credits. */
function effectiveMin(rule: EditableRule): number | null {
	return rule.mandatory ? mandatoryCredits(rule) : rule.min;
}

function effectiveMax(rule: EditableRule): number | null {
	return rule.mandatory ? mandatoryCredits(rule) : rule.max;
}

/** Mandatory rules always hold, optional ones with nothing picked too; the rest by their bounds. */
export function isSatisfied(rule: EditableRule): boolean {
	if (rule.mandatory) return true;
	if (rule.optional && !rule.courses.some((course) => course.selected)) return true;
	const selected = selectedUnits(rule);
	return (rule.min === null || selected >= rule.min) && (rule.max === null || selected <= rule.max);
}

const weightOf = (rule: EditableRule, course: EditableCourse) =>
	rule.unit === 'credits' ? course.credits : 1;

/** Within the rule alone: deselecting is always allowed, selecting while the maximum has room. */
function fitsRule(rule: EditableRule, course: EditableCourse): boolean {
	if (course.mandatory) return false;
	if (course.selected || rule.max === null) return true;
	return selectedUnits(rule) + weightOf(rule, course) <= rule.max;
}

/** Why a course can't be toggled now, if it can't. */
export type CourseLock = 'mandatory' | 'elsewhere' | 'over-cap';

export function courseLock(
	rules: readonly EditableRule[],
	rule: EditableRule,
	course: EditableCourse
): CourseLock | undefined {
	if (course.mandatory) return 'mandatory';
	if (course.selected) return undefined;
	const elsewhere = rules.some(
		(other) =>
			other.choiceId !== rule.choiceId &&
			other.courses.some((item) => item.selected && item.code === course.code)
	);
	if (elsewhere) return 'elsewhere';
	return fitsRule(rule, course) ? undefined : 'over-cap';
}

/** Flips one course; a blocked toggle leaves `rules` as they are. */
export function toggleCourse(
	rules: readonly EditableRule[],
	ruleId: number,
	courseId: number
): readonly EditableRule[] {
	const rule = rules.find((item) => item.choiceId === ruleId);
	const course = rule?.courses.find((item) => item.choiceId === courseId);
	if (!rule || !course || courseLock(rules, rule, course) !== undefined) return rules;
	return rules.map((item) =>
		item !== rule
			? item
			: {
					...item,
					courses: item.courses.map((entry) =>
						entry === course ? { ...entry, selected: !entry.selected } : entry
					)
				}
	);
}

/** A rule taken one pick at a time ("one of these labs"): no two of its activities fit together. */
export function takesOnePick(rule: EditableRule): boolean {
	if (rule.mandatory || rule.max === null) return false;
	const [lightest, next] = rule.courses
		.filter((course) => !course.mandatory)
		.map((course) => weightOf(rule, course))
		.sort((a, b) => a - b);
	return next !== undefined && lightest + next > rule.max;
}

/**
 * The pick a new one replaces in a one-pick rule: the course would exceed the maximum, and
 * dropping the rule's pick makes room for it. Other rules keep their maximum as a hard stop.
 */
export function swapOut(
	rules: readonly EditableRule[],
	rule: EditableRule,
	course: EditableCourse
): EditableCourse | undefined {
	if (!takesOnePick(rule) || courseLock(rules, rule, course) !== 'over-cap') return undefined;
	const picked = rule.courses.filter((item) => item.selected);
	if (picked.length !== 1 || rule.max === null) return undefined;
	return weightOf(rule, course) <= rule.max ? picked[0] : undefined;
}

/** Why a click on the course does nothing, if it does: a swap isn't blocked. */
export function pickLock(
	rules: readonly EditableRule[],
	rule: EditableRule,
	course: EditableCourse
): CourseLock | undefined {
	const lock = courseLock(rules, rule, course);
	return lock === 'over-cap' && swapOut(rules, rule, course) ? undefined : lock;
}

/** Toggles one course, swapping out a one-pick rule's choice when only that makes room. */
export function pickCourse(
	rules: readonly EditableRule[],
	ruleId: number,
	courseId: number
): readonly EditableRule[] {
	const rule = rules.find((item) => item.choiceId === ruleId);
	const course = rule?.courses.find((item) => item.choiceId === courseId);
	if (!rule || !course) return rules;
	const previous = swapOut(rules, rule, course);
	const cleared = previous ? toggleCourse(rules, ruleId, previous.choiceId) : rules;
	return toggleCourse(cleared, ruleId, courseId);
}

/**
 * How far the maximum stops new picks: none of the courses still free fits ('full'), or only
 * some of them ('partial'). Courses chosen under another rule aren't free.
 */
export function capReached(
	rules: readonly EditableRule[],
	rule: EditableRule
): 'full' | 'partial' | undefined {
	const locks = rule.courses
		.filter((course) => !course.selected && !course.mandatory)
		.map((course) => pickLock(rules, rule, course))
		.filter((lock) => lock !== 'elsewhere');
	const over = locks.filter((lock) => lock === 'over-cap').length;
	if (over === 0) return undefined;
	return over === locks.length ? 'full' : 'partial';
}

/** Picks that differ from the plan the draft started from. */
export function hasChanges(rules: readonly EditableRule[]): boolean {
	return rules.some((rule) =>
		rule.courses.some((course) => course.selected !== course.initiallySelected)
	);
}

/** Every rule holds, so the plan can be sent. */
export function isComplete(rules: readonly EditableRule[]): boolean {
	return rules.length > 0 && rules.every(isSatisfied);
}

/** A rule's requirement, worded by the page; a minimum of 0 or less is no minimum. */
export type Requirement =
	| { kind: 'mandatory'; count: number }
	| { kind: 'optional-exact'; count: number; unit: RuleUnit }
	| { kind: 'optional-up-to'; count: number; unit: RuleUnit }
	| { kind: 'optional'; unit: RuleUnit }
	| { kind: 'exact'; count: number; unit: RuleUnit }
	| { kind: 'between'; min: number; max: number; unit: RuleUnit }
	| { kind: 'up-to'; count: number; unit: RuleUnit }
	| { kind: 'at-least'; count: number; unit: RuleUnit }
	| { kind: 'free'; unit: RuleUnit };

export function requirement(rule: EditableRule): Requirement {
	if (rule.mandatory) return { kind: 'mandatory', count: Math.trunc(effectiveMin(rule) ?? 0) };
	const { unit } = rule;
	const floor = rule.min === null ? null : Math.trunc(rule.min);
	const min = floor !== null && floor > 0 ? floor : null;
	const max = rule.max === null ? null : Math.trunc(rule.max);
	if (rule.optional) {
		if (min !== null && max !== null && min === max)
			return { kind: 'optional-exact', count: min, unit };
		if (max !== null) return { kind: 'optional-up-to', count: max, unit };
		return { kind: 'optional', unit };
	}
	if (min !== null && max !== null) {
		return min === max ? { kind: 'exact', count: min, unit } : { kind: 'between', min, max, unit };
	}
	if (max !== null) return { kind: 'up-to', count: max, unit };
	if (min !== null) return { kind: 'at-least', count: min, unit };
	return { kind: 'free', unit };
}

/** The live selection against the rule's target (its maximum, else its minimum). */
export function selection(rule: EditableRule): { selected: number; target: number | null } {
	const target = effectiveMax(rule) ?? effectiveMin(rule);
	return {
		selected: Math.trunc(selectedUnits(rule)),
		target: target === null ? null : Math.trunc(target)
	};
}

/** The selected activities, rule by rule, with their rule. */
export function selectedCourses(
	rules: readonly EditableRule[]
): { rule: EditableRule; course: EditableCourse }[] {
	return rules.flatMap((rule) =>
		rule.courses.filter((course) => course.selected).map((course) => ({ rule, course }))
	);
}

/**
 * The new plan as Esse3 inserts it: a standard plan in proposed state that cancels the valid
 * one. `pdsSceCod` names the path only when it changes; `tipoRegsce` carries the approval.
 */
export function submitBody(
	rules: readonly EditableRule[],
	chosen: PathOption | undefined
): Esse3PostPlanBody {
	return {
		type: 'S',
		state: 'P',
		implementationFlag: false,
		cancelValidPlanFlag: true,
		tafModificationOriginType: null,
		studyPlanChoiceCode: chosen && !chosen.isCurrent ? (chosen.percorso?.code ?? null) : null,
		academicYearOfferId: null,
		academicYearEnrollmentId: null,
		choiceRegulationType:
			chosen?.approval === 'manual' ? 1 : chosen?.approval === 'if-compliant' ? 2 : 0,
		interuniversity: null,
		rules: [],
		activity: selectedCourses(rules).map(({ rule, course }, index) => ({
			itemId: index + 1,
			parentItemId: null,
			generateConventionItemId: null,
			orderNumber: rule.orderNumber,
			courseYear: null,
			anticipatedCourseYear: null,
			overrideFlag: null,
			courseOfStudyTeachingActivityCode: course.courseOfStudyCode,
			orderTeachingActivityId: null,
			studyPlanTeachingActivityCode: course.studyPlanCode,
			academicYearOfferActivityId: course.academicYearOfferId,
			activityCode: course.code,
			activityChoiceId: null,
			treasureFlag: null,
			phdParentTeachingActivityInsertionType: null,
			phdPrograms: null
		}))
	};
}

/** Why a submission failed, as the student is told. */
export type SubmitFailure =
	| { kind: 'profile' }
	| { kind: 'forbidden' }
	/** Esse3's own explanation, in Italian. */
	| { kind: 'server'; message: string }
	| { kind: 'generic' }
	/** Not an answer of Esse3's: offline, a timeout. */
	| { kind: 'data'; error: DataError };

/** Esse3 refuses a profile that may not edit the plan with "Security failed" or "…profilo". */
const PROFILE_REFUSAL = /security failed|profilo/i;

export function submitFailure(error: DataError): SubmitFailure {
	if (error.kind === 'forbidden') {
		return PROFILE_REFUSAL.test(error.message ?? '') ? { kind: 'profile' } : { kind: 'forbidden' };
	}
	if (error.kind === 'rejected') {
		if (PROFILE_REFUSAL.test(error.message)) return { kind: 'profile' };
		const message = error.message.trim();
		return message ? { kind: 'server', message } : { kind: 'generic' };
	}
	return { kind: 'data', error };
}
