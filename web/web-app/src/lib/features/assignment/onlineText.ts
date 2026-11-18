import type { FileArea } from '#lib/features/course/editor/editorHtml.ts';

/*
 * The online text is HTML on Moodle and in the editor. Its embedded files live in an area of the
 * submission's own; the web service shows them at token-guarded addresses, while Moodle stores
 * them as `@@PLUGINFILE@@/name`, which is how they go back.
 */

const BLOCKS = new Set([
	'address',
	'article',
	'blockquote',
	'dd',
	'div',
	'dl',
	'dt',
	'figcaption',
	'figure',
	'footer',
	'h1',
	'h2',
	'h3',
	'h4',
	'h5',
	'h6',
	'header',
	'hr',
	'li',
	'ol',
	'p',
	'pre',
	'section',
	'table',
	'td',
	'th',
	'tr',
	'ul'
]);

function walk(node: Node, out: string[]): void {
	if (node.nodeType === node.TEXT_NODE) {
		out.push(node.textContent ?? '');
		return;
	}
	if (node.nodeType !== node.ELEMENT_NODE) return;
	const tag = (node as Element).tagName.toLowerCase();
	if (tag === 'script' || tag === 'style') return;
	if (tag === 'br') {
		out.push('\n');
		return;
	}
	const block = BLOCKS.has(tag);
	if (block) out.push('\n');
	for (const child of Array.from(node.childNodes)) walk(child, out);
	if (block) out.push('\n');
}

/** The words of Moodle's HTML, blocks kept apart, for counting them. */
export function htmlToText(html: string): string {
	const body = new DOMParser().parseFromString(`<body>${html}</body>`, 'text/html').body;
	const out: string[] = [];
	walk(body, out);
	return out
		.join('')
		.replace(/\u00a0/g, ' ')
		.trim();
}

/** Words as Moodle counts them for the word limit. */
export function countWords(text: string): number {
	const trimmed = text.trim();
	return trimmed === '' ? 0 : trimmed.split(/\s+/).length;
}

/** The online text's own files, which Moodle stores as `@@PLUGINFILE@@/name`. */
export const ONLINE_TEXT_FILES: FileArea = {
	component: 'assignsubmission_onlinetext',
	area: 'submissions_onlinetext'
};
