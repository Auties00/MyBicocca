import type { SessionScope } from '#lib/data/session.svelte.ts';
import type { CourseFile } from '../content.ts';
import type { OfficeApp } from '../files.ts';

const OFFICE_SCHEMES: Readonly<Record<OfficeApp, string>> = {
	word: 'ms-word',
	excel: 'ms-excel',
	powerpoint: 'ms-powerpoint'
};

/**
 * Whether an Office app can take a document by its address: the desktop apps on Windows and
 * macOS register the `ms-word:` schemes; phones, tablets, Linux and ChromeOS have none.
 */
export function officeHandoffAvailable(): boolean {
	if (typeof navigator === 'undefined') return false;
	const agent = navigator.userAgent;
	if (/Windows NT/.test(agent)) return true;
	return /Macintosh/.test(agent) && navigator.maxTouchPoints < 2;
}

/**
 * Hands the document to the Office app, read-only (`ofv`). Office fetches it itself, so the
 * address it gets carries the token: it stays out of every link the page shows, and the dialog
 * that offers this says so.
 */
export async function openInOffice(
	scope: SessionScope,
	file: CourseFile,
	app: OfficeApp
): Promise<void> {
	const token = await scope.api.moodle((_, auth) => Promise.resolve(auth.token));
	const url = new URL(file.url);
	url.searchParams.delete('forcedownload');
	url.searchParams.set('token', token);
	window.location.assign(`${OFFICE_SCHEMES[app]}:ofv|u|${url.href}`);
}
