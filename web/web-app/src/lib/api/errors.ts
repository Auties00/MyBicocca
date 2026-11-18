import type { RelayErrorBody, RelayErrorCode } from '@mybicocca/relay/protocol';

/** The services a session signs into. */
export type AuthService = 'esse3' | 'esse3Web' | 'moodle' | 'moodleWeb';

/** The relay refused or failed a request itself; the upstream server never answered it. */
export class RelayError extends Error {
	override readonly name = 'RelayError';

	constructor(
		readonly code: RelayErrorCode,
		readonly status: number,
		message: string,
		/** The Moodle error code behind an `autologin_refused`. */
		readonly moodleCode?: string
	) {
		super(message);
	}

	static async from(response: Response): Promise<RelayError> {
		const body = (await response.json().catch(() => ({}))) as Partial<RelayErrorBody>;
		const code = (response.headers.get('x-relay-error') ??
			body.error ??
			'bad_request') as RelayErrorCode;
		return new RelayError(code, response.status, body.message ?? code, body.moodleCode);
	}
}

/** No response arrived: the device is offline, or DNS, TLS or the connection failed. */
export class NetworkError extends Error {
	override readonly name = 'NetworkError';
	/** Whether the device was offline when the request failed. */
	readonly offline: boolean;

	constructor(message: string, options: ErrorOptions & { offline?: boolean } = {}) {
		super(message, options);
		this.offline = options.offline ?? false;
	}
}

/** A SAML login that didn't complete. */
export class LoginError extends Error {
	override readonly name = 'LoginError';

	constructor(
		readonly service: AuthService,
		message: string,
		/** Whether the IdP refused the username and password, as opposed to the flow breaking. */
		readonly badCredentials = false
	) {
		super(message);
	}
}

/** A session can't be renewed without the user: no stored password, or the stored one was refused. */
export class ReauthRequiredError extends Error {
	override readonly name = 'ReauthRequiredError';

	constructor(
		readonly service: AuthService,
		message: string
	) {
		super(message);
	}
}
