import { createContext } from 'svelte';
import { SvelteSet } from 'svelte/reactivity';
import { goto } from '$app/navigation';
import type { ResolvedPathname } from '$app/types';
import { toDataError, type DataError } from '#lib/data/errors.ts';
import { cache, policies } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import type { CourseFile } from '../content.ts';
import { classifyFile, mimeFor, viewableKind, type FileKind, type OfficeApp } from '../files.ts';
import { fileQuery } from '../queries.ts';
import { fileOpenPreferences } from './preference.svelte.ts';
import { saveFile } from './save.ts';

/** A file someone asked to open, with the viewer page it opens in, if not already there. */
export interface FileRequest {
	file: CourseFile;
	href?: ResolvedPathname;
}

/** The bytes of `file`, from this visit's cache when it was opened before. */
export async function fetchFile(
	scope: SessionScope,
	file: Pick<CourseFile, 'url' | 'modified'>
): Promise<Uint8Array> {
	const options = fileQuery(scope, file);
	return cache.fetch(options.key, options.load, options.policy ?? policies.live);
}

export function fileMime(file: CourseFile, kind: FileKind = classifyFile(file.name, file.mime)) {
	return file.mime ?? mimeFor(kind, file.name);
}

/**
 * Opens files as Android's shell does, adapted to the web: Office documents offer Word, Excel or
 * PowerPoint or a download; files nothing here can show are downloaded; the rest follow the
 * choice remembered for their kind, asking when there is none. Shift+click asks again, and a
 * click with Ctrl or ⌘ opens the viewer in a new tab as any link does.
 */
export class FileOpener {
	/** The file of the "view here or download" question, kept while its dialog closes. */
	asking = $state.raw<FileRequest | null>(null);
	askingOpen = $state(false);
	/** The Office document of the hand-off, kept while its dialog closes. */
	office = $state.raw<{ request: FileRequest; app: OfficeApp } | null>(null);
	officeOpen = $state(false);
	/** Addresses of the files being downloaded, which their rows mark. */
	readonly downloading = new SvelteSet<string>();
	/** The last download that failed, by address, for its row to say so. */
	failed = $state.raw<{ url: string; error: DataError } | null>(null);

	readonly #scope: () => SessionScope | undefined;

	constructor(scope: () => SessionScope | undefined) {
		this.#scope = scope;
	}

	/** For a click on a file's link: lets the link go, or takes over. */
	click = (event: MouseEvent, request: FileRequest): void => {
		if (event.button !== 0 || event.ctrlKey || event.metaKey || event.altKey) return;
		event.preventDefault();
		this.open(request, event.shiftKey);
	};

	/** Opens `request` by its kind and the remembered choice; `ask` shows the question anyway. */
	open(request: FileRequest, ask = false): void {
		const kind = classifyFile(request.file.name, request.file.mime);
		if (kind.type === 'office') {
			this.office = { request, app: kind.app };
			this.officeOpen = true;
			return;
		}
		const viewable = viewableKind(kind);
		if (viewable === null) {
			void this.download(request.file);
			return;
		}
		const choice = ask ? undefined : fileOpenPreferences.get(viewable);
		if (choice === 'view') this.view(request);
		else if (choice === 'download') void this.download(request.file);
		else {
			this.asking = request;
			this.askingOpen = true;
		}
	}

	/** Opens the file's viewer page, unless already on it. */
	view(request: FileRequest): void {
		if (request.href !== undefined) void goto(request.href);
	}

	/** Saves the file to the device; the failure, if any, is kept for the row and returned. */
	download = async (file: CourseFile): Promise<DataError | undefined> => {
		const scope = this.#scope();
		if (scope === undefined || this.downloading.has(file.url)) return undefined;
		this.downloading.add(file.url);
		if (this.failed?.url === file.url) this.failed = null;
		try {
			saveFile(await fetchFile(scope, file), file.name, fileMime(file));
			return undefined;
		} catch (error) {
			const failure = toDataError(error);
			this.failed = { url: file.url, error: failure };
			return failure;
		} finally {
			this.downloading.delete(file.url);
		}
	};
}

export const [getFileOpener, setFileOpener] = createContext<FileOpener>();
