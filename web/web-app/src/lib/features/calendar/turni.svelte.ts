import { createContext } from 'svelte';
import type { CalendarEvent } from './model.ts';

/** The turno followed in a course, or `all` once the student chose to see every turno. */
export type TurnoChoice = number | 'all';

function isChoice(value: unknown): value is TurnoChoice {
	return value === 'all' || (typeof value === 'number' && Number.isInteger(value) && value > 0);
}

/**
 * The turno the student follows in each course EasyStaff splits into turni. Neither Esse3 nor
 * EasyStaff records it, so the student picks it on the calendar; it is kept on this browser, per
 * career, and lessons of the other turni are hidden.
 */
export class TurnoChoices {
	#chosen = $state.raw<Readonly<Record<string, TurnoChoice>>>({});
	readonly #key: string;

	constructor(careerId: number) {
		this.#key = `mybicocca:calendar-turni:${careerId}`;
		try {
			const saved: unknown = JSON.parse(localStorage.getItem(this.#key) ?? '{}');
			if (saved && typeof saved === 'object') {
				this.#chosen = Object.fromEntries(
					Object.entries(saved).filter((entry): entry is [string, TurnoChoice] =>
						isChoice(entry[1])
					)
				);
			}
		} catch {
			/* Nothing kept, or private mode: every turno shows. */
		}
	}

	/** The choice made for the course with `activityCode`; undefined until the student makes one. */
	of(activityCode: string | null): TurnoChoice | undefined {
		return activityCode === null ? undefined : this.#chosen[activityCode];
	}

	/** Records a choice for a course; undefined forgets it, so the calendar asks again. */
	choose(activityCode: string, choice: TurnoChoice | undefined): void {
		const next = { ...this.#chosen };
		if (choice === undefined) delete next[activityCode];
		else next[activityCode] = choice;
		this.#chosen = next;
		try {
			localStorage.setItem(this.#key, JSON.stringify(next));
		} catch {
			/* Kept for this visit only. */
		}
	}

	/** Whether `event` belongs to a turno the student doesn't follow. */
	hides(event: CalendarEvent): boolean {
		if (event.source !== 'lesson') return false;
		const chosen = this.of(event.activityCode);
		return typeof chosen === 'number' && event.turno !== null && event.turno !== chosen;
	}
}

export interface TurnoPicker {
	/** The turni a course's lessons come in, when more than one. */
	available(activityCode: string | null): readonly number[];
	chosen(activityCode: string | null): TurnoChoice | undefined;
	choose(activityCode: string, choice: TurnoChoice): void;
}

export const [getTurnoPicker, setTurnoPicker, hasTurnoPicker] = createContext<TurnoPicker>();
