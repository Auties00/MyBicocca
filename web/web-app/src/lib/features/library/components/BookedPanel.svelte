<script lang="ts" module>
	export interface Booked {
		/** Affluences' id for it, when it gave one. */
		id: number | null;
		library: string;
		place: string | null;
		zone: string;
		seat: string;
		/** On the library's wall clock. */
		start: Date;
		end: Date;
		duration: number;
	}
</script>

<script lang="ts">
	import IconCalendarPlus from '~icons/ph/calendar-plus';
	import IconCheck from '~icons/ph/check-bold';
	import { romeInstant } from '#lib/format/rome.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import { saveCalendarEvent } from '#lib/ui/files.ts';
	import Outcome from '#lib/ui/Outcome.svelte';
	import { durationLabel, whenLong } from '../format.ts';
	import { homeHref, reservationHref } from '../links.ts';

	interface Props {
		booked: Booked;
	}

	let { booked }: Props = $props();

	function addToCalendar() {
		saveCalendarEvent(
			{
				uid:
					booked.id === null
						? `library-${booked.start.getTime()}-${booked.seat.replace(/\W+/g, '')}@mybicocca`
						: `library-${booked.id}@mybicocca`,
				title: `${booked.library} · ${booked.seat}`,
				start: romeInstant(booked.start),
				end: romeInstant(booked.end),
				location: booked.place ? `${booked.library}, ${booked.place}` : booked.library,
				description: booked.zone
			},
			m.library_ics_filename()
		);
	}
</script>

<Outcome icon={IconCheck} title={m.library_booked()} body={m.library_booked_body()}>
	<p class="what">
		<span>{booked.library}</span>
		<span aria-hidden="true">·</span>
		<span>{booked.zone}</span>
		<span aria-hidden="true">·</span>
		<strong translate="no">{booked.seat}</strong>
	</p>
	<p class="when">{whenLong(booked.start, booked.end)} · {durationLabel(booked.duration)}</p>
	{#snippet actions()}
		{#if booked.id !== null}
			<Button variant="solid" href={reservationHref(booked.id)}>{m.library_view_booking()}</Button>
		{:else}
			<Button variant="solid" href={homeHref()}>{m.library_go_to_bookings()}</Button>
		{/if}
		<Button icon={IconCalendarPlus} onclick={addToCalendar}>{m.library_add_calendar()}</Button>
	{/snippet}
</Outcome>

<style>
	.what {
		display: flex;
		flex-wrap: wrap;
		gap: 4px 8px;
		font: var(--type-body);
	}

	.when {
		color: var(--text-muted);
		font: var(--type-ui);
		font-variant-numeric: tabular-nums;
	}
</style>
