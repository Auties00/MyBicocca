/* GF(256) by the QR polynomial x⁸ + x⁴ + x³ + x² + 1, as exponent and logarithm tables. */
const EXP = new Uint8Array(512);
const LOG = new Uint8Array(256);
for (let i = 0, x = 1; i < 255; i++) {
	EXP[i] = x;
	LOG[x] = i;
	x <<= 1;
	if (x & 0x100) x ^= 0x11d;
}
for (let i = 255; i < 512; i++) EXP[i] = EXP[i - 255];

function multiply(a: number, b: number): number {
	return a === 0 || b === 0 ? 0 : EXP[LOG[a] + LOG[b]];
}

function divide(a: number, b: number): number {
	return a === 0 ? 0 : EXP[LOG[a] + 255 - LOG[b]];
}

/** Evaluates a polynomial given lowest degree first. */
function evaluate(coefficients: readonly number[], x: number): number {
	let value = 0;
	for (let i = coefficients.length - 1; i >= 0; i--) value = multiply(value, x) ^ coefficients[i];
	return value;
}

/**
 * The block's syndromes: the codeword, read as a polynomial with its first byte the highest
 * term, at α⁰ … α^(eccLength − 1), the roots of QR's generator. All zero when it is intact.
 */
function syndromes(codewords: Uint8Array, eccLength: number): number[] {
	const values: number[] = [];
	for (let j = 0; j < eccLength; j++) {
		let value = 0;
		for (const codeword of codewords) value = multiply(value, EXP[j]) ^ codeword;
		values.push(value);
	}
	return values;
}

/**
 * Corrects a block in place: up to half as many wrong codewords as it has error correction
 * ones. Berlekamp–Massey finds the error locator, a Chien search its roots, Forney the values.
 * False when the block is beyond repair.
 */
export function correctErrors(codewords: Uint8Array, eccLength: number): boolean {
	const s = syndromes(codewords, eccLength);
	if (s.every((value) => value === 0)) return true;

	let locator = [1];
	let previous = [1];
	let errors = 0;
	let shift = 1;
	let lastDiscrepancy = 1;
	for (let step = 0; step < eccLength; step++) {
		let discrepancy = s[step];
		for (let i = 1; i <= errors; i++) discrepancy ^= multiply(locator[i] ?? 0, s[step - i]);
		if (discrepancy === 0) {
			shift++;
			continue;
		}
		const factor = divide(discrepancy, lastDiscrepancy);
		const next = locator.slice();
		for (const [i, coefficient] of previous.entries()) {
			next[i + shift] = (next[i + shift] ?? 0) ^ multiply(factor, coefficient);
		}
		if (2 * errors <= step) {
			previous = locator;
			errors = step + 1 - errors;
			lastDiscrepancy = discrepancy;
			shift = 1;
		} else {
			shift++;
		}
		locator = next;
	}
	if (2 * errors > eccLength) return false;

	/* An error in the codeword of degree p has locator α^p, a root of Λ at α^−p. */
	const n = codewords.length;
	const found: { position: number; location: number }[] = [];
	for (let p = 0; p < n; p++) {
		if (evaluate(locator, EXP[255 - p]) === 0) {
			found.push({ position: n - 1 - p, location: EXP[p] });
		}
	}
	if (found.length !== errors) return false;

	const evaluator: number[] = [];
	for (let i = 0; i < eccLength; i++) {
		let value = 0;
		for (let k = 0; k <= i && k < locator.length; k++) value ^= multiply(locator[k], s[i - k]);
		evaluator.push(value);
	}
	/* Λ′: in characteristic 2 only the odd terms survive. */
	const derivative = locator.slice(1).map((coefficient, i) => (i % 2 === 0 ? coefficient : 0));
	for (const { position, location } of found) {
		const inverse = EXP[255 - LOG[location]];
		const slope = evaluate(derivative, inverse);
		if (slope === 0) return false;
		/* Forney, for a generator whose first root is α⁰: e = X · Ω(X⁻¹) / Λ′(X⁻¹). */
		codewords[position] ^= multiply(location, divide(evaluate(evaluator, inverse), slope));
	}
	return syndromes(codewords, eccLength).every((value) => value === 0);
}
