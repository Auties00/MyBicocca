import { describe, expect, it } from 'vitest';
import { renderMath, renderTex, texToHtml } from './math.ts';

describe('texToHtml', () => {
	it('sets variables in italics, with indices, powers and symbols', () => {
		expect(texToHtml(String.raw`x_1, \ldots, x_N`)).toBe(
			'<i>x</i><sub>1</sub>, …, <i>x</i><sub><i>N</i></sub>'
		);
		expect(texToHtml('e^{-x^2}')).toBe('<i>e</i><sup>−<i>x</i><sup>2</sup></sup>');
		expect(texToHtml(String.raw`z \le -1`)).toBe('<i>z</i> ≤ −1');
		expect(texToHtml(String.raw`\alpha \in \mathbb{R}`)).toBe('α ∈ ℝ');
	});

	it('writes fractions and roots on one line, bracketing longer operands', () => {
		expect(texToHtml(String.raw`\frac{1}{n}`)).toBe('1/<i>n</i>');
		expect(texToHtml(String.raw`\frac{a+b}{2}`)).toBe('(<i>a</i>+<i>b</i>)/2');
		expect(texToHtml(String.raw`\sqrt{2}`)).toBe('√2');
		expect(texToHtml(String.raw`\sqrt[3]{x}`)).toBe('<sup>3</sup>√<i>x</i>');
	});

	it('marks a mean with its bar and keeps text and unknown commands readable', () => {
		expect(texToHtml(String.raw`\overline{x}=0`)).toBe('<i>x</i>̅=0');
		expect(texToHtml(String.raw`\text{se } x &gt; 0`)).toBe('se  <i>x</i> &gt; 0');
		expect(texToHtml(String.raw`\left( \log n \right)`)).toBe('( log <i>n</i> )');
	});
});

describe('renderTex and renderMath', () => {
	it('typesets every delimited formula and leaves the rest', () => {
		expect(renderTex(String.raw`Se \(x_i &gt; 0\) allora $$y^2$$.`)).toBe(
			'Se <i>x</i><sub><i>i</i></sub> &gt; 0 allora <i>y</i><sup>2</sup>.'
		);
	});

	it('leaves answers alone: field values and text areas keep their TeX', () => {
		const document = new DOMParser().parseFromString(
			String.raw`<body><p>Calcola \(x^2\)</p><textarea>\(a\)</textarea><input value="\(b\)"></body>`,
			'text/html'
		);
		renderMath(document.body);
		expect(document.body.innerHTML).toBe(
			String.raw`<p>Calcola <i>x</i><sup>2</sup></p><textarea>\(a\)</textarea><input value="\(b\)">`
		);
	});
});
