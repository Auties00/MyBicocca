<script lang="ts">
	import IconCalendarPlus from '~icons/ph/calendar-plus';
	import IconHeadset from '~icons/ph/headset-duotone';
	import IconVideoCamera from '~icons/ph/video-camera';
	import { now } from '#lib/data/clock.svelte.ts';
	import { romeDate } from '#lib/format/rome.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import BookingCard from '#lib/ui/BookingCard.svelte';
	import BookingCardSkeleton from '#lib/ui/BookingCardSkeleton.svelte';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import SectionHeader from '#lib/ui/SectionHeader.svelte';
	import { displayName, serviceSection } from '../directory.ts';
	import { sectionLabels, shortWhen } from '../format.ts';
	import { directoryHref, reservationHref } from '../links.ts';
	import { split, type Reservation } from '../reservation.ts';
	import { useReservations } from '../useReservations.svelte.ts';
	import AddBookingDialog from './AddBookingDialog.svelte';
	import SectionsCard from './SectionsCard.svelte';

	const reservations = useReservations();

	const time = $derived(now.current.getTime());
	let adding = $state(false);

	const lists = $derived(split(reservations.data ?? [], time));
	const loaded = $derived(reservations.data !== undefined);
	const empty = $derived(loaded && lists.upcoming.length === 0 && lists.past.length === 0);
	const gate = new LoadingGate(() => !loaded);

	const subtitle = $derived(
		!loaded
			? undefined
			: lists.upcoming.length > 0
				? m.appointments_upcoming_count({ count: lists.upcoming.length })
				: m.appointments_none_upcoming()
	);
	const ids = $props.id();
</script>

<svelte:head>
	<title>{m.appointments_title()} · {m.app_name()}</title>
</svelte:head>

{#snippet card(reservation: Reservation)}
	{@const ended = reservation.end < time}
	<BookingCard
		href={reservationHref(reservation.code)}
		date={romeDate(reservation.start)}
		past={ended}
		title={displayName(reservation.serviceName)}
		subtitle={sectionLabels[
			serviceSection({ name: reservation.serviceName, group: reservation.serviceGroup })
		]()}
		status={ended ? { label: m.appointments_ended() } : undefined}
		when={shortWhen(reservation, time)}
		place={reservation.webConferenceUrl
			? { label: m.appointments_video_call(), icon: IconVideoCamera }
			: reservation.areaName
				? { label: reservation.areaName, name: true }
				: { label: m.appointments_location_tbc(), pending: true }}
	/>
{/snippet}

<Page width="dashboard">
	<PageHeader
		title={m.appointments_title()}
		{subtitle}
		back={{ href: shellHref('services'), label: m.shell_services() }}
		loading={!loaded && { eyebrow: false }}
	>
		<!-- Always there, as on Android: the header keeps its height whatever the list turns out to be. -->
		{#snippet actions()}
			<Button variant="solid" icon={IconCalendarPlus} href={directoryHref()}>
				{m.appointments_book_action()}
			</Button>
		{/snippet}
	</PageHeader>

	<div class="layout">
		<div class="mine">
			{#if !loaded}
				<section class="group" aria-busy="true" aria-labelledby="{ids}-upcoming">
					<SectionHeader id="{ids}-upcoming" title={m.appointments_upcoming_heading()} />
					<div class="cards">
						{#if gate.visible}<BookingCardSkeleton />{/if}
					</div>
				</section>
			{:else if empty}
				<EmptyState
					size="section"
					icon={IconHeadset}
					title={m.appointments_empty_title()}
					body={m.appointments_empty_body()}
				/>
			{:else}
				{#if lists.upcoming.length > 0}
					<section class="group" aria-labelledby="{ids}-upcoming">
						<SectionHeader id="{ids}-upcoming" title={m.appointments_upcoming_heading()} />
						<ul class="cards" role="list">
							{#each lists.upcoming as reservation (reservation.code)}
								<li>{@render card(reservation)}</li>
							{/each}
						</ul>
					</section>
				{/if}
				{#if lists.past.length > 0}
					<section class="group" aria-labelledby="{ids}-past">
						<SectionHeader id="{ids}-past" title={m.appointments_past_heading()} />
						<ul class="cards" role="list">
							{#each lists.past as reservation (reservation.code)}
								<li>{@render card(reservation)}</li>
							{/each}
						</ul>
					</section>
				{/if}
			{/if}

			<p class="note">
				{m.appointments_device_note()}
				<button type="button" class="link" onclick={() => (adding = true)}>
					{m.appointments_device_note_action()}
				</button>
			</p>
		</div>

		<SectionsCard headingId="{ids}-directory" />
	</div>
</Page>

<AddBookingDialog bind:open={adding} />

<style>
	.layout {
		display: grid;
		align-items: start;
		gap: var(--sp-8) var(--sp-8);
	}

	/* Wide: the appointments, and beside them where to book the next one. */
	@container page (width >= 880px) {
		.layout {
			grid-template-columns: minmax(0, 1.35fr) minmax(300px, 1fr);
		}
	}

	.mine {
		display: grid;
		align-content: start;
		gap: var(--page-gap);
	}

	.group {
		display: grid;
		gap: var(--sp-4);
	}

	/* As many columns as cards wide enough for the place's name fit. */
	.cards {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(min(100%, 340px), 1fr));
		gap: var(--sp-3);
		margin: 0;
		padding: 0;
		list-style: none;
		min-block-size: 132px;
	}

	.cards > li {
		display: grid;
	}

	.note {
		max-inline-size: 60ch;
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}

	.link {
		display: inline;
		padding: 0;
		border: 0;
		background: none;
		color: var(--accent-text);
		font: inherit;
		font-weight: 600;
		text-decoration: underline;
		text-decoration-thickness: 1px;
		text-underline-offset: 3px;
		cursor: pointer;
	}

	.link:hover {
		text-decoration-thickness: 2px;
	}

	.link:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
		border-radius: 2px;
	}
</style>
