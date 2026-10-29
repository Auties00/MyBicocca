<script lang="ts">
	import type { Snippet } from 'svelte';
	import IconClipboard from '~icons/ph/clipboard-text-duotone';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import ListDetail from '#lib/ui/ListDetail.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import { pendingLine } from '../format.ts';
	import { activityHref, questionnairesHref } from '../links.ts';
	import { Questionnaires, provideQuestionnairesView } from '../questionnaires.svelte.ts';
	import ActivityList from './ActivityList.svelte';
	import ListSkeleton from './ListSkeleton.svelte';

	interface Props {
		/** The open activity, from the URL. */
		selected?: number;
		/** A compilation is open: it takes the whole page, without the list. */
		compiling: boolean;
		/** The open activity's page, the compilation, or the overview beside the list. */
		children: Snippet;
	}

	let { selected, compiling, children }: Props = $props();

	/* Courses and their lecturers side by side: where that needs it, the services column folds. */

	const questionnaires = new Questionnaires();
	const query = questionnaires.query;
	let twoPanes = $state<boolean>();

	provideQuestionnairesView({
		questionnaires,
		get wide() {
			return twoPanes === true && !compiling;
		}
	});

	const loaded = $derived(query.data !== undefined);
	const empty = $derived(loaded && questionnaires.activities.length === 0);
	const failed = $derived(query.status === 'error' && query.error !== undefined && !loaded);
	/* On one pane an open activity is a page of its own, with its own header. */
	const headed = $derived(twoPanes === true || selected === undefined || failed || empty);
	const gate = new LoadingGate(() => query.status === 'pending');
	const updatedAt = $derived(query.updatedAt ? new Date(query.updatedAt) : undefined);
	const order = $derived(questionnaires.activities.map((activity) => activityHref(activity.id)));

	const subtitle = $derived.by(() => {
		if (!loaded || empty) return undefined;
		const pending = questionnaires.pending.length;
		return pendingLine(pending, questionnaires.outstanding.length - pending);
	});
</script>

<!-- An open activity or compilation names the page itself. -->
<svelte:head>
	{#if selected === undefined}<title>{m.questionnaires_title()} · {m.app_name()}</title>{/if}
</svelte:head>

{#snippet list()}
	<div class="list">
		{#if !loaded || gate.visible}
			<div aria-busy="true">
				{#if gate.visible}<ListSkeleton />{/if}
			</div>
		{:else}
			<ActivityList {questionnaires} selected={twoPanes ? selected : undefined} />
		{/if}
	</div>
{/snippet}

{#if compiling}
	{@render children()}
{:else}
	<Page width="dashboard">
		{#if headed}
			<PageHeader
				title={m.questionnaires_title()}
				{subtitle}
				back={{ href: shellHref('services'), label: m.shell_services() }}
				loading={!loaded && query.status === 'pending' && { eyebrow: false }}
			/>
			{#if query.error && loaded}
				<ErrorState
					variant="stale"
					message={errorMessage(query.error)}
					{updatedAt}
					onretry={query.refresh}
					retrying={query.refreshing}
				/>
			{/if}
		{/if}

		{#if failed && query.error}
			<ErrorState
				title={m.questionnaires_load_failed()}
				message={errorMessage(query.error)}
				onretry={query.refresh}
				retrying={query.refreshing}
			/>
		{:else if empty}
			<EmptyState
				size="page"
				icon={IconClipboard}
				title={m.questionnaires_empty_title()}
				body={m.questionnaires_empty_body()}
			>
				{#snippet action()}
					<Button href={shellHref('services/exams')}>{m.questionnaires_empty_action()}</Button>
				{/snippet}
			</EmptyState>
		{:else}
			<!-- Beside the list, the index route's overview stands in for an open course. -->
			<ListDetail
				{list}
				detail={selected === undefined ? undefined : children}
				placeholder={children}
				listWidth={300}
				detailMin={480}
				bind:twoPanes
				listLabel={m.questionnaires_list_label()}
				detailLabel={m.questionnaires_detail_label()}
				current={selected === undefined ? undefined : activityHref(selected)}
				items={order}
				closeHref={questionnairesHref()}
			/>
		{/if}
	</Page>
{/if}

<style>
	.list {
		max-inline-size: var(--w-reading);
	}
</style>
