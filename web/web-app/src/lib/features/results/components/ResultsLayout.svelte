<script lang="ts">
	import type { Snippet } from 'svelte';
	import IconStamp from '~icons/ph/stamp-duotone';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { bookHref } from '#lib/features/exams/links.ts';
	import { formatRelative } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import ListDetail from '#lib/ui/ListDetail.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import { detailSubtitle, resultHref, resultTitle } from '../format.ts';
	import { Results, provideResultsView } from '../results.svelte.ts';
	import ListSkeleton from './ListSkeleton.svelte';
	import ResultList from './ResultList.svelte';

	interface Props {
		/** The open result, from the URL. */
		selected?: string;
		/** The open result's page, or the overview beside the list when none is. */
		children: Snippet;
	}

	let { selected, children }: Props = $props();

	const results = new Results();
	const query = results.query;
	let twoPanes = $state<boolean>();
	let listNode = $state<HTMLElement>();

	const split = $derived(results.split);
	const loaded = $derived(query.data !== undefined);
	const empty = $derived(loaded && split.pending.length + split.archived.length === 0);
	const failed = $derived(query.status === 'error' && query.error !== undefined && !loaded);
	/* On one pane an open result is a page of its own, and the header is its header. */
	const alone = $derived(twoPanes !== true && selected !== undefined && !failed && !empty);
	const open = $derived(selected === undefined ? undefined : results.find(selected));
	const gate = new LoadingGate(() => query.status === 'pending');
	const listShown = $derived(loaded && !gate.visible);
	const updatedAt = $derived(query.updatedAt ? new Date(query.updatedAt) : undefined);
	/* Offline, the shell's band already says why nothing refreshes: the page only says how old it is. */
	const offline = $derived(query.error?.kind === 'offline');
	const listHref = shellHref('services/results');

	provideResultsView({
		results,
		get wide() {
			return twoPanes === true;
		}
	});

	const listSubtitle = $derived.by(() => {
		if (!loaded || empty) return undefined;
		const pending =
			split.pending.length > 0
				? m.results_summary_pending({ count: split.pending.length })
				: m.results_summary_none();
		return split.archived.length > 0
			? `${pending} · ${m.results_summary_archived({ count: split.archived.length })}`
			: pending;
	});

	/*
	 * One header serves the list and a result alone on the screen, so the top bar's back link
	 * never sees two headers swap as the panes join or split.
	 */
	const title = $derived.by(() => {
		if (!alone) return m.results_title();
		return open ? resultTitle(open) : loaded ? m.results_title() : '';
	});
	const subtitle = $derived(alone ? open && detailSubtitle(open) : listSubtitle);
	const back = $derived(
		alone
			? { href: listHref, label: m.results_title() }
			: { href: shellHref('services'), label: m.shell_services() }
	);
	const headerLoading = $derived(
		!loaded && query.status === 'pending' && (alone ? true : { eyebrow: false })
	);

	/* A result opened by a link can sit far down the list: its row comes into view once, centred. */
	let revealed = false;
	$effect(() => {
		if (revealed || !twoPanes || !listShown || !listNode || selected === undefined) return;
		revealed = true;
		const row = listNode.querySelector<HTMLElement>(
			`a[href="${CSS.escape(resultHref(selected))}"]`
		);
		/* The list's pane, which scrolls on its own beside the detail. */
		const pane = listNode.parentElement;
		if (!row || !pane) return;
		const offset = row.getBoundingClientRect().top - pane.getBoundingClientRect().top;
		pane.scrollTop += offset - (pane.clientHeight - row.offsetHeight) / 2;
	});
</script>

<!-- An open result names the page itself. -->
<svelte:head>
	{#if selected === undefined || failed || empty}
		<title>{m.results_title()} · {m.app_name()}</title>
	{/if}
</svelte:head>

{#snippet staleness()}
	{#if updatedAt}
		<p class="updated">{m.ui_updated_ago({ time: formatRelative(updatedAt) })}</p>
	{/if}
{/snippet}

<Page width="dashboard">
	<div class={['head', { wide: twoPanes }]}>
		<PageHeader
			{title}
			{subtitle}
			eyebrow={alone ? (open?.code ?? undefined) : undefined}
			eyebrowCode
			{back}
			loading={headerLoading}
			meta={offline && loaded ? staleness : undefined}
			sans={alone && open !== undefined}
		/>
	</div>
	{#if query.error && loaded && !offline}
		<ErrorState
			variant="stale"
			message={errorMessage(query.error)}
			{updatedAt}
			onretry={query.refresh}
			retrying={query.refreshing}
		/>
	{/if}

	{#if failed && query.error}
		<ErrorState
			title={m.results_load_failed()}
			message={errorMessage(query.error)}
			onretry={query.refresh}
			retrying={query.refreshing}
		/>
	{:else if empty}
		<EmptyState
			size="page"
			icon={IconStamp}
			title={m.results_empty_title()}
			body={m.results_empty_body()}
		>
			{#snippet action()}
				<Button href={bookHref()}>{m.results_empty_action()}</Button>
			{/snippet}
		</EmptyState>
	{:else}
		<ListDetail
			listWidth={300}
			detailMin={480}
			bind:twoPanes
			listLabel={m.results_list_label()}
			detailLabel={m.results_detail_label()}
			detail={selected === undefined ? undefined : children}
			placeholder={selected === undefined ? children : undefined}
			current={selected === undefined ? undefined : resultHref(selected)}
			items={results.ordered.map((result) => resultHref(result.id))}
			closeHref={listHref}
		>
			{#snippet list()}
				<div class="list" bind:this={listNode}>
					{#if listShown}
						<ResultList {results} selected={twoPanes ? selected : undefined} />
					{:else}
						<div aria-busy="true">
							{#if gate.visible}<ListSkeleton />{/if}
						</div>
					{/if}
				</div>
			{/snippet}
		</ListDetail>
	{/if}
</Page>

<style>
	.updated {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	/*
	 * Beside the list a result opens without moving the header: it gets its own view-transition
	 * name, so only the detail pane takes part in the page transition.
	 */
	.head.wide {
		view-transition-name: results-head;
	}

	.list {
		container: results / inline-size;
		max-inline-size: var(--w-reading);
	}
</style>
