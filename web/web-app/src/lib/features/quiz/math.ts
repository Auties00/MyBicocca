/*
 * Moodle's MathJax filter leaves TeX in question text, between \( \), \[ \] or $$ $$, for the
 * browser to typeset. Without a typesetter here, the common notation of quizzes (indices,
 * powers, fractions, roots, Greek letters, relations) becomes HTML the sanitizer keeps: <sub>,
 * <sup>, <i> and Unicode symbols. Anything else keeps its TeX name, still readable.
 */

const DELIMITED = /\\\(([\s\S]+?)\\\)|\\\[([\s\S]+?)\\\]|\$\$([\s\S]+?)\$\$/g;

const SYMBOLS: Readonly<Record<string, string>> = {
	ldots: '…',
	dots: '…',
	cdots: '⋯',
	dotsc: '…',
	vdots: '⋮',
	cdot: '·',
	times: '×',
	div: '÷',
	le: '≤',
	leq: '≤',
	ge: '≥',
	geq: '≥',
	ne: '≠',
	neq: '≠',
	approx: '≈',
	sim: '∼',
	simeq: '≃',
	equiv: '≡',
	pm: '±',
	mp: '∓',
	infty: '∞',
	in: '∈',
	notin: '∉',
	ni: '∋',
	subset: '⊂',
	subseteq: '⊆',
	supset: '⊃',
	supseteq: '⊇',
	cup: '∪',
	cap: '∩',
	setminus: '∖',
	emptyset: '∅',
	varnothing: '∅',
	to: '→',
	rightarrow: '→',
	leftarrow: '←',
	Rightarrow: '⇒',
	Leftarrow: '⇐',
	iff: '⇔',
	Leftrightarrow: '⇔',
	mapsto: '↦',
	forall: '∀',
	exists: '∃',
	neg: '¬',
	lnot: '¬',
	land: '∧',
	wedge: '∧',
	lor: '∨',
	vee: '∨',
	sum: '∑',
	prod: '∏',
	int: '∫',
	partial: '∂',
	nabla: '∇',
	prime: '′',
	circ: '∘',
	mid: '∣',
	vert: '|',
	lvert: '|',
	rvert: '|',
	langle: '⟨',
	rangle: '⟩',
	lfloor: '⌊',
	rfloor: '⌋',
	lceil: '⌈',
	rceil: '⌉',
	perp: '⊥',
	parallel: '∥',
	angle: '∠',
	ast: '∗',
	alpha: 'α',
	beta: 'β',
	gamma: 'γ',
	delta: 'δ',
	epsilon: 'ϵ',
	varepsilon: 'ε',
	zeta: 'ζ',
	eta: 'η',
	theta: 'θ',
	vartheta: 'ϑ',
	iota: 'ι',
	kappa: 'κ',
	lambda: 'λ',
	mu: 'μ',
	nu: 'ν',
	xi: 'ξ',
	pi: 'π',
	rho: 'ρ',
	sigma: 'σ',
	tau: 'τ',
	upsilon: 'υ',
	phi: 'ϕ',
	varphi: 'φ',
	chi: 'χ',
	psi: 'ψ',
	omega: 'ω',
	Gamma: 'Γ',
	Delta: 'Δ',
	Theta: 'Θ',
	Lambda: 'Λ',
	Xi: 'Ξ',
	Pi: 'Π',
	Sigma: 'Σ',
	Phi: 'Φ',
	Psi: 'Ψ',
	Omega: 'Ω',
	',': ' ',
	':': ' ',
	';': ' ',
	' ': ' ',
	'!': '',
	quad: ' ',
	qquad: '  ',
	'\\': ' ',
	'{': '{',
	'}': '}',
	'%': '%',
	_: '_',
	'#': '#',
	$: '$'
};

/* Sizing and layout commands that don't change what the formula says. */
const IGNORED = new Set([
	'left',
	'right',
	'big',
	'Big',
	'bigg',
	'Bigg',
	'bigl',
	'bigr',
	'Bigl',
	'Bigr',
	'displaystyle',
	'textstyle',
	'limits',
	'nolimits'
]);

const TEXT = new Set(['text', 'textrm', 'mathrm', 'mbox', 'operatorname', 'textit', 'mathit']);
const BOLD = new Set(['mathbf', 'textbf', 'boldsymbol']);
const ACCENTS: Readonly<Record<string, string>> = {
	overline: '̅',
	bar: '̄',
	hat: '̂',
	widehat: '̂',
	tilde: '̃',
	widetilde: '̃',
	vec: '⃗',
	dot: '̇',
	ddot: '̈'
};
const DOUBLE_STRUCK: Readonly<Record<string, string>> = {
	N: 'ℕ',
	Z: 'ℤ',
	Q: 'ℚ',
	R: 'ℝ',
	C: 'ℂ',
	P: 'ℙ',
	E: '𝔼'
};

const escape = (text: string) =>
	text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

const textOf = (html: string) => html.replace(/<[^>]*>/g, '');

/** The content of the group opening at `start` (a `{`), and the index after its `}`. */
function group(tex: string, start: number): [string, number] {
	let depth = 0;
	for (let index = start; index < tex.length; index++) {
		if (tex[index] === '\\') {
			index++;
			continue;
		}
		if (tex[index] === '{') depth++;
		if (tex[index] === '}' && --depth === 0) return [tex.slice(start + 1, index), index + 1];
	}
	return [tex.slice(start + 1), tex.length];
}

/** One argument: a group, a command or a single character, raw. */
function rawArgument(tex: string, start: number): [string, number] {
	let index = start;
	while (tex[index] === ' ') index++;
	if (tex[index] === '{') return group(tex, index);
	if (tex[index] === '\\') {
		const name = /^\\([a-zA-Z]+|.)/.exec(tex.slice(index))?.[0] ?? '\\';
		return [name, index + name.length];
	}
	return [tex[index] ?? '', index + 1];
}

function argument(tex: string, start: number): [string, number] {
	const [raw, end] = rawArgument(tex, start);
	return [convert(raw), end];
}

/** A fraction's or root's operand, in brackets when it is more than one symbol. */
const operand = (html: string) => (textOf(html).trim().length > 1 ? `(${html})` : html);

function convert(tex: string): string {
	let out = '';
	let index = 0;
	while (index < tex.length) {
		const char = tex[index];
		if (char === '\\') {
			const name = /^\\([a-zA-Z]+|.)/.exec(tex.slice(index))?.[1] ?? '';
			index += name.length + 1;
			if (name in SYMBOLS) {
				out += SYMBOLS[name];
			} else if (IGNORED.has(name)) {
				if (tex[index] === '.') index++;
			} else if (name === 'frac' || name === 'dfrac' || name === 'tfrac') {
				const [top, next] = argument(tex, index);
				const [bottom, end] = argument(tex, next);
				out += `${operand(top)}/${operand(bottom)}`;
				index = end;
			} else if (name === 'sqrt') {
				let degree = '';
				if (tex[index] === '[') {
					const close = tex.indexOf(']', index);
					degree = close > index ? `<sup>${convert(tex.slice(index + 1, close))}</sup>` : '';
					index = close > index ? close + 1 : index;
				}
				const [radicand, end] = argument(tex, index);
				out += `${degree}√${operand(radicand)}`;
				index = end;
			} else if (name in ACCENTS) {
				const [raw, end] = rawArgument(tex, index);
				const base = convert(raw);
				out += textOf(base).length === 1 ? `${base}${ACCENTS[name]}` : base;
				index = end;
			} else if (name === 'mathbb') {
				const [raw, end] = rawArgument(tex, index);
				out += [...raw].map((letter) => DOUBLE_STRUCK[letter] ?? letter).join('');
				index = end;
			} else if (TEXT.has(name)) {
				const [raw, end] = rawArgument(tex, index);
				out += escape(raw);
				index = end;
			} else if (BOLD.has(name)) {
				const [inner, end] = argument(tex, index);
				out += `<b>${inner}</b>`;
				index = end;
			} else {
				out += name;
			}
		} else if (char === '_' || char === '^') {
			const [inner, end] = argument(tex, index + 1);
			out += char === '_' ? `<sub>${inner}</sub>` : `<sup>${inner}</sup>`;
			index = end;
		} else if (char === '{') {
			const [inner, end] = group(tex, index);
			out += convert(inner);
			index = end;
		} else if (char === '}') {
			index++;
		} else if (char === '&') {
			const entity = /^&(?:[a-z]+|#\d+|#x[\da-f]+);/i.exec(tex.slice(index))?.[0];
			out += entity ?? '&amp;';
			index += entity?.length ?? 1;
		} else if (/[a-zA-Z]/.test(char)) {
			out += `<i>${char}</i>`;
			index++;
		} else {
			out += char === '-' ? '−' : char === '<' ? '&lt;' : char === '>' ? '&gt;' : char;
			index++;
		}
	}
	return out;
}

/** TeX (as HTML escapes it: `&lt;` for `<`) as the HTML that shows it. */
export function texToHtml(tex: string): string {
	return convert(tex.trim());
}

/* Their text is a value or plain text only: it stays as TeX. */
const RAW = new Set(['textarea', 'select', 'option', 'script', 'style']);

/** Every delimited formula of `html` typeset as HTML. */
export function renderTex(html: string): string {
	return html.replace(DELIMITED, (_, inline?: string, display?: string, dollars?: string) =>
		texToHtml(inline ?? display ?? dollars ?? '')
	);
}

/**
 * Typesets the formulas in the text of `root`, leaving attributes and form fields alone: a
 * student's answer is sent back exactly as written.
 */
export function renderMath(root: Element): void {
	for (const node of Array.from(root.childNodes)) {
		if (node.nodeType === 1) {
			const tag = (node as Element).tagName.toLowerCase();
			if (!RAW.has(tag)) renderMath(node as Element);
			continue;
		}
		const text = node.nodeType === 3 ? (node.textContent ?? '') : '';
		DELIMITED.lastIndex = 0;
		if (!DELIMITED.test(text)) continue;
		const holder = root.ownerDocument.createElement('span');
		holder.innerHTML = renderTex(escape(text));
		node.replaceWith(...Array.from(holder.childNodes));
	}
}
