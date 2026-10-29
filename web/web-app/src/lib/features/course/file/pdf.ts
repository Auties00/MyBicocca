import { openInTab } from './save.ts';

const latin1 = new TextDecoder('latin1');

/** How far past a cross-reference section its trailer is looked for: tables of 200,000 objects. */
const SECTION_WINDOW = 4_000_000;

/** The dictionary of the trailer that the cross-reference section at `offset` ends with. */
function trailerAt(bytes: Uint8Array, offset: number, end: number): string | undefined {
	const section = latin1.decode(bytes.subarray(offset, Math.min(end, offset + SECTION_WINDOW)));
	if (section.startsWith('xref')) {
		// The first trailer after the table is its own: a linearized file has another at its end.
		const start = section.indexOf('trailer');
		if (start < 0) return undefined;
		const stop = section.indexOf('startxref', start);
		return section.slice(start, stop < 0 ? undefined : stop);
	}
	// A cross-reference stream carries the trailer's keys in its own dictionary.
	if (!/^\d+\s+\d+\s+obj\b/.test(section)) return undefined;
	const stream = section.indexOf('stream');
	const dictionary = section.slice(0, stream < 0 ? undefined : stream);
	return /\/Type\s*\/XRef\b/.test(dictionary) ? dictionary : undefined;
}

/** `text` as a PDF text string: UTF-16BE behind a byte-order mark, in hex. */
function textString(text: string): string {
	let hex = 'FEFF';
	for (let index = 0; index < text.length; index++) {
		hex += text.charCodeAt(index).toString(16).padStart(4, '0').toUpperCase();
	}
	return `<${hex}>`;
}

/**
 * `bytes` with `title` as the document's title, which browsers' viewers show in place of the
 * blob's random address. The title goes in an incremental update, the format's own way of editing
 * a file: the original bytes stay untouched in front of it. Files it can't read safely, and
 * encrypted ones, come back as they are.
 */
export function withPdfTitle(bytes: Uint8Array, title: string): Uint8Array {
	const tailStart = Math.max(0, bytes.length - 2048);
	const tail = latin1.decode(bytes.subarray(tailStart));
	const pointer = /startxref\s+(\d+)\s+%%EOF\s*$/.exec(tail);
	if (pointer === null) return bytes;
	const previous = Number(pointer[1]);
	if (previous <= 0 || previous >= tailStart + pointer.index) return bytes;
	const trailer = trailerAt(bytes, previous, tailStart + pointer.index);
	if (trailer === undefined || /\/Encrypt\b/.test(trailer)) return bytes;
	const root = /\/Root\s+(\d+\s+\d+)\s+R\b/.exec(trailer)?.[1];
	const size = Number(/\/Size\s+(\d+)/.exec(trailer)?.[1]);
	if (root === undefined || !(size > 0)) return bytes;
	const id = /\/ID\s*\[[^\]]*\]/.exec(trailer)?.[0];

	const info = `\n${size} 0 obj\n<< /Title ${textString(title)} >>\nendobj\n`;
	const xref = bytes.length + info.length;
	const update =
		info +
		`xref\n${size} 1\n${String(bytes.length + 1).padStart(10, '0')} 00000 n\r\n` +
		`trailer\n<< /Size ${size + 1} /Root ${root} R /Info ${size} 0 R /Prev ${previous}` +
		`${id ? ` ${id}` : ''} >>\nstartxref\n${xref}\n%%EOF\n`;

	const titled = new Uint8Array(bytes.length + update.length);
	titled.set(bytes);
	for (let index = 0; index < update.length; index++) {
		titled[bytes.length + index] = update.charCodeAt(index);
	}
	return titled;
}

/** Opens a PDF in the browser's own viewer, in a new tab, titled with its name. */
export function openPdfInTab(bytes: Uint8Array, name: string): void {
	openInTab(withPdfTitle(bytes, name), 'application/pdf');
}
