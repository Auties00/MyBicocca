import { PDF_FONTS, pdfFile, pdfStream, pdfText } from '../../pdf.ts';
import { DemoStudent } from '../../persona.ts';
import { bytes, route, text } from '../../router.ts';
import { DEMO_WS_TOKEN } from '../../tokens.ts';

/** A landscape deck of `pages` slides titled `title`, standing in for course material. */
export function demoSlides(title: string, pages = 6): Uint8Array<ArrayBuffer> {
	const pageIds = Array.from({ length: pages }, (_, index) => 5 + index * 2);
	const slides = pageIds.flatMap((id, index) => [
		`<< /Type /Page /Parent 2 0 R /MediaBox [0 0 842 595] ` +
			`/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents ${id + 1} 0 R >>`,
		pdfStream(
			[
				'0.61 0.05 0.21 rg 60 452 96 5 re f',
				`BT /F2 34 Tf 0.1 0.1 0.12 rg 60 400 Td (${pdfText(title)}) Tj ET`,
				`BT /F1 16 Tf 0.35 0.35 0.38 rg 60 360 Td (${pdfText(`Materiale dimostrativo, pagina ${index + 1} di ${pages}`)}) Tj ET`,
				`BT /F1 11 Tf 0.5 0.5 0.52 rg 60 60 Td (${pdfText(`Universita degli Studi di Milano-Bicocca - ${DemoStudent.fullName}`)}) Tj ET`
			].join('\n')
		)
	]);
	return pdfFile([
		'<< /Type /Catalog /Pages 2 0 R >>',
		`<< /Type /Pages /Kids [${pageIds.map((id) => `${id} 0 R`).join(' ')}] /Count ${pages} >>`,
		...PDF_FONTS,
		...slides
	]);
}

const CRC_TABLE = Array.from({ length: 256 }, (_, n) => {
	let c = n;
	for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
	return c >>> 0;
});

function crc32(data: Uint8Array): number {
	let crc = 0xffffffff;
	for (const byte of data) crc = CRC_TABLE[(crc ^ byte) & 0xff] ^ (crc >>> 8);
	return (crc ^ 0xffffffff) >>> 0;
}

/** An uncompressed zip of `files`. */
export function demoZip(
	files: ReadonlyArray<[name: string, content: string]>
): Uint8Array<ArrayBuffer> {
	const encoder = new TextEncoder();
	const locals: Uint8Array[] = [];
	const centrals: Uint8Array[] = [];
	let offset = 0;
	for (const [name, content] of files) {
		const nameBytes = encoder.encode(name);
		const data = encoder.encode(content);
		const crc = crc32(data);
		const local = new Uint8Array(30 + nameBytes.length + data.length);
		const lv = new DataView(local.buffer);
		lv.setUint32(0, 0x04034b50, true);
		lv.setUint16(4, 20, true);
		lv.setUint16(6, 0x0800, true);
		lv.setUint32(14, crc, true);
		lv.setUint32(18, data.length, true);
		lv.setUint32(22, data.length, true);
		lv.setUint16(26, nameBytes.length, true);
		local.set(nameBytes, 30);
		local.set(data, 30 + nameBytes.length);
		const central = new Uint8Array(46 + nameBytes.length);
		const cv = new DataView(central.buffer);
		cv.setUint32(0, 0x02014b50, true);
		cv.setUint16(4, 20, true);
		cv.setUint16(6, 20, true);
		cv.setUint16(8, 0x0800, true);
		cv.setUint32(16, crc, true);
		cv.setUint32(20, data.length, true);
		cv.setUint32(24, data.length, true);
		cv.setUint16(28, nameBytes.length, true);
		cv.setUint32(42, offset, true);
		central.set(nameBytes, 46);
		locals.push(local);
		centrals.push(central);
		offset += local.length;
	}
	const centralSize = centrals.reduce((sum, part) => sum + part.length, 0);
	const end = new Uint8Array(22);
	const ev = new DataView(end.buffer);
	ev.setUint32(0, 0x06054b50, true);
	ev.setUint16(8, files.length, true);
	ev.setUint16(10, files.length, true);
	ev.setUint32(12, centralSize, true);
	ev.setUint32(16, offset, true);
	const out = new Uint8Array(offset + centralSize + 22);
	let at = 0;
	for (const part of [...locals, ...centrals, end]) {
		out.set(part, at);
		at += part.length;
	}
	return out;
}

const PYTHON = `def ricerca_binaria(a, x):
    """Restituisce l'indice di x nella lista ordinata a, oppure -1."""
    lo, hi = 0, len(a) - 1
    while lo <= hi:
        mid = (lo + hi) // 2
        if a[mid] == x:
            return mid
        if a[mid] < x:
            lo = mid + 1
        else:
            hi = mid - 1
    return -1


if __name__ == "__main__":
    numeri = [2, 3, 5, 7, 11, 13, 17, 19]
    print(ricerca_binaria(numeri, 11))  # 4
`;

const SVG = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 400" width="640" height="400">
<rect width="640" height="400" fill="#faf7f7"/>
<g stroke="#9c0c35" stroke-width="3" fill="none">
<line x1="320" y1="70" x2="190" y2="170"/><line x1="320" y1="70" x2="450" y2="170"/>
<line x1="190" y1="170" x2="120" y2="280"/><line x1="190" y1="170" x2="260" y2="280"/>
<line x1="450" y1="170" x2="520" y2="280"/>
</g>
<g font-family="Georgia, serif" font-size="26" text-anchor="middle" fill="#1c1617">
<circle cx="320" cy="70" r="30" fill="#fff" stroke="#9c0c35" stroke-width="3"/><text x="320" y="79">8</text>
<circle cx="190" cy="170" r="30" fill="#fff" stroke="#9c0c35" stroke-width="3"/><text x="190" y="179">3</text>
<circle cx="450" cy="170" r="30" fill="#fff" stroke="#9c0c35" stroke-width="3"/><text x="450" y="179">10</text>
<circle cx="120" cy="280" r="30" fill="#fff" stroke="#9c0c35" stroke-width="3"/><text x="120" y="289">1</text>
<circle cx="260" cy="280" r="30" fill="#fff" stroke="#9c0c35" stroke-width="3"/><text x="260" y="289">6</text>
<circle cx="520" cy="280" r="30" fill="#fff" stroke="#9c0c35" stroke-width="3"/><text x="520" y="289">14</text>
</g>
<text x="320" y="370" font-family="Georgia, serif" font-size="18" font-style="italic" text-anchor="middle" fill="#6b5f61">Albero binario di ricerca</text>
</svg>
`;

const PAGE = `<!DOCTYPE html>
<html lang="it"><head><meta charset="utf-8"><title>Note sul progetto finale</title></head>
<body>
<h1>Note sul progetto finale</h1>
<p>Il progetto si svolge in gruppi di <strong>due o tre</strong> persone e si consegna entro l'ultimo appello della sessione estiva.</p>
<h2>Consegna</h2>
<ul>
<li>Codice sorgente commentato, in un archivio <code>.zip</code>.</li>
<li>Una relazione di al massimo dieci pagine.</li>
<li>Una breve presentazione da discutere all'orale.</li>
</ul>
<h2>Valutazione</h2>
<p>Il progetto vale fino a <em>4 punti</em> sul voto finale; chi non lo consegna sostiene l'orale completo.</p>
</body></html>
`;

const DOCX_TYPES = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/></Types>`;
const DOCX_RELS = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/></Relationships>`;
const DOCX_BODY = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:body><w:p><w:r><w:t>Traccia del laboratorio</w:t></w:r></w:p><w:p><w:r><w:t>Documento dimostrativo.</w:t></w:r></w:p></w:body></w:document>`;

/** A long dataset, as statistics courses publish: 12,000 readings of four sensors. */
function measurements(): string {
	const rows = ['n,sensore,temperatura,umidita'];
	let seed = 7;
	for (let index = 1; index <= 12_000; index++) {
		seed = (seed * 48_271) % 2_147_483_647;
		const temperature = (18 + (seed % 900) / 100).toFixed(2);
		const humidity = 40 + (seed % 37);
		rows.push(`${index},S${(index % 4) + 1},${temperature},${humidity}`);
	}
	return rows.join('\n') + '\n';
}

function file(name: string): Response {
	const extension = /\.([a-z0-9]+)$/i.exec(name)?.[1]?.toLowerCase();
	const title = name
		.replace(/\.[a-z0-9]+$/i, '')
		.replace(/_/g, ' ')
		.replace(/^./, (char) => char.toUpperCase());
	switch (extension) {
		case 'pdf':
			return bytes(demoSlides(title), 'application/pdf');
		case 'py':
			return text(PYTHON, 'text/x-python');
		case 'csv':
			return text(measurements(), 'text/csv');
		case 'svg':
			return text(SVG, 'image/svg+xml');
		case 'html':
			return text(PAGE, 'text/html');
		case 'zip':
			return bytes(
				demoZip([
					['esempi/ordinamento.py', 'def insertion_sort(a):\n    pass\n'],
					['esempi/README.txt', 'Esempi del corso, da completare a casa.\n'],
					['esempi/dati/numeri.csv', 'n\n5\n3\n8\n1\n']
				]),
				'application/zip'
			);
		case 'docx':
			return bytes(
				demoZip([
					['[Content_Types].xml', DOCX_TYPES],
					['_rels/.rels', DOCX_RELS],
					['word/document.xml', DOCX_BODY]
				]),
				'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
			);
		default:
			return text(`${title}\n`);
	}
}

export const routes = [
	route('GET https://elearning.unimib.it/webservice/pluginfile.php/*', ({ url }) => {
		if (url.searchParams.get('token') !== DEMO_WS_TOKEN) {
			return new Response('{"error":"Token non valido","errorcode":"invalidtoken"}', {
				status: 403,
				headers: { 'content-type': 'application/json' }
			});
		}
		const name = decodeURIComponent(url.pathname.split('/').at(-1) ?? '');
		return file(name);
	})
];
