import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';
import { callSlug } from '#lib/features/exams/model.ts';
import { invoiceHref } from '#lib/features/payments/format.ts';
import { resultHref } from '#lib/features/results/format.ts';
import type { Deadline } from './deadlines.ts';

/**
 * Where the deadline is acted on: the result to accept or reject, the invoice to pay, the booked
 * exam's own page, or the booking page opened on the call that closes.
 */
export function deadlineHref(item: Deadline): ResolvedPathname {
	switch (item.source) {
		case 'result':
			return resultHref(String(item.applicationListId));
		case 'fee':
			return invoiceHref(item.invoiceId);
		case 'exam':
			return resolve(`services/exams/${callSlug(item.key)}`);
		case 'booking': {
			const params = new URLSearchParams({ call: callSlug(item.key) });
			const course = item.code ?? item.title;
			if (course !== null) params.set('course', course);
			return resolve(`services/exams/book?${params}`);
		}
	}
}
