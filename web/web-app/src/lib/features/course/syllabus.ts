import type {
	MoodleCourse,
	MoodleWebSyllabus,
	MoodleWebSyllabusField
} from '@mybicocca/data-api/elearning';

export type CourseLevel = 'bachelor' | 'master' | 'doctorate';
export type Semester = 'first' | 'second' | 'annual';

/** One part of the extended programme: a title (maybe empty) and its topics. */
export interface ProgrammePart {
	title: string;
	items: string[];
}

/** The course sheet's facts, from the course's custom fields ("Scheda del corso"). */
export interface CourseMetadata {
	sector: string | null;
	credits: number | null;
	period: string | null;
	type: string | null;
	hours: number | null;
	degreeType: string | null;
	language: string | null;
}

/** What the overview shows: facts, then sections of teacher HTML, to render through `TeacherHtml`. */
export interface Syllabus {
	credits: number | null;
	hours: number | null;
	language: string | null;
	level: CourseLevel | null;
	semester: Semester | null;
	sector: string | null;
	objectives: string | null;
	summary: string | null;
	programme: ProgrammePart[];
	/** The programme as written, for when no parts can be read out of it. */
	programmeHtml: string | null;
	prerequisites: string | null;
	teachingMethod: string | null;
	referenceMaterial: string | null;
	assessment: string | null;
	officeHours: string | null;
	sustainableGoals: string | null;
}

export function courseMetadata(course: MoodleCourse | undefined): CourseMetadata {
	const values = new Map(
		(course?.customFields ?? []).map((field) => [field.shortName, field.value.trim() || null])
	);
	const integer = (key: string) => {
		const value = Number.parseInt(values.get(key) ?? '', 10);
		return Number.isFinite(value) && value > 0 ? value : null;
	};
	return {
		sector: values.get('settore') ?? null,
		credits: integer('cfu'),
		period: values.get('periodo') ?? null,
		type: values.get('tipo') ?? null,
		hours: integer('ore'),
		degreeType: values.get('tipologiacds') ?? null,
		language: values.get('lingua') ?? null
	};
}

/** The syllabus tab in the app's language, else English, else the first one. */
export function pickSyllabus(
	tabs: readonly MoodleWebSyllabus[],
	language: string
): MoodleWebSyllabus | undefined {
	const by = (code: string) => tabs.find((tab) => tab.language.toLowerCase() === code);
	return by(language) ?? by('en') ?? tabs[0];
}

/**
 * The overview from the custom fields and the syllabus fields. Field titles are the site's own,
 * in Italian or English, so they are matched by lowercase substring, first key first.
 */
export function buildSyllabus(
	fields: readonly MoodleWebSyllabusField[],
	metadata: CourseMetadata
): Syllabus {
	const parsed = fields.map((field) => ({
		title: field.title.trim().toLowerCase(),
		body: new DOMParser().parseFromString(`<body>${field.htmlContent}</body>`, 'text/html').body,
		html: field.htmlContent
	}));
	const pick = (...keys: string[]) => {
		for (const key of keys) {
			const match = parsed.find((field) => field.title.includes(key));
			if (match !== undefined && textOf(match.body) !== '') return match;
		}
		return undefined;
	};
	const html = (...keys: string[]) => pick(...keys)?.html ?? null;
	const text = (...keys: string[]) => {
		const field = pick(...keys);
		return field === undefined ? null : textOf(field.body);
	};
	const programme = pick(
		'programma esteso',
		'detailed program',
		'programma del corso',
		'programma'
	);
	return {
		credits: metadata.credits,
		hours: metadata.hours,
		language: metadata.language ?? text('lingua', 'language'),
		level: courseLevel(metadata.degreeType ?? text('livello', 'tipologia di corso')),
		semester:
			semester(metadata.period) ?? semester(text('periodo', 'semestre', 'ciclo', 'semester')),
		sector: metadata.sector,
		objectives: html('obiettivi', 'aims'),
		summary: html('contenuti sintetici', 'contents', 'syllabus'),
		programme: programme === undefined ? [] : parseProgramme(programme.body),
		programmeHtml: programme?.html ?? null,
		prerequisites: html('prerequisiti', 'prerequisites'),
		teachingMethod: html('modalità didattica', 'teaching form', 'metodi didattici'),
		referenceMaterial: html(
			'materiale didattico',
			'textbook and teaching resource',
			'testi di riferimento',
			'bibliografia'
		),
		assessment: html(
			'verifica del profitto',
			'assessment method',
			"modalità d'esame",
			'modalità di esame',
			"verifica dell'apprendimento"
		),
		officeHours: html('orario di ricevimento', 'office hours', 'ricevimento'),
		sustainableGoals: html('sustainable development')
	};
}

export function courseLevel(text: string | null): CourseLevel | null {
	const lower = text?.toLowerCase() ?? '';
	if (/magistrale|specialistica|master/.test(lower)) return 'master';
	if (/dottor|doctorate|phd/.test(lower)) return 'doctorate';
	if (/triennale|bachelor|laurea/.test(lower)) return 'bachelor';
	return null;
}

export function semester(text: string | null): Semester | null {
	const lower = text?.toLowerCase() ?? '';
	if (/primo|1°|\bi sem|first/.test(lower)) return 'first';
	if (/secondo|2°|\bii sem|second/.test(lower)) return 'second';
	if (/annuale|annual|full year/.test(lower)) return 'annual';
	return null;
}

/** Whether a month (0 = January) is taught in `term`; terms run September to August. */
export function teachesIn(term: Semester, month: number): boolean {
	if (term === 'annual') return true;
	const first = month >= 8 || month <= 1;
	return term === 'first' ? first : !first;
}

const BLOCK_TAGS = new Set([
	'p',
	'div',
	'li',
	'ul',
	'ol',
	'tr',
	'td',
	'th',
	'h1',
	'h2',
	'h3',
	'h4',
	'h5',
	'h6',
	'blockquote',
	'pre',
	'br'
]);
const LIST_TAGS = new Set(['ul', 'ol']);
const TERMINALS = new Set(['.', '!', '?', ':', ';']);
const LEADING_BULLET = /^[-–—•·]\s*/;
const MARKDOWN_HEADER = /^\*\*\s*([^*]+?)\s*(?:\*\*)?$/;
const SENTENCE_BOUNDARY = /(?<=[.!?])\s+(?=[A-ZÀ-ÖØ-Þ0-9])/;
const HEADING_ENDINGS = new Set(['.', ',', ';']);

const tag = (element: Element) => element.tagName.toLowerCase();
const isLower = (char: string | undefined) => char !== undefined && /\p{Ll}/u.test(char);

/** Text with whitespace collapsed and a space between blocks, as Jsoup's `text()` reads it. */
function textOf(node: Node): string {
	const parts: string[] = [];
	const walk = (current: Node) => {
		for (const child of Array.from(current.childNodes)) {
			if (child.nodeType === 3) parts.push(child.textContent ?? '');
			else if (child.nodeType === 1) {
				const block = BLOCK_TAGS.has(tag(child as Element));
				if (block) parts.push(' ');
				walk(child);
				if (block) parts.push(' ');
			}
		}
	};
	walk(node);
	return parts.join('').replace(/\s+/g, ' ').trim();
}

/** The element's lines, split at every `<br>`. */
function splitOnBreaks(element: Element): string[] {
	const lines: string[][] = [[]];
	const walk = (current: Node) => {
		for (const child of Array.from(current.childNodes)) {
			if (child.nodeType === 3) lines[lines.length - 1].push(child.textContent ?? '');
			else if (child.nodeType === 1) {
				if (tag(child as Element) === 'br') lines.push([]);
				else walk(child);
			}
		}
	};
	walk(element);
	return lines.map((line) => line.join('').replace(/\s+/g, ' ').trim());
}

function opensWith(parent: Element, child: Element): boolean {
	const first = Array.from(parent.childNodes).find(
		(node) => node.nodeType !== 3 || (node.textContent ?? '').trim() !== ''
	);
	return first === child;
}

function isRunInLabel(emphasis: Element, paragraph: Element): boolean {
	const label = textOf(emphasis);
	return (
		label.length >= 1 &&
		label.length <= 60 &&
		(label.endsWith('.') || label.endsWith(':')) &&
		textOf(paragraph).length > label.length
	);
}

function isHeadingLike(text: string): boolean {
	return text.length <= 80 && !HEADING_ENDINGS.has(text.at(-1) ?? '') && !isLower(text[0]);
}

function splitTopLevelCommas(text: string): string[] {
	const pieces: string[] = [];
	let current = '';
	let depth = 0;
	for (const char of text) {
		if (char === '(' || char === '[') {
			depth++;
			current += char;
		} else if (char === ')' || char === ']') {
			depth = Math.max(0, depth - 1);
			current += char;
		} else if ((char === ',' || char === ';') && depth === 0) {
			pieces.push(current);
			current = '';
		} else current += char;
	}
	pieces.push(current);
	return pieces;
}

function topicItems(detail: string): string[] {
	return detail
		.split(SENTENCE_BOUNDARY)
		.flatMap((sentence) => {
			const parts = splitTopLevelCommas(sentence);
			return parts.length >= 3 ? parts : [sentence];
		})
		.map((item) => item.trim())
		.filter((item) => item !== '');
}

function directChildren(element: Element, tags: ReadonlySet<string>): Element[] {
	return Array.from(element.children).filter((child) => tags.has(tag(child)));
}

/**
 * The extended programme as titled parts, read out of the shapes teachers write it in:
 * `<p><strong>Title</strong><br>topic<br>topic</p>`; a short plain `<p>` (or a one-item
 * `<ol start>`) heading the list after it; bare lists and paragraphs; an `<li>` with its own
 * sub-list; run-in `<em>Label.</em>` headers, typed `**Title**` lines, dash outlines and prose
 * hard-wrapped with `<br>`. Topics that run on in long sentences are split at sentence and comma
 * boundaries, so each reads as one short item.
 */
export function parseProgramme(body: Element): ProgrammePart[] {
	const parts: ProgrammePart[] = [];
	let title: string | null = null;
	let titlePending = false;
	let items: string[] = [];

	const flush = () => {
		if (items.length > 0 || titlePending) parts.push({ title: title ?? '', items });
		items = [];
		titlePending = false;
	};
	const start = (text: string) => {
		flush();
		title = text
			.trim()
			.replace(/[.:;]+$/, '')
			.trim();
		titlePending = true;
	};
	const addLine = (raw: string) => {
		const line = raw.trim();
		if (line === '') return;
		const last = items.at(-1);
		const continuation =
			isLower(line[0]) && last !== undefined && !TERMINALS.has(last.at(-1) ?? '');
		const text = line.replace(LEADING_BULLET, '').trim();
		if (text === '') return;
		if (continuation) items[items.length - 1] = `${last} ${text}`;
		else items.push(text);
	};
	const nextIsList = (node: Element) => {
		const next = node.nextElementSibling;
		return next !== null && LIST_TAGS.has(tag(next));
	};
	const appendList = (list: Element) => {
		const ordered = tag(list) === 'ol';
		const first = Number.parseInt(list.getAttribute('start') ?? '', 10) || 1;
		directChildren(list, new Set(['li'])).forEach((li, index) => {
			const text = textOf(li);
			if (text !== '') items.push(ordered ? `${first + index}. ${text}` : text);
		});
	};
	const runIn = (rest: Element) => {
		const pieces = splitOnBreaks(rest).filter((piece) => piece !== '');
		if (pieces.length === 1) pieces[0].split(SENTENCE_BOUNDARY).forEach(addLine);
		else pieces.forEach(addLine);
	};

	const dashOutline =
		directChildren(body, new Set(['p']))
			.flatMap(splitOnBreaks)
			.filter((line) => LEADING_BULLET.test(line)).length >= 2;

	for (const node of Array.from(body.children)) {
		const name = tag(node);
		if (name === 'p') {
			const strong = node.querySelector('strong, b');
			const emphasis = node.querySelector('em, i');
			if (strong !== null && textOf(strong) !== '' && opensWith(node, strong)) {
				start(textOf(strong));
				const rest = node.cloneNode(true) as Element;
				rest.querySelector('strong, b')?.remove();
				runIn(rest);
			} else if (emphasis !== null && opensWith(node, emphasis) && isRunInLabel(emphasis, node)) {
				start(textOf(emphasis));
				const rest = node.cloneNode(true) as Element;
				rest.querySelector('em, i')?.remove();
				runIn(rest);
			} else {
				const pieces = splitOnBreaks(node).filter((piece) => piece !== '');
				const first = pieces[0]?.trim();
				const markdown = first === undefined ? null : MARKDOWN_HEADER.exec(first);
				if (markdown !== null) {
					start(markdown[1]);
					pieces.slice(1).forEach(addLine);
				} else if (dashOutline) {
					for (const piece of pieces) {
						const line = piece.trim();
						if (!LEADING_BULLET.test(line) && line.length <= 80) start(line);
						else addLine(line);
					}
				} else if (
					first !== undefined &&
					pieces.length === 1 &&
					nextIsList(node) &&
					isHeadingLike(first)
				) {
					start(first);
				} else pieces.forEach(addLine);
			}
		} else if (/^h[1-6]$/.test(name)) {
			start(textOf(node));
		} else if (LIST_TAGS.has(name)) {
			const lis = directChildren(node, new Set(['li']));
			const ordered = name === 'ol';
			const first = Number.parseInt(node.getAttribute('start') ?? '', 10) || 1;
			const nestedOf = (li: Element) => directChildren(li, LIST_TAGS);
			const plain = lis.filter((li) => nestedOf(li).length === 0);
			const topicRun =
				title === null &&
				plain.length >= 2 &&
				plain.reduce((sum, li) => sum + textOf(li).length, 0) / plain.length > 120;
			const lone = lis.length === 1 ? lis[0] : undefined;
			if (
				ordered &&
				lone !== undefined &&
				nextIsList(node) &&
				nestedOf(lone).length === 0 &&
				textOf(lone) !== ''
			) {
				start(`${first}. ${textOf(lone)}`);
				continue;
			}
			lis.forEach((li, index) => {
				const ordinal = ordered ? `${first + index}. ` : '';
				const nested = nestedOf(li);
				if (nested.length > 0) {
					const own = li.cloneNode(true) as Element;
					for (const list of Array.from(own.querySelectorAll('ul, ol'))) list.remove();
					start(ordinal + textOf(own));
					nested.forEach(appendList);
					return;
				}
				const text = textOf(li);
				if (text === '') return;
				if (!topicRun) {
					items.push(ordinal + text);
					return;
				}
				const colon = text.indexOf(':');
				const sentences = text.split(SENTENCE_BOUNDARY);
				if (colon >= 1 && colon <= 60) {
					start(ordinal + text.slice(0, colon));
					items.push(...topicItems(text.slice(colon + 1)));
				} else if (sentences.length >= 2 && sentences[0].length <= 80) {
					start(ordinal + sentences[0]);
					items.push(...topicItems(text.slice(sentences[0].length)));
				} else if (text.length <= 80) {
					start(ordinal + text);
				} else {
					start('');
					items.push(...topicItems(text));
				}
			});
		}
	}
	flush();
	return parts;
}
