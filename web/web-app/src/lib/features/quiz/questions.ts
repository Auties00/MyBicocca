import type { MoodleQuizQuestion } from '@mybicocca/data-api/elearning';
import { renderMath } from './math.ts';

/** How Moodle marked an answer in a review: on choice rows, cloze gaps and match rows. */
export type Mark = 'correct' | 'partial' | 'incorrect' | null;

export interface ChoiceOption {
	name: string;
	value: string;
	/** Teacher HTML. */
	label: string;
	checked: boolean;
	mark: Mark;
}

export interface TextGap {
	kind: 'text';
	name: string;
	value: string;
	/** Moodle's own label for the gap ("Risposta 1"), when it has one. */
	label: string | null;
	numeric: boolean;
	/** Moodle's `size`, in characters: a hint for the gap's width. */
	size: number | null;
	mark: Mark;
}

export interface MenuGap {
	kind: 'menu';
	name: string;
	value: string;
	label: string | null;
	options: { value: string; label: string }[];
	/** The option meaning "no answer": empty, or 0 where choices count from 1 (matching). */
	blank: string | null;
	mark: Mark;
}

export type Gap = TextGap | MenuGap;

export type QuestionBody =
	| { kind: 'single'; name: string; options: ChoiceOption[] }
	| { kind: 'multi'; options: ChoiceOption[] }
	| { kind: 'text'; gap: TextGap }
	/** `html`: Moodle keeps the answer as HTML, so plain text is converted both ways. */
	| { kind: 'essay'; name: string | null; value: string; html: boolean }
	/** `html` holds a {@link GAP} marker where each gap goes, gaps numbered from 0. */
	| { kind: 'cloze'; html: string; gaps: Gap[] }
	/** A description: text between questions, nothing to answer. */
	| { kind: 'info' }
	| { kind: 'unsupported'; html: string };

export interface Question {
	slot: number;
	type: string;
	/** 0-based. */
	page: number;
	/** "3", or null for descriptions. */
	number: string | null;
	/** Teacher HTML; empty when the text runs inside the gaps' HTML. */
	prompt: string;
	body: QuestionBody;
	/** The hidden fields Moodle sent with the question, sent back unchanged. */
	fields: Readonly<Record<string, string>>;
	/** Every field of the question starts with it (`q1234:5_`); null when no field says. */
	prefix: string | null;
	/** Moodle's complaint about the saved answer ("Inserisci un numero valido"). */
	validation: string | null;
	rightAnswer: string | null;
	feedback: string | null;
	/** The teacher's comment, from manual grading. */
	comment: string | null;
	state: string | null;
	mark: number | null;
	maxMark: number | null;
	flagged: boolean;
}

/** Marks a gap's place in a cloze's HTML: `{index}`, a private-use character. */
export const GAP = '';
const GAP_PATTERN = new RegExp(`${GAP}(\\d+)${GAP}`, 'g');

const NUMERIC_TYPES = new Set(['numerical', 'calculated', 'calculatedsimple']);

/** Hidden from sight in Moodle's own pages: screen-reader text, popups, icons, flags. */
const NOISE = [
	'.accesshide',
	'.sr-only',
	'.visually-hidden',
	'.feedbackspan',
	'.questioncorrectnessicon',
	'.questionflag',
	'.im-controls',
	'script',
	'style'
].join(', ');

/**
 * A question as Moodle renders it (`html` of `get_attempt_data` and `get_attempt_review`), read by
 * its structure rather than its type, so most types any quiz uses are answerable here:
 * choices (multichoice, truefalse, calculatedmulti), essays, cloze-like questions (multianswer,
 * gapselect, match, numerical with units) and single text answers (shortanswer, numerical).
 */
export function parseQuestion(question: MoodleQuizQuestion): Question {
	const document = new DOMParser().parseFromString(`<body>${question.html}</body>`, 'text/html');
	const root = document.body;
	renderMath(root);
	const labels = gapLabels(root);
	for (const element of Array.from(root.querySelectorAll(NOISE))) element.remove();
	showDropPlaces(root);

	const base = {
		slot: question.slot,
		type: question.type,
		page: question.page,
		number: question.number === null ? null : question.questionNumber || String(question.number),
		state: question.state,
		mark: parseMark(question.mark),
		maxMark: question.maxMark,
		flagged: question.flagged,
		rightAnswer: innerHtml(root.querySelector('.rightanswer')),
		feedback: feedbackOf(root),
		comment: innerHtml(root.querySelector('.comment')),
		validation: textOf(root.querySelector('.validationerror'))
	};
	for (const element of Array.from(root.querySelectorAll('.outcome, .comment, .validationerror'))) {
		element.remove();
	}

	const formulation = root.querySelector('.formulation');
	if (formulation === null) {
		return {
			...base,
			prompt: '',
			fields: {},
			prefix: null,
			body: { kind: 'unsupported', html: root.innerHTML.trim() }
		};
	}

	const fields: Record<string, string> = {};
	for (const input of Array.from(
		formulation.querySelectorAll<HTMLInputElement>('input[type="hidden"][name]')
	)) {
		const name = input.getAttribute('name') ?? '';
		if (!name.endsWith(':flagged')) fields[name] = input.getAttribute('value') ?? '';
		input.remove();
	}
	const prefix = prefixOf([
		...Object.keys(fields),
		...Array.from(formulation.querySelectorAll('[name]'), (element) => element.getAttribute('name'))
	]);

	const qtext = formulation.querySelector('.qtext');
	const prompt = innerHtml(qtext) ?? '';
	const body = bodyOf(question.type, formulation, qtext, labels, fields);
	return {
		...base,
		prompt: body.kind === 'cloze' ? '' : prompt,
		fields,
		prefix,
		body
	};
}

function bodyOf(
	type: string,
	formulation: Element,
	qtext: Element | null,
	labels: ReadonlyMap<string, string>,
	fields: Readonly<Record<string, string>>
): QuestionBody {
	if (type === 'description') return { kind: 'info' };

	const rows = Array.from(formulation.querySelectorAll('.answer > *')).filter(
		(row) => row.querySelector('input[type="radio"], input[type="checkbox"]') !== null
	);
	if (rows.length > 0) return choices(rows);

	const textarea = formulation.querySelector('textarea');
	if (textarea !== null) {
		const name = textarea.getAttribute('name');
		// Moodle's format codes: 1 is HTML; 0 (auto), 2 (plain) and 4 (Markdown) keep text.
		return {
			kind: 'essay',
			name,
			value: textarea.textContent ?? '',
			html: name !== null && (fields[`${name}format`] ?? '1') === '1'
		};
	}
	const response = formulation.querySelector('.qtype_essay_response');
	if (response !== null) {
		return { kind: 'essay', name: null, value: response.innerHTML.trim(), html: true };
	}

	const controls = Array.from(formulation.querySelectorAll('input[type="text"], select'));
	const texts = controls.filter((control) => control.tagName === 'INPUT');
	const cloze =
		controls.length > texts.length || texts.length >= 2 || (texts.length === 1 && qtext === null);
	const gapOf = (control: Element): Gap =>
		control.tagName === 'SELECT'
			? menuGap(control, formulation, labels)
			: textGap(control, formulation, labels, type);
	if (cloze) {
		const gaps = controls.map(gapOf);
		controls.forEach((control, index) => control.replaceWith(`${GAP}${index}${GAP}`));
		return { kind: 'cloze', html: formulation.innerHTML.trim(), gaps };
	}
	if (texts.length === 1)
		return { kind: 'text', gap: textGap(texts[0], formulation, labels, type) };
	// The question text shows as the prompt; the rest is what the student would answer there.
	qtext?.remove();
	listDragItems(formulation);
	return { kind: 'unsupported', html: formulation.innerHTML.trim() };
}

/**
 * Drag-and-drop places are empty boxes that Moodle's script fills: as text, each shows the item
 * placed there, else a blank. A place's hidden field (`…_p2`) holds the number of its item within
 * the place's group (`draghome choice3 group1`), 0 when none.
 */
function showDropPlaces(root: Element): void {
	for (const drop of Array.from(root.querySelectorAll('span.drop'))) {
		const place = /\bplace(\d+)\b/.exec(drop.className)?.[1];
		const group = /\bgroup(\d+)\b/.exec(drop.className)?.[1];
		const choice = place
			? root.querySelector(`input[type="hidden"][name$="_p${place}"]`)?.getAttribute('value')
			: undefined;
		const item =
			group && choice && /^[1-9]\d*$/.test(choice)
				? root.querySelector(`.draghome.choice${choice}.group${group}`)
				: null;
		if (item === null) {
			drop.replaceWith('＿＿＿');
			continue;
		}
		const placed = root.ownerDocument.createElement('mark');
		placed.append(...Array.from(item.cloneNode(true).childNodes));
		drop.replaceWith(placed);
	}
}

/**
 * Drag-and-drop items sit side by side with nothing between them but their classes, which the
 * sanitizer drops: as a list, each still reads on its own.
 */
function listDragItems(formulation: Element): void {
	const byHome = new Map<Element, Element[]>();
	for (const item of Array.from(formulation.querySelectorAll('.draghome'))) {
		const home = item.parentElement;
		if (home === null) continue;
		byHome.set(home, [...(byHome.get(home) ?? []), item]);
	}
	for (const [home, items] of byHome) {
		const list = home.ownerDocument.createElement('ul');
		for (const item of items) {
			const entry = home.ownerDocument.createElement('li');
			entry.append(...Array.from(item.childNodes));
			list.append(entry);
		}
		items[0].before(list);
		for (const item of items) item.remove();
	}
}

function choices(rows: Element[]): QuestionBody {
	const options = rows.flatMap((row): ChoiceOption[] => {
		const input = row.querySelector<HTMLInputElement>(
			'input[type="radio"], input[type="checkbox"]'
		);
		const name = input?.getAttribute('name');
		if (!input || !name) return [];
		return [
			{
				name,
				value: input.getAttribute('value') ?? '',
				label: choiceLabel(row, input),
				checked: input.hasAttribute('checked'),
				mark: markOf(row)
			}
		];
	});
	const multi = rows.some((row) => row.querySelector('input[type="checkbox"]') !== null);
	return multi
		? { kind: 'multi', options }
		: { kind: 'single', name: options[0]?.name ?? '', options };
}

/** The element `aria-labelledby` names, else the row's label, else the row; without its number. */
function choiceLabel(row: Element, input: Element): string {
	const labelledBy = input.getAttribute('aria-labelledby');
	const source =
		(labelledBy ? row.ownerDocument.getElementById(labelledBy) : null) ??
		row.querySelector('label') ??
		row;
	const copy = source.cloneNode(true) as Element;
	for (const element of Array.from(copy.querySelectorAll('.answernumber, input'))) {
		element.remove();
	}
	return copy.innerHTML.trim();
}

function gapLabel(control: Element, labels: ReadonlyMap<string, string>): string | null {
	const id = control.getAttribute('id');
	return (id && labels.get(id)) || control.getAttribute('aria-label') || null;
}

function menuGap(
	select: Element,
	formulation: Element,
	labels: ReadonlyMap<string, string>
): MenuGap {
	const options = Array.from(select.querySelectorAll('option'), (option) => ({
		value: option.getAttribute('value') ?? option.textContent ?? '',
		label: (option.textContent ?? '').trim()
	}));
	const values = options.map((option) => option.value);
	const blank = values.includes('') ? '' : values[0] === '0' && values[1] === '1' ? '0' : null;
	return {
		kind: 'menu',
		name: select.getAttribute('name') ?? '',
		value: select.querySelector('option[selected]')?.getAttribute('value') ?? blank ?? '',
		label: gapLabel(select, labels),
		options,
		blank,
		mark: markOf(select, formulation)
	};
}

function textGap(
	input: Element,
	formulation: Element,
	labels: ReadonlyMap<string, string>,
	type: string
): TextGap {
	const size = Number(input.getAttribute('size'));
	return {
		kind: 'text',
		name: input.getAttribute('name') ?? '',
		value: input.getAttribute('value') ?? '',
		label: gapLabel(input, labels),
		numeric: NUMERIC_TYPES.has(type),
		size: Number.isFinite(size) && size > 0 ? size : null,
		mark: markOf(input, formulation)
	};
}

/** Labels of the gaps, read before Moodle's screen-reader text is cleared from sight. */
function gapLabels(root: Element): Map<string, string> {
	const labels = new Map<string, string>();
	for (const label of Array.from(root.querySelectorAll('label[for]'))) {
		const text = (label.textContent ?? '').replace(/\s+/g, ' ').trim();
		const target = label.getAttribute('for');
		if (target && text) labels.set(target, text);
	}
	return labels;
}

/** Moodle classes the marked element, or the cell or span around it. */
function markOf(element: Element, stop?: Element): Mark {
	for (let node: Element | null = element; node && node !== stop; node = node.parentElement) {
		if (node.classList.contains('correct')) return 'correct';
		if (node.classList.contains('partiallycorrect')) return 'partial';
		if (node.classList.contains('incorrect')) return 'incorrect';
		if (node.classList.contains('formulation')) return null;
	}
	return null;
}

function prefixOf(names: readonly (string | null)[]): string | null {
	for (const name of names) {
		const match = name ? /^(q\d+:\d+_)/.exec(name) : null;
		if (match) return match[1];
	}
	return null;
}

function feedbackOf(root: Element): string | null {
	const parts = ['.specificfeedback', '.numpartscorrect', '.generalfeedback']
		.map((selector) => innerHtml(root.querySelector(selector)))
		.filter((html): html is string => html !== null);
	return parts.length === 0 ? null : parts.join('<br>');
}

function innerHtml(element: Element | null): string | null {
	const html = element?.innerHTML.trim();
	return html ? html : null;
}

function textOf(element: Element | null): string | null {
	const text = element?.textContent?.replace(/\s+/g, ' ').trim();
	return text ? text : null;
}

/** Moodle writes marks in the site's locale: "0,50". */
export function parseMark(mark: string | null): number | null {
	if (mark === null || mark.trim() === '') return null;
	const value = Number(mark.trim().replace(',', '.'));
	return Number.isFinite(value) ? value : null;
}

/** The gaps of a cloze's HTML, in order, around the text between them. */
export function splitGaps(html: string): (string | number)[] {
	const parts: (string | number)[] = [];
	let last = 0;
	for (const match of html.matchAll(GAP_PATTERN)) {
		parts.push(html.slice(last, match.index));
		parts.push(Number(match[1]));
		last = match.index + match[0].length;
	}
	parts.push(html.slice(last));
	return parts;
}

/**
 * Keys a question's card on the page: a new sequence check is a new state of the question, as
 * Moodle rendered it again, so its card starts afresh.
 */
export function questionKey(question: Question): string {
	const sequence =
		question.prefix === null ? undefined : question.fields[`${question.prefix}:sequencecheck`];
	return `${question.slot}:${sequence ?? ''}`;
}

/** What the student changed, by field name, over what the page showed. */
export type Response = Readonly<Record<string, string>>;

/**
 * Everything Moodle needs to save `question`: its hidden fields (the sequence check above all,
 * without which Moodle refuses the save), every control's value, and the flag. Every choice of a
 * multiple-answer question and every gap is sent, so a partly answered question keeps its parts.
 */
export function questionFields(
	question: Question,
	response: Response,
	flagged?: boolean
): Record<string, string> {
	const fields: Record<string, string> = { ...question.fields };
	const body = question.body;
	switch (body.kind) {
		case 'single': {
			const value = response[body.name] ?? body.options.find((option) => option.checked)?.value;
			if (value !== undefined) fields[body.name] = value;
			break;
		}
		case 'multi':
			for (const option of body.options) {
				fields[option.name] = response[option.name] ?? (option.checked ? '1' : '0');
			}
			break;
		case 'text':
			fields[body.gap.name] = response[body.gap.name] ?? body.gap.value;
			break;
		case 'essay':
			if (body.name !== null) fields[body.name] = response[body.name] ?? body.value;
			break;
		case 'cloze':
			for (const gap of body.gaps) fields[gap.name] = response[gap.name] ?? gap.value;
			break;
		case 'info':
		case 'unsupported':
			break;
	}
	if (flagged !== undefined && question.prefix !== null) {
		fields[`${question.prefix}:flagged`] = flagged ? '1' : '0';
	}
	return fields;
}

/**
 * `fields` as the save and submit calls take them. An emptied answer goes as an empty value, as
 * Moodle's own form sends it: that is how Moodle reads "no answer".
 */
export function toKeyValues(fields: Readonly<Record<string, string>>) {
	return Object.entries(fields)
		.filter(([name]) => name !== '')
		.map(([name, value]) => ({ name, value }));
}

export type Answered = 'complete' | 'partial' | 'empty';

/** Whether `question` has an answer once `response` applies: what the navigator shows. */
export function answered(question: Question, response: Response): Answered {
	const body = question.body;
	const filled = (value: string | undefined) => value !== undefined && value.trim() !== '';
	switch (body.kind) {
		case 'single': {
			const value = response[body.name] ?? body.options.find((option) => option.checked)?.value;
			return value !== undefined && value !== '-1' ? 'complete' : 'empty';
		}
		case 'multi':
			return body.options.some(
				(option) => (response[option.name] ?? (option.checked ? '1' : '0')) === '1'
			)
				? 'complete'
				: 'empty';
		case 'text':
			return filled(response[body.gap.name] ?? body.gap.value) ? 'complete' : 'empty';
		case 'essay':
			return body.name !== null && filled(response[body.name] ?? body.value) ? 'complete' : 'empty';
		case 'cloze': {
			const count = body.gaps.filter((gap) => {
				const value = response[gap.name] ?? gap.value;
				return gap.kind === 'menu'
					? value !== '' && value !== '-1' && value !== gap.blank
					: filled(value);
			}).length;
			if (count === 0) return 'empty';
			return count === body.gaps.length ? 'complete' : 'partial';
		}
		case 'info':
		case 'unsupported':
			return 'empty';
	}
}

const PARAGRAPHS = 'p, div, h1, h2, h3, h4, h5, h6, blockquote, pre, ul, ol, table';
const LINES = 'li, tr';

/**
 * An essay's HTML as the plain text the student edits, the inverse of {@link textToHtml}:
 * paragraphs apart by a blank line, line breaks on their own line.
 */
export function htmlToEssay(html: string): string {
	const body = new DOMParser().parseFromString(`<body>${html}</body>`, 'text/html').body;
	collapseSpaces(body);
	for (const br of Array.from(body.querySelectorAll('br'))) br.replaceWith('\n');
	for (const line of Array.from(body.querySelectorAll(LINES))) line.append('\n');
	for (const block of Array.from(body.querySelectorAll(PARAGRAPHS))) {
		block.before('\n\n');
		block.after('\n\n');
	}
	return (body.textContent ?? '')
		.replace(/ *\n */g, '\n')
		.replace(/\n{3,}/g, '\n\n')
		.trim();
}

/** HTML's own spacing: source line breaks and runs of spaces read as one space, but in `pre`. */
function collapseSpaces(element: Element): void {
	for (const node of Array.from(element.childNodes)) {
		if (node.nodeType === 1) {
			if ((node as Element).tagName.toLowerCase() !== 'pre') collapseSpaces(node as Element);
		} else if (node.nodeType === 3) {
			node.textContent = (node.textContent ?? '').replace(/\s+/g, ' ');
		}
	}
}

/** Plain text as the HTML an essay keeps: paragraphs on blank lines, breaks on single ones. */
export function textToHtml(text: string): string {
	const escape = (value: string) =>
		value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
	return text
		.trim()
		.split(/\n\s*\n/)
		.filter((paragraph) => paragraph.trim() !== '')
		.map((paragraph) => `<p>${escape(paragraph.trim()).replace(/\n/g, '<br>')}</p>`)
		.join('');
}
