/** One file of a zip archive. */
export interface ZipEntry {
	/** The full path inside the archive. */
	path: string;
	/** The last path segment. */
	name: string;
	/** The folder it sits in, empty at the root. */
	folder: string;
	size: number;
	compressedSize: number;
	/** 0 stored, 8 deflated; anything else can't be opened here. */
	method: number;
	/** Where its local header starts. */
	offset: number;
}

const END_SIGNATURE = 0x06054b50;
const CENTRAL_SIGNATURE = 0x02014b50;
const LOCAL_SIGNATURE = 0x04034b50;

/** The upper half of code page 437, which zip tools used for names before UTF-8. */
const CP437 =
	'ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜ¢£¥₧ƒáíóúñÑªº¿⌐¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αßΓπΣσµτΦΘΩδ∞φε∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■ ';

function cp437(bytes: Uint8Array): string {
	return Array.from(bytes, (byte) =>
		byte < 128 ? String.fromCharCode(byte) : CP437[byte - 128]
	).join('');
}

/**
 * The files of a zip archive (folders left out), sorted by path, read from its central
 * directory. Throws when `data` isn't a zip.
 */
export function readZip(data: Uint8Array): ZipEntry[] {
	const view = new DataView(data.buffer, data.byteOffset, data.byteLength);
	let end = -1;
	for (let at = data.length - 22; at >= Math.max(0, data.length - 22 - 0xffff); at--) {
		if (view.getUint32(at, true) === END_SIGNATURE) {
			end = at;
			break;
		}
	}
	if (end < 0) throw new Error('Not a zip archive');
	const count = view.getUint16(end + 10, true);
	let at = view.getUint32(end + 16, true);
	const utf8 = new TextDecoder('utf-8');
	const entries: ZipEntry[] = [];
	for (let index = 0; index < count; index++) {
		if (at + 46 > data.length || view.getUint32(at, true) !== CENTRAL_SIGNATURE) {
			throw new Error('Corrupt zip directory');
		}
		const flags = view.getUint16(at + 8, true);
		const method = view.getUint16(at + 10, true);
		const compressedSize = view.getUint32(at + 20, true);
		const size = view.getUint32(at + 24, true);
		const nameLength = view.getUint16(at + 28, true);
		const extraLength = view.getUint16(at + 30, true);
		const commentLength = view.getUint16(at + 32, true);
		const offset = view.getUint32(at + 42, true);
		const raw = data.subarray(at + 46, at + 46 + nameLength);
		// Bit 11 marks UTF-8 names; older tools wrote the DOS code page.
		const path = flags & 0x0800 ? utf8.decode(raw) : cp437(raw);
		at += 46 + nameLength + extraLength + commentLength;
		if (path.endsWith('/') || path.startsWith('__MACOSX/')) continue;
		const slash = path.lastIndexOf('/');
		entries.push({
			path,
			name: path.slice(slash + 1),
			folder: slash < 0 ? '' : path.slice(0, slash),
			size,
			compressedSize,
			method,
			offset
		});
	}
	return entries.sort((a, b) => a.path.toLowerCase().localeCompare(b.path.toLowerCase()));
}

/** An entry's bytes, inflated with the browser's own decompressor. */
export async function extractZipEntry(
	data: Uint8Array,
	entry: ZipEntry
): Promise<Uint8Array<ArrayBuffer>> {
	const view = new DataView(data.buffer, data.byteOffset, data.byteLength);
	if (view.getUint32(entry.offset, true) !== LOCAL_SIGNATURE) throw new Error('Corrupt zip entry');
	const start =
		entry.offset +
		30 +
		view.getUint16(entry.offset + 26, true) +
		view.getUint16(entry.offset + 28, true);
	const compressed = data.slice(start, start + entry.compressedSize);
	if (entry.method === 0) return compressed;
	if (entry.method !== 8) throw new Error(`Unsupported zip method ${entry.method}`);
	const stream = new Blob([compressed])
		.stream()
		.pipeThrough(new DecompressionStream('deflate-raw'));
	return new Uint8Array(await new Response(stream).arrayBuffer());
}
