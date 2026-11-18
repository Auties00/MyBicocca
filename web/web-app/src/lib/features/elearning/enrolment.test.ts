import { describe, expect, it } from 'vitest';
import type { DataError } from '#lib/data/errors.ts';
import { errorMessage } from '#lib/data/errorMessage.ts';
import { enrolFailure, needsKey } from './enrolment.ts';

const warning = (code: string | undefined, message = ''): DataError => ({
	kind: 'rejected',
	service: 'moodle',
	status: 200,
	...(code === undefined ? {} : { code }),
	message
});

describe('needsKey', () => {
	it('asks for a key on the warnings about a missing or wrong one', () => {
		expect(['2', '3', '4'].map((code) => needsKey(warning(code)))).toEqual([true, true, true]);
	});

	it('leaves other refusals and failures alone', () => {
		expect(needsKey(warning('1', 'Sei già iscritto a questo corso'))).toBe(false);
		expect(needsKey(warning(undefined, 'Iscrizione disabilitata'))).toBe(false);
		expect(needsKey({ kind: 'offline' })).toBe(false);
		expect(needsKey(undefined)).toBe(false);
	});
});

describe('enrolFailure', () => {
	it("shows Moodle's own reason as it is", () => {
		expect(enrolFailure(warning('canntenrol', 'Iscrizione disabilitata o non attiva'))).toBe(
			'Iscrizione disabilitata o non attiva'
		);
		expect(enrolFailure(warning('4', ' Chiave di iscrizione errata, riprova '))).toBe(
			'Chiave di iscrizione errata, riprova'
		);
	});

	it('falls back to the generic message without a reason', () => {
		const empty = warning('4', '  ');
		expect(enrolFailure(empty)).toBe(errorMessage(empty));
		expect(enrolFailure({ kind: 'offline' })).toBe(errorMessage({ kind: 'offline' }));
	});
});
