import type { DecodeRequest, DecodeResponse } from './decoder.worker.ts';

/** Reads the QR code in a video's current frame. */
export interface QrReader {
	read(video: HTMLVideoElement): Promise<string | undefined>;
	close(): void;
}

/** The Barcode Detection API, which TypeScript's DOM library doesn't describe yet. */
interface BarcodeDetectorClass {
	new (options: { formats: string[] }): {
		detect(source: CanvasImageSource): Promise<{ rawValue: string }[]>;
	};
	getSupportedFormats(): Promise<string[]>;
}

/** The platform's own detector, where it reads QR codes: Chromium on Android, ChromeOS and macOS. */
async function nativeReader(): Promise<QrReader | undefined> {
	const Detector = (globalThis as { BarcodeDetector?: BarcodeDetectorClass }).BarcodeDetector;
	if (Detector === undefined) return undefined;
	try {
		if (!(await Detector.getSupportedFormats()).includes('qr_code')) return undefined;
	} catch {
		return undefined;
	}
	const detector = new Detector({ formats: ['qr_code'] });
	return {
		async read(video) {
			const [code] = await detector.detect(video);
			return code?.rawValue || undefined;
		},
		close() {}
	};
}

/** Frames are read at most this large: a code across a lecture hall keeps 3 px a module. */
const MAX_SIDE = 960;

/**
 * Elsewhere (Safari, Firefox, Chrome on Windows and Linux), the app's decoder in a worker, so
 * reading a frame never holds up the page. One frame at a time: a read while another is on its
 * way finds nothing.
 */
function workerReader(): QrReader {
	const worker = new Worker(new URL('./decoder.worker.ts', import.meta.url), {
		type: 'module',
		name: 'qr'
	});
	const canvas = document.createElement('canvas');
	const context = canvas.getContext('2d', { willReadFrequently: true });
	let next = 0;
	let pending: { id: number; resolve: (text: string | undefined) => void } | undefined;
	const settle = (text: string | undefined) => {
		pending?.resolve(text);
		pending = undefined;
	};
	worker.addEventListener('message', ({ data }: MessageEvent<DecodeResponse>) => {
		if (data.id === pending?.id) settle(data.text ?? undefined);
	});
	worker.addEventListener('error', (event) => {
		event.preventDefault();
		settle(undefined);
	});
	return {
		read(video) {
			const scale = Math.min(1, MAX_SIDE / Math.max(video.videoWidth, video.videoHeight));
			const width = Math.round(video.videoWidth * scale);
			const height = Math.round(video.videoHeight * scale);
			if (pending || !context || width === 0 || height === 0) return Promise.resolve(undefined);
			if (canvas.width !== width || canvas.height !== height) {
				canvas.width = width;
				canvas.height = height;
			}
			context.drawImage(video, 0, 0, width, height);
			const pixels = context.getImageData(0, 0, width, height).data;
			const request: DecodeRequest = { id: next++, pixels, width, height };
			return new Promise((resolve) => {
				pending = { id: request.id, resolve };
				worker.postMessage(request, [pixels.buffer]);
			});
		},
		close() {
			worker.terminate();
			settle(undefined);
		}
	};
}

/** The platform's QR detector where it has one, else the app's decoder. Close it when done. */
export async function qrReader(): Promise<QrReader> {
	return (await nativeReader()) ?? workerReader();
}
