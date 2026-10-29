import { describe, expect, it } from 'vitest';
import { isRenderableHtml, sanitizeHtml } from './html.ts';

const parse = (html: string) =>
	new DOMParser().parseFromString(`<body>${html}</body>`, 'text/html');

describe('sanitizeHtml', () => {
	it('drops scripts, handlers, styles and classes, keeping the text', () => {
		const html = sanitizeHtml(
			'<p class="x" style="color:red" onclick="steal()">Ciao <script>steal()</script><b>tutti</b></p><style>p{}</style>'
		);
		expect(html).toBe('<p>Ciao <b>tutti</b></p>');
	});

	it('unwraps elements outside the allowlist', () => {
		expect(sanitizeHtml('<font color="red"><center>Avviso</center></font>')).toBe('Avviso');
	});

	it('keeps web and mail links, opening outside, and removes script addresses', () => {
		const links = parse(
			sanitizeHtml(
				'<a href="javascript:alert(1)">a</a><a href="mailto:prof@unimib.it">b</a><a href="/mod/forum/view.php?id=1">c</a>'
			)
		).querySelectorAll('a');
		expect(links[0].hasAttribute('href')).toBe(false);
		expect(links[1].getAttribute('href')).toBe('mailto:prof@unimib.it');
		expect(links[2].getAttribute('href')).toBe(
			'https://elearning.unimib.it/mod/forum/view.php?id=1'
		);
		expect(links[2].getAttribute('target')).toBe('_blank');
		expect(links[2].getAttribute('rel')).toBe('noopener noreferrer external');
	});

	it('keeps Moodle images out of src, so the token never lands in the page', () => {
		const html = sanitizeHtml(
			'<img src="https://elearning.unimib.it/pluginfile.php/1/course/summary/schema.png" alt="Schema">'
		);
		expect(html).toContain(
			'data-moodle-src="https://elearning.unimib.it/pluginfile.php/1/course/summary/schema.png"'
		);
		expect(html).not.toMatch(/\ssrc=/);
		expect(html).toContain('alt="Schema"');
	});

	it('keeps other web images, and drops data and script ones', () => {
		expect(sanitizeHtml('<img src="https://example.org/a.png">')).toContain(
			'src="https://example.org/a.png"'
		);
		expect(sanitizeHtml('<img src="data:image/svg+xml,<svg onload=x>">')).toBe('');
		expect(sanitizeHtml('<img src="javascript:x">')).toBe('');
	});

	it('turns an embedded player into a link to its page', () => {
		const link = parse(
			sanitizeHtml('<iframe src="https://www.youtube.com/embed/abc"></iframe>')
		).querySelector('p > a');
		expect(link?.getAttribute('href')).toBe('https://www.youtube.com/embed/abc');
		expect(link?.textContent).toBe('youtube.com');
		expect(sanitizeHtml('<iframe src="javascript:alert(1)"></iframe>')).toBe('');
	});

	it('drops comments and form controls', () => {
		expect(sanitizeHtml('<!-- nota --><form><input value="x"></form><p>ok</p>')).toBe('<p>ok</p>');
	});
});

describe('isRenderableHtml', () => {
	it('sees through the empty paragraphs Moodle leaves', () => {
		expect(isRenderableHtml('<p></p><br>')).toBe(false);
		expect(isRenderableHtml(null)).toBe(false);
		expect(isRenderableHtml('<p><img src="x.png"></p>')).toBe(true);
		expect(isRenderableHtml('<p>Testo</p>')).toBe(true);
	});
});
