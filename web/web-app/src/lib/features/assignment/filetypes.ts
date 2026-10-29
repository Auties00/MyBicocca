/*
 * Moodle's file type groups (core_filetypes), which an assignment's accepted types may name
 * instead of extensions: "archive,.odt,.pdf,image". Only the groups Bicocca's courses use are
 * spelled out; a group this list doesn't know turns client checks off, and Moodle decides.
 */
const IMAGE_WEB = ['gif', 'jpe', 'jpeg', 'jpg', 'png', 'svg', 'svgz', 'webp'];
const AUDIO_WEB = ['aac', 'flac', 'm4a', 'mp3', 'oga', 'ogg', 'wav'];
const VIDEO_WEB = ['m4v', 'mp4', 'ogv', 'webm'];

const GROUPS = {
	archive: ['7z', 'gtar', 'gz', 'gzip', 'hqx', 'jar', 'mbz', 'rar', 'sit', 'tar', 'tgz', 'zip'],
	document: [
		'doc',
		'docm',
		'docx',
		'dot',
		'dotm',
		'dotx',
		'epub',
		'gdoc',
		'odm',
		'odt',
		'oth',
		'ott',
		'pdf',
		'ps',
		'rtf',
		'rtx',
		'txt'
	],
	spreadsheet: ['csv', 'gsheet', 'ods', 'ots', 'xls', 'xlsm', 'xlsx', 'xltm', 'xltx', 'xlam'],
	presentation: [
		'gslides',
		'odp',
		'otp',
		'pot',
		'potm',
		'potx',
		'ppam',
		'pps',
		'ppsm',
		'ppsx',
		'ppt',
		'pptm',
		'pptx',
		'pub'
	],
	image: [...IMAGE_WEB, 'ai', 'bmp', 'gdraw', 'heic', 'ico', 'pct', 'pic', 'pict', 'tif', 'tiff'],
	web_image: IMAGE_WEB,
	optimised_image: ['gif', 'jpe', 'jpeg', 'jpg', 'png'],
	audio: [...AUDIO_WEB, 'aif', 'aifc', 'aiff', 'au', 'ra', 'ram', 'rm', 'wma'],
	web_audio: AUDIO_WEB,
	html_audio: AUDIO_WEB,
	video: [
		...VIDEO_WEB,
		'3gp',
		'asf',
		'avi',
		'dv',
		'f4v',
		'flv',
		'mov',
		'movie',
		'mpe',
		'mpeg',
		'mpg',
		'qt',
		'rmvb',
		'wmv'
	],
	web_video: [...VIDEO_WEB, 'avi', 'flv', 'f4v', 'mov', 'mpe', 'mpeg', 'mpg', 'qt'],
	html_video: VIDEO_WEB,
	web_file: ['css', 'htm', 'html', 'js', 'scss', 'xhtml']
} satisfies Readonly<Record<string, readonly string[]>>;

export type FileTypeGroup = keyof typeof GROUPS;

export function isGroup(token: string): token is FileTypeGroup {
	return Object.hasOwn(GROUPS, token);
}

const extensionsIn = (group: FileTypeGroup): readonly string[] => GROUPS[group];

/** One accepted type as configured: an extension, a MIME type, or a group. */
export type TypeToken =
	| { kind: 'extension'; extension: string }
	| { kind: 'mime'; mime: string }
	| { kind: 'group'; group: FileTypeGroup }
	| { kind: 'unknown'; token: string };

export function parseToken(token: string): TypeToken {
	const value = token.trim().toLowerCase();
	if (value.startsWith('.')) return { kind: 'extension', extension: value.slice(1) };
	if (value.includes('/')) return { kind: 'mime', mime: value };
	if (isGroup(value)) return { kind: 'group', group: value };
	return { kind: 'unknown', token: value };
}

/** The name's extensions, longest first: `a.tar.gz` gives `tar.gz` and `gz`. */
function extensionsOf(name: string): string[] {
	const parts = name.toLowerCase().split('.').slice(1);
	return parts.map((_, index) => parts.slice(index).join('.')).filter((part) => part !== '');
}

function mimeMatches(pattern: string, mime: string): boolean {
	if (pattern.endsWith('/*')) return mime.startsWith(pattern.slice(0, -1));
	return pattern === mime;
}

/**
 * Whether a file of this name and type may be handed in. No types means any; a type this app
 * can't read means it can't tell, so the file is let through for Moodle to judge.
 */
export function acceptsFile(types: readonly string[], name: string, mime: string): boolean {
	if (types.length === 0) return true;
	const tokens = types.map(parseToken);
	if (tokens.some((token) => token.kind === 'unknown')) return true;
	const extensions = extensionsOf(name);
	return tokens.some((token) => {
		switch (token.kind) {
			case 'extension':
				return extensions.includes(token.extension);
			case 'mime':
				return mime !== '' && mimeMatches(token.mime, mime.toLowerCase());
			case 'group':
				return extensions.some((extension) => extensionsIn(token.group).includes(extension));
			case 'unknown':
				return true;
		}
	});
}

/** The file picker's `accept`, or undefined when the picker can't be told precisely. */
export function acceptAttribute(types: readonly string[]): string | undefined {
	if (types.length === 0) return undefined;
	const tokens = types.map(parseToken);
	if (tokens.some((token) => token.kind === 'unknown')) return undefined;
	const values = tokens.flatMap((token) => {
		switch (token.kind) {
			case 'extension':
				return [`.${token.extension}`];
			case 'mime':
				return [token.mime];
			case 'group':
				return extensionsIn(token.group).map((extension) => `.${extension}`);
			case 'unknown':
				return [];
		}
	});
	return [...new Set(values)].join(',');
}
