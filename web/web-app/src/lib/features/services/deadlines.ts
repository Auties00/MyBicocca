import type {
	Esse3ExamSessionEnrollment,
	Esse3ExamSessionTranscript,
	Esse3Invoices,
	Esse3StudentDebit,
	Esse3StudyPlan,
	Esse3StudyPlanHeader
} from '@mybicocca/data-api/esse3';
import { esse3Date, esse3DateTime } from '#lib/data/esse3/dates.ts';
import { bookingGrade, outcomeGrade, type ExamGrade } from '#lib/data/esse3/grades.ts';
import { calendarDays, toDate } from '#lib/format/dates.ts';

/** Within this many days (or overdue) a deadline is urgent. */
export const URGENT_DAYS = 7;
export const LOOK_AHEAD_DAYS = 30;
/** Overdue fees stay listed this long. */
export const LOOK_BACK_DAYS = 7;

/** The kinds, in the order same-day deadlines list them. */
export const DEADLINE_SOURCES = ['result', 'fee', 'exam', 'booking'] as const;

export type DeadlineSource = (typeof DEADLINE_SOURCES)[number];

/** The kinds a `?kind=fee,exam` filter keeps; empty keeps all, unknown names are ignored. */
export function parseKinds(param: string | null): ReadonlySet<DeadlineSource> {
	const names = new Set(param?.split(','));
	return new Set(DEADLINE_SOURCES.filter((source) => names.has(source)));
}

interface DeadlineBase {
	/** Stable across reloads: the list's key. */
	id: string;
	/** The day, at local midnight. */
	date: Date;
	/** Calendar days from today; negative when overdue. */
	days: number;
	urgent: boolean;
	/** Esse3's name, often ALL CAPS; null when it has none (the UI names it by its kind). */
	title: string | null;
}

/** A published grade the student must accept or reject by `date`. */
export interface ResultDeadline extends DeadlineBase {
	source: 'result';
	/** The booking's id in the exam's list, which the results pages are keyed by. */
	applicationListId: number;
	grade: ExamGrade;
}

/** An invoice to pay by `date`, or overdue since. */
export interface FeeDeadline extends DeadlineBase {
	source: 'fee';
	invoiceId: number;
	amount: number;
	expired: boolean;
	/** "Terza rata", from the invoice's charges. */
	installment: string | null;
	/** The academic year it bills, for invoices without a name. */
	academicYear: number | null;
}

/** What addresses an exam call in Esse3 (`/appelli/{cdsId}/{adId}/{appId}`). */
export interface CallKey {
	courseOfStudyId: number;
	activityId: number;
	callId: number;
}

/** A booked exam, held on `date`. */
export interface ExamDeadline extends DeadlineBase {
	source: 'exam';
	key: CallKey;
	/** Start time, when Esse3 gives one (not its 00:00 filler). */
	time: Date | null;
	room: string | null;
}

/** The last day to book an exam's next call. */
export interface BookingDeadline extends DeadlineBase {
	source: 'booking';
	/** The call that closes first. */
	key: CallKey;
	code: string | null;
	/** The call the closing belongs to. */
	callDate: Date | null;
	/** When booking opens, if it hasn't yet. */
	opensAt: Date | null;
}

export type Deadline = ResultDeadline | FeeDeadline | ExamDeadline | BookingDeadline;

/** What the merge reads; a source still missing adds nothing. */
export interface DeadlineSources {
	results?: readonly Esse3ExamSessionEnrollment[];
	invoices?: readonly Esse3Invoices[];
	charges?: readonly Esse3StudentDebit[];
	bookings?: readonly Esse3ExamSessionEnrollment[];
	calls?: readonly Esse3ExamSessionTranscript[];
	/** Undefined while unknown or failed, null for a career without a plan: both keep every call. */
	plan?: Esse3StudyPlan | null;
}

function day(text: string | null | undefined): Date | null {
	const date = esse3Date(text);
	return date === null ? null : toDate(date);
}

function startOfDay(date: Date): Date {
	return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function nonBlank(text: string | null | undefined): string | null {
	const value = text?.trim();
	return value === undefined || value === '' ? null : value;
}

function callKey(row: {
	courseOfStudyId: number | null;
	activityId: number | null;
	callId: number | null;
}): CallKey | null {
	const { courseOfStudyId, activityId, callId } = row;
	return courseOfStudyId === null || activityId === null || callId === null
		? null
		: { courseOfStudyId, activityId, callId };
}

/**
 * Whether a published outcome still waits for the student: it can be acted on (a booking id), it
 * is a grade or a pass (a failure or an absence has nothing to accept), nobody accepted or
 * rejected it, and its deadline, the last day to reject it, hasn't passed.
 */
export function requiresDecision(row: Esse3ExamSessionEnrollment, today: Date): boolean {
	if (row.applicationListId === null) return false;
	if (row.acknowledgmentOfReceipt !== 'N' && row.acknowledgmentOfReceipt !== 'V') return false;
	const grade = bookingGrade(row).kind;
	if (grade !== 'numeric' && grade !== 'passed') return false;
	const deadline = day(row.studentOutcomeReferenceDate);
	return deadline !== null && calendarDays(today, deadline) >= 0;
}

/** A result the timeline lists: one waiting for a decision, on a call it can be addressed by. */
export function awaitsDecision(row: Esse3ExamSessionEnrollment, today: Date): boolean {
	return callKey(row) !== null && requiresDecision(row, today);
}

export type InvoiceStatus = 'canceled' | 'paid' | 'expired' | 'pending';

export function invoiceStatus(invoice: Esse3Invoices): InvoiceStatus {
	if (invoice.canceledInvoice !== null && invoice.canceledInvoice !== 0) return 'canceled';
	if (invoice.paidFlag === 1) return 'paid';
	if (invoice.expiredInvoiceFlag === 1) return 'expired';
	return 'pending';
}

/** Whether the invoice is still owed. */
export function isOwed(invoice: Esse3Invoices): boolean {
	const status = invoiceStatus(invoice);
	return status === 'pending' || status === 'expired';
}

function firstCharge(
	invoice: Esse3Invoices,
	charges: readonly Esse3StudentDebit[]
): Esse3StudentDebit | undefined {
	return charges.find(
		(charge) =>
			charge.invoiceId === invoice.invoiceId &&
			nonBlank(charge.taxDescription ?? charge.itemDescription) !== null
	);
}

/** The invoice's own name: its first charge's, else the payment slip's. */
export function invoiceTitle(
	invoice: Esse3Invoices,
	charges: readonly Esse3StudentDebit[]
): string | null {
	const first = firstCharge(invoice, charges);
	return (
		nonBlank(first?.taxDescription ?? first?.itemDescription) ?? nonBlank(invoice.mav1Description)
	);
}

/** The plan Esse3 counts: the approved one, else the latest. */
export function currentPlanId(headers: readonly Esse3StudyPlanHeader[]): number | null {
	const approved = headers.find((header) => header.state === 'A' && header.planId !== null);
	if (approved !== undefined) return approved.planId;
	const ids = headers.flatMap((header) => (header.planId === null ? [] : [header.planId]));
	return ids.length === 0 ? null : Math.max(...ids);
}

const normalizeCode = (code: string) => code.trim().toUpperCase();

/** The activity codes of the plan's chosen activities; null when there are none to filter by. */
export function planCodes(plan: Esse3StudyPlan | null | undefined): ReadonlySet<string> | null {
	if (plan === null || plan === undefined) return null;
	const codes = new Set<string>();
	for (const activity of plan.activity) {
		if (activity.choiceFlag === 0) continue;
		const code = nonBlank(
			activity.activityTranscriptCode ?? activity.contextualizedTeachingActivityKey?.activityCode
		);
		if (code !== null) codes.add(normalizeCode(code));
	}
	return codes.size === 0 ? null : codes;
}

/**
 * Esse3's bookable list also holds record-book rows outside the current plan; those are noise.
 * Fails open: without a plan, or for a call without a code, the call stays.
 */
function inPlan(call: Esse3ExamSessionTranscript, codes: ReadonlySet<string> | null): boolean {
	const code = nonBlank(call.activityCode);
	return codes === null || code === null || codes.has(normalizeCode(code));
}

function base(id: string, date: Date, today: Date, title: string | null) {
	const days = calendarDays(today, date);
	return { id, date, days, urgent: days <= URGENT_DAYS, title };
}

function results(rows: readonly Esse3ExamSessionEnrollment[], today: Date): ResultDeadline[] {
	return rows.flatMap((row) => {
		const date = day(row.studentOutcomeReferenceDate);
		const id = row.applicationListId;
		if (id === null || date === null || !awaitsDecision(row, today)) return [];
		const title = nonBlank(row.studentActivityDescription) ?? nonBlank(row.examCallDescription);
		return [
			{
				...base(`result:${id}`, date, today, title),
				source: 'result' as const,
				applicationListId: id,
				grade: outcomeGrade(row.outcome)
			}
		];
	});
}

function fees(
	invoices: readonly Esse3Invoices[],
	charges: readonly Esse3StudentDebit[],
	today: Date
): FeeDeadline[] {
	return invoices.flatMap((invoice) => {
		const date = day(invoice.invoiceExpiration);
		if (invoice.invoiceId === null || date === null || !isOwed(invoice)) return [];
		const expired = invoiceStatus(invoice) === 'expired';
		const item = base(`fee:${invoice.invoiceId}`, date, today, invoiceTitle(invoice, charges));
		return [
			{
				...item,
				urgent: expired || item.urgent,
				source: 'fee' as const,
				invoiceId: invoice.invoiceId,
				amount: invoice.invoiceAmount ?? 0,
				expired,
				installment: nonBlank(firstCharge(invoice, charges)?.installmentDescription),
				academicYear: invoice.academicYearId
			}
		];
	});
}

function exams(rows: readonly Esse3ExamSessionEnrollment[], today: Date): ExamDeadline[] {
	return rows.flatMap((row) => {
		const at = esse3DateTime(row.shiftDateTime);
		const date = at === null ? day(row.shiftDateTime) : toDate(at.date);
		const key = callKey(row);
		if (key === null || date === null || calendarDays(today, date) < 0) return [];
		const title = nonBlank(row.studentActivityDescription) ?? nonBlank(row.examCallDescription);
		const id = [key.courseOfStudyId, key.activityId, key.callId, row.applicationListId ?? ''];
		return [
			{
				...base(`exam:${id.join('/')}`, date, today, title),
				source: 'exam' as const,
				key,
				time: at !== null && (at.hour !== 0 || at.minute !== 0) ? toDate(at) : null,
				room: nonBlank(row.classroomDescription)
			}
		];
	});
}

/**
 * The first closing booking window of each exam in the plan. An exam already booked is left out:
 * its other calls are a fallback, not something to act on now.
 */
function bookings(
	calls: readonly Esse3ExamSessionTranscript[],
	plan: Esse3StudyPlan | null | undefined,
	booked: readonly ExamDeadline[],
	today: Date
): BookingDeadline[] {
	const codes = planCodes(plan);
	const bookedActivities = new Set(booked.map((exam) => exam.key.activityId));
	const earliest = new Map<
		string,
		{ call: Esse3ExamSessionTranscript; key: CallKey; closes: Date }
	>();
	for (const call of calls) {
		const closes = day(call.enrollmentEndDate);
		const key = callKey(call);
		if (key === null || closes === null || calendarDays(today, closes) < 0) continue;
		if (!inPlan(call, codes) || bookedActivities.has(key.activityId)) continue;
		const group = nonBlank(call.activityDescription) ?? nonBlank(call.activityCode) ?? '';
		const current = earliest.get(group);
		if (current === undefined || closes < current.closes)
			earliest.set(group, { call, key, closes });
	}
	return [...earliest].map(([group, { call, key, closes }]) => {
		const opens = day(call.enrollmentStartDate);
		return {
			...base(`booking:${group}`, closes, today, nonBlank(call.activityDescription)),
			source: 'booking' as const,
			key,
			code: nonBlank(call.activityCode),
			callDate: day(call.callStartDate),
			opensAt: opens !== null && calendarDays(today, opens) > 0 ? opens : null
		};
	});
}

/**
 * Everything with a date the student must act on or attend, from a week back to a month ahead,
 * by date. Same-day entries keep the source order: results, fees, exams, bookings.
 */
export function buildDeadlines(sources: DeadlineSources, today: Date): Deadline[] {
	const midnight = startOfDay(today);
	const booked = exams(sources.bookings ?? [], midnight);
	const all: Deadline[] = [
		...results(sources.results ?? [], midnight),
		...fees(sources.invoices ?? [], sources.charges ?? [], midnight),
		...booked,
		...bookings(sources.calls ?? [], sources.plan, booked, midnight)
	];
	return all
		.filter((item) => item.days >= -LOOK_BACK_DAYS && item.days <= LOOK_AHEAD_DAYS)
		.sort((a, b) => a.date.getTime() - b.date.getTime());
}

export interface DeadlineSummary {
	count: number;
	urgent: number;
	/** The earliest, which may be overdue. */
	next: Deadline | undefined;
}

export function summarize(deadlines: readonly Deadline[]): DeadlineSummary {
	return {
		count: deadlines.length,
		urgent: deadlines.filter((item) => item.urgent).length,
		next: deadlines[0]
	};
}

/** The live state of the services the hub flags, whatever the deadlines' window. */
export interface ServiceStatus {
	/** Results waiting for a decision, and the first of their deadlines. */
	pendingResults: number;
	resultsDue: Date | null;
	/** Owed invoices, how many of them are overdue, and the next expiry of the others. */
	owedFees: number;
	overdueFees: number;
	owedAmount: number;
	feesDue: Date | null;
	/** Booked exams still to sit, and the day of the first. */
	bookedExams: number;
	nextExam: Date | null;
	/** Exams whose booking closes within the urgent window. */
	closingBookings: number;
}

function earliest(dates: readonly Date[]): Date | null {
	return dates.reduce<Date | null>((min, date) => (min === null || date < min ? date : min), null);
}

export function serviceStatus(
	sources: DeadlineSources,
	deadlines: readonly Deadline[],
	today: Date
): ServiceStatus {
	const midnight = startOfDay(today);
	const decisions = (sources.results ?? []).filter((row) => awaitsDecision(row, midnight));
	const owed = (sources.invoices ?? []).filter(isOwed).map((invoice) => {
		const expires = day(invoice.invoiceExpiration);
		const overdue =
			invoiceStatus(invoice) === 'expired' ||
			(expires !== null && calendarDays(midnight, expires) < 0);
		return { invoice, expires, overdue };
	});
	const booked = exams(sources.bookings ?? [], midnight);
	return {
		pendingResults: decisions.length,
		resultsDue: earliest(decisions.flatMap((row) => day(row.studentOutcomeReferenceDate) ?? [])),
		owedFees: owed.length,
		overdueFees: owed.filter((item) => item.overdue).length,
		owedAmount: owed.reduce((sum, item) => sum + (item.invoice.invoiceAmount ?? 0), 0),
		feesDue: earliest(owed.flatMap((item) => (item.overdue ? [] : (item.expires ?? [])))),
		bookedExams: booked.length,
		nextExam: earliest(booked.map((exam) => exam.date)),
		closingBookings: deadlines.filter((item) => item.source === 'booking' && item.urgent).length
	};
}
