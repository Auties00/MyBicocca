export type OfficeApp = 'word' | 'excel' | 'powerpoint';

export type FileKind =
	| { type: 'pdf' }
	| { type: 'office'; app: OfficeApp }
	| { type: 'zip' }
	| { type: 'image' }
	| { type: 'video' }
	| { type: 'audio' }
	| { type: 'html' }
	| { type: 'text' }
	| { type: 'unknown' };

/** The kinds a viewing preference is kept for, as Settings › Files lists them. */
export type ViewableKind = 'pdf' | 'image' | 'video' | 'audio' | 'html' | 'text' | 'zip';

const TEXT_EXTENSIONS = new Set(
	(
		'txt md csv json xml yaml yml log java kt py ipynb sql c cpp h hpp cc cs js ts sh bat asm s m ' +
		'pl rb go rs lark circ dot tex properties gradle toml ini'
	).split(' ')
);

const EXTENSIONS: ReadonlyArray<[FileKind, readonly string[]]> = [
	[{ type: 'pdf' }, ['pdf']],
	[{ type: 'office', app: 'word' }, ['doc', 'docx', 'rtf', 'odt']],
	[{ type: 'office', app: 'excel' }, ['xls', 'xlsx', 'ods']],
	[{ type: 'office', app: 'powerpoint' }, ['ppt', 'pptx', 'pps', 'ppsx', 'odp']],
	[{ type: 'zip' }, ['zip', 'jar']],
	[{ type: 'image' }, ['png', 'jpg', 'jpeg', 'webp', 'gif', 'svg', 'bmp']],
	[{ type: 'video' }, ['mp4', 'webm', 'mkv', 'mov', 'm4v', '3gp']],
	[{ type: 'audio' }, ['mp3', 'm4a', 'wav', 'ogg', 'flac', 'aac']],
	[{ type: 'html' }, ['html', 'htm']]
];

function byMime(mime: string): FileKind | undefined {
	if (mime === 'application/pdf') return { type: 'pdf' };
	if (/wordprocessingml|msword/.test(mime)) return { type: 'office', app: 'word' };
	if (/spreadsheetml|ms-excel/.test(mime)) return { type: 'office', app: 'excel' };
	if (/presentationml|ms-powerpoint/.test(mime)) return { type: 'office', app: 'powerpoint' };
	if (mime === 'application/zip' || mime === 'application/java-archive') return { type: 'zip' };
	if (mime.startsWith('image/')) return { type: 'image' };
	if (mime.startsWith('video/')) return { type: 'video' };
	if (mime.startsWith('audio/')) return { type: 'audio' };
	if (mime === 'text/html') return { type: 'html' };
	if (mime.startsWith('text/') || mime === 'application/json') return { type: 'text' };
	return undefined;
}

/** What a file is, by its MIME type first and then its extension. */
export function classifyFile(name: string, mime: string | null): FileKind {
	const fromMime = mime ? byMime(mime.toLowerCase().split(';')[0].trim()) : undefined;
	if (fromMime !== undefined) return fromMime;
	const extension = /\.([a-z0-9]+)$/i.exec(name)?.[1]?.toLowerCase() ?? '';
	for (const [kind, extensions] of EXTENSIONS) if (extensions.includes(extension)) return kind;
	return TEXT_EXTENSIONS.has(extension) ? { type: 'text' } : { type: 'unknown' };
}

export function viewableKind(kind: FileKind): ViewableKind | null {
	return kind.type === 'office' || kind.type === 'unknown' ? null : kind.type;
}

/** The MIME type to hand the browser when Moodle sent none. */
export function mimeFor(kind: FileKind, name: string): string {
	const extension = /\.([a-z0-9]+)$/i.exec(name)?.[1]?.toLowerCase();
	switch (kind.type) {
		case 'pdf':
			return 'application/pdf';
		case 'image':
			return extension === 'svg'
				? 'image/svg+xml'
				: `image/${extension === 'jpg' ? 'jpeg' : extension}`;
		case 'html':
			return 'text/html';
		case 'text':
			return 'text/plain';
		case 'zip':
			return 'application/zip';
		default:
			return 'application/octet-stream';
	}
}

const PLUGINFILE = /^\/(?:webservice\/)?pluginfile\.php\/(.+)$/;

/**
 * The path `moodle.files.downloadFile` takes: what follows `pluginfile.php/`, query dropped. Pages
 * link files through `/pluginfile.php/`, which needs a browser session; the web service serves the
 * same path with the token.
 */
export function pluginfilePath(fileUrl: string): string | null {
	try {
		const url = new URL(fileUrl);
		if (url.hostname !== 'elearning.unimib.it') return null;
		return PLUGINFILE.exec(url.pathname)?.[1] ?? null;
	} catch {
		return null;
	}
}
