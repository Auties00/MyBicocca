import type { MoodleSiteInfo } from '@mybicocca/data-api/elearning';
import type { Esse3Career, Esse3CareerPortion, Esse3User } from '@mybicocca/data-api/esse3';
import { personName } from '#lib/format/names.ts';

export type CareerStatus =
	'active' | 'suspended' | 'provisional' | 'graduated' | 'interrupted' | 'other';

export type CareerLevel = 'bachelor' | 'master';

/** One of the student's careers in Esse3, with the ids each Esse3 operation takes. */
export interface Career {
	/** `stuId`: the career id. */
	id: number;
	/** `matId`: the career segment id most record-book (`libretti`) calls take. */
	enrollmentTraitId: number;
	/** `cdsId`: the degree programme. */
	programId: number;
	/** `p06CdsCod`: the programme code EasyStaff knows it by, such as `E3101Q`. */
	easyStaffProgramCode: string | null;
	/** `aaIscrId`. */
	academicYearEnrollmentId: number;
	/** `matricola`. */
	studentNumber: string;
	description: string;
	/** The academic year the career started (its first year's September). */
	academicYear: number;
	status: CareerStatus;
	level: CareerLevel | null;
}

/** The student's Moodle identity. */
export interface LearningIdentity {
	userId: number;
	username: string;
	lang: string;
	isSiteAdmin: boolean;
	maxUploadBytes: number;
	quotaBytes: number;
}

/** A signed-in university account. */
export interface Account {
	/** A random id, kept when the same Esse3 user signs in again. */
	id: string;
	/** The full e-mail form, as Esse3 and the IdP take it. */
	username: string;
	displayName: string;
	/** Esse3's `userId`, which identifies the person across sign-ins. */
	recordUserId: string;
	/** `persId`: personal data and photo calls take it. */
	personId: number;
	fiscalCode: string | null;
	careers: Career[];
	selectedCareerId: number;
	/** Null until a Moodle login succeeds; `api.moodle` hands the identity to its calls. */
	learning: LearningIdentity | null;
	createdAt: number;
	lastUsedAt: number;
	lastSyncedAt: number;
}

export type AccountEvent =
	/** The stored credentials stopped working, or none are stored; only the user can sign in again. */
	| { type: 'reauth_required'; accountId: string }
	/** A career refresh found an open career that wasn't stored, such as a new enrollment. */
	| { type: 'new_career'; accountId: string; career: Career }
	/** The selected career is gone from Esse3; another one must be picked. */
	| { type: 'selected_career_missing'; accountId: string }
	/** The selected career is no longer open, such as after graduating. */
	| { type: 'selected_career_ended'; accountId: string; career: Career };

const DEFAULT_DOMAIN = 'campus.unimib.it';

/**
 * The username as Esse3 Basic auth and the IdP's `j_username` take it: students type the short
 * form, so the campus domain is appended when there is no `@`; any other address passes as typed.
 */
export function normalizeUsername(raw: string): string {
	const trimmed = raw.trim();
	return trimmed.includes('@') ? trimmed : `${trimmed}@${DEFAULT_DOMAIN}`;
}

/** The user's name as people write it: Esse3 sends it in capitals ("MARIA D'ANGELO"). */
export function displayNameOf(user: Esse3User): string {
	const name = [user.firstName.trim(), user.lastName.trim()]
		.filter((part) => part !== '')
		.join(' ');
	return name === '' ? user.userId : personName(name);
}

/**
 * The career status for Esse3's `staStuCod`. Esse3 documents A (active), S (suspended) and
 * I (provisional, "ipotesi"); the other codes are guesses kept until real data says otherwise.
 */
export function careerStatus(code: string | null): CareerStatus {
	switch (code?.toUpperCase()) {
		case 'A':
		case 'ATT':
		case 'ATTIVA':
			return 'active';
		case 'S':
		case 'SOS':
		case 'SOSPESA':
			return 'suspended';
		case 'I':
			return 'provisional';
		case 'L':
		case 'LAU':
		case 'LAUREATO':
		case 'LAUREATA':
			return 'graduated';
		case 'INT':
		case 'T':
		case 'TRA':
		case 'R':
		case 'RIN':
			return 'interrupted';
		default:
			return 'other';
	}
}

/** The degree level for Esse3's `tipoCorsoCod`: L2 bachelor, LM/LM5/LM6 master. */
export function careerLevel(code: string | null): CareerLevel | null {
	switch (code?.trim().toUpperCase()) {
		case 'L2':
			return 'bachelor';
		case 'LM':
		case 'LM5':
		case 'LM6':
			return 'master';
		default:
			return null;
	}
}

/** Whether a career can be picked to scope the app. */
export function isOpen(status: CareerStatus): boolean {
	return status === 'active' || status === 'suspended' || status === 'provisional';
}

/**
 * The careers of the student; `segments` (the record books, which the careers endpoint lacks)
 * give each one its level.
 */
export function toCareers(
	careers: readonly Esse3Career[],
	segments: readonly Esse3CareerPortion[]
): Career[] {
	const courseTypes = new Map(
		segments.flatMap((segment) =>
			segment.studentId !== null && segment.courseTypeCode !== null
				? [[segment.studentId, segment.courseTypeCode] as const]
				: []
		)
	);
	return careers.map((career) => ({
		id: career.studentId ?? 0,
		enrollmentTraitId: career.matId ?? 0,
		programId: career.courseOfStudyId ?? 0,
		easyStaffProgramCode: career.p06CourseOfStudyCode,
		academicYearEnrollmentId: career.academicYearEnrollmentId ?? 0,
		studentNumber: career.matricola ?? '',
		description: career.p06CourseOfStudyDescription ?? career.studentStatesDescription ?? '',
		academicYear: career.academicYearImm1 ?? career.academicYearId ?? 0,
		status: careerStatus(career.studentStatusCode),
		level: careerLevel(courseTypes.get(career.studentId ?? 0) ?? null)
	}));
}

/** The career selected by default: the open one started most recently, else the latest overall. */
export function defaultCareerId(careers: readonly Career[]): number {
	const open = careers.filter((career) => isOpen(career.status));
	const pool = open.length > 0 ? open : careers;
	return pool.reduce(
		(best, career) => (career.academicYear > best.academicYear ? career : best),
		pool[0]
	).id;
}

/** Whether the student must pick a career: more than one is open. */
export function needsCareerPick(careers: readonly Career[]): boolean {
	return careers.filter((career) => isOpen(career.status)).length > 1;
}

/** What changed between the stored careers and a fresh Esse3 snapshot, as account events. */
export function reconcileCareers(
	accountId: string,
	previous: readonly Career[],
	current: readonly Career[],
	selectedId: number
): AccountEvent[] {
	const known = new Set(previous.map((career) => career.id));
	const events: AccountEvent[] = current
		.filter((career) => !known.has(career.id) && isOpen(career.status))
		.map((career) => ({ type: 'new_career', accountId, career }));
	const selected = current.find((career) => career.id === selectedId);
	if (selected === undefined) events.push({ type: 'selected_career_missing', accountId });
	else if (!isOpen(selected.status))
		events.push({ type: 'selected_career_ended', accountId, career: selected });
	return events;
}

export function learningIdentity(site: MoodleSiteInfo): LearningIdentity {
	return {
		userId: site.userId,
		username: site.username,
		lang: site.lang,
		isSiteAdmin: site.userIsSiteAdmin === true,
		maxUploadBytes: site.userMaxUploadFileSize ?? 0,
		quotaBytes: site.userQuota ?? 0
	};
}
