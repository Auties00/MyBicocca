import { describe, expect, it } from 'vitest';
import type { CourseFile } from '#lib/features/course/content.ts';
import { addPicked, problemOf } from './draft.ts';
import type { DraftFile } from './writes.ts';

const kept = (name: string, size = 10): DraftFile => ({
	key: `kept:${name}`,
	kind: 'kept',
	file: { url: `u/${name}`, name, folder: '/', mime: null, size, modified: null } as CourseFile
});
const pick = (name: string, size = 10, type = '') =>
	new File([new Uint8Array(size)], name, { type });
const names = (files: readonly DraftFile[]) => files.map((file) => file.file.name);

describe('addPicked', () => {
	let n = 0;
	const key = (file: File) => `${file.name}:${++n}`;
	const rules = (maxFiles: number) => ({ maxFiles, maxBytes: 100, types: ['.pdf', '.zip'] });

	it('adds picks while the count allows and names the rest', () => {
		const result = addPicked(
			[kept('a.pdf')],
			[pick('b.pdf'), pick('c.pdf'), pick('d.pdf')],
			rules(3),
			key
		);
		expect(names(result.files)).toEqual(['a.pdf', 'b.pdf', 'c.pdf']);
		expect(result.dropped).toEqual(['d.pdf']);
	});

	it('refuses picks the rules reject without spending a place on them', () => {
		const result = addPicked(
			[],
			[pick('script.py'), pick('relazione.pdf'), pick('codice.zip'), pick('enorme.pdf', 101)],
			rules(2),
			key
		);
		expect(names(result.files)).toEqual(['relazione.pdf', 'codice.zip']);
		expect(result.rejected).toEqual([
			{ name: 'script.py', problem: { kind: 'type' } },
			{ name: 'enorme.pdf', problem: { kind: 'too-large', limit: 100 } }
		]);
		expect(result.dropped).toEqual([]);
	});

	it('replaces a listed file of the same name in place, even at the limit', () => {
		const result = addPicked([kept('a.pdf'), kept('b.pdf')], [pick('a.pdf', 99)], rules(2), key);
		expect(result.files.map((file) => [file.kind, file.file.name])).toEqual([
			['picked', 'a.pdf'],
			['kept', 'b.pdf']
		]);
		expect(result.replaced).toEqual(['a.pdf']);
		expect(result.dropped).toEqual([]);
	});

	it('swaps the only file where one is allowed, once per pick', () => {
		const result = addPicked(
			[kept('vecchia.pdf')],
			[pick('nuova.pdf'), pick('altra.pdf')],
			rules(1),
			key
		);
		expect(result.files.map((file) => [file.kind, file.file.name])).toEqual([
			['picked', 'nuova.pdf']
		]);
		expect(result.replaced).toEqual(['vecchia.pdf']);
		expect(result.dropped).toEqual(['altra.pdf']);
	});
});

describe('problemOf', () => {
	const rules = { maxFiles: 3, maxBytes: 100, types: ['.pdf'] };
	const picked = (file: File): DraftFile => ({ key: file.name, kind: 'picked', file });

	it('checks size, emptiness and type of picked files', () => {
		expect(problemOf(picked(pick('a.pdf', 101)), rules)).toEqual({ kind: 'too-large', limit: 100 });
		expect(problemOf(picked(pick('a.pdf', 0)), rules)).toEqual({ kind: 'empty' });
		expect(problemOf(picked(pick('a.zip', 50)), rules)).toEqual({ kind: 'type' });
		expect(problemOf(picked(pick('a.pdf', 50)), rules)).toBeNull();
	});

	it('trusts files Moodle already took', () => {
		expect(problemOf(kept('vecchio.zip', 500), rules)).toBeNull();
	});
});
