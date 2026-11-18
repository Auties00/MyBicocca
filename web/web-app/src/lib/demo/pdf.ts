/*
 * Small PDFs for the demo's documents (slips, certificates, receipts, slides): plain enough for any
 * browser's viewer, written by hand so the demo needs no PDF library.
 */

/** Latin-1 bytes, as the PDF's WinAnsi fonts read text; other characters become "?". */
function latin1(value: string): Uint8Array<ArrayBuffer> {
	const out = new Uint8Array(value.length);
	for (let index = 0; index < value.length; index++) {
		const code = value.charCodeAt(index);
		out[index] = code < 256 ? code : 63;
	}
	return out;
}

/** `value` as a PDF string's content, its delimiters escaped. */
export function pdfText(value: string): string {
	return value.replace(/[\\()]/g, (char) => `\\${char}`);
}

/** A content stream object holding `content`. */
export function pdfStream(content: string): string {
	return `<< /Length ${content.length} >>\nstream\n${content}\nendstream`;
}

/** A PDF file of `objects`, numbered from 1 in order; the first must be the catalog. */
export function pdfFile(objects: readonly string[]): Uint8Array<ArrayBuffer> {
	let file = '%PDF-1.4\n';
	const offsets: number[] = [];
	objects.forEach((object, index) => {
		offsets.push(file.length);
		file += `${index + 1} 0 obj\n${object}\nendobj\n`;
	});
	const xref = file.length;
	file += `xref\n0 ${objects.length + 1}\n0000000000 65535 f \n`;
	file += offsets.map((offset) => `${String(offset).padStart(10, '0')} 00000 n \n`).join('');
	file += `trailer\n<< /Size ${objects.length + 1} /Root 1 0 R >>\nstartxref\n${xref}\n%%EOF\n`;
	return latin1(file);
}

/** Helvetica and Helvetica-Bold, as font objects. */
export const PDF_FONTS = [
	'<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>',
	'<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>'
] as const;

export interface LetterOptions {
	/** The bar above the title, as PDF RGB fractions: the university's red unless given. */
	accent?: string;
	/** The line under the title. */
	issuer?: string;
	/** A small closing line, such as a note that the copy has no legal value. */
	footer?: string;
}

/** A one-page A4 letter: an accent bar, a title, the issuer, then lines of Latin-1 text. */
export function demoPdf(
	title: string,
	lines: readonly string[],
	{
		accent = '0.61 0.05 0.21',
		issuer = 'Universita degli Studi di Milano-Bicocca',
		footer
	}: LetterOptions = {}
): Uint8Array<ArrayBuffer> {
	const content = [
		`${accent} rg 56 770 483 3 re f 0 g`,
		'BT /F2 20 Tf 56 736 Td',
		`(${pdfText(title)}) Tj`,
		`/F1 10 Tf 0 -20 Td (${pdfText(issuer)}) Tj`,
		'/F1 11 Tf 0 -30 Td',
		...lines.flatMap((line) => [`(${pdfText(line)}) Tj`, '0 -17 Td']),
		...(footer ? [`0 -24 Td /F1 9 Tf (${pdfText(footer)}) Tj`] : []),
		'ET'
	].join('\n');
	return pdfFile([
		'<< /Type /Catalog /Pages 2 0 R >>',
		'<< /Type /Pages /Kids [3 0 R] /Count 1 >>',
		'<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] ' +
			'/Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>',
		...PDF_FONTS,
		pdfStream(content)
	]);
}
