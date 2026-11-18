import type { MoodleWebAttendanceMarkPage } from '@mybicocca/data-api/elearning';
import { describe, expect, it } from 'vitest';
import { certifyOutcome, markOutcome } from './outcome.ts';

describe('certifyOutcome', () => {
	it('records an ok, with EasyBadge words when it sends any', () => {
		expect(certifyOutcome({ result: 'ok', message: ' Presenza registrata ' })).toEqual({
			kind: 'recorded',
			status: null,
			message: 'Presenza registrata'
		});
		expect(certifyOutcome({ result: 'ok', message: '' })).toMatchObject({ message: null });
	});

	it('reads a refusal about the code as a wrong code, any other as a failure', () => {
		expect(certifyOutcome({ result: 'failure', message: 'Codice lezione non valido' })).toEqual({
			kind: 'wrong-credential',
			message: 'Codice lezione non valido'
		});
		expect(certifyOutcome({ result: 'failure', message: 'Lezione terminata' })).toEqual({
			kind: 'failed',
			reason: 'generic',
			message: 'Lezione terminata'
		});
	});
});

describe('markOutcome', () => {
	const page = (
		overrides: Partial<MoodleWebAttendanceMarkPage> = {}
	): MoodleWebAttendanceMarkPage => ({
		messages: [],
		markActions: [],
		headings: [],
		...overrides
	});
	const messages = (...keys: string[]) => keys.map((key) => ({ key }));

	it('ranks the blocking messages as the plugin does', () => {
		expect(
			markOutcome(page({ messages: messages('subnetwrong', 'incorrectpasswordshort') }), '9')
		).toEqual({
			kind: 'wrong-credential',
			message: null
		});
		expect(
			markOutcome(page({ messages: messages('subnetwrong', 'preventsharederror') }), '9')
		).toEqual({
			kind: 'device-already-used'
		});
		expect(markOutcome(page({ messages: messages('subnetwrong') }), '9')).toEqual({
			kind: 'network-restricted'
		});
		expect(markOutcome(page({ messages: messages('invalidstatus') }), '9')).toMatchObject({
			kind: 'failed'
		});
	});

	it('reads a session that kept its button as closed', () => {
		expect(
			markOutcome(page({ markActions: [{ sessionId: '9' }], headings: ['Gruppo A'] }), '9')
		).toEqual({ kind: 'not-open', reason: 'closed' });
	});

	it('records the status of the last heading, whatever other sessions offer', () => {
		expect(
			markOutcome(
				page({ markActions: [{ sessionId: '10' }], headings: ['Gruppo A', ' Presente '] }),
				'9'
			)
		).toEqual({ kind: 'recorded', status: 'Presente', message: null });
		expect(markOutcome(page(), '9')).toEqual({ kind: 'recorded', status: null, message: null });
	});
});
