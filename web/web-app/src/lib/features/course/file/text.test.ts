import { describe, expect, it } from 'vitest';
import { CHUNK_LINES, decodeText, languageOf, TextLines, tokenizeLine } from './text.ts';

const kinds = (line: string, language = languageOf('x.java'), inBlock = false) =>
	tokenizeLine(line, language, inBlock).tokens.map(({ kind, text }) => [kind, text]);

describe('tokenizeLine', () => {
	it('colours keywords, strings, numbers and line comments', () => {
		expect(kinds('return "a // b" + 42; // fine')).toEqual([
			['keyword', 'return'],
			['text', ' '],
			['string', '"a // b"'],
			['text', ' + '],
			['number', '42'],
			['text', '; '],
			['comment', '// fine']
		]);
	});

	it('carries a block comment across lines', () => {
		const first = tokenizeLine('int x; /* starts', 'c', false);
		expect(first.inBlock).toBe(true);
		const second = tokenizeLine('ends */ y', 'c', true);
		expect(second.inBlock).toBe(false);
		expect(second.tokens[0]).toEqual({ kind: 'comment', text: 'ends */' });
	});

	it('knows each family’s comments and keywords, SQL in any case', () => {
		expect(kinds('def f(): # note', 'python')[0]).toEqual(['keyword', 'def']);
		expect(kinds('SELECT * -- all', 'sql')).toEqual([
			['keyword', 'SELECT'],
			['text', ' * '],
			['comment', '-- all']
		]);
		expect(kinds('plain text 12', 'plain')).toEqual([['text', 'plain text 12']]);
	});

	it('keeps escaped quotes inside a string', () => {
		expect(kinds('"say \\"hi\\""')).toEqual([['string', '"say \\"hi\\""']]);
	});
});

describe('decodeText', () => {
	it('reads UTF-8, and Windows-1252 when the bytes aren’t UTF-8', () => {
		expect(decodeText(new TextEncoder().encode('perché\r\nsì')).text).toBe('perché\nsì');
		expect(decodeText(Uint8Array.from([0x70, 0x65, 0x72, 0x63, 0x68, 0xe9])).text).toBe('perché');
	});
});

describe('TextLines', () => {
	it('colours a later chunk as if the whole text were read, comments carried in', () => {
		const lines = Array.from(
			{ length: CHUNK_LINES * 3 },
			(_, index) => `int a${index} = ${index};`
		);
		lines[CHUNK_LINES * 2 - 1] = '/* opens';
		const text = new TextLines(lines.join('\n'), 'main.c');
		expect(text.chunks).toBe(3);
		expect(text.tokens(2)[0]).toEqual([{ kind: 'comment', text: lines[CHUNK_LINES * 2] }]);
		expect(text.tokens(0)[0][0]).toEqual({ kind: 'keyword', text: 'int' });
	});

	it('counts the rows of the last, shorter chunk', () => {
		const text = new TextLines(
			Array.from({ length: CHUNK_LINES + 5 }, () => 'x').join('\n'),
			'a.txt'
		);
		expect([text.rows(0), text.rows(1)]).toEqual([CHUNK_LINES, 5]);
	});
});
