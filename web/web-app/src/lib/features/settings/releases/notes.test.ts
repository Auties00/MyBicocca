import { describe, expect, it } from 'vitest';
import { parseInline, parseReleaseNotes } from './notes.ts';

describe('parseReleaseNotes', () => {
	it('reads the release template', () => {
		const body = [
			"Check out the [past notes](https://example.org) if you're",
			'upgrading.',
			'',
			'### ✨ New Features',
			'- **Beta updates**: opt in',
			'* Second',
			'1. Third',
			'',
			'> [!NOTE]',
			'> The app asks for',
			'> permission.',
			'',
			'> quoted prose'
		].join('\r\n');
		expect(parseReleaseNotes(body)).toEqual([
			{
				type: 'paragraph',
				text: "Check out the [past notes](https://example.org) if you're upgrading."
			},
			{ type: 'heading', level: 3, text: '✨ New Features' },
			{
				type: 'list',
				items: [{ text: '**Beta updates**: opt in' }, { text: 'Second' }, { text: 'Third' }]
			},
			{ type: 'callout', kind: 'note', text: 'The app asks for\npermission.' },
			{ type: 'paragraph', text: 'quoted prose' }
		]);
	});

	it('ends a list at prose and drops closing hashes of headings', () => {
		expect(parseReleaseNotes('## Fixes ##\n- one\nafter')).toEqual([
			{ type: 'heading', level: 2, text: 'Fixes' },
			{ type: 'list', items: [{ text: 'one' }] },
			{ type: 'paragraph', text: 'after' }
		]);
	});

	it('gives nothing for an empty body', () => {
		expect(parseReleaseNotes('  \n\n')).toEqual([]);
	});
});

describe('parseInline', () => {
	it('styles Markdown and the inline HTML GitHub allows', () => {
		expect(parseInline('a **b** *c* ~~d~~ <u>e</u> `f`')).toEqual([
			{ text: 'a ' },
			{ text: 'b', bold: true },
			{ text: ' ' },
			{ text: 'c', italic: true },
			{ text: ' ' },
			{ text: 'd', strike: true },
			{ text: ' ' },
			{ text: 'e', underline: true },
			{ text: ' ' },
			{ text: 'f', code: true }
		]);
	});

	it('links web addresses and mentions, and keeps other links as text', () => {
		expect(parseInline('[notes](https://x.org) by @LordLux, [x](ftp://host/file)')).toEqual([
			{ text: 'notes', href: 'https://x.org' },
			{ text: ' by ' },
			{ text: '@LordLux', href: 'https://github.com/LordLux' },
			{ text: ', x' }
		]);
	});

	it('leaves an unpaired marker as text and drops an unpaired tag', () => {
		expect(parseInline('5 * 3 = 15 <b>bold')).toEqual([{ text: '5 * 3 = 15 bold' }]);
		expect(parseInline('a</i>b')).toEqual([{ text: 'ab' }]);
	});

	it('nests overlapping styles and breaks lines', () => {
		expect(parseInline('**bold <i>both**</i><br>next')).toEqual([
			{ text: 'bold ', bold: true },
			{ text: 'both', bold: true, italic: true },
			{ text: '', br: true },
			{ text: 'next' }
		]);
	});

	it('does not read an e-mail address as a mention', () => {
		expect(parseInline('write to a@b.it')).toEqual([{ text: 'write to a@b.it' }]);
	});
});
