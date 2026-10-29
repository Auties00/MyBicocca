/** Longer texts are cut here, as on Android. */
const MAX_CHARS = 2_000_000;
/** Longer texts are shown without colours: tokenizing them would stall the page. */
export const MAX_HIGHLIGHTED_LINES = 6_000;

/**
 * The text of a file: UTF-8, else Windows-1252, the encoding teachers' Italian files on Windows
 * come in when they aren't UTF-8.
 */
export function decodeText(bytes: Uint8Array): { text: string; truncated: boolean } {
	let text: string;
	try {
		text = new TextDecoder('utf-8', { fatal: true }).decode(bytes);
	} catch {
		text = new TextDecoder('windows-1252').decode(bytes);
	}
	text = text.replace(/\r\n?/g, '\n');
	return text.length > MAX_CHARS
		? { text: text.slice(0, MAX_CHARS), truncated: true }
		: { text, truncated: false };
}

export type Language = 'c' | 'python' | 'shell' | 'sql' | 'plain';

const BY_EXTENSION: Readonly<Record<string, Language>> = {
	java: 'c',
	kt: 'c',
	kts: 'c',
	c: 'c',
	h: 'c',
	cpp: 'c',
	cc: 'c',
	cxx: 'c',
	hpp: 'c',
	cs: 'c',
	js: 'c',
	ts: 'c',
	go: 'c',
	rs: 'c',
	swift: 'c',
	dart: 'c',
	scala: 'c',
	gradle: 'c',
	py: 'python',
	sh: 'shell',
	bash: 'shell',
	pl: 'shell',
	rb: 'python',
	sql: 'sql'
};

export function languageOf(name: string): Language {
	const extension = /\.([a-z0-9]+)$/i.exec(name)?.[1]?.toLowerCase() ?? '';
	return BY_EXTENSION[extension] ?? 'plain';
}

const KEYWORDS: Readonly<Record<Exclude<Language, 'plain'>, ReadonlySet<string>>> = {
	c: new Set(
		(
			'abstract assert auto bool boolean break byte case catch char class const constexpr continue ' +
			'data default defer delete do double else enum export extends extern false final finally float ' +
			'fn for fun func function go if impl implements import in include inline instanceof int interface ' +
			'internal let long match mod mut namespace new nil null object open operator override package ' +
			'private protected public return sealed short signed sizeof static struct super switch template ' +
			'this throw throws trait true try type typedef typeof union unsigned use using val var virtual ' +
			'void volatile when where while yield async await'
		).split(' ')
	),
	python: new Set(
		(
			'and as assert async await break class continue def del elif else except False finally for ' +
			'from global if import in is lambda None nonlocal not or pass raise return True try while with ' +
			'yield self print end begin module require puts'
		).split(' ')
	),
	shell: new Set(
		(
			'if then else elif fi for while until do done case esac in function return local export echo ' +
			'exit break continue read set unset shift my sub use print'
		).split(' ')
	),
	sql: new Set(
		(
			'select from where and or not insert into values update set delete create table drop alter ' +
			'add primary key foreign references join inner left right outer full on as group by order ' +
			'having distinct union all exists in is null like between case when then else end limit ' +
			'offset view index unique check default constraint integer int varchar char text date ' +
			'decimal numeric boolean count sum avg min max with'
		).split(' ')
	)
};

const LINE_COMMENT: Readonly<Record<Exclude<Language, 'plain'>, string>> = {
	c: '//',
	python: '#',
	shell: '#',
	sql: '--'
};

export type TokenKind = 'text' | 'keyword' | 'string' | 'number' | 'comment';

export interface Token {
	kind: TokenKind;
	text: string;
}

const WORD = /[A-Za-z_][A-Za-z0-9_]*/y;
const NUMBER = /(?:0x[0-9a-f]+|\d+(?:\.\d+)?(?:e[+-]?\d+)?)[lfud]*/iy;

/**
 * Colours code line by line: comments, strings, numbers and keywords; anything else stays text.
 * `inBlock` carries an open `/* … *\/` comment across lines. Deliberately simple: a reading aid,
 * not a parser.
 */
export function tokenizeLine(
	line: string,
	language: Language,
	inBlock: boolean
): { tokens: Token[]; inBlock: boolean } {
	if (language === 'plain') return { tokens: [{ kind: 'text', text: line }], inBlock: false };
	const tokens: Token[] = [];
	const push = (kind: TokenKind, text: string) => {
		const last = tokens.at(-1);
		if (last !== undefined && last.kind === kind) last.text += text;
		else if (text !== '') tokens.push({ kind, text });
	};
	const keywords = KEYWORDS[language];
	const blocks = language === 'c' || language === 'sql';
	let at = 0;
	let open = inBlock;
	while (at < line.length) {
		if (open) {
			const end = line.indexOf('*/', at);
			const stop = end < 0 ? line.length : end + 2;
			push('comment', line.slice(at, stop));
			at = stop;
			open = end < 0;
			continue;
		}
		const rest = line.slice(at);
		if (rest.startsWith(LINE_COMMENT[language])) {
			push('comment', rest);
			break;
		}
		if (blocks && rest.startsWith('/*')) {
			open = true;
			continue;
		}
		const char = line[at];
		if (char === '"' || char === "'" || (char === '`' && language === 'c')) {
			let end = at + 1;
			while (end < line.length && line[end] !== char) end += line[end] === '\\' ? 2 : 1;
			push('string', line.slice(at, end + 1));
			at = end + 1;
			continue;
		}
		WORD.lastIndex = at;
		const word = WORD.exec(line);
		if (word !== null) {
			const text = word[0];
			const keyword = keywords.has(language === 'sql' ? text.toLowerCase() : text);
			push(keyword ? 'keyword' : 'text', text);
			at += text.length;
			continue;
		}
		NUMBER.lastIndex = at;
		const number = /[0-9]/.test(char) ? NUMBER.exec(line) : null;
		if (number !== null) {
			push('number', number[0]);
			at += number[0].length;
			continue;
		}
		push('text', char);
		at++;
	}
	return { tokens, inBlock: open };
}

/** Lines per chunk: what the viewer mounts at once, and the browser skips laying out off screen. */
export const CHUNK_LINES = 200;

/**
 * A text's lines in chunks, coloured on demand. Only C-like and SQL comments run across lines, so
 * only there does a chunk need the chunks before it.
 */
export class TextLines {
	readonly lines: readonly string[];
	readonly chunks: number;
	readonly #language: Language;
	readonly #tokens: Token[][][] = [];
	/** Whether a block comment is still open after each chunk coloured so far, in order. */
	readonly #open: boolean[] = [];

	constructor(text: string, name: string) {
		this.lines = text.split('\n');
		this.chunks = Math.ceil(this.lines.length / CHUNK_LINES);
		this.#language = this.lines.length > MAX_HIGHLIGHTED_LINES ? 'plain' : languageOf(name);
	}

	rows(chunk: number): number {
		return Math.min(CHUNK_LINES, this.lines.length - chunk * CHUNK_LINES);
	}

	tokens(chunk: number): Token[][] {
		const known = this.#tokens[chunk];
		if (known !== undefined) return known;
		if (this.#language !== 'c' && this.#language !== 'sql') {
			return (this.#tokens[chunk] = this.#colour(chunk, false).tokens);
		}
		for (let at = this.#open.length; at <= chunk; at++) {
			const { tokens, open } = this.#colour(at, at > 0 && this.#open[at - 1]);
			this.#tokens[at] = tokens;
			this.#open[at] = open;
		}
		return this.#tokens[chunk];
	}

	#colour(chunk: number, open: boolean): { tokens: Token[][]; open: boolean } {
		let inBlock = open;
		const tokens = this.lines.slice(chunk * CHUNK_LINES, (chunk + 1) * CHUNK_LINES).map((line) => {
			const result = tokenizeLine(line, this.#language, inBlock);
			inBlock = result.inBlock;
			return result.tokens;
		});
		return { tokens, open: inBlock };
	}
}
