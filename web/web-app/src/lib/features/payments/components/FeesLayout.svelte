<script lang="ts">
	import type { Snippet } from 'svelte';
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconReceipt from '~icons/ph/receipt-duotone';
	import { formatCurrency } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import { provideFees } from '../context.ts';
	import { invoiceHeading, invoiceHref } from '../format.ts';
	import { Fees } from '../stores.svelte.ts';
	import FeesList from './FeesList.svelte';
	import ListSkeleton from './ListSkeleton.svelte';
	import PaymentsLayout from './PaymentsLayout.svelte';

	interface Props {
		/** The open invoice, from the URL. */
		selected?: string;
		children: Snippet;
	}

	let { selected, children }: Props = $props();

	/* Esse3's own list of invoices, for a career whose fees are settled outside it. */
	const ESSE3_FEES = 'https://s3w.si.unimib.it/esse3/auth/studente/Tasse/ListaFatture.do';

	const fees = provideFees(new Fees());
	const open = $derived(selected === undefined ? undefined : fees.find(selected));

	const subtitle = $derived.by(() => {
		if (fees.due <= 0) return m.payments_settled_subtitle({ count: fees.list?.length ?? 0 });
		const due = m.payments_due_subtitle({ amount: formatCurrency(fees.due) });
		return fees.overdue > 0 ? `${due} · ${m.payments_overdue_count({ count: fees.overdue })}` : due;
	});
</script>

<PaymentsLayout
	title={m.shell_service_fees()}
	{subtitle}
	data={fees}
	{selected}
	item={open && invoiceHeading(open)}
	hrefFor={invoiceHref}
	listHref={shellHref('services/fees')}
	listLabel={m.payments_fees_list_label()}
	detailLabel={m.payments_fees_detail_label()}
	failedTitle={m.payments_fees_load_failed()}
>
	{#snippet empty()}
		<EmptyState
			size="page"
			icon={IconReceipt}
			title={m.payments_fees_empty_title()}
			body={m.payments_fees_empty_body()}
		>
			{#snippet action()}
				<Button href={ESSE3_FEES} trailingIcon={IconArrowSquareOut}>
					{m.payments_fees_empty_action()}
				</Button>
			{/snippet}
		</EmptyState>
	{/snippet}
	{#snippet skeleton()}
		<ListSkeleton />
	{/snippet}
	{#snippet list(beside)}
		<FeesList {fees} {beside} {selected} />
	{/snippet}
	{@render children()}
</PaymentsLayout>
