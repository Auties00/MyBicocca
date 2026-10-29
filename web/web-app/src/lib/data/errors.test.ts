import { AffluencesError } from '@mybicocca/data-api/affluences';
import { EasyStaffPlanningError } from '@mybicocca/data-api/easystaff';
import { KalturaError, MoodleError } from '@mybicocca/data-api/elearning';
import { createEsse3Api, Esse3Error } from '@mybicocca/data-api/esse3';
import { ApiFailure, ScrapeError } from '@mybicocca/data-api/runtime';
import { describe, expect, it } from 'vitest';
import { LoginError, NetworkError, ReauthRequiredError, RelayError } from '#lib/api/errors.ts';
import { DataFailure, serverMessage, toDataError } from './errors.ts';

/** A failure as the SDK builds it: the server's message, else its code, else the status. */
const failure = (status: number, code?: string, serverMessage?: string) =>
	new ApiFailure(
		status,
		code,
		serverMessage ?? code ?? `Request failed with status ${status}`,
		serverMessage
	);

describe('serverMessage', () => {
	it("is the server's own message", () => {
		expect(serverMessage(new Esse3Error(failure(422, '-1', 'Regola 3: non rispettata')))).toBe(
			'Regola 3: non rispettata'
		);
		expect(serverMessage(new MoodleError(failure(200, undefined, ' Chiave errata ')))).toBe(
			'Chiave errata'
		);
	});

	it('is empty when the server sent none', async () => {
		expect(serverMessage(new Esse3Error(failure(422, '-1')))).toBe('');
		const esse3 = createEsse3Api({ fetch: async () => new Response('', { status: 422 }) });
		const error: unknown = await esse3.careers.getCareers().catch((caught: unknown) => caught);
		expect(error).toBeInstanceOf(Esse3Error);
		expect(serverMessage(error as Esse3Error)).toBe('');
	});
});

describe('toDataError', () => {
	it('maps Esse3 statuses', () => {
		expect(toDataError(new Esse3Error(failure(401)))).toEqual({
			kind: 'auth',
			service: 'esse3',
			reason: 'reauth_required'
		});
		expect(toDataError(new Esse3Error(failure(404)))).toEqual({
			kind: 'not_found',
			service: 'esse3'
		});
		expect(toDataError(new Esse3Error(failure(503)))).toEqual({
			kind: 'unavailable',
			service: 'esse3',
			status: 503
		});
		expect(toDataError(new Esse3Error(failure(422, '-1', 'Appello chiuso')))).toEqual({
			kind: 'rejected',
			service: 'esse3',
			status: 422,
			code: '-1',
			message: 'Appello chiuso'
		});
		expect(toDataError(new Esse3Error(failure(403, undefined, 'Security failed')))).toEqual({
			kind: 'forbidden',
			service: 'esse3',
			message: 'Security failed'
		});
	});

	it('maps Moodle error codes, which come with status 200', () => {
		expect(toDataError(new MoodleError(failure(200, 'invalidtoken')))).toMatchObject({
			kind: 'auth',
			reason: 'reauth_required'
		});
		expect(toDataError(new MoodleError(failure(200, 'accessexception')))).toEqual({
			kind: 'forbidden',
			service: 'moodle',
			code: 'accessexception'
		});
		expect(
			toDataError(new MoodleError(failure(200, 'canntenrol', 'Iscrizione disabilitata')))
		).toEqual({
			kind: 'rejected',
			service: 'moodle',
			status: 200,
			code: 'canntenrol',
			message: 'Iscrizione disabilitata'
		});
		expect(toDataError(new MoodleError(failure(200, 'dmlreadexception')))).toEqual({
			kind: 'unavailable',
			service: 'moodle',
			status: 200
		});
	});

	it('maps the not-found codes and logouts of the other services', () => {
		expect(toDataError(new EasyStaffPlanningError(failure(400, 'reservation_not_found')))).toEqual({
			kind: 'not_found',
			service: 'planning'
		});
		expect(toDataError(new KalturaError(failure(404)))).toEqual({
			kind: 'not_found',
			service: 'kaltura'
		});
		expect(toDataError(new AffluencesError(failure(400, 'missing_auth_token')))).toMatchObject({
			kind: 'auth',
			service: 'affluences'
		});
	});

	it('maps login and renewal failures to auth errors', () => {
		expect(toDataError(new LoginError('moodle', 'x', true))).toEqual({
			kind: 'auth',
			service: 'moodle',
			reason: 'bad_credentials'
		});
		expect(toDataError(new LoginError('moodle', 'Missing login button'))).toEqual({
			kind: 'unavailable',
			service: 'moodle',
			status: 0
		});
		expect(toDataError(new ReauthRequiredError('esse3Web', 'x'))).toMatchObject({
			reason: 'reauth_required'
		});
	});

	it('maps transport failures', () => {
		expect(toDataError(new RelayError('path_not_allowed', 403, 'x'))).toEqual({
			kind: 'relay',
			code: 'path_not_allowed',
			status: 403
		});
		expect(toDataError(new RelayError('upstream_timeout', 504, 'x'))).toEqual({ kind: 'timeout' });
		expect(toDataError(new DOMException('x', 'TimeoutError'))).toEqual({ kind: 'timeout' });
		expect(toDataError(new NetworkError('Failed to fetch')).kind).toMatch(/^(network|offline)$/);
		expect(toDataError(new NetworkError('Failed to fetch', { offline: true }))).toEqual({
			kind: 'offline'
		});
		expect(toDataError(new ScrapeError('x'))).toEqual({ kind: 'decode', detail: 'x' });
	});

	it('unwraps a DataFailure and reports anything else as unknown', () => {
		expect(toDataError(new DataFailure({ kind: 'offline' }))).toEqual({ kind: 'offline' });
		expect(toDataError(new TypeError('x is not a function'))).toEqual({
			kind: 'unknown',
			detail: 'TypeError: x is not a function'
		});
	});
});
