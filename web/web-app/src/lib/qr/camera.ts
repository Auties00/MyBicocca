export type CameraFailure = 'denied' | 'unavailable';

/**
 * Whether scanning can be offered: a secure page with the media API and a camera among its
 * devices, which browsers list (unnamed) without asking for any permission. Any browser will do:
 * where it has no QR detector of its own, the app's decoder reads the frames.
 */
export async function canScanQr(): Promise<boolean> {
	if (!window.isSecureContext || !navigator.mediaDevices?.getUserMedia) return false;
	try {
		const devices = await navigator.mediaDevices.enumerateDevices();
		return devices.some((device) => device.kind === 'videoinput');
	} catch {
		return true;
	}
}

/** The rear camera's stream; asking for it is what prompts for the permission. */
export async function openCamera(): Promise<MediaStream | CameraFailure> {
	try {
		return await navigator.mediaDevices.getUserMedia({
			audio: false,
			video: { facingMode: { ideal: 'environment' }, width: { ideal: 1280 } }
		});
	} catch (error) {
		const name = error instanceof DOMException ? error.name : '';
		return name === 'NotAllowedError' || name === 'SecurityError' ? 'denied' : 'unavailable';
	}
}
