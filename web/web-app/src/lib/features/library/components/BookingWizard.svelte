<script lang="ts">
	import IconArmchair from '~icons/ph/armchair-duotone';
	import IconCalendarX from '~icons/ph/calendar-x-duotone';
	import IconEnvelope from '~icons/ph/envelope-simple';
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import type { ResolvedPathname } from '$app/types';
	import { Clock } from '#lib/data/clock.svelte.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import type { IsoDate } from '#lib/format/days.ts';
	import { romeWallClock } from '#lib/format/rome.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { libraryLinks } from '../account.svelte.ts';
	import { siteDate, slotRange } from '../format.ts';
	import {
		bookHref,
		bookedHref,
		homeHref,
		libraryHref,
		loginHref,
		reservationHref
	} from '../links.ts';
	import { bookableLibraries, cleanPlace, isFreeAt, openDays, toSeats, toZone } from '../model.ts';
	import {
		agreementsQuery,
		availableResourcesQuery,
		librariesQuery,
		reservationInfoQuery,
		resourceTypeFiltersQuery,
		siteQuery
	} from '../queries.ts';
	import { WIZARD_STEPS, choicesBefore, stepOf, type WizardChoices } from '../wizard.ts';
	import BookedPanel, { type Booked } from './BookedPanel.svelte';
	import ConfirmStep from './ConfirmStep.svelte';
	import SeatStep from './SeatStep.svelte';
	import Stepper, { type StepItem } from './Stepper.svelte';
	import WhenStep from './WhenStep.svelte';
	import ZoneStep from './ZoneStep.svelte';

	interface Props {
		siteId: string;
		choices: WizardChoices;
	}

	let { siteId, choices }: Props = $props();

	const clock = new Clock(60_000);
	const today = $derived(romeWallClock(clock.now).date);
	const link = $derived(session.current && libraryLinks.of(session.current.account));

	const site = query(() => session.current && siteQuery(session.current, siteId));
	const libraries = query(() => session.current && librariesQuery(session.current));
	const info = query(() => session.current && reservationInfoQuery(session.current, siteId));
	const agreements = query(() => session.current && agreementsQuery(session.current, siteId));
	const filters = query(() => {
		const scope = session.current;
		const zone = choices.zone;
		return scope && zone !== undefined && resourceTypeFiltersQuery(scope, siteId, zone, today);
	});

	/* The site's own page names it (kept a day); the listing stands in until it loads. */
	const card = $derived(
		libraries.data
			? bookableLibraries(libraries.data).find((item) => item.id === siteId)
			: undefined
	);
	const library = $derived({
		name: site.data?.primaryName.trim() || card?.name || m.library_title(),
		place: site.data ? cleanPlace(site.data.secondaryName) : (card?.place ?? null)
	});
	const placeKnown = $derived(site.data !== undefined || site.error !== undefined);
	const zone = $derived(info.data?.types.map(toZone).find((item) => item.id === choices.zone));

	/* Until the zone's constraints load, the URL's day and duration stand; then they must be offered. */
	const days = $derived(filters.data ? openDays(filters.data.openDays) : []);
	const durations = $derived(filters.data?.durations.filter((value) => value > 0) ?? []);
	const day = $derived.by((): IsoDate | undefined => {
		if (!filters.data) return choices.day;
		return choices.day && days.includes(choices.day) ? choices.day : days[0];
	});
	const duration = $derived.by((): number | undefined => {
		if (!filters.data) return choices.duration;
		return choices.duration && durations.includes(choices.duration)
			? choices.duration
			: durations[0];
	});

	const seats = query(() => {
		const scope = session.current;
		const zoneId = choices.zone;
		if (!scope || zoneId === undefined || !day || !duration) return undefined;
		return availableResourcesQuery(scope, siteId, day, zoneId, duration);
	});

	const step = $derived(stepOf(choices));
	const stepIndex = $derived(WIZARD_STEPS.indexOf(step));

	/* A seat or confirm URL whose day or duration the zone no longer offers: yesterday's, say. */
	const slotGone = $derived(
		filters.data !== undefined &&
			(step === 'seat' || step === 'confirm') &&
			(day !== choices.day || duration !== choices.duration)
	);

	/* Every seat is listed whatever its hours; the one picked must still be free at the start. */
	const allSeats = $derived(seats.data ? toSeats(seats.data) : undefined);
	const chosen = $derived(
		step === 'confirm' ? allSeats?.find((item) => item.id === choices.seat) : undefined
	);
	const chosenFree = $derived(
		chosen !== undefined && choices.start !== undefined && isFreeAt(chosen, choices.start)
	);

	const slot = $derived(
		day && choices.start !== undefined && duration
			? slotRange(day, choices.start, duration)
			: undefined
	);

	const stepItems = $derived.by((): StepItem[] => [
		{
			label: m.library_step_zone(),
			value: zone ? (zone.name ?? m.library_zone_fallback()) : undefined,
			href: bookHref(siteId)
		},
		{
			label: m.library_step_when(),
			value: slot,
			href: bookHref(siteId, choicesBefore(choices, 'when'))
		},
		{
			label: m.library_step_seat(),
			value: chosen?.short,
			href: bookHref(siteId, choicesBefore(choices, 'seat'))
		},
		{ label: m.library_step_confirm() }
	]);

	/* A booking made here keeps its outcome on this URL; reloaded, the booking itself is shown. */
	const bookedMarker = $derived(page.url.searchParams.get('booked'));
	let booked = $state<{ marker: string; booking: Booked }>();
	const done = $derived(
		booked !== undefined && bookedMarker === booked.marker ? booked.booking : undefined
	);

	$effect(() => {
		if (bookedMarker === null || done) return;
		const id = Number(bookedMarker);
		void goto(Number.isSafeInteger(id) && id > 0 ? reservationHref(id) : homeHref(), {
			replace: true
		});
	});

	function replaceChoices(next: WizardChoices) {
		void goto(bookHref(siteId, next), { replace: true, reset: false });
	}

	const startHref = (start: number): ResolvedPathname =>
		bookHref(siteId, { zone: choices.zone, day, duration, start });
	const seatHref = (seatId: number): ResolvedPathname =>
		bookHref(siteId, { zone: choices.zone, day, duration, start: choices.start, seat: seatId });

	function markBooked(reservation: number | null) {
		if (!zone || !chosen || !day || choices.start === undefined || !duration) return;
		booked = {
			marker: String(reservation ?? 'yes'),
			booking: {
				id: reservation,
				library: library.name,
				place: library.place,
				zone: zone.name ?? m.library_zone_fallback(),
				seat: chosen.short,
				start: siteDate(day, choices.start),
				end: siteDate(day, choices.start + duration),
				duration
			}
		};
		void goto(bookedHref(siteId, choices, reservation), { replace: true, reset: false });
	}
</script>

<svelte:head>
	<title>{m.library_book_seat()} · {library.name} · {m.app_name()}</title>
</svelte:head>

<Page width="dashboard">
	<PageHeader
		eyebrow={library.name}
		title={m.library_book_seat()}
		subtitle={library.place ?? undefined}
		back={{ href: libraryHref(siteId), label: library.name }}
		loading={!placeKnown && library.place === null}
	/>

	{#if done}
		<BookedPanel booked={done} />
	{:else if bookedMarker === null}
		<Stepper steps={stepItems} current={stepIndex} />

		{#if link && !link.auth && step !== 'confirm'}
			<Banner tone="info" icon={IconEnvelope} message={m.library_verify_email_body()}>
				{#snippet actions()}
					<Button
						size="sm"
						href={loginHref(`${page.url.pathname}${page.url.search}` as ResolvedPathname)}
					>
						{m.library_verify()}
					</Button>
				{/snippet}
			</Banner>
		{/if}

		{#if slotGone}
			<EmptyState
				size="page"
				icon={IconCalendarX}
				title={m.library_slot_gone()}
				body={m.library_slot_gone_body()}
			>
				{#snippet action()}
					<Button href={bookHref(siteId, { zone: choices.zone })}>{m.library_step_when()}</Button>
				{/snippet}
			</EmptyState>
		{:else if step === 'zone'}
			<ZoneStep {siteId} {info} />
		{:else if step === 'when'}
			<WhenStep
				{filters}
				{seats}
				{days}
				{durations}
				{day}
				{duration}
				start={choices.start}
				{today}
				onday={(next) => replaceChoices({ zone: choices.zone, day: next, duration })}
				onduration={(next) => replaceChoices({ zone: choices.zone, day, duration: next })}
				{startHref}
			/>
		{:else if step === 'seat' && choices.start !== undefined}
			<SeatStep
				{seats}
				start={choices.start}
				color={zone?.color ?? 'other'}
				{seatHref}
				whenHref={bookHref(siteId, choicesBefore(choices, 'when'))}
			/>
		{:else if step === 'confirm'}
			{#if seats.error && !seats.data}
				<ErrorState
					title={m.library_times_loading_failed()}
					message={errorMessage(seats.error)}
					onretry={seats.refresh}
					retrying={seats.refreshing}
				/>
			{:else if !allSeats || !info.data || !link || !day || !duration || choices.start === undefined}
				<div class="confirm-skeleton" aria-busy="true">
					<Skeleton variant="block" height="292px" radius="var(--r-lg)" />
					<Skeleton variant="block" height="150px" radius="var(--r-lg)" />
				</div>
			{:else if !zone || !chosen}
				<EmptyState
					size="page"
					icon={IconArmchair}
					title={m.library_seat_missing()}
					body={m.library_seat_missing_body()}
				>
					{#snippet action()}
						<Button href={bookHref(siteId, zone ? choicesBefore(choices, 'seat') : {})}>
							{m.library_choose_seat()}
						</Button>
					{/snippet}
				</EmptyState>
			{:else}
				<ConfirmStep
					{siteId}
					{library}
					{zone}
					seat={chosen}
					free={chosenFree}
					{day}
					start={choices.start}
					{duration}
					{link}
					agreements={agreements.data ?? []}
					seatsHref={bookHref(siteId, choicesBefore(choices, 'seat'))}
					onbooked={markBooked}
					onfailed={() => void seats.refresh()}
				/>
			{/if}
		{/if}
	{/if}
</Page>

<style>
	.confirm-skeleton {
		display: grid;
		gap: var(--sp-5);
		max-inline-size: var(--w-reading);
	}
</style>
