/**
 * The form a course name takes to join Esse3, EasyStaff and the transcript, which spell the same
 * course with different case and spacing.
 */
export function normalizeSubjectName(name: string): string {
	return name.trim().replace(/\s+/g, ' ').toUpperCase();
}

const BRACKETS = /\([^)]*\)|\[[^\]]*\]/g;
/** The apostrophe split turns "l'Ingegneria" into an article the stop words drop. */
const SEPARATORS = /[\s/'’–—-]+/;
const EDGE_PUNCTUATION = /^[.,;:·"«»]+|[.,;:·"«»]+$/g;
const ROMAN: Readonly<Record<string, string>> = {
	I: '1',
	II: '2',
	III: '3',
	IV: '4',
	V: '5',
	VI: '6',
	VII: '7',
	VIII: '8',
	IX: '9',
	X: '10'
};
const STOP_WORDS = new Set(
	(
		'il lo la i gli le l del dello della dei degli delle al allo alla ai agli alle dal dallo ' +
		'dalla dai dagli dalle nel nello nella nei negli nelle sul sullo sulla sui sugli sulle col ' +
		'coi di a ad da in con su per tra fra e ed o od un uno una che non of the and for on to an ' +
		'or by with'
	).split(' ')
);

/** A trailing number, Roman numeral (as digits) or short code such as "B2"; null for a word. */
function suffixOf(token: string): string | null {
	if (/^\d+$/.test(token)) return token;
	if (ROMAN[token]) return ROMAN[token];
	if (token.length >= 2 && token.length <= 4 && /\d/.test(token) && /\p{L}/u.test(token)) {
		return token.toUpperCase();
	}
	return null;
}

function titlePrefix(word: string): string {
	const prefix = word.slice(0, 4).toLowerCase();
	return prefix.charAt(0).toUpperCase() + prefix.slice(1);
}

/**
 * A 3–5 letter label for narrow cells, ported from Android: "Algoritmi e Strutture Dati" → "ASD",
 * "Analisi Matematica I" → "AM1", "Programmazione 1" → "Prog1", "Lingua Inglese B2" → "LIB2".
 * Null when the name yields nothing.
 */
export function shortLabelFor(name: string): string | null {
	const cleaned = name.replace(BRACKETS, ' ').trim();
	const tokens = cleaned
		.split(SEPARATORS)
		.map((token) => token.trim().replace(EDGE_PUNCTUATION, ''))
		.filter((token) => token !== '');
	if (tokens.length === 0) return null;

	const suffix = suffixOf(tokens[tokens.length - 1]);
	const body = suffix === null ? tokens : tokens.slice(0, -1);
	const content = body.filter((token) => !STOP_WORDS.has(token.toLowerCase()));
	if (content.length === 0) return suffix || titlePrefix(body[0] ?? tokens[tokens.length - 1]);
	if (content.length === 1) return titlePrefix(content[0]) + (suffix ?? '');
	return (
		content
			.slice(0, 5)
			.map((word) => word.charAt(0).toUpperCase())
			.join('') + (suffix ?? '')
	);
}

const VARIANT =
	/\s*(\(([^)]*)\)|\[([^\]]*)\]|\s-\s(LAB|LEZ|ESE|ESER|LABORATORIO|ESERCITAZIONI|TEORIA))\s*$/i;

/**
 * A course name without the part EasyStaff appends to tell its turni and groups apart:
 * "ANALISI E PROGETTO DI ALGORITMI (T1 LEZ + ESE + LAB)" and "INGEGNERIA DEL SOFTWARE - LAB" are
 * "ANALISI E PROGETTO DI ALGORITMI" and "INGEGNERIA DEL SOFTWARE", as the study plan names them.
 */
export function subjectBase(name: string): string {
	let base = name.trim();
	for (let match = VARIANT.exec(base); match; match = VARIANT.exec(base)) {
		base = base.slice(0, match.index).trim();
	}
	return base || name.trim();
}

/** The appended part itself: "T1 LEZ + ESE + LAB", "LAB", or null. */
function variantText(name: string): string | null {
	const match = VARIANT.exec(name.trim());
	if (!match) return null;
	return (match[2] ?? match[3] ?? match[4] ?? '').trim() || null;
}

/**
 * The turno a timetable section names: 1 for `T1`, and for `T1GEA`, an exercise group of turno 1;
 * null for `TU` (the single turno) and `N0` (a course not split).
 */
export function sectionTurno(section: string): number | null {
	const match = /^T(\d+)/i.exec(section.trim());
	return match ? Number(match[1]) : null;
}

/**
 * The turno a variant's name tells, for lessons EasyStaff sends without their section: 1 for
 * "T1 FRONTALE TURNO 1" or "T1GEA ESER", null for none.
 */
export function subjectTurno(name: string): number | null {
	const text = variantText(name)?.toUpperCase();
	const match = text ? /\bT(\d+)(?:\b|G[A-Z])|\bTURNO (\d+)\b/.exec(text) : null;
	return match ? Number(match[1] ?? match[2]) : null;
}

/**
 * What tells a variant apart, short enough for a card: "T1", "Lab · Gruppo A", "T2 · Ese + Lab";
 * null for the lectures of a single turno, which need no label. The turno is the one the lesson's
 * section gives, when it has one. Free text EasyStaff writes in mixed case ("Cognomi A-L") is
 * kept as written.
 */
export function variantLabel(name: string, turno = subjectTurno(name)): string | null {
	const text = variantText(name);
	if (!text) return null;
	if (/\p{Ll}/u.test(text)) return text;
	const group = /\bGRUPPO ([A-Z0-9]+)\b/.exec(text)?.[1];
	const lecture = /\b(LEZ|FRONTALE)\b/.test(text);
	const kinds = lecture
		? []
		: [
				/\bESER?\b|\bT?U?\d?GE[A-Z]\b/.test(text) ? 'Ese' : null,
				/\bLAB\b/.test(text) ? 'Lab' : null
			];
	const parts = [
		turno === null ? null : `T${turno}`,
		kinds.filter(Boolean).join(' + ') || null,
		group ? `Gruppo ${group}` : null
	].filter(Boolean);
	return parts.length > 0 ? parts.join(' · ') : null;
}
