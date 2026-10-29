<script lang="ts">
	import IconCalendarPlus from '~icons/ph/calendar-plus';
	import IconCalendarSlash from '~icons/ph/calendar-slash';
	import IconCalendarX from '~icons/ph/calendar-x-duotone';
	import IconCheckCircle from '~icons/ph/check-circle';
	import IconEnvelope from '~icons/ph/envelope-simple';
	import IconQrCode from '~icons/ph/qr-code';
	import { Clock } from '#lib/data/clock.svelte.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { network } from '#lib/data/network.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { canScanQr } from '#lib/qr/camera.ts';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import Outcome from '#lib/ui/Outcome.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import { saveCalendarEvent } from '#lib/ui/files.ts';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { dayAndTime, durationLabel, whenLong } from '../format.ts';
	import { homeHref, loginHref } from '../links.ts';
	import { linkedReservations } from '../live.svelte.ts';
	import {
		CHECK_IN_LEAD_MINUTES,
		checkInOpensAt,
		checkInPhase,
		toReservation,
		type Reservation
	} from '../model.ts';
	import { cancelBooking, writeFailure } from '../writes.ts';
	import CheckInDialog from './CheckInDialog.svelte';

	interface Props {
		id: number;
	}

	let { id }: Props = $props();

	const clock = new Clock(30_000);
	const own = linkedReservations();
	const reservations = own.reservations;
	const link = $derived(own.link);

	const found = $derived.by(() => {
		for (const wire of reservations.data ?? []) {
			if (wire.reservationId === id) return toReservation(wire) ?? undefined;
		}
		return undefined;
	});

	/* Kept after the list drops it, so the outcome reads in place. */
	let cancelled = $state<Reservation>();
	/* Recorded here, before the list reloads with it. */
	let checkedInHere = $state(false);
	let confirming = $state(false);
	let checkInDialog = $state<ReturnType<typeof CheckInDialog>>();
	let cancelling = $state(false);
	let canScan = $state(false);
	let checkInStatus = $state<HTMLElement>();

	$effect(() => {
		void canScanQr().then((supported) => (canScan = supported));
	});

	const reservation = $derived(cancelled ? undefined : found);
	const shown = $derived(reservation ?? cancelled);
	const now = $derived(clock.now);
	const phase = $derived(reservation ? checkInPhase(reservation, now) : 'ended');
	const checkedIn = $derived(checkedInHere || reservation?.checkedIn === true);
	/*
	 * Affluences' `validated` is read as attendance, which no live booking has confirmed yet: while
	 * the window is open, checking in again stays possible rather than locked away on a misreading.
	 */
	const checkInAgain = $derived(checkedIn && !checkedInHere && phase === 'open');
	const ongoing = $derived(
		reservation !== undefined && reservation.startsAt <= now && now < reservation.endsAt
	);
	const duration = $derived(
		reservation ? Math.round((reservation.endsAt - reservation.startsAt) / 60_000) : 0
	);
	const gate = new LoadingGate(() => reservations.data === undefined && !reservations.error);

	const hint = $derived.by(() => {
		if (!reservation) return '';
		if (checkedIn) return m.library_checked_in_body();
		if (phase === 'early') {
			return m.library_verification_opens({
				when: dayAndTime(checkInOpensAt(reservation), now),
				minutes: CHECK_IN_LEAD_MINUTES
			});
		}
		if (phase === 'ended') return m.library_booking_complete();
		return canScan ? m.library_qr_or_code() : m.library_code_only();
	});

	async function cancel() {
		const scope = session.current;
		const token = reservation?.cancellationToken;
		if (!scope || !reservation || !token) return;
		cancelling = true;
		try {
			await cancelBooking(scope, token);
			cancelled = reservation;
		} finally {
			cancelling = false;
		}
	}

	function checkedInNow() {
		checkedInHere = true;
		// The button that opened the dialog is now disabled: focus the news instead.
		requestAnimationFrame(() => checkInStatus?.focus());
	}

	function addToCalendar() {
		if (!reservation) return;
		saveCalendarEvent(
			{
				uid: `library-${reservation.id}@mybicocca`,
				title: `${reservation.library} · ${reservation.seatShort}`,
				start: reservation.startsAt,
				end: reservation.endsAt,
				location: reservation.place
					? `${reservation.library}, ${reservation.place}`
					: reservation.library,
				description: reservation.code ? `${m.library_fact_code()}: ${reservation.code}` : null
			},
			m.library_ics_filename()
		);
	}

	function guard(event: BeforeUnloadEvent) {
		if (cancelling) event.preventDefault();
	}
</script>

<svelte:head>
	<title>{shown?.library || m.library_reservation()} · {m.library_title()} · {m.app_name()}</title>
</svelte:head>

<svelte:window onbeforeunload={guard} />

<Page width="dashboard">
	<PageHeader
		title={shown?.library || m.library_reservation()}
		subtitle={shown?.place ?? undefined}
		back={{ href: homeHref(), label: m.library_title() }}
	/>

	{#if cancelled}
		<Outcome
			icon={IconCalendarX}
			tone="neutral"
			title={m.library_cancelled_title()}
			body={m.library_cancelled_body()}
		>
			{#snippet actions()}
				<Button variant="solid" href={homeHref()}>{m.library_go_to_bookings()}</Button>
			{/snippet}
		</Outcome>
	{:else if link && !link.auth}
		<EmptyState
			size="page"
			icon={IconCalendarX}
			title={m.library_verify_email()}
			body={m.library_verify_email_body()}
		>
			{#snippet action()}
				<Button variant="solid" href={loginHref()}>{m.library_verify()}</Button>
			{/snippet}
		</EmptyState>
	{:else if reservations.error && !reservations.data}
		<ErrorState
			title={m.library_bookings_failed()}
			message={errorMessage(reservations.error)}
			onretry={reservations.refresh}
			retrying={reservations.refreshing}
		/>
	{:else if reservations.data === undefined}
		<div class="layout" aria-busy="true" aria-label={m.library_reservation()}>
			{#if gate.visible}
				<Skeleton variant="block" height="360px" radius="var(--r-xl)" />
				<Skeleton variant="block" height="120px" radius="var(--r-lg)" />
			{/if}
		</div>
	{:else if !reservation}
		<EmptyState
			size="page"
			icon={IconCalendarX}
			title={m.library_reservation_not_found_title()}
			body={m.library_reservation_not_found_body()}
		>
			{#snippet action()}
				<Button href={homeHref()}>{m.library_go_to_bookings()}</Button>
			{/snippet}
		</EmptyState>
	{:else}
		{#if reservations.error}
			<ErrorState
				variant="stale"
				message={errorMessage(reservations.error)}
				updatedAt={reservations.updatedAt ? new Date(reservations.updatedAt) : undefined}
				onretry={reservations.refresh}
				retrying={reservations.refreshing}
			/>
		{/if}

		<div class="layout">
			<div class="column">
				{#if reservation.state === 'awaiting'}
					<Banner
						tone="info"
						icon={IconEnvelope}
						title={m.library_awaiting()}
						message={m.library_awaiting_body()}
					/>
				{/if}
				<section class="pass" aria-label={m.library_reservation()}>
					<div class="pass-top">
						<div class="seat">
							<span class="eyebrow">{m.library_fact_seat()}</span>
							<span class="seat-name" translate="no">{reservation.seatShort}</span>
							{#if reservation.outlet}
								<span class="outlet">{m.library_power_outlet()}</span>
							{/if}
						</div>
						{#if ongoing}<span class="tag">{m.library_ongoing()}</span>{/if}
					</div>
					<p class="when">
						{whenLong(reservation.start, reservation.end)} · {durationLabel(duration)}
					</p>
					{#if reservation.code || reservation.note}
						<dl class="codes">
							{#if reservation.code}
								<div class="code">
									<dt class="eyebrow">{m.library_fact_code()}</dt>
									<dd class="code-value" translate="no">{reservation.code}</dd>
								</div>
							{/if}
							{#if reservation.note}
								<div class="note">
									<dt class="eyebrow">{m.library_fact_note()}</dt>
									<dd class="note-value">{reservation.note}</dd>
								</div>
							{/if}
						</dl>
					{/if}
					<div class={['check-in', { done: checkedIn }]}>
						<p class="hint" role="status" tabindex="-1" bind:this={checkInStatus}>
							{#if checkedIn}<IconCheckCircle aria-hidden="true" />{/if}
							<span>
								{#if checkedIn}<strong>{m.library_checked_in()}.</strong>{/if}
								{hint}
							</span>
						</p>
						{#if !checkedIn}
							<Button
								variant="solid"
								size="lg"
								icon={IconQrCode}
								disabled={phase !== 'open'}
								onclick={() => checkInDialog?.show()}
							>
								{m.library_check_in()}
							</Button>
						{:else if checkInAgain}
							<Button variant="ghost" icon={IconQrCode} onclick={() => checkInDialog?.show()}>
								{m.library_check_in_again()}
							</Button>
						{/if}
					</div>
				</section>

				<div class="actions">
					<Button icon={IconCalendarPlus} onclick={addToCalendar}>{m.library_add_calendar()}</Button
					>
				</div>
			</div>

			<div class="column">
				<section class="cancel" aria-labelledby="library-cancel">
					<div class="cancel-text">
						<h2 class="cancel-title" id="library-cancel">{m.library_cancel_heading()}</h2>
						<p class="cancel-body">
							{reservation.cancellationToken
								? m.library_cancel_body()
								: m.library_cancel_unavailable()}
						</p>
						{#if !network.online && reservation.cancellationToken}
							<p class="cancel-body">{m.library_offline_write()}</p>
						{/if}
					</div>
					{#if reservation.cancellationToken}
						<Button
							variant="outline"
							icon={IconCalendarSlash}
							disabled={!network.online}
							onclick={() => (confirming = true)}
						>
							{m.library_cancel()}
						</Button>
					{/if}
				</section>
			</div>
		</div>
	{/if}
</Page>

{#if link}
	<CheckInDialog bind:this={checkInDialog} {link} {canScan} ondone={checkedInNow} />
{/if}

<ConfirmDialog
	bind:open={confirming}
	title={m.library_cancel_confirmation()}
	message={shown
		? m.library_confirm_cancel({
				seat: shown.seatShort,
				library: shown.library,
				when: whenLong(shown.start, shown.end, true)
			})
		: ''}
	confirmLabel={m.library_cancel()}
	cancelLabel={m.library_keep()}
	tone="danger"
	onconfirm={cancel}
	errorMessage={writeFailure(m.library_cancellation_failed)}
/>

<style>
	.layout,
	.column {
		display: grid;
		align-content: start;
		gap: var(--sp-5);
		min-inline-size: 0;
	}

	.layout {
		max-inline-size: var(--w-reading);
	}

	/* Wide: the pass to show at the desk on the left, the cancellation beside it. */
	@container page (width >= 900px) {
		.layout {
			grid-template-columns: minmax(0, 6fr) minmax(0, 5fr);
			column-gap: var(--sp-7);
			max-inline-size: none;
		}
	}

	.pass {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-5);
		border-radius: var(--r-xl);
		background: var(--container);
		color: var(--on-container);
		box-shadow: var(--inner-light);
	}

	.pass-top {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: var(--sp-3);
	}

	.seat,
	.code,
	.note {
		display: grid;
		align-content: start;
		gap: 2px;
	}

	.codes {
		display: flex;
		flex-wrap: wrap;
		gap: var(--sp-3) var(--sp-7);
		margin: 0;
		padding-block-start: var(--sp-3);
		border-block-start: 1px dashed color-mix(in oklch, currentColor 30%, transparent);
	}

	dd {
		margin: 0;
	}

	.note-value {
		font: var(--type-body-strong);
		padding-block-start: 6px;
	}

	.eyebrow {
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
		opacity: 0.75;
	}

	/* The seat, read from across the room; the UI face, as the page title is the serif line. */
	.seat-name {
		font: 650 2.75rem / 1.05 var(--font-ui);
		letter-spacing: -0.02em;
		font-variant-numeric: tabular-nums;
	}

	.outlet {
		font: var(--type-ui);
		opacity: 0.85;
	}

	.tag {
		flex-shrink: 0;
		padding: 4px 10px;
		border-radius: var(--r-pill);
		background: var(--accent);
		color: var(--on-accent);
		font: 600 0.75rem / 1.33 var(--font-ui);
	}

	.when {
		font: var(--type-body-strong);
		font-variant-numeric: tabular-nums;
	}

	.code-value {
		font: 600 1.75rem / 1.2 var(--font-ui);
		font-variant-numeric: tabular-nums slashed-zero;
		letter-spacing: 0.08em;
	}

	.check-in {
		display: grid;
		gap: var(--sp-3);
		padding: var(--sp-4);
		border-radius: var(--r-lg);
		background: var(--surface);
		color: var(--text);
	}

	.hint {
		display: flex;
		align-items: flex-start;
		gap: 8px;
		color: var(--text-muted);
		font: var(--type-ui);
		text-wrap: pretty;
		outline: none;
	}

	.done .hint {
		color: var(--text);
	}

	.hint :global(svg) {
		flex-shrink: 0;
		inline-size: 20px;
		block-size: 20px;
		color: var(--success);
	}

	.hint strong {
		margin-inline-end: 4px;
		color: var(--success);
	}

	.actions {
		display: flex;
		flex-wrap: wrap;
		gap: var(--sp-2);
	}

	.cancel {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: space-between;
		gap: var(--sp-3) var(--sp-5);
		padding: var(--sp-4) var(--sp-5);
		border: 1px solid var(--hairline);
		border-radius: var(--r-lg);
	}

	.cancel-text {
		display: grid;
		gap: 2px;
		flex: 1 1 240px;
	}

	.cancel-title {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.cancel-body {
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}
</style>
