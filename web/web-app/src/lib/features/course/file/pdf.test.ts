import { describe, expect, it } from 'vitest';
import { withPdfTitle } from './pdf.ts';

const text = (bytes: Uint8Array) => new TextDecoder('latin1').decode(bytes);
const bytesOf = (source: string) => Uint8Array.from(source, (char) => char.charCodeAt(0));

/** A one-page PDF with a classic cross-reference table and `trailer` extra keys. */
function classicPdf(trailer = ''): Uint8Array {
	const objects = [
		'<< /Type /Catalog /Pages 2 0 R >>',
		'<< /Type /Pages /Kids [3 0 R] /Count 1 >>',
		'<< /Type /Page /Parent 2 0 R /MediaBox [0 0 200 200] >>'
	];
	let file = '%PDF-1.4\n';
	const offsets = objects.map((body, index) => {
		const offset = file.length;
		file += `${index + 1} 0 obj\n${body}\nendobj\n`;
		return offset;
	});
	const xref = file.length;
	file += `xref\n0 ${objects.length + 1}\n0000000000 65535 f\r\n`;
	for (const offset of offsets) file += `${String(offset).padStart(10, '0')} 00000 n\r\n`;
	file += `trailer\n<< /Size ${objects.length + 1} /Root 1 0 R${trailer} >>\nstartxref\n${xref}\n%%EOF\n`;
	return bytesOf(file);
}

/** Reads the update back as a reader does: last `startxref`, its section, its trailer. */
function readUpdate(bytes: Uint8Array) {
	const file = text(bytes);
	const xref = Number(/startxref\s+(\d+)\s+%%EOF\s*$/.exec(file)?.[1]);
	const section = file.slice(xref);
	const [, first, offset] = /^xref\n(\d+) 1\n(\d{10}) 00000 n\r\n/.exec(section) ?? [];
	const trailer = /trailer\n(<<.*>>)/.exec(section)?.[1] ?? '';
	return { object: file.slice(Number(offset)), first: Number(first), trailer };
}

describe('withPdfTitle', () => {
	it('appends an update that points the trailer at a new title', () => {
		const original = classicPdf(' /ID [<AB> <CD>]');
		const titled = withPdfTitle(original, 'Lezione 3 – è.pdf');
		expect(titled.subarray(0, original.length)).toEqual(original);
		const { object, first, trailer } = readUpdate(titled);
		expect(first).toBe(4);
		expect(
			object.startsWith(
				'4 0 obj\n<< /Title <FEFF004C0065007A0069006F006E00650020003300202013002000E8002E007000640066> >>'
			)
		).toBe(true);
		expect(trailer).toBe(
			`<< /Size 5 /Root 1 0 R /Info 4 0 R /Prev ${text(original).indexOf('\nxref\n') + 1} /ID [<AB> <CD>] >>`
		);
	});

	it('reads the trailer of a cross-reference stream', () => {
		const head = '%PDF-1.5\n1 0 obj\n<< /Type /Catalog >>\nendobj\n';
		const stream = `7 0 obj\n<< /Type /XRef /Size 8 /Root 1 0 R /W [1 2 1] /Length 4 >>\nstream\n\u0001\u0000\u0009\u0000\nendstream\nendobj\n`;
		const original = bytesOf(`${head}${stream}startxref\n${head.length}\n%%EOF`);
		const { first, trailer } = readUpdate(withPdfTitle(original, 'a.pdf'));
		expect(first).toBe(8);
		expect(trailer).toBe(`<< /Size 9 /Root 1 0 R /Info 8 0 R /Prev ${head.length} >>`);
	});

	it('reads the first-page trailer of a linearized file, not the one at its end', () => {
		const head = '%PDF-1.4\n';
		const first = `xref\n6 2\n0000000016 00000 n\r\n0000000099 00000 n\r\ntrailer\n<< /Size 8 /Root 7 0 R /Prev 900 >>\nstartxref\n0\n%%EOF\n`;
		const rest =
			'7 0 obj\n<< /Type /Catalog >>\nendobj\nxref\n0 1\n0000000000 65535 f\r\ntrailer\n<< /Size 6 >>\n';
		const original = bytesOf(`${head}${first}${rest}startxref\n${head.length}\n%%EOF\n`);
		const { trailer } = readUpdate(withPdfTitle(original, 'a.pdf'));
		expect(trailer).toBe(`<< /Size 9 /Root 7 0 R /Info 8 0 R /Prev ${head.length} >>`);
	});

	it('leaves encrypted and unreadable files as they are', () => {
		const encrypted = classicPdf(' /Encrypt 9 0 R');
		expect(withPdfTitle(encrypted, 'a.pdf')).toBe(encrypted);
		const cut = classicPdf().subarray(0, 120);
		expect(withPdfTitle(cut, 'a.pdf')).toBe(cut);
		const astray = bytesOf('%PDF-1.4\nstartxref\n3\n%%EOF\n');
		expect(withPdfTitle(astray, 'a.pdf')).toBe(astray);
	});
});
