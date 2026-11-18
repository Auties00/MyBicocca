import type { Esse3Invoices, Esse3StudentDebit, Esse3Transaction } from '@mybicocca/data-api/esse3';
import {
	invoiceStatus,
	invoiceTitle,
	type InvoiceStatus
} from '#lib/features/services/deadlines.ts';
import { ascending, day, descending, nonBlank } from './esse3.ts';

export type { InvoiceStatus };

export interface InvoiceItem {
	description: string;
	amount: number;
	installment: string | null;
	/** The charge's own deadline, else the invoice's. */
	expires: Date | null;
}

export interface Invoice {
	id: number;
	/** The academic year billed, by its September. */
	academicYear: number | null;
	amount: number;
	status: InvoiceStatus;
	issued: Date | null;
	expires: Date | null;
	paidOn: Date | null;
	pagoPa: { enabled: boolean; immediate: boolean; notice: boolean };
	iuv: string | null;
	noticeCode: string | null;
	items: InvoiceItem[];
	/** The first charge's name, else the payment slip's; null when Esse3 gives neither. */
	title: string | null;
}

/**
 * The person's invoices joined with the career's charges, in Esse3's order. Esse3 lists invoices
 * by person and charges by career, so another career's invoices come without items.
 */
export function toInvoices(
	invoices: readonly Esse3Invoices[],
	charges: readonly Esse3StudentDebit[]
): Invoice[] {
	const itemsOf = new Map<number, InvoiceItem[]>();
	for (const charge of charges) {
		const description = nonBlank(charge.taxDescription ?? charge.itemDescription);
		if (charge.invoiceId === null || description === null) continue;
		const items = itemsOf.get(charge.invoiceId) ?? [];
		items.push({
			description,
			amount: charge.itemAmount ?? 0,
			installment: nonBlank(charge.installmentDescription),
			expires: day(charge.chargeExpiration) ?? day(charge.invoiceExpiration)
		});
		itemsOf.set(charge.invoiceId, items);
	}
	const seen = new Set<number>();
	const result: Invoice[] = [];
	for (const invoice of invoices) {
		const id = invoice.invoiceId;
		if (id === null || seen.has(id)) continue;
		seen.add(id);
		result.push({
			id,
			academicYear: invoice.academicYearId,
			amount: invoice.invoiceAmount ?? 0,
			status: invoiceStatus(invoice),
			issued: day(invoice.issuanceDate),
			expires: day(invoice.invoiceExpiration),
			paidOn: day(invoice.paymentDate),
			pagoPa: {
				enabled: invoice.pagopaEnabled === 1,
				immediate: invoice.pagopaImmediate === 1,
				notice: invoice.pagopaNotice === 1
			},
			iuv: nonBlank(invoice.iuv),
			noticeCode: nonBlank(invoice.noticeCode),
			items: itemsOf.get(id) ?? [],
			title: invoiceTitle(invoice, charges)
		});
	}
	return result;
}

/** Still owed: pending, or overdue. */
export function isPayable(status: InvoiceStatus): boolean {
	return status === 'pending' || status === 'expired';
}

/** To pay, the soonest deadline first; settled (paid or cancelled), the latest first. */
export function splitInvoices(invoices: readonly Invoice[]): {
	toPay: Invoice[];
	settled: Invoice[];
} {
	return {
		toPay: invoices
			.filter((invoice) => isPayable(invoice.status))
			.sort((a, b) => ascending(a.expires, b.expires)),
		settled: invoices
			.filter((invoice) => !isPayable(invoice.status))
			.sort((a, b) => descending(a.paidOn ?? a.issued, b.paidOn ?? b.issued))
	};
}

export function dueAmount(invoices: readonly Invoice[]): number {
	return invoices.reduce(
		(sum, invoice) => (isPayable(invoice.status) ? sum + invoice.amount : sum),
		0
	);
}

export interface InvoiceYear {
	year: number | null;
	invoices: Invoice[];
	/** What was paid for that year; cancelled invoices don't count. */
	paid: number;
}

/** Invoices by academic year, newest first, keeping their order within each year. */
export function invoicesByYear(invoices: readonly Invoice[]): InvoiceYear[] {
	const years = new Map<number | null, Invoice[]>();
	for (const invoice of invoices) {
		const group = years.get(invoice.academicYear) ?? [];
		group.push(invoice);
		years.set(invoice.academicYear, group);
	}
	return [...years]
		.sort(([a], [b]) => descending(a, b))
		.map(([year, group]) => ({
			year,
			invoices: group,
			paid: group.reduce((sum, i) => (i.status === 'paid' ? sum + i.amount : sum), 0)
		}));
}

/** The pagoPA actions an invoice offers, by its flags. */
export interface InvoiceActions {
	/** Opens a pagoPA checkout session. */
	pay: boolean;
	/** The payment notice, to pay at a bank, a shop or in a banking app. */
	notice: boolean;
	receipt: boolean;
	status: boolean;
}

export function invoiceActions(invoice: Pick<Invoice, 'status' | 'pagoPa'>): InvoiceActions {
	const payable = isPayable(invoice.status);
	return {
		pay: payable && invoice.pagoPa.immediate,
		notice: payable && invoice.pagoPa.notice,
		receipt: invoice.status === 'paid' && invoice.pagoPa.enabled,
		status: invoice.pagoPa.enabled
	};
}

export type PaymentOutcome =
	| { kind: 'none' }
	| {
			kind: 'completed' | 'pending' | 'failed' | 'unknown';
			/** pagoPA's own wording, in Italian. */
			description: string | null;
			paidOn: Date | null;
			paidAmount: number | null;
	  };

/** How the invoice's last pagoPA transaction went; Esse3 lists it first. */
export function paymentOutcome(transactions: readonly Esse3Transaction[]): PaymentOutcome {
	const last = transactions[0];
	if (last === undefined) return { kind: 'none' };
	const outcome = (last.transactionOutcome ?? '').trim().toUpperCase();
	const completed =
		last.paidFlag === 1 ||
		outcome === 'PAGAMENTO_ESEGUITO' ||
		(last.outcomeCode ?? '').trim().toUpperCase() === 'OK';
	const pending =
		!completed && (last.finalState === 0 || outcome.includes('CORSO') || outcome.includes('PEND'));
	let kind: 'completed' | 'pending' | 'failed' | 'unknown' = 'unknown';
	if (completed) kind = 'completed';
	else if (pending) kind = 'pending';
	else if (last.finalState === 1) kind = 'failed';
	return {
		kind,
		description: nonBlank(last.outcomeDescription),
		paidOn: day(last.paymentDate),
		paidAmount: last.paidAmount !== null && last.paidAmount > 0 ? last.paidAmount : null
	};
}
