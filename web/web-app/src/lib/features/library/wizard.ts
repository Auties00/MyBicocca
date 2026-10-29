import { isIsoDate, type IsoDate } from '#lib/format/days.ts';
import { clockText, parseClock } from './model.ts';

/**
 * The booking's choices, kept in the URL so each step is a page: Back goes one step back, and a
 * reload or a shared link lands on the same step. A choice is dropped when one before it changes.
 */
export interface WizardChoices {
	zone?: number;
	day?: IsoDate;
	duration?: number;
	/** Minutes of the day. */
	start?: number;
	seat?: number;
}

export type WizardStep = 'zone' | 'when' | 'seat' | 'confirm';

export const WIZARD_STEPS: readonly WizardStep[] = ['zone', 'when', 'seat', 'confirm'];

function positive(text: string | null): number | undefined {
	const value = Number(text);
	return text && Number.isSafeInteger(value) && value > 0 ? value : undefined;
}

export function parseChoices(params: { get(name: string): string | null }): WizardChoices {
	const zone = positive(params.get('zone'));
	if (zone === undefined) return {};
	const day = params.get('day');
	const duration = positive(params.get('duration'));
	const start = parseClock(params.get('time'));
	const seat = positive(params.get('seat'));
	const when = isIsoDate(day) && duration !== undefined;
	return {
		zone,
		...(isIsoDate(day) ? { day } : {}),
		...(duration === undefined ? {} : { duration }),
		...(when && start !== undefined ? { start } : {}),
		...(when && start !== undefined && seat !== undefined ? { seat } : {})
	};
}

export function stepOf(choices: WizardChoices): WizardStep {
	if (choices.zone === undefined) return 'zone';
	if (choices.start === undefined) return 'when';
	return choices.seat === undefined ? 'seat' : 'confirm';
}

export function choicesQuery(choices: WizardChoices): string {
	const params = new URLSearchParams();
	if (choices.zone !== undefined) params.set('zone', String(choices.zone));
	if (choices.day) params.set('day', choices.day);
	if (choices.duration !== undefined) params.set('duration', String(choices.duration));
	if (choices.start !== undefined) params.set('time', clockText(choices.start));
	if (choices.seat !== undefined) params.set('seat', String(choices.seat));
	const query = params.toString();
	return query ? `?${query}` : '';
}

/**
 * The choices for going back to `step`: its own choice and those after it are dropped, except
 * that the day and time step keeps its day and duration, so only the start is picked again.
 */
export function choicesBefore(choices: WizardChoices, step: WizardStep): WizardChoices {
	switch (step) {
		case 'zone':
			return {};
		case 'when':
			return { zone: choices.zone, day: choices.day, duration: choices.duration };
		case 'seat':
			return { ...choices, seat: undefined };
		case 'confirm':
			return choices;
	}
}
