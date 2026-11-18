import type { Attachment } from 'svelte/attachments';
import { download } from '#lib/ui/files.ts';

/** The object URL of `bytes`, typed so the browser's own viewers open it. */
export function blobUrl(bytes: Uint8Array, mime: string): string {
	return URL.createObjectURL(new Blob([bytes as Uint8Array<ArrayBuffer>], { type: mime }));
}

/** Shows `bytes` in the element as an object URL, revoked when the element goes or they change. */
export function objectUrl(
	bytes: Uint8Array,
	mime: string
): Attachment<HTMLImageElement | HTMLMediaElement> {
	return (element) => {
		const url = blobUrl(bytes, mime);
		element.src = url;
		return () => URL.revokeObjectURL(url);
	};
}

/** Saves `bytes` to the device's downloads as `name`. */
export function saveFile(bytes: Uint8Array, name: string, mime: string): void {
	download(new Blob([bytes as Uint8Array<ArrayBuffer>], { type: mime }), name);
}

/** Opens `bytes` in a new tab, where the browser shows a PDF, an image or a video itself. */
export function openInTab(bytes: Uint8Array, mime: string): void {
	const url = blobUrl(bytes, mime);
	window.open(url, '_blank', 'noopener');
	setTimeout(() => URL.revokeObjectURL(url), 60_000);
}

function asFile(bytes: Uint8Array, name: string, mime: string): File {
	return new File([bytes as Uint8Array<ArrayBuffer>], name, { type: mime });
}

/** Whether the device's share sheet takes files (phones, some desktops). */
export function canShareFile(name: string, mime: string): boolean {
	if (typeof navigator.canShare !== 'function') return false;
	return navigator.canShare({ files: [asFile(new Uint8Array(0), name, mime)] });
}

/** Hands the file to the share sheet; false when the user closed it without sharing. */
export async function shareFile(bytes: Uint8Array, name: string, mime: string): Promise<boolean> {
	try {
		await navigator.share({ files: [asFile(bytes, name, mime)], title: name });
		return true;
	} catch (error) {
		if (error instanceof DOMException && error.name === 'AbortError') return false;
		throw error;
	}
}
