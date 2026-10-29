<script lang="ts">
	import IconCalendarPlus from '~icons/ph/calendar-plus';
	import IconCheck from '~icons/ph/check-bold';
	import IconTicket from '~icons/ph/ticket';
	import { toIsoDate } from '#lib/format/days.ts';
	import { romeInstant } from '#lib/format/rome.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import Outcome from '#lib/ui/Outcome.svelte';
	import { saveCalendarEvent } from '#lib/ui/files.ts';
	import { sentenceWhen } from '../format.ts';
	import { bookHref, bookingHref } from '../links.ts';
	import { callLabel, type ExamCall } from '../model.ts';

	interface Props {
		call: ExamCall;
		/** 3 under the call's heading beside the list. */
		level?: 2 | 3;
	}

	let { call, level = 2 }: Props = $props();

	const course = $derived(call.title || m.exams_call_unknown());
	const date = $derived(call.date ? sentenceWhen(call.date, call.timed) : m.exams_time_tbc());

	function addToCalendar() {
		if (!call.date) return;
		saveCalendarEvent(
			{
				uid: `exam-${call.slug}@mybicocca`,
				title: course,
				start: call.timed ? romeInstant(call.date) : toIsoDate(call.date),
				description: [
					callLabel(call.description, call.title),
					call.timed ? m.exams_ics_duration_unknown() : null
				]
					.filter(Boolean)
					.join('\n')
			},
			course
		);
	}
</script>

<Outcome
	icon={IconCheck}
	title={m.exams_booked_title()}
	body={m.exams_booked_body({ course, date })}
	{level}
>
	{#snippet actions()}
		<Button variant="solid" icon={IconTicket} href={bookingHref(call.slug)}>
			{m.exams_view_booking()}
		</Button>
		{#if call.date}
			<Button icon={IconCalendarPlus} onclick={addToCalendar}>{m.exams_add_calendar()}</Button>
		{/if}
		<Button variant="ghost" href={bookHref()}>{m.exams_book_another()}</Button>
	{/snippet}
</Outcome>
