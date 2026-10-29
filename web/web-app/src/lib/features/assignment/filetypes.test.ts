import { describe, expect, it } from 'vitest';
import { acceptAttribute, acceptsFile } from './filetypes.ts';

describe('acceptsFile', () => {
	it('takes anything when no type is set', () => {
		expect(acceptsFile([], 'tesi.key', '')).toBe(true);
	});

	it('matches extensions case-insensitively, including double ones', () => {
		expect(acceptsFile(['.pdf'], 'Relazione.PDF', 'application/pdf')).toBe(true);
		expect(acceptsFile(['.gz', '.tar'], 'progetto.tar.gz', '')).toBe(true);
		expect(acceptsFile(['.zip'], 'progetto.rar', '')).toBe(false);
		expect(acceptsFile(['.pdf'], 'pdf', '')).toBe(false);
	});

	it('expands Moodle’s groups', () => {
		const types = ['archive', '.odt', '.pdf', 'image'];
		expect(acceptsFile(types, 'appunti.7z', '')).toBe(true);
		expect(acceptsFile(types, 'foto.HEIC', '')).toBe(true);
		expect(acceptsFile(types, 'note.docx', '')).toBe(false);
		expect(acceptsFile(['document'], 'note.docx', '')).toBe(true);
	});

	it('matches MIME types, with wildcards', () => {
		expect(acceptsFile(['image/*'], 'scan', 'image/png')).toBe(true);
		expect(acceptsFile(['application/pdf'], 'scan', 'image/png')).toBe(false);
	});

	it('leaves a type it doesn’t know to Moodle', () => {
		expect(acceptsFile(['.pdf', 'sourcecode'], 'main.c', '')).toBe(true);
		expect(acceptAttribute(['.pdf', 'sourcecode'])).toBeUndefined();
	});
});

describe('acceptAttribute', () => {
	it('lists the extensions a group stands for, once', () => {
		expect(acceptAttribute([])).toBeUndefined();
		expect(acceptAttribute(['.pdf', 'web_image', '.png'])).toBe(
			'.pdf,.gif,.jpe,.jpeg,.jpg,.png,.svg,.svgz,.webp'
		);
	});
});
