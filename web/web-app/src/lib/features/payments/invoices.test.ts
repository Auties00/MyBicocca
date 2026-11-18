import {
	decodeEsse3Invoices,
	decodeEsse3StudentDebit,
	decodeEsse3Transaction
} from '@mybicocca/data-api/esse3';
import { describe, expect, it } from 'vitest';
import {
	dueAmount,
	invoiceActions,
	invoicesByYear,
	paymentOutcome,
	splitInvoices,
	toInvoices,
	type Invoice
} from './invoices.ts';

function wire<T>(decode: (json: unknown) => T | undefined, json: Record<string, unknown>): T {
	const value = decode(json);
	if (value === undefined) throw new Error('not an object');
	return value;
}

const invoice = (fields: Record<string, unknown>) =>
	wire(decodeEsse3Invoices, {
		fattId: 6003,
		aaId: 2025,
		importoFattura: 612,
		pagatoFlg: 0,
		fattScadutaFlg: 0,
		fattAnnullata: 0,
		scadFattura: '15/07/2026',
		desMav1: 'Terza rata a.a. 2025/26',
		pagopaEnabled: 1,
		pagopaImmediato: 1,
		pagopaAvviso: 1,
		...fields
	});

const charge = (fields: Record<string, unknown>) =>
	wire(decodeEsse3StudentDebit, {
		fattId: 6003,
		voceDes: 'Contributo onnicomprensivo',
		importoVoce: 612,
		rataDes: 'Terza rata',
		scadFattura: '15/07/2026',
		...fields
	});

const transaction = (fields: Record<string, unknown>) =>
	wire(decodeEsse3Transaction, { fattId: 6003, ...fields });

function one(fields: Record<string, unknown>, charges = [charge({})]): Invoice {
	const [result] = toInvoices([invoice(fields)], charges);
	return result;
}

describe('toInvoices', () => {
	it('derives the status: cancelled, then paid, then overdue, else pending', () => {
		expect(one({ fattAnnullata: 1, pagatoFlg: 1, fattScadutaFlg: 1 }).status).toBe('canceled');
		expect(one({ pagatoFlg: 1, fattScadutaFlg: 1 }).status).toBe('paid');
		expect(one({ fattScadutaFlg: 1 }).status).toBe('expired');
		expect(one({}).status).toBe('pending');
		expect(one({ fattAnnullata: null, pagatoFlg: null, fattScadutaFlg: null }).status).toBe(
			'pending'
		);
	});

	it('itemises the invoice with its charges, falling back to the invoice deadline', () => {
		const result = one({}, [
			charge({ tassaDes: 'Tassa regionale', importoVoce: 140, scadenzaAddebito: '30/09/2025' }),
			charge({ voceDes: 'Imposta di bollo', importoVoce: 16 }),
			charge({ voceDes: '  ', importoVoce: 1 }),
			charge({ fattId: 9999, voceDes: 'Altra fattura' })
		]);
		expect(result.items).toEqual([
			{
				description: 'Tassa regionale',
				amount: 140,
				installment: 'Terza rata',
				expires: new Date(2025, 8, 30)
			},
			{
				description: 'Imposta di bollo',
				amount: 16,
				installment: 'Terza rata',
				expires: new Date(2026, 6, 15)
			}
		]);
		expect(result.title).toBe('Tassa regionale');
	});

	it('names an invoice without charges after its payment slip, or not at all', () => {
		expect(one({}, []).title).toBe('Terza rata a.a. 2025/26');
		expect(one({ desMav1: ' ' }, []).title).toBeNull();
	});

	it('reads the pagoPA flags, the codes and the dates', () => {
		const result = one({
			pagopaEnabled: 1,
			pagopaImmediato: 0,
			pagopaAvviso: 1,
			iuv: ' ',
			codiceAvviso: '301000000000006003',
			dataEmissione: '2026-06-01',
			dataPagamento: '02/09/2025 00:00:00'
		});
		expect(result.pagoPa).toEqual({ enabled: true, immediate: false, notice: true });
		expect(result.iuv).toBeNull();
		expect(result.noticeCode).toBe('301000000000006003');
		expect(result.issued).toEqual(new Date(2026, 5, 1));
		expect(result.paidOn).toEqual(new Date(2025, 8, 2));
	});

	it('drops invoices without an id and repeated ones', () => {
		const result = toInvoices(
			[invoice({ fattId: null }), invoice({}), invoice({ importoFattura: 1 })],
			[]
		);
		expect(result.map((item) => [item.id, item.amount])).toEqual([[6003, 612]]);
	});
});

describe('splitInvoices and dueAmount', () => {
	const list = toInvoices(
		[
			invoice({ fattId: 1, scadFattura: null }),
			invoice({ fattId: 2, scadFattura: '20/07/2026', fattScadutaFlg: 1, importoFattura: 100 }),
			invoice({ fattId: 3, scadFattura: '10/07/2026', importoFattura: 50 }),
			invoice({ fattId: 4, pagatoFlg: 1, dataPagamento: '01/02/2026' }),
			invoice({ fattId: 5, pagatoFlg: 1, dataPagamento: null, dataEmissione: '01/03/2026' }),
			invoice({ fattId: 6, fattAnnullata: 1, dataEmissione: '01/01/2026' }),
			invoice({ fattId: 7, pagatoFlg: 1 })
		],
		[]
	);

	it('lists what is owed by deadline, undated last', () => {
		expect(splitInvoices(list).toPay.map((item) => item.id)).toEqual([3, 2, 1]);
	});

	it('lists the settled ones by payment, else issue, newest first', () => {
		expect(splitInvoices(list).settled.map((item) => item.id)).toEqual([5, 4, 6, 7]);
	});

	it('sums the pending and overdue amounts', () => {
		expect(dueAmount(list)).toBe(612 + 100 + 50);
	});
});

describe('invoicesByYear', () => {
	it('groups by academic year, newest first, counting only what was paid', () => {
		const list = toInvoices(
			[
				invoice({ fattId: 1, aaId: 2024, pagatoFlg: 1, importoFattura: 598 }),
				invoice({ fattId: 2, aaId: null, pagatoFlg: 1 }),
				invoice({ fattId: 3, aaId: 2025, pagatoFlg: 1, importoFattura: 156 }),
				invoice({ fattId: 4, aaId: 2024, fattAnnullata: 1, importoFattura: 70 })
			],
			[]
		);
		expect(
			invoicesByYear(list).map((group) => [group.year, group.invoices.map((i) => i.id), group.paid])
		).toEqual([
			[2025, [3], 156],
			[2024, [1, 4], 598],
			[null, [2], 612]
		]);
	});
});

describe('invoiceActions', () => {
	const flags = (enabled: boolean, immediate: boolean, notice: boolean) => ({
		enabled,
		immediate,
		notice
	});

	it('offers payment and the notice only while the invoice is owed, as its flags allow', () => {
		for (const status of ['pending', 'expired'] as const) {
			expect(invoiceActions({ status, pagoPa: flags(true, true, true) })).toEqual({
				pay: true,
				notice: true,
				receipt: false,
				status: true
			});
			expect(invoiceActions({ status, pagoPa: flags(true, false, true) }).pay).toBe(false);
			expect(invoiceActions({ status, pagoPa: flags(true, true, false) }).notice).toBe(false);
		}
	});

	it('offers the receipt for paid invoices with pagoPA enabled', () => {
		expect(invoiceActions({ status: 'paid', pagoPa: flags(true, true, true) })).toEqual({
			pay: false,
			notice: false,
			receipt: true,
			status: true
		});
		expect(invoiceActions({ status: 'paid', pagoPa: flags(false, false, false) })).toEqual({
			pay: false,
			notice: false,
			receipt: false,
			status: false
		});
	});

	it('offers only the status check on a cancelled invoice', () => {
		expect(invoiceActions({ status: 'canceled', pagoPa: flags(true, true, true) })).toEqual({
			pay: false,
			notice: false,
			receipt: false,
			status: true
		});
	});
});

describe('paymentOutcome', () => {
	it('reports no transaction on an empty list', () => {
		expect(paymentOutcome([])).toEqual({ kind: 'none' });
	});

	it('takes a payment as completed by flag, outcome or code, whatever its case', () => {
		expect(paymentOutcome([transaction({ pagatoFlg: 1 })]).kind).toBe('completed');
		expect(paymentOutcome([transaction({ esitoTransazione: 'pagamento_eseguito' })]).kind).toBe(
			'completed'
		);
		expect(paymentOutcome([transaction({ esitoCod: 'ok', statoFinale: 0 })]).kind).toBe(
			'completed'
		);
	});

	it('takes a non-final or in-progress transaction as pending', () => {
		expect(paymentOutcome([transaction({ statoFinale: 0 })]).kind).toBe('pending');
		expect(paymentOutcome([transaction({ esitoTransazione: 'IN_CORSO' })]).kind).toBe('pending');
		expect(
			paymentOutcome([transaction({ esitoTransazione: 'PENDING', statoFinale: 1 })]).kind
		).toBe('pending');
	});

	it('takes a final transaction that did not complete as failed, and anything else as unknown', () => {
		expect(paymentOutcome([transaction({ statoFinale: 1, esitoCod: 'KO' })]).kind).toBe('failed');
		expect(
			paymentOutcome([transaction({ esitoDes: 'Annullato dall’utente', statoFinale: null })])
		).toEqual({
			kind: 'unknown',
			description: 'Annullato dall’utente',
			paidOn: null,
			paidAmount: null
		});
	});

	it('reads the payment date and a positive amount from the latest transaction only', () => {
		const outcome = paymentOutcome([
			transaction({ pagatoFlg: 1, dataPagamento: '02/09/2025', importoPagato: 156 }),
			transaction({ pagatoFlg: 0, importoPagato: 0 })
		]);
		expect(outcome).toEqual({
			kind: 'completed',
			description: null,
			paidOn: new Date(2025, 8, 2),
			paidAmount: 156
		});
		const free = paymentOutcome([transaction({ pagatoFlg: 1, importoPagato: 0 })]);
		expect(free.kind === 'completed' && free.paidAmount).toBeNull();
	});
});
