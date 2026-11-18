<script lang="ts">
	import { today } from '#lib/data/clock.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import { useFees } from '../context.ts';
	import DetailSkeleton from './DetailSkeleton.svelte';
	import InvoiceActions from './InvoiceActions.svelte';
	import InvoiceFacts from './InvoiceFacts.svelte';
	import Missing from './Missing.svelte';
	import Receipt from './Receipt.svelte';

	interface Props {
		id: string;
	}

	let { id }: Props = $props();

	const fees = useFees();
	/* Read from the list, so a refresh (a payment just made) updates it in place. */
	const invoice = $derived(fees.find(id));
	const gate = new LoadingGate(() => fees.pending);
</script>

<!-- Its heading is the layout's: the page's title when alone, the pane's beside the list. -->
{#if invoice}
	{#key invoice.id}
		<div class="detail">
			<Receipt {invoice} today={today.current} />
			<InvoiceActions {invoice} />
			<InvoiceFacts {invoice} />
		</div>
	{/key}
{:else if fees.loaded}
	<Missing
		title={m.payments_invoice_missing_title()}
		body={m.payments_invoice_missing_body()}
		back={{ href: shellHref('services/fees'), label: m.payments_back_to_fees() }}
	/>
{:else if gate.visible}
	<DetailSkeleton />
{/if}

<style>
	.detail {
		display: grid;
		gap: var(--sp-5);
	}
</style>
