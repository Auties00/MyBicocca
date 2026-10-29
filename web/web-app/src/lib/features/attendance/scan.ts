/** What a scanned or typed check-in payload asks for. */
export type PresenceScan =
	/** A Moodle attendance session (`attendance.php?sessid=…`), with the static QR's password. */
	| { kind: 'session'; sessionId: string; password: string | null }
	/** An EasyBadge lesson code, the one shown in the classroom. */
	| { kind: 'lesson'; code: string }
	| { kind: 'unrecognized' };

const SESSION_PAGE = 'mod/attendance/attendance.php';
const LESSON_CODE_PARAMS = ['codice_lezione', 'codice', 'code', 'lezione'];

/** `+` is a space and `%XX` runs are UTF-8 bytes; a malformed run is kept as it is. */
function decode(value: string): string {
	return value.replace(/\+/g, ' ').replace(/(?:%[0-9a-f]{2})+/gi, (run) => {
		try {
			return decodeURIComponent(run);
		} catch {
			return run;
		}
	});
}

/** The query of `url` with lowercased names; a repeated name keeps its last value. */
function queryParams(url: string): Map<string, string> {
	const start = url.indexOf('?');
	const params = new Map<string, string>();
	if (start < 0) return params;
	const query = url.slice(start + 1).split('#', 1)[0];
	for (const pair of query.split('&')) {
		const separator = pair.indexOf('=');
		if (separator <= 0) continue;
		params.set(pair.slice(0, separator).toLowerCase(), decode(pair.slice(separator + 1)));
	}
	return params;
}

/**
 * Whether the text is a link, what a QR code opens, rather than a code typed by hand: an address,
 * or a Moodle session page copied without its scheme.
 */
export function looksLikeLink(raw: string): boolean {
	const text = raw.trim().toLowerCase();
	return /^https?:\/\//.test(text) || text.includes(SESSION_PAGE);
}

/**
 * Routes a check-in payload: Moodle attendance links become sessions, other links carrying a
 * lesson code parameter and any bare text become EasyBadge lesson codes.
 */
export function parsePresenceScan(raw: string): PresenceScan {
	const text = raw.trim();
	if (text === '') return { kind: 'unrecognized' };

	if (text.toLowerCase().includes(SESSION_PAGE)) {
		const params = queryParams(text);
		const sessionId = params.get('sessid');
		if (sessionId === undefined) return { kind: 'unrecognized' };
		return { kind: 'session', sessionId, password: params.get('qrpass') ?? null };
	}

	if (/^https?:\/\//i.test(text)) {
		const params = queryParams(text);
		const name = LESSON_CODE_PARAMS.find((candidate) => params.has(candidate));
		const code = name === undefined ? undefined : params.get(name);
		return code === undefined ? { kind: 'unrecognized' } : { kind: 'lesson', code };
	}

	return { kind: 'lesson', code: text };
}
