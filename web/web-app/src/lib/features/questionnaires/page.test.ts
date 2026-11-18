import { decodeEsse3QuestionnairePage } from '@mybicocca/data-api/esse3';
import { describe, expect, it } from 'vitest';
import {
	isAnswered,
	missingAnswers,
	pageAnswers,
	savedAnswers,
	toggleOption,
	toSurveyPage,
	type Answer,
	type Answers,
	type Question,
	type SurveyPage
} from './page.ts';

interface Spec {
	id: number;
	format: string;
	options: string[];
	mandatory?: boolean;
	max?: number;
	/** Indexes of the options that ask for words. */
	other?: number[];
	saved?: { option: number; text?: string }[];
}

const optionId = (question: number, index: number) => question * 10 + index;

function page(questions: Spec[], pageId: number | null = 1): SurveyPage {
	const decoded = decodeEsse3QuestionnairePage({
		paginaId: pageId,
		paragrafi: [
			{
				paragrafoId: 10,
				elementiDes: ' Insegnamento ',
				domande: questions.map((spec) => ({
					domandaId: spec.id,
					elementiDes: `Domanda ${spec.id}`,
					obbligatorioFlg: spec.mandatory === false ? 0 : 1,
					numMaxSce: spec.max ?? null,
					tipoFormatoCod: spec.format,
					rispDisponibili: spec.options.map((text, index) => ({
						rispostaId: optionId(spec.id, index),
						elementiDes: text,
						rispostaFormatoCod: spec.other?.includes(index) ? 'TL_RSP_ALF' : 'TL_RSP_FIX'
					})),
					rispComplete: (spec.saved ?? []).map((answer) => ({
						quesitoId: optionId(spec.id, answer.option),
						testoLibero: answer.text ?? null
					}))
				}))
			}
		]
	});
	if (decoded === undefined) throw new Error('not an object');
	const value = toSurveyPage(decoded);
	if (value === null) throw new Error('no page');
	return value;
}

const AGREEMENT = ['Decisamente no', 'Più no che sì', 'Più sì che no', 'Decisamente sì'];

function only(spec: Spec): Question {
	return page([spec]).questions[0];
}

describe('toSurveyPage', () => {
	it('reads the kind of each question from its format', () => {
		const kinds = page([
			{ id: 1, format: 'TL_DOM_DFS', options: ['Sì', 'No'] },
			{ id: 2, format: 'TL_DOM_OFS', options: AGREEMENT },
			{ id: 3, format: 'TL_DOM_OFS', options: ['1', '2', '10'] },
			{ id: 4, format: 'TL_DOM_DFM', options: ['A', 'B', 'C'], max: 2 },
			{ id: 5, format: 'TL_DOM_LIB', options: [''] },
			{ id: 6, format: 'TL_DOM_VAR', options: ['A', 'B'] },
			{ id: 7, format: 'TL_DOM_VAR', options: [''] }
		]).questions.map((question) => question.kind);
		expect(kinds).toEqual([
			{ type: 'single' },
			{ type: 'scale', numeric: false },
			{ type: 'scale', numeric: true },
			{ type: 'multiple', max: 2 },
			{ type: 'text' },
			{ type: 'single' },
			{ type: 'text' }
		]);
	});

	it('reads the saved answers, where the compiled answer names the chosen option', () => {
		const question = only({
			id: 1,
			format: 'TL_DOM_DFS',
			options: ['Lavoro', 'Altro'],
			other: [1],
			saved: [{ option: 1, text: 'Pendolare' }]
		});
		expect(question.saved).toEqual({ selected: [optionId(1, 1)], text: 'Pendolare' });
		expect(question.options[1].needsText).toBe(true);
	});

	it('is null past the last page', () => {
		const end = decodeEsse3QuestionnairePage({ paginaId: null, paragrafi: [] });
		expect(end && toSurveyPage(end)).toBeNull();
	});
});

describe('isAnswered', () => {
	const other = only({ id: 1, format: 'TL_DOM_DFS', options: ['Lavoro', 'Altro'], other: [1] });

	it('asks an "other" option for its words', () => {
		expect(isAnswered(other, { selected: [optionId(1, 0)], text: '' })).toBe(true);
		expect(isAnswered(other, { selected: [optionId(1, 1)], text: '  ' })).toBe(false);
		expect(isAnswered(other, { selected: [optionId(1, 1)], text: 'Pendolare' })).toBe(true);
		expect(isAnswered(other, { selected: [], text: 'Pendolare' })).toBe(false);
	});

	it('asks a free-text question for some words', () => {
		const text = only({ id: 2, format: 'TL_DOM_LIB', options: [''] });
		expect(isAnswered(text, { selected: [], text: ' ' })).toBe(false);
		expect(isAnswered(text, { selected: [], text: 'Bene' })).toBe(true);
	});

	it('lists only the mandatory questions left unanswered', () => {
		const survey = page([
			{ id: 1, format: 'TL_DOM_OFS', options: AGREEMENT },
			{ id: 2, format: 'TL_DOM_OFS', options: AGREEMENT },
			{ id: 3, format: 'TL_DOM_LIB', options: [''], mandatory: false }
		]);
		const answers: Answers = { 1: { selected: [optionId(1, 2)], text: '' } };
		expect(missingAnswers(survey, answers).map((question) => question.id)).toEqual([2]);
	});

	it('holds back an optional "other" picked without its words', () => {
		const survey = page([
			{ id: 1, format: 'TL_DOM_DFM', options: ['A', 'Altro'], other: [1], mandatory: false },
			{ id: 2, format: 'TL_DOM_DFM', options: ['A', 'Altro'], other: [1], mandatory: false }
		]);
		const answers: Answers = {
			1: { selected: [optionId(1, 0), optionId(1, 1)], text: ' ' },
			2: { selected: [optionId(2, 0)], text: '' }
		};
		expect(missingAnswers(survey, answers).map((question) => question.id)).toEqual([1]);
	});
});

describe('savedAnswers', () => {
	it('starts every question from what the server holds', () => {
		const survey = page([
			{ id: 1, format: 'TL_DOM_OFS', options: AGREEMENT, saved: [{ option: 3 }] },
			{ id: 2, format: 'TL_DOM_OFS', options: AGREEMENT }
		]);
		expect(savedAnswers(survey)).toEqual({
			1: { selected: [optionId(1, 3)], text: '' },
			2: { selected: [], text: '' }
		});
	});
});

describe('toggleOption', () => {
	it('clears a single choice picked again', () => {
		const question = only({ id: 1, format: 'TL_DOM_OFS', options: AGREEMENT });
		const first = toggleOption(question, { selected: [], text: '' }, optionId(1, 0));
		const second = toggleOption(question, first, optionId(1, 3));
		expect(second.selected).toEqual([optionId(1, 3)]);
		expect(toggleOption(question, second, optionId(1, 3)).selected).toEqual([]);
	});

	it('ignores picks past the maximum of a multiple choice', () => {
		const question = only({ id: 1, format: 'TL_DOM_DFM', options: ['A', 'B', 'C'], max: 2 });
		let answer: Answer = { selected: [], text: '' };
		for (const index of [0, 1, 2]) answer = toggleOption(question, answer, optionId(1, index));
		expect(answer.selected).toEqual([optionId(1, 0), optionId(1, 1)]);
		answer = toggleOption(question, answer, optionId(1, 0));
		expect(toggleOption(question, answer, optionId(1, 2)).selected).toEqual([
			optionId(1, 1),
			optionId(1, 2)
		]);
	});
});

describe('pageAnswers', () => {
	it('sends an empty body for fixed options and the words for the others', () => {
		const survey = page([
			{ id: 1, format: 'TL_DOM_DFM', options: ['A', 'Altro'], other: [1] },
			{ id: 2, format: 'TL_DOM_LIB', options: [''] },
			{ id: 3, format: 'TL_DOM_LIB', options: [''], mandatory: false },
			{ id: 4, format: 'TL_DOM_OFS', options: AGREEMENT }
		]);
		const answers: Answers = {
			1: { selected: [optionId(1, 0), optionId(1, 1)], text: ' più esercizi ' },
			2: { selected: [], text: ' Ottimo corso ' },
			3: { selected: [], text: '   ' }
		};
		expect(pageAnswers(survey, answers)).toEqual([
			{ applicationId: 1, answerId: optionId(1, 0), responseBody: '' },
			{ applicationId: 1, answerId: optionId(1, 1), responseBody: 'più esercizi' },
			{ applicationId: 2, answerId: optionId(2, 0), responseBody: 'Ottimo corso' }
		]);
	});
});
