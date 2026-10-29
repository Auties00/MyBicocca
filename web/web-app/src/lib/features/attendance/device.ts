const TOKEN_KEY = 'mybicocca:attendance-device';

/**
 * A stable identifier of this browser, which EasyBadge uses to spot one device checking in for
 * several students. Without storage it lasts the check-in only.
 */
export function deviceToken(): string {
	try {
		const stored = localStorage.getItem(TOKEN_KEY);
		if (stored) return stored;
		const token = crypto.randomUUID();
		localStorage.setItem(TOKEN_KEY, token);
		return token;
	} catch {
		return crypto.randomUUID();
	}
}

export interface Position {
	latitude: number;
	longitude: number;
}

const NOWHERE: Position = { latitude: 0, longitude: 0 };

/**
 * Where the device is, only if the user already allowed it: checking in never asks for the
 * location, nor waits long for it. EasyBadge takes 0, 0 for unknown, as Android sends.
 */
export async function knownPosition(): Promise<Position> {
	try {
		const permission = await navigator.permissions.query({ name: 'geolocation' });
		if (permission.state !== 'granted') return NOWHERE;
		return await new Promise<Position>((resolve) => {
			navigator.geolocation.getCurrentPosition(
				({ coords }) => resolve({ latitude: coords.latitude, longitude: coords.longitude }),
				() => resolve(NOWHERE),
				{ maximumAge: 10 * 60_000, timeout: 3_000 }
			);
		});
	} catch {
		return NOWHERE;
	}
}
