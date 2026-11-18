import { Esse3Error } from '@mybicocca/data-api/esse3';
import { ApiFailure } from '@mybicocca/data-api/runtime';
import { describe, expect, it } from 'vitest';
import { DataFailure, toDataError } from '#lib/data/errors.ts';
import { mentionsQuestionnaire, refusalReason } from './writes.ts';

const esse3 = (status: number, message: string, code?: string) =>
	new Esse3Error(new ApiFailure(status, code, message, message));

/* How `mutate` hands a failed write to the page. */
const failed = (error: unknown) => new DataFailure(toDataError(error), { cause: error });

describe('refusalReason', () => {
	it('keeps Esse3’s own words for a business refusal', () => {
		const error = failed(esse3(422, "Le iscrizioni all'appello non sono aperte.", '-1'));
		expect(refusalReason(error)).toBe("Le iscrizioni all'appello non sono aperte.");
	});

	it('has nothing to say for outages, expired sessions or bare statuses', () => {
		expect(refusalReason(failed(esse3(503, 'Servizio non disponibile')))).toBeUndefined();
		expect(refusalReason(failed(esse3(401, 'Unauthorized')))).toBeUndefined();
		expect(
			refusalReason(
				failed(new Esse3Error(new ApiFailure(422, undefined, 'Request failed with status 422')))
			)
		).toBeUndefined();
		expect(refusalReason(failed(new TypeError('Failed to fetch')))).toBeUndefined();
	});

	it('spots a refusal that asks for the teaching questionnaire', () => {
		const error = failed(
			esse3(422, 'Per prenotarsi è necessario compilare il questionario di valutazione.')
		);
		expect(mentionsQuestionnaire(error)).toBe(true);
		expect(mentionsQuestionnaire(failed(esse3(422, 'Appello chiuso.')))).toBe(false);
	});
});
