<script lang="ts">
	import IconCalendarSlash from '~icons/ph/calendar-slash';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import Outcome from '#lib/ui/Outcome.svelte';
	import { longWhen } from '../format.ts';
	import { appointmentsHref, bookingHref, directoryHref } from '../links.ts';
	import type { Reservation } from '../reservation.ts';

	interface Props {
		reservation: Reservation;
	}

	let { reservation }: Props = $props();
</script>

<Outcome
	icon={IconCalendarSlash}
	tone="neutral"
	title={m.appointments_cancelled_title()}
	body={m.appointments_cancelled_body({ date: longWhen(reservation) })}
>
	{#snippet actions()}
		<Button variant="solid" href={appointmentsHref()}>{m.appointments_back_to_list()}</Button>
		<Button
			href={reservation.serviceId === null ? directoryHref() : bookingHref(reservation.serviceId)}
		>
			{m.appointments_book_again()}
		</Button>
	{/snippet}
</Outcome>
