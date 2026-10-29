import { isDigit, isLetterOrDigit, isWhitespace } from './fold.ts';

export interface SubsequenceMatch {
	/** The DP score over the best this query could reach, 0..1. */
	normalizedScore: number;
	/** Index in the text of every matched query character, ascending. */
	positions: number[];
}

const SCORE_MATCH = 16;
const SCORE_GAP_START = -3;
const SCORE_GAP_EXTENSION = -1;
const BONUS_BOUNDARY_WHITE = 10;
const BONUS_BOUNDARY_DELIMITER = 9;
const BONUS_BOUNDARY = 8;
const BONUS_CAMEL = 7;
const BONUS_CONSECUTIVE = 4;
const FIRST_CHAR_BONUS_MULTIPLIER = 2;
const NO_SCORE = -(2 ** 30);
const MIN_QUERY_LENGTH = 4;
const MIN_NORMALIZED_SCORE = 0.5;

/**
 * fzf-style in-order subsequence match with affine gaps, for abbreviations no other tier
 * catches: "anmat" finds "ANalisi MATematica". Both strings are folded already. The match must
 * start on a word and score at least half of a perfect one; queries under four characters never
 * match.
 */
export function subsequenceMatch(query: string, text: string): SubsequenceMatch | null {
	const n = query.length;
	const m = text.length;
	if (n < MIN_QUERY_LENGTH || m < n || !anchoredInOrder(query, text)) return null;

	const bonus = buffers.bonus(m);
	for (let j = 0; j < m; j++) bonus[j] = positionBonus(text, j);

	const { endsAt, best, from } = buffers.matrices(n * m);

	for (let i = 0; i < n; i++) {
		let rolling = NO_SCORE;
		let rollingGapped = false;
		const row = i * m;
		const previousRow = (i - 1) * m;
		for (let j = 0; j < m; j++) {
			if (query[i] === text[j]) {
				const boundary = bonus[j] * (i === 0 ? FIRST_CHAR_BONUS_MULTIPLIER : 1);
				let score = NO_SCORE;
				let source = 0;
				if (i === 0) {
					score = SCORE_MATCH + boundary;
					source = 1;
				} else if (j > 0) {
					const afterGap = best[previousRow + j - 1];
					if (afterGap !== NO_SCORE) {
						score = afterGap + SCORE_MATCH + boundary;
						source = 1;
					}
					const consecutive = endsAt[previousRow + j - 1];
					if (consecutive !== NO_SCORE) {
						const run = consecutive + SCORE_MATCH + Math.max(BONUS_CONSECUTIVE, bonus[j]);
						if (run > score) {
							score = run;
							source = 2;
						}
					}
				}
				if (score !== NO_SCORE) {
					endsAt[row + j] = score;
					from[row + j] = source;
				}
			}
			const gapped =
				rolling === NO_SCORE
					? NO_SCORE
					: rolling + (rollingGapped ? SCORE_GAP_EXTENSION : SCORE_GAP_START);
			const here = endsAt[row + j];
			if (here !== NO_SCORE && (gapped === NO_SCORE || here >= gapped)) {
				rolling = here;
				rollingGapped = false;
			} else {
				rolling = gapped;
				rollingGapped = true;
			}
			best[row + j] = rolling;
		}
	}

	const lastRow = (n - 1) * m;
	let endColumn = -1;
	let endScore = NO_SCORE;
	for (let j = 0; j < m; j++) {
		if (endsAt[lastRow + j] > endScore) {
			endScore = endsAt[lastRow + j];
			endColumn = j;
		}
	}
	if (endColumn < 0) return null;

	const positions = new Array<number>(n);
	let i = n - 1;
	let j = endColumn;
	while (i >= 0) {
		positions[i] = j;
		const source = from[i * m + j];
		i--;
		if (i < 0) break;
		j--;
		if (source === 1) {
			while (j > 0 && (endsAt[i * m + j] === NO_SCORE || endsAt[i * m + j] !== best[i * m + j]))
				j--;
		}
	}

	if (bonus[positions[0]] < BONUS_BOUNDARY) return null;
	const perfect =
		n * (SCORE_MATCH + BONUS_CONSECUTIVE) +
		BONUS_BOUNDARY_WHITE * FIRST_CHAR_BONUS_MULTIPLIER -
		BONUS_CONSECUTIVE;
	const normalizedScore = Math.min(1, Math.max(0, endScore / perfect));
	if (normalizedScore < MIN_NORMALIZED_SCORE) return null;
	return { normalizedScore, positions };
}

/**
 * Whether the query's first character starts a word and the rest follows in order: what any
 * accepted match needs. The earliest such start leaves the most room, so it is the one tried.
 */
function anchoredInOrder(query: string, text: string): boolean {
	let start = text.indexOf(query[0]);
	while (start >= 0 && positionBonus(text, start) < BONUS_BOUNDARY) {
		start = text.indexOf(query[0], start + 1);
	}
	if (start < 0) return false;
	let at = start;
	for (let qi = 1; qi < query.length; qi++) {
		at = text.indexOf(query[qi], at + 1);
		if (at < 0) return false;
	}
	return true;
}

/** The DP's matrices, reused across calls: a search runs it on thousands of names. */
const buffers = {
	cells: 0,
	endsAt: new Int32Array(0),
	best: new Int32Array(0),
	from: new Uint8Array(0),
	positions: new Int32Array(0),
	matrices(size: number) {
		if (size > this.cells) {
			this.cells = Math.max(size, this.cells * 2);
			this.endsAt = new Int32Array(this.cells);
			this.best = new Int32Array(this.cells);
			this.from = new Uint8Array(this.cells);
		}
		this.endsAt.fill(NO_SCORE, 0, size);
		this.best.fill(NO_SCORE, 0, size);
		this.from.fill(0, 0, size);
		return { endsAt: this.endsAt, best: this.best, from: this.from };
	},
	bonus(length: number) {
		if (length > this.positions.length) this.positions = new Int32Array(Math.max(length, 256));
		return this.positions;
	}
};

/** Highest at the text's start or after whitespace, then after delimiters, then camel humps. */
function positionBonus(text: string, index: number): number {
	const char = text[index];
	if (!isLetterOrDigit(char)) return 0;
	if (index === 0) return BONUS_BOUNDARY_WHITE;
	const previous = text[index - 1];
	if (isWhitespace(previous)) return BONUS_BOUNDARY_WHITE;
	if ('/-_.:,'.includes(previous)) return BONUS_BOUNDARY_DELIMITER;
	if (!isLetterOrDigit(previous)) return BONUS_BOUNDARY;
	if (isDigit(char) !== isDigit(previous)) return BONUS_CAMEL;
	return 0;
}
