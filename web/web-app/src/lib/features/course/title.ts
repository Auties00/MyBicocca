/** A course title set as its cover does: the last word apart, and any tag after it as a tag. */
export interface CoverTitle {
	head: string;
	tail: string;
	/** "blended", "T1": what Moodle appends to a teaching's name. */
	tag: string | null;
}

/** A trailing "(blended)", "[2025/26]", or a stream: "- T1", "– Turno A". */
const TAG = /\s*(?:\(([^()]+)\)|\[([^[\]]+)\]|\s[-–—]\s*((?:T|turno\s*)\w{1,3}))$/i;

export function coverTitle(title: string): CoverTitle {
	let rest = title.trim();
	let tag: string | null = null;
	const match = TAG.exec(rest);
	if (match !== null && match.index > 0) {
		tag = (match[1] ?? match[2] ?? match[3]).trim();
		rest = rest.slice(0, match.index).trim();
	}
	let at = rest.lastIndexOf(' ');
	// A numbered course keeps its number with the word it counts: "Programmazione 1", "Fisica II".
	if (at > 0 && /^(?:\d+|[IVX]+|[A-Z])$/.test(rest.slice(at + 1)))
		at = rest.lastIndexOf(' ', at - 1);
	return at < 0
		? { head: '', tail: rest, tag }
		: { head: rest.slice(0, at), tail: rest.slice(at + 1), tag };
}
