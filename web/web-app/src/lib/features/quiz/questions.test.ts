import { describe, expect, it } from 'vitest';
import type { MoodleQuizQuestion } from '@mybicocca/data-api/elearning';
import {
	answered,
	GAP,
	htmlToEssay,
	parseQuestion,
	questionFields,
	splitGaps,
	textToHtml,
	toKeyValues
} from './questions.ts';

function question(type: string, html: string, overrides: Partial<MoodleQuizQuestion> = {}) {
	return {
		slot: 1,
		type,
		page: 0,
		questionNumber: '1',
		number: 1,
		html,
		responseFileAreas: [],
		sequenceCheck: 1,
		lastActionTime: null,
		hasAutoSavedStep: null,
		flagged: false,
		state: 'todo',
		stateClass: null,
		status: null,
		blockedByPrevious: null,
		mark: null,
		maxMark: 1,
		settings: null,
		...overrides
	} satisfies MoodleQuizQuestion;
}

const info = `<div class="info"><h3 class="no">Domanda <span class="qno">1</span></h3>
	<div class="questionflag editable"><input type="hidden" name="q9:1_:flagged" value="0">
	<input type="checkbox" id="q9:1_:flaggedcheckbox" name="q9:1_:flagged" value="1"></div></div>`;

const single = `<div id="question-9-1" class="que multichoice deferredfeedback notyetanswered">${info}
<div class="content"><div class="formulation clearfix"><h4 class="accesshide">Testo della domanda</h4>
<input type="hidden" name="q9:1_:sequencecheck" value="2">
<div class="qtext"><p>Complessità del <b>Quicksort</b>?</p></div>
<fieldset class="ablock"><legend class="prompt sr-only">Scegli un'alternativa:</legend>
<div class="answer">
<div class="r0"><input type="radio" name="q9:1_answer" value="0" id="q9:1_answer0" aria-labelledby="q9:1_answer0_label">
<div class="d-flex" id="q9:1_answer0_label" data-region="answer-label"><span class="answernumber">a. </span><div class="flex-fill ml-1"><p>O(n log n)</p></div></div></div>
<div class="r1"><input type="radio" name="q9:1_answer" value="1" id="q9:1_answer1" checked="checked" aria-labelledby="q9:1_answer1_label">
<div class="d-flex" id="q9:1_answer1_label" data-region="answer-label"><span class="answernumber">b. </span><div class="flex-fill ml-1"><p>O(n²)</p></div></div></div>
</div>
<div id="q9:1_clearchoice" class="qtype_multichoice_clearchoice sr-only"><input type="radio" name="q9:1_answer" value="-1"></div>
</fieldset></div></div></div>`;

describe('parseQuestion', () => {
	it('reads single choice options with their labels, choice and hidden fields', () => {
		const parsed = parseQuestion(question('multichoice', single));
		expect(parsed.prompt).toBe('<p>Complessità del <b>Quicksort</b>?</p>');
		expect(parsed.fields).toEqual({ 'q9:1_:sequencecheck': '2' });
		expect(parsed.prefix).toBe('q9:1_');
		expect(parsed.body).toEqual({
			kind: 'single',
			name: 'q9:1_answer',
			options: [
				{
					name: 'q9:1_answer',
					value: '0',
					label: '<div class="flex-fill ml-1"><p>O(n log n)</p></div>',
					checked: false,
					mark: null
				},
				{
					name: 'q9:1_answer',
					value: '1',
					label: '<div class="flex-fill ml-1"><p>O(n²)</p></div>',
					checked: true,
					mark: null
				}
			]
		});
	});

	it('reads multiple answers, keeping the hidden zero of every choice', () => {
		const html = `<div class="formulation"><input type="hidden" name="q9:2_:sequencecheck" value="1">
		<div class="qtext">Stabili?</div><div class="answer">
		<div class="r0 correct"><input type="hidden" name="q9:2_choice0" value="0"><input type="checkbox" name="q9:2_choice0" value="1" checked="checked" id="c0"><label for="c0">Merge sort</label></div>
		<div class="r1 incorrect"><input type="hidden" name="q9:2_choice1" value="0"><input type="checkbox" name="q9:2_choice1" value="1" checked="checked" id="c1"><label for="c1">Heap sort</label></div>
		</div></div>`;
		const parsed = parseQuestion(question('multichoice', html, { slot: 2 }));
		expect(parsed.body.kind).toBe('multi');
		expect(parsed.fields).toEqual({
			'q9:2_:sequencecheck': '1',
			'q9:2_choice0': '0',
			'q9:2_choice1': '0'
		});
		if (parsed.body.kind !== 'multi') throw new Error('not multi');
		expect(parsed.body.options.map((option) => [option.label, option.mark])).toEqual([
			['Merge sort', 'correct'],
			['Heap sort', 'incorrect']
		]);
	});

	it('turns menus and several text inputs into gaps of a cloze, labelled by Moodle', () => {
		const html = `<div class="formulation"><input type="hidden" name="q9:3_:sequencecheck" value="1">
		<p>Il Mergesort costa <span class="subquestion"><label class="subq accesshide" for="g1">Risposta 1</label>
		<select id="g1" name="q9:3_sub1_answer" class="correct"><option value=""></option><option value="0">O(n)</option><option value="1" selected="selected">O(n log n)</option></select></span>
		e usa <span class="subquestion"><label class="accesshide" for="g2">Risposta 2</label><input type="text" id="g2" name="q9:3_sub2_answer" value="lineare" size="8" class="incorrect"></span> memoria.
		<span class="feedbackspan">Feedback nascosto</span></p></div>`;
		const parsed = parseQuestion(question('multianswer', html, { slot: 3 }));
		expect(parsed.prompt).toBe('');
		if (parsed.body.kind !== 'cloze') throw new Error('not cloze');
		expect(parsed.body.gaps).toEqual([
			{
				kind: 'menu',
				name: 'q9:3_sub1_answer',
				value: '1',
				label: 'Risposta 1',
				options: [
					{ value: '', label: '' },
					{ value: '0', label: 'O(n)' },
					{ value: '1', label: 'O(n log n)' }
				],
				blank: '',
				mark: 'correct'
			},
			{
				kind: 'text',
				name: 'q9:3_sub2_answer',
				value: 'lineare',
				label: 'Risposta 2',
				numeric: false,
				size: 8,
				mark: 'incorrect'
			}
		]);
		expect(parsed.body.html).not.toContain('Feedback nascosto');
		expect(parsed.body.html).not.toContain('Risposta 1');
		const parts = splitGaps(parsed.body.html);
		expect(parts.filter((part) => typeof part === 'number')).toEqual([0, 1]);
		expect(parsed.body.html.split(GAP)).toHaveLength(5);
	});

	it('keeps a lone gap without a question text as a cloze, so its text stays', () => {
		const html = `<div class="formulation"><input type="hidden" name="q9:4_:sequencecheck" value="1">
		<p>La radice di 144 è <input type="text" name="q9:4_sub1_answer" value=""></p></div>`;
		expect(parseQuestion(question('multianswer', html)).body.kind).toBe('cloze');
	});

	it('reads a single text answer, numeric for numerical questions', () => {
		const html = `<div class="formulation"><input type="hidden" name="q9:5_:sequencecheck" value="1">
		<div class="qtext">Quanti confronti?</div><div class="ablock"><label for="a5">Risposta:</label>
		<span class="answer"><input type="text" name="q9:5_answer" id="a5" value="11"></span></div></div>
		<div class="outcome"><div class="feedback"><div class="specificfeedback">Giusto.</div>
		<div class="generalfeedback">log₂ 1024 + 1.</div><div class="rightanswer">La risposta corretta è: 11</div></div></div>`;
		const parsed = parseQuestion(
			question('numerical', html, { mark: '1,00', state: 'gradedright' })
		);
		expect(parsed.body).toMatchObject({
			kind: 'text',
			gap: { name: 'q9:5_answer', value: '11', numeric: true, label: 'Risposta:' }
		});
		expect(parsed.feedback).toBe('Giusto.<br>log₂ 1024 + 1.');
		expect(parsed.rightAnswer).toBe('La risposta corretta è: 11');
		expect(parsed.mark).toBe(1);
	});

	it('reads an essay and whether Moodle keeps it as HTML', () => {
		const html = `<div class="formulation"><input type="hidden" name="q9:6_:sequencecheck" value="1">
		<div class="qtext">Spiega.</div><div class="answer"><textarea name="q9:6_answer">Ciao</textarea></div>
		<input type="hidden" name="q9:6_answerformat" value="1"></div>`;
		expect(parseQuestion(question('essay', html)).body).toEqual({
			kind: 'essay',
			name: 'q9:6_answer',
			value: 'Ciao',
			html: true
		});
	});

	it('treats descriptions as text and drag-and-drop as unsupported', () => {
		const description = `<div class="formulation"><div class="qtext">Leggi con attenzione.</div></div>`;
		expect(parseQuestion(question('description', description, { number: null })).body.kind).toBe(
			'info'
		);
		const drag = `<div class="formulation"><input type="hidden" name="q9:7_p1" value="">
		<div class="qtext">Trascina <span class="drop group1"></span></div><div class="draghomes">x</div></div>`;
		const parsed = parseQuestion(question('ddwtos', drag));
		expect(parsed.prompt).toContain('Trascina');
		expect(parsed.body).toEqual({ kind: 'unsupported', html: '<div class="draghomes">x</div>' });
		expect(parsed.fields).toEqual({ 'q9:7_p1': '' });
	});

	it('shows the item placed in each drop place, and a blank where none is', () => {
		const drag = `<div class="formulation">
		<div class="qtext">Il <span class="place1 drop active group1 readonly"><span class="accesshide">vuoto</span></span> ordina in
		<span class="place2 drop active group2 readonly"></span>, poi <span class="place3 drop active group1 readonly"></span>.</div>
		<div class="draggrouphomes9"><div class="draggrouphome group1"><span class="draghome choice1 group1">Quicksort</span>
		<span class="draghome choice2 group1">Mergesort</span></div>
		<div class="draggrouphome group2"><span class="draghome choice1 group2">O(n log n)</span></div></div>
		<input type="hidden" name="q9:7_p1" value="2" class="placeinput place1 group1">
		<input type="hidden" name="q9:7_p2" value="1" class="placeinput place2 group2">
		<input type="hidden" name="q9:7_p3" value="0" class="placeinput place3 group1"></div>`;
		const parsed = parseQuestion(question('ddwtos', drag));
		expect(parsed.prompt.replace(/\s+/g, ' ')).toBe(
			'Il <mark>Mergesort</mark> ordina in <mark>O(n log n)</mark>, poi ＿＿＿.'
		);
		expect(parsed.fields).toEqual({ 'q9:7_p1': '2', 'q9:7_p2': '1', 'q9:7_p3': '0' });
	});

	it('lists the drag items of an unsupported question, so they read apart', () => {
		const drag = `<div class="formulation"><div class="qtext">Ordina.</div><div class="draghomes"><span class="draghome choice1">prima</span><span class="draghome choice2">poi</span></div></div>`;
		expect(parseQuestion(question('ddwtos', drag)).body).toEqual({
			kind: 'unsupported',
			html: '<div class="draghomes"><ul><li>prima</li><li>poi</li></ul></div>'
		});
	});
});

describe('questionFields', () => {
	const parsed = parseQuestion(question('multichoice', single));

	it("sends the hidden fields with the student's choice and the flag", () => {
		expect(questionFields(parsed, { 'q9:1_answer': '0' }, true)).toEqual({
			'q9:1_:sequencecheck': '2',
			'q9:1_answer': '0',
			'q9:1_:flagged': '1'
		});
		expect(questionFields(parsed, {})).toMatchObject({ 'q9:1_answer': '1' });
	});

	it("sends emptied answers as empty values, as Moodle's own form does", () => {
		const html = `<div class="formulation"><input type="hidden" name="q9:3_:sequencecheck" value="1">
		<p><select name="m"><option value=""></option><option value="0">A</option></select>
		<select name="g"><option value=""></option><option value="1">B</option></select>
		<input type="text" name="t" value="x"></p></div>`;
		const cloze = parseQuestion(question('multianswer', html));
		expect(questionFields(cloze, { t: '' })).toEqual({
			'q9:3_:sequencecheck': '1',
			m: '',
			g: '',
			t: ''
		});
		expect(toKeyValues({ a: '1', b: '' })).toEqual([
			{ name: 'a', value: '1' },
			{ name: 'b', value: '' }
		]);
	});
});

describe('menus', () => {
	it("know matching's 0 as no answer", () => {
		const html = `<div class="formulation"><div class="qtext">Associa.</div><table class="answer"><tbody>
		<tr><td class="text">Pila</td><td class="control"><select name="s0"><option value="0">Scegli...</option>
		<option value="1">LIFO</option><option value="2">FIFO</option></select></td></tr>
		<tr><td class="text">Coda</td><td class="control"><select name="s1"><option value="0">Scegli...</option>
		<option value="1">LIFO</option><option value="2" selected="selected">FIFO</option></select></td></tr></tbody></table></div>`;
		const parsed = parseQuestion(question('match', html));
		if (parsed.body.kind !== 'cloze') throw new Error('not cloze');
		expect(parsed.body.gaps.map((gap) => gap.kind === 'menu' && [gap.blank, gap.value])).toEqual([
			['0', '0'],
			['0', '2']
		]);
		expect(answered(parsed, {})).toBe('partial');
	});
});

describe('answered', () => {
	it('tells complete, partial and empty answers apart', () => {
		const html = `<div class="formulation"><p><input type="text" name="a" value="">
		<select name="b"><option value=""></option><option value="1">x</option></select></p></div>`;
		const cloze = parseQuestion(question('multianswer', html));
		expect(answered(cloze, {})).toBe('empty');
		expect(answered(cloze, { a: '3' })).toBe('partial');
		expect(answered(cloze, { a: '3', b: '1' })).toBe('complete');
		expect(answered(parseQuestion(question('multichoice', single)), { 'q9:1_answer': '-1' })).toBe(
			'empty'
		);
	});
});

describe('essay text', () => {
	it('escapes text and keeps its paragraphs and line breaks', () => {
		expect(textToHtml('a < b\nc\n\nd')).toBe('<p>a &lt; b<br>c</p><p>d</p>');
	});

	it('reads paragraphs back as blank lines, so a saved essay edits as it was typed', () => {
		const text = 'Una riga.\n\nSeconda riga,\ncon un a capo.\n\na < b';
		expect(htmlToEssay(textToHtml(text))).toBe(text);
		expect(htmlToEssay('<p>Uno\n  due</p>\n<ul><li>a</li><li>b</li></ul>')).toBe('Uno due\n\na\nb');
	});
});
