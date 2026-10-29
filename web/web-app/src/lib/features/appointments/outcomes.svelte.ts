import { SvelteMap } from 'svelte/reactivity';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import type { Reservation } from './reservation.ts';

/**
 * This visit's bookings and cancellations, so the page that made one can tell it in place: a
 * fresh booking opens on its confirmation, and a cancelled one is gone from the kept list.
 */
class Outcomes {
	readonly #booked = new SvelteMap<string, Reservation>();
	readonly #cancelled = new SvelteMap<string, Reservation>();

	#id(scope: SessionScope, code: string): string {
		return `${scope.account.id}/${code}`;
	}

	booked(scope: SessionScope, code: string): Reservation | undefined {
		return this.#booked.get(this.#id(scope, code));
	}

	cancelled(scope: SessionScope, code: string): Reservation | undefined {
		return this.#cancelled.get(this.#id(scope, code));
	}

	markBooked(scope: SessionScope, reservation: Reservation): void {
		this.#cancelled.delete(this.#id(scope, reservation.code));
		this.#booked.set(this.#id(scope, reservation.code), reservation);
	}

	markCancelled(scope: SessionScope, reservation: Reservation): void {
		this.#booked.delete(this.#id(scope, reservation.code));
		this.#cancelled.set(this.#id(scope, reservation.code), reservation);
	}

	/** The confirmation is told once: leaving the page settles it. */
	settle(scope: SessionScope, code: string): void {
		this.#booked.delete(this.#id(scope, code));
	}
}

export const outcomes = new Outcomes();
