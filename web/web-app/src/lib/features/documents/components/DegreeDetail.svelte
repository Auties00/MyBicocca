<script lang="ts">
	import IconSealQuestion from '~icons/ph/seal-question-duotone';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Button from '#lib/ui/Button.svelte';
	import DetailHeader from '#lib/ui/DetailHeader.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import { useDegrees } from '../degrees.svelte.ts';
	import { headline, subtitle as subtitleOf, titleSections } from '../degreeView.ts';
	import { useDocumentsView } from '../view.ts';
	import DegreeHero from './DegreeHero.svelte';
	import DetailSkeleton from './DetailSkeleton.svelte';
	import FactSections from './FactSections.svelte';

	interface Props {
		id: string;
	}

	let { id }: Props = $props();

	const degrees = useDegrees();
	const view = useDocumentsView();
	const query = degrees.query;
	const loaded = $derived(query.data !== undefined);
	const gate = new LoadingGate(() => query.status === 'pending');
	const updatedAt = $derived(query.updatedAt ? new Date(query.updatedAt) : undefined);

	const pending = $derived(!loaded && query.status === 'pending');
	const title = $derived(degrees.find(id));
	const heading = $derived(title ? headline(title) : pending ? '' : m.shell_service_degrees());
	const subtitle = $derived(title ? subtitleOf(title) : undefined);
	const sections = $derived(title ? titleSections(title) : []);
</script>

<svelte:head>
	<title>{heading ? `${heading} · ` : ''}{m.shell_service_degrees()} · {m.app_name()}</title>
</svelte:head>

{#if !view.wide}
	<PageHeader
		title={heading}
		{subtitle}
		back={{ href: shellHref('services/degrees'), label: m.shell_service_degrees() }}
		loading={pending && { eyebrow: false }}
		sans
	/>
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
{/if}

{#if !loaded}
	<div class="detail" aria-busy="true">
		<DetailHeader title="" loading={{ eyebrow: false }} />
		{#if gate.visible}<DetailSkeleton hero />{/if}
	</div>
{:else if !title}
	<EmptyState
		size="page"
		icon={IconSealQuestion}
		title={m.documents_degrees_not_found_title()}
		body={m.documents_degrees_not_found_body()}
	>
		{#snippet action()}
			<Button href={shellHref('services/degrees')}>{m.documents_degrees_back()}</Button>
		{/snippet}
	</EmptyState>
{:else}
	<article class="detail">
		<DetailHeader title={heading} {subtitle} />
		<FactSections {sections} level={view.wide ? 3 : 2}>
			{#snippet lead()}<DegreeHero {title} />{/snippet}
		</FactSections>
	</article>
{/if}

<style>
	.stale {
		margin-block-start: calc(var(--page-gap) * -0.5);
	}

	.detail {
		display: grid;
		gap: var(--sp-5);
	}
</style>
