<script lang="ts">
	import IconCalendarSlash from '~icons/ph/calendar-slash';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Button from '#lib/ui/Button.svelte';
	import Outcome from '#lib/ui/Outcome.svelte';
	import { sentenceWhen } from '../format.ts';
	import { bookHref } from '../links.ts';
	import { canCancel, type Booking } from '../model.ts';

	interface Props {
		booking: Booking;
		now: Date;
	}

	let { booking, now }: Props = $props();

	const course = $derived(booking.title || m.exams_call_unknown());
	const date = $derived(booking.at ? sentenceWhen(booking.at, booking.timed) : m.exams_time_tbc());
	/* Cancelling and booking close together, at the end of the registration window. */
	const reopen = $derived(canCancel(booking, now));
</script>

<Outcome
	icon={IconCalendarSlash}
	tone="neutral"
	title={m.exams_cancelled_title()}
	body={m.exams_cancelled_body({ course, date })}
>
	{#snippet actions()}
		<Button variant="solid" href={shellHref('services/exams')}>{m.exams_back_to_bookings()}</Button>
		{#if reopen}
			<Button href={bookHref({ call: booking.slug })}>{m.exams_book_again()}</Button>
		{/if}
	{/snippet}
</Outcome>
