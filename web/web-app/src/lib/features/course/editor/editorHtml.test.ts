import { describe, expect, it } from 'vitest';
import { editorHtml, withPlaceholders, type FileArea } from './editorHtml.ts';

const post = (itemId: number): FileArea => ({ component: 'mod_forum', area: 'post', itemId });

describe('editorHtml', () => {
	it('keeps formatting, links and lists, in the elements Moodle expects', () => {
		expect(
			editorHtml(
				'<p><b>Ciao</b> <i>a tutti</i>, ecco il <a href="https://example.org" target="_blank" rel="noopener">link</a></p><ul><li>uno</li></ul>'
			)
		).toBe(
			'<p><strong>Ciao</strong> <em>a tutti</em>, ecco il <a href="https://example.org">link</a></p><ul><li>uno</li></ul>'
		);
	});

	it('drops scripts, styles, classes and unsafe links', () => {
		expect(
			editorHtml(
				'<p class="x" style="color:red" onclick="alert(1)">Testo<script>alert(1)</script> <a href="javascript:alert(1)">qui</a></p><style>p{}</style>'
			)
		).toBe('<p>Testo qui</p>');
	});

	it('gives text typed outside a block its own paragraph', () => {
		expect(editorHtml('Prima riga<br>seconda<p>Terza</p>')).toBe(
			'<p>Prima riga<br>seconda</p><p>Terza</p>'
		);
	});

	it('turns the editor’s divs into paragraphs and unwraps wrappers of blocks', () => {
		expect(editorHtml('<div>Uno</div><div><p>Due</p></div>')).toBe('<p>Uno</p><p>Due</p>');
	});

	it('trims empty paragraphs and trailing line breaks', () => {
		expect(editorHtml('<p><br></p><p>Ciao<br></p><p> </p>')).toBe('<p>Ciao</p>');
	});

	it('is empty when nothing is written', () => {
		expect(editorHtml('<p><br></p>')).toBe('');
		expect(editorHtml('   ')).toBe('');
	});

	it('gives an edited post’s own images back their placeholder', () => {
		const shown =
			'https://elearning.unimib.it/webservice/pluginfile.php/901/mod_forum/post/42/schema.png';
		expect(
			editorHtml(`<p><img data-moodle-src="${shown}" src="blob:x" alt="Schema"></p>`, {
				files: post(42)
			})
		).toBe('<p><img src="@@PLUGINFILE@@/schema.png" alt="Schema"></p>');
		expect(editorHtml(`<p><img src="${shown}"></p>`, { files: post(7) })).toBe(
			`<p><img src="${shown}"></p>`
		);
	});
});

describe('withPlaceholders', () => {
	const onlineText: FileArea = {
		component: 'assignsubmission_onlinetext',
		area: 'submissions_onlinetext'
	};
	const area =
		'https://elearning.unimib.it/webservice/pluginfile.php/7/assignsubmission_onlinetext/submissions_onlinetext/51';

	it('puts the area’s own files back behind Moodle’s placeholder', () => {
		expect(
			withPlaceholders(
				`<p><img src="${area}/cartella/fig%201.png?token=abc&amp;forcedownload=1"> <a href="${area}/dati.csv">dati</a></p>`,
				onlineText
			)
		).toBe(
			'<p><img src="@@PLUGINFILE@@/cartella/fig%201.png"> <a href="@@PLUGINFILE@@/dati.csv">dati</a></p>'
		);
	});

	it('leaves other areas’ files and pages alone', () => {
		const html =
			'<p><img src="https://elearning.unimib.it/pluginfile.php/7/mod_assign/intro/schema.png"> <a href="https://x.it/a">x</a></p>';
		expect(withPlaceholders(html, onlineText)).toBe(html);
		const other = `<img src="${area}/a.png">`;
		expect(withPlaceholders(other, { ...onlineText, itemId: 52 })).toBe(other);
	});
});
