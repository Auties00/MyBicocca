import {
	decodeEsse3PlanSchema,
	decodeEsse3PlanSchemaWithDetails,
	decodeEsse3StudyPlanActivity,
	decodeEsse3StudyPlanHeader,
	type Esse3PlanSchemaWithDetails,
	type Esse3StudyPlanActivity
} from '@mybicocca/data-api/esse3';
import { LocalDate } from '@mybicocca/data-api/runtime';
import { describe, expect, it } from 'vitest';
import { buildStudyPath, type PathOption } from './path.ts';
import {
	buildRules,
	courseLock,
	hasChanges,
	isComplete,
	isSatisfied,
	capReached,
	pickCourse,
	pickLock,
	requirement,
	selection,
	submitBody,
	submitFailure,
	swapOut,
	takesOnePick,
	toggleCourse,
	type EditableRule
} from './rules.ts';

function wire<T>(decode: (json: unknown) => T | undefined, json: unknown): T {
	const value = decode(json);
	if (value === undefined) throw new Error('not an object');
	return value;
}

interface RuleSpec {
	id: number;
	order?: number;
	year?: number;
	type?: string;
	unit?: string;
	min?: number | null;
	max?: number | null;
	optional?: boolean;
	courses: [code: string, credits: number, choiceId?: number | null][];
}

function schema(...rules: RuleSpec[]): Esse3PlanSchemaWithDetails {
	return wire(decodeEsse3PlanSchemaWithDetails, {
		schemaId: 1,
		regoleDiScelta: rules.map((rule) => ({
			sceltaId: rule.id,
			ordNum: rule.order ?? rule.id,
			annoCorso: rule.year ?? 1,
			tipSce: rule.type ?? 'W',
			tipUnt: rule.unit ?? 'CFU',
			minUnt: rule.min ?? null,
			maxUnt: rule.max ?? null,
			opzFlg: rule.optional ? 1 : 0,
			blocchi: [
				{
					blkId: rule.id,
					attivita: rule.courses.map(([code, credits, choiceId]) => ({
						sceltaAdId: choiceId === undefined ? rule.id * 100 + Number(code.slice(1)) : choiceId,
						peso: credits,
						chiaveADContestualizzata: {
							cdsCod: 'E3101Q',
							pdsCod: 'GGG',
							aaOffId: 2026,
							adCod: code,
							adDes: `Course ${code}`
						}
					}))
				}
			]
		}))
	});
}

function inPlan(...codes: string[]): Esse3StudyPlanActivity[] {
	return codes.map((code) =>
		wire(decodeEsse3StudyPlanActivity, { sceltaFlg: 1, chiaveADContestualizzata: { adCod: code } })
	);
}

const ruleOf = (rules: readonly EditableRule[], id: number) => {
	const rule = rules.find((item) => item.choiceId === id);
	if (!rule) throw new Error(`no rule ${id}`);
	return rule;
};

const picked = (rule: EditableRule) =>
	rule.courses.filter((course) => course.selected).map((course) => course.code);

describe('buildRules', () => {
	it('orders rules by year with the year-less ones last, then by their order', () => {
		const rules = buildRules(
			schema(
				{ id: 1, order: 1, year: 0, courses: [['A1', 6]] },
				{ id: 2, order: 5, year: 2, courses: [['A2', 6]] },
				{ id: 3, order: 2, year: 2, courses: [['A3', 6]] },
				{ id: 4, order: 9, year: 1, courses: [['A4', 6]] }
			),
			[]
		);
		expect(rules.map((rule) => rule.choiceId)).toEqual([4, 3, 2, 1]);
	});

	it('selects every activity of a mandatory rule and measures it in credits', () => {
		const [rule] = buildRules(
			schema({
				id: 1,
				type: 'O',
				unit: 'BLK',
				courses: [
					['A1', 8],
					['A2', 6]
				]
			}),
			[]
		);
		expect(rule.unit).toBe('credits');
		expect(picked(rule)).toEqual(['A1', 'A2']);
		expect(rule.courses.every((course) => course.mandatory && course.initiallySelected)).toBe(true);
	});

	it('preselects activities of the existing plan while they fit the maximum', () => {
		const [rule] = buildRules(
			schema({
				id: 1,
				max: 12,
				courses: [
					['A1', 6],
					['A2', 8],
					['A3', 6]
				]
			}),
			inPlan('A1', 'A2', 'A3')
		);
		expect(picked(rule)).toEqual(['A1', 'A3']);
	});

	it('ignores activities the existing plan lists without choosing them', () => {
		const existing = [
			wire(decodeEsse3StudyPlanActivity, {
				sceltaFlg: 0,
				chiaveADContestualizzata: { adCod: 'A1' }
			})
		];
		const [rule] = buildRules(schema({ id: 1, courses: [['A1', 6]] }), existing);
		expect(picked(rule)).toEqual([]);
	});

	it('lets only the first rule claim an activity offered by several', () => {
		const rules = buildRules(
			schema({ id: 1, courses: [['A1', 6]] }, { id: 2, courses: [['A1', 6]] }),
			inPlan('A1')
		);
		expect(picked(ruleOf(rules, 1))).toEqual(['A1']);
		expect(picked(ruleOf(rules, 2))).toEqual([]);
	});

	it('counts picks for block rules', () => {
		const [rule] = buildRules(
			schema({
				id: 1,
				unit: 'BLK',
				max: 1,
				courses: [
					['A1', 6],
					['A2', 6]
				]
			}),
			inPlan('A1', 'A2')
		);
		expect(rule.unit).toBe('activities');
		expect(picked(rule)).toEqual(['A1']);
	});

	it('drops activities without a choice id and rules left with none', () => {
		const rules = buildRules(
			schema(
				{
					id: 1,
					courses: [
						['A1', 6, null],
						['A2', 6]
					]
				},
				{ id: 2, courses: [['A3', 6, null]] }
			),
			[]
		);
		expect(rules.map((rule) => rule.choiceId)).toEqual([1]);
		expect(rules[0].courses.map((course) => course.code)).toEqual(['A2']);
	});
});

describe('isSatisfied', () => {
	const build = (spec: Omit<RuleSpec, 'id' | 'courses'>, ...selected: string[]) =>
		buildRules(
			schema({
				id: 1,
				...spec,
				courses: [
					['A1', 6],
					['A2', 6],
					['A3', 8]
				]
			}),
			inPlan(...selected)
		)[0];

	it('holds for mandatory rules', () => {
		expect(isSatisfied(build({ type: 'O', min: 100 }))).toBe(true);
	});

	it('holds for an optional rule with nothing selected, and checks bounds once something is', () => {
		expect(isSatisfied(build({ optional: true, min: 12, max: 12 }))).toBe(true);
		expect(isSatisfied(build({ optional: true, min: 12, max: 12 }, 'A1'))).toBe(false);
		expect(isSatisfied(build({ optional: true, min: 12, max: 12 }, 'A1', 'A2'))).toBe(true);
	});

	it('compares the selection with both bounds', () => {
		expect(isSatisfied(build({ min: 12, max: 16 }, 'A1'))).toBe(false);
		expect(isSatisfied(build({ min: 12, max: 16 }, 'A1', 'A2'))).toBe(true);
		expect(isSatisfied(build({ min: 12 }, 'A1', 'A2', 'A3'))).toBe(true);
		expect(isSatisfied(build({}))).toBe(true);
	});
});

describe('toggleCourse and courseLock', () => {
	const rules = buildRules(
		schema(
			{
				id: 1,
				max: 12,
				courses: [
					['A1', 6],
					['A2', 8],
					['A3', 6]
				]
			},
			{ id: 2, type: 'O', courses: [['B1', 8]] },
			{
				id: 3,
				year: 0,
				courses: [
					['A3', 6],
					['C1', 6]
				]
			}
		),
		inPlan('A1')
	);
	const first = ruleOf(rules, 1);
	const course = (rule: EditableRule, code: string) => {
		const found = rule.courses.find((item) => item.code === code);
		if (!found) throw new Error(`no course ${code}`);
		return found;
	};

	it('selects within the maximum and blocks what would exceed it', () => {
		expect(courseLock(rules, first, course(first, 'A3'))).toBeUndefined();
		expect(courseLock(rules, first, course(first, 'A2'))).toBe('over-cap');
		const next = toggleCourse(rules, 1, course(first, 'A2').choiceId);
		expect(next).toBe(rules);
	});

	it('always allows deselecting', () => {
		const next = toggleCourse(rules, 1, course(first, 'A1').choiceId);
		expect(picked(ruleOf(next, 1))).toEqual([]);
		expect(hasChanges(next)).toBe(true);
	});

	it('never toggles mandatory activities', () => {
		const mandatory = ruleOf(rules, 2);
		expect(courseLock(rules, mandatory, course(mandatory, 'B1'))).toBe('mandatory');
		expect(toggleCourse(rules, 2, course(mandatory, 'B1').choiceId)).toBe(rules);
	});

	it('blocks an activity already chosen under another rule', () => {
		const withA3 = toggleCourse(rules, 1, course(first, 'A3').choiceId);
		const other = ruleOf(withA3, 3);
		expect(courseLock(withA3, other, course(other, 'A3'))).toBe('elsewhere');
		expect(toggleCourse(withA3, 3, course(other, 'A3').choiceId)).toBe(withA3);
		expect(courseLock(withA3, other, course(other, 'C1'))).toBeUndefined();
	});

	it('reports no change when the picks are back to the start', () => {
		const id = course(first, 'A3').choiceId;
		expect(hasChanges(toggleCourse(toggleCourse(rules, 1, id), 1, id))).toBe(false);
	});
});

describe('swapOut and pickCourse', () => {
	const rules = buildRules(
		schema(
			{
				id: 1,
				min: 3,
				max: 3,
				courses: [
					['L1', 3],
					['L2', 3],
					['L3', 3]
				]
			},
			{
				id: 2,
				max: 12,
				courses: [
					['A1', 6],
					['A2', 6],
					['A3', 6]
				]
			},
			{ id: 3, year: 0, max: 3, courses: [['L2', 3]] },
			{
				id: 4,
				min: 12,
				max: 14,
				courses: [
					['B1', 8],
					['B2', 8],
					['B3', 6]
				]
			}
		),
		inPlan('L1', 'A1', 'A2', 'B1')
	);
	const course = (rule: EditableRule, code: string) => {
		const found = rule.courses.find((item) => item.code === code);
		if (!found) throw new Error(`no course ${code}`);
		return found;
	};
	const labs = ruleOf(rules, 1);

	it('tells one-pick rules by whether any two of their activities fit together', () => {
		expect(takesOnePick(labs)).toBe(true);
		expect(takesOnePick(ruleOf(rules, 2))).toBe(false);
		expect(takesOnePick(ruleOf(rules, 4))).toBe(false);
	});

	it("replaces a one-pick rule's choice with the new one", () => {
		expect(swapOut(rules, labs, course(labs, 'L3'))?.code).toBe('L1');
		expect(pickLock(rules, labs, course(labs, 'L3'))).toBeUndefined();
		const next = pickCourse(rules, 1, course(labs, 'L3').choiceId);
		expect(picked(ruleOf(next, 1))).toEqual(['L3']);
		expect(isSatisfied(ruleOf(next, 1))).toBe(true);
	});

	it('keeps the maximum of rules with several picks, however many they hold', () => {
		const electives = ruleOf(rules, 2);
		expect(swapOut(rules, electives, course(electives, 'A3'))).toBeUndefined();
		expect(pickCourse(rules, 2, course(electives, 'A3').choiceId)).toBe(rules);
		const core = ruleOf(rules, 4);
		expect(picked(core)).toEqual(['B1']);
		expect(swapOut(rules, core, course(core, 'B2'))).toBeUndefined();
		expect(pickLock(rules, core, course(core, 'B2'))).toBe('over-cap');
		expect(pickCourse(rules, 4, course(core, 'B2').choiceId)).toBe(rules);
	});

	it('never swaps in an activity chosen under another rule', () => {
		const taken = pickCourse(rules, 3, ruleOf(rules, 3).courses[0].choiceId);
		const lab = course(ruleOf(taken, 1), 'L2');
		expect(courseLock(taken, ruleOf(taken, 1), lab)).toBe('elsewhere');
		expect(swapOut(taken, ruleOf(taken, 1), lab)).toBeUndefined();
		expect(pickCourse(taken, 1, lab.choiceId)).toBe(taken);
	});

	it('toggles as usual when the course fits', () => {
		const cleared = pickCourse(rules, 1, course(labs, 'L1').choiceId);
		expect(picked(ruleOf(cleared, 1))).toEqual([]);
		expect(picked(ruleOf(pickCourse(cleared, 1, course(labs, 'L2').choiceId), 1))).toEqual(['L2']);
	});
});

describe('capReached', () => {
	const rules = buildRules(
		schema(
			{
				id: 1,
				max: 16,
				courses: [
					['A1', 8],
					['A2', 6],
					['A3', 6],
					['A4', 2]
				]
			},
			{ id: 2, year: 0, max: 2, unit: 'BLK', courses: [['A3', 6]] },
			{
				id: 3,
				min: 3,
				max: 3,
				courses: [
					['L1', 3],
					['L2', 3]
				]
			}
		),
		inPlan('A1', 'A2', 'L1')
	);
	const pick = (from: readonly EditableRule[], ruleId: number, code: string) => {
		const found = ruleOf(from, ruleId).courses.find((item) => item.code === code);
		if (!found) throw new Error(`no course ${code}`);
		return pickCourse(from, ruleId, found.choiceId);
	};

	it('says whether some or all of the free courses would exceed the maximum', () => {
		expect(capReached(rules, ruleOf(rules, 1))).toBe('partial');
		const full = pick(rules, 1, 'A4');
		expect(capReached(full, ruleOf(full, 1))).toBe('full');
	});

	it('leaves out courses chosen under another rule, and one-pick rules, which swap', () => {
		const elsewhere = pick(pick(rules, 2, 'A3'), 1, 'A4');
		expect(capReached(elsewhere, ruleOf(elsewhere, 1))).toBeUndefined();
		expect(capReached(rules, ruleOf(rules, 3))).toBeUndefined();
	});
});

describe('requirement and selection', () => {
	const rule = (spec: Omit<RuleSpec, 'id' | 'courses'>) =>
		buildRules(
			schema({
				id: 1,
				...spec,
				courses: [
					['A1', 6],
					['A2', 7.5]
				]
			}),
			inPlan('A1', 'A2')
		)[0];

	it('words each kind of bound', () => {
		expect(requirement(rule({ type: 'O' }))).toEqual({ kind: 'mandatory', count: 13 });
		expect(requirement(rule({ optional: true, min: 6, max: 6 }))).toMatchObject({
			kind: 'optional-exact',
			count: 6
		});
		expect(requirement(rule({ optional: true, min: 6, max: 12 }))).toMatchObject({
			kind: 'optional-up-to',
			count: 12
		});
		expect(requirement(rule({ optional: true }))).toMatchObject({ kind: 'optional' });
		expect(requirement(rule({ min: 12, max: 12 }))).toMatchObject({ kind: 'exact', count: 12 });
		expect(requirement(rule({ min: 12, max: 16 }))).toMatchObject({
			kind: 'between',
			min: 12,
			max: 16
		});
		expect(requirement(rule({ max: 16 }))).toMatchObject({ kind: 'up-to', count: 16 });
		expect(requirement(rule({ min: 12 }))).toMatchObject({ kind: 'at-least', count: 12 });
		expect(requirement(rule({ unit: 'BLK' }))).toEqual({ kind: 'free', unit: 'activities' });
	});

	it('reads a minimum of 0 as no minimum', () => {
		expect(requirement(rule({ min: 0, max: 16 }))).toMatchObject({ kind: 'up-to', count: 16 });
	});

	it('counts the selection against the maximum, else the minimum, truncating credits', () => {
		expect(selection(rule({ min: 12, max: 16 }))).toEqual({ selected: 13, target: 16 });
		expect(selection(rule({ min: 12 }))).toEqual({ selected: 13, target: 12 });
		expect(selection(rule({}))).toEqual({ selected: 13, target: null });
		expect(selection(rule({ type: 'O' }))).toEqual({ selected: 13, target: 13 });
	});
});

describe('isComplete', () => {
	it('needs at least one rule and every rule to hold', () => {
		expect(isComplete([])).toBe(false);
		const rules = buildRules(schema({ id: 1, min: 6, courses: [['A1', 6]] }), []);
		expect(isComplete(rules)).toBe(false);
		expect(isComplete(toggleCourse(rules, 1, rules[0].courses[0].choiceId))).toBe(true);
	});
});

describe('submitBody', () => {
	const rules = buildRules(
		schema(
			{ id: 1, order: 3, type: 'O', courses: [['A1', 8]] },
			{
				id: 2,
				order: 4,
				courses: [
					['B1', 6],
					['B2', 6]
				]
			}
		),
		inPlan('B2')
	);
	const option = (fields: Partial<PathOption>): PathOption => ({
		schemaId: 9,
		schemaCode: null,
		schemaDescription: null,
		schemaDescriptionEn: null,
		percorso: { code: 'DSC', description: 'Data science', descriptionEn: null },
		orientamento: null,
		profilo: null,
		partTime: null,
		isCurrent: false,
		approval: 'manual',
		conditionNote: null,
		languages: [],
		...fields
	});

	it('sends only the selected activities, numbered, with their rule and offer keys', () => {
		const body = submitBody(rules, undefined);
		expect(body).toMatchObject({
			type: 'S',
			state: 'P',
			implementationFlag: false,
			cancelValidPlanFlag: true,
			studyPlanChoiceCode: null,
			choiceRegulationType: 0,
			rules: []
		});
		expect(body.activity.map((item) => [item.itemId, item.orderNumber, item.activityCode])).toEqual(
			[
				[1, 3, 'A1'],
				[2, 4, 'B2']
			]
		);
		expect(body.activity[0]).toMatchObject({
			courseOfStudyTeachingActivityCode: 'E3101Q',
			studyPlanTeachingActivityCode: 'GGG',
			academicYearOfferActivityId: 2026
		});
	});

	it('names the path only when it changes, and carries the approval', () => {
		expect(submitBody(rules, option({}))).toMatchObject({
			studyPlanChoiceCode: 'DSC',
			choiceRegulationType: 1
		});
		expect(submitBody(rules, option({ isCurrent: true, approval: 'if-compliant' }))).toMatchObject({
			studyPlanChoiceCode: null,
			choiceRegulationType: 2
		});
	});

	it("resubmits the current path with the approval of the plan's own schema", () => {
		const schemas = [
			{ schemaId: 11, schemaCod: 'GGG', pdsCod: 'GGG', tipoApprovazione: 1 },
			{ schemaId: 10, schemaCod: 'GGG-A', pdsCod: 'GGG', tipoApprovazione: 0 }
		].map((json) => wire(decodeEsse3PlanSchema, { webViewFlg: 1, ...json }));
		const path = buildStudyPath({
			header: wire(decodeEsse3StudyPlanHeader, { pianoId: 3, schemaId: 10, pdsStuCod: 'GGG' }),
			regulationId: 7,
			schemas,
			windows: [],
			catalog: [],
			today: new LocalDate(2026, 10, 27)
		});
		expect(submitBody(rules, path.options[0])).toMatchObject({
			studyPlanChoiceCode: null,
			choiceRegulationType: 0
		});
	});
});

describe('submitFailure', () => {
	const rejected = (message: string) =>
		({ kind: 'rejected', service: 'esse3', status: 422, message }) as const;

	it('explains a profile that may not edit the plan', () => {
		expect(submitFailure(rejected('Operazione non consentita per il profilo'))).toEqual({
			kind: 'profile'
		});
		expect(
			submitFailure({ kind: 'forbidden', service: 'esse3', message: 'Security failed' })
		).toEqual({ kind: 'profile' });
	});

	it('explains a refusal', () => {
		expect(submitFailure({ kind: 'forbidden', service: 'esse3' })).toEqual({ kind: 'forbidden' });
		expect(
			submitFailure({ kind: 'forbidden', service: 'esse3', message: 'Accesso negato' })
		).toEqual({ kind: 'forbidden' });
	});

	it("shows Esse3's own message, or a generic one when it sent none", () => {
		expect(submitFailure(rejected('Regola 3 non rispettata'))).toEqual({
			kind: 'server',
			message: 'Regola 3 non rispettata'
		});
		expect(submitFailure(rejected(''))).toEqual({ kind: 'generic' });
	});

	it('leaves failures before Esse3 answered to the usual messages', () => {
		expect(submitFailure({ kind: 'offline' })).toEqual({
			kind: 'data',
			error: { kind: 'offline' }
		});
	});
});
