<script lang="ts">
	import type { ResolvedPathname } from '$app/types';
	import IconCheckCircle from '~icons/ph/check-circle-fill';
	import { formatTime } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import DateBadge from '#lib/ui/DateBadge.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import { callKindLabel, callState } from '../format.ts';
	import { bookingHref } from '../links.ts';
	import {
		callLabel,
		courseRows,
		sameDayCalls,
		windowStatus,
		type Booking,
		type CourseCalls,
		type CourseRow,
		type ExamCall
	} from '../model.ts';

	interface Props {
		groups: readonly CourseCalls[];
		/** Upcoming bookings, shown among their course's calls. */
		booked: readonly Booking[];
		/** The open call's or booking's slug. */
		selected?: string;
		href: (call: ExamCall) => ResolvedPathname;
		now: Date;
	}

	let { groups, booked, selected, href, now }: Props = $props();

	/* "Booked" leads the line rather than sitting in a chip, which a narrow list would squeeze. */
	function bookedLine(booking: Booking): string {
		const time = booking.at && booking.timed ? formatTime(booking.at) : m.exams_time_tbc();
		return [m.exams_booked_chip(), time, booking.room].filter(Boolean).join(' · ');
	}

	function description(call: ExamCall, twins: Set<string>): string {
		return [
			twins.has(call.slug) ? callLabel(call.description, call.title) : null,
			call.date && call.timed ? formatTime(call.date) : null,
			callState(call, now).window
		]
			.filter(Boolean)
			.join(' · ');
	}

	/*
	 * With a booking among them, the course's calls count apart from it: "1 to book · 1 booked",
	 * where a call whose registrations closed is not one to book.
	 */
	function counts(group: CourseCalls, rows: readonly CourseRow[]): string {
		const bookings = rows.filter((row) => row.type === 'booking').length;
		if (bookings === 0) return m.exams_calls_count({ count: group.calls.length });
		const toBook = group.calls.filter((call) => windowStatus(call, now) !== 'closed').length;
		return [
			toBook > 0 ? m.exams_to_book_count({ count: toBook }) : null,
			m.exams_booked_count({ count: bookings })
		]
			.filter(Boolean)
			.join(' · ');
	}
</script>

<!--
	The course is the group's heading, which moving from link to link skips: each row says it too,
	for screen readers only.
-->
{#snippet course(title: string)}
	<span class="sr-only">{title},</span>
{/snippet}

{#snippet closing()}
	<span class="closing">
		<StatusChip status="warning" label={m.exams_status_closing()} size="sm" />
	</span>
{/snippet}

{#snippet bookedMark()}
	<span class="booked"><IconCheckCircle aria-hidden="true" /></span>
{/snippet}

<div class="groups">
	{#each groups as group (group.key)}
		{@const twins = sameDayCalls(group.calls)}
		{@const name = group.title || m.exams_call_unknown()}
		{@const rows = courseRows(group, booked)}
		<ListGroup
			title={name}
			description={[group.code, counts(group, rows)].filter(Boolean).join(' · ')}
			level={2}
			size="sm"
		>
			{#each rows as row (row.id)}
				{#if row.type === 'call'}
					{@const call = row.call}
					<ListRow
						title={callKindLabel(call.kind, call.callType)}
						description={description(call, twins)}
						href={href(call)}
						selected={call.slug === selected}
						lines={3}
						trailing={callState(call, now).status === 'warning' ? closing : undefined}
						chevron
					>
						{#snippet leading()}
							{@render course(name)}
							{#if call.date}
								<DateBadge
									date={call.date}
									weekday
									tone={windowStatus(call, now) === 'open' ? 'accent' : 'neutral'}
								/>
							{/if}
						{/snippet}
					</ListRow>
				{:else}
					{@const booking = row.booking}
					<ListRow
						title={callKindLabel(booking.kind, booking.callType)}
						description={bookedLine(booking)}
						href={bookingHref(booking.slug)}
						selected={booking.slug === selected}
						lines={1}
						trailing={bookedMark}
						chevron
					>
						{#snippet leading()}
							{@render course(name)}
							{#if booking.at}<DateBadge date={booking.at} weekday tone="neutral" />{/if}
						{/snippet}
					</ListRow>
				{/if}
			{/each}
		</ListGroup>
	{/each}
</div>

<style>
	.groups {
		display: grid;
		gap: var(--sp-4);
	}

	.booked {
		display: flex;
		color: var(--success);
	}

	.booked :global(svg) {
		inline-size: 20px;
		block-size: 20px;
	}

	/* The description already says by when: the chip is a highlight a narrow row can spare. */
	@container (width < 300px) {
		.closing {
			display: none;
		}
	}
</style>
