import { icsFile, type CalendarEvent } from '#lib/format/ics.ts';

/** Saves `blob` to the device's downloads as `name`, through a temporary link. */
export function download(blob: Blob, name: string): void {
	const url = URL.createObjectURL(blob);
	const link = document.createElement('a');
	link.href = url;
	link.download = name;
	link.rel = 'noopener';
	document.body.append(link);
	link.click();
	link.remove();
	// The download reads the blob after this task ends; a minute is ample.
	setTimeout(() => URL.revokeObjectURL(url), 60_000);
}

/**
 * Shows a PDF that has still to be fetched in a new tab. The tab opens at once, inside the click,
 * so popup blockers allow it, and receives the file when it arrives; when no tab could be opened,
 * the file is downloaded instead.
 *
 * @param waiting Text the empty tab shows meanwhile.
 * @throws What `load` throws, after closing the tab.
 */
export async function openPdf(
	load: () => Promise<Uint8Array>,
	name: string,
	waiting: string
): Promise<void> {
	const tab = window.open('', '_blank');
	if (tab) {
		tab.document.title = name;
		tab.document.body.style.font = '15px system-ui, sans-serif';
		tab.document.body.textContent = waiting;
	}
	let bytes: Uint8Array;
	try {
		bytes = await load();
	} catch (error) {
		tab?.close();
		throw error;
	}
	const blob = new Blob([new Uint8Array(bytes)], { type: 'application/pdf' });
	if (!tab || tab.closed) {
		download(blob, name);
		return;
	}
	const url = URL.createObjectURL(blob);
	tab.location.href = url;
	setTimeout(() => URL.revokeObjectURL(url), 5 * 60_000);
}

/**
 * Saves `event` as an iCalendar file, which calendar apps open as "add this event". The file is
 * named after `name`, kept to letters and digits.
 */
export function saveCalendarEvent(event: CalendarEvent, name: string): void {
	const base = name.replace(/[^\p{L}\p{N}]+/gu, '-').replace(/^-|-$/g, '') || 'MyBicocca';
	download(new Blob([icsFile(event)], { type: 'text/calendar;charset=utf-8' }), `${base}.ics`);
}
