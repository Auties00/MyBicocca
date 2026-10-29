import { getDocument, GlobalWorkerOptions, TextLayer } from 'pdfjs-dist';
import worker from 'pdfjs-dist/build/pdf.worker.min.mjs?url';
import type { PDFDocumentLoadingTask } from 'pdfjs-dist';

/* pdf.js, loaded only by the PDF reader: its worker parses off the main thread. */
GlobalWorkerOptions.workerSrc = worker;

export { TextLayer };

/** Opens a PDF from memory. pdf.js takes ownership of the buffer it gets, so it gets a copy. */
export function openPdf(bytes: Uint8Array): PDFDocumentLoadingTask {
	return getDocument({ data: bytes.slice() });
}
