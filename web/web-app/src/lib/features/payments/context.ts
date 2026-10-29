import { createContext } from 'svelte';
import type { Fees, Isee, Loadable, Refunds } from './stores.svelte.ts';

export interface PaymentsView {
	/** The list the open item comes from, for its failures. */
	readonly data: Loadable;
	/** The level of the item's section headings: under the pane's h2, or the page's h1. */
	readonly level: 'h2' | 'h3';
}

/** How an open item is named: the page's title when alone, the pane's heading beside the list. */
export interface ItemHeading {
	title: string;
	subtitle?: string;
}

export const [usePaymentsView, providePaymentsView] = createContext<PaymentsView>();
export const [useFees, provideFees] = createContext<Fees>();
export const [useIsee, provideIsee] = createContext<Isee>();
export const [useRefunds, provideRefunds] = createContext<Refunds>();
