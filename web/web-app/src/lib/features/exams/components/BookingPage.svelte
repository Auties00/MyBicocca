<script lang="ts">
	import IconCalendarPlus from '~icons/ph/calendar-plus';
	import IconCalendarSlash from '~icons/ph/calendar-slash';
	import IconCalendarX from '~icons/ph/calendar-x-duotone';
	import IconCertificate from '~icons/ph/certificate';
	import IconReceipt from '~icons/ph/receipt';
	import { now } from '#lib/data/clock.svelte.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { toDataError } from '#lib/data/errors.ts';
	import { network } from '#lib/data/network.svelte.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { formatDate, formatDateTime } from '#lib/format/dates.ts';
	import { toIsoDate } from '#lib/format/days.ts';
	import { personName } from '#lib/format/names.ts';
	import { formatCredits } from '#lib/format/numbers.ts';
	import { romeInstant } from '#lib/format/rome.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import Facts, { type Fact } from '#lib/ui/Facts.svelte';
	import { openPdf, saveCalendarEvent } from '#lib/ui/files.ts';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import { callKindLabel, locationLine, sentenceWhen } from '../format.ts';
	import { callLabel, canCancel, examLocation, isUpcoming, toBooking } from '../model.ts';
	import { outcomes } from '../outcomes.svelte.ts';
	import { bookingsQuery, enrolledCountQuery } from '../queries.ts';
	import { cancelBooking, writeFailure } from '../writes.ts';
	import CancelledPanel from './CancelledPanel.svelte';
	import PastOutcome from './PastOutcome.svelte';
	import Ticket from './Ticket.svelte';
	import TicketSkeleton from './TicketSkeleton.svelte';

	interface Props {
		slug: string;
	}

	let { slug }: Props = $props();

	const bookings = query(() => session.current && bookingsQuery(session.current));

	let confirming = $state(false);
	let cancelling = $state(false);
	let downloading = $state<'receipt' | 'presence'>();
	let fileError = $state<string>();

	const booking = $derived.by(() => {
		for (const row of bookings.data ?? []) {
			const item = toBooking(row);
			if (item?.slug === slug) return item;
		}
		return undefined;
	});
	const cancelled = $derived(
		session.current && !booking ? outcomes.cancelled(session.current, slug) : undefined
	);
	const upcoming = $derived(booking !== undefined && isUpcoming(booking, now.current));

	/* The queue's length is a slow request of its own, made only where the position shows. */
	const total = query(() => {
		const scope = session.current;
		return (
			scope &&
			booking &&
			upcoming &&
			(booking.position ?? 0) > 0 &&
			enrolledCountQuery(scope, booking.key)
		);
	});

	const gate = new LoadingGate(() => bookings.status === 'pending');
	const updatedAt = $derived(bookings.updatedAt ? new Date(bookings.updatedAt) : undefined);

	const shown = $derived(booking ?? cancelled);
	const pending = $derived(!shown && bookings.data === undefined && bookings.status === 'pending');
	const title = $derived.by(() => {
		if (shown) return shown.title || m.exams_call_unknown();
		return pending ? '' : m.exams_booking_label();
	});
	const subtitle = $derived(shown ? callKindLabel(shown.kind, shown.callType) : undefined);
	const where = $derived(
		booking ? locationLine(examLocation(booking.room, booking.building)) : null
	);
	const callName = $derived(booking ? callLabel(booking.callDescription, booking.title) : null);

	const facts = $derived.by((): Fact[] => {
		if (!booking) return [];
		const items: Fact[] = [];
		if (booking.bookedAt) {
			items.push({ label: m.exams_fact_booked_on(), value: formatDateTime(booking.bookedAt) });
		}
		if (!upcoming && where) items.push({ label: m.exams_fact_where(), value: where });
		if (booking.credits !== null && booking.credits > 0) {
			items.push({ label: m.exams_fact_credits(), value: formatCredits(booking.credits) });
		}
		if (!upcoming && callName) items.push({ label: m.exams_fact_call(), value: callName });
		if (booking.note) items.push({ label: m.exams_fact_note(), value: booking.note });
		return items;
	});

	const cancellable = $derived(booking !== undefined && canCancel(booking, now.current));
	/* Still worded while the dialog closes over a booking the list has already dropped. */
	const cancelMessage = $derived.by(() => {
		if (!shown) return '';
		const course = shown.title || m.exams_call_unknown();
		const date = shown.at ? sentenceWhen(shown.at, shown.timed) : m.exams_time_tbc();
		return shown.cancellableUntil
			? m.exams_cancel_message({
					course,
					date,
					deadline: formatDate(shown.cancellableUntil, 'full')
				})
			: m.exams_cancel_message_open({ course, date });
	});

	const student = $derived({
		name: personName(session.current?.account.displayName ?? ''),
		number: session.current?.career.studentNumber ?? ''
	});

	async function cancel() {
		const scope = session.current;
		if (!scope || !booking) return;
		cancelling = true;
		try {
			await cancelBooking(scope, booking);
		} finally {
			cancelling = false;
		}
	}

	async function open(kind: 'receipt' | 'presence') {
		const scope = session.current;
		const studentId = booking?.studentId;
		if (!scope || !booking || studentId === null || studentId === undefined || downloading) return;
		const { courseOfStudyId, activityId, callId } = booking.key;
		downloading = kind;
		fileError = undefined;
		const calendar = scope.api.esse3.examsCalendar;
		try {
			await openPdf(
				() =>
					kind === 'receipt'
						? calendar.getBookingStatino(courseOfStudyId, activityId, callId, studentId)
						: calendar.getPresenceCertificate(courseOfStudyId, activityId, callId, studentId),
				kind === 'receipt'
					? `statino_prenotazione_${activityId}_${callId}.pdf`
					: `attestato_presenza_${activityId}_${callId}.pdf`,
				m.exams_pdf_waiting()
			);
		} catch (error) {
			const failure = toDataError(error);
			fileError =
				failure.kind === 'rejected' || failure.kind === 'not_found'
					? kind === 'receipt'
						? m.exams_receipt_failed()
						: m.exams_presence_failed()
					: errorMessage(failure);
		} finally {
			downloading = undefined;
		}
	}

	function addToCalendar() {
		if (!booking?.at) return;
		const course = booking.title || m.exams_call_unknown();
		saveCalendarEvent(
			{
				uid: `exam-${booking.slug}@mybicocca`,
				title: course,
				start: booking.timed ? romeInstant(booking.at) : toIsoDate(booking.at),
				location: where,
				description: [callName, booking.timed ? m.exams_ics_duration_unknown() : null]
					.filter(Boolean)
					.join('\n')
			},
			course
		);
	}

	function guard(event: BeforeUnloadEvent) {
		if (cancelling) event.preventDefault();
	}
</script>

<svelte:head>
	<title>{title || m.exams_booking_label()} · {m.exams_title()} · {m.app_name()}</title>
</svelte:head>

<svelte:window onbeforeunload={guard} />
<Page width="dashboard">
	<PageHeader
		{title}
		{subtitle}
		back={{ href: shellHref('services/exams'), label: m.exams_title() }}
		loading={pending && { eyebrow: false }}
		sans
	/>

	{#if bookings.error && bookings.data && booking}
		<ErrorState
			variant="stale"
			message={errorMessage(bookings.error)}
			{updatedAt}
			onretry={bookings.refresh}
			retrying={bookings.refreshing}
		/>
	{/if}

	{#if cancelled && !booking}
		<div class="narrow"><CancelledPanel booking={cancelled} now={now.current} /></div>
	{:else if bookings.status === 'error' && bookings.error && !bookings.data}
		<ErrorState
			title={m.exams_bookings_failed()}
			message={errorMessage(bookings.error)}
			onretry={bookings.refresh}
			retrying={bookings.refreshing}
		/>
	{:else if bookings.data === undefined || gate.visible}
		<div class="loading narrow" aria-busy="true" aria-label={m.exams_booking_label()}>
			{#if gate.visible}<TicketSkeleton />{/if}
		</div>
	{:else if !booking}
		<EmptyState
			size="page"
			icon={IconCalendarX}
			title={m.exams_not_found_title()}
			body={m.exams_not_found_body()}
		>
			{#snippet action()}
				<Button href={shellHref('services/exams')}>{m.exams_back_to_bookings()}</Button>
			{/snippet}
		</EmptyState>
	{:else if upcoming}
		<div class="layout">
			<div class="column">
				<Ticket {booking} now={now.current} {student} total={total.data} />

				{#if fileError}
					<Banner
						tone="danger"
						message={fileError}
						role="alert"
						ondismiss={() => (fileError = undefined)}
					/>
				{/if}

				<div class="actions">
					<Button
						variant="solid"
						icon={IconReceipt}
						loading={downloading === 'receipt'}
						disabled={booking.studentId === null || !network.online}
						onclick={() => open('receipt')}
					>
						{m.exams_receipt()}
					</Button>
					{#if booking.at}
						<Button icon={IconCalendarPlus} onclick={addToCalendar}>
							{m.exams_add_calendar()}
						</Button>
					{/if}
				</div>
			</div>
			<div class="column">
				{#if facts.length > 0}
					<Facts items={facts} label={m.exams_booking_details()} />
				{/if}

				<section class="cancel" aria-label={m.exams_cancel_heading()}>
					<div class="cancel-text">
						<h2 class="cancel-title">{m.exams_cancel_heading()}</h2>
						<p class="cancel-body">
							{#if cancellable}
								{booking.cancellableUntil
									? m.exams_cancel_until({ date: formatDate(booking.cancellableUntil, 'full') })
									: m.exams_cancel_while_open()}
							{:else if booking.cancellableUntil}
								{m.exams_cancel_closed({ date: formatDate(booking.cancellableUntil, 'full') })}
							{:else}
								{m.exams_cancel_unavailable()}
							{/if}
						</p>
						{#if !network.online && cancellable}
							<p class="offline">{m.exams_offline_write()}</p>
						{/if}
					</div>
					{#if cancellable}
						<Button
							variant="outline"
							icon={IconCalendarSlash}
							disabled={!network.online}
							onclick={() => (confirming = true)}
						>
							{m.exams_cancel_action()}
						</Button>
					{/if}
				</section>
			</div>
		</div>
	{:else}
		<div class="layout">
			<div class="column">
				<PastOutcome {booking} />

				{#if fileError}
					<Banner
						tone="danger"
						message={fileError}
						role="alert"
						ondismiss={() => (fileError = undefined)}
					/>
				{/if}

				{#if booking.published && booking.studentId !== null}
					<div class="actions">
						<Button
							icon={IconCertificate}
							loading={downloading === 'presence'}
							disabled={!network.online}
							onclick={() => open('presence')}
						>
							{m.exams_presence()}
						</Button>
					</div>
				{/if}
			</div>
			<div class="column">
				{#if facts.length > 0}
					<Facts items={facts} label={m.exams_booking_details()} />
				{/if}
			</div>
		</div>
	{/if}
</Page>

<ConfirmDialog
	bind:open={confirming}
	title={m.exams_cancel_title()}
	message={cancelMessage}
	confirmLabel={m.exams_cancel_confirm()}
	cancelLabel={m.exams_keep()}
	tone="danger"
	onconfirm={cancel}
	errorMessage={writeFailure}
/>

<style>
	.narrow,
	.layout {
		max-inline-size: var(--w-reading);
	}

	.layout,
	.column {
		display: grid;
		align-content: start;
		gap: var(--page-gap);
	}

	/* Wide: the day's ticket on the left, the booking's details and cancellation beside it. */
	@container page (width >= 1000px) {
		.layout {
			grid-template-columns: minmax(0, 1.4fr) minmax(0, 1fr);
			column-gap: var(--sp-8);
			max-inline-size: none;
		}
	}

	.loading {
		min-block-size: 420px;
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
		flex: 1 1 260px;
	}

	.cancel-title {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.cancel-body,
	.offline {
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}

	/* Printed for the exam day: the ticket and the booking's details, without the controls. */
	@media print {
		.actions,
		.cancel {
			display: none;
		}

		.layout {
			max-inline-size: none;
		}
	}
</style>
