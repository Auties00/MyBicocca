import { describe, expect, it } from 'vitest';
import { classifyFile, pluginfilePath, viewableKind } from './files.ts';
import { fileRequestOf } from './viewer.ts';

describe('classifyFile', () => {
	it('trusts the MIME type before the extension', () => {
		expect(classifyFile('notes.txt', 'application/pdf')).toEqual({ type: 'pdf' });
		expect(classifyFile('slides', 'application/vnd.ms-powerpoint')).toEqual({
			type: 'office',
			app: 'powerpoint'
		});
		expect(classifyFile('page.html', 'text/html; charset=utf-8')).toEqual({ type: 'html' });
	});

	it('falls back to the extension, case aside', () => {
		expect(classifyFile('Tesi.DOCX', null)).toEqual({ type: 'office', app: 'word' });
		expect(classifyFile('main.KT', null)).toEqual({ type: 'text' });
		expect(classifyFile('archivio.jar', 'application/octet-stream')).toEqual({ type: 'zip' });
		expect(classifyFile('model.blend', null)).toEqual({ type: 'unknown' });
	});

	it('remembers a choice only for kinds the viewer can show', () => {
		expect(viewableKind({ type: 'office', app: 'excel' })).toBeNull();
		expect(viewableKind({ type: 'image' })).toBe('image');
	});
});

describe('pluginfilePath', () => {
	it('takes the path the web service serves, from either pluginfile address', () => {
		expect(
			pluginfilePath(
				'https://elearning.unimib.it/webservice/pluginfile.php/9/mod_resource/content/1/a%20b.pdf?forcedownload=1'
			)
		).toBe('9/mod_resource/content/1/a%20b.pdf');
		expect(
			pluginfilePath('https://elearning.unimib.it/pluginfile.php/9/mod_page/content/3/x.png')
		).toBe('9/mod_page/content/3/x.png');
	});

	it('refuses addresses of other sites, where the token must never go', () => {
		expect(pluginfilePath('https://evil.example/webservice/pluginfile.php/9/x.pdf')).toBeNull();
		expect(pluginfilePath('not a url')).toBeNull();
	});
});

describe('fileRequestOf', () => {
	it('reads a course file by activity and name, or a Moodle file by address', () => {
		expect(fileRequestOf(new URLSearchParams('cm=12&name=a.pdf&dir=/lab/'))).toEqual({
			by: 'module',
			cmId: 12,
			name: 'a.pdf',
			folder: '/lab/',
			entry: null
		});
		const url =
			'https://elearning.unimib.it/webservice/pluginfile.php/5/mod_forum/attachment/7/b.zip';
		expect(
			fileRequestOf(new URLSearchParams({ url, size: '2048', entry: 'src/main.c' }))
		).toMatchObject({
			by: 'address',
			file: { url, name: 'b.zip', size: 2048, mime: null },
			entry: 'src/main.c'
		});
	});

	it('names nothing for other addresses or a missing name', () => {
		expect(fileRequestOf(new URLSearchParams('cm=12'))).toBeNull();
		expect(fileRequestOf(new URLSearchParams({ url: 'https://example.org/x.pdf' }))).toBeNull();
	});
});
