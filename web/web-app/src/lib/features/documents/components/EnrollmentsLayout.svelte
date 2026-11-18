<script lang="ts">
	import type { Snippet } from 'svelte';
	import { resolve } from '$app/paths';
	import IconCalendarBlank from '~icons/ph/calendar-blank-duotone';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import { Enrollments, provideEnrollments } from '../enrollments.svelte.ts';
	import DocumentsLayout from './DocumentsLayout.svelte';
	import EnrollmentList from './EnrollmentList.svelte';
	import ListSkeleton from './ListSkeleton.svelte';
	import RenewalCard from './RenewalCard.svelte';

	interface Props {
		/** The open academic year, from the URL. */
		year?: string;
		/** The open year's page, or the overview beside the list when none is. */
		children: Snippet;
	}

	let { year, children }: Props = $props();

	const enrollments = new Enrollments();
	provideEnrollments(enrollments);

	const loaded = $derived(enrollments.query.data !== undefined);
	const empty = $derived(loaded && enrollments.years.length === 0);
	const subtitle = $derived(
		loaded && !empty
			? m.documents_enrollments_count({ count: enrollments.years.length })
			: undefined
	);
	/* Any year in the URL opens on its own page, which says so when it isn't one on record. */
	const selected = $derived(
		year === undefined ? undefined : resolve('/(app)/services/enrollments/[year]', { year })
	);
</script>

<!-- An open year names the page itself. -->
<svelte:head>
	{#if year === undefined}
		<title>{m.shell_service_enrollments()} · {m.app_name()}</title>
	{/if}
</svelte:head>

<DocumentsLayout
	title={m.shell_service_enrollments()}
	{subtitle}
	query={enrollments.query}
	{empty}
	failedTitle={m.documents_enrollments_load_failed()}
	{selected}
	hrefs={enrollments.hrefs}
	home={shellHref('services/enrollments')}
	listLabel={m.documents_enrollments_list_label()}
	detailLabel={m.documents_enrollments_detail_label()}
>
	{#snippet list(open)}
		<EnrollmentList {enrollments} selected={open} />
	{/snippet}
	{#snippet skeleton()}
		<ListSkeleton card rows={[38, 32]} />
	{/snippet}
	{#snippet emptyState()}
		<!-- With no year on record the renewal still applies, and it is what to do first. -->
		<div class="empty">
			{#if enrollments.renewal.kind !== 'none'}<RenewalCard renewal={enrollments.renewal} />{/if}
			<EmptyState
				size={enrollments.renewal.kind === 'none' ? 'page' : 'section'}
				icon={IconCalendarBlank}
				title={m.documents_enrollments_empty_title()}
				body={m.documents_enrollments_empty_body()}
			/>
		</div>
	{/snippet}
	{@render children()}
</DocumentsLayout>

<style>
	.empty {
		display: grid;
		gap: var(--sp-5);
		max-inline-size: var(--w-reading);
	}
</style>
