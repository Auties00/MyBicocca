/**
 * The floor as the staff type it: a number, optionally after "Piano" in any case ("1",
 * "Piano -1"), or a spelling of the ground floor ("T", "PT", "Terra", "piano terra", and
 * "Rialzato" for the raised ground floor). Anything else is no floor.
 */
export function parseRoomFloor(text: string | null): number | null {
	if (text === null) return null;
	const floor = text
		.trim()
		.toLowerCase()
		.replace(/^piano/, '')
		.trim();
	if (['t', 'pt', 'terra', 'rialzato'].includes(floor)) return 0;
	return /^[+-]?\d+$/.test(floor) ? Number(floor) : null;
}

const TEXT_NODE = 3;

/** Without zero-width spaces, with non-breaking spaces made plain, trimmed. */
function clean(text: string): string {
	return text.replaceAll('\u200b', '').replaceAll('\u00a0', ' ').trim();
}

/**
 * A free-form field, which may hold HTML, as plain text keeping its line structure: `<br>` is a
 * line break, list items become bulleted lines. Blank text and the registry's "Dato mancante"
 * placeholder are no text.
 */
export function plainText(html: string | null): string | null {
	if (html === null) return null;
	const body = new DOMParser().parseFromString(`<body>${html}</body>`, 'text/html').body;
	let text = '';
	for (const node of body.childNodes) {
		if (node.nodeType === TEXT_NODE) text += node.textContent ?? '';
		else if (node.nodeName === 'BR') text += '\n';
		else if (node.nodeName === 'UL' || node.nodeName === 'OL') {
			for (const item of (node as Element).querySelectorAll('li')) {
				text += `\n• ${clean(item.textContent ?? '')}`;
			}
		} else text += `\n${clean(node.textContent ?? '')}\n`;
	}
	const result = text
		.split(/\r?\n/)
		.map((line) => clean(line))
		.join('\n')
		.replace(/\n{3,}/g, '\n\n')
		.trim();
	return result === '' || result.toLowerCase() === 'dato mancante' ? null : result;
}

/**
 * EasyStaff sends some labels encoded twice ("AttivitÃ  istituzionali"): text whose characters
 * are all bytes and that reads as UTF-8 is decoded once more.
 */
export function repairMojibake(text: string): string {
	if (!/[\u00c2-\u00f4][\u0080-\u00bf]/.test(text)) return text;
	if ([...text].some((char) => char.charCodeAt(0) > 0xff)) return text;
	try {
		const bytes = Uint8Array.from(text, (char) => char.charCodeAt(0));
		return new TextDecoder('utf-8', { fatal: true }).decode(bytes);
	} catch {
		return text;
	}
}

/**
 * The room's category, when it says something: the showcase also writes "non definito" and, for
 * many lecture halls, a seat bracket such as "126-142", which the seat count already tells.
 */
export function roomType(text: string | null): string | null {
	const type = text === null ? '' : clean(text);
	if (type === '' || type.toLowerCase() === 'non definito' || /^\d+\s*-\s*\d+$/.test(type)) {
		return null;
	}
	return type;
}
