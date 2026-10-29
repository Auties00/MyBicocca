import type { Esse3EnrollmentForTuition } from '@mybicocca/data-api/esse3';
import { today } from '#lib/data/clock.svelte.ts';
import type { DataError } from '#lib/data/errors.ts';
import { query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { dueAmount, invoicesByYear, splitInvoices, toInvoices, type Invoice } from './invoices.ts';
import { declareYear, iseeYears, type IseeYear } from './isee.ts';
import { chargesQuery, invoicesQuery, iseeQuery, refundsQuery } from './queries.ts';
import { refundedTotal, toRefunds, type Refund } from './refunds.ts';

/** What a payments page needs to know about its data, whatever the data is. */
export interface Loadable {
	/** The list has loaded at least once (possibly from cache). */
	readonly loaded: boolean;
	/** Nothing to show yet and nothing failed: the skeleton's time. */
	readonly pending: boolean;
	readonly error: DataError | undefined;
	readonly refreshing: boolean;
	/** When the data shown was loaded. */
	readonly updatedAt: number | undefined;
	readonly empty: boolean;
	/** The detail ids in list order, for stepping with J and K. */
	readonly order: readonly string[];
	refresh(): Promise<void>;
}

/**
 * The career's invoices, itemised by its charges. The charges only name and itemise the
 * invoices, so when they fail the invoices still show, with the failure noted.
 */
export class Fees implements Loadable {
	readonly #invoices = query(() => session.current && invoicesQuery(session.current));
	readonly #charges = query(() => session.current && chargesQuery(session.current));

	readonly list: Invoice[] | undefined = $derived.by(() => {
		const invoices = this.#invoices.data;
		const charges = this.#charges.data;
		if (invoices === undefined) return undefined;
		if (charges === undefined && this.#charges.error === undefined) return undefined;
		return toInvoices(invoices, charges ?? []);
	});

	readonly split = $derived(splitInvoices(this.list ?? []));
	readonly years = $derived(invoicesByYear(this.split.settled));
	readonly due = $derived(dueAmount(this.list ?? []));
	readonly overdue = $derived(this.split.toPay.filter((i) => i.status === 'expired').length);

	readonly loaded = $derived(this.list !== undefined);
	readonly error = $derived(this.#invoices.error ?? this.#charges.error);
	readonly pending = $derived(!this.loaded && this.#invoices.error === undefined);
	readonly refreshing = $derived(this.#invoices.refreshing || this.#charges.refreshing);
	readonly updatedAt = $derived(this.#invoices.updatedAt);
	readonly empty = $derived(this.list?.length === 0);
	readonly order = $derived(
		[...this.split.toPay, ...this.years.flatMap((year) => year.invoices)].map((i) => String(i.id))
	);

	find(id: string): Invoice | undefined {
		return this.list?.find((invoice) => String(invoice.id) === id);
	}

	refresh = async (): Promise<void> => {
		await Promise.all([this.#invoices.refresh(), this.#charges.refresh()]);
	};
}

/** The years with an ISEE on file, and the one still to declare. */
export class Isee implements Loadable {
	readonly #rows = query(() => session.current && iseeQuery(session.current));

	readonly #data: Esse3EnrollmentForTuition[] | undefined = $derived(this.#rows.data);
	readonly list: IseeYear[] | undefined = $derived(
		this.#data === undefined ? undefined : iseeYears(this.#data)
	);
	readonly declarable: number | null = $derived(
		this.#data === undefined ? null : declareYear(this.#data, today.current)
	);

	readonly loaded = $derived(this.list !== undefined);
	readonly error = $derived(this.#rows.error);
	readonly pending = $derived(!this.loaded && this.error === undefined);
	readonly refreshing = $derived(this.#rows.refreshing);
	readonly updatedAt = $derived(this.#rows.updatedAt);
	readonly empty = $derived(this.list?.length === 0);
	readonly order = $derived((this.list ?? []).map((year) => String(year.year)));

	find(id: string): IseeYear | undefined {
		return this.list?.find((year) => String(year.year) === id);
	}

	refresh = (): Promise<void> => this.#rows.refresh();
}

export class Refunds implements Loadable {
	readonly #rows = query(() => session.current && refundsQuery(session.current));

	readonly list: Refund[] | undefined = $derived(
		this.#rows.data === undefined ? undefined : toRefunds(this.#rows.data)
	);
	readonly total = $derived(refundedTotal(this.list ?? []));
	readonly processing = $derived((this.list ?? []).filter((refund) => !refund.refunded));

	readonly loaded = $derived(this.list !== undefined);
	readonly error = $derived(this.#rows.error);
	readonly pending = $derived(!this.loaded && this.error === undefined);
	readonly refreshing = $derived(this.#rows.refreshing);
	readonly updatedAt = $derived(this.#rows.updatedAt);
	readonly empty = $derived(this.list?.length === 0);
	readonly order = $derived((this.list ?? []).map((refund) => refund.key));

	find(id: string): Refund | undefined {
		return this.list?.find((refund) => refund.key === id);
	}

	refresh = (): Promise<void> => this.#rows.refresh();
}
