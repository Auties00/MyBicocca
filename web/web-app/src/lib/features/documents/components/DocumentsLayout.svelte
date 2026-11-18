<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { ResolvedPathname } from '$app/types';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import type { Query } from '#lib/data/query.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import ListDetail from '#lib/ui/ListDetail.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import { provideDocumentsView } from '../view.ts';

	interface Props {
		title: string;
		/** The count once loaded, or a line that needs no data. */
		subtitle?: string;
		/** The list's query: its loading and failures are the page's. */
		query: Query<unknown>;
		/** Loaded with nothing to list. */
		empty: boolean;
		/** Heading of the error that replaces the page when nothing could load. */
		failedTitle: string;
		/** The open item's link; none when the list is all there is. */
		selected?: ResolvedPathname;
		/** Every item's link in list order, for J and K. */
		hrefs: readonly ResolvedPathname[];
		/** The page without an open item. */
		home: ResolvedPathname;
		/** The narrowest the detail may be beside the list; below, one replaces the other. */
		detailMin?: number;
		listLabel: string;
		detailLabel: string;
		/** A line under the header, above both panes. */
		note?: Snippet;
		/** The list, told the open item while it shows beside it. */
		list: Snippet<[string | undefined]>;
		skeleton: Snippet;
		emptyState: Snippet;
		/** The open item, or what fills the second pane while none is. */
		children: Snippet;
	}

	let {
		title,
		subtitle,
		query,
		empty,
		failedTitle,
		selected,
		hrefs,
		home,
		detailMin = 480,
		listLabel,
		detailLabel,
		note,
		list: rows,
		skeleton,
		emptyState,
		children
	}: Props = $props();

	let twoPanes = $state<boolean>();
	provideDocumentsView({
		get wide() {
			return twoPanes === true;
		}
	});

	const loaded = $derived(query.data !== undefined);
	const failed = $derived(query.status === 'error' && query.error !== undefined && !loaded);
	/* On one pane an open item is a page of its own, with its own header. */
	const alone = $derived(twoPanes !== true && selected !== undefined && !failed && !empty);
	const gate = new LoadingGate(() => query.status === 'pending');
	const updatedAt = $derived(query.updatedAt ? new Date(query.updatedAt) : undefined);
</script>

<Page width="dashboard" focusOnQuery>
	{#if !alone}
		<div class={['head', { wide: twoPanes }]}>
			<PageHeader
				{title}
				{subtitle}
				back={{ href: shellHref('services'), label: m.shell_services() }}
				loading={!loaded && query.status === 'pending' && { eyebrow: false, title: 1 }}
			/>
		</div>
		{#if query.error && loaded}
			<div class="stale">
				<ErrorState
					variant="stale"
					message={errorMessage(query.error)}
					{updatedAt}
					onretry={query.refresh}
					retrying={query.refreshing}
				/>
			</div>
		{/if}
		{#if note && !failed}{@render note()}{/if}
	{/if}

	{#if failed && query.error}
		<ErrorState
			title={failedTitle}
			message={errorMessage(query.error)}
			onretry={query.refresh}
			retrying={query.refreshing}
		/>
	{:else if empty}
		{@render emptyState()}
	{:else}
		<ListDetail
			listWidth={280}
			{detailMin}
			bind:twoPanes
			{listLabel}
			{detailLabel}
			detail={selected === undefined ? undefined : item}
			placeholder={selected === undefined ? item : undefined}
			current={selected}
			items={hrefs}
			closeHref={home}
		>
			{#snippet list()}
				<div class="list">
					{#if !loaded}
						<div aria-busy="true">
							{#if gate.visible}{@render skeleton()}{/if}
						</div>
					{:else}
						{@render rows(twoPanes ? selected : undefined)}
					{/if}
				</div>
			{/snippet}
		</ListDetail>
	{/if}
</Page>

{#snippet item()}
	<div class={['item', { alone }]}>{@render children()}</div>
{/snippet}

<style>
	.stale {
		margin-block-start: calc(var(--page-gap) * -0.5);
	}

	/*
	 * Beside the list an item opens without moving the header: it gets its own view-transition
	 * name, so only the detail pane takes part in the page transition.
	 */
	.head.wide {
		view-transition-name: documents-head;
	}

	.list,
	.item {
		container: documents / inline-size;
	}

	.list,
	.item.alone {
		max-inline-size: var(--w-reading);
	}

	.item {
		display: grid;
		gap: var(--page-gap);
	}
</style>
