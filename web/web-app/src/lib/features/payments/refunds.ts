import type { Esse3Refunds } from '@mybicocca/data-api/esse3';
import { day, descending, nonBlank } from './esse3.ts';

export interface Refund {
	/** Stable across loads, for the URL: the invoice, else the year, issue date and amount. */
	key: string;
	invoiceId: number | null;
	academicYear: number | null;
	amount: number | null;
	description: string | null;
	reasonCode: string | null;
	mandateNumber: string | null;
	refunded: boolean;
	note: string | null;
	collectedBy: string | null;
	issued: Date | null;
	processed: Date | null;
	paidOn: Date | null;
	creditedOn: Date | null;
}

const compact = (date: Date | null) =>
	date === null
		? 'x'
		: `${date.getFullYear()}${String(date.getMonth() + 1).padStart(2, '0')}${String(date.getDate()).padStart(2, '0')}`;

/** Every refund, refunded or not, by academic year (newest first), in Esse3's order within one. */
export function toRefunds(rows: readonly Esse3Refunds[]): Refund[] {
	const used = new Map<string, number>();
	const refunds = rows.map((row): Refund => {
		const amount = row.invoiceAmount ?? row.paidAmount;
		const issued = day(row.issuanceDate);
		const base =
			row.invoiceId !== null
				? String(row.invoiceId)
				: [
						row.academicYearId ?? 'x',
						compact(issued),
						amount === null ? 'x' : Math.round(amount * 100)
					].join('-');
		const count = (used.get(base) ?? 0) + 1;
		used.set(base, count);
		return {
			key: count === 1 ? base : `${base}-${count}`,
			invoiceId: row.invoiceId,
			academicYear: row.academicYearId,
			amount,
			description: nonBlank(row.mav1Description) ?? nonBlank(row.mav2Description),
			reasonCode: nonBlank(row.refundReasonCode),
			mandateNumber: nonBlank(row.refundMandateNumber),
			refunded: row.refundedFlag === 1,
			note: nonBlank(row.refundNote),
			collectedBy: nonBlank(row.collectedBy),
			issued,
			processed: day(row.processingDate),
			paidOn: day(row.paymentDate),
			creditedOn: day(row.creditDate)
		};
	});
	return refunds.sort((a, b) => descending(a.academicYear, b.academicYear));
}

export function refundedTotal(refunds: readonly Refund[]): number {
	return refunds.reduce((sum, refund) => (refund.refunded ? sum + (refund.amount ?? 0) : sum), 0);
}
