import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';
import { academicYearLabel } from '#lib/data/academicYear.ts';
import { calendarDays, formatDate, type DateStyle } from '#lib/format/dates.ts';
import { sentenceCase } from '#lib/format/names.ts';
import { m } from '#lib/paraglide/messages.js';
import type { Status } from '#lib/ui/types.ts';
import type { ItemHeading } from './context.ts';
import type { Invoice, InvoiceStatus } from './invoices.ts';
import type { IseeYear } from './isee.ts';
import type { Refund } from './refunds.ts';

/* Esse3 has no ISEE form: the DSU is signed at INPS, and Bicocca acquires the result. */
export const INPS_ISEE = 'https://servizi2.inps.it/servizi/ISEEPrecompilato/home.aspx';
export const ISEE_GUIDE =
	'https://www.unimib.it/studiare/servizi-studenti-e-laureati/segreterie/immatricolazione/contribuzione-studentesca';

export const invoiceHref = (id: number | string): ResolvedPathname =>
	resolve('/(app)/services/fees/[id]', { id: String(id) });

export const iseeHref = (year: number | string): ResolvedPathname =>
	resolve('/(app)/services/isee/[year]', { year: String(year) });

export const refundHref = (key: string): ResolvedPathname =>
	resolve('/(app)/services/refunds/[id]', { id: key });

/** Esse3's name for it, in sentence case when it comes in capitals ("ISCRIZIONE"). */
export function invoiceTitle(invoice: Pick<Invoice, 'title' | 'academicYear' | 'id'>): string {
	if (invoice.title) return sentenceCase(invoice.title);
	return invoice.academicYear === null
		? m.payments_invoice_number({ id: invoice.id })
		: m.payments_invoice_fallback({ year: academicYearLabel(invoice.academicYear) });
}

/** Joins the parts that are there with a middle dot; undefined when none is. */
function line(...parts: Array<string | null | undefined>): string | undefined {
	return parts.filter(Boolean).join(' · ') || undefined;
}

export function invoiceHeading(invoice: Invoice): ItemHeading {
	return {
		title: invoiceTitle(invoice),
		subtitle: line(
			invoice.items[0]?.installment,
			invoice.academicYear === null
				? null
				: m.payments_year_short({ year: academicYearLabel(invoice.academicYear) })
		)
	};
}

export function iseeHeading(year: IseeYear): ItemHeading {
	return {
		title: m.payments_academic_year({ year: academicYearLabel(year.year) }),
		subtitle: year.course ?? undefined
	};
}

export function refundHeading(refund: Refund): ItemHeading {
	return {
		title: refund.description ?? m.payments_refund_fallback(),
		subtitle:
			refund.academicYear === null
				? undefined
				: m.payments_academic_year({ year: academicYearLabel(refund.academicYear) })
	};
}

export const statusLabels: Record<InvoiceStatus, () => string> = {
	pending: m.payments_status_pending,
	expired: m.payments_status_expired,
	paid: m.payments_status_paid,
	canceled: m.payments_status_canceled
};

export const statusTones: Record<InvoiceStatus, Status> = {
	pending: 'warning',
	expired: 'danger',
	paid: 'success',
	canceled: 'neutral'
};

/** "Pagata l’8 feb 2026": Italian elides the article before 8 and 11. */
export function paidOnLabel(date: Date, style: DateStyle = 'medium'): string {
	return m.payments_paid_on({ date: formatDate(date, style), day: date.getDate() });
}

/** "Scade tra 27 giorni", "Scade oggi", "Scaduta da 3 giorni". */
export function deadlineLabel(expires: Date, today: Date): string {
	const days = calendarDays(today, expires);
	if (days < 0) return m.payments_overdue_by({ count: -days });
	if (days === 0) return m.payments_due_today();
	if (days === 1) return m.payments_due_tomorrow();
	return m.payments_due_in({ count: days });
}

/** "tra 27 giorni", "oggi", "scaduta da 3 giorni": what follows the due date on a receipt. */
export function relativeDeadline(expires: Date, today: Date): string {
	const days = calendarDays(today, expires);
	if (days < 0) return m.payments_relative_overdue({ count: -days });
	if (days === 0) return m.payments_relative_today();
	if (days === 1) return m.payments_relative_tomorrow();
	return m.payments_relative_in({ count: days });
}

/** How loudly a deadline speaks: overdue or within a week is urgent. */
export function deadlineTone(expires: Date, today: Date): 'danger' | 'warning' | 'neutral' {
	const days = calendarDays(today, expires);
	if (days < 0) return 'danger';
	return days <= 7 ? 'warning' : 'neutral';
}

/** One line under a settled invoice: when it was paid, or that it was cancelled. */
export function settledLabel(invoice: Invoice): string {
	if (invoice.status === 'canceled') return m.payments_status_canceled();
	return invoice.paidOn ? paidOnLabel(invoice.paidOn) : m.payments_status_paid();
}

export function yearLabel(year: number | null): string {
	return year === null ? m.payments_year_unknown() : academicYearLabel(year);
}

/* The refund causali whose meaning is plain; Esse3 lets each university define its own. */
const REFUND_REASONS = new Map<string, () => string>([
	['ISEE', m.payments_reason_isee],
	['DUPL', m.payments_reason_duplicate]
]);

/** The name of a refund's reason when it is a known causale; null for the others. */
export function reasonName(code: string): string | null {
	return REFUND_REASONS.get(code.toUpperCase())?.() ?? null;
}

/** "Ricalcolo ISEE (ISEE)", or the bare code when it has no known name. */
export function refundReason(code: string): string {
	const name = reasonName(code);
	return name === null ? code : `${name} (${code})`;
}
