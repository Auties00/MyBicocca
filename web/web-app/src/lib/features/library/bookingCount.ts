const storageKey = (accountId: string) => `mybicocca:library-bookings:${accountId}`;

/** How many bookings the account had last time, so the loading skeleton shows as many. */
export function expectedBookings(accountId: string): number {
	try {
		const stored = localStorage.getItem(storageKey(accountId));
		const count = Number(stored);
		return stored !== null && Number.isInteger(count) && count >= 0 ? Math.min(count, 6) : 1;
	} catch {
		return 1;
	}
}

export function rememberBookings(accountId: string, count: number): void {
	try {
		localStorage.setItem(storageKey(accountId), String(count));
	} catch {
		// Without storage the skeleton shows a single card.
	}
}
