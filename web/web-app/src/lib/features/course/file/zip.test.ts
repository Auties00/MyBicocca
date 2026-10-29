import { describe, expect, it } from 'vitest';
import { demoZip } from '#lib/demo/fixtures/moodle/files.course.ts';
import { extractZipEntry, readZip } from './zip.ts';

const decode = (bytes: Uint8Array) => new TextDecoder().decode(bytes);

describe('readZip', () => {
	const archive = demoZip([
		['src/b.py', 'print(1)\n'],
		['A.txt', 'ciao'],
		['__MACOSX/src/._b.py', 'junk'],
		['src/', '']
	]);

	it('lists the files, folders and macOS leftovers out, by path', () => {
		expect(readZip(archive).map(({ path, name, folder }) => [path, name, folder])).toEqual([
			['A.txt', 'A.txt', ''],
			['src/b.py', 'b.py', 'src']
		]);
	});

	it('extracts a stored entry', async () => {
		const entry = readZip(archive).find((item) => item.path === 'src/b.py');
		expect(entry).toBeDefined();
		if (entry) expect(decode(await extractZipEntry(archive, entry))).toBe('print(1)\n');
	});

	it('refuses what isn’t a zip', () => {
		expect(() => readZip(new TextEncoder().encode('%PDF-1.4'))).toThrow();
	});
});
