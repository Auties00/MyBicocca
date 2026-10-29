import type { CourseFile } from './content.ts';
import { pluginfilePath } from './files.ts';

/** The file the viewer's address names, and the archive entry inside it, if any. */
export type ViewerRequest =
	| { by: 'module'; cmId: number; name: string; folder: string; entry: string | null }
	| { by: 'address'; file: CourseFile; entry: string | null };

/**
 * Reads the viewer's search params: an activity and a file name (`cm`, `name`, `dir`), located
 * again in the course's contents, or a Moodle file address with what is known of it (`url`,
 * `name`, `mime`, `size`). Null when they name nothing the viewer can open.
 */
export function fileRequestOf(params: Pick<URLSearchParams, 'get'>): ViewerRequest | null {
	const entry = params.get('entry');
	const name = params.get('name')?.trim() ?? '';
	const cmId = Number(params.get('cm'));
	if (Number.isSafeInteger(cmId) && cmId > 0 && name !== '') {
		return { by: 'module', cmId, name, folder: params.get('dir') || '/', entry };
	}
	const url = params.get('url');
	if (url === null || pluginfilePath(url) === null) return null;
	const size = Number(params.get('size'));
	return {
		by: 'address',
		file: {
			url,
			name: name || decodeURIComponent(new URL(url).pathname.split('/').at(-1) ?? ''),
			folder: '/',
			mime: params.get('mime'),
			size: Number.isFinite(size) && size > 0 ? size : null,
			modified: null
		},
		entry
	};
}
