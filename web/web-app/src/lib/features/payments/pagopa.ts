import { errorMessage } from '#lib/data/errorMessage.ts';
import { DataFailure, toDataError } from '#lib/data/errors.ts';
import { mutate, invalidate } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import { m } from '#lib/paraglide/messages.js';
import { getLocale } from '#lib/paraglide/runtime.js';
import { openPdf } from '#lib/ui/files.ts';
import { paymentOutcome, type PaymentOutcome } from './invoices.ts';
import { chargesQuery, invoicesQuery } from './queries.ts';

/**
 * Where pagoPA sends the student back once done. Esse3's portal is the return Android uses and the
 * only one known to be accepted; the app notices the payment when its tab regains focus.
 */
const RETURN_URL = 'https://s3w.si.unimib.it/esse3/';

/** pagoPA answered without a checkout to go to. */
export class NoCheckout extends Error {
	override readonly name = 'NoCheckout';
}

/** The words for a pagoPA action that failed. */
export function actionError(error: unknown): string {
	return error instanceof NoCheckout ? m.payments_pay_no_link() : errorMessage(toDataError(error));
}

/**
 * A pagoPA checkout is always a secure page. The demo's checkout is a page the app made itself, so
 * only the demo build may open one of those.
 */
function checkoutUrl(text: string | null): string {
	let url: URL;
	try {
		url = new URL(text?.trim() ?? '');
	} catch {
		throw new NoCheckout();
	}
	if (url.protocol !== 'https:' && !(__DEMO__ && url.protocol === 'blob:')) throw new NoCheckout();
	return url.href;
}

/** A read that is never cached, failing as the cached reads do. */
async function read<T>(run: () => Promise<T>): Promise<T> {
	try {
		return await run();
	} catch (error) {
		throw new DataFailure(toDataError(error), { cause: error });
	}
}

/**
 * Opens a pagoPA checkout session for the invoice in a new tab. The tab opens inside the click, so
 * popup blockers allow it, and goes to pagoPA once Esse3 answers.
 *
 * @returns The checkout's address when no tab could be opened, for a link to follow instead.
 * @throws What Esse3 throws, or {@link NoCheckout}, after closing the tab.
 */
export async function startPayment(
	scope: SessionScope,
	invoiceId: number,
	waiting: string
): Promise<string | null> {
	const tab = window.open('', '_blank');
	if (tab) {
		tab.opener = null;
		tab.document.title = 'pagoPA';
		tab.document.body.style.font = '15px system-ui, sans-serif';
		tab.document.body.textContent = waiting;
	}
	let url: string;
	try {
		const answer = await mutate(() =>
			scope.api.esse3.tuitionFees.postInitPagoPaTransaction({ invoiceId, returnURL: RETURN_URL })
		);
		url = checkoutUrl(answer.pagopaRedirectUrl);
	} catch (error) {
		tab?.close();
		throw error;
	}
	if (!tab || tab.closed) return url;
	tab.location.href = url;
	return null;
}

/** The pagoPA payment notice, a PDF to pay at a bank, a shop or in a banking app. */
export function openNotice(scope: SessionScope, invoiceId: number): Promise<void> {
	return openPdf(
		() => mutate(() => scope.api.esse3.tuitionFees.putPrintPagoPANotice(invoiceId)),
		`avviso_pagopa_${invoiceId}.pdf`,
		m.payments_opening_pdf()
	);
}

/** The pagoPA receipt, in the app's language. */
export function openReceipt(scope: SessionScope, invoiceId: number): Promise<void> {
	return openPdf(
		() => read(() => scope.api.esse3.tuitionFees.getPagoPAReceipt(invoiceId, getLocale())),
		`quietanza_pagopa_${invoiceId}.pdf`,
		m.payments_opening_pdf()
	);
}

/**
 * Asks pagoPA how the invoice's last payment went. Never cached; the invoices reload with it, so a
 * payment just completed moves the invoice among the paid ones.
 */
export async function checkPayment(
	scope: SessionScope,
	invoiceId: number
): Promise<PaymentOutcome> {
	try {
		const transactions = await read(() =>
			scope.api.esse3.tuitionFees.getPagoPATransactions({ invoiceId, lastTransaction: 1 })
		);
		return paymentOutcome(transactions);
	} finally {
		invalidate(invoicesQuery(scope).key, chargesQuery(scope).key);
	}
}
