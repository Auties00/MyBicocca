<script lang="ts">
	import { untrack, type Snippet } from 'svelte';
	import { beforeNavigate, goto } from '$app/navigation';
	import IconArrowRight from '~icons/ph/arrow-right';
	import IconCalendarCheck from '~icons/ph/calendar-check';
	import IconCalendarX from '~icons/ph/calendar-x-duotone';
	import IconInfo from '~icons/ph/info';
	import IconStorefront from '~icons/ph/storefront-duotone';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { toDataError } from '#lib/data/errors.ts';
	import { network } from '#lib/data/network.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { monthOf } from '#lib/format/days.ts';
	import { sanitizeHtml } from '#lib/format/html.ts';
	import { romeDay } from '#lib/format/rome.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import Prose from '#lib/ui/Prose.svelte';
	import Select from '#lib/ui/Select.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import {
		BookingRefused,
		bookAppointment,
		failureOutcome,
		isRefusal,
		type BookingStep,
		type FailureOutcome
	} from '../booking.ts';
	import { BookingFlow } from '../booking.svelte.ts';
	import { displayName, durationMinutes, serviceSection } from '../directory.ts';
	import { formProblems, primaryEmail } from '../form.ts';
	import { pickerDay, sectionLabels, shortPickerDay, slotRange } from '../format.ts';
	import { bookingHref, directoryHref, reservationHref } from '../links.ts';
	import { outcomes } from '../outcomes.svelte.ts';
	import { saveReservation } from '../reservations.ts';
	import { useServices } from '../services.svelte.ts';
	import BookingDialog, { type BookingFailure } from './BookingDialog.svelte';
	import BookingForm from './BookingForm.svelte';
	import DayTime from './DayTime.svelte';
	import MonthCalendar from './MonthCalendar.svelte';
	import Recap from './Recap.svelte';
	import TimeSlots from './TimeSlots.svelte';
	import { reducedMotion } from '#lib/ui/media.ts';

	interface Props {
		serviceId: number;
		/** The URL asks for the form step, where the steps don't fit side by side. */
		details: boolean;
	}

	let { serviceId, details }: Props = $props();

	/*
	 * Widths of the page's pane. Below the first, the day and the form are two steps; from it, the
	 * day and time sit beside the form (a 1280 px laptop beside the Services list has about 640);
	 * from the second, the calendar, the times and the form are three columns.
	 */
	const TWO_COLUMNS = 600;
	const THREE_COLUMNS = 1100;

	// svelte-ignore state_referenced_locally
	const flow = new BookingFlow(serviceId);
	const today = romeDay(Date.now());

	const ids = $props.id();
	let width = $state(0);
	let checked = $state(false);
	/* Book was pressed with no time picked: said until one is. */
	let timeAsked = $state(false);
	let confirming = $state(false);
	let busy = $state(false);

	const columns = $derived(width >= THREE_COLUMNS ? 3 : width >= TWO_COLUMNS ? 2 : 1);
	const offering = $derived(flow.offering);
	/* The directory, usually cached by the page that led here, names the desk while it loads. */
	const services = useServices();
	const listed = $derived(services.data?.find((item) => item.id === serviceId));
	const known = $derived(
		offering ? { name: offering.serviceName, group: offering.serviceGroup } : listed
	);
	const step = $derived(columns === 1 && details && flow.slot ? 'details' : 'slot');
	const section = $derived(known ? serviceSection(known) : undefined);
	const ready = $derived(flow.offerings !== undefined && flow.fields !== undefined);
	const setupError = $derived(
		(flow.areas.status === 'error' && flow.areas.error) ||
			(flow.form.status === 'error' && flow.form.error) ||
			undefined
	);
	const gate = new LoadingGate(() => !ready && !setupError);
	/* Until the desk is known, bars hold the place of its name, section and details. */
	const settled = $derived(ready || setupError !== undefined);
	const title = $derived(
		known ? displayName(known.name) : settled ? m.appointments_booking_heading() : ''
	);
	const eyebrow = $derived(section && sectionLabels[section]());
	const subtitle = $derived(
		offering &&
			`${m.appointments_minutes({ minutes: durationMinutes(offering.durationSeconds) })} · ${
				offering.virtual ? m.appointments_video_call() : offering.areaName
			}`
	);
	const back = $derived(
		step === 'details'
			? { href: bookingHref(serviceId), label: m.appointments_step_slot() }
			: {
					href: directoryHref(section),
					label: section ? sectionLabels[section]() : m.appointments_directory_title()
				}
	);
	const problems = $derived(flow.fields ? formProblems(flow.fields, flow.values) : []);
	const email = $derived(flow.fields ? primaryEmail(flow.fields, flow.values) : null);
	/* The directory's description shows while the desk's own loads, so the picker stays put. */
	const description = $derived.by(() => {
		const html = offering ? offering.description : listed?.description;
		return html ? sanitizeHtml(html) : '';
	});
	const areaOptions = $derived(
		(flow.offerings ?? []).map((item) => ({ value: String(item.areaId), label: item.areaName }))
	);
	const monthError = $derived(
		(flow.firstMonth.status === 'error' && flow.firstMonth.error) ||
			(flow.monthSchedule.status === 'error' && flow.monthSchedule.error) ||
			undefined
	);
	const dayError = $derived(
		flow.daySchedule.status === 'error' && flow.daySchedule.error
			? flow.daySchedule.error
			: undefined
	);

	/*
	 * The form step with no time picked goes back to picking one: on a reload, or when the time
	 * was lost while booking. The focus stays put, in the failure's dialog if it is open.
	 */
	$effect(() => {
		if (width === 0) return;
		if (details && columns === 1 && flow.slots !== undefined && !flow.slot && !busy) {
			void goto(bookingHref(serviceId), { replace: true, reset: false });
		}
	});

	/*
	 * On the form step, the first required field still blank takes the focus once the page has
	 * settled, as on Android; a form the account filled in leaves it where it is.
	 */
	$effect(() => {
		if (step !== 'details') return;
		const first = untrack(() => problems).find(
			({ field }) => field.kind !== 'consent' && field.kind !== 'select'
		);
		if (!first) return;
		const timer = setTimeout(() => focusField(first.field.code), 250);
		return () => clearTimeout(timer);
	});

	beforeNavigate((navigation) => {
		if (busy) navigation.cancel();
	});

	function guard(event: BeforeUnloadEvent) {
		if (busy) event.preventDefault();
	}

	function measure(node: HTMLElement) {
		const update = () => (width = node.clientWidth);
		update();
		const observer = new ResizeObserver(update);
		observer.observe(node);
		return () => observer.disconnect();
	}

	/*
	 * In one column, what takes the focus is brought to the middle, clear of the booking bar under
	 * the times and of the keyboard a field opens.
	 */
	function reveal(element: HTMLElement | null | undefined) {
		if (!element) return;
		if (columns > 1) {
			element.focus();
			return;
		}
		element.focus({ preventScroll: true });
		const still = reducedMotion.current;
		element.scrollIntoView({ block: 'center', behavior: still ? 'instant' : 'smooth' });
	}

	function focusField(code: string) {
		reveal(document.querySelector<HTMLElement>(`[data-field="${CSS.escape(code)}"]`));
	}

	/* The picked time, else the first free one, else the calendar's day: where a time is chosen. */
	function focusTimes() {
		reveal(
			document.querySelector<HTMLElement>('#main [role="radiogroup"] input:checked') ??
				document.querySelector<HTMLElement>('#main [role="radiogroup"] input:not(:disabled)') ??
				document.querySelector<HTMLElement>('#main table [data-date][tabindex="0"]')
		);
	}

	function retryMonth() {
		if (flow.firstMonth.error) void flow.firstMonth.refresh();
		else void flow.monthSchedule.refresh();
	}

	/*
	 * The dialog hands the focus back to the Book button; when the time was lost meanwhile, it
	 * goes on to the times, where another has to be picked, once the dialog has let it go.
	 */
	function closed() {
		if (!flow.slot) requestAnimationFrame(focusTimes);
	}

	async function change() {
		await goto(bookingHref(serviceId));
		focusTimes();
	}

	function proceed() {
		if (flow.slot) void goto(bookingHref(serviceId, 'details'));
		else focusTimes();
	}

	function submit(event: SubmitEvent) {
		event.preventDefault();
		checked = true;
		timeAsked = !flow.slot;
		if (!flow.slot) {
			focusTimes();
			return;
		}
		const first = problems[0];
		if (first) {
			focusField(first.field.code);
			return;
		}
		if (network.online) confirming = true;
	}

	async function run(onStep: (step: BookingStep) => void) {
		const scope = session.current;
		const date = flow.day;
		const slot = flow.slot;
		if (!date || !slot) throw new BookingRefused('slot');
		if (!scope || !offering || !flow.fields) throw new Error('The booking page is not set up');
		busy = true;
		let code: string;
		try {
			const reservation = await bookAppointment(
				scope.api.planning,
				{
					service: { id: serviceId, name: offering.serviceName, group: offering.serviceGroup },
					offering,
					date,
					slot,
					email: email ?? '',
					values: flow.values
				},
				onStep
			);
			await saveReservation(scope, reservation);
			outcomes.markBooked(scope, reservation);
			code = reservation.code;
		} catch (error) {
			// The time may be the cause: the dialog tells by the day as it stands now.
			await flow.daySchedule.refresh();
			throw error;
		} finally {
			busy = false;
		}
		await goto(reservationHref(code), { replace: true });
	}

	function reason(error: unknown): string {
		if (error instanceof BookingRefused) {
			switch (error.reason) {
				case 'slot':
					return m.appointments_booking_taken();
				case 'captcha':
					return m.appointments_booking_captcha();
				case 'email':
					return m.appointments_booking_email();
				case 'no_code':
					return m.appointments_booking_no_code();
			}
		}
		if (isRefusal(error)) return m.appointments_booking_rejected();
		const failure = toDataError(error);
		if (failure.kind === 'auth') return m.common_error_unavailable();
		return errorMessage(failure);
	}

	const notes: Record<NonNullable<FailureOutcome['note']>, () => string> = {
		released: m.appointments_booking_released,
		held: m.appointments_booking_still_held,
		pick_another: m.appointments_booking_pick_another
	};

	/* Told by the day as it stands after the failure: see failureOutcome. */
	function describe(error: unknown): BookingFailure {
		const outcome = failureOutcome(error, flow.slot !== undefined);
		return {
			message: outcome.taken ? m.appointments_booking_taken() : reason(outcome.cause),
			note: outcome.note ? notes[outcome.note]() : undefined,
			retry: outcome.retry
		};
	}

	const summary = $derived({
		office: offering ? displayName(offering.serviceName) : '',
		day: flow.day ? pickerDay(flow.day) : '',
		time: flow.day && flow.slot ? slotRange(flow.day, flow.slot) : '',
		where: offering
			? offering.virtual
				? m.appointments_video_call()
				: [offering.areaName, offering.areaAddress].filter(Boolean).join(' · ')
			: '',
		email: email ?? ''
	});
</script>

<svelte:head>
	<title>
		{known ? displayName(known.name) : m.appointments_booking_heading()} · {m.app_name()}
	</title>
</svelte:head>

<svelte:window onbeforeunload={guard} />

<!--
	Until the month to show is known, the calendar is the current month's, busy: the same card as
	the one that follows, so nothing moves when it comes.
-->
{#snippet calendar()}
	<MonthCalendar
		month={flow.month ?? monthOf(today)}
		available={flow.days}
		selected={flow.day}
		{today}
		canGoBack={flow.canGoBack}
		canGoForward={flow.canGoForward}
		error={monthError ? m.appointments_month_failed() : undefined}
		onretry={retryMonth}
		onmonth={(month) => flow.showMonth(month)}
		onselect={(date) => flow.selectDay(date)}
	/>
{/snippet}

{#snippet times()}
	{#if !monthError && (flow.days === undefined || flow.days.length > 0)}
		<TimeSlots
			date={flow.day}
			slots={flow.day ? flow.slots : undefined}
			selected={flow.slot}
			error={dayError ? m.appointments_day_failed() : undefined}
			onretry={() => void flow.daySchedule.refresh()}
			onselect={(slot) => flow.selectSlot(slot)}
		/>
	{/if}
{/snippet}

{#snippet about()}
	{#if description && step === 'slot'}
		<details class={['about', { wide: columns > 1 }]}>
			<summary><IconInfo aria-hidden="true" />{m.appointments_about()}</summary>
			<Prose size="sm">
				<!-- eslint-disable-next-line svelte/no-at-html-tags -- sanitized by sanitizeHtml -->
				{@html description}
			</Prose>
		</details>
	{/if}
{/snippet}

<!-- Wide: the day and the time, and beside them the form (or its skeleton). -->
{#snippet columnsLayout(aside: Snippet)}
	{#if columns === 3}
		<div class="layout columns-3">
			{@render calendar()}
			{@render times()}
			<div class="aside">{@render aside()}</div>
		</div>
	{:else}
		<div class="layout columns-2">
			<div class="stack">
				{@render calendar()}
				{@render times()}
			</div>
			<div class="aside">{@render aside()}</div>
		</div>
	{/if}
{/snippet}

{#snippet formSkeleton()}
	<div class="details" aria-hidden="true">
		<Skeleton height="68px" radius="var(--r-lg)" />
		<Skeleton variant="text" width="40%" />
		{#each { length: 4 }, index (index)}
			<div class="field-skeleton">
				<Skeleton variant="text" width="35%" />
				<Skeleton height="44px" radius="var(--r-md)" />
			</div>
		{/each}
		<Skeleton height="64px" radius="var(--r-md)" />
		<Skeleton height="44px" radius="var(--r-pill)" />
	</div>
{/snippet}

{#snippet sideForm()}
	{@render form(true)}
{/snippet}

{#snippet form(withRecap: boolean)}
	{#if offering && flow.fields}
		<form class="details" novalidate onsubmit={submit} aria-labelledby="{ids}-details">
			{#if withRecap}<Recap date={flow.day} slot={flow.slot} {offering} />{/if}
			<h2 class="form-title" id="{ids}-details">{m.appointments_form_heading()}</h2>
			{#if Object.keys(flow.prefilled).length > 0}
				<p class="form-note">{m.appointments_form_prefilled()}</p>
			{/if}
			<BookingForm
				fields={flow.fields}
				values={flow.values}
				{checked}
				disabled={busy}
				onchange={(code, value) => flow.setValue(code, value)}
			/>
			{#if timeAsked && !flow.slot}
				<p class="form-problem" role="alert">{m.appointments_form_no_time()}</p>
			{:else if checked && problems.length > 0}
				<p class="form-problem" role="alert">{m.appointments_form_invalid()}</p>
			{/if}
			{#if !network.online}
				<Banner tone="warning" message={m.appointments_offline_book()} />
			{/if}
			<Button
				variant="solid"
				type="submit"
				icon={IconCalendarCheck}
				full
				disabled={!network.online}
			>
				{m.appointments_book_submit()}
			</Button>
		</form>
	{/if}
{/snippet}

<Page width="dashboard">
	<PageHeader {eyebrow} {title} {subtitle} {back} loading={!settled} />

	<div class="body" {@attach measure}>
		{#if setupError && !ready}
			<ErrorState
				title={m.appointments_setup_failed()}
				message={errorMessage(setupError)}
				onretry={() => flow.retrySetup()}
				retrying={flow.areas.refreshing || flow.form.refreshing}
			/>
		{:else if !ready}
			{#if gate.visible}
				{@render about()}
				<div aria-busy="true">
					{#if columns === 1}
						<div class="single pick">
							{@render calendar()}
							{@render times()}
						</div>
					{:else}
						{@render columnsLayout(formSkeleton)}
					{/if}
				</div>
			{/if}
		{:else if !offering}
			<EmptyState
				size="page"
				icon={IconStorefront}
				title={m.appointments_desk_unavailable_title()}
				body={m.appointments_desk_unavailable_body()}
			>
				{#snippet action()}
					<Button href={directoryHref()}>{m.appointments_other_office()}</Button>
				{/snippet}
			</EmptyState>
		{:else if flow.noAvailability}
			<EmptyState
				size="page"
				icon={IconCalendarX}
				title={m.appointments_no_dates_title()}
				body={m.appointments_no_dates_body({ count: offering.horizonDays })}
			>
				{#snippet action()}
					<Button href={directoryHref(section)}>{m.appointments_other_office()}</Button>
				{/snippet}
			</EmptyState>
		{:else}
			{@render about()}

			{#if areaOptions.length > 1 && step === 'slot'}
				<div class="area">
					<Select
						label={m.appointments_area_label()}
						options={areaOptions}
						value={String(offering.areaId)}
						onchange={(value) => flow.selectArea(Number(value))}
					/>
				</div>
			{/if}

			{#if flow.slotGone}
				<Banner tone="warning" message={m.appointments_slot_gone()} role="status" />
			{/if}

			{#if columns > 1}
				{@render columnsLayout(sideForm)}
			{:else if step === 'details'}
				<div class="single">
					<Recap date={flow.day} slot={flow.slot} {offering} onchange={change} />
					{@render form(false)}
				</div>
			{:else}
				<div class="single pick">
					{@render calendar()}
					{@render times()}
					<div class="bar">
						<div class="bar-text">
							<p class="bar-step">{m.appointments_step({ step: 1 })}</p>
							<p class="bar-when" aria-live="polite">
								{#if flow.day && flow.slot}
									<DayTime day={shortPickerDay(flow.day)} time={slotRange(flow.day, flow.slot)} />
								{:else}
									{flow.day ? m.appointments_recap_time() : m.appointments_recap_none()}
								{/if}
							</p>
						</div>
						<Button variant="solid" trailingIcon={IconArrowRight} onclick={proceed}>
							{m.common_continue()}
						</Button>
					</div>
				</div>
			{/if}
		{/if}
	</div>
</Page>

<BookingDialog bind:open={confirming} {summary} {run} {describe} onclosed={closed} />

<style>
	.body {
		display: grid;
		gap: var(--sp-4);
		align-content: start;
	}

	.layout {
		display: grid;
		align-items: start;
		gap: var(--sp-5);
	}

	/* The calendar's column stops growing where its days would float apart; the form takes the rest. */
	.columns-2 {
		grid-template-columns: minmax(280px, 400px) minmax(300px, 1fr);
	}

	.columns-3 {
		grid-template-columns: minmax(300px, 1.05fr) minmax(260px, 0.95fr) minmax(340px, 1.1fr);
	}

	.stack,
	.single {
		display: grid;
		gap: var(--sp-4);
	}

	.single {
		max-inline-size: var(--w-form);
	}

	.single.pick {
		max-inline-size: 440px;
	}

	/* The form stays in view while the day and time are picked beside it. */
	.aside {
		position: sticky;
		inset-block-start: calc(var(--shell-inset-top) + var(--sp-3));
	}

	.details {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-5);
		border-radius: var(--r-xl);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.field-skeleton {
		display: grid;
		gap: 6px;
	}

	.single .details {
		padding: 0;
		background: none;
		box-shadow: none;
	}

	.form-title {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.form-note {
		margin-block-start: calc(-1 * var(--sp-3));
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}

	.form-problem {
		color: var(--danger);
		font: var(--type-meta);
	}

	.about {
		max-inline-size: 440px;
		border-radius: var(--r-lg);
		background: var(--surface-sunken);
	}

	.about.wide {
		max-inline-size: none;
	}

	.about summary {
		display: flex;
		align-items: center;
		gap: var(--sp-2);
		min-block-size: var(--hit-min);
		padding: var(--sp-2) var(--sp-4);
		border-radius: var(--r-lg);
		font: var(--type-ui-strong);
		cursor: pointer;
	}

	.about summary :global(svg) {
		inline-size: 18px;
		block-size: 18px;
		color: var(--accent-text);
	}

	.about summary:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.about :global(.prose) {
		padding: 0 var(--sp-4) var(--sp-4);
	}

	.area {
		max-inline-size: 420px;
	}

	/* Above the floating tab bar of phones. */
	.bar {
		position: sticky;
		inset-block-end: calc(var(--shell-inset-bottom) + var(--sp-3));
		z-index: var(--z-sticky);
		display: flex;
		align-items: center;
		gap: var(--sp-3);
		padding: 10px 10px 10px var(--sp-4);
		border-radius: var(--r-xl);
		background: var(--surface-raised);
		box-shadow: var(--elev-3), var(--inner-light);
	}

	.bar-text {
		flex: 1;
		min-inline-size: 0;
	}

	.bar-step {
		color: var(--text-muted);
		font: var(--type-label);
	}

	.bar-when {
		font: var(--type-ui-strong);
		font-variant-numeric: tabular-nums;
	}
</style>
