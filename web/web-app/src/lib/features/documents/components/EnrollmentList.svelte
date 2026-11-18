<script lang="ts">
	import IconCalendarBlank from '~icons/ph/calendar-blank';
	import { academicYearLabel } from '#lib/data/academicYear.ts';
	import { m } from '#lib/paraglide/messages.js';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import type { Tone } from '#lib/ui/types.ts';
	import { enrollmentHref, type Enrollments } from '../enrollments.svelte.ts';
	import type { AnnualEnrollment, EnrollmentStatus } from '../enrollments.ts';
	import { courseYearLabel, statusLabel, typeLabel } from '../enrollmentView.ts';
	import { useDocumentsView } from '../view.ts';
	import { joinParts } from '../text.ts';
	import ProgrammeSummary from './ProgrammeSummary.svelte';
	import RenewalCard from './RenewalCard.svelte';

	interface Props {
		enrollments: Enrollments;
		/** The year open beside the list. */
		selected?: string;
	}

	let { enrollments, selected }: Props = $props();

	const view = useDocumentsView();
	const renewal = $derived(enrollments.renewal);
	const latest = $derived(enrollments.years.at(0));
	const first = $derived(enrollments.years.at(-1));

	const tones: Record<EnrollmentStatus, Tone> = {
		active: 'accent',
		canceled: 'danger',
		suspended: 'warning',
		unknown: 'neutral'
	};

	/* The course year and the status, then what sets the year apart from a regular one. */
	function line(year: AnnualEnrollment): string {
		const unusual = year.type === 'out_of_course' || year.type === 'repeating';
		return joinParts([
			courseYearLabel(year.courseYear),
			statusLabel(year.status),
			unusual ? typeLabel(year) : null,
			year.partTime !== null ? m.documents_enrollment_group_part_time() : null
		]);
	}
</script>

<div class="list">
	{#if renewal.kind !== 'none'}<RenewalCard {renewal} />{/if}

	{#if enrollments.years.length > 0}
		<!-- The pane around the list is named already. -->
		<ListGroup>
			{#each enrollments.years as year (year.academicYear)}
				{@const href = enrollmentHref(year.academicYear)}
				<ListRow
					title={academicYearLabel(year.academicYear)}
					description={line(year)}
					icon={IconCalendarBlank}
					iconTone={tones[year.status]}
					{href}
					selected={href === selected}
				/>
			{/each}
		</ListGroup>
	{/if}

	<!-- Beside the list the overview pane shows it; alone, it closes the page. -->
	{#if !view.wide && latest && first}
		<ProgrammeSummary {latest} {first} compact />
	{/if}
</div>

<style>
	.list {
		display: grid;
		gap: var(--sp-5);
	}
</style>
