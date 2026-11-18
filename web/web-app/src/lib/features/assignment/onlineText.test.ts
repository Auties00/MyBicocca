import { describe, expect, it } from 'vitest';
import { countWords, htmlToText } from './onlineText.ts';

describe('htmlToText', () => {
	it('keeps blocks and lines apart and decodes entities once', () => {
		const text = htmlToText('<p>Prima<br>seconda</p><ul><li>a &lt; b</li><li>c&nbsp;d</li></ul>');
		expect(text.split(/\s+/)).toEqual(['Prima', 'seconda', 'a', '<', 'b', 'c', 'd']);
	});
});

describe('countWords', () => {
	it('counts whitespace-separated words, block boundaries included', () => {
		expect(countWords('')).toBe(0);
		expect(countWords('  una   due\ntre ')).toBe(3);
		expect(countWords(htmlToText('<p>una</p><p>due</p>'))).toBe(2);
	});
});
