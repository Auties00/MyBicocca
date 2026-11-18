import { decodeEsse3Refunds } from '@mybicocca/data-api/esse3';
import { describe, expect, it } from 'vitest';
import { refundedTotal, toRefunds } from './refunds.ts';

const row = (fields: Record<string, unknown>) => {
	const value = decodeEsse3Refunds({
		fattId: 5003,
		aaId: 2024,
		importoFattura: 140,
		rimborsatoFlg: 1,
		...fields
	});
	if (value === undefined) throw new Error('not an object');
	return value;
};

describe('toRefunds', () => {
	it('maps every field, blank text as missing', () => {
		expect(
			toRefunds([
				row({
					desMav1: ' ',
					desMav2: 'Rimborso per ricalcolo fascia ISEE',
					cauRimbCod: 'ISEE',
					numMandatoRimb: '2025/004417',
					incassatoDa: 'Accredito su conto corrente',
					notaRimb: '',
					dataEmissione: '06/10/2025',
					dataElab: '20/10/2025',
					dataPagamento: '03/11/2025',
					dataAccredito: '05/11/2025'
				})
			])
		).toEqual([
			{
				key: '5003',
				invoiceId: 5003,
				academicYear: 2024,
				amount: 140,
				description: 'Rimborso per ricalcolo fascia ISEE',
				reasonCode: 'ISEE',
				mandateNumber: '2025/004417',
				refunded: true,
				note: null,
				collectedBy: 'Accredito su conto corrente',
				issued: new Date(2025, 9, 6),
				processed: new Date(2025, 9, 20),
				paidOn: new Date(2025, 10, 3),
				creditedOn: new Date(2025, 10, 5)
			}
		]);
	});

	it('takes the paid amount when the invoice has none', () => {
		expect(toRefunds([row({ importoFattura: null, importoPag: 16 })])[0].amount).toBe(16);
	});

	it('keys refunds without an invoice by year, issue date and amount, and repeats apart', () => {
		const keys = toRefunds([
			row({ fattId: null, dataEmissione: '06/10/2025', importoFattura: 16.5 }),
			row({ fattId: null, dataEmissione: '06/10/2025', importoFattura: 16.5 }),
			row({ fattId: null, aaId: null, importoFattura: null }),
			row({}),
			row({})
		]).map((refund) => refund.key);
		expect(keys).toEqual(['2024-20251006-1650', '2024-20251006-1650-2', '5003', '5003-2', 'x-x-x']);
	});

	it('sorts by academic year, newest first, keeping Esse3’s order within a year', () => {
		const order = toRefunds([
			row({ fattId: 1, aaId: 2023 }),
			row({ fattId: 2, aaId: null }),
			row({ fattId: 3, aaId: 2025 }),
			row({ fattId: 4, aaId: 2023 })
		]).map((refund) => refund.invoiceId);
		expect(order).toEqual([3, 1, 4, 2]);
	});
});

describe('refundedTotal', () => {
	it('sums only what was refunded', () => {
		const refunds = toRefunds([
			row({ fattId: 1, importoFattura: 140 }),
			row({ fattId: 2, importoFattura: 16, rimborsatoFlg: 0 }),
			row({ fattId: 3, importoFattura: null, importoPag: null })
		]);
		expect(refundedTotal(refunds)).toBe(140);
	});
});
