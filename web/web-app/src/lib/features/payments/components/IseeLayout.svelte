<script lang="ts">
	import type { Snippet } from 'svelte';
	import IconBookOpen from '~icons/ph/book-open-text';
	import IconPiggyBank from '~icons/ph/piggy-bank-duotone';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import { provideIsee } from '../context.ts';
	import { ISEE_GUIDE, iseeHeading, iseeHref } from '../format.ts';
	import { Isee } from '../stores.svelte.ts';
	import DeclareCard from './DeclareCard.svelte';
	import IseeList from './IseeList.svelte';
	import ListSkeleton from './ListSkeleton.svelte';
	import PaymentsLayout from './PaymentsLayout.svelte';

	interface Props {
		/** The open academic year, from the URL. */
		selected?: string;
		children: Snippet;
	}

	let { selected, children }: Props = $props();

	const isee = provideIsee(new Isee());
	const open = $derived(selected === undefined ? undefined : isee.find(selected));
</script>

<PaymentsLayout
	title={m.shell_service_isee()}
	subtitle={m.payments_isee_years({ count: isee.list?.length ?? 0 })}
	data={isee}
	{selected}
	item={open && iseeHeading(open)}
	hrefFor={iseeHref}
	listHref={shellHref('services/isee')}
	listLabel={m.payments_isee_list_label()}
	detailLabel={m.payments_isee_detail_label()}
	failedTitle={m.payments_isee_load_failed()}
>
	{#snippet empty()}
		<div class="empty">
			{#snippet guide()}
				<Button href={ISEE_GUIDE} icon={IconBookOpen}>{m.payments_isee_guide()}</Button>
			{/snippet}
			<!-- While a year can be declared, the card below is the action. -->
			<EmptyState
				size="page"
				icon={IconPiggyBank}
				title={m.payments_isee_empty_title()}
				body={m.payments_isee_empty_body()}
				action={isee.declarable === null ? guide : undefined}
			/>
			{#if isee.declarable !== null}
				<DeclareCard year={isee.declarable} />
			{/if}
		</div>
	{/snippet}
	{#snippet skeleton()}
		<ListSkeleton rows={2} />
	{/snippet}
	{#snippet list(beside)}
		<IseeList {isee} {beside} {selected} />
	{/snippet}
	{@render children()}
</PaymentsLayout>

<style>
	.empty {
		display: grid;
		gap: var(--sp-5);
		max-inline-size: var(--w-reading);
	}
</style>
