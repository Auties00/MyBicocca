import IconFileText from '~icons/ph/file-text';
import IconIdentificationCard from '~icons/ph/identification-card';
import IconMedal from '~icons/ph/medal';
import IconReceipt from '~icons/ph/receipt';
import { m } from '#lib/paraglide/messages.js';
import type { Icon } from '#lib/ui/types.ts';
import type { Certificate, CertificateKind } from './certificates.ts';
import { joinParts } from './text.ts';

/**
 * How a certificate opens: previewed in the page, in the browser's own viewer; in a new tab on
 * phones, too narrow to preview a page in, and on Apple's touch devices; saved as a file where the
 * browser shows no PDF at all (Chrome on Android).
 */
export type OpenMode = 'preview' | 'tab' | 'save';

/** Whether this browser shows PDFs inside a page. */
export function hasPdfViewer(): boolean {
	return typeof navigator !== 'undefined' && navigator.pdfViewerEnabled === true;
}

/**
 * Whether a PDF in a frame shows all of it. Safari on iPhone and iPad (which calls itself a Mac,
 * with touch) draws only the first page there, unscrollable; its viewer in a tab shows them all.
 */
export function previewsWhole(): boolean {
	if (!hasPdfViewer()) return false;
	const agent = navigator.userAgent;
	return (
		!/iPhone|iPad|iPod/.test(agent) && !(/Macintosh/.test(agent) && navigator.maxTouchPoints > 1)
	);
}

/**
 * The viewer's opening view, without the thumbnail rail a one-page certificate has no use for; in
 * a frame, the page across its width (Chrome otherwise fits the whole page, too small to read).
 */
export function viewerUrl(url: string, frame: boolean): string {
	return `${url}#navpanes=0&pagemode=none${frame ? '&view=FitH' : ''}`;
}

export const kindIcons: Record<CertificateKind, Icon> = {
	enrolment: IconIdentificationCard,
	degree: IconMedal,
	fees: IconReceipt,
	other: IconFileText
};

export function certificateName(certificate: Certificate): string {
	return certificate.description ?? m.documents_certificates_fallback();
}

/** "Year 2025 · Digitally signed": what tells two prints of one declaration apart. */
export function certificateLine(certificate: Certificate): string {
	return joinParts([
		certificate.solarYear === null
			? null
			: m.documents_certificates_year({ year: String(certificate.solarYear) }),
		certificate.signed ? m.documents_certificates_signed() : null
	]);
}

/** Shares the PDF with another app; dismissing the share sheet is no failure. */
export async function shareFile(blob: Blob, name: string, title: string): Promise<void> {
	try {
		await navigator.share({ files: [new File([blob], name, { type: 'application/pdf' })], title });
	} catch {
		// Dismissed, or refused by the target app: nothing to report.
	}
}

/** Whether this browser can hand a PDF to other apps (the Web Share API with files). */
export function canShareFiles(): boolean {
	if (typeof navigator === 'undefined' || typeof navigator.canShare !== 'function') return false;
	try {
		return navigator.canShare({
			files: [new File([], 'certificate.pdf', { type: 'application/pdf' })]
		});
	} catch {
		return false;
	}
}

/** Saves `url` under `name`, as a download link would. */
export function saveFile(url: string, name: string): void {
	const link = document.createElement('a');
	link.href = url;
	link.download = name;
	link.rel = 'noopener';
	document.body.append(link);
	link.click();
	link.remove();
}
