<script lang="ts">
	import IconCalendarX from '~icons/ph/calendar-x-duotone';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Badge from '#lib/ui/Badge.svelte';
	import Button from '#lib/ui/Button.svelte';
	import DetailHeader from '#lib/ui/DetailHeader.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import type { Status } from '#lib/ui/types.ts';
	import { useEnrollments } from '../enrollments.svelte.ts';
	import type { EnrollmentStatus } from '../enrollments.ts';
	import {
		courseYearLabel,
		enrollmentBadges,
		enrollmentSections,
		enrollmentTitle,
		statusLabel,
		typeLabel
	} from '../enrollmentView.ts';
	import { useDocumentsView } from '../view.ts';
	import { joinParts } from '../text.ts';
	import DetailSkeleton from './DetailSkeleton.svelte';
	import FactSections from './FactSections.svelte';

	interface Props {
		/** The academic year from the URL, as written there. */
		year: string;
	}

	let { year }: Props = $props();

	const enrollments = useEnrollments();
	const view = useDocumentsView();
	const query = enrollments.query;
	const loaded = $derived(query.data !== undefined);
	const gate = new LoadingGate(() => query.status === 'pending');
	const updatedAt = $derived(query.updatedAt ? new Date(query.updatedAt) : undefined);

	const academicYear = $derived(/^\d{4}$/.test(year) ? Number(year) : undefined);
	const enrollment = $derived(
		academicYear === undefined ? undefined : enrollments.find(academicYear)
	);
	/* Named from the URL while loading, so the title doesn't change when the data comes. */
	const named = $derived(academicYear !== undefined && (!loaded || enrollment !== undefined));
	const title = $derived(
		named && academicYear !== undefined
			? enrollmentTitle(academicYear)
			: m.shell_service_enrollments()
	);
	const subtitle = $derived.by(() => {
		if (enrollment) {
			return joinParts([courseYearLabel(enrollment.courseYear), typeLabel(enrollment)]);
		}
		return undefined;
	});
	const badges = $derived(enrollment ? enrollmentBadges(enrollment) : []);
	const sections = $derived(enrollment ? enrollmentSections(enrollment) : []);

	const statuses: Record<EnrollmentStatus, Status> = {
		active: 'success',
		canceled: 'danger',
		suspended: 'warning',
		unknown: 'neutral'
	};
</script>

<svelte:head>
	<title
		>{named && academicYear !== undefined ? `${title} · ` : ''}{m.shell_service_enrollments()} · {m.app_name()}</title
	>
</svelte:head>

{#if !view.wide}
	<PageHeader
		{title}
		{subtitle}
		back={{ href: shellHref('services/enrollments'), label: m.shell_service_enrollments() }}
		loading={!loaded && query.status === 'pending' && { eyebrow: false, title: 1 }}
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

{#if loaded && !enrollment}
	<EmptyState
		size="page"
		icon={IconCalendarX}
		title={m.documents_enrollments_not_found_title()}
		body={m.documents_enrollments_not_found_body()}
	>
		{#snippet action()}
			<Button href={shellHref('services/enrollments')}>{m.documents_enrollments_back()}</Button>
		{/snippet}
	</EmptyState>
{:else}
	<article class="detail" aria-busy={!loaded || undefined}>
		{#if named}<DetailHeader {title} {subtitle} />{/if}

		{#if !enrollment}
			{#if gate.visible}<DetailSkeleton />{/if}
		{:else}
			<ul class="chips" role="list" aria-label={m.documents_enrollment_badges_label()}>
				<li>
					<StatusChip status={statuses[enrollment.status]} label={statusLabel(enrollment.status)} />
				</li>
				{#each badges as badge (badge.label)}
					<li><Badge tone={badge.tone}>{badge.label}</Badge></li>
				{/each}
			</ul>

			<FactSections {sections} level={view.wide ? 3 : 2} />
		{/if}
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

	.chips {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: var(--sp-2);
		margin: 0;
		padding: 0;
		list-style: none;
	}
</style>
