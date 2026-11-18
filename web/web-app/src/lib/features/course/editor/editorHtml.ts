/**
 * The HTML the e-learning editor sends: forum posts and assignments' online text. Moodle stores
 * them as HTML (FORMAT_HTML) and cleans them itself, so this keeps what the editor can produce and
 * the student meant: paragraphs, emphasis, links, lists, quotes and code, plus the images and
 * tables of a text being edited.
 */

const KEPT = new Set([
	'p',
	'br',
	'strong',
	'em',
	'u',
	's',
	'a',
	'ul',
	'ol',
	'li',
	'blockquote',
	'pre',
	'code',
	'h3',
	'h4',
	'sub',
	'sup',
	'img',
	'table',
	'thead',
	'tbody',
	'tr',
	'th',
	'td',
	'hr'
]);

/** Elements written differently by browsers' editing commands and pasted pages. */
const RENAMED: Readonly<Record<string, string>> = {
	b: 'strong',
	i: 'em',
	strike: 's',
	del: 's',
	div: 'p',
	h1: 'h3',
	h2: 'h3',
	h5: 'h4',
	h6: 'h4'
};

const DROPPED = new Set([
	'script',
	'style',
	'iframe',
	'object',
	'embed',
	'form',
	'svg',
	'template'
]);

const ATTRIBUTES: Readonly<Record<string, readonly string[]>> = {
	a: ['href'],
	img: ['src', 'alt', 'width', 'height'],
	ol: ['start'],
	td: ['colspan', 'rowspan'],
	th: ['colspan', 'rowspan']
};

const BLOCKS = new Set(['p', 'ul', 'ol', 'blockquote', 'pre', 'h3', 'h4', 'table', 'hr']);
const BLOCK_SELECTOR = 'p, div, ul, ol, blockquote, pre, table, h1, h2, h3, h4, h5, h6';
const SAFE_LINK = /^(?:https?:|mailto:)/i;

/** A Moodle file area, as pluginfile addresses name it; without an item, any item's. */
export interface FileArea {
	component: string;
	area: string;
	itemId?: number;
}

/** A file's address as the web service writes it, up to its area, then its path and query. */
const ADDRESS = String.raw`https?://[^\s"'<>]+?/(?:webservice/)?pluginfile\.php/\d+/`;
const PATH = String.raw`/([^\s"'<>?#]+)(?:\?[^\s"'<>#]*)?`;

function areaFile({ component, area, itemId }: FileArea): RegExp {
	return new RegExp(`${ADDRESS}${component}/${area}/${itemId ?? String.raw`\d+`}${PATH}`, 'g');
}

/**
 * `html` with its own files back behind Moodle's `@@PLUGINFILE@@/path` placeholder, as Moodle
 * stores them, instead of the token-guarded addresses the web service showed them at.
 */
export function withPlaceholders(html: string, files: FileArea): string {
	return html.replace(areaFile(files), '@@PLUGINFILE@@/$1');
}

export interface EditorHtmlOptions {
	/** The file area of the text being edited, whose embedded images keep their placeholder. */
	files?: FileArea;
}

/** The editor's content as the HTML to send, or '' when it holds nothing. */
export function editorHtml(html: string, options: EditorHtmlOptions = {}): string {
	const document = new DOMParser().parseFromString(`<body>${html}</body>`, 'text/html');
	const body = document.body;
	clean(body, options);
	paragraphs(body);
	trimEmpty(body);
	return isBlank(body) ? '' : body.innerHTML.trim();
}

function clean(parent: Element, options: EditorHtmlOptions): void {
	for (const node of Array.from(parent.childNodes)) {
		if (node.nodeType === 8) {
			node.remove();
			continue;
		}
		if (node.nodeType !== 1) continue;
		let element = node as Element;
		const original = element.tagName.toLowerCase();
		if (DROPPED.has(original)) {
			element.remove();
			continue;
		}
		// A wrapper of blocks is just unwrapped: as a paragraph it would hold paragraphs.
		const wrapper = original === 'div' && element.querySelector(BLOCK_SELECTOR) !== null;
		const tag = wrapper ? original : (RENAMED[original] ?? original);
		if (tag !== original && KEPT.has(tag)) element = rename(element, tag);
		clean(element, options);
		if (!KEPT.has(tag)) {
			element.replaceWith(...Array.from(element.childNodes));
			continue;
		}
		const image = tag === 'img' ? imageSource(element) : null;
		const allowed = ATTRIBUTES[tag] ?? [];
		for (const attribute of Array.from(element.attributes)) {
			if (!allowed.includes(attribute.name.toLowerCase())) element.removeAttribute(attribute.name);
		}
		if (tag === 'a') link(element);
		if (tag === 'img') {
			if (image === null) element.remove();
			else
				element.setAttribute('src', options.files ? withPlaceholders(image, options.files) : image);
		}
	}
}

function rename(element: Element, tag: string): Element {
	const renamed = element.ownerDocument.createElement(tag);
	for (const attribute of Array.from(element.attributes)) {
		renamed.setAttribute(attribute.name, attribute.value);
	}
	renamed.append(...Array.from(element.childNodes));
	element.replaceWith(renamed);
	return renamed;
}

function link(element: Element): void {
	const href = element.getAttribute('href')?.trim() ?? '';
	if (!SAFE_LINK.test(href)) element.replaceWith(...Array.from(element.childNodes));
}

/** An embedded image's real address: the editor shows Moodle's images from `data-moodle-src`. */
function imageSource(element: Element): string | null {
	const source = element.getAttribute('data-moodle-src') ?? element.getAttribute('src') ?? '';
	return /^https?:/i.test(source) || source.startsWith('@@PLUGINFILE@@/') ? source : null;
}

/** Text typed straight into the editor, outside any block, gets a paragraph of its own. */
function paragraphs(body: Element): void {
	let run: ChildNode[] = [];
	const flush = (before: ChildNode | null) => {
		if (run.some((node) => node.nodeType !== 3 || (node.textContent ?? '').trim() !== '')) {
			const paragraph = body.ownerDocument.createElement('p');
			body.insertBefore(paragraph, before);
			paragraph.append(...run);
		} else {
			run.forEach((node) => node.remove());
		}
		run = [];
	};
	for (const node of Array.from(body.childNodes)) {
		const block = node.nodeType === 1 && BLOCKS.has((node as Element).tagName.toLowerCase());
		if (block) flush(node);
		else run.push(node);
	}
	flush(null);
}

function emptyBlock(node: ChildNode): boolean {
	if (node.nodeType === 3) return (node.textContent ?? '').trim() === '';
	if (node.nodeType !== 1) return true;
	const element = node as Element;
	return (
		element.tagName.toLowerCase() === 'p' &&
		(element.textContent ?? '').trim() === '' &&
		element.querySelector('img') === null
	);
}

/** Editors leave empty paragraphs and line breaks at both ends. */
function trimEmpty(body: Element): void {
	while (body.firstChild !== null && emptyBlock(body.firstChild)) body.firstChild.remove();
	while (body.lastChild !== null && emptyBlock(body.lastChild)) body.lastChild.remove();
	for (const paragraph of Array.from(body.querySelectorAll('p'))) {
		while (paragraph.lastChild?.nodeName.toLowerCase() === 'br') paragraph.lastChild.remove();
	}
}

function isBlank(body: Element): boolean {
	return (body.textContent ?? '').trim() === '' && body.querySelector('img, table, hr') === null;
}
