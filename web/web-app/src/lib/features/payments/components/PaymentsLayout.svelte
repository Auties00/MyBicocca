<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { ResolvedPathname } from '$app/types';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import DetailHeader from '#lib/ui/DetailHeader.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import ListDetail from '#lib/ui/ListDetail.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import { providePaymentsView, type ItemHeading } from '../context.ts';
	import type { Loadable } from '../stores.svelte.ts';

	interface Props {
		title: string;
		/** Counts or totals, once loaded. */
		subtitle?: string;
		data: Loadable;
		/** The open item, from the URL. */
		selected?: string;
		/** How the open item is named, once it is loaded and found. */
		item?: ItemHeading;
		hrefFor: (id: string) => ResolvedPathname;
		listHref: ResolvedPathname;
		listLabel: string;
		detailLabel: string;
		/** The heading of the error that replaces the page when nothing could be loaded. */
		failedTitle: string;
		/** Stands for the whole page when there is nothing to list. */
		empty: Snippet;
		/** The list's geometry while it loads. */
		skeleton: Snippet;
		/** The list; its argument tells whether an item is open beside it, in a narrow column. */
		list: Snippet<[boolean]>;
		/** The route's page: the open item, or nothing. */
		children: Snippet;
	}

	let {
		title,
		subtitle,
		data,
		selected,
		item,
		hrefFor,
		listHref,
		listLabel,
		detailLabel,
		failedTitle,
		empty,
		skeleton,
		list: rows,
		children
	}: Props = $props();

	/*
	 * With nothing open the list has the page to itself, whatever its width: the panes split only
	 * while an item is open, the list then a column of rows beside it.
	 */
	const ALONE = 1e5;

	let twoPanes = $state<boolean>();
	const beside = $derived(twoPanes === true && selected !== undefined);
	/* On one pane an open item is the page, named by the page's header. */
	const alone = $derived(twoPanes !== true && selected !== undefined);

	providePaymentsView({
		get data() {
			return data;
		},
		get level() {
			return beside ? 'h3' : 'h2';
		}
	});

	const failed = $derived(!data.loaded && data.error !== undefined);
	const missing = $derived(selected !== undefined && data.loaded && !data.empty && !item);
	const gate = new LoadingGate(() => data.pending);
	const updatedAt = $derived(data.updatedAt ? new Date(data.updatedAt) : undefined);

	/*
	 * One header for every form of the page: switching between list, item and two panes changes
	 * what it says, never which header is mounted, so the top bar's title and back stay in step.
	 * An item URL whose list is empty or failed is named by the list: the page shows why.
	 */
	const header = $derived.by(() => {
		if (!alone || data.empty || failed) {
			return {
				title,
				subtitle: data.loaded && !data.empty ? subtitle : undefined,
				back: { href: shellHref('services'), label: m.shell_services() },
				loading: data.pending && !data.loaded && { eyebrow: false, title: 1 as const }
			};
		}
		return {
			title: item?.title ?? (data.loaded ? title : ''),
			subtitle: item?.subtitle,
			back: { href: listHref, label: title },
			loading: !data.loaded && { eyebrow: false }
		};
	});
</script>

<!-- A missing item names the page itself. -->
<svelte:head>
	{#if !missing}
		<title>{item ? `${item.title} · ` : ''}{title} · {m.app_name()}</title>
	{/if}
</svelte:head>

<Page width="dashboard">
	<div class={['head', { beside }]}>
		<PageHeader
			title={header.title}
			subtitle={header.subtitle}
			back={header.back}
			loading={header.loading}
		/>
	</div>
	{#if data.error && data.loaded}
		<ErrorState
			variant="stale"
			message={errorMessage(data.error)}
			{updatedAt}
			onretry={data.refresh}
			retrying={data.refreshing}
		/>
	{/if}

	{#if failed && data.error}
		<ErrorState
			title={failedTitle}
			message={errorMessage(data.error)}
			onretry={data.refresh}
			retrying={data.refreshing}
		/>
	{:else if data.empty}
		{@render empty()}
	{:else}
		<ListDetail
			listWidth={300}
			detailMin={selected === undefined ? ALONE : 480}
			bind:twoPanes
			{listLabel}
			{detailLabel}
			detail={selected === undefined ? undefined : detail}
			current={selected === undefined ? undefined : hrefFor(selected)}
			items={data.order.map(hrefFor)}
			closeHref={listHref}
		>
			{#snippet list()}
				<div class="pane">
					{#if !data.loaded || gate.visible}
						<div aria-busy="true">
							{#if gate.visible}{@render skeleton()}{/if}
						</div>
					{:else}
						{@render rows(beside)}
					{/if}
				</div>
			{/snippet}
		</ListDetail>
	{/if}
</Page>

{#snippet detail()}
	<div class="pane item">
		{#if item || !data.loaded}
			<DetailHeader
				title={item?.title ?? ''}
				subtitle={item?.subtitle}
				loading={!item && { eyebrow: false }}
			/>
		{/if}
		{@render children()}
	</div>
{/snippet}

<style>
	/*
	 * Beside the list an item opens without moving the header: it gets its own view-transition
	 * name, so only the detail pane takes part in the page transition.
	 */
	.head.beside {
		view-transition-name: payments-head;
	}

	.pane {
		container: payments / inline-size;
	}

	/* One width for the header and the item under it, so the header's controls end with it. */
	.item {
		display: grid;
		gap: var(--sp-5);
		max-inline-size: 640px;
	}
</style>
