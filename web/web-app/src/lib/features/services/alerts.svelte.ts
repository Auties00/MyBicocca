import { now } from '#lib/data/clock.svelte.ts';
import { query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { libraryLinks } from '#lib/features/library/account.svelte.ts';
import { activeReservations, type Reservation } from '#lib/features/library/model.ts';
import { myReservationsQuery } from '#lib/features/library/queries.ts';
import { isPending, toActivities } from '#lib/features/questionnaires/model.ts';
import { recordBookQuestionnairesQuery } from '#lib/features/questionnaires/queries.ts';
import { romeDay } from '#lib/format/rome.ts';
import type { DeadlinesQuery } from './deadlines.svelte.ts';
import type { BadgeInput } from './hub.ts';
import { shared } from './shared.svelte.ts';

/**
 * What the hub flags besides the deadlines, read from the features that own it (and their cache
 * entries): the questionnaires to fill in, and today's library seat.
 */
export class ServiceAlerts {
	readonly #questionnaires = query(
		() => session.current && recordBookQuestionnairesQuery(session.current)
	);
	readonly #reservations = query(() => {
		const scope = session.current;
		const link = scope && libraryLinks.of(scope.account);
		return scope && link && myReservationsQuery(scope, link);
	});

	/** Questionnaires not started or half done; undefined while unknown. */
	readonly questionnaires: number | undefined = $derived(
		this.#questionnaires.data &&
			toActivities(this.#questionnaires.data).filter((activity) => isPending(activity.status))
				.length
	);

	/** The first seat booked for today (Rome's day) that hasn't ended. */
	readonly seat: Reservation | undefined = $derived.by(() => {
		const wires = this.#reservations.data;
		if (wires === undefined) return undefined;
		const time = now.current.getTime();
		const day = romeDay(time);
		return activeReservations(wires, time).find((seat) => romeDay(seat.startsAt) === day);
	});
}

const alerts = () => new ServiceAlerts();

/** The alerts a parent layout shares, or new ones; call it while a component initializes. */
export function useAlerts(): ServiceAlerts {
	return shared(alerts);
}

/** Everything a hub badge reads. Reactive where it is read, like its sources. */
export function badgeInput(deadlines: DeadlinesQuery, alerts: ServiceAlerts): BadgeInput {
	return {
		status: deadlines.status,
		services: deadlines.services,
		missing: deadlines.missing,
		questionnaires: alerts.questionnaires,
		seat: alerts.seat,
		now: now.current.getTime()
	};
}
