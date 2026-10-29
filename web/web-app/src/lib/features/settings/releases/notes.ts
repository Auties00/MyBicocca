/** The GitHub alert kinds a release body may carry (`> [!TIP]`). */
export type CalloutKind = 'note' | 'tip' | 'important' | 'warning' | 'caution';

/** One bullet's raw inline Markdown. */
export interface BulletItem {
	text: string;
}

/** One block of a release body; texts keep their inline Markdown for `parseInline`. */
export type ReleaseBlock =
	| { type: 'heading'; level: number; text: string }
	| { type: 'paragraph'; text: string }
	| { type: 'list'; items: BulletItem[] }
	| { type: 'callout'; kind: CalloutKind; text: string };

const HEADING = /^(#{1,6})\s+(.+?)\s*#*$/;
const CALLOUT = /^>\s*\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)]\s*$/i;
const BULLET = /^(?:[-*+]|\d+\.)\s+(.+)$/;

/**
 * The blocks of a release body: the small Markdown subset the project's release template uses.
 * Blank lines separate blocks, consecutive prose lines join into one paragraph and consecutive
 * bullets into one list; anything unrecognized reads as prose.
 */
export function parseReleaseNotes(body: string): ReleaseBlock[] {
	const lines = body.replace(/\r\n?/g, '\n').split('\n');
	const blocks: ReleaseBlock[] = [];
	let paragraph: string[] = [];
	let bullets: BulletItem[] = [];

	const flushParagraph = () => {
		if (paragraph.length > 0) blocks.push({ type: 'paragraph', text: paragraph.join(' ').trim() });
		paragraph = [];
	};
	const flushBullets = () => {
		if (bullets.length > 0) blocks.push({ type: 'list', items: bullets });
		bullets = [];
	};

	for (let index = 0; index < lines.length; index++) {
		const line = lines[index].trim();
		const heading = HEADING.exec(line);
		const callout = CALLOUT.exec(line);
		const bullet = BULLET.exec(line);
		if (line === '') {
			flushBullets();
			flushParagraph();
		} else if (heading) {
			flushBullets();
			flushParagraph();
			blocks.push({ type: 'heading', level: heading[1].length, text: heading[2].trim() });
		} else if (callout) {
			flushBullets();
			flushParagraph();
			const text: string[] = [];
			while (index + 1 < lines.length && lines[index + 1].trimStart().startsWith('>')) {
				index++;
				text.push(lines[index].trimStart().replace(/^> ?/, ''));
			}
			blocks.push({
				type: 'callout',
				kind: callout[1].toLowerCase() as CalloutKind,
				text: text.join('\n').trim()
			});
		} else if (bullet) {
			flushParagraph();
			bullets.push({ text: bullet[1].trim() });
		} else {
			flushBullets();
			// A blockquote that is not an alert still reads as prose once its marker is gone.
			paragraph.push(line.replace(/^>\s*/, ''));
		}
	}
	flushBullets();
	flushParagraph();
	return blocks;
}

/** A run of inline text with the styles that apply to all of it. */
export interface InlineSegment {
	text: string;
	bold?: boolean;
	italic?: boolean;
	strike?: boolean;
	underline?: boolean;
	code?: boolean;
	/** An http(s) link, or the GitHub profile of an @mention. */
	href?: string;
	/** A forced line break (`<br>`); `text` is empty. */
	br?: boolean;
}

type Style = 'bold' | 'italic' | 'strike' | 'underline';

type Token =
	| { kind: 'text'; text: string }
	| { kind: 'toggle'; style: Style; raw: string }
	| { kind: 'open' | 'close'; style: Style }
	| { kind: 'code'; text: string }
	| { kind: 'link'; text: string; href: string | undefined }
	| { kind: 'br' };

const TAGS: Readonly<Record<string, readonly [Style, 'open' | 'close']>> = {
	b: ['bold', 'open'],
	strong: ['bold', 'open'],
	'/b': ['bold', 'close'],
	'/strong': ['bold', 'close'],
	i: ['italic', 'open'],
	em: ['italic', 'open'],
	'/i': ['italic', 'close'],
	'/em': ['italic', 'close'],
	s: ['strike', 'open'],
	del: ['strike', 'open'],
	strike: ['strike', 'open'],
	'/s': ['strike', 'close'],
	'/del': ['strike', 'close'],
	'/strike': ['strike', 'close'],
	u: ['underline', 'open'],
	ins: ['underline', 'open'],
	'/u': ['underline', 'close'],
	'/ins': ['underline', 'close']
};

function tokenize(raw: string): Token[] {
	const tokens: Token[] = [];
	const text = (value: string) => {
		const last = tokens.at(-1);
		if (last?.kind === 'text') last.text += value;
		else tokens.push({ kind: 'text', text: value });
	};
	let index = 0;
	while (index < raw.length) {
		const char = raw[index];
		const next = raw[index + 1];
		if (char === '`') {
			const end = raw.indexOf('`', index + 1);
			if (end > index) {
				tokens.push({ kind: 'code', text: raw.slice(index + 1, end) });
				index = end + 1;
				continue;
			}
		} else if (char === '[') {
			const close = raw.indexOf(']', index + 1);
			const paren = close > index && raw[close + 1] === '(' ? raw.indexOf(')', close + 2) : -1;
			if (paren > close) {
				const href = raw.slice(close + 2, paren).trim();
				tokens.push({
					kind: 'link',
					text: raw.slice(index + 1, close),
					href: /^https?:\/\//i.test(href) ? href : undefined
				});
				index = paren + 1;
				continue;
			}
		} else if (char === '@' && (index === 0 || !/[\p{L}\p{N}]/u.test(raw[index - 1]))) {
			const user = /^[A-Za-z0-9](?:[A-Za-z0-9-]*)/.exec(raw.slice(index + 1))?.[0];
			if (user) {
				tokens.push({ kind: 'link', text: `@${user}`, href: `https://github.com/${user}` });
				index += user.length + 1;
				continue;
			}
		} else if (char === '<') {
			const close = raw.indexOf('>', index + 1);
			if (close > index) {
				const tag = raw
					.slice(index + 1, close)
					.trim()
					.toLowerCase();
				if (/^br\s*\/?$/.test(tag)) tokens.push({ kind: 'br' });
				else if (TAGS[tag]) tokens.push({ kind: TAGS[tag][1], style: TAGS[tag][0] });
				else if (!/^\/?[a-z][a-z0-9]*$/.test(tag)) text(raw.slice(index, close + 1));
				index = close + 1;
				continue;
			}
		} else if (char === '*' && next === '*') {
			tokens.push({ kind: 'toggle', style: 'bold', raw: '**' });
			index += 2;
			continue;
		} else if (char === '~' && next === '~') {
			tokens.push({ kind: 'toggle', style: 'strike', raw: '~~' });
			index += 2;
			continue;
		} else if (char === '*') {
			tokens.push({ kind: 'toggle', style: 'italic', raw: '*' });
			index++;
			continue;
		}
		text(char);
		index++;
	}
	return tokens;
}

/**
 * The styled runs of one line of inline Markdown: `` `code` ``, `**bold**`, `*italic*`,
 * `~~strike~~`, the inline HTML tags GitHub allows (`<b> <i> <u> <s> <del> <ins> <em> <strong>
 * <br>`), `[links](https://…)` and `@mentions`. A Markdown marker left without its pair stays as
 * text; an HTML tag without its pair, or an unknown tag, is dropped; underscores are not emphasis.
 */
export function parseInline(raw: string): InlineSegment[] {
	const tokens = tokenize(raw);

	// Pair the Markdown toggles of each style in order (an odd one out is literal text), and each
	// HTML close tag with the latest open one of its style (one without a pair is dropped).
	const toggles: Record<Style, number[]> = { bold: [], italic: [], strike: [], underline: [] };
	const opens: Record<Style, number[]> = { bold: [], italic: [], strike: [], underline: [] };
	const literal = new Set<number>();
	const dropped = new Set<number>();
	tokens.forEach((token, index) => {
		if (token.kind === 'toggle') toggles[token.style].push(index);
		else if (token.kind === 'open') opens[token.style].push(index);
		else if (token.kind === 'close' && opens[token.style].pop() === undefined) dropped.add(index);
	});
	for (const style of Object.keys(toggles) as Style[]) {
		const indexes = toggles[style];
		if (indexes.length % 2 === 1) literal.add(indexes[indexes.length - 1]);
		for (const index of opens[style]) dropped.add(index);
	}

	// A style applies while its Markdown pair is open or any of its HTML tags is.
	const markdown: Record<Style, boolean> = {
		bold: false,
		italic: false,
		strike: false,
		underline: false
	};
	const tags: Record<Style, number> = { bold: 0, italic: 0, strike: 0, underline: 0 };
	const styles: readonly Style[] = ['bold', 'italic', 'strike', 'underline'];
	const segments: InlineSegment[] = [];
	const push = (segment: InlineSegment) => {
		for (const style of styles) if (markdown[style] || tags[style] > 0) segment[style] = true;
		const last = segments.at(-1);
		const plain = (item: InlineSegment) => !item.code && !item.href && !item.br;
		if (last && plain(last) && plain(segment) && styles.every((s) => !!last[s] === !!segment[s])) {
			last.text += segment.text;
		} else {
			segments.push(segment);
		}
	};

	tokens.forEach((token, index) => {
		switch (token.kind) {
			case 'text':
				push({ text: token.text });
				break;
			case 'code':
				push({ text: token.text, code: true });
				break;
			case 'link':
				push(token.href ? { text: token.text, href: token.href } : { text: token.text });
				break;
			case 'br':
				segments.push({ text: '', br: true });
				break;
			case 'toggle':
				if (literal.has(index)) push({ text: token.raw });
				else markdown[token.style] = !markdown[token.style];
				break;
			case 'open':
				if (!dropped.has(index)) tags[token.style]++;
				break;
			case 'close':
				if (!dropped.has(index)) tags[token.style]--;
				break;
		}
	});
	return segments.filter((segment) => segment.br || segment.text !== '');
}
