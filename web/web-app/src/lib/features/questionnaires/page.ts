import type {
	Esse3Answer,
	Esse3Applications,
	Esse3QuestionnairePage
} from '@mybicocca/data-api/esse3';

/** How a question is answered, from Esse3's `tipoFormatoCod`. */
export type QuestionKind =
	| { type: 'single' }
	/** An agreement scale; `numeric` when every step is a number or a letter or two ("1"–"10"). */
	| { type: 'scale'; numeric: boolean }
	| { type: 'multiple'; max: number | null }
	| { type: 'text' };

export interface AnswerOption {
	id: number;
	/** Empty for an "other" option Esse3 leaves unlabelled. */
	text: string;
	/** Picking it asks for a few words too (Esse3's `TL_RSP_ALF`, the "Altro, specificare"). */
	needsText: boolean;
}

export interface Question {
	id: number;
	text: string;
	note: string | null;
	mandatory: boolean;
	kind: QuestionKind;
	options: AnswerOption[];
	/** What the server already holds for it, when the page is visited again. */
	saved: Answer;
}

export interface Section {
	id: number;
	title: string | null;
	questions: Question[];
}

export interface SurveyPage {
	id: number;
	sections: Section[];
	questions: Question[];
}

/** One question's answer while it is being edited. */
export interface Answer {
	selected: readonly number[];
	/** A free-text question's answer, or the words an "other" option asks for. */
	text: string;
}

export type Answers = Readonly<Record<number, Answer>>;

export const NO_ANSWER: Answer = { selected: [], text: '' };

const FREE_TEXT_OPTION = 'TL_RSP_ALF';

function kindOf(question: Esse3Applications, options: readonly AnswerOption[]): QuestionKind {
	switch (question.formatTypeCode) {
		case 'TL_DOM_DFS':
			return { type: 'single' };
		case 'TL_DOM_OFS':
			return {
				type: 'scale',
				numeric: options.length > 0 && options.every((option) => option.text.length <= 2)
			};
		case 'TL_DOM_DFM':
			return { type: 'multiple', max: question.maxChoiceNumber };
		case 'TL_DOM_LIB':
			return { type: 'text' };
		default:
			// An unknown format stays answerable rather than disappearing.
			return options.length > 1 ? { type: 'single' } : { type: 'text' };
	}
}

function toQuestion(question: Esse3Applications): Question | null {
	if (question.applicationId === null) return null;
	const options: AnswerOption[] = [];
	for (const answer of question.availableAnswers) {
		if (answer.answerId === null) continue;
		options.push({
			id: answer.answerId,
			text: answer.elementsDescription?.trim() ?? '',
			needsText: answer.answerFormatCode === FREE_TEXT_OPTION
		});
	}
	// In the compiled answers `quesitoId` is the chosen option, not the question.
	const selected = new Set<number>();
	for (const answer of question.completeAnswers) {
		const id = answer.questionId;
		if (id !== null && options.some((option) => option.id === id)) selected.add(id);
	}
	const text = question.completeAnswers.find((answer) => answer.freeText?.trim())?.freeText ?? '';
	return {
		id: question.applicationId,
		text: question.elementsDescription?.trim() ?? '',
		note: question.elementsNote?.trim() || null,
		mandatory: question.mandatoryFlag === 1,
		kind: kindOf(question, options),
		options,
		saved: { selected: [...selected], text }
	};
}

/**
 * A page of the questionnaire, or null for Esse3's answer past the last page (no `paginaId`),
 * which means the summary comes next.
 */
export function toSurveyPage(page: Esse3QuestionnairePage): SurveyPage | null {
	if (page.pageId === null) return null;
	const sections: Section[] = [];
	for (const paragraph of page.paragraphs) {
		if (paragraph.paragraphId === null) continue;
		sections.push({
			id: paragraph.paragraphId,
			title: paragraph.elementsDescription?.trim() || null,
			questions: paragraph.applications.map(toQuestion).filter((question) => question !== null)
		});
	}
	return { id: page.pageId, sections, questions: sections.flatMap((section) => section.questions) };
}

export function savedAnswers(page: SurveyPage): Answers {
	return Object.fromEntries(page.questions.map((question) => [question.id, question.saved]));
}

/** An "other" option is picked and its words are still blank. */
export function wordsMissing(question: Question, answer: Answer = NO_ANSWER): boolean {
	return (
		question.kind.type !== 'text' &&
		answer.text.trim() === '' &&
		question.options.some((option) => option.needsText && answer.selected.includes(option.id))
	);
}

export function isAnswered(question: Question, answer: Answer = NO_ANSWER): boolean {
	if (question.kind.type === 'text') return answer.text.trim() !== '';
	return answer.selected.length > 0 && !wordsMissing(question, answer);
}

/**
 * The questions that keep `page` from being saved, in page order: mandatory ones unanswered, and
 * optional ones with an "other" picked but not explained, which would reach Esse3 as a bare "Altro".
 */
export function missingAnswers(page: SurveyPage, answers: Answers): Question[] {
	return page.questions.filter((question) => {
		const answer = answers[question.id];
		return question.mandatory ? !isAnswered(question, answer) : wordsMissing(question, answer);
	});
}

/**
 * Picks or unpicks `optionId`. A single choice or scale step picked again is cleared; a multiple
 * choice ignores picks past its maximum.
 */
export function toggleOption(question: Question, answer: Answer, optionId: number): Answer {
	const picked = answer.selected.includes(optionId);
	if (question.kind.type !== 'multiple') {
		return { ...answer, selected: picked ? [] : [optionId] };
	}
	if (picked) return { ...answer, selected: answer.selected.filter((id) => id !== optionId) };
	const { max } = question.kind;
	if (max !== null && max > 0 && answer.selected.length >= max) return answer;
	return { ...answer, selected: [...answer.selected, optionId] };
}

/**
 * What `savePageAnswers` sends for a page. A free-text question answers with its one option; a
 * choice sends each picked option, with an empty body unless it asks for words: Esse3 refuses a
 * missing body (422 NO_TEXT).
 */
export function pageAnswers(page: SurveyPage, answers: Answers): Esse3Answer[] {
	return page.questions.flatMap((question): Esse3Answer[] => {
		const answer = answers[question.id] ?? NO_ANSWER;
		const text = answer.text.trim();
		if (question.kind.type === 'text') {
			const option = question.options[0];
			return option && text
				? [{ applicationId: question.id, answerId: option.id, responseBody: text }]
				: [];
		}
		return question.options
			.filter((option) => answer.selected.includes(option.id))
			.map((option) => ({
				applicationId: question.id,
				answerId: option.id,
				responseBody: option.needsText ? text : ''
			}));
	});
}
