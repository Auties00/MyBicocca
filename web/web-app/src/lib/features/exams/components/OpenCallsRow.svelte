<script lang="ts">
	import IconCalendarPlus from '~icons/ph/calendar-plus';
	import { formatDate } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import { bookHref } from '../links.ts';
	import { daysToClose, windowStatus, type ExamCall } from '../model.ts';

	interface Props {
		calls: readonly ExamCall[];
		now: Date;
	}

	let { calls, now }: Props = $props();

	const open = $derived(calls.filter((call) => windowStatus(call, now) === 'open'));

	/** The open call that stops taking bookings first, else when the next window opens. */
	const hint = $derived.by(() => {
		const closing = open
			.filter((call) => call.closesAt)
			.sort((a, b) => (a.closesAt?.getTime() ?? 0) - (b.closesAt?.getTime() ?? 0))[0];
		if (closing?.closesAt) {
			return daysToClose(closing, now) === 0
				? m.exams_next_closing_today({ course: closing.title })
				: m.exams_next_closing({
						course: closing.title,
						date: formatDate(closing.closesAt, 'dayMonth')
					});
		}
		const opening = calls
			.filter((call) => call.opensAt && windowStatus(call, now) === 'upcoming')
			.sort((a, b) => (a.opensAt?.getTime() ?? 0) - (b.opensAt?.getTime() ?? 0))[0];
		return opening?.opensAt
			? m.exams_next_opening({ date: formatDate(opening.opensAt, 'dayMonth') })
			: undefined;
	});
</script>

<ListGroup label={m.exams_book_title()}>
	<ListRow
		title={open.length > 0 ? m.exams_open_calls({ count: open.length }) : m.exams_open_calls_none()}
		description={hint}
		icon={IconCalendarPlus}
		iconTone="accent"
		href={bookHref()}
	/>
</ListGroup>
