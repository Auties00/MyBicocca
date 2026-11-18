<script lang="ts">
	import IconCalendarPlus from '~icons/ph/calendar-plus';
	import IconCalendarBlank from '~icons/ph/calendar-blank-duotone';
	import { now } from '#lib/data/clock.svelte.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import BookingCard from '#lib/ui/BookingCard.svelte';
	import BookingCardSkeleton from '#lib/ui/BookingCardSkeleton.svelte';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import SectionHeader from '#lib/ui/SectionHeader.svelte';
	import { countdown, kindLabel, weekdayTime } from '../format.ts';
	import { splitBookings, toBooking, toExamCall, type Booking, type ExamCall } from '../model.ts';
	import { bookHref, bookingHref } from '../links.ts';
	import { bookingsQuery, examCallsQuery } from '../queries.ts';
	import OpenCallsRow from './OpenCallsRow.svelte';
	import OpenCallsRowSkeleton from './OpenCallsRowSkeleton.svelte';
	import PastBookings from './PastBookings.svelte';

	const bookings = query(() => session.current && bookingsQuery(session.current));
	const calls = query(() => session.current && examCallsQuery(session.current));

	const all = $derived(
		(bookings.data ?? []).map(toBooking).filter((b): b is Booking => b !== undefined)
	);
	const split = $derived(splitBookings(all, now.current));
	const bookable = $derived(
		(calls.data ?? []).map(toExamCall).filter((c): c is ExamCall => c !== undefined)
	);
	const gate = new LoadingGate(() => bookings.status === 'pending');

	/* How soon, for the next week or so; today's in the accent. */
	function soon(booking: Booking) {
		const label = booking.at && countdown(booking.at, now.current);
		if (!label) return undefined;
		const today = booking.at?.toDateString() === now.current.toDateString();
		return { label, tone: today ? ('accent' as const) : ('neutral' as const) };
	}

	const subtitle = $derived(
		bookings.data === undefined
			? undefined
			: split.upcoming.length > 0
				? m.exams_upcoming_count({ count: split.upcoming.length })
				: m.exams_none_scheduled()
	);
	/* The empty and the error state carry the page's one action; the header doesn't add another. */
	const empty = $derived(bookings.data !== undefined && split.upcoming.length === 0);
	const updatedAt = $derived(bookings.updatedAt ? new Date(bookings.updatedAt) : undefined);
	const upcomingId = $props.id();
</script>

<svelte:head>
	<title>{m.exams_title()} · {m.app_name()}</title>
</svelte:head>

<Page width="dashboard">
	<PageHeader
		title={m.exams_title()}
		{subtitle}
		back={{ href: shellHref('services'), label: m.exams_services() }}
		loading={bookings.data === undefined &&
			bookings.status === 'pending' && { eyebrow: false, title: 1 }}
	>
		{#snippet actions()}
			{#if !empty && bookings.status !== 'error'}
				<Button variant="solid" icon={IconCalendarPlus} href={bookHref()}>
					{m.exams_book_action()}
				</Button>
			{/if}
		{/snippet}
	</PageHeader>

	{#if bookings.error && bookings.data}
		<ErrorState
			variant="stale"
			message={errorMessage(bookings.error)}
			{updatedAt}
			onretry={bookings.refresh}
			retrying={bookings.refreshing}
		/>
	{/if}

	{#if bookings.status === 'error' && bookings.error}
		<ErrorState
			title={m.exams_bookings_failed()}
			message={errorMessage(bookings.error)}
			onretry={bookings.refresh}
			retrying={bookings.refreshing}
		/>
	{:else if bookings.data === undefined}
		<section class="upcoming loading" aria-busy="true" aria-label={m.exams_upcoming_heading()}>
			<SectionHeader title={m.exams_upcoming_heading()} />
			<div class="cards">
				{#if gate.visible}
					<BookingCardSkeleton />
					<BookingCardSkeleton />
				{/if}
			</div>
		</section>
	{:else}
		<div class={['layout', { history: split.past.length > 0 }]}>
			<div class="now">
				{#if split.upcoming.length > 0}
					<section class="upcoming" aria-labelledby={upcomingId}>
						<SectionHeader id={upcomingId} title={m.exams_upcoming_heading()} />
						<ul class="cards" role="list">
							{#each split.upcoming as booking (booking.slug)}
								<li>
									<BookingCard
										href={bookingHref(booking.slug)}
										date={booking.at ?? undefined}
										title={booking.title || m.exams_kind_unknown()}
										subtitle={kindLabel(booking.kind, booking.callType)}
										status={soon(booking)}
										when={booking.at ? weekdayTime(booking.at, booking.timed) : m.exams_time_tbc()}
										place={booking.room
											? { label: booking.room, name: true }
											: { label: m.exams_room_tbc(), pending: true }}
									/>
								</li>
							{/each}
						</ul>
					</section>
				{:else}
					<EmptyState
						size="page"
						icon={IconCalendarBlank}
						title={m.exams_empty_title()}
						body={m.exams_empty_body()}
					>
						{#snippet action()}
							<Button variant="solid" icon={IconCalendarPlus} href={bookHref()}>
								{m.exams_book_action()}
							</Button>
						{/snippet}
					</EmptyState>
				{/if}

				{#if calls.data && bookable.length > 0}
					<OpenCallsRow calls={bookable} now={now.current} />
				{:else if calls.data === undefined && calls.status === 'pending'}
					<OpenCallsRowSkeleton />
				{/if}
			</div>

			{#if split.past.length > 0}
				<PastBookings bookings={split.past} />
			{/if}
		</div>
	{/if}
</Page>

<style>
	.layout,
	.now {
		display: grid;
		align-items: start;
		gap: var(--page-gap);
	}

	/* Wide: what is coming on the left, the history beside it. */
	@container page (width >= 920px) {
		.layout.history {
			grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
			column-gap: var(--sp-8);
		}
	}

	.loading {
		max-inline-size: var(--w-reading);
	}

	.upcoming {
		display: grid;
		gap: var(--sp-4);
		container-type: inline-size;
	}

	.cards {
		display: grid;
		grid-template-columns: minmax(0, 1fr);
		gap: var(--sp-3);
		margin: 0;
		padding: 0;
		list-style: none;
		min-block-size: 132px;
	}

	.cards > li {
		display: grid;
	}

	/* Two cards a row only where each keeps its title and its date on one line. */
	@container (width >= 720px) {
		.cards {
			grid-template-columns: repeat(2, minmax(0, 1fr));
		}
	}
</style>
