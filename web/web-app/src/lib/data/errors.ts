import type { RelayErrorCode } from '@mybicocca/relay/protocol';
import { AffluencesError } from '@mybicocca/data-api/affluences';
import { EasyStaffError, EasyStaffPlanningError } from '@mybicocca/data-api/easystaff';
import { KalturaError, MoodleError, MoodleWebError } from '@mybicocca/data-api/elearning';
import { Esse3Error } from '@mybicocca/data-api/esse3';
import { DecodeError, ScrapeError } from '@mybicocca/data-api/runtime';
import { refusesCredentials } from '#lib/api/auth/esse3Token.ts';
import { LoginError, NetworkError, ReauthRequiredError, RelayError } from '#lib/api/errors.ts';

export type Service =
	| 'esse3'
	| 'esse3Web'
	| 'moodle'
	| 'moodleWeb'
	| 'kaltura'
	| 'easyStaff'
	| 'planning'
	| 'affluences';

/** Every failure the data layer reports, by what the UI can say or do about it. */
export type DataError =
	/** The device is offline. */
	| { kind: 'offline' }
	/** No answer while online: DNS, TLS, a dropped connection. */
	| { kind: 'network' }
	| { kind: 'timeout' }
	/** The relay refused the request itself; upstream never saw it. */
	| { kind: 'relay'; code: RelayErrorCode; status: number }
	/** Only the user can restore access: wrong password, or a session that couldn't be renewed. */
	| {
			kind: 'auth';
			service: Service;
			reason: 'bad_credentials' | 'reauth_required' | 'session_expired';
	  }
	/** `message` is the service's own explanation, when it gave one. */
	| { kind: 'forbidden'; service: Service; code?: string; message?: string }
	| { kind: 'not_found'; service: Service }
	/**
	 * A business error, such as a closed exam booking. `message` is the service's own explanation
	 * as it sent it (Esse3's `retErrMsg` in Italian, Moodle's in the app's language), or '' when it
	 * sent none.
	 */
	| { kind: 'rejected'; service: Service; status: number; code?: string; message: string }
	/** A 5xx, a maintenance page, or a login chain that broke (status 0). */
	| { kind: 'unavailable'; service: Service; status: number }
	/** A download answered with a page that is neither the file nor the sign-in page. */
	| { kind: 'no_file'; service: Service }
	/** The answer didn't have the expected shape: the site likely changed. */
	| { kind: 'decode'; detail: string }
	/** A bug: anything else. */
	| { kind: 'unknown'; detail: string };

/** Carries a {@link DataError} where an `Error` must be thrown, such as from `mutate`. */
export class DataFailure extends Error {
	override readonly name = 'DataFailure';

	constructor(
		readonly error: DataError,
		options?: ErrorOptions
	) {
		super(error.kind, options);
	}
}

/** The {@link DataError} for anything a load or a mutation threw. */
export function toDataError(error: unknown): DataError {
	if (error instanceof DataFailure) return error.error;
	if (refusesCredentials(error))
		return { kind: 'auth', service: 'esse3', reason: 'reauth_required' };
	if (error instanceof Esse3Error) return fromStatus('esse3', error);
	if (error instanceof MoodleError) return fromMoodle(error);
	if (error instanceof MoodleWebError) return fromStatus('moodleWeb', error);
	if (error instanceof KalturaError) return fromStatus('kaltura', error);
	if (error instanceof EasyStaffPlanningError) return fromPlanning(error);
	if (error instanceof EasyStaffError) return fromStatus('easyStaff', error);
	if (error instanceof AffluencesError) return fromAffluences(error);
	// A login chain that broke for another reason than the password: the service can't be
	// signed into right now, which the user can't fix by typing it again.
	if (error instanceof LoginError) {
		return error.badCredentials
			? { kind: 'auth', service: error.service, reason: 'bad_credentials' }
			: { kind: 'unavailable', service: error.service, status: 0 };
	}
	if (error instanceof ReauthRequiredError) {
		return { kind: 'auth', service: error.service, reason: 'reauth_required' };
	}
	if (error instanceof RelayError) {
		if (error.code === 'upstream_timeout') return { kind: 'timeout' };
		if (error.code === 'upstream_unreachable') return offlineOr({ kind: 'network' });
		return { kind: 'relay', code: error.code, status: error.status };
	}
	if (error instanceof NetworkError) {
		return error.offline ? { kind: 'offline' } : offlineOr({ kind: 'network' });
	}
	if (error instanceof DOMException && error.name === 'TimeoutError') return { kind: 'timeout' };
	if (
		error instanceof ScrapeError ||
		error instanceof DecodeError ||
		error instanceof SyntaxError
	) {
		return { kind: 'decode', detail: error.message };
	}
	return {
		kind: 'unknown',
		detail: error instanceof Error ? `${error.name}: ${error.message}` : String(error)
	};
}

/** What the SDK's service errors share: an HTTP status, the body's code, and its message. */
interface ServiceError {
	readonly status: number;
	readonly code: string | undefined;
	readonly serverMessage: string | undefined;
}

/** The server's own message (Esse3's `retErrMsg`, Moodle's exception message), or '' when it sent none. */
export function serverMessage(error: ServiceError): string {
	return error.serverMessage?.trim() ?? '';
}

function fromStatus(service: Service, error: ServiceError): DataError {
	const { status } = error;
	if (status === 401) return { kind: 'auth', service, reason: 'reauth_required' };
	if (status === 403) return forbidden(service, error);
	if (status === 404) return { kind: 'not_found', service };
	if (status >= 500) return { kind: 'unavailable', service, status };
	return rejected(service, error);
}

function forbidden(service: Service, error: ServiceError): DataError {
	const message = serverMessage(error);
	return {
		kind: 'forbidden',
		service,
		...(error.code === undefined ? {} : { code: error.code }),
		...(message === '' ? {} : { message })
	};
}

function rejected(service: Service, error: ServiceError): DataError {
	return {
		kind: 'rejected',
		service,
		status: error.status,
		...(error.code === undefined ? {} : { code: error.code }),
		message: serverMessage(error)
	};
}

/** Moodle answers errors with status 200 and an error code. */
function fromMoodle(error: MoodleError): DataError {
	switch (error.code) {
		case MoodleError.InvalidToken:
			return { kind: 'auth', service: 'moodle', reason: 'reauth_required' };
		case 'requireloginerror':
			return { kind: 'auth', service: 'moodle', reason: 'session_expired' };
		case MoodleError.AccessException:
		case 'nopermissions':
		case 'requireloginerror_guest':
			return forbidden('moodle', error);
		default:
			return error.status >= 500 || SERVER_FAILURES.test(error.code ?? '')
				? { kind: 'unavailable', service: 'moodle', status: error.status }
				: rejected('moodle', error);
	}
}

/** Moodle's exceptions for its own failures (database, connections), as opposed to refusals. */
const SERVER_FAILURES = /^(dml\w*exception|dbconnectionfailed)$/;

function fromPlanning(error: EasyStaffPlanningError): DataError {
	if (
		error.code === EasyStaffPlanningError.NotFound ||
		error.code === EasyStaffPlanningError.ReservationNotFound
	) {
		return { kind: 'not_found', service: 'planning' };
	}
	return fromStatus('planning', error);
}

function fromAffluences(error: AffluencesError): DataError {
	if (error.status === 401 || error.code === AffluencesError.MissingAuthToken) {
		return { kind: 'auth', service: 'affluences', reason: 'reauth_required' };
	}
	if (error.code === AffluencesError.NotValidatedYet)
		return { kind: 'not_found', service: 'affluences' };
	return fromStatus('affluences', error);
}

function offlineOr(error: DataError): DataError {
	return typeof navigator !== 'undefined' && !navigator.onLine ? { kind: 'offline' } : error;
}
