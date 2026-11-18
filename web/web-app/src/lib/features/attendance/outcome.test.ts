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
		sessions: [],
		...overrides
	});
	const SESSION = { id: '9', time: 'mer 18 nov 2026 2PM - 4PM' };
	const item = (markActions: { sessionId: string }[], headings: string[], time = SESSION.time) => ({
		time,
		markActions,
		headings
	});
	const messages = (...keys: string[]) => keys.map((key) => ({ key }));

	it('ranks the blocking messages as the plugin does', () => {
		expect(
			markOutcome(page({ messages: messages('subnetwrong', 'incorrectpasswordshort') }), SESSION)
		).toEqual({
			kind: 'wrong-credential',
			message: null
		});
		expect(
			markOutcome(page({ messages: messages('subnetwrong', 'preventsharederror') }), SESSION)
		).toEqual({
			kind: 'device-already-used'
		});
		expect(markOutcome(page({ messages: messages('subnetwrong') }), SESSION)).toEqual({
			kind: 'network-restricted'
		});
		expect(markOutcome(page({ messages: messages('invalidstatus') }), SESSION)).toMatchObject({
			kind: 'failed'
		});
	});

	it('reads a session that kept its button as closed', () => {
		expect(
			markOutcome(page({ sessions: [item([{ sessionId: '9' }], ['Gruppo A'])] }), SESSION)
		).toEqual({ kind: 'not-open', reason: 'closed' });
	});

	it("records the status of the last heading of the session's own item", () => {
		expect(
			markOutcome(
				page({
					sessions: [
						item([{ sessionId: '10' }], ['Gruppo B'], 'mer 18 nov 2026 4PM - 6PM'),
						item([], ['Gruppo A', ' Presente '], ' mer 18 nov 2026  2PM - 4PM ')
					]
				}),
				SESSION
			)
		).toEqual({ kind: 'recorded', status: 'Presente', message: null });
	});

	it('does not confirm a marking whose session the page leaves out, or that shows no status', () => {
		expect(markOutcome(page(), SESSION)).toMatchObject({ kind: 'failed' });
		expect(markOutcome(page({ sessions: [item([], [' '])] }), SESSION)).toMatchObject({
			kind: 'failed'
		});
	});
});
