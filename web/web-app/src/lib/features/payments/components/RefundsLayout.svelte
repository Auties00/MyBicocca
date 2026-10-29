<script lang="ts">
	import type { Snippet } from 'svelte';
	import IconHandCoins from '~icons/ph/hand-coins-duotone';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import { provideRefunds } from '../context.ts';
	import { refundHeading, refundHref } from '../format.ts';
	import { Refunds } from '../stores.svelte.ts';
	import ListSkeleton from './ListSkeleton.svelte';
	import PaymentsLayout from './PaymentsLayout.svelte';
	import RefundsList from './RefundsList.svelte';

	interface Props {
		/** The open refund, from the URL. */
		selected?: string;
		children: Snippet;
	}

	let { selected, children }: Props = $props();

	const refunds = provideRefunds(new Refunds());
	const open = $derived(selected === undefined ? undefined : refunds.find(selected));

	const subtitle = $derived.by(() => {
		const count = m.payments_refunds_count({ count: refunds.list?.length ?? 0 });
		const processing = refunds.processing.length;
		return processing > 0
			? `${count} · ${m.payments_refunds_processing_count({ count: processing })}`
			: count;
	});
</script>

<PaymentsLayout
	title={m.shell_service_refunds()}
	{subtitle}
	data={refunds}
	{selected}
	item={open && refundHeading(open)}
	hrefFor={refundHref}
	listHref={shellHref('services/refunds')}
	listLabel={m.payments_refunds_list_label()}
	detailLabel={m.payments_refunds_detail_label()}
	failedTitle={m.payments_refunds_load_failed()}
>
	{#snippet empty()}
		<EmptyState
			size="page"
			icon={IconHandCoins}
			title={m.payments_refunds_empty_title()}
			body={m.payments_refunds_empty_body()}
		>
			{#snippet action()}
				<Button href={shellHref('services/fees')}>{m.payments_refunds_empty_action()}</Button>
			{/snippet}
		</EmptyState>
	{/snippet}
	{#snippet skeleton()}
		<ListSkeleton rows={2} />
	{/snippet}
	{#snippet list(beside)}
		<RefundsList {refunds} {beside} {selected} />
	{/snippet}
	{@render children()}
</PaymentsLayout>
