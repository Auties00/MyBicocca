import { m } from '#lib/paraglide/messages.js';
import type { DataError } from './errors.ts';

/**
 * What to tell the user about `error`: the kind of failure, and the service's own explanation of
 * a refusal only where it speaks the user's language: Moodle's web services answer in the language
 * the app asks for. Esse3's are Italian-only and often technical: a feature that knows its
 * refusals reads `message` from the error itself.
 */
export function errorMessage(error: DataError): string {
	switch (error.kind) {
		case 'offline':
			return m.common_error_offline();
		case 'timeout':
			return m.common_error_timeout();
		case 'network':
			return m.common_error_network();
		case 'unavailable':
			return m.common_error_unavailable();
		case 'auth':
			return m.common_error_session_expired();
		case 'forbidden':
			return m.common_error_forbidden();
		case 'not_found':
			return m.common_error_not_found();
		case 'rejected':
			return error.service === 'moodle' && error.message !== ''
				? m.common_error_rejected_reason({ reason: error.message })
				: m.common_error_rejected();
		case 'no_file':
			return m.common_error_no_file();
		case 'decode':
			return m.common_error_site_changed();
		case 'relay':
		case 'unknown':
			return m.common_error_unexpected();
	}
}
