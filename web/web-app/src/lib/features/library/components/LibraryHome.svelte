<script lang="ts">
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconArmchair from '~icons/ph/armchair';
	import IconBooks from '~icons/ph/books';
	import IconDotsThree from '~icons/ph/dots-three-outline-vertical';
	import IconEnvelope from '~icons/ph/envelope-simple-open';
	import IconLinkBreak from '~icons/ph/link-break';
	import IconCalendarSlash from '~icons/ph/calendar-slash';
	import { Clock } from '#lib/data/clock.svelte.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { formatRelative } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Banner from '#lib/ui/Banner.svelte';
	import BookingCard from '#lib/ui/BookingCard.svelte';
	import BookingCardSkeleton from '#lib/ui/BookingCardSkeleton.svelte';
	import Button from '#lib/ui/Button.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Menu from '#lib/ui/Menu.svelte';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import SectionHeader from '#lib/ui/SectionHeader.svelte';
	import { libraryLinks } from '../account.svelte.ts';
	import { expectedBookings, rememberBookings } from '../bookingCount.ts';
	import { whenShort } from '../format.ts';
	import { bookHref, cancelHref, loginHref, reservationHref } from '../links.ts';
	import { linkedReservations, refreshWhileVisible } from '../live.svelte.ts';
	import { activeReservations, bookableLibraries, type Reservation } from '../model.ts';
	import { librariesQuery } from '../queries.ts';
	import LibraryCard from './LibraryCard.svelte';
	import LibraryCardSkeleton from './LibraryCardSkeleton.svelte';
	import SectionNote from './SectionNote.svelte';

	const clock = new Clock(30_000);
	const own = linkedReservations();
	const reservations = own.reservations;
	const libraries = query(() => session.current && librariesQuery(session.current));
	refreshWhileVisible(() => libraries.refresh());

	const accountId = $derived(session.current?.account.id);
	const link = $derived(own.link);
	const linked = $derived(link?.auth !== undefined);
	const active = $derived(
		reservations.data ? activeReservations(reservations.data, clock.now) : undefined
	);
	const list = $derived(libraries.data ? bookableLibraries(libraries.data) : undefined);
	const firstOpen = $derived(list?.find((library) => library.open) ?? list?.[0]);

	/* As many skeleton cards as last time, so the libraries below never move once they load. */
	const expected = $derived(accountId ? expectedBookings(accountId) : 1);
	$effect(() => {
		if (accountId && active) rememberBookings(accountId, active.length);
	});

	const bookingsGate = new LoadingGate(() => active === undefined && !reservations.error);
	const librariesGate = new LoadingGate(() => list === undefined && !libraries.error);

	const bookingsFailed = $derived(reservations.error !== undefined && !reservations.data);
	const librariesFailed = $derived(libraries.error !== undefined && !libraries.data);
	/* Affluences down: one error for the page, not one per section. */
	const allFailed = $derived(
		linked &&
			bookingsFailed &&
			librariesFailed &&
			reservations.error?.kind === libraries.error?.kind
	);

	const subtitle = $derived.by(() => {
		if (link === undefined) return undefined;
		if (!linked) return m.library_subtitle_signed_out();
		if (active === undefined) return undefined;
		return active.length === 0
			? m.library_no_bookings()
			: m.library_booking_count({ count: active.length });
	});

	/* A bar holds the subtitle's line while the bookings load, so the page doesn't move. */
	const subtitleLoading = $derived(
		link === undefined || (linked && reservations.data === undefined && !reservations.error)
	);
	/* An empty line holds the description's place too. */
	const NBSP = String.fromCharCode(160);

	/*
	 * When the bookings were last synced, under them; shown from the first load so the line never
	 * appears late. A failed sync says it in its banner instead.
	 */
	const synced = $derived(
		reservations.updatedAt === undefined
			? m.library_syncing()
			: m.ui_updated_ago({
					// The clock ticks every half minute: a sync since then is "now", never "in a minute".
					time: formatRelative(Math.min(reservations.updatedAt, clock.now), new Date(clock.now))
				})
	);

	const date = (at: number | undefined) => (at ? new Date(at) : undefined);

	let unlinking = $state(false);
	let connectHeading = $state<HTMLHeadingElement>();

	async function unlink() {
		const account = session.current?.account;
		if (!account) return;
		await libraryLinks.unlink(account);
	}

	function showLibraries() {
		document.getElementById('library-libraries')?.scrollIntoView({ block: 'start' });
	}

	/* Spins for a refresh the student asked for, not for the occupancy's own every minute. */
	let refreshing = $state(false);

	async function retryAll() {
		refreshing = true;
		try {
			await Promise.all([reservations.refresh(), libraries.refresh()]);
		} finally {
			refreshing = false;
		}
	}
</script>

<svelte:head>
	<title>{m.library_title()} · {m.app_name()}</title>
</svelte:head>

{#snippet noBookings()}
	<SectionNote
		icon={IconArmchair}
		title={m.library_no_bookings_title()}
		body={m.library_no_bookings_body()}
	>
		{#snippet action()}
			{#if firstOpen}
				<Button variant="solid" icon={IconArmchair} href={bookHref(firstOpen.id)}>
					{m.library_book_at({ library: firstOpen.name })}
				</Button>
			{:else}
				<Button variant="solid" icon={IconArmchair} onclick={showLibraries}>
					{m.library_book_seat()}
				</Button>
			{/if}
		{/snippet}
	</SectionNote>
{/snippet}

{#snippet card(reservation: Reservation)}
	{@const ongoing = reservation.startsAt <= clock.now && clock.now < reservation.endsAt}
	<BookingCard
		href={reservationHref(reservation.id)}
		date={reservation.start}
		title={reservation.library || m.library_reservation()}
		status={ongoing
			? { label: m.library_ongoing(), tone: 'accent' }
			: reservation.state === 'awaiting'
				? { label: m.library_awaiting() }
				: undefined}
		when={whenShort(reservation, clock.now)}
		place={reservation.place ? { label: reservation.place } : undefined}
	>
		{#snippet subtitle()}
			<span translate="no">{reservation.seatShort}</span>
			{#if reservation.outlet}· {m.library_power_outlet()}{/if}
		{/snippet}
	</BookingCard>
{/snippet}

<Page width="dashboard">
	<PageHeader
		title={m.library_title()}
		{subtitle}
		back={{ href: shellHref('services'), label: m.shell_services() }}
		loading={subtitleLoading && { eyebrow: false }}
	/>

	{#if allFailed && reservations.error}
		<ErrorState
			title={m.library_service_down()}
			message={errorMessage(reservations.error)}
			onretry={retryAll}
			retrying={refreshing}
		/>
	{:else}
		<div class="layout">
			<section class="section" aria-labelledby="library-bookings">
				<SectionHeader
					id="library-bookings"
					title={m.library_bookings_heading()}
					description={link === undefined
						? NBSP
						: linked && link.email
							? m.library_linked_as({ email: link.email })
							: undefined}
				>
					{#snippet actions()}
						<IconButton
							icon={IconArrowClockwise}
							label={m.common_refresh()}
							variant="outline"
							size="sm"
							loading={refreshing}
							onclick={retryAll}
						/>
						<Menu
							label={m.library_account_menu()}
							placement="bottom-end"
							items={[
								{
									label: m.library_cancel_link_title(),
									icon: IconCalendarSlash,
									href: cancelHref()
								},
								...(linked
									? [
											{
												label: m.library_unlink(),
												icon: IconLinkBreak,
												tone: 'danger' as const,
												onselect: () => (unlinking = true)
											}
										]
									: [])
							]}
						>
							{#snippet trigger(props)}
								<IconButton
									{...props}
									icon={IconDotsThree}
									label={m.library_account_menu()}
									variant="ghost"
									size="sm"
								/>
							{/snippet}
						</Menu>
					{/snippet}
				</SectionHeader>

				{#if link !== undefined && !linked}
					{#if link.expired}
						<Banner tone="warning" message={m.library_session_expired()} />
					{/if}
					<SectionNote
						icon={IconEnvelope}
						tone="accent"
						title={m.library_verify_email()}
						body={m.library_verify_email_body()}
						bind:heading={connectHeading}
					>
						{#snippet action()}
							<Button variant="solid" icon={IconEnvelope} href={loginHref()}>
								{m.library_verify()}
							</Button>
						{/snippet}
					</SectionNote>
				{:else if bookingsFailed && reservations.error}
					<ErrorState
						title={m.library_bookings_failed()}
						message={errorMessage(reservations.error)}
						onretry={reservations.refresh}
						retrying={reservations.refreshing}
					/>
				{:else if active === undefined && expected === 0}
					<div class="hidden" aria-hidden="true">{@render noBookings()}</div>
				{:else if active === undefined}
					<ul class="bookings" aria-busy="true" aria-label={m.library_bookings_heading()}>
						{#each { length: expected }, index (index)}
							<li class={{ hidden: !bookingsGate.visible }}><BookingCardSkeleton /></li>
						{/each}
					</ul>
				{:else}
					{#if reservations.error}
						<ErrorState
							variant="stale"
							message={errorMessage(reservations.error)}
							updatedAt={date(reservations.updatedAt)}
							onretry={reservations.refresh}
							retrying={reservations.refreshing}
						/>
					{/if}
					{#if active.length === 0}
						{@render noBookings()}
					{:else}
						<ul class="bookings">
							{#each active as reservation (reservation.id)}
								<li>{@render card(reservation)}</li>
							{/each}
						</ul>
					{/if}
				{/if}
				{#if linked && !reservations.error}
					<p class="synced">{synced}</p>
				{/if}
			</section>

			<section class="section" aria-labelledby="library-libraries">
				<SectionHeader id="library-libraries" title={m.library_libraries_heading()} />

				{#if librariesFailed && libraries.error}
					<ErrorState
						title={m.library_libraries_loading_failed()}
						message={errorMessage(libraries.error)}
						onretry={libraries.refresh}
						retrying={libraries.refreshing}
					/>
				{:else if list === undefined}
					<ul class="libraries" aria-busy="true" aria-label={m.library_libraries_heading()}>
						{#each { length: 2 }, index (index)}
							<li class={{ hidden: !librariesGate.visible }}><LibraryCardSkeleton /></li>
						{/each}
					</ul>
				{:else}
					{#if libraries.error}
						<ErrorState
							variant="stale"
							message={errorMessage(libraries.error)}
							updatedAt={date(libraries.updatedAt)}
							onretry={libraries.refresh}
							retrying={libraries.refreshing}
						/>
					{/if}
					{#if list.length === 0}
						<SectionNote
							icon={IconBooks}
							title={m.library_no_libraries()}
							body={m.library_no_libraries_body()}
						/>
					{:else}
						<ul class="libraries">
							{#each list as library, index (library.id)}
								<li><LibraryCard {library} eager={index < 2} /></li>
							{/each}
						</ul>
					{/if}
				{/if}
			</section>
		</div>
	{/if}
</Page>

<ConfirmDialog
	bind:open={unlinking}
	title={m.library_unlink_title()}
	message={m.library_unlink_body()}
	confirmLabel={m.library_unlink_confirm()}
	cancelLabel={m.ui_cancel()}
	tone="danger"
	onconfirm={unlink}
	returnFocus={() => connectHeading}
/>

<style>
	.layout {
		display: grid;
		align-items: start;
		gap: var(--page-gap);
	}

	/* Wide: the student's bookings beside the libraries, which sit two by two. */
	@container page (width >= 1000px) {
		.layout {
			grid-template-columns: minmax(300px, 1fr) minmax(0, 2fr);
			column-gap: var(--sp-8);
		}
	}

	.section {
		display: grid;
		align-content: start;
		gap: var(--sp-4);
		min-inline-size: 0;
	}

	.bookings,
	.libraries {
		display: grid;
		gap: var(--sp-3);
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.bookings {
		grid-template-columns: repeat(auto-fill, minmax(min(100%, 340px), 1fr));
	}

	.libraries {
		grid-template-columns: repeat(auto-fill, minmax(min(100%, 280px), 1fr));
		gap: var(--sp-4);
	}

	@container page (width >= 1000px) {
		.bookings {
			grid-template-columns: minmax(0, 1fr);
		}
	}

	.synced {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	/* Holds the skeleton's place before it shows, so nothing moves when it does. */
	.hidden {
		visibility: hidden;
	}
</style>
