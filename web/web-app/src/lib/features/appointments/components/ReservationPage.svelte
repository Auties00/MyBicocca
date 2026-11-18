<script lang="ts">
	import IconCalendarPlus from '~icons/ph/calendar-plus';
	import IconCheck from '~icons/ph/check-bold';
	import IconCalendarSlash from '~icons/ph/calendar-slash';
	import IconCalendarX from '~icons/ph/calendar-x-duotone';
	import IconFilePdf from '~icons/ph/file-pdf';
	import { Clock } from '#lib/data/clock.svelte.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { toDataError } from '#lib/data/errors.ts';
	import { network } from '#lib/data/network.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import { openPdf, saveCalendarEvent } from '#lib/ui/files.ts';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Outcome from '#lib/ui/Outcome.svelte';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import Spinner from '#lib/ui/Spinner.svelte';
	import type { ExternalHref } from '#lib/ui/types.ts';
	import { cancelAppointment } from '../booking.ts';
	import { displayName, serviceSection } from '../directory.ts';
	import { longWhen, placeLabel, sectionLabels } from '../format.ts';
	import { appointmentsHref } from '../links.ts';
	import { outcomes } from '../outcomes.svelte.ts';
	import { forgetReservation } from '../reservations.ts';
	import { useReservations } from '../useReservations.svelte.ts';
	import CancelledPanel from './CancelledPanel.svelte';
	import Pass from './Pass.svelte';
	import PassSkeleton from './PassSkeleton.svelte';
	import QrViewer from './QrViewer.svelte';

	interface Props {
		code: string;
	}

	let { code }: Props = $props();

	const reservations = useReservations();

	/* The pass follows the clock while it is open: Join comes forward, an appointment ends. */
	const clock = new Clock(30_000);
	let confirming = $state(false);
	let cancelling = $state(false);
	let downloading = $state(false);
	let fileError = $state<string>();
	let viewing = $state(false);

	const reservation = $derived(reservations.data?.find((item) => item.code === code));
	const cancelled = $derived(
		session.current && !reservation ? outcomes.cancelled(session.current, code) : undefined
	);
	const booked = $derived(
		session.current && reservation ? outcomes.booked(session.current, code) : undefined
	);
	const shown = $derived(reservation ?? cancelled);
	const ended = $derived(reservation !== undefined && reservation.end < clock.now);
	const section = $derived(
		shown ? serviceSection({ name: shown.serviceName, group: shown.serviceGroup }) : undefined
	);
	const title = $derived(shown ? displayName(shown.serviceName) : m.appointments_title());
	const gate = new LoadingGate(() => reservations.data === undefined);
	const joinHref = $derived(
		reservation?.webConferenceUrl && /^https?:\/\//i.test(reservation.webConferenceUrl)
			? (reservation.webConferenceUrl as ExternalHref)
			: undefined
	);
	const ids = $props.id();
	const cancelMessage = $derived(
		shown ? m.appointments_cancel_message({ service: title, date: longWhen(shown) }) : ''
	);

	/* The confirmation is told once: leaving the page settles it. */
	$effect(() => {
		const scope = session.current;
		const current = code;
		return () => {
			if (scope) outcomes.settle(scope, current);
		};
	});

	async function cancel() {
		const scope = session.current;
		if (!scope || !reservation) return;
		const target = reservation;
		cancelling = true;
		try {
			await cancelAppointment(scope.api.planning, target);
			outcomes.markCancelled(scope, target);
			await forgetReservation(scope, target.code);
		} finally {
			cancelling = false;
		}
	}

	async function openReminder() {
		const scope = session.current;
		const entryId = reservation?.entryId;
		if (!scope || !reservation || entryId === null || entryId === undefined || downloading) return;
		downloading = true;
		fileError = undefined;
		try {
			await openPdf(
				() => scope.api.planning.reservations.getReservationPdf(entryId),
				`Appuntamento-${reservation.code}.pdf`,
				m.appointments_pdf_waiting()
			);
		} catch (error) {
			const failure = toDataError(error);
			fileError =
				failure.kind === 'not_found' || failure.kind === 'rejected'
					? m.appointments_pdf_failed()
					: errorMessage(failure);
		} finally {
			downloading = false;
		}
	}

	function addToCalendar() {
		if (!reservation) return;
		saveCalendarEvent(
			{
				uid: `appointment-${reservation.code}@mybicocca`,
				title: reservation.serviceName,
				start: reservation.start,
				end: reservation.end,
				location: reservation.webConferenceUrl ?? placeLabel(reservation),
				description: `${m.appointments_code_label()}: ${reservation.code}`,
				url: joinHref ?? null
			},
			reservation.serviceName
		);
	}

	function guard(event: BeforeUnloadEvent) {
		if (cancelling) event.preventDefault();
	}
</script>

{#snippet waiting()}
	<Spinner size={18} label={m.appointments_pdf_waiting()} />
{/snippet}

<svelte:head>
	<title>{shown ? `${title} · ` : ''}{m.appointments_title()} · {m.app_name()}</title>
</svelte:head>

<svelte:window onbeforeunload={guard} />

<Page width="dashboard">
	<PageHeader
		{title}
		subtitle={section ? sectionLabels[section]() : undefined}
		back={{ href: appointmentsHref(), label: m.appointments_title() }}
		sans
	/>

	{#if cancelled && !reservation}
		<CancelledPanel reservation={cancelled} />
	{:else if reservations.data === undefined}
		<div class="layout" aria-busy="true" aria-label={m.appointments_detail_label()}>
			<div class="column">
				{#if gate.visible}<PassSkeleton />{/if}
			</div>
		</div>
	{:else if !reservation}
		<EmptyState
			size="page"
			icon={IconCalendarX}
			title={m.appointments_not_found_title()}
			body={m.appointments_not_found_body()}
		>
			{#snippet action()}
				<Button href={appointmentsHref()}>{m.appointments_back_to_list()}</Button>
			{/snippet}
		</EmptyState>
	{:else}
		<div class="layout">
			<div class="column">
				{#if booked}
					<Outcome
						icon={IconCheck}
						title={m.appointments_confirmed_title()}
						body={reservation.webConferenceUrl === null
							? m.appointments_confirmed_body()
							: m.appointments_confirmed_video()}
					>
						{#snippet actions()}
							<Button
								variant="outline"
								href={appointmentsHref()}
								onclick={() => session.current && outcomes.settle(session.current, code)}
							>
								{m.appointments_done()}
							</Button>
						{/snippet}
					</Outcome>
				{/if}
				<Pass {reservation} now={clock.now} {joinHref} onexpand={() => (viewing = true)} />
			</div>

			<div class="column">
				{#if reservation.entryId !== null || !ended}
					<ListGroup label={m.appointments_detail_label()}>
						{#if reservation.entryId !== null}
							<ListRow
								title={m.appointments_pdf()}
								description={m.appointments_pdf_hint()}
								icon={IconFilePdf}
								disabled={!network.online}
								onclick={openReminder}
								trailing={downloading ? waiting : undefined}
							/>
						{/if}
						{#if !ended}
							<ListRow
								title={m.appointments_add_calendar()}
								description={m.appointments_calendar_hint()}
								icon={IconCalendarPlus}
								onclick={addToCalendar}
							/>
						{/if}
					</ListGroup>
				{/if}

				{#if fileError}
					<Banner
						tone="danger"
						message={fileError}
						role="alert"
						ondismiss={() => (fileError = undefined)}
					/>
				{/if}

				{#if ended}
					<Banner tone="neutral" message={m.appointments_ended_note()} />
				{:else}
					<section class="cancel" aria-labelledby="{ids}-cancel">
						<div class="cancel-text">
							<h2 class="cancel-title" id="{ids}-cancel">{m.appointments_cancel_heading()}</h2>
							<p class="cancel-body">{m.appointments_cancel_hint()}</p>
							{#if !network.online}
								<p class="cancel-body">{m.appointments_cancel_offline()}</p>
							{/if}
						</div>
						<Button
							variant="outline"
							icon={IconCalendarSlash}
							disabled={!network.online}
							onclick={() => (confirming = true)}
						>
							{m.appointments_cancel_action()}
						</Button>
					</section>
				{/if}
			</div>
		</div>
	{/if}
</Page>

{#if reservation?.qrCode}
	<QrViewer bind:open={viewing} qrCode={reservation.qrCode} code={reservation.code} {title} />
{/if}

<ConfirmDialog
	bind:open={confirming}
	title={m.appointments_cancel_title()}
	message={cancelMessage}
	confirmLabel={m.appointments_cancel_confirm()}
	cancelLabel={m.appointments_keep()}
	tone="danger"
	onconfirm={cancel}
	errorMessage={(error) => errorMessage(toDataError(error))}
/>

<style>
	.layout,
	.column {
		display: grid;
		align-content: start;
		gap: var(--page-gap);
	}

	.layout {
		max-inline-size: var(--w-reading);
	}

	/* Wide: the pass on the left, what to do with it beside. */
	@container page (width >= 960px) {
		.layout {
			grid-template-columns: minmax(0, 1.25fr) minmax(0, 1fr);
			column-gap: var(--sp-8);
			max-inline-size: none;
		}
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
