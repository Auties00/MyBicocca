import { SvelteMap } from 'svelte/reactivity';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import type { Booking, ExamCall } from './model.ts';

/**
 * This visit's bookings and cancellations, kept so the page that made one can report it in place:
 * once the lists reload, a booked call leaves the bookable ones and a cancelled booking leaves the
 * bookings, so neither could be shown from the lists alone.
 */
class Outcomes {
	readonly #booked = new SvelteMap<string, ExamCall>();
	readonly #cancelled = new SvelteMap<string, Booking>();

	#id(scope: SessionScope, slug: string): string {
		return `${scope.account.id}/${scope.career.id}/${slug}`;
	}

	booked(scope: SessionScope, slug: string): ExamCall | undefined {
		return this.#booked.get(this.#id(scope, slug));
	}

	cancelled(scope: SessionScope, slug: string): Booking | undefined {
		return this.#cancelled.get(this.#id(scope, slug));
	}

	markBooked(scope: SessionScope, call: ExamCall): void {
		this.#cancelled.delete(this.#id(scope, call.slug));
		this.#booked.set(this.#id(scope, call.slug), call);
	}

	markCancelled(scope: SessionScope, booking: Booking): void {
		this.#booked.delete(this.#id(scope, booking.slug));
		this.#cancelled.set(this.#id(scope, booking.slug), booking);
	}
}

export const outcomes = new Outcomes();
