<script lang="ts">
	import type { Snippet } from 'svelte';
	import IconScroll from '~icons/ph/scroll-duotone';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import { Degrees, degreeHref, provideDegrees } from '../degrees.svelte.ts';
	import DegreeList from './DegreeList.svelte';
	import DocumentsLayout from './DocumentsLayout.svelte';
	import ListSkeleton from './ListSkeleton.svelte';

	interface Props {
		/** The open title, from the URL. */
		id?: string;
		/** The open title's page, or the overview beside the list when none is. */
		children: Snippet;
	}

	let { id, children }: Props = $props();

	const degrees = new Degrees();
	provideDegrees(degrees);

	const loaded = $derived(degrees.query.data !== undefined);
	const empty = $derived(loaded && degrees.titles.length === 0);
	const subtitle = $derived(
		loaded && !empty ? m.documents_degrees_count({ count: degrees.titles.length }) : undefined
	);
</script>

<!-- An open title names the page itself. -->
<svelte:head>
	{#if id === undefined}
		<title>{m.shell_service_degrees()} · {m.app_name()}</title>
	{/if}
</svelte:head>

<DocumentsLayout
	title={m.shell_service_degrees()}
	{subtitle}
	query={degrees.query}
	{empty}
	failedTitle={m.documents_degrees_load_failed()}
	selected={id === undefined ? undefined : degreeHref(id)}
	hrefs={degrees.hrefs}
	home={shellHref('services/degrees')}
	listLabel={m.documents_degrees_list_label()}
	detailLabel={m.documents_degrees_detail_label()}
>
	{#snippet list(open)}
		<DegreeList {degrees} selected={open} />
	{/snippet}
	{#snippet skeleton()}
		<ListSkeleton titled rows={[85]} lines={[2, 2]} />
	{/snippet}
	{#snippet emptyState()}
		<EmptyState
			size="page"
			icon={IconScroll}
			title={m.documents_degrees_empty_title()}
			body={m.documents_degrees_empty_body()}
		>
			<!-- Titles are recorded by the registry office, which is who to ask about a missing one. -->
			{#snippet action()}
				<Button href={shellHref('services/appointments')}>
					{m.documents_degrees_empty_action()}
				</Button>
			{/snippet}
		</EmptyState>
	{/snippet}
	{@render children()}
</DocumentsLayout>
