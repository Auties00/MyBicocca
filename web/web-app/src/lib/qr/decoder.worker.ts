import { decodeQr } from './decode.ts';

/** A camera frame to read. */
export interface DecodeRequest {
	id: number;
	pixels: Uint8ClampedArray;
	width: number;
	height: number;
}

/** The frame's QR code text, or null. */
export interface DecodeResponse {
	id: number;
	text: string | null;
}

/** The worker's global scope, typed without the webworker lib, which clashes with the DOM's. */
const worker = self as unknown as {
	addEventListener(type: 'message', listener: (event: MessageEvent<DecodeRequest>) => void): void;
	postMessage(message: DecodeResponse): void;
};

worker.addEventListener('message', ({ data }) => {
	const text = decodeQr(data.pixels, data.width, data.height) ?? null;
	worker.postMessage({ id: data.id, text });
});
