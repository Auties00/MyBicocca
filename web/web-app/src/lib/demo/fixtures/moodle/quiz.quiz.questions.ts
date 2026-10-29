import { DemoCourses, type DemoCourse } from '../../persona.ts';

/**
 * The persona's quiz questions, rendered as Moodle 4.5 renders them for `mod_quiz_get_attempt_data`
 * and `mod_quiz_get_attempt_review`, one bank per course. The routes are in `quiz.quiz.ts`; this
 * file only builds the questions.
 */
/** The student's fields for one question, by Moodle's name without the `q{usage}:{slot}_` prefix. */
export type DemoResponse = Record<string, string>;

export type DemoGrade = { fraction: number } | 'unanswered' | 'manual';

export interface RenderContext {
	prefix: string;
	sequence: number;
	response: DemoResponse;
	/** Null while the attempt runs; the outcome once it is reviewed. */
	review: DemoGrade | null;
}

export interface DemoQuestion {
	type: string;
	maxMark: number;
	/** The formulation, and in review the outcome under it. */
	render(context: RenderContext): { formulation: string; outcome: string };
	grade(response: DemoResponse): DemoGrade;
	/** Moodle's attempt state for the response: answered in full, in part, or not at all. */
	answered(response: DemoResponse): 'complete' | 'invalid' | 'todo';
}

const escape = (value: string) =>
	value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');

const LETTERS = 'abcdefgh';
const checked = (on: boolean) => (on ? ' checked="checked"' : '');
const selected = (on: boolean) => (on ? ' selected="selected"' : '');
const normal = (value: string) => value.trim().toLowerCase().replace(/\s+/g, ' ');

function outcome(specific: string | null, general: string | null, right: string | null): string {
	const parts = [
		specific && `<div class="specificfeedback">${specific}</div>`,
		general && `<div class="generalfeedback">${general}</div>`,
		right && `<div class="rightanswer">${right}</div>`
	].filter(Boolean);
	return `<div class="outcome clearfix"><h4 class="accesshide">Feedback</h4><div class="feedback">${parts.join('')}</div></div>`;
}

interface Choice {
	label: string;
	correct?: boolean;
	feedback?: string;
}

/** Multiple choice with one answer: a radio per option, `answer` = the option's index. */
function single(text: string, options: Choice[], general: string | null = null): DemoQuestion {
	return {
		type: 'multichoice',
		maxMark: 1,
		render({ prefix, response, review }) {
			const answer = response.answer;
			const rows = options.map((option, index) => {
				const on = answer === String(index);
				const mark = review !== null && on ? ` ${option.correct ? 'correct' : 'incorrect'}` : '';
				const id = `${prefix}answer${index}`;
				return `<div class="r${index % 2}${mark}"><input type="radio" name="${prefix}answer" value="${index}" id="${id}" aria-labelledby="${id}_label"${checked(on)}${review ? ' disabled="disabled"' : ''}><div class="d-flex w-auto" id="${id}_label" data-region="answer-label"><span class="answernumber">${LETTERS[index]}. </span><div class="flex-fill ml-1">${option.label}</div></div></div>`;
			});
			const right = options.find((option) => option.correct);
			const chosen = answer === undefined ? undefined : options[Number(answer)];
			return {
				formulation: `<div class="qtext">${text}</div><fieldset class="ablock no-overflow visual-scroll-x"><legend class="prompt h6 font-weight-normal sr-only">Scegli un'alternativa:</legend><div class="answer">${rows.join('')}</div><div id="${prefix}clearchoice" class="qtype_multichoice_clearchoice sr-only" aria-hidden="true"><input type="radio" name="${prefix}answer" id="${prefix}answer-1" value="-1" class="sr-only" aria-hidden="true"${checked(answer === undefined || answer === '-1')}><label for="${prefix}answer-1"><a tabindex="-1" role="button" class="btn btn-link" href="#">Cancella la scelta</a></label></div></fieldset>`,
				outcome: review
					? outcome(
							chosen?.feedback ?? null,
							general,
							right ? `La risposta corretta è: ${right.label}` : null
						)
					: ''
			};
		},
		grade(response) {
			const answer = response.answer;
			if (answer === undefined || answer === '-1' || answer === '') return 'unanswered';
			return { fraction: options[Number(answer)]?.correct ? 1 : 0 };
		},
		answered: (response) =>
			response.answer !== undefined && response.answer !== '-1' && response.answer !== ''
				? 'complete'
				: 'todo'
	};
}

/** Multiple answers: a checkbox per option (`choiceN` = 1), each preceded by its hidden 0. */
function multiple(text: string, options: Choice[], general: string | null = null): DemoQuestion {
	const rightCount = options.filter((option) => option.correct).length;
	const on = (response: DemoResponse, index: number) => response[`choice${index}`] === '1';
	return {
		type: 'multichoice',
		maxMark: 1,
		render({ prefix, response, review }) {
			const rows = options.map((option, index) => {
				const mark =
					review !== null && on(response, index)
						? ` ${option.correct ? 'correct' : 'incorrect'}`
						: '';
				const id = `${prefix}choice${index}`;
				return `<div class="r${index % 2}${mark}"><input type="hidden" name="${id}" value="0"><input type="checkbox" name="${id}" value="1" id="${id}" aria-labelledby="${id}_label"${checked(on(response, index))}${review ? ' disabled="disabled"' : ''}><div class="d-flex w-auto" id="${id}_label" data-region="answer-label"><span class="answernumber">${LETTERS[index]}. </span><div class="flex-fill ml-1">${option.label}</div></div></div>`;
			});
			const right = options.filter((option) => option.correct).map((option) => option.label);
			return {
				formulation: `<div class="qtext">${text}</div><fieldset class="ablock no-overflow visual-scroll-x"><legend class="prompt h6 font-weight-normal sr-only">Scegli una o più alternative:</legend><div class="answer">${rows.join('')}</div></fieldset>`,
				outcome: review
					? outcome(null, general, `Le risposte corrette sono: ${right.join(', ')}`)
					: ''
			};
		},
		grade(response) {
			const chosen = options.filter((_, index) => on(response, index));
			if (chosen.length === 0) return 'unanswered';
			const score = chosen.reduce((sum, option) => sum + (option.correct ? 1 : -1), 0);
			return { fraction: Math.max(0, score / rightCount) };
		},
		answered: (response) => (options.some((_, index) => on(response, index)) ? 'complete' : 'todo')
	};
}

function trueFalse(text: string, answer: boolean, general: string | null = null): DemoQuestion {
	const labels = ['Vero', 'Falso'];
	return {
		type: 'truefalse',
		maxMark: 1,
		render({ prefix, response, review }) {
			const rows = [true, false].map((value, index) => {
				const on = response.answer === (value ? '1' : '0');
				const mark = review !== null && on ? ` ${value === answer ? 'correct' : 'incorrect'}` : '';
				const id = `${prefix}answer${value ? 'true' : 'false'}`;
				return `<div class="r${index}${mark}"><input type="radio" name="${prefix}answer" value="${value ? 1 : 0}" id="${id}" aria-labelledby="${id}-label"${checked(on)}${review ? ' disabled="disabled"' : ''}><label id="${id}-label" for="${id}" class="ml-1">${labels[index]}</label></div>`;
			});
			return {
				formulation: `<div class="qtext">${text}</div><fieldset class="ablock"><legend class="prompt h6 font-weight-normal sr-only">Scegli un'alternativa:</legend><div class="answer">${rows.join('')}</div></fieldset>`,
				outcome: review
					? outcome(null, general, `La risposta corretta è '${answer ? 'Vero' : 'Falso'}'.`)
					: ''
			};
		},
		grade: (response) =>
			response.answer === undefined || response.answer === ''
				? 'unanswered'
				: { fraction: (response.answer === '1') === answer ? 1 : 0 },
		answered: (response) => (response.answer ? 'complete' : 'todo')
	};
}

/** A short text or a number in one field (`answer`); numbers take a decimal comma too. */
function shortAnswer(
	text: string,
	accepted: readonly (string | number)[],
	general: string | null = null
): DemoQuestion {
	const numeric = typeof accepted[0] === 'number';
	const right = (value: string) =>
		numeric
			? accepted.includes(Number(value.trim().replace(',', '.')))
			: accepted.some((answer) => normal(String(answer)) === normal(value));
	return {
		type: numeric ? 'numerical' : 'shortanswer',
		maxMark: 1,
		render({ prefix, response, review }) {
			const value = response.answer ?? '';
			const grade = review === null ? null : right(value) ? 'correct' : 'incorrect';
			return {
				formulation: `<div class="qtext">${text}</div><div class="ablock form-inline"><label for="${prefix}answer">Risposta:</label><span class="answer"><input type="text" name="${prefix}answer" id="${prefix}answer" size="30" class="form-control d-inline${grade ? ` ${grade}` : ''}" value="${escape(value)}"${review ? ' readonly="readonly"' : ''}></span></div>`,
				outcome: review ? outcome(null, general, `La risposta corretta è: ${accepted[0]}`) : ''
			};
		},
		grade: (response) =>
			(response.answer ?? '').trim() === ''
				? 'unanswered'
				: { fraction: right(response.answer ?? '') ? 1 : 0 },
		answered: (response) => ((response.answer ?? '').trim() ? 'complete' : 'todo')
	};
}

type ClozePart = string | { menu: string[]; right: number } | { text: string[]; size?: number };

/** Embedded answers (Cloze): text with menus and fields (`subN_answer`), graded part by part. */
function cloze(parts: readonly ClozePart[], general: string | null = null): DemoQuestion {
	const gaps = parts.filter((part) => typeof part !== 'string');
	const rightOf = (gap: Exclude<ClozePart, string>, value: string | undefined) =>
		'menu' in gap
			? value === String(gap.right)
			: gap.text.some((answer) => normal(answer) === normal(value ?? ''));
	return {
		type: 'multianswer',
		maxMark: 1,
		render({ prefix, response, review }) {
			let number = 0;
			const html = parts.map((part) => {
				if (typeof part === 'string') return part;
				number++;
				const name = `${prefix}sub${number}_answer`;
				const value = response[`sub${number}_answer`];
				const mark = review === null ? '' : ` ${rightOf(part, value) ? 'correct' : 'incorrect'}`;
				const label = `<label class="subq accesshide" for="${name}">Risposta ${number}</label>`;
				const feedback =
					review === null
						? ''
						: `<span class="feedbackspan">${rightOf(part, value) ? 'Corretto' : 'Errato'}</span>`;
				if ('menu' in part) {
					const options = part.menu
						.map(
							(option, index) =>
								`<option value="${index}"${selected(value === String(index))}>${option}</option>`
						)
						.join('');
					return `<span class="subquestion form-inline d-inline">${label}<select class="custom-select${mark}" id="${name}" name="${name}"${review ? ' disabled="disabled"' : ''}><option value=""></option>${options}</select>${feedback}</span>`;
				}
				return `<span class="subquestion form-inline d-inline">${label}<input type="text" name="${name}" id="${name}" value="${escape(value ?? '')}" size="${part.size ?? 10}" class="form-control mb-1${mark}"${review ? ' readonly="readonly"' : ''}>${feedback}</span>`;
			});
			return {
				formulation: `<p>${html.join('')}</p>`,
				outcome: review ? outcome(null, general, null) : ''
			};
		},
		grade(response) {
			const values = gaps.map((_, index) => response[`sub${index + 1}_answer`]);
			if (values.every((value) => value === undefined || value.trim() === '' || value === '-1')) {
				return 'unanswered';
			}
			const right = gaps.filter((gap, index) => rightOf(gap, values[index])).length;
			return { fraction: right / gaps.length };
		},
		answered(response) {
			const filled = gaps.filter((_, index) => {
				const value = response[`sub${index + 1}_answer`];
				return value !== undefined && value.trim() !== '' && value !== '-1';
			}).length;
			return filled === 0 ? 'todo' : filled === gaps.length ? 'complete' : 'invalid';
		}
	};
}

/** Matching: a menu per stem (`subN`, 0 = none), choices numbered from 1. */
function match(
	text: string,
	pairs: readonly [string, string][],
	general: string | null = null
): DemoQuestion {
	const choices = pairs.map(([, answer]) => answer).toSorted((a, b) => a.localeCompare(b, 'it'));
	const rightValue = (index: number) => String(choices.indexOf(pairs[index][1]) + 1);
	return {
		type: 'match',
		maxMark: 1,
		render({ prefix, response, review }) {
			const rows = pairs.map(([stem], index) => {
				const value = response[`sub${index}`] ?? '0';
				const mark =
					review === null ? '' : ` ${value === rightValue(index) ? 'correct' : 'incorrect'}`;
				const options = choices
					.map(
						(choice, choiceIndex) =>
							`<option value="${choiceIndex + 1}"${selected(value === String(choiceIndex + 1))}>${choice}</option>`
					)
					.join('');
				return `<tr class="r${index % 2}"><td class="text"><p>${stem}</p></td><td class="control${mark}"><label class="accesshide" for="${prefix}sub${index}">Risposta ${index + 1}</label><select id="${prefix}sub${index}" name="${prefix}sub${index}" class="custom-select"${review ? ' disabled="disabled"' : ''}><option value="0"${selected(value === '0')}>Scegli...</option>${options}</select></td></tr>`;
			});
			const right = pairs.map(([stem, answer]) => `${stem} → ${answer}`).join(', ');
			return {
				formulation: `<div class="qtext">${text}</div><div class="ablock"><table class="answer"><tbody>${rows.join('')}</tbody></table></div>`,
				outcome: review ? outcome(null, general, `La risposta corretta è: ${right}`) : ''
			};
		},
		grade(response) {
			const values = pairs.map((_, index) => response[`sub${index}`] ?? '0');
			if (values.every((value) => value === '0' || value === '-1')) return 'unanswered';
			const right = values.filter((value, index) => value === rightValue(index)).length;
			return { fraction: right / pairs.length };
		},
		answered(response) {
			const filled = pairs.filter((_, index) => (response[`sub${index}`] ?? '0') !== '0').length;
			return filled === 0 ? 'todo' : filled === pairs.length ? 'complete' : 'invalid';
		}
	};
}

/** Select missing words: `[[n]]` places in the text, each a menu (`pN`) of its group's words. */
function gapSelect(
	text: string,
	groups: readonly (readonly string[])[],
	right: readonly number[],
	general: string | null = null
): DemoQuestion {
	return {
		type: 'gapselect',
		maxMark: 1,
		render({ prefix, response, review }) {
			const html = text.replace(/\[\[(\d+)\]\]/g, (_, place: string) => {
				const index = Number(place) - 1;
				const value = response[`p${place}`] ?? '';
				const mark =
					review === null ? '' : ` ${value === String(right[index] + 1) ? 'correct' : 'incorrect'}`;
				const options = groups[index]
					.map(
						(word, choice) =>
							`<option value="${choice + 1}"${selected(value === String(choice + 1))}>${word}</option>`
					)
					.join('');
				return `<span class="control group${place}${mark}"><label class="accesshide" for="${prefix}p${place}">Spazio vuoto ${place} di ${groups.length}</label><select id="${prefix}p${place}" name="${prefix}p${place}" class="select custom-select place${place}"${review ? ' disabled="disabled"' : ''}><option value="">&nbsp;</option>${options}</select></span>`;
			});
			const answer = groups.map((group, index) => group[right[index]]).join(', ');
			return {
				formulation: `<div class="qtext">${html}</div>`,
				outcome: review ? outcome(null, general, `La risposta corretta è: ${answer}`) : ''
			};
		},
		grade(response) {
			const values = groups.map((_, index) => response[`p${index + 1}`] ?? '');
			if (values.every((value) => value === '' || value === '0')) return 'unanswered';
			const ok = values.filter((value, index) => value === String(right[index] + 1)).length;
			return { fraction: ok / groups.length };
		},
		answered(response) {
			const filled = groups.filter((_, index) => {
				const value = response[`p${index + 1}`] ?? '';
				return value !== '' && value !== '0';
			}).length;
			return filled === 0 ? 'todo' : filled === groups.length ? 'complete' : 'invalid';
		}
	};
}

/** An open answer the teacher grades: HTML in `answer`, its format in `answerformat`. */
function essay(text: string): DemoQuestion {
	return {
		type: 'essay',
		maxMark: 1,
		render({ prefix, response, review }) {
			const value = response.answer ?? '';
			const answer = review
				? `<div class="qtype_essay_response readonly">${value || '<p></p>'}</div>`
				: `<div><textarea name="${prefix}answer" id="${prefix}answer" rows="10" cols="60" class="form-control">${escape(value)}</textarea></div><input type="hidden" name="${prefix}answerformat" value="1">`;
			return {
				formulation: `<div class="qtext">${text}</div><div class="ablock"><div class="prompt">Risposta</div><div class="answer">${answer}</div></div>`,
				outcome: ''
			};
		},
		grade: (response) =>
			(response.answer ?? '').replace(/<[^>]*>/g, '').trim() === '' ? 'unanswered' : 'manual',
		answered: (response) =>
			(response.answer ?? '').replace(/<[^>]*>/g, '').trim() ? 'complete' : 'todo'
	};
}

/**
 * Drag and drop into text: nothing the app answers, so it links to the e-learning site. Place N
 * takes word N; a place's field holds the number of the word dropped there, as Moodle's does.
 */
function dragWords(text: string, words: readonly string[]): DemoQuestion {
	const places = (text.match(/\[\[\d+\]\]/g) ?? []).length;
	const chosen = (response: DemoResponse) =>
		Array.from({ length: places }, (_, index) => response[`p${index + 1}`] ?? '');
	const filled = (value: string) => value !== '' && value !== '0';
	return {
		type: 'ddwtos',
		maxMark: 1,
		render({ prefix, response, review }) {
			let place = 0;
			const html = text.replace(/\[\[\d+\]\]/g, () => {
				place++;
				return review
					? `<span class="drop active group1 place${place} readonly">&nbsp;</span>`
					: `<span class="drop active group1 place${place}" tabindex="0">&nbsp;</span>`;
			});
			const homes = words
				.map(
					(word, index) =>
						`<span class="draghome user-select-none choice${index + 1} group1">${word}</span>`
				)
				.join(' ');
			const inputs = chosen(response)
				.map(
					(value, index) =>
						`<input type="hidden" name="${prefix}p${index + 1}" value="${escape(value)}" class="placeinput place${index + 1} group1">`
				)
				.join('');
			const right = text
				.replace(/\[\[(\d+)\]\]/g, (_, number: string) => `[${words[Number(number) - 1]}]`)
				.replace(/<[^>]*>/g, '');
			return {
				formulation: `<div class="qtext">${html}</div><div class="draghomes">${homes}</div>${inputs}`,
				outcome: review ? outcome(null, null, `La risposta corretta è: ${right}`) : ''
			};
		},
		grade: (response) => {
			const values = chosen(response);
			if (!values.some(filled)) return 'unanswered';
			const right = values.filter((value, index) => value === String(index + 1)).length;
			return { fraction: right / places };
		},
		answered: (response) => {
			const count = chosen(response).filter(filled).length;
			return count === 0 ? 'todo' : count === places ? 'complete' : 'invalid';
		}
	};
}

function description(text: string): DemoQuestion {
	return {
		type: 'description',
		maxMark: 0,
		render: () => ({ formulation: `<div class="qtext">${text}</div>`, outcome: '' }),
		grade: () => 'unanswered',
		answered: () => 'todo'
	};
}

/** Two points a question, so every bank adds up to the quiz's 10. */
const twice = (question: DemoQuestion): DemoQuestion => ({ ...question, maxMark: 2 });

const algorithms: DemoQuestion[] = [
	description(
		'<p>Dieci domande sugli argomenti delle prime settimane. Il voto <strong>non</strong> concorre alla valutazione finale: usalo per capire cosa ripassare.</p>'
	),
	single(
		'<p>Qual è la complessità nel caso pessimo di <strong>Quicksort</strong> con il pivot scelto come ultimo elemento?</p>',
		[
			{ label: '<p>O(<em>n</em> log <em>n</em>)</p>', feedback: 'Questo è il caso medio.' },
			{
				label: '<p>O(<em>n</em><sup>2</sup>)</p>',
				correct: true,
				feedback: 'Sì: con un array già ordinato ogni partizione è sbilanciata.'
			},
			{ label: '<p>O(<em>n</em>)</p>', feedback: 'Nessun ordinamento per confronti è lineare.' },
			{
				label: '<p>O(log <em>n</em>)</p>',
				feedback: 'Troppo poco: bisogna almeno leggere ogni elemento.'
			}
		],
		'<p>Un pivot casuale rende il caso pessimo molto improbabile.</p>'
	),
	multiple(
		'<p>Quali dei seguenti algoritmi di ordinamento sono <strong>stabili</strong>?</p>',
		[
			{ label: '<p>Merge sort</p>', correct: true },
			{ label: '<p>Insertion sort</p>', correct: true },
			{ label: '<p>Heap sort</p>' },
			{ label: '<p>Quicksort (versione di Lomuto)</p>' }
		],
		'<p>Un ordinamento è stabile se mantiene l’ordine relativo degli elementi con chiavi uguali.</p>'
	),
	trueFalse(
		'<p>In un albero AVL con <em>n</em> nodi l’altezza è sempre O(log <em>n</em>).</p>',
		true,
		'<p>Il fattore di bilanciamento di ogni nodo è al più 1 in valore assoluto.</p>'
	),
	shortAnswer(
		'<p>Quale struttura dati usa la visita in ampiezza (BFS) per tenere i nodi ancora da visitare?</p>',
		['coda', 'una coda', 'queue', 'coda FIFO'],
		'<p>La visita in profondità usa invece una pila, esplicita o implicita nella ricorsione.</p>'
	),
	shortAnswer(
		'<p>Quanti confronti esegue <strong>al massimo</strong> la ricerca binaria su un array ordinato di 1024 elementi?</p>',
		[11],
		'<p>⌊log<sub>2</sub> 1024⌋ + 1 = 11.</p>'
	),
	cloze(
		[
			'Il <strong>Mergesort</strong> ha complessità ',
			{ menu: ['O(n)', 'O(n log n)', 'O(n²)'], right: 1 },
			' in ogni caso e richiede memoria ausiliaria ',
			{ text: ['O(n)', 'lineare', 'n'], size: 8 },
			' per la fusione.'
		],
		'<p>La fusione copia le due metà in un array d’appoggio.</p>'
	),
	match('<p>Associa a ogni struttura dati l’operazione che offre in tempo costante.</p>', [
		['Pila', 'Inserimento in cima'],
		['Coda', 'Estrazione dalla testa'],
		['Tabella hash', 'Ricerca (in media)']
	]),
	gapSelect(
		'<p>L’algoritmo di Dijkstra calcola i cammini minimi da [[1]] sorgente verso tutti i nodi e richiede archi con pesi [[2]].</p>',
		[
			['una', 'ogni'],
			['non negativi', 'interi', 'qualsiasi']
		],
		[0, 0],
		'<p>Con pesi negativi serve Bellman-Ford.</p>'
	),
	essay(
		'<p>Spiega in poche righe perché la <strong>programmazione dinamica</strong> migliora la ricorsione ingenua nel calcolo dei numeri di Fibonacci.</p>'
	),
	dragWords('<p>Metti in ordine i passi dell’algoritmo di Kruskal: prima [[1]], poi [[2]].</p>', [
		'ordina gli archi per peso',
		'aggiungi ogni arco che non crea cicli'
	])
];

const databases: DemoQuestion[] = [
	twice(
		single('<p>Quale clausola SQL filtra i <strong>gruppi</strong> prodotti da GROUP BY?</p>', [
			{
				label: '<p><code>WHERE</code></p>',
				feedback: 'WHERE filtra le righe prima del raggruppamento.'
			},
			{ label: '<p><code>HAVING</code></p>', correct: true },
			{ label: '<p><code>ORDER BY</code></p>' },
			{ label: '<p><code>DISTINCT</code></p>' }
		])
	),
	twice(
		multiple(
			'<p>Quali di questi vincoli si esprimono direttamente in SQL con <code>CREATE TABLE</code>?</p>',
			[
				{ label: '<p>Chiave primaria</p>', correct: true },
				{ label: '<p>Chiave esterna</p>', correct: true },
				{ label: '<p>Dipendenza funzionale qualsiasi</p>' }
			]
		)
	),
	twice(
		trueFalse(
			'<p>Una relazione in BCNF è sempre anche in terza forma normale.</p>',
			true,
			'<p>BCNF è più restrittiva della 3NF.</p>'
		)
	),
	twice(
		cloze([
			'Il join che restituisce anche le righe della tabella di sinistra senza corrispondenza è il ',
			{ menu: ['INNER JOIN', 'LEFT OUTER JOIN', 'CROSS JOIN'], right: 1 },
			'; le colonne mancanti valgono ',
			{ text: ['NULL'], size: 6 },
			'.'
		])
	),
	twice(
		shortAnswer(
			'<p>Come si chiama l’operatore dell’algebra relazionale che seleziona alcune <em>colonne</em> di una relazione?</p>',
			['proiezione', 'la proiezione', 'projection']
		)
	)
];

const operatingSystems: DemoQuestion[] = [
	twice(
		single('<p>Quale algoritmo di scheduling può causare <strong>starvation</strong>?</p>', [
			{ label: '<p>Round Robin</p>' },
			{ label: '<p>Shortest Job First</p>', correct: true },
			{ label: '<p>FCFS</p>' }
		])
	),
	twice(
		trueFalse(
			'<p>Un semaforo binario e un mutex sono la stessa cosa in ogni sistema operativo.</p>',
			false
		)
	),
	twice(
		shortAnswer(
			'<p>Con pagine da 4 KiB, quanti bit dell’indirizzo virtuale indicano lo <em>scostamento</em> nella pagina?</p>',
			[12]
		)
	),
	twice(
		multiple('<p>Quali sono condizioni necessarie per lo <strong>stallo</strong> (deadlock)?</p>', [
			{ label: '<p>Mutua esclusione</p>', correct: true },
			{ label: '<p>Attesa circolare</p>', correct: true },
			{ label: '<p>Prelazione delle risorse</p>' }
		])
	),
	twice(
		gapSelect(
			'<p>Un processo che attende un evento passa nello stato [[1]]; quando l’evento arriva torna [[2]].</p>',
			[
				['bloccato', 'in esecuzione'],
				['pronto', 'terminato']
			],
			[0, 0]
		)
	)
];

const statistics: DemoQuestion[] = [
	twice(shortAnswer('<p>Qual è la media dei valori 2, 4, 6, 8?</p>', [5])),
	twice(
		single('<p>Una variabile di Poisson di parametro λ ha varianza:</p>', [
			{ label: '<p>λ</p>', correct: true },
			{ label: '<p>λ²</p>' },
			{ label: '<p>√λ</p>' }
		])
	),
	twice(
		trueFalse('<p>Due eventi incompatibili con probabilità positiva sono indipendenti.</p>', false)
	),
	twice(
		shortAnswer('<p>Come si chiama la probabilità di rifiutare un’ipotesi nulla vera?</p>', [
			'errore di primo tipo',
			'livello di significatività',
			'alfa'
		])
	),
	twice(
		multiple('<p>Quali di queste sono distribuzioni <strong>discrete</strong>?</p>', [
			{ label: '<p>Binomiale</p>', correct: true },
			{ label: '<p>Geometrica</p>', correct: true },
			{ label: '<p>Esponenziale</p>' }
		])
	)
];

const languages: DemoQuestion[] = [
	twice(
		single('<p>In Haskell, la valutazione degli argomenti è di default:</p>', [
			{ label: '<p>Pigra (lazy)</p>', correct: true },
			{ label: '<p>Stretta (eager)</p>' }
		])
	),
	twice(trueFalse('<p>Una funzione pura può modificare una variabile globale.</p>', false)),
	twice(
		shortAnswer('<p>Come si chiama una funzione che prende o restituisce funzioni?</p>', [
			'di ordine superiore',
			'higher-order',
			'funzione di ordine superiore'
		])
	),
	twice(
		match('<p>Associa ogni linguaggio al suo paradigma principale.</p>', [
			['Prolog', 'Logico'],
			['Haskell', 'Funzionale'],
			['C', 'Imperativo']
		])
	),
	twice(essay('<p>Descrivi in breve la differenza tra scoping statico e dinamico.</p>'))
];

const programming: DemoQuestion[] = [
	twice(
		single('<p>Che cosa stampa <code>print(7 // 2)</code> in Python?</p>', [
			{ label: '<p>3</p>', correct: true },
			{ label: '<p>3.5</p>' },
			{ label: '<p>4</p>' }
		])
	),
	twice(trueFalse('<p>In C gli array sono passati alle funzioni per valore.</p>', false)),
	twice(shortAnswer('<p>Quanto vale <code>len("bicocca")</code>?</p>', [7])),
	twice(
		cloze([
			'Una funzione che chiama sé stessa si dice ',
			{ text: ['ricorsiva'], size: 10 },
			' e deve avere un caso ',
			{ menu: ['base', 'medio', 'pessimo'], right: 0 },
			'.'
		])
	),
	twice(
		multiple('<p>Quali sono tipi <strong>immutabili</strong> in Python?</p>', [
			{ label: '<p>tuple</p>', correct: true },
			{ label: '<p>str</p>', correct: true },
			{ label: '<p>list</p>' }
		])
	)
];

const softwareEngineering: DemoQuestion[] = [
	twice(
		single('<p>Quale design pattern garantisce una sola istanza di una classe?</p>', [
			{ label: '<p>Singleton</p>', correct: true },
			{ label: '<p>Factory</p>' },
			{ label: '<p>Observer</p>' }
		])
	),
	twice(
		trueFalse('<p>Nel modello a cascata i requisiti si raccolgono a ogni iterazione.</p>', false)
	),
	twice(
		match('<p>Associa ogni diagramma UML a ciò che descrive.</p>', [
			['Diagramma delle classi', 'Struttura statica'],
			['Diagramma di sequenza', 'Scambio di messaggi'],
			['Diagramma dei casi d’uso', 'Funzionalità per gli attori']
		])
	),
	twice(
		shortAnswer('<p>Come si chiama il test che verifica una singola unità di codice isolata?</p>', [
			'unit test',
			'test di unità',
			'test unitario'
		])
	),
	twice(essay('<p>Spiega perché i test automatici aiutano il refactoring.</p>'))
];

const BANKS = new Map<DemoCourse, DemoQuestion[]>([
	[DemoCourses.algorithms, algorithms],
	[DemoCourses.databases, databases],
	[DemoCourses.operatingSystems, operatingSystems],
	[DemoCourses.statistics, statistics],
	[DemoCourses.languages, languages],
	[DemoCourses.programming, programming],
	[DemoCourses.softwareEngineering, softwareEngineering]
]);

export function demoBank(course: DemoCourse): DemoQuestion[] {
	return BANKS.get(course) ?? algorithms;
}

/**
 * Slots by page, as Moodle's layout lists them: a description shares the first page with the
 * question after it, every other question has a page of its own.
 */
export function demoPages(bank: readonly DemoQuestion[]): number[][] {
	const pages: number[][] = [];
	let pending: number[] = [];
	bank.forEach((question, index) => {
		pending.push(index + 1);
		if (question.type !== 'description') {
			pages.push(pending);
			pending = [];
		}
	});
	if (pending.length > 0) pages.push(pending);
	return pages;
}

export function demoLayout(bank: readonly DemoQuestion[]): string {
	return demoPages(bank)
		.map((slots) => [...slots, 0].join(','))
		.join(',');
}
