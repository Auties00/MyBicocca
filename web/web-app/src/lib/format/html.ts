/*
 * Rich text the university's services send (Moodle's teacher HTML, booking descriptions), made safe
 * to render and read as text: every `{@html}` of remote markup renders what `sanitizeHtml` returns.
 */

/** Elements kept; any other is unwrapped, its text kept. */
const ALLOWED = new Set([
	'a',
	'abbr',
	'b',
	'blockquote',
	'br',
	'caption',
	'cite',
	'code',
	'dd',
	'del',
	'div',
	'dl',
	'dt',
	'em',
	'figcaption',
	'figure',
	'h1',
	'h2',
	'h3',
	'h4',
	'h5',
	'h6',
	'hr',
	'i',
	'img',
	'ins',
	'kbd',
	'li',
	'mark',
	'ol',
	'p',
	'pre',
	'q',
	's',
	'small',
	'span',
	'strong',
	'sub',
	'sup',
	'table',
	'tbody',
	'td',
	'tfoot',
	'th',
	'thead',
	'tr',
	'u',
	'ul'
]);

/** Elements dropped with everything inside them. */
const DROPPED = new Set([
	'script',
	'style',
	'iframe',
	'object',
	'embed',
	'form',
	'input',
	'button',
	'select',
	'textarea',
	'svg',
	'math',
	'link',
	'meta',
	'base',
	'template',
	'noscript',
	'video',
	'audio',
	'source',
	'track',
	'canvas',
	'frame',
	'frameset',
	'applet',
	'title',
	'head'
]);

const ATTRIBUTES: Readonly<Record<string, readonly string[]>> = {
	a: ['href', 'title'],
	img: ['src', 'alt', 'title', 'width', 'height'],
	ol: ['start', 'type', 'reversed'],
	li: ['value'],
	td: ['colspan', 'rowspan'],
	th: ['colspan', 'rowspan', 'scope'],
	abbr: ['title']
};

const SAFE_LINK = /^(?:https?:|mailto:|tel:)/i;
const SAFE_IMAGE = /^https?:/i;
const PLUGINFILE = /\/(?:webservice\/)?pluginfile\.php\//;

/** Moodle's site, against which relative links resolve: most rich text comes from it. */
const BASE = 'https://elearning.unimib.it/';

/**
 * Rich text made safe to render with `{@html}`: only formatting elements and plain links stay;
 * scripts, frames, forms, styles, classes and event handlers go. Links open in a new tab. Images
 * Moodle serves need the user's token, which must never sit in a `src`: they keep their address in
 * `data-moodle-src` for the page to fetch with the token and show as a blob.
 */
export function sanitizeHtml(html: string): string {
	const document = new DOMParser().parseFromString(`<body>${html}</body>`, 'text/html');
	clean(document.body);
	return document.body.innerHTML.trim();
}

function clean(parent: Element): void {
	for (const node of Array.from(parent.childNodes)) {
		if (node.nodeType === 8) {
			node.remove();
			continue;
		}
		if (node.nodeType !== 1) continue;
		const element = node as Element;
		const tag = element.tagName.toLowerCase();
		if (tag === 'iframe') {
			embed(element);
			continue;
		}
		if (DROPPED.has(tag)) {
			element.remove();
			continue;
		}
		clean(element);
		if (!ALLOWED.has(tag)) {
			element.replaceWith(...Array.from(element.childNodes));
			continue;
		}
		const allowed = ATTRIBUTES[tag] ?? [];
		for (const attribute of Array.from(element.attributes)) {
			if (!allowed.includes(attribute.name.toLowerCase())) element.removeAttribute(attribute.name);
		}
		if (tag === 'a') link(element);
		if (tag === 'img') image(element);
	}
}

function absolute(value: string): string | null {
	try {
		return new URL(value.trim(), BASE).href;
	} catch {
		return null;
	}
}

function link(element: Element): void {
	const href = absolute(element.getAttribute('href') ?? '');
	if (href === null || !SAFE_LINK.test(href)) {
		element.removeAttribute('href');
		return;
	}
	element.setAttribute('href', href);
	element.setAttribute('target', '_blank');
	element.setAttribute('rel', 'noopener noreferrer external');
}

/** An embedded player (YouTube, a form) can't run here: it becomes a link to its page. */
function embed(element: Element): void {
	const src = absolute(element.getAttribute('src') ?? '');
	if (src === null || !/^https:/i.test(src)) {
		element.remove();
		return;
	}
	const link = element.ownerDocument.createElement('a');
	link.textContent = new URL(src).hostname.replace(/^www\./, '');
	link.setAttribute('href', src);
	link.setAttribute('target', '_blank');
	link.setAttribute('rel', 'noopener noreferrer external');
	const paragraph = element.ownerDocument.createElement('p');
	paragraph.append(link);
	element.replaceWith(paragraph);
}

function image(element: Element): void {
	const src = absolute(element.getAttribute('src') ?? '');
	element.removeAttribute('src');
	if (src === null || !SAFE_IMAGE.test(src)) {
		element.remove();
		return;
	}
	if (PLUGINFILE.test(new URL(src).pathname)) element.setAttribute('data-moodle-src', src);
	else element.setAttribute('src', src);
	element.setAttribute('loading', 'lazy');
	element.setAttribute('decoding', 'async');
}

/**
 * Whether teacher HTML shows anything: text, or a link, image, table or embed without text.
 * Moodle fills empty summaries with `<p></p>` and stray `<br>`.
 */
export function isRenderableHtml(html: string | null | undefined): boolean {
	if (html == null) return false;
	if (/<a\s|href=|<img|<table|<iframe/i.test(html)) return true;
	return htmlToText(html) !== '';
}

const BLOCKS = 'p, div, li, tr, h1, h2, h3, h4, h5, h6, blockquote, pre, dt, dd';

/** The text of `html` with its line structure: blocks and `<br>` end lines. */
export function htmlToText(html: string): string {
	const document = new DOMParser().parseFromString(`<body>${html}</body>`, 'text/html');
	for (const br of Array.from(document.body.querySelectorAll('br'))) br.replaceWith('\n');
	for (const block of Array.from(document.body.querySelectorAll(BLOCKS))) block.append('\n');
	return (document.body.textContent ?? '')
		.split('\n')
		.map((line) => line.replace(/\s+/g, ' ').trim())
		.join('\n')
		.replace(/\n{3,}/g, '\n\n')
		.trim();
}

/** The text of `html` on one line, as afterlink badges and previews need it. */
export function htmlToLine(html: string): string {
	return htmlToText(html).replace(/\s+/g, ' ').trim();
}
