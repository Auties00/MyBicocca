import { createAffluencesApi, type AffluencesApi } from '@mybicocca/data-api/affluences';
import {
	createEasyStaffApi,
	createEasyStaffPlanningApi,
	type EasyStaffApi,
	type EasyStaffPlanningApi
} from '@mybicocca/data-api/easystaff';
import {
	createKalturaApi,
	createMoodleWebApi,
	type KalturaApi,
	type MoodleWebApi
} from '@mybicocca/data-api/elearning';
import { CookieJar } from './cookies.ts';
import type { Transport } from './transport.ts';

/** The clients of services that know no user: one set per transport, shared by every account. */
export interface DeviceClients {
	/** Lecture videos; direct, never relayed. */
	kaltura: KalturaApi;
	/** Timetables, rooms, exams calendar and EasyBadge. */
	easyStaff: EasyStaffApi;
	/** Information desk appointments (portal 2), with its guest token in a jar of this browser. */
	planning: EasyStaffPlanningApi;
	/** Library seats; the account's auth objects come from the library feature. */
	affluences: AffluencesApi;
	/**
	 * Moodle's public pages, such as a course's `course/info.php`, read as a visitor: no account's
	 * session, so no sign-in ever runs for them.
	 */
	moodleWebPublic: MoodleWebApi;
}

const built = new WeakMap<Transport, DeviceClients>();

export function deviceClients(transport: Transport): DeviceClients {
	let clients = built.get(transport);
	if (clients === undefined) {
		const send = transport.fetch({ timeoutMs: 30_000 });
		clients = {
			kaltura: createKalturaApi({ fetch: send }),
			easyStaff: createEasyStaffApi({ fetch: send }),
			planning: createEasyStaffPlanningApi(
				{ portalId: 2, portalCode: 'unimib-segreteria' },
				{ fetch: transport.fetch({ jar: new CookieJar(), timeoutMs: 30_000 }) }
			),
			affluences: createAffluencesApi({ fetch: send }),
			moodleWebPublic: createMoodleWebApi({
				fetch: transport.fetch({ jar: new CookieJar(), redirect: 'follow', timeoutMs: 30_000 })
			})
		};
		built.set(transport, clients);
	}
	return clients;
}
