import { describe, expect, it, vi } from 'vitest';
import {
	decodeEsse3QuestionnairePage,
	decodeEsse3QuestionnaireSummary,
	type Esse3Answer,
	type Esse3QuestionnairePage,
	type Esse3QuestionnaireSummary
} from '@mybicocca/data-api/esse3';
import { DecodeError } from '@mybicocca/data-api/runtime';
import { DataFailure } from '#lib/data/errors.ts';
import { cache, policies } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import { Compilation } from './compilation.svelte.ts';
import type { TeachingUnit } from './model.ts';
import { recordBookQuestionnairesQuery, teachingUnitsQuery } from './queries.ts';

const ACTIVITY = 9;
const LAST_PAGE = 3;
const yes = (page: number) => page * 100 + 1;

function decoded<T>(value: T | undefined): T {
	if (value === undefined) throw new Error('not an object');
	return value;
}

/**
 * Esse3's side of a three-page questionnaire, one yes-or-no question per page. It keeps what each
 * page saved and serves it back, as Esse3 does; `calls` records every request in order.
 */
function fakeEsse3() {
	const saved = new Map<number, number>();
	const calls: { name: string; args: unknown[] }[] = [];
	const failures: Partial<Record<string, unknown>> = {};

	function page(id: number | null): Esse3QuestionnairePage {
		const option = id === null ? undefined : saved.get(id);
		return decoded(
			decodeEsse3QuestionnairePage({
				paginaId: id,
				questCompId: 500,
				userCompId: 600,
				paragrafi:
					id === null
						? []
						: [
								{
									paragrafoId: id * 10,
									elementiDes: `Sezione ${id}`,
									domande: [
										{
											domandaId: id * 100,
											elementiDes: `Domanda ${id}`,
											obbligatorioFlg: 1,
											tipoFormatoCod: 'TL_DOM_DFS',
											rispDisponibili: [
												{
													rispostaId: yes(id),
													elementiDes: 'Sì',
													rispostaFormatoCod: 'TL_RSP_FIX'
												},
												{
													rispostaId: id * 100 + 2,
													elementiDes: 'No',
													rispostaFormatoCod: 'TL_RSP_FIX'
												}
											],
											rispComplete: option === undefined ? [] : [{ quesitoId: option }]
										}
									]
								}
							]
			})
		);
	}

	async function answer<T>(name: string, args: unknown[], value: () => T): Promise<T> {
		calls.push({ name, args });
		const failure = failures[name];
		if (failure !== undefined) {
			failures[name] = undefined;
			throw failure;
		}
		return value();
	}

	const api = {
		setNewSurvey: (...args: unknown[]) => answer('setNewSurvey', args, () => page(1)),
		savePageAnswers: (...args: unknown[]) =>
			answer('savePageAnswers', args, () => {
				const [, , , pageId, answers] = args as [number, string, number, number, Esse3Answer[]];
				for (const item of answers) if (item.answerId !== null) saved.set(pageId, item.answerId);
				return 'OK';
			}),
		getNextPage: (...args: unknown[]) =>
			answer('getNextPage', args, () => {
				const from = args[4] as number;
				return page(from < LAST_PAGE ? from + 1 : null);
			}),
		getPreviousPage: (...args: unknown[]) =>
			answer('getPreviousPage', args, () => page((args[4] as number) - 1)),
		getComponentSummary: (...args: unknown[]) =>
			answer('getComponentSummary', args, (): Esse3QuestionnaireSummary =>
				decoded(decodeEsse3QuestionnaireSummary({ completoFlg: saved.size === 3 ? '1' : '0' }))
			),
		putQuestionnaireComponentConfirm: (...args: unknown[]) =>
			answer('putQuestionnaireComponentConfirm', args, () => 'OK')
	};

	return { api, calls, saved, failures };
}

const unit: TeachingUnit = {
	index: 1,
	name: 'Basi di Dati - Teoria',
	lecturer: 'Paolo Ferri',
	partition: null,
	done: false,
	tags: 'UD=1'
};

let accounts = 0;

function setup() {
	const esse3 = fakeEsse3();
	const scope = {
		account: { id: `test-${++accounts}` },
		career: { id: 7, enrollmentTraitId: 70 },
		api: { esse3: { questionnaires: esse3.api } }
	} as unknown as SessionScope;
	const compilation = new Compilation(scope, {
		activityChoiceId: ACTIVITY,
		questionnaireId: 42,
		configId: 43,
		unit,
		anonymous: true
	});
	return { ...esse3, scope, compilation };
}

/** Answers "yes" on the page shown and moves on. */
async function yesAndNext(compilation: Compilation): Promise<void> {
	const step = compilation.step;
	if (step.kind !== 'page') throw new Error(`on ${step.kind}`);
	const [question] = step.page.questions;
	compilation.pick(question, yes(step.page.id));
	expect(await compilation.next()).toBe('moved');
}

describe('Compilation', () => {
	it('starts on Esse3 only when asked, with the evaluation event and the unit token', async () => {
		const { compilation, calls } = setup();
		expect(compilation.step.kind).toBe('ready');
		expect(compilation.open).toBe(false);
		expect(calls).toEqual([]);

		await compilation.start();
		expect(calls[0]).toEqual({
			name: 'setNewSurvey',
			args: ['42', ACTIVITY, 7, { tags: 'UD=1' }, 'EV_VAL_DID', 43]
		});
		expect(compilation.step).toMatchObject({ kind: 'page', index: 0 });
		expect(compilation.open).toBe(true);
	});

	it('stops at unanswered questions, then saves each page before asking for the next', async () => {
		const { compilation, calls } = setup();
		await compilation.start();
		expect(await compilation.next()).toBe('missing');
		expect(compilation.invalid).toEqual([100]);
		expect(calls.map((call) => call.name)).toEqual(['setNewSurvey']);

		await yesAndNext(compilation);
		expect(compilation.invalid).toEqual([]);
		expect(calls.slice(1)).toEqual([
			{
				name: 'savePageAnswers',
				args: [
					7,
					'42',
					500,
					1,
					[{ applicationId: 100, answerId: yes(1), responseBody: '' }],
					'EV_VAL_DID'
				]
			},
			{ name: 'getNextPage', args: [7, ACTIVITY, '42', 500, 1, 600] }
		]);
		expect(compilation.step).toMatchObject({ kind: 'page', index: 1 });
	});

	it('reaches the summary after the last page, and returns from it as the pages were left', async () => {
		const { compilation, calls } = setup();
		await compilation.start();
		for (let page = 1; page <= LAST_PAGE; page++) await yesAndNext(compilation);
		expect(compilation.step).toEqual({ kind: 'summary', complete: true });
		expect(compilation.visited.map((entry) => entry.page.id)).toEqual([1, 2, 3]);

		const before = calls.length;
		await compilation.back();
		expect(compilation.step).toMatchObject({ kind: 'page', index: 2 });
		expect(compilation.answers[300]?.selected).toEqual([yes(3)]);
		expect(calls).toHaveLength(before);

		await compilation.back(0);
		expect(calls.slice(before).filter((call) => call.name === 'getPreviousPage')).toEqual([
			{ name: 'getPreviousPage', args: [7, ACTIVITY, '42', 500, 3, 600] },
			{ name: 'getPreviousPage', args: [7, ACTIVITY, '42', 500, 2, 600] }
		]);
		expect(compilation.step).toMatchObject({ kind: 'page', index: 0 });
		expect(compilation.answers[100]?.selected).toEqual([yes(1)]);
	});

	it('takes a reply that is not JSON as Esse3’s OK, and keeps the page when saving fails', async () => {
		const { compilation, failures } = setup();
		await compilation.start();
		failures.savePageAnswers = new DecodeError('OK is not JSON');
		await yesAndNext(compilation);

		failures.savePageAnswers = new Error('500');
		const step = compilation.step;
		if (step.kind !== 'page') throw new Error('not on a page');
		compilation.pick(step.page.questions[0], yes(2));
		expect(await compilation.next()).toBe('failed');
		expect(compilation.moveError?.move).toBe('next');
		expect(compilation.step).toBe(step);
	});

	it('sends a complete compilation and reloads the course list and that course’s units', async () => {
		const { compilation, scope, calls } = setup();
		const loads = { list: 0, units: 0, other: 0 };
		const watch = (key: readonly (string | number | boolean)[], counter: keyof typeof loads) =>
			cache.observe(
				key,
				async () => {
					loads[counter]++;
					return null;
				},
				policies.live,
				() => {}
			);
		const stops = [
			watch(recordBookQuestionnairesQuery(scope).key, 'list'),
			watch(teachingUnitsQuery(scope, ACTIVITY).key, 'units'),
			watch(teachingUnitsQuery(scope, ACTIVITY + 1).key, 'other')
		];
		await vi.waitFor(() => expect(loads).toEqual({ list: 1, units: 1, other: 1 }));

		await compilation.start();
		for (let page = 1; page <= LAST_PAGE; page++) await yesAndNext(compilation);
		await compilation.send();
		expect(calls.at(-1)).toEqual({
			name: 'putQuestionnaireComponentConfirm',
			args: [ACTIVITY, '42', 500, 7, 43, 600, 'EV_VAL_DID']
		});
		expect(compilation.step.kind).toBe('sent');
		expect(compilation.open).toBe(false);
		await vi.waitFor(() => expect(loads).toEqual({ list: 2, units: 2, other: 1 }));
		for (const stop of stops) stop();
	});

	it('stays on the summary when Esse3 refuses the sending', async () => {
		const { compilation, failures } = setup();
		await compilation.start();
		for (let page = 1; page <= LAST_PAGE; page++) await yesAndNext(compilation);
		failures.putQuestionnaireComponentConfirm = new Error('422');
		await expect(compilation.send()).rejects.toBeInstanceOf(DataFailure);
		expect(compilation.step).toEqual({ kind: 'summary', complete: true });
		expect(compilation.working).toBeNull();
	});
});
